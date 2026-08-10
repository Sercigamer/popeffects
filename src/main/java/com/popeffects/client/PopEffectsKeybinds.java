package com.popeffects.client;

import org.lwjgl.glfw.GLFW;

import com.popeffects.client.screen.PopEffectsConfigScreen;
import com.popeffects.config.ConfigManager;
import com.popeffects.config.PopEffectsConfig;
import com.popeffects.config.TriggerType;
import com.popeffects.effect.EffectManager;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Tastenbelegungen. Standard: {@code P} oeffnet das Menue, der Rest ist frei -
 * die Mod soll niemandem eine Taste wegnehmen.
 */
public final class PopEffectsKeybinds {
	public static KeyBinding openConfig;
	public static KeyBinding toggleMod;
	public static KeyBinding preview;

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
		openConfig = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.popeffects.open_config", GLFW.GLFW_KEY_P, KeyBinding.Category.MISC));

		toggleMod = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.popeffects.toggle_mod", GLFW.GLFW_KEY_UNKNOWN, KeyBinding.Category.MISC));

		preview = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.popeffects.preview", GLFW.GLFW_KEY_UNKNOWN, KeyBinding.Category.MISC));

		ClientTickEvents.END_CLIENT_TICK.register(PopEffectsKeybinds::onTick);
	}

	private static void onTick(MinecraftClient client) {
		while (openConfig.wasPressed()) {
			requestConfigScreen();
		}

		if (configScreenRequested && client.currentScreen == null) {
			configScreenRequested = false;
			client.setScreen(new PopEffectsConfigScreen(null));
		}

		if (client.player == null) {
			return;
		}

		while (toggleMod.wasPressed()) {
			PopEffectsConfig config = ConfigManager.get();
			config.enabled = !config.enabled;
			ConfigManager.save();

			if (!config.enabled) {
				EffectManager.clear();
			}

			client.player.sendMessage(Text.translatable(config.enabled
					? "popeffects.message.enabled"
					: "popeffects.message.disabled")
					.formatted(config.enabled ? Formatting.GREEN : Formatting.GRAY), true);
		}

		while (preview.wasPressed()) {
			EffectManager.preview(TriggerType.TOTEM_POP, ConfigManager.get().totemPop);
		}
	}
}
