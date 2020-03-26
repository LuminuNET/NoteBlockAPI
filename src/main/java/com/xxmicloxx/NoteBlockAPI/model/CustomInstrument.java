package com.xxmicloxx.NoteBlockAPI.model;

import org.bukkit.Sound;

/**
 * Create custom instruments from a sound file
 * 
 */
public class CustomInstrument {
	
	private byte index;
	private String name;
	private String soundFileName;
	private Sound sound;

	/**
	 * Creates a CustomInstrument
	 * @param index
	 * @param name
	 * @param soundFileName
	 */
	public CustomInstrument(byte index, String name, String soundFileName) {
		this.index = index;
		this.name = name;
		this.soundFileName = soundFileName.replaceAll(".ogg", "");
		if (this.soundFileName.equalsIgnoreCase("pling")){
			this.sound = Sound.BLOCK_NOTE_BLOCK_PLING;
		}
	}

	/**
	 * Gets index of CustomInstrument
	 * @return index
	 */
	public byte getIndex() {
		return index;
	}

	/**
	 * Gets name of CustomInstrument
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets file name of the sound
	 * @return file name
	 */
	public String getSoundFileName() {
		return soundFileName;
	}

	/**
	 * Gets the org.bukkit.Sound enum for this CustomInstrument
	 * @return org.bukkit.Sound enum
	 */
	public org.bukkit.Sound getSound() {
		return sound;
	}
}
