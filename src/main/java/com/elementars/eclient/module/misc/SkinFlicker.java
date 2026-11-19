package com.elementars.eclient.module.misc;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import net.minecraft.world.entity.player.PlayerModelPart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class SkinFlicker extends Module
{
    private final Value<String> mode;
    private final Value<Integer> slowness;
    private static final PlayerModelPart[] PARTS_HORIZONTAL = new PlayerModelPart[] { PlayerModelPart.LEFT_SLEEVE, PlayerModelPart.JACKET, PlayerModelPart.HAT, PlayerModelPart.LEFT_PANTS_LEG, PlayerModelPart.RIGHT_PANTS_LEG, PlayerModelPart.RIGHT_SLEEVE };
    private static final PlayerModelPart[] PARTS_VERTICAL = new PlayerModelPart[] { PlayerModelPart.HAT, PlayerModelPart.JACKET, PlayerModelPart.LEFT_SLEEVE, PlayerModelPart.RIGHT_SLEEVE, PlayerModelPart.LEFT_PANTS_LEG, PlayerModelPart.RIGHT_PANTS_LEG };
    private final Random r;
    private final int len;
    private final boolean[] originalParts;
    private boolean captured;

    public SkinFlicker() {
        super("SkinFlicker", "Toggles skin layers", 0, Category.MISC, true);
        this.mode = this.register(new Value<String>("Mode", this, "Horizontal", new ArrayList<String>(Arrays.asList("Horizontal", "Vertical", "Random"))));
        this.slowness = this.register(new Value<Integer>("Slowness", this, 2, 1, 5));
        this.r = new Random();
        this.len = PlayerModelPart.values().length;
        this.originalParts = new boolean[this.len];
    }

    @Override
    public void onEnable() {
        for (final PlayerModelPart part : PlayerModelPart.values()) {
            this.originalParts[part.ordinal()] = SkinFlicker.mc.options.isModelPartEnabled(part);
        }
        this.captured = true;
    }

    @Override
    public void onDisable() {
        if (!this.captured) {
            return;
        }
        for (final PlayerModelPart part : PlayerModelPart.values()) {
            SkinFlicker.mc.options.toggleModelPart(part, this.originalParts[part.ordinal()]);
        }
        this.captured = false;
    }

    @Override
    public void onUpdate() {
        if (SkinFlicker.mc.player == null) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Random")) {
            if (SkinFlicker.mc.player.tickCount % this.slowness.getValue() != 0) {
                return;
            }
            final PlayerModelPart part = PlayerModelPart.values()[this.r.nextInt(this.len)];
            SkinFlicker.mc.options.toggleModelPart(part, !SkinFlicker.mc.options.isModelPartEnabled(part));
        }
        else {
            int i = SkinFlicker.mc.player.tickCount / this.slowness.getValue() % (SkinFlicker.PARTS_HORIZONTAL.length * 2);
            boolean on = false;
            if (i >= SkinFlicker.PARTS_HORIZONTAL.length) {
                on = true;
                i -= SkinFlicker.PARTS_HORIZONTAL.length;
            }
            SkinFlicker.mc.options.toggleModelPart(this.mode.getValue().equalsIgnoreCase("Vertical") ? SkinFlicker.PARTS_VERTICAL[i] : SkinFlicker.PARTS_HORIZONTAL[i], on);
        }
    }
}
