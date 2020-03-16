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

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.externalplugins.ExternalPluginManifest;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;

public class PluginPopupPanel extends JPanel
{
	private static final Color BACKGROUND_COLOR = ColorScheme.DARKER_GRAY_COLOR;
	private static final ImageIcon MISSING_ICON;

	static
	{
		BufferedImage missingIcon = ImageUtil.getResourceStreamFromClass(PluginPopupPanel.class, "pluginhub_missingicon.png");
		MISSING_ICON = new ImageIcon(missingIcon);
	}

	public PluginPopupPanel(final ExternalPluginManifest manifest)
	{
		this.setLayout(new GridBagLayout());
		this.setBackground(BACKGROUND_COLOR);

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTH;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 0;

		final JLabel iconLabel = new JLabel();
		iconLabel.setHorizontalAlignment(JLabel.CENTER);
		iconLabel.setIcon(manifest.getStoredIcon() == null ? MISSING_ICON : manifest.getStoredIcon());
		this.add(iconLabel, c);

		final JLabel pluginName = new JLabel(manifest.getDisplayName());
		pluginName.setFont(FontManager.getRunescapeBoldFont());
		pluginName.setToolTipText(manifest.getDisplayName());

		final JLabel author = new JLabel("Made by: " + manifest.getAuthor());
		author.setFont(FontManager.getRunescapeSmallFont());
		author.setToolTipText("Made by: " + manifest.getAuthor());

		final JPanel pluginInfo = new JPanel(new GridLayout(2, 1));
		pluginInfo.setBorder(new EmptyBorder(0, 5, 0, 0));
		pluginInfo.setBackground(BACKGROUND_COLOR);
		pluginInfo.add(pluginName);
		pluginInfo.add(author);
		c.gridx++;
		c.weightx = 1;
		this.add(pluginInfo, c);
		c.weightx = 0;

		final FixedWidthLabel description = new FixedWidthLabel();
		description.setText(manifest.getDescription());
		description.setVerticalAlignment(JLabel.TOP);
		description.setBorder(new EmptyBorder(5, 0, 0, 0));

		c.gridy++;
		c.gridwidth = c.gridx + 1;
		c.gridx = 0;
		this.add(description, c);

		final FixedWidthLabel permsHeader = new FixedWidthLabel();
		permsHeader.setText("Required Plugin Permissions");
		permsHeader.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
		permsHeader.setBorder(new EmptyBorder(10, 0, 0, 0));

		c.gridy++;
		this.add(permsHeader, c);

		final StringBuilder warningList = new StringBuilder()
			.append("<html><ul style='margin-left: 15px;'>");
		for (final String warning : manifest.getWarnings())
		{
			warningList.append("<li>").append(warning).append("</li>");
		}
		warningList.append("</ul></html>");

		final FixedWidthLabel warning = new FixedWidthLabel();
		warning.setText(warningList.toString());
		warning.setHorizontalAlignment(JLabel.LEFT);

		c.gridy++;
		this.add(warning, c);

		final FixedWidthLabel confirmText = new FixedWidthLabel();
		confirmText.setText("<html><body style='text-align: center;'>Are you sure you want to install this plugin?</body></html>");
		confirmText.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
		confirmText.setBorder(new EmptyBorder(0, 0, 10, 0));

		c.gridy++;
		this.add(confirmText, c);
	}
}
