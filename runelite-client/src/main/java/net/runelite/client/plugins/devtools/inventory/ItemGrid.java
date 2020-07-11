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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.api.Item;
import net.runelite.client.game.ItemManager;

public class ItemGrid extends JPanel implements Scrollable
{
	private final ItemManager itemManager;
	private final List<GridItem> gridItems = new ArrayList<>();

	public ItemGrid(final ItemManager itemManager)
	{
		this.itemManager = itemManager;
		setBorder(new EmptyBorder(2, 2, 2, 2));
		setLayout(new GridLayout(0, 1, 1, 1));

		// Listen for resize events
		addComponentListener(new ComponentAdapter() {
			public void componentResized(final ComponentEvent componentEvent) {
				// Account for container and slot padding
				final int cols = Math.max((getWidth() - 4) / (GridItem.ITEM_SIZE.width + 1), 1);
				setLayout(new GridLayout(0, cols, 1, 1));
			}
		});
	}

	public void clearGrid()
	{
		gridItems.clear();
		removeAll();
		revalidate();
		repaint();
	}

	public void displayItems(final Item[] items, final Consumer<Item> selectionConsumer)
	{
		clearGrid();

		for (final Item item : items)
		{
			final GridItem gridItem = new GridItem(item);
			gridItem.setSelectionConsumer(selectionConsumer);
			itemManager.getImage(item.getId(), item.getQuantity(), item.getQuantity() > 1).addTo(gridItem);

			gridItems.add(gridItem);
			add(gridItem);
		}

		revalidate();
		repaint();
	}

	public void deselectGridItems()
	{
		gridItems.forEach(gi -> gi.setSelected(false));
	}

	@Override
	public Dimension getPreferredScrollableViewportSize()
	{
		return null;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return 1 + (orientation == SwingConstants.VERTICAL ? GridItem.ITEM_SIZE.height : GridItem.ITEM_SIZE.width);
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return 1 + (orientation == SwingConstants.VERTICAL ? GridItem.ITEM_SIZE.height : GridItem.ITEM_SIZE.width);
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
