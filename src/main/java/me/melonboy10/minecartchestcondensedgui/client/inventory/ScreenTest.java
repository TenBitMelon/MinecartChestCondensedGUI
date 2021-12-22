package me.melonboy10.minecartchestcondensedgui.client.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryListener;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.option.HotbarStorageEntry;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.search.SearchableContainer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class ScreenTest extends AbstractInventoryScreen<ScreenTest.ScreenTestHandler> {

    private static final Identifier TEXTURE = new Identifier("minecartchestcondensedgui", "textures/gui/container/grid.png");
    static final SimpleInventory INVENTORY = new SimpleInventory(45);

    private float scrollPosition;
    private boolean scrolling;
    private TextFieldWidget searchBox;
    private static boolean showCraftingTable = true;
    private BlockPos craftingTableLocation;
    private int rowCount;

    private List<Slot> slots;
    private boolean ignoreTypedCharacter;
    private boolean lastClickOutsideBounds;
    private final Map<Identifier, Tag<Item>> searchResultTags = Maps.newTreeMap();

    public ScreenTest() {
        super(new ScreenTest.ScreenTestHandler(), MinecraftClient.getInstance().player.getInventory(), LiteralText.EMPTY);
        client = MinecraftClient.getInstance();
        client.player.currentScreenHandler = this.handler;
        this.passEvents = true;
        this.backgroundHeight = 229;
        this.backgroundWidth = 195;
    }

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
//        this.x = (this.height - (craftingTableLocation == null || !showCraftingTable ? this.backgroundHeight - 56 : this.backgroundHeight) - (rowCount - 3) * 18) / 2;
//        this.y = (this.width - this.backgroundWidth + 17) / 2;

        this.client.keyboard.setRepeatEvents(true);
        TextRenderer var10003 = this.textRenderer;
        int var10004 = this.x + 82;
        int var10005 = this.y + 6;
        Objects.requireNonNull(this.textRenderer);
        this.searchBox = new TextFieldWidget(var10003, var10004, var10005, 80, 9, new TranslatableText("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setDrawsBackground(false);
        this.searchBox.setVisible(false);
        this.searchBox.setEditableColor(16777215);
        this.addSelectableChild(this.searchBox);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);


        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int numberOfAddedRows = rowCount - 3;
//        this.guiY = (this.height - this.backgroundHeight - numberOfAddedRows * 18) / 2;
//        this.guiX = (this.width - this.backgroundWidth + 17) / 2;
        this.fillGradient(matrices, this.x, this.y, this.x + 10, this.y + 10, 16777215, 16777215);
        this.fillGradient(matrices, 0, 0, 10, 10, 16777215, 16777215);
        this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight - 150); // Top
        for (int i = 0; i < numberOfAddedRows; i++) {
            this.drawTexture(matrices, this.x, this.y + 72 + (18 * i), 0, 54, 193, 25); // Row Segment
        }
        if (showCraftingTable && craftingTableLocation != null) {
            // Crafting Table Segment
            this.drawTexture(matrices, this.x, this.y + 78 + numberOfAddedRows * 18, 0, 79, this.backgroundWidth, this.backgroundHeight - 79); // Bottom
        } else {
            this.drawTexture(matrices, this.x, this.y + 78 + numberOfAddedRows * 18, 0, 135, this.backgroundWidth, this.backgroundHeight - 135); // Bottom
        }

    }

    protected void renderTooltip(MatrixStack matrices, ItemStack stack, int x, int y) {
        List<Text> list = stack.getTooltip(this.client.player, this.client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL);
        List<Text> list2 = Lists.newArrayList((Iterable)list);

        this.searchResultTags.forEach((id, tag) -> {
            if (stack.isIn(tag)) {
                list2.add(1, (new LiteralText("#" + id)).formatted(Formatting.DARK_PURPLE));
            }
        });

        this.renderTooltip(matrices, list2, stack.getTooltipData(), x, y);

    }

    public void resize(MinecraftClient client, int width, int height) {
        String string = this.searchBox.getText();
        this.init(client, width, height);
        this.searchBox.setText(string);
        if (!this.searchBox.getText().isEmpty()) {
            this.search();
        }

    }

    protected void onMouseClick(@Nullable Slot slot, int slotId, int button, SlotActionType actionType) {
        if (this.isCreativeInventorySlot(slot)) {
            this.searchBox.setCursorToEnd();
            this.searchBox.setSelectionEnd(0);
        }

        boolean bl = actionType == SlotActionType.QUICK_MOVE;
        actionType = slotId == -999 && actionType == SlotActionType.PICKUP ? SlotActionType.THROW : actionType;
        ItemStack i;
        if (slot == null && actionType != SlotActionType.QUICK_CRAFT) {
            if (!this.handler.getCursorStack().isEmpty() && this.lastClickOutsideBounds) {
                if (button == 0) {
                    this.client.player.dropItem(this.handler.getCursorStack(), true);
                    this.client.interactionManager.dropCreativeStack(this.handler.getCursorStack());
                    this.handler.setCursorStack(ItemStack.EMPTY);
                }

                if (button == 1) {
                    i = this.handler.getCursorStack().split(1);
                    this.client.player.dropItem(i, true);
                    this.client.interactionManager.dropCreativeStack(i);
                }
            }
        } else {
            if (slot != null && !slot.canTakeItems(this.client.player)) {
                return;
            }


            ItemStack itemStack;
            if (actionType != SlotActionType.QUICK_CRAFT && slot.inventory == INVENTORY) {
                i = this.handler.getCursorStack();
                itemStack = slot.getStack();
                ItemStack itemStack2;
                if (actionType == SlotActionType.SWAP) {
                    if (!itemStack.isEmpty()) {
                        itemStack2 = itemStack.copy();
                        itemStack2.setCount(itemStack2.getMaxCount());
                        this.client.player.getInventory().setStack(button, itemStack2);
                        this.client.player.playerScreenHandler.sendContentUpdates();
                    }

                    return;
                }

                if (actionType == SlotActionType.CLONE) {
                    if (this.handler.getCursorStack().isEmpty() && slot.hasStack()) {
                        itemStack2 = slot.getStack().copy();
                        itemStack2.setCount(itemStack2.getMaxCount());
                        this.handler.setCursorStack(itemStack2);
                    }

                    return;
                }

                if (actionType == SlotActionType.THROW) {
                    if (!itemStack.isEmpty()) {
                        itemStack2 = itemStack.copy();
                        itemStack2.setCount(button == 0 ? 1 : itemStack2.getMaxCount());
                        this.client.player.dropItem(itemStack2, true);
                        this.client.interactionManager.dropCreativeStack(itemStack2);
                    }

                    return;
                }

                if (!i.isEmpty() && !itemStack.isEmpty() && i.isItemEqualIgnoreDamage(itemStack) && ItemStack.areNbtEqual(i, itemStack)) {
                    if (button == 0) {
                        if (bl) {
                            i.setCount(i.getMaxCount());
                        } else if (i.getCount() < i.getMaxCount()) {
                            i.increment(1);
                        }
                    } else {
                        i.decrement(1);
                    }
                } else if (!itemStack.isEmpty() && i.isEmpty()) {
                    this.handler.setCursorStack(itemStack.copy());
                    i = this.handler.getCursorStack();
                    if (bl) {
                        i.setCount(i.getMaxCount());
                    }
                } else if (button == 0) {
                    this.handler.setCursorStack(ItemStack.EMPTY);
                } else {
                    this.handler.getCursorStack().decrement(1);
                }
            } else if (this.handler != null) {
                i = slot == null ? ItemStack.EMPTY : this.handler.getSlot(slot.id).getStack();
                this.handler.onSlotClick(slot == null ? slotId : slot.id, button, actionType, this.client.player);
                if (ScreenHandler.unpackQuickCraftStage(button) == 2) {
                    for(int j = 0; j < 9; ++j) {
                        this.client.interactionManager.clickCreativeStack(this.handler.getSlot(45 + j).getStack(), 36 + j);
                    }
                } else if (slot != null) {
                    itemStack = this.handler.getSlot(slot.id).getStack();
                    this.client.interactionManager.clickCreativeStack(itemStack, slot.id - this.handler.slots.size() + 9 + 36);
                    int itemStack2 = 45 + button;
                    if (actionType == SlotActionType.SWAP) {
                        this.client.interactionManager.clickCreativeStack(i, itemStack2 - this.handler.slots.size() + 9 + 36);
                    } else if (actionType == SlotActionType.THROW && !i.isEmpty()) {
                        ItemStack itemStack3 = i.copy();
                        itemStack3.setCount(button == 0 ? 1 : itemStack3.getMaxCount());
                        this.client.player.dropItem(itemStack3, true);
                        this.client.interactionManager.dropCreativeStack(itemStack3);
                    }

                    this.client.player.playerScreenHandler.sendContentUpdates();
                }
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.isClickInScrollbar(mouseX, mouseY)) {
                this.scrolling = this.hasScrollbar();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.scrolling = false;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!this.hasScrollbar()) {
            return false;
        } else {
            int i = (this.handler.itemList.size() + 9 - 1) / 9 - 5;
            this.scrollPosition = (float)((double)this.scrollPosition - amount / (double)i);
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
            this.handler.scrollItems(this.scrollPosition);
            return true;
        }
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrolling) {
            int i = this.y + 18;
            int j = i + 112;
            this.scrollPosition = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
            this.handler.scrollItems(this.scrollPosition);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        boolean bl = !this.isCreativeInventorySlot(this.focusedSlot) || this.focusedSlot.hasStack();
        boolean bl2 = InputUtil.fromKeyCode(keyCode, scanCode).toInt().isPresent();
        if (bl && bl2 && this.handleHotbarKeyPressed(keyCode, scanCode)) {
            this.ignoreTypedCharacter = true;
            return true;
        } else {
            String string = this.searchBox.getText();
            if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
                if (!Objects.equals(string, this.searchBox.getText())) {
                    this.search();
                }

                return true;
            } else {
                return this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != GLFW.GLFW_KEY_ESCAPE || super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char chr, int modifiers) {
        if (this.ignoreTypedCharacter) {
            return false;
        } else {
            String string = this.searchBox.getText();
            if (this.searchBox.charTyped(chr, modifiers)) {
                if (!Objects.equals(string, this.searchBox.getText())) {
                    this.search();
                }

                return true;
            } else {
                return false;
            }
        }
    }

    private void search() {
        this.handler.itemList.clear();
        String string = this.searchBox.getText();
        if (string.isEmpty()) {

            for (Item item : Registry.ITEM) {
                item.appendStacks(ItemGroup.SEARCH, this.handler.itemList);
            }
        } else {
            SearchableContainer<ItemStack> searchable;
            if (string.startsWith("#")) {
                string = string.substring(1);
                searchable = this.client.getSearchableContainer(SearchManager.ITEM_TAG);
            } else {
                searchable = this.client.getSearchableContainer(SearchManager.ITEM_TOOLTIP);
            }

            this.handler.itemList.addAll(searchable.findAll(string.toLowerCase(Locale.ROOT)));
        }

        this.scrollPosition = 0.0F;
        this.handler.scrollItems(0.0F);
    }

    private boolean isCreativeInventorySlot(@Nullable Slot slot) {
        return slot != null && slot.inventory == INVENTORY;
    }

    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        boolean bl = mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.backgroundWidth) || mouseY >= (double)(top + this.backgroundHeight);
        this.lastClickOutsideBounds = bl;
        return this.lastClickOutsideBounds;
    }

    protected boolean isClickInScrollbar(double mouseX, double mouseY) {
        int i = this.x;
        int j = this.y;
        int k = i + 175;
        int l = j + 18;
        int m = k + 14;
        int n = l + 112;
        return mouseX >= (double)k && mouseY >= (double)l && mouseX < (double)m && mouseY < (double)n;
    }

    private boolean hasScrollbar() {
        return this.handler.shouldShowScrollbar();
    }

    @Environment(EnvType.CLIENT)
    public static class ScreenTestHandler extends ScreenHandler {
        public final DefaultedList<ItemStack> itemList = DefaultedList.of();
        private final ScreenHandler parent;
        private static MinecraftClient client = MinecraftClient.getInstance();

        public ScreenTestHandler() {
            super((ScreenHandlerType)null, 0);
            this.parent = client.player.playerScreenHandler;
            PlayerInventory playerInventory = client.player.getInventory();

            int i;
            for(i = 0; i < 5; ++i) {
                for(int j = 0; j < 9; ++j) {
                    this.addSlot(new LockableSlot(INVENTORY, i * 9 + j, 9 + j * 18, 18 + i * 18));
                }
            }

            for(i = 0; i < 9; ++i) {
                this.addSlot(new Slot(playerInventory, i, 9 + i * 18, 112));
            }

            this.scrollItems(0.0F);
        }

        public boolean canUse(PlayerEntity player) {
            return true;
        }

        public void scrollItems(float position) {
            int i = (this.itemList.size() + 9 - 1) / 9 - 5;
            int j = (int)((double)(position * (float)i) + 0.5D);
            if (j < 0) {
                j = 0;
            }

            for(int k = 0; k < 5; ++k) {
                for(int l = 0; l < 9; ++l) {
                    int m = l + (k + j) * 9;
                    if (m >= 0 && m < this.itemList.size()) {
                        INVENTORY.setStack(l + k * 9, (ItemStack)this.itemList.get(m));
                    } else {
                        INVENTORY.setStack(l + k * 9, ItemStack.EMPTY);
                    }
                }
            }

        }

        public boolean shouldShowScrollbar() {
            return this.itemList.size() > 45;
        }

        public ItemStack transferSlot(PlayerEntity player, int index) {
            if (index >= this.slots.size() - 9 && index < this.slots.size()) {
                Slot slot = (Slot)this.slots.get(index);
                if (slot != null && slot.hasStack()) {
                    slot.setStack(ItemStack.EMPTY);
                }
            }

            return ItemStack.EMPTY;
        }

        public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
            return slot.inventory != INVENTORY;
        }

        public boolean canInsertIntoSlot(Slot slot) {
            return slot.inventory != INVENTORY;
        }

        public ItemStack getCursorStack() {
            return this.parent.getCursorStack();
        }

        public void setCursorStack(ItemStack stack) {
            this.parent.setCursorStack(stack);
        }
    }

    @Environment(EnvType.CLIENT)
    private static class CreativeSlot extends Slot {
        final Slot slot;

        public CreativeSlot(Slot slot, int invSlot, int x, int y) {
            super(slot.inventory, invSlot, x, y);
            this.slot = slot;
        }

        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            this.slot.onTakeItem(player, stack);
        }

        public boolean canInsert(ItemStack stack) {
            return this.slot.canInsert(stack);
        }

        public ItemStack getStack() {
            return this.slot.getStack();
        }

        public boolean hasStack() {
            return this.slot.hasStack();
        }

        public void setStack(ItemStack stack) {
            this.slot.setStack(stack);
        }

        public void markDirty() {
            this.slot.markDirty();
        }

        public int getMaxItemCount() {
            return this.slot.getMaxItemCount();
        }

        public int getMaxItemCount(ItemStack stack) {
            return this.slot.getMaxItemCount(stack);
        }

        @Nullable
        public Pair<Identifier, Identifier> getBackgroundSprite() {
            return this.slot.getBackgroundSprite();
        }

        public ItemStack takeStack(int amount) {
            return this.slot.takeStack(amount);
        }

        public boolean isEnabled() {
            return this.slot.isEnabled();
        }

        public boolean canTakeItems(PlayerEntity playerEntity) {
            return this.slot.canTakeItems(playerEntity);
        }
    }

    @Environment(EnvType.CLIENT)
    private static class LockableSlot extends Slot {
        public LockableSlot(Inventory inventory, int i, int j, int k) {
            super(inventory, i, j, k);
        }

        public boolean canTakeItems(PlayerEntity playerEntity) {
            if (super.canTakeItems(playerEntity) && this.hasStack()) {
                return this.getStack().getSubNbt("CustomCreativeLock") == null;
            } else {
                return !this.hasStack();
            }
        }
    }
}
