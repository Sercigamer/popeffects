package com.popeffects.compat;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Fuehrt einen Befehl auf dem eingebauten Server aus.
 *
 * <p>Fassung fuer 1.21.11, wo die Methode {@code parseAndExecute} heisst.
 * Nur der Selbsttest braucht das.
 */
public final class CommandCompat {
	private CommandCompat() {
	}

	public static void run(MinecraftServer server, ServerCommandSource source, String command) {
		server.getCommandManager().parseAndExecute(source, command);
	}
}
