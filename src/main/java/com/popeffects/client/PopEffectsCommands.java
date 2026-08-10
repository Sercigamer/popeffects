package com.popeffects.client;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.util.Locale;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.popeffects.config.ConfigManager;
import com.popeffects.config.EffectSettings;
import com.popeffects.config.EffectStyle;
import com.popeffects.config.TriggerType;
import com.popeffects.effect.EffectManager;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * {@code /popeffects} (kurz: {@code /pe}) - laeuft komplett clientseitig, der
 * Server sieht davon nichts.
 */
public final class PopEffectsCommands {
	private PopEffectsCommands() {
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			LiteralArgumentBuilder<FabricClientCommandSource> root = literal("popeffects")
					.executes(PopEffectsCommands::status)
					.then(literal("config").executes(PopEffectsCommands::openConfig))
					.then(literal("status").executes(PopEffectsCommands::status))
					.then(literal("help").executes(PopEffectsCommands::help))
					.then(literal("on").executes(ctx -> setEnabled(ctx, true)))
					.then(literal("off").executes(ctx -> setEnabled(ctx, false)))
					.then(literal("reset").executes(ctx -> {
						ConfigManager.reset();
						return feedback(ctx,
								Text.translatable("popeffects.command.reset_all").formatted(Formatting.GREEN));
					}));

			for (TriggerType type : TriggerType.values()) {
				root = root.then(effectNode(type));
			}

			dispatcher.register(root);
			dispatcher.register(literal("pe").executes(PopEffectsCommands::status)
					.redirect(dispatcher.getRoot().getChild("popeffects")));
		});
	}

	/**
	 * Ein Unterbaum je Effekt, also {@code /popeffects totem style sphere} und
	 * so weiter.
	 */
	private static LiteralArgumentBuilder<FabricClientCommandSource> effectNode(TriggerType type) {
		return literal(commandName(type))
				.executes(ctx -> effectStatus(ctx, type))
				.then(literal("on").executes(ctx -> setEffectEnabled(ctx, type, true)))
				.then(literal("off").executes(ctx -> setEffectEnabled(ctx, type, false)))
				.then(literal("preview").executes(ctx -> {
					EffectManager.preview(type, ConfigManager.get().get(type));
					return 1;
				}))
				.then(literal("reset").executes(ctx -> {
					ConfigManager.reset(type);
					return feedback(ctx, Text.translatable("popeffects.command.reset_one",
							Text.translatable(type.translationKey())).formatted(Formatting.GREEN));
				}))
				.then(literal("style")
						.then(argument("style", StringArgumentType.word())
								.suggests((ctx, builder) -> {
									for (EffectStyle style : EffectStyle.values()) {
										builder.suggest(style.name().toLowerCase(Locale.ROOT));
									}

									return builder.buildFuture();
								})
								.executes(ctx -> setStyle(ctx, type))))
				// Bewusst zwei Literale statt "color <hex>" plus "color end <hex>":
				// sonst weiss Brigadier bei der Eingabe "end" nicht, ob eine
				// Farbe oder das Unterkommando gemeint ist.
				.then(literal("color")
						.then(literal("start")
								.then(argument("hex", StringArgumentType.word())
										.executes(ctx -> setColor(ctx, type, true))))
						.then(literal("end")
								.then(argument("hex", StringArgumentType.word())
										.executes(ctx -> setColor(ctx, type, false)))))
				.then(literal("duration")
						.then(argument("ticks", IntegerArgumentType.integer(3, 200)).executes(ctx -> {
							ConfigManager.get().get(type).durationTicks = IntegerArgumentType.getInteger(ctx, "ticks");
							return saved(ctx, type);
						})))
				.then(literal("fadein")
						.then(argument("ticks", IntegerArgumentType.integer(0, 60)).executes(ctx -> {
							ConfigManager.get().get(type).fadeInTicks = IntegerArgumentType.getInteger(ctx, "ticks");
							return saved(ctx, type);
						})))
				.then(literal("fadeout")
						.then(argument("ticks", IntegerArgumentType.integer(0, 60)).executes(ctx -> {
							ConfigManager.get().get(type).fadeOutTicks = IntegerArgumentType.getInteger(ctx, "ticks");
							return saved(ctx, type);
						})))
				.then(literal("radius")
						.then(argument("blocks", FloatArgumentType.floatArg(0.2F, 32.0F)).executes(ctx -> {
							ConfigManager.get().get(type).endRadius = FloatArgumentType.getFloat(ctx, "blocks");
							return saved(ctx, type);
						})))
				.then(literal("threshold")
						.then(argument("damage", FloatArgumentType.floatArg(0.5F, 200.0F)).executes(ctx -> {
							ConfigManager.get().get(type).threshold = FloatArgumentType.getFloat(ctx, "damage");
							return saved(ctx, type);
						})));
	}

	private static int openConfig(CommandContext<FabricClientCommandSource> ctx) {
		// Nicht sofort oeffnen - der Chat-Screen wird gerade erst geschlossen.
		PopEffectsKeybinds.requestConfigScreen();
		return 1;
	}

	private static int help(CommandContext<FabricClientCommandSource> ctx) {
		FabricClientCommandSource source = ctx.getSource();
		source.sendFeedback(header());

		for (String line : new String[] {
				"popeffects.help.config",
				"popeffects.help.toggle",
				"popeffects.help.effect",
				"popeffects.help.preview",
				"popeffects.help.style",
				"popeffects.help.color",
				"popeffects.help.fade",
				"popeffects.help.threshold",
				"popeffects.help.reset",
				"popeffects.help.keys" }) {
			source.sendFeedback(Text.translatable(line).formatted(Formatting.GRAY));
		}

		return 1;
	}

	private static int status(CommandContext<FabricClientCommandSource> ctx) {
		FabricClientCommandSource source = ctx.getSource();
		source.sendFeedback(header());
		source.sendFeedback(Text.translatable("popeffects.command.status.enabled", onOff(ConfigManager.get().enabled))
				.formatted(Formatting.GRAY));

		for (TriggerType type : TriggerType.values()) {
			EffectSettings settings = ConfigManager.get().get(type);

			source.sendFeedback(Text.translatable("popeffects.command.status.effect",
					Text.translatable(type.translationKey()).formatted(Formatting.WHITE),
					onOff(settings.enabled),
					Text.translatable(settings.style.translationKey()).formatted(Formatting.AQUA))
					.formatted(Formatting.GRAY));
		}

		return 1;
	}

	private static int effectStatus(CommandContext<FabricClientCommandSource> ctx, TriggerType type) {
		EffectSettings settings = ConfigManager.get().get(type);

		ctx.getSource().sendFeedback(Text.translatable("popeffects.command.status.effect",
				Text.translatable(type.translationKey()).formatted(Formatting.WHITE),
				onOff(settings.enabled),
				Text.translatable(settings.style.translationKey()).formatted(Formatting.AQUA))
				.formatted(Formatting.GRAY));

		return 1;
	}

	private static int setEnabled(CommandContext<FabricClientCommandSource> ctx, boolean enabled) {
		ConfigManager.get().enabled = enabled;
		ConfigManager.save();

		if (!enabled) {
			EffectManager.clear();
		}

		return feedback(ctx, Text.translatable(enabled ? "popeffects.message.enabled" : "popeffects.message.disabled")
				.formatted(enabled ? Formatting.GREEN : Formatting.GRAY));
	}

	private static int setEffectEnabled(CommandContext<FabricClientCommandSource> ctx, TriggerType type,
			boolean enabled) {
		ConfigManager.get().get(type).enabled = enabled;
		ConfigManager.save();

		return feedback(ctx, Text.translatable("popeffects.command.effect_toggled",
				Text.translatable(type.translationKey()), onOff(enabled)).formatted(Formatting.GRAY));
	}

	private static int setStyle(CommandContext<FabricClientCommandSource> ctx, TriggerType type) {
		String raw = StringArgumentType.getString(ctx, "style").toUpperCase(Locale.ROOT);

		for (EffectStyle style : EffectStyle.values()) {
			if (style.name().equals(raw)) {
				ConfigManager.get().get(type).style = style;
				ConfigManager.save();

				return feedback(ctx, Text.translatable("popeffects.command.style_set",
						Text.translatable(type.translationKey()), Text.translatable(style.translationKey()))
						.formatted(Formatting.GREEN));
			}
		}

		ctx.getSource().sendError(Text.translatable("popeffects.command.unknown_style", raw.toLowerCase(Locale.ROOT)));
		return 0;
	}

	private static int setColor(CommandContext<FabricClientCommandSource> ctx, TriggerType type, boolean start) {
		String raw = StringArgumentType.getString(ctx, "hex").replace("#", "").trim();

		int color;

		try {
			color = Integer.parseInt(raw, 16) & 0xFFFFFF;
		} catch (NumberFormatException e) {
			ctx.getSource().sendError(Text.translatable("popeffects.command.bad_color", raw));
			return 0;
		}

		EffectSettings settings = ConfigManager.get().get(type);

		if (start) {
			settings.setColorStartRgb(color);
		} else {
			settings.setColorEndRgb(color);
		}

		ConfigManager.save();

		return feedback(ctx, Text.translatable("popeffects.command.color_set",
				Text.translatable(type.translationKey()), EffectSettings.formatColor(color))
				.formatted(Formatting.GREEN));
	}

	private static int saved(CommandContext<FabricClientCommandSource> ctx, TriggerType type) {
		ConfigManager.get().sanitize();
		ConfigManager.save();

		return feedback(ctx, Text.translatable("popeffects.command.saved", Text.translatable(type.translationKey()))
				.formatted(Formatting.GREEN));
	}

	private static int feedback(CommandContext<FabricClientCommandSource> ctx, Text message) {
		ctx.getSource().sendFeedback(message);
		return 1;
	}

	private static String commandName(TriggerType type) {
		return switch (type) {
			case TOTEM_POP -> "totem";
			case BIG_DAMAGE -> "damage";
			case KILL -> "kill";
			case SELF_HURT -> "self";
		};
	}

	private static Text onOff(boolean value) {
		return Text.translatable(value ? "popeffects.generic.on" : "popeffects.generic.off")
				.formatted(value ? Formatting.GREEN : Formatting.RED);
	}

	private static Text header() {
		return Text.literal("PopEffects").formatted(Formatting.GOLD, Formatting.BOLD);
	}
}
