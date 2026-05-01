package myau.ui.components;

import myau.module.Module;
import myau.ui.Component;
import myau.ui.StyleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CategoryComponent {
    public ArrayList<Component> modulesInCategory = new ArrayList<>();
    public String categoryName;
    private boolean categoryOpened;
    private int width;
    private int y;
    private int x;
    public boolean dragging;
    public int xx;
    public int yy;
    public boolean pin = false;
    private int scroll = 0;
    private double animScroll = 0;
    private int height = 0;
    private int maxHeight;

    public CategoryComponent(String category, List<Module> modules) {
        this.categoryName = category;
        this.width = StyleHelper.PANEL_WIDTH;
        this.x = 5;
        this.y = 5;
        this.xx = 0;
        this.categoryOpened = false;
        this.dragging = false;
        int tY = StyleHelper.PANEL_HEADER_HEIGHT + 3;
        for (Module mod : modules) {
            ModuleComponent b = new ModuleComponent(mod, this, tY);
            this.modulesInCategory.add(b);
            tY += StyleHelper.MODULE_HEIGHT;
        }
    }

    public ArrayList<Component> getModules() { return this.modulesInCategory; }
    public void setX(int n) { this.x = n; }
    public void setY(int y) { this.y = y; }
    public void mousePressed(boolean d) { this.dragging = d; }
    public boolean isPin() { return this.pin; }
    public void setPin(boolean on) { this.pin = on; }
    public boolean isOpened() { return this.categoryOpened; }
    public void setOpened(boolean on) { this.categoryOpened = on; }

    public void render(FontRenderer renderer) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        this.maxHeight = sr.getScaledHeight() - 40; // dynamic max height
        this.width = StyleHelper.PANEL_WIDTH;
        update();
        height = 0;
        for (Component m : this.modulesInCategory) height += m.getHeight();

        int maxScroll = Math.max(0, height - maxHeight);
        if (scroll > maxScroll) scroll = maxScroll;
        if (animScroll > maxScroll) animScroll = maxScroll;
        animScroll += (scroll - animScroll) * 0.2;

        // Panel background when open
        if (!this.modulesInCategory.isEmpty() && this.categoryOpened) {
            int displayHeight = Math.min(height, maxHeight);
            Gui.drawRect(this.x, this.y + StyleHelper.PANEL_HEADER_HEIGHT + 2,
                    this.x + this.width, this.y + StyleHelper.PANEL_HEADER_HEIGHT + 2 + displayHeight,
                    StyleHelper.PANEL_BG);
            // Bottom border line
            Gui.drawRect(this.x, this.y + StyleHelper.PANEL_HEADER_HEIGHT + 2 + displayHeight,
                    this.x + this.width, this.y + StyleHelper.PANEL_HEADER_HEIGHT + 3 + displayHeight,
                    StyleHelper.PANEL_BORDER);
        }

        // Header bar
        Gui.drawRect(this.x, this.y, this.x + this.width, this.y + StyleHelper.PANEL_HEADER_HEIGHT, StyleHelper.HEADER_BG);
        // Bottom border of header
        Gui.drawRect(this.x, this.y + StyleHelper.PANEL_HEADER_HEIGHT, this.x + this.width,
                this.y + StyleHelper.PANEL_HEADER_HEIGHT + 1, StyleHelper.PANEL_BORDER);

        // Accent bar (left edge)
        int accent = StyleHelper.getCategoryAccent(this.categoryName);
        Gui.drawRect(this.x, this.y, this.x + StyleHelper.ACCENT_BAR_WIDTH, this.y + StyleHelper.PANEL_HEADER_HEIGHT, accent);

        // Title text (scaled 0.5x)
        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        renderer.drawString(this.categoryName,
                (float) ((this.x + StyleHelper.ACCENT_BAR_WIDTH + 4) * 2),
                (float) ((this.y + 4) * 2),
                StyleHelper.TEXT_PRIMARY, false);
        // ± toggle indicator drawn as text
        renderer.drawString(this.categoryOpened ? "−" : "+",
                (float) ((this.x + this.width - 18) * 2),
                (float) ((this.y + 2) * 2),
                StyleHelper.TEXT_SECONDARY, false);
        GL11.glPopMatrix();

        // Pin indicator (top-right 8x8)
        int pinX = this.x + this.width - 11;
        int pinY = this.y + 3;
        Gui.drawRect(pinX, pinY, pinX + 8, pinY + 8, this.pin ? (accent & 0x00FFFFFF) | 0xC0000000 : 0x30404060);
        if (this.pin) {
            Gui.drawRect(pinX + 2, pinY + 2, pinX + 6, pinY + 6, accent);
        }

        // Module list with scissor
        if (this.categoryOpened && !this.modulesInCategory.isEmpty()) {
            int renderHeight = 0;
            double scale = sr.getScaleFactor();
            int contentTop = this.y + StyleHelper.PANEL_HEADER_HEIGHT + 3;
            int contentBottom = contentTop + maxHeight;
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor((int) (this.x * scale), (int) ((sr.getScaledHeight() - contentBottom) * scale),
                    (int) (this.width * scale), (int) (maxHeight * scale));

            int i = 0;
            for (Component c2 : this.modulesInCategory) {
                int compHeight = c2.getHeight();
                if (renderHeight + compHeight > animScroll && renderHeight < animScroll + maxHeight) {
                    int drawY = (int) (renderHeight - animScroll);
                    c2.setComponentStartAt(StyleHelper.PANEL_HEADER_HEIGHT + 3 + drawY);
                    c2.draw(new AtomicInteger(i));
                }
                renderHeight += compHeight;
                i++;
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            // Scrollbar
            if (height > maxHeight) {
                float trackY = contentTop;
                float trackH = maxHeight;
                Gui.drawRect(this.x + this.width - StyleHelper.SCROLLBAR_WIDTH, (int) trackY,
                        this.x + this.width, (int) (trackY + trackH), StyleHelper.SCROLLBAR_BG);
                float thumbH = Math.max(16, trackH * trackH / height);
                float thumbY = trackY + (float) ((animScroll / height) * trackH);
                Gui.drawRect(this.x + this.width - StyleHelper.SCROLLBAR_WIDTH, (int) thumbY,
                        this.x + this.width, (int) (thumbY + thumbH), StyleHelper.SCROLLBAR_FG);
            }
        }
    }

    public void update() {
        int offset = StyleHelper.PANEL_HEADER_HEIGHT + 3;
        for (Component component : this.modulesInCategory) {
            component.setComponentStartAt(offset);
            offset += component.getHeight();
        }
    }

    public int getX() { return this.x; }
    public int getY() { return this.y; }
    public int getWidth() { return this.width; }

    public void handleDrag(int x, int y) {
        if (this.dragging) {
            int newX = x - this.xx;
            int newY = y - this.yy;
            // Clamp to keep at least the header area within the screen
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            int sw = sr.getScaledWidth();
            int sh = sr.getScaledHeight();
            newX = StyleHelper.clamp(newX, -(this.width - 24), sw - 24);
            newY = StyleHelper.clamp(newY, -4, sh - StyleHelper.PANEL_HEADER_HEIGHT - 4);
            this.setX(newX);
            this.setY(newY);
        }
    }

    /** Pin button hit area (top-right) */
    public boolean isHovered(int x, int y) {
        int pinX = this.x + this.width - 11;
        int pinY = this.y + 3;
        return x >= pinX && x <= pinX + 8 && y >= pinY && y <= pinY + 8;
    }

    /** ± toggle hit area (right side of header) */
    public boolean mousePressed(int x, int y) {
        return x >= this.x + this.width - 24 && x <= this.x + this.width - 14
                && y >= this.y + 2 && y <= this.y + StyleHelper.PANEL_HEADER_HEIGHT - 1;
    }

    /** Header drag area (everything else in header) */
    public boolean insideArea(int x, int y) {
        return x >= this.x && x <= this.x + this.width
                && y >= this.y && y <= this.y + StyleHelper.PANEL_HEADER_HEIGHT
                && !isHovered(x, y) && !mousePressed(x, y);
    }

    public String getName() { return categoryName; }
    public void setLocation(int parseInt, int parseInt1) { this.x = parseInt; this.y = parseInt1; }

    public void onScroll(int mouseX, int mouseY, int scrollAmount) {
        if (!categoryOpened || height <= maxHeight) return;
        int contentTop = this.y + StyleHelper.PANEL_HEADER_HEIGHT + 3;
        int contentBottom = contentTop + maxHeight;
        if (mouseX >= this.x && mouseX <= this.x + width && mouseY >= contentTop && mouseY <= contentBottom) {
            scroll -= scrollAmount * 12;
            scroll = Math.max(0, Math.min(scroll, height - maxHeight));
        }
    }
}
