package com.example.hitfx;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

/** Axe hits a raised shield: shatter burst + a "SHIELD DOWN" countdown under the Target HUD. */
public final class ShieldBreaker {
    private static final int DISABLE_TICKS = 100; // vanilla axe shield-disable time (5s)
    private static final Map<Integer, Long> DOWN_UNTIL = new HashMap<>();

    /** Called on your attack. Returns true if a shield break was triggered. */
    public static boolean tryBreak(ClientWorld world, PlayerEntity attacker, LivingEntity target) {
        if (!Config.on(Config.Feature.SHIELD_BREAKER)) return false;
        if (!(attacker.getMainHandStack().getItem() instanceof AxeItem)) return false;
        if (!(target instanceof PlayerEntity victim) || !victim.isBlocking()) return false;
        if (attacker.getAttackCooldownProgress(0.5f) < 0.9f) return false; // weak hits don't disable shields

        long now = world.getTime();
        DOWN_UNTIL.put(target.getId(), now + DISABLE_TICKS);

        Vec3d c = target.getPos().add(0, target.getHeight() * 0.65, 0);
        Theme th = Config.theme();
        for (int i = 0; i < 30; i++) {
            double a = world.random.nextDouble() * Math.PI * 2;
            double up = world.random.nextDouble() * 0.2;
            double sp = 0.12 + world.random.nextDouble() * 0.18;
            world.addParticle(i % 3 == 0 ? ParticleTypes.CRIT : (i % 2 == 0 ? th.dust(1.2f) : th.dust2(0.9f)),
                    c.x, c.y, c.z, Math.cos(a) * sp, up, Math.sin(a) * sp);
        }
        world.playSound(c.x, c.y, c.z, SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f, false);
        return true;
    }

    static void drawPill(DrawContext ctx, MinecraftClient mc, LivingEntity t, int x, int y, int w, long now) {
        if (!Config.on(Config.Feature.SHIELD_BREAKER)) return;
        Long end = DOWN_UNTIL.get(t.getId());
        if (end == null || end <= now) return;
        Theme th = Config.theme();
        String txt = String.format("SHIELD DOWN  %.1fs", (end - now) / 20f);
        ctx.fill(x, y, x + w, y + 14, 0xB0101018);
        ctx.fill(x, y, x + 2, y + 14, th.argb(255));
        ctx.drawText(mc.textRenderer, txt, x + 7, y + 3, th.argb(255), true);
    }
}
