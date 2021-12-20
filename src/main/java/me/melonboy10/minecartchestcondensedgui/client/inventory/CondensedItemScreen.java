package me.melonboy10.minecartchestcondensedgui.client.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.List;

public class CondensedItemScreen extends Screen {
    private static final Identifier GRID = new Identifier("minecartchestcondensedgui", "textures/gui/container/grid.png");

    private final int backgroundHeight = 172;
    private final int backgroundWidth = 193;

    enum SortDirection {ASCENDING, DESCENDING;

        public SortDirection other() {
            if (this.equals(ASCENDING))
                return DESCENDING;
            else
                return ASCENDING;
        }
    }
    enum SortFilter {QUANTITY, ALPHABETICALLY;

        public SortFilter other() {
            if (this.equals(QUANTITY))
                return ALPHABETICALLY;
            else
                return QUANTITY;
        }
    }
    private static SortDirection sortDirection = SortDirection.ASCENDING;
    private static SortFilter sortFilter = SortFilter.QUANTITY;
    private static boolean showCraftingTable = false;

    private int guiX;
    private int guiY;

    private int rowCount;

    private float scrollPosition;
    private int rowsScrolled;
    private boolean scrolling = false;
    private TextFieldWidget searchBox;
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public List<VirtualItemStack> items = new ArrayList<VirtualItemStack>();
    public List<ItemStack> playerItems = new ArrayList<ItemStack>();
    private ItemStack touchDragStack = ItemStack.EMPTY;
    private ItemStack pickStack = ItemStack.EMPTY;

    public CondensedItemScreen() {
        super(new LiteralText("Condensed Minecarts"));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
//        if (items.size() >= 2) {
//            pickStack = items.get(1);
//        }
        renderBackground(matrices);
        drawGrid(matrices, delta, mouseX, mouseY);
        drawButtons(matrices, delta, mouseX, mouseY);
        drawLabels(matrices, delta, mouseX, mouseY);
        drawSearchBox(matrices, delta, mouseX, mouseY);
        drawScrollBar(matrices, delta, mouseX, mouseY);
        drawPlayerInventory(matrices, delta, mouseX, mouseY);
        drawMinecartItems(matrices, delta, mouseX, mouseY);
        drawTouchDragStack(matrices, delta, mouseX, mouseY);
        drawPickStack(matrices, delta, mouseX, mouseY);
    }

    private void drawGrid(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GRID);

        int numberOfAddedRows = rowCount - 3;
        this.guiY = (this.height - this.backgroundHeight - numberOfAddedRows * 18) / 2;
        this.guiX = (this.width - this.backgroundWidth + 17) / 2;

        this.drawTexture(matrices, this.guiX, this.guiY, 0, 0, this.backgroundWidth, this.backgroundHeight);
        for (int i = 0; i < numberOfAddedRows; i++) {
            this.drawTexture(matrices, this.guiX, this.guiY + 72 + (18 * i), 0, 54, 193, 18);
        }
        this.drawTexture(matrices, this.guiX, this.guiY + 72 + numberOfAddedRows * 18, 0, 72, this.backgroundWidth, this.backgroundHeight - 72);
    }

    public void drawButtons(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GRID);

        for (int i = 0, j = 0; j < 4; i++, j++) {
            int x = guiX - 18;
            int y = guiY + 8 + (j * 18);

            switch (i) {
                case 1 -> { if (sortDirection.equals(SortDirection.ASCENDING)) i++; }
                case 3 -> { if (sortFilter.equals(SortFilter.ALPHABETICALLY)) i++; }
                case 5 -> { if (showCraftingTable) i++; }
            }
            if (isMouseOver(mouseX, mouseY, x, y, guiX - 2, guiY + 24 + (j * 18))){
                this.drawTexture(matrices, x, y, i * 16, 192, 16, 16);
            } else {
                this.drawTexture(matrices, x, y, i * 16, 176, 16, 16);
            }
            if (i == 1 || i == 3 || i == 5) i++;
        }
    }

    private void drawLabels(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        textRenderer.draw(matrices, "Minecarts", (this.guiX + 8), (this.guiY + 9), 4210752);
        textRenderer.draw(matrices, "Inventory", (this.guiX + 8), (this.guiY + rowCount*18 + 25), 4210752);
    }

    private void drawSearchBox(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        searchBox.render(matrices, mouseX, mouseY, delta);
    }

    private void drawScrollBar(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        int scrollBarX = this.guiX + 174;
        int scrollBarY = this.guiY + 20 + (int) ((float)((rowCount * 18) - 17) * this.scrollPosition);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GRID);
        if (rowCount >= Math.ceil(items.size()/9F)) {
            this.drawTexture(matrices, scrollBarX, scrollBarY, 244, 0, 12, 15);
        } else {
            this.drawTexture(matrices, scrollBarX, scrollBarY, 232, 0, 12, 15);
        }
    }

    private void drawPlayerInventory(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        for (int i = 0; i < 27; i++) {
            ItemStack inventoryItem = playerItems.get(i);
            int slotX = this.guiX + 8 + 18*(i % 9);
            int slotY = this.guiY + (rowCount * 18) + 36 + (18*(i / 9));
            itemRenderer.renderInGuiWithOverrides(inventoryItem, slotX, slotY);
            itemRenderer.renderGuiItemOverlay(this.textRenderer, inventoryItem, slotX, slotY, inventoryItem.getCount() == 1 ? "" : Integer.toString(inventoryItem.getCount()));
            if (mouseX >= slotX - 1 && mouseX <= slotX + 16 && mouseY >= slotY - 1 && mouseY <=  slotY + 16) {
                fillGradient(matrices, slotX, slotY, slotX + 16, slotY + 16, -2130706433, -2130706433, 200);
                if (pickStack == ItemStack.EMPTY && inventoryItem != ItemStack.EMPTY) {
                    renderTooltip(matrices, inventoryItem, mouseX, mouseY);
                }
            }
        }
        for (int i = 27; i < 36; i++) {
            ItemStack inventoryItem = playerItems.get(i);
            int slotX = this.guiX + 8 + 18*(i%9);
            int slotY = this.guiY + rowCount * 18 + 94;
            itemRenderer.renderInGuiWithOverrides(inventoryItem, slotX, slotY);
            itemRenderer.renderGuiItemOverlay(this.textRenderer, inventoryItem, slotX, slotY, inventoryItem.getCount() == 1 ? "" : Integer.toString(inventoryItem.getCount()));
            if (mouseX >= slotX - 1 && mouseX <= slotX + 16 && mouseY >= slotY - 1 && mouseY <=  slotY + 16) {
                fillGradient(matrices, slotX, slotY, slotX + 16, slotY + 16, -2130706433, -2130706433, 200);
                if (pickStack == ItemStack.EMPTY && inventoryItem != ItemStack.EMPTY) {
                    renderTooltip(matrices, inventoryItem, mouseX, mouseY);
                }
            }
        }
        for (int i = 0; i < 27; i++) {
            ItemStack inventoryItem = playerItems.get(i);
            int slotX = this.guiX + 8 + 18*(i % 9);
            int slotY = this.guiY + (rowCount * 18) + 36 + (18*(i / 9));
            if (mouseX >= slotX - 1 && mouseX <= slotX + 16 && mouseY >= slotY - 1 && mouseY <=  slotY + 16) {
                if (pickStack == ItemStack.EMPTY && inventoryItem != ItemStack.EMPTY) {
                    renderTooltip(matrices, inventoryItem, mouseX, mouseY);
                }
            }
        }
        for (int i = 27; i < 36; i++) {
            ItemStack inventoryItem = playerItems.get(i);
            int slotX = this.guiX + 8 + 18*(i%9);
            int slotY = this.guiY + rowCount * 18 + 94;
            if (mouseX >= slotX - 1 && mouseX <= slotX + 16 && mouseY >= slotY - 1 && mouseY <=  slotY + 16) {
                if (pickStack == ItemStack.EMPTY && inventoryItem != ItemStack.EMPTY) {
                    renderTooltip(matrices, inventoryItem, mouseX, mouseY);
                }
            }
        }
    }

    private void drawMinecartItems(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        for (int i = 0; i < rowCount * 9; i++) {
            int slotX = this.guiX + 8 + 18 * (i % 9);
            int slotY = this.guiY + 20 + 18 * (i / 9);
            if ((i + rowsScrolled*9) < items.size()) {
                ItemStack inventoryItem = items.get(i + rowsScrolled*9).visualItemStack;
                itemRenderer.renderInGuiWithOverrides(inventoryItem, slotX, slotY);
                String amountString;
                if (Math.abs(items.get(i + rowsScrolled*9).amount) > 999) {
                    amountString = Float.toString((float)Math.round(((float)(items.get(i + rowsScrolled*9).amount)/1000F)*10F)/10F) + "K";
                } else {
                    amountString = items.get(i + rowsScrolled*9).amount == 1 ? "" : Integer.toString(items.get(i + rowsScrolled*9).amount);
                }
                itemRenderer.renderGuiItemOverlay(this.textRenderer, inventoryItem, slotX, slotY, amountString);
            }
            if (mouseX >= slotX - 1 && mouseX <= slotX + 16 && mouseY >= slotY - 1 && mouseY <= slotY + 16) {
                fillGradient(matrices, slotX, slotY, slotX + 16, slotY + 16, -2130706433, -2130706433, 200);
            }
        }
        for (int i = 0; i < rowCount * 9; i++) {
            int slotX = this.guiX + 8 + 18 * (i % 9);
            int slotY = this.guiY + 20 + 18 * (i / 9);
            if ((i + rowsScrolled*9) < items.size()) {
                ItemStack inventoryItem = items.get(i + rowsScrolled * 9).visualItemStack;
                if (mouseX >= slotX - 1 && mouseX <= slotX + 16 && mouseY >= slotY - 1 && mouseY <= slotY + 16) {
                    if (pickStack == ItemStack.EMPTY && inventoryItem != ItemStack.EMPTY) {
                        renderTooltip(matrices, inventoryItem, mouseX, mouseY);
                    }
                }
            }
        }
    }

    private void drawTouchDragStack(MatrixStack matrices, float delta, int mouseX, int mouseY) {

    }

    private void drawPickStack(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        this.itemRenderer.zOffset = 200.0F;
        itemRenderer.renderInGuiWithOverrides(pickStack, (mouseX - 8), (mouseY - 8));
        itemRenderer.renderGuiItemOverlay(this.textRenderer, pickStack, (mouseX - 8), (mouseY - 8), pickStack.getCount() == 1 ? "" : Integer.toString(pickStack.getCount()));
        this.itemRenderer.zOffset = 0.0F;
    }

    private boolean isMouseOver(double mouseX, double mouseY, int x1, int y1, int x2, int y2) {
        return mouseX >= (double)x1 && mouseY >= (double)y1 && mouseX < (double)x2 && mouseY < (double)y2;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isMouseOver(mouseX, mouseY, this.guiX + 174, this.guiY + 20, this.guiX + 186, this.guiY + 18 + rowCount * 18) && rowCount < Math.ceil(items.size()/9F) || scrolling) {
            int y1 = this.guiY + 20;
            int y2 = y1 + (rowCount) * 18;

            this.scrollPosition = ((float)mouseY - (float)y1 - 7.5F) / ((float)(y2 - y1) - 15F);
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);

            rowsScrolled = Math.round(scrollPosition * (float)(Math.ceil(items.size()/9F) - rowCount));
            scrolling = true;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scrollPosition -= (float)amount/((float)(Math.ceil(items.size()/9F) - rowCount));
        scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
        rowsScrolled = Math.round(scrollPosition * (float)(Math.ceil(items.size()/9F) - rowCount));
        return true;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        scrolling = false;
        mouseDragged(mouseX, mouseY, button, 0, 0);
        checkButtons(mouseX, mouseY);
//        checkItems()

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void checkButtons(double mouseX, double mouseY) {
        if (isMouseOver(mouseX, mouseY, guiX - 18, guiY + 8, guiX + 2, guiY + 72)) {
            for (int i = 0; i < 4; i++) {
                int x = guiX - 18;
                int y = guiY + 8 + (i * 18);

                if (isMouseOver(mouseX, mouseY, x, y, guiX - 2, guiY + 24 + (i * 18))) {
                    switch (i) {
                        case 0 -> { // sort minecarts
//                            sortMinecarts();
                        }
                        case 1 -> { // sort direction
                            sortDirection = sortDirection.other();
                            if (sortFilter == SortFilter.ALPHABETICALLY) {
                                items.sort(nameComparator);
                            } else {
                                items.sort(quantityComparator);
                            }
                        }
                        case 2 -> { // sort filter
                            sortFilter = sortFilter.other();
                            if (sortFilter == SortFilter.ALPHABETICALLY) {
                                items.sort(nameComparator);
                            } else {
                                items.sort(quantityComparator);
                            }
                        }
                        case 3 -> // crafting table
                            showCraftingTable = !showCraftingTable;
                    }
                }
            }
        }
    }

    protected void init() {
        super.init();
        rowCount = (this.height - 220) / 18 + 3;
        this.guiY = (this.height - this.backgroundHeight - (rowCount - 3) * 18) / 2;
        this.guiX = (this.width - this.backgroundWidth) / 2;
        for (int i = 0; i < 36; i++) {
            playerItems.add(ItemStack.EMPTY);
        }
        client.keyboard.setRepeatEvents(true);
        TextRenderer textRenderer = this.textRenderer;
        this.searchBox = new TextFieldWidget(textRenderer, this.guiX + 82, this.guiY + 7, 80, 9, new TranslatableText("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setDrawsBackground(false);
        this.searchBox.setVisible(true);
        this.searchBox.setEditableColor(16777215);
        this.addSelectableChild(this.searchBox);
    }

    public void addItems(ChestMinecartEntity minecart, ItemStack itemstack, int slot) {
        boolean newItem = true;
        for (int i = 0; i < items.size(); i++) {
            VirtualItemStack virtualItemStack = items.get(i);
            if (virtualItemStack.visualItemStack.isItemEqualIgnoreDamage(itemstack) && virtualItemStack.visualItemStack.getDamage() == itemstack.getDamage()) {
                newItem = false;
                virtualItemStack.setItems(minecart, slot, itemstack.getCount());
                if (sortFilter == SortFilter.ALPHABETICALLY) {
                    items.sort(nameComparator);
                } else {
                    items.sort(quantityComparator);
                }
            }
        }
        if (newItem) {
            items.add(new VirtualItemStack(itemstack, minecart, slot, itemstack.getCount()));
            if (sortFilter == SortFilter.ALPHABETICALLY) {
                items.sort(nameComparator);
            } else {
                items.sort(quantityComparator);
            }
        }
    }

    private final Comparator<VirtualItemStack> quantityComparator = (virtualItemStack1, virtualItemStack2) -> {
        int difference = virtualItemStack2.amount - virtualItemStack1.amount;
        if (difference > 0) {
            return sortDirection == SortDirection.ASCENDING ? 1 : -1;
        } else if (difference < 0) {
            return sortDirection == SortDirection.ASCENDING ? -1 : 1;
        } else {
            difference = virtualItemStack1.visualItemStack.getName().getString().compareToIgnoreCase(virtualItemStack2.visualItemStack.getName().getString());
            if (difference > 0) {
                return sortDirection == SortDirection.ASCENDING ? 1 : -1;
            } else if (difference < 0) {
                return sortDirection == SortDirection.ASCENDING ? -1 : 1;
            } else {
                assert virtualItemStack1.visualItemStack.getNbt() != null;
                assert virtualItemStack2.visualItemStack.getNbt() != null;
                difference = virtualItemStack1.visualItemStack.getNbt().toString().compareToIgnoreCase(virtualItemStack2.visualItemStack.getNbt().toString());
                if (difference > 0) {
                    return sortDirection == SortDirection.ASCENDING ? 1 : -1;
                } else if (difference < 0) {
                    return sortDirection == SortDirection.ASCENDING ? -1 : 1;
                } else {
                    return 0;
                }
            }
        }
    };

    private final Comparator<VirtualItemStack> nameComparator = (virtualItemStack1, virtualItemStack2) -> {
        int difference = virtualItemStack1.visualItemStack.getName().getString().compareToIgnoreCase(virtualItemStack2.visualItemStack.getName().getString());
        if (difference > 0) {
            return sortDirection == SortDirection.ASCENDING ? 1 : -1;
        } else if (difference < 0) {
            return sortDirection == SortDirection.ASCENDING ? -1 : 1;
        } else {
            difference = virtualItemStack2.amount - virtualItemStack1.amount;
            if (difference > 0) {
                return sortDirection == SortDirection.ASCENDING ? 1 : -1;
            } else if (difference < 0) {
                return sortDirection == SortDirection.ASCENDING ? -1 : 1;
            } else {
                assert virtualItemStack1.visualItemStack.getNbt() != null;
                assert virtualItemStack2.visualItemStack.getNbt() != null;
                difference = virtualItemStack1.visualItemStack.getNbt().toString().compareToIgnoreCase(virtualItemStack2.visualItemStack.getNbt().toString());
                if (difference > 0) {
                    return sortDirection == SortDirection.ASCENDING ? 1 : -1;
                } else if (difference < 0) {
                    return sortDirection == SortDirection.ASCENDING ? -1 : 1;
                } else {
                    return 0;
                }
            }
        }
    };

    public boolean isPauseScreen() { return false; }

}