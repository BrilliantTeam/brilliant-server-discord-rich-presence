package net.brilliantw.richpresence;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;

public final class ChannelBar {
	private record Channel(String name, String prefix, int color) {}

	private record Action(String label, String command, boolean insert) {
		Action(String label, String command) {
			this(label, command, false);
		}
	}

	private record Row(String name, int color, List<Action> actions) {}

	private record Button<T>(int x, int y, int width, int height, T value) {
		boolean contains(double mouseX, double mouseY) {
			return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
		}
	}

	private static final int CHAT = 0xFFBBDDF6;
	private static final int TRADE = 0xFFF9C3F5;
	private static final int AREA = 0xFFF7E0A4;
	private static final int GUILD = 0xFFF6C236;
	private static final int RESIDENCE = 0xFFF6E0A4;
	private static final int SYSTEM = 0xFF7AD3FF;

	private static final List<Channel> CHANNELS = List.of(
			new Channel("一般", "", 0xFFFFFFFF),
			new Channel("聊天", "!", CHAT),
			new Channel("交易", "?", TRADE),
			new Channel("區域", "-", AREA),
			new Channel("公會", "+", GUILD),
			new Channel("領地", "*", RESIDENCE)
	);

	private static final List<Row> ROWS = List.of(
			new Row("聊天頻道", CHAT, List.of(new Action("切換發言", "joinchat"), new Action("讀取", "listenchat"), new Action("關閉讀取", "leavechat"))),
			new Row("交易頻道", TRADE, List.of(new Action("切換發言", "jointrade"), new Action("讀取", "listentrade"), new Action("關閉讀取", "leavetrade"))),
			new Row("區域頻道", AREA, List.of(new Action("切換發言", "joinarea"), new Action("讀取", "listenarea"), new Action("關閉讀取", "leavearea"))),
			new Row("領地頻道", RESIDENCE, List.of(new Action("進入／離開", "rc"))),
			new Row("公會頻道", GUILD, List.of(new Action("進入／離開", "gd gc"))),
			new Row("私訊", 0xFFAAAAAA, List.of(new Action("全體私訊開關", "msgtoggle"), new Action("封鎖列表", "ignore list"), new Action("私訊音效", "notify"))),
			new Row("顯示", SYSTEM, List.of(new Action("遊戲廣播", "bla"), new Action("系統訊息", "tsm")))
	);

	private static final List<Row> INTERACT_ROWS = List.of(
			new Row("展示", 0xFFCFFFC0, List.of(new Action("手上物品", "[i]", true), new Action("背包", "[inv]", true), new Action("終界箱", "[ender]", true))),
			new Row("動作", 0xFFF6E0A4, List.of(new Action("坐下", "sit"), new Action("躺下", "lay"), new Action("旋轉", "spin"), new Action("趴下", "bellyflop")))
	);

	private static final String TOGGLE_LABEL = "頻道設定";
	private static final String INTERACT_LABEL = "互動";
	private static final String COMMANDS_LABEL = "自訂指令";
	private static final String ANNOUNCEMENTS_LABEL = "公告";
	private static final String GUIDE_LABEL = "官網";
	private static final int HEIGHT = 14;
	private static final int GAP = 2;
	private static final int PADDING = 3;
	private static final int EMOJI_BUTTON = 14;

	private static int selected;
	private static List<Row> openRows;

	private ChannelBar() {}

	private static boolean active() {
		return RichPresenceModClient.isOnBrilliantServer();
	}

	public static void close() {
		openRows = null;
	}

	public static String applyPrefix(String message) {
		String prefix = CHANNELS.get(selected).prefix();
		if (prefix.isEmpty() || message.isBlank() || message.startsWith("/") || !active()) {
			return message;
		}
		return prefix + message;
	}

	private static int barY(ChatScreen screen) {
		return screen.height - 16 - HEIGHT;
	}

	private static int buttonWidth(Font font, String label) {
		return font.width(label) + 8;
	}

	private static List<Button<Integer>> chips(Font font, ChatScreen screen) {
		List<Button<Integer>> buttons = new ArrayList<>();
		int x = 2;
		for (int i = 0; i < CHANNELS.size(); i++) {
			int width = buttonWidth(font, CHANNELS.get(i).name());
			buttons.add(new Button<>(x, barY(screen), width, HEIGHT, i));
			x += width + GAP;
		}
		return buttons;
	}

	private static Button<Void> toggle(Font font, ChatScreen screen) {
		int width = buttonWidth(font, TOGGLE_LABEL);
		return new Button<>(screen.width - 2 - EMOJI_BUTTON - GAP - width, barY(screen), width, HEIGHT, null);
	}

	private static Button<Void> commandsButton(Font font, ChatScreen screen) {
		Button<Void> toggle = toggle(font, screen);
		int width = buttonWidth(font, COMMANDS_LABEL);
		return new Button<>(toggle.x() - GAP - width, toggle.y(), width, HEIGHT, null);
	}

	private static String announcementsLabel() {
		int unread = AnnouncementStore.totalUnread();
		return unread > 0 ? ANNOUNCEMENTS_LABEL + " " + unread : ANNOUNCEMENTS_LABEL;
	}

	private static Button<Void> announcementsButton(Font font, ChatScreen screen) {
		Button<Void> commands = commandsButton(font, screen);
		int width = buttonWidth(font, announcementsLabel());
		return new Button<>(commands.x() - GAP - width, commands.y(), width, HEIGHT, null);
	}

	private static Button<Void> guideButton(Font font, ChatScreen screen) {
		Button<Void> announcements = announcementsButton(font, screen);
		int width = buttonWidth(font, GUIDE_LABEL);
		return new Button<>(announcements.x() - GAP - width, announcements.y(), width, HEIGHT, null);
	}

	private static Button<Void> interactButton(Font font, ChatScreen screen) {
		Button<Void> guide = guideButton(font, screen);
		int width = buttonWidth(font, INTERACT_LABEL);
		return new Button<>(guide.x() - GAP - width, guide.y(), width, HEIGHT, null);
	}

	private static int labelWidth(Font font) {
		return openRows.stream().mapToInt(row -> font.width(row.name())).max().orElse(0) + 8;
	}

	private static Button<Void> panel(Font font, ChatScreen screen) {
		int actionsWidth = openRows.stream()
				.mapToInt(row -> row.actions().stream().mapToInt(action -> buttonWidth(font, action.label()) + GAP).sum() - GAP)
				.max().orElse(0);
		int width = PADDING * 2 + labelWidth(font) + actionsWidth;
		int height = PADDING * 2 + openRows.size() * (HEIGHT + GAP) - GAP;
		return new Button<>(screen.width - 2 - width, barY(screen) - 2 - height, width, height, null);
	}

	private static List<Button<Action>> actions(Font font, Button<Void> panel) {
		List<Button<Action>> buttons = new ArrayList<>();
		for (int i = 0; i < openRows.size(); i++) {
			int x = panel.x() + PADDING + labelWidth(font);
			int y = panel.y() + PADDING + i * (HEIGHT + GAP);
			for (Action action : openRows.get(i).actions()) {
				int width = buttonWidth(font, action.label());
				buttons.add(new Button<>(x, y, width, HEIGHT, action));
				x += width + GAP;
			}
		}
		return buttons;
	}

	//? if >=26.1 {
	public static void render(ChatScreen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
	//?} else {
	/*public static void render(ChatScreen screen, GuiGraphics graphics, int mouseX, int mouseY) {
	*///?}
		if (!active()) {
			return;
		}
		Font font = Minecraft.getInstance().font;
		Component tooltip = null;

		for (Button<Integer> chip : chips(font, screen)) {
			Channel channel = CHANNELS.get(chip.value());
			boolean hovered = chip.contains(mouseX, mouseY);
			int background = chip.value() == selected ? 0xD0505050 : hovered ? 0xA0303030 : 0x80000000;
			graphics.fill(chip.x(), chip.y(), chip.x() + chip.width(), chip.y() + chip.height(), background);
			if (chip.value() == selected) {
				graphics.fill(chip.x(), chip.y() + chip.height() - 1, chip.x() + chip.width(), chip.y() + chip.height(), channel.color());
			}
			EmojiPicker.text(graphics, font, channel.name(), chip.x() + 4, chip.y() + 3, channel.color());
			if (hovered) {
				tooltip = Component.literal(channel.prefix().isEmpty()
						? "照目前所在頻道發言"
						: "快速發言：送出時自動在訊息前加上 " + channel.prefix());
			}
		}

		Button<Void> toggle = toggle(font, screen);
		boolean toggleHovered = toggle.contains(mouseX, mouseY);
		graphics.fill(toggle.x(), toggle.y(), toggle.x() + toggle.width(), toggle.y() + toggle.height(), openRows == ROWS || toggleHovered ? 0xC0505050 : 0x80000000);
		EmojiPicker.text(graphics, font, TOGGLE_LABEL, toggle.x() + 4, toggle.y() + 3, SYSTEM);

		Button<Void> commands = commandsButton(font, screen);
		boolean commandsHovered = commands.contains(mouseX, mouseY);
		graphics.fill(commands.x(), commands.y(), commands.x() + commands.width(), commands.y() + commands.height(), commandsHovered ? 0xC0505050 : 0x80000000);
		EmojiPicker.text(graphics, font, COMMANDS_LABEL, commands.x() + 4, commands.y() + 3, 0xFFCFFFC0);
		if (commandsHovered) {
			tooltip = Component.literal("開啟自訂指令選單");
		}

		Button<Void> announcements = announcementsButton(font, screen);
		boolean announcementsHovered = announcements.contains(mouseX, mouseY);
		int unread = AnnouncementStore.totalUnread();
		graphics.fill(announcements.x(), announcements.y(), announcements.x() + announcements.width(), announcements.y() + announcements.height(), announcementsHovered ? 0xC0505050 : 0x80000000);
		EmojiPicker.text(graphics, font, announcementsLabel(), announcements.x() + 4, announcements.y() + 3, unread > 0 ? 0xFFF6E0A4 : 0xFFFFFFFF);
		if (announcementsHovered) {
			tooltip = Component.literal(unread > 0 ? "有 " + unread + " 則新公告" : "查看伺服器公告");
		}

		Button<Void> guide = guideButton(font, screen);
		boolean guideHovered = guide.contains(mouseX, mouseY);
		graphics.fill(guide.x(), guide.y(), guide.x() + guide.width(), guide.y() + guide.height(), guideHovered ? 0xC0505050 : 0x80000000);
		EmojiPicker.text(graphics, font, GUIDE_LABEL, guide.x() + 4, guide.y() + 3, 0xFF7AD3FF);
		if (guideHovered) {
			tooltip = Component.literal("搜尋並閱讀輝煌官網文章");
		}

		Button<Void> interact = interactButton(font, screen);
		boolean interactHovered = interact.contains(mouseX, mouseY);
		graphics.fill(interact.x(), interact.y(), interact.x() + interact.width(), interact.y() + interact.height(), openRows == INTERACT_ROWS || interactHovered ? 0xC0505050 : 0x80000000);
		EmojiPicker.text(graphics, font, INTERACT_LABEL, interact.x() + 4, interact.y() + 3, 0xFFCFFFC0);
		if (interactHovered) {
			tooltip = Component.literal("展示物品、坐下躺下等快捷動作");
		}

		if (openRows != null) {
			Button<Void> panel = panel(font, screen);
			graphics.fill(panel.x(), panel.y(), panel.x() + panel.width(), panel.y() + panel.height(), 0xD0000000);
			for (int i = 0; i < openRows.size(); i++) {
				Row row = openRows.get(i);
				EmojiPicker.text(graphics, font, row.name(), panel.x() + PADDING + 2, panel.y() + PADDING + i * (HEIGHT + GAP) + 3, row.color());
			}
			for (Button<Action> button : actions(font, panel)) {
				boolean hovered = button.contains(mouseX, mouseY);
				graphics.fill(button.x(), button.y(), button.x() + button.width(), button.y() + button.height(), hovered ? 0x60FFFFFF : 0x30FFFFFF);
				EmojiPicker.text(graphics, font, button.value().label(), button.x() + 4, button.y() + 3, 0xFFFFFFFF);
				if (hovered) {
					tooltip = Component.literal(button.value().insert() ? "在聊天欄插入 " + button.value().command() : "/" + button.value().command());
				}
			}
		}

		if (tooltip != null) {
			graphics.setTooltipForNextFrame(font, tooltip, mouseX, mouseY);
		}
	}

	public static boolean mouseClicked(ChatScreen screen, double mouseX, double mouseY, int button) {
		if (!active() || button != InputConstants.MOUSE_BUTTON_LEFT) {
			return false;
		}
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		for (Button<Integer> chip : chips(font, screen)) {
			if (chip.contains(mouseX, mouseY)) {
				selected = chip.value();
				return true;
			}
		}
		if (announcementsButton(font, screen).contains(mouseX, mouseY)) {
			close();
			EmojiPicker.close();
			CommandMenuScreen.setScreen(new AnnouncementScreen(screen));
			return true;
		}
		if (guideButton(font, screen).contains(mouseX, mouseY)) {
			close();
			EmojiPicker.close();
			CommandMenuScreen.setScreen(new GuideScreen(screen));
			return true;
		}
		if (interactButton(font, screen).contains(mouseX, mouseY)) {
			openRows = openRows == INTERACT_ROWS ? null : INTERACT_ROWS;
			EmojiPicker.close();
			return true;
		}
		if (commandsButton(font, screen).contains(mouseX, mouseY)) {
			close();
			EmojiPicker.close();
			CommandMenuScreen.setScreen(new CommandMenuScreen(screen));
			return true;
		}
		if (toggle(font, screen).contains(mouseX, mouseY)) {
			openRows = openRows == ROWS ? null : ROWS;
			EmojiPicker.close();
			return true;
		}
		if (openRows == null) {
			return false;
		}
		Button<Void> panel = panel(font, screen);
		if (!panel.contains(mouseX, mouseY)) {
			return false;
		}
		for (Button<Action> action : actions(font, panel)) {
			if (!action.contains(mouseX, mouseY)) {
				continue;
			}
			if (action.value().insert()) {
				PrivateWindow.insert(screen, action.value().command());
			} else if (minecraft.player != null) {
				minecraft.player.connection.sendCommand(action.value().command());
			}
		}
		return true;
	}
}
