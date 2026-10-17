package net.brilliantw.richpresence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class UpdateHelper {
	private static final int ATTEMPTS = 120;
	private static final long RETRY_MS = 500;

	private UpdateHelper() {}

	public static void main(String[] args) throws Exception {
		long pid = Long.parseLong(args[0]);
		Path oldJar = Path.of(args[1]);
		Path newJar = Path.of(args[2]);
		Path target = Path.of(args[3]);
		ProcessHandle.of(pid).ifPresent(game -> game.onExit().join());
		System.out.println("game exited, replacing " + oldJar + " with " + target);
		if (!install(oldJar, newJar, target)) {
			System.out.println("old jar is still locked, update skipped");
			System.exit(1);
		}
		System.out.println("update installed");
	}

	static boolean install(Path oldJar, Path newJar, Path target) throws IOException, InterruptedException {
		for (int i = 0; i < ATTEMPTS; i++) {
			try {
				Files.deleteIfExists(oldJar);
				break;
			} catch (IOException e) {
				Thread.sleep(RETRY_MS);
			}
		}
		if (Files.exists(oldJar)) {
			return false;
		}
		Path part = target.resolveSibling(target.getFileName() + ".part");
		Files.copy(newJar, part, StandardCopyOption.REPLACE_EXISTING);
		Files.move(part, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		return true;
	}
}
