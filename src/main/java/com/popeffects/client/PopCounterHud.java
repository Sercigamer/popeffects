package com.popeffects.client;

import java.util.List;

import com.popeffects.config.ConfigManager;
import com.popeffects.effect.PopCounter;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
	public void render(DrawContext context, RenderTickCounter tickCounter) {
		if (!ConfigManager.get().enabled || !ConfigManager.get().popCounter) {
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();

		if (client.player == null || client.options.hudHidden) {
			return;
		}

		List<PopCounter.Entry> entries = PopCounter.visible();

		if (entries.isEmpty()) {
			return;
		}

		TextRenderer textRenderer = client.textRenderer;
		Text title = Text.translatable("popeffects.hud.title").formatted(Formatting.BOLD);

		int rows = Math.min(MAX_ROWS, entries.size());
		int width = textRenderer.getWidth(title);

		for (int i = 0; i < rows; i++) {
			width = Math.max(width, textRenderer.getWidth(label(entries.get(i))));
		}

		int right = context.getScaledWindowWidth() - MARGIN;
		int left = right - width - MARGIN * 2;
		int top = 40;
		int bottom = top + (rows + 1) * LINE_HEIGHT + MARGIN;

		context.fill(left, top - MARGIN, right, bottom, COLOR_BACKGROUND);
		context.drawTextWithShadow(textRenderer, title, left + MARGIN, top, COLOR_TITLE);

		for (int i = 0; i < rows; i++) {
			int y = top + (i + 1) * LINE_HEIGHT + 2;
			context.drawTextWithShadow(textRenderer, label(entries.get(i)), left + MARGIN, y, COLOR_NAME);
		}
	}

	private static Text label(PopCounter.Entry entry) {
		return Text.translatable("popeffects.hud.entry", entry.name, entry.pops);
	}
}
