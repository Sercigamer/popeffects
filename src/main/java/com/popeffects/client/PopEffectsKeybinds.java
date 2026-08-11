package com.popeffects.client;

import org.lwjgl.glfw.GLFW;

import com.popeffects.client.screen.PopEffectsConfigScreen;
import com.popeffects.config.ConfigManager;
import com.popeffects.config.PopEffectsConfig;
import com.popeffects.config.TriggerType;
import com.popeffects.effect.EffectManager;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Tastenbelegungen. Standard: {@code P} oeffnet das Menue, der Rest ist frei -
 * die Mod soll niemandem eine Taste wegnehmen.
 */
public final class PopEffectsKeybinds {
	public static KeyMapping openConfig;
	public static KeyMapping toggleMod;
	public static KeyMapping preview;

	/**
	 * Ein Screen darf nicht mitten aus einem Command heraus geoeffnet werden -
	 * der schliessende Chat wuerde ihn sofort wieder wegraeumen. Also merken
	 * und im naechsten Tick oeffnen.
	 */
	private static boolean configScreenRequested;

	private PopEffectsKeybinds() {
	}

	public static void requestConfigScreen() {
		configScreenRequested = true;
	}

	public static void register() {
		openConfig = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.popeffects.open_config", GLFW.GLFW_KEY_P, KeyMapping.Category.MISC));

		toggleMod = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.popeffects.toggle_mod", GLFW.GLFW_KEY_UNKNOWN, KeyMapping.Category.MISC));

		preview = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.popeffects.preview", GLFW.GLFW_KEY_UNKNOWN, KeyMapping.Category.MISC));

		ClientTickEvents.END_CLIENT_TICK.register(PopEffectsKeybinds::onTick);
	}

	private static void onTick(Minecraft client) {
		while (openConfig.consumeClick()) {
			requestConfigScreen();
		}

		if (configScreenRequested) {
			configScreenRequested = false;
			client.setScreenAndShow(new PopEffectsConfigScreen(null));
		}

		if (client.player == null) {
			return;
		}

		while (toggleMod.consumeClick()) {
			PopEffectsConfig config = ConfigManager.get();
			config.enabled = !config.enabled;
			ConfigManager.save();

			if (!config.enabled) {
				EffectManager.clear();
			}

			client.player.sendOverlayMessage(Component.translatable(config.enabled
					? "popeffects.message.enabled"
					: "popeffects.message.disabled")
					.withStyle(config.enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY));
		}

		while (preview.consumeClick()) {
			EffectManager.preview(TriggerType.TOTEM_POP, ConfigManager.get().totemPop);
		}
	}
}
