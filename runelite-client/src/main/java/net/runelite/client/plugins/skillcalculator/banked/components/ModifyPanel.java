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
package net.runelite.client.plugins.skillcalculator.banked.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.game.AsyncBufferedImage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.skillcalculator.banked.BankedCalculator;
import net.runelite.client.plugins.skillcalculator.banked.beans.Activity;
import net.runelite.client.plugins.skillcalculator.banked.beans.BankedItem;
import net.runelite.client.plugins.skillcalculator.banked.beans.CriticalItem;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.ComboBoxIconEntry;
import net.runelite.client.ui.components.ComboBoxListRenderer;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

public class ModifyPanel extends JPanel
{
	private static final Dimension ICON_SIZE = new Dimension(36, 36);
	private static final DecimalFormat FORMAT_COMMA = new DecimalFormat("#,###.#");

	private static final Border PANEL_BORDER = new EmptyBorder(3, 0, 3, 0);
	private static final Color BACKGROUND_COLOR = ColorScheme.DARKER_GRAY_COLOR;

	private final BankedCalculator calc;
	private final ItemManager itemManager;

	@Getter
	private BankedItem bankedItem;
	private double xp;
	private Map<CriticalItem, Integer> linkedMap;
	@Getter
	private int amount = 0;
	@Getter
	private double total = 0;

	// Banked item information display
	private final JPanel labelContainer;
	private final JLabel image;
	private final JShadowedLabel labelName;
	private final JShadowedLabel labelValue;

	// Elements used to adjust banked item
	private final JPanel adjustContainer;

	public ModifyPanel(final BankedCalculator calc, final ItemManager itemManager)
	{
		this.calc = calc;
		this.itemManager = itemManager;

		this.setLayout(new GridBagLayout());
		this.setBorder(PANEL_BORDER);
		this.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Banked item information display
		labelContainer = new JPanel();
		labelContainer.setLayout(new BorderLayout());
		labelContainer.setBackground(BACKGROUND_COLOR);
		labelContainer.setBorder(new EmptyBorder(5, 0, 5, 0));

		// Icon
		image = new JLabel();
		image.setMinimumSize(ICON_SIZE);
		image.setMaximumSize(ICON_SIZE);
		image.setPreferredSize(ICON_SIZE);
		image.setHorizontalAlignment(SwingConstants.CENTER);
		image.setBorder(new EmptyBorder(0, 8, 0, 0));

		// Wrapper panel for the shadowed labels
		final JPanel uiInfo = new JPanel(new GridLayout(2, 1));
		uiInfo.setBorder(new EmptyBorder(0, 5, 0, 0));
		uiInfo.setBackground(BACKGROUND_COLOR);

		labelName = new JShadowedLabel();
		labelName.setForeground(Color.WHITE);
		labelName.setVerticalAlignment(SwingUtilities.BOTTOM);

		labelValue = new JShadowedLabel();
		labelValue.setFont(FontManager.getRunescapeSmallFont());
		labelValue.setVerticalAlignment(SwingUtilities.TOP);

		uiInfo.add(labelName);
		uiInfo.add(labelValue);

		// Append elements to item info panel
		labelContainer.add(image, BorderLayout.LINE_START);
		labelContainer.add(uiInfo, BorderLayout.CENTER);

		// Container for tools to adjust banked calculation for this item
		adjustContainer = new JPanel();
		adjustContainer.setLayout(new GridBagLayout());
		adjustContainer.setBackground(BACKGROUND_COLOR);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.ipady = 0;

		this.add(labelContainer, c);
		c.gridy++;
		this.add(adjustContainer, c);
	}

	// Updates the UI for the selected item
	public void setBankedItem(final BankedItem bankedItem)
	{
		this.bankedItem = bankedItem;

		this.xp = this.calc.getItemXpRate(bankedItem);
		this.linkedMap = this.calc.createLinksMap(bankedItem.getItem());

		this.amount = bankedItem.getQty();
		for (int i : linkedMap.values())
		{
			this.amount += i;
		}

		updateImageTooltip();
		updateLabelContainer();
		updateAdjustContainer();
	}

	private void updateImageTooltip()
	{
		final StringBuilder b = new StringBuilder("<html>");
		b.append(bankedItem.getQty()).append(" x ").append(bankedItem.getItem().getComposition().getName());

		for (final Map.Entry<CriticalItem, Integer> e : this.linkedMap.entrySet())
		{
			b.append("<br/>").append(e.getValue()).append(" x ").append(e.getKey().getComposition().getName());
		}

		b.append("</html>");
		this.image.setToolTipText(b.toString());
	}

	private void updateLabelContainer()
	{
		final CriticalItem item = bankedItem.getItem();

		// Update image icon
		final boolean stackable = item.getComposition().isStackable() || amount > 1;
		final AsyncBufferedImage icon = itemManager.getImage(item.getItemID(), amount, stackable);
		final Runnable resize = () -> image.setIcon(new ImageIcon(icon.getScaledInstance(ICON_SIZE.width, ICON_SIZE.height, Image.SCALE_SMOOTH)));
		icon.onChanged(resize);
		resize.run();

		final String itemName = item.getComposition().getName();
		labelName.setText(itemName);

		xp = bankedItem.getXpRate();
		total = amount * xp;

		final String value = FORMAT_COMMA.format(total) + "xp";
		labelValue.setText(value);

		labelContainer.setToolTipText("<html>" + itemName
			+ "<br/>xp: " +  this.xp
			+ "<br/>Total: " + total +	"</html");

		labelContainer.revalidate();
		labelContainer.repaint();
	}

	private void updateAdjustContainer()
	{
		adjustContainer.removeAll();

		final JLabel label = new JLabel("Producing:");
		label.setVerticalAlignment(JLabel.CENTER);
		label.setHorizontalAlignment(JLabel.CENTER);

		final Activity selected = bankedItem.getSelectedActivity();
		final JComboBox<ComboBoxIconEntry> dropdown = new JComboBox<>();

		final ComboBoxListRenderer renderer = new ComboBoxListRenderer();
		dropdown.setRenderer(renderer);

		final Collection<Activity> activities = Activity.getByCriticalItem(bankedItem.getItem(), calc.getSkillLevel());
		if (activities == null || activities.size() == 0)
		{
			if (bankedItem.getForwardsLinkedItem() != null)
			{
				final CriticalItem linked = bankedItem.getForwardsLinkedItem();
				final AsyncBufferedImage img = itemManager.getImage(linked.getItemID());
				final ImageIcon icon = new ImageIcon(img.getScaledInstance(24, 24, Image.SCALE_SMOOTH));
				final ComboBoxIconEntry entry = new ComboBoxIconEntry(icon, linked.getComposition().getName(), null);
				dropdown.addItem(entry);
				dropdown.setSelectedItem(entry);

				img.onChanged(() ->
				{
					icon.setImage(img);
					dropdown.revalidate();
					dropdown.repaint();
				});
			}
		}
		else
		{
			for (final Activity option : activities)
			{
				final double xp = option.getXp();
				String name = option.getName();
				if (xp > 0)
				{
					name += " (" + xp + "xp)";
				}

				final AsyncBufferedImage img = itemManager.getImage(option.getIcon());
				final ImageIcon icon = new ImageIcon(img.getScaledInstance(24, 24, Image.SCALE_SMOOTH));
				final ComboBoxIconEntry entry = new ComboBoxIconEntry(icon, name, option);
				dropdown.addItem(entry);

				img.onChanged(() ->
				{
					icon.setImage(img);
					dropdown.revalidate();
					dropdown.repaint();
				});

				if (option.equals(selected))
				{
					dropdown.setSelectedItem(entry);
				}
			}
		}

		// Add click event handler now to prevent above code from triggering it.
		dropdown.addItemListener(e ->
		{
			if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof ComboBoxIconEntry)
			{
				final ComboBoxIconEntry source = (ComboBoxIconEntry) e.getItem();
				if (source.getData() instanceof Activity)
				{
					final Activity selectedActivity = ((Activity) source.getData());
					calc.activitySelected(bankedItem, selectedActivity);
					updateLabelContainer();
				}
			}
		});

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.ipady = 0;

		adjustContainer.add(label, c);
		c.gridy++;
		adjustContainer.add(dropdown, c);
	}
}
