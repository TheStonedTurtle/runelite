/*
 * Copyright (c) 2017, Seth <Sethtroll3@gmail.com>
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

import com.google.common.base.Strings;
import com.google.inject.Provides;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GameState;
import net.runelite.api.IndexedSprite;
import net.runelite.api.SpritePixels;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.SessionOpen;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.OSType;

@PluginDescriptor(
	name = "Login Screen",
	description = "Provides various enhancements for login screen"
)
@Slf4j
public class LoginScreenPlugin extends Plugin implements KeyListener
{
	private static final int MAX_USERNAME_LENGTH = 254;
	private static final int MAX_PIN_LENGTH = 6;
	private static final File CUSTOM_LOGIN_SCREEN_FILE = new File(RuneLite.RUNELITE_DIR, "login.png");

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private LoginScreenConfig config;

	@Inject
	private KeyManager keyManager;

	@Inject
	private SpriteManager spriteManager;

	private String usernameCache;

	@Override
	protected void startUp() throws Exception
	{
		applyUsername();
		keyManager.registerKeyListener(this);
		clientThread.invoke(this::overrideLoginScreen);
		clientThread.invoke(this::overrideLoginSprites);
	}

	@Override
	protected void shutDown() throws Exception
	{
		if (config.syncUsername())
		{
			client.getPreferences().setRememberedUsername(usernameCache);
		}

		keyManager.unregisterKeyListener(this);
		clientThread.invoke(() ->
		{
			restoreLoginScreen();
			restoreLoginScreenSprites();
			client.setShouldRenderLoginScreenFire(true);
		});
	}

	@Provides
	LoginScreenConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LoginScreenConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("loginscreen"))
		{
			clientThread.invoke(this::overrideLoginScreen);
			clientThread.invoke(this::overrideLoginSprites);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (!config.syncUsername())
		{
			return;
		}

		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			applyUsername();
			if (config.loginSprites() != LoginScreenSpriteOverride.OFF)
			{
				overrideLoginSprites();
			}
		}
		else if (event.getGameState() == GameState.LOGGED_IN)
		{
			String username = "";

			if (client.getPreferences().getRememberedUsername() != null)
			{
				username = client.getUsername();
			}

			if (config.username().equals(username))
			{
				return;
			}

			log.debug("Saving username: {}", username);
			config.username(username);
		}
	}

	@Subscribe
	public void onSessionOpen(SessionOpen event)
	{
		// configuation for the account is available now, so update the username
		applyUsername();
	}

	private void applyUsername()
	{
		if (!config.syncUsername())
		{
			return;
		}

		GameState gameState = client.getGameState();
		if (gameState == GameState.LOGIN_SCREEN)
		{
			String username = config.username();

			if (Strings.isNullOrEmpty(username))
			{
				return;
			}

			// Save it only once
			if (usernameCache == null)
			{
				usernameCache = client.getPreferences().getRememberedUsername();
			}

			client.getPreferences().setRememberedUsername(username);
		}
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (!config.pasteEnabled() || (
			client.getGameState() != GameState.LOGIN_SCREEN &&
			client.getGameState() != GameState.LOGIN_SCREEN_AUTHENTICATOR))
		{
			return;
		}

		// enable pasting on macOS with the Command (meta) key
		boolean isModifierDown = OSType.getOSType() == OSType.MacOS ? e.isMetaDown() : e.isControlDown();

		if (e.getKeyCode() == KeyEvent.VK_V && isModifierDown)
		{
			try
			{
				final String data = Toolkit
					.getDefaultToolkit()
					.getSystemClipboard()
					.getData(DataFlavor.stringFlavor)
					.toString()
					.trim();

				switch (client.getLoginIndex())
				{
					// Username/password form
					case 2:
						if (client.getCurrentLoginField() == 0)
						{
							// Truncate data to maximum username length if necessary
							client.setUsername(data.substring(0, Math.min(data.length(), MAX_USERNAME_LENGTH)));
						}

						break;
					// Authenticator form
					case 4:
						// Truncate data to maximum OTP code length if necessary
						client.setOtp(data.substring(0, Math.min(data.length(), MAX_PIN_LENGTH)));
						break;
				}
			}
			catch (UnsupportedFlavorException | IOException ex)
			{
				log.warn("failed to fetch clipboard data", ex);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{

	}

	private void overrideLoginScreen()
	{
		client.setShouldRenderLoginScreenFire(config.showLoginFire());

		if (config.loginScreen() == LoginScreenOverride.OFF)
		{
			restoreLoginScreen();
			return;
		}

		SpritePixels pixels = null;
		if (config.loginScreen() == LoginScreenOverride.CUSTOM)
		{
			if (CUSTOM_LOGIN_SCREEN_FILE.exists())
			{
				try
				{
					BufferedImage image;
					synchronized (ImageIO.class)
					{
						image = ImageIO.read(CUSTOM_LOGIN_SCREEN_FILE);
					}

					if (image.getHeight() > Constants.GAME_FIXED_HEIGHT)
					{
						final double scalar = Constants.GAME_FIXED_HEIGHT / (double) image.getHeight();
						image = ImageUtil.resizeImage(image, (int) (image.getWidth() * scalar), Constants.GAME_FIXED_HEIGHT);
					}
					pixels = ImageUtil.getImageSpritePixels(image, client);
				}
				catch (IOException e)
				{
					log.error("error loading custom login screen", e);
					restoreLoginScreen();
					return;
				}
			}
		}
		else
		{
			pixels = getFileSpritePixels(config.loginScreen().getFileName());
		}

		if (pixels != null)
		{
			client.setLoginScreen(pixels);
		}
	}

	private void restoreLoginScreen()
	{
		client.setLoginScreen(null);
	}

	private SpritePixels getFileSpritePixels(String file)
	{
		try
		{
			log.debug("Loading: {}", file);
			BufferedImage image = ImageUtil.getResourceStreamFromClass(this.getClass(), file);
			return ImageUtil.getImageSpritePixels(image, client);
		}
		catch (RuntimeException ex)
		{
			log.debug("Unable to load image: ", ex);
		}

		return null;
	}

	private IndexedSprite getIndexedSprite(LoginScreenSprites sprite)
	{
		if (config.loginSprites() == LoginScreenSpriteOverride.OFF)
		{
			return getIndexedSpriteFromSpriteManager(sprite);
		}

		return getIndexedSpriteFromFile(sprite, config.loginSprites());
	}

	private IndexedSprite getIndexedSpriteFromSpriteManager(LoginScreenSprites sprite)
	{
		final BufferedImage img = spriteManager.getSprite(sprite.getSpriteID(), sprite.getFileIdx());
		if (img == null)
		{
			// These sprites should always exist?
			return ImageUtil.getImageIndexedSprite(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), client);
		}

		return ImageUtil.getImageIndexedSprite(img, client);
	}

	private IndexedSprite getIndexedSpriteFromFile(LoginScreenSprites sprite, LoginScreenSpriteOverride override)
	{
		final BufferedImage img = sprite.getOverrideImage(override);
		if (img == null)
		{
			// Default to the current jagex sprite
			return getIndexedSpriteFromSpriteManager(sprite);
		}

		return ImageUtil.getImageIndexedSprite(img, client);
	}

	private void restoreLoginScreenSprites()
	{
		client.setTitleWorldSwitchSprite(null);

		// All other login sprites are reset on GameState changed
		if (client.getGameState() == GameState.LOGIN_SCREEN)
		{
			client.setGameState(GameState.LOGIN_SCREEN);
		}
	}

	private void overrideLoginSprites()
	{
		final LoginScreenSpriteOverride spriteOverride = config.loginSprites();
		if (spriteOverride == LoginScreenSpriteOverride.OFF)
		{
			restoreLoginScreenSprites();
			return;
		}

		client.setLogoSprite(getIndexedSprite(LoginScreenSprites.LOGO));
		client.setTitleBox(getIndexedSprite(LoginScreenSprites.DIALOG_BACKGROUND));
		client.setTitleLoginButtonSprite(getIndexedSprite(LoginScreenSprites.BUTTON_BACKGROUND));
		client.setTitleWorldSwitchSprite(getIndexedSprite(LoginScreenSprites.WORLD_SELECT));
		client.setTitlemuteSprite(new IndexedSprite[]{
			getIndexedSprite(LoginScreenSprites.MUSIC_BUTTON), getIndexedSprite(LoginScreenSprites.MUSIC_BUTTON_MUTED)
		});
	}
}