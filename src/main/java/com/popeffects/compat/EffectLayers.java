package com.popeffects.compat;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * Render-Typ fuer die Effekte.
 *
 * <p>Fassung fuer Minecraft 26.x. Anders als in 1.21.11 bauen wir hier keine
 * eigenen Pipelines: die Bausteine dafuer sind nicht oeffentlich erreichbar.
 * Wir nehmen den Vanilla-Typ {@code debugQuads} - Vierecke, kein Culling,
 * normale Deckkraft, kein Schreiben in den Tiefenpuffer. Genau das, was die
 * Mod in der Voreinstellung braucht.
 *
 * <p>Was dadurch fehlt, meldet {@link #supportsAdditive()} und
 * {@link #supportsThroughWalls()}; das Menue graut die beiden Schalter dann
 * aus, statt sie wirkungslos anzubieten.
 */
public final class EffectLayers {
	private EffectLayers() {
	}

	public static void init() {
	}

	public static RenderType forEffect(boolean additive, boolean throughWalls) {
		return RenderTypes.debugQuads();
	}

	public static boolean supportsAdditive() {
		return false;
	}

	public static boolean supportsThroughWalls() {
		return false;
	}
}
