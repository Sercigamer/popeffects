package com.popeffects.client.screen;

import com.popeffects.config.ConfigManager;
import com.popeffects.config.PopEffectsConfig;
import com.popeffects.config.TriggerType;
import com.popeffects.effect.EffectManager;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Hauptmenue der Mod. Erreichbar ueber Mod Menu, {@code /popeffects config}
 * oder die Taste P.
 */
public class PopEffectsConfigScreen extends Screen {
	private static final int ROW_HEIGHT = 24;
	private static final int COLUMN_WIDTH = 150;

	private final Screen parent;

	public PopEffectsConfigScreen(Screen parent) {
		super(Component.translatable("popeffects.config.title"));
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
		addRenderableWidget(CycleButton.onOffBuilder(config.enabled)
				.create(leftX, top, COLUMN_WIDTH, 20, Component.translatable("popeffects.config.enabled"),
						(button, value) -> {
							config.enabled = value;

							if (!value) {
								EffectManager.clear();
							}

							ConfigManager.save();
						}));

		addRenderableWidget(CycleButton.onOffBuilder(config.showOnSelf)
				.create(leftX, top + ROW_HEIGHT, COLUMN_WIDTH, 20, Component.translatable("popeffects.config.show_self"),
						(button, value) -> {
							config.showOnSelf = value;
							ConfigManager.save();
						}));

		addRenderableWidget(CycleButton.onOffBuilder(config.showOnPlayers)
				.create(leftX, top + ROW_HEIGHT * 2, COLUMN_WIDTH, 20,
						Component.translatable("popeffects.config.show_players"), (button, value) -> {
							config.showOnPlayers = value;
							ConfigManager.save();
						}));

		addRenderableWidget(CycleButton.onOffBuilder(config.showOnMobs)
				.create(leftX, top + ROW_HEIGHT * 3, COLUMN_WIDTH, 20, Component.translatable("popeffects.config.show_mobs"),
						(button, value) -> {
							config.showOnMobs = value;
							ConfigManager.save();
						}));

		addRenderableWidget(CycleButton.onOffBuilder(config.onlyOwnHits)
				.create(leftX, top + ROW_HEIGHT * 4, COLUMN_WIDTH, 20,
						Component.translatable("popeffects.config.only_own_hits"), (button, value) -> {
							config.onlyOwnHits = value;
							ConfigManager.save();
						}));

		addRenderableWidget(CycleButton.onOffBuilder(config.popCounter)
				.create(leftX, top + ROW_HEIGHT * 5, COLUMN_WIDTH, 20,
						Component.translatable("popeffects.config.pop_counter"), (button, value) -> {
							config.popCounter = value;
							ConfigManager.save();
						}));

		// Rechte Spalte: die vier Effekte und die Leistungsbremsen
		int row = 0;

		for (TriggerType type : TriggerType.values()) {
			int y = top + ROW_HEIGHT * row;
			boolean active = config.get(type).enabled;

			addRenderableWidget(Button
					.builder(Component.translatable("popeffects.config.edit", Component.translatable(type.translationKey()))
							.withStyle(active ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY),
							button -> this.minecraft.setScreenAndShow(new EffectEditScreen(this, type, 0)))
					.bounds(rightX, y, COLUMN_WIDTH, 20).build());

			row++;
		}

		addRenderableWidget(new IntSliderWidget(rightX, top + ROW_HEIGHT * 4, COLUMN_WIDTH, 20,
				"popeffects.config.distance", 8, 128, (int) config.maxDistance, value -> {
					config.maxDistance = value;
					ConfigManager.save();
				}));

		addRenderableWidget(new IntSliderWidget(rightX, top + ROW_HEIGHT * 5, COLUMN_WIDTH, 20,
				"popeffects.config.max_effects", 1, 128, config.maxActiveEffects, value -> {
					config.maxActiveEffects = value;
					ConfigManager.save();
				}));

		addRenderableWidget(Button.builder(Component.translatable("popeffects.config.reset_all"), button -> {
			ConfigManager.reset();
			this.rebuildWidgets();
		}).bounds(centerX - 155, this.height - 30, 150, 20).build());

		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
				.bounds(centerX + 5, this.height - 30, 150, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		super.extractRenderState(context, mouseX, mouseY, deltaTicks);

		context.centeredText(this.font, this.title, this.width / 2, 14, 0xFFFFFFFF);

		Component hint = Component.translatable("popeffects.config.hint").withStyle(ChatFormatting.GRAY);
		context.centeredText(this.font, hint, this.width / 2, this.height - 44, 0xFFAAAAAA);
	}

	@Override
	public boolean isPauseScreen() {
		// Nicht pausieren, damit man die Vorschau im Einzelspieler auch laufen
		// sieht.
		return false;
	}

	@Override
	public void onClose() {
		ConfigManager.save();
		this.minecraft.setScreenAndShow(this.parent);
	}
}
