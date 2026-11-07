package com.example.hitfx;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Per-entity ripple timers plus the player's ripple cooldown. */
public final class RippleManager {
    public static final int DURATION_TICKS = 14;
    public static final int COOLDOWN_TICKS = 100; // 5s

    private static final Map<Integer, Long> ACTIVE = new HashMap<>();
    private static long cooldownEnd = 0;

    public static boolean ready(long now) { return now >= cooldownEnd; }
    public static float cooldownSecondsLeft(long now) { return Math.max(0, (cooldownEnd - now) / 20f); }

    public static void trigger(int entityId, long now) {
        ACTIVE.put(entityId, now);
        cooldownEnd = now + COOLDOWN_TICKS;
    }

    /** 0..1 progress of the ripple on this entity, or -1 when it isn't rippling. */
    public static float progress(int entityId, float now) {
        Long start = ACTIVE.get(entityId);
        if (start == null) return -1;
        float p = (now - start) / DURATION_TICKS;
        return p > 1 ? -1 : p;
    }

    public static void cleanup(long now) {
        Iterator<Map.Entry<Integer, Long>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) if (now - it.next().getValue() > DURATION_TICKS) it.remove();
    }
}
