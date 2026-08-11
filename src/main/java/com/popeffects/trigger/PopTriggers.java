package com.popeffects.trigger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.popeffects.config.ConfigManager;
import com.popeffects.config.EffectSettings;
import com.popeffects.config.PopEffectsConfig;
import com.popeffects.config.TriggerType;
import com.popeffects.effect.EffectManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

/**
 * Hier laufen alle Ausloeser zusammen. Die Mixins melden nur rohe Ereignisse,
 * entschieden wird erst hier.
 *
 * <p>Wie viel Schaden jemand bekommen hat, verraet der Server nicht direkt.
 * Wir sehen aber, wie sich die Lebensanzeige aendert - die Differenz ist der
 * Schaden. Wer zugeschlagen hat, steht im Schadens-Paket, das kurz davor
 * ankommt; beides zusammen ergibt "wer hat wem wie viel gegeben".
 */
public final class PopTriggers {
	/**
	 * So lange gilt ein Schadens-Paket als passend zur naechsten Aenderung der
	 * Lebensanzeige. Beides kommt normal im selben Tick an, eine halbe Sekunde
	 * ist also grosszuegig.
	 */
	private static final long ATTRIBUTION_WINDOW_MILLIS = 500L;

	private static final Map<Integer, Attribution> ATTRIBUTIONS = new HashMap<>();

	/** Letzter bekannter Lebensstand von dir selbst, oder NaN am Anfang. */
	private static float lastSelfHealth = Float.NaN;

	private PopTriggers() {
	}

	private record Attribution(int causeId, long time) {
	}

	/** Beim Verlassen der Welt aufraeumen - IDs gelten danach nicht mehr. */
	public static void reset() {
		ATTRIBUTIONS.clear();
		lastSelfHealth = Float.NaN;
	}

	/** Aus {@code EntityDamageS2CPacket}: wer hat wen getroffen. */
	public static void onDamagePacket(int entityId, int causeId) {
		long now = System.currentTimeMillis();
		purge(now);
		ATTRIBUTIONS.put(entityId, new Attribution(causeId, now));
	}

	/** Aus dem Entity-Status 35: jemand hat ein Totem verbraucht. */
	public static void onTotemPop(Entity entity) {
		EffectManager.trigger(TriggerType.TOTEM_POP, entity, 0.0F);
	}

	/** Aus dem Entity-Status 3: jemand ist gestorben. */
	public static void onDeath(Entity entity) {
		if (entity instanceof LivingEntity) {
			EffectManager.trigger(TriggerType.KILL, entity, 0.0F);
		}
	}

	/**
	 * Die Lebensanzeige eines Lebewesens ist gefallen. Das ist unser Schaden.
	 */
	public static void onHealthDrop(LivingEntity entity, float amount) {
		MinecraftClient client = MinecraftClient.getInstance();

		// Der eigene Lebensstand kommt ueber ein eigenes Paket - sonst gaebe
		// es den Effekt doppelt.
		if (client.player != null && entity.getId() == client.player.getId()) {
			return;
		}

		PopEffectsConfig config = ConfigManager.get();
		EffectSettings settings = config.bigDamage;

		if (!settings.enabled || amount < settings.threshold) {
			return;
		}

		// Ein toedlicher Treffer soll den Kill-Effekt zeigen, nicht zusaetzlich
		// noch den Schadens-Effekt.
		if (entity.getHealth() <= 0.0F || entity.isDead()) {
			return;
		}

		// Zu einem echten Treffer gehoert immer ein Schadens-Paket. Fehlt es,
		// ist die Aenderung der Lebensanzeige etwas anderes: zum Beispiel ein
		// verletzter Gegner, der gerade in Sichtweite geraet - den legt der
		// Client erst mit vollen Herzen an, der echte Wert kommt einen Moment
		// spaeter nach. Ohne diese Pruefung sah das aus wie ein Treffer, und
		// es erschienen Ringe aus dem Nichts.
		if (!recentlyDamaged(entity.getId())) {
			return;
		}

		if (config.onlyOwnHits && !causedByLocalPlayer(entity.getId(), client)) {
			return;
		}

		EffectManager.trigger(TriggerType.BIG_DAMAGE, entity, amount);
	}

	/**
	 * Dein eigener Lebensstand aus {@code HealthUpdateS2CPacket}.
	 */
	public static void onSelfHealth(float health) {
		float previous = lastSelfHealth;
		lastSelfHealth = health;

		// Der allererste Wert nach dem Beitreten ist kein Schaden.
		if (Float.isNaN(previous)) {
			return;
		}

		float amount = previous - health;

		if (amount <= 0.0F) {
			return;
		}

		EffectSettings settings = ConfigManager.get().selfHurt;

		if (!settings.enabled || amount < settings.threshold) {
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();

		if (client.player != null) {
			EffectManager.trigger(TriggerType.SELF_HURT, client.player, amount);
		}
	}

	/** Kam fuer dieses Ziel gerade eben ein Schadens-Paket herein? */
	private static boolean recentlyDamaged(int entityId) {
		Attribution attribution = ATTRIBUTIONS.get(entityId);

		return attribution != null
				&& System.currentTimeMillis() - attribution.time() <= ATTRIBUTION_WINDOW_MILLIS;
	}

	private static boolean causedByLocalPlayer(int entityId, MinecraftClient client) {
		if (client.player == null) {
			return false;
		}

		Attribution attribution = ATTRIBUTIONS.get(entityId);

		if (attribution == null) {
			return false;
		}

		if (System.currentTimeMillis() - attribution.time() > ATTRIBUTION_WINDOW_MILLIS) {
			return false;
		}

		return attribution.causeId() == client.player.getId();
	}

	private static void purge(long now) {
		Iterator<Attribution> iterator = ATTRIBUTIONS.values().iterator();

		while (iterator.hasNext()) {
			if (now - iterator.next().time() > ATTRIBUTION_WINDOW_MILLIS) {
				iterator.remove();
			}
		}
	}
}
