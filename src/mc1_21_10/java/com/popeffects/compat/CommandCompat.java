package com.popeffects.compat;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Fassung fuer 1.21.10 - gleicher Methodenname wie in 1.21.11.
 * Nur der Selbsttest braucht das.
 */
public final class CommandCompat {
	private CommandCompat() {
	}

	public static void run(MinecraftServer server, ServerCommandSource source, String command) {
		server.getCommandManager().parseAndExecute(source, command);
	}
}
