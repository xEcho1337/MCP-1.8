package it.echo.kira.module.impl;

import it.echo.kira.event.impl.ClientTickEvent;
import it.echo.kira.event.impl.RenderWorldEvent;
import it.echo.kira.module.Module;
import it.echo.kira.module.ModuleCategory;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

/**
 * Draws a trail behind every flying ender pearl.
 */
public class PearlCrumbs extends Module {

    private static final int MAX_CRUMBS = 600;
    private static final double MIN_STEP = 0.05D;
    private static final float LINE_WIDTH = 2.0F;
    private static final int TRAIL = new Color(255, 70, 70, 150).getRGB();

    private final Map<Integer, List<Vec3>> trails = new HashMap<>();

    public PearlCrumbs() {
        super("PearlCrumbs", ModuleCategory.RENDER);
        listen(ClientTickEvent.class, event -> trackPearls());
        listen(RenderWorldEvent.class, this::renderTrails);
    }

    @Override
    protected void onDisable() {
        trails.clear();
    }

    private void trackPearls() {
        if (!enabled() || MC.theWorld == null || MC.thePlayer == null) {
            trails.clear();
            return;
        }
        Set<Integer> alive = new HashSet<>();
        for (Entity entity : MC.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityEnderPearl pearl)) {
                continue;
            }
            alive.add(pearl.getEntityId());
            List<Vec3> crumbs = trails.computeIfAbsent(pearl.getEntityId(), key -> new ArrayList<>());
            Vec3 current = new Vec3(pearl.posX, pearl.posY, pearl.posZ);
            if (crumbs.isEmpty() || crumbs.get(crumbs.size() - 1).distanceTo(current) > MIN_STEP) {
                crumbs.add(current);
            }
            while (crumbs.size() > MAX_CRUMBS) {
                crumbs.remove(0);
            }
        }
        trails.keySet().retainAll(alive);
    }

    private void renderTrails(RenderWorldEvent event) {
        if (!enabled() || trails.isEmpty()) {
            return;
        }
        Entity viewer = MC.getRenderViewEntity();
        if (viewer == null) {
            return;
        }
        float partialTicks = event.partialTicks();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(red(TRAIL), green(TRAIL), blue(TRAIL), alpha(TRAIL));
        GL11.glLineWidth(LINE_WIDTH);
        for (List<Vec3> crumbs : trails.values()) {
            if (crumbs.size() < 2) {
                continue;
            }
            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (Vec3 crumb : crumbs) {
                GL11.glVertex3d(crumb.xCoord - viewerX, crumb.yCoord - viewerY, crumb.zCoord - viewerZ);
            }
            GL11.glEnd();
        }
        GL11.glLineWidth(1.0F);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static float alpha(int color) {
        return ((color >> 24) & 0xFF) / 255.0F;
    }

    private static float red(int color) {
        return ((color >> 16) & 0xFF) / 255.0F;
    }

    private static float green(int color) {
        return ((color >> 8) & 0xFF) / 255.0F;
    }

    private static float blue(int color) {
        return (color & 0xFF) / 255.0F;
    }
}
