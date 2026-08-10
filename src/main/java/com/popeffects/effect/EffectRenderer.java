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
	/** Wie viel breiter der Schein hinter der Form ist. */
	private static final float GLOW_WIDTH = 2.2F;

	/** Wie blass der Schein gegenueber der Form selbst ist. */
	private static final float GLOW_ALPHA = 0.22F;

	/** Um so viel duenner wird das Band bis zum Ende des Ausblendens. */
	private static final float DISSOLVE_THINNING = 0.55F;

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

		float ageTicks = effect.age() + tickDelta;
		float progress = effect.progress(tickDelta);
		float seconds = ageTicks / 20.0F;
		float rotation = (float) Math.toRadians(effect.rotationOffset + settings.rotationSpeed * seconds);

		int baseColor = baseColor(effect, progress, seconds);

		// Erst der breite, blasse Schein, dann die Form selbst darueber - so
		// bekommt der Effekt einen weichen Rand statt einer harten Kante.
		if (settings.glow) {
			emit(consumer, matrix, effect, ageTicks, progress, baseColor, rotation, GLOW_WIDTH, GLOW_ALPHA);
		}

		emit(consumer, matrix, effect, ageTicks, progress, baseColor, rotation, 1.0F, 1.0F);
	}

	/**
	 * Zeichnet die Form einmal komplett.
	 *
	 * @param thicknessScale Breite gegenueber der Einstellung - der Schein
	 *                       benutzt hier einen groesseren Wert.
	 * @param alphaScale     Deckkraft gegenueber der Einstellung.
	 */
	private static void emit(VertexConsumer consumer, Matrix4f matrix, ActiveEffect effect, float ageTicks,
			float progress, int baseColor, float rotation, float thicknessScale, float alphaScale) {
		EffectSettings settings = effect.settings;

		float dissolve = dissolve(settings, ageTicks);
		float radius = radiusAt(settings, progress, dissolve) * effect.scale;
		float height = settings.height * ease(progress) * effect.scale;
		float y = settings.yOffset;

		// Beim Ausblenden wird das Band duenner. Zusammen mit dem
		// Weiterlaufen nach aussen loest sich der Effekt dadurch auf, statt
		// stehenzubleiben und dann einfach weg zu sein.
		float thickness = settings.thickness * thicknessScale * (1.0F - DISSOLVE_THINNING * dissolve);
		float half = thickness * 0.5F;

		int color = ColorMath.scaleAlpha(baseColor, fade(settings, ageTicks) * alphaScale);
		int transparent = ColorMath.scaleAlpha(baseColor, 0.0F);

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

					// Jeder Ring rechnet auf seiner eigenen Uhr - so blendet
					// auch jeder fuer sich aus und dehnt sich dabei nach.
					float ringAge = ringProgress * settings.durationTicks;
					float ringDissolve = dissolve(settings, ringAge);
					float ringRadius = radiusAt(settings, ringProgress, ringDissolve) * effect.scale;
					float ringHalf = settings.thickness * thicknessScale
							* (1.0F - DISSOLVE_THINNING * ringDissolve) * 0.5F;

					int ringColor = ColorMath.scaleAlpha(baseColor, fade(settings, ringAge) * alphaScale);

					softRing(consumer, matrix, y, ringRadius, ringHalf, settings.segments, rotation, ringColor,
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

			case HELIX -> ShapeRenderer.helix(consumer, matrix, y, radius, height, settings.ringCount, thickness,
					settings.segments, rotation, color, ColorMath.scaleAlpha(baseColor, 0.15F * alphaScale));

			case BURST -> {
				int rayCount = MathHelper.clamp(settings.segments / 4, 4, 24);
				ShapeRenderer.rays(consumer, matrix, y, radius * 0.15F, radius, rayCount, thickness * 2.0F, rotation,
						color, transparent);
			}

			case CROWN -> {
				int spikes = MathHelper.clamp(settings.segments / 6, 5, 16);
				ShapeRenderer.crown(consumer, matrix, y, radius, height, spikes, thickness * 3.0F, rotation, color,
						transparent);
				softRing(consumer, matrix, y, radius, half, settings.segments, rotation, color, transparent);
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

	/**
	 * Radius zum Zeitpunkt {@code progress}, inklusive der Nachdehnung
	 * waehrend des Ausblendens.
	 */
	private static float radiusAt(EffectSettings settings, float progress, float dissolve) {
		float base = MathHelper.lerp(ease(progress), settings.startRadius, settings.endRadius);
		return base + settings.fadeOutExpansion * dissolve;
	}

	/**
	 * Kurve fuer das Wachsen: erst schnell nach aussen, dann ruhiger.
	 *
	 * <p>Bewusst eine Potenz kleiner als 1: die laeuft am Ende noch weiter,
	 * statt auf den letzten Prozent stehenzubleiben. Ein Effekt, der erst
	 * einfriert und dann verschwindet, sieht nach Abbruch aus - einer, der
	 * beim Verblassen noch nach aussen zieht, loest sich auf.
	 */
	private static float ease(float progress) {
		return (float) Math.pow(MathHelper.clamp(progress, 0.0F, 1.0F), 0.6D);
	}

	/**
	 * Wie weit der Effekt in der Ausblendphase steckt: 0 davor, 1 ganz am
	 * Ende. Steuert Nachdehnung und Ausduennen.
	 */
	private static float dissolve(EffectSettings settings, float ageTicks) {
		if (settings.fadeOutTicks <= 0) {
			return 0.0F;
		}

		float remaining = settings.durationTicks - ageTicks;

		if (remaining >= settings.fadeOutTicks) {
			return 0.0F;
		}

		return smooth(MathHelper.clamp(1.0F - remaining / settings.fadeOutTicks, 0.0F, 1.0F));
	}

	/**
	 * Deckkraft-Faktor von 0 bis 1, gebaut aus den eingestellten Ein- und
	 * Ausblendzeiten.
	 *
	 * <p>Gerechnet wird in Ticks statt in Prozent: sonst wuerde ein laenger
	 * eingestellter Effekt auch laenger ausblenden, obwohl man am Ausblenden
	 * gar nichts geaendert hat.
	 *
	 * <p>Der Verlauf ist bewusst linear: die Farbe soll ueber die eingestellte
	 * Zeit gleichmaessig durchsichtig werden. Eine weiche Kurve haelt den
	 * Effekt erst lange fast voll sichtbar und laesst ihn dann schnell
	 * wegkippen - das sieht aus, als wuerde er abgeschnitten.
	 */
	private static float fade(EffectSettings settings, float ageTicks) {
		float factor = 1.0F;

		if (settings.fadeInTicks > 0 && ageTicks < settings.fadeInTicks) {
			factor = ageTicks / settings.fadeInTicks;
		}

		float remaining = settings.durationTicks - ageTicks;

		if (settings.fadeOutTicks > 0 && remaining < settings.fadeOutTicks) {
			factor = Math.min(factor, remaining / settings.fadeOutTicks);
		}

		return MathHelper.clamp(factor, 0.0F, 1.0F);
	}

	/** Weiches Ein- und Auslaufen - linear wirkt an den Enden abgehackt. */
	private static float smooth(float value) {
		return value * value * (3.0F - 2.0F * value);
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
			int start = ColorMath.argb(settings.alpha, settings.colorStartRgb());
			int end = ColorMath.argb(settings.alpha, settings.colorEndRgb());
			return ColorMath.lerp(progress, start, end);
		}

		return ColorMath.argb(settings.alpha, settings.colorStartRgb());
	}
}
