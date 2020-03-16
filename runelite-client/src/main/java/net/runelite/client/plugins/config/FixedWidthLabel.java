/*
 * Copyright (c) 2020, Michael Goodwin <https://github.com/MichaelGoodwin>
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
package net.runelite.client.plugins.config;

import com.google.common.html.HtmlEscapers;
import java.awt.Dimension;
import java.awt.Insets;
import javax.annotation.Nullable;
import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.PluginPanel;

@Slf4j
public class FixedWidthLabel extends JLabel
{
	@Override
	public void setText(@Nullable String text)
	{
		if (text != null && !text.startsWith("<html>"))
		{
			text = "<html>" +  HtmlEscapers.htmlEscaper().escape(text) + "</html>";
		}

		super.setText(text);
	}

	@Override
	public Dimension getPreferredSize()
	{
		final View view = (View) this.getClientProperty(BasicHTML.propertyKey);
		if (view == null)
		{
			log.warn("Trying to use FixedWidthLabel with non-html content: {}", getText());
			return super.getPreferredSize();
		}

		view.setSize(PluginPanel.PANEL_WIDTH, 0.0f);
		int w = (int) view.getPreferredSpan(View.X_AXIS);
		int h = (int) view.getPreferredSpan(View.Y_AXIS);
		final Insets insets = getInsets();

		return new Dimension(Math.min(super.getPreferredSize().width, w), h + insets.top + insets.bottom);
	}
}
