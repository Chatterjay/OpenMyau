package myau.ui;

import myau.Myau;
import myau.module.modules.HUD;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class StyleHelper {

    // ── Category accent colors ──
    public static final int ACCENT_COMBAT    = 0xFFE53935;
    public static final int ACCENT_MOVEMENT  = 0xFF1E88E5;
    public static final int ACCENT_RENDER    = 0xFF8E24AA;
    public static final int ACCENT_PLAYER    = 0xFF43A047;
    public static final int ACCENT_MISC      = 0xFFFB8C00;

    // ── Panel colors (fully opaque for readability) ──
    public static final int PANEL_BORDER     = 0xFF1A1A2E;
    public static final int PANEL_BG         = 0xFF1E1E32;
    public static final int HEADER_BG        = 0xFF2A2A48;
    public static final int HEADER_ACCENT_BG = 0xFF32325A;
    public static final int MODULE_BG        = 0xFF242442;
    public static final int MODULE_HOVER     = 0xFF34345E;
    public static final int SETTINGS_BG      = 0xFF1E1E38;
    public static final int SETTINGS_HOVER   = 0xFF2A2A50;
    public static final int SETTINGS_LINE    = 0xFF2E2E52;

    // ── Scrollbar ──
    public static final int SCROLLBAR_BG     = 0xFF1A1A30;
    public static final int SCROLLBAR_FG     = 0xFF4A4A78;

    // ── Text colors ──
    public static final int TEXT_PRIMARY     = 0xFFF0F0F0;
    public static final int TEXT_SECONDARY   = 0xFFA0A0B0;
    public static final int TEXT_DISABLED    = 0xFF686878;
    public static final int TEXT_ENABLED     = 0xFFFFFFFF;
    public static final int TEXT_ACCENT      = 0xFF80D0FF;

    // ── Toggle ──
    public static final int TOGGLE_ON        = 0xFF43A047;
    public static final int TOGGLE_ON_INNER  = 0xFF81C784;
    public static final int TOGGLE_OFF       = 0xFF4A4A5A;
    public static final int TOGGLE_OFF_INNER = 0xFF6A6A7A;

    // ── Slider ──
    public static final int SLIDER_TRACK     = 0xFF2A2A48;
    public static final int SLIDER_FILL      = 0xFF4DCCFF;

    // ── Dimensions ──
    public static final int ACCENT_BAR_WIDTH  = 3;
    public static final int PANEL_WIDTH       = 110;
    public static final int PANEL_HEADER_HEIGHT = 14;
    public static final int SCROLLBAR_WIDTH   = 3;
    public static final int MODULE_HEIGHT     = 16;
    public static final int TOGGLE_WIDTH      = 22;
    public static final int TOGGLE_HEIGHT     = 8;
    public static final int SLIDER_HEIGHT     = 4;
    public static final int SLIDER_THUMB_SIZE = 6;

    public static int getCategoryAccent(String name) {
        switch (name.toLowerCase()) {
            case "combat":    return ACCENT_COMBAT;
            case "movement":  return ACCENT_MOVEMENT;
            case "render":    return ACCENT_RENDER;
            case "player":    return ACCENT_PLAYER;
            case "misc":      return ACCENT_MISC;
            default:          return TEXT_ACCENT;
        }
    }

    public static int getCategoryAccentDimmed(String name) {
        int c = getCategoryAccent(name);
        return (c & 0x00FFFFFF) | 0x60000000;
    }

    public static int darken(int color, float factor) {
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    /** Fill a rectangle with a gradient between two colors (top → bottom). */
    public static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        float sa = (float) (startColor >> 24 & 255) / 255.0F;
        float sr = (float) (startColor >> 16 & 255) / 255.0F;
        float sg = (float) (startColor >> 8 & 255) / 255.0F;
        float sb = (float) (startColor & 255) / 255.0F;
        float ea = (float) (endColor >> 24 & 255) / 255.0F;
        float er = (float) (endColor >> 16 & 255) / 255.0F;
        float eg = (float) (endColor >> 8 & 255) / 255.0F;
        float eb = (float) (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer world = tessellator.getWorldRenderer();
        world.begin(7, DefaultVertexFormats.POSITION_COLOR);
        world.pos(right, top, 0).color(sr, sg, sb, sa).endVertex();
        world.pos(left, top, 0).color(sr, sg, sb, sa).endVertex();
        world.pos(left, bottom, 0).color(er, eg, eb, ea).endVertex();
        world.pos(right, bottom, 0).color(er, eg, eb, ea).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    /** Draw a 1px horizontal line. */
    public static void drawHorizontalLine(int left, int right, int y, int color) {
        if (color == 0) return;
        Gui.drawRect(left, y, right, y + 1, color);
    }

    /** Draw a 1px vertical line. */
    public static void drawVerticalLine(int x, int top, int bottom, int color) {
        if (color == 0) return;
        Gui.drawRect(x, top, x + 1, bottom, color);
    }

    /** Get module text color: accent when enabled, gray when disabled. */
    public static int getModuleColor(boolean isEnabled, int offset) {
        if (!isEnabled) return TEXT_DISABLED;
        return TEXT_ENABLED;
    }

    /** Get module accent indicator color when enabled. */
    public static int getModuleAccent(boolean isEnabled, int offset) {
        if (!isEnabled) return TEXT_DISABLED;
        return ((HUD) Myau.moduleManager.modules.get(HUD.class)).getColor(System.currentTimeMillis(), offset).getRGB();
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Draw a hollow rectangle border. */
    public static void drawBorder(int x, int y, int w, int h, int color) {
        Gui.drawRect(x, y, x + w, y + 1, color);           // top
        Gui.drawRect(x, y + h - 1, x + w, y + h, color);   // bottom
        Gui.drawRect(x, y, x + 1, y + h, color);           // left
        Gui.drawRect(x + w - 1, y, x + w, y + h, color);   // right
    }
}
