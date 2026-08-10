package com.popeffects.config;

/**
 * Form des Effekts. Die Geometrie dazu steckt in
 * {@link com.popeffects.effect.ShapeRenderer}.
 */
public enum EffectStyle {
	/** Ein flacher Ring am Boden, der nach aussen laeuft. */
	RING("popeffects.style.ring"),

	/** Mehrere Ringe hintereinander - der Klassiker. */
	SHOCKWAVE("popeffects.style.shockwave"),

	/** Gefuellte Scheibe, die von innen nach aussen ausblendet. */
	DISC("popeffects.style.disc"),

	/** Halbkugel aus Ringen ueber dem Kopf. */
	DOME("popeffects.style.dome"),

	/** Vollstaendige Kugel aus waagerechten Ringen. */
	SPHERE("popeffects.style.sphere"),

	/** Senkrechter Zylinder, der um das Ziel steht. */
	PILLAR("popeffects.style.pillar"),

	/** Spirale, die um das Ziel nach oben laeuft. */
	HELIX("popeffects.style.helix"),

	/** Strahlen, die sternfoermig nach aussen schiessen. */
	BURST("popeffects.style.burst"),

	/** Ein Kranz aus stehenden Zacken, der nach aussen laeuft. */
	CROWN("popeffects.style.crown");

	private final String translationKey;

	EffectStyle(String translationKey) {
		this.translationKey = translationKey;
	}

	public String translationKey() {
		return translationKey;
	}

	public EffectStyle next() {
		EffectStyle[] all = values();
		return all[(ordinal() + 1) % all.length];
	}

	/** Nur diese Stile brauchen eine Hoehe - der Editor blendet sie sonst aus. */
	public boolean usesHeight() {
		return this == DOME || this == SPHERE || this == PILLAR || this == HELIX || this == CROWN;
	}
}
