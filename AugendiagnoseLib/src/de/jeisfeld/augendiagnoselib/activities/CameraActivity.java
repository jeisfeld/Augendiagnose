package de.jeisfeld.augendiagnoselib.activities;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.Toast;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.util.CameraUtil;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * An activity to take pictures with the camera.
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends Activity {
	/**
	 * The resource key for the input folder.
	 */
	private static final String STRING_EXTRA_PHOTOFOLDER = "de.jeisfeld.augendiagnoselib.PHOTOFOLDER";

	/**
	 * The camera used by the activity.
	 */
	private Camera camera;

	/**
	 * The folder where the photos are stored.
	 */
	private File photoFolder;

	/**
	 * The preview.
	 */
	private SurfaceView preview = null;

	/**
	 * The surface of the preview.
	 */
	private SurfaceHolder previewHolder = null;

	/**
	 * A flag indicating if the preview is active.
	 */
	private boolean inPreview = false;

	/**
	 * A flag indicating if the camera is configured.
	 */
	private boolean cameraConfigured = false;

	/**
	 * Static helper method to start the activity.
	 *
	 * @param context
	 *            The context in which the activity is started.
	 * @param photoFolderName
	 *            The folder where the photo is stored.
	 */
	public static final void startActivity(final Context context, final String photoFolderName) {
		Intent intent = new Intent(context, CameraActivity.class);
		intent.putExtra(STRING_EXTRA_PHOTOFOLDER, photoFolderName);
		context.startActivity(intent);
	}

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		String photoFolderString = getIntent().getStringExtra(STRING_EXTRA_PHOTOFOLDER);
		if (photoFolderString != null) {
			photoFolder = new File(photoFolderString);
		}
		else {
			photoFolder = new File(PreferenceUtil.getSharedPreferenceString(R.string.key_folder_input));
		}

		preview = (SurfaceView) findViewById(R.id.camera_preview);
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// Add a listener to the Capture button
		Button captureButton = (Button) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(final View v) {
						// get an image from the camera
						camera.takePicture(null, null, photoCallback);
					}
				});
	}

	@Override
	public final void onResume() {
		super.onResume();

		camera = CameraUtil.getCameraInstance();

		startPreview();
	}

	@Override
	public final void onPause() {
		if (inPreview) {
			camera.stopPreview();
		}

		camera.release();
		camera = null;
		inPreview = false;

		super.onPause();
	}

	/**
	 * Initialize the camera.
	 *
	 * @param width
	 *            The width of the preview
	 * @param height
	 *            The height of the preview
	 */
	private void initPreview(final int width, final int height) {
		if (camera != null && previewHolder.getSurface() != null) {
			try {
				camera.setPreviewDisplay(previewHolder);
			}
			catch (Throwable t) {
				Log.e(Application.TAG,
						"Exception in setPreviewDisplay()", t);
				Toast.makeText(CameraActivity.this, t.getMessage(),
						Toast.LENGTH_LONG).show();
			}

			if (!cameraConfigured) {
				Camera.Parameters parameters = camera.getParameters();
				Camera.Size pictureSize = CameraUtil.getBiggestPictureSize(parameters);
				if (pictureSize == null) {
					return;
				}
				Camera.Size previewSsize = CameraUtil.getBestPreviewSize(((float) pictureSize.width) / pictureSize.height, parameters);
				if (previewSsize == null) {
					return;
				}

				parameters.setPreviewSize(previewSsize.width, previewSsize.height);
				parameters.setPictureSize(pictureSize.width, pictureSize.height);
				parameters.setPictureFormat(ImageFormat.JPEG);
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				camera.setParameters(parameters);

				float aspectRatio = ((float) pictureSize.width) / pictureSize.height;
				LayoutParams layoutParams = preview.getLayoutParams();
				if (preview.getWidth() > aspectRatio * preview.getHeight()) {
					layoutParams.width = Math.round(preview.getHeight() * aspectRatio);
					layoutParams.height = preview.getHeight();
				}
				else {
					layoutParams.width = preview.getWidth();
					layoutParams.height = Math.round(preview.getWidth() / aspectRatio);
				}
				preview.setLayoutParams(layoutParams);

				cameraConfigured = true;
			}
		}
	}

	/**
	 * Start the camera preview.
	 */
	private void startPreview() {
		if (cameraConfigured && camera != null) {
			camera.startPreview();
			inPreview = true;
		}
	}

	/**
	 * The callback client for the preview.
	 */
	private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		@Override
		public void surfaceCreated(final SurfaceHolder holder) {
			// no-op -- wait until surfaceChanged()
		}

		@Override
		public void surfaceChanged(final SurfaceHolder holder, final int format,
				final int width, final int height) {
			if (inPreview) {
				camera.stopPreview();
				inPreview = false;
			}
			initPreview(width, height);
			startPreview();
		}

		@Override
		public void surfaceDestroyed(final SurfaceHolder holder) {
			// no-op
		}
	};

	/**
	 * The callback called when pictures are taken.
	 */
	private PictureCallback photoCallback = new PictureCallback() {
		@Override
		@SuppressFBWarnings(value = "VA_PRIMITIVE_ARRAY_PASSED_TO_OBJECT_VARARG",
				justification = "Intentionally sending byte array")
		public void onPictureTaken(final byte[] data, final Camera photoCamera) {
			new SavePhotoTask().execute(data);
			camera.startPreview();
			inPreview = true;
		}
	};

	/**
	 * The task responsible for saving the picture.
	 */
	class SavePhotoTask extends AsyncTask<byte[], String, String> {
		@Override
		protected String doInBackground(final byte[]... jpeg) {
			File photo =
					new File(photoFolder, "photo.jpg");

			if (photo.exists()) {
				photo.delete();
			}

			try {
				FileOutputStream fos = new FileOutputStream(photo.getPath());

				fos.write(jpeg[0]);
				fos.close();
			}
			catch (java.io.IOException e) {
				Log.e(Application.TAG, "Exception when saving photo", e);
			}

			return null;
		}
	}
}
