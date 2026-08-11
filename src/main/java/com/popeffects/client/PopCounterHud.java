package com.popeffects.client;

import java.util.List;

import com.popeffects.config.ConfigManager;
import com.popeffects.effect.PopCounter;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Kleine Liste am rechten Rand: wer hat gerade wie oft gepoppt. Genau die
 * Information, die im Getuemmel sonst untergeht.
 */
public final class PopCounterHud implements HudElement {
	private static final int MAX_ROWS = 6;
	private static final int LINE_HEIGHT = 10;
	private static final int MARGIN = 4;

	private static final int COLOR_BACKGROUND = 0x80000000;
	private static final int COLOR_TITLE = 0xFFFFD54A;
	private static final int COLOR_NAME = 0xFFFFFFFF;

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
		if (!ConfigManager.get().enabled || !ConfigManager.get().popCounter) {
			return;
		}

		Minecraft client = Minecraft.getInstance();

		if (client.player == null) {
			return;
		}

		List<PopCounter.Entry> entries = PopCounter.visible();

		if (entries.isEmpty()) {
			return;
		}

		Font font = client.font;
		Component title = Component.translatable("popeffects.hud.title").withStyle(ChatFormatting.BOLD);

		int rows = Math.min(MAX_ROWS, entries.size());
		int width = font.width(title);

		for (int i = 0; i < rows; i++) {
			width = Math.max(width, font.width(label(entries.get(i))));
		}

		int right = context.guiWidth() - MARGIN;
		int left = right - width - MARGIN * 2;
		int top = 40;
		int bottom = top + (rows + 1) * LINE_HEIGHT + MARGIN;

		context.fill(left, top - MARGIN, right, bottom, COLOR_BACKGROUND);
		context.text(font, title, left + MARGIN, top, COLOR_TITLE);

		for (int i = 0; i < rows; i++) {
			int y = top + (i + 1) * LINE_HEIGHT + 2;
			context.text(font, label(entries.get(i)), left + MARGIN, y, COLOR_NAME);
		}
	}

	private static Component label(PopCounter.Entry entry) {
		return Component.translatable("popeffects.hud.entry", entry.name, entry.pops);
	}
}
