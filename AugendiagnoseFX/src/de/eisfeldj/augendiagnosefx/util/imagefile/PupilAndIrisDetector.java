package de.eisfeldj.augendiagnosefx.util.imagefile;

import java.util.ArrayList;
import java.util.Arrays;
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
	 * The number of points on the boundaries of circles of sizes 0 - 2000.
	 */
	private static final int[] CIRCLE_SIZES = {1, 8, 12, 16, 32, 28, 40, 40, 48, 68, 56, 72, 68, 88, 88, 84, 112, 112, 112, 116,
			112, 144, 140, 144, 144, 168, 164, 160, 184, 172, 200, 192, 188, 208, 224, 224, 228, 224, 248, 236,
			264, 248, 264, 276, 264, 288, 276, 304, 304, 312, 316, 320, 344, 316, 336, 352, 340, 376, 336, 392,
			380, 368, 400, 364, 440, 400, 424, 420, 416, 448, 428, 432, 480, 444, 472, 456, 488, 500, 472, 496,
			492, 536, 512, 512, 516, 552, 544, 540, 536, 584, 556, 576, 568, 592, 580, 592, 600, 612, 664, 592,
			640, 596, 664, 672, 620, 664, 656, 680, 692, 672, 720, 668, 704, 704, 716, 744, 728, 728, 724, 736,
			776, 772, 768, 792, 760, 780, 776, 832, 788, 816, 800, 812, 848, 832, 856, 828, 880, 848, 844, 888,
			896, 888, 876, 864, 936, 932, 920, 904, 944, 956, 912, 968, 916, 992, 960, 948, 992, 1008, 1008, 972,
			976, 1048, 1004, 1048, 1008, 1048, 1052, 1016, 1064, 1076, 1088, 1088, 1020, 1112, 1096, 1088, 1116, 1088, 1152, 1116,
			1144, 1128, 1152, 1148, 1144, 1144, 1172, 1192, 1184, 1180, 1168, 1232, 1192, 1220, 1216, 1240, 1236, 1256, 1216, 1280,
			1228, 1248, 1288, 1260, 1312, 1248, 1304, 1292, 1328, 1344, 1276, 1320, 1352, 1324, 1376, 1296, 1392, 1372, 1320, 1432,
			1356, 1424, 1384, 1376, 1380, 1448, 1440, 1412, 1432, 1448, 1444, 1456, 1448, 1488, 1460, 1456, 1472, 1492, 1520, 1496,
			1488, 1524, 1496, 1536, 1556, 1536, 1576, 1512, 1564, 1536, 1608, 1572, 1584, 1568, 1596, 1624, 1608, 1624, 1612, 1624,
			1656, 1596, 1688, 1648, 1664, 1644, 1624, 1728, 1700, 1696, 1688, 1676, 1736, 1680, 1736, 1732, 1720, 1776, 1716, 1728,
			1816, 1760, 1780, 1728, 1816, 1788, 1768, 1808, 1824, 1852, 1792, 1832, 1844, 1864, 1848, 1820, 1840, 1872, 1896, 1860,
			1848, 1960, 1876, 1936, 1840, 1992, 1924, 1856, 1960, 1948, 1976, 1944, 1908, 2024, 1944, 2000, 1956, 1944, 2096, 1956,
			2040, 1992, 2040, 2028, 2016, 2056, 2076, 2056, 2056, 1996, 2144, 2080, 2072, 2076, 2072, 2160, 2076, 2128, 2120, 2144,
			2148, 2104, 2192, 2132, 2192, 2168, 2120, 2180, 2184, 2240, 2188, 2168, 2248, 2188, 2256, 2200, 2256, 2244, 2200, 2304,
			2252, 2304, 2272, 2272, 2276, 2280, 2320, 2308, 2296, 2352, 2292, 2352, 2264, 2424, 2348, 2344, 2360, 2364, 2384, 2368,
			2376, 2420, 2392, 2400, 2364, 2416, 2488, 2432, 2452, 2392, 2480, 2484, 2464, 2440, 2476, 2496, 2440, 2496, 2516, 2496,
			2584, 2444, 2568, 2528, 2552, 2548, 2480, 2664, 2492, 2616, 2552, 2556, 2664, 2536, 2600, 2612, 2592, 2688, 2556, 2656,
			2656, 2600, 2684, 2616, 2760, 2636, 2640, 2680, 2660, 2744, 2696, 2688, 2740, 2704, 2768, 2708, 2768, 2720, 2744, 2748,
			2712, 2880, 2756, 2800, 2728, 2832, 2764, 2824, 2840, 2844, 2832, 2784, 2812, 2904, 2840, 2856, 2820, 2872, 2920, 2876,
			2936, 2808, 2976, 2932, 2848, 2944, 2908, 2992, 2896, 2876, 3024, 2944, 3040, 2924, 2960, 3024, 2932, 3016, 2968, 3024,
			3052, 2976, 3088, 3004, 3088, 3048, 3008, 3116, 3048, 3048, 3084, 3096, 3112, 3044, 3096, 3080, 3184, 3116, 3088, 3144,
			3172, 3176, 3120, 3128, 3204, 3176, 3200, 3100, 3272, 3200, 3180, 3224, 3128, 3336, 3196, 3216, 3256, 3244, 3288, 3200,
			3272, 3340, 3248, 3328, 3244, 3288, 3304, 3260, 3368, 3296, 3384, 3356, 3288, 3360, 3388, 3392, 3360, 3328, 3388, 3376,
			3416, 3372, 3424, 3408, 3416, 3412, 3440, 3488, 3420, 3424, 3400, 3476, 3552, 3432, 3496, 3476, 3472, 3504, 3452, 3592,
			3480, 3576, 3492, 3536, 3608, 3492, 3608, 3480, 3572, 3608, 3568, 3624, 3588, 3608, 3584, 3508, 3736, 3648, 3624, 3620,
			3568, 3776, 3604, 3672, 3640, 3736, 3668, 3648, 3664, 3772, 3688, 3712, 3676, 3768, 3736, 3744, 3764, 3672, 3800, 3764,
			3752, 3744, 3856, 3756, 3792, 3816, 3804, 3848, 3776, 3788, 3896, 3832, 3856, 3820, 3896, 3848, 3828, 3912, 3848, 3960,
			3900, 3840, 3896, 3924, 3944, 3928, 3888, 4028, 3944, 3952, 3916, 3976, 3984, 3956, 4000, 3976, 4032, 3964, 3976, 3984,
			4084, 4024, 4024, 4016, 4116, 4040, 4088, 3996, 4096, 4080, 4052, 4136, 4056, 4184, 4084, 4048, 4128, 4140, 4176, 4104,
			4136, 4172, 4128, 4240, 4108, 4232, 4192, 4124, 4200, 4200, 4272, 4172, 4192, 4232, 4212, 4328, 4256, 4216, 4332, 4168,
			4296, 4244, 4360, 4312, 4248, 4292, 4296, 4352, 4324, 4304, 4312, 4372, 4336, 4384, 4320, 4412, 4344, 4408, 4308, 4464,
			4416, 4376, 4436, 4392, 4440, 4380, 4464, 4416, 4484, 4488, 4384, 4456, 4524, 4512, 4512, 4380, 4584, 4440, 4568, 4492,
			4432, 4624, 4500, 4576, 4600, 4520, 4588, 4536, 4544, 4628, 4544, 4640, 4548, 4640, 4624, 4544, 4668, 4632, 4664, 4628,
			4616, 4688, 4688, 4692, 4608, 4672, 4732, 4672, 4664, 4732, 4736, 4696, 4768, 4652, 4760, 4784, 4732, 4800, 4696, 4832,
			4732, 4728, 4824, 4804, 4832, 4768, 4732, 4936, 4800, 4856, 4788, 4856, 4912, 4796, 4928, 4848, 4880, 4884, 4800, 4928,
			4908, 4968, 4872, 4840, 5036, 4896, 4968, 4900, 4984, 5008, 4868, 4984, 4984, 5008, 4988, 4928, 5064, 5004, 5064, 4976,
			5000, 5092, 4968, 5032, 5052, 5168, 5048, 5012, 5064, 5104, 5136, 5044, 5064, 5184, 5092, 5144, 5024, 5136, 5212, 5112,
			5200, 5028, 5280, 5192, 5176, 5204, 5120, 5264, 5124, 5232, 5264, 5172, 5256, 5152, 5216, 5348, 5224, 5328, 5172, 5320,
			5272, 5304, 5260, 5304, 5328, 5308, 5320, 5320, 5308, 5368, 5296, 5376, 5316, 5360, 5408, 5308, 5480, 5328, 5392, 5348,
			5416, 5480, 5356, 5440, 5384, 5440, 5516, 5328, 5592, 5372, 5520, 5472, 5412, 5608, 5456, 5480, 5460, 5504, 5592, 5468,
			5528, 5560, 5544, 5524, 5536, 5592, 5596, 5560, 5576, 5476, 5680, 5600, 5576, 5540, 5712, 5664, 5540, 5696, 5656, 5632,
			5676, 5568, 5768, 5676, 5728, 5640, 5596, 5776, 5664, 5768, 5684, 5752, 5736, 5684, 5744, 5768, 5832, 5700, 5720, 5856,
			5732, 5816, 5760, 5800, 5844, 5752, 5880, 5748, 5928, 5808, 5796, 5856, 5864, 5832, 5932, 5768, 6024, 5820, 5920, 5832,
			5928, 5996, 5848, 5928, 5876, 6056, 5904, 5916, 5944, 5976, 6016, 5932, 5968, 6040, 5980, 6088, 5888, 6024, 6004, 6008,
			6104, 5932, 6168, 6008, 6000, 6132, 6024, 6192, 6012, 6056, 6168, 6036, 6136, 6088, 6128, 6148, 6040, 6208, 6116, 6216,
			6176, 6056, 6164, 6240, 6224, 6196, 6128, 6264, 6164, 6216, 6192, 6272, 6236, 6232, 6168, 6284, 6304, 6264, 6304, 6164,
			6304, 6344, 6268, 6296, 6304, 6316, 6304, 6296, 6432, 6380, 6344, 6280, 6292, 6464, 6344, 6416, 6332, 6424, 6432, 6292,
			6488, 6392, 6408, 6420, 6408, 6496, 6436, 6528, 6392, 6420, 6576, 6384, 6536, 6436, 6504, 6584, 6420, 6536, 6592, 6472,
			6588, 6432, 6624, 6524, 6512, 6640, 6524, 6632, 6608, 6528, 6580, 6680, 6624, 6540, 6592, 6664, 6648, 6644, 6592, 6760,
			6604, 6672, 6616, 6728, 6732, 6624, 6760, 6644, 6776, 6728, 6644, 6712, 6728, 6800, 6756, 6616, 6904, 6764, 6752, 6768,
			6760, 6820, 6800, 6840, 6796, 6808, 6792, 6804, 6840, 6944, 6808, 6876, 6840, 6880, 6860, 6864, 6912, 6944, 6884, 6832,
			6936, 6900, 7024, 6840, 6844, 6984, 7024, 6952, 6988, 6920, 7008, 6948, 7008, 6960, 7104, 6964, 6952, 7016, 6956, 7160,
			7016, 7024, 7004, 7104, 7088, 7012, 7080, 7176, 6980, 7168, 7016, 7192, 7116, 7064, 7160, 7036, 7232, 7168, 7096, 7172,
			7128, 7248, 7108, 7104, 7312, 7108, 7256, 7176, 7216, 7332, 7160, 7192, 7252, 7216, 7256, 7224, 7332, 7272, 7296, 7236,
			7256, 7328, 7384, 7300, 7248, 7376, 7292, 7384, 7288, 7316, 7448, 7240, 7368, 7380, 7368, 7480, 7260, 7408, 7464, 7392,
			7468, 7336, 7496, 7404, 7416, 7384, 7484, 7592, 7424, 7424, 7492, 7512, 7464, 7460, 7496, 7552, 7520, 7500, 7408, 7696,
			7468, 7600, 7472, 7560, 7588, 7600, 7640, 7524, 7608, 7616, 7500, 7704, 7616, 7616, 7604, 7480, 7816, 7572, 7696, 7640,
			7688, 7740, 7576, 7704, 7692, 7776, 7656, 7636, 7816, 7672, 7776, 7700, 7656, 7864, 7660, 7800, 7728, 7856, 7812, 7680,
			7840, 7812, 7792, 7840, 7684, 7952, 7808, 7800, 7892, 7784, 7928, 7836, 7848, 7848, 7904, 7932, 7840, 7840, 7940, 8000,
			7848, 7936, 7940, 7936, 7960, 7844, 7960, 8008, 7924, 8112, 7864, 8064, 7916, 7992, 8088, 7932, 8136, 7872, 8064, 8132,
			7920, 8168, 7972, 8016, 8176, 8004, 8128, 8024, 8144, 8132, 7992, 8144, 8196, 8176, 8080, 8048, 8188, 8120, 8256, 8060,
			8224, 8176, 8192, 8180, 8096, 8352, 8164, 8136, 8240, 8204, 8336, 8184, 8248, 8268, 8144, 8312, 8236, 8328, 8320, 8288,
			8292, 8160, 8464, 8308, 8352, 8232, 8316, 8368, 8344, 8384, 8316, 8376, 8376, 8308, 8488, 8392, 8352, 8420, 8312, 8496,
			8364, 8456, 8424, 8380, 8520, 8400, 8504, 8484, 8496, 8480, 8396, 8552, 8504, 8528, 8444, 8400, 8624, 8548, 8512, 8496,
			8672, 8484, 8488, 8600, 8516, 8696, 8520, 8548, 8688, 8584, 8656, 8532, 8560, 8840, 8524, 8688, 8496, 8736, 8708, 8592,
			8696, 8716, 8648, 8720, 8628, 8792, 8664, 8712, 8700, 8656, 8872, 8700, 8744, 8704, 8784, 8732, 8728, 8816, 8836, 8816,
			8736, 8808, 8868, 8816, 8816, 8780, 8888, 8784, 8804, 8928, 8784, 8984, 8788, 8792, 9000, 8916, 8952, 8864, 8824, 8996,
			8840, 9040, 8844, 9024, 8920, 8900, 8944, 8912, 9104, 8916, 9048, 8920, 9004, 9096, 8960, 8984, 9060, 8856, 9176, 9004,
			9112, 9072, 8992, 9060, 9008, 9200, 9148, 9080, 9032, 9116, 9088, 9208, 9008, 9212, 9088, 9144, 9140, 9168, 9208, 9152,
			9116, 9144, 9296, 9132, 9288, 9096, 9236, 9264, 9128, 9232, 9236, 9328, 9280, 9116, 9432, 9104, 9360, 9244, 9224, 9424,
			9236, 9320, 9288, 9316, 9400, 9272, 9272, 9412, 9376, 9384, 9244, 9440, 9416, 9296, 9396, 9344, 9496, 9404, 9400, 9368,
			9464, 9516, 9384, 9384, 9484, 9480, 9448, 9372, 9528, 9448, 9544, 9500, 9416, 9664, 9436, 9584, 9432, 9584, 9508, 9520,
			9600, 9556, 9592, 9544, 9476, 9680, 9576, 9648, 9516, 9648, 9648, 9580, 9616, 9664, 9624, 9620, 9592, 9664, 9700, 9720,
			9648, 9568, 9852, 9592, 9800, 9660, 9704, 9808, 9636, 9696, 9792, 9792, 9812, 9648, 9744, 9764, 9840, 9760, 9696, 9884,
			9768, 9848, 9772, 9928, 9800, 9772, 9872, 9784, 9904, 9828, 9888, 9840, 9868, 9928, 9824, 9896, 10044, 9832, 9968, 9756,
			10008, 9976, 9804, 10072, 9896, 10064, 9900, 9968, 10000, 9980, 10040, 9888, 9992, 10140, 9952, 10048, 9956, 10056, 10080, 10000,
			10100, 10064, 10080, 10036, 10056, 10088, 10124, 10112, 10040, 10112, 10164, 10104, 10176, 10028, 10280, 10088, 10072, 10196, 10152,
			10304, 10092, 10168, 10152, 10156, 10328, 10200, 10160, 10308, 10160, 10240, 10172, 10352, 10232, 10288, 10220, 10264, 10384, 10236,
			10312, 10216, 10296, 10364, 10304, 10312, 10420, 10304, 10304, 10236, 10464, 10376, 10320, 10380, 10352, 10440, 10300, 10440, 10464,
			10456, 10428, 10328, 10416, 10492, 10512, 10360, 10348, 10624, 10408, 10520, 10404, 10528, 10544, 10420, 10544, 10480, 10624, 10524,
			10376, 10608, 10436, 10664, 10504, 10432, 10836, 10440, 10704, 10492, 10568, 10728, 10484, 10648, 10648, 10576, 10740, 10488, 10744,
			10684, 10600, 10632, 10608, 10804, 10648, 10648, 10668, 10672, 10816, 10660, 10736, 10720, 10760, 10692, 10760, 10768, 10844, 10712,
			10696, 10760, 10844, 10776, 10808, 10708, 10904, 10688, 10844, 10904, 10848, 10928, 10708, 10800, 10784, 10972, 10976, 10784, 10920,
			10884, 10840, 11040, 10748, 11064, 10888, 10840, 10980, 10904, 11088, 10868, 10920, 10976, 10876, 11120, 10896, 11008, 11028, 10872,
			11120, 10932, 11112, 11064, 11040, 10980, 11008, 11104, 11028, 11040, 11144, 11052, 11072, 11040, 11224, 11116, 11088, 11096, 11028,
			11216, 11120, 11160, 11092, 11120, 11248, 11068, 11224, 11200, 11200, 11212, 11040, 11352, 11220, 11240, 11112, 11156, 11384, 11200,
			11256, 11212, 11192, 11408, 11204, 11272, 11328, 11288, 11332, 11176, 11384, 11332, 11248, 11416, 11220, 11464, 11360, 11328, 11300,
			11416, 11360, 11356, 11400, 11368, 11392, 11428, 11424, 11480, 11340, 11432, 11392, 11380, 11616, 11400, 11480, 11348, 11600, 11512,
			11428, 11544, 11424, 11584, 11412, 11488, 11624, 11468, 11616, 11496, 11584, 11524, 11512, 11640, 11508, 11600, 11640, 11468, 11616,
			11696, 11632, 11612, 11448, 11808, 11620, 11712, 11608, 11616, 11700, 11592, 11744, 11548, 11792, 11720, 11588, 11704, 11808, 11760,
			11724, 11640, 11784, 11716, 11776, 11680, 11824, 11796, 11704, 11784, 11724, 11968, 11736, 11792, 11780, 11808, 11856, 11860, 11808,
			11944, 11764, 11856, 11840, 11920, 11964, 11728, 11856, 11860, 12024, 11992, 11792, 11916, 11968, 11936, 11844, 11944, 12048, 11916,
			11960, 11928, 11976, 11988, 12048, 11968, 11956, 12080, 12032, 11992, 11956, 12104, 12104, 11948, 12008, 12080, 12068, 12128, 11960,
			12192, 12020, 12192, 12104, 12012, 12296, 11992, 12176, 12028, 12112, 12296, 12060, 12184, 12232, 12072, 12324, 11952, 12328, 12196,
			12184, 12168, 12140, 12384, 12160, 12200, 12268, 12264, 12264, 12292, 12184, 12352, 12224, 12308, 12168, 12408, 12348, 12240, 12336,
			12260, 12376, 12368, 12288, 12316, 12472, 12328, 12292, 12408, 12440, 12392, 12300, 12296, 12544, 12356, 12520, 12384, 12432, 12412,
			12408, 12472, 12380, 12584, 12456, 12404, 12472, 12512, 12528, 12476, 12344, 12656, 12452, 12528, 12592, 12560, 12564, 12480, 12608,
			12492, 12640};

	/**
	 * The image to be analyzed.
	 */
	private Image mImage;

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
		refineIrisPosition();
	}

	/**
	 * Update the stored metadata with the iris and pupil position from the detector.
	 *
	 * @param metadata The metadata to be updated.
	 */
	public final void updateMetadata(final JpegMetadata metadata) {
		metadata.setXCenter(mIrisXCenter);
		metadata.setYCenter(mIrisYCenter);
		metadata.setOverlayScaleFactor(mIrisRadius * 8 / 3); // MAGIC_NUMBER

		metadata.setPupilXOffset((mPupilXCenter - mIrisXCenter) / (2 * mIrisRadius));
		metadata.setPupilYOffset((mPupilYCenter - mIrisYCenter) / (2 * mIrisRadius));
		metadata.setPupilSize(mPupilRadius / mIrisRadius);
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
			mPupilXCenter = bestPupilCenter.mXCenter / (float) image.getWidth();
			mPupilYCenter = bestPupilCenter.mYCenter / (float) image.getHeight();
			mPupilRadius = bestPupilCenter.mPupilRadius / (float) Math.max(image.getWidth(), image.getHeight());
			mIrisXCenter = mPupilXCenter;
			mIrisYCenter = mPupilYCenter;
			mIrisRadius = bestPupilCenter.mIrisRadius / (float) Math.max(image.getWidth(), image.getHeight());
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
		int pupilRadius = (int) Math.round(mPupilRadius * Math.max(image.getWidth(), image.getHeight()));

		boolean isStable = false;

		for (int step = 0; step < MAX_REFINEMENT_STEPS && !isStable; step++) {
			for (int x = pupilXCenter - 1; x <= pupilXCenter + 1; x++) {
				for (int y = pupilYCenter - 1; y <= pupilYCenter + 1; y++) {
					PupilCenterInfo pupilCenterInfo = new PupilCenterInfo(image, x, y, PupilCenterInfo.Phase.PUPIL_REFINEMENT);
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

		mPupilXCenter = pupilXCenter / (float) image.getWidth();
		mPupilYCenter = pupilYCenter / (float) image.getHeight();
		mPupilRadius = pupilRadius / (float) Math.max(image.getWidth(), image.getHeight());
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

		mIrisXCenter = irisBoundary.mXCenter / (float) mImage.getWidth();
		mIrisYCenter = irisBoundary.mYCenter / (float) mImage.getHeight();
		mIrisRadius = irisBoundary.mRadius / (float) Math.max(mImage.getWidth(), mImage.getHeight());
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
		private float mLeapValue = Float.MIN_VALUE;

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
			int maxPossibleRadius = (int) Math.min(
					Math.min(mImage.getWidth() - 1 - mXCenter, mXCenter),
					Math.min(mImage.getHeight() - 1 - mYCenter, mYCenter));
			int maxRadius = Math.min(maxRelevantRadius, maxPossibleRadius);
			// For iris refinement, ignore points on top and bottom
			long maxRadius2 = (maxRadius + 1) * (maxRadius + 1);
			for (int x = mXCenter - maxRadius; x <= mXCenter + maxRadius; x++) {
				for (int y = mYCenter - maxRadius; y <= mYCenter + maxRadius; y++) {
					long d2 = (x - mXCenter) * (x - mXCenter) + (y - mYCenter) * (y - mYCenter);
					if (d2 <= maxRadius2) {
						int d = (int) Math.round(Math.sqrt(d2));
						float brightness = getBrightness(pixelReader.getColor(x, y));
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
		private static float getBrightness(final Color color) {
			float min = (float) Math.min(Math.min(color.getRed(), color.getGreen()), color.getBlue());
			float sum = (float) (color.getRed() + color.getGreen() + color.getBlue());
			// Ensure that colors count more than dark grey, but white counts more then colors.
			return sum - min;
		}

		/**
		 * Add pixel info for another pixel.
		 *
		 * @param distance The distance of the pixel.
		 * @param brightness The brightness of the pixel.
		 */
		private void addInfo(final int distance, final float brightness) {
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
		 * @param p The quantile parameter.
		 * @param fromRadius The start radius.
		 * @param toRadius The end radius.
		 * @param max if true, the maximum is returned, otherwise the minimum.
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
			mBrightnesses = new float[CIRCLE_SIZES[radius]];
			mCurrentIndex = 0;
		}

		/**
		 * The radius.
		 */
		private int mRadius;
		/**
		 * The brightnesses.
		 */
		private float[] mBrightnesses;
		/**
		 * The current index on the brightness array.
		 */
		private int mCurrentIndex;

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
		private void addBrightness(final float brightness) {
			mBrightnesses[mCurrentIndex++] = brightness;
		}

		/**
		 * Do statistical calculations after all brightnesses are available. Here, only sorting is required.
		 */
		private void calculateStatistics() {
			Arrays.sort(mBrightnesses);
		}

		/**
		 * Get the p-quantile of the brightnesses. Prerequisite: calculateStatistics must have been run before.
		 *
		 * @param p the quantile parameter.
		 * @return the p-quantile of the brightnesses (not considering equality).
		 */
		private float getQuantile(final float p) {
			return mBrightnesses[(int) (mBrightnesses.length * p)];
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

			for (int yCoord = mYCenter; yCoord <= mYCenter + mRadius * IRIS_BOUNDARY_SEARCH_RANGE && yCoord < mImage.getHeight(); yCoord++) {
				determineBoundaryPoints(pixelReader, yCoord);
			}

			for (int yCoord = mYCenter - 1; yCoord >= mYCenter - mRadius * IRIS_BOUNDARY_SEARCH_RANGE && yCoord >= 0; yCoord--) {
				determineBoundaryPoints(pixelReader, yCoord);
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
			int xDistanceRange = Math.round(IRIS_BOUNDARY_UNCERTAINTY_FACTOR * mRadius);
			int xDistanceMinRange = Math.round(IRIS_BOUNDARY_MIN_RANGE * mRadius);
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
			float brightnessSum = 0;
			int leftBoundary = Math.max(mXCenter - expectedXDistance - xDistanceRange, 0);
			int rightBoundary = Math.min(mXCenter - expectedXDistance + xDistanceRange, (int) mImage.getWidth() - 1);
			for (int x = leftBoundary; x <= rightBoundary; x++) {
				brightnessSum += getBrightness(pixelReader.getColor(x, yCoord));
			}
			float avgBrightness = brightnessSum / (2 * xDistanceRange + 1);

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
			float brightnessSum2 = 0;
			int leftBoundary2 = Math.max(mXCenter + expectedXDistance - xDistanceRange, 0);
			int rightBoundary2 = Math.min(mXCenter + expectedXDistance + xDistanceRange, (int) mImage.getWidth() - 1);
			for (int x = leftBoundary2; x <= rightBoundary2; x++) {
				brightnessSum2 += getBrightness(pixelReader.getColor(x, yCoord));
			}
			float avgBrightness2 = brightnessSum2 / (2 * xDistanceRange + 1);

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
		private static float getBrightness(final Color color) {
			// Blue seems to be particulary helpful in the separation.
			return (float) (Math.min(Math.min(color.getRed(), color.getGreen()), color.getBlue()) + color.getBlue());
		}

	}

}
