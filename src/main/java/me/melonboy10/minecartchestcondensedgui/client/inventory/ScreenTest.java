package me.melonboy10.minecartchestcondensedgui.client.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public class ScreenTest extends AbstractInventoryScreen<ScreenTest.ScreenTestHandler> {

    static final NotSimpleInventory INVENTORY = new NotSimpleInventory(27);
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Identifier TEXTURE = new Identifier("minecartchestcondensedgui", "textures/gui/container/grid.png");

    private float scrollPosition;
    private boolean scrolling;
    private TextFieldWidget searchBox;
    private List<Slot> slots;

    // Settings
    enum GUISize {SMALL, MEDIUM, LARGE, SCALE}
    GUISize guiSize = GUISize.SCALE;

    public ScreenTest() {
        super(new ScreenTestHandler(), client.player.getInventory(), new LiteralText("Minecart GUI"));
        client.player.currentScreenHandler = this.handler;
        this.passEvents = true;
        this.backgroundHeight = 172;
        this.backgroundWidth = 193;

        this.titleY += 2;
        this.playerInventoryTitleY += (this.height - 220) + 6;
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

        if (guiSize.equals(GUISize.SCALE)) {
            int numberOfAddedRows = (this.height - 220) / 18;
            this.y = (this.height - this.backgroundHeight - numberOfAddedRows * 18) / 2;

            this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
            for (int i = 0; i < numberOfAddedRows; i++) {
                this.drawTexture(matrices, this.x, this.y + 72 + (17 * i), 0, 54, 193, 17);
            }
            this.drawTexture(matrices, this.x, this.y + 72 + numberOfAddedRows * 17, 0, 72, this.backgroundWidth, this.backgroundHeight - 72);
        }




        this.searchBox.render(matrices, mouseX, mouseY, delta);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderTexture(0, TEXTURE);
        if (true) {
//            this.drawTexture(matrices, i, j + (int)((float)(k - j - 17) * this.scrollPosition), 232 + (this.hasScrollbar() ? 0 : 12), 0, 12, 15);
        }
    }

    protected void init() {
        super.init();
        client.keyboard.setRepeatEvents(true);
        TextRenderer textRenderer = this.textRenderer;
        this.searchBox = new TextFieldWidget(textRenderer, this.x + 82, this.y + 7, 80, 9, new TranslatableText("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setDrawsBackground(false);
        this.searchBox.setVisible(true);
        this.searchBox.setEditableColor(16777215);
        this.addSelectableChild(this.searchBox);
    }

    public static class ScreenTestHandler extends ScreenHandler {
        public final DefaultedList<ItemStack> itemList = DefaultedList.of();
        private final ScreenHandler parent;

        protected ScreenTestHandler() {
            super(null, 0);

            this.parent = client.player.playerScreenHandler;
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

            this.scrollItems(0.0F);
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
}
