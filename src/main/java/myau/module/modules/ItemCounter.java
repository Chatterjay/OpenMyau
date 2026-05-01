package myau.module.modules;

import myau.event.EventTarget;
import myau.events.Render2DEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemCounter extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public final BooleanProperty diamond = new BooleanProperty("Diamond", true);
    public final BooleanProperty emerald = new BooleanProperty("Emerald", true);
    public final BooleanProperty gold = new BooleanProperty("Gold-Ingot", true);
    public final BooleanProperty iron = new BooleanProperty("Iron-Ingot", true);
    public final BooleanProperty wool = new BooleanProperty("Wool", true);
    public final ModeProperty side = new ModeProperty("Side", 0, new String[]{"LEFT", "RIGHT"});

    public ItemCounter() {
        super("ItemCounter", false);
    }

    private int countItem(Item item) {
        int count = 0;
        for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
            if (stack != null && stack.getItem() == item) {
                count += stack.stackSize;
            }
        }
        return count;
    }

    private int countWool() {
        int count = 0;
        for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
            if (stack != null && stack.getItem() == Item.getItemFromBlock(Blocks.wool)) {
                count += stack.stackSize;
            }
        }
        return count;
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!this.isEnabled() || mc.thePlayer == null || mc.thePlayer.inventory == null) return;

        ScaledResolution sr = new ScaledResolution(mc);
        FontRenderer fr = mc.fontRendererObj;

        // Collect visible entries: item name, count, display color
        int entries = 0;
        int diamondCount = 0, emeraldCount = 0, goldCount = 0, ironCount = 0, woolCount = 0;

        if (diamond.getValue()) { diamondCount = countItem(Items.diamond); entries++; }
        if (emerald.getValue()) { emeraldCount = countItem(Items.emerald); entries++; }
        if (gold.getValue())    { goldCount = countItem(Items.gold_ingot); entries++; }
        if (iron.getValue())    { ironCount = countItem(Items.iron_ingot); entries++; }
        if (wool.getValue())    { woolCount = countWool(); entries++; }

        if (entries == 0) return;

        // Measure maximum text width for panel sizing
        int lineH = fr.FONT_HEIGHT + 2;
        int padding = 3;
        int maxTextW = 0;

        if (diamond.getValue()) maxTextW = Math.max(maxTextW, fr.getStringWidth("Diamond: " + diamondCount));
        if (emerald.getValue()) maxTextW = Math.max(maxTextW, fr.getStringWidth("Emerald: " + emeraldCount));
        if (gold.getValue())    maxTextW = Math.max(maxTextW, fr.getStringWidth("Gold: " + goldCount));
        if (iron.getValue())    maxTextW = Math.max(maxTextW, fr.getStringWidth("Iron: " + ironCount));
        if (wool.getValue())    maxTextW = Math.max(maxTextW, fr.getStringWidth("Wool: " + woolCount));

        int panelW = maxTextW + padding * 2 + 8;
        int panelH = entries * lineH + padding * 2;

        // Position above hotbar (left or right)
        int panelX = side.getValue() == 1 ? sr.getScaledWidth() - panelW - 4 : 4;
        int panelY = sr.getScaledHeight() - 22 - panelH - 4;

        // Background panel
        Gui.drawRect(panelX, panelY, panelX + panelW, panelY + panelH, 0x90000000);

        // Render each entry
        int renderY = panelY + padding + 1;

        if (diamond.getValue()) {
            Gui.drawRect(panelX + padding, renderY + 2, panelX + padding + 4, renderY + 6, 0xFF55FFFF);
            fr.drawStringWithShadow("Diamond: " + diamondCount, panelX + padding + 8, renderY, 0xFF55FFFF);
            renderY += lineH;
        }
        if (emerald.getValue()) {
            Gui.drawRect(panelX + padding, renderY + 2, panelX + padding + 4, renderY + 6, 0xFF55FF55);
            fr.drawStringWithShadow("Emerald: " + emeraldCount, panelX + padding + 8, renderY, 0xFF55FF55);
            renderY += lineH;
        }
        if (gold.getValue()) {
            Gui.drawRect(panelX + padding, renderY + 2, panelX + padding + 4, renderY + 6, 0xFFFFAA00);
            fr.drawStringWithShadow("Gold: " + goldCount, panelX + padding + 8, renderY, 0xFFFFAA00);
            renderY += lineH;
        }
        if (iron.getValue()) {
            Gui.drawRect(panelX + padding, renderY + 2, panelX + padding + 4, renderY + 6, 0xFFC0C0C0);
            fr.drawStringWithShadow("Iron: " + ironCount, panelX + padding + 8, renderY, 0xFFC0C0C0);
            renderY += lineH;
        }
        if (wool.getValue()) {
            Gui.drawRect(panelX + padding, renderY + 2, panelX + padding + 4, renderY + 6, 0xFFAAAAAA);
            fr.drawStringWithShadow("Wool: " + woolCount, panelX + padding + 8, renderY, 0xFFAAAAAA);
        }
    }
}
