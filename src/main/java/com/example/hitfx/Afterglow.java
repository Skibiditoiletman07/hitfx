package com.example.hitfx;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** Kill pillar: the fallen dissolve into a rising column of themed light, a sky beam and a ground ring. */
public final class Afterglow {
    private static final int LIFETIME = 50;

    private record Glow(Vec3d pos, long start) {}

    private static final List<Glow> ACTIVE = new ArrayList<>();
    private static final Set<Integer> SEEN_DEAD = new HashSet<>();

    public static void init() { ClientTickEvents.END_CLIENT_TICK.register(Afterglow::tick); }

    private static void tick(MinecraftClient mc) {
        ClientWorld world = mc.world;
        if (world == null) { ACTIVE.clear(); SEEN_DEAD.clear(); return; }
        if (!Config.on(Config.Feature.AFTERGLOW)) { ACTIVE.clear(); return; }
        Theme th = Config.theme();

        for (Entity e : world.getEntities()) {
            if (e instanceof PlayerEntity p && p != mc.player && p.getHealth() <= 0f && SEEN_DEAD.add(p.getId()))
                ACTIVE.add(new Glow(p.getPos(), world.getTime()));
        }
        SEEN_DEAD.removeIf(id -> !(world.getEntityById(id) instanceof LivingEntity le) || le.getHealth() > 0f);

        Iterator<Glow> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Glow g = it.next();
            int age = (int) (world.getTime() - g.start());
            if (age > LIFETIME) { it.remove(); continue; }
            float fade = 1f - age / (float) LIFETIME;

            int count = Math.max(1, (int) (14 * fade));
            for (int i = 0; i < count; i++) {
                double a = world.random.nextDouble() * Math.PI * 2;
                double r = world.random.nextDouble() * 0.35;
                double y = world.random.nextDouble() * 1.8;
                world.addParticle(world.random.nextBoolean() ? th.dust(1.1f) : th.dust2(0.8f),
                        g.pos().x + Math.cos(a) * r, g.pos().y + y, g.pos().z + Math.sin(a) * r,
                        0, 0.04 + 0.05 * world.random.nextDouble(), 0);
            }
            if (age < 30) {
                for (double y = 2; y < 2 + age * 1.2; y += 0.6)
                    world.addParticle(ParticleTypes.END_ROD, g.pos().x, g.pos().y + y, g.pos().z, 0, 0.01, 0);
            }
            if (age < 24) {
                double radius = 0.4 + age * 0.12;
                for (int i = 0; i < 24; i++) {
                    double a = i / 24.0 * Math.PI * 2;
                    world.addParticle(th.dust(1.0f),
                            g.pos().x + Math.cos(a) * radius, g.pos().y + 0.05, g.pos().z + Math.sin(a) * radius, 0, 0.005, 0);
                }
            }
        }
    }
}
