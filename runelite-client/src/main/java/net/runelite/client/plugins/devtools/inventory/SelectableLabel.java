/*
 * Copyright (c) 2020, TheStonedTurtle <https://github.com/TheStonedTurtle>
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
package net.runelite.client.plugins.devtools.inventory;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.ui.ColorScheme;

public class SelectableLabel extends JLabel
{
	private Color unselectedBackground = ColorScheme.DARKER_GRAY_COLOR;
	@Setter
	private Color unselectedHoverBackground = ColorScheme.DARKER_GRAY_HOVER_COLOR;

	private Color selectedBackground = new Color(0, 70, 0);
	@Setter
	private Color selectedHoverBackground =  new Color(0, 100, 0);

	@Setter
	private Color hoverForeground = Color.WHITE;
	private Color prevForeground = null;

	@Getter
	private boolean selected = false;

	public SelectableLabel()
	{
		super();
		setOpaque(true);

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				if (mouseEvent.getButton() == MouseEvent.BUTTON1)
				{
					if (selected)
					{
						return;
					}

					// Uses a method so it can be overridden
					select();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				final SelectableLabel item = (SelectableLabel) e.getSource();
				item.setBackground(selected ? selectedHoverBackground : unselectedHoverBackground);
				prevForeground = item.getForeground();
				item.setForeground(hoverForeground);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				final SelectableLabel item = (SelectableLabel) e.getSource();
				item.setBackground(selected ? selectedBackground : unselectedBackground);
				item.setForeground(prevForeground);
			}
		});
	}

	public void setSelectedBackground(final Color color)
	{
		selectedBackground = color;
		if (selected)
		{
			setBackground(color);
		}
	}

	public void setUnselectedBackground(final Color color)
	{
		unselectedBackground = color;
		if (!selected)
		{
			setBackground(color);
		}
	}

	public void select()
	{
		selected = true;
		setBackground(selectedHoverBackground);
	}

	public void setSelected(final boolean selected)
	{
		this.selected = selected;
		setBackground(selected ? selectedBackground : unselectedBackground);
	}
}
