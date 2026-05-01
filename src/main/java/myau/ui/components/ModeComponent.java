package myau.ui.components;

import myau.property.properties.ModeProperty;
import myau.ui.Component;
import myau.ui.StyleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.atomic.AtomicInteger;

public class ModeComponent implements Component {
    private final ModeProperty property;
    private final ModuleComponent parentModule;
    private int x;
    private int y;
    private int offsetY;

    public ModeComponent(ModeProperty desc, ModuleComponent parentModule, int offsetY) {
        this.property = desc;
        this.parentModule = parentModule;
        this.x = parentModule.category.getX() + parentModule.category.getWidth();
        this.y = parentModule.category.getY() + parentModule.offsetY;
        this.offsetY = offsetY;
    }

    public void draw(AtomicInteger offset) {
        int rowX = this.parentModule.category.getX();
        int rowY = this.parentModule.category.getY() + this.offsetY;

        String mode = this.property.getModeString().replace("_", " ");
        String label = this.property.getName() + ": ";
        String modeDisplay = mode.substring(0, 1).toUpperCase() + mode.substring(1).toLowerCase();

        // Label (scaled 0.5x)
        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        int labelEnd = (int) ((rowX + 4) * 2) + Minecraft.getMinecraft().fontRendererObj.getStringWidth(label);
        Minecraft.getMinecraft().fontRendererObj.drawString(
                label, (float) ((rowX + 4) * 2), (float) ((rowY + 4) * 2),
                StyleHelper.TEXT_SECONDARY, true);
        GL11.glPopMatrix();

        // Badge (unscaled)
        int labelW = Minecraft.getMinecraft().fontRendererObj.getStringWidth(label);
        int modeW = Minecraft.getMinecraft().fontRendererObj.getStringWidth(modeDisplay);
        int badgeX = rowX + 4 + (int) (labelW * 0.5D);
        int badgeY = rowY + 2;
        int badgeW = (int) (modeW * 0.5D) + 4;
        Gui.drawRect(badgeX - 2, badgeY, badgeX + badgeW, badgeY + 10,
                StyleHelper.getCategoryAccentDimmed(this.parentModule.category.categoryName));

        // Mode value (scaled 0.5x)
        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        Minecraft.getMinecraft().fontRendererObj.drawString(
                modeDisplay, (float) labelEnd, (float) ((rowY + 4) * 2),
                StyleHelper.TEXT_PRIMARY, true);
        GL11.glPopMatrix();
    }

    public void update(int mousePosX, int mousePosY) {
        this.y = this.parentModule.category.getY() + this.offsetY;
        this.x = this.parentModule.category.getX();
    }

    public void setComponentStartAt(int newOffsetY) { this.offsetY = newOffsetY; }

    @Override
    public int getHeight() { return 14; }

    public void mouseDown(int x, int y, int button) {
        if (isHovered(x, y)) {
            if (button == 0) this.property.nextMode();
            else if (button == 1) this.property.previousMode();
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {}
    @Override
    public void keyTyped(char chatTyped, int keyCode) {}

    private boolean isHovered(int x, int y) {
        return x > this.x && x < this.x + this.parentModule.category.getWidth()
                && y > this.y && y < this.y + 14;
    }

    @Override
    public boolean isVisible() { return property.isVisible(); }
}
