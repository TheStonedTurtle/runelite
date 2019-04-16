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
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.WorldType;
import net.runelite.api.events.ExperienceChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.game.NPCManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Performance Tracker",
	description = "Displays your current combat performance stats",
	tags = {"performance", "tracker", "stats", "dps", "damage"},
	enabledByDefault = false
)
@Slf4j
public class PerformanceTrackerPlugin extends Plugin
{
	// For every damage point dealt 1.33 experience is given to the player's hitpoints (base rate)
	private static final double HITPOINT_RATIO = 1.33;
	private static final double DMM_MULTIPLIER_RATIO = 10;

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
	private OverlayManager overlayManager;

	@Inject
	private NPCManager npcManager;

	@Getter
	private boolean enabled = false;
	@Getter
	private boolean paused = false;
	@Getter
	private final Performance performance = new Performance();

	// Keep track of actor last tick as sometimes getInteracting can return null when hp xp event is triggered
	// as the player clicked away at the perfect time
	private Actor oldTarget;
	private double hpExp;
	private boolean hopping;
	private int pausedTicks = 0;

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

	@Override
	protected void startUp()
	{
		overlayManager.add(performanceTrackerOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(performanceTrackerOverlay);
		disable();
		reset();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case LOGIN_SCREEN:
				disable();
				break;
			case HOPPING:
				hopping = true;
				break;
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied e)
	{
		if (isPaused())
		{
			return;
		}

		if (e.getActor().equals(client.getLocalPlayer()))
		{
			// Auto enables when hitsplat is applied to player
			if (!isEnabled())
			{
				enable();
			}

			performance.addDamageTaken(e.getHitsplat().getAmount());
			performance.setLastActivityTick(client.getTickCount());
		}
	}

	@Subscribe
	public void onExperienceChanged(ExperienceChanged c)
	{
		if (isPaused() || hopping)
		{
			return;
		}

		if (c.getSkill().equals(Skill.HITPOINTS))
		{
			final double oldExp = hpExp;
			hpExp = client.getSkillExperience(Skill.HITPOINTS);

			// Ignore initial login
			if (client.getTickCount() < 2)
			{
				return;
			}

			final double diff = hpExp - oldExp;
			if (diff < 1)
			{
				return;
			}

			// Auto enables when player receives hp exp
			if (!isEnabled())
			{
				enable();
			}

			final double damageDealt = calculateDamageDealt(diff);
			performance.addDamageDealt(damageDealt);
			performance.setLastActivityTick(client.getTickCount());
		}
	}

	@Subscribe
	public void onScriptCallbackEvent(ScriptCallbackEvent e)
	{
		// Handles Fake XP drops (Ironman in PvP, DMM Cap, 200m xp, etc)
		if (isPaused())
		{
			return;
		}

		if (!"fakeXpDrop".equals(e.getEventName()))
		{
			return;
		}

		final int[] intStack = client.getIntStack();
		final int intStackSize = client.getIntStackSize();

		final int skillId = intStack[intStackSize - 2];
		final Skill skill = Skill.values()[skillId];
		if (skill.equals(Skill.HITPOINTS))
		{
			// Auto enables when player would have received hp exp
			if (!isEnabled())
			{
				enable();
			}

			final int exp = intStack[intStackSize - 1];
			performance.addDamageDealt(calculateDamageDealt(exp));
			performance.setLastActivityTick(client.getTickCount());
		}
	}

	@Subscribe
	public void onGameTick(GameTick t)
	{
		oldTarget = client.getLocalPlayer().getInteracting();

		if (!isEnabled())
		{
			return;
		}

		if (isPaused())
		{
			pausedTicks++;
			return;
		}

		performance.incrementTicksSpent();
		hopping = false;

		final int timeout = config.trackerTimeout();
		if (timeout > 0)
		{
			final double tickTimeout = timeout / GAME_TICK_SECONDS;
			final int activityDiff = (client.getTickCount() - pausedTicks) - performance.getLastActivityTick();
			if (activityDiff > tickTimeout)
			{
				// offset the tracker time to account for idle timeout
				// Leave an additional tick to pad elapsed time
				final double offset = tickTimeout - GAME_TICK_SECONDS;
				performance.setTicksSpent(performance.getTicksSpent() - offset);

				submit();
			}
		}
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
				togglePaused();
				break;
			case "Reset":
				reset();
				break;
			case "Submit":
				submit();
				break;
		}
	}

	private void enable()
	{
		this.enabled = true;
		hpExp = client.getSkillExperience(Skill.HITPOINTS);
	}

	private void disable()
	{
		this.enabled = false;
	}

	private void togglePaused()
	{
		this.paused = !this.paused;
	}

	private void reset()
	{
		this.enabled = false;
		this.paused = false;

		this.performance.reset();
		pausedTicks = 0;
	}

	private void submit()
	{
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAME)
			.runeLiteFormattedMessage(performance.createChatMessage())
			.build());

		reset();
	}

	/**
	 * Calculates damage dealt based on HP xp gained accounting for multipliers such as DMM mode
	 * @param diff HP xp gained
	 * @return damage dealt
	 */
	private double calculateDamageDealt(double diff)
	{
		double damageDealt = diff / HITPOINT_RATIO;
		// DeadMan mode has an XP modifier
		if (client.getWorldType().contains(WorldType.DEADMAN))
		{
			damageDealt = damageDealt / DMM_MULTIPLIER_RATIO;
		}

		// Some NPCs have an XP modifier, account for it here.
		Actor a = client.getLocalPlayer().getInteracting();
		if (!(a instanceof NPC))
		{
			// If we are interacting with nothing we may have clicked away at the perfect time fall back to last tick
			if (!(oldTarget instanceof NPC))
			{
				log.warn("Couldn't find current or past target for experienced gain...");
				return damageDealt;
			}

			a = oldTarget;
		}

		NPC target = (NPC) a;
		return damageDealt / 1.0;
	}
}
