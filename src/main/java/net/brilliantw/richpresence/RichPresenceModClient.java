package net.brilliantw.richpresence;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

public final class RichPresenceModClient implements ClientModInitializer {
	private final DiscordPresenceManager presence = new DiscordPresenceManager();

	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			String host = brilliantHost(client);
			if (host != null) {
				startPresenceFor(host);
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> presence.stop());
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

	private void startPresenceFor(String host) {
		String label = RPConfig.SERVER_LABELS.getOrDefault(host, RPConfig.SERVER_LABELS.get(RPConfig.BASE_DOMAIN));
		String smallImageKey = RPConfig.SERVER_IMAGE_KEYS.getOrDefault(host, RPConfig.DEFAULT_IMAGE_KEY);
		String state = host + "｜" + RPConfig.MINECRAFT_VERSION;
		presence.start(label, state, smallImageKey);
	}
}
