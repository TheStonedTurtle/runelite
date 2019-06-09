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
package net.runelite.client.plugins.skillcalculator.banked.components;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BooleanSupplier;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.game.AsyncBufferedImage;
import net.runelite.client.plugins.skillcalculator.banked.beans.Activity;
import net.runelite.client.plugins.skillcalculator.banked.beans.BankedItem;
import net.runelite.client.ui.ColorScheme;

public class GridItem extends JLabel
{
	private final static String IGNORE = "Ignore Item";
	private final static String INCLUDE = "Include Item";

	/* To be executed when this element is clicked */
	@Setter
	private BooleanSupplier onSelectEvent;

	/* To be executed when this element is ignored */
	@Setter
	private BooleanSupplier onIgnoreEvent;

	@Getter
	private boolean selected = false;

	@Getter
	private boolean ignored = false;

	@Setter
	private Color unselectedBackground = ColorScheme.DARKER_GRAY_COLOR;

	@Setter
	private Color selectedBackground = ColorScheme.GRAND_EXCHANGE_PRICE;

	@Setter
	private Color ignoredBackground = ColorScheme.PROGRESS_ERROR_COLOR;

	private final JMenuItem ignoreOption = new JMenuItem(IGNORE);
	private final BankedItem bankedItem;

	public GridItem(final BankedItem item, final AsyncBufferedImage icon)
	{
		super("");

		this.bankedItem = item;

		this.setOpaque(true);
		this.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		this.setBorder(BorderFactory.createEmptyBorder(5, 0, 2, 0));

		this.setVerticalAlignment(SwingConstants.CENTER);
		this.setHorizontalAlignment(SwingConstants.CENTER);

		updateToolTip();
		updateIcon(icon);

		this.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				select();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				GridItem item = (GridItem) e.getSource();
				item.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				GridItem item = (GridItem) e.getSource();
				item.setBackground(selected ? selectedBackground : unselectedBackground);
			}
		});

		ignoreOption.addActionListener(e ->
		{
			if (onIgnoreEvent != null && !onIgnoreEvent.getAsBoolean())
			{
				return;
			}

			this.ignored = !this.ignored;
			this.ignoreOption.setText(this.ignored ? INCLUDE : IGNORE);
			this.setBackground(this.ignored ? ignoredBackground : (selected ? selectedBackground : unselectedBackground));
		});

		final JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
		popupMenu.add(ignoreOption);
	}

	public boolean select()
	{
		if (onSelectEvent != null && !onSelectEvent.getAsBoolean())
		{
			return false;
		}

		setBackground(selectedBackground);
		return selected = true;
	}

	public void unselect()
	{
		setBackground(unselectedBackground);
		selected = false;
	}

	public void updateToolTip()
	{
		this.setToolTipText(buildToolTip());
	}

	public void updateIcon(final AsyncBufferedImage icon)
	{
		icon.addTo(this);
	}

	private String buildToolTip()
	{
		String tip = "<html>" + bankedItem.getItem().getComposition().getName();

		final Activity a = bankedItem.getSelectedActivity();
		if (a != null)
		{
			tip += "<br/>Making: " +  a.getName();
			tip += "<br/>Xp/Action: " + a.getXp();
		}

		return tip + "</html>";
	}
}
