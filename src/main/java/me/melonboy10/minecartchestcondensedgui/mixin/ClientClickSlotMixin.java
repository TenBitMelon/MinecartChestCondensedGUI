package me.melonboy10.minecartchestcondensedgui.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientClickSlotMixin {
    @SuppressWarnings("all")
    @Inject(method = "clickSlot()V", at = @At(value = "INVOKE", ordinal = 0/*target = "sendPacket(Lnet/minecraft/network/Packet)V"*/), cancellable = true)
    private void checkClickedSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (syncId == 0 && slotId > 46) {
            ci.cancel();
        }
    }
}
