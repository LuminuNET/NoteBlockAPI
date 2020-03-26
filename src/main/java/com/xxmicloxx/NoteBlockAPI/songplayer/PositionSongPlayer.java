package com.xxmicloxx.NoteBlockAPI.songplayer;

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import com.xxmicloxx.NoteBlockAPI.event.PlayerRangeStateChangeEvent;
import com.xxmicloxx.NoteBlockAPI.model.*;
import com.xxmicloxx.NoteBlockAPI.utils.CompatibilityUtils;
import com.xxmicloxx.NoteBlockAPI.utils.InstrumentUtils;
import com.xxmicloxx.NoteBlockAPI.utils.PitchUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * SongPlayer created at a specified Location
 *
 */
public class PositionSongPlayer extends RangeSongPlayer {

	private Location targetLocation;

	public PositionSongPlayer(Song song) {
		super(song);
	}

	public PositionSongPlayer(Song song, SoundCategory soundCategory) {
		super(song, soundCategory);
	}
	
	public PositionSongPlayer(Playlist playlist, SoundCategory soundCategory) {
		super(playlist, soundCategory);
	}

	public PositionSongPlayer(Playlist playlist) {
		super(playlist);
	}

	/**
	 * Gets location on which is the PositionSongPlayer playing
	 *
	 * @return {@link Location}
	 */
	public Location getTargetLocation() {
		return targetLocation;
	}

	/**
	 * Sets location on which is the PositionSongPlayer playing
	 */
	public void setTargetLocation(Location targetLocation) {
		this.targetLocation = targetLocation; }

	@Override
	public void playTick(Player player, int tick) {
		if (!player.getWorld().getName().equals(targetLocation.getWorld().getName())) {
			return; // not in same world
		}

		byte playerVolume = NoteBlockAPI.getPlayerVolume(player);

		for (Layer layer : song.getLayerHashMap().values()) {
			Note note = layer.getNote(tick);
			if (note == null) continue;

			float volume = ((layer.getVolume() * (int) this.volume * (int) playerVolume * note.getVelocity()) / 100_00_00_00F)
					* ((1F / 16F) * getDistance());

			float pitch = PitchUtils.getPitch(note);

			if (InstrumentUtils.isCustomInstrument(note.getInstrument())) {
				CustomInstrument instrument = song.getCustomInstruments()
						[note.getInstrument() - InstrumentUtils.getCustomInstrumentFirstIndex()];

				if (instrument.getSound() != null) {
					CompatibilityUtils.playSound(player, targetLocation, instrument.getSound(),
							this.soundCategory, volume, pitch, false);
				} else {
					CompatibilityUtils.playSound(player, targetLocation, instrument.getSoundFileName(),
							this.soundCategory, volume, pitch, false);
				}
			} else {
				CompatibilityUtils.playSound(player, targetLocation,
						InstrumentUtils.getInstrument(note.getInstrument()), this.soundCategory, 
						volume, pitch, false);
			}

			if (isInRange(player)) {
				if (!this.playerList.get(player.getUniqueId())) {
					playerList.put(player.getUniqueId(), true);
					Bukkit.getPluginManager().callEvent(new PlayerRangeStateChangeEvent(this, player, true));
				}
			} else {
				if (this.playerList.get(player.getUniqueId())) {
					playerList.put(player.getUniqueId(), false);
					Bukkit.getPluginManager().callEvent(new PlayerRangeStateChangeEvent(this, player, false));
				}
			}
		}
	}
	
	/**
	 * Returns true if the Player is able to hear the current PositionSongPlayer 
	 * @param player in range
	 * @return ability to hear the current PositionSongPlayer
	 */
	@Override
	public boolean isInRange(Player player) {
		return player.getLocation().distanceSquared(targetLocation) <= getDistanceSquared();
	}

}
