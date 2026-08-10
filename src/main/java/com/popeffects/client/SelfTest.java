package com.popeffects.client;

import java.io.File;

import com.popeffects.PopEffects;
import com.popeffects.config.EffectSettings;
import com.popeffects.config.EffectStyle;
import com.popeffects.config.TriggerType;
import com.popeffects.effect.EffectManager;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;

/**
 * Spielt nach dem Betreten einer Welt einmal jeden Effekt-Stil ab und legt von
 * jedem einen Screenshot ab.
 *
 * <p>Damit laesst sich nach einem Minecraft-Update in einem Rutsch pruefen, ob
 * die Render-Pipelines noch stehen - ein Blick in {@code screenshots/} genuegt.
 * Laeuft nur, wenn beim Start {@code -Dpopeffects.selftest=true} gesetzt ist,
 * im normalen Spiel passiert hier also nichts.
 */
public final class SelfTest {
	private static final String PROPERTY = "popeffects.selftest";

	/** Wartezeit nach dem Beitreten, damit die Welt geladen ist. */
	private static final int WARMUP_TICKS = 60;

	/** Ticks zwischen Ausloesen und Screenshot - dann ist der Effekt gross. */
	private static final int SHOT_DELAY = 8;

	/** Ticks von einem Stil zum naechsten. */
	private static final int STYLE_INTERVAL = 24;

	private static int ticksUntilNextStep = -1;
	private static int nextStyle;
	private static boolean shotPending;

	private SelfTest() {
	}

	public static void registerIfRequested() {
		if (!Boolean.getBoolean(PROPERTY)) {
			return;
		}

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ticksUntilNextStep = WARMUP_TICKS;
			nextStyle = 0;
			shotPending = false;
		});

		ClientTickEvents.END_CLIENT_TICK.register(SelfTest::tick);
		PopEffects.LOGGER.info("Selbsttest aktiv - spielt nach dem Beitreten alle Stile ab");
	}

	private static void tick(MinecraftClient client) {
		if (ticksUntilNextStep < 0 || client.player == null) {
			return;
		}

		if (--ticksUntilNextStep > 0) {
			return;
		}

		if (shotPending) {
			shotPending = false;
			ticksUntilNextStep = STYLE_INTERVAL - SHOT_DELAY;
			screenshot(client, EffectStyle.values()[nextStyle - 1]);
			return;
		}

		EffectStyle[] styles = EffectStyle.values();

		if (nextStyle >= styles.length) {
			ticksUntilNextStep = -1;
			PopEffects.LOGGER.info("Selbsttest OK - {} Stile ohne Fehler gezeichnet", styles.length);
			return;
		}

		EffectStyle style = styles[nextStyle++];

		EffectSettings settings = EffectSettings.defaultFor(TriggerType.TOTEM_POP);
		settings.style = style;
		settings.durationTicks = 24;
		settings.sound = false;
		settings.followEntity = false;

		// Der Effekt erscheint vier Bloecke vor dem Spieler - also schauen wir
		// leicht nach unten, damit er sicher im Bild ist.
		client.player.setPitch(15.0F);

		EffectManager.preview(TriggerType.TOTEM_POP, settings);
		PopEffects.LOGGER.info("Selbsttest: Stil {}", style);

		shotPending = true;
		ticksUntilNextStep = SHOT_DELAY;
	}

	private static void screenshot(MinecraftClient client, EffectStyle style) {
		File directory = new File(client.runDirectory, ScreenshotRecorder.SCREENSHOTS_DIRECTORY);
		String name = "selftest-" + style.name().toLowerCase(java.util.Locale.ROOT) + ".png";

		ScreenshotRecorder.saveScreenshot(client.runDirectory, name, client.getFramebuffer(), 1,
				message -> PopEffects.LOGGER.info("Selbsttest-Bild: {}", new File(directory, name)));
	}
}
