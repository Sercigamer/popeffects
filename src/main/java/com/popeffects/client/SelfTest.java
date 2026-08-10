package com.popeffects.client;

import java.io.File;
import java.util.Locale;

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
 * jedem zwei Screenshots ab: einen frueh und einen kurz vor Schluss.
 *
 * <p>Zwei Bilder deshalb, weil man erst am Paar sieht, ob das Ausblenden
 * wirklich greift - ein einzelnes Bild sagt darueber nichts. Nach einem
 * Minecraft-Update genuegt so ein Blick in {@code screenshots/}, um zu wissen,
 * ob die Render-Pipelines noch stehen.
 *
 * <p>Laeuft nur mit {@code -Dpopeffects.selftest=true}, im normalen Spiel
 * passiert hier also nichts.
 */
public final class SelfTest {
	private static final String PROPERTY = "popeffects.selftest";

	/** Wartezeit nach dem Beitreten, damit die Welt geladen ist. */
	private static final int WARMUP_TICKS = 60;

	/** Lebensdauer, mit der jeder Stil im Test laeuft. */
	private static final int DURATION_TICKS = 24;

	/** Alter beim ersten Bild - da ist der Effekt voll da. */
	private static final int EARLY_SHOT_AT = 6;

	/** Alter beim zweiten Bild - da sollte er sichtbar blasser sein. */
	private static final int LATE_SHOT_AT = 20;

	/** Pause zwischen zwei Stilen. */
	private static final int GAP_TICKS = 8;

	/** Wartezeit, bis die neue Blickrichtung auch gezeichnet wurde. */
	private static final int AIM_SETTLE_TICKS = 10;

	/**
	 * Hoehe ueber dem Boden. Hoch genug, dass hohes Gras den Effekt nicht
	 * verdeckt - aber deutlich unter Augenhoehe, sonst sieht man die flachen
	 * Stile wie Ring und Scheibe genau von der Kante und damit fast gar nicht.
	 */
	private static final float TEST_Y_OFFSET = 0.6F;

	/** Blickwinkel nach unten, damit auch die flachen Stile Flaeche zeigen. */
	private static final float TEST_PITCH = 25.0F;

	private enum Phase {
		/** Blickrichtung festlegen und dem Bild Zeit geben, sie zu uebernehmen. */
		AIM,
		/** Ein Bild der leeren Szene als Vergleichsmassstab. */
		BASELINE,
		SPAWN,
		EARLY_SHOT,
		LATE_SHOT
	}

	private static int ticksUntilNextStep = -1;
	private static Phase phase = Phase.SPAWN;
	private static int nextStyle;
	private static EffectStyle currentStyle;

	private SelfTest() {
	}

	public static void registerIfRequested() {
		if (!Boolean.getBoolean(PROPERTY)) {
			return;
		}

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ticksUntilNextStep = WARMUP_TICKS;
			phase = Phase.AIM;
			nextStyle = 0;
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

		switch (phase) {
			case AIM -> {
				// Der Effekt erscheint vor dem Spieler - also leicht nach
				// unten schauen, damit er sicher im Bild ist.
				client.player.setPitch(TEST_PITCH);
				phase = Phase.BASELINE;
				ticksUntilNextStep = AIM_SETTLE_TICKS;
			}
			case BASELINE -> {
				// Erst jetzt schiessen: ein Screenshot greift das zuletzt
				// gezeichnete Bild ab, und das entstand noch mit der alten
				// Blickrichtung. Sonst zeigt das Vergleichsbild eine andere
				// Szene als die Aufnahmen danach - und taugt zu nichts.
				screenshotNamed(client, "selftest-00-leer.png");
				phase = Phase.SPAWN;
				ticksUntilNextStep = GAP_TICKS;
			}
			case SPAWN -> spawnNextStyle(client);
			case EARLY_SHOT -> {
				screenshot(client, "fruh");
				phase = Phase.LATE_SHOT;
				ticksUntilNextStep = LATE_SHOT_AT - EARLY_SHOT_AT;
			}
			case LATE_SHOT -> {
				screenshot(client, "spaet");
				phase = Phase.SPAWN;
				ticksUntilNextStep = GAP_TICKS;
			}
		}
	}

	private static void spawnNextStyle(MinecraftClient client) {
		EffectStyle[] styles = EffectStyle.values();

		if (nextStyle >= styles.length) {
			ticksUntilNextStep = -1;
			PopEffects.LOGGER.info("Selbsttest OK - {} Stile ohne Fehler gezeichnet", styles.length);

			// Danach beenden. Sonst bleibt das Fenster offen, haelt den
			// Weltordner gesperrt und der naechste Durchlauf kommt gar nicht
			// erst hinein.
			client.scheduleStop();
			return;
		}

		currentStyle = styles[nextStyle++];

		EffectSettings settings = EffectSettings.defaultFor(TriggerType.TOTEM_POP);
		settings.style = currentStyle;
		settings.durationTicks = DURATION_TICKS;
		settings.fadeInTicks = 2;
		settings.fadeOutTicks = 12;
		settings.sound = false;
		settings.followEntity = false;
		settings.yOffset = TEST_Y_OFFSET;

		EffectManager.preview(TriggerType.TOTEM_POP, settings);
		PopEffects.LOGGER.info("Selbsttest: Stil {}", currentStyle);

		phase = Phase.EARLY_SHOT;
		ticksUntilNextStep = EARLY_SHOT_AT;
	}

	private static void screenshot(MinecraftClient client, String suffix) {
		screenshotNamed(client, "selftest-" + currentStyle.name().toLowerCase(Locale.ROOT) + "-" + suffix + ".png");
	}

	private static void screenshotNamed(MinecraftClient client, String name) {
		File directory = new File(client.runDirectory, ScreenshotRecorder.SCREENSHOTS_DIRECTORY);

		ScreenshotRecorder.saveScreenshot(client.runDirectory, name, client.getFramebuffer(), 1,
				message -> PopEffects.LOGGER.info("Selbsttest-Bild: {}", new File(directory, name)));
	}
}
