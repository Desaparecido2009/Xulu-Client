package com.elementars.eclient.module.combat;

import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventMiddleClick;
import com.elementars.eclient.event.events.LocalPlayerUpdateEvent;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;

public class MiddleClickPearl extends Module
{
    private final Value<String> mode;
    private final Value<Integer> delayA;
    private final Value<Integer> delay;
    private int slot;
    private int slot2;
    int time;
    boolean hasClicked;
    boolean isFinishingTask;
    boolean isThrowingPearl;
    public static boolean switchInProgress;
    boolean didFirst;
    int switchDelay1;
    int switchDelay2;

    public MiddleClickPearl() {
        super("MiddleClickPearl", "Throws a pearl without it being in your hotbar", 0, Category.COMBAT, true);
        this.mode = this.register(new Value<String>("Mode", this, "Switch", new String[] { "Switch", "Instant" }));
        this.delayA = this.register(new Value<Integer>("Click Delay", this, 1, 0, 5)).visibleWhen(integer -> this.mode.getValue().equalsIgnoreCase("Switch"));
        this.delay = this.register(new Value<Integer>("Switch Delay", this, 10, 0, 80)).visibleWhen(integer -> this.mode.getValue().equalsIgnoreCase("Switch"));
        this.slot = -1;
        this.slot2 = -1;
        this.hasClicked = false;
        this.isFinishingTask = false;
        this.isThrowingPearl = false;
        this.didFirst = false;
        this.switchDelay1 = -1;
        this.switchDelay2 = -1;
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    private void resetState() {
        MiddleClickPearl.switchInProgress = false;
        this.hasClicked = false;
        this.isFinishingTask = false;
        this.isThrowingPearl = false;
        this.didFirst = false;
        this.switchDelay1 = -1;
        this.switchDelay2 = -1;
        this.slot = -1;
        this.slot2 = -1;
        this.time = 0;
    }

    private void debugMsg(final String message) {
        if (MiddleClickPearl.mc.player != null) {
            MiddleClickPearl.mc.player.displayClientMessage(new TextComponent(message), false);
        }
    }

    @EventTarget
    public void onMiddleClick(final EventMiddleClick event) {
        if (MiddleClickPearl.mc.player == null || MiddleClickPearl.mc.level == null || MiddleClickPearl.mc.gameMode == null) {
            return;
        }
        if (MiddleClickPearl.mc.screen instanceof AbstractContainerScreen) {
            return;
        }
        // En 1.18.2, mirar al aire da un BlockHitResult de tipo MISS: hay que mirar getType()
        final HitResult ray = MiddleClickPearl.mc.hitResult;
        if (ray == null || ray.getType() != HitResult.Type.MISS) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Instant")) {
            this.throwPearl();
            return;
        }
        // Un intercambio ya en curso: ignorar para no pisar slot/slot2
        if (MiddleClickPearl.switchInProgress || this.hasClicked) {
            return;
        }
        if (MiddleClickPearl.mc.player.getMainHandItem().getItem() != Items.ENDER_PEARL && MiddleClickPearl.mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL) {
            MiddleClickPearl.mc.gameMode.useItem(MiddleClickPearl.mc.player, MiddleClickPearl.mc.level, InteractionHand.OFF_HAND);
            MiddleClickPearl.mc.player.swing(InteractionHand.OFF_HAND);
            return;
        }
        final int pearlIndex = this.getSlot();
        if (pearlIndex == -1) {
            this.debugMsg("No pearl found!");
            return;
        }
        if (MiddleClickPearl.mc.player.getMainHandItem().getItem() == Items.ENDER_PEARL) {
            return;
        }
        final int handIndex = this.getSlot2();
        this.slot = (pearlIndex < 9) ? (pearlIndex + 36) : pearlIndex;
        this.slot2 = (handIndex < 9) ? (handIndex + 36) : handIndex;
        this.time = 0;
        MiddleClickPearl.mc.execute(() -> {
            MiddleClickPearl.switchInProgress = true;
            this.isThrowingPearl = true;
        });
        this.hasClicked = true;
    }

    @Override
    public void onUpdate() {
        if (MiddleClickPearl.mc.player == null || MiddleClickPearl.mc.gameMode == null) {
            this.resetState();
            return;
        }
        if (MiddleClickPearl.switchInProgress) {
            if (this.switchDelay1 > 0) {
                --this.switchDelay1;
            }
            if (this.switchDelay2 > 0) {
                --this.switchDelay2;
            }
            if (!this.didFirst) {
                MiddleClickPearl.mc.gameMode.handleInventoryMouseClick(0, this.slot, 0, ClickType.PICKUP, MiddleClickPearl.mc.player);
                this.switchDelay1 = this.delayA.getValue();
                this.didFirst = true;
                return;
            }
            if (this.switchDelay1 == 0) {
                MiddleClickPearl.mc.gameMode.handleInventoryMouseClick(0, this.slot2, 0, ClickType.PICKUP, MiddleClickPearl.mc.player);
                this.switchDelay1 = -1;
                this.switchDelay2 = this.delayA.getValue();
                return;
            }
            if (this.switchDelay2 == 0) {
                MiddleClickPearl.mc.gameMode.handleInventoryMouseClick(0, this.slot, 0, ClickType.PICKUP, MiddleClickPearl.mc.player);
                this.switchDelay1 = -1;
                this.switchDelay2 = -1;
                MiddleClickPearl.switchInProgress = false;
                this.didFirst = false;
                if (this.isThrowingPearl) {
                    MiddleClickPearl.mc.gameMode.useItem(MiddleClickPearl.mc.player, MiddleClickPearl.mc.level, InteractionHand.MAIN_HAND);
                    MiddleClickPearl.mc.player.swing(InteractionHand.MAIN_HAND);
                    this.isThrowingPearl = false;
                }
                if (this.isFinishingTask) {
                    this.slot = -1;
                    this.slot2 = -1;
                    this.time = 0;
                    this.hasClicked = false;
                    this.isFinishingTask = false;
                }
            }
        }
    }

    @EventTarget
    public void onPlayerUpdate(final LocalPlayerUpdateEvent event) {
        if (MiddleClickPearl.mc.player == null) {
            return;
        }
        if (MiddleClickPearl.mc.player.getMainHandItem().getItem() == Items.ENDER_PEARL && this.hasClicked) {
            ++this.time;
            if (this.time < this.delay.getValue()) {
                return;
            }
            MiddleClickPearl.mc.execute(() -> {
                MiddleClickPearl.switchInProgress = true;
                this.isFinishingTask = true;
            });
        }
        else if (this.getSlot() == -1 && this.hasClicked) {
            MiddleClickPearl.mc.execute(() -> {
                MiddleClickPearl.switchInProgress = true;
                this.isFinishingTask = true;
            });
        }
    }

    int getSlot() {
        int slot = -1;
        for (int i = 35; i > -1; --i) {
            if (MiddleClickPearl.mc.player.getInventory().getItem(i).getItem() == Items.ENDER_PEARL) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    /** Slot seleccionado de la hotbar (comparar ItemStack por referencia fallaba con la mano vacía). */
    int getSlot2() {
        return MiddleClickPearl.mc.player.getInventory().selected;
    }

    private void throwPearl() {
        final int pearlSlot = findHotbarBlock(EnderpearlItem.class);
        final boolean offhand = MiddleClickPearl.mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL;
        if (pearlSlot != -1 || offhand) {
            final int oldslot = MiddleClickPearl.mc.player.getInventory().selected;
            if (!offhand) {
                switchToHotbarSlot(pearlSlot, false);
            }
            MiddleClickPearl.mc.gameMode.useItem(MiddleClickPearl.mc.player, MiddleClickPearl.mc.level, offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (!offhand) {
                switchToHotbarSlot(oldslot, false);
            }
        }
    }

    public static void switchToHotbarSlot(final int slot, final boolean silent) {
        if (MiddleClickPearl.mc.player.getInventory().selected == slot || slot < 0) {
            return;
        }
        MiddleClickPearl.mc.player.connection.send(new ServerboundSetCarriedItemPacket(slot));
        if (!silent) {
            MiddleClickPearl.mc.player.getInventory().selected = slot;
        }
    }

    public static void switchToHotbarSlot(final Class<?> clazz, final boolean silent) {
        final int slot = findHotbarBlock(clazz);
        if (slot > -1) {
            switchToHotbarSlot(slot, silent);
        }
    }

    public static int findHotbarBlock(final Class<?> clazz) {
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = MiddleClickPearl.mc.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                if (clazz.isInstance(stack.getItem())) {
                    return i;
                }
                if (stack.getItem() instanceof BlockItem) {
                    final Block block = ((BlockItem)stack.getItem()).getBlock();
                    if (clazz.isInstance(block)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    static {
        MiddleClickPearl.switchInProgress = false;
    }
}