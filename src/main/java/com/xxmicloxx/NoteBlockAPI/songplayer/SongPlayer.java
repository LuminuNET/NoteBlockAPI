package com.xxmicloxx.NoteBlockAPI.songplayer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.xxmicloxx.NoteBlockAPI.model.*;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import com.xxmicloxx.NoteBlockAPI.event.SongDestroyingEvent;
import com.xxmicloxx.NoteBlockAPI.event.SongEndEvent;
import com.xxmicloxx.NoteBlockAPI.event.SongLoopEvent;
import com.xxmicloxx.NoteBlockAPI.event.SongNextEvent;
import com.xxmicloxx.NoteBlockAPI.event.SongStoppedEvent;


/**
 * Plays a Song for a list of Players
 *
 */
public abstract class SongPlayer {

	protected Song song;
	protected Playlist playlist;
	protected int actualSong = 0;

	protected boolean playing = false;
	protected short tick = -1;
	protected Map<UUID, Boolean> playerList = new ConcurrentHashMap<>();

	protected boolean autoDestroy = false;
	protected boolean destroyed = false;

	protected byte volume = 100;
	protected Fade fadeIn;
	protected Fade fadeOut;
	protected RepeatMode repeat = RepeatMode.NO;
	protected boolean random = false;

	protected Map<Song, Boolean> songQueue = new ConcurrentHashMap<>(); //True if already played

	private final Lock lock = new ReentrantLock();
	private static final Random rng = new Random();

	protected NoteBlockAPI plugin;

	protected SoundCategory soundCategory;

	public SongPlayer(Song song) {
		this(new Playlist(song), SoundCategory.MASTER);
	}

	public SongPlayer(Song song, SoundCategory soundCategory) {
		this(new Playlist(song), soundCategory);
	}

	public SongPlayer(Song song, SoundCategory soundCategory, boolean random) {
		this(new Playlist(song), soundCategory, random);
	}
	
	public SongPlayer(Playlist playlist){
		this(playlist, SoundCategory.MASTER);
	}

	public SongPlayer(Playlist playlist, SoundCategory soundCategory){
		this(playlist, soundCategory, false);
	}

	public SongPlayer(Playlist playlist, SoundCategory soundCategory, boolean random){
		this.playlist = playlist;
		this.random = random;
		this.soundCategory = soundCategory;
		plugin = NoteBlockAPI.getAPI();
		
		fadeIn = new Fade(FadeType.NONE, 60);
		fadeIn.setFadeStart((byte) 0);
		fadeIn.setFadeTarget(volume);
		
		fadeOut = new Fade(FadeType.NONE, 60);
		fadeOut.setFadeStart(volume);
		fadeOut.setFadeTarget((byte) 0);

		if (random){
			checkPlaylistQueue();
			actualSong = rng.nextInt(playlist.getCount());
		}
		this.song = playlist.get(actualSong);

		start();
	}
	
	/**
	 * Gets the FadeType for this SongPlayer (unused)
	 * @return FadeType
	 * @deprecated returns fadeIn value
	 */
	@Deprecated
	public FadeType getFadeType() {
		return fadeIn.getType();
	}

	/**
	 * Sets the FadeType for this SongPlayer
	 * @param fadeType
	 * @deprecated set fadeIn value
	 */
	@Deprecated
	public void setFadeType(FadeType fadeType) {
		fadeIn.setType(fadeType);
	}

	/**
	 * Target volume for fade
	 * @return byte representing fade target
	 * @deprecated returns fadeIn value
	 */
	@Deprecated
	public byte getFadeTarget() {
		return fadeIn.getFadeTarget();
	}

	/**
	 * Set target volume for fade
	 * @param fadeTarget
	 * @deprecated set fadeIn value
	 */
	@Deprecated
	public void setFadeTarget(byte fadeTarget) {
		fadeIn.setFadeTarget(fadeTarget);
	}

	/**
	 * Gets the starting volume for the fade
	 * @return
	 * @deprecated returns fadeIn value
	 */
	@Deprecated
	public byte getFadeStart() {
		return fadeIn.getFadeStart();
	}

	/**
	 * Sets the starting volume for the fade
	 * @param fadeStart
	 * @deprecated set fadeIn value
	 */
	@Deprecated
	public void setFadeStart(byte fadeStart) {
		fadeIn.setFadeStart(fadeStart);
	}

	/**
	 * Gets the duration of the fade
	 * @return duration of the fade
	 * @deprecated returns fadeIn value
	 */
	@Deprecated
	public int getFadeDuration() {
		return fadeIn.getFadeDuration();
	}

	/**
	 * Sets the duration of the fade
	 * @param fadeDuration
	 * @deprecated set fadeIn value
	 */
	@Deprecated
	public void setFadeDuration(int fadeDuration) {
		fadeIn.setFadeDuration(fadeDuration);
	}

	/**
	 * Gets the tick when fade will be finished
	 * @return tick
	 * @deprecated returns fadeIn value
	 */
	@Deprecated
	public int getFadeDone() {
		return fadeIn.getFadeDone();
	}

	/**
	 * Sets the tick when fade will be finished
	 * @param fadeDone
	 * @deprecated set fadeIn value
	 */
	@Deprecated
	public void setFadeDone(int fadeDone) {
		fadeIn.setFadeDone(fadeDone);
	}

	/**
	 * Starts this SongPlayer
	 */
	private void start() {
		plugin.doAsync(() -> {
			while (!destroyed) {
				long startTime = System.currentTimeMillis();
				lock.lock();
				try {
					if (destroyed || NoteBlockAPI.getAPI().isDisabling()){
						break;
					}

					if (playing) {
						if (tick < fadeIn.getFadeDuration()){
							int fade = fadeIn.calculateFade();
							if (fade != -1){
								volume = (byte) fade;
							}
						} else if (tick >= song.getLength() - fadeOut.getFadeDuration()){
							int fade = fadeOut.calculateFade();
							if (fade != -1){
								volume = (byte) fade;
							}
						}
						
						tick++;
						if (tick > song.getLength()) {
							tick = -1;
							fadeIn.setFadeDone(0);
							fadeOut.setFadeDone(0);
							if (repeat == RepeatMode.ONE){
								SongLoopEvent event = new SongLoopEvent(this);
								plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));

								if (!event.isCancelled()) {
									continue;
								}
							} else {
								if (random) {
									songQueue.put(song, true);
									checkPlaylistQueue();
									ArrayList<Song> left = new ArrayList<>();
									for (Map.Entry<Song, Boolean> entry : songQueue.entrySet()) {
										if (!entry.getValue()) {
											left.add(entry.getKey());
										}
									}

									if (left.isEmpty()) {
										left.addAll(songQueue.keySet());
                                        songQueue.replaceAll((s, v) -> false);
										song = left.get(rng.nextInt(left.size()));
										actualSong = playlist.getIndex(song);
										if (repeat == RepeatMode.ALL) {
											SongLoopEvent event = new SongLoopEvent(this);
											plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));

											if (!event.isCancelled()) {
												continue;
											}
										}
									} else {
										song = left.get(rng.nextInt(left.size()));
										actualSong = playlist.getIndex(song);

										SongNextEvent event = new SongNextEvent(this);
										plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));
										continue;
									}
								} else {
									if (playlist.hasNext(actualSong)) {
										actualSong++;
										song = playlist.get(actualSong);
										SongNextEvent event = new SongNextEvent(this);
										plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));
										continue;
									} else {
										actualSong = 0;
										song = playlist.get(actualSong);
										if (repeat == RepeatMode.ALL) {
											SongLoopEvent event = new SongLoopEvent(this);
											plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));

											if (!event.isCancelled()) {
												continue;
											}
										}
									}
								}
							}
							playing = false;
							SongEndEvent event = new SongEndEvent(this);
							plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));
							if (autoDestroy) {
								destroy();
							}
							continue;
						}

						plugin.doSync(() -> {
							for (UUID uuid : playerList.keySet()) {
								Player player = Bukkit.getPlayer(uuid);
								if (player == null) {
									// offline...
									continue;
								}
								playTick(player, tick);
							}
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}

				if (destroyed) {
					break;
				}

				long duration = System.currentTimeMillis() - startTime;
				float delayMillis = song.getDelay() * 50;
				if (duration < delayMillis) {
					try {
						Thread.sleep((long) (delayMillis - duration));
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			}
		});
	}

	private void checkPlaylistQueue(){
		for (Song s : songQueue.keySet()){
			if (!playlist.contains(s)){
				songQueue.remove(s);
			}
		}

		for (Song s : playlist.getSongList()){
			if (!songQueue.containsKey(s)){
				songQueue.put(s, false);
			}
		}
	}
	
	/**
	 * Returns {@link Fade} for Fade in effect
	 * @return Fade
	 */
	public Fade getFadeIn(){
		return fadeIn;
	}
	
	/**
	 * Returns {@link Fade} for Fade out effect
	 * @return Fade
	 */
	public Fade getFadeOut(){
		return fadeOut;
	}
	
	/**
	 * Gets list of current Player UUIDs listening to this SongPlayer
	 * @return list of Player UUIDs
	 */
	public Set<UUID> getPlayerUUIDs() {
        Set<UUID> uuids = new HashSet<>(playerList.keySet());
		return Collections.unmodifiableSet(uuids);
	}

	/**
	 * Adds a Player to the list of Players listening to this SongPlayer
	 * @param player
	 */
	public void addPlayer(Player player) {
		addPlayer(player.getUniqueId());
	}

	/**
	 * Adds a Player to the list of Players listening to this SongPlayer
	 * @param player's uuid
	 */
	private void addPlayer(UUID player){
		lock.lock();
		try {
			if (!playerList.containsKey(player)) {
				playerList.put(player, false);
				ArrayList<SongPlayer> songs = NoteBlockAPI.getSongPlayersByPlayer(player);
				if (songs == null) {
					songs = new ArrayList<>();
				}
				songs.add(this);
				NoteBlockAPI.setSongPlayersByPlayer(player, songs);
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns whether the SongPlayer is set to destroy itself when no one is listening 
	 * or when the Song ends
	 * @return if autoDestroy is enabled
	 */
	public boolean getAutoDestroy() {
		lock.lock();
		try {
			return autoDestroy;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Sets whether the SongPlayer is going to destroy itself when no one is listening 
	 * or when the Song ends
	 * @param autoDestroy if autoDestroy is enabled
	 */
	public void setAutoDestroy(boolean autoDestroy) {
		lock.lock();
		try {
			this.autoDestroy = autoDestroy;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Plays the Song for the specific player
	 * @param player to play this SongPlayer for
	 * @param tick to play at
	 */
	public abstract void playTick(Player player, int tick);

	/**
	 * SongPlayer will destroy itself
	 */
	public void destroy() {
		lock.lock();
		try {
			SongDestroyingEvent event = new SongDestroyingEvent(this);
			plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));
			//Bukkit.getScheduler().cancelTask(threadId);
			if (event.isCancelled()) {
				return;
			}
			destroyed = true;
			playing = false;
			setTick((short) -1);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns whether the SongPlayer is actively playing
	 * @return is playing
	 */
	public boolean isPlaying() {
		return playing;
	}

	/**
	 * Sets whether the SongPlayer is playing
	 * @param playing
	 */
	public void setPlaying(boolean playing) {
		this.playing = playing;
		if (!playing) {
			SongStoppedEvent event = new SongStoppedEvent(this);
			plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));
		}
	}

	/**
	 * Gets the current tick of this SongPlayer
	 * @return
	 */
	public short getTick() {
		return tick;
	}

	/**
	 * Sets the current tick of this SongPlayer
	 * @param tick
	 */
	public void setTick(short tick) {
		this.tick = tick;
	}

	/**
	 * Removes a player from this SongPlayer
	 * @param player to remove
	 */
	public void removePlayer(Player player) {
		removePlayer(player.getUniqueId());
	}
	
	/**
	 * Removes a player from this SongPlayer
	 * @param uuid of player to remove
	 */
	public void removePlayer(UUID uuid) {
		removePlayer(uuid, true);
	}
	
	private void removePlayer(UUID player, boolean notify) {
		lock.lock();
		try {
			playerList.remove(player);
			if (NoteBlockAPI.getSongPlayersByPlayer(player) == null) {
				return;
			}
			ArrayList<SongPlayer> songs = new ArrayList<>(
					NoteBlockAPI.getSongPlayersByPlayer(player));
			songs.remove(this);
			NoteBlockAPI.setSongPlayersByPlayer(player, songs);
			if (playerList.isEmpty() && autoDestroy) {
				SongEndEvent event = new SongEndEvent(this);
				plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));
				destroy();
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Gets the current volume of this SongPlayer
	 * @return volume (0-100)
	 */
	public byte getVolume() {
		return volume;
	}

	/**
	 * Sets the current volume of this SongPlayer
	 * @param volume (0-100)
	 */
	public void setVolume(byte volume) {
		if (volume > 100){
			volume = 100;
		} else if (volume < 0){
			volume = 0;
		}
		this.volume = volume;
		
		fadeIn.setFadeTarget(volume);
		fadeOut.setFadeStart(volume);
	}

	/**
	 * Gets the Song being played by this SongPlayer
	 * @return
	 */
	public Song getSong() {
		return song;
	}
	
	/**
	 * Gets the Playlist being played by this SongPlayer
	 * @return
	 */
	public Playlist getPlaylist() {
		return playlist;
	}
	
	/**
	 * Sets the Playlist being played by this SongPlayer. Will affect next Song
	 */
	public void setPlaylist(Playlist playlist) {
		this.playlist = playlist;
	}
	
	/**
	 * Get index of actually played {@link Song} in {@link Playlist}
	 * @return
	 */
	public int getPlayedSongIndex(){
		return actualSong;
	}
	
	/**
	 * Start playing {@link Song} at specified index in {@link Playlist}
	 * If there is no {@link Song} at this index, {@link SongPlayer} will continue playing current song
	 * @param index
	 */
	public void playSong(int index){
		lock.lock();
		try {
			if (playlist.exist(index)){
				song = playlist.get(index);
				actualSong = index;
				tick = -1;
				fadeIn.setFadeDone(0);
				fadeOut.setFadeDone(0);
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Start playing {@link Song} that is next in {@link Playlist} or random {@link Song} from {@link Playlist}
	 */
	public void playNextSong(){
		lock.lock();
		try {
			tick = song.getLength();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Gets the SoundCategory of this SongPlayer
	 * @see SoundCategory
	 * @return SoundCategory of this SongPlayer
	 */
	public SoundCategory getCategory() {
		return soundCategory;
	}

	/**
	 * Sets the SoundCategory for this SongPlayer
	 * @param soundCategory
	 */
	public void setCategory(SoundCategory soundCategory) {
		this.soundCategory = soundCategory;
	}
	
	/**
	 * Sets whether the SongPlayer will loop
	 * @deprecated
	 * @param loop
	 */
	public void setLoop(boolean loop){
		this.repeat = RepeatMode.ALL;
	}
	
	/**
	 * Gets whether the SongPlayer will loop
	 * @deprecated
	 * @return is loop
	 */
	public boolean isLoop(){
		return repeat == RepeatMode.ALL;
	}

	/**
	 * Sets SongPlayer's {@link RepeatMode}
	 * @param repeatMode
	 */
	public void setRepeatMode(RepeatMode repeatMode){
		this.repeat = repeatMode;
	}

	/**
	 * Gets SongPlayer's {@link RepeatMode}
	 * @return
	 */
	public RepeatMode getRepeatMode(){
		return repeat;
	}

	/**
	 * Sets whether the SongPlayer will choose next song from player randomly
	 * @param random
	 */
	public void setRandom(boolean random){
		this.random = random;
	}

	/**
	 * Gets whether the SongPlayer will choose next song from player randomly
	 * @return is random
	 */
	public boolean isRandom(){
		return random;
	}

}
