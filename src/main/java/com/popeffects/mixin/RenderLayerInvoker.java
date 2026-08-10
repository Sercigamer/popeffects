package com.popeffects.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;

/**
 * Vanilla baut seine RenderLayer ueber eine paketprivate Methode. Die brauchen
 * wir auch, sonst liessen sich keine eigenen Layer bauen - und ohne eigene
 * Layer gaebe es weder "durch Waende" noch additives Leuchten.
 */
@Mixin(RenderLayer.class)
public interface RenderLayerInvoker {
	@Invoker("of")
	static RenderLayer popeffects$of(String name, RenderSetup setup) {
		throw new AssertionError("Wird von Mixin ersetzt");
	}
}
