package com.elementars.eclient.event.events;

import com.elementars.eclient.module.misc.Announcer;
import com.elementars.eclient.util.AnnouncerUtil;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class AnnouncerRegistry
{
    private final Announcer announcer;
    private final Minecraft minecraft;
    private String heldItem;
    private boolean wasEating;
    private boolean wasDead;
    private Map<String, Integer> lastInventory;
    private BlockPos blockPos;
    private String blockName;
    private int breakTicks;
    private int dropItemDelay;
    private int itemPickUpDelay;
    private int blockPlacedDelay;
    private int blockBrokeDelay;
    private int chatDelay;
    private int commandDelay;
    private int pauseDelay;
    private int inventoryDelay;
    private int playerListDelay;
    private int perspectivesDelay;
    private int crouchedDelay;
    private int jumpDelay;
    private int attackDelay;
    private int eattingDelay;
    private long lastPositionUpdate;
    private double lastPositionX;
    private double lastPositionY;
    private double lastPositionZ;

    public AnnouncerRegistry(final Announcer announcer) {
        this.announcer = announcer;
        this.minecraft = Minecraft.getInstance();
        this.heldItem = "";
        this.blockName = "";
        this.breakTicks = -1;
    }

    public void reset() {
        Announcer.delayy = 0;
        this.heldItem = "";
        this.wasEating = false;
        this.wasDead = false;
        this.lastInventory = null;
        this.breakTicks = -1;
        this.dropItemDelay = 0;
        this.itemPickUpDelay = 0;
        this.blockPlacedDelay = 0;
        this.blockBrokeDelay = 0;
        this.chatDelay = 0;
        this.commandDelay = 0;
        this.pauseDelay = 0;
        this.inventoryDelay = 0;
        this.playerListDelay = 0;
        this.perspectivesDelay = 0;
        this.crouchedDelay = 0;
        this.jumpDelay = 0;
        this.attackDelay = 0;
        this.eattingDelay = 0;
        this.lastPositionUpdate = 0L;
        this.lastPositionX = 0.0;
        this.lastPositionY = 0.0;
        this.lastPositionZ = 0.0;
    }

    private void sendMessage(final String string) {
        if (this.minecraft.player == null) {
            return;
        }
        if (Announcer.delayy == 0) {
            this.minecraft.player.chat(string);
            Announcer.delayy = Announcer.delay.getValue() * 20;
        }
    }

    private ItemStack getHeldStack() {
        if (this.minecraft.player == null) {
            return ItemStack.EMPTY;
        }
        return this.minecraft.player.getInventory().getSelected();
    }

    private String getItemName(final ItemStack stack) {
        return stack.isEmpty() ? "Air" : stack.getHoverName().getString();
    }

    private Map<String, Integer> snapshotInventory(final LocalPlayer player) {
        final Map<String, Integer> counts = new HashMap<String, Integer>();
        final int size = player.getInventory().getContainerSize();
        for (int i = 0; i < size; ++i) {
            final ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                counts.merge(stack.getHoverName().getString(), stack.getCount(), Integer::sum);
            }
        }
        return counts;
    }

    @SubscribeEvent
    public void livingUpdate(final TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof LocalPlayer)) {
            return;
        }
        if (this.minecraft.player == null || !this.announcer.isToggled()) {
            return;
        }
        final LocalPlayer player = this.minecraft.player;
        this.heldItem = this.getItemName(this.getHeldStack());
        ++this.dropItemDelay;
        ++this.itemPickUpDelay;
        ++this.blockPlacedDelay;
        ++this.blockBrokeDelay;
        ++this.chatDelay;
        ++this.commandDelay;
        ++this.pauseDelay;
        ++this.inventoryDelay;
        ++this.playerListDelay;
        ++this.perspectivesDelay;
        ++this.crouchedDelay;
        ++this.jumpDelay;
        ++this.attackDelay;
        ++this.eattingDelay;
        final long now = System.currentTimeMillis();
        if (this.lastPositionUpdate == 0L) {
            this.lastPositionUpdate = now;
            this.lastPositionX = player.getX();
            this.lastPositionY = player.getY();
            this.lastPositionZ = player.getZ();
        }
        else if (this.lastPositionUpdate + 30000L < now && Announcer.walk.getValue()) {
            final double d0 = player.getX() - this.lastPositionX;
            final double d1 = player.getY() - this.lastPositionY;
            final double d2 = player.getZ() - this.lastPositionZ;
            final double distance = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            this.lastPositionUpdate = now;
            this.lastPositionX = player.getX();
            this.lastPositionY = player.getY();
            this.lastPositionZ = player.getZ();
            if (distance > 0.0 && distance <= 5000.0) {
                this.sendMessage(AnnouncerUtil.getMove(new DecimalFormat("#").format(distance)));
            }
        }
        if (this.breakTicks >= 0) {
            if (this.minecraft.level != null && this.minecraft.level.getBlockState(this.blockPos).isAir()) {
                if (this.blockBrokeDelay >= 300 && Announcer.blockBroke.getValue()) {
                    this.sendMessage(AnnouncerUtil.getBlockBreak(this.blockName));
                    this.blockBrokeDelay = 0;
                }
                this.breakTicks = -1;
            }
            else if (++this.breakTicks > 200) {
                this.breakTicks = -1;
            }
        }
        final boolean usingFood = player.isUsingItem() && player.getUseItem().getItem().isEdible();
        if (usingFood && !this.wasEating && this.eattingDelay >= 300 && Announcer.eatting.getValue()) {
            this.sendMessage(AnnouncerUtil.getAte(player.getUseItem().getHoverName().getString()));
            this.eattingDelay = 0;
        }
        this.wasEating = usingFood;

        // Respawn (detectado en el cliente)
        if (player.isDeadOrDying()) {
            this.wasDead = true;
        }
        else if (this.wasDead) {
            this.wasDead = false;
            if (Announcer.respawn.getValue()) {
                this.sendMessage(AnnouncerUtil.getRespawn());
            }
        }

        // PickUpItem (detectado en el cliente comparando el inventario)
        final Map<String, Integer> current = this.snapshotInventory(player);
        if (this.lastInventory != null && this.minecraft.screen == null && this.itemPickUpDelay >= 150 && Announcer.pickUpItem.getValue()) {
            for (final Map.Entry<String, Integer> entry : current.entrySet()) {
                final Integer before = this.lastInventory.get(entry.getKey());
                if (before == null || entry.getValue() > before) {
                    this.sendMessage(AnnouncerUtil.getPickedUp(entry.getKey()));
                    this.itemPickUpDelay = 0;
                    break;
                }
            }
        }
        this.lastInventory = current;
    }

    @SubscribeEvent
    public void itemCrafted(final PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() == this.minecraft.player && this.announcer.isToggled() && Announcer.craftedItem.getValue()) {
            final ItemStack crafting = event.getCrafting();
            this.sendMessage(AnnouncerUtil.getCraft(crafting.getCount() + " " + crafting.getHoverName().getString()));
        }
    }

    @SubscribeEvent
    public void itemSmelted(final PlayerEvent.ItemSmeltedEvent event) {
        if (event.getEntity() == this.minecraft.player && this.announcer.isToggled() && Announcer.smeltedItem.getValue()) {
            final ItemStack smelting = event.getSmelting();
            this.sendMessage(AnnouncerUtil.getSmelted(smelting.getCount() + " " + smelting.getHoverName().getString()));
        }
    }

    @SubscribeEvent
    public void playerBlockPlaced(final PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() == this.minecraft.player && this.announcer.isToggled() && this.blockPlacedDelay >= 150 && Announcer.blockPlaced.getValue() && event.getItemStack().getItem() instanceof BlockItem) {
            this.sendMessage(AnnouncerUtil.getPlaced(event.getItemStack().getHoverName().getString()));
            this.blockPlacedDelay = 0;
        }
    }

    @SubscribeEvent
    public void playerBlockBroke(final PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() != this.minecraft.player || this.minecraft.level == null) {
            return;
        }
        this.blockPos = event.getPos();
        this.blockName = this.minecraft.level.getBlockState(event.getPos()).getBlock().getName().getString();
        this.breakTicks = 0;
    }

    @SubscribeEvent
    public void onKeyInput(final InputEvent.KeyInputEvent event) {
        if (event.getAction() != GLFW.GLFW_PRESS || this.minecraft.player == null || this.minecraft.screen != null || !this.announcer.isToggled()) {
            return;
        }
        final Options options = this.minecraft.options;
        final int key = event.getKey();
        final int scanCode = event.getScanCode();
        if (options.keyDrop.matches(key, scanCode) && !"Air".equals(this.heldItem) && this.dropItemDelay >= 150 && Announcer.itemDroped.getValue()) {
            this.sendMessage(AnnouncerUtil.getDropped(this.heldItem));
            this.dropItemDelay = 0;
        }
        if (options.keyChat.matches(key, scanCode) && this.chatDelay >= 150 && Announcer.openChat.getValue()) {
            this.sendMessage(AnnouncerUtil.getChat());
            this.chatDelay = 0;
        }
        if (options.keyCommand.matches(key, scanCode) && this.commandDelay >= 150 && Announcer.command.getValue()) {
            this.sendMessage(AnnouncerUtil.getConsole());
            this.commandDelay = 0;
        }
        if (options.keyFullscreen.matches(key, scanCode) && Announcer.fullScreen.getValue()) {
            this.sendMessage(AnnouncerUtil.getFullScreen());
        }
        if (key == GLFW.GLFW_KEY_ESCAPE && this.pauseDelay >= 150 && Announcer.pauseGame.getValue()) {
            this.sendMessage(AnnouncerUtil.getPause());
            this.pauseDelay = 0;
        }
        if (options.keyInventory.matches(key, scanCode) && this.inventoryDelay >= 150 && Announcer.openInv.getValue()) {
            this.sendMessage(AnnouncerUtil.getInventory());
            this.inventoryDelay = 0;
        }
        if (options.keyPlayerList.matches(key, scanCode) && this.playerListDelay >= 150 && Announcer.playerList.getValue()) {
            this.sendMessage(AnnouncerUtil.getPlayerList());
            this.playerListDelay = 0;
        }
        if (options.keyScreenshot.matches(key, scanCode) && Announcer.screenShot.getValue()) {
            this.sendMessage(AnnouncerUtil.getScreenShot());
        }
        if (options.keySwapOffhand.matches(key, scanCode) && Announcer.swapHand.getValue()) {
            this.sendMessage(AnnouncerUtil.getSwappedHands());
        }
        if (options.keyShift.matches(key, scanCode) && this.crouchedDelay >= 150 && Announcer.sneak.getValue()) {
            this.sendMessage(AnnouncerUtil.getCrouched());
            this.crouchedDelay = 0;
        }
        if (options.keyTogglePerspective.matches(key, scanCode) && this.perspectivesDelay >= 150 && Announcer.Perspective.getValue()) {
            this.sendMessage(AnnouncerUtil.getPerspectives());
            this.perspectivesDelay = 0;
        }
        if (options.keyJump.matches(key, scanCode) && this.jumpDelay >= 300 && Announcer.jump.getValue()) {
            this.sendMessage(AnnouncerUtil.getJumped());
            this.jumpDelay = 0;
        }
    }

    @SubscribeEvent
    public void onMouseInput(final InputEvent.MouseInputEvent event) {
        if (event.getAction() != GLFW.GLFW_PRESS || this.minecraft.player == null || this.minecraft.screen != null || !this.announcer.isToggled()) {
            return;
        }
        if (this.minecraft.options.keyPickItem.matchesMouse(event.getButton()) && Announcer.pickBlock.getValue()) {
            this.sendMessage(AnnouncerUtil.getPickBlock());
        }
    }

    @SubscribeEvent
    public void onAttack(final AttackEntityEvent event) {
        if (event.getEntity() != this.minecraft.player || !this.announcer.isToggled()) {
            return;
        }
        if (this.attackDelay >= 300 && Announcer.attack.getValue()) {
            this.sendMessage(AnnouncerUtil.getAttacked(event.getTarget().getName().getString(), this.getItemName(this.getHeldStack())));
            this.attackDelay = 0;
        }
    }
}