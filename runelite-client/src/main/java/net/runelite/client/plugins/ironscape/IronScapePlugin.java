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
package net.runelite.client.plugins.ironscape;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ClanMember;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.VarClientStr;
import net.runelite.api.events.ClanChanged;
import net.runelite.api.events.ClanMemberJoined;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.ironscape.data.CsvWriter;
import net.runelite.client.plugins.ironscape.data.Member;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "IronScape",
	description = "All IronScape related features",
	tags = {"iron", "im", "ironscape"}
)
public class IronScapePlugin extends Plugin
{
	private static final SimpleDateFormat dateformat = new SimpleDateFormat("MM-dd-yy");

	@Inject
	private Client client;

	private final Multimap<String, Member> playersSeen = ArrayListMultimap.create();
	private final CsvWriter writer = new CsvWriter();

	private String clanName;

	@Subscribe
	public void onGameStateChanged(GameStateChanged c)
	{
		if (c.getGameState().equals(GameState.LOGIN_SCREEN))
		{
			if (playersSeen.size() > 0)
			{
				final boolean b = writer.updateClanChatFiles(playersSeen);
				final String text = b ? "Successfully wrote Seen CC Members to output file(s)" : "Error creating output file(s)";
				SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(client.getCanvas(), text));
			}
		}
	}

	@Subscribe
	public void onClanChanged(ClanChanged event)
	{
		if (event.isJoined())
		{
			clanName = Text.toJagexName(client.getVar(VarClientStr.RECENT_CLAN_CHAT)).toLowerCase();
			log.debug("Joined cc: {}", clanName);
		}
	}

	@Subscribe
	public void onClanMemberJoined(ClanMemberJoined event)
	{
		final ClanMember m = event.getMember();

		switch (m.getRank())
		{
			case FRIEND:
			case RECRUIT:
			case CORPORAL:
			case SERGEANT:
			case LIEUTENANT:
			case CAPTAIN:
			case GENERAL:
				log.debug("Recording member: {}", m.getUsername());

				final Member them = new Member(m.getUsername(), m.getRank(), clanName, dateformat.format(new Date()), 1);
				if (!playersSeen.get(clanName).contains(them))
				{
					playersSeen.put(clanName, them);
				}
				else
				{
					for (Member member : playersSeen.get(clanName))
					{
						if (them.equals(member))
						{
							// Only re-add to list if they were seen on a different day
							// This requires the local player to have been on or kept the client open for multiple days
							// As the client data isn't loaded on change
							if (!member.getDate().equals(them.getDate()))
							{
								playersSeen.put(clanName, them);
							}
							break;
						}
					}
				}
		}
	}
}
