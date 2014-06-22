package de.eisfeldj.augendiagnose.util;

import java.io.File;

import android.os.Environment;

/**
 * Utility class for helping parsing file systems.
 */
public abstract class FolderUtil {

	/**
	 * Determine the camera folder. There seems to be no Android API to work for real devices, so this is a best guess.
	 * 
	 * @return
	 */
	public static String getDefaultCameraFolder() {
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		if (path.exists()) {
			File test1 = new File(path, "Camera/");
			if (test1.exists()) {
				path = test1;
			}
			else {
				File test2 = new File(path, "100ANDRO/");
				if (test2.exists()) {
					path = test2;
				}
				else {
					File test3 = new File(path, "100MEDIA/");
					path = test3;
				}
			}
		}
		else {
			File test3 = new File(path, "Camera/");
			path = test3;
		}
		return path.getAbsolutePath();
	}
}
