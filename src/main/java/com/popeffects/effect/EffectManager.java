package com.popeffects.effect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.popeffects.PopEffects;
import com.popeffects.config.ConfigManager;
import com.popeffects.config.EffectSettings;
import com.popeffects.config.PopEffectsConfig;
import com.popeffects.config.TriggerType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/**
 * Haelt die laufenden Effekte, startet neue und raeumt fertige weg.
 *
 * <p>Alles hier laeuft im Client-Thread: die Trigger kommen aus dem
 * Netzwerk-Handler, getickt wird im Client-Tick, gezeichnet im Renderer.
 */
public final class EffectManager {
	private static final List<ActiveEffect> ACTIVE = new ArrayList<>();

	private EffectManager() {
	}

	public static List<ActiveEffect> active() {
		return Collections.unmodifiableList(ACTIVE);
	}

	public static int count() {
		return ACTIVE.size();
	}

	public static void clear() {
		ACTIVE.clear();
	}

	/**
	 * Startet einen Effekt an einem Ziel.
	 *
	 * @param amount Schaden in halben Herzen, oder 0 wenn es nicht um Schaden
	 *               geht. Steuert die Groesse, wenn das eingeschaltet ist.
	 */
	public static void trigger(TriggerType type, Entity target, float amount) {
		if (target == null) {
			return;
		}

		PopEffectsConfig config = ConfigManager.get();

		if (!config.enabled) {
			return;
		}

		EffectSettings settings = config.get(type);

		if (settings == null || !settings.enabled) {
			return;
		}

		if (!passesTargetFilter(config, target)) {
			return;
		}

		Minecraft client = Minecraft.getInstance();

		if (client.player == null || client.level == null) {
			return;
		}

		if (target.distanceToSqr(client.player) > config.maxDistance * config.maxDistance) {
			return;
		}

		// Bei einem Grossangriff koennen sehr viele Trigger auf einmal kommen.
		// Dann fliegt der aelteste Effekt raus, statt dass die Bildrate faellt.
		while (ACTIVE.size() >= config.maxActiveEffects) {
			ACTIVE.remove(0);
		}

		float scale = 1.0F;

		if (settings.scaleWithDamage && amount > 0.0F && settings.threshold > 0.0F) {
			scale = Math.min(3.0F, Math.max(1.0F, amount / settings.threshold));
		}

		Vec3 position = new Vec3(target.getX(), target.getY(), target.getZ());
		float rotationOffset = client.level.getRandom().nextFloat() * 360.0F;

		ACTIVE.add(new ActiveEffect(type, settings.copy(), position, target.getId(), scale, rotationOffset));

		playSound(client, settings, position);
		spawnParticles(client.level, settings, position, scale);

		if (type == TriggerType.TOTEM_POP && config.popCounter) {
			PopCounter.record(target);
		}
	}

	/**
	 * Effekt ohne echten Ausloeser - dahinter steckt der Vorschau-Knopf im
	 * Menue. Filter und Reichweite gelten hier bewusst nicht.
	 *
	 * <p>Der Effekt landet ein paar Bloecke vor dir statt auf deinen Fuessen,
	 * sonst wuerde man in der Ich-Perspektive kaum etwas davon sehen. Sieben
	 * Bloecke sind weit genug, dass auch Saeule und Kugel komplett ins Bild
	 * passen, statt dass man mitten drin steht.
	 */
	public static void preview(TriggerType type, EffectSettings settings) {
		Minecraft client = Minecraft.getInstance();

		if (client.player == null || client.level == null) {
			return;
		}

		Vec3 look = client.player.getLookAngle();
		Vec3 position = new Vec3(client.player.getX() + look.x * 7.0D, client.player.getY(),
				client.player.getZ() + look.z * 7.0D);
		float rotationOffset = client.level.getRandom().nextFloat() * 360.0F;

		// Entity-ID -1 gibt es nicht, dadurch bleibt der Effekt liegen, auch
		// wenn "folgt dem Ziel" an ist.
		ACTIVE.add(new ActiveEffect(type, settings.copy(), position, -1, 1.0F, rotationOffset));

		playSound(client, settings, position);
		spawnParticles(client.level, settings, position, 1.0F);
	}

	public static void tick(Minecraft client) {
		if (ACTIVE.isEmpty()) {
			return;
		}

		ClientLevel world = client.level;

		if (world == null) {
			ACTIVE.clear();
			return;
		}

		Iterator<ActiveEffect> iterator = ACTIVE.iterator();

		while (iterator.hasNext()) {
			ActiveEffect effect = iterator.next();
			effect.tick(world.getEntity(effect.entityId()));

			if (effect.isFinished()) {
				iterator.remove();
			}
		}
	}

	private static boolean passesTargetFilter(PopEffectsConfig config, Entity target) {
		Minecraft client = Minecraft.getInstance();
		boolean self = client.player != null && target.getId() == client.player.getId();

		if (self) {
			return config.showOnSelf;
		}

		if (target instanceof Player) {
			return config.showOnPlayers;
		}

		return config.showOnMobs;
	}

	private static void playSound(Minecraft client, EffectSettings settings, Vec3 position) {
		if (!settings.sound || settings.soundVolume <= 0.0F || client.level == null) {
			return;
		}

		Identifier id = Identifier.tryParse(settings.soundId);

		if (id == null) {
			return;
		}

		SoundEvent event = BuiltInRegistries.SOUND_EVENT.getValue(id);

		if (event == null) {
			// Unbekannte IDs sind erlaubt (Resourcepacks), aber ohne
			// Registry-Eintrag laesst sich nichts abspielen.
			PopEffects.LOGGER.warn("Sound {} gibt es nicht", settings.soundId);
			return;
		}

		client.getSoundManager().play(new SimpleSoundInstance(event, SoundSource.PLAYERS,
				settings.soundVolume, settings.soundPitch, client.level.getRandom(),
				position.x, position.y + 1.0D, position.z));
	}

	private static void spawnParticles(ClientLevel world, EffectSettings settings, Vec3 position, float scale) {
		if (!settings.particles || settings.particleCount <= 0) {
			return;
		}

		Identifier id = Identifier.tryParse(settings.particleId);

		if (id == null) {
			return;
		}

		ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.getValue(id);

		// Partikel mit Extra-Daten (Block-, Staub- oder Item-Partikel) lassen
		// sich nicht ohne Zusatzangaben erzeugen - die lassen wir hier weg.
		if (!(type instanceof ParticleOptions effect)) {
			PopEffects.LOGGER.warn("Partikel {} laesst sich nicht ohne Zusatzdaten erzeugen", settings.particleId);
			return;
		}

		float radius = settings.endRadius * scale;

		for (int i = 0; i < settings.particleCount; i++) {
			double angle = (Math.PI * 2.0D * i) / settings.particleCount;
			double x = position.x + Math.cos(angle) * radius;
			double z = position.z + Math.sin(angle) * radius;
			double y = position.y + settings.yOffset + 0.1D;

			world.addParticle(effect, x, y, z, Math.cos(angle) * 0.08D, 0.06D, Math.sin(angle) * 0.08D);
		}
	}
}
