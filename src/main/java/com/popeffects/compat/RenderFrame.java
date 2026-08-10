package com.popeffects.compat;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

/**
 * Alles, was der Effekt-Renderer aus einem Welt-Render-Durchgang braucht.
 *
 * <p>Der Weg zu diesen drei Dingen ist je Minecraft-Version verschieden -
 * mal heisst der Ereignis-Typ anders, mal liegt er in einem anderen Paket.
 * Der Renderer selbst soll davon nichts wissen, deshalb reicht ihm
 * {@link WorldRenderHook} dieses schlichte Buendel herein.
 *
 * @param matrices  Matrizen-Stapel des Welt-Renderers, Ursprung ist die Kamera
 * @param consumers Puffer, in die gezeichnet wird
 * @param cameraPos Weltposition der Kamera
 */
public record RenderFrame(MatrixStack matrices, VertexConsumerProvider consumers, Vec3d cameraPos) {
}
