/*
 * Copyright (c) 2020, TheStonedTurtle <https://github.com/TheStonedTurtle>
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

import com.google.common.collect.ComparisonChain;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigItemDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.CustomNotifier;
import net.runelite.client.config.Range;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.ComboBoxListRenderer;
import net.runelite.client.util.SwingUtil;
import net.runelite.client.util.Text;

@Slf4j
public class NotifierPanel extends PluginPanel
{
	@Inject
	private PluginListPanel pluginList;

	@Inject
	private ConfigManager configManager;

	private final JLabel title;
	private final FixedWidthPanel mainPanel;

	private PluginConfigurationDescriptor pluginConfig = null;

	public NotifierPanel()
	{
		super(false);

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel topPanel = new JPanel();
		topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		topPanel.setLayout(new BorderLayout(0, BORDER_OFFSET));
		add(topPanel, BorderLayout.NORTH);

		mainPanel = new FixedWidthPanel();
		mainPanel.setBorder(new EmptyBorder(8, 10, 10, 10));
		mainPanel.setLayout(new DynamicGridLayout(0, 1, 0, 5));
		mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel northPanel = new FixedWidthPanel();
		northPanel.setLayout(new BorderLayout());
		northPanel.add(mainPanel, BorderLayout.NORTH);

		JScrollPane scrollPane = new JScrollPane(northPanel);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);

		JButton topPanelBackButton = new JButton(ConfigPanel.BACK_ICON);
		topPanelBackButton.setRolloverIcon(ConfigPanel.BACK_ICON_HOVER);
		SwingUtil.removeButtonDecorations(topPanelBackButton);
		topPanelBackButton.setPreferredSize(new Dimension(22, 0));
		topPanelBackButton.setBorder(new EmptyBorder(0, 0, 0, 5));
		topPanelBackButton.addActionListener(e -> pluginList.getMuxer().popState());
		topPanelBackButton.setToolTipText("Back");
		topPanel.add(topPanelBackButton, BorderLayout.WEST);

		title = new JLabel();
		title.setForeground(Color.WHITE);

		topPanel.add(title);
	}

	void init(PluginConfigurationDescriptor pluginConfig)
	{
		assert this.pluginConfig == null;
		this.pluginConfig = pluginConfig;

		String name = pluginConfig.getName();
		title.setText(name);
		title.setForeground(Color.WHITE);
		title.setToolTipText("<html>" + name + ":<br>" + pluginConfig.getDescription() + "</html>");

		rebuild();
	}

	private void rebuild()
	{
		final CustomNotifier notifier = pluginConfig.getCustomNotifier();
		final ConfigDescriptor cd = pluginConfig.getConfigDescriptor();
		if (notifier == null || cd == null)
		{
			log.warn("Trying to rebuild a notifier panel for a pluginConfig without the config or custom notifier annotation. {}", pluginConfig);
			return;
		}

		mainPanel.removeAll();

		final Class<?> inter = notifier.getClass().getInterfaces()[0];
		// Grab ConfigItemDescriptors here instead of using ConfigManager as we need to only look at declared methods
		final List<ConfigItemDescriptor> descriptors = Arrays.stream(inter.getDeclaredMethods())
			.filter(m -> m.getParameterCount() == 0)
			.map(m -> new ConfigItemDescriptor(
				m.getDeclaredAnnotation(ConfigItem.class),
				m.getReturnType(),
				m.getDeclaredAnnotation(Range.class),
				m.getDeclaredAnnotation(Alpha.class)
			))
			.sorted((a, b) -> ComparisonChain.start()
				.compare(a.getItem().position(), b.getItem().position())
				.compare(a.getItem().name(), b.getItem().name())
				.result())
			.collect(Collectors.toList());

		for (ConfigItemDescriptor cid : descriptors)
		{
			if (cid.getItem().hidden())
			{
				continue;
			}

			JPanel item = new JPanel();
			item.setLayout(new BorderLayout());
			item.setMinimumSize(new Dimension(PANEL_WIDTH, 0));
			String name = cid.getItem().name();
			JLabel configEntryName = new JLabel(name);
			configEntryName.setForeground(Color.WHITE);
			configEntryName.setToolTipText("<html>" + name + ":<br>" + cid.getItem().description() + "</html>");
			item.add(configEntryName, BorderLayout.CENTER);

			// Copied from ConfigPanel
			if (cid.getType() == boolean.class)
			{
				JCheckBox checkbox = new JCheckBox();
				checkbox.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
				checkbox.setSelected(Boolean.parseBoolean(configManager.getConfiguration(cd.getGroup().value(), cid.getItem().keyName())));
				checkbox.addActionListener(ae -> changeConfiguration(checkbox, cd, cid));

				item.add(checkbox, BorderLayout.EAST);
			}

			// Copied from ConfigPanel
			if (cid.getType().isEnum())
			{
				Class<? extends Enum> type = (Class<? extends Enum>) cid.getType();
				JComboBox box = new JComboBox(type.getEnumConstants());
				box.setPreferredSize(new Dimension(box.getPreferredSize().width, 25));
				box.setRenderer(new ComboBoxListRenderer());
				box.setForeground(Color.WHITE);
				box.setFocusable(false);
				box.setPrototypeDisplayValue("XXXXXXXX"); //sorry but this is the way to keep the size of the combobox in check.
				try
				{
					Enum selectedItem = Enum.valueOf(type, configManager.getConfiguration(cd.getGroup().value(), cid.getItem().keyName()));
					box.setSelectedItem(selectedItem);
					box.setToolTipText(Text.titleCase(selectedItem));
				}
				catch (IllegalArgumentException ex)
				{
					log.debug("invalid seleced item", ex);
				}
				box.addItemListener(e ->
				{
					if (e.getStateChange() == ItemEvent.SELECTED)
					{
						changeConfiguration(box, cd, cid);
						box.setToolTipText(Text.titleCase((Enum) box.getSelectedItem()));
					}
				});
				item.add(box, BorderLayout.EAST);
			}

			mainPanel.add(item);
		}

		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener((e) ->
		{
			final int result = JOptionPane.showOptionDialog(resetButton, "Are you sure you want to reset this plugin's notification configuration?",
				"Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
				null, new String[]{"Yes", "No"}, "No");

			if (result == JOptionPane.YES_OPTION)
			{
				configManager.setDefaultNotifierConfiguration(notifier, cd.getGroup().value(), true);

				rebuild();
			}
		});
		mainPanel.add(resetButton);

		JButton backButton = new JButton("Back");
		backButton.addActionListener(e -> pluginList.getMuxer().popState());
		mainPanel.add(backButton);

		revalidate();
	}

	private void changeConfiguration(Component component, ConfigDescriptor cd, ConfigItemDescriptor cid)
	{
		String value;
		if (component instanceof JCheckBox)
		{
			JCheckBox checkbox = (JCheckBox) component;
			value = "" + checkbox.isSelected();
		}
		else if (component instanceof JComboBox)
		{
			final JComboBox jComboBox = (JComboBox) component;
			value = ((Enum) jComboBox.getSelectedItem()).name();
		}
		else
		{
			return;
		}

		configManager.setConfiguration(cd.getGroup().value(), cid.getItem().keyName(), value);
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PANEL_WIDTH + SCROLLBAR_WIDTH, super.getPreferredSize().height);
	}
}
