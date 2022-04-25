package me.melonboy10.minecartchestcondensedgui.client.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SideButtonWidget extends ButtonWidget {
    private final Identifier texture = new Identifier("minecartchestcondensedgui", "textures/gui/container/grid.png");;
    private final int index;
    private final int toggleIndex;
    final String tooltip;
    final String toggledTooltip;
    private final Screen screen;
    boolean toggled;

    /**
     * Creates a new button with a texture from the grid texture buttons
     * @param x x pos of button
     * @param y y pos of button
     * @param index texture index on grid texture
     * @param pressAction what to do when pressed
     * @param tooltip tooltip to display when hovered
     * @param screen the screen being rendered to
     */
    public SideButtonWidget(int x, int y, int index, ButtonWidget.PressAction pressAction, String tooltip, Screen screen) {
        this(x, y, index, -1, pressAction, tooltip, screen);
    }

    /**
     * Creates a new button that can be toggled between textures on the grid
     * @param x x pos of button
     * @param y y pos of button
     * @param index texture index on grid texture
     * @param toggleIndex texture index of toggled texture
     * @param pressAction what to do when pressed
     * @param tooltip tooltip to display when hovered
     * @param screen the screen being rendered to
     */
    public SideButtonWidget(int x, int y, int index, int toggleIndex, ButtonWidget.PressAction pressAction, String tooltip, Screen screen) {
        this(x, y, index, toggleIndex, pressAction, tooltip, null, screen);
    }

    /**
     * Creates a new button that can be toggled between textures on the grid
     * @param x x pos of button
     * @param y y pos of button
     * @param index texture index on grid texture
     * @param toggleIndex texture index of toggled texture
     * @param pressAction what to do when pressed
     * @param tooltip tooltip to display when hovered
     * @param toggledTooltip tooltip to display when toggled and hovered
     * @param screen the screen being rendered to
     */
    public SideButtonWidget(int x, int y, int index, int toggleIndex, ButtonWidget.PressAction pressAction, String tooltip, String toggledTooltip, Screen screen) {
        super(x, y, 16, 16, LiteralText.EMPTY, pressAction, (button, matrices, mouseX, mouseY) -> {});
        this.index = index;
        this.toggleIndex = toggleIndex;
        this.tooltip = tooltip;
        this.toggledTooltip = toggledTooltip;
        this.screen = screen;
    }

    public void setToggled(boolean toggled) {
        if (toggleIndex > 0) this.toggled = toggled;
    }

    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        int tx = (isHovered() ? 16 : 0) + 197; // x pos on texture
        int ty = (toggled ? toggleIndex : index) * 16; // y pos on texture

        RenderSystem.enableDepthTest();
        drawTexture(matrices, x, y, tx, ty, 16, 16);
    }
}
