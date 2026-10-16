package net.brilliantw.richpresence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
//? if >=26.3 {
/*import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDL_DialogFileCallback;
import org.lwjgl.system.MemoryUtil;
*///?} else {
import org.lwjgl.util.tinyfd.TinyFileDialogs;
//?}

public final class FilePicker {
	private FilePicker() {}

	public static void openTextFile(String title, Consumer<Path> onPicked) {
		Minecraft minecraft = Minecraft.getInstance();
		//? if >=26.3 {
		/*SDL_DialogFileCallback[] holder = new SDL_DialogFileCallback[1];
		holder[0] = SDL_DialogFileCallback.create((userdata, fileList, filter) -> {
			String path = null;
			if (fileList != 0) {
				long first = MemoryUtil.memGetAddress(fileList);
				if (first != 0) {
					path = MemoryUtil.memUTF8(first);
				}
			}
			String picked = path;
			minecraft.execute(() -> {
				holder[0].free();
				if (picked != null) {
					onPicked.accept(Path.of(picked));
				}
			});
		});
		SDLDialog.SDL_ShowOpenFileDialog(holder[0], 0L, 0L, null, (CharSequence) null, false);
		*///?} else {
		Thread thread = new Thread(() -> {
			String path = TinyFileDialogs.tinyfd_openFileDialog(title, null, null, null, false);
			if (path != null) {
				minecraft.execute(() -> onPicked.accept(Path.of(path)));
			}
		}, "brilliant-server-mod-file-picker");
		thread.setDaemon(true);
		thread.start();
		//?}
	}

	public static String readText(Path path) throws IOException {
		if (Files.size(path) > CommandStore.MAX_IMPORT_FILE) {
			throw new IOException("檔案太大（超過 8 MB）");
		}
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}
}
