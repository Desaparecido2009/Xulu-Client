package com.elementars.eclient.mixin.mixins;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.module.render.ShulkerPreview;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class MixinScreenShulkerPreview
{
    @Shadow
    protected Font font;

    @Inject(method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"), cancellable = true)
    private void xulu$shulkerPreview(final PoseStack poseStack, final ItemStack stack, final int x, final int y, final CallbackInfo info) {
        final ShulkerPreview module = Xulu.MODULE_MANAGER.getModuleT(ShulkerPreview.class);
        if (module == null || !module.isToggled()) {
            return;
        }
        if (!(stack.getItem() instanceof BlockItem)) {
            return;
        }
        if (!(((BlockItem)stack.getItem()).getBlock() instanceof ShulkerBoxBlock)) {
            return;
        }
        final CompoundTag tag = BlockItem.getBlockEntityData(stack);
        if (tag == null || !tag.contains("Items", Tag.TAG_LIST)) {
            return;
        }
        info.cancel();
        final NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        final Minecraft minecraft = Minecraft.getInstance();
        final ItemRenderer itemRenderer = minecraft.getItemRenderer();
        final int width = Math.max(144, font.width(stack.getHoverName()) + 3);
        final int x2 = x + 12;
        final int y2 = y - 12;
        final int height = 57;
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, 400.0);
        RenderSystem.enableDepthTest();
        GuiComponent.fill(poseStack, x2 - 3, y2 - 4, x2 + width + 3, y2 - 3, -267386864);
        GuiComponent.fill(poseStack, x2 - 3, y2 + height + 3, x2 + width + 3, y2 + height + 4, -267386864);
        GuiComponent.fill(poseStack, x2 - 3, y2 - 3, x2 + width + 3, y2 + height + 3, -267386864);
        GuiComponent.fill(poseStack, x2 - 4, y2 - 3, x2 - 3, y2 + height + 3, -267386864);
        GuiComponent.fill(poseStack, x2 + width + 3, y2 - 3, x2 + width + 4, y2 + height + 3, -267386864);
        GuiComponent.fill(poseStack, x2 - 3, y2 - 2, x2 - 2, y2 + height + 2, 1347420415);
        GuiComponent.fill(poseStack, x2 + width + 2, y2 - 2, x2 + width + 3, y2 + height + 2, 1347420415);
        GuiComponent.fill(poseStack, x2 - 3, y2 - 3, x2 + width + 3, y2 - 2, 1347420415);
        GuiComponent.fill(poseStack, x2 - 3, y2 + height + 2, x2 + width + 3, y2 + height + 3, 1344798847);
        font.draw(poseStack, stack.getHoverName().getString(), x + 12, y - 12, 16777215);
        final float oldBlitOffset = itemRenderer.blitOffset;
        itemRenderer.blitOffset = 400.0f;
        try {
            for (int i = 0; i < items.size(); ++i) {
                final int itemX = x + i % 9 * 16 + 11;
                final int itemY = y + i / 9 * 16 - 11 + 8;
                final ItemStack itemStack = items.get(i);
                itemRenderer.renderAndDecorateItem(itemStack, itemX, itemY);
                itemRenderer.renderGuiItemDecorations(font, itemStack, itemX, itemY);
            }
        }
        finally {
            itemRenderer.blitOffset = oldBlitOffset;
            poseStack.popPose();
        }
    }
}
