package me.melonboy10.minecartchestcondensedgui.client;

import me.melonboy10.minecartchestcondensedgui.client.inventory.CondensedItemScreen;
import me.melonboy10.minecartchestcondensedgui.client.inventory.ScreenTest;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class MinecartChestCondensedGUIClient implements ClientModInitializer {

    private static KeyBinding keyBinding;
    private static KeyBinding keyBinding2;
    private static final String displayName = "12345";
    public static CondensedItemScreen gui = new CondensedItemScreen();

    @Override
    public void onInitializeClient() {
        // Register new keybind for Y
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minecartchestcondensedgui.openmenu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            "minecartcondensedgui"
        ));
        keyBinding2 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minecartchestcondensedgui.openmenu2",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "minecartcondensedgui"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding2.wasPressed()) {
                client.setScreen(new ScreenTest());
            }
        });

        // Check for key bind press and tick the minecart searcher
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                client.player.sendMessage(new LiteralText("Key Y was pressed!"), false);

                if (!MinecartManager.running) {
                    MinecraftClient.getInstance().setScreen(gui);
                    // If the key was presses search the area for minecarts and add them to the list

                    client.player.getWorld().getNonSpectatingEntities(ChestMinecartEntity.class, client.player.getBoundingBox().expand(3))
                    .stream().filter(chestMinecartEntity -> chestMinecartEntity.getDisplayName().getString().equals(displayName)).forEach(minecart -> {
                        MinecartManager.addTask(new MinecartManager.ScanTask(minecart));
                    });

                    MinecartManager.runTask();
                }
            }
        });
    }
}
