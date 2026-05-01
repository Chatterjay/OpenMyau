package myau.ui.components;

import myau.module.modules.GuiModule;
import myau.ui.Component;
import myau.ui.StyleHelper;
import myau.ui.dataset.BindStage;
import myau.util.KeyBindUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.atomic.AtomicInteger;

public class BindComponent implements Component {
    private boolean isBinding;
    private final ModuleComponent parentModule;
    private int offsetY;
    private int x;
    private int y;

    public BindComponent(ModuleComponent b, int offsetY) {
        this.parentModule = b;
        this.x = b.category.getX() + b.category.getWidth();
        this.y = b.category.getY() + b.offsetY;
        this.offsetY = offsetY;
    }

    public void draw(AtomicInteger offset) {
        int rowX = this.parentModule.category.getX();
        int rowY = this.parentModule.category.getY() + this.offsetY;

        // Row background (solid opaque)
        Gui.drawRect(rowX, rowY, rowX + this.parentModule.category.getWidth(), rowY + 14, StyleHelper.SETTINGS_BG);

        String label = "Bind";
        String valueText;
        boolean showText = true;

        if (this.isBinding) {
            valueText = BindStage.binding;
            showText = System.currentTimeMillis() % 1000 < 500;
        } else {
            valueText = KeyBindUtil.getKeyName(this.parentModule.mod.getKey());
        }

        // Label (scaled 0.5x)
        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        int labelEnd = (int) ((rowX + 4) * 2) + Minecraft.getMinecraft().fontRendererObj.getStringWidth(label + ": ");
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                label + ": ", (float) ((rowX + 4) * 2), (float) ((rowY + 3) * 2),
                StyleHelper.TEXT_SECONDARY);
        GL11.glPopMatrix();

        // Badge (unscaled)
        int labelW = Minecraft.getMinecraft().fontRendererObj.getStringWidth(label + ": ");
        int valueW = Minecraft.getMinecraft().fontRendererObj.getStringWidth(valueText);
        int badgeX = rowX + 4 + (int) (labelW * 0.5D);
        int badgeY = rowY + 1;
        int badgeW = (int) (valueW * 0.5D) + 4;
        int badgeColor = this.isBinding
                ? StyleHelper.getCategoryAccent(this.parentModule.category.categoryName)
                : StyleHelper.SETTINGS_BG;
        Gui.drawRect(badgeX - 2, badgeY, badgeX + badgeW, badgeY + 12, badgeColor);

        // Value text (scaled 0.5x) — blink when binding
        if (showText) {
            GL11.glPushMatrix();
            GL11.glScaled(0.5D, 0.5D, 0.5D);
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                    valueText, (float) labelEnd, (float) ((rowY + 3) * 2),
                    StyleHelper.TEXT_PRIMARY);
            GL11.glPopMatrix();
        }
    }

    @Override
    public void update(int mousePosX, int mousePosY) {
        this.y = this.parentModule.category.getY() + this.offsetY;
        this.x = this.parentModule.category.getX();
    }

    public void mouseDown(int x, int y, int button) {
        if (this.isHovered(x, y) && button == 0 && this.parentModule.panelExpand) {
            this.isBinding = !this.isBinding;
        } else if (this.isBinding && this.parentModule.panelExpand) {
            if (button == 0) { this.isBinding = false; return; }
            this.parentModule.mod.setKey(button - 100);
            this.isBinding = false;
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {}

    @Override
    public void keyTyped(char chatTyped, int keyCode) {
        if (this.isBinding) {
            if (keyCode == 1) { this.isBinding = false; return; }
            if (keyCode == 11) {
                this.parentModule.mod.setKey(this.parentModule.mod instanceof GuiModule ? 54 : 0);
            } else {
                this.parentModule.mod.setKey(keyCode);
            }
            this.isBinding = false;
        }
    }

    @Override
    public void setComponentStartAt(int newOffsetY) { this.offsetY = newOffsetY; }

    public boolean isHovered(int x, int y) {
        return x > this.x && x < this.x + this.parentModule.category.getWidth()
                && y > this.y - 1 && y < this.y + 14;
    }

    public int getHeight() { return 14; }

    @Override
    public boolean isVisible() { return true; }
}
