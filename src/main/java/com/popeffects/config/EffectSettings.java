package com.popeffects.config;

/**
 * Alles, was einen einzelnen Effekt ausmacht. Jeder {@link TriggerType} hat
 * eine eigene Instanz davon, deshalb kann ein Totem-Pop komplett anders
 * aussehen als ein Kill.
 *
 * <p>Die Felder sind absichtlich oeffentlich und einfach gehalten: so bleibt
 * die JSON-Datei lesbar und man kann sie auch von Hand bearbeiten oder mit
 * Freunden tauschen.
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

	public boolean enabled = true;
	public EffectStyle style = EffectStyle.RING;

	/** Farbe am Anfang des Effekts, als 0xRRGGBB. */
	public int colorStart = 0xFF5555;

	/** Farbe am Ende. Wird nur benutzt, wenn {@link #gradient} an ist. */
	public int colorEnd = 0xFFAA00;

	/** Ueber die Lebensdauer von {@link #colorStart} nach {@link #colorEnd} blenden. */
	public boolean gradient = true;

	/** Ignoriert beide Farben und dreht stattdessen den Farbkreis durch. */
	public boolean rainbow = false;

	/** Deckkraft 0-255. */
	public int alpha = 200;

	/** Additiv mischen - leuchtet staerker, sieht nach Neon aus. */
	public boolean additive = true;

	/** Auch durch Waende sichtbar. */
	public boolean throughWalls = false;

	/** Lebensdauer in Ticks (20 Ticks = 1 Sekunde). */
	public int durationTicks = 20;

	/** Radius am Anfang und am Ende, in Bloecken. */
	public float startRadius = 0.4F;
	public float endRadius = 3.0F;

	/** Hoehe fuer Kuppel, Kugel, Saeule und Spirale. */
	public float height = 2.2F;

	/** Verschiebung nach oben, gemessen ab den Fuessen. */
	public float yOffset = 0.05F;

	/** Dicke der Linien bzw. Baender in Bloecken. */
	public float thickness = 0.18F;

	/** Ecken pro Ring. Mehr = runder, aber auch mehr Dreiecke. */
	public int segments = 48;

	/** Anzahl Ringe bei Schockwelle, Kuppel und Kugel. */
	public int ringCount = 3;

	/** Drehung in Grad pro Sekunde. 0 = steht still. */
	public float rotationSpeed = 40.0F;

	/** Effekt haengt am Ziel statt an der Stelle, wo er ausgeloest wurde. */
	public boolean followEntity = true;

	/** Radius waechst mit dem Schaden - nur bei Schadens-Triggern sinnvoll. */
	public boolean scaleWithDamage = true;

	/** Ab wie viel Schaden (in halben Herzen) ausgeloest wird. */
	public float threshold = 6.0F;

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
				settings.colorStart = 0xFFD54A;
				settings.colorEnd = 0x37D67A;
				settings.endRadius = 3.4F;
				settings.durationTicks = 26;
				settings.ringCount = 3;
				settings.soundId = "minecraft:block.beacon.activate";
				settings.soundPitch = 1.4F;
				settings.scaleWithDamage = false;
			}
			case BIG_DAMAGE -> {
				settings.style = EffectStyle.RING;
				settings.colorStart = 0xFF3B3B;
				settings.colorEnd = 0xFF9F1C;
				settings.endRadius = 2.6F;
				settings.durationTicks = 16;
				settings.threshold = 6.0F;
				settings.sound = false;
				settings.soundId = "minecraft:block.note_block.pling";
			}
			case KILL -> {
				settings.style = EffectStyle.DOME;
				settings.colorStart = 0xB05CFF;
				settings.colorEnd = 0x2E1A66;
				settings.endRadius = 2.2F;
				settings.height = 2.6F;
				settings.durationTicks = 30;
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
				settings.colorStart = 0xFF2D55;
				settings.colorEnd = 0x7A0025;
				settings.endRadius = 1.4F;
				settings.height = 2.0F;
				settings.durationTicks = 14;
				settings.threshold = 6.0F;
				settings.sound = false;
			}
		}

		return settings;
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

		colorStart &= 0xFFFFFF;
		colorEnd &= 0xFFFFFF;
		alpha = clamp(alpha, 10, 255);
		durationTicks = clamp(durationTicks, 3, 200);
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
		copy.startRadius = startRadius;
		copy.endRadius = endRadius;
		copy.height = height;
		copy.yOffset = yOffset;
		copy.thickness = thickness;
		copy.segments = segments;
		copy.ringCount = ringCount;
		copy.rotationSpeed = rotationSpeed;
		copy.followEntity = followEntity;
		copy.scaleWithDamage = scaleWithDamage;
		copy.threshold = threshold;
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
