package com.elementars.eclient.module.combat;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.command.Command;
import com.elementars.eclient.enemy.Enemies;
import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventReceivePacket;
import com.elementars.eclient.event.events.EventSendPacket;
import com.elementars.eclient.event.events.MotionEvent;
import com.elementars.eclient.event.events.MotionEventPost;
import com.elementars.eclient.event.events.RenderEvent;
import com.elementars.eclient.friend.Friends;
import com.elementars.eclient.guirewrite.elements.PvPInfo;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.BlockInteractionHelper;
import com.elementars.eclient.util.ColorTextUtils;
import com.elementars.eclient.util.MathUtil;
import com.elementars.eclient.util.Plane;
import com.elementars.eclient.util.RainbowUtils;
import com.elementars.eclient.util.TargetPlayers;
import com.elementars.eclient.util.VectorUtils;
import com.elementars.eclient.util.XuluTessellator;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.settings.Value;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AutoCrystal extends Module
{
    private final List<BlockPos> placedCrystals;
    private final List<Entity> ignoreList;
    private BlockPos render;
    private BlockPos renderOld;
    private Entity renderEnt;
    private boolean switchCooldown;
    private boolean isAttacking;
    private int oldSlot;
    private int newSlot;
    private int waitCounter;
    private int placeCounter;
    private Level trackedLevel;
    Direction f;
    public static boolean isRand;
    private final Value<Boolean> explode;
    private final Value<String> explodeMode;
    private final Value<Boolean> sync;
    private final Value<Boolean> spam;
    private final Value<Integer> hitAttempts;
    private final Value<Integer> hitRetryDelay;
    private final Value<Integer> waitTick;
    private final Value<Integer> placeTick;
    private final Value<Float> range;
    private final Value<Float> walls;
    private final Value<Float> ER;
    private final Value<Boolean> pre;
    private final Value<Boolean> antiWeakness;
    private final Value<Boolean> nodesync;
    private final Value<Boolean> place;
    private final Value<Boolean> oneHole;
    private final Value<Boolean> noSuicide;
    private final Value<Boolean> autoSwitch;
    private final Value<Float> placeRange;
    private final Value<Integer> minDmg;
    private final Value<Integer> facePlace;
    private final Value<Boolean> armor;
    private final Value<Integer> armorDmg;
    private final Value<Boolean> raytrace;
    private final Value<Boolean> rotate;
    private final Value<String> rotateMode;
    private final Value<Boolean> randRotations;
    private final Value<Boolean> fast;
    private final Value<String> fastType;
    private final Value<Boolean> toggleOff;
    private final Value<Integer> toggleHealth;
    private final Value<Boolean> chat;
    private final Value<Boolean> watermark;
    private final Value<String> echatcolor;
    private final Value<String> dchatcolor;
    private final Value<Boolean> renderDamage;
    private final Value<Boolean> damageWhite;
    private final Value<Boolean> renderBoolean;
    private final Value<String> rendermode;
    private final Value<Boolean> rainbow;
    private final Value<Integer> espR;
    private final Value<Integer> espG;
    private final Value<Integer> espB;
    private final Value<Integer> espA;
    private final Value<Integer> espF;
    private final Value<Integer> maxSelfDmg;
    private final Value<Boolean> noGappleSwitch;
    private final Value<Boolean> smoothEsp;
    private final Value<Integer> smoothSpeed;
    public boolean isActive;
    ConcurrentHashMap<BlockPos, Integer> fadeList;
    private Map<EndCrystal, Integer> attemptMap;
    private Map<EndCrystal, Integer> retryMap;
    private static final Random ROTATION_RANDOM = new Random();
    private static float newYaw;
    private static float newPitch;
    private static boolean isSpoofingAngles;
    private static double yaw;
    private static double pitch;

    public AutoCrystal() {
        super("AutoCrystal", "Xulu AutoCrystal", 0, Category.COMBAT, true);
        this.placedCrystals = new ArrayList<BlockPos>();
        this.ignoreList = new ArrayList<Entity>();
        this.switchCooldown = false;
        this.isAttacking = false;
        this.oldSlot = -1;
        this.explode = this.register(new Value<Boolean>("Hit", this, true));
        this.explodeMode = this.register(new Value<String>("Hit Mode", this, "All", new String[] { "All", "OnlyOwn" }));
        this.sync = this.register(new Value<Boolean>("Sync Break", this, true));
        this.spam = this.register(new Value<Boolean>("Spam", this, true));
        this.hitAttempts = this.register(new Value<Integer>("Hit Attempts", this, 5, 0, 20));
        this.hitRetryDelay = this.register(new Value<Integer>("Retry Delay", this, 2, 0, 20));
        this.waitTick = this.register(new Value<Integer>("Tick Delay", this, 1, 0, 20));
        this.placeTick = this.register(new Value<Integer>("Place Delay", this, 1, 0, 20));
        this.range = this.register(new Value<Float>("Hit Range", this, 5.0f, 0.0f, 10.0f));
        this.walls = this.register(new Value<Float>("Walls Range", this, 3.5f, 0.0f, 10.0f));
        this.ER = this.register(new Value<Float>("Enemy Range", this, 5.0f, 0.0f, 10.0f));
        this.pre = this.register(new Value<Boolean>("Prioritize Enemies", this, false));
        this.antiWeakness = this.register(new Value<Boolean>("Anti Weakness", this, true));
        this.nodesync = this.register(new Value<Boolean>("No Desync", this, true));
        this.place = this.register(new Value<Boolean>("Place", this, true));
        this.oneHole = this.register(new Value<Boolean>("1.13 Mode", this, false));
        this.noSuicide = this.register(new Value<Boolean>("No Suicide", this, true));
        this.autoSwitch = this.register(new Value<Boolean>("Auto Switch", this, true));
        this.placeRange = this.register(new Value<Float>("Place Range", this, 5.0f, 0.0f, 10.0f));
        this.minDmg = this.register(new Value<Integer>("Min Damage", this, 5, 0, 40));
        this.facePlace = this.register(new Value<Integer>("Faceplace HP", this, 6, 0, 40));
        this.armor = this.register(new Value<Boolean>("Armor Place", this, true));
        this.armorDmg = this.register(new Value<Integer>("Armor %", this, 15, 0, 100));
        this.raytrace = this.register(new Value<Boolean>("Raytrace", this, false));
        this.rotate = this.register(new Value<Boolean>("Rotate", this, true));
        this.rotateMode = this.register(new Value<String>("Rot. Mode", this, "New", new String[] { "Old", "New" })).visibleWhen(string -> this.rotate.getValue());
        this.randRotations = this.register(new Value<Boolean>("Random Rotations", this, true));
        this.fast = this.register(new Value<Boolean>("Fast Mode", this, false));
        this.fastType = this.register(new Value<String>("Fast Type", this, "Instant", new String[] { "Instant", "Ignore" }));
        this.toggleOff = this.register(new Value<Boolean>("Toggle Off", this, false));
        this.toggleHealth = this.register(new Value<Integer>("Toggle Off Health", this, 10, 0, 20));
        this.chat = this.register(new Value<Boolean>("Toggle Msgs", this, true));
        this.watermark = this.register(new Value<Boolean>("Watermark", this, true));
        this.echatcolor = this.register(new Value<String>("Enable Color", this, "White", ColorTextUtils.colors));
        this.dchatcolor = this.register(new Value<String>("Disable Color", this, "White", ColorTextUtils.colors));
        this.renderDamage = this.register(new Value<Boolean>("Render Damage", this, false));
        this.damageWhite = this.register(new Value<Boolean>("Damage Color White", this, false));
        this.renderBoolean = this.register(new Value<Boolean>("Render", this, true));
        this.rendermode = this.register(new Value<String>("RenderMode", this, "Solid", new ArrayList<String>(Arrays.asList("Solid", "Outline", "Full"))));
        this.rainbow = this.register(new Value<Boolean>("Esp Rainbow", this, false));
        this.espR = this.register(new Value<Integer>("Esp Red", this, 200, 0, 255));
        this.espG = this.register(new Value<Integer>("Esp Green", this, 50, 0, 255));
        this.espB = this.register(new Value<Integer>("Esp Blue", this, 200, 0, 255));
        this.espA = this.register(new Value<Integer>("Esp Alpha", this, 50, 0, 255));
        this.espF = this.register(new Value<Integer>("Full Alpha", this, 80, 0, 255));
        this.maxSelfDmg = this.register(new Value<Integer>("Max Self Dmg", this, 10, 0, 36));
        this.noGappleSwitch = this.register(new Value<Boolean>("No Gap Switch", this, false));
        this.smoothEsp = this.register(new Value<Boolean>("Smooth ESP", this, false));
        this.smoothSpeed = this.register(new Value<Integer>("Smooth Speed", this, 5, 1, 20));
        this.isActive = false;
        this.fadeList = new ConcurrentHashMap<BlockPos, Integer>();
        this.attemptMap = new WeakHashMap<EndCrystal, Integer>();
        this.retryMap = new WeakHashMap<EndCrystal, Integer>();
    }

    @Override
    public void onUpdate() {
        if (!this.rotate.getValue() || this.rotateMode.getValue().equalsIgnoreCase("Old")) {
            this.doAutoCrystal();
        }
    }

    private void doAutoCrystal() {
        AutoCrystal.isRand = this.randRotations.getValue();
        this.isActive = false;
        if (AutoCrystal.mc.player == null || AutoCrystal.mc.player.isRemoved() || !AutoCrystal.mc.player.isAlive() || AutoCrystal.mc.level == null) {
            return;
        }
        if (AutoCrystal.mc.level != this.trackedLevel) {
            this.trackedLevel = AutoCrystal.mc.level;
            this.resetState();
        }
        if (this.shouldPause()) {
            resetRotation();
            return;
        }
        if (this.toggleOff.getValue() && AutoCrystal.mc.player.getHealth() <= this.toggleHealth.getValue()) {
            this.toggle();
            return;
        }
        PvPInfo.attack = false;
        PvPInfo.place = false;
        this.updateFadeList();
        int crystalSlot = (AutoCrystal.mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL) ? AutoCrystal.mc.player.getInventory().selected : -1;
        if (crystalSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (AutoCrystal.mc.player.getInventory().getItem(l).getItem() == Items.END_CRYSTAL) {
                    crystalSlot = l;
                    break;
                }
            }
        }
        this.ignoreList.removeIf(Entity::isRemoved);
        for (final Iterator<Map.Entry<EndCrystal, Integer>> iterator = this.retryMap.entrySet().iterator(); iterator.hasNext();) {
            final Map.Entry<EndCrystal, Integer> entry = iterator.next();
            if (entry.getKey().isRemoved()) {
                iterator.remove();
            }
            else if (entry.getValue() != 0) {
                entry.setValue(entry.getValue() - 1);
            }
        }
        EndCrystal crystal = null;
        final List<EndCrystal> crystals = new ArrayList<EndCrystal>();
        for (final Entity e2 : AutoCrystal.mc.level.entitiesForRendering()) {
            if (!(e2 instanceof EndCrystal)) {
                continue;
            }
            final EndCrystal ec = (EndCrystal)e2;
            if (AutoCrystal.mc.player.distanceTo(ec) > this.range.getValue()) {
                continue;
            }
            if (!this.checkCrystal(ec)) {
                continue;
            }
            if (this.attemptMap.containsKey(ec) && this.attemptMap.get(ec).equals(this.hitAttempts.getValue())) {
                if (this.retryMap.containsKey(ec) && this.retryMap.get(ec) == 0) {
                    this.attemptMap.put(ec, 0);
                    this.retryMap.remove(ec);
                }
                else {
                    if (!this.retryMap.containsKey(ec)) {
                        this.retryMap.put(ec, this.hitRetryDelay.getValue());
                        continue;
                    }
                    continue;
                }
            }
            crystals.add(ec);
        }
        if (!crystals.isEmpty()) {
            crystals.sort(Comparator.comparing(c -> AutoCrystal.mc.player.distanceTo(c)));
            crystal = crystals.get(0);
        }
        if (this.explode.getValue() && crystal != null) {
            if (!AutoCrystal.mc.player.hasLineOfSight(crystal) && AutoCrystal.mc.player.distanceTo(crystal) > this.walls.getValue()) {
                PvPInfo.attack = false;
                return;
            }
            if (!this.fast.getValue() && this.waitTick.getValue() > 0) {
                if (this.waitCounter < this.waitTick.getValue()) {
                    ++this.waitCounter;
                    PvPInfo.attack = false;
                    return;
                }
                this.waitCounter = 0;
            }
            if (this.antiWeakness.getValue() && AutoCrystal.mc.player.hasEffect(MobEffects.WEAKNESS)) {
                if (!this.isAttacking) {
                    this.oldSlot = AutoCrystal.mc.player.getInventory().selected;
                    PvPInfo.attack = true;
                    this.isAttacking = true;
                }
                this.newSlot = -1;
                for (int i = 0; i < 9; ++i) {
                    final ItemStack stack = AutoCrystal.mc.player.getInventory().getItem(i);
                    if (!stack.isEmpty()) {
                        if (stack.getItem() instanceof SwordItem) {
                            this.newSlot = i;
                            break;
                        }
                        if (stack.getItem() instanceof DiggerItem) {
                            this.newSlot = i;
                            break;
                        }
                    }
                }
                if (this.newSlot != -1) {
                    this.setSlot(this.newSlot);
                    this.switchCooldown = true;
                }
            }
            PvPInfo.attack = true;
            this.isActive = true;
            if (this.rotate.getValue()) {
                this.lookAtPacket(crystal.getX(), crystal.getY(), crystal.getZ(), AutoCrystal.mc.player);
            }
            if (this.spam.getValue()) {
                for (int i = 0; i < 50; ++i) {
                    this.attack(crystal);
                }
            }
            else {
                this.attack(crystal);
            }
            AutoCrystal.mc.player.swing(InteractionHand.MAIN_HAND);
            this.isActive = false;
            if (this.fast.getValue()) {
                if (this.fastType.getValue().equalsIgnoreCase("Instant")) {
                    crystal.discard();
                }
                else if (this.fastType.getValue().equalsIgnoreCase("Ignore") && !this.ignoreList.contains(crystal)) {
                    this.ignoreList.add(crystal);
                }
            }
            if (this.attemptMap.containsKey(crystal)) {
                this.attemptMap.put(crystal, this.attemptMap.get(crystal) + 1);
            }
            else {
                this.attemptMap.put(crystal, 1);
            }
        }
        else {
            resetRotation();
            if (this.oldSlot != -1) {
                this.setSlot(this.oldSlot);
                this.oldSlot = -1;
            }
            this.isAttacking = false;
            this.isActive = false;
            if (this.placeTick.getValue() > 0) {
                if (this.placeCounter < this.placeTick.getValue()) {
                    ++this.placeCounter;
                    PvPInfo.place = false;
                    return;
                }
                this.placeCounter = 0;
            }
            final boolean offhand = AutoCrystal.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL;
            if (!offhand && crystalSlot == -1) {
                return;
            }
            final List<BlockPos> blocks = this.findCrystalBlocks();
            final List<Entity> entities = new ArrayList<Entity>();
            final List<Entity> enemies = new ArrayList<Entity>();
            for (final Player player : AutoCrystal.mc.level.players()) {
                if (Friends.isFriend(player.getName().getString())) {
                    continue;
                }
                entities.add(player);
            }
            entities.sort(Comparator.comparing(e -> AutoCrystal.mc.player.distanceTo(e)));
            entities.removeIf(entity -> AutoCrystal.mc.player.distanceTo(entity) > this.ER.getValue());
            if (this.pre.getValue()) {
                for (final Player player : AutoCrystal.mc.level.players()) {
                    if (Friends.isFriend(player.getName().getString())) {
                        continue;
                    }
                    if (!Enemies.isEnemy(player.getName().getString())) {
                        continue;
                    }
                    enemies.add(player);
                }
                enemies.sort(Comparator.comparing(e -> AutoCrystal.mc.player.distanceTo(e)));
                enemies.removeIf(entity -> AutoCrystal.mc.player.distanceTo(entity) > this.ER.getValue());
                if (!enemies.isEmpty()) {
                    entities.clear();
                    entities.addAll(enemies);
                }
            }
            BlockPos q = null;
            double damage = 0.5;
            for (final Entity entity2 : entities) {
                if (entity2 == AutoCrystal.mc.player) {
                    continue;
                }
                if (!(entity2 instanceof LivingEntity) || ((LivingEntity)entity2).getHealth() <= 0.0f || entity2.isRemoved()) {
                    continue;
                }
                for (final BlockPos blockPos : blocks) {
                    final double b = entity2.distanceToSqr(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
                    if (b >= 169.0) {
                        continue;
                    }
                    final double d = calculateDamage(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5, entity2);
                    if (d < this.minDmg.getValue() && ((LivingEntity)entity2).getHealth() + ((LivingEntity)entity2).getAbsorptionAmount() > ((AutoTotem.isFullArmor((Player)entity2) && !this.isArmorLow((Player)entity2)) ? this.facePlace.getValue() : 36)) {
                        continue;
                    }
                    if (d <= damage) {
                        continue;
                    }
                    final double self = calculateDamage(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5, AutoCrystal.mc.player);
                    if (self > d && d >= ((LivingEntity)entity2).getHealth()) {
                        continue;
                    }
                    if (self - 0.5 > AutoCrystal.mc.player.getHealth()) {
                        continue;
                    }
                    if (self > this.maxSelfDmg.getValue()) {
                        continue;
                    }
                    damage = d;
                    q = blockPos;
                    this.renderEnt = entity2;
                }
            }
            if (q == null) {
                this.render = null;
                this.renderEnt = null;
                resetRotation();
                return;
            }
            this.render = q;
            if (this.place.getValue()) {
                this.isActive = true;
                if (this.rotate.getValue()) {
                    this.lookAtPacket(q.getX() + 0.5, q.getY() - 0.5, q.getZ() + 0.5, AutoCrystal.mc.player);
                }
                final BlockHitResult result = AutoCrystal.mc.level.clip(new ClipContext(new Vec3(AutoCrystal.mc.player.getX(), AutoCrystal.mc.player.getY() + AutoCrystal.mc.player.getEyeHeight(), AutoCrystal.mc.player.getZ()), new Vec3(q.getX() + 0.5, q.getY() - 0.5, q.getZ() + 0.5), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, AutoCrystal.mc.player));
                if (this.raytrace.getValue()) {
                    if (result == null || result.getType() != HitResult.Type.BLOCK || result.getDirection() == null) {
                        this.f = null;
                        this.render = null;
                        this.renderEnt = null;
                        resetRotation();
                        this.isActive = false;
                        PvPInfo.place = false;
                        return;
                    }
                    this.f = result.getDirection();
                }
                if (!offhand && AutoCrystal.mc.player.getInventory().selected != crystalSlot) {
                    if (this.autoSwitch.getValue() && !(this.noGappleSwitch.getValue() && this.isEatingGap())) {
                        this.setSlot(crystalSlot);
                        resetRotation();
                        this.switchCooldown = true;
                    }
                    else {
                        this.isActive = false;
                        resetRotation();
                    }
                    PvPInfo.place = false;
                    return;
                }
                if (this.switchCooldown) {
                    this.switchCooldown = false;
                    this.isActive = false;
                    PvPInfo.place = false;
                    return;
                }
                PvPInfo.place = true;
                this.isActive = true;
                final Vec3 hitVec = (this.raytrace.getValue() && result != null && result.getType() == HitResult.Type.BLOCK) ? result.getLocation() : new Vec3(q.getX() + 0.5, q.getY() + 1.0, q.getZ() + 0.5);
                final Direction face = (this.raytrace.getValue() && this.f != null) ? this.f : Direction.UP;
                AutoCrystal.mc.player.connection.send(new ServerboundUseItemOnPacket(offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, face, q, false)));
                if (this.renderEnt != null) {
                    TargetPlayers.addTargetedPlayer(this.renderEnt.getName().getString());
                }
                this.isActive = false;
            }
        }
    }

    private void attack(final EndCrystal crystal) {
        if (this.sync.getValue()) {
            AutoCrystal.mc.player.connection.send(ServerboundInteractPacket.createAttackPacket(crystal, AutoCrystal.mc.player.isShiftKeyDown()));
        }
        else {
            AutoCrystal.mc.gameMode.attack(AutoCrystal.mc.player, crystal);
        }
    }

    private void updateFadeList() {
        if (!this.smoothEsp.getValue()) {
            this.fadeList.clear();
            return;
        }
        final int speed = this.smoothSpeed.getValue();
        for (final Iterator<Map.Entry<BlockPos, Integer>> iterator = this.fadeList.entrySet().iterator(); iterator.hasNext();) {
            final Map.Entry<BlockPos, Integer> entry = iterator.next();
            final int alpha = entry.getValue() - speed;
            if (alpha <= 0) {
                iterator.remove();
            }
            else {
                entry.setValue(alpha);
            }
        }
    }

    private void resetState() {
        this.placedCrystals.clear();
        this.ignoreList.clear();
        this.attemptMap.clear();
        this.retryMap.clear();
        this.fadeList.clear();
        this.render = null;
        this.renderEnt = null;
        this.renderOld = null;
        this.waitCounter = 0;
        this.placeCounter = 0;
        this.switchCooldown = false;
        this.isAttacking = false;
        this.oldSlot = -1;
        this.newSlot = -1;
        resetRotation();
    }

    @Override
    public void onRender() {
        if (this.render == null || this.renderEnt == null || AutoCrystal.mc.player == null) {
            return;
        }
        if (!this.renderBoolean.getValue()) {
            return;
        }
        if (this.renderDamage.getValue()) {
            int color1 = this.espR.getValue();
            int color2 = this.espG.getValue();
            int color3 = this.espB.getValue();
            if (this.rainbow.getValue()) {
                color1 = RainbowUtils.r;
                color2 = RainbowUtils.g;
                color3 = RainbowUtils.b;
            }
            final Plane pos = VectorUtils.toScreen(this.render.getX() + 0.5, this.render.getY() + 0.5, this.render.getZ() + 0.5);
            final float damage = calculateDamage(this.render.getX() + 0.5, this.render.getY() + 1.0, this.render.getZ() + 0.5, this.renderEnt);
            final String text = String.valueOf(MathUtil.round(damage, 1));
            AutoCrystal.fontRenderer.drawShadow(new PoseStack(), text, (float)pos.getX() - AutoCrystal.fontRenderer.width(text) / 2, (float)pos.getY() - AutoCrystal.fontRenderer.lineHeight / 2, this.damageWhite.getValue() ? -1 : new Color(color1, color2, color3).getRGB());
        }
    }

    @Override
    public void onWorldRender(final RenderEvent event) {
        if (this.render == null || AutoCrystal.mc.player == null || AutoCrystal.mc.level == null) {
            return;
        }
        if (!this.renderBoolean.getValue()) {
            return;
        }
        if (this.smoothEsp.getValue() && this.renderOld != null && !this.renderOld.equals(this.render)) {
            this.fadeList.put(this.renderOld, this.espA.getValue());
        }
        int color1 = this.espR.getValue();
        int color2 = this.espG.getValue();
        int color3 = this.espB.getValue();
        if (this.rainbow.getValue()) {
            color1 = RainbowUtils.r;
            color2 = RainbowUtils.g;
            color3 = RainbowUtils.b;
        }
        if (this.rendermode.getValue().equalsIgnoreCase("Solid")) {
            XuluTessellator.prepare(7);
            XuluTessellator.drawBox(this.render, color1, color2, color3, this.espA.getValue(), 63);
            XuluTessellator.release();
        }
        else if (this.rendermode.getValue().equalsIgnoreCase("Outline")) {
            final BlockState iBlockState2 = AutoCrystal.mc.level.getBlockState(this.render);
            final Vec3 interp2 = event.getRenderPos();
            final AABB box2 = BlockInteractionHelper.getBlockBounds(iBlockState2, AutoCrystal.mc.level, this.render).inflate(0.0020000000949949026).move(-interp2.x, -interp2.y, -interp2.z);
            XuluTessellator.drawBoundingBox(box2, 1.5f, color1, color2, color3, this.espA.getValue());
        }
        else if (this.rendermode.getValue().equalsIgnoreCase("Full")) {
            final BlockState iBlockState3 = AutoCrystal.mc.level.getBlockState(this.render);
            final Vec3 interp3 = event.getRenderPos();
            final AABB box3 = BlockInteractionHelper.getBlockBounds(iBlockState3, AutoCrystal.mc.level, this.render).inflate(0.0020000000949949026).move(-interp3.x, -interp3.y, -interp3.z);
            XuluTessellator.drawFullBox(box3, this.render, 1.5f, color1, color2, color3, this.espA.getValue(), this.espF.getValue());
        }
        if (this.smoothEsp.getValue()) {
            for (final BlockPos pos : this.fadeList.keySet()) {
                final Integer alpha = this.fadeList.get(pos);
                if (alpha == null) {
                    continue;
                }
                if (alpha <= 0) {
                    this.fadeList.remove(pos);
                }
                else if (this.rendermode.getValue().equalsIgnoreCase("Solid")) {
                    XuluTessellator.prepare(7);
                    XuluTessellator.drawBox(pos, color1, color2, color3, alpha, 63);
                    XuluTessellator.release();
                }
                else if (this.rendermode.getValue().equalsIgnoreCase("Outline")) {
                    final BlockState iBlockState4 = AutoCrystal.mc.level.getBlockState(pos);
                    final Vec3 interp4 = event.getRenderPos();
                    final AABB box4 = BlockInteractionHelper.getBlockBounds(iBlockState4, AutoCrystal.mc.level, pos).inflate(0.0020000000949949026).move(-interp4.x, -interp4.y, -interp4.z);
                    XuluTessellator.drawBoundingBox(box4, 1.5f, color1, color2, color3, alpha);
                }
                else if (this.rendermode.getValue().equalsIgnoreCase("Full")) {
                    final BlockState iBlockState5 = AutoCrystal.mc.level.getBlockState(pos);
                    final Vec3 interp5 = event.getRenderPos();
                    final AABB box5 = BlockInteractionHelper.getBlockBounds(iBlockState5, AutoCrystal.mc.level, pos).inflate(0.0020000000949949026).move(-interp5.x, -interp5.y, -interp5.z);
                    XuluTessellator.drawFullBox(box5, pos, 1.5f, color1, color2, color3, alpha, this.espF.getValue());
                }
            }
        }
        this.renderOld = this.render;
    }

    @Override
    public String getHudInfo() {
        if (this.renderEnt == null || AutoCrystal.mc.player == null) {
            return null;
        }
        if (AutoCrystal.mc.player.distanceTo(this.renderEnt) <= this.range.getValue()) {
            return ChatFormatting.GREEN + this.renderEnt.getName().getString();
        }
        return ChatFormatting.RED + this.renderEnt.getName().getString();
    }

    private boolean checkCrystal(final Entity e) {
        if (e == null || AutoCrystal.mc.player == null) {
            return false;
        }
        if (this.noSuicide.getValue()) {
            final double self = calculateDamage(e.getX(), e.getY(), e.getZ(), AutoCrystal.mc.player);
            if (self - 0.5 > AutoCrystal.mc.player.getHealth()) {
                return false;
            }
        }
        final String s = this.explodeMode.getValue();
        if (s.equalsIgnoreCase("OnlyOwn")) {
            final BlockPos crystalPos = new BlockPos(e.getX(), e.getY(), e.getZ());
            if (this.render != null && this.render.distManhattan(crystalPos) <= 3) {
                return true;
            }
            synchronized (this.placedCrystals) {
                for (final BlockPos pos : this.placedCrystals) {
                    if (pos.distManhattan(crystalPos) <= 3) {
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }

    private boolean isArmorLow(final Player player) {
        if (!this.armor.getValue()) {
            return false;
        }
        for (final ItemStack stack : player.getArmorSlots()) {
            if (stack.isEmpty() || stack.getMaxDamage() <= 0) {
                return true;
            }
            final float green = (stack.getMaxDamage() - (float)stack.getDamageValue()) / stack.getMaxDamage();
            final float red = 1.0f - green;
            final int dmg = 100 - (int)(red * 100.0f);
            if (dmg <= this.armorDmg.getValue()) {
                return true;
            }
        }
        return false;
    }

    private boolean isEatingGap() {
        final ItemStack stack = AutoCrystal.mc.player.getMainHandItem();
        return (stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) && AutoCrystal.mc.player.isUsingItem();
    }

    private void lookAtPacket(final double px, final double py, final double pz, final Player me) {
        final double[] v = calculateLookAt(px, py, pz, me);
        setYawAndPitch((float)v[0], (float)v[1]);
    }

    private void setSlot(final int slot) {
        if (slot < 0 || slot > 8 || AutoCrystal.mc.player == null || AutoCrystal.mc.player.getInventory().selected == slot) {
            return;
        }
        AutoCrystal.mc.player.getInventory().selected = slot;
        if (AutoCrystal.mc.player.connection != null) {
            AutoCrystal.mc.player.connection.send(new ServerboundSetCarriedItemPacket(slot));
        }
    }

    private boolean canPlaceCrystal(final BlockPos blockPos) {
        if (AutoCrystal.mc.level == null || !AutoCrystal.mc.level.isInWorldBounds(blockPos)) {
            return false;
        }
        final BlockPos boost = blockPos.above();
        final BlockPos boost2 = blockPos.above(2);
        final BlockState base = AutoCrystal.mc.level.getBlockState(blockPos);
        if (base.getBlock() != Blocks.BEDROCK && base.getBlock() != Blocks.OBSIDIAN) {
            return false;
        }
        if (!AutoCrystal.mc.level.isInWorldBounds(boost) || !AutoCrystal.mc.level.getBlockState(boost).isAir()) {
            return false;
        }
        if (!this.isTrulyEmpty(AutoCrystal.mc.level.getEntitiesOfClass(Entity.class, new AABB(boost)))) {
            return false;
        }
        if (!this.oneHole.getValue()) {
            if (!AutoCrystal.mc.level.isInWorldBounds(boost2) || !AutoCrystal.mc.level.getBlockState(boost2).isAir()) {
                return false;
            }
            if (!this.isTrulyEmpty(AutoCrystal.mc.level.getEntitiesOfClass(Entity.class, new AABB(boost2)))) {
                return false;
            }
        }
        return true;
    }

    private boolean isTrulyEmpty(final List<Entity> list) {
        for (final Entity e : list) {
            if (!this.ignoreList.contains(e)) {
                return false;
            }
        }
        return true;
    }

    public static BlockPos getPlayerPos() {
        if (AutoCrystal.mc.player == null) {
            return BlockPos.ZERO;
        }
        return new BlockPos(Math.floor(AutoCrystal.mc.player.getX()), Math.floor(AutoCrystal.mc.player.getY()), Math.floor(AutoCrystal.mc.player.getZ()));
    }

    private List<BlockPos> findCrystalBlocks() {
        final List<BlockPos> positions = new ArrayList<BlockPos>();
        for (final BlockPos pos : this.getSphere(getPlayerPos(), this.placeRange.getValue(), this.placeRange.getValue().intValue(), false, true, 0)) {
            if (this.canPlaceCrystal(pos)) {
                positions.add(pos);
            }
        }
        return positions;
    }

    public List<BlockPos> getSphere(final BlockPos loc, final float r, final int h, final boolean hollow, final boolean sphere, final int plus_y) {
        final List<BlockPos> circleblocks = new ArrayList<BlockPos>();
        final int cx = loc.getX();
        final int cy = loc.getY();
        final int cz = loc.getZ();
        for (int x = cx - (int)r; x <= cx + r; ++x) {
            for (int z = cz - (int)r; z <= cz + r; ++z) {
                for (int y = sphere ? (cy - (int)r) : cy; y < (sphere ? (cy + r) : ((float)(cy + h))); ++y) {
                    final double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? ((cy - y) * (cy - y)) : 0);
                    if (dist < r * r && (!hollow || dist >= (r - 1.0f) * (r - 1.0f))) {
                        final BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }

    public static float calculateDamage(final double posX, final double posY, final double posZ, final Entity entity) {
        if (entity == null || AutoCrystal.mc.level == null) {
            return 0.0f;
        }
        final float doubleExplosionSize = 12.0f;
        final double distancedsize = Math.sqrt(entity.distanceToSqr(posX, posY, posZ)) / doubleExplosionSize;
        final Vec3 vec3d = new Vec3(posX, posY, posZ);
        final double blockDensity = Explosion.getSeenPercent(vec3d, entity);
        final double v = (1.0 - distancedsize) * blockDensity;
        final float damage = (float)(int)((v * v + v) / 2.0 * 7.0 * doubleExplosionSize + 1.0);
        double finald = 1.0;
        if (entity instanceof LivingEntity) {
            finald = getBlastReduction((LivingEntity)entity, getDamageMultiplied(damage), new Explosion(AutoCrystal.mc.level, null, posX, posY, posZ, 6.0f, false, Explosion.BlockInteraction.NONE));
        }
        return (float)finald;
    }

    public static float getBlastReduction(final LivingEntity entity, float damage, final Explosion explosion) {
        if (entity instanceof Player) {
            final Player ep = (Player)entity;
            final DamageSource ds = DamageSource.explosion(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float)ep.getArmorValue(), (float)ep.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
            final int k = EnchantmentHelper.getDamageProtection(ep.getArmorSlots(), ds);
            final float f = Mth.clamp((float)k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                damage -= damage / 4.0f;
            }
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float)entity.getArmorValue(), (float)entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        return damage;
    }

    private static float getDamageMultiplied(final float damage) {
        if (AutoCrystal.mc.level == null) {
            return damage;
        }
        final int diff = AutoCrystal.mc.level.getDifficulty().getId();
        return damage * ((diff == 0) ? 0.0f : ((diff == 2) ? 1.0f : ((diff == 1) ? 0.5f : 1.5f)));
    }

    public static float calculateDamage(final EndCrystal crystal, final Entity entity) {
        return calculateDamage(crystal.getX(), crystal.getY(), crystal.getZ(), entity);
    }

    public static void updateRotations() {
        if (AutoCrystal.mc.player == null) {
            return;
        }
        AutoCrystal.newYaw = AutoCrystal.mc.player.getYRot();
        AutoCrystal.newPitch = AutoCrystal.mc.player.getXRot();
    }

    public static void restoreRotations() {
        if (AutoCrystal.mc.player == null) {
            resetRotation();
            return;
        }
        AutoCrystal.mc.player.setYRot(AutoCrystal.newYaw);
        AutoCrystal.mc.player.setYHeadRot(AutoCrystal.newYaw);
        AutoCrystal.mc.player.setXRot(AutoCrystal.newPitch);
        resetRotation();
    }

    public static void setPlayerRotations(final float yaw, final float pitch) {
        if (AutoCrystal.mc.player == null) {
            return;
        }
        AutoCrystal.mc.player.setYRot(yaw);
        AutoCrystal.mc.player.setYHeadRot(yaw);
        AutoCrystal.mc.player.setXRot(pitch);
    }

    @EventTarget
    public void onMotionPre(final MotionEvent event) {
        if (this.rotate.getValue() && this.rotateMode.getValue().equalsIgnoreCase("New")) {
            updateRotations();
            this.doAutoCrystal();
        }
    }

    @EventTarget
    public void onMotionPost(final MotionEventPost event) {
        if (this.rotate.getValue() && this.rotateMode.getValue().equalsIgnoreCase("New")) {
            restoreRotations();
        }
    }

    private static void setYawAndPitch(final float yaw1, final float pitch1) {
        AutoCrystal.yaw = yaw1 + (AutoCrystal.isRand ? (ROTATION_RANDOM.nextFloat() / 100.0f) : 0.0f);
        AutoCrystal.pitch = pitch1 + (AutoCrystal.isRand ? (ROTATION_RANDOM.nextFloat() / 100.0f) : 0.0f);
        AutoCrystal.isSpoofingAngles = true;
    }

    private static void resetRotation() {
        if (!AutoCrystal.isSpoofingAngles) {
            return;
        }
        if (AutoCrystal.mc.player != null) {
            AutoCrystal.yaw = AutoCrystal.mc.player.getYRot();
            AutoCrystal.pitch = AutoCrystal.mc.player.getXRot();
        }
        AutoCrystal.isSpoofingAngles = false;
    }

    public static double[] calculateLookAt(final double px, final double py, final double pz, final Player me) {
        double dirx = me.getX() - px;
        double diry = me.getY() - py;
        double dirz = me.getZ() - pz;
        final double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);
        if (len <= 1.0E-6) {
            return new double[] { me.getYRot(), me.getXRot() };
        }
        dirx /= len;
        diry /= len;
        dirz /= len;
        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);
        pitch = pitch * 180.0 / 3.141592653589793;
        yaw = yaw * 180.0 / 3.141592653589793;
        yaw += 90.0;
        return new double[] { yaw, pitch };
    }

    @EventTarget
    public void onSend(final EventSendPacket event) {
        final Packet<?> packet = event.getPacket();
        if (packet instanceof ServerboundMovePlayerPacket && this.rotate.getValue() && AutoCrystal.isSpoofingAngles) {
            final ServerboundMovePlayerPacket movePacket = (ServerboundMovePlayerPacket)packet;
            if (movePacket.hasPosition()) {
                event.setPacket(new ServerboundMovePlayerPacket.PosRot(movePacket.getX(0.0), movePacket.getY(0.0), movePacket.getZ(0.0), (float)AutoCrystal.yaw, (float)AutoCrystal.pitch, movePacket.isOnGround()));
            }
            else {
                event.setPacket(new ServerboundMovePlayerPacket.Rot((float)AutoCrystal.yaw, (float)AutoCrystal.pitch, movePacket.isOnGround()));
            }
        }
        if (event.getPacket() instanceof ServerboundUseItemOnPacket) {
            final ServerboundUseItemOnPacket cpacket = (ServerboundUseItemOnPacket)event.getPacket();
            if (AutoCrystal.mc.player == null) {
                return;
            }
            if (AutoCrystal.mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL || AutoCrystal.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL) {
                final BlockPos placed = cpacket.getHitResult().getBlockPos();
                synchronized (this.placedCrystals) {
                    if (!this.placedCrystals.contains(placed)) {
                        this.placedCrystals.add(placed.immutable());
                    }
                }
            }
        }
    }

    @EventTarget
    public void onRecieve(final EventReceivePacket event) {
        if (!(event.getPacket() instanceof ClientboundSoundPacket) || !this.nodesync.getValue()) {
            return;
        }
        final ClientboundSoundPacket packet = (ClientboundSoundPacket)event.getPacket();
        if (packet.getSource() != SoundSource.BLOCKS || packet.getSound() != SoundEvents.GENERIC_EXPLODE) {
            return;
        }
        final double x = packet.getX();
        final double y = packet.getY();
        final double z = packet.getZ();
        AutoCrystal.mc.execute(() -> this.handleExplosion(x, y, z));
    }

    private void handleExplosion(final double x, final double y, final double z) {
        if (AutoCrystal.mc.level == null) {
            return;
        }
        final List<Entity> exploded = new ArrayList<Entity>();
        for (final Entity e : AutoCrystal.mc.level.entitiesForRendering()) {
            if (e instanceof EndCrystal && e.distanceToSqr(x, y, z) <= 36.0) {
                exploded.add(e);
            }
        }
        for (final Entity e : exploded) {
            e.discard();
        }
        synchronized (this.placedCrystals) {
            this.placedCrystals.removeIf(pos -> pos.distToCenterSqr(x, y, z) <= 36.0);
        }
    }

    @Override
    public void onEnable() {
        this.isActive = false;
        this.trackedLevel = null;
        this.resetState();
        if (this.chat.getValue() && AutoCrystal.mc.player != null) {
            if (this.watermark.getValue()) {
                Command.sendChatMessage(ColorTextUtils.getColor(this.echatcolor.getValue()) + "AutoCrystal ON");
            }
            else {
                Command.sendRawChatMessage(ColorTextUtils.getColor(this.echatcolor.getValue()) + "AutoCrystal ON");
            }
        }
    }

    @Override
    public void onDisable() {
        PvPInfo.place = false;
        PvPInfo.attack = false;
        this.render = null;
        this.renderEnt = null;
        this.renderOld = null;
        resetRotation();
        this.isActive = false;
        if (this.oldSlot != -1) {
            this.setSlot(this.oldSlot);
            this.oldSlot = -1;
        }
        this.ignoreList.clear();
        this.attemptMap.clear();
        this.retryMap.clear();
        this.placedCrystals.clear();
        this.fadeList.clear();
        if (this.chat.getValue() && AutoCrystal.mc.player != null) {
            if (this.watermark.getValue()) {
                Command.sendChatMessage(ColorTextUtils.getColor(this.dchatcolor.getValue()) + "AutoCrystal OFF");
            }
            else {
                Command.sendRawChatMessage(ColorTextUtils.getColor(this.dchatcolor.getValue()) + "AutoCrystal OFF");
            }
        }
    }

    private boolean shouldPause() {
        final Surround surround = Xulu.MODULE_MANAGER.getModuleT(Surround.class);
        if (surround != null && surround.isToggled() && Surround.isExposed() && surround.findObiInHotbar() != -1) {
            return true;
        }
        final AutoTrap autoTrap = Xulu.MODULE_MANAGER.getModuleT(AutoTrap.class);
        if (autoTrap != null && autoTrap.isToggled()) {
            return true;
        }
        final HoleFill holeFill = Xulu.MODULE_MANAGER.getModuleT(HoleFill.class);
        if (holeFill != null && holeFill.isToggled()) {
            return true;
        }
        final HoleBlocker holeBlocker = Xulu.MODULE_MANAGER.getModuleT(HoleBlocker.class);
        return holeBlocker != null && holeBlocker.isToggled() && HoleBlocker.isExposed() && surround != null && surround.findObiInHotbar() != -1;
    }
}
