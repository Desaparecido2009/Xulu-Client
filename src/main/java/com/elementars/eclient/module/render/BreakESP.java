package com.elementars.eclient.module.render;

import com.elementars.eclient.event.events.RenderEvent;
import com.elementars.eclient.mixin.mixins.AccessorLevelRenderer;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.BlockInteractionHelper;
import com.elementars.eclient.util.XuluTessellator;
import dev.xulu.settings.Value;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BreakESP extends Module
{
    private final Map<Integer, Integer> alphaMap;
    private final Value<String> mode;
    private final Value<Boolean> ignoreSelf;
    private final Value<Boolean> onlyObby;
    private final Value<Boolean> fade;
    private final Value<Integer> red;
    private final Value<Integer> green;
    private final Value<Integer> blue;
    private final Value<Integer> alpha;
    private final Value<Integer> alphaF;

    public BreakESP() {
        super("BreakESP", "Highlights blocks being broken", 0, Category.RENDER, true);
        this.alphaMap = new HashMap<Integer, Integer>();
        this.mode = this.register(new Value<String>("Mode", this, "Solid", new ArrayList<String>(Arrays.asList("Solid", "Outline", "Full"))));
        this.ignoreSelf = this.register(new Value<Boolean>("Ignore Self", this, true));
        this.onlyObby = this.register(new Value<Boolean>("Only Obsidian", this, true));
        this.fade = this.register(new Value<Boolean>("Fade Progress", this, true));
        this.red = this.register(new Value<Integer>("Red", this, 255, 0, 255));
        this.green = this.register(new Value<Integer>("Green", this, 0, 0, 255));
        this.blue = this.register(new Value<Integer>("Blue", this, 0, 0, 255));
        this.alpha = this.register(new Value<Integer>("Alpha", this, 70, 0, 255));
        this.alphaF = this.register(new Value<Integer>("Full Alpha", this, 100, 0, 255));
        this.alphaMap.put(0, 28);
        this.alphaMap.put(1, 56);
        this.alphaMap.put(2, 84);
        this.alphaMap.put(3, 112);
        this.alphaMap.put(4, 140);
        this.alphaMap.put(5, 168);
        this.alphaMap.put(6, 196);
        this.alphaMap.put(7, 224);
        this.alphaMap.put(8, 255);
        this.alphaMap.put(9, 255);
    }

    @Override
    public void onWorldRender(final RenderEvent event) {
        if (BreakESP.mc.level == null || BreakESP.mc.player == null) {
            return;
        }
        final Vec3 renderPos = event.getRenderPos();
        final Int2ObjectMap<BlockDestructionProgress> destroyed = ((AccessorLevelRenderer)BreakESP.mc.levelRenderer).xulu$getDestroyingBlocks();
        for (final Int2ObjectMap.Entry<BlockDestructionProgress> entry : destroyed.int2ObjectEntrySet()) {
            final BlockDestructionProgress progress = entry.getValue();
            if (progress == null) {
                continue;
            }
            if (this.ignoreSelf.getValue() && BreakESP.mc.level.getEntity(entry.getIntKey()) == BreakESP.mc.player) {
                continue;
            }
            final BlockPos pos = progress.getPos();
            final BlockState state = BreakESP.mc.level.getBlockState(pos);
            if (this.onlyObby.getValue() && !state.is(Blocks.OBSIDIAN)) {
                continue;
            }
            final int transparency = this.fade.getValue()
                ? this.alphaMap.getOrDefault(Math.min(9, progress.getProgress()), 255)
                : this.alpha.getValue();
            if (this.mode.getValue().equalsIgnoreCase("Solid")) {
                XuluTessellator.prepare(7);
                XuluTessellator.drawBox(pos, this.red.getValue(), this.green.getValue(), this.blue.getValue(), transparency, 63);
                XuluTessellator.release();
            }
            else if (this.mode.getValue().equalsIgnoreCase("Full")) {
                final AABB box = BlockInteractionHelper.getBlockBounds(state, BreakESP.mc.level, pos).inflate(0.002).move(-renderPos.x, -renderPos.y, -renderPos.z);
                XuluTessellator.drawFullBox(box, pos, 1.5f, this.red.getValue(), this.green.getValue(), this.blue.getValue(), transparency, this.alphaF.getValue());
            }
            else if (this.mode.getValue().equalsIgnoreCase("Outline")) {
                final AABB box = BlockInteractionHelper.getBlockBounds(state, BreakESP.mc.level, pos).inflate(0.002).move(-renderPos.x, -renderPos.y, -renderPos.z);
                XuluTessellator.drawBoundingBox(box, 1.5f, this.red.getValue(), this.green.getValue(), this.blue.getValue(), transparency);
            }
            else {
                XuluTessellator.prepare(7);
                XuluTessellator.drawBox(pos, this.red.getValue(), this.green.getValue(), this.blue.getValue(), transparency, 63);
                XuluTessellator.release();
            }
        }
    }
}
