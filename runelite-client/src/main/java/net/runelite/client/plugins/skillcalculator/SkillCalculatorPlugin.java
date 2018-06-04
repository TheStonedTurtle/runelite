/*
 * Copyright (c) 2018, Kruithne <kruithne@gmail.com>
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

package net.runelite.client.plugins.skillcalculator;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.inject.Inject;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.PluginToolbar;
import net.runelite.client.util.QueryRunner;

@PluginDescriptor(name = "Skill Calculator")
public class SkillCalculatorPlugin extends Plugin
{
	@Inject
	private ClientUI ui;

	@Inject
	private Client client;

	@Inject
	private SkillIconManager skillIconManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private PluginToolbar pluginToolbar;

	@Inject
	private SkillCalculatorConfig skillCalculatorConfig;

	private NavigationButton uiNavigationButton;
	private SkillCalculatorPanel uiPanel;

	@Inject
	private QueryRunner queryRunner;

	@Getter
	private Map<Integer, Integer> bankMap = new HashMap<>();

	@Provides
	SkillCalculatorConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkillCalculatorConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		BufferedImage icon;
		synchronized (ImageIO.class)
		{
			icon = ImageIO.read(getClass().getResourceAsStream("calc.png"));
		}

		SkillCalculator.spriteManager = spriteManager;
		SkillCalculator.itemManager = itemManager;
		SkillCalculator.plugin = this;

		uiPanel = new SkillCalculatorPanel(skillIconManager, client);
		uiNavigationButton = NavigationButton.builder()
			.tooltip("Skill Calculator")
			.icon(icon)
			.priority(6)
			.panel(uiPanel)
			.build();
		pluginToolbar.addNavigation(uiNavigationButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		pluginToolbar.removeNavigation(uiNavigationButton);
		bankMap.clear();
	}

	// Pulled from bankvalue plugin
	@Subscribe
	public void onGameTick(GameTick event)
	{
		Widget widgetBankTitleBar = client.getWidget(WidgetInfo.BANK_TITLE_BAR);

		// Don't update on a search because rs seems to constantly update the title
		if (widgetBankTitleBar == null || widgetBankTitleBar.isHidden() || widgetBankTitleBar.getText().contains("Showing"))
		{
			return;
		}

		updateBankItems();
	}

	private void updateBankItems()
	{
		if (showBankedXp())
		{

			WidgetItem[] widgetItems = queryRunner.runQuery(new BankItemQuery());

			if (widgetItems.length == 0)
			{
				return;
			}

			Map<Integer, Integer> map = new HashMap<>();

			for (WidgetItem widgetItem : widgetItems)
			{
				map.put(widgetItem.getId(), widgetItem.getQuantity());
			}

			bankMap = map;
		}
	}

	boolean showBankedXp()
	{
		return skillCalculatorConfig.showBankedXp();
	}
}