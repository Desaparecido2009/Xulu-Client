package com.elementars.eclient.module;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import com.elementars.eclient.event.events.RenderEvent;
import com.elementars.eclient.util.XuluTessellator;
import com.elementars.eclient.util.VectorUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import com.elementars.eclient.Xulu;
import com.elementars.eclient.module.core.Global;
import com.elementars.eclient.module.core.CustomFont;
import com.elementars.eclient.module.core.TitleScreenShader;
import com.elementars.eclient.module.core.GuiBackgroundShader;
import com.elementars.eclient.module.combat.AutoArmor;
import com.elementars.eclient.module.combat.AutoCity;
import com.elementars.eclient.module.combat.AutoCrystal;
import com.elementars.eclient.module.combat.AutoTotem;
import com.elementars.eclient.module.combat.AutoTrap;
import com.elementars.eclient.module.combat.Aura;
import com.elementars.eclient.module.combat.Criticals;
import com.elementars.eclient.module.combat.EXPFast;
import com.elementars.eclient.module.combat.FastBow;
import com.elementars.eclient.module.combat.HoleBlocker;
import com.elementars.eclient.module.combat.HoleFill;
import com.elementars.eclient.module.combat.Offhand;
import com.elementars.eclient.module.combat.PearlPhase;
import com.elementars.eclient.module.combat.PopCounter;
import com.elementars.eclient.module.combat.Surround;
import com.elementars.eclient.module.movement.ElytraFly;
import com.elementars.eclient.module.movement.FastSwim;
import com.elementars.eclient.module.movement.Flight;
import com.elementars.eclient.module.movement.GuiMove;
import com.elementars.eclient.module.movement.NoSlowDown;
import com.elementars.eclient.module.movement.Sprint;
import com.elementars.eclient.module.movement.Velocity;
import com.elementars.eclient.module.movement.Scaffold;
import com.elementars.eclient.module.render.AntiFog;
import com.elementars.eclient.module.render.BreakESP;
import com.elementars.eclient.module.render.Chams;
import com.elementars.eclient.module.render.ExeterGui;
import com.elementars.eclient.module.render.NewGui;
import com.elementars.eclient.module.render.FullBright;
import com.elementars.eclient.module.render.HoleESP;
import com.elementars.eclient.module.render.ItemESP;
import com.elementars.eclient.module.render.NoHurtCam;
import com.elementars.eclient.module.render.ShulkerPreview;
import com.elementars.eclient.module.render.Skeleton;
import com.elementars.eclient.module.render.ToolTips;
import com.elementars.eclient.module.render.VoidESP;
import com.elementars.eclient.guirewrite.HudEditor;
import com.elementars.eclient.guirewrite.elements.Armor;
import com.elementars.eclient.guirewrite.elements.CraftingPreview;
import com.elementars.eclient.guirewrite.elements.Crystals;
import com.elementars.eclient.guirewrite.elements.Exp;
import com.elementars.eclient.guirewrite.elements.FeatureList;
import com.elementars.eclient.guirewrite.elements.Gapples;
import com.elementars.eclient.guirewrite.elements.HoleHud;
import com.elementars.eclient.guirewrite.elements.Info;
import com.elementars.eclient.guirewrite.elements.InvPreview;
import com.elementars.eclient.guirewrite.elements.Logo;
import com.elementars.eclient.guirewrite.elements.Obsidian;
import com.elementars.eclient.guirewrite.elements.Position;
import com.elementars.eclient.guirewrite.elements.Potions;
import com.elementars.eclient.guirewrite.elements.PvPInfo;
import com.elementars.eclient.guirewrite.elements.Target;
import com.elementars.eclient.guirewrite.elements.TextRadar;
import com.elementars.eclient.guirewrite.elements.Totems;
import com.elementars.eclient.guirewrite.elements.Watermark;
import com.elementars.eclient.module.misc.FakeVanilla;
import com.elementars.eclient.module.misc.FovSlider;
import com.elementars.eclient.module.misc.Time;
import com.elementars.eclient.module.player.AntiVoid;
import com.elementars.eclient.module.player.NoBreakAnimation;
import com.elementars.eclient.module.player.Speedmine;
import com.elementars.eclient.module.player.XCarry;
import java.util.ArrayList;

public class ModuleManager
{
    private static ArrayList<Module> modules;

    public void init() {
        ModuleManager.modules.add(new Global());
        ModuleManager.modules.add(new CustomFont());
        ModuleManager.modules.add(new TitleScreenShader());
        ModuleManager.modules.add(new GuiBackgroundShader());
        ModuleManager.modules.add(new Aura());
        ModuleManager.modules.add(new AutoArmor());
        ModuleManager.modules.add(new AutoCity());
        ModuleManager.modules.add(new AutoCrystal());
        ModuleManager.modules.add(new AutoTotem());
        ModuleManager.modules.add(new AutoTrap());
        ModuleManager.modules.add(new Criticals());
        ModuleManager.modules.add(new EXPFast());
        ModuleManager.modules.add(new FastBow());
        ModuleManager.modules.add(new HoleBlocker());
        ModuleManager.modules.add(new HoleFill());
        ModuleManager.modules.add(new Offhand());
        ModuleManager.modules.add(new PearlPhase());
        ModuleManager.modules.add(new PopCounter());
        ModuleManager.modules.add(new Surround());
        ModuleManager.modules.add(new ElytraFly());
        ModuleManager.modules.add(new FastSwim());
        ModuleManager.modules.add(new Flight());
        ModuleManager.modules.add(new GuiMove());
        ModuleManager.modules.add(new NoSlowDown());
        ModuleManager.modules.add(new Sprint());
        ModuleManager.modules.add(new Scaffold());
        ModuleManager.modules.add(new Velocity());
        ModuleManager.modules.add(new NewGui());
        ModuleManager.modules.add(new ExeterGui());
        ModuleManager.modules.add(new AntiFog());
        ModuleManager.modules.add(new BreakESP());
        ModuleManager.modules.add(new Chams());
        ModuleManager.modules.add(new FullBright());
        ModuleManager.modules.add(new HoleESP());
        ModuleManager.modules.add(new ItemESP());
        ModuleManager.modules.add(new NoHurtCam());
        ModuleManager.modules.add(new ShulkerPreview());
        ModuleManager.modules.add(new Skeleton());
        ModuleManager.modules.add(new ToolTips());
        ModuleManager.modules.add(new VoidESP());
        ModuleManager.modules.add(new FakeVanilla());
        ModuleManager.modules.add(new FovSlider());
        ModuleManager.modules.add(new Time());
        ModuleManager.modules.add(new AntiVoid());
        ModuleManager.modules.add(new NoBreakAnimation());
        ModuleManager.modules.add(new Speedmine());
        ModuleManager.modules.add(new XCarry());
        ModuleManager.modules.add(new PvPInfo());
        ModuleManager.modules.add(new TextRadar());
        ModuleManager.modules.add(new FeatureList());
        ModuleManager.modules.add(new Position());
        ModuleManager.modules.add(new Watermark());
        ModuleManager.modules.add(new Potions());
        ModuleManager.modules.add(new Totems());
        ModuleManager.modules.add(new Obsidian());
        ModuleManager.modules.add(new Crystals());
        ModuleManager.modules.add(new Gapples());
        ModuleManager.modules.add(new Exp());
        ModuleManager.modules.add(new InvPreview());
        ModuleManager.modules.add(new CraftingPreview());
        ModuleManager.modules.add(new HoleHud());
        ModuleManager.modules.add(new Info());
        ModuleManager.modules.add(new Armor());
        ModuleManager.modules.add(new Logo());
        ModuleManager.modules.add(new Target());
        ModuleManager.modules.add(new HudEditor());
    }

    public static void onUpdate() {
        Xulu.MODULE_MANAGER.getModules().stream().filter(Module::isToggled).forEach(Module::onUpdate);
        NewGui.resetGui();
        Xulu.TASK_SCHEDULER.onUpdate();
    }

    public static void onRender() {
        Xulu.MODULE_MANAGER.getModules().stream().filter(Module::isToggled).forEach(Module::onRender);
    }

    public static void onWorldRender(final RenderLevelLastEvent event) {
        Minecraft.getInstance().getProfiler().push("eclient");
        Minecraft.getInstance().getProfiler().push("setup");
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.lineWidth(1.0f);
        final Vec3 renderPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        final RenderEvent e = new RenderEvent(XuluTessellator.INSTANCE, renderPos);
        e.resetTranslation();
        final PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.mulPoseMatrix(event.getPoseStack().last().pose());
        VectorUtils.modelMatrix = poseStack.last().pose().copy();
        VectorUtils.projectionMatrix = event.getProjectionMatrix().copy();
        RenderSystem.applyModelViewMatrix();
        Minecraft.getInstance().getProfiler().pop();
        Xulu.MODULE_MANAGER.getModules().stream().filter(Module::isToggled).forEach(module -> {
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.lineWidth(1.0f);
            Minecraft.getInstance().getProfiler().push(module.getName());
            module.onWorldRender(e);
            Minecraft.getInstance().getProfiler().pop();
        });
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
        Minecraft.getInstance().getProfiler().push("release");
        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        XuluTessellator.releaseGL();
        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
    }

    public ArrayList<Module> getModules() {
        return ModuleManager.modules;
    }

    @Deprecated
    public Module getModuleByName(final String name) {
        return ModuleManager.modules.stream().filter(module -> module.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Module getModule(final Class<? extends Module> clazz) {
        return ModuleManager.modules.stream().filter(module -> module.getClass() == clazz).findFirst().orElse(null);
    }

    public <T extends Module> T getModuleT(final Class<T> clazz) {
        return ModuleManager.modules.stream().filter(module -> module.getClass() == clazz).map(clazz::cast).findFirst().orElse(null);
    }

    public static boolean isModuleEnabled(final String name) {
        final Module m = ModuleManager.modules.stream().filter(module -> module.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        return m != null && m.isToggled();
    }

    static {
        ModuleManager.modules = new ArrayList<Module>();
    }
}
