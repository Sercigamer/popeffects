package com.popeffects.compat;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

/**
 * Haengt den Effekt-Renderer an das Welt-Rendern.
 *
 * <p>Fassung fuer Minecraft 1.21.11: die Ereignisse liegen im Unterpaket
 * {@code rendering.v1.world}, und die Kameraposition kommt aus dem
 * Render-Zustand.
 */
public final class WorldRenderHook {
	private WorldRenderHook() {
	}

	/** Ruft {@code drawer} nach den Entities auf, einmal pro Bild. */
	public static void register(Consumer<RenderFrame> drawer) {
		WorldRenderEvents.AFTER_ENTITIES.register(context -> {
			RenderFrame frame = toFrame(context);

			if (frame != null) {
				drawer.accept(frame);
			}
		});
	}

	private static RenderFrame toFrame(WorldRenderContext context) {
		MatrixStack matrices = context.matrices();
		VertexConsumerProvider consumers = context.consumers();

		if (matrices == null || consumers == null) {
			return null;
		}

		Vec3d camera = context.worldState().cameraRenderState.pos;

		return camera == null ? null : new RenderFrame(matrices, consumers, camera);
	}
}
