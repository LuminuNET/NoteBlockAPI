package com.xxmicloxx.NoteBlockAPI.utils;

import org.bukkit.Instrument;
import org.bukkit.Sound;

/**
 * Various methods for working with instruments
 */
public class InstrumentUtils {

    private static final int FIRST_CUSTOM_INSTRUMENT_INDEX = Instrument.values().length;

	/**
	 * Returns the org.bukkit.Sound enum for the current server version
	 * @param instrument
	 * @see Sound
	 * @return Sound enum (for the current server version)
	 */
	public static Sound getInstrument(byte instrument) {
        switch (instrument) {
            case 0:
                return Sound.BLOCK_NOTE_BLOCK_HARP;
            case 1:
                return Sound.BLOCK_NOTE_BLOCK_BASS;
            case 2:
                return Sound.BLOCK_NOTE_BLOCK_BASEDRUM;
            case 3:
                return Sound.BLOCK_NOTE_BLOCK_SNARE;
            case 4:
                return Sound.BLOCK_NOTE_BLOCK_HAT;
            case 5:
                return Sound.BLOCK_NOTE_BLOCK_GUITAR;
            case 6:
                return Sound.BLOCK_NOTE_BLOCK_FLUTE;
            case 7:
                return Sound.BLOCK_NOTE_BLOCK_BELL;
            case 8:
                return Sound.BLOCK_NOTE_BLOCK_CHIME;
            case 9:
                return Sound.BLOCK_NOTE_BLOCK_XYLOPHONE;
            case 10:
                return Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE;
            case 11:
                return Sound.BLOCK_NOTE_BLOCK_COW_BELL;
            case 12:
                return Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO;
            case 13:
                return Sound.BLOCK_NOTE_BLOCK_BIT;
            case 14:
                return Sound.BLOCK_NOTE_BLOCK_BANJO;
            case 15:
                return Sound.BLOCK_NOTE_BLOCK_PLING;
            default:
                return Sound.BLOCK_NOTE_BLOCK_HARP;
        }
	}

	/**
	 * Returns the name of the org.bukkit.Instrument enum for the current server version
	 * @param instrument
	 * @return Instrument enum (for the current server version)
	 */
	public static Instrument getBukkitInstrument(byte instrument) {
		switch (instrument) {
            default:
            case 0:
				return Instrument.PIANO;
			case 1:
				return Instrument.BASS_GUITAR;
			case 2:
				return Instrument.BASS_DRUM;
			case 3:
				return Instrument.SNARE_DRUM;
			case 4:
                return Instrument.STICKS;
            case 5:
                return Instrument.GUITAR;
            case 6:
                return Instrument.FLUTE;
            case 7:
                return Instrument.BELL;
            case 8:
                return Instrument.CHIME;
            case 9:
                return Instrument.XYLOPHONE;
            case 10:
                return Instrument.IRON_XYLOPHONE;
            case 11:
                return Instrument.COW_BELL;
            case 12:
                return Instrument.DIDGERIDOO;
            case 13:
                return Instrument.BIT;
            case 14:
                return Instrument.BANJO;
            case 15:
                return Instrument.PLING;
        }
    }

	/**
	 * If true, the byte given represents a custom instrument
	 * @param instrument
	 * @return whether the byte represents a custom instrument
	 */
	public static boolean isCustomInstrument(byte instrument) {
		return instrument >= getCustomInstrumentFirstIndex();
	}

	/**
	 * Gets the first index in which a custom instrument
	 * can be added to the existing list of instruments
	 * @return index where an instrument can be added
	 */
	public static int getCustomInstrumentFirstIndex() {
        return FIRST_CUSTOM_INSTRUMENT_INDEX;
	}

}