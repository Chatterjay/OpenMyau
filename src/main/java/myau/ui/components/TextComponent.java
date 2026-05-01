package myau.ui.components;

import myau.enums.ChatColors;
import myau.property.properties.TextProperty;
import myau.ui.ClickGui;
import myau.ui.Component;
import myau.ui.StyleHelper;
import myau.ui.callback.GuiInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.atomic.AtomicInteger;

public class TextComponent implements Component {
    private final TextProperty property;
    private final ModuleComponent module;
    private int offsetY;
    private int x;
    private int y;
    private boolean hovered;

    public TextComponent(TextProperty property, ModuleComponent parentModule, int offsetY) {
        this.property = property;
        this.module = parentModule;
        this.x = parentModule.category.getX() + parentModule.category.getWidth();
        this.y = parentModule.category.getY() + parentModule.offsetY;
        this.offsetY = offsetY;
    }

    public void draw(AtomicInteger offset) {
        int rowX = this.module.category.getX();
        int rowY = this.module.category.getY() + this.offsetY;

        // Row background (solid opaque)
        Gui.drawRect(rowX, rowY, rowX + this.module.category.getWidth(), rowY + 14,
                this.hovered ? StyleHelper.SETTINGS_HOVER : StyleHelper.SETTINGS_BG);

        // Text (scaled 0.5x)
        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        Minecraft.getMinecraft().fontRendererObj.drawString(
                this.property.getName().replace("-", " ") + ": " + ChatColors.formatColor(this.property.formatValue()),
                (float) ((rowX + 4) * 2), (float) ((rowY + 4) * 2),
                StyleHelper.TEXT_PRIMARY, false);
        GL11.glPopMatrix();
    }

    public void setComponentStartAt(int newOffsetY) { this.offsetY = newOffsetY; }

    @Override
    public int getHeight() { return 14; }

    public void update(int mousePosX, int mousePosY) {
        this.y = this.module.category.getY() + this.offsetY;
        this.x = this.module.category.getX();
        this.hovered = isHovered(mousePosX, mousePosY);
    }

    public void mouseDown(int x, int y, int button) {
        if (this.isHovered(x, y) && button == 0 && this.module.panelExpand) {
            GuiInput.prompt(property.getName().replace("-", " "), property.getValue(), property::setValue, ClickGui.getInstance());
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {}
    @Override
    public void keyTyped(char chatTyped, int keyCode) {}

    public boolean isHovered(int x, int y) {
        return x > this.x && x < this.x + this.module.category.getWidth() && y > this.y && y < this.y + 14;
    }

    @Override
    public boolean isVisible() { return property.isVisible(); }
}
