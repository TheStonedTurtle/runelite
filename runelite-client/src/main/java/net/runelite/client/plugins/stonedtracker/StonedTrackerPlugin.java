/*
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
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
package net.runelite.client.plugins.stonedtracker;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.NpcID;
import net.runelite.api.events.ConfigChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.localstorage.LootRecordWriter;
import net.runelite.client.plugins.loottracker.localstorage.LTItemEntry;
import net.runelite.client.plugins.loottracker.localstorage.LTRecord;
import net.runelite.client.plugins.stonedtracker.data.UniqueItem;
import net.runelite.client.plugins.stonedtracker.data.UniqueItemPrepared;
import net.runelite.client.plugins.stonedtracker.ui.LootTrackerPanel;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Stoned Tracker",
	description = "Local data persistence and unique UI for the Loot Tracker.",
	tags = {"Stoned", "Loot", "Tracker"}
)
@Slf4j
public class StonedTrackerPlugin extends Plugin
{
	// Activity/Event loot handling
	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	public StonedTrackerConfig config;

	@Inject
	private ClientThread clientThread;

	private LootTrackerPanel panel;
	private NavigationButton navButton;

	private Multimap<String, LTRecord> lootRecordMultimap = ArrayListMultimap.create();
	private Multimap<String, LTRecord> sessionLootRecordMultimap = ArrayListMultimap.create();
	private Multimap<String, UniqueItemPrepared> uniques = ArrayListMultimap.create();

	// key = name, value=current killCount
	private boolean loaded = false;
	private String currentPlayer;

	@Getter(AccessLevel.PACKAGE)
	private LootRecordWriter writer;

	@Provides
	StonedTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(StonedTrackerConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("stonedtracker"))
		{
			panel.refreshUI();
		}
	}

	@Override
	protected void startUp() throws Exception
	{
		panel = new LootTrackerPanel(itemManager, this);

		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "panel-icon.png");

		navButton = NavigationButton.builder()
			.tooltip("Stoned Tracker")
			.icon(icon)
			.priority(5)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		writer = new LootRecordWriter();

		if (!loaded)
		{
			clientThread.invokeLater(() ->
			{
				switch (client.getGameState())
				{
					case UNKNOWN:
					case STARTING:
						return false;
				}

				prepareUniqueItems();

				return true;
			});
		}
	}

	private void prepareUniqueItems()
	{
		loaded = true;
		for (UniqueItem i : UniqueItem.values())
		{
			ItemComposition c = itemManager.getItemComposition(i.getItemID());
			for (String s : i.getActivities())
			{
				uniques.put(s.toUpperCase(), new UniqueItemPrepared(c.getLinkedNoteId(), itemManager.getItemPrice(i.getItemID()), i));
			}
		}
	}

	public Collection<UniqueItemPrepared> getUniques(String name)
	{
		return uniques.get(name.toUpperCase());
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		switch (event.getGroupId())
		{
			// Unsired redemption tracking
			case (WidgetID.DIALOG_SPRITE_GROUP_ID):
				Widget text = client.getWidget(WidgetInfo.DIALOG_SPRITE_TEXT);
				if ("you place the unsired into the font of consumption...".equals(text.getText().toLowerCase()))
				{
					unsiredReclaiming = true;
				}
		}
	}

	public Collection<LTRecord> getData()
	{
		return lootRecordMultimap.values();
	}

	public Collection<LTRecord> getDataByName(String name)
	{
		return lootRecordMultimap.get(name);
	}

	public void refreshData()
	{
		// Pull data from files
		lootRecordMultimap.clear();
		Collection<LTRecord> recs = writer.loadAllLootTrackerRecords();
		for (LTRecord r : recs)
		{
			lootRecordMultimap.put(r.getName(), r);
		}
	}

	public void refreshDataByName(String name)
	{
		lootRecordMultimap.removeAll(name);
		Collection<LTRecord> recs = writer.loadLootTrackerRecords(name);
		lootRecordMultimap.putAll(name, recs);
	}

	public Collection<LTRecord> getSessionData()
	{
		return sessionLootRecordMultimap.values();
	}

	// Clear all data from this session
	public void clearData()
	{
		sessionLootRecordMultimap.clear();
	}

	public void clearStoredDataByName(String name)
	{
		lootRecordMultimap.removeAll(name);
		writer.deleteLootTrackerRecords(name);
	}

	public TreeSet<String> getNames()
	{
		return new TreeSet<>(lootRecordMultimap.keySet());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged c)
	{
		if (c.getGameState().equals(GameState.LOGGED_IN))
		{
			clientThread.invokeLater(() ->
			{
				switch (client.getGameState())
				{
					case LOGGED_IN:
						break;
					case LOGGING_IN:
					case LOADING:
						return false;
					default:
						// Quit running if any other state
						return true;
				}

				String name = client.getLocalPlayer().getName();
				if (name != null)
				{
					updatePlayerFolder(name);
					return true;
				}
				else
				{
					return false;
				}
			});
		}
	}

	private void updatePlayerFolder(String name)
	{
		if (Objects.equals(currentPlayer, name))
		{
			return;
		}

		currentPlayer = name;
		writer.setPlayerUsername(name);

		refreshData();

		SwingUtilities.invokeLater(() -> panel.updateNames());
	}

	private boolean unsiredReclaiming = false;

	@Subscribe
	public void onGameTick(GameTick t)
	{
		if (unsiredReclaiming)
		{
			checkUnsiredWidget();
		}
	}

	// Handles checking for unsired loot reclamation
	private void checkUnsiredWidget()
	{
		log.info("Checking for text widget change...");
		Widget text = client.getWidget(WidgetInfo.DIALOG_SPRITE_TEXT);
		if (text.getText().toLowerCase().contains("the font consumes the unsired"))
		{
			unsiredReclaiming = false;
			log.info("Text widget changed, reclaimed an item");
			Widget sprite = client.getWidget(WidgetInfo.DIALOG_SPRITE);
			log.info("Sprite Item ID: {}", sprite.getItemId());
			receivedUnsiredLoot(sprite.getItemId());
		}
		else
		{
			log.info("Old text still...");
		}
	}

	// Handles adding the unsired loot to the tracker
	private void receivedUnsiredLoot(int itemID)
	{
		clientThread.invokeLater(() ->
		{
			Collection<LTRecord> data = getDataByName("Abyssal sire");
			ItemComposition c = itemManager.getItemComposition(itemID);
			LTItemEntry itemEntry = new LTItemEntry(c.getName(), itemID, 1, 0);

			log.debug("Received Unsired item: {}", c.getName());

			// Don't have data for sire, create a new record with just this data.
			if (data == null)
			{
				log.debug("No previous Abyssal sire loot, creating new loot record");
				LTRecord r = new LTRecord(NpcID.ABYSSAL_SIRE, "Abyssal sire", 350, -1, Collections.singletonList(itemEntry));
				writer.addLootTrackerRecord(r);
				return;
			}

			log.debug("Adding drop to last abyssal sire loot record");
			// Add data to last kill count
			List<LTRecord> items = new ArrayList<>(data);
			LTRecord r = items.get(items.size() - 1);
			r.addDropEntry(itemEntry);
			writer.writeLootTrackerFile("Abyssal sire", items);
		});
	}

}