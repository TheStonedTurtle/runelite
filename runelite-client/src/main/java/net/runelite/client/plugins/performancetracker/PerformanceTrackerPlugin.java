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
package net.runelite.client.plugins.performancetracker;

import com.google.inject.Binder;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Performance Tracker",
	description = "Tracks & displays your current combat performance stats",
	tags = {"performance", "tracker", "stats", "dps", "damage"}
)
@Slf4j
public class PerformanceTrackerPlugin extends Plugin
{
	private static final double GAME_TICK_SECONDS = 0.6;

	@Inject
	private Client client;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private PerformanceTrackerConfig config;

	@Inject
	private PerformanceTrackerOverlay performanceTrackerOverlay;

	@Inject
	@Getter
	private PerformanceServiceImpl performanceService;

	@Inject
	private OverlayManager overlayManager;

	@Provides
	PerformanceTrackerConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PerformanceTrackerConfig.class);
	}

	@Override
	public void configure(Binder binder)
	{
		binder.bind(PerformanceService.class).to(PerformanceServiceImpl.class);
	}

	private int pausedTicks = 0;

	@Override
	protected void startUp()
	{
		overlayManager.add(performanceTrackerOverlay);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			performanceService.enable();
		}
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(performanceTrackerOverlay);
		performanceService.reset();
	}

	@Subscribe
	public void onOverlayMenuClicked(OverlayMenuClicked c)
	{
		if (!c.getOverlay().equals(performanceTrackerOverlay))
		{
			return;
		}

		switch (c.getEntry().getOption())
		{
			case "Pause":
				performanceService.togglePaused();
				break;
			case "Reset":
				performanceService.reset();
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick t)
	{
		if (!performanceService.isEnabled())
		{
			return;
		}

		if (performanceService.isPaused())
		{
			pausedTicks++;
			return;
		}

		final int timeout = config.trackerTimeout();
		if (timeout <= 0)
		{
			return;
		}

		final double tickTimeout = timeout / GAME_TICK_SECONDS;
		final int activityDiff = (client.getTickCount() - pausedTicks) - performanceService.getLastActivityTick();
		if (activityDiff > tickTimeout)
		{
			// offset the tracker time to account for idle timeout
			// Leave an additional tick to pad elapsed time
			final double offset = tickTimeout - GAME_TICK_SECONDS;
			performanceService.setTicksSpent(performanceService.getTicksSpent() - offset);

			chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAME)
			.runeLiteFormattedMessage(performanceService.createPerformanceChatMessage())
			.build());

			performanceService.reset();
			pausedTicks = 0;
		}

	}
}
