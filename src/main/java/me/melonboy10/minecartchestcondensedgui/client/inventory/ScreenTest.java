package me.melonboy10.minecartchestcondensedgui.client.inventory;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryListener;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.search.SearchableContainer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.tag.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;

@Environment(EnvType.CLIENT)
public class ScreenTest extends AbstractInventoryScreen<ScreenTest.ScreenTestHandler> {

    static final NotSimpleInventory INVENTORY = new NotSimpleInventory();
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Identifier TEXTURE = new Identifier("minecartchestcondensedgui", "textures/gui/container/grid.png");

    int numberOfAddedRows;

    private float scrollPosition;
    private TextFieldWidget searchBox;
    private List<Slot> slots;

    private ScreenTestListener listener;
    private boolean ignoreTypedCharacter;
    private boolean lastClickOutsideBounds;
    private final Map<Identifier, Tag<Item>> searchResultTags = Maps.newTreeMap();

    // Settings
    enum GUISize {SMALL, MEDIUM, LARGE, SCALE}
    GUISize guiSize = GUISize.SCALE;

    public ScreenTest() {
        super(new ScreenTestHandler(), client.player.getInventory(), new LiteralText("Minecarts"));
        client.player.currentScreenHandler = this.handler;
        this.passEvents = true;
        this.backgroundHeight = 172;
        this.backgroundWidth = 193;

        this.titleY += 2;
        this.playerInventoryTitleY += this.height - 214;
    }

    public void handledScreenTick() {
        super.handledScreenTick();
        if (this.searchBox != null) {
            this.searchBox.tick();
        }
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {


        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);

        this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        for (int i = 0; i < numberOfAddedRows; i++) {
            this.drawTexture(matrices, this.x, this.y + 72 + (17 * i), 0, 54, 193, 17);
        }
        this.drawTexture(matrices, this.x, this.y + 72 + numberOfAddedRows * 17, 0, 72, this.backgroundWidth, this.backgroundHeight - 72);

        this.searchBox.render(matrices, mouseX, mouseY, delta);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.drawTexture(matrices, this.x + 174, this.y + 20 + (int) ((float)((numberOfAddedRows + 3) * 18) * this.scrollPosition), 232, 0, 12, 15);
    }

    protected void init() {
        super.init();
        client.keyboard.setRepeatEvents(true);

        numberOfAddedRows = Math.max(0, (this.height - 220) / 18);
        this.y = (this.height - this.backgroundHeight - numberOfAddedRows * 18) / 2;

        TextRenderer textRenderer = this.textRenderer;
        this.searchBox = new TextFieldWidget(textRenderer, this.x + 82, this.y + 7, 80, 9, new TranslatableText("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setDrawsBackground(false);
        this.searchBox.setVisible(true);
        this.searchBox.setEditableColor(16777215);
        this.addSelectableChild(this.searchBox);
    }

    public void resize(MinecraftClient client, int width, int height) {
        String string = this.searchBox.getText();
        this.init(client, width, height);
        this.searchBox.setText(string);
        if (!this.searchBox.getText().isEmpty()) {
            this.search();
        }

    }

    public void removed() {
        super.removed();
        if (this.client.player != null && this.client.player.getInventory() != null) {
            this.client.player.playerScreenHandler.removeListener(this.listener);
        }

        this.client.keyboard.setRepeatEvents(false);
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

    private boolean isMinecartInventorySlot(Slot slot) {
        return slot != null && slot.inventory == INVENTORY;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;

        boolean bl = !this.isMinecartInventorySlot(this.focusedSlot) || this.focusedSlot.hasStack();
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

    private void search() {
        this.handler.itemList.clear();
        String string = this.searchBox.getText();
        if (string.isEmpty()) {
            for (Item item : Registry.ITEM) {
                item.appendStacks(ItemGroup.SEARCH, this.handler.itemList);
            }
        } else {
            SearchableContainer<ItemStack> searchable = client.getSearchableContainer(SearchManager.ITEM_TOOLTIP);
            this.handler.itemList.addAll(searchable.findAll(string.toLowerCase(Locale.ROOT)));
        }

        this.scrollPosition = 0.0F;
        this.handler.scrollItems(0.0F);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int i = (this.handler.itemList.size() + 9 - 1) / 9 - 5;
        this.scrollPosition = (float)((double)this.scrollPosition - amount / (double)i);
        this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
        this.handler.scrollItems(this.scrollPosition);
        return true;
    }

    protected boolean isClickInScrollbar(double mouseX, double mouseY) {
        int x1 = this.x + 174;
        int y1 = this.y + 20;
        int x2 = x1 + numberOfAddedRows * 18;
        int y2 = x2 + 12;
        return mouseX >= (double)x2 && mouseY >= (double)y2 && mouseX < (double)x1 && mouseY < (double)y1;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int i = this.y + 18;
        int j = i + 112;
        this.scrollPosition = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
        this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
        this.handler.scrollItems(this.scrollPosition);
        return true;
    }

    public static class ScreenTestHandler extends ScreenHandler {
        public final DefaultedList<ItemStack> itemList = DefaultedList.of();
        private final ScreenHandler parent;

        protected ScreenTestHandler() {
            super(null, 0);
            this.parent = client.player.playerScreenHandler;
            this.scrollItems(0.0F);
        }

        public void setSize(int size) {
            INVENTORY.setSize(size);
            slots.clear();

            PlayerInventory playerInventory = client.player.getInventory();

            for(int i = 0; i < INVENTORY.size() / 9; ++i) {
                for(int j = 0; j < 9; ++j) {
                    this.addSlot(new Slot(INVENTORY, i * 9 + j, 8 + j * 18, 20 + i * 18));
                }
            }

            for(int i = 0; i < 9; ++i) {
                this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 148));
            }
            for(int i = 0; i < 3; ++i) {
                for(int j = 0; j < 9; ++j) {
                    this.addSlot(new Slot(playerInventory, 9 + i * 9 + j, 8 + j * 18, 90 + i * 18));
                }
            }
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return false;
        }

        public void scrollItems(float position) {
            int i = (this.itemList.size() + 9 - 1) / 9 - INVENTORY.size() / 9;
            int j = (int)((double)(position * (float)i) + 0.5D);
            if (j < 0) {
                j = 0;
            }

            for(int k = 0; k < INVENTORY.size() / 9; ++k) {
                for(int l = 0; l < 9; ++l) {
                    int m = l + (k + j) * 9;
                    if (m >= 0 && m < this.itemList.size()) {
                        INVENTORY.setStack(l + k * 9, this.itemList.get(m));
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
    private static class MinecartSlot extends Slot {
        public MinecartSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }
    }
}
