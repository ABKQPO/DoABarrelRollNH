package com.hfstudio.elytrahud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

/**
 * Renders gauge-style HUD widgets using the hud_widgets.png sprite sheet.
 * Ports the ElytraHUD mod's gauge rendering to 1.7.10 OpenGL.
 */
public class ElytraHudRenderer extends Gui {

    private static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation(
        "doabarrelroll",
        "textures/hud_widgets.png");

    private final Minecraft client;
    private int scaledWidth;
    private int scaledHeight;
    private int defaultY = 10;
    private double displayedSpeed = 0.0;
    private double displayedRate = 0.0;
    private double prevDisplayedRate = 0.0;
    private double displayedDur = 1.0;
    public double displayedHeight = 0.0;
    private double displayedVertical = 0.0;
    private double displayedYaw = 180.0;

    public ElytraHudRenderer(Minecraft client) {
        this.client = client;
    }

    public void render(ElytraHudData hudData, float tickDelta) {
        int rateX = 10;
        int durX = 10;
        int vertX = 10;

        // Lerp speed for smooth display
        displayedSpeed = displayedSpeed + (hudData.speed - displayedSpeed) * tickDelta;
        displayedRate = hudData.fireworkRate;
        displayedDur = hudData.durability;
        displayedHeight = hudData.height;
        displayedVertical = hudData.verticalSpeed;
        displayedYaw = hudData.yaw * -1.0 + 180.0;

        int intAirspeed = (int) Math.round(displayedSpeed);
        int intRate = (int) Math.round(displayedRate);
        int intDur = hudData.currentDurability;
        int intHeight = (int) Math.round(displayedHeight);
        int intVertical = (int) Math.round(displayedVertical);

        defaultY = ElytraHudConfig.renderValues ? 15 : 10;

        ScaledResolution sr = new ScaledResolution(client, client.displayWidth, client.displayHeight);
        scaledWidth = sr.getScaledWidth();
        scaledHeight = sr.getScaledHeight();

        // Apply HUD scale
        float scale = ElytraHudConfig.hudScale;
        if (scale != 1.0f) {
            scaledWidth = (int) (scaledWidth / scale);
            scaledHeight = (int) (scaledHeight / scale);
            GL11.glPushMatrix();
            GL11.glScalef(scale, scale, 1.0f);
        }

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (ElytraHudConfig.renderAirspeed) {
            rateX += 102;
            durX += 102;
            int speedometerSize = 100;
            int speedometerX = 10;
            int speedometerY = speedometerSize + defaultY;
            int[] backGrAirspeed = { speedometerX, scaledHeight - speedometerY, 0, 0, 100, 100 };
            int[] pointerAirspeed = { speedometerX + 49, scaledHeight - speedometerY + 8, 215, 105, 2, 42 };
            int[] foreGrAirspeed = { speedometerX + 45, scaledHeight - speedometerY + 45, 215, 73, 10, 10 };
            int[] titleAirspeed = { speedometerX + 33, scaledHeight - speedometerY - 10, 215, 0, 34, 9 };
            int[] valueAirspeed = { speedometerX + 40, scaledHeight - speedometerY + 101, 215, 165, 21, 11 };

            double clampedSpeed = displayedSpeed;
            if (clampedSpeed > 80.0) clampedSpeed = 80.0;

            renderMeter(
                speedometerX + 50,
                scaledHeight - speedometerY + 50,
                (float) Math.toRadians(clampedSpeed * 4.5),
                backGrAirspeed,
                pointerAirspeed,
                foreGrAirspeed,
                titleAirspeed,
                valueAirspeed,
                intAirspeed);
        }

        if (ElytraHudConfig.renderFireworksRate) {
            durX += 52;
            int rateSize = 50;
            int rateY = rateSize + defaultY;
            int[] backGrRate = { rateX, scaledHeight - rateY, 100, 0, 50, 50 };
            int[] pointerRate = { rateX + 24, scaledHeight - rateY + 7, 215, 147, 2, 18 };
            int[] foreGrRate = { rateX + 22, scaledHeight - rateY + 22, 215, 93, 6, 6 };
            int[] titleRate = { rateX + 13, scaledHeight - rateY - 10, 215, 9, 24, 9 };
            int[] valueRate = { rateX + 15, scaledHeight - rateY + 51, 215, 165, 21, 11 };

            double clampedRate = displayedRate;
            if (clampedRate > 300.0) clampedRate = 300.0;

            // Smooth rate decrease
            if (prevDisplayedRate - clampedRate > 200.0) {
                clampedRate = prevDisplayedRate - (prevDisplayedRate - clampedRate) * 0.1;
            } else if (prevDisplayedRate - clampedRate > 150.0) {
                clampedRate = prevDisplayedRate - (prevDisplayedRate - clampedRate) * 0.2;
            } else if (prevDisplayedRate - clampedRate > 100.0) {
                clampedRate = prevDisplayedRate - (prevDisplayedRate - clampedRate) * 0.3;
            } else if (prevDisplayedRate - clampedRate > 50.0) {
                clampedRate = prevDisplayedRate - (prevDisplayedRate - clampedRate) * 0.5;
            }
            prevDisplayedRate = clampedRate;

            renderMeter(
                rateX + 25,
                scaledHeight - rateY + 25,
                (float) (Math.toRadians(clampedRate * 0.8333333134651184 + 235.0) % (2 * Math.PI)),
                backGrRate,
                pointerRate,
                foreGrRate,
                titleRate,
                valueRate,
                intRate);
        }

        if (ElytraHudConfig.renderDurability) {
            int durSize = 50;
            int durY = durSize + defaultY;
            int topPoint = scaledHeight - durY + 2;
            int bottomPoint = topPoint + 44;
            int yCoordinate = (int) ((double) topPoint + (1.0 - displayedDur) * (double) (bottomPoint - topPoint));
            int[] backGrDur = { durX, scaledHeight - durY, 150, 0, 15, 50 };
            int[] pointerDur = { durX + 8, yCoordinate, 215, 56, 4, 3 };
            int[] titleDur = { durX, scaledHeight - durY - 10, 215, 18, 15, 9 };
            int[] valueDur = { durX - 3, scaledHeight - durY + 51, 215, 165, 21, 11 };

            renderBar(backGrDur, pointerDur, titleDur, valueDur, intDur);
        }

        if (ElytraHudConfig.renderAltitude) {
            vertX += 102;
            int altitudeSize = 100;
            int altitudeX = 10 + altitudeSize;
            int altitudeY = altitudeSize + defaultY;
            int[] backGrAltitude = { scaledWidth - altitudeX, scaledHeight - altitudeY, 0, 100, 100, 100 };
            int[] pointer1Altitude = { scaledWidth - altitudeX + 49, scaledHeight - altitudeY + 32, 215, 147, 2, 18 };
            int[] pointer2Altitude = { scaledWidth - altitudeX + 49, scaledHeight - altitudeY + 8, 215, 105, 2, 42 };
            int[] foreGrAltitude = { scaledWidth - altitudeX + 45, scaledHeight - altitudeY + 45, 215, 83, 10, 10 };
            int[] titleAltitude = { scaledWidth - altitudeX + 34, scaledHeight - altitudeY - 10, 215, 36, 32, 9 };
            int[] valueAltitude = { scaledWidth - altitudeX + 40, scaledHeight - altitudeY + 101, 215, 165, 21, 11 };

            renderDoubleMeter(
                scaledWidth - altitudeX + 50,
                scaledHeight - altitudeY + 50,
                (float) Math.toRadians(displayedHeight * 0.36),
                (float) Math.toRadians(displayedHeight * 3.6),
                backGrAltitude,
                pointer1Altitude,
                pointer2Altitude,
                foreGrAltitude,
                titleAltitude,
                valueAltitude,
                intHeight);
        }

        if (ElytraHudConfig.renderVertical) {
            int vertSize = 50;
            int vertY = vertSize + defaultY;
            vertX += vertSize;
            int[] backGrVert = { scaledWidth - vertX, scaledHeight - vertY, 165, 0, 50, 50 };
            int[] pointerVert = { scaledWidth - vertX + 24, scaledHeight - vertY + 7, 215, 147, 2, 18 };
            int[] foreGrVert = { scaledWidth - vertX + 22, scaledHeight - vertY + 22, 215, 99, 6, 6 };
            int[] titleVert = { scaledWidth - vertX + 11, scaledHeight - vertY - 10, 215, 27, 28, 9 };
            int[] valueVert = { scaledWidth - vertX + 15, scaledHeight - vertY + 51, 215, 165, 21, 11 };

            double clampedVertical = displayedVertical;
            if (clampedVertical > 5.0) clampedVertical = 5.0;
            else if (clampedVertical < -5.0) clampedVertical = -5.0;

            renderMeter(
                scaledWidth - vertX + 25,
                scaledHeight - vertY + 25,
                (float) (Math.toRadians((clampedVertical + 5.0) * 25.0 + 145.0) % (2 * Math.PI)),
                backGrVert,
                pointerVert,
                foreGrVert,
                titleVert,
                valueVert,
                intVertical);
        }

        if (ElytraHudConfig.renderCompass) {
            int compassScreen = scaledWidth - 120;
            double compassOffset = (double) ElytraHudConfig.compassDefaultX / 100.0 * (double) compassScreen;
            int compassX = (int) Math.round(compassOffset) + 10;
            int compassY = 15;
            int[] backGrCompass = { compassX, compassY, 100, 100, 100, 100 };
            int[] pointerCompass = { compassX + 45, compassY + 3, 215, 59, 6, 5 };
            int[] foreGrCompass = { compassX + 45, compassY + 44, 215, 64, 10, 9 };
            int[] titleCompass = { compassX + 35, compassY - 8, 215, 45, 26, 11 };

            renderCompass(
                compassX + 50,
                compassY + 50,
                (float) Math.toRadians(displayedYaw),
                backGrCompass,
                pointerCompass,
                foreGrCompass,
                titleCompass);

            int intYaw = (((int) Math.round(hudData.yaw) % 360) + 360 + 180) % 360;
            client.fontRenderer
                .drawString(String.format("%3d\u00b0", intYaw), compassX + 35 + 2, compassY - 8 + 2, 0xFFFFFF);
        }

        GL11.glDisable(GL11.GL_BLEND);

        // Pop scale matrix
        if (ElytraHudConfig.hudScale != 1.0f) {
            GL11.glPopMatrix();
        }
    }

    private void renderDoubleMeter(int centerX, int centerY, float angle1, float angle2, int[] backGr, int[] pointer1,
        int[] pointer2, int[] foreGr, int[] title, int[] value, int valueInt) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        bindAndDraw(backGr);

        // Pointer 1 (slow hand)
        GL11.glPushMatrix();
        GL11.glTranslatef(centerX, centerY, 0.0f);
        GL11.glRotatef((float) Math.toDegrees(angle1), 0.0f, 0.0f, 1.0f);
        GL11.glTranslatef(-centerX, -centerY, 0.0f);
        bindAndDraw(pointer1);
        GL11.glPopMatrix();

        // Pointer 2 (fast hand)
        GL11.glPushMatrix();
        GL11.glTranslatef(centerX, centerY, 0.0f);
        GL11.glRotatef((float) Math.toDegrees(angle2), 0.0f, 0.0f, 1.0f);
        GL11.glTranslatef(-centerX, -centerY, 0.0f);
        bindAndDraw(pointer2);
        GL11.glPopMatrix();

        bindAndDraw(foreGr);

        if (ElytraHudConfig.renderTitles) {
            bindAndDraw(title);
        }

        if (ElytraHudConfig.renderValues) {
            bindAndDraw(value);
            client.fontRenderer.drawString(String.format("%3d", valueInt), value[0] + 2, value[1] + 2, 0xFFFFFF);
        }
    }

    private void renderMeter(int centerX, int centerY, float angle, int[] backGr, int[] pointer, int[] foreGr,
        int[] title, int[] value, int valueInt) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        bindAndDraw(backGr);

        GL11.glPushMatrix();
        GL11.glTranslatef(centerX, centerY, 0.0f);
        GL11.glRotatef((float) Math.toDegrees(angle), 0.0f, 0.0f, 1.0f);
        GL11.glTranslatef(-centerX, -centerY, 0.0f);
        bindAndDraw(pointer);
        GL11.glPopMatrix();

        bindAndDraw(foreGr);

        if (ElytraHudConfig.renderTitles) {
            bindAndDraw(title);
        }

        if (ElytraHudConfig.renderValues) {
            bindAndDraw(value);
            client.fontRenderer.drawString(String.format("%3d", valueInt), value[0] + 2, value[1] + 2, 0xFFFFFF);
        }
    }

    private void renderCompass(int centerX, int centerY, float angle, int[] backGr, int[] pointer, int[] foreGr,
        int[] title) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Compass background rotates
        GL11.glPushMatrix();
        GL11.glTranslatef(centerX, centerY, 0.0f);
        GL11.glRotatef((float) Math.toDegrees(angle), 0.0f, 0.0f, 1.0f);
        GL11.glTranslatef(-centerX, -centerY, 0.0f);
        bindAndDraw(backGr);
        GL11.glPopMatrix();

        // Fixed elements
        bindAndDraw(pointer);
        bindAndDraw(foreGr);
        bindAndDraw(title);
    }

    private void renderBar(int[] backGr, int[] pointer, int[] title, int[] value, int valueInt) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        bindAndDraw(backGr);
        bindAndDraw(pointer);

        if (ElytraHudConfig.renderTitles) {
            bindAndDraw(title);
        }

        if (ElytraHudConfig.renderValues) {
            bindAndDraw(value);
            client.fontRenderer.drawString(String.format("%3d", valueInt), value[0] + 2, value[1] + 2, 0xFFFFFF);
        }
    }

    /**
     * Binds the widget texture and draws a sprite region.
     * params: [screenX, screenY, textureU, textureV, width, height]
     */
    private void bindAndDraw(int[] params) {
        client.getTextureManager()
            .bindTexture(WIDGETS_TEXTURE);
        // drawTexturedModalRect assumes 256x256 texture
        drawTexturedModalRect(params[0], params[1], params[2], params[3], params[4], params[5]);
    }
}
