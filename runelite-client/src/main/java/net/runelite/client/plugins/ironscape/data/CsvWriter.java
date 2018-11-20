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
package net.runelite.client.plugins.ironscape.data;

import com.google.common.base.Splitter;
import com.google.common.collect.Multimap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ClanMemberRank;
import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Slf4j
public class CsvWriter
{
	private static final String FILE_EXTENSION = ".csv";
	private static final File OUTPUT_DIR = new File(RUNELITE_DIR, "IronScape");
	private static final Splitter SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

	private final Set<String> filenames = new HashSet<>();

	public CsvWriter()
	{
		OUTPUT_DIR.mkdir();

		final File[] files = OUTPUT_DIR.listFiles((dir, name) -> name.endsWith(FILE_EXTENSION));
		if (files != null)
		{
			for (final File f : files)
			{
				filenames.add(f.getName());
				log.info("Found log file: {}", f.getName());
			}
		}
	}

	private synchronized Collection<Member> loadData(final String filename)
	{
		final Collection<Member> data = new ArrayList<>();
		if (!filenames.contains(filename))
		{
			return data;
		}

		final File file = new File(OUTPUT_DIR, filename);
		final String clanName = filename.replace(FILE_EXTENSION, "");

		try (final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = br.readLine()) != null)
			{
				if (line.length() > 0)
				{
					final List<String> d = SPLITTER.splitToList(line);
					final Member m = new Member(d.get(0), ClanMemberRank.valueOf(d.get(1)), clanName, d.get(2), Integer.parseInt(d.get(3)));
					data.add(m);
				}
			}

			return data;
		}
		catch (FileNotFoundException e)
		{
			log.debug("File not found: {}", filename);
			return data;
		}
		catch (IOException e)
		{
			log.warn("IOException for file {}: {}", filename, e.getMessage());
			return data;
		}
	}

	private boolean writeToOutput(final String filename, Collection<Member> data)
	{
		final File output = new File(OUTPUT_DIR, filename);

		if (filenames.contains(filename))
		{
			final Collection<Member> savedData = loadData(filename);
			log.info("Consolidating data: {} | {}", data, savedData);
			data = Member.consolidate(data, savedData);
			log.info("Consolidated data: {}", data);
		}
		filenames.add(filename);

		try
		{
			final BufferedWriter file = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), StandardCharsets.UTF_8));
			for (final Member entry : data)
			{
				// Convert entry to CSV
				final String out = entry.toOutputString();
				log.debug("Writing data to file: {}", out);
				file.append(out);
				file.newLine();
			}
			file.close();

			return true;
		}
		catch (IOException ioe)
		{
			log.warn("Error writing data to file {}: {}", filename, ioe.getMessage());
			return false;
		}
	}

	public boolean updateClanChatFiles(final Multimap<String, Member> map)
	{
		log.debug("Updating Clan Chat files...");
		boolean suc = true;
		for (final String clanChat : map.keySet())
		{
			final String filename = clanChat + FILE_EXTENSION;
			final Collection<Member> members = map.get(clanChat);
			log.info("Writing Clan Chat file {} | {}", filename, members);
			boolean success = writeToOutput(filename, members);
			if (!success)
			{
				suc = false;
				log.warn("Error saving data via writeToOutput. {} | {}", clanChat, members);
			}
		}

		return suc;
	}
}
