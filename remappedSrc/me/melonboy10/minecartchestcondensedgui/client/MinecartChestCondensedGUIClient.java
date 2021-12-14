package me.melonboy10.minecartchestcondensedgui.client;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import me.melonboy10.minecartchestcondensedgui.InventoryGUI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class MinecartChestCondensedGUIClient implements ClientModInitializer {

    private static KeyBinding keyBinding;

    @Override
    public void onInitializeClient() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minecartchestcondensedgui.openmenu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            "open.menu.key"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                client.player.sendMessage(new LiteralText("Key Y was pressed!"), false);
//                MinecraftClient.getInstance().setScreen(new CottonClientScreen(new InventoryGUI()));
            }
        });
    }
}
