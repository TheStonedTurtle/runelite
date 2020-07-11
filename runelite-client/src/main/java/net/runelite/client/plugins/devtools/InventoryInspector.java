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
package net.runelite.client.plugins.devtools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.devtools.inventory.ItemGrid;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.ColorScheme;

@Slf4j
@Singleton
public class InventoryInspector extends JFrame
{
	private final EventBus eventBus;

	private final JPanel tracker = new JPanel();
	private final JPanel editor = new JPanel();
	private final ItemGrid itemGrid;
	private int lastTick;

	@Inject
	InventoryInspector(EventBus eventBus, DevToolsPlugin plugin, ItemManager itemManager)
	{
		this.eventBus = eventBus;
		this.itemGrid = new ItemGrid(itemManager);

		setTitle("RuneLite Inventory Inspector");
		setIconImage(ClientUI.ICON);

		setLayout(new BorderLayout());

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// Reset highlight on close
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				close();
				plugin.getInventoryInspector().setActive(false);
			}
		});

		final JPanel rightSide = new JPanel();
		rightSide.setLayout(new BorderLayout());
		rightSide.setPreferredSize(new Dimension(400, 400));

		final JScrollPane gridScroller = new JScrollPane(itemGrid);
		gridScroller.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);

		rightSide.add(editor, BorderLayout.NORTH);
		rightSide.add(gridScroller, BorderLayout.CENTER);
		add(rightSide, BorderLayout.CENTER);

		pack();
	}

	public void open()
	{
		eventBus.register(this);
		setVisible(true);
		toFront();
		repaint();

		itemGrid.displayItems(new Item[]{
			new Item(995, 124567890),
			new Item(ItemID.DRAGON_CLAWS, 2),
			new Item(ItemID.DRAGON_2H_SWORD, 2),
			new Item(ItemID.DRAGON_AXE, 2),
			new Item(ItemID.DRAGON_BATTLEAXE, 2),
			new Item(ItemID.VOID_KNIGHT_GLOVES, 1),
			new Item(ItemID.VOID_KNIGHT_ROBE, 1),
			new Item(ItemID.VOID_KNIGHT_TOP, 1),
			new Item(ItemID.VOID_MELEE_HELM, 1),
			new Item(ItemID.BANDOS_CHESTPLATE, 1),
			new Item(ItemID.BANDOS_TASSETS, 1),
			new Item(ItemID.BANDOS_BOOTS, 1),
			new Item(ItemID.INFERNAL_CAPE, 1),
			new Item(ItemID.SCYTHE_OF_VITUR, 1),
		}, this::selectItem);
	}

	public void close()
	{
		eventBus.unregister(this);
		setVisible(false);
	}

	private void selectItem(final Item item)
	{
		itemGrid.deselectGridItems();
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		log.info("Item container {} has been updated. Named {}", event.getContainerId(), getNameForInventoryID(event.getContainerId()));
	}

	@Nullable
	private static String getNameForInventoryID(final int id)
	{
		for (final InventoryID inv : InventoryID.values())
		{
			if (inv.getId() == id)
			{
				return inv.name();
			}
		}

		return null;
	}
}
