package com.example.hitfx;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

/** Redraws the model as a translucent themed shell that flashes in and fades while a ripple plays. */
public class RippleOverlayFeature<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
    private final FeatureRendererContext<T, M> ctx;

    public RippleOverlayFeature(FeatureRendererContext<T, M> context) {
        super(context);
        this.ctx = context;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vcp, int light, T entity,
                       float limbAngle, float limbDistance, float tickDelta,
                       float animationProgress, float headYaw, float headPitch) {
        if (!Config.on(Config.Feature.RIPPLE)) return;

        float now = entity.getWorld().getTime() + tickDelta;
        float p = RippleManager.progress(entity.getId(), now);
        if (p < 0) return;

        float envelope = (float) Math.sin(Math.PI * Math.pow(p, 0.5));
        float shimmer = 0.75f + 0.25f * MathHelper.sin(p * 18f);
        int alpha = (int) (200 * envelope * shimmer);
        int argb = Config.theme().argb(alpha);

        matrices.push();
        float s = 1.0f + 0.06f * envelope;
        matrices.scale(s, s, s);
        var buffer = vcp.getBuffer(RenderLayer.getEntityTranslucentEmissive(ctx.getTexture(entity)));
        getContextModel().render(matrices, buffer, 0xF000F0, OverlayTexture.DEFAULT_UV, argb);
        matrices.pop();
    }
}
