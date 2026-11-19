package com.elementars.eclient.module.render;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.util.ColorTextUtils;
import dev.xulu.settings.Value;

public class ExtraTab extends Module
{
    public final Value<Integer> tabSize;
    public final Value<String> color;
    public final Value<String> ecolor;
    public static ExtraTab INSTANCE;

    public ExtraTab() {
        super("ExtraTab", "Expands tab menu", 0, Category.RENDER, true);
        this.tabSize = this.register(new Value<Integer>("Players", this, 80, 1, 1000));
        this.color = this.register(new Value<String>("Friend Color", this, "LightGreen", ColorTextUtils.colors));
        this.ecolor = this.register(new Value<String>("Enemy Color", this, "LightRed", ColorTextUtils.colors));
        ExtraTab.INSTANCE = this;
    }
}
