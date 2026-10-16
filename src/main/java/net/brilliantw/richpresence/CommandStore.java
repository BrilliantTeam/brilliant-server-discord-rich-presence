package net.brilliantw.richpresence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.loader.api.FabricLoader;

public final class CommandStore {
	public static final class Entry {
		public String command = "";
		public String note = "";

		public Entry() {}

		public Entry(String command, String note) {
			this.command = command;
			this.note = note;
		}
	}

	public static final class Group {
		public String name = "";
		public List<Entry> commands = new ArrayList<>();

		public Group() {}

		public Group(String name) {
			this.name = name;
		}
	}

	private static final class Data {
		List<Group> groups = new ArrayList<>();
	}

	public static final int MAX_COMMAND = 256;
	public static final int MAX_NOTE = 100;
	public static final int MAX_NAME = 32;
	private static final String EXPORT_PREFIX = "BSM-CMD:";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Gson COMPACT = new Gson();
	private static CommandStore instance;

	private final Path file;
	public final List<Group> groups;

	private CommandStore(Path file, List<Group> groups) {
		this.file = file;
		this.groups = groups;
	}

	public static CommandStore get() {
		if (instance == null) {
			instance = load(FabricLoader.getInstance().getConfigDir().resolve("brilliant-server-mod-commands.json"));
		}
		return instance;
	}

	private static CommandStore load(Path file) {
		if (!Files.exists(file)) {
			return new CommandStore(file, new ArrayList<>());
		}
		try {
			return new CommandStore(file, sanitize(GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), Data.class)));
		} catch (Exception e) {
			try {
				Files.move(file, file.resolveSibling(file.getFileName() + "." + System.currentTimeMillis() + ".bak"));
			} catch (IOException ignored) {
			}
			return new CommandStore(file, new ArrayList<>());
		}
	}

	public void save() {
		try {
			Path temp = file.resolveSibling(file.getFileName() + ".tmp");
			Data data = new Data();
			data.groups = groups;
			Files.writeString(temp, GSON.toJson(data), StandardCharsets.UTF_8);
			Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			System.err.println("[brilliant-server-mod] Failed to save commands: " + e.getMessage());
		}
	}

	public String replaceAll(List<Group> incoming) {
		String backup = null;
		try {
			if (Files.exists(file)) {
				Path target = file.resolveSibling(file.getFileName() + "." + System.currentTimeMillis() + ".bak");
				Files.copy(file, target);
				backup = target.getFileName().toString();
			}
		} catch (IOException e) {
			throw new IllegalStateException("無法備份現有指令，已取消覆蓋", e);
		}
		groups.clear();
		merge(groups, incoming);
		save();
		return backup;
	}

	public static String export(List<Group> groups) {
		Data data = new Data();
		data.groups = groups;
		return EXPORT_PREFIX + Base64.getEncoder().encodeToString(COMPACT.toJson(data).getBytes(StandardCharsets.UTF_8));
	}

	public static List<Group> parse(String text) {
		String trimmed = text == null ? "" : text.trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException("請貼上指令集代碼");
		}
		boolean prefixed = trimmed.startsWith(EXPORT_PREFIX);
		Data data;
		try {
			String json = prefixed
					? new String(Base64.getDecoder().decode(trimmed.substring(EXPORT_PREFIX.length()).trim()), StandardCharsets.UTF_8)
					: trimmed;
			data = COMPACT.fromJson(json, Data.class);
		} catch (RuntimeException e) {
			throw new IllegalArgumentException(prefixed ? "代碼不完整或已損毀，請確認有從頭到尾完整複製" : "這不是有效的指令集代碼，代碼應以 " + EXPORT_PREFIX + " 開頭");
		}
		List<Group> groups = sanitize(data);
		if (groups.stream().allMatch(group -> group.commands.isEmpty())) {
			throw new IllegalArgumentException("這份指令集裡沒有任何指令");
		}
		return groups;
	}

	public record PreviewLine(String group, boolean newGroup, Entry entry, boolean added) {}

	public static List<PreviewLine> preview(List<Group> target, List<Group> incoming) {
		Map<String, Set<String>> existing = new HashMap<>();
		for (Group group : target) {
			existing.putIfAbsent(group.name, group.commands.stream().map(entry -> entry.command).collect(Collectors.toCollection(HashSet::new)));
		}
		List<PreviewLine> lines = new ArrayList<>();
		for (Group group : incoming) {
			boolean newGroup = !existing.containsKey(group.name);
			Set<String> commands = existing.computeIfAbsent(group.name, name -> new HashSet<>());
			for (Entry entry : group.commands) {
				lines.add(new PreviewLine(group.name, newGroup, entry, commands.add(entry.command)));
			}
		}
		return lines;
	}

	public static int merge(List<Group> target, List<Group> incoming) {
		int added = 0;
		for (Group group : incoming) {
			Group existing = target.stream().filter(g -> g.name.equals(group.name)).findFirst().orElse(null);
			if (existing == null) {
				existing = new Group(group.name);
				target.add(existing);
			}
			for (Entry entry : group.commands) {
				Group finalExisting = existing;
				if (finalExisting.commands.stream().noneMatch(e -> e.command.equals(entry.command))) {
					finalExisting.commands.add(entry);
					added++;
				}
			}
		}
		return added;
	}

	public static String cleanCommand(String command) {
		String cleaned = command == null ? "" : command.trim();
		while (cleaned.startsWith("/")) {
			cleaned = cleaned.substring(1).trim();
		}
		return truncate(cleaned.replaceAll("[\\r\\n]", " "), MAX_COMMAND);
	}

	public static String cleanText(String text, int max) {
		return truncate(text == null ? "" : text.trim().replaceAll("[\\r\\n]", " "), max);
	}

	private static String truncate(String text, int max) {
		return text.length() > max ? text.substring(0, max) : text;
	}

	private static List<Group> sanitize(Data data) {
		List<Group> result = new ArrayList<>();
		if (data == null || data.groups == null) {
			return result;
		}
		for (Group group : data.groups.stream().filter(Objects::nonNull).toList()) {
			String name = cleanText(group.name, MAX_NAME);
			Group clean = new Group(name.isEmpty() ? "未命名" : name);
			if (group.commands != null) {
				for (Entry entry : group.commands.stream().filter(Objects::nonNull).toList()) {
					String command = cleanCommand(entry.command);
					if (!command.isEmpty()) {
						clean.commands.add(new Entry(command, cleanText(entry.note, MAX_NOTE)));
					}
				}
			}
			result.add(clean);
		}
		return result;
	}
}
