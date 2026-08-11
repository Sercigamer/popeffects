package com.popeffects.compat;

import java.util.function.Function;

import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;

/**
 * Kleine Unterschiede in der Widget-API zwischen den Minecraft-Versionen.
 *
 * <p>Fassung fuer 1.21.11: der Startwert wird direkt an
 * {@code builder} uebergeben. Vorher gab es dafuer {@code initially}.
 */
public final class WidgetCompat {
	private WidgetCompat() {
	}

	public static <T> CycleButton.Builder<T> cycler(Function<T, Component> valueToText, T initial) {
		return CycleButton.builder(valueToText, initial);
	}
}
