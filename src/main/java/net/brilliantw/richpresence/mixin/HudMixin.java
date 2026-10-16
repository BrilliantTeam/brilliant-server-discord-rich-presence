package net.brilliantw.richpresence.mixin;

import net.brilliantw.richpresence.Gfx;
import net.brilliantw.richpresence.PrivateWindow;
import net.minecraft.client.DeltaTracker;
//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public abstract class HudMixin {
	//? if >=26.1 {
	@Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At("TAIL"))
	private void brilliant$renderPrivateWindow(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
	//?} else {
	/*@Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("TAIL"))
	private void brilliant$renderPrivateWindow(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
	*///?}
		//? if >=26.2 {
		if (((Hud) (Object) this).isHidden()) {
			return;
		}
		//?}
		PrivateWindow.renderHud(new Gfx(graphics));
	}
}
