/*
 * Copyright (c) 2018, TheStonedTurtle <www.github.com/TheStonedTurtle>
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
package net.runelite.client.plugins.bosslogger;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Provides;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.SwingUtilities;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ConfigChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.Notifier;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.loot.LootTypes;
import net.runelite.client.game.loot.data.ItemStack;
import net.runelite.client.game.loot.events.EventLootReceived;
import net.runelite.client.game.loot.events.NpcLootReceived;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.PluginToolbar;
import net.runelite.client.util.Text;
import net.runelite.http.api.RuneLiteAPI;

import static net.runelite.client.RuneLite.LOOTS_DIR;

@PluginDescriptor(
	name = "Boss Logger"
)
@Slf4j
public class BossLoggerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private BossLoggerConfig bossLoggerConfig;

	@Inject
	private Notifier notifier;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private PluginToolbar pluginToolbar;

	private File playerFolder;	// Where we are storing files
	private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9]+)");
	private static final Pattern BOSS_NAME_PATTERN = Pattern.compile("Your (.*) kill count is:");
	private static final Pattern PET_RECEIVED_PATTERN = Pattern.compile("You have a funny feeling like ");
	private static final Pattern PET_RECEIVED_INVENTORY_PATTERN = Pattern.compile("You feel something weird sneaking into your backpack.");
	private String messageColor = ""; // in-game chat message color

	private BossLoggerPanel panel;
	private NavigationButton navButton;

	// Mapping Variables
	private Map<String, Boolean> recordingMap = new HashMap<>(); 			// Store config recording value by tab boss name
	private Map<String, ArrayList<LootEntry>> lootMap = new HashMap<>();	// Store loot entries by boss name
	private Map<String, Integer> killcountMap = new HashMap<>(); 			// Store boss kill count by name
	private Map<String, String> filenameMap = new HashMap<>(); 				// Stores filename for each boss name

	private boolean gotPet = false;			// Got the pet chat message?

	@Provides
	BossLoggerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BossLoggerConfig.class);
	}

	@Override
	protected void startUp()
	{
		if (bossLoggerConfig.showLootTotals())
		{
			// Waits 2 seconds, helps ensure itemManager is loaded
			// Client cache loading is async, plugins can be loaded before it is finished
			ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
			scheduler.schedule(() -> SwingUtilities.invokeLater(this::createPanel), 2, TimeUnit.SECONDS);
		}
		init();

		// Ensure Loot Directory has been created
		LOOTS_DIR.mkdir();
	}

	@Override
	protected void shutDown() throws Exception
	{
		removePanel();
	}

	@Subscribe
	protected void onEventLootReceived(EventLootReceived e)
	{
		int kc = -1;
		switch (e.getEvent())
		{
			case(LootTypes.BARROWS):
				kc = killcountMap.get("BARROWS");
				break;
			case(LootTypes.CHAMBERS_OF_XERIC):
				kc = killcountMap.get("RAIDS");
				break;
			case(LootTypes.THEATRE_OF_BLOOD):
				kc = killcountMap.get("RAIDS 2");
				break;
			case(LootTypes.CLUE_SCROLL_EASY):
			case(LootTypes.CLUE_SCROLL_MEDIUM):
			case(LootTypes.CLUE_SCROLL_HARD):
			case(LootTypes.CLUE_SCROLL_ELITE):
			case(LootTypes.CLUE_SCROLL_MASTER):
				log.info("Not handling clues currently");
				break;
			case(LootTypes.UNKNOWN_EVENT):
				log.debug("Unknown Event: {}", e);
				break;
			default:
				log.debug("Unhandled Event: {}", e.getEvent());
		}
		if (kc == -1)
			return;

		// Create loot entry and store it to file
		LootEntry entry = new LootEntry(kc, e.getItems());
		// Got a pet?
		if (gotPet)
			entry.drops.add(handlePet(e.getEvent()));
		addLootEntry(e.getEvent(), entry);

		BossLoggedAlert("Loot from " + e.getEvent().toLowerCase() + " added to log.");
	}

	// Check for Unsired loot reclaiming
	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		// Received unsired loot?
		if (event.getGroupId() == WidgetID.DIALOG_SPRITE_GROUP_ID)
		{
			Widget sprite = client.getWidget(WidgetInfo.DIALOG_SPRITE);
			int itemID = sprite.getItemId();
			switch (itemID)
			{
				case ItemID.BLUDGEON_CLAW:
				case ItemID.BLUDGEON_SPINE:
				case ItemID.BLUDGEON_AXON:
				case ItemID.ABYSSAL_DAGGER:
				case ItemID.ABYSSAL_WHIP:
				case ItemID.ABYSSAL_ORPHAN:
				case ItemID.ABYSSAL_HEAD:
					break;
				default:
					return;
			}

			receivedUnsiredLoot(itemID);
		}
	}

	// Only check for Boss NPCs
	@Subscribe
	protected void onNpcLootReceived(NpcLootReceived e)
	{
		String npcName = e.getComposition().getName().toUpperCase();
		// Special Cases
		if (npcName.equals("Dusk"))
		{
			npcName = "GROTESQUE GUARDIANS";
		}
		Boolean recordingFlag = recordingMap.get(npcName);

		if (recordingFlag == null || !recordingFlag)
			return;

		// We are recording this NPC, add the loot to the file
		AddBossLootEntry(e.getComposition().getName(), e.getItems());
	}


	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		// Only update if our plugin config was changed
		if (!event.getGroup().equals("bosslogger"))
		{
			return;
		}

		handleConfigChanged(event.getKey());
	}

	// Chat Message parsing kill count value and/or pet drop
	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.SERVER && event.getType() != ChatMessageType.FILTERED)
		{
			return;
		}

		String chatMessage = event.getMessage();

		// Barrows KC
		if (chatMessage.startsWith("Your Barrows chest count is"))
		{
			Matcher m = NUMBER_PATTERN.matcher(Text.removeTags(chatMessage));
			if (m.find())
			{
				killcountMap.put("BARROWS", Integer.valueOf(m.group()));
				return;
			}
		}

		// Raids KC
		if (chatMessage.startsWith("Your completed Chambers of Xeric count is:"))
		{
			Matcher m = NUMBER_PATTERN.matcher(Text.removeTags(chatMessage));
			if (m.find())
			{
				killcountMap.put("RAIDS", Integer.valueOf(m.group()));
				return;
			}
		}

		// Raids KC
		if (chatMessage.startsWith("Your completed Theatre of Blood count is:"))
		{
			Matcher m = NUMBER_PATTERN.matcher(Text.removeTags(chatMessage));
			if (m.find())
			{
				killcountMap.put("RAIDS 2", Integer.valueOf(m.group()));
				return;
			}
		}

		// Handle all other boss
		Matcher boss = BOSS_NAME_PATTERN.matcher(Text.removeTags(chatMessage));
		if (boss.find())
		{
			String bossName = boss.group(1);
			Matcher m = NUMBER_PATTERN.matcher(Text.removeTags(chatMessage));
			if (!m.find())
				return;
			int KC = Integer.valueOf(m.group());
			killcountMap.put(bossName.toUpperCase(), KC);
		}

		// Pet Drop
		Matcher pet1 = PET_RECEIVED_PATTERN.matcher(Text.removeTags(chatMessage));
		Matcher pet2 = PET_RECEIVED_INVENTORY_PATTERN.matcher(Text.removeTags(chatMessage));
		if (pet1.find() || pet2.find())
		{
			gotPet = true;
		}
	}

	private void resetStoredData()
	{
		for (Tab tab : Tab.values())
		{
			lootMap.put(tab.getBossName().toUpperCase(), new ArrayList<>());
		}
	}

	private void updateTabData()
	{
		// Do nothing is panel isn't enabled
		if (!bossLoggerConfig.showLootTotals())
		{
			return;
		}

		for (Tab tab : Tab.values())
		{
			// Only update tabs if the tabs are being shown.
			if (isBeingRecorded(tab.getName()))
			{
				panel.updateTab(tab.getName());
			}
		}
	}

	private void init()
	{
		// Create maps for easy management of certain features
		Map<String, Boolean> mapRecording = new HashMap<>();
		Map<String, ArrayList<LootEntry>> mapLoot = new HashMap<>();
		Map<String, Integer> mapKillcount = new HashMap<>();
		Map<String, String> mapFilename = new HashMap<>();
		for (Tab tab : Tab.values())
		{
			String bossName = tab.getBossName().toUpperCase();
			// Is Boss being recorded?
			mapRecording.put(bossName, isBeingRecorded(tab.getName()));
			// Loot Entries by Tab Name
			ArrayList<LootEntry> array = new ArrayList<LootEntry>();
			mapLoot.put(bossName, array);
			// Kill Count
			int killcount = 0;
			mapKillcount.put(bossName, killcount);
			// Filenames. Removes all spaces, periods, and apostrophes
			String filename = tab.getName().replaceAll("( |'|\\.)", "").toLowerCase() + ".log";
			mapFilename.put(bossName, filename);
		}
		recordingMap = mapRecording;
		lootMap = mapLoot;
		killcountMap = mapKillcount;
		filenameMap = mapFilename;

		// Ensure we are using the requested message coloring for in-game messages
		updateMessageColor();
	}

	//
	// Panel Functions
	//

	// Separated from startUp for toggling panel from settings
	private void createPanel()
	{
		panel = new BossLoggerPanel(itemManager, this);
		// Panel Icon (Looting Bag)
		BufferedImage icon = null;
		synchronized (ImageIO.class)
		{
			try
			{
				icon = ImageIO.read(getClass().getResourceAsStream("panel_icon.png"));
			}
			catch (IOException e)
			{
				log.warn("Error getting panel icon:", e);
			}
		}

		navButton = NavigationButton.builder()
				.tooltip("Boss Logger")
				.icon(icon)
				.panel(panel)
				.priority(10)
				.build();

		pluginToolbar.addNavigation(navButton);
	}

	private void removePanel()
	{
		pluginToolbar.removeNavigation(navButton);
	}


	// Toggles visibility of tab in side panel
	private void ToggleTab(String tabName, boolean status)
	{
		// Remove panel tab if showing panel
		if (bossLoggerConfig.showLootTotals())
		{
			panel.toggleTab(tabName, status);
		}
		// Update tab map
		String bossName = Tab.getByName(tabName).getBossName().toUpperCase();
		recordingMap.put(bossName, status);
	}



	//
	// General Functionality functions
	//

	// Will use the main loots folder if your ingame username is not available
	private void updatePlayerFolder()
	{
		String old = "";
		if (playerFolder != null)
		{
			old = playerFolder.toString();
		}

		if (client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null)
		{
			playerFolder = new File(LOOTS_DIR, client.getLocalPlayer().getName());
			// Ensure player folder is made
			playerFolder.mkdir();
		}
		else
		{
			playerFolder = LOOTS_DIR;
		}

		// Reset Stored and UI Data on change of data directory
		if (!playerFolder.toString().equals(old))
		{
			resetStoredData();
			updateTabData();
		}
	}

	// All alerts from this plugin should use this function
	private void BossLoggedAlert(String message)
	{
		message = "Boss Logger: " + message;
		if (bossLoggerConfig.showChatMessages())
		{
			final ChatMessageBuilder chatMessage = new ChatMessageBuilder()
					.append("<col=" + messageColor + ">")
					.append(message)
					.append("</col>");

			chatMessageManager.queue(QueuedMessage.builder()
					.type(ChatMessageType.EXAMINE_ITEM)
					.runeLiteFormattedMessage(chatMessage.build())
					.build());
		}

		if (bossLoggerConfig.showTrayAlerts())
		{
			notifier.notify(message);
		}
	}

	void loadTabData(Tab tab)
	{
		loadLootEntries(tab);
	}

	// Load data for all bosses being recorded
	void loadAllData()
	{
		for (Tab tab : Tab.values())
		{
			if (isBeingRecorded(tab.getName()))
			{
				loadLootEntries(tab);
			}
		}
	}

	// Returns stored data by tab name
	ArrayList<LootEntry> getData(String type)
	{
		// Loot Entries are stored on lootMap by boss name (upper cased)
		String name = Tab.getByName(type).getBossName().toUpperCase();
		return lootMap.get(name);
	}

	//
	// LootEntry helper functions
	//

	// Adds the data to the correct boss log file
	private void AddBossLootEntry(String bossName, List<ItemStack> drops)
	{
		if (bossName.toUpperCase().equals("DUSK"))
			bossName = "Grotesque Guardians";
		int KC = killcountMap.get(bossName.toUpperCase());

		LootEntry newEntry = new LootEntry(KC, drops);

		addLootEntry(bossName, newEntry);
		BossLoggedAlert(bossName + " kill added to log.");
	}

	// Add Loot Entry to the necessary file
	private void addLootEntry(String bossName, LootEntry entry)
	{
		// Update data inside plugin
		ArrayList<LootEntry> loots = lootMap.get(bossName.toUpperCase());
		loots.add(entry);
		lootMap.put(bossName.toUpperCase(), loots);
		// Convert entry to JSON
		String dataAsString = RuneLiteAPI.GSON.toJson(entry);
		// Grab file by username or loots directory if not logged in
		updatePlayerFolder();
		String fileName = filenameMap.get(bossName.toUpperCase());
		// Open File and append data
		File lootFile = new File(playerFolder, fileName);
		try
		{
			BufferedWriter file = new BufferedWriter(new FileWriter(String.valueOf(lootFile), true));
			file.append(dataAsString);
			file.newLine();
			file.close();
		}
		catch (IOException ioe)
		{
			log.warn("Error writing loot data in file.", ioe);
		}

		// Update tab if being displayed;
		Tab tab = Tab.getByBossName(bossName);
		if (isBeingRecorded(tab.getName()))
		{
			panel.updateTab(tab.getName());
		}
	}

	private synchronized void clearLootFile(Tab tab)
	{
		String fileName = filenameMap.get(tab.getBossName().toUpperCase());
		File lootFile = new File(playerFolder, fileName);

		try
		{
			BufferedWriter file = new BufferedWriter(new FileWriter(String.valueOf(lootFile), false));
			file.close();
		}
		catch (IOException e)
		{
			log.warn("Error clearing loot data in file.", e);
		}
	}

	// Receive Loot from the necessary file
	private synchronized void loadLootEntries(Tab tab)
	{
		ArrayList<LootEntry> data = new ArrayList<>();
		// Grab target directory (username or loots directory if not logged in)
		updatePlayerFolder();
		String fileName = filenameMap.get(tab.getBossName().toUpperCase());

		// Open File and read line by line
		File file = new File(playerFolder, fileName);
		try (BufferedReader br = new BufferedReader(new FileReader(file)))
		{
			String line;
			while ((line = br.readLine()) != null)
			{
				// Convert JSON to LootEntry and add to data ArrayList
				if (line.length() > 0)
				{
					LootEntry entry = RuneLiteAPI.GSON.fromJson(line, LootEntry.class);
					data.add(entry);
				}
			}

			// Update Loot Map with new data
			lootMap.put(tab.getBossName().toUpperCase(), data);
			// Update Killcount map with latest value
			if (data.size() > 0)
			{
				int killcount = data.get(data.size() - 1).getKillCount();
				killcountMap.put(tab.getBossName().toUpperCase(), killcount);
			}
		}
		catch (FileNotFoundException e)
		{
			log.debug("File not found: " + fileName);
		}
		catch (IOException e)
		{
			log.warn("Unexpected error", e);
		}
	}

	// Add Loot Entry to the necessary file
	private void addDropToLastLootEntry(String bossName, DropEntry newDrop)
	{
		// Update data inside plugin
		ArrayList<LootEntry> loots = lootMap.get(bossName.toUpperCase());
		LootEntry entry = loots.get(loots.size() - 1);
		entry.drops.add(newDrop);
		// Ensure updates are applied, may not be necessary
		loots.add(loots.size() - 1, entry);
		lootMap.put(bossName.toUpperCase(), loots);

		// Grab file by username or loots directory if not logged in
		updatePlayerFolder();
		String fileName = filenameMap.get(bossName.toUpperCase());

		// Rewrite the log file (to update the last loot entry)
		File lootFile = new File(playerFolder, fileName);
		try
		{
			BufferedWriter file = new BufferedWriter(new FileWriter(String.valueOf(lootFile), false));
			for ( LootEntry lootEntry : loots)
			{
				// Convert entry to JSON
				String dataAsString = RuneLiteAPI.GSON.toJson(lootEntry);
				file.append(dataAsString);
				file.newLine();
			}
			file.close();
		}
		catch (IOException ioe)
		{
			log.warn("Error witting loot data in file.", ioe);
		}
		// Update tab if being displayed;
		Tab tab = Tab.getByBossName(bossName);
		if (isBeingRecorded(tab.getName()))
		{
			panel.updateTab(tab.getName());
		}
	}

	// Upon cleaning an Unsired add the item to the previous LootEntry
	private void receivedUnsiredLoot(int itemID)
	{
		DropEntry drop = new DropEntry(itemID, 1);
		// Update the last drop
		addDropToLastLootEntry("Abyssal Sire", drop);
	}

	//
	// Other Helper Functions
	//

	private DropEntry handlePet(String name)
	{
		gotPet = false;
		int petID = getPetIdByNpcName(name);
		BossLoggedAlert("Oh lookie a pet! Don't forget to insure it!");
		return new DropEntry(petID, 1);
	}

	private int getPetIdByNpcName(String name)
	{
		Pet pet = Pet.getByBossName(name);
		if (pet != null)
		{
			return pet.getPetID();
		}
		return -1;
	}

	void clearData(Tab tab)
	{
		log.debug("Clearing data for tab: " + tab.getName());
		clearLootFile(tab);
	}

	// Updates in-game alert chat color based on config settings
	private void updateMessageColor()
	{
		Color c = bossLoggerConfig.chatMessageColor();
		messageColor = String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
	}

	//
	// Config Helper Switch Statements
	//

	// Handles if panel should be shown by Tab Name
	boolean isBeingRecorded(String tabName)
	{
		switch (tabName.toUpperCase())
		{
			case "BARROWS":
				return bossLoggerConfig.recordBarrowsChest();
			case "RAIDS":
				return bossLoggerConfig.recordRaidsChest();
			case "ZULRAH":
				return bossLoggerConfig.recordZulrahKills();
			case "VORKATH":
				return bossLoggerConfig.recordVorkathKills();
			// God Wars Dungeon
			case "ARMADYL":
				return bossLoggerConfig.recordArmadylKills();
			case "BANDOS":
				return bossLoggerConfig.recordBandosKills();
			case "SARADOMIN":
				return bossLoggerConfig.recordSaradominKills();
			case "ZAMMY":
				return bossLoggerConfig.recordZammyKills();
			// Wildy Bosses
			case "VET'ION":
				return bossLoggerConfig.recordVetionKills();
			case "VENENATIS":
				return bossLoggerConfig.recordVenenatisKills();
			case "CALLISTO":
				return bossLoggerConfig.recordCallistoKills();
			case "CHAOS ELEMENTAL":
				return bossLoggerConfig.recordChaosElementalKills();
			case "CHAOS FANATIC":
				return bossLoggerConfig.recordChaosFanaticKills();
			case "CRAZY ARCHAEOLOGIST":
				return bossLoggerConfig.recordCrazyArchaeologistKills();
			case "SCORPIA":
				return bossLoggerConfig.recordScorpiaKills();
			case "KING BLACK DRAGON":
				return bossLoggerConfig.recordKbdKills();
			// Slayer Bosses
			case "SKOTIZO":
				return bossLoggerConfig.recordSkotizoKills();
			case "GROTESQUE GUARDIANS":
				return bossLoggerConfig.recordGrotesqueGuardiansKills();
			case "ABYSSAL SIRE":
				return bossLoggerConfig.recordAbyssalSireKills();
			case "KRAKEN":
				return bossLoggerConfig.recordKrakenKills();
			case "CERBERUS":
				return bossLoggerConfig.recordCerberusKills();
			case "THERMONUCLEAR SMOKE DEVIL":
				return bossLoggerConfig.recordThermonuclearSmokeDevilKills();
			// Other
			case "GIANT MOLE":
				return bossLoggerConfig.recordGiantMoleKills();
			case "KALPHITE QUEEN":
				return bossLoggerConfig.recordKalphiteQueenKills();
			case "CORPOREAL BEAST":
				return bossLoggerConfig.recordCorporealBeastKills();
			case "DAGANNOTH REX":
				return bossLoggerConfig.recordDagannothRexKills();
			case "DAGANNOTH PRIME":
				return bossLoggerConfig.recordDagannothPrimeKills();
			case "DAGANNOTH SUPREME":
				return bossLoggerConfig.recordDagannothSupremeKills();
			case "RAIDS 2":
				return bossLoggerConfig.recordTobChest();
			default:
				return false;
		}
	}

	// Keep the subscribe a bit cleaner, may be a better way to handle this
	private void handleConfigChanged(String eventKey)
	{
		switch (eventKey)
		{
			case "recordBarrowsChest":
				ToggleTab("Barrows", bossLoggerConfig.recordBarrowsChest());
				return;
			case "recordRaidsChest":
				ToggleTab("Raids", bossLoggerConfig.recordRaidsChest());
				return;
			case "recordZulrahKills":
				ToggleTab("Zulrah", bossLoggerConfig.recordZulrahKills());
				return;
			case "recordVorkathKills":
				ToggleTab("Vorkath", bossLoggerConfig.recordVorkathKills());
				return;
			case "recordArmadylKills":
				ToggleTab("Armadyl", bossLoggerConfig.recordArmadylKills());
				return;
			case "recordBandosKills":
				ToggleTab("Bandos", bossLoggerConfig.recordBandosKills());
				return;
			case "recordSaradominKills":
				ToggleTab("Saradomin", bossLoggerConfig.recordSaradominKills());
				return;
			case "recordZammyKills":
				ToggleTab("Zammy", bossLoggerConfig.recordZammyKills());
				return;
			case "recordVetionKills":
				ToggleTab("Vet'ion", bossLoggerConfig.recordVetionKills());
				return;
			case "recordVenenatisKills":
				ToggleTab("Venenatis", bossLoggerConfig.recordVenenatisKills());
				return;
			case "recordCallistoKills":
				ToggleTab("Callisto", bossLoggerConfig.recordCallistoKills());
				return;
			case "recordChaosElementalKills":
				ToggleTab("Chaos Elemental", bossLoggerConfig.recordChaosElementalKills());
				return;
			case "recordChaosFanaticKills":
				ToggleTab("Chaos Fanatic", bossLoggerConfig.recordChaosFanaticKills());
				return;
			case "recordCrazyArchaeologistKills":
				ToggleTab("Crazy Archaeologist", bossLoggerConfig.recordCrazyArchaeologistKills());
				return;
			case "recordScorpiaKills":
				ToggleTab("Scorpia", bossLoggerConfig.recordScorpiaKills());
				return;
			case "recordKbdKills":
				ToggleTab("King Black Dragon", bossLoggerConfig.recordKbdKills());
				return;
			case "recordSkotizoKills":
				ToggleTab("Skotizo", bossLoggerConfig.recordSkotizoKills());
				return;
			case "recordGrotesqueGuardiansKills":
				ToggleTab("Grotesque Guardians", bossLoggerConfig.recordGrotesqueGuardiansKills());
				return;
			case "recordAbyssalSireKills":
				ToggleTab("Abyssal Sire", bossLoggerConfig.recordAbyssalSireKills());
				return;
			case "recordKrakenKills":
				ToggleTab("Kraken", bossLoggerConfig.recordKrakenKills());
				return;
			case "recordCerberusKills":
				ToggleTab("Cerberus", bossLoggerConfig.recordCerberusKills());
				return;
			case "recordThermonuclearSmokeDevilKills":
				ToggleTab("Thermonuclear Smoke Devil", bossLoggerConfig.recordThermonuclearSmokeDevilKills());
				return;
			case "recordGiantMoleKills":
				ToggleTab("Giant Mole", bossLoggerConfig.recordGiantMoleKills());
				return;
			case "recordKalphiteQueenKills":
				ToggleTab("Kalphite Queen", bossLoggerConfig.recordKalphiteQueenKills());
				return;
			case "recordCorporealBeastKills":
				ToggleTab("Corporeal Beast", bossLoggerConfig.recordCorporealBeastKills());
				return;
			case "recordDagannothRexKills":
				ToggleTab("Dagannoth Rex", bossLoggerConfig.recordDagannothRexKills());
				return;
			case "recordDagannothPrimeKills":
				ToggleTab("Dagannoth Prime", bossLoggerConfig.recordDagannothPrimeKills());
				return;
			case "recordDagannothSupremeKills":
				ToggleTab("Dagannoth Supreme", bossLoggerConfig.recordDagannothSupremeKills());
				return;
			case "recordTobChest":
				ToggleTab("Raids 2", bossLoggerConfig.recordTobChest());
				return;
			case "showLootTotals":
				loadAllData();
				if (bossLoggerConfig.showLootTotals())
				{
					createPanel();
				}
				else
				{
					removePanel();
				}
				return;
			case "chatMessageColor":
				// Update in-game alert color
				updateMessageColor();
				BossLoggedAlert("Example Message");
				return;
			default:
				break;
		}
	}
}