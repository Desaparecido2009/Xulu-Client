package com.elementars.eclient.event.events;

import com.elementars.eclient.event.Event;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

public class EventPostRenderLayers extends Event
{
    public LivingEntityRenderer<?, ?> renderer;
    public EntityModel<?> modelBase;
    public LivingEntity entity;
    public float limbSwing;
    public float limbSwingAmount;
    public float partialTicks;
    public float ageInTicks;
    public float netHeadYaw;
    public float headPitch;
    public float scaleIn;
    public PoseStack poseStack;
    public MultiBufferSource bufferSource;
    public int packedLight;

    public EventPostRenderLayers(final LivingEntityRenderer<?, ?> renderer, final EntityModel<?> modelBase, final LivingEntity entitylivingbaseIn, final float limbSwing, final float limbSwingAmount, final float partialTicks, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scaleIn, final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight) {
        this.renderer = renderer;
        this.modelBase = modelBase;
        this.entity = entitylivingbaseIn;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.partialTicks = partialTicks;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
        this.scaleIn = scaleIn;
        this.poseStack = poseStack;
        this.bufferSource = bufferSource;
        this.packedLight = packedLight;
    }

    public LivingEntityRenderer<?, ?> getRenderer() {
        return this.renderer;
    }

    public EntityModel<?> getModelBase() {
        return this.modelBase;
    }

    public LivingEntity getEntitylivingbaseIn() {
        return this.entity;
    }

    public float getLimbSwing() {
        return this.limbSwing;
    }

    public float getLimbSwingAmount() {
        return this.limbSwingAmount;
    }

    @Override
    public float getPartialTicks() {
        return this.partialTicks;
    }

    public float getAgeInTicks() {
        return this.ageInTicks;
    }

    public float getNetHeadYaw() {
        return this.netHeadYaw;
    }

    public float getHeadPitch() {
        return this.headPitch;
    }

    public float getScaleIn() {
        return this.scaleIn;
    }

    public PoseStack getPoseStack() {
        return this.poseStack;
    }

    public MultiBufferSource getBufferSource() {
        return this.bufferSource;
    }

    public int getPackedLight() {
        return this.packedLight;
    }
}
