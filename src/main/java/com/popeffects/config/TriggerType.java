package com.popeffects.config;

/**
 * Die vier Anlaesse, zu denen ein Effekt gezeigt werden kann. Jeder hat seine
 * eigenen Einstellungen, damit ein Totem-Pop anders aussieht als ein Kill.
 */
public enum TriggerType {
	/** Jemand hat ein Totem der Unsterblichkeit verbraucht. */
	TOTEM_POP("popeffects.trigger.totem_pop"),

	/** Ein Lebewesen hat auf einen Schlag viel Schaden bekommen. */
	BIG_DAMAGE("popeffects.trigger.big_damage"),

	/** Ein Lebewesen ist gestorben. */
	KILL("popeffects.trigger.kill"),

	/** Du selbst hast einen harten Treffer kassiert. */
	SELF_HURT("popeffects.trigger.self_hurt");

	private final String translationKey;

	TriggerType(String translationKey) {
		this.translationKey = translationKey;
	}

	public String translationKey() {
		return translationKey;
	}
}
