package net.runelite.client.plugins.dpscounter;

import com.google.inject.Provides;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.events.PartyChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ws.PartyMember;
import net.runelite.client.ws.PartyService;
import net.runelite.client.ws.WSClient;
import org.apache.commons.lang3.ArrayUtils;

@PluginDescriptor(
	name = "DPS Counter",
	description = "Counts damage (per second) to a boss by a party",
	enabledByDefault = false
)
@Slf4j
public class DpsCounterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PartyService partyService;

	@Inject
	private WSClient wsClient;

	@Inject
	private DpsOverlay dpsOverlay;

	@Inject
	private DpsConfig dpsConfig;

	private Boss boss;
	private NPC bossNpc;
	@Getter(AccessLevel.PACKAGE)
	private final Map<String, DpsMember> members = new ConcurrentHashMap<>();
	@Getter(AccessLevel.PACKAGE)
	private DpsMember total = new DpsMember("Total");

	@Provides
	DpsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DpsConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(dpsOverlay);
		wsClient.registerMessage(DpsUpdate.class);
	}

	@Override
	protected void shutDown()
	{
		wsClient.unregisterMessage(DpsUpdate.class);
		overlayManager.remove(dpsOverlay);
		members.clear();
		boss = null;
	}

	@Subscribe
	public void onPartyChanged(PartyChanged partyChanged)
	{
		members.clear();
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged interactingChanged)
	{
		Actor source = interactingChanged.getSource();
		Actor target = interactingChanged.getTarget();

		if (source != client.getLocalPlayer())
		{
			return;
		}

		if (target instanceof NPC)
		{
			NPC npc = (NPC) target;
			int npcId = npc.getId();
			Boss boss = Boss.findBoss(npcId);
			if (boss != null)
			{
				this.boss = boss;
				bossNpc = (NPC) target;
			}
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		Actor actor = hitsplatApplied.getActor();

		if (bossNpc == actor)
		{
			Hitsplat hitsplat = hitsplatApplied.getHitsplat();

			if (hitsplat.getHitsplatType() != Hitsplat.HitsplatType.DAMAGE_ME
				// We want to track damage done to the boss by others as well using the total tracker
				&& hitsplat.getHitsplatType() != Hitsplat.HitsplatType.DAMAGE_OTHER)
			{
				return;
			}

			if (total.isPaused())
			{
				total.unpause();
			}

			total.addDamage(hitsplat.getAmount());
			if (hitsplat.getHitsplatType() == Hitsplat.HitsplatType.DAMAGE_ME)
			{
				// Update local member
				PartyMember localMember = partyService.getLocalMember();
				// If not in a party, user local player name
				final String name = localMember == null ? client.getLocalPlayer().getName() : localMember.getName();
				DpsMember dpsMember = members.computeIfAbsent(name, DpsMember::new);
				if (dpsMember.isPaused())
				{
					dpsMember.unpause();
					log.debug("Unpausing {}", dpsMember.getName());
				}

				dpsMember.addDamage(hitsplatApplied.getHitsplat().getAmount());
			}
		}
	}

	@Subscribe
	public void onDpsUpdate(DpsUpdate dpsUpdate)
	{
		if (partyService.getLocalMember().getMemberId().equals(dpsUpdate.getMemberId()))
		{
			return;
		}

		String name = partyService.getMemberById(dpsUpdate.getMemberId()).getName();
		if (name == null)
		{
			return;
		}

		// Hmm - not attacking the same boss I am
		if (bossNpc == null || dpsUpdate.getNpcId() != bossNpc.getId())
		{
			return;
		}

		DpsMember dpsMember = members.computeIfAbsent(name, DpsMember::new);
		dpsMember.addDamage(dpsUpdate.getHit());

		if (dpsMember.isPaused())
		{
			dpsMember.unpause();
			log.debug("Unpausing {}", dpsMember.getName());
		}
	}

	@Subscribe
	public void onOverlayMenuClicked(OverlayMenuClicked event)
	{
		if (event.getEntry().getMenuAction() == MenuAction.RUNELITE_OVERLAY &&
			event.getEntry().getOption().equals("Reset") &&
			event.getEntry().getTarget().equals("DPS counter"))
		{
			members.clear();
			total.reset();
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		if (boss == null)
		{
			return;
		}

		NPC npc = npcSpawned.getNpc();
		int npcId = npc.getId();
		if (!ArrayUtils.contains(boss.getIds(), npcId))
		{
			return;
		}

		log.debug("Boss has spawned!");
		bossNpc = npc;
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (bossNpc == null || npcDespawned.getNpc() != bossNpc)
		{
			return;
		}

		if (bossNpc.isDead())
		{
			log.debug("Boss has died!");
			pause();
		}

		bossNpc = null;
	}

	private void pause()
	{
		for (DpsMember dpsMember : members.values())
		{
			dpsMember.pause();
		}
		total.pause();
	}
}
