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
import net.runelite.client.plugins.skillcalculator.banked.BankedCalculator;
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
	private Color unselectedHoverBackground = ColorScheme.DARKER_GRAY_HOVER_COLOR;

	@Setter
	private Color selectedBackground = new Color(0, 70, 0);

	@Setter
	private Color selectedHoverBackground =  new Color(0, 100, 0);

	@Setter
	private Color ignoredBackground = new Color(90, 0, 0);

	@Setter
	private Color ignoredHoverBackground = new Color(120, 0, 0);

	@Getter
	private final BankedItem bankedItem;

	@Getter
	private int amount;

	private final JMenuItem ignoreOption = new JMenuItem(IGNORE);

	public GridItem(final BankedItem item, final AsyncBufferedImage icon, final int amount)
	{
		super("");

		this.bankedItem = item;

		this.setOpaque(true);
		this.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		this.setBorder(BorderFactory.createEmptyBorder(5, 0, 2, 0));

		this.setVerticalAlignment(SwingConstants.CENTER);
		this.setHorizontalAlignment(SwingConstants.CENTER);

		updateIcon(icon, amount);
		updateToolTip();

		this.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				if (mouseEvent.getButton() == MouseEvent.BUTTON1)
				{
					select();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				final GridItem item = (GridItem) e.getSource();
				item.setBackground(getHoverBackgroundColor());
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				final GridItem item = (GridItem) e.getSource();
				item.setBackground(getBackgroundColor());
			}
		});

		ignoreOption.addActionListener(e ->
		{
			// Update ignored flag now so event knows new state
			this.ignored = !this.ignored;

			if (onIgnoreEvent != null && !onIgnoreEvent.getAsBoolean())
			{
				// Reset state
				this.ignored = !this.ignored;
				return;
			}

			this.ignoreOption.setText(this.ignored ? INCLUDE : IGNORE);
			this.setBackground(getBackgroundColor());
		});

		final JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
		popupMenu.add(ignoreOption);

		this.setComponentPopupMenu(popupMenu);
	}

	private Color getBackgroundColor()
	{
		return ignored ? ignoredBackground : (selected ? selectedBackground : unselectedBackground);
	}

	private Color getHoverBackgroundColor()
	{
		return ignored ? ignoredHoverBackground : (selected ? selectedHoverBackground : unselectedHoverBackground);
	}

	public boolean select()
	{
		if (onSelectEvent != null && !onSelectEvent.getAsBoolean())
		{
			return false;
		}

		selected = true;
		setBackground(getBackgroundColor());
		return true;
	}

	public void unselect()
	{
		selected = false;
		setBackground(getBackgroundColor());
	}

	public void updateToolTip()
	{
		this.setToolTipText(buildToolTip());
	}

	public void updateIcon(final AsyncBufferedImage icon, final int amount)
	{
		icon.addTo(this);
		this.amount = amount;
	}

	private String buildToolTip()
	{
		String tip = "<html>" + bankedItem.getItem().getItemInfo().getName();

		final Activity a = bankedItem.getItem().getSelectedActivity();
		if (a != null)
		{
			tip += "<br/>Activity: " +  a.getName();
			tip += "<br/>Xp/Action: " + BankedCalculator.XP_FORMAT_COMMA.format(a.getXp());
			tip += "<br/>Total Xp: " + BankedCalculator.XP_FORMAT_COMMA.format(a.getXp() * amount);
		}
		else
		{
			tip += "<br/>Outputs: " + bankedItem.getItem().getItemInfo().getName();
		}

		return tip + "</html>";
	}
}
