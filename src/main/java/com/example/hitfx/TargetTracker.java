package com.example.hitfx;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

/** Remembers who you're fighting and your combo / hit stats against them. */
public final class TargetTracker {
    public static int targetId = -1;
    public static int combo, hits, swings;
    private static long lastHitTick = -1000, lastAttackTick = -1000;

    public static void swing(LivingEntity t, long now) {
        if (t.getId() != targetId) { targetId = t.getId(); combo = 0; hits = 0; swings = 0; }
        swings++;
        lastAttackTick = now;
    }

    public static void landed(LivingEntity t, long now) {
        if (t.getId() != targetId) swing(t, now);
        combo = (now - lastHitTick <= 30) ? combo + 1 : 1;
        hits++;
        lastHitTick = now;
        lastAttackTick = now;
    }

    /** The current target, or null if you haven't attacked anyone for 5 seconds. */
    public static LivingEntity current(ClientWorld w, long now) {
        if (targetId < 0 || now - lastAttackTick > 100) return null;
        Entity e = w.getEntityById(targetId);
        return (e instanceof LivingEntity le && le.isAlive()) ? le : null;
    }
}
