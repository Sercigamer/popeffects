package com.popeffects.client;

import com.popeffects.PopEffects;
import com.popeffects.config.ConfigManager;
import com.popeffects.effect.EffectManager;
import com.popeffects.effect.EffectRenderer;
import com.popeffects.effect.PopCounter;
import com.popeffects.effect.PopRenderLayers;
import com.popeffects.trigger.PopTriggers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public final class PopEffectsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ConfigManager.load();

		// Muss vor dem ersten Uebersetzen der Shader passieren, deshalb gleich
		// hier und nicht erst beim ersten Effekt.
		PopRenderLayers.init();

		EffectRenderer.register();
		PopEffectsKeybinds.register();
		PopEffectsCommands.register();

		ClientTickEvents.END_CLIENT_TICK.register(EffectManager::tick);

		SelfTest.registerIfRequested();

		HudElementRegistry.attachElementAfter(VanillaHudElements.STATUS_EFFECTS, PopEffects.id("pop_counter"),
				new PopCounterHud());

		// Beim Serverwechsel sind alle Entity-IDs hinfaellig.
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			EffectManager.clear();
			PopCounter.clear();
			PopTriggers.reset();
		});

		PopEffects.LOGGER.info("PopEffects bereit - Effekte {}", ConfigManager.get().enabled ? "an" : "aus");
	}
}
