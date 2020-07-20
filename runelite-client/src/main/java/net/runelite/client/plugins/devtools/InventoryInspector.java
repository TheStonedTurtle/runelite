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
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.devtools.inventory.InventoryDeltaPanel;
import net.runelite.client.plugins.devtools.inventory.InventoryLog;
import net.runelite.client.plugins.devtools.inventory.InventoryLogNode;
import net.runelite.client.plugins.devtools.inventory.InventoryTreeNode;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.ColorScheme;

@Slf4j
@Singleton
public class InventoryInspector extends JFrame
{
	private static final int MAX_LOG_ENTRIES = 25;
	private static final String REFRESH_CONFIG_KEY = "inventory-auto-refresh";

	private final Client client;
	private final EventBus eventBus;

	private final Map<Integer, InventoryTreeNode> nodeMap = new HashMap<>();
	// Used to store the most recent inventory log updates if not auto refreshing
	private final Map<Integer, InventoryLog> logMap = new HashMap<>();
	private final DefaultMutableTreeNode trackerRootNode = new DefaultMutableTreeNode();
	private final JTree tree;
	private final InventoryDeltaPanel deltaPanel;

	@Inject
	InventoryInspector(Client client, EventBus eventBus, DevToolsPlugin plugin, ItemManager itemManager)
	{
		this.eventBus = eventBus;
		this.client = client;
		this.deltaPanel = new InventoryDeltaPanel(itemManager);

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

		tree = new JTree(trackerRootNode);
		tree.setBorder(new EmptyBorder(2, 2, 2, 2));
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.addTreeSelectionListener(e -> {
			if (e.getNewLeadSelectionPath() == null)
			{
				return;
			}

			final Object node = e.getNewLeadSelectionPath().getLastPathComponent();
			if (node instanceof InventoryLogNode)
			{
				final InventoryLogNode logNode = (InventoryLogNode) node;

				final InventoryTreeNode treeNode = nodeMap.get(logNode.getLog().getContainerId());
				if (treeNode == null)
				{
					log.warn("Attempted to click on a node that doesn't map anywhere: {}", logNode);
					return;
				}

				final int idx = treeNode.getIndex(logNode);
				// No previous snapshot to compare against
				if (idx <= 0)
				{
					deltaPanel.displayItems(logNode.getLog().getItems());
					return;
				}

				final TreeNode prevNode = treeNode.getChildAt(idx - 1);
				if (!(prevNode instanceof InventoryLogNode))
				{
					return;
				}
				final InventoryLogNode prevLogNode = (InventoryLogNode) prevNode;

				final Item[][] delta = compareItemSnapshots(prevLogNode.getLog().getItems(), logNode.getLog().getItems());
				deltaPanel.displayItems(logNode.getLog().getItems(), delta[0], delta[1]);
			}
		});
		tree.setModel(new DefaultTreeModel(trackerRootNode));

		final JPanel leftSide = new JPanel();
		leftSide.setLayout(new BorderLayout());

		final JScrollPane trackerScroller = new JScrollPane(tree);
		trackerScroller.setPreferredSize(new Dimension(200, 400));

		final JScrollBar vertical = trackerScroller.getVerticalScrollBar();
		vertical.addAdjustmentListener(new AdjustmentListener()
		{
			int lastMaximum = actualMax();

			private int actualMax()
			{
				return vertical.getMaximum() - vertical.getModel().getExtent();
			}

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e)
			{
				if (vertical.getValue() >= lastMaximum)
				{
					vertical.setValue(actualMax());
				}
				lastMaximum = actualMax();
			}
		});

		leftSide.add(trackerScroller, BorderLayout.CENTER);

		final JButton refreshBtn = new JButton("Refresh");
		refreshBtn.setFocusable(false);
		refreshBtn.addActionListener(e -> refreshTracker());

		final JButton clearBtn = new JButton("Clear");
		clearBtn.setFocusable(false);
		clearBtn.addActionListener(e -> clearTracker());

		final JPanel bottomRow = new JPanel();
		bottomRow.add(refreshBtn);
		bottomRow.add(clearBtn);

		leftSide.add(bottomRow, BorderLayout.SOUTH);

		final JScrollPane gridScroller = new JScrollPane(deltaPanel);
		gridScroller.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
		gridScroller.setPreferredSize(new Dimension(200, 400));

		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSide, gridScroller);
		add(split, BorderLayout.CENTER);

		pack();
	}

	public void open()
	{
		eventBus.register(this);
		setVisible(true);
		toFront();
		repaint();
	}

	public void close()
	{
		eventBus.unregister(this);
		clearTracker();
		setVisible(false);
		nodeMap.clear();
		logMap.clear();
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		final int id = event.getContainerId();
		final InventoryLog log = new InventoryLog(id, getNameForInventoryID(id), event.getItemContainer().getItems(), client.getTickCount());

		logMap.put(id, log);
	}

	private void addLog(final InventoryLog invLog)
	{
		InventoryTreeNode node = nodeMap.get(invLog.getContainerId());
		if (node == null)
		{
			node = new InventoryTreeNode(invLog.getContainerId(), invLog.getContainerName());
			nodeMap.put(invLog.getContainerId(), node);
		}

		node.add(new InventoryLogNode(invLog));

		// Cull very old stuff
		for (; node.getChildCount() > MAX_LOG_ENTRIES; )
		{
			node.remove(0);
		}
	}

	private void clearTracker()
	{
		nodeMap.clear();
		deltaPanel.clear();
		trackerRootNode.removeAllChildren();
		tree.setModel(new DefaultTreeModel(trackerRootNode));
	}

	private void refreshTracker()
	{
		if (logMap.size() > 0)
		{
			logMap.values().forEach(this::addLog);
			logMap.clear();
		}

		SwingUtilities.invokeLater(() ->
		{
			trackerRootNode.removeAllChildren();
			nodeMap.values().forEach(trackerRootNode::add);
			tree.setModel(new DefaultTreeModel(trackerRootNode));
		});
	}

	/**
	 * Compares the current inventory to the old one returning the items that were added and removed.
	 * @param previous old snapshot
	 * @param current new snapshot
	 * @return The first Item[] contains the items that were added and the second contains the items that were removed
	 */
	private static Item[][] compareItemSnapshots(final Item[] previous, final Item[] current)
	{
		final Map<Integer, Integer> qtyMap = new HashMap<>();

		// ItemContainers shouldn't become smaller over time, but just in case
		final int maxSlots = Math.max(previous.length, current.length);
		for (int i = 0; i < maxSlots; i++)
		{
			final Item prev = previous.length > i ? previous[i] : null;
			final Item cur = current.length > i ? current[i] : null;

			if (prev != null)
			{
				qtyMap.merge(prev.getId(), -1 * prev.getQuantity(), Integer::sum);
			}
			if (cur != null)
			{
				qtyMap.merge(cur.getId(), cur.getQuantity(), Integer::sum);
			}
		}

		final Map<Boolean, List<Item>> result = qtyMap.entrySet().stream()
			.filter(e -> e.getValue() != 0)
			.map(e -> new Item(e.getKey(), e.getValue()))
			.collect(Collectors.partitioningBy(item -> item.getQuantity() > 0));

		final Item[] added = result.get(true).toArray(new Item[0]);
		final Item[] removed = result.get(false).stream()
			// Make quantities positive now that its been sorted.
			.map(i -> new Item(i.getId(), i.getQuantity() * -1))
			.toArray(Item[]::new);

		return new Item[][]{
			added, removed
		};
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
