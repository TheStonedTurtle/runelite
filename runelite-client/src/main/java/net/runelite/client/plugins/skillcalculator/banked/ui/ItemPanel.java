/*
 * Copyright (c) 2018, TheStonedTurtle <https://github.com/TheStonedTurtle>
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
package net.runelite.client.plugins.skillcalculator.banked.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.AsyncBufferedImage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.skillcalculator.SkillCalculator;
import net.runelite.client.plugins.skillcalculator.banked.CriticalItem;
import net.runelite.client.plugins.skillcalculator.banked.beans.Activity;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.text.DecimalFormat;
import net.runelite.client.util.StackFormatter;

@Slf4j
public class ItemPanel extends JPanel
{
	private static final Dimension ICON_SIZE = new Dimension(36, 36);
	private static final DecimalFormat FORMAT_COMMA = new DecimalFormat("#,###.#");

	private static final BufferedImage ICON_SETTINGS;

	private static final Border PANEL_BORDER = new EmptyBorder(3, 0, 3, 0);
	private final static Color BACKGROUND_COLOR = ColorScheme.DARKER_GRAY_COLOR;
	private final static Color BUTTON_HOVER_COLOR = ColorScheme.DARKER_GRAY_HOVER_COLOR;

	static
	{
		BufferedImage i1;
		try
		{
			synchronized (ImageIO.class)
			{
				i1 = ImageIO.read(SkillCalculator.class.getResourceAsStream("view-more-white.png"));
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		ICON_SETTINGS = i1;
	}

	private final SkillCalculator calc;
	private final CriticalItem item;
	private final ItemManager itemManager;
	private final JPanel infoContainer;

	private boolean infoVisibility = false;

	public ItemPanel(SkillCalculator calc, ItemManager itemManager, CriticalItem item, double xp, int amount, double total)
	{
		this.item = item;
		this.itemManager = itemManager;
		this.calc = calc;

		this.setLayout(new GridBagLayout());
		this.setBorder(PANEL_BORDER);
		this.setBackground(ColorScheme.DARK_GRAY_COLOR);

		infoContainer = new JPanel();
		infoContainer.setLayout(new GridBagLayout());
		infoContainer.setVisible(false);
		infoContainer.setBackground(BACKGROUND_COLOR);
		infoContainer.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));

		// Icon
		AsyncBufferedImage icon = itemManager.getImage(item.getItemID(), amount, item.getComposition().isStackable() || amount > 1);
		JLabel image = new JLabel();
		image.setMinimumSize(ICON_SIZE);
		image.setMaximumSize(ICON_SIZE);
		image.setPreferredSize(ICON_SIZE);
		image.setHorizontalAlignment(SwingConstants.LEFT);
		image.setBorder(new EmptyBorder(0, 8, 0, 0));

		Runnable resize = () ->
			image.setIcon(new ImageIcon(icon.getScaledInstance((int)ICON_SIZE.getWidth(), (int)ICON_SIZE.getHeight(), Image.SCALE_SMOOTH)));
		icon.onChanged(resize);
		resize.run();

		// Container for Info
		JPanel uiInfo = new JPanel(new GridLayout(2, 1));
		uiInfo.setBorder(new EmptyBorder(0, 5, 0, 0));
		uiInfo.setBackground(BACKGROUND_COLOR);

		JShadowedLabel labelName = new JShadowedLabel(item.getComposition().getName());
		labelName.setForeground(Color.WHITE);
		labelName.setVerticalAlignment(SwingUtilities.BOTTOM);

		JShadowedLabel labelValue = new JShadowedLabel(FORMAT_COMMA.format(total) + "xp");
		labelValue.setFont(FontManager.getRunescapeSmallFont());
		labelValue.setVerticalAlignment(SwingUtilities.TOP);

		uiInfo.add(labelName);
		uiInfo.add(labelValue);

		// Settings Button
		JLabel settingsButton = new JLabel();
		settingsButton.setBorder(new EmptyBorder(0, 5, 0, 5));
		settingsButton.setIcon(new ImageIcon(ICON_SETTINGS));
		settingsButton.setOpaque(true);
		settingsButton.setBackground(BACKGROUND_COLOR);

		settingsButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				settingsButton.setBackground(BUTTON_HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				settingsButton.setBackground(BACKGROUND_COLOR);
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
				toggleInfo();
			}
		});


		// Create and append elements to container panel
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBackground(BACKGROUND_COLOR);

		panel.add(image, BorderLayout.LINE_START);
		panel.add(uiInfo, BorderLayout.CENTER);

		// Only add button if something more to display.
		if (item.getLinkedItemId() != -1 || Activity.getByCriticalItem(item) != null)
		{
			panel.add(settingsButton, BorderLayout.LINE_END);
		}

		panel.setToolTipText("<html>" + item.getComposition().getName()
			+ "<br/>xp: " +  xp
			+ "<br/>Total: " + StackFormatter.quantityToStackSize((long) total) +	"</html");

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.ipady = 20;

		this.add(panel, c);
		c.gridy++;
		this.add(infoContainer, c);
	}

	private void toggleInfo()
	{
		infoVisibility = !infoVisibility;

		if (infoVisibility)
		{
			createInfoPanel();
		}
		else
		{
			infoContainer.removeAll();
			infoContainer.setVisible(false);

			infoContainer.revalidate();
			infoContainer.repaint();
		}
	}

	private void createInfoPanel()
	{
		infoContainer.removeAll();
		infoContainer.setVisible(true);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.ipady = 0;

		if (item.getLinkedItemId() != -1)
		{
			CriticalItem linked = CriticalItem.getByItemId(item.getLinkedItemId());
			if (linked != null)
			{
				infoContainer.add(new JLabel("Turns into: " + linked.getComposition().getName()), c);
				c.gridy++;
			}
		}

		JPanel p = createActivitiesPanel();
		if (p != null)
		{
			infoContainer.add(p, c);
		}
	}


	private JPanel createActivitiesPanel()
	{
		ArrayList<Activity> activities = Activity.getByCriticalItem(item);
		if (activities == null)
		{
			return null;
		}

		JPanel p = new JPanel();
		p.setBackground(BACKGROUND_COLOR);
		p.setLayout(new BorderLayout());

		JLabel label = new JLabel("Possible training methods");

		MaterialTabGroup group = new MaterialTabGroup();
		group.setLayout(new GridLayout(0, 6, 0, 2));
		group.setBorder(new MatteBorder(1, 1, 1, 1, Color.BLACK));

		for (Activity option : activities)
		{
			AsyncBufferedImage icon = itemManager.getImage(option.getIcon());
			MaterialTab matTab = new MaterialTab("", group, null);
			matTab.setHorizontalAlignment(SwingUtilities.RIGHT);
			matTab.setToolTipText(option.getName());

			matTab.setOnSelectEvent(() ->
			{
				log.info("Changed to option: {}", option);
				//calc.activitySelected(item, option);
				return true;
			});

			Runnable resize = () ->
				matTab.setIcon(new ImageIcon(icon.getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
			icon.onChanged(resize);
			resize.run();

			group.addTab(matTab);
		}

		group.select(group.getTab(0)); // Select first option;

		p.add(label, BorderLayout.NORTH);
		p.add(group, BorderLayout.SOUTH);

		return p;
	}
}
