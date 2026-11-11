package com.elementars.eclient.module.render;

import com.elementars.eclient.module.Category;
import com.elementars.eclient.module.Module;
import dev.xulu.settings.Value;
import net.minecraft.resources.ResourceLocation;

public class Cape extends Module
{
    public static Cape INSTANCE;
    private final Value<CapeType> cape;

    public Cape() {
        super("Capes", "Shows Xulu capes", 0, Category.CORE, true);
        this.cape = this.register(new Value<CapeType>("Cape", this, CapeType.NOCHE, CapeType.values()));
        Cape.INSTANCE = this;
    }

    public static boolean isEnabled() {
        return Cape.INSTANCE != null && Cape.INSTANCE.isToggled();
    }

    public static ResourceLocation getSelectedCape() {
        return Cape.INSTANCE == null ? null : Cape.INSTANCE.cape.getValue().getTexture();
    }

    public enum CapeType
    {
        NOCHE("capa_xuluport_noche_64x32.png"),
        NEGRA("capa_xuluport_negra_64x32.png"),
        NEGRA_COLOR("capa_xuluport_negra_color_64x32.png"),
        DEGRADADO("capa_xuluport_degradado_64x32.png"),
        BLANCA("capa_xuluport_blanca_64x32.png"),
        MARCO_NEON("capa_xuluport_marco_neon_64x32.png");

        private final ResourceLocation texture;

        CapeType(final String fileName) {
            this.texture = new ResourceLocation("xulu", "textures/cape/" + fileName);
        }

        public ResourceLocation getTexture() {
            return this.texture;
        }
    }
}
