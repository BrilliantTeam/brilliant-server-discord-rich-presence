package net.brilliantw.richpresence.mixin;

import net.brilliantw.richpresence.ChannelBar;
import net.brilliantw.richpresence.EmojiPicker;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
	@Shadow
	protected EditBox input;

	@Unique
	private boolean brilliant$typingCommand() {
		boolean typing = input != null && input.getValue().startsWith("/");
		if (typing) {
			EmojiPicker.close();
			ChannelBar.close();
		}
		return typing;
	}

	//? if >=26.1 {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void brilliant$renderEmojiPicker(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
	//?} else {
	/*@Inject(method = "render", at = @At("TAIL"))
	private void brilliant$renderEmojiPicker(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
	*///?}
		if (brilliant$typingCommand()) {
			return;
		}
		ChannelBar.render((ChatScreen) (Object) this, graphics, mouseX, mouseY);
		EmojiPicker.render((ChatScreen) (Object) this, graphics, mouseX, mouseY);
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void brilliant$clickEmojiPicker(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		if (brilliant$typingCommand()) {
			return;
		}
		ChatScreen screen = (ChatScreen) (Object) this;
		if (EmojiPicker.mouseClicked(screen, event.x(), event.y(), event.button())
				|| ChannelBar.mouseClicked(screen, event.x(), event.y(), event.button())) {
			cir.setReturnValue(true);
		}
	}

	@ModifyVariable(method = "handleChatInput", at = @At("HEAD"), argsOnly = true)
	private String brilliant$applyChannelPrefix(String message) {
		return ChannelBar.applyPrefix(message);
	}

	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
	private void brilliant$scrollEmojiPicker(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
		if (brilliant$typingCommand()) {
			return;
		}
		if (EmojiPicker.mouseScrolled((ChatScreen) (Object) this, mouseX, mouseY, scrollY)) {
			cir.setReturnValue(true);
		}
	}
}
