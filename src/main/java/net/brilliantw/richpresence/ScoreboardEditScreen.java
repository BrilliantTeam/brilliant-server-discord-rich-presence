package net.brilliantw.richpresence;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.client.gui.components.Button;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class ScoreboardEditScreen extends Screen {
	private static final float STEP = 0.05f;

	private final Screen parent;
	private boolean dragging;

	public ScoreboardEditScreen(Screen parent) {
		super(Component.literal("記分板位置"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int y = height - 28;
		addRenderableWidget(Button.builder(Component.literal("重設"), b -> ScoreboardLayout.reset()).bounds(width / 2 - 104, y, 100, 20).build());
		addRenderableWidget(Button.builder(Component.literal("完成"), b -> onClose()).bounds(width / 2 + 4, y, 100, 20).build());
	}

	//? if >=26.1 {
	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		draw(new Gfx(graphics));
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}
	//?} else {
	/*@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		draw(new Gfx(graphics));
		super.render(graphics, mouseX, mouseY, partialTick);
	}
	*///?}

	private void draw(Gfx gfx) {
		List<String> lines = List.of(
				"拖曳滑鼠移動記分板 · 滾輪縮放",
				"目前大小 " + Math.round(ScoreboardLayout.scale() * 100) + "%（" + Math.round(ScoreboardLayout.MIN_SCALE * 100) + "% ~ " + Math.round(ScoreboardLayout.MAX_SCALE * 100) + "%）· Esc 完成");
		int boxWidth = lines.stream().mapToInt(font::width).max().orElse(0) + 16;
		int x = (width - boxWidth) / 2;
		gfx.fill(x, 8, x + boxWidth, 8 + lines.size() * 11 + 8, 0xC0101418);
		for (int i = 0; i < lines.size(); i++) {
			gfx.centered(font, lines.get(i), x, 12 + i * 11, boxWidth, 9, i == 0 ? Theme.ACCENT : Theme.TEXT_SECONDARY);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}
		dragging = event.button() == InputConstants.MOUSE_BUTTON_LEFT;
		return dragging;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (dragging) {
			ScoreboardLayout.move(dragX, dragY);
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		dragging = false;
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		ScoreboardLayout.zoom((float) Math.signum(scrollY) * STEP);
		return true;
	}

	@Override
	public void onClose() {
		ScoreboardLayout.save();
		CommandMenuScreen.setScreen(parent);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
