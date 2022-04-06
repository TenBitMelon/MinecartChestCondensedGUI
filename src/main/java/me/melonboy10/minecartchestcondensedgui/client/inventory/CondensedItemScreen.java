package me.melonboy10.minecartchestcondensedgui.client.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.List;

public class CondensedItemScreen extends Screen {
    private static final Identifier GRID = new Identifier("minecartchestcondensedgui", "textures/gui/container/grid.png");

    private final int backgroundHeight = 229;
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

    private static SortDirection sortDirection = SortDirection.DESCENDING;
    private static SortFilter sortFilter = SortFilter.QUANTITY;
    private static boolean showCraftingTable = true;
    private BlockPos craftingTableLocation;

    private int guiX;
    private int guiY;

    private int rowCount;

    private float scrollPosition;
    private int rowsScrolled;
    private boolean scrolling = false;
    private TextFieldWidget searchBox;
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private long lastButtonClickTime;
    private int lastClickedSlot;
    private HoveredInventory lastClickedInventory;
    private ItemStack lastClickedItem = ItemStack.EMPTY;
    private int lastClickedButton;

    private boolean draggingItems;
    private boolean beforeDragDragging;
    private int draggingStartingSlot;
    private int draggingItemsButton;
    private int draggedStackRemainder;
    private final Set<Integer> draggingItemSlots = new HashSet<>();
    private final ArrayList<Integer> draggingItemsPerSlot = new ArrayList<>(Collections.nCopies(36, 0));

    public List<VirtualItemStack> items = new ArrayList<>();
    public List<VirtualItemStack> visibleItems = new ArrayList<>();
    public List<VirtualItemStack> searchedVisibleItems = new ArrayList<>();
    public List<ItemStack> visiblePlayerItems = new ArrayList<>();

    enum HoveredInventory {NONE, MINECARTS, PLAYER, OUTSIDE}
    private HoveredInventory hoveredInventory;
    private int hoveredSlot;
    private ItemStack mouseStack = ItemStack.EMPTY;
    private boolean itemFromMinecarts = false;

    public CondensedItemScreen() {
        super(new LiteralText("Condensed Minecarts"));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawGrid(matrices, delta, mouseX, mouseY);
        drawSearchBox(matrices, delta, mouseX, mouseY);
        drawLabels(matrices, delta, mouseX, mouseY);
        drawScrollBar(matrices, delta, mouseX, mouseY);
        drawButtons(matrices, delta, mouseX, mouseY);
        drawPlayerInventory(matrices, delta, mouseX, mouseY);
        drawMinecartItems(matrices, delta, mouseX, mouseY);
        drawPlayerTooltips(matrices, delta, mouseX, mouseY);
        drawMinecartTooltips(matrices, delta, mouseX, mouseY);
        drawButtonTooltips(matrices, delta, mouseX, mouseY);
        drawMouseStack(matrices, delta, mouseX, mouseY);
    }

    private void drawGrid(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GRID);

        int numberOfAddedRows = rowCount - 3;

        this.drawTexture(matrices, this.guiX, this.guiY, 0, 0, this.backgroundWidth, this.backgroundHeight - 150); // Top
        for (int i = 0; i < numberOfAddedRows; i++) {
            this.drawTexture(matrices, this.guiX, this.guiY + 72 + (18 * i), 0, 54, 193, 25); // Row Segment
        }
        if (showCraftingTable && craftingTableLocation != null) {
            // Crafting Table Segment
            this.drawTexture(matrices, this.guiX, this.guiY + 78 + numberOfAddedRows * 18, 0, 79, this.backgroundWidth, this.backgroundHeight - 79); // Bottom
        } else {
            this.drawTexture(matrices, this.guiX, this.guiY + 78 + numberOfAddedRows * 18, 0, 135, this.backgroundWidth, this.backgroundHeight - 135); // Bottom
        }
    }

    public void drawButtons(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        for (int i = 0, j = 0; j < 4; i++, j++) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GRID);

            int x = guiX - 18;
            int y = guiY + 8 + (j * 18);

            switch (i) {
                case 1 -> { if (sortDirection.equals(SortDirection.ASCENDING)) i++; }
                case 3 -> { if (sortFilter.equals(SortFilter.ALPHABETICALLY)) i++; }
                case 5 -> { if (!showCraftingTable) i++; }
            }
            if (isMouseOver(mouseX, mouseY, x, y, guiX - 2, guiY + 24 + (j * 18))){
                this.drawTexture(matrices, x, y, 213, i * 16, 16, 16);
            } else {
                this.drawTexture(matrices, x, y, 197, i * 16, 16, 16);
            }
            if (i == 1 || i == 3 || i == 5) i++;
        }
    }

    public void drawButtonTooltips(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        for (int i = 0, j = 0; j < 4; i++, j++) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, GRID);

            int x = guiX - 18;
            int y = guiY + 8 + (j * 18);

            switch (i) {
                case 1 -> { if (sortDirection.equals(SortDirection.ASCENDING)) i++; }
                case 3 -> { if (sortFilter.equals(SortFilter.ALPHABETICALLY)) i++; }
                case 5 -> { if (!showCraftingTable) i++; }
            }
            if (isMouseOver(mouseX, mouseY, x, y, guiX - 2, guiY + 24 + (j * 18))){
                renderTooltip(matrices, new LiteralText(switch (j) {
                    case 0 -> "Sort Nearby Minecarts"; // Sort Carts
                    case 1 -> sortDirection.equals(SortDirection.ASCENDING) ? "Sorting Ascending" : "Sorting Descending"; // Sort Direction
                    case 2 -> sortFilter.equals(SortFilter.QUANTITY) ? "Sorting Quantity" : "Sorting Alphabetically"; // Sort Filter
                    case 3 -> showCraftingTable ? "Showing Crafting Table" : "Hiding Crafting Table"; // Crafting table
                    default -> throw new IllegalStateException("Unexpected value: " + j);
                }), mouseX, mouseY);
            }
            if (i == 1 || i == 3 || i == 5) i++;
        }
    }

    private void drawLabels(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        textRenderer.draw(matrices, "Minecarts", this.guiX + 8, this.guiY + 8, 4210752);
        textRenderer.draw(matrices, "Inventory", this.guiX + 8, this.guiY + rowCount * 18 + 24 + (showCraftingTable && craftingTableLocation != null ? 56 : 0 ), 4210752);
    }

    private void drawSearchBox(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        searchBox.render(matrices, mouseX, mouseY, delta);
    }

    private void drawScrollBar(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        int scrollBarX = this.guiX + 174;
        int scrollBarY = this.guiY + 20 + (int) ((float)((rowCount * 18) - 17) * this.scrollPosition);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GRID);
        if (rowCount >= Math.ceil(searchedVisibleItems.size()/9F)) {
            this.drawTexture(matrices, scrollBarX, scrollBarY, 244, 0, 12, 15);
        } else {
            this.drawTexture(matrices, scrollBarX, scrollBarY, 232, 0, 12, 15);
        }
    }

    private void drawPlayerInventory(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        hoveredSlot = -1;
        hoveredInventory = HoveredInventory.NONE;
        for (int i = 0; i < 36; i++) {
            ItemStack inventoryItem = visiblePlayerItems.get(i);
            int slotX = this.guiX + 8 + 18*(i % 9);
            int slotY;
            if (i < 27)
                slotY = this.guiY + (rowCount * 18) + 36 + (18*(i / 9));
            else
                slotY = this.guiY + rowCount * 18 + 94;
            if (showCraftingTable && craftingTableLocation != null) slotY += 56;

            this.itemRenderer.zOffset = 100.0F;
            if (draggingItemsPerSlot.get(i) > 0 && draggingItemSlots.contains(i)) {
                fillGradient(matrices, slotX, slotY, slotX + 16, slotY + 16, -2130706433, -2130706433, 200);
                int amount = inventoryItem.getCount() + draggingItemsPerSlot.get(i);
                itemRenderer.renderInGui(mouseStack, slotX, slotY);
                itemRenderer.renderGuiItemOverlay(this.textRenderer, mouseStack, slotX, slotY, amount <= 1 ? "" : Integer.toString(amount));
            } else {
                itemRenderer.renderInGui(inventoryItem, slotX, slotY);
                itemRenderer.renderGuiItemOverlay(this.textRenderer, inventoryItem, slotX, slotY, inventoryItem.getCount() <= 1 ? "" : Integer.toString(inventoryItem.getCount()));
            }
            this.itemRenderer.zOffset = 0.0F;

            if (mouseX >= slotX - 1 && mouseX <= slotX + 16 && mouseY >= slotY - 1 && mouseY <=  slotY + 16) {
                fillGradient(matrices, slotX, slotY, slotX + 16, slotY + 16, -2130706433, -2130706433, 200);
                hoveredSlot = i;
                hoveredInventory = HoveredInventory.PLAYER;
            }
        }
    }

    private void drawMinecartItems(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        for (int i = 0; i < rowCount * 9; i++) {
            int slotX = this.guiX + 8 + 18 * (i % 9);
            int slotY = this.guiY + 20 + 18 * (i / 9);
            if ((i + rowsScrolled*9) < searchedVisibleItems.size()) {
                ItemStack inventoryItem = searchedVisibleItems.get(i + rowsScrolled*9).visualItemStack;
                itemRenderer.renderInGui(inventoryItem, slotX, slotY);
                itemRenderer.renderGuiItemOverlay(this.textRenderer, inventoryItem, slotX, slotY, "");
                String amountString = abbreviateAmount(searchedVisibleItems.get(i + rowsScrolled*9).amount);
                MatrixStack textMatrixStack = new MatrixStack();
                textMatrixStack.scale(0.5F, 0.5F, 1);
                textMatrixStack.translate(0, 0, itemRenderer.zOffset + 200.0F);
                if (searchedVisibleItems.get(i + rowsScrolled*9).amount == 0) {
                    textRenderer.drawWithShadow(textMatrixStack, amountString, slotX * 2 + 31 - textRenderer.getWidth(amountString), slotY * 2 + 23, Formatting.RED.getColorValue());
                } else {
                    textRenderer.drawWithShadow(textMatrixStack, amountString, slotX * 2 + 31 - textRenderer.getWidth(amountString), slotY * 2 + 23, Formatting.WHITE.getColorValue());
                }
            }
            if (mouseX >= slotX - 1 && mouseX <= slotX + 16 && mouseY >= slotY - 1 && mouseY <= slotY + 16) {
                fillGradient(matrices, slotX, slotY, slotX + 16, slotY + 16, -2130706433, -2130706433, 200);
                hoveredSlot = i;
                hoveredInventory = HoveredInventory.MINECARTS;
            }
        }
    }

    private void drawPlayerTooltips(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        for (int i = 0; i < 36; i++) {
            ItemStack inventoryItem = visiblePlayerItems.get(i);
            int slotX = this.guiX + 8 + 18*(i % 9);
            int slotY;
            if (i < 27)
                slotY = this.guiY + (rowCount * 18) + 36 + (18*(i / 9));
            else
                slotY = this.guiY + rowCount * 18 + 94;
            if (showCraftingTable && craftingTableLocation != null) slotY += 56;
            if (mouseX >= slotX - 1 && mouseX <= slotX + 16 && mouseY >= slotY - 1 && mouseY <=  slotY + 16) {
                if (mouseStack == ItemStack.EMPTY && inventoryItem != ItemStack.EMPTY) {
                    renderTooltip(matrices, inventoryItem, mouseX, mouseY);
                }
            }
        }
    }

    private void drawMinecartTooltips(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        for (int i = 0; i < rowCount * 9; i++) {
            int slotX = this.guiX + 8 + 18 * (i % 9);
            int slotY = this.guiY + 20 + 18 * (i / 9);
            if ((i + rowsScrolled*9) < searchedVisibleItems.size()) {
                ItemStack inventoryItem = searchedVisibleItems.get(i + rowsScrolled * 9).visualItemStack;
                if (mouseX >= slotX - 1 && mouseX <= slotX + 16 && mouseY >= slotY - 1 && mouseY <= slotY + 16) {
                    if (mouseStack == ItemStack.EMPTY && inventoryItem != ItemStack.EMPTY) {
                        renderTooltip(matrices, inventoryItem, mouseX, mouseY);
                    }
                }
            }
        }
    }


    private void drawMouseStack(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        if (draggingItems && draggedStackRemainder < 1) return;
        this.itemRenderer.zOffset = 200.0F;
        itemRenderer.renderInGuiWithOverrides(mouseStack, (mouseX - 8), (mouseY - 8));
        itemRenderer.renderGuiItemOverlay(this.textRenderer, mouseStack, (mouseX - 8), (mouseY - 8), mouseStack.getCount() == 1 ? "" : draggingItems ? (draggedStackRemainder == 1 ? "" : String.valueOf(draggedStackRemainder)) : String.valueOf(mouseStack.getCount()));
        this.itemRenderer.zOffset = 0.0F;
    }

    private boolean isMouseOver(double mouseX, double mouseY, int x1, int y1, int x2, int y2) {
        return mouseX >= (double)x1 && mouseY >= (double)y1 && mouseX < (double)x2 && mouseY < (double)y2;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isMouseOver(mouseX, mouseY, this.guiX + 174, this.guiY + 20, this.guiX + 186, this.guiY + 18 + rowCount * 18) &&
            rowCount < Math.ceil(searchedVisibleItems.size()/9F) || scrolling) {
            int y1 = this.guiY + 20;
            int y2 = y1 + (rowCount) * 18;

            this.scrollPosition = ((float)mouseY - (float)y1 - 7.5F) / ((float)(y2 - y1) - 15F);
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);

            rowsScrolled = Math.round(scrollPosition * (float)(Math.ceil(searchedVisibleItems.size()/9F) - rowCount));
            scrolling = true;
            return true;
        } else if (hoveredInventory.equals(HoveredInventory.PLAYER)) {
            if (hoveredSlot > -1
                && button < 3
                && !mouseStack.isEmpty()
                && (mouseStack.getCount() > this.draggingItemSlots.size())
                && (visiblePlayerItems.get(hoveredSlot).getItem() == Items.AIR
                || ItemStack.canCombine(mouseStack, visiblePlayerItems.get(hoveredSlot)))
                && visiblePlayerItems.get(hoveredSlot).getCount() < visiblePlayerItems.get(hoveredSlot).getMaxCount()) {
                if (draggingItems) {
                    draggingItemSlots.add(hoveredSlot);
                    calculateSlots();
                } else if (!beforeDragDragging) {
                    beforeDragDragging = true;
                    draggingStartingSlot = hoveredSlot;
                } else if (draggingStartingSlot != hoveredSlot){
                    draggingItems = true;
                    draggingItemSlots.add(draggingStartingSlot);
                    draggingItemsButton = button;
                }
            }
            System.out.println("draggingItems = " + draggingItems);
            System.out.println("beforeDragDragging = " + beforeDragDragging);
        }
        return false;
    }

    public void calculateSlots() {
        if (draggingItemsButton == 1) {
            draggedStackRemainder = mouseStack.getCount() - draggingItemSlots.size();
            draggingItemsPerSlot.clear();
            draggingItemsPerSlot.addAll(Collections.nCopies(36, 1));
        } else {
            int amountDivided = mouseStack.getCount();
            int itemsPerSlot = amountDivided / draggingItemSlots.size();
            int maxedSlots = 0;

            for (int slot : draggingItemSlots) {
                final ItemStack itemStack = visiblePlayerItems.get(slot);
                if (itemStack.getCount() + itemsPerSlot > itemStack.getMaxCount()) {
                    maxedSlots++;
                    amountDivided -= itemsPerSlot - itemStack.getCount();
                    itemsPerSlot = (amountDivided) / (draggingItemsPerSlot.size() - maxedSlots);
                }
            }

            draggingItemsPerSlot.clear();
            draggingItemsPerSlot.addAll(Collections.nCopies(36, 0));
            for (int slot : draggingItemSlots) {
                draggingItemsPerSlot.set(slot, itemsPerSlot);
            }
            draggedStackRemainder = amountDivided % draggingItemSlots.size() - maxedSlots;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (rowCount < Math.ceil(searchedVisibleItems.size()/9F)) {
            scrollPosition -= (float) amount / ((float) (Math.ceil(searchedVisibleItems.size() / 9F) - rowCount));
            scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
            rowsScrolled = Math.round(scrollPosition * (float) (Math.ceil(searchedVisibleItems.size() / 9F) - rowCount));
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        scrolling = false;
        if (draggingItems && !draggingItemSlots.isEmpty()) {
//            this.client.interactionManager.clickSlot(0, -999, ScreenHandler.packQuickCraftData(0, draggingItemsButton), SlotActionType.QUICK_CRAFT, this.client.player); // Signals that the drag has begun

            int itemsPerSlot = 1;
            if (draggingItemsButton == 0) itemsPerSlot = mouseStack.getCount() / draggingItemSlots.size();

            for(int slot : draggingItemSlots) {
//                this.client.interactionManager.clickSlot(0, slot, ScreenHandler.packQuickCraftData(1, draggingItemsButton), SlotActionType.QUICK_CRAFT, this.client.player); // one event per slot dragged on
                ItemStack stack = mouseStack.copy();
                stack.setCount(itemsPerSlot + visiblePlayerItems.get(slot).getCount());
                visiblePlayerItems.set(slot, stack);
            }

            mouseStack.setCount(draggedStackRemainder);
            draggingItems = false;
            beforeDragDragging = false;
            draggingItemSlots.clear();
            return true;

//            this.client.interactionManager.clickSlot(0, -999, ScreenHandler.packQuickCraftData(2, draggingItemsButton), SlotActionType.QUICK_CRAFT, this.client.player); // signals that the drag has ended
        }else {
            boolean isDoubleClicking = checkForDoubleClick(button);
            if (!checkButtons(mouseX, mouseY))
                checkItems(mouseX, mouseY, button, isDoubleClicking);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean checkForDoubleClick(int button) {
        long currentTime = Util.getMeasuringTimeMs();
        boolean isDoubleClicking = (hoveredSlot == lastClickedSlot && hoveredInventory == lastClickedInventory && lastClickedButton == button && currentTime - lastButtonClickTime < 250L);
        if (isDoubleClicking) {
            System.out.println("CONGRATULATIONS! YOU JUST DOUBLE CLICKED!");
        }
        lastClickedSlot = hoveredSlot;
        lastClickedInventory = hoveredInventory;
        lastClickedButton = button;
        lastButtonClickTime = currentTime;
        return isDoubleClicking;
    }

    public boolean checkButtons(double mouseX, double mouseY) {
        if (isMouseOver(mouseX, mouseY, guiX - 18, guiY + 8, guiX + 2, guiY + 80)) {
            for (int i = 0; i < 4; i++) {
                int x = guiX - 18;
                int y = guiY + 8 + (i * 18);

                if (isMouseOver(mouseX, mouseY, x, y, guiX - 2, guiY + 24 + (i * 18))) {
                    client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
                            search();
                        }
                        case 2 -> { // sort filter
                            sortFilter = sortFilter.other();
                            if (sortFilter == SortFilter.ALPHABETICALLY) {
                                items.sort(nameComparator);
                            } else {
                                items.sort(quantityComparator);
                            }
                            search();
                        }
                        case 3 -> { // crafting table
                            showCraftingTable = !showCraftingTable;
                            init();
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void checkItems(double mouseX, double mouseY, int button, boolean isDoubleClicking) {
        if (hoveredSlot > -1) {
            int y = (this.guiY + rowCount * 18 + 24 + (showCraftingTable && craftingTableLocation != null ? 56 : 0));
            if (isMouseOver(mouseX, mouseY, this.guiX, y, this.guiX + 176, y + 88)) { // Handle Player Inventory
                if (hasShiftDown()) {
                    if (button == 0) {
                        if (isDoubleClicking && !mouseStack.equals(ItemStack.EMPTY)) {
                            //Move all away
                            client.player.sendMessage(new LiteralText("move all away"), false);
                            if (!lastClickedItem.isEmpty()) {
                                int increasingItemIndex = getVirtualItemStackForItem(lastClickedItem);
                                if (increasingItemIndex == -1) {
                                    visibleItems.add(new VirtualItemStack(lastClickedItem.copy(), 0, new ArrayList<VirtualItemStack.ItemMinecart>()));
                                    increasingItemIndex = visibleItems.size() - 1;
                                }
                                VirtualItemStack increasingItem = visibleItems.get(increasingItemIndex);
                                for (int i = 0; i < 36; i++) {
                                    if (ItemStack.canCombine(lastClickedItem, visiblePlayerItems.get(i))) {
                                        increasingItem.amount = increasingItem.amount + visiblePlayerItems.get(i).getCount();
                                        visiblePlayerItems.set(i, ItemStack.EMPTY);
                                    }
                                }
                                if (sortFilter == SortFilter.ALPHABETICALLY) {
                                    visibleItems.sort(nameComparator);
                                } else {
                                    visibleItems.sort(quantityComparator);
                                }
                                search();
                            }
                        } else {
                            //Quick Move hovered Item stack
                            client.player.sendMessage(new LiteralText("quick move"), false);
                            lastClickedItem = visiblePlayerItems.get(hoveredSlot);
                            if (!visiblePlayerItems.get(hoveredSlot).isEmpty()) {
                                int increasingItemIndex = getVirtualItemStackForItem(visiblePlayerItems.get(hoveredSlot));
                                if (increasingItemIndex == -1) {
                                    visibleItems.add(new VirtualItemStack(visiblePlayerItems.get(hoveredSlot).copy(), 0, new ArrayList<VirtualItemStack.ItemMinecart>()));
                                    increasingItemIndex = visibleItems.size() - 1;
                                }
                                VirtualItemStack increasingItem = visibleItems.get(increasingItemIndex);
                                increasingItem.amount = increasingItem.amount + visiblePlayerItems.get(hoveredSlot).getCount();
                                visiblePlayerItems.set(hoveredSlot, ItemStack.EMPTY);
                                if (sortFilter == SortFilter.ALPHABETICALLY) {
                                    visibleItems.sort(nameComparator);
                                } else {
                                    visibleItems.sort(quantityComparator);
                                }
                                search();
                            }
                        }
                    } else if (button == 1) {
                        //Quick Move hovered Item stack
                        client.player.sendMessage(new LiteralText("quick move"), false);
                        if (!visiblePlayerItems.get(hoveredSlot).isEmpty()) {
                            int increasingItemIndex = getVirtualItemStackForItem(visiblePlayerItems.get(hoveredSlot));
                            if (increasingItemIndex == -1) {
                                visibleItems.add(new VirtualItemStack(visiblePlayerItems.get(hoveredSlot).copy(), 0, new ArrayList<VirtualItemStack.ItemMinecart>()));
                                increasingItemIndex = visibleItems.size() - 1;
                            }
                            VirtualItemStack increasingItem = visibleItems.get(increasingItemIndex);
                            increasingItem.amount = increasingItem.amount + visiblePlayerItems.get(hoveredSlot).getCount();
                            visiblePlayerItems.set(hoveredSlot, ItemStack.EMPTY);
                            if (sortFilter == SortFilter.ALPHABETICALLY) {
                                visibleItems.sort(nameComparator);
                            } else {
                                visibleItems.sort(quantityComparator);
                            }
                            search();
                        }
                    }
                } else {
                    if (mouseStack.isEmpty()) {
                        if (button == 0) {
                            //Pickup all
                            client.player.sendMessage(new LiteralText("pickup all"), false);
                            mouseStack = visiblePlayerItems.get(hoveredSlot);
                            visiblePlayerItems.set(hoveredSlot, ItemStack.EMPTY);
                        } else if (button == 1) {
                            //pickup half
                            client.player.sendMessage(new LiteralText("pickup half"), false);
                            mouseStack = visiblePlayerItems.get(hoveredSlot).copy();
                            mouseStack.setCount((int) Math.ceil(visiblePlayerItems.get(hoveredSlot).getCount() / 2F));
                            visiblePlayerItems.get(hoveredSlot).setCount(visiblePlayerItems.get(hoveredSlot).getCount() / 2);
                        }
                    } else {
                        if (button == 0) {
                            if (isDoubleClicking) {
                                //move all towards
                                client.player.sendMessage(new LiteralText("move all towards"), false);
                                for (int i = 0; i < 36; i++) {
                                    if (ItemStack.canCombine(mouseStack, visiblePlayerItems.get(i)) && visiblePlayerItems.get(i).getCount() < visiblePlayerItems.get(i).getMaxCount()) {
                                        if (mouseStack.getMaxCount() <= mouseStack.getCount() + visiblePlayerItems.get(i).getCount()) {
                                            visiblePlayerItems.get(i).setCount(visiblePlayerItems.get(i).getCount() - (mouseStack.getMaxCount() - mouseStack.getCount()));
                                            mouseStack.setCount(mouseStack.getMaxCount());
                                            break;
                                        }
                                        mouseStack.setCount(mouseStack.getCount() + visiblePlayerItems.get(i).getCount());
                                        visiblePlayerItems.set(i, ItemStack.EMPTY);
                                    }
                                }
                                if (mouseStack.getCount() < mouseStack.getMaxCount()) {
                                    for (int i = 0; i < 36; i++) {
                                        if (ItemStack.canCombine(mouseStack, visiblePlayerItems.get(i))) {
                                            visiblePlayerItems.get(i).setCount(visiblePlayerItems.get(i).getCount() - (mouseStack.getMaxCount() - mouseStack.getCount()));
                                            mouseStack.setCount(mouseStack.getMaxCount());
                                            break;
                                        }
                                    }
                                }
                                if (mouseStack.getCount() < mouseStack.getMaxCount()) {
                                    int decreasingItemIndex = getVirtualItemStackForItem(mouseStack);
                                    if (decreasingItemIndex != -1) {
                                        VirtualItemStack decreasingItem = visibleItems.get(decreasingItemIndex);
                                        if (decreasingItem.amount > mouseStack.getMaxCount() - mouseStack.getMaxCount()) {
                                            decreasingItem.amount = decreasingItem.amount - mouseStack.getMaxCount() - mouseStack.getMaxCount();
                                            mouseStack.setCount(mouseStack.getMaxCount());
                                        } else {
                                            mouseStack.setCount(mouseStack.getCount() + decreasingItem.amount);
                                            visibleItems.remove(decreasingItemIndex);
                                        }
                                        if (sortFilter == SortFilter.ALPHABETICALLY) {
                                            visibleItems.sort(nameComparator);
                                        } else {
                                            visibleItems.sort(quantityComparator);
                                        }
                                        search();
                                    }
                                }
                            } else {
                                if (visiblePlayerItems.get(hoveredSlot).isEmpty()) {
                                    //place all
                                    client.player.sendMessage(new LiteralText("place all"), false);
                                    visiblePlayerItems.set(hoveredSlot, mouseStack);
                                    mouseStack = ItemStack.EMPTY;
                                } else if (ItemStack.canCombine(visiblePlayerItems.get(hoveredSlot), mouseStack)) {
                                    //place all
                                    client.player.sendMessage(new LiteralText("place all"), false);
                                    if (visiblePlayerItems.get(hoveredSlot).getMaxCount() < visiblePlayerItems.get(hoveredSlot).getCount() + mouseStack.getCount()) {
                                        mouseStack.setCount(visiblePlayerItems.get(hoveredSlot).getCount() + mouseStack.getCount() - visiblePlayerItems.get(hoveredSlot).getMaxCount());
                                        visiblePlayerItems.get(hoveredSlot).setCount(visiblePlayerItems.get(hoveredSlot).getMaxCount());
                                    } else {
                                        visiblePlayerItems.get(hoveredSlot).setCount(visiblePlayerItems.get(hoveredSlot).getCount() + mouseStack.getCount());
                                        mouseStack = ItemStack.EMPTY;
                                    }
                                } else {
                                    //swap
                                    client.player.sendMessage(new LiteralText("swap"), false);
                                    ItemStack previousMouseStack = mouseStack.copy();
                                    mouseStack = visiblePlayerItems.get(hoveredSlot);
                                    visiblePlayerItems.set(hoveredSlot, previousMouseStack);
                                }
                            }
                        } else if (button == 1) {
                            if (visiblePlayerItems.get(hoveredSlot).isEmpty()) {
                                //place one
                                client.player.sendMessage(new LiteralText("place one"), false);
                                visiblePlayerItems.set(hoveredSlot, mouseStack.copy());
                                visiblePlayerItems.get(hoveredSlot).setCount(1);
                                mouseStack.decrement(1);
                                if (mouseStack.getCount() == 0) {
                                    mouseStack = ItemStack.EMPTY;
                                }
                            } else if (ItemStack.canCombine(visiblePlayerItems.get(hoveredSlot), mouseStack)) {
                                //place one
                                client.player.sendMessage(new LiteralText("place one"), false);
                                if (visiblePlayerItems.get(hoveredSlot).getMaxCount() > visiblePlayerItems.get(hoveredSlot).getCount()) {
                                    visiblePlayerItems.get(hoveredSlot).increment(1);
                                    mouseStack.decrement(1);
                                }
                                if (mouseStack.getCount() == 0) {
                                    mouseStack = ItemStack.EMPTY;
                                }
                            } else {
                                //swap
                                client.player.sendMessage(new LiteralText("swap"), false);
                                ItemStack previousMouseStack = mouseStack.copy();
                                mouseStack = visiblePlayerItems.get(hoveredSlot);
                                visiblePlayerItems.set(hoveredSlot, previousMouseStack);
                            }
                        }
                    }
                }
            } else if (isMouseOver(mouseX, mouseY, guiX, guiY, guiX + 172, guiY + 24 + rowCount * 18)) { // Handle Minecart Inventory
                if (hasShiftDown()) {
                    if (button == 0) {
                        if (isDoubleClicking && !mouseStack.equals(ItemStack.EMPTY)) {
                            //Move all away
                            client.player.sendMessage(new LiteralText("move all away"), false);
                            if (searchedVisibleItems.size() > hoveredSlot + rowsScrolled * 9 && searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount > 0) {
                                int decreasingItemIndex = getVirtualItemStackForItem(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack);
                                VirtualItemStack decreasingItem = visibleItems.get(decreasingItemIndex);
                                for (int i = 0; i < 36; i++) {
                                    int playerInventorySlot = 35 - i;
                                    if (visiblePlayerItems.get(playerInventorySlot).isEmpty()) {
                                        if (decreasingItem.amount > decreasingItem.visualItemStack.getMaxCount()) {
                                            ItemStack newItemStack = decreasingItem.visualItemStack.copy();
                                            newItemStack.setCount(newItemStack.getMaxCount());
                                            decreasingItem.amount = decreasingItem.amount - decreasingItem.visualItemStack.getMaxCount();
                                            visiblePlayerItems.set(playerInventorySlot, newItemStack);
                                        } else {
                                            ItemStack newItemStack = decreasingItem.visualItemStack.copy();
                                            newItemStack.setCount(decreasingItem.amount);
                                            decreasingItem.amount = 0;
                                            visibleItems.remove(decreasingItemIndex);
                                            visiblePlayerItems.set(playerInventorySlot, newItemStack);
                                            break;
                                        }
                                    } else if (ItemStack.canCombine(decreasingItem.visualItemStack, visiblePlayerItems.get(playerInventorySlot))) {
                                        if (decreasingItem.amount > visiblePlayerItems.get(playerInventorySlot).getMaxCount() - visiblePlayerItems.get(playerInventorySlot).getCount()) {
                                            decreasingItem.amount = decreasingItem.amount - (visiblePlayerItems.get(playerInventorySlot).getMaxCount() - visiblePlayerItems.get(playerInventorySlot).getCount());
                                            visiblePlayerItems.get(playerInventorySlot).setCount(visiblePlayerItems.get(playerInventorySlot).getMaxCount());
                                        } else {
                                            visiblePlayerItems.get(playerInventorySlot).setCount(visiblePlayerItems.get(playerInventorySlot).getCount() + decreasingItem.amount);
                                            decreasingItem.amount = 0;
                                            visibleItems.remove(decreasingItemIndex);
                                            break;
                                        }
                                    }
                                }
                                searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = decreasingItem.amount;
                            }
                        } else {
                            //Quick Move hovered Item stack
                            client.player.sendMessage(new LiteralText("quick move"), false);
                            if (searchedVisibleItems.size() > hoveredSlot + rowsScrolled * 9 && searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount > 0) {
                                int decreasingItemIndex = getVirtualItemStackForItem(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack);
                                VirtualItemStack decreasingItem = visibleItems.get(decreasingItemIndex);
                                int itemsToTransfer = decreasingItem.visualItemStack.getMaxCount();
                                for (int i = 0; i < 36; i++) {
                                    int playerInventorySlot = 35 - i;
                                    if (visiblePlayerItems.get(playerInventorySlot).isEmpty()) {
                                        if (decreasingItem.amount > decreasingItem.visualItemStack.getMaxCount()) {
                                            ItemStack newItemStack = decreasingItem.visualItemStack.copy();
                                            newItemStack.setCount(itemsToTransfer);
                                            decreasingItem.amount = decreasingItem.amount - decreasingItem.visualItemStack.getMaxCount();
                                            visiblePlayerItems.set(playerInventorySlot, newItemStack);
                                            searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = decreasingItem.amount;
                                            break;
                                        } else {
                                            ItemStack newItemStack = decreasingItem.visualItemStack.copy();
                                            newItemStack.setCount(decreasingItem.amount);
                                            System.out.println(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount);
                                            searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = 0;
                                            System.out.println(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount);
                                            visibleItems.remove(decreasingItemIndex);
                                            visiblePlayerItems.set(playerInventorySlot, newItemStack);
                                            break;
                                        }
                                    } else if (ItemStack.canCombine(decreasingItem.visualItemStack, visiblePlayerItems.get(playerInventorySlot))) {
                                        if (decreasingItem.amount > Math.min(itemsToTransfer, visiblePlayerItems.get(playerInventorySlot).getMaxCount() - visiblePlayerItems.get(playerInventorySlot).getCount())) {
                                            decreasingItem.amount = decreasingItem.amount - (visiblePlayerItems.get(playerInventorySlot).getMaxCount() - visiblePlayerItems.get(playerInventorySlot).getCount());
                                            itemsToTransfer = itemsToTransfer - Math.min(itemsToTransfer, visiblePlayerItems.get(playerInventorySlot).getMaxCount() - visiblePlayerItems.get(playerInventorySlot).getCount());
                                            visiblePlayerItems.get(playerInventorySlot).setCount(visiblePlayerItems.get(playerInventorySlot).getMaxCount());
                                            System.out.println(itemsToTransfer);
                                        } else {
                                            visiblePlayerItems.get(playerInventorySlot).setCount(visiblePlayerItems.get(playerInventorySlot).getCount() + decreasingItem.amount);
                                            searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = 0;
                                            visibleItems.remove(decreasingItemIndex);
                                            break;
                                        }
                                    }
                                    if (itemsToTransfer == 0) {
                                        searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = decreasingItem.amount;
                                        break;
                                    }
                                }
                            }
                        }
                    } else if (button == 1) {
                        //Quick Move hovered Item stack
                        client.player.sendMessage(new LiteralText("quick move"), false);
                        if (searchedVisibleItems.size() > hoveredSlot + rowsScrolled * 9 && searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount > 0) {
                            int decreasingItemIndex = getVirtualItemStackForItem(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack);
                            VirtualItemStack decreasingItem = visibleItems.get(decreasingItemIndex);
                            int itemsToTransfer = decreasingItem.visualItemStack.getMaxCount();
                            for (int i = 0; i < 36; i++) {
                                int playerInventorySlot = 35 - i;
                                if (visiblePlayerItems.get(playerInventorySlot).isEmpty()) {
                                    if (decreasingItem.amount > decreasingItem.visualItemStack.getMaxCount()) {
                                        ItemStack newItemStack = decreasingItem.visualItemStack.copy();
                                        newItemStack.setCount(itemsToTransfer);
                                        decreasingItem.amount = decreasingItem.amount - decreasingItem.visualItemStack.getMaxCount();
                                        visiblePlayerItems.set(playerInventorySlot, newItemStack);
                                        searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = decreasingItem.amount;
                                        break;
                                    } else {
                                        ItemStack newItemStack = decreasingItem.visualItemStack.copy();
                                        newItemStack.setCount(decreasingItem.amount);
                                        System.out.println(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount);
                                        searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = 0;
                                        System.out.println(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount);
                                        visibleItems.remove(decreasingItemIndex);
                                        visiblePlayerItems.set(playerInventorySlot, newItemStack);
                                        break;
                                    }
                                } else if (ItemStack.canCombine(decreasingItem.visualItemStack, visiblePlayerItems.get(playerInventorySlot))) {
                                    if (decreasingItem.amount > Math.min(itemsToTransfer, visiblePlayerItems.get(playerInventorySlot).getMaxCount() - visiblePlayerItems.get(playerInventorySlot).getCount())) {
                                        decreasingItem.amount = decreasingItem.amount - (visiblePlayerItems.get(playerInventorySlot).getMaxCount() - visiblePlayerItems.get(playerInventorySlot).getCount());
                                        itemsToTransfer = itemsToTransfer - Math.min(itemsToTransfer, visiblePlayerItems.get(playerInventorySlot).getMaxCount() - visiblePlayerItems.get(playerInventorySlot).getCount());
                                        visiblePlayerItems.get(playerInventorySlot).setCount(visiblePlayerItems.get(playerInventorySlot).getMaxCount());
                                        System.out.println(itemsToTransfer);
                                    } else {
                                        visiblePlayerItems.get(playerInventorySlot).setCount(visiblePlayerItems.get(playerInventorySlot).getCount() + decreasingItem.amount);
                                        searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = 0;
                                        visibleItems.remove(decreasingItemIndex);
                                        break;
                                    }
                                }
                                if (itemsToTransfer == 0) {
                                    searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = decreasingItem.amount;
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    if (mouseStack.isEmpty()) {
                        if (button == 0) {
                            //Pickup all
                            client.player.sendMessage(new LiteralText("pickup all"), false);
                            if (searchedVisibleItems.size() > hoveredSlot + rowsScrolled * 9) {
                                int decreasingItemIndex = getVirtualItemStackForItem(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack);
                                VirtualItemStack decreasingItem = visibleItems.get(decreasingItemIndex);
                                if (decreasingItem.amount > decreasingItem.visualItemStack.getMaxCount()) {
                                    ItemStack newMouseStack = decreasingItem.visualItemStack.copy();
                                    newMouseStack.setCount(newMouseStack.getMaxCount());
                                    mouseStack = newMouseStack;
                                    decreasingItem.amount = decreasingItem.amount - decreasingItem.visualItemStack.getMaxCount();
                                } else {
                                    ItemStack newMouseStack = searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack.copy();
                                    newMouseStack.setCount(decreasingItem.amount);
                                    mouseStack = newMouseStack;
                                    visibleItems.remove(decreasingItemIndex);
                                }
                                if (sortFilter == SortFilter.ALPHABETICALLY) {
                                    visibleItems.sort(nameComparator);
                                } else {
                                    visibleItems.sort(quantityComparator);
                                }
                                search();
                            }
                        } else if (button == 1) {
                            //pickup half
                            client.player.sendMessage(new LiteralText("pickup half"), false);
                            if (searchedVisibleItems.size() > hoveredSlot + rowsScrolled * 9) {
                                int decreasingItemIndex = getVirtualItemStackForItem(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack);
                                VirtualItemStack decreasingItem = visibleItems.get(decreasingItemIndex);
                                if (decreasingItem.amount > decreasingItem.visualItemStack.getMaxCount()) {
                                    ItemStack newMouseStack = decreasingItem.visualItemStack.copy();
                                    newMouseStack.setCount((int) Math.ceil(newMouseStack.getMaxCount() / 2F));
                                    mouseStack = newMouseStack;
                                    decreasingItem.amount = decreasingItem.amount - decreasingItem.visualItemStack.getMaxCount() / 2;
                                } else {
                                    ItemStack newMouseStack = searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack.copy();
                                    newMouseStack.setCount((int) Math.ceil(decreasingItem.amount / 2F));
                                    mouseStack = newMouseStack;
                                    if (decreasingItem.amount > 1) {
                                        decreasingItem.amount = decreasingItem.amount / 2;
                                    } else {
                                        visibleItems.remove(decreasingItemIndex);
                                    }
                                }
                                if (sortFilter == SortFilter.ALPHABETICALLY) {
                                    visibleItems.sort(nameComparator);
                                } else {
                                    visibleItems.sort(quantityComparator);
                                }
                                search();
                            }
                        }
                    } else {
                        if (button == 0) {
                            if (isDoubleClicking) {
                                //move all towards
                                client.player.sendMessage(new LiteralText("move all towards"), false);
                                int increasingItemIndex = getVirtualItemStackForItem(mouseStack);
                                if (increasingItemIndex == -1) {
                                    visibleItems.add(new VirtualItemStack(mouseStack.copy(), 0, new ArrayList<VirtualItemStack.ItemMinecart>()));
                                    increasingItemIndex = visibleItems.size() - 1;
                                }
                                VirtualItemStack increasingItem = visibleItems.get(increasingItemIndex);
                                for (int i = 0; i < 36; i++) {
                                    if (ItemStack.canCombine(mouseStack, visiblePlayerItems.get(i))) {
                                        if (mouseStack.getMaxCount() <= mouseStack.getCount() + visiblePlayerItems.get(i).getCount()) {
                                            increasingItem.amount = increasingItem.amount + visiblePlayerItems.get(i).getCount() - (mouseStack.getMaxCount() - mouseStack.getCount());
                                            mouseStack.setCount(mouseStack.getMaxCount());
                                            visiblePlayerItems.set(i, ItemStack.EMPTY);
                                        }
                                        mouseStack.setCount(mouseStack.getCount() + visiblePlayerItems.get(i).getCount());
                                        visiblePlayerItems.set(i, ItemStack.EMPTY);
                                    }
                                }
                                if (increasingItem.amount == 0) {
                                    visibleItems.remove(increasingItemIndex);
                                } else {
                                    if (sortFilter == SortFilter.ALPHABETICALLY) {
                                        visibleItems.sort(nameComparator);
                                    } else {
                                        visibleItems.sort(quantityComparator);
                                    }
                                    search();
                                }
                            } else {
                                //place all
                                client.player.sendMessage(new LiteralText("place all"), false);
                                int increasingItemIndex = getVirtualItemStackForItem(mouseStack);
                                if (increasingItemIndex == -1) {
                                    visibleItems.add(new VirtualItemStack(mouseStack.copy(), 0, new ArrayList<VirtualItemStack.ItemMinecart>()));
                                    increasingItemIndex = visibleItems.size() - 1;
                                }
                                VirtualItemStack increasingItem = visibleItems.get(increasingItemIndex);
                                increasingItem.amount = increasingItem.amount + mouseStack.getCount();
                                mouseStack = ItemStack.EMPTY;
                                if (sortFilter == SortFilter.ALPHABETICALLY) {
                                    visibleItems.sort(nameComparator);
                                } else {
                                    visibleItems.sort(quantityComparator);
                                }
                                search();
                            }
                        } else if (button == 1) {
                            //place one
                            client.player.sendMessage(new LiteralText("place one"), false);
                            int increasingItemIndex = getVirtualItemStackForItem(mouseStack);
                            if (increasingItemIndex == -1) {
                                visibleItems.add(new VirtualItemStack(mouseStack.copy(), 0, new ArrayList<VirtualItemStack.ItemMinecart>()));
                                increasingItemIndex = visibleItems.size() - 1;
                            }
                            VirtualItemStack increasingItem = visibleItems.get(increasingItemIndex);
                            increasingItem.amount = increasingItem.amount + 1;
                            mouseStack.decrement(1);
                            if (mouseStack.getCount() == 0) {
                                mouseStack = ItemStack.EMPTY;
                            }
                            if (sortFilter == SortFilter.ALPHABETICALLY) {
                                visibleItems.sort(nameComparator);
                            } else {
                                visibleItems.sort(quantityComparator);
                            }
                            search();
                        }
                    }
                }
            }
        }
    }

    public boolean charTyped(char chr, int modifiers) {
        String string = this.searchBox.getText();
        if(this.searchBox.charTyped(chr, modifiers)) {
            if (!Objects.equals(string, this.searchBox.getText())) {
                search();
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        String string = this.searchBox.getText();
        if(this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            if (!Objects.equals(string, this.searchBox.getText())) {
                search();
            }
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 340 || keyCode == 344) {
            if (sortFilter == SortFilter.ALPHABETICALLY) {
                visibleItems.sort(nameComparator);
            } else {
                visibleItems.sort(quantityComparator);
            }
            search();
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private String abbreviateAmount(int amount) {
        if (amount > 999999999) {
            return Math.round(amount/100000000F)/10F + "B";
        } else if (amount > 99999999) {
            return Math.round(amount/1000000F) + "M";
        } else if (amount > 999999) {
            return Math.round(amount/100000F)/10F + "M";
        } else if (amount > 99999) {
            return Math.round(amount/1000F)+ "K";
        } else if (amount > 9999) {
            return Math.round(amount/100F)/10F + "K";
        } else {
            return amount == 1 ? "" : Integer.toString(amount);
        }
    }

    private void search() {
        searchedVisibleItems.clear();
        for (VirtualItemStack testedStack : visibleItems) {
            if (searchBox.getText() == "") {
                searchedVisibleItems.add(testedStack.copy());
            } else if (Character.toString( searchBox.getText().charAt(0)) == "@") {
                searchedVisibleItems.add(testedStack.copy());
            } else {
                if (testedStack.visualItemStack.getName().getString().toLowerCase().matches(".*\\Q" + searchBox.getText().toLowerCase() + "\\E.*")) {
                    searchedVisibleItems.add(testedStack.copy());
                }
            }
        }
        if (rowCount < Math.ceil(searchedVisibleItems.size()/9F)) {
            rowsScrolled = Math.round(scrollPosition * (float) (Math.ceil(searchedVisibleItems.size() / 9F) - rowCount));
        } else {
            scrollPosition = 0;
            rowsScrolled = 0;
        }
    }

    @Override
    protected void init() {
        super.init();

        craftingTableLocation = null;
        for (int i = -5; i < 5; i++) {
            for (int j = -5; j < 5; j++) {
                for (int k = -5; k < 5; k++) {
                    if (client.player.getWorld().getBlockState(client.player.getBlockPos().add(i, j, k)).getBlock() instanceof CraftingTableBlock) {
                        craftingTableLocation = client.player.getBlockPos().add(i, j, k);
                        System.out.println("Found Crafting Table");
                        break;
                    }
                }
                if (craftingTableLocation != null) break;
            }
            if (craftingTableLocation != null) break;
        }

        rowCount = (this.height - 220 - (craftingTableLocation != null || showCraftingTable ? 20 : 0)) / 18 + 3;
        this.guiY = (this.height - (craftingTableLocation == null || !showCraftingTable ? this.backgroundHeight - 56 : this.backgroundHeight) - (rowCount - 3) * 18) / 2;
        this.guiX = (this.width - this.backgroundWidth + 17) / 2;

        for (int i = 0; i < 36; i++) {
            visiblePlayerItems.add(ItemStack.EMPTY);
        }

        client.keyboard.setRepeatEvents(true);
        TextRenderer textRenderer = this.textRenderer;
        this.searchBox = new TextFieldWidget(textRenderer, this.guiX + 82, this.guiY + 7, 80, 9, new TranslatableText("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setDrawsBackground(false);
        this.searchBox.setVisible(true);
        this.searchBox.setEditableColor(16777215);
        this.addSelectableChild(this.searchBox);

        if (rowCount < Math.ceil(searchedVisibleItems.size()/9F)) {
            rowsScrolled = Math.round(scrollPosition * (float) (Math.ceil(searchedVisibleItems.size() / 9F) - rowCount));
        } else {
            scrollPosition = 0;
            rowsScrolled = 0;
        }
    }

    private int getVirtualItemStackForItem(ItemStack itemstack) {
        for (int i = 0; i < visibleItems.size(); i++) {
            if (ItemStack.canCombine(visibleItems.get(i).visualItemStack, itemstack)) {
                return i;
            }
        }
        return -1;
    }

    public void setItems(ChestMinecartEntity minecart, ItemStack itemstack, int slot) {
        boolean newItem = true;
        for (int i = 0; i < items.size(); i++) {
            VirtualItemStack virtualItemStack = items.get(i);
            if (ItemStack.canCombine(virtualItemStack.visualItemStack, itemstack)) {
                newItem = false;
                virtualItemStack.setItems(minecart, slot, itemstack.getCount());
                if (virtualItemStack.amount < 1) {
                    items.remove(i);
                } else {
                    if (sortFilter == SortFilter.ALPHABETICALLY) {
                        items.sort(nameComparator);
                    } else {
                        items.sort(quantityComparator);
                    }
                    search();
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
            search();
        }
        visibleItems.clear();
        visibleItems.addAll(items);
        search();
    }

    public void setItems(ChestMinecartEntity minecart, VirtualItemStack virtualItemStack, int newAmount, int slot) {
        for (int i = 0; i < items.size(); i++) {
            VirtualItemStack currentVirtualItemStack = items.get(i);
            if (ItemStack.canCombine(currentVirtualItemStack.visualItemStack, virtualItemStack.visualItemStack)) {
                currentVirtualItemStack.setItems(minecart, slot, newAmount);
                if (currentVirtualItemStack.amount < 1) {
                    items.remove(i);
                } else {
                    if (sortFilter == SortFilter.ALPHABETICALLY) {
                        items.sort(nameComparator);
                    } else {
                        items.sort(quantityComparator);
                    }
                    search();
                }
            }
        }
        visibleItems.clear();
        visibleItems.addAll(items);
        search();
    }

    private final Comparator<VirtualItemStack> quantityComparator = (virtualItemStack1, virtualItemStack2) -> {
        int difference = virtualItemStack2.amount - virtualItemStack1.amount;
        if (difference > 0) {
            return sortDirection.equals(SortDirection.DESCENDING) ? 1 : -1;
        } else if (difference < 0) {
            return sortDirection.equals(SortDirection.DESCENDING) ? -1 : 1;
        } else {
            difference = virtualItemStack1.visualItemStack.getName().getString().compareToIgnoreCase(virtualItemStack2.visualItemStack.getName().getString());
            if (difference > 0) {
                return sortDirection.equals(SortDirection.DESCENDING) ? 1 : -1;
            } else if (difference < 0) {
                return sortDirection.equals(SortDirection.DESCENDING) ? -1 : 1;
            } else {
                assert virtualItemStack1.visualItemStack.getNbt() != null;
                assert virtualItemStack2.visualItemStack.getNbt() != null;
                difference = virtualItemStack1.visualItemStack.getNbt().toString().compareToIgnoreCase(virtualItemStack2.visualItemStack.getNbt().toString());
                if (difference > 0) {
                    return sortDirection.equals(SortDirection.DESCENDING) ? 1 : -1;
                } else if (difference < 0) {
                    return sortDirection.equals(SortDirection.DESCENDING) ? -1 : 1;
                } else {
                    return 0;
                }
            }
        }
    };

    private final Comparator<VirtualItemStack> nameComparator = (virtualItemStack1, virtualItemStack2) -> {
        int difference = virtualItemStack1.visualItemStack.getName().getString().compareToIgnoreCase(virtualItemStack2.visualItemStack.getName().getString());
        if (difference > 0) {
            return sortDirection.equals(SortDirection.DESCENDING) ? 1 : -1;
        } else if (difference < 0) {
            return sortDirection.equals(SortDirection.DESCENDING) ? -1 : 1;
        } else {
            difference = virtualItemStack2.amount - virtualItemStack1.amount;
            if (difference > 0) {
                return sortDirection.equals(SortDirection.DESCENDING) ? 1 : -1;
            } else if (difference < 0) {
                return sortDirection.equals(SortDirection.DESCENDING) ? -1 : 1;
            } else {
                assert virtualItemStack1.visualItemStack.getNbt() != null;
                assert virtualItemStack2.visualItemStack.getNbt() != null;
                difference = virtualItemStack1.visualItemStack.getNbt().toString().compareToIgnoreCase(virtualItemStack2.visualItemStack.getNbt().toString());
                if (difference > 0) {
                    return sortDirection.equals(SortDirection.DESCENDING) ? 1 : -1;
                } else if (difference < 0) {
                    return sortDirection.equals(SortDirection.DESCENDING) ? -1 : 1;
                } else {
                    return 0;
                }
            }
        }
    };

    @Override
    public boolean isPauseScreen() { return false; }

}
