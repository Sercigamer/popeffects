package com.popeffects.client.screen;

import java.util.ArrayList;
import java.util.List;

import com.popeffects.compat.EffectLayers;
import com.popeffects.compat.WidgetCompat;
import com.popeffects.config.ConfigManager;
import com.popeffects.config.EffectSettings;
import com.popeffects.config.EffectStyle;
import com.popeffects.config.TriggerType;
import com.popeffects.effect.EffectManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Editor fuer einen einzelnen Effekt.
 *
 * <p>Vier Reiter mit je einem klaren Thema - Form, Farbe, Ablauf, Ton -, damit
 * nichts scrollen muss und man nicht suchen muss, wo eine Einstellung steckt.
 * Der Vorschau-Knopf setzt den Effekt vor dich in die Welt, so sieht man jede
 * Aenderung sofort.
 */
public class EffectEditScreen extends Screen {
	private static final int TAB_SHAPE = 0;
	private static final int TAB_COLOR = 1;
	private static final int TAB_FLOW = 2;
	private static final int TAB_SOUND = 3;

	private static final int ROW_HEIGHT = 24;
	private static final int COLUMN_WIDTH = 150;
	private static final int FIRST_ROW = 60;

	private static final int TAB_WIDTH = 76;
	private static final int TAB_GAP = 5;

	private final Screen parent;
	private final TriggerType type;
	private final EffectSettings settings;
	private final int tab;

	public EffectEditScreen(Screen parent, TriggerType type, int tab) {
		super(Text.translatable(type.translationKey()));

		this.parent = parent;
		this.type = type;
		this.tab = tab;
		this.settings = ConfigManager.get().get(type);
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;
		int tabX = centerX - (TAB_WIDTH * 4 + TAB_GAP * 3) / 2;

		addTabButton(tabX, TAB_SHAPE, "popeffects.edit.tab.shape");
		addTabButton(tabX + (TAB_WIDTH + TAB_GAP), TAB_COLOR, "popeffects.edit.tab.color");
		addTabButton(tabX + (TAB_WIDTH + TAB_GAP) * 2, TAB_FLOW, "popeffects.edit.tab.flow");
		addTabButton(tabX + (TAB_WIDTH + TAB_GAP) * 3, TAB_SOUND, "popeffects.edit.tab.sound");

		switch (tab) {
			case TAB_COLOR -> initColorTab(centerX);
			case TAB_FLOW -> initFlowTab(centerX);
			case TAB_SOUND -> initSoundTab(centerX);
			default -> initShapeTab(centerX);
		}

		addDrawableChild(ButtonWidget
				.builder(Text.translatable("popeffects.edit.preview"),
						button -> EffectManager.preview(type, settings))
				.dimensions(centerX - 155, this.height - 54, 150, 20).build());

		addDrawableChild(ButtonWidget.builder(Text.translatable("popeffects.edit.reset"), button -> {
			ConfigManager.reset(type);
			this.client.setScreen(new EffectEditScreen(parent, type, tab));
		}).dimensions(centerX + 5, this.height - 54, 150, 20).build());

		addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
				.dimensions(centerX - 100, this.height - 28, 200, 20).build());
	}

	private void addTabButton(int x, int target, String translationKey) {
		Text label = Text.translatable(translationKey);

		ButtonWidget button = ButtonWidget
				.builder(tab == target ? label.copy().formatted(Formatting.YELLOW) : label,
						widget -> this.client.setScreen(new EffectEditScreen(parent, type, target)))
				.dimensions(x, 32, TAB_WIDTH, 20).build();

		button.active = tab != target;
		addDrawableChild(button);
	}

	private void initShapeTab(int centerX) {
		int leftX = centerX - COLUMN_WIDTH - 5;
		int rightX = centerX + 5;

		addDrawableChild(WidgetCompat
				.cycler((EffectStyle style) -> Text.translatable(style.translationKey()), settings.style)
				.values(EffectStyle.values())
				.build(leftX, row(0), COLUMN_WIDTH, 20, Text.translatable("popeffects.edit.style"),
						(button, value) -> settings.style = value));

		addDrawableChild(new FloatSliderWidget(leftX, row(1), COLUMN_WIDTH, 20, "popeffects.edit.start_radius", 0.0F,
				12.0F, 0.1F, settings.startRadius, value -> settings.startRadius = value));

		addDrawableChild(new FloatSliderWidget(leftX, row(2), COLUMN_WIDTH, 20, "popeffects.edit.end_radius", 0.2F,
				16.0F, 0.1F, settings.endRadius, value -> settings.endRadius = value));

		addDrawableChild(new FloatSliderWidget(leftX, row(3), COLUMN_WIDTH, 20, "popeffects.edit.thickness", 0.02F,
				2.0F, 0.02F, settings.thickness, value -> settings.thickness = value));

		addDrawableChild(new FloatSliderWidget(leftX, row(4), COLUMN_WIDTH, 20, "popeffects.edit.height", 0.2F, 8.0F,
				0.1F, settings.height, value -> settings.height = value));

		addDrawableChild(new FloatSliderWidget(leftX, row(5), COLUMN_WIDTH, 20, "popeffects.edit.y_offset", -2.0F,
				4.0F, 0.05F, settings.yOffset, value -> settings.yOffset = value));

		addDrawableChild(new IntSliderWidget(rightX, row(0), COLUMN_WIDTH, 20, "popeffects.edit.segments", 8, 96,
				settings.segments, value -> settings.segments = value));

		addDrawableChild(new IntSliderWidget(rightX, row(1), COLUMN_WIDTH, 20, "popeffects.edit.rings", 1, 12,
				settings.ringCount, value -> settings.ringCount = value));

		addDrawableChild(new IntSliderWidget(rightX, row(2), COLUMN_WIDTH, 20, "popeffects.edit.rotation", -360, 360,
				(int) settings.rotationSpeed, value -> settings.rotationSpeed = value));

		addDrawableChild(CyclingButtonWidget.onOffBuilder(settings.followEntity)
				.build(rightX, row(3), COLUMN_WIDTH, 20, Text.translatable("popeffects.edit.follow"),
						(button, value) -> settings.followEntity = value));
	}

	private void initColorTab(int centerX) {
		int leftX = centerX - COLUMN_WIDTH - 5;
		int rightX = centerX + 5;

		addDrawableChild(new IntSliderWidget(leftX, row(0), COLUMN_WIDTH, 20, "popeffects.edit.start_red", 0, 255,
				channel(settings.colorStartRgb(), 16),
				value -> settings.setColorStartRgb(withChannel(settings.colorStartRgb(), 16, value))));

		addDrawableChild(new IntSliderWidget(leftX, row(1), COLUMN_WIDTH, 20, "popeffects.edit.start_green", 0, 255,
				channel(settings.colorStartRgb(), 8),
				value -> settings.setColorStartRgb(withChannel(settings.colorStartRgb(), 8, value))));

		addDrawableChild(new IntSliderWidget(leftX, row(2), COLUMN_WIDTH, 20, "popeffects.edit.start_blue", 0, 255,
				channel(settings.colorStartRgb(), 0),
				value -> settings.setColorStartRgb(withChannel(settings.colorStartRgb(), 0, value))));

		addDrawableChild(new IntSliderWidget(leftX, row(3), COLUMN_WIDTH, 20, "popeffects.edit.end_red", 0, 255,
				channel(settings.colorEndRgb(), 16),
				value -> settings.setColorEndRgb(withChannel(settings.colorEndRgb(), 16, value))));

		addDrawableChild(new IntSliderWidget(leftX, row(4), COLUMN_WIDTH, 20, "popeffects.edit.end_green", 0, 255,
				channel(settings.colorEndRgb(), 8),
				value -> settings.setColorEndRgb(withChannel(settings.colorEndRgb(), 8, value))));

		addDrawableChild(new IntSliderWidget(leftX, row(5), COLUMN_WIDTH, 20, "popeffects.edit.end_blue", 0, 255,
				channel(settings.colorEndRgb(), 0),
				value -> settings.setColorEndRgb(withChannel(settings.colorEndRgb(), 0, value))));

		addDrawableChild(new IntSliderWidget(rightX, row(0), COLUMN_WIDTH, 20, "popeffects.edit.alpha", 10, 255,
				settings.alpha, value -> settings.alpha = value));

		addDrawableChild(CyclingButtonWidget.onOffBuilder(settings.gradient)
				.build(rightX, row(1), COLUMN_WIDTH, 20, Text.translatable("popeffects.edit.gradient"),
						(button, value) -> settings.gradient = value));

		addDrawableChild(CyclingButtonWidget.onOffBuilder(settings.rainbow)
				.build(rightX, row(2), COLUMN_WIDTH, 20, Text.translatable("popeffects.edit.rainbow"),
						(button, value) -> settings.rainbow = value));

		// Beide Schalter brauchen eigene Render-Pipelines. Wo Minecraft die
		// nicht hergibt, wird der Knopf ausgegraut statt wirkungslos
		// angeboten - lieber ehrlich als still.
		CyclingButtonWidget<Boolean> additiveButton = CyclingButtonWidget.onOffBuilder(settings.additive)
				.build(rightX, row(3), COLUMN_WIDTH, 20, Text.translatable("popeffects.edit.additive"),
						(button, value) -> settings.additive = value);
		additiveButton.active = EffectLayers.supportsAdditive();
		addDrawableChild(additiveButton);

		addDrawableChild(CyclingButtonWidget.onOffBuilder(settings.glow)
				.build(rightX, row(4), COLUMN_WIDTH, 20, Text.translatable("popeffects.edit.glow"),
						(button, value) -> settings.glow = value));

		CyclingButtonWidget<Boolean> throughWallsButton = CyclingButtonWidget.onOffBuilder(settings.throughWalls)
				.build(rightX, row(5), COLUMN_WIDTH, 20, Text.translatable("popeffects.edit.through_walls"),
						(button, value) -> settings.throughWalls = value);
		throughWallsButton.active = EffectLayers.supportsThroughWalls();
		addDrawableChild(throughWallsButton);
	}

	private void initFlowTab(int centerX) {
		int leftX = centerX - COLUMN_WIDTH - 5;
		int rightX = centerX + 5;

		addDrawableChild(CyclingButtonWidget.onOffBuilder(settings.enabled)
				.build(leftX, row(0), COLUMN_WIDTH, 20, Text.translatable("popeffects.edit.enabled"),
						(button, value) -> settings.enabled = value));

		addDrawableChild(new IntSliderWidget(leftX, row(1), COLUMN_WIDTH, 20, "popeffects.edit.duration", 3, 100,
				settings.durationTicks, value -> settings.durationTicks = value));

		addDrawableChild(new IntSliderWidget(leftX, row(2), COLUMN_WIDTH, 20, "popeffects.edit.fade_in", 0, 60,
				settings.fadeInTicks, value -> settings.fadeInTicks = value));

		addDrawableChild(new IntSliderWidget(leftX, row(3), COLUMN_WIDTH, 20, "popeffects.edit.fade_out", 0, 60,
				settings.fadeOutTicks, value -> settings.fadeOutTicks = value));

		addDrawableChild(new FloatSliderWidget(leftX, row(4), COLUMN_WIDTH, 20, "popeffects.edit.fade_expansion", 0.0F,
				6.0F, 0.1F, settings.fadeOutExpansion, value -> settings.fadeOutExpansion = value));

		// Schwelle und Groessenkopplung ergeben nur bei Schaden einen Sinn -
		// ein Totem poppt nun mal ohne Schadenswert.
		if (type == TriggerType.BIG_DAMAGE || type == TriggerType.SELF_HURT) {
			addDrawableChild(new FloatSliderWidget(rightX, row(0), COLUMN_WIDTH, 20, "popeffects.edit.threshold", 0.5F,
					40.0F, 0.5F, settings.threshold, value -> settings.threshold = value));

			addDrawableChild(CyclingButtonWidget.onOffBuilder(settings.scaleWithDamage)
					.build(rightX, row(1), COLUMN_WIDTH, 20, Text.translatable("popeffects.edit.scale_with_damage"),
							(button, value) -> settings.scaleWithDamage = value));
		}
	}

	private void initSoundTab(int centerX) {
		int leftX = centerX - COLUMN_WIDTH - 5;
		int rightX = centerX + 5;

		addDrawableChild(CyclingButtonWidget.onOffBuilder(settings.sound)
				.build(leftX, row(0), COLUMN_WIDTH, 20, Text.translatable("popeffects.edit.sound"),
						(button, value) -> settings.sound = value));

		addDrawableChild(WidgetCompat
				.cycler((String id) -> Text.literal(shortName(id)), settings.soundId)
				.values(withCurrent(EffectSettings.SOUND_PRESETS, settings.soundId))
				.build(leftX, row(1), COLUMN_WIDTH, 20, Text.translatable("popeffects.edit.sound_id"),
						(button, value) -> settings.soundId = value));

		addDrawableChild(new FloatSliderWidget(leftX, row(2), COLUMN_WIDTH, 20, "popeffects.edit.sound_volume", 0.0F,
				2.0F, 0.05F, settings.soundVolume, value -> settings.soundVolume = value));

		addDrawableChild(new FloatSliderWidget(leftX, row(3), COLUMN_WIDTH, 20, "popeffects.edit.sound_pitch", 0.5F,
				2.0F, 0.05F, settings.soundPitch, value -> settings.soundPitch = value));

		addDrawableChild(CyclingButtonWidget.onOffBuilder(settings.particles)
				.build(rightX, row(0), COLUMN_WIDTH, 20, Text.translatable("popeffects.edit.particles"),
						(button, value) -> settings.particles = value));

		addDrawableChild(WidgetCompat
				.cycler((String id) -> Text.literal(shortName(id)), settings.particleId)
				.values(withCurrent(EffectSettings.PARTICLE_PRESETS, settings.particleId))
				.build(rightX, row(1), COLUMN_WIDTH, 20, Text.translatable("popeffects.edit.particle_id"),
						(button, value) -> settings.particleId = value));

		addDrawableChild(new IntSliderWidget(rightX, row(2), COLUMN_WIDTH, 20, "popeffects.edit.particle_count", 0, 96,
				settings.particleCount, value -> settings.particleCount = value));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);

		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, 0xFFFFFFFF);

		if (tab == TAB_COLOR) {
			drawColorPreview(context);
		} else if (tab == TAB_FLOW) {
			Text hint = Text.translatable("popeffects.edit.fade_hint").formatted(Formatting.GRAY);
			context.drawCenteredTextWithShadow(this.textRenderer, hint, this.width / 2, this.height - 68, 0xFFAAAAAA);
		}
	}

	/**
	 * Zwei Farbfelder mit Hex-Code, damit man die Regler nicht raten muss.
	 * Sitzt unter den Spalten, damit rechts Platz fuer einen Schalter mehr
	 * bleibt.
	 */
	private void drawColorPreview(DrawContext context) {
		int centerX = this.width / 2;
		int y = this.height - 76;

		context.fill(centerX - 105, y, centerX - 5, y + 16, 0xFF000000 | settings.colorStartRgb());
		context.fill(centerX + 5, y, centerX + 105, y + 16, 0xFF000000 | settings.colorEndRgb());

		context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(settings.colorStart), centerX - 55,
				y + 4, 0xFFFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(settings.colorEnd), centerX + 55,
				y + 4, 0xFFFFFFFF);
	}

	private static int row(int index) {
		return FIRST_ROW + index * ROW_HEIGHT;
	}

	private static int channel(int rgb, int shift) {
		return (rgb >> shift) & 0xFF;
	}

	private static int withChannel(int rgb, int shift, int value) {
		return (rgb & ~(0xFF << shift)) | ((value & 0xFF) << shift);
	}

	/**
	 * Die Vorschlagsliste plus - falls noetig - der aktuell eingestellte Wert.
	 * Sonst faende der Knopf seinen eigenen Startwert nicht wieder, wenn
	 * jemand die JSON-Datei von Hand angepasst hat.
	 */
	private static List<String> withCurrent(String[] presets, String current) {
		List<String> values = new ArrayList<>(List.of(presets));

		if (current != null && !values.contains(current)) {
			values.add(0, current);
		}

		return values;
	}

	/** "minecraft:block.beacon.activate" wird zu "beacon.activate". */
	private static String shortName(String id) {
		String withoutNamespace = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
		String[] parts = withoutNamespace.split("\\.");

		if (parts.length <= 2) {
			return withoutNamespace;
		}

		return parts[parts.length - 2] + "." + parts[parts.length - 1];
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void removed() {
		// Auch beim Wechsel des Reiters - dann geht nichts verloren, wenn das
		// Spiel danach abstuerzt. sanitize() kappt dabei Ein- und Ausblenden
		// auf die Dauer, falls beide Regler zu weit aufgedreht wurden.
		ConfigManager.get().sanitize();
		ConfigManager.save();
	}

	@Override
	public void close() {
		this.client.setScreen(this.parent);
	}
}
