package com.elementars.eclient.module.render;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.event.events.RenderEvent;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.XuluTessellator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.xulu.settings.Value;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Trajectories extends Module
{
    private final Value<Float> scale;
    private final Value<Boolean> rainbow;
    private final Value<Integer> red;
    private final Value<Integer> green;
    private final Value<Integer> blue;
    private final Value<Integer> redC;
    private final Value<Integer> greenC;
    private final Value<Integer> blueC;

    public Trajectories() {
        super("Trajectories", "Ingrosware trajectories", 0, Category.RENDER, true);
        this.scale = this.register(new Value<Float>("Scale", this, 1.0f, 1.0f, 2.0f));
        this.rainbow = this.register(new Value<Boolean>("Rainbow", this, false));
        this.red = this.register(new Value<Integer>("Red", this, 0, 0, 255));
        this.green = this.register(new Value<Integer>("Green", this, 255, 0, 255));
        this.blue = this.register(new Value<Integer>("Blue", this, 0, 0, 255));
        this.redC = this.register(new Value<Integer>("Charge Red", this, 204, 0, 255));
        this.greenC = this.register(new Value<Integer>("Charge Green", this, 127, 0, 255));
        this.blueC = this.register(new Value<Integer>("Charge Blue", this, 0, 0, 255));
    }

    @Override
    public void onWorldRender(final RenderEvent event) {
        if (Trajectories.mc.level == null || Trajectories.mc.player == null) {
            return;
        }
        if (!Trajectories.mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        final ItemStack stack = Trajectories.mc.player.getMainHandItem();
        final Item item = stack.getItem();
        final boolean bow = item instanceof BowItem;
        final boolean fishing = item instanceof FishingRodItem;
        final boolean pearl = item instanceof EnderpearlItem;
        final boolean egg = item instanceof EggItem;
        final boolean snowball = item instanceof SnowballItem;
        final boolean experience = item instanceof ExperienceBottleItem;
        if (!bow && !fishing && !pearl && !egg && !snowball && !experience) {
            return;
        }
        final float partialTicks = Trajectories.mc.getFrameTime();
        final Vec3 renderPos = event.getRenderPos();
        final Vec3 view = Trajectories.mc.player.getViewVector(partialTicks);
        final double yaw = Math.toRadians(Trajectories.mc.player.getYRot());
        final float eyeHeight = Trajectories.mc.player.getEyeHeight();
        float power = (float)Trajectories.mc.player.getTicksUsingItem() / 20.0f;
        power = (power * power + power * 2.0f) / 3.0f;
        if (power > 1.0f) {
            power = 1.0f;
        }
        final double speed = bow ? power * 3.0 : (fishing ? 0.9375 : (experience ? 0.675 : 1.5));
        final double gravity = bow ? 0.05 : 0.03;
        double motionX = view.x * speed;
        double motionY = view.y * speed;
        double motionZ = view.z * speed;
        double posX = Trajectories.mc.player.getX() - Math.cos(yaw) * 0.16;
        double posY = Trajectories.mc.player.getY() + eyeHeight - 0.1;
        double posZ = Trajectories.mc.player.getZ() - Math.sin(yaw) * 0.16;
        final int argb = this.getColor(power);
        final List<Vec3> points = new ArrayList<Vec3>();
        points.add(new Vec3(posX, posY, posZ));
        Vec3 landing = null;
        boolean entityLanding = false;
        for (int i = 0; i < 300 && posY > 0.0; ++i) {
            final Vec3 present = new Vec3(posX, posY, posZ);
            final Vec3 future = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
            final BlockHitResult blockHit = Trajectories.mc.level.clip(new ClipContext(present, future, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, Trajectories.mc.player));
            if (blockHit.getType() != HitResult.Type.MISS) {
                landing = blockHit.getLocation();
                points.add(landing);
                break;
            }
            final double size = bow ? 0.3 : 0.25;
            final AABB search = new AABB(posX - size, posY - size, posZ - size, posX + size, posY + size, posZ + size).inflate(0.3);
            for (final Entity entity : Trajectories.mc.level.getEntitiesOfClass(Entity.class, search, entity -> entity != Trajectories.mc.player && entity.isAlive() && entity.isPickable())) {
                final Optional<Vec3> hit = entity.getBoundingBox().inflate(0.3).clip(present, future);
                if (hit.isPresent()) {
                    landing = hit.get();
                    entityLanding = true;
                    points.add(landing);
                    break;
                }
            }
            if (landing != null) {
                break;
            }
            posX += motionX;
            posY += motionY;
            posZ += motionZ;
            motionX *= 0.99;
            motionY *= 0.99;
            motionZ *= 0.99;
            motionY -= gravity;
            points.add(new Vec3(posX, posY, posZ));
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        XuluTessellator.prepareGL();
        RenderSystem.lineWidth(2.0f);
        XuluTessellator.begin(3);
        final BufferBuilder buffer = XuluTessellator.getBufferBuilder();
        for (final Vec3 point : points) {
            final boolean last = entityLanding && point == points.get(points.size() - 1);
            buffer.vertex(point.x - renderPos.x, point.y - renderPos.y, point.z - renderPos.z).color(last ? 0xFFFF0000 : argb).endVertex();
        }
        XuluTessellator.render();
        RenderSystem.lineWidth(1.0f);
        XuluTessellator.releaseGL();
        if (landing != null) {
            final double marker = 0.3 * this.scale.getValue();
            final AABB box = new AABB(landing.x - marker, landing.y - marker, landing.z - marker, landing.x + marker, landing.y + marker, landing.z + marker)
                .move(-renderPos.x, -renderPos.y, -renderPos.z);
            XuluTessellator.drawBoundingBox(box, 2.0f, entityLanding ? 0xFFFF0000 : argb);
        }
    }

    private int getColor(final float power) {
        if (this.rainbow.getValue()) {
            return Xulu.rgb;
        }
        if (power > 0.6f) {
            return (255 << 24) | (this.red.getValue() << 16) | (this.green.getValue() << 8) | this.blue.getValue();
        }
        return (255 << 24) | (this.redC.getValue() << 16) | (this.greenC.getValue() << 8) | this.blueC.getValue();
    }
}
