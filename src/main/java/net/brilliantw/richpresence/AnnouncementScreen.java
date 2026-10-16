package net.brilliantw.richpresence;

import com.mojang.blaze3d.platform.InputConstants;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public final class AnnouncementScreen extends Screen {
	private record Rect(int x, int y, int w, int h) {
		boolean contains(double mouseX, double mouseY) {
			return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
		}
	}

	private record Segment(int x, int width, String text, Style style, AnnouncementStore.Run run) {}

	private record Row(int y, int height, int indent, String kind, List<Integer> bars, Integer poll, List<Segment> segments,
			AnnouncementStore.Line image, int imageWidth, int requestWidth) {}

	private record Card(AnnouncementStore.Message message, int y, int height, List<Row> rows) {}

	private static final int HEADER = 26;
	private static final int FOOTER = 16;
	private static final int PAD = 7;
	private static final int CARD_GAP = 6;
	private static final int LINE = 11;
	private static final int BAR_STEP = 8;
	private static final int CHIP_HEIGHT = 14;
	private static final int QUOTE = 0xFF4E5058;
	private static final int EMOJI = 9;
	private static final int EMOJI_ADVANCE = 11;
	private static final int MAX_IMAGE_HEIGHT = 200;
	private static final int SMALL_IMAGE = 96;
	private static final int CODE_BG = 0xFF0F1316;
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").withZone(ZoneId.systemDefault());

	private final Screen parent;
	private final Map<String, String> seenAtOpen = new HashMap<>();
	private final Set<String> revealed = new HashSet<>();
	private String board;
	private double scroll;
	private List<Card> cards = List.of();
	private int layoutWidth = -1;
	private int layoutVersion = -1;
	private int layoutReveals = -1;
	private int layoutImages = -1;
	private String layoutBoard;

	public AnnouncementScreen(Screen parent) {
		super(Component.literal("公告"));
		this.parent = parent;
		if (AnnouncementStore.BOARDS.isEmpty()) {
			AnnouncementStore.loadBoards(this::selectDefaultBoard);
		} else {
			selectDefaultBoard();
		}
	}

	private void selectDefaultBoard() {
		if (AnnouncementStore.BOARDS.isEmpty()) {
			return;
		}
		String target = AnnouncementStore.BOARDS.stream()
				.filter(b -> AnnouncementStore.unread(b.key) > 0)
				.map(b -> b.key)
				.findFirst()
				.orElse(AnnouncementStore.BOARDS.getFirst().key);
		selectBoard(target);
	}

	private void selectBoard(String key) {
		if (board != null) {
			AnnouncementStore.markSeen(board);
		}
		board = key;
		scroll = 0;
		seenAtOpen.putIfAbsent(key, AnnouncementStore.seenId(key) == null ? "" : AnnouncementStore.seenId(key));
		if (AnnouncementStore.isStale(key)) {
			AnnouncementStore.refresh(key, () -> AnnouncementStore.markSeen(key));
		} else {
			AnnouncementStore.markSeen(key);
		}
	}

	private Rect panel() {
		int w = Math.min(width - 16, 520);
		return new Rect((width - w) / 2, 8, w, height - 16);
	}

	private Rect list() {
		Rect p = panel();
		return new Rect(p.x() + 8, p.y() + HEADER + 6, p.w() - 16, p.h() - HEADER - FOOTER - 10);
	}

	private List<Rect> tabs() {
		Rect p = panel();
		List<Rect> rects = new ArrayList<>();
		int x = p.x() + 10 + font.width(title) + 12;
		for (AnnouncementStore.Board b : AnnouncementStore.BOARDS) {
			int w = font.width(tabLabel(b)) + 12;
			rects.add(new Rect(x, p.y() + 6, w, CHIP_HEIGHT));
			x += w + 3;
		}
		return rects;
	}

	private String tabLabel(AnnouncementStore.Board b) {
		int unread = AnnouncementStore.unread(b.key);
		return unread > 0 && !b.key.equals(board) ? b.label + " " + unread : b.label;
	}

	private Rect refreshChip() {
		Rect p = panel();
		int w = font.width("重新整理") + 12;
		return new Rect(p.x() + p.w() - 8 - w, p.y() + 6, w, CHIP_HEIGHT);
	}

	private Rect websiteChip() {
		Rect refresh = refreshChip();
		int w = font.width("網站") + 12;
		return new Rect(refresh.x() - 3 - w, refresh.y(), w, CHIP_HEIGHT);
	}

	private Rect retryChip() {
		Rect l = list();
		int w = font.width("重試") + 12;
		return new Rect(l.x() + (l.w() - w) / 2, contentBottom() + 14, w, CHIP_HEIGHT);
	}

	private int contentHeight() {
		return cards.isEmpty() ? 0 : cards.getLast().y() + cards.getLast().height();
	}

	private int contentBottom() {
		return list().y() + contentHeight() - (int) scroll + 4;
	}

	private int colorOf(String hex, int fallback) {
		if (hex != null && hex.matches("#[0-9a-fA-F]{6}")) {
			return 0xFF000000 | Integer.parseInt(hex.substring(1), 16);
		}
		return fallback;
	}

	private Style styleOf(AnnouncementStore.Run run, String kind, boolean reveal) {
		int base = switch (kind) {
			case "h1" -> Theme.ACCENT;
			case "small" -> Theme.TEXT_SECONDARY;
			case "code" -> 0xFFC8D0D8;
			default -> Theme.TEXT;
		};
		int color = run.dim == 1 ? Theme.TEXT_DIM : run.code == 1 ? 0xFFE6C07B : colorOf(run.c, base);
		return Style.EMPTY
				.withColor(TextColor.fromRgb(color & 0xFFFFFF))
				.withBold(run.b == 1 || kind.startsWith("h"))
				.withItalic(run.i == 1)
				.withUnderlined(run.u == 1 || run.h != null)
				.withStrikethrough(run.s == 1)
				.withObfuscated(run.sp == 1 && !reveal);
	}

	private RemoteImages.Entry emoji(String src) {
		return RemoteImages.get(src, RemoteImages.requestWidth(EMOJI));
	}

	private int[] imageSize(AnnouncementStore.Line line, int maxWidth) {
		int limit = Math.max(16, line.sm == 1 ? Math.min(SMALL_IMAGE, maxWidth) : maxWidth);
		int scale = Math.max(1, minecraft.getWindow().getGuiScale());
		int sourceWidth = line.w;
		int sourceHeight = line.h;
		if (sourceWidth <= 0 || sourceHeight <= 0) {
			RemoteImages.Entry probe = RemoteImages.get(line.src, RemoteImages.requestWidth(limit));
			if (probe.state != RemoteImages.State.READY) {
				int w = Math.min(limit, 160);
				return new int[] { w, w * 9 / 16, RemoteImages.requestWidth(limit) };
			}
			sourceWidth = probe.width;
			sourceHeight = probe.height;
		}
		int w = Math.min(limit, Math.max(24, sourceWidth / scale));
		int h = Math.max(8, w * sourceHeight / sourceWidth);
		if (h > MAX_IMAGE_HEIGHT) {
			h = MAX_IMAGE_HEIGHT;
			w = Math.max(16, h * sourceWidth / sourceHeight);
		}
		int request = line.w > 0 && line.h > 0 ? RemoteImages.requestWidth(w) : RemoteImages.requestWidth(limit);
		return new int[] { w, h, request };
	}

	private void layout() {
		AnnouncementStore.BoardState state = board == null ? null : AnnouncementStore.state(board);
		int width = list().w() - 4 - PAD * 2;
		if (state == null) {
			cards = List.of();
			return;
		}
		if (width == layoutWidth && state.version == layoutVersion && revealed.size() == layoutReveals && board.equals(layoutBoard)
				&& RemoteImages.version() == layoutImages) {
			return;
		}
		layoutImages = RemoteImages.version();
		layoutWidth = width;
		layoutVersion = state.version;
		layoutReveals = revealed.size();
		layoutBoard = board;

		List<Card> result = new ArrayList<>();
		int y = 0;
		for (AnnouncementStore.Message message : state.messages) {
			List<Row> rows = new ArrayList<>();
			int rowY = PAD + LINE + 4;
			boolean reveal = revealed.contains(message.id);
			for (AnnouncementStore.Line line : message.lines) {
				if ("gap".equals(line.k)) {
					rowY += 5;
					continue;
				}
				if (line.k.startsWith("h") && !rows.isEmpty()) {
					rowY += 3;
				}
				List<Integer> bars = new ArrayList<>();
				if (line.q == 1) {
					bars.add(QUOTE);
				}
				if (line.bar != null) {
					bars.add(colorOf(line.bar, QUOTE));
				}
				int indent = bars.size() * BAR_STEP;
				if ("img".equals(line.k)) {
					int[] size = imageSize(line, width - indent);
					rows.add(new Row(rowY, size[1], indent, line.k, bars, null, List.of(), line, size[0], size[2]));
					rowY += size[1] + 3;
					continue;
				}
				String marker = "li".equals(line.k) && line.n != null ? line.n + " " : "";
				int markerX = indent + Math.max(0, line.d) * 10;
				int hang = markerX + (marker.isEmpty() ? 0 : font.width(marker));
				int available = Math.max(20, width - hang - ("poll".equals(line.k) ? 8 : 0));
				int textX = hang + ("poll".equals(line.k) ? 4 : 0);

				List<List<Segment>> wrapped = new ArrayList<>();
				List<Segment> current = new ArrayList<>();
				int x = 0;
				for (AnnouncementStore.Run run : line.r) {
					Style style = styleOf(run, line.k, reveal);
					if (run.e != null && emoji(run.e).state != RemoteImages.State.FAILED) {
						if (x > 0 && x + EMOJI_ADVANCE > available) {
							wrapped.add(current);
							current = new ArrayList<>();
							x = 0;
						}
						current.add(new Segment(textX + x, EMOJI_ADVANCE, run.t, style, run));
						x += EMOJI_ADVANCE;
						continue;
					}
					String rest = run.t == null ? "" : run.t;
					while (!rest.isEmpty()) {
						int fit = font.getSplitter().plainIndexAtWidth(rest, available - x, style);
						if (fit >= rest.length()) {
							int w = font.width(Component.literal(rest).withStyle(style));
							current.add(new Segment(textX + x, w, rest, style, run));
							x += w;
							break;
						}
						if (fit <= 0) {
							if (x == 0) {
								fit = 1;
							} else {
								wrapped.add(current);
								current = new ArrayList<>();
								x = 0;
								continue;
							}
						}
						int space = rest.lastIndexOf(' ', fit);
						int take = space > 0 && rest.substring(0, fit).chars().allMatch(ch -> ch < 0x2E80) ? space + 1 : fit;
						String piece = rest.substring(0, take);
						int w = font.width(Component.literal(piece).withStyle(style));
						current.add(new Segment(textX + x, w, piece, style, run));
						rest = rest.substring(take);
						wrapped.add(current);
						current = new ArrayList<>();
						x = 0;
					}
				}
				if (!current.isEmpty() || wrapped.isEmpty()) {
					wrapped.add(current);
				}
				for (int i = 0; i < wrapped.size(); i++) {
					List<Segment> segments = new ArrayList<>(wrapped.get(i));
					if (i == 0 && !marker.isEmpty()) {
						segments.addFirst(new Segment(markerX, font.width(marker), marker, Style.EMPTY.withColor(TextColor.fromRgb(Theme.TEXT_DIM & 0xFFFFFF)), new AnnouncementStore.Run()));
					}
					int height = "poll".equals(line.k) ? LINE + 4 : LINE;
					rows.add(new Row(rowY, height, indent, line.k, bars, "poll".equals(line.k) ? line.p : null, segments, null, 0, 0));
					rowY += height + ("poll".equals(line.k) ? 2 : 0);
				}
			}
			int height = rowY + PAD - 2;
			result.add(new Card(message, y, height, rows));
			y += height + CARD_GAP;
		}
		cards = result;
	}

	//? if >=26.1 {
	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		draw(new Gfx(graphics), mouseX, mouseY);
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}
	//?} else {
	/*@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		draw(new Gfx(graphics), mouseX, mouseY);
		super.render(graphics, mouseX, mouseY, partialTick);
	}
	*///?}

	private void drawChip(Gfx gfx, Rect chip, String label, int color, boolean active, int mouseX, int mouseY) {
		int background = active ? Theme.SELECTED : chip.contains(mouseX, mouseY) ? Theme.CHIP_HOVER : Theme.CHIP;
		gfx.fill(chip.x(), chip.y(), chip.x() + chip.w(), chip.y() + chip.h(), background);
		if (active) {
			gfx.fill(chip.x(), chip.y() + chip.h() - 1, chip.x() + chip.w(), chip.y() + chip.h(), Theme.ACCENT);
		}
		gfx.text(font, label, chip.x() + 6, chip.y() + 3, color);
	}

	private void draw(Gfx gfx, int mouseX, int mouseY) {
		if (board == null && !AnnouncementStore.BOARDS.isEmpty()) {
			selectDefaultBoard();
		}
		layout();
		Rect p = panel();
		gfx.fill(p.x(), p.y(), p.x() + p.w(), p.y() + p.h(), Theme.PANEL);
		gfx.fill(p.x(), p.y(), p.x() + p.w(), p.y() + HEADER, Theme.HEADER);
		gfx.fill(p.x(), p.y() + HEADER - 1, p.x() + p.w(), p.y() + HEADER, Theme.ACCENT_DIM);
		gfx.text(font, title.getString(), p.x() + 10, p.y() + 9, Theme.ACCENT);

		List<Component> tooltip = new ArrayList<>();
		List<Rect> tabs = tabs();
		for (int i = 0; i < tabs.size(); i++) {
			AnnouncementStore.Board b = AnnouncementStore.BOARDS.get(i);
			boolean unread = AnnouncementStore.unread(b.key) > 0 && !b.key.equals(board);
			drawChip(gfx, tabs.get(i), tabLabel(b), unread ? Theme.WARNING : Theme.TEXT, b.key.equals(board), mouseX, mouseY);
		}
		drawChip(gfx, refreshChip(), "重新整理", Theme.TEXT, false, mouseX, mouseY);
		drawChip(gfx, websiteChip(), "網站", Theme.TEXT, false, mouseX, mouseY);
		if (websiteChip().contains(mouseX, mouseY)) {
			tooltip.add(Component.literal("在瀏覽器開啟輝煌官網，可看圖片與影片"));
		}

		Rect l = list();
		AnnouncementStore.BoardState state = board == null ? null : AnnouncementStore.state(board);
		scroll = Math.clamp(scroll, 0, Math.max(0, contentHeight() + 30 - l.h()));
		if (state != null && state.more && !state.loading && state.error.isEmpty() && scroll >= contentHeight() - l.h() - 40) {
			AnnouncementStore.loadMore(board, () -> {});
		}

		gfx.scissor(l.x(), l.y(), l.x() + l.w(), l.y() + l.h());
		String seenBefore = board == null ? "" : seenAtOpen.getOrDefault(board, "");
		for (Card card : cards) {
			int cy = l.y() + card.y() - (int) scroll;
			if (cy + card.height() < l.y() || cy > l.y() + l.h()) {
				continue;
			}
			drawCard(gfx, card, l.x(), cy, l.w() - 4, seenBefore, mouseX, mouseY, l.contains(mouseX, mouseY), tooltip);
		}
		drawListStatus(gfx, state, l, mouseX, mouseY);
		gfx.endScissor();

		int content = contentHeight() + 30;
		if (content > l.h()) {
			int barHeight = Math.max(16, l.h() * l.h() / content);
			int barY = l.y() + (int) ((l.h() - barHeight) * (scroll / (content - l.h())));
			gfx.fill(l.x() + l.w() - 2, barY, l.x() + l.w(), barY + barHeight, Theme.CHIP_HOVER);
		}

		gfx.fill(p.x(), p.y() + p.h() - FOOTER, p.x() + p.w(), p.y() + p.h() - FOOTER + 1, Theme.HEADER);
		gfx.text(font, "資料來源：輝煌 Discord 公告頻道 · 滾輪捲動 · Esc 返回", p.x() + 8, p.y() + p.h() - FOOTER + 4, Theme.TEXT_DIM);

		if (!tooltip.isEmpty()) {
			gfx.tooltip(font, tooltip, mouseX, mouseY);
		}
	}

	private void drawCard(Gfx gfx, Card card, int x, int y, int w, String seenBefore, int mouseX, int mouseY, boolean mouseInList, List<Component> tooltip) {
		AnnouncementStore.Message message = card.message();
		gfx.fill(x, y, x + w, y + card.height(), Theme.CARD);
		boolean isNew = !seenBefore.isEmpty() && AnnouncementStore.isNewer(message.id, seenBefore);
		if (isNew) {
			gfx.fill(x, y, x + 2, y + card.height(), Theme.ACCENT);
		}
		int hx = x + PAD;
		gfx.text(font, Component.literal(message.author).withStyle(ChatFormatting.BOLD), hx, y + PAD, Theme.TEXT);
		hx += font.width(Component.literal(message.author).withStyle(ChatFormatting.BOLD)) + 6;
		String time = TIME.format(Instant.ofEpochMilli(message.time)) + (message.edited ? "（已編輯）" : "");
		gfx.text(font, time, hx, y + PAD, Theme.TEXT_DIM);
		if (isNew) {
			String label = "NEW";
			int lw = font.width(label) + 8;
			int lx = x + w - PAD - lw;
			gfx.fill(lx, y + PAD - 2, lx + lw, y + PAD + 10, Theme.ACCENT_DIM);
			gfx.text(font, label, lx + 4, y + PAD, Theme.ACCENT);
		}

		int cx = x + PAD;
		int contentWidth = w - PAD * 2;
		for (Row row : card.rows()) {
			int ry = y + row.y();
			for (int i = 0; i < row.bars().size(); i++) {
				gfx.fill(cx + i * BAR_STEP, ry - 1, cx + i * BAR_STEP + 2, ry + row.height(), row.bars().get(i));
			}
			if ("code".equals(row.kind())) {
				gfx.fill(cx + row.indent(), ry - 1, cx + contentWidth, ry + row.height(), CODE_BG);
			}
			if (row.poll() != null) {
				int px = cx + row.indent();
				int pw = contentWidth - row.indent();
				gfx.fill(px, ry, px + pw, ry + row.height() - 1, Theme.CHIP);
				gfx.fill(px, ry, px + pw * Math.clamp(row.poll(), 0, 100) / 100, ry + row.height() - 1, Theme.ACCENT_DIM);
			}
			if (row.image() != null) {
				drawImage(gfx, row, cx + row.indent(), ry, revealed.contains(message.id), mouseX, mouseY, mouseInList, tooltip);
				continue;
			}
			int textY = ry + (row.poll() != null ? 3 : 1);
			for (Segment segment : row.segments()) {
				int sx = cx + segment.x();
				if (segment.run().code == 1 && !"code".equals(row.kind())) {
					gfx.fill(sx - 1, ry - 1, sx + segment.width() + 1, ry + row.height() - 1, CODE_BG);
				}
				boolean hovered = mouseInList && mouseX >= sx && mouseX < sx + segment.width() && mouseY >= ry - 1 && mouseY < ry + row.height();
				if (segment.run().e != null && emoji(segment.run().e).state != RemoteImages.State.FAILED) {
					RemoteImages.Entry icon = emoji(segment.run().e);
					if (icon.state == RemoteImages.State.READY) {
						gfx.image(icon.texture, sx + 1, textY - 1, EMOJI, EMOJI);
					}
					if (hovered) {
						tooltip.add(Component.literal(segment.text()));
					}
					continue;
				}
				gfx.text(font, Component.literal(segment.text()).withStyle(segment.style()), sx, textY, Theme.TEXT);
				if (hovered && segment.run().h != null) {
					tooltip.add(Component.literal(segment.run().h).withStyle(ChatFormatting.AQUA));
					tooltip.add(Component.literal("點擊開啟連結").withStyle(ChatFormatting.GRAY));
				} else if (hovered && segment.run().sp == 1 && !revealed.contains(message.id)) {
					tooltip.add(Component.literal("暴雷內容，點擊顯示"));
				}
			}
		}
	}

	private void drawImage(Gfx gfx, Row row, int x, int y, boolean reveal, int mouseX, int mouseY, boolean mouseInList, List<Component> tooltip) {
		AnnouncementStore.Line line = row.image();
		int w = row.imageWidth();
		int h = row.height();
		RemoteImages.Entry entry = RemoteImages.get(line.src, row.requestWidth());
		boolean hidden = line.sp == 1 && !reveal;
		boolean hovered = mouseInList && mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
		if (entry.state == RemoteImages.State.READY) {
			gfx.image(entry.texture, x, y, w, h);
		} else {
			gfx.fill(x, y, x + w, y + h, Theme.CHIP);
			String text = entry.state == RemoteImages.State.LOADING ? "載入中…" : "無法載入，點擊在瀏覽器開啟";
			gfx.text(font, font.plainSubstrByWidth(text, w - 8), x + Math.max(4, (w - font.width(text)) / 2), y + (h - 8) / 2, Theme.TEXT_DIM);
		}
		if (hidden) {
			gfx.fill(x, y, x + w, y + h, 0xF01A1E22);
			String text = "暴雷內容・點擊顯示";
			gfx.text(font, font.plainSubstrByWidth(text, w - 8), x + Math.max(4, (w - font.width(text)) / 2), y + (h - 8) / 2, Theme.TEXT);
		} else if (line.v == 1 && entry.state == RemoteImages.State.READY) {
			int bw = 22;
			int bh = 16;
			int bx = x + (w - bw) / 2;
			int by = y + (h - bh) / 2;
			gfx.fill(bx, by, bx + bw, by + bh, hovered ? 0xE0000000 : 0xB0000000);
			gfx.text(font, "▶", bx + (bw - font.width("▶")) / 2 + 1, by + 4, 0xFFFFFFFF);
		}
		if (!hidden && line.l != null && !line.l.isEmpty() && entry.state == RemoteImages.State.READY) {
			String label = font.plainSubstrByWidth(line.l, w - 8);
			gfx.fill(x, y + h - 11, x + font.width(label) + 6, y + h, 0xB0000000);
			gfx.text(font, label, x + 3, y + h - 9, 0xFFFFFFFF);
		}
		if (hovered) {
			gfx.fill(x, y, x + w, y + 1, Theme.ACCENT);
			gfx.fill(x, y + h - 1, x + w, y + h, Theme.ACCENT);
			if (hidden) {
				tooltip.add(Component.literal("暴雷內容，點擊顯示"));
			} else if (line.href != null) {
				tooltip.add(Component.literal(line.v == 1 ? "點擊在瀏覽器播放影片" : "點擊在瀏覽器開啟原圖"));
			}
		}
	}

	private void drawListStatus(Gfx gfx, AnnouncementStore.BoardState state, Rect l, int mouseX, int mouseY) {
		int y = contentBottom() + 2;
		String text;
		int color = Theme.TEXT_DIM;
		boolean retry = false;
		if (board == null) {
			if (AnnouncementStore.boardsLoading()) {
				text = "載入中…";
			} else if (!AnnouncementStore.boardsError().isEmpty()) {
				text = AnnouncementStore.boardsError();
				color = Theme.DANGER;
				retry = true;
			} else {
				text = "目前沒有公告頻道";
			}
		} else if (state.loading) {
			text = state.messages.isEmpty() ? "載入公告中…" : "載入更早的公告…";
		} else if (!state.error.isEmpty()) {
			text = state.error;
			color = Theme.DANGER;
			retry = true;
		} else if (state.loaded && state.messages.isEmpty()) {
			text = "這個頻道目前沒有公告";
		} else if (state.loaded && !state.more) {
			text = "已經是最早的公告了";
		} else {
			return;
		}
		if (cards.isEmpty()) {
			y = l.y() + l.h() / 3;
		}
		gfx.text(font, text, l.x() + (l.w() - font.width(text)) / 2, y, color);
		if (retry) {
			Rect chip = cards.isEmpty() ? new Rect(retryChip().x(), y + 14, retryChip().w(), CHIP_HEIGHT) : retryChip();
			drawChip(gfx, chip, "重試", Theme.TEXT, false, mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}
		if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
			return false;
		}
		double mouseX = event.x();
		double mouseY = event.y();
		if (!panel().contains(mouseX, mouseY)) {
			onClose();
			return true;
		}
		List<Rect> tabs = tabs();
		for (int i = 0; i < tabs.size(); i++) {
			if (tabs.get(i).contains(mouseX, mouseY)) {
				selectBoard(AnnouncementStore.BOARDS.get(i).key);
				return true;
			}
		}
		if (refreshChip().contains(mouseX, mouseY)) {
			if (board == null) {
				AnnouncementStore.loadBoards(this::selectDefaultBoard);
			} else {
				String key = board;
				AnnouncementStore.refresh(key, () -> AnnouncementStore.markSeen(key));
			}
			return true;
		}
		if (websiteChip().contains(mouseX, mouseY)) {
			ConfirmLinkScreen.confirmLinkNow(this, URI.create(RPConfig.WEBSITE_URL));
			return true;
		}
		Rect l = list();
		if (!l.contains(mouseX, mouseY)) {
			return true;
		}
		Rect retry = cards.isEmpty() ? new Rect(retryChip().x(), l.y() + l.h() / 3 + 14, retryChip().w(), CHIP_HEIGHT) : retryChip();
		if (retry.contains(mouseX, mouseY)) {
			if (board == null) {
				AnnouncementStore.loadBoards(this::selectDefaultBoard);
			} else if (AnnouncementStore.state(board).messages.isEmpty()) {
				String key = board;
				AnnouncementStore.refresh(key, () -> AnnouncementStore.markSeen(key));
			} else {
				AnnouncementStore.loadMore(board, () -> {});
			}
			return true;
		}
		int cx = l.x() + PAD;
		for (Card card : cards) {
			int cy = l.y() + card.y() - (int) scroll;
			if (mouseY < cy || mouseY >= cy + card.height()) {
				continue;
			}
			for (Row row : card.rows()) {
				int ry = cy + row.y();
				if (mouseY < ry - 1 || mouseY >= ry + row.height()) {
					continue;
				}
				if (row.image() != null) {
					int ix = cx + row.indent();
					if (mouseX >= ix && mouseX < ix + row.imageWidth()) {
						if (row.image().sp == 1 && !revealed.contains(card.message().id)) {
							revealed.add(card.message().id);
						} else if (row.image().href != null) {
							openLink(row.image().href);
						}
					}
					return true;
				}
				for (Segment segment : row.segments()) {
					int sx = cx + segment.x();
					if (mouseX < sx || mouseX >= sx + segment.width()) {
						continue;
					}
					if (segment.run().h != null) {
						openLink(segment.run().h);
					} else if (segment.run().sp == 1) {
						revealed.add(card.message().id);
					}
					return true;
				}
			}
			return true;
		}
		return true;
	}

	private void openLink(String url) {
		try {
			URI uri = URI.create(url);
			if ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) {
				ConfirmLinkScreen.confirmLinkNow(this, uri);
			}
		} catch (IllegalArgumentException ignored) {
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		scroll -= scrollY * LINE * 3;
		return true;
	}

	@Override
	public void onClose() {
		if (board != null) {
			AnnouncementStore.markSeen(board);
		}
		CommandMenuScreen.setScreen(parent);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
