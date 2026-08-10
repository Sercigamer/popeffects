package com.popeffects.config;

import java.util.Locale;

/**
 * Alles, was einen einzelnen Effekt ausmacht. Jeder {@link TriggerType} hat
 * eine eigene Instanz davon, deshalb kann ein Totem-Pop komplett anders
 * aussehen als ein Kill.
 *
 * <p>Die Felder sind nach Themen sortiert - erst was, dann welche Farbe, dann
 * wie lange, dann wie gross, zuletzt Ton und Partikel. In derselben
 * Reihenfolge landen sie auch in der JSON-Datei, damit man sie dort von Hand
 * bearbeiten oder mit Freunden tauschen kann.
 */
public final class EffectSettings {
	/** Vorschlaege fuer den Sound-Knopf im Menue. Eigene IDs gehen trotzdem. */
	public static final String[] SOUND_PRESETS = {
			"minecraft:block.beacon.activate",
			"minecraft:entity.player.levelup",
			"minecraft:block.note_block.pling",
			"minecraft:entity.wither.spawn",
			"minecraft:entity.generic.explode",
			"minecraft:block.end_portal_frame.fill",
			"minecraft:item.totem.use",
			"minecraft:entity.ender_dragon.growl"
	};

	/** Vorschlaege fuer den Partikel-Knopf. Nur Partikel ohne Extra-Daten. */
	public static final String[] PARTICLE_PRESETS = {
			"minecraft:end_rod",
			"minecraft:crit",
			"minecraft:electric_spark",
			"minecraft:soul_fire_flame",
			"minecraft:totem_of_undying",
			"minecraft:enchanted_hit",
			"minecraft:firework",
			"minecraft:glow"
	};

	// --- Was ---------------------------------------------------------------

	public boolean enabled = true;
	public EffectStyle style = EffectStyle.RING;

	// --- Farbe -------------------------------------------------------------

	/** Farbe am Anfang, als {@code #RRGGBB}. */
	public String colorStart = "#FF5555";

	/** Farbe am Ende. Wird nur benutzt, wenn {@link #gradient} an ist. */
	public String colorEnd = "#FFAA00";

	/** Ueber die Lebensdauer von {@link #colorStart} nach {@link #colorEnd} blenden. */
	public boolean gradient = true;

	/** Ignoriert beide Farben und dreht stattdessen den Farbkreis durch. */
	public boolean rainbow = false;

	/** Deckkraft 0-255, bevor das Ein- und Ausblenden daraufkommt. */
	public int alpha = 200;

	/** Additiv mischen - leuchtet staerker, sieht nach Neon aus. */
	public boolean additive = true;

	/** Auch durch Waende sichtbar. */
	public boolean throughWalls = false;

	// --- Zeit --------------------------------------------------------------

	/** Lebensdauer in Ticks (20 Ticks = 1 Sekunde). */
	public int durationTicks = 24;

	/** So viele Ticks am Anfang wird eingeblendet. 0 = sofort voll da. */
	public int fadeInTicks = 2;

	/** So viele Ticks am Ende wird ausgeblendet. 0 = harter Schnitt. */
	public int fadeOutTicks = 12;

	// --- Groesse -----------------------------------------------------------

	/** Radius am Anfang und am Ende, in Bloecken. */
	public float startRadius = 0.4F;
	public float endRadius = 3.0F;

	/** Hoehe fuer Kuppel, Kugel, Saeule und Spirale. */
	public float height = 2.2F;

	/** Verschiebung nach oben, gemessen ab den Fuessen. */
	public float yOffset = 0.05F;

	/** Dicke der Baender in Bloecken. */
	public float thickness = 0.18F;

	/** Ecken pro Ring. Mehr = runder, aber auch mehr Dreiecke. */
	public int segments = 48;

	/** Anzahl Ringe bei Schockwelle, Kuppel und Kugel; Windungen bei der Spirale. */
	public int ringCount = 3;

	/** Drehung in Grad pro Sekunde. 0 = steht still. */
	public float rotationSpeed = 40.0F;

	/** Effekt haengt am Ziel statt an der Stelle, wo er ausgeloest wurde. */
	public boolean followEntity = true;

	// --- Ausloesen ---------------------------------------------------------

	/** Ab wie viel Schaden ausgeloest wird. Nur bei den Schadens-Effekten. */
	public float threshold = 6.0F;

	/** Radius waechst mit dem Schaden - nur bei Schadens-Triggern sinnvoll. */
	public boolean scaleWithDamage = true;

	// --- Ton und Partikel --------------------------------------------------

	public boolean sound = true;
	public String soundId = "minecraft:block.beacon.activate";
	public float soundVolume = 0.7F;
	public float soundPitch = 1.2F;

	public boolean particles = false;
	public String particleId = "minecraft:end_rod";
	public int particleCount = 24;

	/**
	 * Standardwerte je Trigger. Bewusst unterschiedlich, damit man nach der
	 * Installation sofort sieht, welcher Effekt woher kommt.
	 */
	public static EffectSettings defaultFor(TriggerType type) {
		EffectSettings settings = new EffectSettings();

		switch (type) {
			case TOTEM_POP -> {
				settings.style = EffectStyle.SHOCKWAVE;
				settings.colorStart = "#FFD54A";
				settings.colorEnd = "#37D67A";
				settings.endRadius = 3.4F;
				settings.durationTicks = 28;
				settings.fadeInTicks = 2;
				settings.fadeOutTicks = 18;
				settings.ringCount = 3;
				settings.soundId = "minecraft:block.beacon.activate";
				settings.soundPitch = 1.4F;
				settings.scaleWithDamage = false;
			}
			case BIG_DAMAGE -> {
				settings.style = EffectStyle.RING;
				settings.colorStart = "#FF3B3B";
				settings.colorEnd = "#FF9F1C";
				settings.endRadius = 2.6F;
				settings.durationTicks = 18;
				settings.fadeInTicks = 1;
				settings.fadeOutTicks = 12;
				settings.threshold = 6.0F;
				settings.sound = false;
				settings.soundId = "minecraft:block.note_block.pling";
			}
			case KILL -> {
				settings.style = EffectStyle.DOME;
				settings.colorStart = "#B05CFF";
				settings.colorEnd = "#2E1A66";
				settings.endRadius = 2.2F;
				settings.height = 2.6F;
				settings.durationTicks = 32;
				settings.fadeInTicks = 3;
				settings.fadeOutTicks = 20;
				settings.ringCount = 5;
				settings.followEntity = false;
				settings.scaleWithDamage = false;
				settings.soundId = "minecraft:entity.wither.spawn";
				settings.soundVolume = 0.5F;
				settings.soundPitch = 1.8F;
			}
			case SELF_HURT -> {
				settings.enabled = false;
				settings.style = EffectStyle.PILLAR;
				settings.colorStart = "#FF2D55";
				settings.colorEnd = "#7A0025";
				settings.endRadius = 1.4F;
				settings.height = 2.0F;
				settings.durationTicks = 16;
				settings.fadeInTicks = 1;
				settings.fadeOutTicks = 10;
				settings.threshold = 6.0F;
				settings.sound = false;
			}
		}

		return settings;
	}

	public int colorStartRgb() {
		return parseColor(colorStart, 0xFF5555);
	}

	public int colorEndRgb() {
		return parseColor(colorEnd, 0xFFAA00);
	}

	public void setColorStartRgb(int rgb) {
		colorStart = formatColor(rgb);
	}

	public void setColorEndRgb(int rgb) {
		colorEnd = formatColor(rgb);
	}

	/** {@code "#FF5555"} wird zu {@code 0xFF5555}. */
	public static int parseColor(String hex, int fallback) {
		if (hex == null) {
			return fallback;
		}

		String cleaned = hex.trim();

		if (cleaned.startsWith("#")) {
			cleaned = cleaned.substring(1);
		}

		try {
			return Integer.parseInt(cleaned, 16) & 0xFFFFFF;
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	public static String formatColor(int rgb) {
		return String.format(Locale.ROOT, "#%06X", rgb & 0xFFFFFF);
	}

	/**
	 * Haelt die Werte in einem Bereich, in dem sie weder unsichtbar sind noch
	 * die Grafikkarte quaelen. Wird nach jedem Laden aufgerufen, damit auch
	 * eine von Hand verbogene JSON-Datei das Spiel nicht ruiniert.
	 */
	public void sanitize() {
		if (style == null) {
			style = EffectStyle.RING;
		}

		// Schreibt nebenbei krumme Schreibweisen wie "ff5555" oder "#FF5555 "
		// in die einheitliche Form zurueck.
		colorStart = formatColor(colorStartRgb());
		colorEnd = formatColor(colorEndRgb());

		alpha = clamp(alpha, 10, 255);
		durationTicks = clamp(durationTicks, 3, 200);
		fadeInTicks = clamp(fadeInTicks, 0, durationTicks);
		fadeOutTicks = clamp(fadeOutTicks, 0, durationTicks);

		// Zusammen duerfen beide nicht laenger als der Effekt selbst sein,
		// sonst waere er nie voll sichtbar. Das Ausblenden hat Vorrang, weil
		// genau darum der Effekt gebaut ist.
		if (fadeInTicks + fadeOutTicks > durationTicks) {
			fadeInTicks = durationTicks - fadeOutTicks;
		}

		startRadius = clamp(startRadius, 0.0F, 32.0F);
		endRadius = clamp(endRadius, 0.2F, 32.0F);
		height = clamp(height, 0.2F, 16.0F);
		yOffset = clamp(yOffset, -4.0F, 8.0F);
		thickness = clamp(thickness, 0.02F, 4.0F);
		segments = clamp(segments, 8, 96);
		ringCount = clamp(ringCount, 1, 12);
		rotationSpeed = clamp(rotationSpeed, -360.0F, 360.0F);
		threshold = clamp(threshold, 0.5F, 200.0F);
		soundVolume = clamp(soundVolume, 0.0F, 2.0F);
		soundPitch = clamp(soundPitch, 0.5F, 2.0F);
		particleCount = clamp(particleCount, 0, 128);

		if (soundId == null || soundId.isBlank()) {
			soundId = SOUND_PRESETS[0];
		}

		if (particleId == null || particleId.isBlank()) {
			particleId = PARTICLE_PRESETS[0];
		}

		// Ein Endradius kleiner als der Startradius laesst den Effekt
		// schrumpfen. Das ist erlaubt - es sieht bei der Saeule gut aus.
	}

	/**
	 * Kopie fuer einen laufenden Effekt. Damit aendert sich ein Effekt nicht
	 * mitten in der Animation, wenn man gerade im Menue an den Reglern dreht.
	 */
	public EffectSettings copy() {
		EffectSettings copy = new EffectSettings();

		copy.enabled = enabled;
		copy.style = style;
		copy.colorStart = colorStart;
		copy.colorEnd = colorEnd;
		copy.gradient = gradient;
		copy.rainbow = rainbow;
		copy.alpha = alpha;
		copy.additive = additive;
		copy.throughWalls = throughWalls;
		copy.durationTicks = durationTicks;
		copy.fadeInTicks = fadeInTicks;
		copy.fadeOutTicks = fadeOutTicks;
		copy.startRadius = startRadius;
		copy.endRadius = endRadius;
		copy.height = height;
		copy.yOffset = yOffset;
		copy.thickness = thickness;
		copy.segments = segments;
		copy.ringCount = ringCount;
		copy.rotationSpeed = rotationSpeed;
		copy.followEntity = followEntity;
		copy.threshold = threshold;
		copy.scaleWithDamage = scaleWithDamage;
		copy.sound = sound;
		copy.soundId = soundId;
		copy.soundVolume = soundVolume;
		copy.soundPitch = soundPitch;
		copy.particles = particles;
		copy.particleId = particleId;
		copy.particleCount = particleCount;

		return copy;
	}

	private static int clamp(int value, int min, int max) {
		return Math.min(max, Math.max(min, value));
	}

	private static float clamp(float value, float min, float max) {
		return Math.min(max, Math.max(min, value));
	}
}
