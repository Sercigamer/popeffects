package com.popeffects.effect;

import org.joml.Matrix4f;

import net.minecraft.client.render.VertexConsumer;

/**
 * Die Geometrie der Effekte. Alles besteht aus Vierecken um den Nullpunkt
 * herum - verschoben wird spaeter ueber die Matrix.
 *
 * <p>Backface-Culling ist in unseren Render-Layern aus, deshalb ist die
 * Drehrichtung der Ecken egal und jede Flaeche von beiden Seiten sichtbar.
 */
public final class ShapeRenderer {
	private static final float TAU = (float) (Math.PI * 2.0D);

	private ShapeRenderer() {
	}

	/**
	 * Waagerechter Ring am Boden. Mit {@code inner == 0} wird daraus eine
	 * volle Scheibe.
	 */
	public static void annulus(VertexConsumer consumer, Matrix4f matrix, float y, float inner, float outer,
			int segments, float rotation, int innerColor, int outerColor) {
		if (outer <= 0.0F) {
			return;
		}

		inner = Math.max(0.0F, inner);

		for (int i = 0; i < segments; i++) {
			float a0 = rotation + (TAU * i) / segments;
			float a1 = rotation + (TAU * (i + 1)) / segments;

			float cos0 = cos(a0);
			float sin0 = sin(a0);
			float cos1 = cos(a1);
			float sin1 = sin(a1);

			consumer.vertex(matrix, cos0 * inner, y, sin0 * inner).color(innerColor);
			consumer.vertex(matrix, cos0 * outer, y, sin0 * outer).color(outerColor);
			consumer.vertex(matrix, cos1 * outer, y, sin1 * outer).color(outerColor);
			consumer.vertex(matrix, cos1 * inner, y, sin1 * inner).color(innerColor);
		}
	}

	/**
	 * Senkrechtes Ringband - also die Wand eines Zylinders. Das ist der
	 * Baustein fuer Saeule, Kuppel und Kugel, weil so ein Band aus jedem
	 * Blickwinkel sichtbar bleibt (ein flacher Ring verschwindet, wenn man
	 * genau von der Seite draufschaut).
	 */
	public static void band(VertexConsumer consumer, Matrix4f matrix, float bottomY, float topY, float radius,
			int segments, float rotation, int bottomColor, int topColor) {
		if (radius <= 0.0F) {
			return;
		}

		for (int i = 0; i < segments; i++) {
			float a0 = rotation + (TAU * i) / segments;
			float a1 = rotation + (TAU * (i + 1)) / segments;

			float x0 = cos(a0) * radius;
			float z0 = sin(a0) * radius;
			float x1 = cos(a1) * radius;
			float z1 = sin(a1) * radius;

			consumer.vertex(matrix, x0, bottomY, z0).color(bottomColor);
			consumer.vertex(matrix, x0, topY, z0).color(topColor);
			consumer.vertex(matrix, x1, topY, z1).color(topColor);
			consumer.vertex(matrix, x1, bottomY, z1).color(bottomColor);
		}
	}

	/**
	 * Spiralband, das sich um das Ziel nach oben schraubt.
	 */
	public static void helix(VertexConsumer consumer, Matrix4f matrix, float baseY, float radius, float height,
			int turns, float thickness, int segments, float rotation, int startColor, int endColor) {
		int steps = Math.min(512, Math.max(8, segments * Math.max(1, turns)));

		for (int i = 0; i < steps; i++) {
			float t0 = i / (float) steps;
			float t1 = (i + 1) / (float) steps;

			float a0 = rotation + t0 * TAU * turns;
			float a1 = rotation + t1 * TAU * turns;

			float y0 = baseY + t0 * height;
			float y1 = baseY + t1 * height;

			float x0 = cos(a0) * radius;
			float z0 = sin(a0) * radius;
			float x1 = cos(a1) * radius;
			float z1 = sin(a1) * radius;

			int color0 = ColorMath.lerp(t0, startColor, endColor);
			int color1 = ColorMath.lerp(t1, startColor, endColor);

			consumer.vertex(matrix, x0, y0, z0).color(color0);
			consumer.vertex(matrix, x0, y0 + thickness, z0).color(color0);
			consumer.vertex(matrix, x1, y1 + thickness, z1).color(color1);
			consumer.vertex(matrix, x1, y1, z1).color(color1);
		}
	}

	/**
	 * Sternfoermige Strahlen, die vom Ziel nach aussen zeigen.
	 */
	public static void rays(VertexConsumer consumer, Matrix4f matrix, float y, float inner, float outer, int rayCount,
			float width, float rotation, int innerColor, int outerColor) {
		float half = width * 0.5F;

		for (int i = 0; i < rayCount; i++) {
			float angle = rotation + (TAU * i) / rayCount;

			float dirX = cos(angle);
			float dirZ = sin(angle);
			float perpX = -dirZ * half;
			float perpZ = dirX * half;

			float innerX = dirX * inner;
			float innerZ = dirZ * inner;
			float outerX = dirX * outer;
			float outerZ = dirZ * outer;

			consumer.vertex(matrix, innerX + perpX, y, innerZ + perpZ).color(innerColor);
			consumer.vertex(matrix, outerX + perpX, y, outerZ + perpZ).color(outerColor);
			consumer.vertex(matrix, outerX - perpX, y, outerZ - perpZ).color(outerColor);
			consumer.vertex(matrix, innerX - perpX, y, innerZ - perpZ).color(innerColor);
		}
	}

	/**
	 * Kranz aus stehenden Zacken. Jede Zacke ist ein Trapez, unten breit und
	 * oben schmal - das laesst sie wie eine Flamme wirken, kostet aber nur ein
	 * einziges Viereck.
	 */
	public static void crown(VertexConsumer consumer, Matrix4f matrix, float y, float radius, float height,
			int spikes, float width, float rotation, int bottomColor, int topColor) {
		float half = width * 0.5F;

		for (int i = 0; i < spikes; i++) {
			float angle = rotation + (TAU * i) / spikes;

			float dirX = cos(angle);
			float dirZ = sin(angle);
			float baseX = dirX * radius;
			float baseZ = dirZ * radius;

			// Senkrecht zur Blickrichtung nach aussen - so steht die Zacke
			// quer zum Radius und ist von aussen gut zu sehen.
			float perpX = -dirZ * half;
			float perpZ = dirX * half;

			consumer.vertex(matrix, baseX + perpX, y, baseZ + perpZ).color(bottomColor);
			consumer.vertex(matrix, baseX + perpX * 0.15F, y + height, baseZ + perpZ * 0.15F).color(topColor);
			consumer.vertex(matrix, baseX - perpX * 0.15F, y + height, baseZ - perpZ * 0.15F).color(topColor);
			consumer.vertex(matrix, baseX - perpX, y, baseZ - perpZ).color(bottomColor);
		}
	}

	private static float cos(float angle) {
		return (float) Math.cos(angle);
	}

	private static float sin(float angle) {
		return (float) Math.sin(angle);
	}
}
