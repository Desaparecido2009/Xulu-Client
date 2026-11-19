package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.enemy.Enemies;
import com.elementars.eclient.friend.Friends;
import com.elementars.eclient.module.render.ExtraTab;
import com.elementars.eclient.util.ColorTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(net.minecraft.client.gui.components.PlayerTabOverlay.class)
public class MixinPlayerTabOverlay
{
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;subList(II)Ljava/util/List;"))
    private List<PlayerInfo> xulu$expandTab(final List<PlayerInfo> list, final int fromIndex, final int toIndex) {
        final ExtraTab module = ExtraTab.INSTANCE;
        if (module == null || !module.isToggled()) {
            return list.subList(fromIndex, toIndex);
        }
        int endIndex = Math.min(module.tabSize.getValue(), list.size());
        if (endIndex < fromIndex) {
            endIndex = fromIndex;
        }
        return list.subList(fromIndex, endIndex);
    }

    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void xulu$colorName(final PlayerInfo info, final CallbackInfoReturnable<Component> callback) {
        final ExtraTab module = ExtraTab.INSTANCE;
        if (module == null || !module.isToggled()) {
            return;
        }
        final String name = info.getProfile().getName();
        String colorName = null;
        if (Friends.isFriend(name)) {
            colorName = module.color.getValue();
        }
        else if (Enemies.isEnemy(name)) {
            colorName = module.ecolor.getValue();
        }
        if (colorName == null) {
            return;
        }
        final String code = ColorTextUtils.getColor(colorName);
        final ChatFormatting formatting = ChatFormatting.getByCode(code.charAt(1));
        if (formatting != null) {
            callback.setReturnValue(callback.getReturnValue().copy().withStyle(formatting));
        }
    }
}
