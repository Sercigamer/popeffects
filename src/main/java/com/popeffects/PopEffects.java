package com.popeffects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.Identifier;

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
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
