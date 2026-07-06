package net.brilliantw.richpresence;

import java.util.Map;

public final class RPConfig {
	private RPConfig() {}

	public static final long CLIENT_ID = 1014145002496065577L;

	public static final String BASE_DOMAIN = "brilliantw.net";

	public static final Map<String, String> SERVER_LABELS = Map.ofEntries(
			Map.entry("brilliantw.net", "輝煌伺服器"),
			Map.entry("test.brilliantw.net", "輝煌伺服器"),
			Map.entry("ipv6.brilliantw.net", "輝煌伺服器"),
			Map.entry("s0.brilliantw.net", "輝煌伺服器｜時隙奉廳"),
			Map.entry("s1.brilliantw.net", "輝煌伺服器｜炒飯服"),
			Map.entry("s2.brilliantw.net", "輝煌伺服器｜書碼服"),
			Map.entry("s3.brilliantw.net", "輝煌伺服器｜羽森服"),
			Map.entry("s4.brilliantw.net", "輝煌伺服器｜恐龍服"),
			Map.entry("wt.brilliantw.net", "輝煌伺服器｜重啟等待")
	);

	public static final String LARGE_IMAGE_KEY = "brilliantirichicon";

	public static final String DEFAULT_IMAGE_KEY = "kapippi";

	public static final Map<String, String> SERVER_IMAGE_KEYS = Map.of(
			"s1.brilliantw.net", "komukumu",
			"s2.brilliantw.net", "hanawa",
			"s3.brilliantw.net", "konkon",
			"s4.brilliantw.net", "pikadon"
	);

	public static final String LARGE_IMAGE_TEXT = "brilliantw.net";
	public static final String SMALL_IMAGE_TEXT = "brilliantw.net";
}
