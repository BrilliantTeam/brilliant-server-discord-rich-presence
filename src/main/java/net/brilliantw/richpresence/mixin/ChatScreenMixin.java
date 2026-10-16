package net.brilliantw.richpresence.mixin;

import net.brilliantw.richpresence.ChannelBar;
import net.brilliantw.richpresence.EmojiPicker;
import net.brilliantw.richpresence.Gfx;
import net.brilliantw.richpresence.PrivateWindow;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
	@Shadow
	protected EditBox input;

	protected ChatScreenMixin(Component title) {
		super(title);
	}

	@Unique
	private boolean brilliant$typingCommand() {
		boolean typing = input != null && input.getValue().startsWith("/");
		if (typing) {
			EmojiPicker.close();
			ChannelBar.close();
		}
		return typing;
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void brilliant$addPrivateInput(CallbackInfo ci) {
		addRenderableWidget(PrivateWindow.createInput());
	}

	//? if >=26.1 {
	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void brilliant$renderPrivateWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
	//?} else {
	/*@Inject(method = "render", at = @At("HEAD"))
	private void brilliant$renderPrivateWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
	*///?}
		PrivateWindow.render(new Gfx(graphics), width, height, mouseX, mouseY, true);
	}

	//? if >=26.1 {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void brilliant$renderEmojiPicker(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
	//?} else {
	/*@Inject(method = "render", at = @At("TAIL"))
	private void brilliant$renderEmojiPicker(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
	*///?}
		if (!brilliant$typingCommand()) {
			ChannelBar.render((ChatScreen) (Object) this, graphics, mouseX, mouseY);
			EmojiPicker.render((ChatScreen) (Object) this, graphics, mouseX, mouseY);
		}
		PrivateWindow.renderOverlay(new Gfx(graphics), width, height, mouseX, mouseY);
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void brilliant$clickEmojiPicker(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		ChatScreen screen = (ChatScreen) (Object) this;
		if (PrivateWindow.mouseClicked(screen, event, doubleClick)) {
			cir.setReturnValue(true);
			return;
		}
		if (brilliant$typingCommand()) {
			return;
		}
		if (EmojiPicker.mouseClicked(screen, event.x(), event.y(), event.button())
				|| ChannelBar.mouseClicked(screen, event.x(), event.y(), event.button())) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void brilliant$typePrivateMessage(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (PrivateWindow.keyPressed((ChatScreen) (Object) this, event)) {
			cir.setReturnValue(true);
		}
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		return PrivateWindow.mouseDragged((ChatScreen) (Object) this, event.x(), event.y()) || super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		return PrivateWindow.mouseReleased((ChatScreen) (Object) this, event.x(), event.y()) || super.mouseReleased(event);
	}

	@ModifyVariable(method = "handleChatInput", at = @At("HEAD"), argsOnly = true)
	private String brilliant$applyChannelPrefix(String message) {
		return ChannelBar.applyPrefix(message);
	}

	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
	private void brilliant$scrollEmojiPicker(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
		ChatScreen screen = (ChatScreen) (Object) this;
		if (PrivateWindow.mouseScrolled(screen, mouseX, mouseY, scrollY)) {
			cir.setReturnValue(true);
			return;
		}
		if (brilliant$typingCommand()) {
			return;
		}
		if (EmojiPicker.mouseScrolled(screen, mouseX, mouseY, scrollY)) {
			cir.setReturnValue(true);
		}
	}
}
