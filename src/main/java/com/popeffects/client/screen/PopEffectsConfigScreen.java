package com.popeffects.client.screen;

import com.popeffects.config.ConfigManager;
import com.popeffects.config.PopEffectsConfig;
import com.popeffects.config.TriggerType;
import com.popeffects.effect.EffectManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Hauptmenue der Mod. Erreichbar ueber Mod Menu, {@code /popeffects config}
 * oder die Taste P.
 */
public class PopEffectsConfigScreen extends Screen {
	private static final int ROW_HEIGHT = 24;
	private static final int COLUMN_WIDTH = 150;

	private final Screen parent;

	public PopEffectsConfigScreen(Screen parent) {
		super(Text.translatable("popeffects.config.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		PopEffectsConfig config = ConfigManager.get();

		int centerX = this.width / 2;
		int leftX = centerX - COLUMN_WIDTH - 5;
		int rightX = centerX + 5;
		int top = 36;

		// Linke Spalte: was ueberhaupt gezeigt wird
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.enabled)
				.build(leftX, top, COLUMN_WIDTH, 20, Text.translatable("popeffects.config.enabled"),
						(button, value) -> {
							config.enabled = value;

							if (!value) {
								EffectManager.clear();
							}

							ConfigManager.save();
						}));

		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.showOnSelf)
				.build(leftX, top + ROW_HEIGHT, COLUMN_WIDTH, 20, Text.translatable("popeffects.config.show_self"),
						(button, value) -> {
							config.showOnSelf = value;
							ConfigManager.save();
						}));

		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.showOnPlayers)
				.build(leftX, top + ROW_HEIGHT * 2, COLUMN_WIDTH, 20,
						Text.translatable("popeffects.config.show_players"), (button, value) -> {
							config.showOnPlayers = value;
							ConfigManager.save();
						}));

		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.showOnMobs)
				.build(leftX, top + ROW_HEIGHT * 3, COLUMN_WIDTH, 20, Text.translatable("popeffects.config.show_mobs"),
						(button, value) -> {
							config.showOnMobs = value;
							ConfigManager.save();
						}));

		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.onlyOwnHits)
				.build(leftX, top + ROW_HEIGHT * 4, COLUMN_WIDTH, 20,
						Text.translatable("popeffects.config.only_own_hits"), (button, value) -> {
							config.onlyOwnHits = value;
							ConfigManager.save();
						}));

		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.popCounter)
				.build(leftX, top + ROW_HEIGHT * 5, COLUMN_WIDTH, 20,
						Text.translatable("popeffects.config.pop_counter"), (button, value) -> {
							config.popCounter = value;
							ConfigManager.save();
						}));

		// Rechte Spalte: die vier Effekte und die Leistungsbremsen
		int row = 0;

		for (TriggerType type : TriggerType.values()) {
			int y = top + ROW_HEIGHT * row;
			boolean active = config.get(type).enabled;

			addDrawableChild(ButtonWidget
					.builder(Text.translatable("popeffects.config.edit", Text.translatable(type.translationKey()))
							.formatted(active ? Formatting.WHITE : Formatting.DARK_GRAY),
							button -> this.client.setScreen(new EffectEditScreen(this, type, 0)))
					.dimensions(rightX, y, COLUMN_WIDTH, 20).build());

			row++;
		}

		addDrawableChild(new IntSliderWidget(rightX, top + ROW_HEIGHT * 4, COLUMN_WIDTH, 20,
				"popeffects.config.distance", 8, 128, (int) config.maxDistance, value -> {
					config.maxDistance = value;
					ConfigManager.save();
				}));

		addDrawableChild(new IntSliderWidget(rightX, top + ROW_HEIGHT * 5, COLUMN_WIDTH, 20,
				"popeffects.config.max_effects", 1, 128, config.maxActiveEffects, value -> {
					config.maxActiveEffects = value;
					ConfigManager.save();
				}));

		addDrawableChild(ButtonWidget.builder(Text.translatable("popeffects.config.reset_all"), button -> {
			ConfigManager.reset();
			this.clearAndInit();
		}).dimensions(centerX - 155, this.height - 30, 150, 20).build());

		addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
				.dimensions(centerX + 5, this.height - 30, 150, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);

		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, 0xFFFFFFFF);

		Text hint = Text.translatable("popeffects.config.hint").formatted(Formatting.GRAY);
		context.drawCenteredTextWithShadow(this.textRenderer, hint, this.width / 2, this.height - 44, 0xFFAAAAAA);
	}

	@Override
	public boolean shouldPause() {
		// Nicht pausieren, damit man die Vorschau im Einzelspieler auch laufen
		// sieht.
		return false;
	}

	@Override
	public void close() {
		ConfigManager.save();
		this.client.setScreen(this.parent);
	}
}
