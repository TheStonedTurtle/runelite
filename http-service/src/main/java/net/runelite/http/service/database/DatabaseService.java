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

import lombok.extern.slf4j.Slf4j;
import net.runelite.http.api.RuneLiteAPI;
import net.runelite.http.api.database.DatabaseClient;
import net.runelite.http.api.database.DatabaseEndpoint;
import net.runelite.http.api.database.LootRecord;
import net.runelite.http.service.util.exception.InternalServerErrorException;
import net.runelite.http.service.util.exception.NotFoundException;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;

@Service
@Slf4j
public class DatabaseService
{
	public ArrayList<LootRecord> lookUpBoss(DatabaseEndpoint endpoint, String username, int boss) throws IOException
	{
		return lookUpBoss(endpoint, username, String.valueOf(boss));
	}

	public ArrayList<LootRecord> lookUpBoss(DatabaseEndpoint endpoint, String username, String boss) throws IOException
	{
		HttpUrl url = endpoint.getDatabaseURL().newBuilder()
				.addQueryParameter("username", username)
				.addQueryParameter("boss", boss)
				.build();

		log.debug("Built URL {}", url);

		Request okrequest = new Request.Builder()
				.url(url)
				.build();

		String responseStr;

		try (Response okresponse = RuneLiteAPI.CLIENT.newCall(okrequest).execute())
		{
			if (!okresponse.isSuccessful())
			{
				switch (HttpStatus.valueOf(okresponse.code()))
				{
					case NOT_FOUND:
						throw new NotFoundException();
					default:
						throw new InternalServerErrorException("Error retrieving data from RuneLite Web API: " + okresponse.message());
				}
			}

			responseStr = okresponse.body().string();
		}

		LootRecord[] records = RuneLiteAPI.GSON.fromJson(responseStr, LootRecord[].class);
		ArrayList<LootRecord> result = DatabaseClient.unpackLootRecords(records);
		System.out.println(result);

		return result;
	}

}