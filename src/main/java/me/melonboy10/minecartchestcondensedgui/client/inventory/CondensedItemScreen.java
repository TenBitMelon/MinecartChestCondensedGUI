package me.melonboy10.minecartchestcondensedgui.client.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import me.melonboy10.minecartchestcondensedgui.client.MinecartManager;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private int lastClickedButton;

    public List<VirtualItemStack> items = new ArrayList<>();
    public List<VirtualItemStack> visibleItems = new ArrayList<>();
    public List<ItemStack> playerItems = new ArrayList<>();

    enum HoveredInventory {MINECARTS, PLAYER}
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
        drawTouchDragStack(matrices, delta, mouseX, mouseY);
        drawPickStack(matrices, delta, mouseX, mouseY);
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
        if (rowCount >= Math.ceil(visibleItems.size()/9F)) {
            this.drawTexture(matrices, scrollBarX, scrollBarY, 244, 0, 12, 15);
        } else {
            this.drawTexture(matrices, scrollBarX, scrollBarY, 232, 0, 12, 15);
        }
    }

    private void drawPlayerInventory(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        for (int i = 0; i < 36; i++) {
            ItemStack inventoryItem = playerItems.get(i);
            int slotX = this.guiX + 8 + 18*(i % 9);
            int slotY;
            if (i < 27)
                slotY = this.guiY + (rowCount * 18) + 36 + (18*(i / 9));
            else
                slotY = this.guiY + rowCount * 18 + 94;
            if (showCraftingTable && craftingTableLocation != null) slotY += 56;
            itemRenderer.renderInGui(inventoryItem, slotX, slotY);
            itemRenderer.renderGuiItemOverlay(this.textRenderer, inventoryItem, slotX, slotY, inventoryItem.getCount() == 1 ? "" : Integer.toString(inventoryItem.getCount()));
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
            if ((i + rowsScrolled*9) < visibleItems.size()) {
                ItemStack inventoryItem = visibleItems.get(i + rowsScrolled*9).visualItemStack;
                itemRenderer.renderInGui(inventoryItem, slotX, slotY);
                itemRenderer.renderGuiItemOverlay(this.textRenderer, inventoryItem, slotX, slotY, "");
                String amountString;
                if (visibleItems.get(i + rowsScrolled*9).amount > 999) {
                    amountString = Float.toString((float)Math.round(((float)(visibleItems.get(i + rowsScrolled*9).amount)/1000F)*10F)/10F) + "K";
                } else {
                    amountString = visibleItems.get(i + rowsScrolled*9).amount == 1 ? "" : Integer.toString(visibleItems.get(i + rowsScrolled*9).amount);
                }
                MatrixStack textMatrixStack = new MatrixStack();
                textMatrixStack.scale(0.5F, 0.5F, 1);
                textMatrixStack.translate(0, 0, itemRenderer.zOffset + 200.0F);
                textRenderer.drawWithShadow(textMatrixStack, amountString, slotX*2+31-textRenderer.getWidth(amountString), slotY*2+23, -1);
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
            ItemStack inventoryItem = playerItems.get(i);
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
            if ((i + rowsScrolled*9) < visibleItems.size()) {
                ItemStack inventoryItem = visibleItems.get(i + rowsScrolled * 9).visualItemStack;
                if (mouseX >= slotX - 1 && mouseX <= slotX + 16 && mouseY >= slotY - 1 && mouseY <= slotY + 16) {
                    if (mouseStack == ItemStack.EMPTY && inventoryItem != ItemStack.EMPTY) {
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
        itemRenderer.renderInGuiWithOverrides(mouseStack, (mouseX - 8), (mouseY - 8));
        itemRenderer.renderGuiItemOverlay(this.textRenderer, mouseStack, (mouseX - 8), (mouseY - 8), mouseStack.getCount() == 1 ? "" : Integer.toString(mouseStack.getCount()));
        this.itemRenderer.zOffset = 0.0F;
    }

    private boolean isMouseOver(double mouseX, double mouseY, int x1, int y1, int x2, int y2) {
        return mouseX >= (double)x1 && mouseY >= (double)y1 && mouseX < (double)x2 && mouseY < (double)y2;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isMouseOver(mouseX, mouseY, this.guiX + 174, this.guiY + 20, this.guiX + 186, this.guiY + 18 + rowCount * 18) &&
            rowCount < Math.ceil(visibleItems.size()/9F) || scrolling) {
            int y1 = this.guiY + 20;
            int y2 = y1 + (rowCount) * 18;

            this.scrollPosition = ((float)mouseY - (float)y1 - 7.5F) / ((float)(y2 - y1) - 15F);
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);

            rowsScrolled = Math.round(scrollPosition * (float)(Math.ceil(visibleItems.size()/9F) - rowCount));
            scrolling = true;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (rowCount < Math.ceil(visibleItems.size()/9F)) {
            scrollPosition -= (float) amount / ((float) (Math.ceil(visibleItems.size() / 9F) - rowCount));
            scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
            rowsScrolled = Math.round(scrollPosition * (float) (Math.ceil(visibleItems.size() / 9F) - rowCount));
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        scrolling = false;
        boolean isDoubleClicking = checkForDoubleClick(button);
        mouseDragged(mouseX, mouseY, button, 0, 0);
        checkButtons(mouseX, mouseY);
        checkItems(mouseX, mouseY, button, isDoubleClicking);

        return super.mouseClicked(mouseX, mouseY, button);
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

    public void checkButtons(double mouseX, double mouseY) {
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
                }
            }
        }
    }

    public void checkItems(double mouseX, double mouseY, int button, boolean isDoubleClicking) {

        int y = (this.guiY + rowCount * 18 + 24 + (showCraftingTable && craftingTableLocation != null ? 56 : 0 ));
        if (isMouseOver(mouseX, mouseY, this.guiX, y, this.guiX + 176, y + 88)) { // Handle Player Inventory
            if (hasShiftDown()) {
                if (button == 0) {
                    if (isDoubleClicking && !mouseStack.equals(ItemStack.EMPTY)) {
                        //Move all away
                        client.player.sendMessage(new LiteralText("move all away"), false);
                    } else {
                        //Quick Move hovered Item stack
                        client.player.sendMessage(new LiteralText("quick move"), false);

                    }
                } else if (button == 1) {
                    //Quick Move hovered Item stack
                    client.player.sendMessage(new LiteralText("quick move"), false);
                }
            } else {
                if (mouseStack.equals(ItemStack.EMPTY)) {
                    if (button == 0) {
                        //Pickup one
                        client.player.sendMessage(new LiteralText("pickup one"), false);
                        mouseStack = playerItems.get(hoveredSlot);
                    } else if (button == 1) {
                        //pickup half
                        client.player.sendMessage(new LiteralText("pickup half"), false);
                        mouseStack = playerItems.get(hoveredSlot);
                    }
                } else {
                    if (button == 0) {
                        if (isDoubleClicking) {
                            //move all towards
                            client.player.sendMessage(new LiteralText("move all towards"), false);
                        } else {
                            //place all
                            client.player.sendMessage(new LiteralText("place all"), false);
                            mouseStack = ItemStack.EMPTY;
                        }
                    } else if (button == 1) {
                        //place one
                        client.player.sendMessage(new LiteralText("place one"), false);
                    }
                }
            }
        } else if (isMouseOver(mouseX, mouseY, guiX, guiY, guiX + 172, guiY + 24 + rowCount * 18)) { // Handle Minecart Inventory
            if (hasShiftDown()) {
                if (button == 0) {
                    if (isDoubleClicking && !mouseStack.equals(ItemStack.EMPTY)) {
                        //Move all away
                        client.player.sendMessage(new LiteralText("move all away"), false);
                    } else {
                        //Quick Move hovered Item stack
                        client.player.sendMessage(new LiteralText("quick move"), false);

                    }
                } else if (button == 1) {
                    //Quick Move hovered Item stack
                    client.player.sendMessage(new LiteralText("quick move"), false);
                }
            } else {
                if (mouseStack.equals(ItemStack.EMPTY)) {
                    if (button == 0) {
                        //Pickup one
                        client.player.sendMessage(new LiteralText("pickup one"), false);
                    } else if (button == 1) {
                        //pickup half
                        client.player.sendMessage(new LiteralText("pickup half"), false);
                    }
                } else {
                    if (button == 0) {
                        if (isDoubleClicking) {
                            //move all towards
                            client.player.sendMessage(new LiteralText("move all towards"), false);
                        } else {
                            //place all
                            client.player.sendMessage(new LiteralText("place all"), false);
                        }
                    } else if (button == 1) {
                        //place one
                        client.player.sendMessage(new LiteralText("place one"), false);
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

    private void search() {
        visibleItems.clear();
        for (VirtualItemStack testedStack : items) {
            if (searchBox.getText() == "") {
                visibleItems.add(testedStack.copy());
            } else if (Character.toString( searchBox.getText().charAt(0)) == "@") {
                visibleItems.add(testedStack.copy());
            } else {
                if (testedStack.visualItemStack.getName().getString().toLowerCase().matches(".*\\Q" + searchBox.getText().toLowerCase() + "\\E.*")) {
                    visibleItems.add(testedStack.copy());
                }
            }
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

        if (rowCount < Math.ceil(visibleItems.size()/9F)) {
            rowsScrolled = Math.round(scrollPosition * (float) (Math.ceil(visibleItems.size() / 9F) - rowCount));
        } else {
            scrollPosition = 0;
            rowsScrolled = 0;
        }
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
