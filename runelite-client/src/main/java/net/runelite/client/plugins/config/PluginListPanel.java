/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.config;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.CustomNotifier;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ExternalPluginsChanged;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.externalplugins.ExternalPluginManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.MultiplexingPluginPanel;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
class PluginListPanel extends PluginPanel
{
	private static final String RUNELITE_GROUP_NAME = RuneLiteConfig.class.getAnnotation(ConfigGroup.class).value();
	private static final String PINNED_PLUGINS_CONFIG_KEY = "pinnedPlugins";
	private static final String CUSTOM_NOTIFICATIONS_CONFIG_KEY = "enableCustomNotifier";

	private final ConfigManager configManager;
	private final PluginManager pluginManager;
	private final ScheduledExecutorService executorService;
	private final Provider<ConfigPanel> configPanelProvider;
	private final Provider<NotifierPanel> notifierPanelProvider;
	private final List<PluginConfigurationDescriptor> fakePlugins = new ArrayList<>();
	private final RuneLiteConfig runeLiteConfig;

	@Getter
	private final ExternalPluginManager externalPluginManager;

	@Getter
	private final MultiplexingPluginPanel muxer;

	private final IconTextField searchBar;
	private final JScrollPane scrollPane;
	private final FixedWidthPanel mainPanel;
	private List<PluginListItem> pluginList;

	@Inject
	public PluginListPanel(
		ConfigManager configManager,
		PluginManager pluginManager,
		ExternalPluginManager externalPluginManager,
		ScheduledExecutorService executorService,
		EventBus eventBus,
		Provider<ConfigPanel> configPanelProvider,
		Provider<NotifierPanel> notifierPanelProvider,
		Provider<PluginHubPanel> pluginHubPanelProvider,
		RuneLiteConfig runeLiteConfig)
	{
		super(false);

		this.configManager = configManager;
		this.pluginManager = pluginManager;
		this.externalPluginManager = externalPluginManager;
		this.executorService = executorService;
		this.configPanelProvider = configPanelProvider;
		this.notifierPanelProvider = notifierPanelProvider;
		this.runeLiteConfig = runeLiteConfig;

		muxer = new MultiplexingPluginPanel(this)
		{
			@Override
			protected void onAdd(PluginPanel p)
			{
				eventBus.register(p);
			}

			@Override
			protected void onRemove(PluginPanel p)
			{
				eventBus.unregister(p);
			}
		};

		searchBar = new IconTextField();
		searchBar.setIcon(IconTextField.Icon.SEARCH);
		searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
		searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
		searchBar.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				onSearchBarChanged();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				onSearchBarChanged();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				onSearchBarChanged();
			}
		});

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel topPanel = new JPanel();
		topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		topPanel.setLayout(new BorderLayout(0, BORDER_OFFSET));
		topPanel.add(searchBar, BorderLayout.CENTER);
		add(topPanel, BorderLayout.NORTH);

		mainPanel = new FixedWidthPanel();
		mainPanel.setBorder(new EmptyBorder(8, 10, 10, 10));
		mainPanel.setLayout(new DynamicGridLayout(0, 1, 0, 5));
		mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JButton externalPluginButton = new JButton("Plugin Hub");
		externalPluginButton.setBorder(new EmptyBorder(5, 5, 5, 5));
		externalPluginButton.setLayout(new BorderLayout(0, BORDER_OFFSET));
		externalPluginButton.addActionListener(l -> muxer.pushState(pluginHubPanelProvider.get()));

		JPanel northPanel = new FixedWidthPanel();
		northPanel.setLayout(new BorderLayout());
		northPanel.add(mainPanel, BorderLayout.NORTH);
		northPanel.add(externalPluginButton, BorderLayout.SOUTH);

		scrollPane = new JScrollPane(northPanel);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);
	}

	void rebuildPluginList()
	{
		final List<String> pinnedPlugins = getPinnedPluginNames();

		// populate pluginList with all non-hidden plugins
		pluginList = Stream.concat(
			fakePlugins.stream(),
			pluginManager.getPlugins().stream()
				.filter(plugin -> !plugin.getClass().getAnnotation(PluginDescriptor.class).hidden())
				.map(plugin ->
				{
					PluginDescriptor descriptor = plugin.getClass().getAnnotation(PluginDescriptor.class);
					Config config = pluginManager.getPluginConfigProxy(plugin);
					ConfigDescriptor configDescriptor = config == null ? null : configManager.getConfigDescriptor(config);
					CustomNotifier notifier = config == null ? null :
						config.getClass().getInterfaces()[0].getAnnotation(CustomNotifier.class);

					return new PluginConfigurationDescriptor(
						descriptor.name(),
						descriptor.description(),
						descriptor.tags(),
						plugin,
						config,
						configDescriptor,
						notifier);
				})
		).map(desc ->
		{
			PluginListItem listItem = new PluginListItem(this, desc, runeLiteConfig.enableCustomNotifier());
			listItem.setPinned(pinnedPlugins.contains(desc.getName()));
			return listItem;
		}).collect(Collectors.toList());

		pluginList.sort(Comparator.comparing(p -> p.getPluginConfig().getName()));
		mainPanel.removeAll();
		refresh();
	}

	void addFakePlugin(PluginConfigurationDescriptor... descriptor)
	{
		Collections.addAll(fakePlugins, descriptor);
	}

	void refresh()
	{
		// update enabled / disabled status of all items
		pluginList.forEach(listItem ->
		{
			final Plugin plugin = listItem.getPluginConfig().getPlugin();
			if (plugin != null)
			{
				listItem.setPluginEnabled(pluginManager.isPluginEnabled(plugin));
			}
		});

		int scrollBarPosition = scrollPane.getVerticalScrollBar().getValue();

		onSearchBarChanged();
		searchBar.requestFocusInWindow();
		validate();

		scrollPane.getVerticalScrollBar().setValue(scrollBarPosition);
	}

	void openWithFilter(String filter)
	{
		searchBar.setText(filter);
		onSearchBarChanged();
		muxer.pushState(this);
	}

	private void onSearchBarChanged()
	{
		final String text = searchBar.getText();

		pluginList.forEach(mainPanel::remove);

		showMatchingPlugins(true, text);
		showMatchingPlugins(false, text);

		revalidate();
	}

	private void showMatchingPlugins(boolean pinned, String text)
	{
		if (text.isEmpty())
		{
			pluginList.stream().filter(item -> pinned == item.isPinned()).forEach(mainPanel::add);
			return;
		}

		final String[] searchTerms = text.toLowerCase().split(" ");
		pluginList.forEach(listItem ->
		{
			if (pinned == listItem.isPinned() && listItem.matchesSearchTerms(searchTerms))
			{
				mainPanel.add(listItem);
			}
		});
	}

	void openConfigurationPanel(String configGroup)
	{
		for (PluginListItem pluginListItem : pluginList)
		{
			if (pluginListItem.getPluginConfig().getName().equals(configGroup))
			{
				openConfigurationPanel(pluginListItem.getPluginConfig());
				break;
			}
		}
	}

	void openConfigurationPanel(Plugin plugin)
	{
		for (PluginListItem pluginListItem : pluginList)
		{
			if (pluginListItem.getPluginConfig().getPlugin() == plugin)
			{
				openConfigurationPanel(pluginListItem.getPluginConfig());
				break;
			}
		}
	}

	void openConfigurationPanel(PluginConfigurationDescriptor plugin)
	{
		ConfigPanel panel = configPanelProvider.get();
		panel.init(plugin);
		muxer.pushState(panel);
	}

	void openCustomNotifierPanel(PluginConfigurationDescriptor plugin)
	{
		NotifierPanel panel = notifierPanelProvider.get();
		panel.init(plugin);
		muxer.pushState(panel);
	}

	void startPlugin(Plugin plugin)
	{
		executorService.submit(() ->
		{
			pluginManager.setPluginEnabled(plugin, true);

			try
			{
				pluginManager.startPlugin(plugin);
			}
			catch (PluginInstantiationException ex)
			{
				log.warn("Error when starting plugin {}", plugin.getClass().getSimpleName(), ex);
			}
		});
	}

	void stopPlugin(Plugin plugin)
	{
		executorService.submit(() ->
		{
			pluginManager.setPluginEnabled(plugin, false);

			try
			{
				pluginManager.stopPlugin(plugin);
			}
			catch (PluginInstantiationException ex)
			{
				log.warn("Error when stopping plugin {}", plugin.getClass().getSimpleName(), ex);
			}
		});
	}

	private List<String> getPinnedPluginNames()
	{
		final String config = configManager.getConfiguration(RUNELITE_GROUP_NAME, PINNED_PLUGINS_CONFIG_KEY);

		if (config == null)
		{
			return Collections.emptyList();
		}

		return Text.fromCSV(config);
	}

	void savePinnedPlugins()
	{
		final String value = pluginList.stream()
			.filter(PluginListItem::isPinned)
			.map(p -> p.getPluginConfig().getName())
			.collect(Collectors.joining(","));

		configManager.setConfiguration(RUNELITE_GROUP_NAME, PINNED_PLUGINS_CONFIG_KEY, value);
	}

	@Subscribe
	public void onPluginChanged(PluginChanged event)
	{
		SwingUtilities.invokeLater(this::refresh);
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PANEL_WIDTH + SCROLLBAR_WIDTH, super.getPreferredSize().height);
	}

	@Override
	public void onActivate()
	{
		super.onActivate();

		if (searchBar.getParent() != null)
		{
			searchBar.requestFocusInWindow();
		}
	}

	@Subscribe
	private void onExternalPluginsChanged(ExternalPluginsChanged ev)
	{
		SwingUtilities.invokeLater(this::rebuildPluginList);
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged ev)
	{
		if (ev.getGroup().equals(RUNELITE_GROUP_NAME) && ev.getKey().equals(CUSTOM_NOTIFICATIONS_CONFIG_KEY))
		{
			SwingUtilities.invokeLater(this::rebuildPluginList);
		}
	}
}
