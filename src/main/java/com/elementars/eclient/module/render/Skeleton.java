package com.elementars.eclient.module.render;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.event.events.RenderEvent;
import com.elementars.eclient.friend.Friends;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.TargetPlayers;
import com.elementars.eclient.util.XuluTessellator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import dev.xulu.newgui.util.ColorUtil;
import dev.xulu.settings.Value;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.HashMap;

public class Skeleton extends Module
{
    private final Value<String> color;
    private final Value<Boolean> friends;
    private final Value<Float> width;
    private final Value<Integer> red;
    private final Value<Integer> green;
    private final Value<Integer> blue;
    private Frustum camera;
    private static final HashMap<Player, float[][]> entities = new HashMap<Player, float[][]>();

    public Skeleton() {
        super("Skeleton", "Renders player entities skeletons", 0, Category.RENDER, true);
        this.color = this.register(new Value<String>("Color Mode", this, "White", new String[] { "White", "ClickGui", "Tracers", "Target", "Rainbow" }));
        this.friends = this.register(new Value<Boolean>("Friends", this, true));
        this.width = this.register(new Value<Float>("Width", this, 1.5f, 0.0f, 10.0f));
        this.red = this.register(new Value<Integer>("Target Red", this, 255, 0, 255));
        this.green = this.register(new Value<Integer>("Target Green", this, 0, 0, 255));
        this.blue = this.register(new Value<Integer>("TargetBlue", this, 0, 0, 255));
    }

    private Vec3 getVec3(final AbstractClientPlayer e, final float partialTicks) {
        final double x = e.xo + (e.getX() - e.xo) * partialTicks;
        final double y = e.yo + (e.getY() - e.yo) * partialTicks;
        final double z = e.zo + (e.getZ() - e.zo) * partialTicks;
        return new Vec3(x, y, z);
    }

    private float[] glColor(final AbstractClientPlayer entity) {
        if (Friends.isFriend(entity.getName().getString()) && this.friends.getValue()) {
            return new float[] { 0.27f, 0.7f, 0.92f, 1.0f };
        }
        if (this.color.getValue().equalsIgnoreCase("ClickGui")) {
            final Color clickGuiColor = ColorUtil.getClickGUIColor();
            return new float[] { clickGuiColor.getRed() / 255.0f, clickGuiColor.getGreen() / 255.0f, clickGuiColor.getBlue() / 255.0f, 1.0f };
        }
        if (this.color.getValue().equalsIgnoreCase("Tracers")) {
            final float distance = mc.player.distanceTo(entity);
            if (distance <= 32.0f) {
                return new float[] { 1.0f - distance / 32.0f / 2.0f, distance / 32.0f, 0.0f, 1.0f };
            }
            return new float[] { 0.0f, 0.9f, 0.0f, 1.0f };
        }
        if (this.color.getValue().equalsIgnoreCase("Target")) {
            if (TargetPlayers.targettedplayers.containsKey(entity.getName().getString())) {
                return new float[] { this.red.getValue() / 255.0f, this.green.getValue() / 255.0f, this.blue.getValue() / 255.0f, 1.0f };
            }
            final Color clickGuiColor = ColorUtil.getClickGUIColor();
            return new float[] { clickGuiColor.getRed() / 255.0f, clickGuiColor.getGreen() / 255.0f, clickGuiColor.getBlue() / 255.0f, 1.0f };
        }
        if (this.color.getValue().equalsIgnoreCase("Rainbow")) {
            final Color c = new Color(Xulu.rgb);
            return new float[] { c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, c.getAlpha() / 255.0f };
        }
        return new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    }

    @Override
    public void onWorldRender(final RenderEvent event) {
        if (mc.level == null || mc.player == null) {
            return;
        }
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        final Vec3 renderPos = event.getRenderPos();
        this.camera = new Frustum(RenderSystem.getModelViewStack().last().pose(), RenderSystem.getProjectionMatrix());
        this.camera.prepare(renderPos.x, renderPos.y, renderPos.z);
        Skeleton.entities.keySet().removeIf(this::doesntContain);
        mc.level.players().forEach(e -> this.drawSkeleton(event, e));
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.lineWidth(1.0f);
    }

    private void drawSkeleton(final RenderEvent event, final AbstractClientPlayer e) {
        final float partialTicks = mc.getFrameTime();
        final float[][] entPos = Skeleton.entities.get(e);
        if (entPos != null && e.isAlive() && this.camera.isVisible(e.getBoundingBox()) && !e.isDeadOrDying() && e != mc.player && !e.isSleeping()) {
            final float[] col = this.glColor(e);
            final Vec3 vec = this.getVec3(e, partialTicks);
            final double x = vec.x - event.getRenderPos().x;
            final double y = vec.y - event.getRenderPos().y;
            final double z = vec.z - event.getRenderPos().z;
            final float xOff = e.yBodyRotO + (e.yBodyRot - e.yBodyRotO) * partialTicks;
            final float yOff = e.isCrouching() ? 0.6f : 0.75f;
            RenderSystem.lineWidth(this.width.getValue());
            final PoseStack poseStack = new PoseStack();
            poseStack.translate(x, y, z);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-xOff));
            poseStack.translate(0.0, 0.0, e.isCrouching() ? -0.235 : 0.0);
            poseStack.pushPose();
            poseStack.translate(-0.125, yOff, 0.0);
            if (entPos[3][0] != 0.0f) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(entPos[3][0] * 57.295776f));
            }
            if (entPos[3][1] != 0.0f) {
                poseStack.mulPose(Vector3f.YP.rotationDegrees(entPos[3][1] * 57.295776f));
            }
            if (entPos[3][2] != 0.0f) {
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(entPos[3][2] * 57.295776f));
            }
            drawLine(poseStack, 0.0, 0.0, 0.0, 0.0, -yOff, 0.0, col);
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.translate(0.125, yOff, 0.0);
            if (entPos[4][0] != 0.0f) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(entPos[4][0] * 57.295776f));
            }
            if (entPos[4][1] != 0.0f) {
                poseStack.mulPose(Vector3f.YP.rotationDegrees(entPos[4][1] * 57.295776f));
            }
            if (entPos[4][2] != 0.0f) {
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(entPos[4][2] * 57.295776f));
            }
            drawLine(poseStack, 0.0, 0.0, 0.0, 0.0, -yOff, 0.0, col);
            poseStack.popPose();
            poseStack.translate(0.0, 0.0, e.isCrouching() ? 0.25 : 0.0);
            poseStack.pushPose();
            poseStack.translate(0.0, e.isCrouching() ? -0.05 : 0.0, e.isCrouching() ? -0.01725 : 0.0);
            poseStack.pushPose();
            poseStack.translate(-0.375, yOff + 0.55, 0.0);
            if (entPos[1][0] != 0.0f) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(entPos[1][0] * 57.295776f));
            }
            if (entPos[1][1] != 0.0f) {
                poseStack.mulPose(Vector3f.YP.rotationDegrees(entPos[1][1] * 57.295776f));
            }
            if (entPos[1][2] != 0.0f) {
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(-entPos[1][2] * 57.295776f));
            }
            drawLine(poseStack, 0.0, 0.0, 0.0, 0.0, -0.5, 0.0, col);
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.translate(0.375, yOff + 0.55, 0.0);
            if (entPos[2][0] != 0.0f) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(entPos[2][0] * 57.295776f));
            }
            if (entPos[2][1] != 0.0f) {
                poseStack.mulPose(Vector3f.YP.rotationDegrees(entPos[2][1] * 57.295776f));
            }
            if (entPos[2][2] != 0.0f) {
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(-entPos[2][2] * 57.295776f));
            }
            drawLine(poseStack, 0.0, 0.0, 0.0, 0.0, -0.5, 0.0, col);
            poseStack.popPose();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(xOff - e.yHeadRot));
            poseStack.pushPose();
            poseStack.translate(0.0, yOff + 0.55, 0.0);
            if (entPos[0][0] != 0.0f) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(entPos[0][0] * 57.295776f));
            }
            drawLine(poseStack, 0.0, 0.0, 0.0, 0.0, 0.3, 0.0, col);
            poseStack.popPose();
            poseStack.popPose();
            poseStack.mulPose(Vector3f.XP.rotationDegrees(e.isCrouching() ? 25.0f : 0.0f));
            poseStack.translate(0.0, e.isCrouching() ? -0.16175 : 0.0, e.isCrouching() ? -0.48025 : 0.0);
            poseStack.pushPose();
            poseStack.translate(0.0, yOff, 0.0);
            drawLine(poseStack, -0.125, 0.0, 0.0, 0.125, 0.0, 0.0, col);
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.translate(0.0, yOff, 0.0);
            drawLine(poseStack, 0.0, 0.0, 0.0, 0.0, 0.55, 0.0, col);
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.translate(0.0, yOff + 0.55, 0.0);
            drawLine(poseStack, -0.375, 0.0, 0.0, 0.375, 0.0, 0.0, col);
            poseStack.popPose();
        }
    }

    private static void drawLine(final PoseStack poseStack, final double fromX, final double fromY, final double fromZ, final double toX, final double toY, final double toZ, final float[] col) {
        final Matrix4f matrix = poseStack.last().pose();
        final Vector4f from = new Vector4f((float)fromX, (float)fromY, (float)fromZ, 1.0f);
        from.transform(matrix);
        final Vector4f to = new Vector4f((float)toX, (float)toY, (float)toZ, 1.0f);
        to.transform(matrix);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        final BufferBuilder bufferBuilder = XuluTessellator.getBufferBuilder();
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(from.x(), from.y(), from.z()).color(col[0], col[1], col[2], col[3]).endVertex();
        bufferBuilder.vertex(to.x(), to.y(), to.z()).color(col[0], col[1], col[2], col[3]).endVertex();
        Tesselator.getInstance().end();
    }

    public static void addEntity(final Player e, final PlayerModel model) {
        Skeleton.entities.put(e, new float[][] { { model.head.xRot, model.head.yRot, model.head.zRot }, { model.rightArm.xRot, model.rightArm.yRot, model.rightArm.zRot }, { model.leftArm.xRot, model.leftArm.yRot, model.leftArm.zRot }, { model.rightLeg.xRot, model.rightLeg.yRot, model.rightLeg.zRot }, { model.leftLeg.xRot, model.leftLeg.yRot, model.leftLeg.zRot } });
    }

    private boolean doesntContain(final Player var0) {
        return !mc.level.players().contains(var0);
    }
}
