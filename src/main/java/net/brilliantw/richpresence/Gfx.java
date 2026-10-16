package net.brilliantw.richpresence;

import java.util.List;
import net.minecraft.client.gui.Font;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.network.chat.Component;

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
