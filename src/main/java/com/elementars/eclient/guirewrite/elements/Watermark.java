package com.elementars.eclient.guirewrite.elements;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.guirewrite.Element;
import com.elementars.eclient.module.core.Global;
import com.elementars.eclient.util.ColorUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.newgui.util.ColorUtil;
import dev.xulu.settings.Value;

public class Watermark extends Element
{
    private final Value<String> text;
    private final Value<Boolean> rainbow;

    public Watermark() {
        super("Watermark");
        this.text = this.register(new Value<String>("Mode", this, "Xulu", new String[] { "Xulu", "PK Client", "WideHack" })).onChanged(onChangedValue -> this.updateSize());
        this.rainbow = this.register(new Value<Boolean>("Rainbow", this, false));
    }

    @Override
    public void onEnable() {
        this.updateSize();
        super.onEnable();
    }

    private void updateSize() {
        this.width = this.getStringWidth(this.getText() + " v1.5.2") + 2;
        this.height = this.getFontHeight() + 2;
    }

    private String getText() {
        return this.text.getValue() + " v1.5.2";
    }

    @Override
    public void onRender() {
        this.updateSize();
        int color = ColorUtil.getClickGUIColor().getRGB();
        if (this.rainbow.getValue()) {
            color = Xulu.rgb;
        }
        if (Xulu.CustomFont) {
            Xulu.cFontRenderer.drawStringWithShadow(this.getText(), this.x + 1.0, this.y + 1.0, ColorUtils.changeAlpha(color, Global.hudAlpha.getValue()));
        }
        else {
            Watermark.fontRenderer.drawShadow(new PoseStack(), this.getText(), (float)this.x + 1.0f, (float)this.y + 1.0f, ColorUtils.changeAlpha(color, Global.hudAlpha.getValue()));
        }
    }

    private int getStringWidth(final String text) {
        return Xulu.CustomFont ? Xulu.cFontRenderer.getStringWidth(text) : Watermark.fontRenderer.width(text);
    }

    private float getFontHeight() {
        return Xulu.CustomFont ? Xulu.cFontRenderer.getHeight() : Watermark.fontRenderer.lineHeight;
    }
}
