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
package net.runelite.client.plugins.loottracker;

import com.google.common.base.Splitter;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.GameItem;
import net.runelite.http.api.RuneLiteAPI;

@Slf4j
@Data
class LootTrackerData
{
	// In order to save on space inside the config all variable names will be 1 letter long
	private final String name;
	private final Collection<GameItem> items;
	private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();
	private static final Splitter COLON_SPLITTER = Splitter.on(':').trimResults().omitEmptyStrings();

	String asJson()
	{
		return RuneLiteAPI.GSON.toJson(this);
	}

	static Collection<LootTrackerData> fromJson(String json)
	{
		Type listType = new TypeToken<Collection<LootTrackerData>>()
		{
		}.getType();
		return RuneLiteAPI.GSON.fromJson(json, listType);
	}

	String asCSV()
	{
		StringBuilder ids = new StringBuilder();
		StringBuilder qtys = new StringBuilder();
		boolean delimited = false;

		for (GameItem item : this.items)
		{
			if (delimited)
			{
				ids.append(":");
				qtys.append(":");
			}
			ids.append(item.getId());
			qtys.append(item.getQty());
			delimited = true;
		}

		return this.name + "," + ids.toString() + "," + qtys.toString();
	}

	static String getCSVHeader()
	{
		return "name,item ids,item quantities";
	}

	static LootTrackerData fromCSV(String csv)
	{
		if (csv.equals(getCSVHeader()))
		{
			return null;
		}

		List<String> d = COMMA_SPLITTER.splitToList(csv);
		List<String> ids = COLON_SPLITTER.splitToList(d.get(1));
		List<String> qtys = COLON_SPLITTER.splitToList(d.get(2));
		if (ids.size() != qtys.size())
		{
			log.warn("Invalid CSV format for line: {}", csv);
			return null;
		}

		Collection<GameItem> items = new ArrayList<>();
		for (int i = 0; i < ids.size(); i++)
		{
			items.add(new GameItem(Integer.valueOf(ids.get(i)), Integer.valueOf(qtys.get(i))));
		}
		return new LootTrackerData(d.get(0), items);
	}
}
