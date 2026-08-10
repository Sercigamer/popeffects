package com.popeffects.compat;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.popeffects.PopEffects;
import com.popeffects.mixin.RenderLayerInvoker;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;

/**
 * Vier eigene Render-Layer: einmal normal und einmal additiv (leuchtend), das
 * Ganze je einmal mit und ohne Tiefentest.
 *
 * <p>Fassung fuer Minecraft 1.21.11. Dort werden Layer aus einem
 * {@link RenderSetup} gebaut - in 1.21.9 und 1.21.10 sah das noch anders aus,
 * deshalb liegt diese Klasse in einem versionseigenen Quellordner.
 *
 * <p>Alle vier bauen auf dem Vanilla-Shader {@code core/position_color} auf -
 * es kommen also keine eigenen Shader-Dateien dazu, die bei einem
 * Minecraft-Update kaputtgehen koennten.
 */
public final class EffectLayers {
	private static final RenderLayer SOLID = create("popeffects_solid", false, false);
	private static final RenderLayer SOLID_THROUGH_WALLS = create("popeffects_solid_xray", false, true);
	private static final RenderLayer GLOW = create("popeffects_glow", true, false);
	private static final RenderLayer GLOW_THROUGH_WALLS = create("popeffects_glow_xray", true, true);

	private EffectLayers() {
	}

	/**
	 * Sorgt dafuer, dass die Pipelines registriert sind, bevor Minecraft seine
	 * Shader uebersetzt. Aufrufen reicht - die Arbeit macht der statische
	 * Initialisierer dieser Klasse.
	 */
	public static void init() {
	}

	/** Ab 1.21.11 lassen sich eigene Pipelines bauen - beides geht also. */
	public static boolean supportsAdditive() {
		return true;
	}

	public static boolean supportsThroughWalls() {
		return true;
	}

	public static RenderLayer forEffect(boolean additive, boolean throughWalls) {
		if (additive) {
			return throughWalls ? GLOW_THROUGH_WALLS : GLOW;
		}

		return throughWalls ? SOLID_THROUGH_WALLS : SOLID;
	}

	private static RenderLayer create(String name, boolean additive, boolean throughWalls) {
		RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
				.withLocation(PopEffects.id("pipeline/" + name))
				// Ohne Culling sieht man den Ring auch von unten und von innen.
				.withCull(false)
				// Nie in den Tiefenpuffer schreiben, sonst schneiden sich
				// mehrere Effekte gegenseitig Loecher.
				.withDepthWrite(false)
				.withBlend(additive ? BlendFunction.ADDITIVE : BlendFunction.TRANSLUCENT);

		if (throughWalls) {
			builder.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST);
		}

		RenderPipeline pipeline = RenderPipelines.register(builder.build());

		return RenderLayerInvoker.popeffects$of(name, RenderSetup.builder(pipeline).translucent().build());
	}
}
