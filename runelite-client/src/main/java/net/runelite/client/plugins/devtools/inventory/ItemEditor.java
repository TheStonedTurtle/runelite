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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import net.runelite.client.game.ItemManager;
import static net.runelite.client.plugins.devtools.inventory.GridItem.ITEM_SIZE;

public class ItemEditor extends JPanel
{
	private final ItemManager itemManager;

	public ItemEditor(final ItemManager itemManager)
	{
		this.itemManager = itemManager;

		add(new JLabel("Select an item to modify below"));
	}

	public void selectItem(final InventoryItem item)
	{
		removeAll();

		final JLabel image = new JLabel();
		image.setMinimumSize(ITEM_SIZE);
		image.setMaximumSize(ITEM_SIZE);
		image.setPreferredSize(ITEM_SIZE);
		image.setHorizontalAlignment(SwingConstants.CENTER);
		itemManager.getImage(item.getId(), item.getQuantity(), item.getQuantity() > 1).addTo(image);
		image.setToolTipText(createInventoryItemToolTipText(item));

		add(image);
	}

	private static String createInventoryItemToolTipText(final InventoryItem item)
	{
		return "<html>" + item.getName() + " x " + item.getQuantity()
			+ "<br/>Item ID: " + item.getId()
			+ "<br/>Inv Slot: " + item.getSlot();
	}
}
