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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import de.ims.icarus.Core;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.plugins.prosody.ProsodicDocumentData;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodyConstants;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class WavPlayer {

	private volatile static WavPlayer instance;

	public static WavPlayer getInstance() {
		WavPlayer result = instance;

		if (result == null) {
			synchronized (WavPlayer.class) {
				result = instance;

				if (result == null) {
					instance = new WavPlayer();
					result = instance;
				}
			}
		}

		return result;
	}

	private Clip clip;
	private Path path;
	private ProsodicDocumentData document;
	private final LineObserver lineObserver = new LineObserver();

	private final Object lock = new Object();

	private Path getPath(ProsodicDocumentData document) throws SoundException {
		String fileName = (String) document.getProperty(ProsodyConstants.AUDIO_FILE_KEY);
		if(fileName==null)
			throw new SoundException("Document does not declare a valid audio file name: "+document.getId()); //$NON-NLS-1$

		Path folder = Core.getCore().getDataFolder().resolve("sound"); //$NON-NLS-1$

		return folder.resolve(fileName);
	}

	private synchronized void setDocument(ProsodicDocumentData newDocument) throws SoundException {

		// Always stop current clip, even if arguments are invalid!
		stopClip();

		if (newDocument == null)
			throw new NullPointerException("Invalid document"); //$NON-NLS-1$

		if(newDocument.size()==0)
			throw new IllegalArgumentException("Provided document is empty: "+document.getId()); //$NON-NLS-1$

		if(newDocument==document) {
			return;
		}

		Path newPath = getPath(newDocument);

		if(newPath.equals(path)) {
			return;
		}

		if(!Files.exists(newPath, LinkOption.NOFOLLOW_LINKS))
			throw new SoundException("Audio file of document "+newDocument.getId()+" does not exist: "+newPath); //$NON-NLS-1$ //$NON-NLS-2$

		if(clip==null) {
			try {
				clip = AudioSystem.getClip();
				clip.addLineListener(lineObserver);
			} catch (LineUnavailableException e) {
				throw new SoundException("Failed to obtain new clip - no line available", e); //$NON-NLS-1$
			}
		} else {
			clip.close();
		}

		document = newDocument;
		path = newPath;

		try(AudioInputStream audioStream = AudioSystem.getAudioInputStream(path.toFile())) {
			clip.open(audioStream);
		} catch (UnsupportedAudioFileException e) {
			throw new SoundException("Unsupported audio format in file: "+path, e); //$NON-NLS-1$
		} catch (IOException e) {
			throw new SoundException("Failed to load audio file: "+path, e); //$NON-NLS-1$
		} catch (LineUnavailableException e) {
			throw new SoundException("Unexpected error with audio device - line not available", e); //$NON-NLS-1$
		}

		System.out.printf("frames=%d duration=%.02fs\n", clip.getFrameLength(), clip.getMicrosecondLength()/1_000_000D);
	}

	private void setSentence(ProsodicSentenceData sentence) throws SoundException {
		if (sentence == null)
			throw new NullPointerException("Invalid sentence"); //$NON-NLS-1$

		if(sentence.length()==0)
			throw new IllegalArgumentException("Provided sentence is empty"); //$NON-NLS-1$

		setDocument(sentence.getDocument());
	}

	public void close() {
		stopClip();

		clip = null;
		document = null;
		path = null;
	}

	private void stopClip() {
		if(clip!=null && clip.isActive()) {
			clip.stop();
		}
	}

	private void checkClip() throws SoundException {
		if(clip==null)
			throw new SoundException("Illegal state of player - no clip resource obtained yet"); //$NON-NLS-1$

		if(!clip.isOpen())
			throw new SoundException("Current sound clip is not open"); //$NON-NLS-1$
	}

	private synchronized void play(float offset0, float offset1, boolean doLoop) throws SoundException {
		checkClip();
		stopClip();

		long msBegin = offset0==Offsets.NO_VALUE ? 0L : Offsets.toMicroSeconds(offset0);
		long msEnd = offset1==Offsets.NO_VALUE ? clip.getMicrosecondLength() : Offsets.toMicroSeconds(offset1);

		clip.setMicrosecondPosition(msBegin);

//		int lastFrame = clip.getFrameLength()-1;
//		int loopCount = Clip.LOOP_CONTINUOUSLY;
//
//		int frameBegin = 0;
//		int frameEnd = lastFrame;
//
//		double msToFrames = (double)clip.getMicrosecondLength() / (double)clip.getFrameLength();
//
//		if(offset0!=Offsets.NO_VALUE) {
//			frameBegin = (int) (offset0 * msToFrames);
//		}
//
//		if(offset1!=Offsets.NO_VALUE) {
//			frameEnd = (int) (offset1 * msToFrames);
//		}
//
//		if(frameEnd!=lastFrame && !doLoop) {
//			doLoop = true;
//			loopCount = 1;
//		}
//
//		System.out.printf("begin=%d end=%d doLoop=%b loopCount=%d length=%d\n",
//				frameBegin, frameEnd, doLoop, loopCount, lastFrame+1);
//
//		clip.setFramePosition(frameBegin);

//		if(doLoop) {
//			clip.setLoopPoints(frameBegin, frameEnd);
//			clip.loop(loopCount);
//		} else {
//			clip.start();
//		}

		clip.start();

//		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
//		gainControl.setValue(2.0F);
	}

	public void stop() throws SoundException {
		checkClip();
		stopClip();
	}

	public void playDocument(ProsodicDocumentData document, boolean doLoop) throws SoundException {
		setDocument(document);

		float offset0 = Offsets.getBeginOffset(document);
		float offset1 = Offsets.getEndOffset(document);

		play(offset0, offset1, doLoop);
	}

	public void playSentence(ProsodicSentenceData sentence, boolean doLoop) throws SoundException {
		setSentence(sentence);

		float offset0 = Offsets.getBeginOffset(sentence);
		float offset1 = Offsets.getEndOffset(sentence);

		play(offset0, offset1, doLoop);
	}

	public void playWord(ProsodicSentenceData sentence, int wordIndex, boolean doLoop) throws SoundException {
		setSentence(sentence);

		float offset0 = Offsets.getBeginOffset(sentence, wordIndex);
		float offset1 = Offsets.getEndOffset(sentence, wordIndex);

		play(offset0, offset1, doLoop);
	}

	public void playSyllable(ProsodicSentenceData sentence, int wordIndex, int sylIndex, boolean doLoop) throws SoundException {
		setSentence(sentence);

		float offset0 = Offsets.getBeginOffset(sentence, wordIndex, sylIndex);
		float offset1 = Offsets.getEndOffset(sentence, wordIndex, sylIndex);

		play(offset0, offset1, doLoop);
	}

	private class LineObserver implements LineListener {

		/**
		 * @see javax.sound.sampled.LineListener#update(javax.sound.sampled.LineEvent)
		 */
		@Override
		public void update(LineEvent event) {
			System.out.println(event);
		}

	}

	private static class Offsets {
		public static final float NO_VALUE = LanguageUtils.DATA_UNDEFINED_VALUE;

		static float getBeginOffset(ProsodicDocumentData document) {
			String audioOffset = (String) document.getProperty(ProsodyConstants.AUDIO_OFFSET_KEY);
			return audioOffset==null ? NO_VALUE : Float.parseFloat(audioOffset);
		}

		static float getEndOffset(ProsodicDocumentData document) {
			CoreferenceDocumentSet documentSet = document.getDocumentSet();
			int index = document.getDocumentIndex();
			if(index>=documentSet.size()-1) {
				return NO_VALUE;
			}

			document = (ProsodicDocumentData) documentSet.get(index+1);
			String audioOffset = (String) document.getProperty(ProsodyConstants.AUDIO_OFFSET_KEY);
			return audioOffset==null ? NO_VALUE : Float.parseFloat(audioOffset);
		}

		static float getBeginOffset(ProsodicSentenceData sentence) {
			String audioOffset = (String) sentence.getProperty(ProsodyConstants.AUDIO_OFFSET_KEY);
			return audioOffset==null ? sentence.getBeginTimestamp(0) : Float.parseFloat(audioOffset);
		}

		static float getEndOffset(ProsodicSentenceData sentence) {
			return sentence.getEndTimestamp(sentence.length()-1);
		}

		static float getBeginOffset(ProsodicSentenceData sentence, int wordIndex) {
			return sentence.getBeginTimestamp(wordIndex);
		}

		static float getEndOffset(ProsodicSentenceData sentence, int wordIndex) {
			return sentence.getEndTimestamp(wordIndex);
		}

		static float getBeginOffset(ProsodicSentenceData sentence, int wordIndex, int sylIndex) {
			return sentence.getSyllableTimestamp(wordIndex, sylIndex);
		}

		static float getEndOffset(ProsodicSentenceData sentence, int wordIndex, int sylIndex) {
			return sentence.getSyllableTimestamp(wordIndex, sylIndex)
					+ sentence.getSyllableDuration(wordIndex, sylIndex);
		}

		static long toMicroSeconds(float offset) {
			return (long) (offset*1_000_000);
		}
	}
}
