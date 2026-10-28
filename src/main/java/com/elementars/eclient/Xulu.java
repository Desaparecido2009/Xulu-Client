package com.elementars.eclient;

import com.elementars.eclient.command.Command;
import com.elementars.eclient.command.CommandManager;
import com.elementars.eclient.event.EventManager;
import com.elementars.eclient.event.events.EventKey;
import com.elementars.eclient.event.events.EventUpdate;
import com.elementars.eclient.event.events.LocalPlayerUpdateEvent;
import com.elementars.eclient.font.CFontManager;
import com.elementars.eclient.font.XFontRenderer;
import com.elementars.eclient.font.custom.CustomFont;
import com.elementars.eclient.guirewrite.HUD;
import com.elementars.eclient.module.Module;
import com.elementars.eclient.module.ModuleManager;
import com.elementars.eclient.module.core.Global;
import com.elementars.eclient.util.ColorTextUtils;
import com.elementars.eclient.util.ColorUtils;
import com.elementars.eclient.util.LagCompensator;
import com.elementars.eclient.util.LagUtil;
import com.elementars.eclient.util.RainbowUtils;
import com.elementars.eclient.util.TaskScheduler;
import com.elementars.eclient.util.TargetPlayers;
import com.elementars.eclient.util.Wrapper;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xulu.newgui.NewGUI;
import dev.xulu.newgui.util.ColorUtil;
import dev.xulu.settings.ConfigManager;
import dev.xulu.settings.ValueManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.text.DecimalFormat;

@Mod("xulu")
public class Xulu
{
    public static final TaskScheduler TASK_SCHEDULER;
    public static final ValueManager VALUE_MANAGER;
    public static final EventManager EVENT_MANAGER;
    public static final ModuleManager MODULE_MANAGER;
    public static final CommandManager COMMAND_MANAGER;
    public static boolean CustomFont;
    public static int rgb;
    public static CFontManager cFontRenderer;
    public static NewGUI newGUI;
    public static HUD hud;
    public static Xulu INSTANCE;
    public static com.elementars.eclient.util.GLSLSandboxShader backgroundShader;
    public static com.elementars.eclient.util.GLSLShaders randomShader;
    public static long initTime;
    public static DecimalFormat df;
    private boolean shownLag;
    private int beginY;
    private int endY;
    private int yCount;
    private static boolean initialized;

    static {
        TASK_SCHEDULER = new TaskScheduler();
        VALUE_MANAGER = new ValueManager();
        EVENT_MANAGER = new EventManager();
        MODULE_MANAGER = new ModuleManager();
        COMMAND_MANAGER = new CommandManager();
        df = new DecimalFormat("##,###,###,###,##0.00");
    }

    public Xulu() {
        if (initialized) {
            return;
        }
        initialized = true;
        INSTANCE = this;
        ColorTextUtils.initColors();
        MODULE_MANAGER.init();
        COMMAND_MANAGER.init();
        cFontRenderer = new CFontManager();
        LagCompensator.INSTANCE = new LagCompensator();
        LagUtil.INSTANCE = new LagUtil();
        rgb = Color.HSBtoRGB(0.01f, Global.rainbowSaturation.getValue() / 255.0f, Global.rainbowLightness.getValue() / 255.0f);
        new com.elementars.eclient.font.FontInit().initFonts();
        Wrapper.getFileManager().loadFriends();
        Wrapper.getFileManager().loadEnemies();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(this::initClientGui);
    }

    private void initClientGui() {
        Xulu.newGUI = new NewGUI();
        Xulu.hud = new HUD();
        HUD.registerElements();
        Xulu.hud.refreshPanel();
        Wrapper.getFileManager().loadHUD();
        Wrapper.getFileManager().loadNewGui();
        ConfigManager.load("main");
    }

    @SubscribeEvent
    public void onUpdate(final TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().player == null) {
            return;
        }
        new EventUpdate().call();
        ModuleManager.onUpdate();
        TargetPlayers.onUpdate();
        RainbowUtils.updateRainbow();
        final com.elementars.eclient.module.core.CustomFont customFont = MODULE_MANAGER.getModuleT(com.elementars.eclient.module.core.CustomFont.class);
        CustomFont = customFont != null && customFont.isToggled();
    }

    @SubscribeEvent
    public void onPlayerUpdate(final LivingEvent.LivingUpdateEvent event) {
        if (Minecraft.getInstance().level != null && event.getEntity().level.isClientSide && event.getEntityLiving().equals(Minecraft.getInstance().player)) {
            new LocalPlayerUpdateEvent(event.getEntityLiving()).call();
        }
    }

    @SubscribeEvent
    public void onAttack(final AttackEntityEvent event) {
        TargetPlayers.onAttack(event);
    }

    @SubscribeEvent
    public void onKeyInput(final InputEvent.KeyInputEvent event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        final EventKey eventKey = new EventKey(event.getKey());
        eventKey.call();
        MODULE_MANAGER.getModules().stream().filter(module -> module.getKey() == eventKey.getKey() && eventKey.getKey() != 0).forEach(Module::toggle);
    }

    @SubscribeEvent
    public void onRenderWorldLast(final RenderLevelLastEvent event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        ModuleManager.onWorldRender(event);
    }

    @SubscribeEvent
    public void onRenderOverlay(final RenderGameOverlayEvent.Post event) {
        CustomFont = MODULE_MANAGER.getModuleT(com.elementars.eclient.module.core.CustomFont.class).isToggled();
        final int targetBeginY = CustomFont ? -((int)cFontRenderer.getHeight()) : (-Minecraft.getInstance().font.lineHeight);
        if (this.beginY != targetBeginY) {
            this.beginY = targetBeginY;
        }
        if (this.endY != 3) {
            this.endY = 3;
        }
        Rainbow.updateRainbow();
        Xulu.rgb = Rainbow.rgb;
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        if (!(Boolean)VALUE_MANAGER.getValueByName("Rainbow Watermark").getValue()) {
            Xulu.rgb = ColorUtil.getClickGUIColor().getRGB();
        }
        if (Minecraft.getInstance().player == null) {
            return;
        }
        final Minecraft mc = Minecraft.getInstance();
        final PoseStack poseStack = event.getMatrixStack();
        final int width = mc.getWindow().getGuiScaledWidth();
        if (Global.showLag.getValue() && !mc.hasSingleplayerServer()) {
            if (LagUtil.INSTANCE.getLastTimeDiff() != 0L && LagUtil.INSTANCE.getLastTimeDiff() > 5000L) {
                final String text = String.format("Server has been lagging for %01.1fs", (LagUtil.INSTANCE.getLastTimeDiff() - 1000L) / 1000.0f);
                if (!this.shownLag) {
                    this.yCount = this.beginY;
                }
                if (CustomFont) {
                    cFontRenderer.drawStringWithShadow(text, width / 2 - cFontRenderer.getStringWidth(text) / 2, this.yCount, Global.lagColor.getValue().equalsIgnoreCase("Default") ? Color.LIGHT_GRAY.getRGB() : ColorUtil.getClickGUIColor().getRGB());
                }
                else {
                    mc.font.drawShadow(poseStack, text, (float)(width / 2 - mc.font.width(text) / 2), (float)this.yCount, Global.lagColor.getValue().equalsIgnoreCase("Default") ? Color.LIGHT_GRAY.getRGB() : ColorUtil.getClickGUIColor().getRGB());
                }
                this.shownLag = true;
                if (this.yCount != this.endY) {
                    ++this.yCount;
                }
            }
            else if (this.shownLag) {
                final String text = "Server has been lagging for 0.0s";
                if (CustomFont) {
                    cFontRenderer.drawStringWithShadow(text, width / 2 - cFontRenderer.getStringWidth(text) / 2, this.yCount, Global.lagColor.getValue().equalsIgnoreCase("Default") ? Color.LIGHT_GRAY.getRGB() : ColorUtil.getClickGUIColor().getRGB());
                }
                else {
                    mc.font.drawShadow(poseStack, text, (float)(width / 2 - mc.font.width(text) / 2), (float)this.yCount, Global.lagColor.getValue().equalsIgnoreCase("Default") ? Color.LIGHT_GRAY.getRGB() : ColorUtil.getClickGUIColor().getRGB());
                }
                if (this.yCount != this.beginY) {
                    --this.yCount;
                }
                else {
                    this.shownLag = false;
                }
            }
        }
        ModuleManager.onRender();
        Xulu.rgb = Rainbow.rgb;
    }

    @SubscribeEvent
    public void onClientLoggedIn(final ClientPlayerNetworkEvent.LoggedInEvent event) {
        ConfigManager.applyPending();
    }

    @SubscribeEvent
    public void onClientLoggedOut(final ClientPlayerNetworkEvent.LoggedOutEvent event) {
        ConfigManager.saveMain();
    }

    @SubscribeEvent
    public void onClientChat(final ClientChatEvent event) {
        final String message = event.getMessage();
        if (message.startsWith(Command.getPrefix())) {
            event.setCanceled(true);
            CommandManager.runCommand(message.substring(Command.getPrefix().length()));
        }
    }

    @SubscribeEvent
    public void onDrawScreenPost(final ScreenEvent.DrawScreenEvent.Post event) {
        if (event.getScreen() instanceof TitleScreen) {
            Minecraft.getInstance().font.drawShadow(event.getPoseStack(), "Xulu Reborn by Desaparecido", 4, event.getScreen().height - 12, -1);
        }
    }

    public static String[] getFonts() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    }

    public static String getCorrectFont(final String stringIn) {
        for (final String s : getFonts()) {
            if (s.equalsIgnoreCase(stringIn)) {
                return s;
            }
        }
        return null;
    }

    public static String getTitle(String in) {
        in = Character.toUpperCase(in.toLowerCase().charAt(0)) + in.toLowerCase().substring(1);
        return in;
    }

    public static String getFacing(final String in) {
        final String facing = getTitle(in);
        String add;
        if (in.equalsIgnoreCase("North")) {
            add = " \u00a77[\u00a7r-Z\u00a77]";
        }
        else if (in.equalsIgnoreCase("East")) {
            add = " \u00a77[\u00a7r+X\u00a77]";
        }
        else if (in.equalsIgnoreCase("South")) {
            add = " \u00a77[\u00a7r+Z\u00a77]";
        }
        else if (in.equalsIgnoreCase("West")) {
            add = " \u00a77[\u00a7r-X\u00a77]";
        }
        else {
            add = " ERROR";
        }
        return facing + add;
    }

    public static void setcFontRendererDefault() {
        try {
            CFontManager.customFont = new CustomFont(new Font("Verdana", 0, 18), true, false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setCFontRenderer(final String stringIn, final int size) {
        try {
            if (getCorrectFont(stringIn) == null) {
                Command.sendChatMessage("Invalid font!");
                return;
            }
            CFontManager.customFont = new CustomFont(new Font(getCorrectFont(stringIn), 0, size), true, false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setXdolfFontRenderer(final String stringIn, final int size) {
        try {
            if (getCorrectFont(stringIn) == null) {
                Command.sendChatMessage("Invalid font!");
                return;
            }
            CFontManager.xFontRenderer = new XFontRenderer(new Font(getCorrectFont(stringIn), 0, size), true, 8);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setXdolfFontRendererDefault() {
        try {
            CFontManager.xFontRenderer = new XFontRenderer(new Font("Verdana", 0, 36), true, 8);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Rainbow
    {
        private static int rgb;
        public static int a;
        public static int r;
        public static int g;
        public static int b;
        static float hue;

        public static void updateRainbow() {
            Rainbow.rgb = Color.HSBtoRGB(Rainbow.hue, com.elementars.eclient.module.core.Global.rainbowSaturation.getValue() / 255.0f, com.elementars.eclient.module.core.Global.rainbowLightness.getValue() / 255.0f);
            Rainbow.a = (Rainbow.rgb >>> 24 & 0xFF);
            Rainbow.r = (Rainbow.rgb >>> 16 & 0xFF);
            Rainbow.g = (Rainbow.rgb >>> 8 & 0xFF);
            Rainbow.b = (Rainbow.rgb & 0xFF);
            Rainbow.hue += com.elementars.eclient.module.core.Global.rainbowspeed2.getValue() / 100000.0f;
            if (Rainbow.hue > 1.0f) {
                --Rainbow.hue;
            }
        }

        static {
            Rainbow.hue = 0.01f;
        }
    }
}
