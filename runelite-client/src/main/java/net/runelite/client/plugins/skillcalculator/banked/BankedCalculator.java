/*
 * Copyright (c) 2019, TheStonedTurtle <https://github.com/TheStonedTurtle>
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
package net.runelite.client.plugins.skillcalculator.banked;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.Skill;
import net.runelite.client.game.AsyncBufferedImage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.skillcalculator.UICalculatorInputArea;
import net.runelite.client.plugins.skillcalculator.banked.beans.Activity;
import net.runelite.client.plugins.skillcalculator.banked.beans.BankedItem;
import net.runelite.client.plugins.skillcalculator.banked.beans.CriticalItem;
import net.runelite.client.plugins.skillcalculator.banked.components.GridItem;
import net.runelite.client.plugins.skillcalculator.banked.components.ModifyPanel;
import net.runelite.client.plugins.skillcalculator.banked.components.SelectionGrid;
import net.runelite.client.ui.DynamicGridLayout;

@Slf4j
public class BankedCalculator extends JPanel
{
	public static final DecimalFormat XP_FORMAT_COMMA = new DecimalFormat("#,###.#");

	private final Client client;
	private final UICalculatorInputArea uiInput;
	private final ItemManager itemManager;

	// Some activities output a CriticalItem and may need to be included in the calculable qty
	// Using multimap for cases where there are multiple items linked directly to one item, use recursion for otherwise
	private final Multimap<CriticalItem, BankedItem> linkedMap = ArrayListMultimap.create();

	private final Map<CriticalItem, BankedItem> bankedItemMap = new LinkedHashMap<>();
	private final JLabel totalXpLabel = new JLabel();
	private final ModifyPanel modifyPanel;
	private SelectionGrid itemGrid;

	@Setter
	private Map<Integer, Integer> bankMap = new HashMap<>();

	@Getter
	private Skill currentSkill;

	@Getter
	private int skillLevel, skillExp, endLevel, endExp;

	// TODO: Add support for xp modifiers (prayer altars, outfit, etc)
	private float xpFactor = 1.0f;

	BankedCalculator(UICalculatorInputArea uiInput, Client client, ItemManager itemManager)
	{
		this.uiInput = uiInput;
		this.client = client;
		this.itemManager = itemManager;

		setLayout(new DynamicGridLayout(0, 1, 0, 5));

		// Panel used to modify banked item values
		this.modifyPanel = new ModifyPanel(this, itemManager);
	}

	/**
	 * opens the Banked Calculator for this skill
	 */
	void open(final Skill newSkill)
	{
		if (newSkill.equals(currentSkill))
		{
			return;
		}

		this.currentSkill = newSkill;
		removeAll();

		if (bankMap.size() <= 0)
		{
			add(new JLabel( "Please visit a bank!", JLabel.CENTER));
			revalidate();
			repaint();
			return;
		}

		skillLevel = client.getRealSkillLevel(currentSkill);
		skillExp =  client.getSkillExperience(currentSkill);
		endLevel = skillLevel;
		endExp = skillExp;

		uiInput.setCurrentLevelInput(skillLevel);
		uiInput.setCurrentXPInput(skillExp);
		uiInput.setTargetLevelInput(endLevel);
		uiInput.setTargetXPInput(endExp);

		recreateBankedItemMap();

		recreateItemGrid();

		// This should only be null if there are no items in their bank for this skill
		if (itemGrid.getSelectedItem() == null)
		{
			add(new JLabel( "Couldn't find any items for this skill.", JLabel.CENTER));
		}
		else
		{
			add(totalXpLabel);
			add(modifyPanel);
			add(itemGrid);
		}

		revalidate();
		repaint();
	}

	private void recreateBankedItemMap()
	{
		bankedItemMap.clear();
		linkedMap.clear();

		final Collection<CriticalItem> items = CriticalItem.getBySkill(currentSkill);
		log.info("Critical Items for the {} Skill: {}", currentSkill.getName(), items);

		for (final CriticalItem item : items)
		{
			final BankedItem banked = new BankedItem(item, bankMap.getOrDefault(item.getItemID(), 0));
			bankedItemMap.put(item, banked);

			Activity a = item.getSelectedActivity();
			if (a == null)
			{
				final List<Activity> activities = Activity.getByCriticalItem(item);
				if (activities.size() == 0)
				{
					continue;
				}

				item.setSelectedActivity(activities.get(0));
				a = activities.get(0);
			}

			if (a.getLinkedItem() != null)
			{
				linkedMap.put(a.getLinkedItem(), banked);
			}
		}
		log.info("Banked Item Map: {}", bankedItemMap);
		log.info("Linked Map: {}", linkedMap);
	}

	/**
	 * Populates the detailContainer with the necessary BankedItemPanels
	 */
	private void recreateItemGrid()
	{
		// TODO: Filter items to display based on quantity.
		itemGrid = new SelectionGrid(this, bankedItemMap.values(), itemManager);
		itemGrid.setOnSelectEvent(() ->
		{
			modifyPanel.setBankedItem(itemGrid.getSelectedItem());
			return true;
		});

		itemGrid.setOnIgnoreEvent(() ->
		{
			CriticalItem item = itemGrid.getLastIgnoredItem().getItem();
			updateLinkedItems(item.getSelectedActivity());
			calculateBankedXpTotal();
			return true;
		});

		// Select the first item in the list
		modifyPanel.setBankedItem(itemGrid.getSelectedItem());

		calculateBankedXpTotal();
	}

	public double getItemXpRate(final BankedItem bankedItem)
	{
		return bankedItem.getXpRate() * (bankedItem.getItem().isIgnoreBonus() ? 1.0f : xpFactor);
	}

	/**
	 * Calculates total item quantity accounting for backwards linked items
	 * @param item starting item
	 * @return item qty including linked items
	 */
	public int getItemQty(final BankedItem item)
	{
		int qty = item.getQty();

		// TODO: Add config check
		if (false)
		{
			return qty;
		}

		final Map<CriticalItem, Integer> linked = createLinksMap(item);
		final int linkedQty = linked.values().stream().mapToInt(Integer::intValue).sum();
		log.info("{} : {}", item, linkedQty);

		return qty + linkedQty;
	}

	private void calculateBankedXpTotal()
	{
		double total = 0.0;
		for (final GridItem i : itemGrid.getPanelMap().values())
		{
			if (i.isIgnored())
			{
				continue;
			}

			final BankedItem bi = i.getBankedItem();
			total += getItemQty(bi) * getItemXpRate(bi);
		}

		endExp = (int) (skillExp + total);
		endLevel = Experience.getLevelForXp(endExp);

		totalXpLabel.setText("Total Banked xp: " + XP_FORMAT_COMMA.format(total));
		uiInput.setTargetLevelInput(endLevel);
		uiInput.setTargetXPInput(Math.min(Experience.MAX_SKILL_XP, endExp));

		revalidate();
		repaint();
	}

	/**
	 * Used to select an Activity for an item
	 * @param i BankedItem item the activity is tied to
	 * @param a Activity the selected activity
	 */
	public void activitySelected(final BankedItem i, final Activity a)
	{
		final CriticalItem item = i.getItem();
		final Activity old = item.getSelectedActivity();
		if (a.equals(old))
		{
			return;
		}

		item.setSelectedActivity(a);

		// TODO: Add config check
		// Cascade activity changes if necessary.
		if (true && (old.getLinkedItem() != a.getLinkedItem()))
		{
			// Update Linked Map
			linkedMap.remove(old.getLinkedItem(), i);
			linkedMap.put(a.getLinkedItem(), i);
			// Update all items the old activity effects
			updateLinkedItems(old);
			// Update all the items the new activity effects
			updateLinkedItems(a);
		}

		modifyPanel.setBankedItem(i);
		itemGrid.getPanelMap().get(i).updateToolTip();

		// recalculate total xp
		calculateBankedXpTotal();
	}

	/**
	 * Updates the item quantities of all forward linked items
	 * @param activity the starting {@link Activity} to start the cascade from
	 */
	private void updateLinkedItems(final Activity activity)
	{
		if (activity == null)
		{
			return;
		}

		boolean foundSelected = false;
		boolean panelAmountChange = false;

		CriticalItem i = activity.getLinkedItem();
		while (i != null)
		{
			final BankedItem bi = bankedItemMap.get(i);
			if (bi == null)
			{
				break;
			}

			final int qty = getItemQty(bi);
			final boolean stackable = bi.getItem().getItemInfo().isStackable() || qty > 1;
			final AsyncBufferedImage img = itemManager.getImage(bi.getItem().getItemID(), qty, stackable);

			final GridItem gridItem = itemGrid.getPanelMap().get(bi);
			final int oldQty = gridItem.getAmount();
			panelAmountChange = panelAmountChange || ( (oldQty == 0 && qty > 0) || (oldQty > 0 && qty == 0) );
			gridItem.updateIcon(img, qty);
			gridItem.updateToolTip();

			foundSelected = foundSelected || itemGrid.getSelectedItem().equals(bi);

			final Activity a = bi.getItem().getSelectedActivity();
			if (a == null)
			{
				break;
			}

			i = a.getLinkedItem();
		}

		if (panelAmountChange)
		{
			itemGrid.refreshGridDisplay();
		}

		if (foundSelected)
		{
			// Refresh current modify panel if the cascade effects it
			modifyPanel.setBankedItem(itemGrid.getSelectedItem());
		}
	}

	/**
	 * Creates a Map of CriticalItem to bank qty for all items that are being linked to this one
	 * @param item starting item
	 * @return Map of CriticalItem to bank qty
	 */
	public Map<CriticalItem, Integer> createLinksMap(final BankedItem item)
	{
		final Map<CriticalItem, Integer> qtyMap = new HashMap<>();

		final Activity a = item.getItem().getSelectedActivity();
		if (a == null)
		{
			return qtyMap;
		}

		final Collection<BankedItem> linkedBank = linkedMap.get(item.getItem());
		if (linkedBank == null || linkedBank.size() == 0)
		{
			return qtyMap;
		}

		for (final BankedItem linked : linkedBank)
		{
			// Check if the item is ignored in the grid
			if (itemGrid != null)
			{
				final GridItem grid = itemGrid.getPanelMap().get(linked);
				if (grid != null && grid.isIgnored())
				{
					continue;
				}
			}

			final int qty = linked.getQty();
			if (qty > 0)
			{
				qtyMap.put(linked.getItem(), qty);
			}
			qtyMap.putAll(createLinksMap(linked));
		}

		return qtyMap;
	}
}
