package com.elementars.eclient.module.render;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.event.Event;
import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventModelPlayerRender;
import com.elementars.eclient.event.events.EventModelRender;
import com.elementars.eclient.event.events.EventPostRenderLayers;
import com.elementars.eclient.friend.Friends;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.ChamsRenderTypes;
import com.elementars.eclient.util.EntityUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xulu.settings.Value;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.awt.Color;

public class Chams extends Module
{
    public static Value<String> mode;
    public final Value<Boolean> hand;
    public final Value<Boolean> lines;
    public final Value<Float> width;
    public final Value<Boolean> friendColor;
    public final Value<Boolean> rainbow;
    public final Value<Integer> r;
    public final Value<Integer> g;
    public final Value<Integer> b;
    public final Value<Integer> a;
    private static Value<Boolean> players;
    private static Value<Boolean> animals;
    private static Value<Boolean> mobs;
    public static Value<Boolean> crystals;
    public final Value<Integer> Vr;
    public final Value<Integer> Vg;
    public final Value<Integer> Vb;
    public final Value<Integer> Wr;
    public final Value<Integer> Wg;
    public final Value<Integer> Wb;

    public Chams() {
        super("Chams", "See entities through walls", 0, Category.RENDER, true);
        Chams.mode = this.register(new Value<String>("Mode", this, "ESP", new String[] { "ESP", "Normal", "Walls" }));
        this.Vr = this.register(new Value<Integer>("Visible Red", this, 255, 0, 255)).visibleWhen(value -> Chams.mode.getValue().equalsIgnoreCase("Walls"));
        this.Vg = this.register(new Value<Integer>("Visible Green", this, 0, 0, 255)).visibleWhen(value -> Chams.mode.getValue().equalsIgnoreCase("Walls"));
        this.Vb = this.register(new Value<Integer>("Visible Blue", this, 0, 0, 255)).visibleWhen(value -> Chams.mode.getValue().equalsIgnoreCase("Walls"));
        this.Wr = this.register(new Value<Integer>("Wall Red", this, 0, 0, 255)).visibleWhen(value -> Chams.mode.getValue().equalsIgnoreCase("Walls"));
        this.Wg = this.register(new Value<Integer>("Wall Green", this, 255, 0, 255)).visibleWhen(value -> Chams.mode.getValue().equalsIgnoreCase("Walls"));
        this.Wb = this.register(new Value<Integer>("Wall Blue", this, 0, 0, 255)).visibleWhen(value -> Chams.mode.getValue().equalsIgnoreCase("Walls"));
        this.hand = this.register(new Value<Boolean>("Hand", this, true)).visibleWhen(value -> Chams.mode.getValue().equalsIgnoreCase("ESP"));
        this.lines = this.register(new Value<Boolean>("Lines", this, false)).visibleWhen(value -> Chams.mode.getValue().equalsIgnoreCase("ESP"));
        this.width = this.register(new Value<Float>("Width", this, 1.0f, 0.0f, 10.0f)).visibleWhen(value -> Chams.mode.getValue().equalsIgnoreCase("ESP"));
        this.friendColor = this.register(new Value<Boolean>("Friends", this, true)).visibleWhen(value -> Chams.mode.getValue().equalsIgnoreCase("ESP"));
        this.rainbow = this.register(new Value<Boolean>("Rainbow", this, false)).visibleWhen(value -> Chams.mode.getValue().equalsIgnoreCase("ESP"));
        this.r = this.register(new Value<Integer>("Red", this, 0, 0, 255)).visibleWhen(value -> Chams.mode.getValue().equalsIgnoreCase("ESP"));
        this.g = this.register(new Value<Integer>("Green", this, 255, 0, 255)).visibleWhen(value -> Chams.mode.getValue().equalsIgnoreCase("ESP"));
        this.b = this.register(new Value<Integer>("Blue", this, 255, 0, 255)).visibleWhen(value -> Chams.mode.getValue().equalsIgnoreCase("ESP"));
        this.a = this.register(new Value<Integer>("Alpha", this, 63, 0, 255)).visibleWhen(value -> Chams.mode.getValue().equalsIgnoreCase("ESP"));
        Chams.players = this.register(new Value<Boolean>("Players", this, true));
        Chams.animals = this.register(new Value<Boolean>("Animals", this, false));
        Chams.mobs = this.register(new Value<Boolean>("Mobs", this, false));
        Chams.crystals = this.register(new Value<Boolean>("Crystals", this, true)).visibleWhen(value -> Chams.mode.getValue().equalsIgnoreCase("ESP") || Chams.mode.getValue().equalsIgnoreCase("Walls"));
    }

    public static boolean renderChams(final Entity entity) {
        return !Chams.mode.getValue().equalsIgnoreCase("ESP") && ((entity instanceof Player) ? Chams.players.getValue() : (EntityUtil.isPassive(entity) ? Chams.animals.getValue() : (boolean)Chams.mobs.getValue()));
    }

    private Color getColor(final Entity entity) {
        if (this.friendColor.getValue() && Friends.isFriend(entity.getName().getString())) {
            return new Color(0.27f, 0.7f, 0.92f);
        }
        if (this.rainbow.getValue()) {
            return new Color(Xulu.rgb);
        }
        return new Color(this.r.getValue(), this.g.getValue(), this.b.getValue());
    }

    private void drawModel(final EntityModel<?> model, final PoseStack poseStack, final MultiBufferSource bufferSource, final RenderType type, final int packedLight, final int packedOverlay, final Color color, final float alpha, final boolean lines) {
        final VertexConsumer consumer = bufferSource.getBuffer(type);
        model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, alpha);
        ChamsRenderTypes.beginLines(lines, this.width.getValue());
        ChamsRenderTypes.endBatch(bufferSource, type);
        ChamsRenderTypes.endLines(lines);
    }

    @EventTarget
    public void renderPre(final EventModelRender event) {
        if (Chams.mode.getValue().equalsIgnoreCase("Walls")) {
            if (event.entity instanceof RemotePlayer && !Chams.players.getValue()) {
                return;
            }
            if (EntityUtil.isPassive(event.entity) && !Chams.animals.getValue()) {
                return;
            }
            if (!EntityUtil.isPassive(event.entity) && !Chams.mobs.getValue()) {
                return;
            }
            this.drawModel(event.modelBase, event.poseStack, event.bufferSource, ChamsRenderTypes.SOLID_NO_DEPTH, event.packedLight, event.packedOverlay, new Color(this.Wr.getValue(), this.Wg.getValue(), this.Wb.getValue()), 1.0f, false);
            this.drawModel(event.modelBase, event.poseStack, event.bufferSource, ChamsRenderTypes.SOLID_DEPTH, event.packedLight, event.packedOverlay, new Color(this.Vr.getValue(), this.Vg.getValue(), this.Vb.getValue()), 1.0f, false);
            event.setCancelled(true);
        }
        else if (Chams.mode.getValue().equalsIgnoreCase("ESP")) {
            if (event.getEventState() == Event.State.PRE && !(event.entity instanceof RemotePlayer)) {
                if (EntityUtil.isPassive(event.entity) && Chams.animals.getValue()) {
                    this.drawModel(event.modelBase, event.poseStack, event.bufferSource, ChamsRenderTypes.SOLID_NO_DEPTH, event.packedLight, event.packedOverlay, this.getColor(event.entity), this.a.getValue() / 255.0f, this.lines.getValue());
                    event.setCancelled(true);
                }
                else if (!EntityUtil.isPassive(event.entity) && Chams.mobs.getValue()) {
                    this.drawModel(event.modelBase, event.poseStack, event.bufferSource, ChamsRenderTypes.SOLID_NO_DEPTH, event.packedLight, event.packedOverlay, this.getColor(event.entity), this.a.getValue() / 255.0f, this.lines.getValue());
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventTarget
    public void renderPost(final EventPostRenderLayers event) {
        if (Chams.mode.getValue().equalsIgnoreCase("ESP")) {
            if (((LivingEntityRenderer)event.renderer).getTextureLocation(event.entity) == null) {
                return;
            }
            if (event.getEventState() == Event.State.PRE && event.entity instanceof RemotePlayer && Chams.players.getValue()) {
                this.drawModel(event.modelBase, event.poseStack, event.bufferSource, ChamsRenderTypes.SOLID_NO_DEPTH, event.packedLight, OverlayTexture.NO_OVERLAY, this.getColor(event.entity), this.a.getValue() / 255.0f / 2.0f, this.lines.getValue());
            }
        }
    }

    @EventTarget
    public void onPlayerModel(final EventModelPlayerRender event) {
        if (Chams.mode.getValue().equalsIgnoreCase("ESP") && Chams.players.getValue()) {
            if (event.getEventState() == Event.State.PRE) {
                this.drawModel(event.modelBase, event.poseStack, event.bufferSource, ChamsRenderTypes.SOLID_NO_DEPTH, event.packedLight, event.packedOverlay, this.getColor(event.entity), this.a.getValue() / 255.0f / 2.0f, this.lines.getValue());
                event.setCancelled(true);
            }
        }
    }
}
