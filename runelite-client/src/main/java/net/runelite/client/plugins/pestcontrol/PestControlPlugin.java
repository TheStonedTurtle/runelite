/*
 *  Copyright (c) 2017, Kronos <https://github.com/KronosDesign>
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.pestcontrol;

import com.google.common.eventbus.Subscribe;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.TelemetryManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Pest Control",
	description = "Show helpful information for the Pest Control minigame",
	tags = {"minigame", "overlay"}
)
public class PestControlPlugin extends Plugin
{
	private final Pattern SHIELD_DROP = Pattern.compile("The ([a-z]+), [^ ]+ portal shield has dropped!", Pattern.CASE_INSENSITIVE);
	private static final String FINISH_TEXT = "Congratulations! You managed to destroy all the portals!";

	private static final WorldPoint NOVICE_SPAWN = new WorldPoint(2657, 2639, 0);
	private static final WorldPoint INTERMEDIATE_SPAWN = new WorldPoint(2644, 2644, 0);
	private static final WorldPoint VETERAN_SPAWN = new WorldPoint(2638, 2653, 0);

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Client client;

	@Inject
	private TelemetryManager telemetryManager;

	@Inject
	private PestControlOverlay overlay;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (overlay.getGame() != null && chatMessage.getType() == ChatMessageType.SERVER)
		{
			Matcher matcher = SHIELD_DROP.matcher(chatMessage.getMessage());
			if (matcher.lookingAt())
			{
				overlay.getGame().fall(matcher.group(1));
			}
		}
	}

	void submitGame(int endingPercent, long start)
	{
		long timeElapsed = Instant.now().toEpochMilli() - start;

		Widget w = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);
		boolean won = w != null && w.getText().contains(FINISH_TEXT);

		String boat = "UNKNOWN";
		WorldPoint p = client.getLocalPlayer().getWorldLocation();
		if (p.equals(NOVICE_SPAWN))
		{
			boat = "NOVICE";
		}
		else if (p.equals(INTERMEDIATE_SPAWN))
		{
			boat = "INTERMEDIATE";
		}
		else if (p.equals(VETERAN_SPAWN))
		{
			boat = "VETERAN";
		}

		telemetryManager.submitPestControl(won, boat, endingPercent, timeElapsed);
	}
}
