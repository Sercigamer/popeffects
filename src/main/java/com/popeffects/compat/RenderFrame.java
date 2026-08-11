package com.popeffects.compat;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;

/**
 * Alles, was der Effekt-Renderer aus einem Welt-Render-Durchgang braucht.
 *
 * <p>In 26.x schreibt man nicht mehr selbst in einen Puffer, sondern reicht
 * Zeichenbefehle bei einem {@link SubmitNodeCollector} ein. Der Renderer
 * bekommt deshalb den Sammler statt eines Puffer-Anbieters.
 *
 * @param poseStack Matrizen-Stapel des Welt-Renderers, Ursprung ist die Kamera
 * @param collector nimmt die Zeichenbefehle entgegen
 * @param cameraPos Weltposition der Kamera
 */
public record RenderFrame(PoseStack poseStack, SubmitNodeCollector collector, Vec3 cameraPos) {
}
