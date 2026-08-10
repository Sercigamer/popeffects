package com.popeffects.client;

import com.popeffects.PopEffects;
import com.popeffects.config.TriggerType;
import com.popeffects.effect.ActiveEffect;
import com.popeffects.effect.EffectManager;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Prueft, ob die Ausloeser im Spiel wirklich feuern.
 *
 * <p>Der Renderer laesst sich bequem ueber {@link SelfTest} pruefen, die
 * Ausloeser nicht: dafuer muss tatsaechlich jemand ein Totem verbrauchen,
 * Schaden fressen oder sterben. Im Einzelspieler geht das - dieser Test setzt
 * ueber den eingebauten Server echte Befehle ab und schaut danach nach, ob ein
 * passender Effekt entstanden ist.
 *
 * <p>Damit ist der Weg vom Server-Paket ueber die Mixins bis zum fertigen
 * Effekt einmal komplett abgedeckt. Laeuft nur mit
 * {@code -Dpopeffects.triggertest=true}.
 */
public final class TriggerSelfTest {
	private static final String PROPERTY = "popeffects.triggertest";

	/** Wartezeit nach dem Beitreten, damit die Welt geladen ist. */
	private static final int WARMUP_TICKS = 60;

	/** Ticks zwischen Befehl und Nachschauen. */
	private static final int SETTLE_TICKS = 6;

	private enum Step {
		/** Ueberleben statt Kreativ - sonst prallt jeder Schaden ab. */
		ENSURE_SURVIVAL,
		/** Totem in die zweite Hand legen. */
		ARM_TOTEM,
		/** Toedlichen Schaden auf den Spieler - das Totem muss poppen. */
		POP_TOTEM,
		CHECK_TOTEM,
		/** Zombie herbeirufen, der als Opfer dient. */
		SUMMON_ZOMBIE,
		/** Kraeftiger Treffer auf den Zombie. */
		HURT_ZOMBIE,
		CHECK_DAMAGE,
		KILL_ZOMBIE,
		CHECK_KILL,
		DONE
	}

	private static int ticksUntilNextStep = -1;
	private static Step step = Step.ARM_TOTEM;
	private static int failures;

	private TriggerSelfTest() {
	}

	public static void registerIfRequested() {
		if (!Boolean.getBoolean(PROPERTY)) {
			return;
		}

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ticksUntilNextStep = WARMUP_TICKS;
			step = Step.ENSURE_SURVIVAL;
			failures = 0;
		});

		ClientTickEvents.END_CLIENT_TICK.register(TriggerSelfTest::tick);
		PopEffects.LOGGER.info("Ausloeser-Test aktiv - prueft Totem, Schaden und Kill mit echten Ereignissen");
	}

	private static void tick(MinecraftClient client) {
		if (ticksUntilNextStep < 0 || client.player == null || client.getServer() == null) {
			return;
		}

		if (--ticksUntilNextStep > 0) {
			return;
		}

		switch (step) {
			case ENSURE_SURVIVAL -> {
				run(client, "gamemode survival @s");
				next(Step.ARM_TOTEM, SETTLE_TICKS);
			}
			case ARM_TOTEM -> {
				// Vorher aufraeumen, damit ein Effekt von vorhin nicht als
				// Treffer durchgeht.
				EffectManager.clear();
				run(client, "item replace entity @s weapon.offhand with minecraft:totem_of_undying");
				next(Step.POP_TOTEM, SETTLE_TICKS);
			}
			case POP_TOTEM -> {
				run(client, "damage @s 1000 minecraft:generic");
				next(Step.CHECK_TOTEM, SETTLE_TICKS);
			}
			case CHECK_TOTEM -> {
				check(TriggerType.TOTEM_POP);
				EffectManager.clear();
				next(Step.SUMMON_ZOMBIE, 2);
			}
			case SUMMON_ZOMBIE -> {
				run(client, "summon minecraft:zombie ~ ~ ~4 {NoAI:1b,PersistenceRequired:1b}");
				next(Step.HURT_ZOMBIE, SETTLE_TICKS);
			}
			case HURT_ZOMBIE -> {
				run(client, "damage @e[type=zombie,limit=1,sort=nearest] 12 minecraft:generic");
				next(Step.CHECK_DAMAGE, SETTLE_TICKS);
			}
			case CHECK_DAMAGE -> {
				check(TriggerType.BIG_DAMAGE);
				EffectManager.clear();
				next(Step.KILL_ZOMBIE, 2);
			}
			case KILL_ZOMBIE -> {
				run(client, "kill @e[type=zombie]");
				next(Step.CHECK_KILL, SETTLE_TICKS);
			}
			case CHECK_KILL -> {
				check(TriggerType.KILL);
				next(Step.DONE, 2);
			}
			case DONE -> {
				ticksUntilNextStep = -1;

				if (failures == 0) {
					PopEffects.LOGGER.info("Ausloeser-Test OK - Totem, Schaden und Kill haben alle ausgeloest");
				} else {
					PopEffects.LOGGER.error("Ausloeser-Test FEHLGESCHLAGEN - {} von 3 haben nicht ausgeloest",
							failures);
				}

				client.scheduleStop();
			}
			default -> ticksUntilNextStep = -1;
		}
	}

	private static void next(Step nextStep, int delay) {
		step = nextStep;
		ticksUntilNextStep = delay;
	}

	private static void check(TriggerType expected) {
		for (ActiveEffect effect : EffectManager.active()) {
			if (effect.trigger == expected) {
				PopEffects.LOGGER.info("Ausloeser {}: ausgeloest", expected);
				return;
			}
		}

		failures++;
		PopEffects.LOGGER.error("Ausloeser {}: KEIN Effekt entstanden", expected);
	}

	/**
	 * Setzt einen Befehl auf dem eingebauten Server ab.
	 *
	 * <p>Die Quelle ist die Server-Konsole - die darf alles, unabhaengig
	 * davon, ob in der Welt Cheats erlaubt sind. Angebunden an den Spieler,
	 * damit {@code @s} und {@code ~ ~ ~} das Erwartete treffen.
	 */
	private static void run(MinecraftClient client, String command) {
		MinecraftServer server = client.getServer();

		if (server == null) {
			return;
		}

		// Befehle gehoeren auf den Server-Thread, nicht in den Client-Tick.
		server.execute(() -> {
			ServerPlayerEntity player = server.getPlayerManager().getPlayerList().isEmpty()
					? null
					: server.getPlayerManager().getPlayerList().get(0);

			if (player == null) {
				return;
			}

			// Die Welt bleibt die der Konsole, also die Oberwelt - genau dort
			// steht der Spieler im Test auch. Position und Entity setzen wir
			// dagegen, damit "@s" und "~ ~ ~" stimmen.
			// Bewusst nicht stumm: schlaegt ein Befehl fehl, soll der Grund im
			// Log stehen. Ein stiller Test, der aus dem falschen Grund
			// durchfaellt, ist schlimmer als gar keiner.
			ServerCommandSource source = server.getCommandSource()
					.withEntity(player)
					.withPosition(new Vec3d(player.getX(), player.getY(), player.getZ()));

			server.getCommandManager().parseAndExecute(source, command);
		});
	}
}
