package com.popeffects.effect;

import java.util.List;

import org.joml.Matrix4f;

import com.popeffects.config.ConfigManager;
import com.popeffects.config.EffectSettings;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Zeichnet die laufenden Effekte in die Welt.
 *
 * <p>Gezeichnet wird nach den Entities, damit die Effekte ueber den Gegnern
 * liegen. Alle Koordinaten sind relativ zur Kamera - so macht es Minecraft
 * selbst auch, sonst wird es weit weg vom Weltursprung ungenau.
 */
public final class EffectRenderer {
	/** Erster Teil der Lebensdauer, in dem der Effekt eingeblendet wird. */
	private static final float FADE_IN = 0.15F;

	private EffectRenderer() {
	}

	public static void register() {
		WorldRenderEvents.AFTER_ENTITIES.register(EffectRenderer::render);
	}

	private static void render(WorldRenderContext context) {
		List<ActiveEffect> effects = EffectManager.active();

		if (effects.isEmpty()) {
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		ClientWorld world = client.world;
		MatrixStack matrices = context.matrices();
		VertexConsumerProvider consumers = context.consumers();

		if (world == null || matrices == null || consumers == null) {
			return;
		}

		Vec3d camera = context.worldState().cameraRenderState.pos;

		if (camera == null) {
			return;
		}

		float tickDelta = client.getRenderTickCounter().getTickProgress(false);
		double maxDistance = ConfigManager.get().maxDistance;
		double maxDistanceSquared = maxDistance * maxDistance;

		for (ActiveEffect effect : effects) {
			Entity tracked = world.getEntityById(effect.entityId());
			Vec3d position = effect.renderPosition(tracked, tickDelta);

			if (position.squaredDistanceTo(camera) > maxDistanceSquared) {
				continue;
			}

			matrices.push();
			matrices.translate(position.x - camera.x, position.y - camera.y, position.z - camera.z);
			draw(effect, matrices.peek().getPositionMatrix(), consumers, tickDelta);
			matrices.pop();
		}
	}

	private static void draw(ActiveEffect effect, Matrix4f matrix, VertexConsumerProvider consumers, float tickDelta) {
		EffectSettings settings = effect.settings;
		VertexConsumer consumer = consumers.getBuffer(PopRenderLayers.forEffect(settings.additive,
				settings.throughWalls));

		float progress = effect.progress(tickDelta);
		float seconds = (effect.age() + tickDelta) / 20.0F;
		float rotation = (float) Math.toRadians(effect.rotationOffset + settings.rotationSpeed * seconds);

		int baseColor = baseColor(effect, progress, seconds);
		int color = ColorMath.scaleAlpha(baseColor, fade(progress));
		int transparent = ColorMath.scaleAlpha(baseColor, 0.0F);

		float radius = radiusAt(settings, progress) * effect.scale;
		float height = settings.height * ease(progress) * effect.scale;
		float half = settings.thickness * 0.5F;
		float y = settings.yOffset;

		switch (settings.style) {
			case RING -> softRing(consumer, matrix, y, radius, half, settings.segments, rotation, color, transparent);

			case SHOCKWAVE -> {
				for (int ring = 0; ring < settings.ringCount; ring++) {
					// Jeder Ring startet ein Stueck spaeter, dadurch laufen sie
					// hintereinander nach aussen.
					float delay = (ring * 0.6F) / settings.ringCount;
					float ringProgress = (progress - delay) / (1.0F - delay);

					if (ringProgress <= 0.0F) {
						continue;
					}

					float ringRadius = radiusAt(settings, ringProgress) * effect.scale;
					int ringColor = ColorMath.scaleAlpha(baseColor, fade(ringProgress));

					softRing(consumer, matrix, y, ringRadius, half, settings.segments, rotation, ringColor,
							transparent);
				}
			}

			case DISC -> ShapeRenderer.annulus(consumer, matrix, y, 0.0F, radius, settings.segments, rotation, color,
					transparent);

			case DOME -> {
				softRing(consumer, matrix, y, radius, half, settings.segments, rotation, color, transparent);

				for (int ring = 0; ring < settings.ringCount; ring++) {
					float t = (ring + 0.5F) / settings.ringCount;
					float angle = t * MathHelper.HALF_PI;
					float ringRadius = radius * MathHelper.cos(angle);
					float ringY = y + height * MathHelper.sin(angle);

					ShapeRenderer.band(consumer, matrix, ringY - half, ringY + half, ringRadius, settings.segments,
							rotation, color, color);
				}
			}

			case SPHERE -> {
				for (int ring = 0; ring < settings.ringCount; ring++) {
					float t = (ring + 0.5F) / settings.ringCount;
					float angle = (t - 0.5F) * MathHelper.PI;
					float ringRadius = radius * MathHelper.cos(angle);
					float ringY = y + height * 0.5F + height * 0.5F * MathHelper.sin(angle);

					ShapeRenderer.band(consumer, matrix, ringY - half, ringY + half, ringRadius, settings.segments,
							rotation, color, color);
				}
			}

			case PILLAR -> {
				ShapeRenderer.band(consumer, matrix, y, y + height, radius, settings.segments, rotation, color,
						transparent);
				softRing(consumer, matrix, y + 0.02F, radius, half, settings.segments, rotation, color, transparent);
			}

			case HELIX -> ShapeRenderer.helix(consumer, matrix, y, radius, height, settings.ringCount,
					settings.thickness, settings.segments, rotation, color, ColorMath.scaleAlpha(baseColor, 0.15F));

			case BURST -> {
				int rayCount = MathHelper.clamp(settings.segments / 4, 4, 24);
				ShapeRenderer.rays(consumer, matrix, y, radius * 0.15F, radius, rayCount, settings.thickness * 2.0F,
						rotation, color, transparent);
			}
		}
	}

	/**
	 * Ring mit weichen Kanten: innen und aussen laeuft die Farbe auf Null aus,
	 * in der Mitte ist sie voll da. Sieht deutlich besser aus als eine harte
	 * Kante und kostet nur ein zweites Viereck pro Segment.
	 */
	private static void softRing(VertexConsumer consumer, Matrix4f matrix, float y, float radius, float half,
			int segments, float rotation, int color, int transparent) {
		ShapeRenderer.annulus(consumer, matrix, y, radius - half, radius, segments, rotation, transparent, color);
		ShapeRenderer.annulus(consumer, matrix, y, radius, radius + half, segments, rotation, color, transparent);
	}

	private static float radiusAt(EffectSettings settings, float progress) {
		return MathHelper.lerp(ease(progress), settings.startRadius, settings.endRadius);
	}

	/**
	 * Schnell raus, dann auslaufen - ohne das wirkt jede Explosion traege.
	 */
	private static float ease(float progress) {
		float inverse = 1.0F - MathHelper.clamp(progress, 0.0F, 1.0F);
		return 1.0F - inverse * inverse * inverse;
	}

	private static float fade(float progress) {
		progress = MathHelper.clamp(progress, 0.0F, 1.0F);

		if (progress < FADE_IN) {
			return progress / FADE_IN;
		}

		float remaining = (1.0F - progress) / (1.0F - FADE_IN);
		return remaining * remaining;
	}

	private static int baseColor(ActiveEffect effect, float progress, float seconds) {
		EffectSettings settings = effect.settings;

		if (settings.rainbow) {
			// Der Versatz sorgt dafuer, dass zwei gleichzeitige Effekte nicht
			// exakt dieselbe Farbe haben.
			float hue = seconds * 0.6F + effect.rotationOffset / 360.0F;
			return ColorMath.argb(settings.alpha, ColorMath.rainbow(hue));
		}

		if (settings.gradient) {
			int start = ColorMath.argb(settings.alpha, settings.colorStart);
			int end = ColorMath.argb(settings.alpha, settings.colorEnd);
			return ColorMath.lerp(progress, start, end);
		}

		return ColorMath.argb(settings.alpha, settings.colorStart);
	}
}
