package com.popeffects.compat;

import net.minecraft.client.render.RenderLayer;

/**
 * Render-Layer fuer Minecraft 1.21.10.
 *
 * <p>Hier gibt es noch keine eigenen Layer: die Bausteine, aus denen man in
 * 1.21.11 eine eigene Pipeline zusammensetzt, sind in 1.21.10 alle
 * paketprivat. Man koennte sie sich ueber mehrere Zugriffs-Mixins greifen -
 * das waere aber eine Kette von Hacks, die bei jedem Patch bricht.
 *
 * <p>Stattdessen nehmen wir den Vanilla-Layer {@code debugQuads}: Vierecke,
 * kein Culling, normale Deckkraft, kein Schreiben in den Tiefenpuffer - genau
 * das, was die Mod in der Voreinstellung braucht. Was dadurch fehlt, meldet
 * {@link #supportsAdditive()} und {@link #supportsThroughWalls()}, damit das
 * Menue die beiden Schalter ausgraut statt sie wirkungslos anzubieten.
 */
public final class EffectLayers {
	private EffectLayers() {
	}

	public static void init() {
	}

	public static RenderLayer forEffect(boolean additive, boolean throughWalls) {
		return RenderLayer.getDebugQuads();
	}

	public static boolean supportsAdditive() {
		return false;
	}

	public static boolean supportsThroughWalls() {
		return false;
	}
}
