package com.elementars.eclient.module.combat;

import com.elementars.eclient.event.EventTarget;
import com.elementars.eclient.event.events.EventAttackEntity;
import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.LivingEntity;

public class Criticals extends Module
{
    private final Value<String> mode;

    public Criticals() {
        super("Criticals", "Automatically crits people", 0, Category.COMBAT, true);
        this.mode = this.register(new Value<String>("Mode", this, "Packet", new String[] { "Jump", "Packet" }));
    }

    @EventTarget
    public void onAttack(final EventAttackEntity event) {
        if (Criticals.mc.player.isOnGround() && !Criticals.mc.options.keyJump.isDown() && event.getTarget() instanceof LivingEntity) {
            if (this.mode.getValue().equalsIgnoreCase("Packet")) {
                Criticals.mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 0.10000000149011612, Criticals.mc.player.getZ(), false));
                Criticals.mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY(), Criticals.mc.player.getZ(), false));
            }
            else {
                Criticals.mc.player.jumpFromGround();
            }
        }
    }
}
