package com.example.hitfx;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HitFXClient implements ClientModInitializer {
    /** An attack we sent that we're still waiting to see land (the target's hurt animation proves it did). */
    private record Pending(int targetId, long tick, boolean crit) {}

    private static final List<Pending> PENDING = new ArrayList<>();

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onInitializeClient() {
        Config.load();
        HitFXCommand.register();
        TargetHud.init();
        Afterglow.init();
        Backtrack.init();

        // Ripple glow layer on every living-entity renderer.
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, ctx) ->
                helper.register(new RippleOverlayFeature(renderer)));

        // Our swings.
        AttackEntityCallback.EVENT.register((player, world, hand, target, hit) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (!world.isClient || player != mc.player || !(target instanceof LivingEntity le)) return ActionResult.PASS;
            ClientWorld cw = (ClientWorld) world;
            long now = cw.getTime();

            TargetTracker.swing(le, now);
            ShieldBreaker.tryBreak(cw, player, le);
            PENDING.add(new Pending(le.getId(), now, isCrit(player)));
            return ActionResult.PASS; // never cancel the real attack
        });

        // Confirm hits and fire the effects.
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.world == null || mc.player == null) { PENDING.clear(); return; }
            long now = mc.world.getTime();

            Iterator<Pending> it = PENDING.iterator();
            while (it.hasNext()) {
                Pending p = it.next();
                Entity e = mc.world.getEntityById(p.targetId());
                if (!(e instanceof LivingEntity t) || now - p.tick() > 4) { it.remove(); continue; }
                if (t.hurtTime > 0 && t.hurtTime >= t.maxHurtTime - 2) { // hurt animation just started
                    onLanded(mc, t, p, now);
                    it.remove();
                }
            }
            RippleManager.cleanup(now);
        });
    }

    private static void onLanded(MinecraftClient mc, LivingEntity t, Pending p, long now) {
        PlayerEntity me = mc.player;
        ClientWorld w = mc.world;
        TargetTracker.landed(t, now);

        if (Config.on(Config.Feature.RIPPLE) && me.getMainHandStack().getItem() instanceof SwordItem && RippleManager.ready(now)) {
            RippleManager.trigger(t.getId(), now);
            Theme th = Config.theme();
            for (int i = 0; i < 40; i++) {
                double a = i / 40.0 * Math.PI * 2;
                w.addParticle(i % 2 == 0 ? ParticleTypes.END_ROD : th.dust2(0.9f),
                        t.getX() + Math.cos(a) * 0.4, t.getY() + 0.1, t.getZ() + Math.sin(a) * 0.4,
                        Math.cos(a) * 0.12, 0.02, Math.sin(a) * 0.12);
            }
        }
        HitSplash.burst(w, me, t);
        if (p.crit()) CritStars.burst(w, t);
    }

    private static boolean isCrit(PlayerEntity p) {
        return p.getAttackCooldownProgress(0.5f) > 0.9f && p.fallDistance > 0 && !p.isOnGround() && !p.isClimbing()
                && !p.isTouchingWater() && !p.hasStatusEffect(StatusEffects.BLINDNESS) && !p.hasVehicle() && !p.isSprinting();
    }
}
