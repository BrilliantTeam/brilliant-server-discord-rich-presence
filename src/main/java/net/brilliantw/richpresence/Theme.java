package net.brilliantw.richpresence;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class Theme {
	public static final int PANEL = 0xF0121619;
	public static final int HEADER = 0xFF1A2026;
	public static final int SIDEBAR = 0xFF161B20;
	public static final int CARD = 0xFF1E252C;
	public static final int CARD_HOVER = 0xFF2A333C;
	public static final int SELECTED = 0xFF253543;
	public static final int CHIP = 0xFF35404B;
	public static final int CHIP_HOVER = 0xFF4A5966;
	public static final int ACCENT = 0xFF7AD3FF;
	public static final int ACCENT_DIM = 0xFF2E5A70;
	public static final int TEXT = 0xFFE8EEF2;
	public static final int TEXT_SECONDARY = 0xFF8C98A4;
	public static final int TEXT_DIM = 0xFF5C6773;
	public static final int SUCCESS = 0xFF8FE3A0;
	public static final int WARNING = 0xFFF6E0A4;
	public static final int DANGER = 0xFFFF7A7A;

	public static final String BACK = "返回";
	private static final Identifier WATERMARK = Identifier.fromNamespaceAndPath("brilliant-server-mod", "textures/gui/watermark.png");
	private static final int WATERMARK_TEXTURE = 192;
	private static final int WATERMARK_MAX = 96;
	private static final int WATERMARK_TINT = 0x18FFFFFF;

	private Theme() {}

	public static void watermark(Gfx gfx, int panelX, int panelY, int panelW, int panelH) {
		int size = Math.min(WATERMARK_MAX, Math.min(panelW, panelH - 60) / 2);
		if (size < 32) {
			return;
		}
		gfx.texture(WATERMARK, panelX + panelW - size - 10, panelY + panelH - 16 - size - 6, size, size, WATERMARK_TEXTURE, WATERMARK_TEXTURE, WATERMARK_TINT);
	}

	public static void hint(EditBox box, String text) {
		Font font = Minecraft.getInstance().font;
		int width = Math.max(0, box.getInnerWidth());
		String shown = font.width(text) <= width ? text : font.plainSubstrByWidth(text, Math.max(0, width - font.width("…"))) + "…";
		box.setHint(Component.literal(shown).withStyle(ChatFormatting.DARK_GRAY));
	}

	public static int backWidth(Font font) {
		return Gfx.iconLabelWidth(font, Icons.BACK, BACK) + 12;
	}

	public static int titleX(Font font, int panelX) {
		return panelX + 6 + backWidth(font) + 8;
	}

	public static boolean backHit(Font font, int panelX, int panelY, double mouseX, double mouseY) {
		return mouseX >= panelX + 6 && mouseX < panelX + 6 + backWidth(font) && mouseY >= panelY + 6 && mouseY < panelY + 20;
	}

	public static void drawBack(Gfx gfx, Font font, int panelX, int panelY, int mouseX, int mouseY) {
		boolean hovered = backHit(font, panelX, panelY, mouseX, mouseY);
		gfx.fill(panelX + 6, panelY + 6, panelX + 6 + backWidth(font), panelY + 20, hovered ? CHIP_HOVER : CHIP);
		gfx.iconLabel(font, Icons.BACK, BACK, panelX + 6, panelY + 6, backWidth(font), 14, TEXT);
	}
}
