package com.elementars.eclient.module.combat;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.item.BowItem;

public class FastBow extends Module
{
    public FastBow() {
        super("FastBow", "Not a slow bow", 0, Category.COMBAT, true);
    }

    @Override
    public void onUpdate() {
        if (FastBow.mc.player.getMainHandItem().getItem() instanceof BowItem && FastBow.mc.player.isUsingItem() && FastBow.mc.player.getTicksUsingItem() >= 3) {
            FastBow.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, FastBow.mc.player.getDirection()));
            FastBow.mc.player.connection.send(new ServerboundUseItemPacket(FastBow.mc.player.getUsedItemHand()));
            FastBow.mc.player.stopUsingItem();
        }
    }
}
