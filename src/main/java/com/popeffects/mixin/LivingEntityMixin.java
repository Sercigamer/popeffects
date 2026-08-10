package com.popeffects.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.popeffects.trigger.PopTriggers;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;

/**
 * Misst, wie viel Leben ein Gegner verliert.
 *
 * <p>Der Server schickt keinen Schadenswert mit, nur den neuen Lebensstand.
 * Wir merken uns deshalb bei jeder Aenderung den zuletzt gesehenen Wert und
 * ziehen beim naechsten Mal ab - die Differenz ist der Schaden.
 *
 * <p>Der Einhaengepunkt ist mit Bedacht gewaehlt: {@code onDataTrackerUpdate}
 * liegt zwar naeher am Paket, wird aber erst aufgerufen, wenn alle neuen Werte
 * schon geschrieben sind. Dort ist der alte Lebensstand also nicht mehr zu
 * holen, und die Differenz waere immer null.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	/** Zuletzt gesehener Lebensstand, {@code NaN} solange es keinen gibt. */
	@Unique
	private float popeffects$knownHealth = Float.NaN;

	@Inject(method = "onTrackedDataSet", at = @At("TAIL"))
	private void popeffects$watchHealth(TrackedData<?> data, CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;

		float now = self.getHealth();
		float before = popeffects$knownHealth;
		popeffects$knownHealth = now;

		// Der erste Wert ist der Ausgangspunkt, kein Schaden.
		if (Float.isNaN(before) || now >= before) {
			return;
		}

		PopTriggers.onHealthDrop(self, before - now);
	}
}
