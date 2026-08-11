package com.popeffects.compat;

import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;

/**
 * Fuehrt einen Befehl auf dem eingebauten Server aus.
 *
 * <p>Fassung fuer 1.21.11, wo die Methode {@code parseAndExecute} heisst.
 * Nur der Selbsttest braucht das.
 */
public final class CommandCompat {
	private CommandCompat() {
	}

	public static void run(MinecraftServer server, CommandSourceStack source, String command) {
		server.getCommands().performPrefixedCommand(source, command);
	}
}
