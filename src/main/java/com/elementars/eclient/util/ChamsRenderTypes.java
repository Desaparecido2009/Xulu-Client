package com.elementars.eclient.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class ChamsRenderTypes
{
    public static final ResourceLocation WHITE_TEXTURE = new ResourceLocation("textures/misc/white.png");
    public static final RenderType SOLID_DEPTH = create(WHITE_TEXTURE, true);
    public static final RenderType SOLID_NO_DEPTH = create(WHITE_TEXTURE, false);
    private static final Map<ResourceLocation, RenderType> TEXTURED = new HashMap<>();

    public static RenderType textured(final ResourceLocation texture) {
        return TEXTURED.computeIfAbsent(texture, t -> create(t, true));
    }

    private static RenderType create(final ResourceLocation texture, final boolean depthTest) {
        final RenderType.CompositeState state = RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentShader))
            .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
            .setTransparencyState(new RenderStateShard.TransparencyStateShard("xulu_chams_translucent", () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            }))
            .setDepthTestState(new RenderStateShard.DepthTestStateShard("xulu_chams_depth", depthTest ? GL11.GL_LEQUAL : GL11.GL_ALWAYS))
            .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, depthTest))
            .setCullState(new RenderStateShard.CullStateShard(false))
            .setLightmapState(new RenderStateShard.LightmapStateShard(true))
            .setOverlayState(new RenderStateShard.OverlayStateShard(true))
            .createCompositeState(false);
        return RenderType.create("xulu_chams", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, state);
    }

    public static void endBatch(final MultiBufferSource bufferSource, final RenderType type) {
        if (bufferSource instanceof MultiBufferSource.BufferSource) {
            ((MultiBufferSource.BufferSource)bufferSource).endBatch(type);
        }
    }

    public static void beginLines(final boolean lines, final float width) {
        if (lines) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            GL11.glLineWidth(width);
        }
    }

    public static void endLines(final boolean lines) {
        if (lines) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        }
    }
}
