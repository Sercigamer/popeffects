package com.popeffects.effect;

import com.popeffects.config.EffectSettings;
import com.popeffects.config.TriggerType;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * Ein Effekt, der gerade laeuft. Die Einstellungen sind eine Kopie: dreht man
 * im Menue an den Reglern, springt eine schon laufende Animation nicht.
 */
public final class ActiveEffect {
	public final TriggerType trigger;
	public final EffectSettings settings;
	public final float scale;
	public final float rotationOffset;

	private final int entityId;

	private Vec3d position;
	private int age;

	public ActiveEffect(TriggerType trigger, EffectSettings settings, Vec3d position, int entityId, float scale,
			float rotationOffset) {
		this.trigger = trigger;
		this.settings = settings;
		this.position = position;
		this.entityId = entityId;
		this.scale = scale;
		this.rotationOffset = rotationOffset;
	}

	public Vec3d position() {
		return position;
	}

	public int age() {
		return age;
	}

	public boolean isFinished() {
		return age >= settings.durationTicks;
	}

	/**
	 * Fortschritt von 0 bis 1. {@code tickDelta} ist der Bruchteil zwischen
	 * zwei Ticks, sonst wuerde der Effekt mit 20 statt mit voller Bildrate
	 * laufen.
	 */
	public float progress(float tickDelta) {
		float value = (age + tickDelta) / settings.durationTicks;
		return Math.min(1.0F, Math.max(0.0F, value));
	}

	/**
	 * Ein Tick weiter. Haengt der Effekt am Ziel, wandert er mit - sonst
	 * bleibt er da, wo er ausgeloest wurde.
	 */
	public void tick(Entity tracked) {
		age++;

		if (settings.followEntity && tracked != null && tracked.isAlive()) {
			position = new Vec3d(tracked.getX(), tracked.getY(), tracked.getZ());
		}
	}

	/**
	 * Position fuer diesen Frame. Zwischen zwei Ticks wird interpoliert, damit
	 * der Effekt an einem laufenden Gegner nicht ruckelt.
	 */
	public Vec3d renderPosition(Entity tracked, float tickDelta) {
		if (settings.followEntity && tracked != null && tracked.isAlive()) {
			return tracked.getLerpedPos(tickDelta);
		}

		return position;
	}

	public int entityId() {
		return entityId;
	}
}
