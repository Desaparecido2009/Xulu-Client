package com.elementars.eclient.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

public class GLSLSandboxShader
{
    private final ShaderInstance shader;

    public GLSLSandboxShader(final String fragmentShaderLocation) {
        ShaderInstance instance = null;
        try {
            instance = new ShaderInstance(Minecraft.getInstance().getResourceManager(), new ResourceLocation("xulu", "xulu_bg"), DefaultVertexFormat.POSITION);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.shader = instance;
    }

    public boolean isValid() {
        return this.shader != null;
    }

    public void setShaderId(final int id) {
        if (this.shader == null) {
            return;
        }
        this.shader.safeGetUniform("ShaderId").set(id);
    }

    public void useShader(final int width, final int height, final float mouseX, final float mouseY, final float time) {
        if (this.shader == null) {
            return;
        }
        this.shader.safeGetUniform("Time").set(time);
        this.shader.safeGetUniform("Resolution").set((float)width, (float)height);
        RenderSystem.setShader(() -> this.shader);
    }
}
