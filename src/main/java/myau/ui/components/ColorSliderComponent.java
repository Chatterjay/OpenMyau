package myau.ui.components;

import myau.property.properties.ColorProperty;
import myau.ui.Component;
import myau.ui.StyleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ColorSliderComponent implements Component {

    private static final int SLIDER_GAP = 2;
    private static final int SLIDER_H = 3;

    private final ModuleComponent parentModule;
    private final ColorProperty property;
    private int offsetY;
    private boolean draggingHue, draggingSat, draggingBri, draggingAlp;
    private float hue, saturation, brightness, alpha;

    public ColorSliderComponent(ColorProperty property, ModuleComponent parentModule, int offsetY) {
        this.parentModule = parentModule;
        this.offsetY = offsetY;
        this.property = property;
        Color c = new Color(property.getValue(), true);
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = ((float) c.getAlpha()) / 255;
    }

    @Override
    public void draw(java.util.concurrent.atomic.AtomicInteger offset) {
        int x = parentModule.category.getX() + 4;
        int y = parentModule.category.getY() + offsetY;
        int width = parentModule.category.getWidth() - 8;

        // Label (scaled 0.5x)
        GL11.glPushMatrix();
        GL11.glScaled(0.5, 0.5, 0.5);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                property.getName().replace("-", " "),
                (float) (x * 2), (float) ((this.parentModule.category.getY() + this.offsetY + 3) * 2.0F),
                StyleHelper.TEXT_PRIMARY);
        GL11.glPopMatrix();

        if (!draggingHue && !draggingSat && !draggingBri && !draggingAlp) {
            Color c = new Color(property.getValue(), true);
            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            hue = hsb[0];
            saturation = hsb[1];
            brightness = hsb[2];
            alpha = ((float) c.getAlpha()) / 255;
        }

        // Color preview 10x10 with border
        int previewSize = 10;
        int previewX = x + width - previewSize;
        int previewY = y + 2;
        Color pc = Color.getHSBColor(hue, saturation, brightness);
        int previewColor = new Color(pc.getRed(), pc.getGreen(), pc.getBlue(), (int) (alpha * 255)).getRGB();
        Gui.drawRect(previewX - 1, previewY - 1, previewX + previewSize + 1, previewY + previewSize + 1, StyleHelper.PANEL_BORDER);
        Gui.drawRect(previewX, previewY, previewX + previewSize, previewY + previewSize, previewColor);

        // Sliders (3px each, 2px gap)
        int baseY = y + 14;
        int step = SLIDER_H + SLIDER_GAP;

        drawHueBar(x, baseY, width);
        drawPointer(x, baseY, width, hue);

        drawGradientRect(x, baseY + step, x + width, baseY + step + SLIDER_H,
                Color.WHITE.getRGB(), Color.HSBtoRGB(hue, 1f, 1f));
        drawPointer(x, baseY + step, width, saturation);

        drawGradientRect(x, baseY + step * 2, x + width, baseY + step * 2 + SLIDER_H,
                Color.BLACK.getRGB(), Color.HSBtoRGB(hue, saturation, 1f));
        drawPointer(x, baseY + step * 2, width, brightness);

        drawGradientRect(x, baseY + step * 3, x + width, baseY + step * 3 + SLIDER_H,
                Color.HSBtoRGB(hue, saturation, 1f) & 0xFFFFFF, Color.HSBtoRGB(hue, saturation, 1f));
        drawPointer(x, baseY + step * 3, width, alpha);

        // Hex value (scaled 0.5x)
        GL11.glPushMatrix();
        GL11.glScaled(0.5, 0.5, 0.5);
        String hex = String.format("#%06X", (previewColor & 0x00FFFFFF));
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                hex, (float) (x * 2), (float) ((baseY + step * 3 + SLIDER_H + 2) * 2),
                StyleHelper.TEXT_SECONDARY);
        GL11.glPopMatrix();
    }

    private void drawHueBar(int x, int y, int width) {
        for (int i = 0; i < width; i++) {
            float hue = (float) i / (float) width;
            Gui.drawRect(x + i, y, x + i + 1, y + SLIDER_H, Color.HSBtoRGB(hue, 1f, 1f));
        }
    }

    private void drawPointer(int x, int y, int width, float value) {
        int posX = x + (int) (width * value);
        // Dark outline
        Gui.drawRect(posX - 2, y - 1, posX + 1, y + SLIDER_H + 1, 0xFF000000);
        // White inner
        Gui.drawRect(posX - 1, y, posX, y + SLIDER_H, 0xFFFFFFFF);
    }

    @Override
    public void update(int mouseX, int mouseY) {
        int baseX = parentModule.category.getX() + 4;
        int width = parentModule.category.getWidth() - 8;
        boolean changed = false;

        if (draggingHue) { hue = getSliderValue(mouseX, baseX, width); changed = true; }
        if (draggingSat) { saturation = getSliderValue(mouseX, baseX, width); changed = true; }
        if (draggingBri) { brightness = getSliderValue(mouseX, baseX, width); changed = true; }
        if (draggingAlp) { alpha = getSliderValue(mouseX, baseX, width); changed = true; }

        if (changed) {
            Color signed = Color.getHSBColor(hue, saturation, brightness);
            property.setValue(new Color(signed.getRed(), signed.getGreen(), signed.getBlue(), (int) (alpha * 255)).getRGB());
        }
    }

    private float getSliderValue(int mouseX, int startX, int width) {
        double d = Math.min(width, Math.max(0, mouseX - startX));
        return (float) roundToPrecision(d / width, 3);
    }

    private static double roundToPrecision(double v, int precision) {
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(precision, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void mouseDown(int mouseX, int mouseY, int button) {
        if (button != 0 || !parentModule.panelExpand) return;
        int baseY = parentModule.category.getY() + offsetY + 14;
        int step = SLIDER_H + SLIDER_GAP;
        if (isHovered(mouseX, mouseY, baseY)) draggingHue = true;
        else if (isHovered(mouseX, mouseY, baseY + step)) draggingSat = true;
        else if (isHovered(mouseX, mouseY, baseY + step * 2)) draggingBri = true;
        else if (isHovered(mouseX, mouseY, baseY + step * 3)) draggingAlp = true;
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        draggingHue = draggingSat = draggingBri = draggingAlp = false;
    }

    private boolean isHovered(int mx, int my, int sliderY) {
        int startX = parentModule.category.getX() + 4;
        int endX = startX + parentModule.category.getWidth() - 8;
        return mx >= startX && mx <= endX && my >= sliderY && my <= sliderY + SLIDER_H;
    }

    @Override
    public boolean isVisible() { return property.isVisible(); }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {}

    @Override
    public void setComponentStartAt(int newOffsetY) { offsetY = newOffsetY; }

    @Override
    public int getHeight() { return 40; }

    private void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        float sa = (float) (startColor >> 24 & 255) / 255.0F;
        float sr = (float) (startColor >> 16 & 255) / 255.0F;
        float sg = (float) (startColor >> 8 & 255) / 255.0F;
        float sb = (float) (startColor & 255) / 255.0F;
        float ea = (float) (endColor >> 24 & 255) / 255.0F;
        float er = (float) (endColor >> 16 & 255) / 255.0F;
        float eg = (float) (endColor >> 8 & 255) / 255.0F;
        float eb = (float) (endColor & 255) / 255.0F;
        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.WorldRenderer world = tessellator.getWorldRenderer();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        world.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR);
        world.pos(right, top, 0).color(er, eg, eb, ea).endVertex();
        world.pos(left, top, 0).color(sr, sg, sb, sa).endVertex();
        world.pos(left, bottom, 0).color(sr, sg, sb, sa).endVertex();
        world.pos(right, bottom, 0).color(er, eg, eb, ea).endVertex();
        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
