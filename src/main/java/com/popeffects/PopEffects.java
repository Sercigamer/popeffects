package com.popeffects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.Identifier;

/**
 * Gemeinsame Konstanten der Mod.
 */
public final class PopEffects {
	public static final String MOD_ID = "popeffects";
	public static final String MOD_NAME = "PopEffects";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	private PopEffects() {
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
