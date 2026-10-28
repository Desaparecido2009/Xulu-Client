package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.guirewrite.Element;
import com.elementars.eclient.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HoleHud extends Element
{
    private final BlockPos NORTH;
    private final BlockPos EAST;
    private final BlockPos SOUTH;
    private final BlockPos WEST;

    public HoleHud() {
        super("HoleHud");
        this.NORTH = new BlockPos(0, 0, -1);
        this.EAST = new BlockPos(1, 0, 0);
        this.SOUTH = new BlockPos(0, 0, 1);
        this.WEST = new BlockPos(-1, 0, 0);
    }

    @Override
    public void onEnable() {
        this.width = 48.0;
        this.height = 48.0;
        super.onEnable();
    }

    @Override
    public void onRender() {
        if (HoleHud.mc.player == null || HoleHud.mc.level == null) {
            return;
        }
        Entity camera = HoleHud.mc.getCameraEntity();
        final Direction direction = (camera != null ? camera : HoleHud.mc.player).getDirection();
        switch (direction) {
            case NORTH: {
                this.itemrender(this.getNorth(), (int)this.x, (int)this.y);
                break;
            }
            case EAST: {
                this.itemrender(this.getEast(), (int)this.x, (int)this.y);
                break;
            }
            case SOUTH: {
                this.itemrender(this.getSouth(), (int)this.x, (int)this.y);
                break;
            }
            case WEST: {
                this.itemrender(this.getWest(), (int)this.x, (int)this.y);
                break;
            }
        }
    }

    private boolean isBrockOrObby(final BlockPos pos) {
        return HoleHud.mc.level.getBlockState(pos).getBlock() == Blocks.BEDROCK || HoleHud.mc.level.getBlockState(pos).getBlock() == Blocks.OBSIDIAN;
    }

    private ItemStack holeItem(final BlockPos pos) {
        if (!this.isBrockOrObby(pos)) {
            return ItemStack.EMPTY;
        }
        final BlockState state = HoleHud.mc.level.getBlockState(pos);
        return new ItemStack(state.getBlock().asItem());
    }

    private List<ItemStack> getEast() {
        final BlockPos playerPos = HoleHud.mc.player.blockPosition();
        return new ArrayList<ItemStack>(Arrays.asList(this.holeItem(playerPos.offset(this.EAST)), this.holeItem(playerPos.offset(this.SOUTH)), this.holeItem(playerPos.offset(this.WEST)), this.holeItem(playerPos.offset(this.NORTH))));
    }

    private List<ItemStack> getSouth() {
        final BlockPos playerPos = HoleHud.mc.player.blockPosition();
        return new ArrayList<ItemStack>(Arrays.asList(this.holeItem(playerPos.offset(this.SOUTH)), this.holeItem(playerPos.offset(this.WEST)), this.holeItem(playerPos.offset(this.NORTH)), this.holeItem(playerPos.offset(this.EAST))));
    }

    private List<ItemStack> getWest() {
        final BlockPos playerPos = HoleHud.mc.player.blockPosition();
        return new ArrayList<ItemStack>(Arrays.asList(this.holeItem(playerPos.offset(this.WEST)), this.holeItem(playerPos.offset(this.NORTH)), this.holeItem(playerPos.offset(this.EAST)), this.holeItem(playerPos.offset(this.SOUTH))));
    }

    private List<ItemStack> getNorth() {
        final BlockPos playerPos = HoleHud.mc.player.blockPosition();
        return new ArrayList<ItemStack>(Arrays.asList(this.holeItem(playerPos.offset(this.NORTH)), this.holeItem(playerPos.offset(this.EAST)), this.holeItem(playerPos.offset(this.SOUTH)), this.holeItem(playerPos.offset(this.WEST))));
    }

    private void itemrender(final List<ItemStack> items, final int x, final int y) {
        final List<Pair<Integer, Integer>> coordinates = new ArrayList<Pair<Integer, Integer>>(Arrays.asList(new Pair<Integer, Integer>(x + 16, y), new Pair<Integer, Integer>(x + 32, y + 16), new Pair<Integer, Integer>(x + 16, y + 32), new Pair<Integer, Integer>(x, y + 16)));
        for (int item = 0; item < 4; ++item) {
            HoleHud.mc.getItemRenderer().renderAndDecorateItem(items.get(item), coordinates.get(item).getKey(), coordinates.get(item).getValue());
        }
    }
}
