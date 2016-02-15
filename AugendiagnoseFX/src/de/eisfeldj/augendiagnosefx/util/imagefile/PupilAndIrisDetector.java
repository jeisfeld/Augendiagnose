package de.eisfeldj.augendiagnosefx.util.imagefile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.eisfeldj.augendiagnosefx.util.Logger;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

/**
 * Class that serves to detect the pupil and iris within an eye photo.
 */
public class PupilAndIrisDetector {
	/**
	 * The resolution of the image when searching for a point within the pupil.
	 */
	private static final int[] PUPIL_SEARCH_RESOLUTIONS = {100, 200, 600, 1800};
	/**
	 * The resolution of the image when searching for a point within the pupil.
	 */
	private static final int[] IRIS_SEARCH_RESOLUTIONS = {100, 300};
	/**
	 * The size of the maximum change distance from one zone (pupil/iris/outer) to the next, relative to the image size.
	 */
	private static final double MAX_LEAP_WIDTH = 0.05;
	/**
	 * The minimum brightness difference accepted as a leap.
	 */
	private static final double MIN_LEAP_DIFF = 0.05;
	/**
	 * The minimum pupil radius, relative to the image size.
	 */
	private static final double MIN_PUPIL_RADIUS = 0.04;
	/**
	 * The minimum distance between iris and pupil, relative to the image size.
	 */
	private static final double MIN_IRIS_PUPIL_DISTANCE = 0.1;
	/**
	 * The maximum steps of position refinement that should be done at each resolution.
	 */
	private static final int MAX_REFINEMENT_STEPS = 5;
	/**
	 * The brightness of the pupil assumed when calculating the leaps.
	 */
	private static final double ASSUMED_PUPIL_BRIGHTNESS = 0.3;

	/**
	 * The minimum white quota expected outside the iris.
	 */
	private static final double MIN_WHITE_QUOTA = 0.3;
	/**
	 * The secondary minimum white quota expected outside the iris.
	 */
	private static final double MIN_WHITE_QUOTA2 = 0.7;
	/**
	 * The minimum black quota expected within the pupil.
	 */
	private static final double MIN_BLACK_QUOTA = 0.7;
	/**
	 * The maximum black quota expected outside the pupil.
	 */
	private static final double MAX_BLACK_QUOTA = 0.3;

	/**
	 * The image to be analyzed.
	 */
	private Image mImage;

	/**
	 * The horizontal center of the pupil (in the interval [0,1]).
	 */
	private double mPupilXCenter = 0;

	public final double getPupilXCenter() {
		return mPupilXCenter;
	}

	/**
	 * The vertical center of the pupil (in the interval [0,1]).
	 */
	private double mPupilYCenter = 0;

	public final double getPupilYCenter() {
		return mPupilYCenter;
	}

	/**
	 * The radius of the pupil (in the interval [0,1], relative to the minimum of width and height).
	 */
	private double mPupilRadius = 0;

	public final double getPupilRadius() {
		return mPupilRadius;
	}

	/**
	 * The horizontal center of the iris (in the interval [0,1]).
	 */
	private double mIrisXCenter = 0;

	public final double getIrisXCenter() {
		return mIrisXCenter;
	}

	/**
	 * The vertical center of the iris (in the interval [0,1]).
	 */
	private double mIrisYCenter = 0;

	public final double getIrisYCenter() {
		return mIrisYCenter;
	}

	/**
	 * The radius of the iris (in the interval [0,1], relative to the minimum of width and height).
	 */
	private double mIrisRadius = 0;

	public final double getIrisRadius() {
		return mIrisRadius;
	}

	/**
	 * Create a detector for a certain image.
	 *
	 * @param image The image to be analyzed.
	 */
	public PupilAndIrisDetector(final Image image) {
		mImage = image;
		determineInitialParameterValues();
		for (int i = 1; i < PUPIL_SEARCH_RESOLUTIONS.length; i++) {
			int resolution = PUPIL_SEARCH_RESOLUTIONS[i];
			refinePupilPosition(resolution);
			if (resolution >= image.getWidth() && resolution >= image.getHeight()) {
				break;
			}
		}
		refineIrisPosition(IRIS_SEARCH_RESOLUTIONS[0], true);
		for (int i = 1; i < IRIS_SEARCH_RESOLUTIONS.length; i++) {
			int resolution = IRIS_SEARCH_RESOLUTIONS[i];
			refineIrisPosition(resolution, false);
			if (resolution >= image.getWidth() && resolution >= image.getHeight()) {
				break;
			}
		}
	}

	/**
	 * Find initial values of pupil center and pupil and iris radius.
	 */
	private void determineInitialParameterValues() {
		Image image = ImageUtil.resizeImage(mImage, PUPIL_SEARCH_RESOLUTIONS[0], false);
		List<PupilCenterInfo> pupilCenterInfoList = new ArrayList<>();

		for (int x = (int) image.getWidth() / 4; x < image.getWidth() * 3 / 4; x++) { // MAGIC_NUMBER
			for (int y = (int) image.getHeight() / 4; y < image.getHeight() * 3 / 4; y++) { // MAGIC_NUMBER
				PupilCenterInfo pupilCenterInfo = new PupilCenterInfo(image, x, y, PupilCenterInfo.Phase.INITIAL);
				pupilCenterInfo.collectCircleInfo(Integer.MAX_VALUE);
				pupilCenterInfoList.add(pupilCenterInfo);
			}
		}

		double maxLeapValue = Double.MIN_VALUE;
		PupilCenterInfo bestPupilCenter = null;
		for (PupilCenterInfo pupilCenterInfo : pupilCenterInfoList) {
			pupilCenterInfo.calculateStatistics(0);
//			if(pupilCenterInfo.mXCoord == 38 && pupilCenterInfo.mYCoord == 32) {
			if (pupilCenterInfo.mLeapValue > maxLeapValue) {
				maxLeapValue = pupilCenterInfo.mLeapValue;
				bestPupilCenter = pupilCenterInfo;
			}
		}
		if (bestPupilCenter != null) {
			mPupilXCenter = bestPupilCenter.mXCoord / image.getWidth();
			mPupilYCenter = bestPupilCenter.mYCoord / image.getHeight();
			mPupilRadius = bestPupilCenter.mPupilRadius / Math.min(image.getWidth(), image.getHeight());
			mIrisXCenter = mPupilXCenter;
			mIrisYCenter = mPupilYCenter;
			mIrisRadius = bestPupilCenter.mIrisRadius / Math.min(image.getWidth(), image.getHeight());
		}
	}

	/**
	 * Refine the pupil position based on the previously found position and a higher resolution.
	 *
	 * @param resolution The resolution.
	 */
	private void refinePupilPosition(final int resolution) {
		Image image = ImageUtil.resizeImage(mImage, resolution, false);
		List<PupilCenterInfo> pupilCenterInfoList = new ArrayList<>();

		int pupilXCenter = (int) Math.round(mPupilXCenter * image.getWidth());
		int pupilYCenter = (int) Math.round(mPupilYCenter * image.getHeight());
		int pupilRadius = (int) Math.round(mPupilRadius * Math.min(image.getWidth(), image.getHeight()));

		boolean isStable = false;

		for (int step = 0; step < MAX_REFINEMENT_STEPS && !isStable; step++) {
			for (int x = pupilXCenter - 1; x <= pupilXCenter + 1; x++) {
				for (int y = pupilYCenter - 1; y <= pupilYCenter + 1; y++) {
					PupilCenterInfo pupilCenterInfo = new PupilCenterInfo(image, x, y, PupilCenterInfo.Phase.PUPIL_REFINEMENT);
					pupilCenterInfo.collectCircleInfo((int) (pupilRadius + MAX_REFINEMENT_STEPS + MAX_LEAP_WIDTH * resolution));
					pupilCenterInfoList.add(pupilCenterInfo);
				}
			}

			double maxLeapValue = Double.MIN_VALUE;
			PupilCenterInfo bestPupilCenter = null;
			for (PupilCenterInfo pupilCenterInfo : pupilCenterInfoList) {
				pupilCenterInfo.calculateStatistics(pupilRadius);
				if (pupilCenterInfo.mLeapValue > maxLeapValue) {
					maxLeapValue = pupilCenterInfo.mLeapValue;
					bestPupilCenter = pupilCenterInfo;
				}
			}

			isStable = bestPupilCenter == null
					|| (bestPupilCenter.mXCoord == pupilXCenter && bestPupilCenter.mYCoord == pupilYCenter
							&& bestPupilCenter.mPupilRadius == pupilRadius);
			if (bestPupilCenter != null) {
				pupilXCenter = bestPupilCenter.mXCoord;
				pupilYCenter = bestPupilCenter.mYCoord;
				pupilRadius = bestPupilCenter.mPupilRadius;
			}
		}

		mPupilXCenter = pupilXCenter / image.getWidth();
		mPupilYCenter = pupilYCenter / image.getHeight();
		mPupilRadius = pupilRadius / Math.min(image.getWidth(), image.getHeight());
	}

	/**
	 * Refine the iris position based on the previously found position and a higher resolution.
	 *
	 * @param resolution The resolution.
	 * @param allAtOnce Flag indicating if all refinement steps should be done at once.
	 */
	private void refineIrisPosition(final int resolution, final boolean allAtOnce) {
		Image image = ImageUtil.resizeImage(mImage, resolution, false);
		List<PupilCenterInfo> pupilCenterInfoList = new ArrayList<>();
		int stepCount = allAtOnce ? 1 : MAX_REFINEMENT_STEPS;
		int stepSize = allAtOnce ? MAX_REFINEMENT_STEPS : 1;

		int irisXCenter = (int) Math.round(mIrisXCenter * image.getWidth());
		int irisYCenter = (int) Math.round(mIrisYCenter * image.getHeight());
		int irisRadius = (int) Math.round(mIrisRadius * Math.min(image.getWidth(), image.getHeight()));

		boolean isStable = false;
		Logger.log("Before: " + irisXCenter + "," + irisYCenter + "," + irisRadius);
		for (int step = 0; step < stepCount && !isStable; step++) {
			for (int x = irisXCenter - stepCount; x <= irisXCenter + stepCount; x++) {
				for (int y = irisYCenter - stepSize; y <= irisYCenter + stepSize; y++) {
					PupilCenterInfo pupilCenterInfo = new PupilCenterInfo(image, x, y, PupilCenterInfo.Phase.IRIS_REFINEMENT);
					pupilCenterInfo.collectCircleInfo((int) (irisRadius + MAX_REFINEMENT_STEPS + MAX_LEAP_WIDTH * resolution));
					pupilCenterInfoList.add(pupilCenterInfo);
				}
			}

			double maxLeapValue = Double.MIN_VALUE;
			PupilCenterInfo bestIrisCenter = null;
			for (PupilCenterInfo pupilCenterInfo : pupilCenterInfoList) {
				pupilCenterInfo.calculateStatistics(irisRadius);
				if (pupilCenterInfo.mLeapValue > maxLeapValue) {
					maxLeapValue = pupilCenterInfo.mLeapValue;
					bestIrisCenter = pupilCenterInfo;
				}
			}

			isStable = bestIrisCenter == null
					|| (bestIrisCenter.mXCoord == irisXCenter && bestIrisCenter.mYCoord == irisYCenter
							&& bestIrisCenter.mIrisRadius == irisRadius);
			if (bestIrisCenter != null) {
				irisXCenter = bestIrisCenter.mXCoord;
				irisYCenter = bestIrisCenter.mYCoord;
				irisRadius = bestIrisCenter.mIrisRadius;
			}
		}
		Logger.log("After: " + irisXCenter + "," + irisYCenter + "," + irisRadius);

		mIrisXCenter = irisXCenter / image.getWidth();
		mIrisYCenter = irisYCenter / image.getHeight();
		mIrisRadius = irisRadius / Math.min(image.getWidth(), image.getHeight());
	}

	/**
	 * The collected info about the circles around a potential pupil center.
	 */
	private static final class PupilCenterInfo {
		/**
		 * The x coordinate.
		 */
		private int mXCoord;
		/**
		 * The y coordinate.
		 */
		private int mYCoord;
		/**
		 * The image.
		 */
		private Image mImage;
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
		private double mLeapValue = Double.MIN_VALUE;
		/**
		 * The calculated pupil radius for this center.
		 */
		private int mPupilRadius = 0;
		/**
		 * The calculated iris radius for this center.
		 */
		private int mIrisRadius = 0;

		/**
		 * Create a PupilCenterInfo with certain coordinates.
		 *
		 * @param image the image.
		 * @param xCoord The x coordinate.
		 * @param yCoord The y coordinate.
		 * @param phase The phase in which the info is used.
		 */
		private PupilCenterInfo(final Image image, final int xCoord, final int yCoord, final Phase phase) {
			mXCoord = xCoord;
			mYCoord = yCoord;
			mImage = image;
			mPhase = phase;
		}

		/**
		 * Collect the information of all circles around the center.
		 *
		 * @param maxRelevantRadius The maximal circle radius considered
		 */
		private void collectCircleInfo(final int maxRelevantRadius) {
			PixelReader pixelReader = mImage.getPixelReader();
			int maxPossibleRadius = (int) (Math.min(
					Math.min(mImage.getWidth() - 1 - mXCoord, mXCoord),
					Math.min(mImage.getHeight() - 1 - mYCoord, mYCoord)));
			int maxRadius = Math.min(maxRelevantRadius, maxPossibleRadius);
			// For iris refinement, ignore points on top and bottom
			long maxRadius2 = (maxRadius + 1) * (maxRadius + 1);
			for (int x = mXCoord - maxRadius; x <= mXCoord + maxRadius; x++) {
				for (int y = mYCoord - maxRadius; y <= mYCoord + maxRadius; y++) {
					long d2 = (x - mXCoord) * (x - mXCoord) + (y - mYCoord) * (y - mYCoord);
					if (d2 <= maxRadius2) {
						int d = (int) Math.round(Math.sqrt(d2));
						double brightness = getBrightness(pixelReader.getColor(x, y));
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
		private static double getBrightness(final Color color) {
			double min = Math.min(Math.min(color.getRed(), color.getGreen()), color.getBlue());
			double sum = color.getRed() + color.getGreen() + color.getBlue();
			// Ensure that colors count more than dark grey, but white counts more then colors.
			return sum - min;
		}

		/**
		 * Add pixel info for another pixel.
		 *
		 * @param distance The distance of the pixel.
		 * @param brightness The brightness of the pixel.
		 */
		private void addInfo(final int distance, final double brightness) {
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

			int resolution = (int) Math.max(mImage.getWidth(), mImage.getHeight());
			int maxRadius = mPhase == Phase.INITIAL
					? mCircleInfos.size() - 1
					: Math.min(mCircleInfos.size() - 1, baseRadius + MAX_REFINEMENT_STEPS + (int) (MAX_LEAP_WIDTH * resolution));
			int minRadius = mPhase == Phase.INITIAL ? 0
					: Math.max(0, baseRadius - MAX_REFINEMENT_STEPS - (int) (MAX_LEAP_WIDTH * resolution));

			// Calculate the minimum of medians outside each circle.
			double innerQuantileSum = 0;
			double[] innerDarkness = new double[mCircleInfos.size()];
			mLogInfo = new String[mCircleInfos.size()];

			for (int i = minRadius; i <= maxRadius; i++) {
				double currentQuantile = mCircleInfos.get(Integer.valueOf(i)).getQuantile(MIN_BLACK_QUOTA);
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
					mLogInfo[i] = " ";
					double pupilLeapValue = 0;
					int maxLeapDistance = Math.min((int) Math.round(MAX_LEAP_WIDTH * resolution),
							Math.min(i / 2, (mCircleInfos.size() - 1 - i) / 2));
					for (int j = 1; j <= maxLeapDistance; j++) {
						double diff = mPhase == Phase.INITIAL
								? (ASSUMED_PUPIL_BRIGHTNESS + getMinMaxQuantile(MAX_BLACK_QUOTA, i + j, i + j + maxLeapDistance, false))
										/ (ASSUMED_PUPIL_BRIGHTNESS + getMinMaxQuantile(MIN_BLACK_QUOTA, i - j - Math.max(j, 2), i - j, true)) - 1
								: (ASSUMED_PUPIL_BRIGHTNESS + getMinMaxQuantile(MAX_BLACK_QUOTA, i + j, i + j + maxLeapDistance, false))
										/ (ASSUMED_PUPIL_BRIGHTNESS + getMinMaxQuantile(MIN_BLACK_QUOTA, i - j - Math.max(j, 2), i, true)) - 1;
						if (diff > MIN_LEAP_DIFF) {
							mLogInfo[i] += " | (" + j + "," + diff + ")";
							// prefer big jumps in small radius difference.
							double newLeapValue = diff / Math.pow(j, 0.8); // MAGIC_NUMBER
							if (newLeapValue > pupilLeapValue) {
								pupilLeapValue = newLeapValue;
							}
						}
					}
					if (pupilLeapValue > 0) {
						CircleInfo circleInfo = mCircleInfos.get(Integer.valueOf(i));
						// prefer big, dark circles
						circleInfo.mPupilLeapValue = Math.sqrt(i) * pupilLeapValue / innerDarkness[i];
						relevantPupilCircles.add(circleInfo);
					}
				}
			}

			if (mPhase == Phase.INITIAL || mPhase == Phase.IRIS_REFINEMENT) {
				// determine iris leap
				for (int i = minRadius; i <= maxRadius; i++) {
					double irisLeapValue = 0;
					double irisQuantileSum = 0;
					int maxLeapDistance = Math.min((int) Math.round(MAX_LEAP_WIDTH * resolution),
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
							double newLeapValue = irisQuantileSum / j;
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
							double newLeapValue = pupilCircleInfo.mPupilLeapValue * (1 + irisCircleInfo.mIrisLeapValue);
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
					double newLeapValue = pupilCircleInfo.mPupilLeapValue;
					if (newLeapValue > mLeapValue) {
						mLeapValue = newLeapValue;
						mPupilRadius = pupilCircleInfo.mRadius;
					}
				}
				break;
			case IRIS_REFINEMENT:
			default:
				for (CircleInfo irisCircleInfo : relevantIrisCircles) {
					double newLeapValue = irisCircleInfo.mIrisLeapValue;
					if (newLeapValue > mLeapValue) {
						mLeapValue = newLeapValue;
						mIrisRadius = irisCircleInfo.mRadius;
					}
				}
				break;
			}
		}

		private String[] mLogInfo;

		private void logStatistics() {
			Logger.log("P " + mXCoord + "," + mYCoord + " - " + mPupilRadius + "," + mIrisRadius);
			for (int radius = 0; radius < mCircleInfos.size(); radius++) {
				CircleInfo circleInfo = mCircleInfos.get(Integer.valueOf(radius));
				Logger.log("R " + radius + ": " + circleInfo.getQuantile(MIN_BLACK_QUOTA) + "," + circleInfo.getQuantile(MAX_BLACK_QUOTA));
				if (mLogInfo[radius] != null && mLogInfo[radius].length() > 2) {
					Logger.log(mLogInfo[radius]);
				}
			}
		}

		/**
		 * Get the minimum p-quantile for a certain set of radii.
		 *
		 * @param p The quantile parameter.
		 * @param fromRadius The start radius.
		 * @param toRadius The end radius.
		 * @param max if true, the maximum is returned, otherwise the minimum.
		 * @return The minimum quantile.
		 */
		private double getMinMaxQuantile(final double p, final int fromRadius, final int toRadius, final boolean max) {
			double result = max ? Double.MIN_VALUE : Double.MAX_VALUE;
			for (int radius = fromRadius; radius <= toRadius; radius++) {
				double newValue = mCircleInfos.get(Integer.valueOf(radius)).getQuantile(p);
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
	 * Bean for storing information about an image pixel.
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
		private List<Double> mBrightnesses = new ArrayList<>();
		/**
		 * The brightness leap at this radius used for pupil identification.
		 */
		private double mPupilLeapValue;
		/**
		 * The brightness leap at this radius used for iris identification.
		 */
		private double mIrisLeapValue;

		/**
		 * Add a brightness to the information of this circle.
		 *
		 * @param brightness the brightness.
		 */
		private void addBrightness(final double brightness) {
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
		private double getQuantile(final double p) {
			return mBrightnesses.get((int) (mBrightnesses.size() * p));
		}
	}
}
