package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.guirewrite.Element;
import com.elementars.eclient.util.ColorUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.settings.Value;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class InvPreview extends Element
{
    private static final ResourceLocation BOX = new ResourceLocation("textures/gui/container/generic_54.png");
    private final Value<String> background;

    public InvPreview() {
        super("InvPreview");
        this.background = this.register(new Value<String>("Background", this, "Texture", new String[] { "Texture", "Transparent", "None" }));
    }

    @Override
    public void onEnable() {
        this.width = 162.0;
        this.height = 54.0;
        super.onEnable();
    }

    @Override
    public void onRender() {
        if (InvPreview.mc.player == null) {
            return;
        }
        final int x = (int)this.x;
        final int y = (int)this.y;
        if (this.background.getValue().equalsIgnoreCase("Texture")) {
            RenderSystem.setShaderTexture(0, InvPreview.BOX);
            GuiComponent.blit(new PoseStack(), x, y, (int)this.width, (int)this.height, 7.0f, 17.0f, (int)this.width, (int)this.height, 256, 256);
        }
        else if (this.background.getValue().equalsIgnoreCase("Transparent")) {
            GuiComponent.fill(new PoseStack(), x, y, x + (int)this.width, y + (int)this.height, ColorUtils.changeAlpha(-15592942, 100));
        }
        this.itemrender(InvPreview.mc.player.getInventory().items, x, y);
    }

    private void itemrender(final java.util.List<ItemStack> items, final int x, final int y) {
        for (int item = 9; item < items.size(); ++item) {
            final int slotx = x + 1 + item % 9 * 18;
            final int sloty = y + 1 + (item / 9 - 1) * 18;
            final ItemStack itemStack = items.get(item);
            InvPreview.mc.getItemRenderer().renderAndDecorateItem(itemStack, slotx, sloty);
            InvPreview.mc.getItemRenderer().renderGuiItemDecorations(InvPreview.mc.font, itemStack, slotx, sloty);
        }
    }
}
