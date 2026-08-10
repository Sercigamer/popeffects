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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

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

		MinecraftClient client = MinecraftClient.getInstance();

		if (client.player == null || client.world == null) {
			return;
		}

		if (target.squaredDistanceTo(client.player) > config.maxDistance * config.maxDistance) {
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

		Vec3d position = new Vec3d(target.getX(), target.getY(), target.getZ());
		float rotationOffset = client.world.getRandom().nextFloat() * 360.0F;

		ACTIVE.add(new ActiveEffect(type, settings.copy(), position, target.getId(), scale, rotationOffset));

		playSound(client, settings, position);
		spawnParticles(client.world, settings, position, scale);

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
		MinecraftClient client = MinecraftClient.getInstance();

		if (client.player == null || client.world == null) {
			return;
		}

		Vec3d look = client.player.getRotationVector();
		Vec3d position = new Vec3d(client.player.getX() + look.x * 7.0D, client.player.getY(),
				client.player.getZ() + look.z * 7.0D);
		float rotationOffset = client.world.getRandom().nextFloat() * 360.0F;

		// Entity-ID -1 gibt es nicht, dadurch bleibt der Effekt liegen, auch
		// wenn "folgt dem Ziel" an ist.
		ACTIVE.add(new ActiveEffect(type, settings.copy(), position, -1, 1.0F, rotationOffset));

		playSound(client, settings, position);
		spawnParticles(client.world, settings, position, 1.0F);
	}

	public static void tick(MinecraftClient client) {
		if (ACTIVE.isEmpty()) {
			return;
		}

		ClientWorld world = client.world;

		if (world == null) {
			ACTIVE.clear();
			return;
		}

		Iterator<ActiveEffect> iterator = ACTIVE.iterator();

		while (iterator.hasNext()) {
			ActiveEffect effect = iterator.next();
			effect.tick(world.getEntityById(effect.entityId()));

			if (effect.isFinished()) {
				iterator.remove();
			}
		}
	}

	private static boolean passesTargetFilter(PopEffectsConfig config, Entity target) {
		MinecraftClient client = MinecraftClient.getInstance();
		boolean self = client.player != null && target.getId() == client.player.getId();

		if (self) {
			return config.showOnSelf;
		}

		if (target instanceof PlayerEntity) {
			return config.showOnPlayers;
		}

		return config.showOnMobs;
	}

	private static void playSound(MinecraftClient client, EffectSettings settings, Vec3d position) {
		if (!settings.sound || settings.soundVolume <= 0.0F || client.world == null) {
			return;
		}

		Identifier id = Identifier.tryParse(settings.soundId);

		if (id == null) {
			return;
		}

		SoundEvent event = Registries.SOUND_EVENT.get(id);

		if (event == null) {
			// Unbekannte IDs sind erlaubt (Resourcepacks), aber ohne
			// Registry-Eintrag laesst sich nichts abspielen.
			PopEffects.LOGGER.warn("Sound {} gibt es nicht", settings.soundId);
			return;
		}

		client.getSoundManager().play(new PositionedSoundInstance(event, SoundCategory.PLAYERS,
				settings.soundVolume, settings.soundPitch, client.world.getRandom(),
				position.x, position.y + 1.0D, position.z));
	}

	private static void spawnParticles(ClientWorld world, EffectSettings settings, Vec3d position, float scale) {
		if (!settings.particles || settings.particleCount <= 0) {
			return;
		}

		Identifier id = Identifier.tryParse(settings.particleId);

		if (id == null) {
			return;
		}

		ParticleType<?> type = Registries.PARTICLE_TYPE.get(id);

		// Partikel mit Extra-Daten (Block-, Staub- oder Item-Partikel) lassen
		// sich nicht ohne Zusatzangaben erzeugen - die lassen wir hier weg.
		if (!(type instanceof ParticleEffect effect)) {
			PopEffects.LOGGER.warn("Partikel {} laesst sich nicht ohne Zusatzdaten erzeugen", settings.particleId);
			return;
		}

		float radius = settings.endRadius * scale;

		for (int i = 0; i < settings.particleCount; i++) {
			double angle = (Math.PI * 2.0D * i) / settings.particleCount;
			double x = position.x + Math.cos(angle) * radius;
			double z = position.z + Math.sin(angle) * radius;
			double y = position.y + settings.yOffset + 0.1D;

			world.addParticleClient(effect, x, y, z, Math.cos(angle) * 0.08D, 0.06D, Math.sin(angle) * 0.08D);
		}
	}
}
