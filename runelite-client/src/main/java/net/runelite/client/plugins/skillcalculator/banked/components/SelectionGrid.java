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

import java.awt.GridLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import javax.swing.JPanel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.game.AsyncBufferedImage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.skillcalculator.banked.beans.BankedItem;

/**
 * A grid that supports mouse events
 */
public class SelectionGrid extends JPanel
{
	private static final int ITEMS_PER_ROW = 5;

	private final Map<BankedItem, GridItem> panelMap = new HashMap<>();

	@Getter
	private BankedItem selectedItem;

	/* To be executed when this element is clicked */
	@Setter
	private BooleanSupplier onSelectEvent;

	/* To be executed when this element is ignored */
	@Setter
	private BooleanSupplier onIgnoreEvent;

	public SelectionGrid(final Collection<BankedItem> items, final ItemManager itemManager)
	{
		// Calculates how many rows need to be display to fit all items
		final int rowSize = ((items.size() % ITEMS_PER_ROW == 0) ? 0 : 1) + items.size() / ITEMS_PER_ROW;
		setLayout(new GridLayout(rowSize, ITEMS_PER_ROW, 1, 1));

		for (final BankedItem item : items)
		{
			final int qty = item.getQty();
			final boolean stackable = item.getItem().getComposition().isStackable() || qty > 1;
			final AsyncBufferedImage img = itemManager.getImage(item.getItem().getItemID(), qty, stackable);

			final GridItem gridItem = new GridItem(item, img);

			gridItem.setOnSelectEvent(() -> selected(item));
			panelMap.put(item, gridItem);

			// Select the first option
			if (selectedItem == null)
			{
				selected(item);
			}

			this.add(gridItem);
		}
	}

	private boolean selected(final BankedItem item)
	{
		final BankedItem old = this.selectedItem;
		if (item.equals(old))
		{
			return false;
		}

		// Set selected item now so the boolean can see what was just clicked
		this.selectedItem = item;
		if (onSelectEvent != null && !onSelectEvent.getAsBoolean())
		{
			this.selectedItem = old;
			return false;
		}

		final GridItem gridItem = panelMap.get(old);
		if (gridItem != null)
		{
			gridItem.unselect();
		}

		return true;
	}
}
