package com.xxmicloxx.NoteBlockAPI.utils;

import org.bukkit.Location;

public class MathUtils {

	private static final double[] COS = new double[360];
	private static final double[] SIN = new double[360];

	static {
		for (int deg = 0; deg < 360; deg++) {
			final double radians = Math.toRadians(deg);
			COS[deg] = Math.cos(radians);
			SIN[deg] = Math.sin(radians);
		}
	}
	
	private static double[] getCos(){
		return COS;
	}
	
	private static double[] getSin(){
		return SIN;
	}
	
	public static Location stereoSourceLeft(Location location, float distance) {
		float yaw = location.getYaw();
		final int index = (int) (yaw + 360) % 360;
		return location.clone().add(-getCos()[index] * distance, 0, -getSin()[index] * distance);
	}
	public static Location stereoSourceRight(Location location, float distance) {
	    float yaw = location.getYaw();
		final int index = (int) (yaw + 360) % 360;
		return location.clone().add(getCos()[index] * distance, 0, getSin()[index] * distance);
	}
	
}
