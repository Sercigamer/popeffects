package com.popeffects.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.popeffects.trigger.PopTriggers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;

/**
 * Hoert mit, was der Server ueber Totems, Treffer und Tode erzaehlt.
 *
 * <p>Alle Einhaengepunkte sitzen am Ende der Vanilla-Methode: davor laeuft der
 * Code noch im Netzwerk-Thread, und von dort darf man nichts anfassen, was zum
 * Rendern gehoert.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Inject(method = "onEntityStatus", at = @At("TAIL"))
	private void popeffects$onEntityStatus(ClientboundEntityEventPacket packet, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();

		if (client.level == null) {
			return;
		}

		Entity entity = packet.getEntity(client.level);

		if (entity == null) {
			return;
		}

		byte status = packet.getEventId();

		if (status == EntityEvent.PROTECTED_FROM_DEATH) {
			PopTriggers.onTotemPop(entity);
		} else if (status == EntityEvent.DEATH) {
			PopTriggers.onDeath(entity);
		}
	}

	@Inject(method = "onEntityDamage", at = @At("TAIL"))
	private void popeffects$onEntityDamage(ClientboundDamageEventPacket packet, CallbackInfo ci) {
		PopTriggers.onDamagePacket(packet.entityId(), packet.sourceCauseId());
	}

	@Inject(method = "onHealthUpdate", at = @At("TAIL"))
	private void popeffects$onHealthUpdate(ClientboundSetHealthPacket packet, CallbackInfo ci) {
		PopTriggers.onSelfHealth(packet.getHealth());
	}
}
