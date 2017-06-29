package de.jeisfeld.augendiagnoselib.util;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import de.jeisfeld.augendiagnoselib.Application;

/**
 * Utility class for handling audio data.
 */
public final class AudioUtil {
	/**
	 * The Audio bitrate used.
	 */
	private static final double BITRATE = 44100.0;

	/**
	 * The number of milliseconds in a second.
	 */
	private static final double MILLIS_IN_SECOND = 1000.0;

	/**
	 * A map used for generation of high frequency sine wave.
	 */
	private static final Short[] TONE_MAP = {(short) 0, Short.MAX_VALUE, (short) 0, Short.MIN_VALUE};

	/**
	 * Hide default constructor.
	 */
	private AudioUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Create a sin wave of certain frequency and duration.
	 *
	 * @param freqHz     The frequency in Hertz
	 * @param durationMs The duration in milliseconds
	 * @return An AudioTrack with the corresponding sine wave.
	 */
	public static AudioTrack generateTone(final double freqHz, final int durationMs) {
		int count = (int) (BITRATE * 2.0 * (durationMs / MILLIS_IN_SECOND)) & ~1;
		short[] samples = new short[count];
		for (int i = 0; i < count; i += 2) {
			short sample = (short) (Math.sin(2 * Math.PI * i / (BITRATE / freqHz)) * 0x7FFF); // MAGIC_NUMBER
			samples[i] = sample;
			samples[i + 1] = sample;
		}
		AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, (int) BITRATE,
				AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
				count * (Short.SIZE / 8), AudioTrack.MODE_STATIC); // MAGIC_NUMBER
		track.write(samples, 0, count);
		return track;
	}

	/**
	 * Create a tone of highest possible frequency.
	 *
	 * @param durationMs The duration in milliseconds
	 * @return An AudioTrack with the corresponding sine wave.
	 */
	public static AudioTrack generateHighFreqTone(final int durationMs) {
		int count = (int) (BITRATE * 2.0 * (durationMs / MILLIS_IN_SECOND)) & ~1;
		short[] samples = new short[count];
		for (int i = 0; i < count; i += 2) {
			short sample = TONE_MAP[(i / 2) % 4]; // MAGIC_NUMBER
			samples[i] = sample;
			samples[i + 1] = sample;
		}
		AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, (int) BITRATE,
				AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
				count * (Short.SIZE / 8), AudioTrack.MODE_STATIC); // MAGIC_NUMBER
		track.write(samples, 0, count);
		return track;
	}

	/**
	 * Check if a headset is plugged.
	 *
	 * @return true if a headset is plugged.
	 */
	@SuppressWarnings("deprecation")
	public static boolean isHeadphonePlugged() {
		AudioManager audioManager = (AudioManager) Application.getAppContext().getSystemService(Context.AUDIO_SERVICE);
		//noinspection deprecation
		return audioManager.isWiredHeadsetOn();
	}


	/**
	 * A helper class handling a high frequency audio beep.
	 */
	public static class Beep {
		/**
		 * The tone length in milliseconds.
		 */
		private static final int TONE_LENGTH = 15000;

		/**
		 * The mAudioTrack holding the beep.
		 */
		private AudioTrack mAudioTrack = generateHighFreqTone(TONE_LENGTH);

		/**
		 * Start playing the beep.
		 */
		public final void start() {
			mAudioTrack.play();
		}

		/**
		 * Stop playing the beep.
		 */
		public final void stop() {
			// method AudioTrack.stop() does not work reliably on all devices.
			mAudioTrack.release();
			mAudioTrack = generateHighFreqTone(TONE_LENGTH);
		}

		@Override
		public final void finalize() throws Throwable {
			mAudioTrack.release();
			super.finalize();
		}
	}

}
