/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.prosody.sound;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import de.ims.icarus.Core;
import de.ims.icarus.Core.NamedRunnable;
import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.io.IOUtil;
import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.prosody.ProsodicDocumentData;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.ui.events.ChangeSource;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SoundPlayer {

	private volatile static SoundPlayer instance;

	public static SoundPlayer getInstance() {
		SoundPlayer result = instance;

		if (result == null) {
			synchronized (SoundPlayer.class) {
				result = instance;

				if (result == null) {
					instance = new SoundPlayer();
					result = instance;
				}
			}
		}

		return result;
	}

	private Set<Path> folders = new HashSet<>();

	// Maps file name to sound file instances
	private Map<String, SoundFile> fileCache = new HashMap<>();

	private SoundPlayer() {
		Core.getCore().addShutdownHook(new ShutdownHook());

		ConfigRegistry.getGlobalRegistry().addGroupListener("plugins.prosody.audioPlayer", new ConfigListener() { //$NON-NLS-1$

			@Override
			public void invoke(ConfigRegistry sender, ConfigEvent event) {
				reloadFolders();
			}
		});

		reloadFolders();

		if(folders.isEmpty()) {
			//FIXME prompt user to select audio folder!!!
		}
	}

	private synchronized void reloadFolders() {
		for(SoundFile soundFile : fileCache.values()) {
			try {
				close(soundFile);
			} catch(Exception e) {
				// ignore
			}
		}

		folders.clear();

		List<?> folderList = ConfigRegistry.getGlobalRegistry().getList("plugins.prosody.audioPlayer.folders"); //$NON-NLS-1$
		boolean includeSubFolders = ConfigRegistry.getGlobalRegistry().getBoolean("plugins.prosody.audioPlayer.includeSubFolders"); //$NON-NLS-1$

		if(folderList.isEmpty()) {
			return;
		}

		for(Object entry : folderList) {
			Path folder = Paths.get(entry.toString());
			if(!folder.isAbsolute()) {
				folder = Core.getCore().getRootFolder().resolve(folder);
			}

			folders.add(folder);

			if(includeSubFolders) {
				try {
					collectSubFolders(folder);
				} catch (IOException e) {
					LoggerFactory.error(this, "Failed to collect sub-folders for folder: "+folder, e); //$NON-NLS-1$
				}
			}
		}
	}

	private void collectSubFolders(Path folder) throws IOException {
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder, IOUtil.directoryFilter)) {
			for(Path subFolder : stream) {
				folders.add(subFolder);

				collectSubFolders(subFolder);
			}
		}
	}

	public SoundFile getSoundFile(String fileName) {
		if (fileName == null)
			throw new NullPointerException("Invalid fileName"); //$NON-NLS-1$

		if(!fileName.endsWith(".wav")) { //$NON-NLS-1$
			fileName += ".wav"; //$NON-NLS-1$
		}

		SoundFile soundFile = fileCache.get(fileName);
		if(soundFile==null) {
			Path path = createPath(fileName);

			if(path==null)
				throw new IllegalArgumentException("No audio file available for name: "+fileName); //$NON-NLS-1$

			soundFile = new SoundFile(path);
			fileCache.put(fileName, soundFile);
		}

		return soundFile;
	}

	public SoundFile getSoundFile(ProsodicDocumentData document) throws SoundException {
		if (document == null)
			throw new NullPointerException("Invalid document"); //$NON-NLS-1$
		String fileName = (String) document.getProperty(ProsodyConstants.AUDIO_FILE_KEY);
		if(fileName==null)
			throw new SoundException("Document does not declare a valid audio file name: "+document.getId()); //$NON-NLS-1$

		return getSoundFile(fileName);
	}

	public SoundFile getSoundFile(ProsodicSentenceData sentence) throws SoundException {
		if (sentence == null)
			throw new NullPointerException("Invalid sentence"); //$NON-NLS-1$

		return getSoundFile(sentence.getDocument());
	}

	private Path createPath(String fileName) {
		if(fileName.trim().isEmpty())
			throw new IllegalArgumentException("File name is empty"); //$NON-NLS-1$

		for(Path folder : folders) {
			Path file = folder.resolve(fileName);

			if(Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
				return file;
			}
		}

		return null;
	}

	public synchronized void open(SoundFile soundFile) throws SoundException {
		if (soundFile == null)
			throw new NullPointerException("Invalid soundFile"); //$NON-NLS-1$

		FileState state = soundFile.getState();
		if(state!=FileState.BLANK && state!=FileState.CLOSED)
			throw new IllegalStateException("Cannot open sound file while in state: "+state); //$NON-NLS-1$

		Path path = soundFile.path;

		if(!Files.exists(path, LinkOption.NOFOLLOW_LINKS))
			throw new SoundException("Audio file does not exist: "+path); //$NON-NLS-1$

		try(AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(path.toFile())) {
			soundFile.audioFormat = audioInputStream.getFormat();

			soundFile.frameLength = audioInputStream.getFrameLength();
			soundFile.microSecondsLength = (long) (soundFile.frameLength * 1000 / soundFile.audioFormat.getFrameRate());
		} catch (UnsupportedAudioFileException e) {
			throw new SoundException("Unsupported audio format in file: "+path, e); //$NON-NLS-1$
		} catch (IOException e) {
			throw new SoundException("Failed to load audio file: "+path, e); //$NON-NLS-1$
		}

		try {
			// Open file only for reading!
			soundFile.randomAccessFile = new RandomAccessFile(path.toFile(), "r"); //$NON-NLS-1$
		} catch (FileNotFoundException e) {
			// Should not happen, going to catch anyways
			throw new SoundException("Audio file does not exist: "+path); //$NON-NLS-1$
		}

		soundFile.state = FileState.OPEN;
	}

	public synchronized void close(SoundFile soundFile) throws SoundException {
		if (soundFile == null)
			throw new NullPointerException("Invalid soundFile"); //$NON-NLS-1$

		FileState state = soundFile.getState();
		stop(soundFile);

		if(state!=FileState.OPEN && state!=FileState.INACTIVE)
			throw new IllegalStateException("Cannot close sound file while in state: "+state); //$NON-NLS-1$

		try {
			soundFile.randomAccessFile.close();
		} catch (IOException e) {
			throw new SoundException("Failed to close sound file: "+soundFile.path); //$NON-NLS-1$
		} finally {
			soundFile.randomAccessFile = null;
		}

		soundFile.state = FileState.CLOSED;
	}

	public synchronized void start(SoundFile soundFile) throws SoundException {
		if (soundFile == null)
			throw new NullPointerException("Invalid soundFile"); //$NON-NLS-1$

		//TODO sanity checks!

		dispatchThread().scheduleFile(soundFile);
	}

	public synchronized void stop(SoundFile soundFile) throws SoundException {
		if (soundFile == null)
			throw new NullPointerException("Invalid soundFile"); //$NON-NLS-1$

		FileState state = soundFile.getState();
		if(state==FileState.INACTIVE) {
			return;
		}

		if(state!=FileState.ACTIVE && state!=FileState.PAUSED)
			throw new IllegalStateException("Cannot stop sound file while in state: "+state); //$NON-NLS-1$

		dispatchThread().stopFile();
	}

	public synchronized void pause(SoundFile soundFile) throws SoundException {
		if (soundFile == null)
			throw new NullPointerException("Invalid soundFile"); //$NON-NLS-1$

		FileState state = soundFile.getState();
		if(state==FileState.INACTIVE) {
			return;
		}

		if(state!=FileState.ACTIVE)
			throw new IllegalStateException("Cannot pause sound file while in state: "+state); //$NON-NLS-1$

		dispatchThread().pauseFile();
	}


	public synchronized void resume(SoundFile soundFile) throws SoundException {
		if (soundFile == null)
			throw new NullPointerException("Invalid soundFile"); //$NON-NLS-1$

		FileState state = soundFile.getState();
		if(state==FileState.INACTIVE) {
			return;
		}

		if(state!=FileState.PAUSED)
			throw new IllegalStateException("Cannot resume sound file while in state: "+state); //$NON-NLS-1$

		dispatchThread().resumeFile();
	}

	public synchronized void export(SoundFile soundFile, OutputStream out) throws SoundException, IOException {
		if (soundFile == null)
			throw new NullPointerException("Invalid soundFile"); //$NON-NLS-1$

		if(!soundFile.isOpen()) {
			open(soundFile);
		}

		// Save settings for playback
		long startFrame = soundFile.getStartFrame();
		if(startFrame==LanguageConstants.DATA_UNDEFINED_VALUE) {
			startFrame = 0;
		}
		long endFrame = soundFile.getEndFrame();
		if(endFrame==LanguageConstants.DATA_UNDEFINED_VALUE) {
			endFrame = soundFile.getFrameLength()-1;
		}

		RandomAccessFile source = soundFile.randomAccessFile;

		AudioFormat audioFormat = soundFile.getAudioFormat();

		float frameRate = audioFormat.getFrameRate();
		if(frameRate==AudioSystem.NOT_SPECIFIED) {
			frameRate = 16_000;
		}
		int frameSize = audioFormat.getFrameSize();
		if(frameSize==AudioSystem.NOT_SPECIFIED) {
			frameSize = 2;
		}

		long framesToRead = endFrame-startFrame+1;

		int framesToBuffer = 8000;

		int bufferSize = frameSize * framesToBuffer;
		byte[] buffer = new byte[bufferSize];

		int contentSize = (int) (framesToRead * frameSize);

		// Copy wav header
		source.seek(0);
		byte[] header = new byte[WAV_HEADER_SIZE];
		source.read(header);
		//Modify header info
		int fileSize = contentSize + WAV_HEADER_SIZE - 8;
		insertInt(header, fileSize, 4);
		int dataLength = contentSize-44;
		insertInt(header, dataLength, 40);
		out.write(header);

		// Now move cursor to actual data and copy
		long firstByteOffset = WAV_HEADER_SIZE + frameSize * startFrame;

		source.seek(firstByteOffset);
		int bytesToRead = frameSize * (int) Math.min(framesToRead, framesToBuffer);
		int bytesRead;
		while(framesToRead>0 && (bytesRead = source.read(buffer, 0, bytesToRead)) > 0 ) {

			int bytesToWrite = Math.min((int)framesToRead*frameSize, bytesRead);
			out.write(buffer, 0, bytesToWrite);

			framesToRead -= bytesToWrite/frameSize;
		}
	}

	private void insertInt(byte[] b, int val, int index) {
		b[index++] = (byte) val;
		b[index++] = (byte) (val>>8);
		b[index++] = (byte) (val>>16);
		b[index++] = (byte) (val>>24);
	}

	private enum FileState {
		BLANK,
		OPEN,
		ACTIVE,
		INACTIVE,
		PAUSED,
		CLOSED,
	}

	public final static class SoundFile extends ChangeSource {

		private final Path path;

		private AudioFormat audioFormat;
		private long frameLength;
		private long microSecondsLength;

		private FileState state = FileState.BLANK;
		private RandomAccessFile randomAccessFile;

		private long startFrame = 0, endFrame = LanguageConstants.DATA_UNDEFINED_VALUE;
		private boolean repeating = false;

		public SoundFile(Path path) {
			if (path == null)
				throw new NullPointerException("Invalid path"); //$NON-NLS-1$

			this.path = path;
		}

		synchronized void setState(FileState newState) {
			state = newState;

			fireStateChanged();
		}

		synchronized FileState getState() {
			return state;
		}

		private void checkOpen() {
			if(!isOpen())
				throw new IllegalStateException("Sound file not open yet: "+path); //$NON-NLS-1$
		}

		/**
		 * @return the startFrame
		 */
		public long getStartFrame() {
			return startFrame;
		}

		/**
		 * @return the endFrame
		 */
		public long getEndFrame() {
			return endFrame;
		}

		/**
		 * @return the repeating
		 */
		public boolean isRepeating() {
			return repeating;
		}

		/**
		 * @param startFrame the startFrame to set
		 */
		public void setStartFrame(long startFrame) {
			checkOpen();

			if(startFrame==LanguageConstants.DATA_UNDEFINED_VALUE) {
				this.startFrame = 0;
			} else {
				this.startFrame = startFrame;
			}
		}

		public void setStartOffset(float startOffset) {
			checkOpen();

			if(startOffset==LanguageConstants.DATA_UNDEFINED_VALUE) {
				this.startFrame = 0;
			} else {
				this.startFrame = SoundOffsets.toFrames(startOffset, audioFormat.getFrameRate());
			}
		}

		/**
		 * @param endFrame the endFrame to set
		 */
		public void setEndFrame(long endFrame) {
			checkOpen();

			if(endFrame==LanguageConstants.DATA_UNDEFINED_VALUE) {
				this.endFrame = frameLength-1;
			} else {
				this.endFrame = endFrame;
			}
		}

		public void setEndOffset(float endOffset) {
			checkOpen();

			if(endOffset==LanguageConstants.DATA_UNDEFINED_VALUE) {
				this.endFrame = frameLength-1;
			} else {
				this.endFrame = SoundOffsets.toFrames(endOffset, audioFormat.getFrameRate());
			}
		}

		/**
		 * @param repeating the repeating to set
		 */
		public void setRepeating(boolean repeating) {
			checkOpen();
			this.repeating = repeating;
		}

		/**
		 * @return the path
		 */
		public Path getPath() {
			return path;
		}

		/**
		 * @return the audioFormat
		 */
		public AudioFormat getAudioFormat() {
			return audioFormat;
		}

		/**
		 * @return the frameLength
		 */
		public long getFrameLength() {
			return frameLength;
		}

		/**
		 * @return the microSecondsLength
		 */
		public long getMicroSecondsLength() {
			return microSecondsLength;
		}

		public boolean isOpen() {
			FileState state = getState();
			return state!=FileState.BLANK
					&& state!=FileState.CLOSED;
		}

		public boolean isActive() {
			return getState()==FileState.ACTIVE;
		}

		public boolean isClosed() {
			return getState()==FileState.CLOSED;
		}

		public boolean isPaused() {
			return getState()==FileState.PAUSED;
		}
	}

	/**
	 * Stable predicate: dispatchThread!={@code null}
	 */
	private static volatile SoundDispatchThread dispatchThread;
//	private static final Semaphore semaphore = new Semaphore(0);

	private static SoundDispatchThread dispatchThread() {
		SoundDispatchThread result = dispatchThread;
		if(result==null) {
			synchronized (SoundFile.class) {
				result = dispatchThread;
				if(result==null) {
					result = new SoundDispatchThread();
					dispatchThread = result;
					result.start();
				}
			}
		}

		return result;
	}

	/**
	 * 44 Bytes wave header
	 * @see http://de.wikipedia.org/wiki/RIFF_WAVE
	 */
	private static final int WAV_HEADER_SIZE = 44;

	private static class SoundDispatchThread extends Thread {

		private BlockingQueue<SoundFile> queue = new LinkedBlockingQueue<>(1);

		private volatile boolean active = true;

		private volatile boolean doStop = false;
		private volatile boolean doPause = false;

		private SourceDataLine sharedSoundLine;

		public SoundDispatchThread() {
			super("SoundDispatchThread"); //$NON-NLS-1$
			setDaemon(true);
		}

		void close() {
			active = false;
			doStop = true;
			doPause = false;

			interrupt();
		}

		void stopFile() {
			doStop = true;
			doPause = false;
		}

		void pauseFile() {
			doPause = true;
		}

		void resumeFile() {
			doPause = false;
		}

		void scheduleFile(SoundFile soundFile) {
			queue.clear();
			queue.offer(soundFile);
			doStop = true;
			doPause = false;
		}

		private void ensureSoundLine(SoundFile soundFile) {
			AudioFormat newFormat = soundFile.audioFormat;

			// If a sound line is already loaded, check compatibility
			if(sharedSoundLine!=null) {
				AudioFormat currentFormat = sharedSoundLine.getFormat();
				if(!newFormat.matches(currentFormat)) {
					if(sharedSoundLine.isActive()) {
						sharedSoundLine.stop();
					}
					sharedSoundLine.close();
					sharedSoundLine = null;
				}
			}

			// If no sound line is loaded or previous line got deleted, obtain a new one
			if(sharedSoundLine==null) {
				try {
					sharedSoundLine = AudioSystem.getSourceDataLine(newFormat);
					sharedSoundLine.open(newFormat);
				} catch (LineUnavailableException e) {
					LoggerFactory.error(this, "Failed to obtain new sound line for format: "+newFormat, e); //$NON-NLS-1$
					close();
				}
			}

			// Immediately start the sound line
			sharedSoundLine.start();
		}

		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				while(active) {

					SoundFile activeFile = null;
					// If nothing to do wait till we get a job scheduled
					try {
						activeFile = queue.take();
					} catch (InterruptedException e) {
						// Cancellation uses interrupt() to break out of lock, so check if we still are good to go
						if(active) {
							LoggerFactory.warning(this, "Interrupted while waiting for next sound file", e); //$NON-NLS-1$
						}
						continue;
					}

					// Highly unlikely, but check nevertheless
					if(activeFile==null) {
						continue;
					}

					doStop = false;
					doPause = false;

					if(!activeFile.isOpen()) {
						LoggerFactory.error(this, "Cannot play sound file in state: "+activeFile.getState()); //$NON-NLS-1$
						continue;
					}

					// Set up sound line
					ensureSoundLine(activeFile);

					// Save settings for playback
					long startFrame = activeFile.getStartFrame();
					if(startFrame==LanguageConstants.DATA_UNDEFINED_VALUE) {
						startFrame = 0;
					}
					long endFrame = activeFile.getEndFrame();
					if(endFrame==LanguageConstants.DATA_UNDEFINED_VALUE) {
						endFrame = activeFile.getFrameLength()-1;
					}
					boolean repeating = activeFile.isRepeating();

					//DEBUG
//					startFrame = 0;
//					endFrame = activeFile.getFrameLength()-1;

					// Activate sound file
					activeFile.setState(FileState.ACTIVE);

					RandomAccessFile source = activeFile.randomAccessFile;

					while(!doStop && activeFile.isActive()) {

						try {
							streamAudio(source, startFrame, endFrame, activeFile);
						} catch (IOException e) {
							LoggerFactory.error(this, "I/O error during play back of file: "+activeFile.getPath(), e); //$NON-NLS-1$
							break;
						}

						if(!repeating) {
							break;
						}
					}

					activeFile.setState(FileState.INACTIVE);
				}
			} finally {
				if(sharedSoundLine!=null) {
					sharedSoundLine.close();
					sharedSoundLine = null;
				}
			}
		}

		private void streamAudio(RandomAccessFile source, long startFrame, long endFrame, SoundFile soundFile) throws IOException {

			sharedSoundLine.flush();

			AudioFormat audioFormat = soundFile.getAudioFormat();

			float frameRate = audioFormat.getFrameRate();
			if(frameRate==AudioSystem.NOT_SPECIFIED) {
				frameRate = 16_000;
			}
			int frameSize = audioFormat.getFrameSize();
			if(frameSize==AudioSystem.NOT_SPECIFIED) {
				frameSize = 2;
			}

			long firstByteOffset = WAV_HEADER_SIZE + frameSize * startFrame;

			source.seek(firstByteOffset);

			long framesToRead = endFrame-startFrame+1;

//			System.out.printf("firstFrame=%d lastFrame=%d framesToRead=%d duration=%.02f frameRate=%.02f frameSize=%d\n", //$NON-NLS-1$
//					startFrame, startFrame+framesToRead, framesToRead, framesToRead/frameRate, frameRate, frameSize);

			// Make a small 0.1 seconds buffer
			//TODO maybe increase?
			int framesToBuffer = (int)Math.ceil(frameRate / 10);

			int bufferSize = frameSize * framesToBuffer;
			byte[] buffer = new byte[bufferSize];

			int bytesToRead = frameSize * (int) Math.min(framesToRead, framesToBuffer);
			int bytesRead;
			while(!doStop && framesToRead>0 && (bytesRead = source.read(buffer, 0, bytesToRead)) > 0 ) {

				if(doPause) {
					soundFile.setState(FileState.PAUSED);
					while(doPause && !doStop) {
						// Busy waiting
					}
					soundFile.setState(FileState.ACTIVE);

					if(doStop) {
						break;
					}
				}


				int bytesToWrite = Math.min((int)framesToRead*frameSize, bytesRead);
				int bytesWritten = sharedSoundLine.write(buffer, 0, bytesToWrite);

//				System.out.printf("bytesToRead=%d bytesRead=%d bytesWritten=%d\n",
//						bytesToRead, bytesRead, bytesWritten);

				framesToRead -= bytesWritten/frameSize;
			}

//			sharedSoundLine.drain();
		}

	}

	private class ShutdownHook implements NamedRunnable {

		/**
		 * @see de.ims.icarus.Core.NamedRunnable#getName()
		 */
		@Override
		public String getName() {
			return "Sound player shutdown"; //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.Core.NamedRunnable#run()
		 */
		@Override
		public void run() throws Exception {
			for(SoundFile soundFile : fileCache.values()) {
				try {
					close(soundFile);
				} catch(Exception e) {
					// ignore
				}
			}

			if(dispatchThread!=null) {
				dispatchThread.close();
			}
		}

	}
}
