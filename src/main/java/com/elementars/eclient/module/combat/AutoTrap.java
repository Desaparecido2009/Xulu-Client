package com.elementars.eclient.module.combat;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.event.events.RenderEvent;
import com.elementars.eclient.friend.Friends;
import com.elementars.eclient.mixin.mixins.MixinMinecraft;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.module.ModuleManager;
import com.elementars.eclient.util.BlockInteractionHelper;
import com.elementars.eclient.util.EntityUtil;
import com.elementars.eclient.util.XuluTessellator;
import dev.xulu.settings.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AutoTrap extends Module
{
    private ArrayList<String> options;
    private final Value<Float> range;
    private final Value<Integer> blocksPerTick;
    private final Value<Integer> tickDelay;
    private final Value<String> cage;
    private final Value<Boolean> rotate;
    private final Value<Boolean> noGlitchBlocks;
    private final Value<Boolean> activeInFreecam;
    private final Value<Boolean> announceUsage;
    private final Value<Boolean> toggleoff;
    private final Value<Boolean> turnOffCauras;
    private final Value<Boolean> esp;
    private final Value<String> mode;
    private final Value<Integer> red;
    private final Value<Integer> green;
    private final Value<Integer> blue;
    private final Value<Integer> alpha;
    private final Value<Integer> oalpha;
    private final Value<Boolean> expand;
    private final Value<Boolean> selfDisable;
    private Player closestTarget;
    private String lastTickTargetName;
    private int playerHotbarSlot;
    private int lastHotbarSlot;
    private int delayStep;
    private boolean isSneaking;
    private int offsetStep;
    private boolean firstRun;
    private int test;
    private Set<BlockPos> placeList;
    int cDelay;
    String caura;
    boolean isDisabling;
    boolean hasDisabled;

    public AutoTrap() {
        super("AutoTrap", "Automatically traps people", 0, Category.COMBAT, true);
        this.range = this.register(new Value<Float>("Range", this, 4.5f, 3.5f, 32.0f));
        this.blocksPerTick = this.register(new Value<Integer>("BlocksPerTick", this, 2, 1, 23));
        this.tickDelay = this.register(new Value<Integer>("TickDelay", this, 2, 0, 10));
        this.cage = this.register(new Value<String>("Cage", this, "Trap", new ArrayList<String>(Arrays.asList("Trap", "TrapTop", "TrapFullRoof", "TrapFullRoofTop", "Crystalexa", "Crystal", "CrystalFullRoof"))));
        this.rotate = this.register(new Value<Boolean>("Rotate", this, false));
        this.noGlitchBlocks = this.register(new Value<Boolean>("NoGlitchBlocks", this, true));
        this.activeInFreecam = this.register(new Value<Boolean>("ActiveInFreecam", this, true));
        this.announceUsage = this.register(new Value<Boolean>("AnnounceUsage", this, true));
        this.toggleoff = this.register(new Value<Boolean>("Toggle Off", this, false));
        this.turnOffCauras = this.register(new Value<Boolean>("Toggle Other Cauras", this, false));
        this.esp = this.register(new Value<Boolean>("Show esp", this, false));
        this.mode = this.register(new Value<String>("Esp Mode", this, "Solid", new String[] { "Solid", "Outline", "Full" }));
        this.red = this.register(new Value<Integer>("Red", this, 0, 0, 255));
        this.green = this.register(new Value<Integer>("Green", this, 255, 0, 255));
        this.blue = this.register(new Value<Integer>("Blue", this, 255, 0, 255));
        this.alpha = this.register(new Value<Integer>("Alpha", this, 70, 0, 255));
        this.oalpha = this.register(new Value<Integer>("Outline Alpha", this, 70, 0, 255));
        this.expand = this.register(new Value<Boolean>("Expand", this, true));
        this.selfDisable = this.register(new Value<Boolean>("SelfDisable", this, true));
        this.placeList = new HashSet<BlockPos>();
        this.cDelay = 0;
        this.playerHotbarSlot = -1;
        this.lastHotbarSlot = -1;
        this.delayStep = 0;
        this.isSneaking = false;
        this.offsetStep = 0;
    }

    @Override
    public void onEnable() {
        this.test = 0;
        if (AutoTrap.mc.player == null) {
            this.disable();
            return;
        }
        this.hasDisabled = false;
        this.firstRun = true;
        this.offsetStep = 0;
        this.playerHotbarSlot = AutoTrap.mc.player.getInventory().selected;
        this.lastHotbarSlot = -1;
    }

    @Override
    public void onDisable() {
        if (AutoTrap.mc.player == null) {
            return;
        }
        if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
            AutoTrap.mc.player.getInventory().selected = this.playerHotbarSlot;
        }
        if (this.isSneaking) {
            AutoTrap.mc.player.connection.send(new ServerboundPlayerCommandPacket(AutoTrap.mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
            this.isSneaking = false;
        }
        this.playerHotbarSlot = -1;
        this.lastHotbarSlot = -1;
        this.placeList.clear();
        if (this.announceUsage.getValue()) {
            this.sendDebugMessage(ChatFormatting.RED.toString() + "Disabled!");
        }
    }

    @Override
    public void onWorldRender(final RenderEvent event) {
        if (this.esp.getValue()) {
            final int color1 = this.red.getValue();
            final int color2 = this.green.getValue();
            final int color3 = this.blue.getValue();
            for (final BlockPos pos : this.placeList) {
                if (this.mode.getValue().equalsIgnoreCase("Solid")) {
                    XuluTessellator.prepare(7);
                    XuluTessellator.drawBox(pos, color1, color2, color3, this.alpha.getValue(), 63);
                    XuluTessellator.release();
                }
                else if (this.mode.getValue().equalsIgnoreCase("Outline")) {
                    final BlockState state2 = AutoTrap.mc.level.getBlockState(pos);
                    final Vec3 interp2 = event.getRenderPos();
                    final AABB box2 = BlockInteractionHelper.getBlockBounds(state2, AutoTrap.mc.level, pos).inflate(0.0020000000949949026).move(-interp2.x, -interp2.y, -interp2.z);
                    XuluTessellator.drawBoundingBox(box2, 1.5f, color1, color2, color3, this.alpha.getValue());
                }
                else {
                    if (!this.mode.getValue().equalsIgnoreCase("Full")) {
                        continue;
                    }
                    final BlockState state3 = AutoTrap.mc.level.getBlockState(pos);
                    final Vec3 interp3 = event.getRenderPos();
                    final AABB box3 = BlockInteractionHelper.getBlockBounds(state3, AutoTrap.mc.level, pos).inflate(0.0020000000949949026).move(-interp3.x, -interp3.y, -interp3.z);
                    XuluTessellator.drawFullBox(box3, pos, 1.5f, color1, color2, color3, this.alpha.getValue(), this.oalpha.getValue());
                }
            }
        }
    }

    @Override
    public void onUpdate() {
        if (this.cDelay > 0) {
            --this.cDelay;
        }
        if (this.cDelay == 0 && this.isDisabling && Xulu.MODULE_MANAGER.getModuleByName(this.caura) != null) {
            Xulu.MODULE_MANAGER.getModuleByName(this.caura).toggle();
            this.isDisabling = false;
            this.hasDisabled = true;
        }
        if (Xulu.MODULE_MANAGER.getModuleByName("AutoCrystal") != null && Xulu.MODULE_MANAGER.getModuleByName("AutoCrystal").isToggled() && this.turnOffCauras.getValue() && !this.hasDisabled) {
            this.caura = "AutoCrystal";
            this.cDelay = 19;
            this.isDisabling = true;
            Xulu.MODULE_MANAGER.getModuleByName(this.caura).toggle();
        }
        if (Xulu.MODULE_MANAGER.getModuleByName("AutoCrystalO") != null && Xulu.MODULE_MANAGER.getModuleByName("AutoCrystalO").isToggled() && this.turnOffCauras.getValue() && !this.hasDisabled) {
            this.caura = "AutoCrystalO";
            this.cDelay = 19;
            this.isDisabling = true;
            Xulu.MODULE_MANAGER.getModuleByName(this.caura).toggle();
        }
        if (Xulu.MODULE_MANAGER.getModuleByName("AutoCrystalX") != null && Xulu.MODULE_MANAGER.getModuleByName("AutoCrystalX").isToggled() && this.turnOffCauras.getValue() && !this.hasDisabled) {
            this.caura = "AutoCrystalX";
            this.cDelay = 19;
            this.isDisabling = true;
            Xulu.MODULE_MANAGER.getModuleByName(this.caura).toggle();
        }
        if (this.toggleoff.getValue()) {
            ++this.test;
            if (this.test == 20) {
                super.toggle();
            }
        }
        if (AutoTrap.mc.player == null) {
            return;
        }
        if (!this.activeInFreecam.getValue() && ModuleManager.isModuleEnabled("Freecam")) {
            return;
        }
        if (!this.firstRun) {
            if (this.delayStep < this.tickDelay.getValue()) {
                ++this.delayStep;
                return;
            }
            this.delayStep = 0;
        }
        this.findClosestTarget();
        if (this.closestTarget == null) {
            if (this.firstRun) {
                this.firstRun = false;
                if (this.announceUsage.getValue()) {
                    this.sendDebugMessage(ChatFormatting.GREEN + "enabled" + ChatFormatting.RESET + ", waiting for target.");
                }
            }
            return;
        }
        if (this.firstRun) {
            this.firstRun = false;
            this.lastTickTargetName = this.closestTarget.getName().getString();
            if (this.announceUsage.getValue()) {
                this.sendDebugMessage(ChatFormatting.GREEN + "enabled" + ChatFormatting.RESET + ", target: " + this.lastTickTargetName);
            }
        }
        else if (!this.lastTickTargetName.equals(this.closestTarget.getName().getString())) {
            this.lastTickTargetName = this.closestTarget.getName().getString();
            this.offsetStep = 0;
            if (this.announceUsage.getValue()) {
                this.sendDebugMessage("New target: " + this.lastTickTargetName);
            }
        }
        final List<Vec3> offsetTargets = new ArrayList<Vec3>();
        if (this.cage.getValue().equalsIgnoreCase("Trap")) {
            Collections.addAll(offsetTargets, Offsets.TRAP);
        }
        if (this.cage.getValue().equalsIgnoreCase("TrapTop")) {
            Collections.addAll(offsetTargets, Offsets.TRAPTOP);
        }
        if (this.cage.getValue().equalsIgnoreCase("TrapFullRoof")) {
            Collections.addAll(offsetTargets, Offsets.TRAPFULLROOF);
        }
        if (this.cage.getValue().equalsIgnoreCase("TrapFullRoofTop")) {
            Collections.addAll(offsetTargets, Offsets.TRAPFULLROOFTOP);
        }
        if (this.cage.getValue().equalsIgnoreCase("Crystalexa")) {
            Collections.addAll(offsetTargets, Offsets.CRYSTALEXA);
        }
        if (this.cage.getValue().equalsIgnoreCase("Crystal")) {
            Collections.addAll(offsetTargets, Offsets.CRYSTAL);
        }
        if (this.cage.getValue().equalsIgnoreCase("CrystalFullRoof")) {
            Collections.addAll(offsetTargets, Offsets.CRYSTALFULLROOF);
        }
        final List<BlockPos> placeTargets = this.getPlaceTargets(this.closestTarget, offsetTargets);
        int blocksPlaced = 0;
        if (!placeTargets.isEmpty()) {
            if (this.offsetStep >= placeTargets.size()) {
                this.offsetStep = 0;
            }
            while (blocksPlaced < this.blocksPerTick.getValue() && this.offsetStep < placeTargets.size()) {
                final BlockPos targetPos = placeTargets.get(this.offsetStep);
                this.placeList.add(targetPos);
                if (this.placeBlockInRange(targetPos, this.range.getValue())) {
                    ++blocksPlaced;
                }
                ++this.offsetStep;
            }
            if (this.offsetStep >= placeTargets.size()) {
                this.offsetStep = 0;
            }
        }
        if (blocksPlaced > 0) {
            if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
                AutoTrap.mc.player.getInventory().selected = this.playerHotbarSlot;
                this.lastHotbarSlot = this.playerHotbarSlot;
            }
            if (this.isSneaking) {
                AutoTrap.mc.player.connection.send(new ServerboundPlayerCommandPacket(AutoTrap.mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
                this.isSneaking = false;
            }
        }
        if (this.selfDisable.getValue() && this.isPlacementComplete(placeTargets)) {
            this.disable();
        }
    }

    private List<BlockPos> getBasePositions(final Player target) {
        final List<BlockPos> positions = new ArrayList<BlockPos>();
        if (!this.expand.getValue()) {
            positions.add(new BlockPos(target.position()).below());
            return positions;
        }
        final AABB box = target.getBoundingBox();
        final int minX = Mth.floor(box.minX);
        final int maxX = Mth.floor(box.maxX - 1.0E-7);
        final int minZ = Mth.floor(box.minZ);
        final int maxZ = Mth.floor(box.maxZ - 1.0E-7);
        final int y = Mth.floor(target.getY()) - 1;
        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                positions.add(new BlockPos(x, y, z));
            }
        }
        return positions;
    }

    private List<BlockPos> getPlaceTargets(final Player target, final List<Vec3> offsets) {
        final Set<BlockPos> targets = new LinkedHashSet<BlockPos>();
        for (final BlockPos basePos : this.getBasePositions(target)) {
            for (final Vec3 offset : offsets) {
                final BlockPos targetPos = basePos.offset((int)offset.x, (int)offset.y, (int)offset.z);
                if (target.getBoundingBox().intersects(new AABB(targetPos))) {
                    continue;
                }
                targets.add(targetPos);
            }
        }
        return new ArrayList<BlockPos>(targets);
    }

    private boolean isPlacementComplete(final List<BlockPos> placeTargets) {
        if (placeTargets.isEmpty()) {
            return false;
        }
        for (final BlockPos pos : placeTargets) {
            final Block block = AutoTrap.mc.level.getBlockState(pos).getBlock();
            if (block instanceof AirBlock || block instanceof LiquidBlock) {
                return false;
            }
        }
        return true;
    }

    private boolean placeBlockInRange(final BlockPos pos, final double range) {
        final Block block = AutoTrap.mc.level.getBlockState(pos).getBlock();
        if (!(block instanceof AirBlock) && !(block instanceof LiquidBlock)) {
            this.placeList.remove(pos);
            return false;
        }
        for (final Entity entity : AutoTrap.mc.level.getEntities((Entity)null, new AABB(pos))) {
            if (!(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrb)) {
                return false;
            }
        }
        final Direction side = BlockInteractionHelper.getPlaceableSide(pos);
        if (side == null) {
            return false;
        }
        final BlockPos neighbour = pos.relative(side);
        final Direction opposite = side.getOpposite();
        if (!BlockInteractionHelper.canBeClicked(neighbour)) {
            return false;
        }
        final Vec3 hitVec = Vec3.atLowerCornerOf(neighbour).add(0.5, 0.5, 0.5).add(Vec3.atLowerCornerOf(opposite.getNormal()).scale(0.5));
        final Block neighbourBlock = AutoTrap.mc.level.getBlockState(neighbour).getBlock();
        if (AutoTrap.mc.player.position().distanceTo(hitVec) > range) {
            return false;
        }
        final int obiSlot = this.findObiInHotbar();
        if (obiSlot == -1) {
            this.disable();
            return false;
        }
        if (this.lastHotbarSlot != obiSlot) {
            AutoTrap.mc.player.getInventory().selected = obiSlot;
            this.lastHotbarSlot = obiSlot;
        }
        if ((!this.isSneaking && BlockInteractionHelper.blackList.contains(neighbourBlock)) || BlockInteractionHelper.shulkerList.contains(neighbourBlock)) {
            AutoTrap.mc.player.connection.send(new ServerboundPlayerCommandPacket(AutoTrap.mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
            this.isSneaking = true;
        }
        if (this.rotate.getValue()) {
            BlockInteractionHelper.faceVectorPacketInstant(hitVec);
        }
        AutoTrap.mc.gameMode.useItemOn(AutoTrap.mc.player, AutoTrap.mc.level, InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, opposite, neighbour, false));
        AutoTrap.mc.player.swing(InteractionHand.MAIN_HAND);
        ((MixinMinecraft)AutoTrap.mc).setRightClickDelay(4);
        if (this.noGlitchBlocks.getValue() && !AutoTrap.mc.gameMode.getPlayerMode().equals(GameType.CREATIVE)) {
            AutoTrap.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, neighbour, opposite));
        }
        return true;
    }

    private int findObiInHotbar() {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = AutoTrap.mc.player.getInventory().getItem(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof BlockItem) {
                final Block block = ((BlockItem)stack.getItem()).getBlock();
                if (block == Blocks.OBSIDIAN) {
                    slot = i;
                    break;
                }
            }
        }
        return slot;
    }

    private void findClosestTarget() {
        final List<? extends Player> playerList = AutoTrap.mc.level.players();
        this.closestTarget = null;
        for (final Player target : playerList) {
            if (target == AutoTrap.mc.player) {
                continue;
            }
            if (Friends.isFriend(target.getName().getString())) {
                continue;
            }
            if (!EntityUtil.isLiving((Entity)target)) {
                continue;
            }
            if (target.getHealth() <= 0.0f) {
                continue;
            }
            if (this.closestTarget == null) {
                this.closestTarget = target;
            }
            else {
                if (AutoTrap.mc.player.distanceTo(target) >= AutoTrap.mc.player.distanceTo(this.closestTarget)) {
                    continue;
                }
                this.closestTarget = target;
            }
        }
    }

    @Override
    public String getHudInfo() {
        if (this.closestTarget != null) {
            return this.closestTarget.getName().getString().toUpperCase();
        }
        return null;
    }

    private enum Cage
    {
        TRAP,
        TRAPTOP,
        TRAPFULLROOF,
        TRAPFULLROOFTOP,
        CRYSTALEXA,
        CRYSTAL,
        CRYSTALFULLROOF;
    }

    private static class Offsets
    {
        private static final Vec3[] TRAP;
        private static final Vec3[] TRAPTOP;
        private static final Vec3[] TRAPFULLROOF;
        private static final Vec3[] TRAPFULLROOFTOP;
        private static final Vec3[] CRYSTALEXA;
        private static final Vec3[] CRYSTAL;
        private static final Vec3[] CRYSTALFULLROOF;

        static {
            TRAP = new Vec3[] { new Vec3(0.0, 0.0, -1.0), new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 1.0, -1.0), new Vec3(1.0, 1.0, 0.0), new Vec3(0.0, 1.0, 1.0), new Vec3(-1.0, 1.0, 0.0), new Vec3(0.0, 2.0, -1.0), new Vec3(1.0, 2.0, 0.0), new Vec3(0.0, 2.0, 1.0), new Vec3(-1.0, 2.0, 0.0), new Vec3(0.0, 3.0, -1.0), new Vec3(0.0, 3.0, 0.0) };
            TRAPTOP = new Vec3[] { new Vec3(0.0, 0.0, -1.0), new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 1.0, -1.0), new Vec3(1.0, 1.0, 0.0), new Vec3(0.0, 1.0, 1.0), new Vec3(-1.0, 1.0, 0.0), new Vec3(0.0, 2.0, -1.0), new Vec3(1.0, 2.0, 0.0), new Vec3(0.0, 2.0, 1.0), new Vec3(-1.0, 2.0, 0.0), new Vec3(0.0, 3.0, -1.0), new Vec3(0.0, 3.0, 0.0), new Vec3(0.0, 4.0, 0.0) };
            TRAPFULLROOF = new Vec3[] { new Vec3(0.0, 0.0, -1.0), new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 1.0, -1.0), new Vec3(1.0, 1.0, 0.0), new Vec3(0.0, 1.0, 1.0), new Vec3(-1.0, 1.0, 0.0), new Vec3(0.0, 2.0, -1.0), new Vec3(1.0, 2.0, 0.0), new Vec3(0.0, 2.0, 1.0), new Vec3(-1.0, 2.0, 0.0), new Vec3(0.0, 3.0, -1.0), new Vec3(1.0, 3.0, 0.0), new Vec3(0.0, 3.0, 1.0), new Vec3(-1.0, 3.0, 0.0), new Vec3(0.0, 3.0, 0.0) };
            TRAPFULLROOFTOP = new Vec3[] { new Vec3(0.0, 0.0, -1.0), new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 1.0, -1.0), new Vec3(1.0, 1.0, 0.0), new Vec3(0.0, 1.0, 1.0), new Vec3(-1.0, 1.0, 0.0), new Vec3(0.0, 2.0, -1.0), new Vec3(1.0, 2.0, 0.0), new Vec3(0.0, 2.0, 1.0), new Vec3(-1.0, 2.0, 0.0), new Vec3(0.0, 3.0, -1.0), new Vec3(1.0, 3.0, 0.0), new Vec3(0.0, 3.0, 1.0), new Vec3(-1.0, 3.0, 0.0), new Vec3(0.0, 3.0, 0.0), new Vec3(0.0, 4.0, 0.0) };
            CRYSTALEXA = new Vec3[] { new Vec3(0.0, 0.0, -1.0), new Vec3(0.0, 1.0, -1.0), new Vec3(0.0, 2.0, -1.0), new Vec3(1.0, 2.0, 0.0), new Vec3(0.0, 2.0, 1.0), new Vec3(-1.0, 2.0, 0.0), new Vec3(-1.0, 2.0, -1.0), new Vec3(1.0, 2.0, 1.0), new Vec3(1.0, 2.0, -1.0), new Vec3(-1.0, 2.0, 1.0), new Vec3(0.0, 3.0, -1.0), new Vec3(0.0, 3.0, 0.0) };
            CRYSTAL = new Vec3[] { new Vec3(0.0, 0.0, -1.0), new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), new Vec3(-1.0, 0.0, 0.0), new Vec3(-1.0, 0.0, 1.0), new Vec3(1.0, 0.0, -1.0), new Vec3(-1.0, 0.0, -1.0), new Vec3(1.0, 0.0, 1.0), new Vec3(-1.0, 1.0, -1.0), new Vec3(1.0, 1.0, 1.0), new Vec3(-1.0, 1.0, 1.0), new Vec3(1.0, 1.0, -1.0), new Vec3(0.0, 2.0, -1.0), new Vec3(1.0, 2.0, 0.0), new Vec3(0.0, 2.0, 1.0), new Vec3(-1.0, 2.0, 0.0), new Vec3(-1.0, 2.0, 1.0), new Vec3(1.0, 2.0, -1.0), new Vec3(0.0, 3.0, -1.0), new Vec3(0.0, 3.0, 0.0) };
            CRYSTALFULLROOF = new Vec3[] { new Vec3(0.0, 0.0, -1.0), new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), new Vec3(-1.0, 0.0, 0.0), new Vec3(-1.0, 0.0, 1.0), new Vec3(1.0, 0.0, -1.0), new Vec3(-1.0, 0.0, -1.0), new Vec3(1.0, 0.0, 1.0), new Vec3(-1.0, 1.0, -1.0), new Vec3(1.0, 1.0, 1.0), new Vec3(-1.0, 1.0, 1.0), new Vec3(1.0, 1.0, -1.0), new Vec3(0.0, 2.0, -1.0), new Vec3(1.0, 2.0, 0.0), new Vec3(0.0, 2.0, 1.0), new Vec3(-1.0, 2.0, 0.0), new Vec3(-1.0, 2.0, 1.0), new Vec3(1.0, 2.0, -1.0), new Vec3(0.0, 3.0, -1.0), new Vec3(1.0, 3.0, 0.0), new Vec3(0.0, 3.0, 1.0), new Vec3(-1.0, 3.0, 0.0), new Vec3(0.0, 3.0, 0.0) };
        }
    }
}
