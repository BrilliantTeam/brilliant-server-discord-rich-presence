package net.brilliantw.richpresence;

import net.minecraft.resources.Identifier;

public enum Icons {
	CLOSE, MINUS, PLUS, MENU, BACK, SEARCH, EDIT, CARET_RIGHT,
	CARET_DOWN, MAIL, PLAY, EYE, EYE_OFF;

	public static final int SIZE = 12;
	private static final int COLUMNS = 8;
	private static final int ATLAS_WIDTH = 96;
	private static final int ATLAS_HEIGHT = 24;
	private static final Identifier ATLAS = Identifier.fromNamespaceAndPath("brilliant-server-mod", "textures/gui/icons.png");

	public int width() {
		return SIZE;
	}

	public int height() {
		return SIZE;
	}

	public void draw(Gfx gfx, int x, int y, int color) {
		gfx.sprite(ATLAS, x, y, ordinal() % COLUMNS * SIZE, ordinal() / COLUMNS * SIZE, SIZE, ATLAS_WIDTH, ATLAS_HEIGHT, color);
	}

	public void drawCentered(Gfx gfx, int x, int y, int w, int h, int color) {
		draw(gfx, x + (w - SIZE) / 2, y + (h - SIZE) / 2, color);
	}
}
