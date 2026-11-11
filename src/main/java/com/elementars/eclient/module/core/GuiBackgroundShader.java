package com.elementars.eclient.module.core;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.GLSLSandboxShader;
import com.elementars.eclient.util.GLSLShaders;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.xulu.settings.Value;
import net.minecraft.client.Minecraft;

public class GuiBackgroundShader extends Module {
    public final Value<String> mode;
    public final Value<GLSLShaders> shader;
    public static Value<Integer> fps;
    public static Value<Integer> alpha;

    public GuiBackgroundShader() {
        super("GuiBackgroundShader", "Displays a shader background for the ClickGUI and HUD Editor", 0, Category.CORE, false);
        this.mode = this.register(new Value<String>("Mode", this, "Select", new String[] { "Random", "Select" }));
        this.shader = this.register(new Value<GLSLShaders>("Shader", this, GLSLShaders.AURORA, GLSLShaders.values()));
        GuiBackgroundShader.fps = this.register(new Value<Integer>("FPS", this, 60, 5, 60));
        GuiBackgroundShader.alpha = this.register(new Value<Integer>("Alpha", this, 255, 0, 255));
    }

    public static boolean renderBackground(final int width, final int height, final float time) {
        final GuiBackgroundShader module = Xulu.MODULE_MANAGER.getModuleT(GuiBackgroundShader.class);
        if (module == null || !module.isToggled()) {
            return false;
        }

        final GLSLShaders selected;
        if (module.mode.getValue().equalsIgnoreCase("Random")) {
            if (Xulu.randomShader == null) {
                final GLSLShaders[] values = GLSLShaders.values();
                Xulu.randomShader = values[(int)(Math.random() * values.length)];
            }
            selected = Xulu.randomShader;
        } else {
            selected = module.shader.getValue();
        }

        if (Xulu.backgroundShader == null) {
            Xulu.initTime = System.currentTimeMillis();
            Xulu.backgroundShader = new GLSLSandboxShader(selected.get());
        }

        if (Xulu.backgroundShader == null || !Xulu.backgroundShader.isValid()) {
            return false;
        }

        final Minecraft mc = Minecraft.getInstance();
        final float currentTime = (System.currentTimeMillis() - Xulu.initTime) / 1000.0f;
        Xulu.backgroundShader.setShaderId(selected.ordinal());
        Xulu.backgroundShader.useShader(width, height, 0.0f, 0.0f, currentTime);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        int alphaValue = alpha.getValue();
        if (alphaValue < 255) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alphaValue / 255.0f);
        }

        final BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        buffer.vertex(0.0, height, 0.0).endVertex();
        buffer.vertex(width, height, 0.0).endVertex();
        buffer.vertex(width, 0.0, 0.0).endVertex();
        buffer.vertex(0.0, 0.0, 0.0).endVertex();
        Tesselator.getInstance().end();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableDepthTest();

        return true;
    }
}