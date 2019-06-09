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
package net.runelite.client.plugins.skillcalculator.banked.beans;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.http.api.loottracker.GameItem;

@Getter
public enum Activity
{
	/**
	 * Herblore Activities
	 */
	// Creating Potions
	// Guam
	GUAM_POTION_UNF(ItemID.GUAM_POTION_UNF, CriticalItem.GUAM_LEAF, "Unfinished Potion", Skill.HERBLORE, 1, 0, Secondaries.UNFINISHED_POTION),
	GUAM_TAR(ItemID.GUAM_TAR, CriticalItem.GUAM_LEAF, "Guam tar", Skill.HERBLORE, 19, 30, Secondaries.SWAMP_TAR, true),

	ATTACK_POTION(ItemID.ATTACK_POTION4, CriticalItem.GUAM_LEAF_POTION_UNF, "Attack potion", Skill.HERBLORE, 3, 25, Secondaries.ATTACK_POTION),
	// Marrentil
	MARRENTILL_POTION_UNF(ItemID.MARRENTILL_POTION_UNF, CriticalItem.MARRENTILL, "Unfinished potion", Skill.HERBLORE, 1, 0, Secondaries.UNFINISHED_POTION),
	MARRENTILL_TAR(ItemID.MARRENTILL_TAR, CriticalItem.MARRENTILL, "Marrentill tar", Skill.HERBLORE, 31, 42.5, Secondaries.SWAMP_TAR, true),

	ANTIPOISON(ItemID.ANTIPOISON4, CriticalItem.MARRENTILL_POTION_UNF, "Antipoison", Skill.HERBLORE, 5, 37.5, Secondaries.ANTIPOISON),
	// Tarromin
	TARROMIN_POTION_UNF(ItemID.TARROMIN_POTION_UNF, CriticalItem.TARROMIN, "Unfinished potion", Skill.HERBLORE, 1, 0, Secondaries.UNFINISHED_POTION),
	TARROMIN_TAR(ItemID.TARROMIN_TAR, CriticalItem.TARROMIN, "Tarromin tar", Skill.HERBLORE, 39, 55, Secondaries.SWAMP_TAR, true),

	STRENGTH_POTION(ItemID.STRENGTH_POTION4, CriticalItem.TARROMIN_POTION_UNF, "Strength potion", Skill.HERBLORE, 12, 50, Secondaries.STRENGTH_POTION),
	SERUM_207(ItemID.SERUM_207_4, CriticalItem.TARROMIN_POTION_UNF, "Serum 207", Skill.HERBLORE, 15, 50, Secondaries.SERUM_207),
	// Harralander
	HARRALANDER_POTION_UNF(ItemID.HARRALANDER_POTION_UNF, CriticalItem.HARRALANDER, "Unfinished potion", Skill.HERBLORE, 1, 0, Secondaries.UNFINISHED_POTION),
	HARRALANDER_TAR(ItemID.HARRALANDER_TAR, CriticalItem.HARRALANDER, "Harralander tar", Skill.HERBLORE, 44, 72.5, Secondaries.SWAMP_TAR, true),

	COMPOST_POTION(ItemID.COMPOST_POTION4, CriticalItem.HARRALANDER_POTION_UNF, "Compost potion", Skill.HERBLORE, 21, 60, Secondaries.COMPOST_POTION),
	RESTORE_POTION(ItemID.RESTORE_POTION4, CriticalItem.HARRALANDER_POTION_UNF, "Restore potion", Skill.HERBLORE, 22, 62.5, Secondaries.RESTORE_POTION),
	ENERGY_POTION(ItemID.ENERGY_POTION4, CriticalItem.HARRALANDER_POTION_UNF, "Energy potion", Skill.HERBLORE, 26, 67.5, Secondaries.ENERGY_POTION),
	COMBAT_POTION(ItemID.COMBAT_POTION4, CriticalItem.HARRALANDER_POTION_UNF, "Combat potion", Skill.HERBLORE, 36, 84, Secondaries.COMBAT_POTION),
	// Ranarr Weed
	RANARR_POTION_UNF(ItemID.RANARR_POTION_UNF, CriticalItem.RANARR_WEED, "Unfinished potion", Skill.HERBLORE, 30, 0, Secondaries.UNFINISHED_POTION),
	DEFENCE_POTION(ItemID.DEFENCE_POTION4, CriticalItem.RANARR_POTION_UNF, "Defence potion", Skill.HERBLORE, 30, 75, Secondaries.DEFENCE_POTION),
	PRAYER_POTION(ItemID.PRAYER_POTION4, CriticalItem.RANARR_POTION_UNF, "Prayer potion", Skill.HERBLORE, 38, 87.5, Secondaries.PRAYER_POTION),
	// Toadflax
	TOADFLAX_POTION_UNF(ItemID.TOADFLAX_POTION_UNF, CriticalItem.TOADFLAX, "Unfinished potion", Skill.HERBLORE, 34, 0, Secondaries.UNFINISHED_POTION),
	AGILITY_POTION(ItemID.AGILITY_POTION4, CriticalItem.TOADFLAX_POTION_UNF, "Agility potion", Skill.HERBLORE, 34, 80, Secondaries.AGILITY_POTION),
	SARADOMIN_BREW(ItemID.SARADOMIN_BREW4, CriticalItem.TOADFLAX_POTION_UNF, "Saradomin brew", Skill.HERBLORE, 81, 180, Secondaries.SARADOMIN_BREW),
	// Irit
	IRIT_POTION_UNF(ItemID.IRIT_POTION_UNF, CriticalItem.IRIT_LEAF, "Unfinished potion", Skill.HERBLORE, 45, 0, Secondaries.UNFINISHED_POTION),
	SUPER_ATTACK(ItemID.SUPER_ATTACK4, CriticalItem.IRIT_POTION_UNF, "Super attack", Skill.HERBLORE, 45, 100, Secondaries.SUPER_ATTACK),
	SUPERANTIPOISON(ItemID.SUPERANTIPOISON4, CriticalItem.IRIT_POTION_UNF, "Superantipoison", Skill.HERBLORE, 48, 106.3, Secondaries.SUPERANTIPOISON),
	// Avantoe
	AVANTOE_POTION_UNF(ItemID.AVANTOE_POTION_UNF, CriticalItem.AVANTOE, "Unfinished potion", Skill.HERBLORE, 50, 0, Secondaries.UNFINISHED_POTION),
	FISHING_POTION(ItemID.FISHING_POTION4, CriticalItem.AVANTOE_POTION_UNF, "Fishing potion", Skill.HERBLORE, 50, 112.5, Secondaries.FISHING_POTION),
	SUPER_ENERGY_POTION(ItemID.SUPER_ENERGY3_20549, CriticalItem.AVANTOE_POTION_UNF, "Super energy potion", Skill.HERBLORE, 52, 117.5, Secondaries.SUPER_ENERGY_POTION),
	HUNTER_POTION(ItemID.HUNTER_POTION4, CriticalItem.AVANTOE_POTION_UNF, "Hunter potion", Skill.HERBLORE, 53, 120, Secondaries.HUNTER_POTION),
	// Kwuarm
	KWUARM_POTION_UNF(ItemID.KWUARM_POTION_UNF, CriticalItem.KWUARM, "Unfinished potion", Skill.HERBLORE, 55, 0, Secondaries.UNFINISHED_POTION),
	SUPER_STRENGTH(ItemID.SUPER_STRENGTH4, CriticalItem.KWUARM_POTION_UNF, "Super strength", Skill.HERBLORE, 55, 125, Secondaries.SUPER_STRENGTH),
	// Snapdragon
	SNAPDRAGON_POTION_UNF(ItemID.SNAPDRAGON_POTION_UNF, CriticalItem.SNAPDRAGON, "Unfinished potion", Skill.HERBLORE, 63, 0, Secondaries.UNFINISHED_POTION),
	SUPER_RESTORE(ItemID.SUPER_RESTORE4, CriticalItem.SNAPDRAGON_POTION_UNF, "Super restore", Skill.HERBLORE, 63, 142.5, Secondaries.SUPER_RESTORE),
	SANFEW_SERUM(ItemID.SANFEW_SERUM4, CriticalItem.SNAPDRAGON_POTION_UNF, "Sanfew serum", Skill.HERBLORE, 65, 160, Secondaries.SANFEW_SERUM),
	// Cadantine
	CADANTINE_POTION_UNF(ItemID.CADANTINE_POTION_UNF, CriticalItem.CADANTINE, "Unfinished potion", Skill.HERBLORE, 66, 0, Secondaries.UNFINISHED_POTION),
	SUPER_DEFENCE_POTION(ItemID.SUPER_DEFENCE4, CriticalItem.CADANTINE_POTION_UNF, "Super defence", Skill.HERBLORE, 66, 150, Secondaries.SUPER_DEFENCE_POTION),
	// Lantadyme
	LANTADYME_POTION_UNF(ItemID.LANTADYME_POTION_UNF, CriticalItem.LANTADYME, "Unfinished potion", Skill.HERBLORE, 69, 0, Secondaries.UNFINISHED_POTION),
	ANTIFIRE_POTION(ItemID.ANTIFIRE_POTION4, CriticalItem.LANTADYME_POTION_UNF, "Anti-fire potion", Skill.HERBLORE, 69, 157.5, Secondaries.ANTIFIRE_POTION),
	MAGIC_POTION(ItemID.MAGIC_POTION4, CriticalItem.LANTADYME_POTION_UNF, "Magic potion", Skill.HERBLORE, 76, 172.5, Secondaries.MAGIC_POTION),
	// Dwarf Weed
	DWARF_WEED_POTION_UNF(ItemID.DWARF_WEED_POTION_UNF, CriticalItem.DWARF_WEED, "Unfinished potion", Skill.HERBLORE, 72, 0, Secondaries.UNFINISHED_POTION),
	RANGING_POTION(ItemID.RANGING_POTION4, CriticalItem.DWARF_WEED_POTION_UNF, "Ranging potion", Skill.HERBLORE, 72, 162.5, Secondaries.RANGING_POTION),
	// Torstol
	TORSTOL_POTION_UNF(ItemID.TORSTOL_POTION_UNF, CriticalItem.TORSTOL, "Unfinished potion", Skill.HERBLORE, 78, 0, Secondaries.UNFINISHED_POTION),
	SUPER_COMBAT_POTION(ItemID.SUPER_COMBAT_POTION4, CriticalItem.TORSTOL, "Super combat", Skill.HERBLORE, 90, 150, Secondaries.SUPER_COMBAT_POTION, true),
	ANTIVENOM_PLUS(ItemID.ANTIVENOM4_12913, CriticalItem.TORSTOL, "Anti-venom+", Skill.HERBLORE, 94, 125, Secondaries.ANTIVENOM_PLUS, true),

	ZAMORAK_BREW(ItemID.ZAMORAK_BREW4, CriticalItem.TORSTOL_POTION_UNF, "Zamorak brew", Skill.HERBLORE, 78, 175, Secondaries.ZAMORAK_BREW),

	// Cleaning Grimy Herbs
	CLEAN_GUAM(ItemID.GUAM_LEAF, CriticalItem.GRIMY_GUAM_LEAF, "Clean guam", Skill.HERBLORE, 3, 2.5),
	CLEAN_MARRENTILL(ItemID.MARRENTILL, CriticalItem.GRIMY_MARRENTILL, "Clean marrentill", Skill.HERBLORE, 5, 3.8),
	CLEAN_TARROMIN(ItemID.TARROMIN, CriticalItem.GRIMY_TARROMIN, "Clean tarromin", Skill.HERBLORE, 11, 5),
	CLEAN_HARRALANDER(ItemID.HARRALANDER, CriticalItem.GRIMY_HARRALANDER, "Clean harralander", Skill.HERBLORE, 20, 6.3),
	CLEAN_RANARR_WEED(ItemID.RANARR_WEED, CriticalItem.GRIMY_RANARR_WEED, "Clean ranarr weed", Skill.HERBLORE, 25, 7.5),
	CLEAN_TOADFLAX(ItemID.TOADFLAX, CriticalItem.GRIMY_TOADFLAX, "Clean toadflax", Skill.HERBLORE, 30, 8),
	CLEAN_IRIT_LEAF(ItemID.IRIT_LEAF, CriticalItem.GRIMY_IRIT_LEAF, "Clean irit leaf", Skill.HERBLORE, 40, 8.8),
	CLEAN_AVANTOE(ItemID.AVANTOE, CriticalItem.GRIMY_AVANTOE, "Clean avantoe", Skill.HERBLORE, 48, 10),
	CLEAN_KWUARM(ItemID.KWUARM, CriticalItem.GRIMY_KWUARM, "Clean kwuarm", Skill.HERBLORE, 54, 11.3),
	CLEAN_SNAPDRAGON(ItemID.SNAPDRAGON, CriticalItem.GRIMY_SNAPDRAGON, "Clean snapdragon", Skill.HERBLORE, 59, 11.8),
	CLEAN_CADANTINE(ItemID.CADANTINE, CriticalItem.GRIMY_CADANTINE, "Clean cadantine", Skill.HERBLORE, 65, 12.5),
	CLEAN_LANTADYME(ItemID.LANTADYME, CriticalItem.GRIMY_LANTADYME, "Clean lantadyme", Skill.HERBLORE, 67, 13.1),
	CLEAN_DWARF_WEED(ItemID.DWARF_WEED, CriticalItem.GRIMY_DWARF_WEED, "Clean dwarf weed", Skill.HERBLORE, 70, 13.8),
	CLEAN_TORSTOL(ItemID.TORSTOL, CriticalItem.GRIMY_TORSTOL, "Clean torstol", Skill.HERBLORE, 75, 15),

	/**
	 * Construction Options
	 */
	PLANKS(ItemID.PLANK, CriticalItem.PLANK, "Regular plank products", Skill.CONSTRUCTION, 1, 29),
	OAK_PLANKS(ItemID.OAK_PLANK, CriticalItem.OAK_PLANK, "Oak products", Skill.CONSTRUCTION, 1, 60),
	TEAK_PLANKS(ItemID.TEAK_PLANK, CriticalItem.TEAK_PLANK, "Teak products", Skill.CONSTRUCTION, 1, 90),
	MYTHICAL_CAPE(ItemID.MYTHICAL_CAPE, CriticalItem.TEAK_PLANK, "Mythical cape rakes", Skill.CONSTRUCTION, 1, 123.33),
	MAHOGANY_PLANKS(ItemID.MAHOGANY_PLANK, CriticalItem.MAHOGANY_PLANK, "Mahogany products", Skill.CONSTRUCTION, 1, 140),

	/**
	 * Prayer Options
	 */
	BONES(ItemID.BONES, CriticalItem.BONES, "Bones", Skill.PRAYER, 1, 4.5),
	WOLF_BONES(ItemID.WOLF_BONES, CriticalItem.WOLF_BONES, "Wolf bones", Skill.PRAYER, 1, 4.5),
	BURNT_BONES(ItemID.BURNT_BONES, CriticalItem.BURNT_BONES, "Burnt bones", Skill.PRAYER, 1, 4.5),
	MONKEY_BONES(ItemID.MONKEY_BONES, CriticalItem.MONKEY_BONES, "Monkey bones", Skill.PRAYER, 1, 5.0),
	BAT_BONES(ItemID.BAT_BONES, CriticalItem.BAT_BONES, "Bat bones", Skill.PRAYER, 1, 5.3),
	JOGRE_BONES(ItemID.JOGRE_BONES, CriticalItem.JOGRE_BONES, "Jogre bones", Skill.PRAYER, 1, 15.0),
	BIG_BONES(ItemID.BIG_BONES, CriticalItem.BIG_BONES, "Big bones", Skill.PRAYER, 1, 15.0),
	ZOGRE_BONES(ItemID.ZOGRE_BONES, CriticalItem.ZOGRE_BONES, "Zogre bones", Skill.PRAYER, 1, 22.5),
	SHAIKAHAN_BONES(ItemID.SHAIKAHAN_BONES, CriticalItem.SHAIKAHAN_BONES, "Shaikahan bones", Skill.PRAYER, 1, 25.0),
	BABYDRAGON_BONES(ItemID.BABYDRAGON_BONES, CriticalItem.BABYDRAGON_BONES, "Babydragon bones", Skill.PRAYER, 1, 30.0),
	WYVERN_BONES(ItemID.WYVERN_BONES, CriticalItem.WYVERN_BONES, "Wyvern bones", Skill.PRAYER, 1, 72.0),
	DRAGON_BONES(ItemID.DRAGON_BONES, CriticalItem.DRAGON_BONES, "Dragon bones", Skill.PRAYER, 1, 72.0),
	FAYRG_BONES(ItemID.FAYRG_BONES, CriticalItem.FAYRG_BONES, "Fayrg bones", Skill.PRAYER, 1, 84.0),
	LAVA_DRAGON_BONES(ItemID.LAVA_DRAGON_BONES, CriticalItem.LAVA_DRAGON_BONES, "Lava dragon bones", Skill.PRAYER, 1, 85.0),
	RAURG_BONES(ItemID.RAURG_BONES, CriticalItem.RAURG_BONES, "Raurg bones", Skill.PRAYER, 1, 96.0),
	DAGANNOTH_BONES(ItemID.DAGANNOTH_BONES, CriticalItem.DAGANNOTH_BONES, "Dagannoth bones", Skill.PRAYER, 1, 125.0),
	OURG_BONES(ItemID.OURG_BONES, CriticalItem.OURG_BONES, "Ourg bones", Skill.PRAYER, 1, 140.0),
	SUPERIOR_DRAGON_BONES(ItemID.SUPERIOR_DRAGON_BONES, CriticalItem.SUPERIOR_DRAGON_BONES, "Superior dragon bones", Skill.PRAYER, 1, 150.0),
	// Shade Remains (Pyre Logs)
	LOAR_REMAINS(ItemID.LOAR_REMAINS, CriticalItem.LOAR_REMAINS, "Loar remains", Skill.PRAYER, 1, 33.0),
	PHRIN_REMAINS(ItemID.PHRIN_REMAINS, CriticalItem.PHRIN_REMAINS, "Phrin remains", Skill.PRAYER, 1, 46.5),
	RIYL_REMAINS(ItemID.RIYL_REMAINS, CriticalItem.RIYL_REMAINS, "Riyl remains", Skill.PRAYER, 1, 59.5),
	ASYN_REMAINS(ItemID.ASYN_REMAINS, CriticalItem.ASYN_REMAINS, "Asyn remains", Skill.PRAYER, 1, 82.5),
	FIYR_REMAINS(ItemID.FIYR_REMAINS, CriticalItem.FIYR_REMAINS, "Fiyre remains", Skill.PRAYER, 1, 84.0),
	// Ensouled Heads
	ENSOULED_GOBLIN_HEAD(ItemID.ENSOULED_GOBLIN_HEAD_13448, CriticalItem.ENSOULED_GOBLIN_HEAD, "Ensouled goblin head", Skill.PRAYER, 1, 130.0),
	ENSOULED_MONKEY_HEAD(ItemID.ENSOULED_MONKEY_HEAD_13451, CriticalItem.ENSOULED_MONKEY_HEAD, "Ensouled monkey head", Skill.PRAYER, 1, 182.0),
	ENSOULED_IMP_HEAD(ItemID.ENSOULED_IMP_HEAD_13454, CriticalItem.ENSOULED_IMP_HEAD, "Ensouled imp head", Skill.PRAYER, 1, 286.0),
	ENSOULED_MINOTAUR_HEAD(ItemID.ENSOULED_MINOTAUR_HEAD_13457, CriticalItem.ENSOULED_MINOTAUR_HEAD, "Ensouled minotaur head", Skill.PRAYER, 1, 364.0),
	ENSOULED_SCORPION_HEAD(ItemID.ENSOULED_SCORPION_HEAD_13460, CriticalItem.ENSOULED_SCORPION_HEAD, "Ensouled scorpion head", Skill.PRAYER, 1, 454.0),
	ENSOULED_BEAR_HEAD(ItemID.ENSOULED_BEAR_HEAD_13463, CriticalItem.ENSOULED_BEAR_HEAD, "Ensouled bear head", Skill.PRAYER, 1, 480.0),
	ENSOULED_UNICORN_HEAD(ItemID.ENSOULED_UNICORN_HEAD_13466, CriticalItem.ENSOULED_UNICORN_HEAD, "Ensouled unicorn head", Skill.PRAYER, 1, 494.0),
	ENSOULED_DOG_HEAD(ItemID.ENSOULED_DOG_HEAD_13469, CriticalItem.ENSOULED_DOG_HEAD, "Ensouled dog head", Skill.PRAYER, 1, 520.0),
	ENSOULED_CHAOS_DRUID_HEAD(ItemID.ENSOULED_CHAOS_DRUID_HEAD_13472, CriticalItem.ENSOULED_CHAOS_DRUID_HEAD, "Ensouled druid head", Skill.PRAYER, 1, 584.0),
	ENSOULED_GIANT_HEAD(ItemID.ENSOULED_GIANT_HEAD_13475, CriticalItem.ENSOULED_GIANT_HEAD, "Ensouled giant head", Skill.PRAYER, 1, 650.0),
	ENSOULED_OGRE_HEAD(ItemID.ENSOULED_OGRE_HEAD_13478, CriticalItem.ENSOULED_OGRE_HEAD, "Ensouled ogre head", Skill.PRAYER, 1, 716.0),
	ENSOULED_ELF_HEAD(ItemID.ENSOULED_ELF_HEAD_13481, CriticalItem.ENSOULED_ELF_HEAD, "Ensouled elf head", Skill.PRAYER, 1, 754.0),
	ENSOULED_TROLL_HEAD(ItemID.ENSOULED_TROLL_HEAD_13484, CriticalItem.ENSOULED_TROLL_HEAD, "Ensouled troll head", Skill.PRAYER, 1, 780.0),
	ENSOULED_HORROR_HEAD(ItemID.ENSOULED_HORROR_HEAD_13487, CriticalItem.ENSOULED_HORROR_HEAD, "Ensouled horror head", Skill.PRAYER, 1, 832.0),
	ENSOULED_KALPHITE_HEAD(ItemID.ENSOULED_KALPHITE_HEAD_13490, CriticalItem.ENSOULED_KALPHITE_HEAD, "Ensouled kalphite head", Skill.PRAYER, 1, 884.0),
	ENSOULED_DAGANNOTH_HEAD(ItemID.ENSOULED_DAGANNOTH_HEAD_13493, CriticalItem.ENSOULED_DAGANNOTH_HEAD, "Ensouled dagannoth head", Skill.PRAYER, 1, 936.0),
	ENSOULED_BLOODVELD_HEAD(ItemID.ENSOULED_BLOODVELD_HEAD_13496, CriticalItem.ENSOULED_BLOODVELD_HEAD, "Ensouled bloodveld head", Skill.PRAYER, 1, 1040.0),
	ENSOULED_TZHAAR_HEAD(ItemID.ENSOULED_TZHAAR_HEAD_13499, CriticalItem.ENSOULED_TZHAAR_HEAD, "Ensouled tzhaar head", Skill.PRAYER, 1, 1104.0),
	ENSOULED_DEMON_HEAD(ItemID.ENSOULED_DEMON_HEAD_13502, CriticalItem.ENSOULED_DEMON_HEAD, "Ensouled demon head", Skill.PRAYER, 1, 1170.0),
	ENSOULED_AVIANSIE_HEAD(ItemID.ENSOULED_AVIANSIE_HEAD_13505, CriticalItem.ENSOULED_AVIANSIE_HEAD, "Ensouled aviansie head", Skill.PRAYER, 1, 1234.0),
	ENSOULED_ABYSSAL_HEAD(ItemID.ENSOULED_ABYSSAL_HEAD_13508, CriticalItem.ENSOULED_ABYSSAL_HEAD, "Ensouled abyssal head", Skill.PRAYER, 1, 1300.0),
	ENSOULED_DRAGON_HEAD(ItemID.ENSOULED_DRAGON_HEAD_13511, CriticalItem.ENSOULED_DRAGON_HEAD, "Ensouled dragon head", Skill.PRAYER, 1, 1560.0),

	/*
	 * Cooking Items
	 */
	RAW_HERRING(ItemID.RAW_HERRING, CriticalItem.RAW_HERRING, "Raw herring", Skill.COOKING, 5, 50.0),
	RAW_MACKEREL(ItemID.RAW_MACKEREL, CriticalItem.RAW_MACKEREL, "Raw mackerel", Skill.COOKING, 10, 60.0),
	RAW_TROUT(ItemID.RAW_TROUT, CriticalItem.RAW_TROUT, "Raw trout", Skill.COOKING, 15, 70.0),
	RAW_COD(ItemID.RAW_COD, CriticalItem.RAW_COD, "Raw cod", Skill.COOKING, 18, 75.0),
	RAW_PIKE(ItemID.RAW_PIKE, CriticalItem.RAW_PIKE, "Raw pike", Skill.COOKING, 20, 80.0),
	RAW_SALMON(ItemID.RAW_SALMON, CriticalItem.RAW_SALMON, "Raw salmon", Skill.COOKING, 25, 90.0),
	RAW_TUNA(ItemID.RAW_TUNA, CriticalItem.RAW_TUNA, "Raw tuna", Skill.COOKING, 30, 100.0),
	RAW_KARAMBWAN(ItemID.RAW_KARAMBWAN, CriticalItem.RAW_KARAMBWAN, "Raw karambwan", Skill.COOKING, 30, 190.0),
	RAW_LOBSTER(ItemID.RAW_LOBSTER, CriticalItem.RAW_LOBSTER, "Raw lobster", Skill.COOKING, 40, 120.0),
	RAW_BASS(ItemID.RAW_BASS, CriticalItem.RAW_BASS, "Raw bass", Skill.COOKING, 43, 130.0),
	RAW_SWORDFISH(ItemID.RAW_SWORDFISH, CriticalItem.RAW_SWORDFISH, "Raw swordfish", Skill.COOKING, 45, 140.0),
	RAW_MONKFISH(ItemID.RAW_MONKFISH, CriticalItem.RAW_MONKFISH, "Raw monkfish", Skill.COOKING, 62, 150.0),
	RAW_SHARK(ItemID.RAW_SHARK, CriticalItem.RAW_SHARK, "Raw shark", Skill.COOKING, 80, 210.0),
	RAW_SEA_TURTLE(ItemID.RAW_SEA_TURTLE, CriticalItem.RAW_SEA_TURTLE, "Raw sea turtle", Skill.COOKING, 82, 211.3),
	RAW_ANGLERFISH(ItemID.RAW_ANGLERFISH, CriticalItem.RAW_ANGLERFISH, "Raw anglerfish", Skill.COOKING, 84, 230.0),
	RAW_DARK_CRAB(ItemID.RAW_DARK_CRAB, CriticalItem.RAW_DARK_CRAB, "Raw dark crab", Skill.COOKING, 90, 215.0),
	RAW_MANTA_RAY(ItemID.RAW_MANTA_RAY, CriticalItem.RAW_MANTA_RAY, "Raw manta ray", Skill.COOKING, 91, 216.2),

	WINE(ItemID.JUG_OF_WINE, CriticalItem.GRAPES, "Jug of wine", Skill.COOKING, 35, 200, Secondaries.JUG_OF_WATER),

	/*
	 * Crafting Items
	 */
	// Spinning
	BALL_OF_WOOL(ItemID.WOOL, CriticalItem.WOOL, "Ball of wool", Skill.CRAFTING, 1, 2.5),
	BOW_STRING(ItemID.BOW_STRING, CriticalItem.FLAX, "Bow string", Skill.CRAFTING, 1, 15),
	// Glass Blowing
	BEER_GLASS(ItemID.BEER_GLASS, CriticalItem.MOLTEN_GLASS, "Beer glass", Skill.CRAFTING, 1, 17.5),
	CANDLE_LANTERN(ItemID.CANDLE_LANTERN, CriticalItem.MOLTEN_GLASS, "Candle lantern", Skill.CRAFTING, 4, 19),
	OIL_LAMP(ItemID.OIL_LAMP, CriticalItem.MOLTEN_GLASS, "Oil lamp", Skill.CRAFTING, 12, 25),
	VIAL(ItemID.VIAL, CriticalItem.MOLTEN_GLASS, "Vial", Skill.CRAFTING, 33, 35),
	EMPTY_FISHBOWL(ItemID.EMPTY_FISHBOWL, CriticalItem.MOLTEN_GLASS, "Empty fishbowl", Skill.CRAFTING, 42, 42.5),
	UNPOWERED_ORB(ItemID.UNPOWERED_ORB, CriticalItem.MOLTEN_GLASS, "Unpowered orb", Skill.CRAFTING, 46, 52.5),
	LANTERN_LENS(ItemID.LANTERN_LENS, CriticalItem.MOLTEN_GLASS, "Lantern lens", Skill.CRAFTING, 49, 55),
	LIGHT_ORB(ItemID.LIGHT_ORB, CriticalItem.MOLTEN_GLASS, "Light orb", Skill.CRAFTING, 87, 70),
	// D'hide/Dragon Leather
	GREEN_DRAGON_LEATHER(ItemID.GREEN_DRAGON_LEATHER, CriticalItem.GREEN_DRAGON_LEATHER, "Green D'hide", Skill.CRAFTING, 57, 62.0),
	BLUE_DRAGON_LEATHER(ItemID.BLUE_DRAGON_LEATHER, CriticalItem.BLUE_DRAGON_LEATHER, "Blue D'hide", Skill.CRAFTING, 66, 70.0),
	RED_DRAGON_LEATHER(ItemID.RED_DRAGON_LEATHER, CriticalItem.RED_DRAGON_LEATHER, "Red D'hide", Skill.CRAFTING, 73, 78.0),
	BLACK_DRAGON_LEATHER(ItemID.BLACK_DRAGON_LEATHER, CriticalItem.BLACK_DRAGON_LEATHER, "Black D'hide", Skill.CRAFTING, 79, 86.0),
	// Uncut Gems
	UNCUT_OPAL(ItemID.UNCUT_OPAL, CriticalItem.UNCUT_OPAL, "Uncut opal", Skill.CRAFTING, 1, 15.0),
	UNCUT_JADE(ItemID.UNCUT_JADE, CriticalItem.UNCUT_JADE, "Uncut jade", Skill.CRAFTING, 13, 20.0),
	UNCUT_RED_TOPAZ(ItemID.UNCUT_RED_TOPAZ, CriticalItem.UNCUT_RED_TOPAZ, "Uncut red topaz", Skill.CRAFTING, 16, 25.0),
	UNCUT_SAPPHIRE(ItemID.UNCUT_SAPPHIRE, CriticalItem.UNCUT_SAPPHIRE, "Uncut sapphire", Skill.CRAFTING, 20, 50.0),
	UNCUT_EMERALD(ItemID.UNCUT_EMERALD, CriticalItem.UNCUT_EMERALD, "Uncut emerald", Skill.CRAFTING, 27, 67.5),
	UNCUT_RUBY(ItemID.UNCUT_RUBY, CriticalItem.UNCUT_RUBY, "Uncut ruby", Skill.CRAFTING, 34, 85),
	UNCUT_DIAMOND(ItemID.UNCUT_DIAMOND, CriticalItem.UNCUT_DIAMOND, "Uncut diamond", Skill.CRAFTING, 43, 107.5),
	UNCUT_DRAGONSTONE(ItemID.UNCUT_DRAGONSTONE, CriticalItem.UNCUT_DRAGONSTONE, "Uncut dragonstone", Skill.CRAFTING, 55, 137.5),
	UNCUT_ONYX(ItemID.UNCUT_ONYX, CriticalItem.UNCUT_ONYX, "Uncut onyx", Skill.CRAFTING, 67, 167.5),
	UNCUT_ZENYTE(ItemID.UNCUT_ZENYTE, CriticalItem.UNCUT_ZENYTE, "Uncut zenyte", Skill.CRAFTING, 89, 200.0),
	// Silver Jewelery
	OPAL_RING(ItemID.OPAL_RING, CriticalItem.OPAL, "Opal ring", Skill.CRAFTING, 1 , 10, Secondaries.SILVER_BAR),
	OPAL_NECKLACE(ItemID.OPAL_NECKLACE, CriticalItem.OPAL, "Opal necklace", Skill.CRAFTING, 16 , 35, Secondaries.SILVER_BAR),
	OPAL_BRACELET(ItemID.OPAL_BRACELET, CriticalItem.OPAL, "Opal bracelet", Skill.CRAFTING, 22 , 45, Secondaries.SILVER_BAR),
	OPAL_AMULET(ItemID.OPAL_AMULET, CriticalItem.OPAL, "Opal amulet", Skill.CRAFTING, 27 , 55, Secondaries.SILVER_BAR),
	JADE_RING(ItemID.JADE_RING, CriticalItem.JADE, "Jade ring", Skill.CRAFTING, 13 , 32, Secondaries.SILVER_BAR),
	JADE_NECKLACE(ItemID.JADE_NECKLACE, CriticalItem.JADE, "Jade necklace", Skill.CRAFTING, 25 , 54, Secondaries.SILVER_BAR),
	JADE_BRACELET(ItemID.JADE_BRACELET, CriticalItem.JADE, "Jade bracelet", Skill.CRAFTING, 29 , 60, Secondaries.SILVER_BAR),
	JADE_AMULET(ItemID.JADE_AMULET, CriticalItem.JADE, "Jade amulet", Skill.CRAFTING, 34 , 70, Secondaries.SILVER_BAR),
	TOPAZ_RING(ItemID.TOPAZ_RING, CriticalItem.RED_TOPAZ, "Topaz ring", Skill.CRAFTING, 16 , 35, Secondaries.SILVER_BAR),
	TOPAZ_NECKLACE(ItemID.TOPAZ_NECKLACE, CriticalItem.RED_TOPAZ, "Topaz necklace", Skill.CRAFTING, 32 , 70, Secondaries.SILVER_BAR),
	TOPAZ_BRACELET(ItemID.TOPAZ_BRACELET, CriticalItem.RED_TOPAZ, "Topaz bracelet", Skill.CRAFTING, 38 , 75, Secondaries.SILVER_BAR),
	TOPAZ_AMULET(ItemID.TOPAZ_AMULET, CriticalItem.RED_TOPAZ, "Topaz amulet", Skill.CRAFTING, 45 , 80, Secondaries.SILVER_BAR),
	// Gold Jewelery
	SAPPHIRE_RING(ItemID.SAPPHIRE_RING, CriticalItem.SAPPHIRE, "Sapphire ring", Skill.CRAFTING, 20 , 40, Secondaries.GOLD_BAR),
	SAPPHIRE_NECKLACE(ItemID.SAPPHIRE_NECKLACE, CriticalItem.SAPPHIRE, "Sapphire necklace", Skill.CRAFTING, 22 , 55, Secondaries.GOLD_BAR),
	SAPPHIRE_BRACELET(ItemID.SAPPHIRE_BRACELET, CriticalItem.SAPPHIRE, "Sapphire bracelet", Skill.CRAFTING, 23 , 60, Secondaries.GOLD_BAR),
	SAPPHIRE_AMULET(ItemID.SAPPHIRE_AMULET, CriticalItem.SAPPHIRE, "Sapphire amulet", Skill.CRAFTING, 24 , 65, Secondaries.GOLD_BAR),
	EMERALD_RING(ItemID.EMERALD_RING, CriticalItem.EMERALD, "Emerald ring", Skill.CRAFTING, 27 , 55, Secondaries.GOLD_BAR),
	EMERALD_NECKLACE(ItemID.EMERALD_NECKLACE, CriticalItem.EMERALD, "Emerald necklace", Skill.CRAFTING, 29 , 60, Secondaries.GOLD_BAR),
	EMERALD_BRACELET(ItemID.EMERALD_BRACELET, CriticalItem.EMERALD, "Emerald bracelet", Skill.CRAFTING, 30 , 65, Secondaries.GOLD_BAR),
	EMERALD_AMULET(ItemID.EMERALD_AMULET, CriticalItem.EMERALD, "Emerald amulet", Skill.CRAFTING, 31 , 70, Secondaries.GOLD_BAR),
	RUBY_RING(ItemID.RUBY_RING, CriticalItem.RUBY, "Ruby ring", Skill.CRAFTING, 34 , 70, Secondaries.GOLD_BAR),
	RUBY_NECKLACE(ItemID.RUBY_NECKLACE, CriticalItem.RUBY, "Ruby necklace", Skill.CRAFTING, 40 , 75, Secondaries.GOLD_BAR),
	RUBY_BRACELET(ItemID.RUBY_BRACELET, CriticalItem.RUBY, "Ruby bracelet", Skill.CRAFTING, 42 , 80, Secondaries.GOLD_BAR),
	RUBY_AMULET(ItemID.RUBY_AMULET, CriticalItem.RUBY, "Ruby amulet", Skill.CRAFTING, 50 , 85, Secondaries.GOLD_BAR),
	DIAMOND_RING(ItemID.DIAMOND_RING, CriticalItem.DIAMOND, "Diamond ring", Skill.CRAFTING, 43 , 85, Secondaries.GOLD_BAR),
	DIAMOND_NECKLACE(ItemID.DIAMOND_NECKLACE, CriticalItem.DIAMOND, "Diamond necklace", Skill.CRAFTING, 56 , 90, Secondaries.GOLD_BAR),
	DIAMOND_BRACELET(ItemID.DIAMOND_BRACELET, CriticalItem.DIAMOND, "Diamond bracelet", Skill.CRAFTING, 58 , 95, Secondaries.GOLD_BAR),
	DIAMOND_AMULET(ItemID.DIAMOND_AMULET, CriticalItem.DIAMOND, "Diamond amulet", Skill.CRAFTING, 70 , 100, Secondaries.GOLD_BAR),
	DRAGONSTONE_RING(ItemID.DRAGONSTONE_RING, CriticalItem.DRAGONSTONE, "Dragonstone ring", Skill.CRAFTING, 55 , 100, Secondaries.GOLD_BAR),
	DRAGON_NECKLACE(ItemID.DRAGON_NECKLACE, CriticalItem.DRAGONSTONE, "Dragon necklace", Skill.CRAFTING, 72 , 105, Secondaries.GOLD_BAR),
	DRAGONSTONE_BRACELET(ItemID.DRAGONSTONE_BRACELET, CriticalItem.DRAGONSTONE, "Dragonstone bracelet", Skill.CRAFTING, 74 , 110, Secondaries.GOLD_BAR),
	DRAGONSTONE_AMULET(ItemID.DRAGONSTONE_AMULET, CriticalItem.DRAGONSTONE, "Dragonstone amulet", Skill.CRAFTING, 80 , 150, Secondaries.GOLD_BAR),
	ONYX_RING(ItemID.ONYX_RING, CriticalItem.ONYX, "Onyx ring", Skill.CRAFTING, 67 , 115, Secondaries.GOLD_BAR),
	ONYX_NECKLACE(ItemID.ONYX_NECKLACE, CriticalItem.ONYX, "Onyx necklace", Skill.CRAFTING, 82 , 120, Secondaries.GOLD_BAR),
	REGEN_BRACELET(ItemID.REGEN_BRACELET, CriticalItem.ONYX, "Regen bracelet", Skill.CRAFTING, 84 , 125, Secondaries.GOLD_BAR),
	ONYX_AMULET(ItemID.ONYX_AMULET, CriticalItem.ONYX, "Onyx amulet", Skill.CRAFTING, 90 , 165, Secondaries.GOLD_BAR),
	ZENYTE_RING(ItemID.ZENYTE_RING, CriticalItem.ZENYTE, "Zenyte ring", Skill.CRAFTING, 89 , 150, Secondaries.GOLD_BAR),
	ZENYTE_NECKLACE(ItemID.ZENYTE_NECKLACE, CriticalItem.ZENYTE, "Zenyte necklace", Skill.CRAFTING, 92 , 165, Secondaries.GOLD_BAR),
	ZENYTE_BRACELET(ItemID.ZENYTE_BRACELET, CriticalItem.ZENYTE, "Zenyte bracelet", Skill.CRAFTING, 95 , 180, Secondaries.GOLD_BAR),
	ZENYTE_AMULET(ItemID.ZENYTE_AMULET, CriticalItem.ZENYTE, "Zenyte amulet", Skill.CRAFTING, 98 , 200 , Secondaries.GOLD_BAR),
	// Battle Staves
	WATER_BATTLESTAFF(ItemID.WATER_BATTLESTAFF, CriticalItem.BATTLESTAFF, "Water battlestaff", Skill.CRAFTING, 54, 100, Secondaries.WATER_ORB),
	EARTH_BATTLESTAFF(ItemID.EARTH_BATTLESTAFF, CriticalItem.BATTLESTAFF, "Earth battlestaff", Skill.CRAFTING, 58, 112.5, Secondaries.EARTH_ORB),
	FIRE_BATTLESTAFF(ItemID.FIRE_BATTLESTAFF, CriticalItem.BATTLESTAFF, "Fire battlestaff", Skill.CRAFTING, 62, 125, Secondaries.FIRE_ORB),
	AIR_BATTLESTAFF(ItemID.AIR_BATTLESTAFF, CriticalItem.BATTLESTAFF, "Air battlestaff", Skill.CRAFTING, 66, 137.5, Secondaries.AIR_ORB),

	/*
	 * Smithing Items
	 */

	// Smelting ores (Furnace)
	IRON_ORE(ItemID.IRON_BAR, CriticalItem.IRON_ORE, "Iron bar", Skill.SMITHING, 15, 12.5, Secondaries.COAL_ORE),
	STEEL_ORE(ItemID.STEEL_BAR, CriticalItem.IRON_ORE, "Steel bar", Skill.SMITHING, 30, 17.5, Secondaries.COAL_ORE_2),
	SILVER_ORE(ItemID.SILVER_ORE, CriticalItem.SILVER_ORE, "Silver Bar", Skill.SMITHING, 20, 13.67),
	GOLD_ORE(ItemID.GOLD_BAR, CriticalItem.GOLD_ORE, "Gold bar", Skill.SMITHING, 40, 22.5),
	GOLD_ORE_GAUNTLETS(ItemID.GOLDSMITH_GAUNTLETS, CriticalItem.GOLD_ORE, "Goldsmith gauntlets", Skill.SMITHING, 40, 56.2),
	MITHRIL_ORE(ItemID.MITHRIL_ORE, CriticalItem.MITHRIL_ORE, "Mithril bar", Skill.SMITHING, 50, 30, Secondaries.COAL_ORE_4),
	ADAMANTITE_ORE(ItemID.ADAMANTITE_ORE, CriticalItem.ADAMANTITE_ORE, "Adamantite bar", Skill.SMITHING, 70, 37.5, Secondaries.COAL_ORE_6),
	RUNITE_ORE(ItemID.RUNITE_ORE, CriticalItem.RUNITE_ORE, "Runite bar", Skill.SMITHING, 85, 50, Secondaries.COAL_ORE_8),

	// Smelting bars (Anvil)
	BRONZE_BAR(ItemID.BRONZE_BAR, CriticalItem.BRONZE_BAR, "Bronze products", Skill.SMITHING, 1, 12.5),
	IRON_BAR(ItemID.IRON_BAR, CriticalItem.IRON_BAR, "Iron products", Skill.SMITHING, 15, 25.0),
	STEEL_BAR(ItemID.STEEL_BAR, CriticalItem.STEEL_BAR, "Steel products", Skill.SMITHING, 30, 37.5),
	CANNONBALLS(ItemID.CANNONBALL, CriticalItem.STEEL_BAR, "Cannonballs", Skill.SMITHING, 35, 25.5),
	MITHRIL_BAR(ItemID.MITHRIL_BAR, CriticalItem.MITHRIL_BAR, "Mithril products", Skill.SMITHING, 50, 50.0),
	ADAMANTITE_BAR(ItemID.ADAMANTITE_BAR, CriticalItem.ADAMANTITE_BAR, "Adamantite products", Skill.SMITHING, 70, 62.5),
	RUNITE_BAR(ItemID.RUNITE_BAR, CriticalItem.RUNITE_BAR, "Runite products", Skill.SMITHING, 85, 75.0),

	/**
	 * Farming Items
	 */
	ACORN(ItemID.ACORN, CriticalItem.ACORN, "Acorn", Skill.FARMING, 15, 481.3),
	WILLOW_SEED(ItemID.WILLOW_SEED, CriticalItem.WILLOW_SEED, "Willow seed", Skill.FARMING, 30, 1481.5),
	MAPLE_SEED(ItemID.MAPLE_SEED, CriticalItem.MAPLE_SEED, "Maple seed", Skill.FARMING, 45, 3448.4),
	YEW_SEED(ItemID.YEW_SEED, CriticalItem.YEW_SEED, "Yew seed", Skill.FARMING, 60, 7150.9),
	MAGIC_SEED(ItemID.MAGIC_SEED, CriticalItem.MAGIC_SEED, "Magic seed", Skill.FARMING, 75, 13913.8),
	APPLE_TREE_SEED(ItemID.APPLE_TREE_SEED, CriticalItem.APPLE_TREE_SEED, "Apple tree seed", Skill.FARMING, 27, 1272.5),
	BANANA_TREE_SEED(ItemID.BANANA_TREE_SEED, CriticalItem.BANANA_TREE_SEED, "Banana tree seed", Skill.FARMING, 33, 1841.5),
	ORANGE_TREE_SEED(ItemID.ORANGE_TREE_SEED, CriticalItem.ORANGE_TREE_SEED, "Orange tree seed", Skill.FARMING, 39, 2586.7),
	CURRY_TREE_SEED(ItemID.CURRY_TREE_SEED, CriticalItem.CURRY_TREE_SEED, "Curry tree seed", Skill.FARMING, 42, 3036.9),
	PINEAPPLE_SEED(ItemID.PINEAPPLE_SEED, CriticalItem.PINEAPPLE_SEED, "Pineapple seed", Skill.FARMING, 51, 4791.7),
	PAPAYA_TREE_SEED(ItemID.PAPAYA_TREE_SEED, CriticalItem.PAPAYA_TREE_SEED, "Papaya tree seed", Skill.FARMING, 57, 6380.4),
	PALM_TREE_SEED(ItemID.PALM_TREE_SEED, CriticalItem.PALM_TREE_SEED, "Palm tree seed", Skill.FARMING, 68, 10509.6),
	CALQUAT_TREE_SEED(ItemID.CALQUAT_TREE_SEED, CriticalItem.CALQUAT_TREE_SEED, "Calquat tree seed", Skill.FARMING, 72, 12516.5),
	TEAK_SEED(ItemID.TEAK_SEED, CriticalItem.TEAK_SEED, "Teak seed", Skill.FARMING, 35, 7325),
	MAHOGANY_SEED(ItemID.MAHOGANY_SEED, CriticalItem.MAHOGANY_SEED, "Mahogany seed", Skill.FARMING, 55, 15783),
	SPIRIT_SEED(ItemID.SPIRIT_SEED, CriticalItem.SPIRIT_SEED, "Spirit seed", Skill.FARMING, 83, 19500),
	;

	private final int icon;
	private final CriticalItem criticalItem;
	private final Skill skill;
	private final String name;
	private final int level;
	private final double xp;
	private final GameItem[] secondaries;
	private final boolean preventLinked;

	// Store activity by CriticalItem
	private static final Multimap<CriticalItem, Activity> CRITICAL_MAP = ArrayListMultimap.create();
	static
	{
		for (Activity item : values())
		{
			CRITICAL_MAP.put(item.getCriticalItem(), item);
		}
	}

	Activity(int icon, CriticalItem criticalItem, String name, Skill skill, int level, double xp, Secondaries secondaries, boolean preventLinked)
	{
		this.icon = icon;
		this.name = name;
		this.skill = skill;
		this.level = level;
		this.xp = xp;
		this.criticalItem = criticalItem;
		this.secondaries = secondaries == null ? new GameItem[0] : secondaries.getItems();
		this.preventLinked = preventLinked;
	}

	Activity(int icon, CriticalItem criticalItem, String name, Skill skill, int level, double xp, Secondaries secondaries)
	{
		this(icon, criticalItem, name, skill, level, xp, secondaries, false);
	}

	Activity(int icon, CriticalItem criticalItem, String name, Skill skill, int level, double xp)
	{
		this(icon, criticalItem, name, skill, level, xp, null, false);
	}

	/**
	 * Get all Activities for this CriticalItem
	 * @param item CriticalItem to check for
	 * @return an empty Collection if no activities
	 */
	public static Collection<Activity> getByCriticalItem(CriticalItem item)
	{
		Collection<Activity> activities = CRITICAL_MAP.get(item);
		if (activities == null)
		{
			return new ArrayList<>();
		}

		return activities;
	}

	/**
	 * Get all Activities for this CriticalItem limited to level
	 * @param item CriticalItem to check for
	 * @param limitLevel Level to check Activitiy requirements against. -1/0 value disables limits
	 * @return an empty Collection if no activities
	 */
	public static Collection<Activity> getByCriticalItem(final CriticalItem item, final int limitLevel)
	{
		// Return as list to allow getting by index
		final Collection<Activity> l = getByCriticalItem(item);
		if (limitLevel <= 0)
		{
			return l;
		}

		return l.stream().filter(a -> a.getLevel() <= limitLevel).collect(Collectors.toList());
	}
}
