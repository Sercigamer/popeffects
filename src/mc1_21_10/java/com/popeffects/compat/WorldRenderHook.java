package com.popeffects.compat;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

/**
 * Fassung fuer 1.21.10 - die Ereignisse liegen wie in 1.21.11 im Unterpaket
 * {@code rendering.v1.world}.
 */
public final class WorldRenderHook {
	private WorldRenderHook() {
	}

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
