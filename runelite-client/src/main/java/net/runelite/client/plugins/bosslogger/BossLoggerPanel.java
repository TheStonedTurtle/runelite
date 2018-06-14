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
import java.awt.Container;
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
	// Displays the specified tabs content
	private JPanel display;
	// Holds all possible tabs
	private final MaterialTabGroup tabGroup;

	private JPanel landingPanel;
	private JPanel bossPanel;

	@Inject
	BossLoggerPanel(ItemManager itemManager, BossLoggerPlugin bossLoggerPlugin)
	{
		super(false);

		this.itemManager = itemManager;
		this.bossLoggerPlugin = bossLoggerPlugin;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		tabGroup = new MaterialTabGroup();
		display = null;

		this.add(createLandingPanel());
	}

	// Landing page (Boss Selection Screen)
	private JPanel createLandingPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		tabGroup.setBorder(new EmptyBorder(5, 8, 0, 0));
		tabGroup.setLayout(new GridBagLayout());

		createLandingTitle();

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

		// Add everything to the panel
		panel.add(title);
		panel.add(tabGroup);

		return panel;
	}

	// Creates the title panel for the landing page
	private void createLandingTitle()
	{
		title = new JPanel();
		title.setBorder(new EmptyBorder(5, 5, 0, 0));
		title.setLayout(new GridBagLayout());

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

	// Creates the title panel for the recorded loot tab
	private void createTabTitle(String name)
	{
		title = new JPanel();
		title.setBorder(new EmptyBorder(5, 5, 0, 0));
		title.setLayout(new GridBagLayout());

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


	// Wrapper for creating LootPanel
	private JPanel createLootPanel(Tab tab)
	{
		// Grab Tab Data
		ArrayList<LootEntry> data = bossLoggerPlugin.getData(tab.getName());

		// Unique Items Info
		ArrayList<UniqueItem> list = UniqueItem.getByActivityName(tab.getName());
		Map<Integer, ArrayList<UniqueItem>> sets = UniqueItem.createPositionSetMap(list);

		// Create & Return Loot Panel
		LootPanel lootPanel = new LootPanel(data, sets, itemManager);

		// Scrolling Ability for lootPanel
		JPanel wrapped = new JPanel(new BorderLayout());
		wrapped.add(lootPanel, BorderLayout.NORTH);
		JScrollPane scroller = new JScrollPane(wrapped);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.getVerticalScrollBar().setUnitIncrement(16);

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
		tabPanel.add(scroller);

		return tabPanel;
	}

	private void showTabDisplay(Tab tab)
	{
		this.removeAll();

		createTabTitle(tab.getBossName());

		bossLoggerPlugin.loadTabData(tab);
		display = createLootPanel(tab);

		this.add(title);
		this.add(display);

		this.revalidate();
		this.repaint();
	}

	private void showLandingPage()
	{
		this.removeAll();

		// Ensure `title` element contains the correct labels
		createLandingTitle();

		// Add info back from stored variables
		this.add(title, BorderLayout.NORTH);
		this.add(tabGroup, BorderLayout.CENTER);

		this.revalidate();
		this.repaint();
	}


	// Updates panel for this tab name
	void updateTab(String tabName)
	{
		Tab tab = Tab.getByName(tabName);
		// Reload data from file to ensure data and UI match
		bossLoggerPlugin.loadTabData(tab);
		// Grab LootPanel that needs to be updated
		//LootPanel lootPanel = lootMap.get(tab.getName().toUpperCase());
		// Invoke Later to ensure EDT thread
		//SwingUtilities.invokeLater(() -> lootPanel.updateRecords(bossLoggerPlugin.getData(tabName)));
	}

	public void toggleTab(String tab, boolean flag)
	{
		JPanel panel = createLandingPanel();
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