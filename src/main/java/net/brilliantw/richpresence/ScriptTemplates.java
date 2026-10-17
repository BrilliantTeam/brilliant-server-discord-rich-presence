package net.brilliantw.richpresence;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ScriptTemplates {
	public record Template(String name, String description, Supplier<Scripts.Group> build) {
		public String code() {
			return Scripts.export(build.get(), List.of(), List.of());
		}
	}

	public static final List<Template> ALL = List.of(
			new Template("掛機自動回覆", "收到私訊時自動回覆，別人不會以為你不理他", () -> group("掛機自動回覆",
					stack(b("ev_private"), b("wait_random", "1", "3"), b("reply", "我現在掛機中，晚點回你！")))),
			new Template("喝水提醒", "每 30 分鐘在畫面中央提醒你休息一下", () -> group("喝水提醒",
					stack(b("ev_timer", "1800"), b("sound", "鈴"), b("title", "喝水時間！", "已經玩 30 分鐘了")))),
			new Template("私訊通知", "收到私訊時播放音效，並在畫面下方顯示是誰", () -> group("私訊通知",
					stack(b("ev_private"), b("sound", "叮"), b("actionbar", join("收到 ", join(b("s_sender"), " 的私訊")))))),
			new Template("指定玩家上線通知", "Tab 列表出現指定的玩家時提醒你，把「玩家名稱」改成對方的遊戲名稱", () -> group("指定玩家上線通知",
					stack(b("ev_player_join", "玩家名稱"), b("sound", "升級"), b("local_color", "綠", join(b("s_sender"), " 上線了！"))))),
			new Template("低血量警報", "生命值掉到 6 以下時跳出大字提醒", () -> group("低血量警報",
					stack(b("ev_health", "6"), b("sound", "鈴"), b("title", "生命值危險！", join("剩下 ", b("s_health")))))),
			new Template("背包滿了提醒", "背包塞滿時提醒你回家整理", () -> group("背包滿了提醒",
					stack(b("ev_full"), b("sound", "鈴"), b("actionbar", "背包滿了，記得回家整理")))),
			new Template("收購關鍵字通知", "聊天中出現「收購」時發出音效，並用黃色重點顯示", () -> group("收購關鍵字通知",
					stack(b("ev_message", "收購"), b("sound", "叮"), b("local_color", "黃", join(b("s_sender"), join("：", b("s_message"))))))),
			new Template("交易頻道輪播公告", "每 20 分鐘輪流在交易頻道送出清單裡的公告（配合交易頻道冷卻，用到清單）", ScriptTemplates::rotation),
			new Template("上線天數統計", "每天第一次上線時累計天數，重開遊戲也會記得（用到變數存檔）", ScriptTemplates::days),
			new Template("自訂積木範例", "把「大字提醒」做成自己的積木，按腳本鍵 1 呼叫它", ScriptTemplates::procedure),
			new Template("被提到時通知", "聊天中有人提到你的名字時發出音效並跳出大字", () -> group("被提到時通知",
					stack(b("ev_mention"), b("sound", "鈴"), b("title", "有人提到你！", join(b("s_sender"), join("：", b("s_message"))))))),
			new Template("進入領地提示", "走進別人的領地時，在畫面下方顯示領地名稱與擁有者", () -> group("進入領地提示",
					stack(b("ev_res_enter", "任何領地"), b("actionbar", join("你在 ", join(b("s_sender"), join(" 的 ", join(b("s_message"), " 領地")))))))),
			new Template("切換分流通知", "切換分流後在聊天欄提示你目前在哪個分流", () -> group("切換分流通知",
					stack(b("ev_server"), b("local_color", "藍", join("已切換到 ", b("s_server")))))),
			new Template("整點報時", "每到整點播放音效並提醒現在幾點", () -> group("整點報時",
					stack(b("ev_timer", "60"), body(b("if", b("op_eq", b("s_time", "分"), "0")),
							b("sound", "升級"), b("local_color", "黃", join("現在 ", join(b("s_time", "時"), " 點整"))))))),
			new Template("飢餓提醒", "每 30 秒檢查一次，飢餓值低於 7 時提醒你吃東西", () -> group("飢餓提醒",
					stack(b("ev_timer", "30"), body(b("if", b("op_lt", b("s_food"), "7")),
							b("sound", "叮"), b("actionbar", join("肚子餓了（", join(b("s_food"), "／20），記得吃東西"))))))),
			new Template("收購紀錄清單", "把聊天中的收購訊息記到清單，按腳本鍵 5 一次列出（用到清單）", ScriptTemplates::buyLog),
			new Template("自動挖掘 60 秒", "按腳本鍵 2 後自動按住左鍵挖掘 60 秒", () -> group("自動挖掘 60 秒",
					stack(b("ev_key", "2"), b("actionbar", "開始自動挖掘 60 秒"), b("act_hold", "攻擊／挖掘", "60"), b("actionbar", "自動挖掘結束")))),
			new Template("自動跑圖 30 秒", "按腳本鍵 3 後自動衝刺前進 30 秒，結束時放開按鍵", () -> group("自動跑圖 30 秒",
					stack(b("ev_key", "3"), b("act_press", "衝刺"), b("act_hold", "前進", "30"), b("act_release_all")))),
			new Template("一鍵開選單點選", "按腳本鍵 4 執行指令開啟選單並點第 13 格，把「menu」換成伺服器的選單指令", () -> group("一鍵開選單點選",
					stack(b("ev_key", "4"), b("command", "menu"), b("gui_click_slot", "13", "左鍵")))));

	private static Scripts.Group buyLog() {
		Scripts.Group group = group("收購紀錄清單",
				stack(b("ev_message", "收購"),
						body(b("if", b("op_gt", b("list_length", "收購紀錄"), "49")), b("list_delete", "收購紀錄", "1")),
						b("list_add", join(b("s_sender"), join("：", b("s_message"))), "收購紀錄")),
				stack(b("ev_key", "5"), b("local_color", "黃", join("收購紀錄：", b("list_all", "收購紀錄")))));
		group.lists.add("收購紀錄");
		return group;
	}

	private ScriptTemplates() {}

	private static Scripts.Block b(String op, Object... args) {
		Scripts.Block block = new Scripts.Block();
		block.op = op;
		ScriptBlocks.Def def = ScriptBlocks.get(op);
		List<ScriptBlocks.Slot> slots = def.slots();
		for (int i = 0; i < slots.size(); i++) {
			Scripts.Arg arg = new Scripts.Arg(slots.get(i).value());
			if (i < args.length && args[i] instanceof Scripts.Block nested) {
				arg.block = nested;
			} else if (i < args.length) {
				arg.text = String.valueOf(args[i]);
			}
			block.args.add(arg);
		}
		return block;
	}

	private static Scripts.Block join(Object a, Object b) {
		return b("op_join", a, b);
	}

	private static Scripts.Block body(Scripts.Block block, Scripts.Block... inner) {
		block.body = new ArrayList<>(List.of(inner));
		return block;
	}

	private static Scripts.Stack stack(Scripts.Block... blocks) {
		return new Scripts.Stack(10, 10, new ArrayList<>(List.of(blocks)));
	}

	private static Scripts.Group group(String name, Scripts.Stack... stacks) {
		Scripts.Group group = new Scripts.Group(name);
		int y = 10;
		for (Scripts.Stack stack : stacks) {
			stack.y = y;
			y += 30 + Scripts.count(stack.blocks) * 14;
			group.stacks.add(stack);
		}
		return group;
	}

	private static Scripts.Group rotation() {
		Scripts.Block loop = body(b("forever"),
				b("var_change", "第幾則", "1"),
				body(b("if", b("op_gt", b("var_get", "第幾則"), b("list_length", "公告"))), b("var_set", "第幾則", "1")),
				b("say_channel", "交易", b("list_item", "公告", b("var_get", "第幾則"))),
				b("wait", "1210"));
		Scripts.Group group = group("交易頻道輪播公告", stack(b("ev_start"),
				b("list_clear", "公告"),
				b("list_add", "出售鑽石，有需要私訊我", "公告"),
				b("list_add", "收購村民交易所，價格好談", "公告"),
				b("var_set", "第幾則", "0"),
				loop));
		group.variables.add("第幾則");
		group.lists.add("公告");
		return group;
	}

	private static Scripts.Group days() {
		Scripts.Group group = group("上線天數統計", stack(b("ev_join"),
				body(b("if", b("op_not", b("op_eq", b("var_get", "上次上線"), b("s_date")))),
						b("var_change", "上線天數", "1"),
						b("var_set", "上次上線", b("s_date")),
						b("local_color", "綠", join("今天是你第 ", join(b("var_get", "上線天數"), " 天上線！"))))));
		group.variables.add("上線天數");
		group.variables.add("上次上線");
		return group;
	}

	private static Scripts.Group procedure() {
		return group("自訂積木範例",
				stack(b("proc_def", "大字提醒"), b("sound", "升級"), b("title", b("proc_arg", "1"), b("proc_arg", "2"))),
				stack(b("ev_key", "1"), b("proc_call", "大字提醒", "記得回家", "按下腳本鍵 1 觸發")));
	}
}
