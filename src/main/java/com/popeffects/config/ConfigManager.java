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

		config.configVersion = PopEffectsConfig.CURRENT_VERSION;
		config.sanitize();
		save();
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
