package com.elementars.eclient.module.combat;

import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.LocalPlayerUpdateEvent;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Offhand extends Module
{
    private Map<String, Item> itemMap;
    private final Value<String> item;
    public final Value<Boolean> conserveGapples;
    private final Value<Boolean> gapOnSword;
    private final Value<Integer> delay;
    public static boolean switchInProgress;
    boolean didFirst;
    int switchDelay1;
    int switchDelay2;
    int slot;

    public Offhand() {
        super("Offhand", "Automatically places items in your offhand", 0, Category.COMBAT, true);
        this.itemMap = new HashMap<String, Item>();
        this.item = this.register(new Value<String>("Item", this, "Crystals", new String[] { "Crystals", "Gapples" }));
        this.conserveGapples = this.register(new Value<Boolean>("Conserve Gap", this, true));
        this.gapOnSword = this.register(new Value<Boolean>("Gap On Sword", this, false));
        this.delay = this.register(new Value<Integer>("Delay", this, 1, 0, 5));
        this.didFirst = false;
        this.switchDelay1 = -1;
        this.switchDelay2 = -1;
        this.slot = -1;
        this.itemMap.put("Crystals", Items.END_CRYSTAL);
        this.itemMap.put("Gapples", Items.GOLDEN_APPLE);
    }

    @Override
    public void onUpdate() {
        if (Offhand.switchInProgress) {
            return;
        }
        if (Offhand.mc.screen instanceof AbstractContainerScreen) {
            return;
        }
        if (AutoTotem.offhand_delay != 0) {
            return;
        }
        if (this.isOk() && !this.isTarget(Offhand.mc.player.getOffhandItem())) {
            final int slot = (this.getSlot() < 9) ? (this.getSlot() + 36) : this.getSlot();
            if (this.getSlot() != -1) {
                this.slot = slot;
                Offhand.switchInProgress = true;
            }
        }
    }

    @EventTarget
    public void onPlayerUpdate(final LocalPlayerUpdateEvent event) {
        if (Offhand.switchInProgress) {
            if (this.switchDelay1 > 0) {
                --this.switchDelay1;
            }
            if (this.switchDelay2 > 0) {
                --this.switchDelay2;
            }
            if (!this.didFirst) {
                Offhand.mc.gameMode.handleInventoryMouseClick(0, this.slot, 0, ClickType.PICKUP, Offhand.mc.player);
                this.switchDelay1 = this.delay.getValue();
                this.didFirst = true;
                return;
            }
            if (this.switchDelay1 == 0) {
                Offhand.mc.gameMode.handleInventoryMouseClick(0, 45, 0, ClickType.PICKUP, Offhand.mc.player);
                this.switchDelay1 = -1;
                this.switchDelay2 = this.delay.getValue();
                return;
            }
            if (this.switchDelay2 == 0) {
                Offhand.mc.gameMode.handleInventoryMouseClick(0, this.slot, 0, ClickType.PICKUP, Offhand.mc.player);
                this.switchDelay1 = -1;
                this.switchDelay2 = -1;
                this.slot = -1;
                Offhand.switchInProgress = false;
                this.didFirst = false;
            }
        }
    }

    private boolean isOk() {
        return Offhand.mc.player.getHealth() + Offhand.mc.player.getAbsorptionAmount() > Module.getModuleT(AutoTotem.class).health.getValue() && Offhand.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA;
    }

    String getItem() {
        final Item mainhand = Offhand.mc.player.getMainHandItem().getItem();
        if (this.gapOnSword.getValue() && (mainhand == Items.DIAMOND_SWORD || mainhand == Items.NETHERITE_SWORD)) {
            return "Gapples";
        }
        return this.item.getValue();
    }

    boolean isTarget(final ItemStack stack, final String target) {
        final Item item = this.itemMap.get(target);
        if (item == Items.GOLDEN_APPLE) {
            return stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE;
        }
        return stack.getItem() == item;
    }

    boolean isTarget(final ItemStack stack) {
        return this.isTarget(stack, this.getItem());
    }

    int getSlot() {
        int slot = -1;
        for (int i = 45; i > -1; --i) {
            if (this.isTarget(Offhand.mc.player.getInventory().getItem(i))) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    @Override
    public String getHudInfo() {
        int items = 0;
        for (int i = 45; i > -1; --i) {
            if (this.isTarget(Offhand.mc.player.getInventory().getItem(i), this.item.getValue())) {
                items += Offhand.mc.player.getInventory().getItem(i).getCount();
            }
        }
        return items + "";
    }

    static {
        Offhand.switchInProgress = false;
    }
}
