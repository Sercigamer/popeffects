package com.popeffects.client.screen;

import java.util.function.Consumer;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

/**
 * Schieberegler fuer Kommazahlen. Der Wert rastet auf {@code step} ein, damit
 * im Menue nicht "2,7391" steht.
 */
public class FloatSliderWidget extends SliderWidget {
	private String translationKey;
	private float min;
	private float max;
	private float step;
	private Consumer<Float> setter;

	public FloatSliderWidget(int x, int y, int width, int height, String translationKey, float min, float max,
			float step, float current, Consumer<Float> setter) {
		super(x, y, width, height, Text.empty(), toSliderValue(current, min, max));

		this.translationKey = translationKey;
		this.min = min;
		this.max = max;
		this.step = step;
		this.setter = setter;

		updateMessage();
	}

	private static double toSliderValue(float current, float min, float max) {
		if (max <= min) {
			return 0.0D;
		}

		double normalized = (current - min) / (double) (max - min);
		return Math.min(1.0D, Math.max(0.0D, normalized));
	}

	public float getFloatValue() {
		float raw = min + (float) (this.value * (max - min));
		return Math.round(raw / step) * step;
	}

	@Override
	protected void updateMessage() {
		if (translationKey == null) {
			return;
		}

		setMessage(Text.translatable(translationKey, String.format("%.2f", getFloatValue())));
	}

	@Override
	protected void applyValue() {
		if (setter != null) {
			setter.accept(getFloatValue());
		}
	}
}
