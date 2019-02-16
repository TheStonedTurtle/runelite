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
package net.runelite.client.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.runelite.api.Item;
import net.runelite.http.api.loottracker.GameItem;

/**
 * Utility class for Item arrays.
 */
public class ItemUtil
{
	/**
	 * Converts itemArray into a GameItem collection limited to the passed ids
	 * Consolidates all matching Item's qty, by id
	 * @param itemArray item array to work on
	 * @param ids item ids to include in result
	 * @return Map of GameItem by item id
	 */
	public static Map<Integer, GameItem> toGameItemMap(Item[] itemArray, @Nullable Set<Integer> ids)
	{
		final Map<Integer, GameItem> map = new HashMap<>();
		for (Item i : itemArray)
		{
			final int id = i.getId();
			if (ids == null || ids.contains(id))
			{
				int qty = i.getQuantity();
				if (map.containsKey(id))
				{
					qty += map.get(id).getQty();
				}
				map.put(id, new GameItem(id, qty));
			}
		}

		return map;
	}

	/**
	 * Converts itemArray into a GameItem collection
	 * Consolidates all matching Item's qty, by id
	 * @param itemArray item array to work on
	 * @return Map of GameItem by item id
	 */
	public static Map<Integer, GameItem> toGameItemMap(Item[] itemArray)
	{
		return toGameItemMap(itemArray, null);
	}

	public static boolean containsAllItemIds(Item[] itemArray, Set<Integer> ids)
	{
		return toGameItemMap(itemArray, ids).size() == ids.size();
	}

	public static boolean containsAnyItemId(Item[] itemArray, Set<Integer> ids)
	{
		for (Item i : itemArray)
		{
			final int id = i.getId();
			if (ids.contains(id))
			{
				return true;
			}
		}

		return false;
	}

	public static boolean containsItemId(Item[] itemArray, int itemID)
	{
		for (Item i : itemArray)
		{
			if (i.getId() == itemID)
			{
				return true;
			}
		}

		return false;
	}
}
