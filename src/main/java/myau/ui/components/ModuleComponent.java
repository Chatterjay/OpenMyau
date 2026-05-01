package myau.ui.components;

import myau.Myau;
import myau.module.Module;
import myau.property.Property;
import myau.property.properties.*;
import myau.ui.Component;
import myau.ui.StyleHelper;
import myau.ui.dataset.impl.FloatSlider;
import myau.ui.dataset.impl.IntSlider;
import myau.ui.dataset.impl.PercentageSlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ModuleComponent implements Component {
    public Module mod;
    public CategoryComponent category;
    public int offsetY;
    private final ArrayList<Component> settings;
    public boolean panelExpand;
    private int x;
    private int y;
    private boolean hovered;

    public ModuleComponent(Module mod, CategoryComponent category, int offsetY) {
        this.mod = mod;
        this.category = category;
        this.offsetY = offsetY;
        this.settings = new ArrayList<>();
        this.panelExpand = false;
        int y = offsetY + StyleHelper.MODULE_HEIGHT;
        if (!Myau.propertyManager.properties.get(mod.getClass()).isEmpty()) {
            for (Property<?> baseProperty : Myau.propertyManager.properties.get(mod.getClass())) {
                if (baseProperty instanceof BooleanProperty) {
                    CheckBoxComponent c = new CheckBoxComponent((BooleanProperty) baseProperty, this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof FloatProperty) {
                    SliderComponent c = new SliderComponent(new FloatSlider((FloatProperty) baseProperty), this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof IntProperty) {
                    SliderComponent c = new SliderComponent(new IntSlider((IntProperty) baseProperty), this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof PercentProperty) {
                    SliderComponent c = new SliderComponent(new PercentageSlider((PercentProperty) baseProperty), this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof ModeProperty) {
                    ModeComponent c = new ModeComponent((ModeProperty) baseProperty, this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof ColorProperty) {
                    ColorSliderComponent c = new ColorSliderComponent((ColorProperty) baseProperty, this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseProperty instanceof TextProperty) {
                    TextComponent c = new TextComponent((TextProperty) baseProperty, this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                }
            }
        }
        this.settings.add(new BindComponent(this, y));
    }

    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
        int y = this.offsetY + StyleHelper.MODULE_HEIGHT;
        for (Component c : this.settings) {
            c.setComponentStartAt(y);
            if (c.isVisible()) y += c.getHeight();
        }
    }

    public void draw(AtomicInteger offset) {
        int rowX = this.category.getX();
        int rowY = this.category.getY() + this.offsetY;
        int rowW = this.category.getWidth();

        // Row background
        Gui.drawRect(rowX, rowY, rowX + rowW, rowY + StyleHelper.MODULE_HEIGHT,
                this.hovered ? StyleHelper.MODULE_HOVER : StyleHelper.MODULE_BG);

        // Enabled indicator dot
        if (this.mod.isEnabled()) {
            int accent = StyleHelper.getCategoryAccent(this.category.categoryName);
            Gui.drawRect(rowX + 3, rowY + (StyleHelper.MODULE_HEIGHT / 2) - 2,
                    rowX + 7, rowY + (StyleHelper.MODULE_HEIGHT / 2) + 2, accent);
        }

        // Module name (scaled 0.5x)
        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        int textX = (rowX + (this.mod.isEnabled() ? 11 : 4)) * 2;
        int textY = (rowY + 4) * 2;
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                this.mod.getName(), (float) textX, (float) textY,
                StyleHelper.getModuleColor(this.mod.isEnabled(), offset.get()));

        // Expanded arrow
        if (this.panelExpand) {
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                    "^", (float) ((rowX + rowW - 8) * 2), (float) ((rowY + 2) * 2),
                    StyleHelper.TEXT_SECONDARY);
        }
        GL11.glPopMatrix();

        // Settings
        if (this.panelExpand && !this.settings.isEmpty()) {
            for (Component c : this.settings) {
                if (c.isVisible()) {
                    c.draw(offset);
                    offset.incrementAndGet();
                }
            }
        }
    }

    public int getHeight() {
        if (!this.panelExpand) return StyleHelper.MODULE_HEIGHT;
        int h = StyleHelper.MODULE_HEIGHT;
        for (Component c : this.settings) {
            if (c.isVisible()) h += c.getHeight();
        }
        return h;
    }

    public void update(int mousePosX, int mousePosY) {
        this.y = this.category.getY() + this.offsetY;
        this.x = this.category.getX();
        this.hovered = isHovered(mousePosX, mousePosY);
        if (!panelExpand) return;
        for (Component c : this.settings) {
            if (c.isVisible()) c.update(mousePosX, mousePosY);
        }
    }

    public void mouseDown(int x, int y, int button) {
        if (this.isHovered(x, y) && button == 0) this.mod.toggle();
        if (this.isHovered(x, y) && button == 1) this.panelExpand = !this.panelExpand;
        if (!panelExpand) return;
        for (Component c : this.settings) {
            if (c.isVisible()) c.mouseDown(x, y, button);
        }
    }

    public void mouseReleased(int x, int y, int button) {
        if (!panelExpand) return;
        for (Component c : this.settings) {
            if (c.isVisible()) c.mouseReleased(x, y, button);
        }
    }

    public void keyTyped(char chatTyped, int keyCode) {
        if (!panelExpand) return;
        for (Component c : this.settings) {
            if (c.isVisible()) c.keyTyped(chatTyped, keyCode);
        }
    }

    public boolean isHovered(int x, int y) {
        return x > this.category.getX() && x < this.category.getX() + this.category.getWidth()
                && y > this.category.getY() + this.offsetY
                && y < this.category.getY() + StyleHelper.MODULE_HEIGHT + this.offsetY;
    }

    @Override
    public boolean isVisible() { return true; }
}
