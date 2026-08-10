package com.popeffects.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.popeffects.trigger.PopTriggers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;

/**
 * Hoert mit, was der Server ueber Totems, Treffer und Tode erzaehlt.
 *
 * <p>Alle Einhaengepunkte sitzen am Ende der Vanilla-Methode: davor laeuft der
 * Code noch im Netzwerk-Thread, und von dort darf man nichts anfassen, was zum
 * Rendern gehoert.
 */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Inject(method = "onEntityStatus", at = @At("TAIL"))
	private void popeffects$onEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();

		if (client.world == null) {
			return;
		}

		Entity entity = packet.getEntity(client.world);

		if (entity == null) {
			return;
		}

		byte status = packet.getStatus();

		if (status == EntityStatuses.USE_TOTEM_OF_UNDYING) {
			PopTriggers.onTotemPop(entity);
		} else if (status == EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES) {
			PopTriggers.onDeath(entity);
		}
	}

	@Inject(method = "onEntityDamage", at = @At("TAIL"))
	private void popeffects$onEntityDamage(EntityDamageS2CPacket packet, CallbackInfo ci) {
		PopTriggers.onDamagePacket(packet.entityId(), packet.sourceCauseId());
	}

	@Inject(method = "onHealthUpdate", at = @At("TAIL"))
	private void popeffects$onHealthUpdate(HealthUpdateS2CPacket packet, CallbackInfo ci) {
		PopTriggers.onSelfHealth(packet.getHealth());
	}
}
