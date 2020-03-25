package com.xxmicloxx.NoteBlockAPI.utils;

import com.xxmicloxx.NoteBlockAPI.model.Layer;
import com.xxmicloxx.NoteBlockAPI.model.Note;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import org.bukkit.Bukkit;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Utils for reading Note Block Studio data
 */
public class NBSDecoder {

	private NBSDecoder() {

	}

	/**
	 * Parses a song from a .nbs-file.
	 *
	 * @param songFile The .nbs-file
	 * @return The song as {@link Song}
	 */
	public static Song parse(final File songFile) {
		final Map<Integer, Layer> layerMap = new HashMap<>();
		try (final DataInputStream dataInputStream = new DataInputStream(new FileInputStream(songFile))) {
			short length = dataInputStream.readShort();
			int firstCustomInstrumentIndex = 10;
			int firstCustomInstrumentIndexDiff;
			int version = 0;
			if (length == 0) {
				version = dataInputStream.readByte();
				firstCustomInstrumentIndex = dataInputStream.readByte();
				if (version >= 3) {
					length = dataInputStream.readShort();
				}
			}
			firstCustomInstrumentIndexDiff = InstrumentUtils.getCustomInstrumentFirstIndex() - firstCustomInstrumentIndex;
			final short height = dataInputStream.readShort();
			final String title = dataInputStream.readUTF();
			final String author = dataInputStream.readUTF();
			final String originalAuthor = dataInputStream.readUTF();
			final String description = dataInputStream.readUTF();
			final float speed = dataInputStream.readShort();
			// The following information is unnecessary; we're skipping it
			dataInputStream.readBoolean(); // Auto-save enabled
			dataInputStream.readByte(); // Auto-save period
			dataInputStream.readByte(); // Time signature
			dataInputStream.readInt(); // Minutes spent for project
			dataInputStream.readInt(); // Left clicks
			dataInputStream.readInt(); // Right clicks
			dataInputStream.readInt(); // Blocks added
			dataInputStream.readInt(); // Blocks removed
			dataInputStream.readUTF(); // .midi / .schem name

			// Read additional information in version NBS 4 or higher
			if (version >= 4) {
				dataInputStream.readByte(); // Loop on/off
				dataInputStream.readByte(); // Max Loops
				dataInputStream.readShort(); // Loop start
			}

			short currentTick = -1;
			while (true) {
				final short jumpTicks = dataInputStream.readShort();
				if (jumpTicks == 0) break;

				currentTick += jumpTicks;
				short currentLayer = -1;
				while (true) {
					final short jumpLayers = dataInputStream.readShort();
					if (jumpLayers == 0) break;

					currentLayer += jumpLayers;
					byte instrumentIndex = dataInputStream.readByte();

					if (firstCustomInstrumentIndexDiff > 0 && instrumentIndex >= firstCustomInstrumentIndex) {
						instrumentIndex += firstCustomInstrumentIndexDiff;
					}

					final byte key = dataInputStream.readByte();
					byte velocity = 100;
					byte panning = 100;
					short pitch = 0;

					// Only read this information in version NBS 4 or higher
					if (version >= 4) {
						velocity = dataInputStream.readByte();
						panning = dataInputStream.readByte();
						pitch = dataInputStream.readShort();
					}

					final Layer layer = layerMap.computeIfAbsent((int) currentLayer, index -> new Layer());
					layer.setNote(currentTick, new Note(instrumentIndex, key, velocity, panning, pitch));
				}

				if (version > 0 && version < 3) {
					length = currentTick;
				}

				for (int i = 0; i < height; i++) {
					final Layer layer = layerMap.get(i);
					final String name = dataInputStream.readUTF();

					// Only read this information in version NBS 4 or higher
					if (version >= 4) {
						dataInputStream.readByte(); // Whether layer is locked
					}

					final byte volume = dataInputStream.readByte();
					if (version >= 2) {
						dataInputStream.readByte();
					}

					if (layer != null) {
						layer.setName(name);
						layer.setVolume(volume);
					}
				}

				return new Song(speed, layerMap, height, length, title, author, description, songFile, firstCustomInstrumentIndex);
			}
		} catch (final EOFException e) {
			Bukkit.getLogger().severe("Song is corrupted: " + songFile.getName());
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
