package com.popeffects.compat;

import java.util.function.Function;

import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

/**
 * Kleine Unterschiede in der Widget-API zwischen den Minecraft-Versionen.
 *
 * <p>Fassung fuer 1.21.11: der Startwert wird direkt an
 * {@code builder} uebergeben. Vorher gab es dafuer {@code initially}.
 */
public final class WidgetCompat {
	private WidgetCompat() {
	}

	public static <T> CyclingButtonWidget.Builder<T> cycler(Function<T, Text> valueToText, T initial) {
		return CyclingButtonWidget.builder(valueToText, initial);
	}
}
