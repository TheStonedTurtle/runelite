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
package net.runelite.client.plugins.loginscreen;

import java.awt.image.BufferedImage;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.SpriteID;
import net.runelite.client.util.ImageUtil;

@Getter
@AllArgsConstructor
@Slf4j
public enum LoginScreenSprites
{
	LOGO(SpriteID.LOGIN_SCREEN_RUNESCAPE_LOGO),
	DIALOG_BACKGROUND(SpriteID.LOGIN_SCREEN_DIALOG_BACKGROUND),
	BUTTON_BACKGROUND(SpriteID.LOGIN_SCREEN_BUTTON_BACKGROUND),
	MUSIC_BUTTON(SpriteID.LOGIN_SCREEN_MUSIC_BUTTON),
	MUSIC_BUTTON_MUTED(SpriteID.LOGIN_SCREEN_MUSIC_BUTTON, 1),
	WORLD_SELECT(SpriteID.LOGIN_SCREEN_WORLD_SELECT_BUTTON),
	;

	private final int spriteID;
	private final int fileIdx;

	LoginScreenSprites(int spriteID)
	{
		this(spriteID, 0);
	}

	@Nullable
	public BufferedImage getOverrideImage(LoginScreenSpriteOverride type)
	{
		if (type == LoginScreenSpriteOverride.OFF)
		{
			return null;
		}

		final String filename = String.format("sprites/%s-%s-%s.png", type.getFilePrefix(), this.spriteID, this.fileIdx);
		try
		{
			return ImageUtil.getResourceStreamFromClass(LoginScreenSprites.class, filename);
		}
		catch (Exception e)
		{
			log.warn("Missing login sprite file {}", filename);
			return null;
		}
	}
}
