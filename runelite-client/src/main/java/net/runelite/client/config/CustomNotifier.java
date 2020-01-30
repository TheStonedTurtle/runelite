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
package net.runelite.client.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.runelite.client.Notifier;

/**
 * Add to a {@link Config} to auto create options for customizing the notifier.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CustomNotifier
{
	/**
	 * If disabled the notification settings from {@link RuneLiteConfig} will be used
	 * @return should use custom notifier
	 */
	@ConfigItem(
		keyName = "enableCustomNotifications",
		name = "Enable notifier overrides",
		description = "Toggles whether the client will prioritize these notification settings.",
		position = 0
	)
	boolean enableCustomNotifier() default false;

	@ConfigItem(
		keyName = "enableTrayNotifications",
		name = "Enable tray notifications",
		description = "Enables tray notifications",
		position = 1
	)
	boolean enableTrayNotifications() default true;

	@ConfigItem(
		keyName = "requestFocusOnNotification",
		name = "Request focus on notification",
		description = "Toggles window focus request",
		position = 2
	)
	boolean requestFocusOnNotification() default true;

	@ConfigItem(
		keyName = "notificationSound",
		name = "Notification sound",
		description = "Enables the playing of a beep sound when notifications are displayed",
		position = 3
	)
	Notifier.NativeCustomOff notificationSound() default Notifier.NativeCustomOff.NATIVE;

	@ConfigItem(
		keyName = "enableGameMessageNotification",
		name = "Enable game message notifications",
		description = "Puts a notification message in the chatbox",
		position = 4
	)
	boolean enableGameMessageNotification() default false;

	@ConfigItem(
		keyName = "flashNotification",
		name = "Flash notification",
		description = "Flashes the game frame as a notification",
		position = 5
	)
	FlashNotification flashNotification() default FlashNotification.DISABLED;

	@ConfigItem(
		keyName = "sendNotificationsWhenFocused",
		name = "Send notifications when focused",
		description = "Toggles all notifications for when the client is focused",
		position = 6
	)
	boolean sendNotificationsWhenFocused() default false;
}
