package net.brilliantw.richpresence;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.ActivityType;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.google.gson.JsonObject;

public final class DiscordPresenceManager {
	private IPCClient client;
	private String details;
	private String state;
	private String smallImageKey;

	public void start(String details, String state, String smallImageKey) {
		stop();
		this.details = details;
		this.state = state;
		this.smallImageKey = smallImageKey;
		client = new IPCClient(RPConfig.CLIENT_ID);
		client.setListener(new IPCListener() {
			@Override
			public void onReady(IPCClient readyClient) {
				readyClient.sendRichPresence(buildPresence());
			}

			@Override
			public void onPacketSent(IPCClient c, Packet packet) {
			}

			@Override
			public void onPacketReceived(IPCClient c, Packet packet) {
			}

			@Override
			public void onActivityJoin(IPCClient c, String secret) {
			}

			@Override
			public void onActivitySpectate(IPCClient c, String secret) {
			}

			@Override
			public void onActivityJoinRequest(IPCClient c, String secret, User user) {
			}

			@Override
			public void onClose(IPCClient c, JsonObject json) {
			}

			@Override
			public void onDisconnect(IPCClient c, Throwable t) {
			}
		});

		Thread connectThread = new Thread(() -> {
			try {
				client.connect();
			} catch (Exception e) {
				System.err.println("[brilliantw-richpresence] Failed to connect to Discord: " + e.getMessage());
			}
		}, "brilliantw-richpresence-connect");
		connectThread.setDaemon(true);
		connectThread.start();
	}

	public void stop() {
		if (client != null) {
			IPCClient closing = client;
			client = null;
			try {
				closing.close();
			} catch (Exception ignored) {
			}
		}
	}

	private RichPresence buildPresence() {
		return new RichPresence.Builder()
				.setActivityType(ActivityType.Playing)
				.setDetails(details)
				.setState(state)
				.setStartTimestamp(System.currentTimeMillis() / 1000L)
				.setLargeImage(RPConfig.LARGE_IMAGE_KEY, RPConfig.LARGE_IMAGE_TEXT)
				.setSmallImage(smallImageKey, RPConfig.SMALL_IMAGE_TEXT)
				.build();
	}
}
