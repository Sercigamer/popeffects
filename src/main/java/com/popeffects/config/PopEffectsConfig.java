package com.popeffects.config;

/**
 * Die gesamte Konfiguration. Landet als {@code config/popeffects.json} im
 * Spielordner.
 */
public final class PopEffectsConfig {
	/**
	 * Version 2: Farben stehen als {@code "#RRGGBB"} in der Datei statt als
	 * Dezimalzahl.
	 *
	 * <p>Version 3: Additives Mischen und der Schein sind aus, damit das
	 * Ausblenden die Farbe wirklich durchsichtig macht.
	 */
	public static final int CURRENT_VERSION = 3;

	public int configVersion = CURRENT_VERSION;

	/** Hauptschalter - aus heisst: gar keine Effekte. */
	public boolean enabled = true;

	/** Effekte an dir selbst zeigen. */
	public boolean showOnSelf = true;

	/** Effekte an anderen Spielern zeigen. */
	public boolean showOnPlayers = true;

	/** Effekte auch an Mobs zeigen, nicht nur an Spielern. */
	public boolean showOnMobs = true;

	/**
	 * Schadens-Effekte nur, wenn du selbst zugeschlagen hast. Der Server muss
	 * dafuer das Schadens-Paket schicken - tun praktisch alle ab 1.19.4.
	 */
	public boolean onlyOwnHits = false;

	/** Weiter entfernte Ziele bekommen keinen Effekt. */
	public double maxDistance = 48.0D;

	/** Notbremse gegen Effekt-Spam bei grossen Kaempfen. */
	public int maxActiveEffects = 32;

	/** Kleine Liste im HUD, wer wie oft gepoppt hat. */
	public boolean popCounter = true;

	/** So lange bleibt ein Eintrag in der Liste stehen (Sekunden). */
	public int popCounterSeconds = 12;

	public EffectSettings totemPop = EffectSettings.defaultFor(TriggerType.TOTEM_POP);
	public EffectSettings bigDamage = EffectSettings.defaultFor(TriggerType.BIG_DAMAGE);
	public EffectSettings kill = EffectSettings.defaultFor(TriggerType.KILL);
	public EffectSettings selfHurt = EffectSettings.defaultFor(TriggerType.SELF_HURT);

	public EffectSettings get(TriggerType type) {
		return switch (type) {
			case TOTEM_POP -> totemPop;
			case BIG_DAMAGE -> bigDamage;
			case KILL -> kill;
			case SELF_HURT -> selfHurt;
		};
	}

	public void set(TriggerType type, EffectSettings settings) {
		switch (type) {
			case TOTEM_POP -> totemPop = settings;
			case BIG_DAMAGE -> bigDamage = settings;
			case KILL -> kill = settings;
			case SELF_HURT -> selfHurt = settings;
		}
	}

	public void sanitize() {
		maxDistance = Math.min(256.0D, Math.max(8.0D, maxDistance));
		maxActiveEffects = Math.min(256, Math.max(1, maxActiveEffects));
		popCounterSeconds = Math.min(120, Math.max(2, popCounterSeconds));

		for (TriggerType type : TriggerType.values()) {
			EffectSettings settings = get(type);

			// Fehlt der Block in der JSON-Datei, kommt hier null an.
			if (settings == null) {
				settings = EffectSettings.defaultFor(type);
				set(type, settings);
			}

			settings.sanitize();
		}
	}
}
