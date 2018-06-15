/*
 * Copyright (c) 2018, TheStonedTurtle <www.github.com/TheStonedTurtle>
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
package net.runelite.client.plugins.bosslogger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.AsyncBufferedImage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;


@Slf4j
class BossLoggerPanel extends PluginPanel
{
	private final ItemManager itemManager;
	private final BossLoggerPlugin bossLoggerPlugin;

	private JPanel title;
	private JPanel tabGroup;
	private LootPanel lootPanel;

	private Tab currentTab = null;

	private final static Dimension TITLE_DIMENSION = new Dimension(250, 75);

	@Inject
	BossLoggerPanel(ItemManager itemManager, BossLoggerPlugin bossLoggerPlugin)
	{
		super(false);

		this.itemManager = itemManager;
		this.bossLoggerPlugin = bossLoggerPlugin;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		tabGroup = new JPanel();
		tabGroup.setBorder(new EmptyBorder(0, 8, 0, 0));
		//tabGroup.setBorder(new MatteBorder(1, 1, 1, 1, Color.RED));
		tabGroup.setLayout(new GridBagLayout());

		title = new JPanel();
		title.setBorder(new EmptyBorder(5, 0, 0, 0));
		//title.setBorder(new MatteBorder(1, 1, 1, 1, Color.PINK));
		title.setLayout(new GridBagLayout());
		title.setMaximumSize(TITLE_DIMENSION);
		title.setPreferredSize(TITLE_DIMENSION);

		createLandingPanel();
	}

	// Landing page (Boss Selection Screen)
	private void createLandingPanel()
	{
		currentTab = null;
		this.removeAll();

		createLandingTitle();

		createTabGroup();

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 1;
		c.gridy = 0;

		this.add(title, c);
		c.gridy++;
		this.add(wrapContainer(tabGroup), c);
	}

	private void createTabGroup()
	{
		tabGroup.removeAll();

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;

		// Add the bosses tabs, by category, to tabGroup
		Set<String> categories = Tab.categories;
		for (String categoryName : categories)
		{
			createTabCategory(categoryName, c);
		}
	}

	// Creates the title panel for the landing page
	private void createLandingTitle()
	{
		title.removeAll();

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridy = 0;

		// Plugin Name
		JLabel plugin = new JLabel("Boss Logger Plugin", SwingConstants.CENTER);
		plugin.setForeground(Color.WHITE);

		// Selection Text
		JLabel text = new JLabel("Select boss icon to view recorded loot", SwingConstants.CENTER);
		text.setFont(FontManager.getRunescapeSmallFont());
		text.setBorder(new EmptyBorder(15, 0, 5, 0));
		text.setForeground(Color.WHITE);

		title.add(plugin, c);
		c.gridy++;
		title.add(text, c);
	}

	// Creates all tabs for a specific category
	private void createTabCategory(String categoryName, GridBagConstraints c)
	{
		MaterialTabGroup thisTabGroup = new MaterialTabGroup();
		thisTabGroup.setLayout(new GridLayout(0, 4, 7, 7));
		thisTabGroup.setBorder(new EmptyBorder(4, 0, 0, 0));

		JLabel name = new JLabel(categoryName);
		name.setBorder(new EmptyBorder(8, 0, 0, 0));
		name.setForeground(Color.WHITE);
		name.setVerticalAlignment(SwingConstants.CENTER);

		int enabledCount = 0;
		ArrayList<Tab> categoryTabs = Tab.getByCategoryName(categoryName);
		for (Tab tab : categoryTabs)
		{
			// Only create tabs for enabled recording options
			if (bossLoggerPlugin.isBeingRecorded(tab.getName()))
			{
				enabledCount++;

				// Create tab (with hover effects/text)
				MaterialTab materialTab = new MaterialTab("", thisTabGroup, null);
				materialTab.setName(tab.getName());
				materialTab.setToolTipText(tab.getBossName());
				materialTab.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseEntered(MouseEvent e)
					{
						materialTab.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
					}

					@Override
					public void mouseExited(MouseEvent e)
					{
						materialTab.setBackground(ColorScheme.DARKER_GRAY_COLOR);
					}
				});

				// Attach Icon to the Tab
				AsyncBufferedImage image = itemManager.getImage(tab.getItemID());
				Runnable resize = () ->
				{
					materialTab.setIcon(new ImageIcon(image.getScaledInstance(35, 35, Image.SCALE_SMOOTH)));
					materialTab.setOpaque(true);
					materialTab.setBackground(ColorScheme.DARKER_GRAY_COLOR);
					materialTab.setHorizontalAlignment(SwingConstants.CENTER);
					materialTab.setVerticalAlignment(SwingConstants.CENTER);
					materialTab.setPreferredSize(new Dimension(35, 35));
				};
				image.onChanged(resize);
				resize.run();

				materialTab.setOnSelectEvent(() ->
				{
					this.showTabDisplay(tab);
					materialTab.unselect();
					materialTab.setBackground(ColorScheme.DARKER_GRAY_COLOR);
				});

				thisTabGroup.addTab(materialTab);
			}
		}

		if (enabledCount > 0)
		{
			tabGroup.add(name, c);
			c.gridy++;
			tabGroup.add(thisTabGroup, c);
			c.gridy++;
		}
	}

	// Landing page (Boss Selection Screen)
	private void createTabPanel(Tab tab)
	{
		currentTab = tab;
		this.removeAll();

		createTabTitle(tab.getBossName());

		bossLoggerPlugin.loadTabData(tab);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 1;
		c.gridy = 0;

		this.add(title, c);
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		this.add(wrapContainer(createLootPanel(tab)), c);
	}

	// Creates the title panel for the recorded loot tab
	private void createTabTitle(String name)
	{
		title.removeAll();

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridy = 0;

		// Back Button
		JButton button = new JButton("Return to selection screen");
		button.addActionListener(e -> this.showLandingPage());

		// Plugin Name
		JLabel text = new JLabel(name, SwingConstants.CENTER);
		text.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
		text.setForeground(Color.WHITE);
		text.setBorder(new EmptyBorder(15, 0, 0, 0));

		title.add(button, c);
		c.gridy++;
		title.add(text, c);
	}

	// Wrapper for creating LootPanel
	private JPanel createLootPanel(Tab tab)
	{
		// Grab Tab Data
		ArrayList<LootEntry> data = bossLoggerPlugin.getData(tab.getName());

		// Unique Items Info
		ArrayList<UniqueItem> list = UniqueItem.getByActivityName(tab.getName());
		Map<Integer, ArrayList<UniqueItem>> sets = UniqueItem.createPositionSetMap(list);

		// Create & Return Loot Panel
		lootPanel = new LootPanel(data, sets, itemManager);

		// Button Container
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.setBorder(new EmptyBorder(0, 0, 4, 0));

		// Refresh Button
		JButton refresh = new JButton("Refresh Data");
		refresh.addActionListener(e ->
				refreshLootPanel(lootPanel, tab));

		// Refresh Button
		JButton clear = new JButton("Clear Data");
		clear.addActionListener(e ->
			log.info("Clearing data for: " + tab.getName())
		);
				//clearTabData(tab));

		buttons.add(refresh);
		buttons.add(Box.createHorizontalGlue());
		buttons.add(clear);

		JPanel tabPanel = new JPanel();
		tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));
		tabPanel.setBorder(new EmptyBorder(2, 2, 2, 2));

		tabPanel.add(buttons);
		tabPanel.add(lootPanel);

		return tabPanel;
	}

	private void showTabDisplay(Tab tab)
	{
		createTabPanel(tab);

		this.revalidate();
		this.repaint();
	}

	private void showLandingPage()
	{
		// Add info back from stored variables
		createLandingPanel();

		this.revalidate();
		this.repaint();
	}

	// Wrap the panel inside a scroll pane
	private JScrollPane wrapContainer(JPanel container)
	{
		JPanel wrapped = new JPanel(new BorderLayout());
		wrapped.add(container, BorderLayout.NORTH);
		wrapped.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JScrollPane scroller = new JScrollPane(wrapped);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.getVerticalScrollBar().setPreferredSize(new Dimension(16, 0));
		scroller.getVerticalScrollBar().setBorder(new EmptyBorder(0, 9, 0, 0));
		scroller.setBackground(ColorScheme.DARK_GRAY_COLOR);

		return scroller;
	}

	// Updates panel for this tab name
	void updateTab(String tabName)
	{
		// Change to tab of recently killed boss if on landing page
		if (currentTab == null)
		{
			showTabDisplay(Tab.getByName(tabName));
			return;
		}

		// only update the tab if they are currently looking at current boss tab
		if (tabName.equals(currentTab.getName()))
		{
			// Reload data from file to ensure data and UI match
			bossLoggerPlugin.loadTabData(currentTab);
			// Grab LootPanel that needs to be updated
			SwingUtilities.invokeLater(() -> lootPanel.updateRecords(bossLoggerPlugin.getData(tabName)));
		}
	}

	void toggleTab(String tab, boolean flag)
	{
		// Only toggle tab if on landing page since the tabs are recreated each time
		if (currentTab == null)
			createLandingPanel();
	}

	// Refresh the Loot Panel with updated data (requests the data from file)
	private void refreshLootPanel(LootPanel lootPanel, Tab tab)
	{
		// Refresh data for necessary tab
		bossLoggerPlugin.loadTabData(tab);

		// Recreate the loot panel
		lootPanel.updateRecords(bossLoggerPlugin.getData(tab.getName()));

		// Ensure changes are applied
		this.revalidate();
		this.repaint();
	}
}