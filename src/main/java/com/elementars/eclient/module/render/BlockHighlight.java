package com.elementars.eclient.module.render;

import com.elementars.eclient.event.events.RenderEvent;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.RainbowUtils;
import com.elementars.eclient.util.XuluTessellator;
import dev.xulu.settings.Value;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BlockHighlight extends Module
{
    private final Value<String> mode;
    private final Value<Boolean> rainbow;
    private final Value<Integer> red;
    private final Value<Integer> green;
    private final Value<Integer> blue;
    private final Value<Integer> alpha;
    private final Value<Integer> alphaF;
    private final Value<Float> width;

    public BlockHighlight() {
        super("BlockHighlight", "Highlights block you're looking at", 0, Category.RENDER, true);
        this.mode = this.register(new Value<String>("Mode", this, "Outline", new String[] { "Solid", "Outline", "Full" }));
        this.rainbow = this.register(new Value<Boolean>("Rainbow", this, false));
        this.red = this.register(new Value<Integer>("Red", this, 255, 0, 255));
        this.green = this.register(new Value<Integer>("Green", this, 255, 0, 255));
        this.blue = this.register(new Value<Integer>("Blue", this, 255, 0, 255));
        this.alpha = this.register(new Value<Integer>("Alpha", this, 255, 0, 255));
        this.alphaF = this.register(new Value<Integer>("Alpha Full", this, 255, 0, 255));
        this.width = this.register(new Value<Float>("Width", this, 1.0f, 1.0f, 10.0f));
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public void onWorldRender(final RenderEvent event) {
        if (BlockHighlight.mc.level == null || BlockHighlight.mc.player == null) {
            return;
        }
        if (!(BlockHighlight.mc.hitResult instanceof BlockHitResult)) {
            return;
        }
        final BlockPos pos = ((BlockHitResult)BlockHighlight.mc.hitResult).getBlockPos();
        final BlockState state = BlockHighlight.mc.level.getBlockState(pos);
        if (state.isAir() || !BlockHighlight.mc.level.getWorldBorder().isWithinBounds(pos)) {
            return;
        }
        int r = this.red.getValue();
        int g = this.green.getValue();
        int b = this.blue.getValue();
        if (this.rainbow.getValue()) {
            r = RainbowUtils.r;
            g = RainbowUtils.g;
            b = RainbowUtils.b;
        }
        final Vec3 renderPos = event.getRenderPos();
        if (this.mode.getValue().equalsIgnoreCase("Solid")) {
            XuluTessellator.prepare(7);
            XuluTessellator.drawBox(pos, r, g, b, this.alpha.getValue(), 63);
            XuluTessellator.release();
        }
        else if (this.mode.getValue().equalsIgnoreCase("Outline")) {
            final AABB bb = state.getShape(BlockHighlight.mc.level, pos).bounds().inflate(0.002).move(pos)
                .move(-renderPos.x, -renderPos.y, -renderPos.z);
            XuluTessellator.drawBoundingBox(bb, this.width.getValue(), r, g, b, this.alpha.getValue());
        }
        else if (this.mode.getValue().equalsIgnoreCase("Full")) {
            final AABB bb = state.getShape(BlockHighlight.mc.level, pos).bounds().inflate(0.002).move(pos)
                .move(-renderPos.x, -renderPos.y, -renderPos.z);
            XuluTessellator.drawFullBox(bb, pos, this.width.getValue(), r, g, b, this.alpha.getValue(), this.alphaF.getValue());
        }
    }

    @SubscribeEvent
    public void onDrawBlockHighlight(final DrawSelectionEvent.HighlightBlock event) {
        if (BlockHighlight.mc.player == null || BlockHighlight.mc.level == null) {
            return;
        }
        if (BlockHighlight.mc.gameMode == null) {
            return;
        }
        final GameType gameType = BlockHighlight.mc.gameMode.getPlayerMode();
        if (gameType == GameType.SURVIVAL || gameType == GameType.CREATIVE) {
            event.setCanceled(true);
        }
    }
}
