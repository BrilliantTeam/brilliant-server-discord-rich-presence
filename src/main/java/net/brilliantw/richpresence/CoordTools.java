package net.brilliantw.richpresence;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class CoordTools {
	private CoordTools() {}

	static String dimensionName(ResourceKey<Level> dimension) {
		if (Level.OVERWORLD.equals(dimension)) {
			return "主世界";
		}
		if (Level.NETHER.equals(dimension)) {
			return "地獄";
		}
		if (Level.END.equals(dimension)) {
			return "終界";
		}
		return "其他世界";
	}

	static String converted(ResourceKey<Level> dimension, int x, int z) {
		if (Level.OVERWORLD.equals(dimension)) {
			return "（對應地獄 " + Math.floorDiv(x, 8) + ", " + Math.floorDiv(z, 8) + "）";
		}
		if (Level.NETHER.equals(dimension)) {
			return "（對應主世界 " + x * 8 + ", " + z * 8 + "）";
		}
		return "";
	}

	public static String shareText() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return "";
		}
		BlockPos pos = player.blockPosition();
		ResourceKey<Level> dimension = player.level().dimension();
		String server = RichPresenceModClient.serverName();
		return "座標：" + (server.isEmpty() ? "" : server + " ") + dimensionName(dimension)
				+ " " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + converted(dimension, pos.getX(), pos.getZ());
	}
}
