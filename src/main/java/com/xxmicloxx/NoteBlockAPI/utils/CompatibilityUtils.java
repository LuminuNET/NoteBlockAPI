package com.xxmicloxx.NoteBlockAPI.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.xxmicloxx.NoteBlockAPI.model.CustomInstrument;
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory;

/**
 * Fields/methods for reflection &amp; version checking
 */
public class CompatibilityUtils {

	public static final String OBC_DIR = Bukkit.getServer().getClass().getPackage().getName();
	public static final String NMS_DIR = OBC_DIR.replaceFirst("org.bukkit.craftbukkit", "net.minecraft.server");


	private static float serverVersion = -1;

	/**
	 * Returns if SoundCategory is able to be used
	 * @see org.bukkit.SoundCategory
	 * @see SoundCategory
	 * @return can use SoundCategory
	 */
	protected static boolean isSoundCategoryCompatible() {
		return getServerVersion() >= 0.0111f;
	}
	
	/**
	 * Plays a sound using NMS &amp; reflection
	 * @param player
	 * @param location
	 * @param sound
	 * @param category
	 * @param volume
	 * @param pitch
	 * 
	 * @deprecated stereo is set to false
	 */
	public static void playSound(Player player, Location location, String sound, 
			SoundCategory category, float volume, float pitch) {
		playSound(player, location, sound, category, volume, pitch, false);
	}

	/**
	 * Plays a sound using NMS &amp; reflection
	 * @param player
	 * @param location
	 * @param sound
	 * @param category
	 * @param volume
	 * @param pitch
	 */
	public static void playSound(Player player, Location location, String sound, SoundCategory category, float volume, float pitch, boolean stereo) {
        final org.bukkit.SoundCategory soundCategory = org.bukkit.SoundCategory.valueOf(category.name());
        if (stereo) {
            player.playSound(location, sound, soundCategory, volume, pitch);
        } else {
            player.playSound(MathUtils.stereoSourceLeft(location, 2), sound, soundCategory, volume, pitch);
            player.playSound(MathUtils.stereoSourceRight(location, 2), sound, soundCategory, volume, pitch);
        }
	}

	/**
	 * Plays a sound using NMS &amp; reflection
	 * @param player
	 * @param location
	 * @param sound
	 * @param category
	 * @param volume
	 * @param pitch
	 * 
	 * @deprecated stereo is set to false
	 */
	public static void playSound(Player player, Location location, Sound sound, 
			SoundCategory category, float volume, float pitch) {
		playSound(player, location, sound, category, volume, pitch, false);
	}
	
	/**
	 * Plays a sound using NMS &amp; reflection
	 * @param player
	 * @param location
	 * @param sound
	 * @param category
	 * @param volume
	 * @param pitch
	 */
	public static void playSound(Player player, Location location, Sound sound, SoundCategory category, float volume, float pitch, boolean stereo) {
        final org.bukkit.SoundCategory soundCategory = org.bukkit.SoundCategory.valueOf(category.name());
        if (stereo) {
            player.playSound(location, sound, soundCategory, volume, pitch);
        } else {
            player.playSound(MathUtils.stereoSourceLeft(location, 2), sound, soundCategory, volume, pitch);
            player.playSound(MathUtils.stereoSourceRight(location, 2), sound, soundCategory, volume, pitch);
        }
	}

	/**
	 * Return list of instuments which were added in specified version
	 * @param serverVersion 1.12 = 0.0112f, 1.14 = 0.0114f,...
	 * @return list of custom instruments, if no instuments were added in specified version returns empty list
	 */
	public static ArrayList<CustomInstrument> getVersionCustomInstruments(float serverVersion){
		ArrayList<CustomInstrument> instruments = new ArrayList<>();
		if (serverVersion == 0.0112f){
			instruments.add(new CustomInstrument((byte) 0, "Guitar", "guitar.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Flute", "flute.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Bell", "bell.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Chime", "icechime.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Xylophone", "xylobone.ogg"));
			return instruments;
		}

		if (serverVersion == 0.0114f){
			instruments.add(new CustomInstrument((byte) 0, "Iron Xylophone", "iron_xylophone.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Cow Bell", "cow_bell.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Didgeridoo", "didgeridoo.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Bit", "bit.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Banjo", "banjo.ogg"));
			instruments.add(new CustomInstrument((byte) 0, "Pling", "pling.ogg"));
			return instruments;
		}
		return instruments;
	}

	/**
	 * Return list of custom instruments based on song first custom instrument index and server version
	 * @param firstCustomInstrumentIndex
	 * @return
	 */
	public static ArrayList<CustomInstrument> getVersionCustomInstrumentsForSong(int firstCustomInstrumentIndex){
		ArrayList<CustomInstrument> instruments = new ArrayList<>();

		if (getServerVersion() < 0.0112f){
			if (firstCustomInstrumentIndex == 10) {
				instruments.addAll(getVersionCustomInstruments(0.0112f));
			} else if (firstCustomInstrumentIndex == 16){
				instruments.addAll(getVersionCustomInstruments(0.0112f));
				instruments.addAll(getVersionCustomInstruments(0.0114f));
			}
		} else if (getServerVersion() < 0.0114f){
			if (firstCustomInstrumentIndex == 16){
				instruments.addAll(getVersionCustomInstruments(0.0114f));
			}
		}

		return instruments;
	}

	/**
	 * Returns server version as float less than 1 with two digits for each version part
	 * @return e.g. 0.011401f for 1.14.1
	 */
	public static float getServerVersion(){
		if (serverVersion != -1){
			return serverVersion;
		}

		String versionInfo = Bukkit.getServer().getVersion();
		int start = versionInfo.lastIndexOf('(');
		int end = versionInfo.lastIndexOf(')');

		String[] versionParts = versionInfo.substring(start + 5, end).split("\\.");

		String versionString = "0.";
		for (String part : versionParts){
			if (part.length() == 1){
				versionString += "0";
			}

			versionString += part;
		}
		serverVersion = Float.parseFloat(versionString);
		return serverVersion;
	}

}
