package com.example.hitfx;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/** "Ink" burst at the point of impact: a cone away from the attacker plus droplets that fall. */
public final class HitSplash {
    public static void burst(ClientWorld world, PlayerEntity attacker, LivingEntity target) {
        if (!Config.on(Config.Feature.HIT_SPLASH)) return;
        Theme th = Config.theme();
        Vec3d origin = target.getPos().add(0, target.getHeight() * 0.6, 0);
        Vec3d dir = target.getPos().subtract(attacker.getPos()).multiply(1, 0, 1);
        dir = dir.lengthSquared() < 1e-4 ? new Vec3d(0, 0, 1) : dir.normalize();

        for (int i = 0; i < 20; i++) {
            double spread = 0.18;
            double vx = dir.x * 0.22 + (world.random.nextDouble() - 0.5) * spread;
            double vz = dir.z * 0.22 + (world.random.nextDouble() - 0.5) * spread;
            double vy = 0.04 + world.random.nextDouble() * 0.12;
            world.addParticle(i % 3 == 0 ? th.dust2(0.9f) : th.dust(1.3f), origin.x, origin.y, origin.z, vx, vy, vz);
        }
        // Heavy drops that drop straight down.
        for (int i = 0; i < 6; i++) {
            world.addParticle(th.dust(1.6f),
                    origin.x + (world.random.nextDouble() - 0.5) * 0.4, origin.y, origin.z + (world.random.nextDouble() - 0.5) * 0.4,
                    0, -0.06 - world.random.nextDouble() * 0.04, 0);
        }
    }
}
