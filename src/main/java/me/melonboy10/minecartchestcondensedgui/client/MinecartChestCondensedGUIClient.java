package me.melonboy10.minecartchestcondensedgui.client;

import me.melonboy10.minecartchestcondensedgui.client.inventory.CondensedItemHandledScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class MinecartChestCondensedGUIClient implements ClientModInitializer {

    private static KeyBinding keyBinding;
    private static KeyBinding keyBinding2;
    private static final String displayName = "12345";
    public static CondensedItemHandledScreen gui;
//    public static CondensedItemScreen gui = new CondensedItemScreen();

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
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("fmi").executes(
                context -> {
                    for (int i = 0; i < 36; i++) {
                        StackReference stackReference = MinecraftClient.getInstance().player.getStackReference(i);
                        stackReference.set(new ItemStack(Items.MELON, i + 1));
                    }
                    return 1;
                }
            ));}
        );

        // Check for key bind press and tick the minecart searcher
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                client.player.sendMessage(new LiteralText("Key Y was pressed!"), false);
                gui = CondensedItemHandledScreen.create();

                if (!MinecartManager.running) {
                    MinecraftClient.getInstance().setScreen(gui);
                    MinecraftClient.getInstance().player.currentScreenHandler = gui.getScreenHandler();
                    // If the key was presses search the area for minecarts and add them to the list

                    client.player.getWorld().getNonSpectatingEntities(ChestMinecartEntity.class, client.player.getBoundingBox().expand(3))
                    .stream().filter(chestMinecartEntity -> chestMinecartEntity.getDisplayName().getString().equals(displayName)).forEach(minecart -> {
                        MinecartManager.addTask(new MinecartManager.ScanTask(minecart));
                    });

                    MinecartManager.runTask();
                }
            }
            while (keyBinding2.wasPressed()) {
                MinecraftClient.getInstance().setScreen(CondensedItemHandledScreen.create());
            }
        });
    }
}
