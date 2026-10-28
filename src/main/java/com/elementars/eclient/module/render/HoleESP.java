package com.elementars.eclient.module.render;

import com.elementars.eclient.event.events.RenderEvent;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.module.combat.AutoCrystal;
import com.elementars.eclient.util.BlockInteractionHelper;
import com.elementars.eclient.util.EnumUtil;
import com.elementars.eclient.util.Pair;
import com.elementars.eclient.util.RainbowUtils;
import com.elementars.eclient.util.XuluTessellator;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.xulu.settings.Value;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HoleESP extends Module
{
    private final BlockPos[] surroundOffset;
    private final Value<Boolean> frustum;
    private final Value<Boolean> hideOwn;
    private final Value<Boolean> offset;
    private final Value<Boolean> future;
    private final Value<Float> renderDistance;
    private final Value<Boolean> max;
    private final Value<Integer> maxHoles;
    private final Value<String> holeMode;
    private final Value<String> renderMode;
    private final Value<String> drawMode;
    private final Value<Float> cuboid;
    private final Value<Boolean> rainbow;
    private final Value<Integer> obiRed;
    private final Value<Integer> obiGreen;
    private final Value<Integer> obiBlue;
    private final Value<Integer> brockRed;
    private final Value<Integer> brockGreen;
    private final Value<Integer> brockBlue;
    private final Value<Integer> alpha;
    private final Value<Integer> alpha2;
    private ConcurrentHashMap<BlockPos, Pair<Boolean, Boolean>> safeHoles;
    private int holes;
    private Frustum camera;

    public HoleESP() {
        super("HoleESP", "Highlights holes for pvp", 0, Category.RENDER, true);
        this.frustum = this.register(new Value<Boolean>("Frustum", this, true));
        this.hideOwn = this.register(new Value<Boolean>("HideOwn", this, false));
        this.offset = this.register(new Value<Boolean>("Offset Lower", this, false));
        this.future = this.register(new Value<Boolean>("Future Mode", this, false));
        this.renderDistance = this.register(new Value<Float>("RenderDistance", this, 8.0f, 1.0f, 32.0f));
        this.max = this.register(new Value<Boolean>("Maximum Holes", this, false));
        this.maxHoles = this.register(new Value<Integer>("Maximum Num", this, 8, 1, 100));
        this.holeMode = this.register(new Value<String>("Hole Mode", this, "Both", new String[] { "Bedrock", "Obsidian", "Both" }));
        this.renderMode = this.register(new Value<String>("RenderMode", this, "Solid", new ArrayList<String>(Arrays.asList("Solid", "Flat"))));
        this.drawMode = this.register(new Value<String>("DrawMode", this, "Solid", EnumUtil.enumConverter(Modes.class)));
        this.cuboid = this.register(new Value<Float>("Cuboid Height", this, 0.9f, 0.0f, 1.0f));
        this.rainbow = this.register(new Value<Boolean>("Rainbow", this, false));
        this.obiRed = this.register(new Value<Integer>("ObiRed", this, 104, 0, 255));
        this.obiGreen = this.register(new Value<Integer>("ObiGreen", this, 12, 0, 255));
        this.obiBlue = this.register(new Value<Integer>("ObiBlue", this, 35, 0, 255));
        this.brockRed = this.register(new Value<Integer>("BrockRed", this, 81, 0, 255));
        this.brockGreen = this.register(new Value<Integer>("BrockGreen", this, 12, 0, 255));
        this.brockBlue = this.register(new Value<Integer>("BrockBlue", this, 104, 0, 255));
        this.alpha = this.register(new Value<Integer>("Alpha", this, 169, 0, 255));
        this.alpha2 = this.register(new Value<Integer>("Outline Alpha", this, 255, 0, 255));
        this.surroundOffset = new BlockPos[] { new BlockPos(0, -1, 0), new BlockPos(0, 0, -1), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(-1, 0, 0) };
    }

    @Override
    public void onUpdate() {
        if (HoleESP.mc.level == null || HoleESP.mc.player == null) {
            return;
        }
        if (this.camera != null) {
            final Vec3 cameraPos = HoleESP.mc.gameRenderer.getMainCamera().getPosition();
            this.camera.prepare(cameraPos.x, cameraPos.y, cameraPos.z);
        }
        if (this.safeHoles == null) {
            this.safeHoles = new ConcurrentHashMap<BlockPos, Pair<Boolean, Boolean>>();
        }
        else {
            this.safeHoles.clear();
        }
        final int range = (int)Math.ceil(this.renderDistance.getValue());
        final List<BlockPos> blockPosList = BlockInteractionHelper.getSphere(AutoCrystal.getPlayerPos(), (float)range, range, false, true, 0);
        final BlockPos ownPos = AutoCrystal.getPlayerPos();
        this.holes = 0;
        for (final BlockPos pos : blockPosList) {
            if (!HoleESP.mc.level.getBlockState(pos).isAir()) {
                continue;
            }
            if (!HoleESP.mc.level.getBlockState(pos.above()).isAir()) {
                continue;
            }
            if (!HoleESP.mc.level.getBlockState(pos.above(2)).isAir()) {
                continue;
            }
            if (this.hideOwn.getValue() && pos.equals(ownPos)) {
                continue;
            }
            if (this.frustum.getValue() && this.camera != null && !this.camera.isVisible(BlockInteractionHelper.getBlockBounds(HoleESP.mc.level.getBlockState(pos), HoleESP.mc.level, pos))) {
                continue;
            }
            boolean isSafe = true;
            boolean isBedrock = true;
            boolean hasBedrock = false;
            for (final BlockPos offset : this.surroundOffset) {
                final BlockState offsetState = HoleESP.mc.level.getBlockState(pos.offset(offset));
                if (!offsetState.is(Blocks.BEDROCK)) {
                    isBedrock = false;
                }
                if (offsetState.is(Blocks.BEDROCK)) {
                    hasBedrock = true;
                }
                if (!offsetState.is(Blocks.BEDROCK) && !offsetState.is(Blocks.OBSIDIAN) && !offsetState.is(Blocks.ENDER_CHEST) && !offsetState.is(Blocks.ANVIL)) {
                    isSafe = false;
                    break;
                }
            }
            if (!isSafe) {
                continue;
            }
            if (isBedrock && this.holeMode.getValue().equalsIgnoreCase("Obsidian")) {
                continue;
            }
            if (!isBedrock && this.holeMode.getValue().equalsIgnoreCase("Bedrock")) {
                continue;
            }
            this.safeHoles.put(pos, new Pair<Boolean, Boolean>(isBedrock, hasBedrock));
            if (!this.max.getValue()) {
                continue;
            }
            ++this.holes;
            if (this.holes == this.maxHoles.getValue()) {
                return;
            }
        }
    }

    @Override
    public void onWorldRender(final RenderEvent event) {
        if (HoleESP.mc.level == null || HoleESP.mc.player == null || this.safeHoles == null || this.safeHoles.isEmpty()) {
            return;
        }
        final Vec3 renderPos = event.getRenderPos();
        if (this.frustum.getValue()) {
            this.camera = new Frustum(RenderSystem.getModelViewStack().last().pose(), RenderSystem.getProjectionMatrix());
            this.camera.prepare(renderPos.x, renderPos.y, renderPos.z);
        }
        if (this.drawMode.getValue().equalsIgnoreCase("Solid")) {
            XuluTessellator.prepare(7);
            this.safeHoles.forEach((blockPos, pair) -> {
                final BlockPos pos = this.offset.getValue() ? blockPos.below() : blockPos;
                if (pair.getKey()) {
                    this.drawBlock(pos, this.rainbow.getValue() ? RainbowUtils.r : this.brockRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.brockGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.brockBlue.getValue(), pair);
                }
                else {
                    this.drawBlock(pos, this.rainbow.getValue() ? RainbowUtils.r : this.obiRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.obiGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.obiBlue.getValue(), pair);
                }
            });
            XuluTessellator.release();
        }
        else if (this.drawMode.getValue().equalsIgnoreCase("Outline")) {
            this.safeHoles.forEach((blockPos, pair) -> {
                final BlockPos pos = this.offset.getValue() ? blockPos.below() : blockPos;
                if (this.renderMode.getValue().equalsIgnoreCase("Solid")) {
                    if (pair.getKey()) {
                        this.drawBlockO(pos, this.rainbow.getValue() ? RainbowUtils.r : this.brockRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.brockGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.brockBlue.getValue(), pair, renderPos);
                    }
                    else {
                        this.drawBlockO(pos, this.rainbow.getValue() ? RainbowUtils.r : this.obiRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.obiGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.obiBlue.getValue(), pair, renderPos);
                    }
                }
                else if (this.renderMode.getValue().equalsIgnoreCase("Flat")) {
                    if (pair.getKey()) {
                        this.drawBlockOF(pos, this.rainbow.getValue() ? RainbowUtils.r : this.brockRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.brockGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.brockBlue.getValue(), pair, renderPos);
                    }
                    else {
                        this.drawBlockOF(pos, this.rainbow.getValue() ? RainbowUtils.r : this.obiRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.obiGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.obiBlue.getValue(), pair, renderPos);
                    }
                }
            });
        }
        else if (this.drawMode.getValue().equalsIgnoreCase("Full")) {
            this.safeHoles.forEach((blockPos, pair) -> {
                final BlockPos pos = this.offset.getValue() ? blockPos.below() : blockPos;
                if (this.renderMode.getValue().equalsIgnoreCase("Solid")) {
                    if (pair.getKey()) {
                        XuluTessellator.prepare(7);
                        this.drawBlock(pos, this.rainbow.getValue() ? RainbowUtils.r : this.brockRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.brockGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.brockBlue.getValue(), pair);
                        XuluTessellator.release();
                        this.drawBlockO(pos, this.rainbow.getValue() ? RainbowUtils.r : this.brockRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.brockGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.brockBlue.getValue(), pair, renderPos);
                    }
                    else {
                        XuluTessellator.prepare(7);
                        this.drawBlock(pos, this.rainbow.getValue() ? RainbowUtils.r : this.obiRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.obiGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.obiBlue.getValue(), pair);
                        XuluTessellator.release();
                        this.drawBlockO(pos, this.rainbow.getValue() ? RainbowUtils.r : this.obiRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.obiGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.obiBlue.getValue(), pair, renderPos);
                    }
                }
                else if (this.renderMode.getValue().equalsIgnoreCase("Flat")) {
                    if (pair.getKey()) {
                        XuluTessellator.prepare(7);
                        this.drawBlock(pos, this.rainbow.getValue() ? RainbowUtils.r : this.brockRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.brockGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.brockBlue.getValue(), pair);
                        XuluTessellator.release();
                        this.drawBlockOF(pos, this.rainbow.getValue() ? RainbowUtils.r : this.brockRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.brockGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.brockBlue.getValue(), pair, renderPos);
                    }
                    else {
                        XuluTessellator.prepare(7);
                        this.drawBlock(pos, this.rainbow.getValue() ? RainbowUtils.r : this.obiRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.obiGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.obiBlue.getValue(), pair);
                        XuluTessellator.release();
                        this.drawBlockOF(pos, this.rainbow.getValue() ? RainbowUtils.r : this.obiRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.obiGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.obiBlue.getValue(), pair, renderPos);
                    }
                }
            });
        }
        else if (this.drawMode.getValue().equalsIgnoreCase("Cuboid")) {
            this.safeHoles.forEach((blockPos, pair) -> {
                final BlockPos pos = this.offset.getValue() ? blockPos.below() : blockPos;
                if (pair.getKey()) {
                    this.drawBlockCUB(pos, this.rainbow.getValue() ? RainbowUtils.r : this.brockRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.brockGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.brockBlue.getValue(), pair, renderPos);
                }
                else {
                    this.drawBlockCUB(pos, this.rainbow.getValue() ? RainbowUtils.r : this.obiRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.obiGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.obiBlue.getValue(), pair, renderPos);
                }
            });
        }
        else if (this.drawMode.getValue().equalsIgnoreCase("Indicator")) {
            this.safeHoles.forEach((blockPos, pair) -> {
                final BlockPos pos = this.offset.getValue() ? blockPos.below() : blockPos;
                if (pair.getKey()) {
                    this.drawBlockIndicator(pos, this.rainbow.getValue() ? RainbowUtils.r : this.brockRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.brockGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.brockBlue.getValue(), pair, renderPos);
                }
                else {
                    this.drawBlockIndicator(pos, this.rainbow.getValue() ? RainbowUtils.r : this.obiRed.getValue(), this.rainbow.getValue() ? RainbowUtils.g : this.obiGreen.getValue(), this.rainbow.getValue() ? RainbowUtils.b : this.obiBlue.getValue(), pair, renderPos);
                }
            });
        }
    }

    private void drawBlock(final BlockPos blockPos, final int r, final int g, final int b, final Pair<Boolean, Boolean> pair) {
        Color color = new Color(r, g, b, this.alpha.getValue());
        if (this.future.getValue() && !pair.getKey() && pair.getValue()) {
            color = new Color(255, 255, 0, this.alpha.getValue());
        }
        int mask = 1;
        if (this.renderMode.getValue().equalsIgnoreCase("Solid")) {
            mask = 63;
        }
        XuluTessellator.drawBox(blockPos, color.getRGB(), mask);
    }

    private void drawBlockO(final BlockPos blockPos, final int r, final int g, final int b, final Pair<Boolean, Boolean> pair, final Vec3 renderPos) {
        final int red = (this.future.getValue() && !pair.getKey() && pair.getValue()) ? 255 : r;
        final int green = (this.future.getValue() && !pair.getKey() && pair.getValue()) ? 255 : g;
        final int blue = (this.future.getValue() && !pair.getKey() && pair.getValue()) ? 0 : b;
        final AABB box = BlockInteractionHelper.getBlockBounds(HoleESP.mc.level.getBlockState(blockPos), HoleESP.mc.level, blockPos).inflate(0.002).move(-renderPos.x, -renderPos.y, -renderPos.z);
        XuluTessellator.drawBoundingBox(box, 1.5f, red, green, blue, this.alpha2.getValue());
    }

    private void drawBlockCUB(final BlockPos blockPos, final int r, final int g, final int b, final Pair<Boolean, Boolean> pair, final Vec3 renderPos) {
        final int red = (this.future.getValue() && !pair.getKey() && pair.getValue()) ? 255 : r;
        final int green = (this.future.getValue() && !pair.getKey() && pair.getValue()) ? 255 : g;
        final int blue = (this.future.getValue() && !pair.getKey() && pair.getValue()) ? 0 : b;
        final AABB raw = BlockInteractionHelper.getBlockBounds(HoleESP.mc.level.getBlockState(blockPos), HoleESP.mc.level, blockPos);
        final AABB box = new AABB(raw.minX, raw.minY, raw.minZ, raw.maxX, raw.maxY - this.cuboid.getValue(), raw.maxZ).inflate(0.002).move(-renderPos.x, -renderPos.y, -renderPos.z);
        XuluTessellator.drawFullBox2(box, blockPos, 1.5f, new Color(red, green, blue, this.alpha.getValue()).getRGB(), this.alpha2.getValue());
    }

    private void drawBlockIndicator(final BlockPos blockPos, final int r, final int g, final int b, final Pair<Boolean, Boolean> pair, final Vec3 renderPos) {
        final int red = (this.future.getValue() && !pair.getKey() && pair.getValue()) ? 255 : r;
        final int green = (this.future.getValue() && !pair.getKey() && pair.getValue()) ? 255 : g;
        final int blue = (this.future.getValue() && !pair.getKey() && pair.getValue()) ? 0 : b;
        final AABB raw = BlockInteractionHelper.getBlockBounds(HoleESP.mc.level.getBlockState(blockPos), HoleESP.mc.level, blockPos);
        final double extra = HoleESP.mc.player.distanceToSqr(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5) < 10.0 ? 0.0 : 3.0;
        final AABB box = new AABB(raw.minX, raw.minY, raw.minZ, raw.maxX, raw.maxY + extra, raw.maxZ).inflate(0.002).move(-renderPos.x, -renderPos.y, -renderPos.z);
        RenderSystem.enableCull();
        XuluTessellator.drawIndicator(box, new Color(red, green, blue, this.alpha.getValue()).getRGB(), 63);
    }

    private void drawBlockOF(final BlockPos blockPos, final int r, final int g, final int b, final Pair<Boolean, Boolean> pair, final Vec3 renderPos) {
        final int red = (this.future.getValue() && !pair.getKey() && pair.getValue()) ? 255 : r;
        final int green = (this.future.getValue() && !pair.getKey() && pair.getValue()) ? 255 : g;
        final int blue = (this.future.getValue() && !pair.getKey() && pair.getValue()) ? 0 : b;
        final AABB box = BlockInteractionHelper.getBlockBounds(HoleESP.mc.level.getBlockState(blockPos), HoleESP.mc.level, blockPos).inflate(0.002).move(-renderPos.x, -renderPos.y, -renderPos.z);
        XuluTessellator.drawBoundingBoxFace(box, 1.5f, red, green, blue, this.alpha2.getValue());
    }

    @Override
    public String getHudInfo() {
        return this.renderMode.getValue() + ", " + this.drawMode.getValue();
    }

    public enum Modes
    {
        SOLID,
        OUTLINE,
        FULL,
        CUBOID,
        INDICATOR;
    }
}
