package net.brilliantw.richpresence;

import java.util.stream.Collectors;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

public final class RichPresenceModClient implements ClientModInitializer {
	private final DiscordPresenceManager presence = new DiscordPresenceManager();

	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			String host = brilliantHost(client);
			if (host != null) {
				startPresenceFor(host);
				AnnouncementStore.refreshAll(RichPresenceModClient::notifyUnread);
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> presence.stop());
		DiscordImageLinks.register();
	}

	public static boolean isOnBrilliantServer() {
		return brilliantHost(Minecraft.getInstance()) != null;
	}

	private static String brilliantHost(Minecraft client) {
		ServerData server = client.getCurrentServer();
		if (server == null || server.ip == null) {
			return null;
		}

		String host = server.ip.toLowerCase().split(":")[0];
		return host.equals(RPConfig.BASE_DOMAIN) || host.endsWith("." + RPConfig.BASE_DOMAIN) ? host : null;
	}

	private static void notifyUnread(int unread) {
		Minecraft client = Minecraft.getInstance();
		if (unread <= 0 || client.player == null || !isOnBrilliantServer()) {
			return;
		}
		String boards = AnnouncementStore.BOARDS.stream()
				.filter(board -> AnnouncementStore.unread(board.key) > 0)
				.map(board -> board.label)
				.collect(Collectors.joining("、"));
		Component message = Component.literal("｜").withStyle(ChatFormatting.GRAY)
				.append(Component.literal("公告").withStyle(ChatFormatting.GOLD))
				.append(Component.literal("｜").withStyle(ChatFormatting.GRAY))
				.append(Component.literal("有 " + unread + " 則新公告（" + boards + "），打開聊天欄點右下角「公告」查看。").withStyle(ChatFormatting.WHITE));
		//? if >=26.1 {
		client.player.sendSystemMessage(message);
		//?} else {
		/*client.player.displayClientMessage(message, false);
		*///?}
	}

	private void startPresenceFor(String host) {
		String label = RPConfig.SERVER_LABELS.getOrDefault(host, RPConfig.SERVER_LABELS.get(RPConfig.BASE_DOMAIN));
		String smallImageKey = RPConfig.SERVER_IMAGE_KEYS.getOrDefault(host, RPConfig.DEFAULT_IMAGE_KEY);
		String state = host + "｜" + RPConfig.MINECRAFT_VERSION;
		presence.start(label, state, smallImageKey);
	}
}
