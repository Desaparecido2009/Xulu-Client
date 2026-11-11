package com.elementars.eclient.module.player;

import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventSendPacket;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.phys.Vec3;

public class ItemSpoof extends Module
{
    private boolean send;

    public ItemSpoof() {
        super("ItemSpoof", "Allows you to display a different item server-side(Use the top left slot in your inventory)", 0, Category.PLAYER, true);
    }

    @Override
    public void onDisable() {
        this.send = false;
    }

    @EventTarget
    public void sendPacket(final EventSendPacket event) {
        if (ItemSpoof.mc.player == null || ItemSpoof.mc.getConnection() == null) {
            return;
        }
        if (this.send) {
            this.send = false;
            return;
        }
        boolean use = false;
        if (event.getPacket() instanceof ServerboundUseItemOnPacket) {
            use = true;
        }
        else if (event.getPacket() instanceof ServerboundUseItemPacket) {
            use = true;
        }
        else if (event.getPacket() instanceof ServerboundInteractPacket) {
            final boolean[] attack = { false };
            ((ServerboundInteractPacket)event.getPacket()).dispatch(new ServerboundInteractPacket.Handler() {
                @Override
                public void onInteraction(final InteractionHand hand) {
                }

                @Override
                public void onInteraction(final InteractionHand hand, final Vec3 location) {
                }

                @Override
                public void onAttack() {
                    attack[0] = true;
                }
            });
            use = attack[0];
        }
        if (!use) {
            return;
        }
        event.setCancelled(true);
        this.swap();
        this.send = true;
        ItemSpoof.mc.getConnection().send(event.getPacket());
        this.swap();
    }

    private void swap() {
        ItemSpoof.mc.gameMode.handleInventoryMouseClick(ItemSpoof.mc.player.inventoryMenu.containerId, 9, ItemSpoof.mc.player.getInventory().selected, ClickType.SWAP, ItemSpoof.mc.player);
    }
}
