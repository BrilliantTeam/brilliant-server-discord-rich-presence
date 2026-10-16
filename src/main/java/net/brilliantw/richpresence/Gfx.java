package net.brilliantw.richpresence;

import java.util.List;
import net.minecraft.client.gui.Font;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class Gfx {
	//? if >=26.1 {
	private final GuiGraphicsExtractor graphics;

	public Gfx(GuiGraphicsExtractor graphics) {
		this.graphics = graphics;
	}
	//?} else {
	/*private final GuiGraphics graphics;

	public Gfx(GuiGraphics graphics) {
		this.graphics = graphics;
	}
	*///?}

	public void fill(int x1, int y1, int x2, int y2, int color) {
		graphics.fill(x1, y1, x2, y2, color);
	}

	public void text(Font font, String text, int x, int y, int color) {
		//? if >=26.1 {
		graphics.text(font, text, x, y, color, false);
		//?} else {
		/*graphics.drawString(font, text, x, y, color, false);
		*///?}
	}

	public void text(Font font, Component text, int x, int y, int color) {
		//? if >=26.1 {
		graphics.text(font, text, x, y, color, false);
		//?} else {
		/*graphics.drawString(font, text, x, y, color, false);
		*///?}
	}

	public void image(Identifier texture, int x, int y, int width, int height) {
		graphics.blit(texture, x, y, x + width, y + height, 0f, 1f, 0f, 1f);
	}

	public void tooltip(Font font, List<Component> lines, int x, int y) {
		graphics.setComponentTooltipForNextFrame(font, lines, x, y);
	}

	public void scissor(int x1, int y1, int x2, int y2) {
		graphics.enableScissor(x1, y1, x2, y2);
	}

	public void endScissor() {
		graphics.disableScissor();
	}
}
