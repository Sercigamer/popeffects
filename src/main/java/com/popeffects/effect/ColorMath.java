package com.popeffects.effect;

import net.minecraft.util.math.MathHelper;

/**
 * Kleine Farb-Helfer. Farben liegen ueberall als 0xAARRGGBB vor - genau so,
 * wie {@code VertexConsumer.color(int)} sie erwartet.
 */
public final class ColorMath {
	private ColorMath() {
	}

	public static int argb(int alpha, int rgb) {
		return ((alpha & 0xFF) << 24) | (rgb & 0xFFFFFF);
	}

	public static int alpha(int argb) {
		return (argb >>> 24) & 0xFF;
	}

	public static int rgb(int argb) {
		return argb & 0xFFFFFF;
	}

	/** Neue Deckkraft, Farbton bleibt. */
	public static int withAlpha(int argb, int alpha) {
		return argb(alpha, argb);
	}

	/** Deckkraft mit einem Faktor multiplizieren, z.B. zum Ausblenden. */
	public static int scaleAlpha(int argb, float factor) {
		int alpha = Math.round(alpha(argb) * MathHelper.clamp(factor, 0.0F, 1.0F));
		return argb(alpha, argb);
	}

	/** Kanalweise mischen, Deckkraft eingeschlossen. */
	public static int lerp(float delta, int from, int to) {
		delta = MathHelper.clamp(delta, 0.0F, 1.0F);

		int a = MathHelper.lerp(delta, (from >>> 24) & 0xFF, (to >>> 24) & 0xFF);
		int r = MathHelper.lerp(delta, (from >> 16) & 0xFF, (to >> 16) & 0xFF);
		int g = MathHelper.lerp(delta, (from >> 8) & 0xFF, (to >> 8) & 0xFF);
		int b = MathHelper.lerp(delta, from & 0xFF, to & 0xFF);

		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	/**
	 * Regenbogen: {@code hue} laeuft von 0 bis 1 einmal durch den Farbkreis.
	 */
	public static int rainbow(float hue) {
		return MathHelper.hsvToRgb(hue - (float) Math.floor(hue), 1.0F, 1.0F) & 0xFFFFFF;
	}
}
