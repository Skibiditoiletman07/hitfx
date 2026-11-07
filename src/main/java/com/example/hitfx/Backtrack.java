package com.example.hitfx;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Backtrack ghost: a glowing outline where your target most likely is on the server right now.
 * Approximation: current position pushed forward along their velocity by (your ping + interpolation delay).
 */
public final class Backtrack {
    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx -> {
            if (!Config.on(Config.Feature.BACKTRACK)) return;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world == null || mc.player == null || ctx.consumers() == null) return;

            LivingEntity t = TargetTracker.current(mc.world, mc.world.getTime());
            if (t == null) return;

            Vec3d vel = t.getPos().subtract(t.prevX, t.prevY, t.prevZ);          // blocks per tick
            double ping = 0;
            if (mc.getNetworkHandler() != null) {
                PlayerListEntry me = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                if (me != null) ping = me.getLatency();
            }
            double ticksAhead = ping / 50.0 + 2.0;
            Vec3d offset = vel.multiply(ticksAhead);
            if (offset.length() < 0.25) return; // standing still: ghost would just overlap them

            Vec3d cam = ctx.camera().getPos();
            Box box = t.getBoundingBox().offset(offset).offset(-cam.x, -cam.y, -cam.z);
            MatrixStack m = ctx.matrixStack();
            VertexConsumer lines = ctx.consumers().getBuffer(RenderLayer.getLines());
            Theme th = Config.theme();
            float r = th.r / 255f, g = th.g / 255f, b = th.b / 255f;

            WorldRenderer.drawBox(m, lines, box, r, g, b, 1.0f);
            WorldRenderer.drawBox(m, lines, box.expand(0.04), th.r2 / 255f, th.g2 / 255f, th.b2 / 255f, 0.45f);
        });
    }
}
