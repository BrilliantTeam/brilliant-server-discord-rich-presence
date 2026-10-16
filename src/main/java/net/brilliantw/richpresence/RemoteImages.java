package net.brilliantw.richpresence;

import com.mojang.blaze3d.platform.NativeImage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class RemoteImages {
	public enum State { LOADING, READY, FAILED }

	public static final class Entry {
		public State state = State.LOADING;
		public Identifier texture;
		public int width;
		public int height;
	}

	private static final String MEDIA_PREFIX = "/api/announcements/media?";
	private static final int MAX_ENTRIES = 96;
	private static final int MAX_BYTES = 8 * 1024 * 1024;
	private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
	private static final Map<String, Entry> CACHE = new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
			if (size() <= MAX_ENTRIES) {
				return false;
			}
			if (eldest.getValue().texture != null) {
				Minecraft.getInstance().getTextureManager().release(eldest.getValue().texture);
			}
			return true;
		}
	};
	private static int counter;
	private static int version;

	private RemoteImages() {}

	public static int version() {
		return version;
	}

	public static int requestWidth(int guiWidth) {
		int pixels = (int) Math.ceil(guiWidth * (double) Minecraft.getInstance().getWindow().getGuiScale());
		return Math.clamp((pixels + 15) / 16 * 16, 16, 1024);
	}

	public static Entry get(String src, int requestWidth) {
		String key = src + "|" + requestWidth;
		Entry entry = CACHE.get(key);
		if (entry != null) {
			return entry;
		}
		Entry created = new Entry();
		CACHE.put(key, created);
		if (src == null || !src.startsWith(MEDIA_PREFIX)) {
			created.state = State.FAILED;
			return created;
		}
		HttpRequest request = HttpRequest.newBuilder(URI.create(RPConfig.WEBSITE_URL + src + "&w=" + requestWidth))
				.timeout(Duration.ofSeconds(20))
				.header("User-Agent", "Brilliant-Server-Mod")
				.GET()
				.build();
		HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray()).whenComplete((response, error) -> Minecraft.getInstance().execute(() -> {
			boolean ok = error == null && response.statusCode() == 200 && response.body().length <= MAX_BYTES;
			if (ok && CACHE.get(key) == created) {
				try {
					NativeImage image = NativeImage.read(response.body());
					created.width = image.getWidth();
					created.height = image.getHeight();
					created.texture = Identifier.fromNamespaceAndPath("brilliant-server-mod", "announcement/" + counter++);
					Minecraft.getInstance().getTextureManager().register(created.texture, new DynamicTexture(() -> "brilliant-server-mod announcement image", image));
					created.state = State.READY;
				} catch (Exception e) {
					created.texture = null;
					created.state = State.FAILED;
				}
			} else {
				created.state = State.FAILED;
			}
			version++;
		}));
		return created;
	}
}
