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
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.devtools.inventory.InventoryLog;
import net.runelite.client.plugins.devtools.inventory.InventoryLogNode;
import net.runelite.client.plugins.devtools.inventory.InventoryTreeNode;
import net.runelite.client.plugins.devtools.inventory.ItemGrid;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;

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
	private final JPanel tracker = new JPanel();
	private final JPanel editor = new JPanel();
	private final ItemGrid itemGrid;
	private boolean autoRefresh = false;

	@Inject
	InventoryInspector(Client client, EventBus eventBus, DevToolsPlugin plugin, ItemManager itemManager, ConfigManager configManager)
	{
		this.eventBus = eventBus;
		this.client = client;
		this.itemGrid = new ItemGrid(itemManager);

		final Boolean refreshVal = configManager.getConfiguration("devtools", REFRESH_CONFIG_KEY, Boolean.class);
		if (refreshVal != null)
		{
			autoRefresh = refreshVal;
		}

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

		tracker.setLayout(new DynamicGridLayout(0, 1, 0, 3));
		tracker.setBorder(new EmptyBorder(2, 2, 2, 2));

		final JPanel leftSide = new JPanel();
		leftSide.setLayout(new BorderLayout());

		final JPanel trackerWrapper = new JPanel();
		trackerWrapper.setLayout(new BorderLayout());
		trackerWrapper.add(tracker, BorderLayout.NORTH);

		final JScrollPane trackerScroller = new JScrollPane(trackerWrapper);
		trackerScroller.setPreferredSize(new Dimension(300, 400));

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

		final JCheckBox autoRefreshBtn = new JCheckBox("Auto Refresh");
		autoRefreshBtn.setSelected(autoRefresh);
		autoRefreshBtn.setFocusable(false);
		autoRefreshBtn.addActionListener(e ->
		{
			autoRefresh = !autoRefresh;
			configManager.setConfiguration("devtools", REFRESH_CONFIG_KEY, autoRefresh);
		});

		final JButton refreshBtn = new JButton("Refresh");
		refreshBtn.setFocusable(false);
		refreshBtn.addActionListener(e -> refreshTracker());

		final JButton clearBtn = new JButton("Clear");
		clearBtn.setFocusable(false);
		clearBtn.addActionListener(e -> clearTracker());

		final JPanel bottomRow = new JPanel();
		bottomRow.add(autoRefreshBtn);
		bottomRow.add(refreshBtn);
		bottomRow.add(clearBtn);

		leftSide.add(bottomRow, BorderLayout.SOUTH);

		final JPanel rightSide = new JPanel();
		rightSide.setLayout(new BorderLayout());
		rightSide.setPreferredSize(new Dimension(200, 400));

		final JScrollPane gridScroller = new JScrollPane(itemGrid);
		gridScroller.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);

		rightSide.add(editor, BorderLayout.NORTH);
		rightSide.add(gridScroller, BorderLayout.CENTER);

		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSide, rightSide);
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

		if (!autoRefresh)
		{
			logMap.put(id, log);
			return;
		}

		addLog(log);
		refreshTracker();
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
		itemGrid.clearGrid();
		tracker.removeAll();
		tracker.revalidate();
	}

	private void refreshTracker()
	{
		if (logMap.size() > 0)
		{
			logMap.values().forEach(this::addLog);
			logMap.clear();
		}

		// TODO: Look into storing root node for better updating?
		SwingUtilities.invokeLater(() ->
		{
			tracker.removeAll();

			final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
			final JTree tree = new JTree(root);
			tree.setRootVisible(false);
			tree.setShowsRootHandles(true);
			tree.addTreeSelectionListener(e -> {
				final Object node = e.getNewLeadSelectionPath().getLastPathComponent();
				if (node instanceof InventoryLogNode)
				{
					final InventoryLogNode logNode = (InventoryLogNode) node;
					itemGrid.displayItems(logNode.getLog().getItems(), null);
				}
			});

			nodeMap.values().forEach(root::add);

			tree.setModel(new DefaultTreeModel(root));
			tracker.add(tree);
			tracker.revalidate();
		});
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
