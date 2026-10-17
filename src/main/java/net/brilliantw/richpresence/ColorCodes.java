package net.brilliantw.richpresence;

import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

public final class ColorCodes {
	private static final int MAX_PREVIEW_LINES = 3;
	private static final Pattern CODE = Pattern.compile("&(#[0-9a-fA-F]{6}|[0-9a-fA-Fk-oK-OrR])");

	private ColorCodes() {}

	public static boolean hasCodes(String text) {
		return text != null && CODE.matcher(text).find();
	}

	public static Component parse(String text) {
		MutableComponent result = Component.empty();
		Style style = Style.EMPTY;
		StringBuilder pending = new StringBuilder();
		int i = 0;
		while (i < text.length()) {
			char c = text.charAt(i);
			if (c == '&' && i + 8 <= text.length() && text.charAt(i + 1) == '#' && text.substring(i + 2, i + 8).matches("[0-9a-fA-F]{6}")) {
				flush(result, pending, style);
				style = Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt(text.substring(i + 2, i + 8), 16)));
				i += 8;
				continue;
			}
			ChatFormatting format = c == '&' && i + 1 < text.length() ? ChatFormatting.getByCode(Character.toLowerCase(text.charAt(i + 1))) : null;
			if (format != null) {
				flush(result, pending, style);
				if (format == ChatFormatting.RESET) {
					style = Style.EMPTY;
				} else if (format.ordinal() <= ChatFormatting.WHITE.ordinal()) {
					style = Style.EMPTY.applyFormat(format);
				} else {
					style = style.applyFormat(format);
				}
				i += 2;
				continue;
			}
			pending.append(c);
			i++;
		}
		flush(result, pending, style);
		return result;
	}

	public static void renderPreview(Gfx gfx, Font font, String text, int x, int bottom, int maxWidth) {
		if (!hasCodes(text)) {
			return;
		}
		String label = "預覽 ";
		int labelWidth = font.width(label);
		List<FormattedCharSequence> lines = font.split(parse(text), Math.max(40, maxWidth - labelWidth - 6));
		if (lines.size() > MAX_PREVIEW_LINES) {
			lines = lines.subList(lines.size() - MAX_PREVIEW_LINES, lines.size());
		}
		int width = lines.stream().mapToInt(font::width).max().orElse(0) + labelWidth + 6;
		int top = bottom - lines.size() * 10 - 4;
		gfx.fill(x, top, x + width, bottom, 0xD0101010);
		gfx.text(font, label, x + 3, top + 3, Theme.TEXT_DIM);
		for (int i = 0; i < lines.size(); i++) {
			gfx.text(font, lines.get(i), x + 3 + labelWidth, top + 3 + i * 10, 0xFFFFFFFF);
		}
	}

	private static void flush(MutableComponent result, StringBuilder pending, Style style) {
		if (!pending.isEmpty()) {
			result.append(Component.literal(pending.toString()).setStyle(style));
			pending.setLength(0);
		}
	}
}
