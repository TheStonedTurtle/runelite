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
package net.runelite.client.plugins.party.performance;

import java.text.DecimalFormat;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.WorldType;
import net.runelite.api.events.ExperienceChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Getter
public class PerformanceServiceImpl implements PerformanceService
{
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###");
	// For every damage point dealt 1.33 experience is given to the player's hitpoints (base rate)
	private static final double HITPOINT_RATIO = 1.33;
	private static final double DMM_MULTIPLIER_RATIO = 10;

	private final Client client;

	private boolean enabled = false;
	private boolean paused = false;

	private double damageTaken = 0;
	private double damageDealt = 0;
	private double ticksSpent = 0;
	private double highestHitDealt = 0;
	private double highestHitTaken = 0;

	private boolean loginTick = false;
	private double hpExp;
	private Actor oldTarget;

	@Inject
	public PerformanceServiceImpl(
		final Client client,
		final EventBus eventBus)
	{
		this.client = client;
		eventBus.register(this);
	}

	@Override
	public void reset()
	{
		this.damageTaken = 0;
		this.damageDealt = 0;
		this.ticksSpent = 0;

		this.highestHitDealt = 0;
		this.highestHitTaken = 0;
	}

	@Override
	public void togglePaused()
	{
		this.paused = !this.paused;
	}

	@Override
	public void enable()
	{
		this.enabled = true;
		hpExp = client.getSkillExperience(Skill.HITPOINTS);
	}

	@Override
	public void disable()
	{
		this.enabled = false;
	}

	private void addDamageTaken(double a)
	{
		this.damageTaken += a;
		if (a > this.highestHitTaken)
		{
			this.highestHitTaken = a;
		}
	}

	private void addDamageDealt(double a)
	{
		this.damageDealt += a;
		if (a > this.highestHitDealt)
		{
			this.highestHitDealt = a;
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (!isEnabled())
		{
			return;
		}

		switch (event.getGameState())
		{
			case LOGGED_IN:
				break;
			// Disable tracking when login screen
			case LOGIN_SCREEN:
				loginTick = true;
				disable();
		}
	}

	// Calculate Damage Taken
	@Subscribe
	protected void onHitsplatApplied(HitsplatApplied e)
	{
		if (!isEnabled() || isPaused())
		{
			return;
		}

		if (e.getActor().equals(client.getLocalPlayer()))
		{
			addDamageTaken(e.getHitsplat().getAmount());
		}
	}

	// Calculate Damage Dealt
	@Subscribe
	protected void onExperienceChanged(ExperienceChanged c)
	{
		if (!isEnabled() || isPaused())
		{
			return;
		}

		if (loginTick)
		{
			return;
		}

		if (c.getSkill().equals(Skill.HITPOINTS))
		{
			final double oldExp = hpExp;
			hpExp = client.getSkillExperience(Skill.HITPOINTS);

			final double diff = hpExp - oldExp;
			if (diff < 1)
			{
				return;
			}

			final double damageDealt = calculateDamageDealt(diff);
			addDamageDealt(damageDealt);
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		loginTick = false;
		oldTarget = client.getLocalPlayer().getInteracting();

		if (isEnabled() && !isPaused())
		{
			this.ticksSpent++;
		}
	}

	// Handle Fake XP drops (Ironman, DMM Cap, 200m xp, etc)
	@Subscribe
	public void onScriptCallbackEvent(ScriptCallbackEvent e)
	{
		if (!isEnabled() || isPaused())
		{
			return;
		}

		if ( e.getEventName().equals("fakeXpDrop"))
		{
			final int[] intStack = client.getIntStack();
			final int intStackSize = client.getIntStackSize();

			final int skillId = intStack[intStackSize - 2];
			final Skill skill = Skill.values()[skillId];
			if (skill.equals(Skill.HITPOINTS))
			{
				final int exp = intStack[intStackSize - 1];
				addDamageDealt(calculateDamageDealt(exp));
			}

			client.setIntStackSize(intStackSize - 2);
		}
	}

	/**
	 * Calculates damage dealt based on HP xp gained accounting for NPC Exp Modifiers
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
		return damageDealt / NpcExpModifier.getByNpcId(target.getId());
	}
}
