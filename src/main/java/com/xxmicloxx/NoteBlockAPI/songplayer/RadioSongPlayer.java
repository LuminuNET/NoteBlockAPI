package com.xxmicloxx.NoteBlockAPI.songplayer;

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import com.xxmicloxx.NoteBlockAPI.model.*;
import com.xxmicloxx.NoteBlockAPI.utils.CompatibilityUtils;
import com.xxmicloxx.NoteBlockAPI.utils.InstrumentUtils;
import com.xxmicloxx.NoteBlockAPI.utils.PitchUtils;
import org.bukkit.entity.Player;

/**
 * SongPlayer playing to everyone added to it no matter where he is
 *
 */
public class RadioSongPlayer extends SongPlayer {
	
	protected boolean stereo = true;
	
	public RadioSongPlayer(Song song) {
		super(song);
	}

	public RadioSongPlayer(Song song, SoundCategory soundCategory) {
		super(song, soundCategory);
	}

	public RadioSongPlayer(Playlist playlist, SoundCategory soundCategory) {
		super(playlist, soundCategory);
	}

	public RadioSongPlayer(Playlist playlist) {
		super(playlist);
	}

	@Override
	public void playTick(Player player, int tick) {
		byte playerVolume = NoteBlockAPI.getPlayerVolume(player);

		for (Layer layer : song.getLayerHashMap().values()) {
			Note note = layer.getNote(tick);
			if (note == null) {
				continue;
			}

			float volume = (layer.getVolume() * (int) this.volume * (int) playerVolume * note.getVelocity()) / 100_00_00_00F;
			float pitch = PitchUtils.getPitch(note);

			if (InstrumentUtils.isCustomInstrument(note.getInstrument())) {
				CustomInstrument instrument = song.getCustomInstruments()
						[note.getInstrument() - InstrumentUtils.getCustomInstrumentFirstIndex()];

				if (instrument.getSound() != null) {
					CompatibilityUtils.playSound(player, player.getEyeLocation(),
							instrument.getSound(),
							this.soundCategory, volume, pitch, stereo);
				} else {
					CompatibilityUtils.playSound(player, player.getEyeLocation(),
							instrument.getSoundFileName(),
							this.soundCategory, volume, pitch, stereo);
				}
			} else {
				CompatibilityUtils.playSound(player, player.getEyeLocation(),
						InstrumentUtils.getInstrument(note.getInstrument()), this.soundCategory, volume, pitch, stereo);
			}
		}
	}

	/**
	 * Returns if the SongPlayer will play Notes from two sources as stereo
	 * @return if is played stereo
	 */
	public boolean isStereo(){
		return stereo;
	}
	
	/**
	 * Sets if the SongPlayer will play Notes from two sources as stereo
	 * @param stereo
	 */
	public void setStereo(boolean stereo){
		this.stereo = stereo;
	}

}
