package com.popeffects.compat;

import java.util.function.Function;

import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

/**
 * Fassung fuer 1.21.10: der Startwert kommt ueber {@code initially}, den
 * zweiten Parameter von {@code builder} gibt es erst ab 1.21.11.
 */
public final class WidgetCompat {
	private WidgetCompat() {
	}

	public static <T> CyclingButtonWidget.Builder<T> cycler(Function<T, Text> valueToText, T initial) {
		return CyclingButtonWidget.builder(valueToText).initially(initial);
	}
}
