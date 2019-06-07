/*
 * Copyright (c) 2019, TheStonedTurtle <https://github.com/TheStonedTurtle>
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
package net.runelite.client.plugins.skillcalculator.banked;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.ComboBoxIconEntry;
import net.runelite.client.ui.components.ComboBoxListRenderer;

@Slf4j
public class BankedCalculatorPanel extends PluginPanel
{
	private final Client client;
	private final SkillIconManager skillIconManager;
	private final ItemManager itemManager;

	private GridBagConstraints c;
	private Skill currentSkill;

	public BankedCalculatorPanel(Client client, SkillIconManager skillIconManager, ItemManager itemManager)
	{
		super();

		this.client = client;
		this.skillIconManager = skillIconManager;
		this.itemManager = itemManager;

		setBorder(new EmptyBorder(10, 10, 10, 10));
		setLayout(new GridBagLayout());

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;

		// Create the Skill dropdown
		final JComboBox<ComboBoxIconEntry> dropdown = new JComboBox<>();

		final ComboBoxListRenderer renderer = new ComboBoxListRenderer();
		renderer.setDefaultText("Select a Skill...");
		dropdown.setRenderer(renderer);

		for (final BankableSkill bankableSkill : BankableSkill.values())
		{
			final Skill skill = bankableSkill.getSkill();
			final BufferedImage img = skillIconManager.getSkillImage(skill, true);
			final ComboBoxIconEntry entry = new ComboBoxIconEntry(new ImageIcon(img), skill.getName(), skill);
			dropdown.addItem(entry);
		}

		// Add click event handler now to prevent above code from triggering it.
		dropdown.addItemListener(e ->
		{
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				final ComboBoxIconEntry source = (ComboBoxIconEntry) e.getItem();
				if (source.getData() instanceof Skill)
				{
					final Skill skill = (Skill) source.getData();
					this.selectSkill(skill);
				}
			}
		});

		dropdown.setSelectedIndex(-1);

		add(dropdown, c);
		c.gridy++;
	}

	private void selectSkill(final Skill skill)
	{
		this.currentSkill = skill;
	}
}
