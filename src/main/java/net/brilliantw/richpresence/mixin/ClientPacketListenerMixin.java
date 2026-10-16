package net.brilliantw.richpresence.mixin;

import net.brilliantw.richpresence.DiscordImageLinks;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
	@Inject(method = "sendUnattendedCommand", at = @At("HEAD"), cancellable = true)
	private void brilliant$openImageViewer(String command, Screen screen, CallbackInfo ci) {
		if (DiscordImageLinks.intercept(command)) {
			ci.cancel();
		}
	}
}
