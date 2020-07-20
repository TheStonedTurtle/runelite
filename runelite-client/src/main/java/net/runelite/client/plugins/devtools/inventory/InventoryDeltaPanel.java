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
package net.runelite.client.plugins.devtools.inventory;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DecimalFormat;
import javax.annotation.Nullable;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.api.Constants;
import net.runelite.api.Item;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;

public class InventoryDeltaPanel extends JPanel implements Scrollable
{
	private static final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###");
	private static final Dimension ITEM_SIZE = new Dimension(Constants.ITEM_SPRITE_WIDTH + 4, Constants.ITEM_SPRITE_HEIGHT);

	private final ItemManager itemManager;
	private final JPanel addedGrid = new JPanel();
	private final JPanel removedGrid = new JPanel();
	private final JPanel currentGrid = new JPanel();

	public InventoryDeltaPanel(final ItemManager itemManager)
	{
		this.itemManager = itemManager;

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		final EmptyBorder border = new EmptyBorder(2, 2, 2, 2);
		setBorder(border);
		addedGrid.setBorder(border);
		removedGrid.setBorder(border);
		currentGrid.setBorder(border);

		final GridLayout layout = new GridLayout(0, 1, 1, 1);
		addedGrid.setLayout(layout);
		removedGrid.setLayout(layout);
		currentGrid.setLayout(layout);

		// Listen for resize events
		addComponentListener(new ComponentAdapter() {
			public void componentResized(final ComponentEvent componentEvent) {
				// Account for container and slot padding
				final int cols = Math.max((getWidth() - 4) / (ITEM_SIZE.width + 1), 1);
				final GridLayout layout = new GridLayout(0, cols, 1, 1);
				addedGrid.setLayout(layout);
				removedGrid.setLayout(layout);
				currentGrid.setLayout(layout);
			}
		});
	}

	public void clear()
	{
		removeAll();
		revalidate();
		repaint();
	}

	public void displayItems(final Item[] items)
	{
		displayItems(items, null, null);
	}

	public void displayItems(final Item[] items, @Nullable final Item[] added, @Nullable final Item[] removed)
	{
		clear();

		if (added != null && added.length > 0)
		{
			final JLabel label = new JLabel("Items Added:", JLabel.CENTER);
			label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			add(label);
			add(addedGrid);

			addItemsToPanel(addedGrid, added);
			for (final Component c : addedGrid.getComponents())
			{
				c.setBackground(new Color(0, 100, 0));
			}
		}

		if (removed != null && removed.length > 0)
		{
			final JLabel label = new JLabel("Items Removed:", JLabel.CENTER);
			label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			add(label);
			add(removedGrid);

			addItemsToPanel(removedGrid, removed);
			for (final Component c : removedGrid.getComponents())
			{
				c.setBackground(new Color(120, 0, 0));
			}
		}

		final JLabel label = new JLabel("Items in Inventory:", JLabel.CENTER);
		label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		add(label);
		add(currentGrid);

		addItemsToPanel(currentGrid, items);

		revalidate();
		repaint();
	}

	private void addItemsToPanel(final JPanel panel, final Item[] items)
	{
		panel.removeAll();

		for (int i = 0; i < items.length; i++)
		{
			final Item item = items[i];
			final JLabel gridItem = new JLabel();
			gridItem.setOpaque(true);
			gridItem.setPreferredSize(ITEM_SIZE);
			gridItem.setVerticalAlignment(SwingConstants.CENTER);
			gridItem.setHorizontalAlignment(SwingConstants.CENTER);

			gridItem.setToolTipText("<html>Slot: " + i
				+ "<br/>Item ID: " + item.getId()
				+ "<br/>Quantity: " + COMMA_FORMAT.format(item.getQuantity())
				+ "</html>");

			if (item.getId() == -1)
			{
				gridItem.setText("EMPTY");
				gridItem.setFont(FontManager.getRunescapeSmallFont());
			}
			else
			{
				itemManager.getImage(item.getId(), item.getQuantity(), item.getQuantity() > 1).addTo(gridItem);
			}

			panel.add(gridItem);
		}
	}

	@Override
	public Dimension getPreferredScrollableViewportSize()
	{
		return null;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return 1 + (orientation == SwingConstants.VERTICAL ? ITEM_SIZE.height : ITEM_SIZE.width);
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return 1 + (orientation == SwingConstants.VERTICAL ? ITEM_SIZE.height : ITEM_SIZE.width);
	}

	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return true;
	}

	@Override
	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}
}
