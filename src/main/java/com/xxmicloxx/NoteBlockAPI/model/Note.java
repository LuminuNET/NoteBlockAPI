package com.xxmicloxx.NoteBlockAPI.model;

/**
 * Represents a note played; contains the instrument and the key
 *
 */
public class Note {

	private byte instrument;
	private byte key;
	private byte velocity;
	private short pitch;

	public Note(byte instrument, byte key) {
		this(instrument, key, (byte) 100, (byte) 100, (short) 0);
	}

	public Note(byte instrument, byte key, byte velocity, byte panning, short pitch) {
		this.instrument = instrument;
		this.key = key;
		this.velocity = velocity;
		this.pitch = pitch;
	}

	/**
	 * Gets instrument number
	 */
	public byte getInstrument() {
		return instrument;
	}

	/**
	 * Sets instrument number
	 */
	public void setInstrument(byte instrument) {
		this.instrument = instrument;
	}

	/**
	 * Returns note key number (Minecraft key 0 corresponds to key 33)
	 * @return
	 */
	public byte getKey() {
		return key;
	}

	/**
	 * Sets note key number (Minecraft key 0 corresponds to key 33)
	 * @param key
	 */
	public void setKey(byte key) {
		this.key = key;
	}

	/**
	 * Returns note pitch.
	 * 100 = 1 key
	 * 1200 = 1 octave
 	 * @return
	 */
	public short getPitch() {
		return pitch;
	}

	/**
	 * Sets note pitch.
	 * 100 = 1 key
	 * 1200 = 1 octave
	 * @param pitch note pitch
	 */
	public void setPitch(short pitch) {
		this.pitch = pitch;
	}

	/**
	 * Returns note velocity (volume)
	 * @return
	 */
	public byte getVelocity() {
		return velocity;
	}

	/**
	 * Sets note velocity (volume)
	 * @param velocity number from 0 - 100
	 */
	public void setVelocity(byte velocity) {
		if (velocity < 0) velocity = 0;
		if (velocity > 100) velocity = 100;

		this.velocity = velocity;
	}

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Note note = (Note) o;
        if (instrument != note.instrument) return false;
        if (key != note.key) return false;
        if (velocity != note.velocity) return false;
        return pitch == note.pitch;
    }

    @Override
    public int hashCode() {
        int result = instrument;
        result = 31 * result + key;
        result = 31 * result + velocity;
        result = 31 * result + pitch;
        return result;
    }
}
