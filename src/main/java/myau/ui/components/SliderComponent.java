package myau.ui.components;

import myau.Myau;
import myau.module.modules.HUD;
import myau.ui.ClickGui;
import myau.ui.Component;
import myau.ui.StyleHelper;
import myau.ui.callback.GuiInput;
import myau.ui.dataset.Slider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;

public class SliderComponent implements Component {
    private final Slider slider;
    private final ModuleComponent parentModule;
    private int offsetY;
    private int x;
    private int y;
    private boolean dragging = false;
    private double sliderWidth;
    private long increment = 0;
    private long decrement = 0;

    public SliderComponent(Slider slider, ModuleComponent parentModule, int offsetY) {
        this.slider = slider;
        this.parentModule = parentModule;
        this.x = parentModule.category.getX() + parentModule.category.getWidth();
        this.y = parentModule.category.getY() + parentModule.offsetY;
        this.offsetY = offsetY;
    }

    public void draw(AtomicInteger offset) {
        int rowX = this.parentModule.category.getX();
        int rowY = this.parentModule.category.getY() + this.offsetY;
        int rowW = this.parentModule.category.getWidth();

        // Row background (solid opaque)
        Gui.drawRect(rowX, rowY, rowX + rowW, rowY + 18, StyleHelper.SETTINGS_BG);

        // Label + value (scaled 0.5x)
        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                this.slider.getName() + ": " + this.slider.getValueColorString(),
                (float) ((rowX + 4) * 2), (float) ((rowY + 2) * 2),
                StyleHelper.TEXT_PRIMARY);
        GL11.glPopMatrix();

        // Track
        int trackX = rowX + 4;
        int trackY = rowY + 10;
        int trackW = rowW - 8;
        Gui.drawRect(trackX, trackY, trackX + trackW, trackY + StyleHelper.SLIDER_HEIGHT, StyleHelper.SLIDER_TRACK);

        // Fill (full width, no 84px cap)
        int fillW = (int) this.sliderWidth;
        if (fillW > 0) {
            int fillColor = ((HUD) Myau.moduleManager.modules.get(HUD.class))
                    .getColor(System.currentTimeMillis(), offset.get()).getRGB();
            Gui.drawRect(trackX, trackY, trackX + fillW, trackY + StyleHelper.SLIDER_HEIGHT, fillColor);
        }

        // Thumb (6x6 at fill end)
        int thumbX = trackX + fillW - 3;
        int thumbY = trackY - 1;
        Gui.drawRect(thumbX, thumbY, thumbX + StyleHelper.SLIDER_THUMB_SIZE, thumbY + StyleHelper.SLIDER_THUMB_SIZE, 0xFFFFFFFF);
    }

    public void setComponentStartAt(int newOffsetY) { this.offsetY = newOffsetY; }

    @Override
    public int getHeight() { return 18; }

    public void update(int mousePosX, int mousePosY) {
        this.y = this.parentModule.category.getY() + this.offsetY;
        this.x = this.parentModule.category.getX();

        double d = Math.min(this.parentModule.category.getWidth() - 8, Math.max(0, mousePosX - this.x - 4));
        this.sliderWidth = (double) (this.parentModule.category.getWidth() - 8)
                * (this.slider.getInput() - this.slider.getMin())
                / (this.slider.getMax() - this.slider.getMin());

        if (this.dragging) {
            if (d == 0.0D) {
                this.slider.setValue(this.slider.getMin());
            } else {
                double rawValue = d / (double) (this.parentModule.category.getWidth() - 8)
                        * (this.slider.getMax() - this.slider.getMin()) + this.slider.getMin();
                double inc = this.slider.getIncrement();
                if (inc > 0) rawValue = Math.round(rawValue / inc) * inc;
                rawValue = Math.max(this.slider.getMin(), Math.min(this.slider.getMax(), roundToPrecision(rawValue, 2)));
                this.slider.setValue(rawValue);
            }
        }
        // Only step when NOT dragging
        if (!this.dragging && this.increment != 0 && this.increment < System.currentTimeMillis()) {
            this.increment = System.currentTimeMillis() + 50;
            this.slider.stepping(true);
        }
        if (!this.dragging && this.decrement != 0 && this.decrement < System.currentTimeMillis()) {
            this.decrement = System.currentTimeMillis() + 50;
            this.slider.stepping(false);
        }
    }

    private static double roundToPrecision(double v, int precision) {
        if (precision < 0) return 0.0D;
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(precision, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void mouseDown(int x, int y, int button) {
        if (this.isTextHovered(x, y) && button == 0 && this.parentModule.panelExpand) {
            GuiInput.prompt(slider.getName(), slider.getValueString(), slider::setValueString, ClickGui.getInstance());
            return;
        }
        if (this.isLeftHalfHovered(x, y) && this.parentModule.panelExpand) {
            if (button == 0) this.dragging = true;
            else if (button == 1 && this.decrement == 0) {
                this.decrement = System.currentTimeMillis() + 500;
                this.slider.stepping(false);
            }
        }
        if (this.isRightHalfHovered(x, y) && this.parentModule.panelExpand) {
            if (button == 0) this.dragging = true;
            else if (button == 1 && this.increment == 0) {
                this.increment = System.currentTimeMillis() + 500;
                this.slider.stepping(true);
            }
        }
    }

    public void mouseReleased(int x, int y, int button) {
        this.dragging = false;
        this.increment = 0;
        this.decrement = 0;
    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {}

    public boolean isTextHovered(int x, int y) {
        return x > this.x && x < this.x + this.parentModule.category.getWidth()
                && y > this.y && y < this.y + 10;
    }

    public boolean isLeftHalfHovered(int x, int y) {
        return x > this.x && x < this.x + this.parentModule.category.getWidth() / 2 + 1
                && y > this.y + 10 && y < this.y + 18;
    }

    public boolean isRightHalfHovered(int x, int y) {
        return x > this.x + this.parentModule.category.getWidth() / 2
                && x < this.x + this.parentModule.category.getWidth()
                && y > this.y + 10 && y < this.y + 18;
    }

    @Override
    public boolean isVisible() { return slider.isVisible(); }
}
