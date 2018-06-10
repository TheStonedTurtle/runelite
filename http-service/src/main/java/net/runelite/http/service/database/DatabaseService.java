/*
 * Copyright (c) 2018, TheStonedTurtle <http://github.com/TheStonedTurtle>
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
package net.runelite.http.service.database;

import net.runelite.http.api.database.DatabaseEndpoint;
import net.runelite.http.api.database.LootRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseService
{
	private final Sql2o sql2o;

	@Autowired
	public DatabaseService(
		@Qualifier("Runelite SQL2O") Sql2o sql2o
	)
	{
		this.sql2o = sql2o;
	}

	public ArrayList<LootRecord> lookUpBoss(DatabaseEndpoint endpoint, String username, int boss) throws IOException
	{
		return lookUpBoss(endpoint, username, String.valueOf(boss));
	}

	public ArrayList<LootRecord> lookUpBoss(DatabaseEndpoint endpoint, String username, String boss) throws IOException
	{
		String queryText = "SELECT kills.*, CONCAT(\"[\", group_concat( CONCAT(\"{'itemId':\", drops.itemId,\",'itemAmount':\",drops.itemAmount,\"}\")), \"]\") as drops2 " +
				"FROM kills JOIN " +
				"drops " +
				"on drops.kill_entry_id = kills.entry_id " +
				"WHERE (username = :username AND npcID = :id) OR (username = :username AND npcName = :id) " +
				"group by kills.entry_id";
		try (Connection con = sql2o.open())
		{
			ArrayList<LootRecord> result = new ArrayList<>();
			List<LootRecord> records = con.createQuery(queryText)
					.addParameter("username", username)
					.addParameter("id", boss)
					.throwOnMappingFailure(false)		// Ignores entry_id mapping error
					.executeAndFetch(LootRecord.class);

			if (records != null)
			{
				result = new ArrayList<>(records);
				for (LootRecord r : result)
				{
					r.parseDrops();
				}
			}

			return result;
			/*
			List<Map<String, Object>> map = con.createQuery(queryText)
					.addParameter("username", username)
					.addParameter("id", boss)
					.executeAndFetchTable().asList();

			for (Map<String, Object> e : map)
			{
				System.out.println("KETSET:" + e.keySet());
				for (String key : e.keySet())
				{
					System.out.println("Value: " + e.get(key));
				}
			}
			*/
		}
	}

}