package myau.ui.components;

import myau.property.properties.BooleanProperty;
import myau.ui.Component;
import myau.ui.StyleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.atomic.AtomicInteger;

public class CheckBoxComponent implements Component {
    private final BooleanProperty property;
    private final ModuleComponent module;
    private int offsetY;
    private int x;
    private int y;
    private boolean hovered;

    public CheckBoxComponent(BooleanProperty property, ModuleComponent parentModule, int offsetY) {
        this.property = property;
        this.module = parentModule;
        this.x = parentModule.category.getX() + parentModule.category.getWidth();
        this.y = parentModule.category.getY() + parentModule.offsetY;
        this.offsetY = offsetY;
    }

    public void draw(AtomicInteger offset) {
        int rowX = this.module.category.getX();
        int rowY = this.module.category.getY() + this.offsetY;
        int rowW = this.module.category.getWidth();

        // Row background (solid opaque)
        Gui.drawRect(rowX, rowY, rowX + rowW, rowY + 14,
                this.hovered ? StyleHelper.SETTINGS_HOVER : StyleHelper.SETTINGS_BG);

        // Label (scaled 0.5x)
        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        Minecraft.getMinecraft().fontRendererObj.drawString(
                this.property.getName().replace("-", " "),
                (float) ((rowX + 4) * 2), (float) ((rowY + 4) * 2),
                StyleHelper.TEXT_SECONDARY, false);
        GL11.glPopMatrix();

        // Toggle switch
        boolean isOn = this.property.getValue();
        int trackX = rowX + rowW - 4 - StyleHelper.TOGGLE_WIDTH;
        int trackY = rowY + (14 - StyleHelper.TOGGLE_HEIGHT) / 2;

        // Track
        Gui.drawRect(trackX, trackY, trackX + StyleHelper.TOGGLE_WIDTH, trackY + StyleHelper.TOGGLE_HEIGHT,
                isOn ? StyleHelper.TOGGLE_ON : StyleHelper.TOGGLE_OFF);
        Gui.drawRect(trackX + 1, trackY + 1, trackX + StyleHelper.TOGGLE_WIDTH - 1, trackY + StyleHelper.TOGGLE_HEIGHT - 1,
                isOn ? StyleHelper.TOGGLE_ON : StyleHelper.TOGGLE_OFF);

        // Thumb (6x6)
        int thumbX = isOn ? trackX + StyleHelper.TOGGLE_WIDTH - 6 : trackX;
        int thumbY = trackY + 1;
        Gui.drawRect(thumbX, thumbY, thumbX + 6, thumbY + 6,
                isOn ? StyleHelper.TOGGLE_ON_INNER : StyleHelper.TOGGLE_OFF_INNER);
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
            this.property.setValue(!this.property.getValue());
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
