package com.example.hitfx;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

/** Compact panel pinned beside the crosshair: name, weapon, health, distance, combo, hits. */
public final class TargetHud {
    public static void init() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world == null || mc.player == null || mc.options.hudHidden) return;
            long now = mc.world.getTime();
            Theme th = Config.theme();

            drawRippleCooldown(ctx, mc, now, th);

            if (!Config.on(Config.Feature.TARGET_HUD)) return;
            LivingEntity t = TargetTracker.current(mc.world, now);
            if (t == null) return;

            int w = 132, h = 40;
            int x = ctx.getScaledWindowWidth() / 2 - w - 18;
            int y = ctx.getScaledWindowHeight() / 2 - h / 2 + 16;

            ctx.fill(x, y, x + w, y + h, 0xB0101018);                 // panel
            ctx.fill(x, y, x + 2, y + h, th.argb(255));                // accent edge

            ctx.drawText(mc.textRenderer, t.getName().getString(), x + 7, y + 5, 0xFFFFFFFF, true);
            String weapon = weaponLabel(t.getMainHandStack());
            ctx.drawText(mc.textRenderer, weapon, x + w - 6 - mc.textRenderer.getWidth(weapon), y + 5, th.argb(255), true);

            float hp = t.getHealth() + t.getAbsorptionAmount();
            float frac = Math.max(0f, Math.min(1f, t.getHealth() / t.getMaxHealth()));
            ctx.fill(x + 7, y + 17, x + w - 7, y + 21, 0xFF2A2A36);
            ctx.fill(x + 7, y + 17, x + 7 + (int) ((w - 14) * frac), y + 21, th.argb(255));
            ctx.drawText(mc.textRenderer, String.format("\u2665 %.1f", hp), x + 7, y + 25, 0xFFE8E8F0, true);

            String dist = String.format("%.1f m", mc.player.distanceTo(t));
            ctx.drawText(mc.textRenderer, dist, x + w - 6 - mc.textRenderer.getWidth(dist), y + 25, 0xFFB8B8C8, true);

            String stats = "x" + TargetTracker.combo + " combo  " + TargetTracker.hits + "/" + TargetTracker.swings + " hits";
            ctx.drawText(mc.textRenderer, stats, x + 7, y + 33 - 1, th.rgb2() | 0xFF000000, false);

            ShieldBreaker.drawPill(ctx, mc, t, x, y + h + 3, w, now);
        });
    }

    private static void drawRippleCooldown(DrawContext ctx, MinecraftClient mc, long now, Theme th) {
        if (!Config.on(Config.Feature.RIPPLE)) return;
        float left = RippleManager.cooldownSecondsLeft(now);
        if (left <= 0) return;
        ctx.drawText(mc.textRenderer, String.format("%.1fs", left),
                ctx.getScaledWindowWidth() / 2 + 12, ctx.getScaledWindowHeight() / 2 + 12, th.argb(255), true);
    }

    private static String weaponLabel(ItemStack s) {
        if (s.getItem() instanceof SwordItem) return "SWORD";
        if (s.getItem() instanceof AxeItem) return "AXE";
        return s.isEmpty() ? "FIST" : "ITEM";
    }
}
