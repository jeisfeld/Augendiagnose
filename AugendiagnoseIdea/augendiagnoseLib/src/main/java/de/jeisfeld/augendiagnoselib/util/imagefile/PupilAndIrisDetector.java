package de.jeisfeld.augendiagnoselib.util.imagefile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import de.jeisfeld.augendiagnoselib.Application;

/**
 * Class that serves to detect the pupil and iris within an eye photo.
 */
public class PupilAndIrisDetector {
	/**
	 * The resolution of the image when searching for a point within the pupil.
	 */
	private static final int[] PUPIL_SEARCH_RESOLUTIONS = {100, 200, 600};
	/**
	 * The size of the maximum change distance from one zone (pupil/iris/outer) to the next, relative to the image size.
	 */
	private static final float MAX_LEAP_WIDTH = 0.05f;
	/**
	 * The minimum brightness difference accepted as a leap.
	 */
	private static final float MIN_LEAP_DIFF = 0.05f;
	/**
	 * The minimum pupil radius, relative to the image size.
	 */
	private static final float MIN_PUPIL_RADIUS = 0.04f;
	/**
	 * The minimum distance between iris and pupil, relative to the image size.
	 */
	private static final float MIN_IRIS_PUPIL_DISTANCE = 0.1f;
	/**
	 * The maximum steps of position refinement that should be done at each resolution.
	 */
	private static final int MAX_REFINEMENT_STEPS = 5;
	/**
	 * The brightness of the pupil assumed when calculating the leaps.
	 */
	private static final float ASSUMED_PUPIL_BRIGHTNESS = 0.3f;
	/**
	 * The minimum white quota expected outside the iris.
	 */
	private static final float MIN_WHITE_QUOTA = 0.3f;
	/**
	 * The secondary minimum white quota expected outside the iris.
	 */
	private static final float MIN_WHITE_QUOTA2 = 0.7f;
	/**
	 * The minimum black quota expected within the pupil.
	 */
	private static final float MIN_BLACK_QUOTA = 0.7f;
	/**
	 * The maximum black quota expected outside the pupil.
	 */
	private static final float MAX_BLACK_QUOTA = 0.3f;
	/**
	 * The vertical range where iris boundary points should be searched for.
	 */
	private static final float IRIS_BOUNDARY_SEARCH_RANGE = 0.7f;
	/**
	 * The uncertainty of the positions of the iris boundary points.
	 */
	private static final float IRIS_BOUNDARY_UNCERTAINTY_FACTOR = 0.2f;
	/**
	 * The minimum range considered when determining the iris boundary.
	 */
	private static final float IRIS_BOUNDARY_MIN_RANGE = 0.02f;
	/**
	 * Factor by which the range is changed with each retry after a search failure.
	 */
	private static final float IRIS_BOUNDARY_RETRY_FACTOR = 0.7f;
	/**
	 * The quota of points that are allowed to be too bright in the iris or too dark outside the iris.
	 */
	private static final float IRIS_BOUNDARY_WRONG_BRIGHTNESS_QUOTA = 0.2f;
	/**
	 * The quota of points around the center considered for determining the vertical center.
	 */
	private static final float IRIS_BOUNDARY_POINTS_CONSIDERED_FOR_YCENTER = 0.3f;
	/**
	 * The minimum number of boundary points needed to refine the iris position.
	 */
	private static final float IRIS_BOUNDARY_MIN_BOUNDARY_POINTS = 10;

	/**
	 * The files which are currently processed by this class. The value is used for previous file names in case of renaming.
	 */
	private static final Map<String, Set<String>> FILES_IN_PROCESS = new HashMap<>();
	/**
	 * The files which are currently processed by this class. The value contains the current file name in case of renaming.
	 */
	private static final Map<String, String> FILES_IN_PROCESS2 = new HashMap<>();

	/**
	 * The image to be analyzed.
	 */
	private Bitmap mImage;

	/**
	 * The horizontal center of the pupil (in the interval [0,1]).
	 */
	private float mPupilXCenter = 0;

	public final float getPupilXCenter() {
		return mPupilXCenter;
	}

	/**
	 * The vertical center of the pupil (in the interval [0,1]).
	 */
	private float mPupilYCenter = 0;

	public final float getPupilYCenter() {
		return mPupilYCenter;
	}

	/**
	 * The radius of the pupil (in the interval [0,1], relative to the minimum of width and height).
	 */
	private float mPupilRadius = 0;

	public final float getPupilRadius() {
		return mPupilRadius;
	}

	/**
	 * The horizontal center of the iris (in the interval [0,1]).
	 */
	private float mIrisXCenter = 0;

	public final float getIrisXCenter() {
		return mIrisXCenter;
	}

	/**
	 * The vertical center of the iris (in the interval [0,1]).
	 */
	private float mIrisYCenter = 0;

	public final float getIrisYCenter() {
		return mIrisYCenter;
	}

	/**
	 * The radius of the iris (in the interval [0,1], relative to the minimum of width and height).
	 */
	private float mIrisRadius = 0;

	public final float getIrisRadius() {
		return mIrisRadius;
	}

	/**
	 * Create a detector for a certain image.
	 *
	 * @param image The image to be analyzed.
	 */
	public PupilAndIrisDetector(final Bitmap image) {
		mImage = image;
		determineInitialParameterValues();
		for (int i = 1; i < PUPIL_SEARCH_RESOLUTIONS.length; i++) {
			int resolution = PUPIL_SEARCH_RESOLUTIONS[i];
			refinePupilPosition(resolution);
			if (resolution >= image.getWidth() && resolution >= image.getHeight()) {
				break;
			}
		}
		refineIrisPosition();
	}

	/**
	 * Determine the iris position in an image path and store it in the metadata.
	 *
	 * @param imagePath The path of the image.
	 */
	public static final void determineAndStoreIrisPosition(final String imagePath) {
		JpegMetadata origMetadata = JpegSynchronizationUtil.getJpegMetadata(imagePath);
		if (origMetadata == null
				|| origMetadata.hasOverlayPosition() && !origMetadata.hasFlag(JpegMetadata.FLAG_OVERLAY_SET_BY_CAMERA_ACTIVITY)) {
			// Do not overwrite manually set overlay position.
			return;
		}

		new Thread() {
			@Override
			public void run() {
				synchronized (FILES_IN_PROCESS) {
					if (FILES_IN_PROCESS2.keySet().contains(imagePath)) {
						return;
					}
					else {
						FILES_IN_PROCESS.put(imagePath, new HashSet<String>());
						FILES_IN_PROCESS2.put(imagePath, imagePath);
					}
				}

				try {
					Log.v(Application.TAG, "Start finding iris for " + imagePath);
					long timestamp = System.currentTimeMillis();
					PupilAndIrisDetector detector = new PupilAndIrisDetector(ImageUtil.getImageBitmap(imagePath, 0));
					Log.v(Application.TAG, "Finished finding iris for " + imagePath + ". Duration: "
							+ ((System.currentTimeMillis() - timestamp) / 1000.0)); // MAGIC_NUMBER

					// Retrieve image path - in case the file has moved.
					String newImagePath = FILES_IN_PROCESS2.get(imagePath);

					JpegMetadata metadata = JpegSynchronizationUtil.getJpegMetadata(newImagePath);
					// re-check if position has been set manually.
					if (metadata != null && (!metadata.hasOverlayPosition() || metadata.hasFlag(JpegMetadata.FLAG_OVERLAY_SET_BY_CAMERA_ACTIVITY))) {
						detector.updateMetadata(metadata);
						JpegSynchronizationUtil.storeJpegMetadata(newImagePath, metadata);
					}
				}
				catch (Throwable e) {
					Log.e(Application.TAG, "Failed to find iris and pupil position for file " + imagePath, e);
				}
				finally {
					synchronized (FILES_IN_PROCESS) {
						String newImagePath = FILES_IN_PROCESS2.get(imagePath);
						Set<String> oldPaths = FILES_IN_PROCESS.get(newImagePath);
						if (oldPaths != null) {
							for (String oldPath : oldPaths) {
								FILES_IN_PROCESS2.remove(oldPath);
							}
						}
						FILES_IN_PROCESS.remove(newImagePath);
						FILES_IN_PROCESS2.remove(newImagePath);
					}
				}
			}
		}.start();
	}

	/**
	 * Inform about the move of a file during determination of iris position, so that the result may be applied to the moved file.
	 *
	 * @param oldFileName The old file name.
	 * @param newFileName the new file name.
	 */
	public static final void notifyFileRename(final String oldFileName, final String newFileName) {
		if (!FILES_IN_PROCESS.containsKey(oldFileName)) {
			return;
		}

		synchronized (FILES_IN_PROCESS) {
			Set<String> oldPaths = FILES_IN_PROCESS.get(oldFileName);
			if (oldPaths != null) {
				for (String oldPath : oldPaths) {
					FILES_IN_PROCESS2.put(oldPath, newFileName);
				}
				FILES_IN_PROCESS2.put(oldFileName, newFileName);
				FILES_IN_PROCESS2.put(newFileName, newFileName);

				oldPaths.add(oldFileName);
				FILES_IN_PROCESS.remove(oldFileName);
				FILES_IN_PROCESS.put(newFileName, oldPaths);
			}
		}
	}

	/**
	 * Update the stored metadata with the iris and pupil position from the detector.
	 *
	 * @param metadata The metadata to be updated.
	 */
	public final void updateMetadata(final JpegMetadata metadata) {
		if (mPupilRadius > 0 && mIrisRadius > mPupilRadius) {
			metadata.setXCenter(mIrisXCenter);
			metadata.setYCenter(mIrisYCenter);
			metadata.setOverlayScaleFactor(mIrisRadius * 8 / 3); // MAGIC_NUMBER

			metadata.setPupilXOffset((mPupilXCenter - mIrisXCenter) / (2 * mIrisRadius));
			metadata.setPupilYOffset((mPupilYCenter - mIrisYCenter) / (2 * mIrisRadius));
			metadata.setPupilSize(mPupilRadius / mIrisRadius);

			metadata.addFlag(JpegMetadata.FLAG_OVERLAY_POSITION_DETERMINED_AUTOMATICALLY);
			metadata.removeFlag(JpegMetadata.FLAG_OVERLAY_SET_BY_CAMERA_ACTIVITY);
		}
	}

	/**
	 * Find initial values of pupil center and pupil and iris radius.
	 */
	private void determineInitialParameterValues() {
		Bitmap image = ImageUtil.resizeBitmap(mImage, PUPIL_SEARCH_RESOLUTIONS[0], false);
		List<PupilCenterInfo> pupilCenterInfoList = new ArrayList<>();

		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

		for (int x = image.getWidth() / 4; x < image.getWidth() * 3 / 4; x++) { // MAGIC_NUMBER
			for (int y = image.getHeight() / 4; y < image.getHeight() * 3 / 4; y++) { // MAGIC_NUMBER
				PupilCenterInfo pupilCenterInfo = new PupilCenterInfo(image, pixels, x, y, PupilCenterInfo.Phase.INITIAL);
				pupilCenterInfo.collectCircleInfo(Integer.MAX_VALUE);
				pupilCenterInfoList.add(pupilCenterInfo);
			}
		}

		float maxLeapValue = Float.MIN_VALUE;
		PupilCenterInfo bestPupilCenter = null;
		for (PupilCenterInfo pupilCenterInfo : pupilCenterInfoList) {
			pupilCenterInfo.calculateStatistics(0);
			if (pupilCenterInfo.mLeapValue > maxLeapValue) {
				maxLeapValue = pupilCenterInfo.mLeapValue;
				bestPupilCenter = pupilCenterInfo;
			}
		}
		if (bestPupilCenter != null) {
			mPupilXCenter = (float) bestPupilCenter.mXCenter / image.getWidth();
			mPupilYCenter = (float) bestPupilCenter.mYCenter / image.getHeight();
			mPupilRadius = (float) bestPupilCenter.mPupilRadius / Math.max(image.getWidth(), image.getHeight());
			mIrisXCenter = mPupilXCenter;
			mIrisYCenter = mPupilYCenter;
			mIrisRadius = (float) bestPupilCenter.mIrisRadius / Math.max(image.getWidth(), image.getHeight());
		}
	}

	/**
	 * Refine the pupil position based on the previously found position and a higher resolution.
	 *
	 * @param resolution The resolution.
	 */
	private void refinePupilPosition(final int resolution) {
		Bitmap image = ImageUtil.resizeBitmap(mImage, resolution, false);
		List<PupilCenterInfo> pupilCenterInfoList = new ArrayList<>();

		int pupilXCenter = Math.round(mPupilXCenter * image.getWidth());
		int pupilYCenter = Math.round(mPupilYCenter * image.getHeight());
		int pupilRadius = Math.round(mPupilRadius * Math.max(image.getWidth(), image.getHeight()));

		boolean isStable = false;
		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

		for (int step = 0; step < MAX_REFINEMENT_STEPS && !isStable; step++) {
			for (int x = pupilXCenter - 1; x <= pupilXCenter + 1; x++) {
				for (int y = pupilYCenter - 1; y <= pupilYCenter + 1; y++) {
					PupilCenterInfo pupilCenterInfo = new PupilCenterInfo(image, pixels, x, y, PupilCenterInfo.Phase.PUPIL_REFINEMENT);
					pupilCenterInfo.collectCircleInfo((int) (pupilRadius + MAX_REFINEMENT_STEPS + MAX_LEAP_WIDTH * resolution));
					pupilCenterInfoList.add(pupilCenterInfo);
				}
			}

			float maxLeapValue = Float.MIN_VALUE;
			PupilCenterInfo bestPupilCenter = null;
			for (PupilCenterInfo pupilCenterInfo : pupilCenterInfoList) {
				pupilCenterInfo.calculateStatistics(pupilRadius);
				if (pupilCenterInfo.mLeapValue > maxLeapValue) {
					maxLeapValue = pupilCenterInfo.mLeapValue;
					bestPupilCenter = pupilCenterInfo;
				}
			}

			isStable = bestPupilCenter == null
					|| (bestPupilCenter.mXCenter == pupilXCenter && bestPupilCenter.mYCenter == pupilYCenter
					&& bestPupilCenter.mPupilRadius == pupilRadius);
			if (bestPupilCenter != null) {
				pupilXCenter = bestPupilCenter.mXCenter;
				pupilYCenter = bestPupilCenter.mYCenter;
				pupilRadius = bestPupilCenter.mPupilRadius;
			}
		}

		mPupilXCenter = (float) pupilXCenter / image.getWidth();
		mPupilYCenter = (float) pupilYCenter / image.getHeight();
		mPupilRadius = (float) pupilRadius / Math.max(image.getWidth(), image.getHeight());
	}

	/**
	 * Refine the iris position based on the previously found position.
	 */
	private void refineIrisPosition() {
		IrisBoundary irisBoundary = new IrisBoundary(mImage,
				(int) (mImage.getWidth() * mIrisXCenter),
				(int) (mImage.getHeight() * mIrisYCenter),
				(int) (Math.max(mImage.getWidth(), mImage.getHeight()) * mIrisRadius));

		irisBoundary.analyzeBoundary();

		mIrisXCenter = (float) irisBoundary.mXCenter / mImage.getWidth();
		mIrisYCenter = (float) irisBoundary.mYCenter / mImage.getHeight();
		mIrisRadius = (float) irisBoundary.mRadius / Math.max(mImage.getWidth(), mImage.getHeight());
	}

	/**
	 * The collected info about the circles around a potential pupil center.
	 */
	private static final class PupilCenterInfo {
		/**
		 * The x coordinate of the center.
		 */
		private int mXCenter;
		/**
		 * The y coordinate of the center.
		 */
		private int mYCenter;
		/**
		 * The calculated pupil radius for this center.
		 */
		private int mPupilRadius = 0;
		/**
		 * The calculated iris radius for this center.
		 */
		private int mIrisRadius = 0;
		/**
		 * The image.
		 */
		private Bitmap mImage;
		/**
		 * The image pixels.
		 */
		private int[] mPixels;
		/**
		 * The phase in which the info is used.
		 */
		private Phase mPhase;

		/**
		 * The information about the circles around this point.
		 */
		private Map<Integer, CircleInfo> mCircleInfos = new HashMap<>();

		/**
		 * The brightness leap value for this center.
		 */
		private float mLeapValue = Float.MIN_VALUE;

		/**
		 * Create a PupilCenterInfo with certain coordinates.
		 *
		 * @param image  the image.
		 * @param pixels the image pixels.
		 * @param xCoord The x coordinate.
		 * @param yCoord The y coordinate.
		 * @param phase  The phase in which the info is used.
		 */
		private PupilCenterInfo(final Bitmap image, final int[] pixels, final int xCoord, final int yCoord, final Phase phase) {
			mXCenter = xCoord;
			mYCenter = yCoord;
			mImage = image;
			mPixels = pixels;
			mPhase = phase;
		}

		/**
		 * Collect the information of all circles around the center.
		 *
		 * @param maxRelevantRadius The maximal circle radius considered
		 */
		private void collectCircleInfo(final int maxRelevantRadius) {
			int width = mImage.getWidth();
			int maxPossibleRadius = Math.min(
					Math.min(mImage.getWidth() - 1 - mXCenter, mXCenter),
					Math.min(mImage.getHeight() - 1 - mYCenter, mYCenter));
			int maxRadius = Math.min(maxRelevantRadius, maxPossibleRadius);
			long maxRadius2 = (maxRadius + 1) * (maxRadius + 1);
			for (int x = mXCenter - maxRadius; x <= mXCenter + maxRadius; x++) {
				for (int y = mYCenter - maxRadius; y <= mYCenter + maxRadius; y++) {
					long d2 = (x - mXCenter) * (x - mXCenter) + (y - mYCenter) * (y - mYCenter);
					if (d2 <= maxRadius2) {
						int d = (int) Math.round(Math.sqrt(d2));
						int brightness = getBrightness(mPixels[y * width + x]);
						// short brightness = getBrightness(mImage.getPixel(x, y));
						addInfo(d, brightness);
					}
				}
			}
		}

		/**
		 * Get a brightness value from a color.
		 *
		 * @param color The color
		 * @return The brightness value.
		 */
		private static int getBrightness(final int color) {
			int min = Math.min(Math.min(Color.red(color), Color.green(color)), Color.blue(color));
			int sum = Color.red(color) + Color.green(color) + Color.blue(color);
			// Ensure that colors count more than dark grey, but white counts more then colors.
			return sum - min;
		}

		/**
		 * Add pixel info for another pixel.
		 *
		 * @param distance   The distance of the pixel.
		 * @param brightness The brightness of the pixel.
		 */
		private void addInfo(final int distance, final int brightness) {
			CircleInfo circleInfo = mCircleInfos.get(distance);
			if (circleInfo == null) {
				circleInfo = new CircleInfo(distance);
				mCircleInfos.put(distance, circleInfo);
			}
			circleInfo.addBrightness(brightness);
		}

		/**
		 * Do statistical calculations after all brightnesses are available.
		 *
		 * @param baseRadius the base radius to be used in refinement phases.
		 */
		private void calculateStatistics(final int baseRadius) {
			// Base calculations for each circle.
			for (CircleInfo circleInfo : mCircleInfos.values()) {
				circleInfo.calculateStatistics();
			}

			int resolution = Math.max(mImage.getWidth(), mImage.getHeight());
			int maxRadius = mPhase == Phase.INITIAL
					? mCircleInfos.size() - 1
					: Math.min(mCircleInfos.size() - 1, baseRadius + MAX_REFINEMENT_STEPS + (int) (MAX_LEAP_WIDTH * resolution));
			int minRadius = mPhase == Phase.INITIAL ? 0
					: Math.max(0, baseRadius - MAX_REFINEMENT_STEPS - (int) (MAX_LEAP_WIDTH * resolution));

			// Calculate the minimum of medians outside each circle.
			float innerQuantileSum = 0;
			float[] innerDarkness = new float[mCircleInfos.size()];

			for (int i = minRadius; i <= maxRadius; i++) {
				float currentQuantile = mCircleInfos.get(Integer.valueOf(i)).getQuantile(MIN_BLACK_QUOTA);
				innerQuantileSum += currentQuantile * i;
				innerDarkness[i] = i == 0 ? 0 : 2 * innerQuantileSum / (i * (i + 1));
			}

			List<CircleInfo> relevantPupilCircles = new ArrayList<>();
			List<CircleInfo> relevantIrisCircles = new ArrayList<>();
			maxRadius = mPhase == Phase.INITIAL
					? mCircleInfos.size() - 2
					: Math.min(mCircleInfos.size() - 2, baseRadius + MAX_REFINEMENT_STEPS);
			minRadius = mPhase == Phase.INITIAL ? (int) (resolution * MIN_PUPIL_RADIUS)
					: Math.max(1, baseRadius - MAX_REFINEMENT_STEPS);

			if (mPhase == Phase.INITIAL || mPhase == Phase.PUPIL_REFINEMENT) {
				// determine pupil leap
				for (int i = minRadius; i <= maxRadius; i++) {
					float pupilLeapValue = 0;
					int maxLeapDistance = Math.min(Math.round(MAX_LEAP_WIDTH * resolution),
							Math.min(i / 2, (mCircleInfos.size() - 1 - i) / 2));
					for (int j = 1; j <= maxLeapDistance; j++) {
						float diff = mPhase == Phase.INITIAL
								? (ASSUMED_PUPIL_BRIGHTNESS + getMinMaxQuantile(MAX_BLACK_QUOTA, i + j, i + j + maxLeapDistance, false))
								/ (ASSUMED_PUPIL_BRIGHTNESS
								+ getMinMaxQuantile(MIN_BLACK_QUOTA, i - j - Math.min(maxLeapDistance, Math.max(j, 2)), i - j, true))
								- 1
								: (ASSUMED_PUPIL_BRIGHTNESS + getMinMaxQuantile(MAX_BLACK_QUOTA, i + j, i + j + maxLeapDistance, false))
								/ (ASSUMED_PUPIL_BRIGHTNESS
								+ getMinMaxQuantile(MIN_BLACK_QUOTA, i - Math.min(maxLeapDistance, Math.max(j, 2)), i, true))
								- 1;
						if (diff > MIN_LEAP_DIFF) {
							// prefer big jumps in small radius difference.
							float newLeapValue = (float) (diff / Math.pow(j, 0.8)); // MAGIC_NUMBER
							if (newLeapValue > pupilLeapValue) {
								pupilLeapValue = newLeapValue;
							}
						}
					}
					if (pupilLeapValue > 0) {
						CircleInfo circleInfo = mCircleInfos.get(Integer.valueOf(i));
						// prefer big, dark circles
						circleInfo.mPupilLeapValue = (float) (Math.sqrt(i) * pupilLeapValue / innerDarkness[i]);
						relevantPupilCircles.add(circleInfo);
					}
				}
			}

			if (mPhase == Phase.INITIAL || mPhase == Phase.IRIS_REFINEMENT) {
				// determine iris leap
				for (int i = minRadius; i <= maxRadius; i++) {
					float irisLeapValue = 0;
					float irisQuantileSum = 0;
					int maxLeapDistance = Math.min(Math.round(MAX_LEAP_WIDTH * resolution),
							Math.min(i, mCircleInfos.size() - 1 - i));
					for (int j = 1; j <= maxLeapDistance; j++) {
						irisQuantileSum +=
								(mCircleInfos.get(Integer.valueOf(i + j)).getQuantile(1 - MIN_WHITE_QUOTA)
										- mCircleInfos.get(Integer.valueOf(i - j)).getQuantile(1 - MIN_WHITE_QUOTA)
										+ mCircleInfos.get(Integer.valueOf(i + j)).getQuantile(1 - MIN_WHITE_QUOTA2)
										- mCircleInfos.get(Integer.valueOf(i - j)).getQuantile(1 - MIN_WHITE_QUOTA2))
										/ (2 * Math.sqrt(j));
						if (irisQuantileSum > 0) {
							// prefer big jumps in small radius difference.
							float newLeapValue = irisQuantileSum / j;
							if (newLeapValue > irisLeapValue) {
								irisLeapValue = newLeapValue;
							}
						}
					}
					if (irisLeapValue > 0) {
						CircleInfo circleInfo = mCircleInfos.get(Integer.valueOf(i));
						// prefer big radius in order to prevent selection of small spots.
						// prefer dark inner area
						circleInfo.mIrisLeapValue = irisLeapValue;
						relevantIrisCircles.add(circleInfo);
					}
				}
			}

			switch (mPhase) {
			case INITIAL:
				for (CircleInfo pupilCircleInfo : relevantPupilCircles) {
					for (CircleInfo irisCircleInfo : relevantIrisCircles) {
						if (irisCircleInfo.mRadius - pupilCircleInfo.mRadius >= resolution
								* MIN_IRIS_PUPIL_DISTANCE) {
							float newLeapValue = pupilCircleInfo.mPupilLeapValue * (1 + irisCircleInfo.mIrisLeapValue);
							if (newLeapValue > mLeapValue) {
								mLeapValue = newLeapValue;
								mPupilRadius = pupilCircleInfo.mRadius;
								mIrisRadius = irisCircleInfo.mRadius;
							}
						}
					}
				}
				break;
			case PUPIL_REFINEMENT:
				for (CircleInfo pupilCircleInfo : relevantPupilCircles) {
					float newLeapValue = pupilCircleInfo.mPupilLeapValue;
					if (newLeapValue > mLeapValue) {
						mLeapValue = newLeapValue;
						mPupilRadius = pupilCircleInfo.mRadius;
					}
				}
				break;
			case IRIS_REFINEMENT:
			default:
				for (CircleInfo irisCircleInfo : relevantIrisCircles) {
					float newLeapValue = irisCircleInfo.mIrisLeapValue;
					if (newLeapValue > mLeapValue) {
						mLeapValue = newLeapValue;
						mIrisRadius = irisCircleInfo.mRadius;
					}
				}
				break;
			}
		}

		/**
		 * Get the minimum p-quantile for a certain set of radii.
		 *
		 * @param p          The quantile parameter.
		 * @param fromRadius The start radius.
		 * @param toRadius   The end radius.
		 * @param max        if true, the maximum is returned, otherwise the minimum.
		 * @return The minimum quantile.
		 */
		private float getMinMaxQuantile(final float p, final int fromRadius, final int toRadius, final boolean max) {
			float result = max ? Float.MIN_VALUE : Float.MAX_VALUE;
			for (int radius = fromRadius; radius <= toRadius; radius++) {
				float newValue = mCircleInfos.get(Integer.valueOf(radius)).getQuantile(p);
				if ((!max && newValue < result) || (max && newValue > result)) {
					result = newValue;
				}
			}
			return result;
		}

		/**
		 * The phase in which the algorithm is.
		 */
		private enum Phase {
			/**
			 * Initial positioning of pupil and iris.
			 */
			INITIAL,
			/**
			 * Refinement of pupil position.
			 */
			PUPIL_REFINEMENT,
			/**
			 * Refinement of iris position.
			 */
			IRIS_REFINEMENT
		}
	}

	/**
	 * Class for storing information about a circle of points.
	 */
	private static final class CircleInfo {
		/**
		 * Create a pixelInfo with certain coordinates.
		 *
		 * @param radius The radius.
		 */
		private CircleInfo(final int radius) {
			mRadius = radius;
		}

		/**
		 * The radius.
		 */
		private int mRadius;
		/**
		 * The brightnesses.
		 */
		private List<Integer> mBrightnesses = new ArrayList<>();
		/**
		 * The brightness leap at this radius used for pupil identification.
		 */
		private float mPupilLeapValue;
		/**
		 * The brightness leap at this radius used for iris identification.
		 */
		private float mIrisLeapValue;

		/**
		 * Add a brightness to the information of this circle.
		 *
		 * @param brightness the brightness.
		 */
		private void addBrightness(final int brightness) {
			mBrightnesses.add(brightness);
		}

		/**
		 * Do statistical calculations after all brightnesses are available. Here, only sorting is required.
		 */
		private void calculateStatistics() {
			Collections.sort(mBrightnesses);
		}

		/**
		 * Get the p-quantile of the brightnesses. Prerequisite: calculateStatistics must have been run before.
		 *
		 * @param p the quantile parameter.
		 * @return the p-quantile of the brightnesses (not considering equality).
		 */
		private int getQuantile(final float p) {
			return mBrightnesses.get((int) (mBrightnesses.size() * p));
		}
	}

	/**
	 * Class for collecting information about the iris boundary.
	 */
	private static final class IrisBoundary {
		/**
		 * The image.
		 */
		private Bitmap mImage;

		/**
		 * The x coordinate of the center.
		 */
		private int mXCenter;
		/**
		 * The y coordinate of the center.
		 */
		private int mYCenter;
		/**
		 * The iris radius.
		 */
		private int mRadius = 0;

		/**
		 * The points on the left side of the iris boundary (map from y to x coordinate).
		 */
		private Map<Integer, Integer> mLeftPoints = new HashMap<>();
		/**
		 * The points on the right side of the iris boundary (map from y to x coordinate).
		 */
		private Map<Integer, Integer> mRightPoints = new HashMap<>();

		/**
		 * Initialize the IrisBoundary.
		 *
		 * @param image   The image.
		 * @param xCenter the initial x coordinate of the center.
		 * @param yCenter the initial y coordinate of the center.
		 * @param radius  the initial iris radius.
		 */
		private IrisBoundary(final Bitmap image, final int xCenter, final int yCenter, final int radius) {
			mImage = image;
			mXCenter = xCenter;
			mYCenter = yCenter;
			mRadius = radius;
		}

		/**
		 * Search points on the iris boundary.
		 */
		private void determineBoundaryPoints() {
			for (int yCoord = mYCenter; yCoord <= mYCenter + mRadius * IRIS_BOUNDARY_SEARCH_RANGE && yCoord < mImage.getHeight(); yCoord++) {
				determineBoundaryPoints(yCoord);
			}

			for (int yCoord = mYCenter - 1; yCoord >= mYCenter - mRadius * IRIS_BOUNDARY_SEARCH_RANGE && yCoord >= 0; yCoord--) {
				determineBoundaryPoints(yCoord);
			}
		}

		/**
		 * Determine the boundary points for a certain y coordinate.
		 *
		 * @param yCoord The y coordinate for which to find the boundary points.
		 * @return true if a boundary point has been found.
		 */
		private boolean determineBoundaryPoints(final int yCoord) {
			int xDistanceRange = Math.round(IRIS_BOUNDARY_UNCERTAINTY_FACTOR * mRadius);
			int xDistanceMinRange = Math.round(IRIS_BOUNDARY_MIN_RANGE * mRadius);
			boolean found = false;

			while (!found && xDistanceRange >= xDistanceMinRange) {
				found = determineBoundaryPoints(yCoord, xDistanceRange);
				xDistanceRange *= IRIS_BOUNDARY_RETRY_FACTOR;
			}
			return found;
		}

		/**
		 * Determine the boundary points for a certain y coordinate.
		 *
		 * @param yCoord         The y coordinate for which to find the boundary points.
		 * @param xDistanceRange the horizontal range which is considered.
		 * @return true if a boundary point has been found.
		 */
		private boolean determineBoundaryPoints(final int yCoord, final int xDistanceRange) {
			int yDiff = yCoord - mYCenter;
			if (Math.abs(yDiff) > IRIS_BOUNDARY_SEARCH_RANGE * mRadius) {
				return false;
			}

			int expectedXDistance = (int) Math.round(Math.sqrt(mRadius * mRadius - yDiff * yDiff));

			// Left side - calculate average brightness
			float brightnessSum = 0;
			int leftBoundary = Math.max(mXCenter - expectedXDistance - xDistanceRange, 0);
			int rightBoundary = Math.min(mXCenter - expectedXDistance + xDistanceRange, mImage.getWidth() - 1);
			for (int x = leftBoundary; x <= rightBoundary; x++) {
				brightnessSum += getBrightness(mImage.getPixel(x, yCoord));
			}
			float avgBrightness = brightnessSum / (2 * xDistanceRange + 1);

			// Left side - find transition from light to dark
			int leftCounter = 0;
			int rightCounter = 0;
			while (leftBoundary < rightBoundary) {
				if (rightCounter > leftCounter) {
					if (getBrightness(mImage.getPixel(leftBoundary++, yCoord)) < avgBrightness) {
						leftCounter++;
					}
				}
				else {
					if (getBrightness(mImage.getPixel(rightBoundary--, yCoord)) > avgBrightness) {
						rightCounter++;
					}
				}
			}
			if (leftCounter > IRIS_BOUNDARY_WRONG_BRIGHTNESS_QUOTA * xDistanceRange) {
				return false;
			}

			// Right side - calculate average brightness
			float brightnessSum2 = 0;
			int leftBoundary2 = Math.max(mXCenter + expectedXDistance - xDistanceRange, 0);
			int rightBoundary2 = Math.min(mXCenter + expectedXDistance + xDistanceRange, mImage.getWidth() - 1);
			for (int x = leftBoundary2; x <= rightBoundary2; x++) {
				brightnessSum2 += getBrightness(mImage.getPixel(x, yCoord));
			}
			float avgBrightness2 = brightnessSum2 / (2 * xDistanceRange + 1);

			// Right side - find transition from light to dark
			int leftCounter2 = 0;
			int rightCounter2 = 0;
			while (leftBoundary2 < rightBoundary2) {
				if (leftCounter2 > rightCounter2) {
					if (getBrightness(mImage.getPixel(rightBoundary2--, yCoord)) < avgBrightness2) {
						rightCounter2++;
					}
				}
				else {
					if (getBrightness(mImage.getPixel(leftBoundary2++, yCoord)) > avgBrightness2) {
						leftCounter2++;
					}
				}
			}
			if (rightCounter2 > IRIS_BOUNDARY_WRONG_BRIGHTNESS_QUOTA * xDistanceRange) {
				return false;
			}

			mLeftPoints.put(yCoord, rightBoundary);
			mRightPoints.put(yCoord, leftBoundary2);
			return true;
		}

		/**
		 * Determine the iris center and radius from the iris boundary points.
		 */
		private void analyzeBoundary() {
			determineBoundaryPoints();
			if (mLeftPoints.size() > IRIS_BOUNDARY_MIN_BOUNDARY_POINTS) {
				determineXCenter();
				determineYCenter();
				determineRadius();
			}
		}

		/**
		 * Determine the x center from the boundary points.
		 */
		private void determineXCenter() {
			// Determine x center as median of the boundary mid points
			List<Integer> xSumValues = new ArrayList<>();
			for (Integer yCoord : mLeftPoints.keySet()) {
				xSumValues.add(mLeftPoints.get(yCoord) + mRightPoints.get(yCoord));
			}

			Collections.sort(xSumValues);

			mXCenter = xSumValues.get(xSumValues.size() / 2) / 2;
		}

		/**
		 * Determine the y center from the boundary points, knowing the x center.
		 */
		private void determineYCenter() {
			// Consider the sum of left and right distance.
			Map<Integer, List<Integer>> distanceSums = new HashMap<>();
			for (Integer y : mLeftPoints.keySet()) {
				int sum = mRightPoints.get(y) - mLeftPoints.get(y);
				List<Integer> listForSum = distanceSums.get(sum);
				if (listForSum == null) {
					listForSum = new ArrayList<>();
					distanceSums.put(sum, listForSum);
				}
				listForSum.add(y);
			}

			// Sort distances in descending order
			List<Integer> distances = new ArrayList<>(distanceSums.keySet());
			Collections.sort(distances);
			Collections.reverse(distances);

			int count = 0;
			int sum = 0;
			int countUntil = (int) (IRIS_BOUNDARY_POINTS_CONSIDERED_FOR_YCENTER * mLeftPoints.size());
			for (Integer distance : distances) {
				for (int y : distanceSums.get(distance)) {
					sum += y;
					count++;
				}
				if (count >= countUntil) {
					break;
				}
			}

			mYCenter = sum / count;
		}

		/**
		 * Determine the radius from boundary points, after center is known.
		 */
		private void determineRadius() {
			float sum = 0;
			for (Integer y : mLeftPoints.keySet()) {
				int yDistance = y - mYCenter;
				int xDistance = mLeftPoints.get(y) - mXCenter;
				sum += Math.sqrt(xDistance * xDistance + yDistance * yDistance);
			}
			for (Integer y : mRightPoints.keySet()) {
				int yDistance = y - mYCenter;
				int xDistance = mRightPoints.get(y) - mXCenter;
				sum += Math.sqrt(xDistance * xDistance + yDistance * yDistance);
			}

			mRadius = Math.round(sum / (2 * mLeftPoints.size()));
		}

		/**
		 * Get a brightness value from a color.
		 *
		 * @param color The color
		 * @return The brightness value.
		 */
		private static int getBrightness(final int color) {
			// Blue seems to be particulary helpful in the separation.
			return Math.min(Math.min(Color.red(color), Color.green(color)), Color.blue(color)) + Color.blue(color);
		}

	}

}
