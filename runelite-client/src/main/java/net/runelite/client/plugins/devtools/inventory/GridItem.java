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
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import lombok.Setter;
import net.runelite.api.Constants;
import net.runelite.api.Item;
import net.runelite.client.ui.ColorScheme;

class GridItem extends JLabel
{
	static final Dimension ITEM_SIZE = new Dimension(Constants.ITEM_SPRITE_WIDTH + 4, Constants.ITEM_SPRITE_HEIGHT);

	private static final Color UNSELECTED_BACKGROUND = ColorScheme.DARKER_GRAY_COLOR;
	private static final Color UNSELECTED_HOVER_BACKGROUND = ColorScheme.DARKER_GRAY_HOVER_COLOR;

	private static final Color SELECTED_BACKGROUND = new Color(0, 70, 0);
	private static final Color SELECTED_HOVER_BACKGROUND =  new Color(0, 100, 0);

	@Setter
	private Consumer<Item> selectionConsumer;
	private boolean selected = false;

	GridItem(final Item item)
	{
		super();

		setOpaque(true);
		setVerticalAlignment(SwingConstants.CENTER);
		setHorizontalAlignment(SwingConstants.CENTER);
		setPreferredSize(ITEM_SIZE);

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				if (mouseEvent.getButton() == MouseEvent.BUTTON1)
				{
					if (selected || selectionConsumer == null)
					{
						return;
					}

					selectionConsumer.accept(item);
					selected = true;
					setBackground(SELECTED_HOVER_BACKGROUND);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				final GridItem item = (GridItem) e.getSource();
				item.setBackground(selected ? SELECTED_HOVER_BACKGROUND : UNSELECTED_HOVER_BACKGROUND);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				final GridItem item = (GridItem) e.getSource();
				item.setBackground(selected ? SELECTED_BACKGROUND : UNSELECTED_BACKGROUND);
			}
		});
	}

	public void setSelected(final boolean selected)
	{
		this.selected = selected;
		setBackground(selected ? SELECTED_BACKGROUND : UNSELECTED_BACKGROUND);
	}
}
