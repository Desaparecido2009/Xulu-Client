package com.elementars.eclient.module.render;

import com.elementars.eclient.event.events.RenderEvent;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.BlockInteractionHelper;
import com.elementars.eclient.util.ColorUtils;
import com.elementars.eclient.util.OutlineUtils;
import com.elementars.eclient.util.XuluTessellator;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.settings.Value;
import java.awt.Color;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class StorageESP extends Module
{
    private final Value<String> mode;
    private final Value<Boolean> all;
    private static Value<Boolean> future;
    private final Value<Float> width;
    private final Value<Boolean> save;
    private final Value<Integer> threshold;
    private final Random random;
    private int delay;
    private int count;
    public static StorageESP INSTANCE;

    public StorageESP() {
        super("StorageESP", "Highlights storage blocks", 0, Category.RENDER, true);
        this.mode = this.register(new Value<String>("Mode", this, "Solid", new ArrayList<String>(Arrays.asList("Solid", "Full", "Outline", "Shader"))));
        this.all = this.register(new Value<Boolean>("All Tile Entities", this, false));
        this.width = this.register(new Value<Float>("Line Width", this, 1.0f, 1.0f, 10.0f));
        this.save = this.register(new Value<Boolean>("Save coords above threshold", this, false));
        this.threshold = this.register(new Value<Integer>("Threshold", this, 200, 1, 2000));
        this.random = new Random();
        StorageESP.future = this.register(new Value<Boolean>("Future Colors", this, false));
        this.count = 1;
        StorageESP.INSTANCE = this;
    }

    public static int getTileEntityColor(final BlockEntity blockEntity) {
        if (blockEntity instanceof ChestBlockEntity || blockEntity instanceof DispenserBlockEntity) {
            return ColorUtils.Colors.ORANGE;
        }
        if (blockEntity instanceof ShulkerBoxBlockEntity) {
            return ColorUtils.toRGBA(255, 0, 95, 255);
        }
        if (blockEntity instanceof EnderChestBlockEntity) {
            return ColorUtils.Colors.PURPLE;
        }
        if (blockEntity instanceof FurnaceBlockEntity) {
            return ColorUtils.Colors.GRAY;
        }
        if (blockEntity instanceof HopperBlockEntity) {
            return ColorUtils.Colors.DARK_RED;
        }
        if (blockEntity instanceof BarrelBlockEntity) {
            return ColorUtils.Colors.ORANGE;
        }
        return -1;
    }

    public static int getTileEntityColorF(final BlockEntity blockEntity) {
        if (blockEntity instanceof ChestBlockEntity || blockEntity instanceof DispenserBlockEntity || blockEntity instanceof BarrelBlockEntity) {
            return ColorUtils.toRGBA(200, 200, 101, 255);
        }
        if (blockEntity instanceof ShulkerBoxBlockEntity) {
            return ColorUtils.toRGBA(180, 21, 99, 255);
        }
        if (blockEntity instanceof EnderChestBlockEntity) {
            return ColorUtils.toRGBA(155, 0, 200, 255);
        }
        if (blockEntity instanceof FurnaceBlockEntity) {
            return ColorUtils.Colors.GRAY;
        }
        if (blockEntity instanceof HopperBlockEntity) {
            return ColorUtils.Colors.GRAY;
        }
        return -1;
    }

    public boolean isShaderMode() {
        return this.mode.getValue().equalsIgnoreCase("Shader");
    }

    public boolean isShaderTarget(final BlockEntity blockEntity) {
        return this.all.getValue()
            || blockEntity instanceof ChestBlockEntity
            || blockEntity instanceof EnderChestBlockEntity
            || blockEntity instanceof ShulkerBoxBlockEntity;
    }

    public int getConfiguredColor(final BlockEntity blockEntity) {
        return StorageESP.future.getValue() ? getTileEntityColorF(blockEntity) : getTileEntityColor(blockEntity);
    }

    public static <T extends BlockEntity> void renderShaderOutline(final T blockEntity, final BlockEntityRenderer<T> renderer, final float partialTicks, final PoseStack poseStack, final MultiBufferSource bufferSource) {
        try {
            int color = StorageESP.INSTANCE.getConfiguredColor(blockEntity);
            if (color == -1) {
                color = ColorUtils.Colors.WHITE;
            }
            final Color outlineColor = new Color(color, true);
            OutlineUtils.renderOne(StorageESP.INSTANCE.width.getValue());
            renderShaderPass(renderer, blockEntity, partialTicks, poseStack, bufferSource);
            OutlineUtils.renderTwo();
            renderShaderPass(renderer, blockEntity, partialTicks, poseStack, bufferSource);
            OutlineUtils.renderThree();
            renderShaderPass(renderer, blockEntity, partialTicks, poseStack, bufferSource);
            OutlineUtils.renderFour(outlineColor);
            renderShaderPass(renderer, blockEntity, partialTicks, poseStack, bufferSource);
            OutlineUtils.renderFive();
        }
        catch (Throwable throwable) {
            final CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering Block Entity");
            final CrashReportCategory crashReportCategory = crashReport.addCategory("Block Entity Details");
            blockEntity.fillCrashReportCategory(crashReportCategory);
            throw new ReportedException(crashReport);
        }
    }

    private static <T extends BlockEntity> void renderShaderPass(final BlockEntityRenderer<T> renderer, final T blockEntity, final float partialTicks, final PoseStack poseStack, final MultiBufferSource bufferSource) {
        renderer.render(blockEntity, partialTicks, poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
        if (bufferSource instanceof MultiBufferSource.BufferSource) {
            ((MultiBufferSource.BufferSource)bufferSource).endBatch();
        }
    }

    @Override
    public String getHudInfo() {
        final long chests = this.countLoadedChests();
        if (this.save.getValue() && (int)chests >= this.threshold.getValue() && this.delay == 0) {
            StorageESP.fileManager.saveStorageESP(LocalDate.now().toString() + "-" + this.random.nextInt(9999) + " - " + this.count, (int)StorageESP.mc.player.getX() + " " + (int)StorageESP.mc.player.getY() + " " + (int)StorageESP.mc.player.getZ(), String.valueOf(chests));
            this.delay = 4000;
            ++this.count;
        }
        return "Chests: " + chests;
    }

    @Override
    public void onWorldRender(final RenderEvent event) {
        if (this.delay > 0) {
            --this.delay;
        }
        if (StorageESP.mc.level == null || StorageESP.mc.player == null) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Shader")) {
            OutlineUtils.checkSetupFBO();
            return;
        }
        final Vec3 renderPos = event.getRenderPos();
        for (final BlockEntity blockEntity : getLoadedBlockEntities()) {
            int color = this.getConfiguredColor(blockEntity);
            if (color == -1) {
                if (!this.all.getValue()) {
                    continue;
                }
                color = ColorUtils.Colors.WHITE;
            }
            final BlockPos pos = blockEntity.getBlockPos();
            try {
                if (this.mode.getValue().equalsIgnoreCase("Solid")) {
                    XuluTessellator.prepare(7);
                    XuluTessellator.drawBox(pos, this.changeAlpha(color, 100), getSideMask(blockEntity));
                    XuluTessellator.release();
                }
                else if (this.mode.getValue().equalsIgnoreCase("Outline")) {
                    final AABB box = BlockInteractionHelper.getBlockBounds(blockEntity.getBlockState(), StorageESP.mc.level, pos).inflate(0.002).move(-renderPos.x, -renderPos.y, -renderPos.z);
                    XuluTessellator.prepare(7);
                    XuluTessellator.drawBoundingBox(box, 1.5f, color);
                    XuluTessellator.release();
                }
                else if (this.mode.getValue().equalsIgnoreCase("Full")) {
                    final AABB box = BlockInteractionHelper.getBlockBounds(blockEntity.getBlockState(), StorageESP.mc.level, pos).inflate(0.002).move(-renderPos.x, -renderPos.y, -renderPos.z);
                    XuluTessellator.drawFullBox2(box, pos, 1.5f, this.changeAlpha(color, 100));
                }
                else {
                    XuluTessellator.prepare(7);
                    XuluTessellator.drawBox(pos, this.changeAlpha(color, 100), getSideMask(blockEntity));
                    XuluTessellator.release();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private long countLoadedChests() {
        long chests = 0;
        for (final BlockEntity blockEntity : getLoadedBlockEntities()) {
            if (blockEntity instanceof ChestBlockEntity) {
                ++chests;
            }
        }
        return chests;
    }

    private static List<BlockEntity> getLoadedBlockEntities() {
        final List<BlockEntity> blockEntities = new ArrayList<BlockEntity>();
        final ClientLevel level = StorageESP.mc.level;
        if (level == null || StorageESP.mc.player == null) {
            return blockEntities;
        }
        final int radius = StorageESP.mc.options.renderDistance + 4;
        final int centerX = StorageESP.mc.player.chunkPosition().x;
        final int centerZ = StorageESP.mc.player.chunkPosition().z;
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dz = -radius; dz <= radius; ++dz) {
                final LevelChunk chunk = (LevelChunk)level.getChunkSource().getChunk(centerX + dx, centerZ + dz, ChunkStatus.FULL, false);
                if (chunk != null) {
                    blockEntities.addAll(chunk.getBlockEntities().values());
                }
            }
        }
        return blockEntities;
    }

    private static int getSideMask(final BlockEntity blockEntity) {
        int side = 63;
        if (blockEntity instanceof ChestBlockEntity) {
            final BlockState state = blockEntity.getBlockState();
            if (state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                switch (ChestBlock.getConnectedDirection(state)) {
                    case NORTH: {
                        side &= ~0x4;
                        break;
                    }
                    case SOUTH: {
                        side &= ~0x8;
                        break;
                    }
                    case WEST: {
                        side &= ~0x10;
                        break;
                    }
                    case EAST: {
                        side &= ~0x20;
                        break;
                    }
                }
            }
        }
        return side;
    }

    int changeAlpha(final int origColor, final int userInputedAlpha) {
        return userInputedAlpha << 24 | (origColor & 0xFFFFFF);
    }
}
