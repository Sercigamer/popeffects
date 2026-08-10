package com.popeffects.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.popeffects.PopEffects;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Laedt und speichert {@link PopEffectsConfig} als JSON.
 */
public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.create();

	private static Path configPath;
	private static PopEffectsConfig config = new PopEffectsConfig();

	private ConfigManager() {
	}

	public static PopEffectsConfig get() {
		return config;
	}

	public static Path path() {
		if (configPath == null) {
			configPath = FabricLoader.getInstance().getConfigDir().resolve(PopEffects.MOD_ID + ".json");
		}

		return configPath;
	}

	public static void load() {
		Path file = path();

		if (Files.exists(file)) {
			try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
				PopEffectsConfig loaded = GSON.fromJson(reader, PopEffectsConfig.class);

				if (loaded != null) {
					config = loaded;
				}
			} catch (Exception e) {
				PopEffects.LOGGER.error("Config konnte nicht gelesen werden, benutze Standardwerte", e);
				config = new PopEffectsConfig();
			}
		}

		migrate();
		config.sanitize();
		save();
	}

	/**
	 * Zieht aeltere Config-Dateien nach.
	 *
	 * <p>Version 2: Farben standen vorher als Dezimalzahl in der Datei
	 * ({@code 16733525}), jetzt als {@code "#FF5555"}. Beim Einlesen einer
	 * alten Datei landet die Zahl als Ziffernfolge im Textfeld - die muessen
	 * wir als Dezimalzahl deuten, sonst wuerde sie faelschlich als Hex
	 * gelesen und die Farbe waere hinterher eine andere.
	 */
	private static void migrate() {
		if (config.configVersion < 2) {
			for (TriggerType type : TriggerType.values()) {
				EffectSettings settings = config.get(type);

				if (settings != null) {
					settings.colorStart = decimalToHex(settings.colorStart);
					settings.colorEnd = decimalToHex(settings.colorEnd);
				}
			}

			PopEffects.LOGGER.info("Config auf Version 2 gehoben: Farben stehen jetzt als Hex-Code in der Datei");
		}

		if (config.configVersion < 3) {
			// Beides ueberdeckt das Ausblenden: additiv steuert die Deckkraft
			// die Helligkeit statt der Durchsichtigkeit, und der Schein macht
			// die Form breiter. Wer den Neonlook mochte, schaltet die beiden
			// Schalter im Reiter "Farbe" wieder ein.
			for (TriggerType type : TriggerType.values()) {
				EffectSettings settings = config.get(type);

				if (settings != null) {
					settings.additive = false;
					settings.glow = false;
				}
			}

			PopEffects.LOGGER.info("Config auf Version 3 gehoben: Leuchten und Schein sind aus, "
					+ "damit das Ausblenden die Farbe durchsichtig macht");
		}

		config.configVersion = PopEffectsConfig.CURRENT_VERSION;
	}

	private static String decimalToHex(String raw) {
		if (raw == null) {
			return null;
		}

		try {
			return EffectSettings.formatColor(Integer.parseInt(raw.trim()));
		} catch (NumberFormatException e) {
			// Steht schon als Hex drin - dann ist nichts zu tun.
			return raw;
		}
	}

	/** Setzt alles auf Werkseinstellungen zurueck. */
	public static void reset() {
		config = new PopEffectsConfig();
		save();
	}

	/** Setzt nur einen einzelnen Effekt zurueck. */
	public static void reset(TriggerType type) {
		config.set(type, EffectSettings.defaultFor(type));
		save();
	}

	public static void save() {
		Path file = path();

		try {
			Path parent = file.getParent();

			if (parent != null) {
				Files.createDirectories(parent);
			}

			try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException e) {
			PopEffects.LOGGER.error("Config konnte nicht gespeichert werden", e);
		}
	}
}
