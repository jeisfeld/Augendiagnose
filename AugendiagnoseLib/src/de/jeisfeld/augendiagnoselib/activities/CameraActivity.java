package de.jeisfeld.augendiagnoselib.activities;

import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.CurrentAction.LEFT_EYE;
import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.CurrentAction.NAME;
import static de.jeisfeld.augendiagnoselib.activities.CameraActivity.CurrentAction.RIGHT_EYE;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.components.PinchImageView;
import de.jeisfeld.augendiagnoselib.util.CameraUtil;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.FileUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.ImageUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * An activity to take pictures with the camera.
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends Activity {
	/**
	 * The camera used by the activity.
	 */
	private Camera camera;

	/**
	 * The folder where the photos are finally placed.
	 */
	private File outputFolder;

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
	 * The current action in the activity.
	 */
	private CurrentAction currentAction;

	/**
	 * The temp file holding the right eye.
	 */
	private File rightEyeFile = null;

	/**
	 * The temp file holding the left eye.
	 */
	private File leftEyeFile = null;

	/**
	 * Static helper method to start the activity.
	 *
	 * @param context
	 *            The context in which the activity is started.
	 */
	public static final void startActivity(final Context context) {
		Intent intent = new Intent(context, CameraActivity.class);
		context.startActivity(intent);
	}

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		outputFolder = new File(PreferenceUtil.getSharedPreferenceString(R.string.key_folder_photos));

		boolean rightEyeLast = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_eye_sequence_choice);
		setAction(rightEyeLast ? LEFT_EYE : RIGHT_EYE);

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
						animateFlash();
					}
				});
	}

	/**
	 * Change to the given action.
	 *
	 * @param action
	 *            The new action.
	 */
	private void setAction(final CurrentAction action) {
		LinearLayout cameraThumbRight = (LinearLayout) findViewById(R.id.camera_thumb_layout_right);
		LinearLayout cameraThumbLeft = (LinearLayout) findViewById(R.id.camera_thumb_layout_left);
		FrameLayout cameraMainFrame = (FrameLayout) findViewById(R.id.camera_main_frame);
		LinearLayout cameraReviewFrame = (LinearLayout) findViewById(R.id.camera_review_frame);

		switch (action) {
		case RIGHT_EYE:
			startPreview();
			cameraMainFrame.setVisibility(View.VISIBLE);
			cameraReviewFrame.setVisibility(View.GONE);

			cameraThumbRight.setBackgroundResource(R.drawable.camera_thumb_background_highlighted);
			cameraThumbLeft.setBackgroundResource(R.drawable.camera_thumb_background);
			break;
		case LEFT_EYE:
			startPreview();
			cameraMainFrame.setVisibility(View.VISIBLE);
			cameraReviewFrame.setVisibility(View.GONE);

			cameraThumbRight.setBackgroundResource(R.drawable.camera_thumb_background);
			cameraThumbLeft.setBackgroundResource(R.drawable.camera_thumb_background_highlighted);
			break;
		case NAME:
			stopPreview();
			cameraMainFrame.setVisibility(View.GONE);
			cameraReviewFrame.setVisibility(View.VISIBLE);

			break;
		default:
			break;
		}

		currentAction = action;
	}

	/**
	 * Set the thumb image.
	 *
	 * @param data
	 *            The data representing the bitmap.
	 */
	private void setThumbImage(final byte[] data) {
		ImageView imageView = (ImageView) findViewById(currentAction == RIGHT_EYE ? R.id.camera_thumb_image_right : R.id.camera_thumb_image_left);

		Bitmap bitmap = ImageUtil.getImageBitmap(data, getResources().getDimensionPixelSize(R.dimen.camera_thumb_size));

		imageView.setImageBitmap(bitmap);
	}

	@Override
	public final void onResume() {
		super.onResume();

		if (currentAction == RIGHT_EYE || currentAction == LEFT_EYE) {
			if (!inPreview) {
				camera = CameraUtil.getCameraInstance();
				startPreview();
			}
		}
	}

	@Override
	public final void onPause() {
		if (currentAction == RIGHT_EYE || currentAction == LEFT_EYE) {
			stopPreview();
		}
		super.onPause();
	}

	/**
	 * Show a flashlight in the preview.
	 */
	private void animateFlash() {
		final View flashView = findViewById(R.id.camera_flash);

		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new DecelerateInterpolator());
		fadeOut.setDuration(500); // MAGIC_NUMBER
		fadeOut.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(final Animation animation) {
				flashView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(final Animation animation) {
				// do nothing
			}

			@Override
			public void onAnimationEnd(final Animation animation) {
				flashView.setVisibility(View.GONE);
			}
		});

		AnimationSet animation = new AnimationSet(false);
		animation.addAnimation(fadeOut);

		flashView.startAnimation(animation);
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

				// Resize frame to match aspect ratio
				float aspectRatio = ((float) pictureSize.width) / pictureSize.height;
				FrameLayout previewFrame = (FrameLayout) findViewById(R.id.camera_preview_frame);
				LayoutParams layoutParams = previewFrame.getLayoutParams();
				if (previewFrame.getWidth() > aspectRatio * previewFrame.getHeight()) {
					layoutParams.width = Math.round(previewFrame.getHeight() * aspectRatio);
					layoutParams.height = previewFrame.getHeight();
				}
				else {
					layoutParams.width = previewFrame.getWidth();
					layoutParams.height = Math.round(previewFrame.getWidth() / aspectRatio);
				}
				previewFrame.setLayoutParams(layoutParams);

				cameraConfigured = true;
			}
		}
	}

	/**
	 * Start the camera preview.
	 */
	private void startPreview() {
		if (cameraConfigured && camera != null && !inPreview) {
			camera.startPreview();
			inPreview = true;
		}
	}

	/**
	 * Stop the camera preview.
	 */
	private void stopPreview() {
		if (camera != null) {
			if (inPreview) {
				camera.stopPreview();
			}

			camera.release();
			camera = null;
			inPreview = false;
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
			if (currentAction == RIGHT_EYE || currentAction == LEFT_EYE) {
				startPreview();
			}
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
			inPreview = false;
			setThumbImage(data);
			File imageFile = FileUtil.getTempJpegFile();

			// Analyze next required step
			CurrentAction nextAction = currentAction;
			if (currentAction == RIGHT_EYE) {
				if (rightEyeFile != null && rightEyeFile.exists()) {
					rightEyeFile.delete();
				}
				rightEyeFile = imageFile;

				if (leftEyeFile == null) {
					nextAction = LEFT_EYE;
				}
				else {
					nextAction = NAME;
				}
			}
			else if (currentAction == LEFT_EYE) {
				if (leftEyeFile != null && leftEyeFile.exists()) {
					leftEyeFile.delete();
				}
				leftEyeFile = imageFile;

				if (rightEyeFile == null) {
					nextAction = RIGHT_EYE;
				}
				else {
					nextAction = NAME;
				}
			}

			// save photo
			new SavePhotoTask(data, currentAction).execute(imageFile);

			// go to next step
			setAction(nextAction);
		}
	};

	/**
	 * The task responsible for saving the picture.
	 */
	private final class SavePhotoTask extends AsyncTask<File, String, File> {
		/**
		 * The data to be saved.
		 */
		private byte[] data;

		/**
		 * The action indicating if right or left eye is saved.
		 */
		private CurrentAction action;

		/**
		 * Constructor, passing the data to be saved.
		 *
		 * @param data
		 *            The data to be saved.
		 * @param action
		 *            The action indicating if right or left eye is saved.
		 */
		private SavePhotoTask(final byte[] data, final CurrentAction action) {
			this.data = data;
			this.action = action;
		}

		@Override
		protected File doInBackground(final File... imageFiles) {
			File imageFile = imageFiles[0];

			try {
				FileOutputStream fos = new FileOutputStream(imageFile.getPath());

				fos.write(data);
				fos.close();
			}
			catch (java.io.IOException e) {
				Log.e(Application.TAG, "Exception when saving photo", e);
			}

			return imageFile;
		}

		@Override
		protected void onPostExecute(final File imageFile) {
			if (action == RIGHT_EYE) {
				PinchImageView imageViewRight = (PinchImageView) findViewById(R.id.imageViewRightEye);
				imageViewRight.setImage(imageFile.getAbsolutePath(), CameraActivity.this, 1);
			}
			else if (action == LEFT_EYE) {
				PinchImageView imageViewLeft = (PinchImageView) findViewById(R.id.imageViewLeftEye);
				imageViewLeft.setImage(imageFile.getAbsolutePath(), CameraActivity.this, 2);
			}
		}
	}

	/**
	 * Enumeration for holding the current action that the activity is doing.
	 */
	enum CurrentAction {
		/**
		 * Capturing the photo of the right eye.
		 */
		RIGHT_EYE,
		/**
		 * Capturing the photo of the left eye.
		 */
		LEFT_EYE,
		/**
		 * Capturing the name (and date).
		 */
		NAME
	}

}
