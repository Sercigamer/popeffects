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

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Editor fuer einen einzelnen Effekt.
 *
 * <p>Vier Reiter mit je einem klaren Thema - Form, Farbe, Ablauf, Ton.
 * Standardmaessig zeigt jeder Reiter nur die Einstellungen, die man wirklich
 * oft anfasst; der Schalter unten holt die restlichen dazu. Es faellt also
 * nichts weg, es steht nur nicht alles gleichzeitig auf dem Bildschirm.
 *
 * <p>Der Vorschau-Knopf setzt den Effekt vor dich in die Welt, so sieht man
 * jede Aenderung sofort.
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

	/**
	 * Naechste freie Zeile je Spalte. Dadurch bleibt die Anordnung dicht, egal
	 * wie viele Einstellungen gerade ausgeblendet sind - sonst klafften in der
	 * einfachen Ansicht Luecken.
	 */
	private int leftRow;
	private int rightRow;

	public EffectEditScreen(Screen parent, TriggerType type, int tab) {
		super(Component.translatable(type.translationKey()));

		this.parent = parent;
		this.type = type;
		this.tab = tab;
		this.settings = ConfigManager.get().get(type);
	}

	private boolean showAll() {
		return ConfigManager.get().showAllSettings;
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;
		int tabX = centerX - (TAB_WIDTH * 4 + TAB_GAP * 3) / 2;

		leftRow = 0;
		rightRow = 0;

		addTabButton(tabX, TAB_SHAPE, "popeffects.edit.tab.shape");
		addTabButton(tabX + (TAB_WIDTH + TAB_GAP), TAB_COLOR, "popeffects.edit.tab.color");
		addTabButton(tabX + (TAB_WIDTH + TAB_GAP) * 2, TAB_FLOW, "popeffects.edit.tab.flow");
		addTabButton(tabX + (TAB_WIDTH + TAB_GAP) * 3, TAB_SOUND, "popeffects.edit.tab.sound");

		switch (tab) {
			case TAB_COLOR -> initColorTab();
			case TAB_FLOW -> initFlowTab();
			case TAB_SOUND -> initSoundTab();
			default -> initShapeTab();
		}

		addRenderableWidget(Button
				.builder(Component.translatable("popeffects.edit.preview"),
						button -> EffectManager.preview(type, settings))
				.bounds(centerX - 155, this.height - 54, 100, 20).build());

		addRenderableWidget(Button.builder(Component.translatable("popeffects.edit.reset"), button -> {
			ConfigManager.reset(type);
			this.minecraft.setScreenAndShow(new EffectEditScreen(parent, type, tab));
		}).bounds(centerX - 50, this.height - 54, 100, 20).build());

		addRenderableWidget(Button.builder(
				Component.translatable(showAll() ? "popeffects.edit.detail_all" : "popeffects.edit.detail_simple"),
				button -> {
					ConfigManager.get().showAllSettings = !showAll();
					ConfigManager.save();
					this.minecraft.setScreenAndShow(new EffectEditScreen(parent, type, tab));
				}).bounds(centerX + 55, this.height - 54, 100, 20).build());

		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
				.bounds(centerX - 100, this.height - 28, 200, 20).build());
	}

	private void addTabButton(int x, int target, String translationKey) {
		Component label = Component.translatable(translationKey);

		Button button = Button
				.builder(tab == target ? label.copy().withStyle(ChatFormatting.YELLOW) : label,
						widget -> this.minecraft.setScreenAndShow(new EffectEditScreen(parent, type, target)))
				.bounds(x, 32, TAB_WIDTH, 20).build();

		button.active = tab != target;
		addRenderableWidget(button);
	}

	private int leftX() {
		return this.width / 2 - COLUMN_WIDTH - 5;
	}

	private int rightX() {
		return this.width / 2 + 5;
	}

	private int nextLeft() {
		return FIRST_ROW + leftRow++ * ROW_HEIGHT;
	}

	private int nextRight() {
		return FIRST_ROW + rightRow++ * ROW_HEIGHT;
	}

	private void initShapeTab() {
		addRenderableWidget(WidgetCompat
				.cycler((EffectStyle style) -> Component.translatable(style.translationKey()), settings.style)
				.withValues(EffectStyle.values())
				.create(leftX(), nextLeft(), COLUMN_WIDTH, 20, Component.translatable("popeffects.edit.style"),
						(button, value) -> settings.style = value));

		addRenderableWidget(new FloatSliderWidget(leftX(), nextLeft(), COLUMN_WIDTH, 20, "popeffects.edit.end_radius",
				0.2F, 16.0F, 0.1F, settings.endRadius, value -> settings.endRadius = value));

		addRenderableWidget(new FloatSliderWidget(leftX(), nextLeft(), COLUMN_WIDTH, 20, "popeffects.edit.thickness",
				0.02F, 2.0F, 0.02F, settings.thickness, value -> settings.thickness = value));

		addRenderableWidget(new FloatSliderWidget(rightX(), nextRight(), COLUMN_WIDTH, 20, "popeffects.edit.height",
				0.2F, 8.0F, 0.1F, settings.height, value -> settings.height = value));

		addRenderableWidget(CycleButton.onOffBuilder(settings.followEntity)
				.create(rightX(), nextRight(), COLUMN_WIDTH, 20, Component.translatable("popeffects.edit.follow"),
						(button, value) -> settings.followEntity = value));

		if (!showAll()) {
			return;
		}

		addRenderableWidget(new FloatSliderWidget(leftX(), nextLeft(), COLUMN_WIDTH, 20, "popeffects.edit.start_radius",
				0.0F, 12.0F, 0.1F, settings.startRadius, value -> settings.startRadius = value));

		addRenderableWidget(new FloatSliderWidget(leftX(), nextLeft(), COLUMN_WIDTH, 20, "popeffects.edit.y_offset",
				-2.0F, 4.0F, 0.05F, settings.yOffset, value -> settings.yOffset = value));

		addRenderableWidget(new IntSliderWidget(rightX(), nextRight(), COLUMN_WIDTH, 20, "popeffects.edit.segments", 8,
				96, settings.segments, value -> settings.segments = value));

		addRenderableWidget(new IntSliderWidget(rightX(), nextRight(), COLUMN_WIDTH, 20, "popeffects.edit.rings", 1, 12,
				settings.ringCount, value -> settings.ringCount = value));

		addRenderableWidget(new IntSliderWidget(rightX(), nextRight(), COLUMN_WIDTH, 20, "popeffects.edit.rotation",
				-360, 360, (int) settings.rotationSpeed, value -> settings.rotationSpeed = value));
	}

	private void initColorTab() {
		addRenderableWidget(new IntSliderWidget(leftX(), nextLeft(), COLUMN_WIDTH, 20, "popeffects.edit.start_red", 0,
				255, channel(settings.colorStartRgb(), 16),
				value -> settings.setColorStartRgb(withChannel(settings.colorStartRgb(), 16, value))));

		addRenderableWidget(new IntSliderWidget(leftX(), nextLeft(), COLUMN_WIDTH, 20, "popeffects.edit.start_green", 0,
				255, channel(settings.colorStartRgb(), 8),
				value -> settings.setColorStartRgb(withChannel(settings.colorStartRgb(), 8, value))));

		addRenderableWidget(new IntSliderWidget(leftX(), nextLeft(), COLUMN_WIDTH, 20, "popeffects.edit.start_blue", 0,
				255, channel(settings.colorStartRgb(), 0),
				value -> settings.setColorStartRgb(withChannel(settings.colorStartRgb(), 0, value))));

		addRenderableWidget(new IntSliderWidget(leftX(), nextLeft(), COLUMN_WIDTH, 20, "popeffects.edit.end_red", 0,
				255, channel(settings.colorEndRgb(), 16),
				value -> settings.setColorEndRgb(withChannel(settings.colorEndRgb(), 16, value))));

		addRenderableWidget(new IntSliderWidget(leftX(), nextLeft(), COLUMN_WIDTH, 20, "popeffects.edit.end_green", 0,
				255, channel(settings.colorEndRgb(), 8),
				value -> settings.setColorEndRgb(withChannel(settings.colorEndRgb(), 8, value))));

		addRenderableWidget(new IntSliderWidget(leftX(), nextLeft(), COLUMN_WIDTH, 20, "popeffects.edit.end_blue", 0,
				255, channel(settings.colorEndRgb(), 0),
				value -> settings.setColorEndRgb(withChannel(settings.colorEndRgb(), 0, value))));

		addRenderableWidget(new IntSliderWidget(rightX(), nextRight(), COLUMN_WIDTH, 20, "popeffects.edit.alpha", 10,
				255, settings.alpha, value -> settings.alpha = value));

		if (!showAll()) {
			return;
		}

		addRenderableWidget(CycleButton.onOffBuilder(settings.gradient)
				.create(rightX(), nextRight(), COLUMN_WIDTH, 20, Component.translatable("popeffects.edit.gradient"),
						(button, value) -> settings.gradient = value));

		addRenderableWidget(CycleButton.onOffBuilder(settings.rainbow)
				.create(rightX(), nextRight(), COLUMN_WIDTH, 20, Component.translatable("popeffects.edit.rainbow"),
						(button, value) -> settings.rainbow = value));

		// Beide Schalter brauchen eigene Render-Pipelines. Wo Minecraft die
		// nicht hergibt, wird der Knopf ausgegraut statt wirkungslos
		// angeboten - lieber ehrlich als still.
		CycleButton<Boolean> additiveButton = CycleButton.onOffBuilder(settings.additive)
				.create(rightX(), nextRight(), COLUMN_WIDTH, 20, Component.translatable("popeffects.edit.additive"),
						(button, value) -> settings.additive = value);
		additiveButton.active = EffectLayers.supportsAdditive();
		addRenderableWidget(additiveButton);

		addRenderableWidget(CycleButton.onOffBuilder(settings.glow)
				.create(rightX(), nextRight(), COLUMN_WIDTH, 20, Component.translatable("popeffects.edit.glow"),
						(button, value) -> settings.glow = value));

		CycleButton<Boolean> throughWallsButton = CycleButton.onOffBuilder(settings.throughWalls)
				.create(rightX(), nextRight(), COLUMN_WIDTH, 20,
						Component.translatable("popeffects.edit.through_walls"),
						(button, value) -> settings.throughWalls = value);
		throughWallsButton.active = EffectLayers.supportsThroughWalls();
		addRenderableWidget(throughWallsButton);
	}

	private void initFlowTab() {
		addRenderableWidget(CycleButton.onOffBuilder(settings.enabled)
				.create(leftX(), nextLeft(), COLUMN_WIDTH, 20, Component.translatable("popeffects.edit.enabled"),
						(button, value) -> settings.enabled = value));

		addRenderableWidget(new IntSliderWidget(leftX(), nextLeft(), COLUMN_WIDTH, 20, "popeffects.edit.duration", 3,
				100, settings.durationTicks, value -> settings.durationTicks = value));

		addRenderableWidget(new IntSliderWidget(leftX(), nextLeft(), COLUMN_WIDTH, 20, "popeffects.edit.fade_out", 0,
				60, settings.fadeOutTicks, value -> settings.fadeOutTicks = value));

		// Schwelle und Groessenkopplung ergeben nur bei Schaden einen Sinn -
		// ein Totem poppt nun mal ohne Schadenswert.
		boolean damageTrigger = type == TriggerType.BIG_DAMAGE || type == TriggerType.SELF_HURT;

		if (damageTrigger) {
			addRenderableWidget(new FloatSliderWidget(rightX(), nextRight(), COLUMN_WIDTH, 20,
					"popeffects.edit.threshold", 0.5F, 40.0F, 0.5F, settings.threshold,
					value -> settings.threshold = value));
		}

		if (!showAll()) {
			return;
		}

		addRenderableWidget(new IntSliderWidget(leftX(), nextLeft(), COLUMN_WIDTH, 20, "popeffects.edit.fade_in", 0, 60,
				settings.fadeInTicks, value -> settings.fadeInTicks = value));

		addRenderableWidget(new FloatSliderWidget(leftX(), nextLeft(), COLUMN_WIDTH, 20,
				"popeffects.edit.fade_expansion", 0.0F, 6.0F, 0.1F, settings.fadeOutExpansion,
				value -> settings.fadeOutExpansion = value));

		if (damageTrigger) {
			addRenderableWidget(CycleButton.onOffBuilder(settings.scaleWithDamage)
					.create(rightX(), nextRight(), COLUMN_WIDTH, 20,
							Component.translatable("popeffects.edit.scale_with_damage"),
							(button, value) -> settings.scaleWithDamage = value));
		}
	}

	private void initSoundTab() {
		addRenderableWidget(CycleButton.onOffBuilder(settings.sound)
				.create(leftX(), nextLeft(), COLUMN_WIDTH, 20, Component.translatable("popeffects.edit.sound"),
						(button, value) -> settings.sound = value));

		addRenderableWidget(WidgetCompat
				.cycler((String id) -> Component.literal(shortName(id)), settings.soundId)
				.withValues(withCurrent(EffectSettings.SOUND_PRESETS, settings.soundId))
				.create(leftX(), nextLeft(), COLUMN_WIDTH, 20, Component.translatable("popeffects.edit.sound_id"),
						(button, value) -> settings.soundId = value));

		addRenderableWidget(CycleButton.onOffBuilder(settings.particles)
				.create(rightX(), nextRight(), COLUMN_WIDTH, 20, Component.translatable("popeffects.edit.particles"),
						(button, value) -> settings.particles = value));

		addRenderableWidget(WidgetCompat
				.cycler((String id) -> Component.literal(shortName(id)), settings.particleId)
				.withValues(withCurrent(EffectSettings.PARTICLE_PRESETS, settings.particleId))
				.create(rightX(), nextRight(), COLUMN_WIDTH, 20, Component.translatable("popeffects.edit.particle_id"),
						(button, value) -> settings.particleId = value));

		if (!showAll()) {
			return;
		}

		addRenderableWidget(new FloatSliderWidget(leftX(), nextLeft(), COLUMN_WIDTH, 20, "popeffects.edit.sound_volume",
				0.0F, 2.0F, 0.05F, settings.soundVolume, value -> settings.soundVolume = value));

		addRenderableWidget(new FloatSliderWidget(leftX(), nextLeft(), COLUMN_WIDTH, 20, "popeffects.edit.sound_pitch",
				0.5F, 2.0F, 0.05F, settings.soundPitch, value -> settings.soundPitch = value));

		addRenderableWidget(new IntSliderWidget(rightX(), nextRight(), COLUMN_WIDTH, 20,
				"popeffects.edit.particle_count", 0, 96, settings.particleCount,
				value -> settings.particleCount = value));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		super.extractRenderState(context, mouseX, mouseY, deltaTicks);

		context.centeredText(this.font, this.title, this.width / 2, 14, 0xFFFFFFFF);

		if (tab == TAB_COLOR) {
			drawColorPreview(context);
		} else if (tab == TAB_FLOW && showAll()) {
			Component hint = Component.translatable("popeffects.edit.fade_hint").withStyle(ChatFormatting.GRAY);
			context.centeredText(this.font, hint, this.width / 2, this.height - 68, 0xFFAAAAAA);
		}
	}

	/**
	 * Zwei Farbfelder mit Hex-Code, damit man die Regler nicht raten muss.
	 * Sitzt unter den Spalten, damit rechts Platz fuer einen Schalter mehr
	 * bleibt.
	 */
	private void drawColorPreview(GuiGraphicsExtractor context) {
		int centerX = this.width / 2;
		int y = this.height - 76;

		context.fill(centerX - 105, y, centerX - 5, y + 16, 0xFF000000 | settings.colorStartRgb());
		context.fill(centerX + 5, y, centerX + 105, y + 16, 0xFF000000 | settings.colorEndRgb());

		context.centeredText(this.font, Component.literal(settings.colorStart), centerX - 55, y + 4, 0xFFFFFFFF);
		context.centeredText(this.font, Component.literal(settings.colorEnd), centerX + 55, y + 4, 0xFFFFFFFF);
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
	public boolean isPauseScreen() {
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
	public void onClose() {
		this.minecraft.setScreenAndShow(this.parent);
	}
}
