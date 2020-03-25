package com.xxmicloxx.NoteBlockAPI.utils;

import com.xxmicloxx.NoteBlockAPI.model.CustomInstrument;
import com.xxmicloxx.NoteBlockAPI.model.Layer;
import com.xxmicloxx.NoteBlockAPI.model.Note;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
			short length = readShort(dataInputStream);
			int firstCustomInstrumentIndex = 10;
			int firstCustomInstrumentIndexDiff;
			int version = 0;
			if (length == 0) {
				version = dataInputStream.readByte();
				firstCustomInstrumentIndex = dataInputStream.readByte();
				if (version >= 3) {
					length = readShort(dataInputStream);
				}
			}
			firstCustomInstrumentIndexDiff = InstrumentUtils.getCustomInstrumentFirstIndex() - firstCustomInstrumentIndex;
			final short height = readShort(dataInputStream);
			final String title = readString(dataInputStream);
			final String author = readString(dataInputStream);
			final String originalAuthor = readString(dataInputStream);
			final String description = readString(dataInputStream);
			final float speed = readShort(dataInputStream) / 100f;
			// The following information is unnecessary; we're skipping it
			dataInputStream.readBoolean(); // Auto-save enabled
			dataInputStream.readByte(); // Auto-save period
			dataInputStream.readByte(); // Time signature
			readInt(dataInputStream); // Minutes spent for project
			readInt(dataInputStream); // Left clicks
			readInt(dataInputStream); // Right clicks
			readInt(dataInputStream); // Blocks added
			readInt(dataInputStream); // Blocks removed
			readString(dataInputStream); // .midi / .schem name

			// Only read this information in NBS 4 or higher
			if (version >= 4) {
				dataInputStream.readByte(); // Loop on/off
				dataInputStream.readByte(); // Max Loops
				readShort(dataInputStream); // Loop start
			}

			short currentTick = -1;
			while (true) {
				final short jumpTicks = readShort(dataInputStream);
				if (jumpTicks == 0) break;

				currentTick += jumpTicks;

				short currentLayer = -1;
				while (true) {
					final short jumpLayers = readShort(dataInputStream);
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

					// Only read this information in NBS 4 or higher
					if (version >= 4) {
						velocity = dataInputStream.readByte();
						panning = dataInputStream.readByte();
						pitch = readShort(dataInputStream);
					}

					final Layer layer = layerMap.computeIfAbsent((int) currentLayer, index -> new Layer());
					layer.setNote(currentTick, new Note(instrumentIndex, key, velocity, panning, pitch));
				}
			}

			if (version > 0 && version < 3) {
				length = currentTick;
			}

			for (int i = 0; i < height; i++) {
				final Layer layer = layerMap.get(i);
				final String name = readString(dataInputStream);

				// Only read this information in NBS 4 or higher
				if (version >= 4) {
					dataInputStream.readByte(); // Whether layer is locked
				}

				final byte volume = dataInputStream.readByte();

				// Only read this information in NBS 2 or higher
				if (version >= 2) {
					dataInputStream.readByte(); // Whether layer is stereo
				}

				if (layer != null) {
					layer.setName(name);
					layer.setVolume(volume);
				}
			}

			final byte instrumentCount = dataInputStream.readByte();
			CustomInstrument[] instruments = new CustomInstrument[instrumentCount];

			for (int i = 0; i < instrumentCount; i++) {
				instruments[i] = new CustomInstrument(
						(byte) i,
						readString(dataInputStream),
						readString(dataInputStream));
				dataInputStream.readByte(); // Custom pitch
				dataInputStream.readByte(); // Custom key
			}

			if (firstCustomInstrumentIndexDiff < 0) {
				final List<CustomInstrument> customInstruments = CompatibilityUtils.getVersionCustomInstrumentsForSong(firstCustomInstrumentIndex);
				customInstruments.addAll(Arrays.asList(instruments));
				instruments = customInstruments.toArray(instruments);
			} else {
				firstCustomInstrumentIndex = firstCustomInstrumentIndexDiff;
			}

			return new Song(speed, layerMap, height, length, title, author, originalAuthor, description, songFile, firstCustomInstrumentIndex, instruments);
		} catch (final EOFException e) {
			Bukkit.getLogger().severe("Song is corrupted: " + songFile.getName());
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static short readShort(final DataInputStream dataInputStream) throws IOException {
		final int byte1 = dataInputStream.readUnsignedByte();
		final int byte2 = dataInputStream.readUnsignedByte();
		return (short) (byte1 + (byte2 << 8));
	}

	private static int readInt(final DataInputStream dataInputStream) throws IOException {
		final int byte1 = dataInputStream.readUnsignedByte();
		final int byte2 = dataInputStream.readUnsignedByte();
		final int byte3 = dataInputStream.readUnsignedByte();
		final int byte4 = dataInputStream.readUnsignedByte();
		return (byte1 + (byte2 << 8) + (byte3 << 16) + (byte4 << 24));
	}

	private static String readString(final DataInputStream dataInputStream) throws IOException {
		final byte[] bytes = new byte[readInt(dataInputStream)];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = dataInputStream.readByte();
		}
		return new String(bytes, StandardCharsets.UTF_8);
	}

}
