package com.popeffects.compat;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.world.phys.Vec3;

/**
 * Haengt den Effekt-Renderer an das Welt-Rendern.
 *
 * <p>In 26.x heisst die Welt ueberall "Level", die Ereignisse also
 * {@code LevelRenderEvents}. Wir haengen uns an {@code COLLECT_SUBMITS} - das
 * ist der Durchgang, in dem Zeichenbefehle eingesammelt werden.
 */
public final class WorldRenderHook {
	private WorldRenderHook() {
	}

	/** Ruft {@code drawer} einmal pro Bild auf. */
	public static void register(Consumer<RenderFrame> drawer) {
		LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
			RenderFrame frame = toFrame(context);

			if (frame != null) {
				drawer.accept(frame);
			}
		});
	}

	private static RenderFrame toFrame(LevelRenderContext context) {
		if (context.poseStack() == null || context.submitNodeCollector() == null) {
			return null;
		}

		Vec3 camera = context.levelState().cameraRenderState.pos;

		return camera == null ? null : new RenderFrame(context.poseStack(), context.submitNodeCollector(), camera);
	}
}
