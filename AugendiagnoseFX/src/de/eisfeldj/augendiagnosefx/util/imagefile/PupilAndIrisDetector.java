package de.eisfeldj.augendiagnosefx.util.imagefile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private static final int[] IRIS_SEARCH_RESOLUTIONS = {100};
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
	 * The vertical range where iris boundary points should be searched for.
	 */
	private static final double IRIS_BOUNDARY_SEARCH_RANGE = 0.7;
	/**
	 * The uncertainty of the positions of the iris boundary points.
	 */
	private static final double IRIS_BOUNDARY_UNCERTAINTY_FACTOR = 0.2;
	/**
	 * The minimum range considered when determining the iris boundary.
	 */
	private static final double IRIS_BOUNDARY_MIN_RANGE = 0.02;
	/**
	 * Factor by which the range is changed with each retry after a search failure.
	 */
	private static final double IRIS_BOUNDARY_RETRY_FACTOR = 0.7;
	/**
	 * The quota of points that are allowed to be too bright in the iris or too dark outside the iris.
	 */
	private static final double IRIS_BOUNDARY_WRONG_BRIGHTNESS_QUOTA = 0.2;
	/**
	 * The quota of points around the center considered for determining the vertical center.
	 */
	private static final double IRIS_BOUNDARY_POINTS_CONSIDERED_FOR_YCENTER = 0.3;
	/**
	 * The minimum number of boundary points needed to refine the iris position.
	 */
	private static final double IRIS_BOUNDARY_MIN_BOUNDARY_POINTS = 10;

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
//		refineIrisPosition(IRIS_SEARCH_RESOLUTIONS[0], true);
//		for (int i = 1; i < IRIS_SEARCH_RESOLUTIONS.length; i++) {
//			int resolution = IRIS_SEARCH_RESOLUTIONS[i];
//			refineIrisPosition(resolution, false);
//			if (resolution >= image.getWidth() && resolution >= image.getHeight()) {
//				break;
//			}
//		}
		refineIrisPosition();
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
			if (pupilCenterInfo.mLeapValue > maxLeapValue) {
				maxLeapValue = pupilCenterInfo.mLeapValue;
				bestPupilCenter = pupilCenterInfo;
			}
		}
		if (bestPupilCenter != null) {
			mPupilXCenter = bestPupilCenter.mXCenter / image.getWidth();
			mPupilYCenter = bestPupilCenter.mYCenter / image.getHeight();
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
					|| (bestPupilCenter.mXCenter == pupilXCenter && bestPupilCenter.mYCenter == pupilYCenter
							&& bestPupilCenter.mPupilRadius == pupilRadius);
			if (bestPupilCenter != null) {
				pupilXCenter = bestPupilCenter.mXCenter;
				pupilYCenter = bestPupilCenter.mYCenter;
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
					|| (bestIrisCenter.mXCenter == irisXCenter && bestIrisCenter.mYCenter == irisYCenter
							&& bestIrisCenter.mIrisRadius == irisRadius);
			if (bestIrisCenter != null) {
				irisXCenter = bestIrisCenter.mXCenter;
				irisYCenter = bestIrisCenter.mYCenter;
				irisRadius = bestIrisCenter.mIrisRadius;
			}
		}

		mIrisXCenter = irisXCenter / image.getWidth();
		mIrisYCenter = irisYCenter / image.getHeight();
		mIrisRadius = irisRadius / Math.min(image.getWidth(), image.getHeight());
	}

	/**
	 * Refine the iris position based on the previously found position.
	 */
	private void refineIrisPosition() {
		IrisBoundary irisBoundary = new IrisBoundary(mImage,
				(int) (mImage.getWidth() * mIrisXCenter),
				(int) (mImage.getHeight() * mIrisYCenter),
				(int) (Math.min(mImage.getWidth(), mImage.getHeight()) * mIrisRadius));

		irisBoundary.analyzeBoundary();

		mIrisXCenter = irisBoundary.mXCenter / mImage.getWidth();
		mIrisYCenter = irisBoundary.mYCenter / mImage.getHeight();
		mIrisRadius = irisBoundary.mRadius / Math.min(mImage.getWidth(), mImage.getHeight());

		mIrisBoundary = irisBoundary;
	}

	// TODO: remove later - this is only used to help displaying the points in the debug phase.
	private IrisBoundary mIrisBoundary;

	public List<Point> getIrisBoundaryPoints() {
		List<Point> result = new ArrayList<>();
		if(mIrisBoundary != null && mIrisBoundary.mLeftPoints != null) {
			for (Integer y : mIrisBoundary.mLeftPoints.keySet()) {
				result.add(new Point(mIrisBoundary.mLeftPoints.get(y), y));
				result.add(new Point(mIrisBoundary.mRightPoints.get(y), y));
			}
		}
		return result;
	}

	public static class Point {
		public Point(final int x, final int y) {
			this.x = x;
			this.y = y;
		}

		public int x;
		public int y;
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
		 * Create a PupilCenterInfo with certain coordinates.
		 *
		 * @param image the image.
		 * @param xCoord The x coordinate.
		 * @param yCoord The y coordinate.
		 * @param phase The phase in which the info is used.
		 */
		private PupilCenterInfo(final Image image, final int xCoord, final int yCoord, final Phase phase) {
			mXCenter = xCoord;
			mYCenter = yCoord;
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
					Math.min(mImage.getWidth() - 1 - mXCenter, mXCenter),
					Math.min(mImage.getHeight() - 1 - mYCenter, mYCenter)));
			int maxRadius = Math.min(maxRelevantRadius, maxPossibleRadius);
			// For iris refinement, ignore points on top and bottom
			long maxRadius2 = (maxRadius + 1) * (maxRadius + 1);
			for (int x = mXCenter - maxRadius; x <= mXCenter + maxRadius; x++) {
				for (int y = mYCenter - maxRadius; y <= mYCenter + maxRadius; y++) {
					long d2 = (x - mXCenter) * (x - mXCenter) + (y - mYCenter) * (y - mYCenter);
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

	/**
	 * Class for collecting information about the iris boundary.
	 */
	private static final class IrisBoundary {
		/**
		 * The image.
		 */
		private Image mImage;

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
		 * @param image The image.
		 * @param xCenter the initial x coordinate of the center.
		 * @param yCenter the initial y coordinate of the center.
		 * @param radius the initial iris radius.
		 */
		private IrisBoundary(final Image image, final int xCenter, final int yCenter, final int radius) {
			mImage = image;
			mXCenter = xCenter;
			mYCenter = yCenter;
			mRadius = radius;
		}

		/**
		 * Search points on the iris boundary.
		 */
		private void determineBoundaryPoints() {
			PixelReader pixelReader = mImage.getPixelReader();

			boolean found = true;
			for (int yCoord = mYCenter; yCoord <= mYCenter + mRadius * IRIS_BOUNDARY_SEARCH_RANGE && yCoord < mImage.getHeight(); yCoord++) {
				found = determineBoundaryPoints(pixelReader, yCoord);
			}

			found = true;
			for (int yCoord = mYCenter - 1; yCoord >= mYCenter - mRadius * IRIS_BOUNDARY_SEARCH_RANGE && yCoord >= 0; yCoord--) {
				found = determineBoundaryPoints(pixelReader, yCoord);
			}
		}

		/**
		 * Determine the boundary points for a certain y coordinate.
		 *
		 * @param pixelReader The pixel reader.
		 * @param yCoord The y coordinate for which to find the boundary points.
		 * @return true if a boundary point has been found.
		 */
		private boolean determineBoundaryPoints(final PixelReader pixelReader, final int yCoord) {
			int xDistanceRange = (int) Math.round(IRIS_BOUNDARY_UNCERTAINTY_FACTOR * mRadius);
			int xDistanceMinRange = (int) Math.round(IRIS_BOUNDARY_MIN_RANGE * mRadius);
			boolean found = false;

			while (!found && xDistanceRange >= xDistanceMinRange) {
				found = determineBoundaryPoints(pixelReader, yCoord, xDistanceRange);
				xDistanceRange *= IRIS_BOUNDARY_RETRY_FACTOR;
			}
			return found;
		}

		/**
		 * Determine the boundary points for a certain y coordinate.
		 *
		 * @param pixelReader The pixel reader.
		 * @param yCoord The y coordinate for which to find the boundary points.
		 * @param xDistanceRange the horizontal range which is considered.
		 * @return true if a boundary point has been found.
		 */
		private boolean determineBoundaryPoints(final PixelReader pixelReader, final int yCoord, final int xDistanceRange) {
			int yDiff = yCoord - mYCenter;
			if (Math.abs(yDiff) > IRIS_BOUNDARY_SEARCH_RANGE * mRadius) {
				return false;
			}

			int expectedXDistance = (int) Math.round(Math.sqrt(mRadius * mRadius - yDiff * yDiff));

			// Left side - calculate average brightness
			double brightnessSum = 0;
			int leftBoundary = Math.max(mXCenter - expectedXDistance - xDistanceRange, 0);
			int rightBoundary = Math.min(mXCenter - expectedXDistance + xDistanceRange, (int) mImage.getWidth() - 1);
			for (int x = leftBoundary; x <= rightBoundary; x++) {
				brightnessSum += getBrightness(pixelReader.getColor(x, yCoord));
			}
			double avgBrightness = brightnessSum / (2 * xDistanceRange + 1);

			// Left side - find transition from light to dark
			int leftCounter = 0;
			int rightCounter = 0;
			while (leftBoundary < rightBoundary) {
				if (rightCounter > leftCounter) {
					if (getBrightness(pixelReader.getColor(leftBoundary++, yCoord)) < avgBrightness) {
						leftCounter++;
					}
				}
				else {
					if (getBrightness(pixelReader.getColor(rightBoundary--, yCoord)) > avgBrightness) {
						rightCounter++;
					}
				}
			}
			if (leftCounter > IRIS_BOUNDARY_WRONG_BRIGHTNESS_QUOTA * xDistanceRange) {
				return false;
			}

			// Right side - calculate average brightness
			double brightnessSum2 = 0;
			int leftBoundary2 = Math.max(mXCenter + expectedXDistance - xDistanceRange, 0);
			int rightBoundary2 = Math.min(mXCenter + expectedXDistance + xDistanceRange, (int) mImage.getWidth() - 1);
			for (int x = leftBoundary2; x <= rightBoundary2; x++) {
				brightnessSum2 += getBrightness(pixelReader.getColor(x, yCoord));
			}
			double avgBrightness2 = brightnessSum2 / (2 * xDistanceRange + 1);

			// Right side - find transition from light to dark
			int leftCounter2 = 0;
			int rightCounter2 = 0;
			while (leftBoundary2 < rightBoundary2) {
				if (leftCounter2 > rightCounter2) {
					if (getBrightness(pixelReader.getColor(rightBoundary2--, yCoord)) < avgBrightness2) {
						rightCounter2++;
					}
				}
				else {
					if (getBrightness(pixelReader.getColor(leftBoundary2++, yCoord)) > avgBrightness2) {
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

			xSumValues.sort(new Comparator<Integer>() {
				@Override
				public int compare(final Integer integer1, final Integer integer2) {
					return Integer.compare(integer1, integer2);
				}
			});

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
			distances.sort(new Comparator<Integer>() {
				@Override
				public int compare(final Integer integer1, final Integer integer2) {
					return Integer.compare(integer2, integer1);
				}
			});

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
			double sum = 0;
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

			mRadius = (int) Math.round(sum / (2 * mLeftPoints.size()));
		}

		/**
		 * Get a brightness value from a color.
		 *
		 * @param color The color
		 * @return The brightness value.
		 */
		private static double getBrightness(final Color color) {
			// Blue seems to be particulary helpful in the separation.
			return Math.min(Math.min(color.getRed(), color.getGreen()), color.getBlue()) + color.getBlue();
		}

	}

}
