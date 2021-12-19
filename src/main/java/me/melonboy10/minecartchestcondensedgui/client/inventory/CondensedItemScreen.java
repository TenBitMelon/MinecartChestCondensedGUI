package me.melonboy10.minecartchestcondensedgui.client.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

import java.util.Dictionary;

public class CondensedItemScreen extends Screen {
    private static final Identifier TEXTURE = new Identifier("minecartchestcondensedgui", "textures/gui/container/grid.png");

    private final int backgroundHeight = 172;
    private final int backgroundWidth = 193;

    private int guiX;
    private int guiY;

    private int rowCount;

    private float scrollPosition;
    private boolean scrolling;
    private TextFieldWidget searchBox;
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public CondensedItemScreen(Text title) {
        super(title);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        RenderSystem.setShaderTexture(0, TEXTURE);
        drawBackground(matrices, delta, mouseX, mouseY);
        renderTooltip(matrices, new LiteralText(":D"), mouseX, mouseY );
//        renderBackgroundTexture(0);
    }

    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {


        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);


        int numberOfAddedRows = rowCount - 3;
        this.guiY = (this.height - this.backgroundHeight - numberOfAddedRows * 18) / 2;
        this.guiX = (this.width - this.backgroundWidth) / 2;

        this.drawTexture(matrices, this.guiX, this.guiY, 0, 0, this.backgroundWidth, this.backgroundHeight);
        for (int i = 0; i < numberOfAddedRows; i++) {
            this.drawTexture(matrices, this.guiX, this.guiY + 72 + (17 * i), 0, 54, 193, 17);
        }
        this.drawTexture(matrices, this.guiX, this.guiY + 72 + numberOfAddedRows * 17, 0, 72, this.backgroundWidth, this.backgroundHeight - 72);

        this.searchBox.render(matrices, mouseX, mouseY, delta);


        client.textRenderer.draw(matrices, "Minecarts", (this.guiX + 8), (this.guiY + 9), 0);
        client.textRenderer.draw(matrices, "Inventory", (this.guiX + 8), (this.guiY + rowCount*17 + 27), 0);
    }

    protected void init() {
        super.init();
        rowCount = (this.height - 220) / 18 + 3;
        this.guiY = (this.height - this.backgroundHeight - (rowCount - 3) * 18) / 2;
        this.guiX = (this.width - this.backgroundWidth) / 2;
        client.keyboard.setRepeatEvents(true);
        TextRenderer textRenderer = this.textRenderer;
        this.searchBox = new TextFieldWidget(textRenderer, this.guiX + 82, this.guiY + 7, 80, 9, new TranslatableText("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setDrawsBackground(false);
        this.searchBox.setVisible(true);
        this.searchBox.setEditableColor(16777215);
        this.addSelectableChild(this.searchBox);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}