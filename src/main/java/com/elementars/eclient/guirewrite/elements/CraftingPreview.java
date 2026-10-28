package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.guirewrite.Element;
import com.elementars.eclient.util.ColorUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.settings.Value;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CraftingPreview extends Element
{
    private static final ResourceLocation BOX = new ResourceLocation("textures/gui/container/generic_54.png");
    private final Value<String> background;

    public CraftingPreview() {
        super("CraftingPreview");
        this.background = this.register(new Value<String>("Background", this, "Texture", new String[] { "Texture", "Transparent", "None" }));
    }

    @Override
    public void onEnable() {
        this.width = 36.0;
        this.height = 36.0;
        super.onEnable();
    }

    @Override
    public void onRender() {
        if (CraftingPreview.mc.player == null) {
            return;
        }
        final int x = (int)this.x;
        final int y = (int)this.y;
        if (this.background.getValue().equalsIgnoreCase("Texture")) {
            RenderSystem.setShaderTexture(0, CraftingPreview.BOX);
            GuiComponent.blit(new PoseStack(), x, y, (int)this.width, (int)this.height, 7.0f, 17.0f, (int)this.width, (int)this.height, 256, 256);
        }
        else if (this.background.getValue().equalsIgnoreCase("Transparent")) {
            GuiComponent.fill(new PoseStack(), x, y, x + (int)this.width, y + (int)this.height, ColorUtils.changeAlpha(-15592942, 100));
        }
        for (int i = 1; i < 5; ++i) {
            final ItemStack itemStack = CraftingPreview.mc.player.inventoryMenu.getSlot(i).getItem();
            final int slotx = x + 1 + (i - 1) * 18 - ((i > 2) ? 36 : 0);
            final int sloty = y + 1 + ((i > 2) ? 18 : 0);
            CraftingPreview.mc.getItemRenderer().renderAndDecorateItem(itemStack, slotx, sloty);
            CraftingPreview.mc.getItemRenderer().renderGuiItemDecorations(CraftingPreview.mc.font, itemStack, slotx, sloty);
        }
    }
}
