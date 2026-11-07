package com.example.hitfx;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

/** Critical hits throw a burst of sparkling stars outward in every direction. */
public final class CritStars {
    public static void burst(ClientWorld world, LivingEntity target) {
        if (!Config.on(Config.Feature.CRIT_STARS)) return;
        Theme th = Config.theme();
        Vec3d c = target.getPos().add(0, target.getHeight() * 0.7, 0);
        for (int i = 0; i < 18; i++) {
            double yaw = world.random.nextDouble() * Math.PI * 2;
            double pitch = (world.random.nextDouble() - 0.3) * Math.PI * 0.8;
            double sp = 0.18 + world.random.nextDouble() * 0.2;
            double vx = Math.cos(yaw) * Math.cos(pitch) * sp;
            double vy = Math.sin(pitch) * sp;
            double vz = Math.sin(yaw) * Math.cos(pitch) * sp;
            world.addParticle(ParticleTypes.END_ROD, c.x, c.y, c.z, vx, vy, vz);
            world.addParticle(th.dust2(1.0f), c.x, c.y, c.z, vx * 0.8, vy * 0.8, vz * 0.8);
        }
    }
}
