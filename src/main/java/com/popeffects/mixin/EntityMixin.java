package com.popeffects.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.popeffects.trigger.PopTriggers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;

/**
 * Misst, wie viel Leben ein Gegner verliert.
 *
 * <p>Der Server schickt keinen Schadenswert mit, nur den neuen Lebensstand.
 * Also merken wir uns den alten Wert direkt vor dem Einspielen des Pakets und
 * ziehen danach ab - die Differenz ist der Schaden, den wir fuer die Groesse
 * des Effekts brauchen.
 */
@Mixin(Entity.class)
public abstract class EntityMixin {
	@Unique
	private float popeffects$healthBefore;

	@Inject(method = "onDataTrackerUpdate", at = @At("HEAD"))
	private void popeffects$rememberHealth(List<DataTracker.SerializedEntry<?>> entries, CallbackInfo ci) {
		if ((Object) this instanceof LivingEntity living) {
			popeffects$healthBefore = living.getHealth();
		}
	}

	@Inject(method = "onDataTrackerUpdate", at = @At("TAIL"))
	private void popeffects$compareHealth(List<DataTracker.SerializedEntry<?>> entries, CallbackInfo ci) {
		if ((Object) this instanceof LivingEntity living) {
			float lost = popeffects$healthBefore - living.getHealth();

			if (lost > 0.0F) {
				PopTriggers.onHealthDrop(living, lost);
			}
		}
	}
}
