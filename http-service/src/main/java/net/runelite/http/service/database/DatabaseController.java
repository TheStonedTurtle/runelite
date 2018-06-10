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

import java.io.IOException;
import java.util.List;

import net.runelite.http.api.database.DatabaseEndpoint;
import net.runelite.http.api.database.LootRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

@RestController
@RequestMapping("/database")
public class DatabaseController
{
	private static final String CREATE_KILLS = "CREATE TABLE IF NOT EXISTS `kills` (\n"
			+ "  `entry_id` INT AUTO_INCREMENT UNIQUE,\n"
			+ "  `username` VARCHAR(255) NOT NULL,\n"
			+ "  `npcName` VARCHAR(255) NOT NULL,\n"
			+ "  `npcID` INT NOT NULL,\n"
			+ "  `killCount` INT NOT NULL\n"
			+ ") ENGINE=InnoDB";


	private static final String CREATE_DROPS = "CREATE TABLE IF NOT EXISTS `drops` (\n"
			+ "  `kill_entry_id` INT NOT NULL,\n"
			+ "  `itemId` INT NOT NULL,\n"
			+ "  `itemAmount` INT NOT NULL\n"
			+ ") ENGINE=InnoDB";

	private final Sql2o sql2o;

	@Autowired
	public DatabaseController(@Qualifier("Runelite SQL2O") Sql2o sql2o)
	{
		this.sql2o = sql2o;

		try (Connection con = sql2o.open())
		{
			con.createQuery(CREATE_KILLS)
					.executeUpdate();
			con.createQuery(CREATE_DROPS)
					.executeUpdate();
		}
	}

	@Autowired
	private DatabaseService service;

	@RequestMapping("/boss")
	public List<LootRecord> lookupBoss(@RequestParam String username, @RequestParam String boss) throws IOException
	{
		return service.lookUpBoss(DatabaseEndpoint.BOSS, username, boss);
	}

	// Wrapper for boss ID (int)
	public List<LootRecord> lookupBossId(@RequestParam String username, @RequestParam Integer boss) throws IOException
	{
		return service.lookUpBoss(DatabaseEndpoint.BOSS, username, String.valueOf(boss));
	}
}
