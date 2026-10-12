package it.echo.kira.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.optifine.CustomPanorama;
import net.optifine.CustomPanoramaProperties;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

/**
 * Immediate-mode 2D primitives.
 */
public final class Render2D {

    private static final int QUADS = 7;
    private static final int FAN = 6;
    private static final double TAU = Math.PI * 2.0D;
    private static final int SKYBOX_VIEWPORT = 256;
    private static final float SKYBOX_FOV = 120.0F;
    private static final int SKYBOX_JITTER_GRID = 8;
    private static final int SKYBOX_PASSES = 64;
    private static final int SKYBOX_BLUR_PASSES = 3;

    public static final ResourceLocation[] DEFAULT_PANORAMA = {
            new ResourceLocation("textures/gui/title/background/panorama_0.png"),
            new ResourceLocation("textures/gui/title/background/panorama_1.png"),
            new ResourceLocation("textures/gui/title/background/panorama_2.png"),
            new ResourceLocation("textures/gui/title/background/panorama_3.png"),
            new ResourceLocation("textures/gui/title/background/panorama_4.png"),
            new ResourceLocation("textures/gui/title/background/panorama_5.png")
    };

    private Render2D() {
    }

    public static boolean inside(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public static void rect(double x, double y, double width, double height, int color) {
        fill((float) x, (float) y, (float) (x + width), (float) (y + height), color);
    }

    public static void outline(double x, double y, double width, double height, float lineWidth, int color) {
        float left = (float) x;
        float top = (float) y;
        float right = (float) (x + width);
        float bottom = (float) (y + height);
        // 1px-style borders built from quads so they stay crisp without line smoothing
        float w = Math.max(1.0F, lineWidth);
        fill(left, top, right, top + w, color);
        fill(left, bottom - w, right, bottom, color);
        fill(left, top + w, left + w, bottom - w, color);
        fill(right - w, top + w, right, bottom - w, color);
    }

    public static void outline(double x, double y, double width, double height, int color) {
        outline(x, y, width, height, 1.0F, color);
    }

    public static void gradientVertical(double x, double y, double width, double height, int topColor, int bottomColor) {
        float left = (float) x;
        float top = (float) y;
        float right = (float) (x + width);
        float bottom = (float) (y + height);
        float topA = alpha(topColor);
        float topR = red(topColor);
        float topG = green(topColor);
        float topB = blue(topColor);
        float botA = alpha(bottomColor);
        float botR = red(bottomColor);
        float botG = green(bottomColor);
        float botB = blue(bottomColor);

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(QUADS, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos(right, top, 0.0D).color(topR, topG, topB, topA).endVertex();
        renderer.pos(left, top, 0.0D).color(topR, topG, topB, topA).endVertex();
        renderer.pos(left, bottom, 0.0D).color(botR, botG, botB, botA).endVertex();
        renderer.pos(right, bottom, 0.0D).color(botR, botG, botB, botA).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void horizontalLine(double x1, double x2, double y, int color) {
        double left = Math.min(x1, x2);
        double width = Math.abs(x2 - x1);
        rect(left, y, width, 1.0D, color);
    }

    public static void verticalLine(double x, double y1, double y2, int color) {
        double top = Math.min(y1, y2);
        double height = Math.abs(y2 - y1);
        rect(x, top, 1.0D, height, color);
    }

    public static void circle(double centerX, double centerY, double radius, int color) {
        disc((float) centerX, (float) centerY, (float) radius, color, 0.0D, TAU);
    }

    public static void roundedRect(double x, double y, double width, double height, double radius, int color) {
        float left = (float) x;
        float top = (float) y;
        float right = (float) (x + width);
        float bottom = (float) (y + height);
        float rad = (float) Math.min(radius, Math.min(width, height) / 2.0D);
        if (rad <= 0.5F) {
            fill(left, top, right, bottom, color);
            return;
        }
        fill(left + rad, top, right - rad, bottom, color);
        fill(left, top + rad, left + rad, bottom - rad, color);
        fill(right - rad, top + rad, right, bottom - rad, color);
        disc(left + rad, top + rad, rad, color, Math.PI, Math.PI * 1.5D);
        disc(right - rad, top + rad, rad, color, Math.PI * 1.5D, TAU);
        disc(left + rad, bottom - rad, rad, color, Math.PI * 0.5D, Math.PI);
        disc(right - rad, bottom - rad, rad, color, 0.0D, Math.PI * 0.5D);
    }

    public static void roundedOutline(double x, double y, double width, double height, double radius, float lineWidth,
            int color) {
        float rad = (float) Math.min(radius, Math.min(width, height) / 2.0D);
        if (rad <= 0.5F) {
            outline(x, y, width, height, lineWidth, color);
            return;
        }
        float alpha = alpha(color);
        float red = red(color);
        float green = green(color);
        float blue = blue(color);

        double[] centersX = {x + rad, x + width - rad, x + width - rad, x + rad};
        double[] centersY = {y + rad, y + rad, y + height - rad, y + height - rad};
        double[] starts = {Math.PI, Math.PI * 1.5D, 0.0D, Math.PI * 0.5D};
        int arcSteps = 10;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glLineWidth(Math.max(1.0F, lineWidth));
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GlStateManager.color(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int corner = 0; corner < 4; corner++) {
            for (int i = 0; i <= arcSteps; i++) {
                double angle = starts[corner] + (Math.PI * 0.5D * i / arcSteps);
                GL11.glVertex2d(centersX[corner] + Math.cos(angle) * rad,
                        centersY[corner] + Math.sin(angle) * rad);
            }
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void dropShadow(double x, double y, double width, double height, double radius, int passes) {
        GlStateManager.disableTexture2D();
        for (int i = 0; i < passes; i++) {
            float expand = (float) (passes - i);
            int alpha = 8 + i * 4;
            int color = (alpha << 24);
            roundedRect(x - expand, y - expand, width + expand * 2.0D, height + expand * 2.0D, radius + expand, color);
        }
        GlStateManager.enableTexture2D();
    }

    public static void dimBackground(int screenWidth, int screenHeight) {
        rect(0.0D, 0.0D, screenWidth, screenHeight, Palette.DIM);
    }

    public static void enableScissor(double x, double y, double width, double height) {
        Minecraft minecraft = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(minecraft);
        enableScissorScaled(resolution.getScaleFactor(), x, y, width, height,
                resolution.getScaledHeight());
    }

    public static void enableScissorScaled(int scaleFactor, double x, double y, double width, double height,
            int scaledHeight) {
        int glX = (int) Math.round(x * scaleFactor);
        int glY = (int) Math.round((scaledHeight - (y + height)) * scaleFactor);
        int glW = (int) Math.round(width * scaleFactor);
        int glH = (int) Math.round(height * scaleFactor);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(glX, glY, Math.max(0, glW), Math.max(0, glH));
    }

    public static void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public static void pushTranslate(double x, double y) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, 0.0F);
    }

    public static void pop() {
        GlStateManager.popMatrix();
    }

    /**
     * Rounded rect with real border: draws outer color, then inner background color on top.
     * Prefer this over {@link #roundedOutline} when the background is known.
     */
    public static void roundedRectWithBorder(double x, double y, double width, double height, double radius,
            float borderWidth, int fillColor, int borderColor) {
        roundedRect(x, y, width, height, radius, borderColor);
        double inset = borderWidth;
        if (width > inset * 2.0D && height > inset * 2.0D) {
            roundedRect(x + inset, y + inset, width - inset * 2.0D, height - inset * 2.0D,
                    Math.max(0.0D, radius - inset), fillColor);
        }
    }

    public static DynamicTexture createSkyboxViewport() {
        return new DynamicTexture(SKYBOX_VIEWPORT, SKYBOX_VIEWPORT);
    }

    public static ResourceLocation registerSkyboxViewport(Minecraft minecraft, DynamicTexture viewport) {
        return minecraft.getTextureManager().getDynamicTextureLocation("background", viewport);
    }

    public static void renderSkybox(Minecraft minecraft, ResourceLocation backgroundTexture, double totalTicks,
            int screenWidth, int screenHeight, float zLevel) {
        renderSkybox(minecraft, minecraft.getFramebuffer(), backgroundTexture, DEFAULT_PANORAMA, totalTicks,
                screenWidth, screenHeight, zLevel);
    }

    public static double panoramaTime() {
        return Minecraft.getSystemTime() / 50.0D;
    }

    public static void renderSkybox(Minecraft minecraft, Framebuffer target, ResourceLocation backgroundTexture,
            ResourceLocation[] faces, double totalTicks, int screenWidth, int screenHeight, float zLevel) {
        target.unbindFramebuffer();
        GlStateManager.viewport(0, 0, SKYBOX_VIEWPORT, SKYBOX_VIEWPORT);
        drawPanoramaCube(minecraft, faces, totalTicks);
        blurSkybox(minecraft, backgroundTexture, screenWidth, screenHeight, zLevel, panoramaBlur2());
        int extraPasses = panoramaBlur3();
        for (int i = 0; i < extraPasses; i++) {
            blurSkybox(minecraft, backgroundTexture, screenWidth, screenHeight, zLevel, SKYBOX_BLUR_PASSES);
            blurSkybox(minecraft, backgroundTexture, screenWidth, screenHeight, zLevel, SKYBOX_BLUR_PASSES);
        }
        target.bindFramebuffer(true);
        GlStateManager.viewport(0, 0, minecraft.displayWidth, minecraft.displayHeight);
        minecraft.getTextureManager().bindTexture(backgroundTexture);
        drawSkyboxToScreen(screenWidth, screenHeight, zLevel);
    }

    public static void drawSkyboxOverlay(int screenWidth, int screenHeight, float zLevel) {
        int top = -2130706433;
        int bottom = 16777215;
        int secondTop = 0;
        int secondBottom = Integer.MIN_VALUE;
        CustomPanoramaProperties custom = CustomPanorama.getCustomPanoramaProperties();
        if (custom != null) {
            top = custom.getOverlay1Top();
            bottom = custom.getOverlay1Bottom();
            secondTop = custom.getOverlay2Top();
            secondBottom = custom.getOverlay2Bottom();
        }
        if (top != 0 || bottom != 0) {
            gradientWithZ(0, 0, screenWidth, screenHeight, zLevel, top, bottom);
        }
        if (secondTop != 0 || secondBottom != 0) {
            gradientWithZ(0, 0, screenWidth, screenHeight, zLevel, secondTop, secondBottom);
        }
    }

    public static void renderSkyboxWithOverlay(Minecraft minecraft, ResourceLocation backgroundTexture,
            double totalTicks, int screenWidth, int screenHeight, float zLevel) {
        renderSkyboxWithOverlay(minecraft, minecraft.getFramebuffer(), backgroundTexture, DEFAULT_PANORAMA,
                totalTicks, screenWidth, screenHeight, zLevel);
    }

    public static void renderSkyboxWithOverlay(Minecraft minecraft, Framebuffer target,
            ResourceLocation backgroundTexture, double totalTicks, int screenWidth, int screenHeight,
            float zLevel) {
        renderSkyboxWithOverlay(minecraft, target, backgroundTexture, DEFAULT_PANORAMA, totalTicks, screenWidth,
                screenHeight, zLevel);
    }

    public static void renderSkyboxWithOverlay(Minecraft minecraft, Framebuffer target,
            ResourceLocation backgroundTexture, ResourceLocation[] faces, double totalTicks, int screenWidth,
            int screenHeight, float zLevel) {
        GlStateManager.disableAlpha();
        renderSkybox(minecraft, target, backgroundTexture, faces, totalTicks, screenWidth, screenHeight, zLevel);
        GlStateManager.enableAlpha();
        drawSkyboxOverlay(screenWidth, screenHeight, zLevel);
    }

    private static void drawPanoramaCube(Minecraft minecraft, ResourceLocation[] faces, double totalTicks) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        Project.gluPerspective(SKYBOX_FOV, 1.0F, 0.05F, 10.0F);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        double pitch = Math.sin(totalTicks / 400.0D) * 25.0D + 20.0D;
        double yaw = -(totalTicks * 0.1D % 360.0D);
        int passes = panoramaBlur1();

        for (int pass = 0; pass < passes; pass++) {
            GlStateManager.pushMatrix();
            float jitterX = ((pass % SKYBOX_JITTER_GRID) / (float) SKYBOX_JITTER_GRID - 0.5F) / 64.0F;
            float jitterY = ((pass / SKYBOX_JITTER_GRID) / (float) SKYBOX_JITTER_GRID - 0.5F) / 64.0F;
            GlStateManager.translate(jitterX, jitterY, 0.0F);
            GlStateManager.rotate((float) pitch, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate((float) yaw, 0.0F, 1.0F, 0.0F);

            for (int face = 0; face < 6; face++) {
                GlStateManager.pushMatrix();
                rotateToFace(face);
                minecraft.getTextureManager().bindTexture(panoramaFace(faces, face));
                renderer.begin(QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                int alpha = 255 / (pass + 1);
                renderer.pos(-1.0D, -1.0D, 1.0D).tex(0.0D, 0.0D).color(255, 255, 255, alpha).endVertex();
                renderer.pos(1.0D, -1.0D, 1.0D).tex(1.0D, 0.0D).color(255, 255, 255, alpha).endVertex();
                renderer.pos(1.0D, 1.0D, 1.0D).tex(1.0D, 1.0D).color(255, 255, 255, alpha).endVertex();
                renderer.pos(-1.0D, 1.0D, 1.0D).tex(0.0D, 1.0D).color(255, 255, 255, alpha).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();
            GlStateManager.colorMask(true, true, true, false);
        }

        renderer.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
    }

    private static void rotateToFace(int face) {
        switch (face) {
            case 1 -> GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            case 2 -> GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            case 3 -> GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            case 4 -> GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            case 5 -> GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            default -> {
            }
        }
    }

    private static ResourceLocation panoramaFace(ResourceLocation[] faces, int face) {
        CustomPanoramaProperties custom = CustomPanorama.getCustomPanoramaProperties();
        if (custom != null) {
            return custom.getPanoramaLocations()[face];
        }
        return faces[face];
    }

    private static void blurSkybox(Minecraft minecraft, ResourceLocation backgroundTexture, int screenWidth,
            int screenHeight, float zLevel, int passes) {
        minecraft.getTextureManager().bindTexture(backgroundTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, SKYBOX_VIEWPORT, SKYBOX_VIEWPORT);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.colorMask(true, true, true, false);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        GlStateManager.disableAlpha();

        for (int pass = 0; pass < passes; pass++) {
            float alpha = 1.0F / (pass + 1);
            float offset = (pass - 1) / 256.0F;
            renderer.pos(screenWidth, screenHeight, zLevel).tex(0.0F + offset, 1.0D)
                    .color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            renderer.pos(screenWidth, 0.0D, zLevel).tex(1.0F + offset, 1.0D)
                    .color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            renderer.pos(0.0D, 0.0D, zLevel).tex(1.0F + offset, 0.0D)
                    .color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            renderer.pos(0.0D, screenHeight, zLevel).tex(0.0F + offset, 0.0D)
                    .color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.colorMask(true, true, true, true);
    }

    private static void drawSkyboxToScreen(int screenWidth, int screenHeight,
            float zLevel) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        float scale = screenWidth > screenHeight ? 120.0F / screenWidth : 120.0F / screenHeight;
        float halfHeight = screenHeight * scale / 256.0F;
        float halfWidth = screenWidth * scale / 256.0F;
        renderer.begin(QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        renderer.pos(0.0D, screenHeight, zLevel).tex(0.5F - halfHeight, 0.5F + halfWidth)
                .color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        renderer.pos(screenWidth, screenHeight, zLevel).tex(0.5F - halfHeight, 0.5F - halfWidth)
                .color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        renderer.pos(screenWidth, 0.0D, zLevel).tex(0.5F + halfHeight, 0.5F - halfWidth)
                .color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        renderer.pos(0.0D, 0.0D, zLevel).tex(0.5F + halfHeight, 0.5F + halfWidth)
                .color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        tessellator.draw();
    }

    private static int panoramaBlur1() {
        CustomPanoramaProperties custom = CustomPanorama.getCustomPanoramaProperties();
        return custom == null ? SKYBOX_PASSES : custom.getBlur1();
    }

    private static int panoramaBlur2() {
        CustomPanoramaProperties custom = CustomPanorama.getCustomPanoramaProperties();
        return custom == null ? SKYBOX_BLUR_PASSES : custom.getBlur2();
    }

    private static int panoramaBlur3() {
        CustomPanoramaProperties custom = CustomPanorama.getCustomPanoramaProperties();
        return custom == null ? SKYBOX_BLUR_PASSES : custom.getBlur3();
    }

    private static void gradientWithZ(int left, int top, int right, int bottom, float zLevel, int topColor,
            int bottomColor) {
        float topA = alpha(topColor);
        float topR = red(topColor);
        float topG = green(topColor);
        float topB = blue(topColor);
        float botA = alpha(bottomColor);
        float botR = red(bottomColor);
        float botG = green(bottomColor);
        float botB = blue(bottomColor);

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(QUADS, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos(right, top, zLevel).color(topR, topG, topB, topA).endVertex();
        renderer.pos(left, top, zLevel).color(topR, topG, topB, topA).endVertex();
        renderer.pos(left, bottom, zLevel).color(botR, botG, botB, botA).endVertex();
        renderer.pos(right, bottom, zLevel).color(botR, botG, botB, botA).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    private static void disc(float centerX, float centerY, float radius, int color, double fromAngle, double toAngle) {
        float alpha = alpha(color);
        float red = red(color);
        float green = green(color);
        float blue = blue(color);
        int segments = Math.max(8, (int) (radius * 1.5D));

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(red, green, blue, alpha);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(FAN, DefaultVertexFormats.POSITION);
        renderer.pos(centerX, centerY, 0.0D).endVertex();
        for (int i = 0; i <= segments; i++) {
            double angle = fromAngle + ((toAngle - fromAngle) * i / segments);
            renderer.pos(centerX + Math.cos(angle) * radius, centerY + Math.sin(angle) * radius, 0.0D).endVertex();
        }
        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    private static void fill(float left, float top, float right, float bottom, int color) {
        if (left > right) {
            float temp = left;
            left = right;
            right = temp;
        }
        if (top > bottom) {
            float temp = top;
            top = bottom;
            bottom = temp;
        }
        float alpha = alpha(color);
        float red = red(color);
        float green = green(color);
        float blue = blue(color);

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(red, green, blue, alpha);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(QUADS, DefaultVertexFormats.POSITION);
        renderer.pos(left, bottom, 0.0D).endVertex();
        renderer.pos(right, bottom, 0.0D).endVertex();
        renderer.pos(right, top, 0.0D).endVertex();
        renderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
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
