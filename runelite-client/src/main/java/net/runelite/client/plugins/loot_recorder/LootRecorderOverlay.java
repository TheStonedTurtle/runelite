/*
 * Copyright (c) 2018, TheStonedTurtle <www.github.com/TheStonedTurtle>
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
package net.runelite.client.plugins.loot_recorder;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Instant;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

class LootRecorderOverlay extends Overlay
{
	private final LootRecorderPlugin plugin;
	private final LootRecorderConfig config;
	private final PanelComponent panelComponent = new PanelComponent();

	// Wrapper functions for overlay manipluation
	private void resetOverlay()
	{
		panelComponent.getChildren().clear();
	}

	private void addOverlayTitle(String titleText)
	{
		panelComponent.getChildren().add(TitleComponent.builder()
			.text(titleText)	// Loot From
			.build());		
	}

	private void addOverlayItem(String itemName, Integer itemAmount)
	{
		panelComponent.getChildren().add(LineComponent.builder()
			.left(itemName)
			.right(Integer.toString(itemAmount))
			.build());	
	}

	@Inject
	LootRecorderOverlay(LootRecorderPlugin plugin, LootRecorderConfig config)
	{
		setPosition(OverlayPosition.BOTTOM_LEFT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		resetOverlay();
		if(!config.showLootTotals())
		{
			return null;
		}

		// Show Recorder Loot Totals


		// Render the new panel
		return panelComponent.render(graphics);
	}

}