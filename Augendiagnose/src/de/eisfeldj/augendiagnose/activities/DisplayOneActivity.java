package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.PinchImageView;

/**
 * Activity to display one photo on full screen
 * 
 * @author Joerg
 */
public class DisplayOneActivity extends Activity {
	protected static final String STRING_EXTRA_TYPE = "de.eisfeldj.augendiagnose.TYPE";
	protected static final String STRING_EXTRA_FILE = "de.eisfeldj.augendiagnose.FILE";
	protected static final String STRING_EXTRA_FILERESOURCE = "de.eisfeldj.augendiagnose.FILERESOURCE";
	protected static final int TYPE_FILENAME = 1;
	protected static final int TYPE_FILERESOURCE = 2;

	protected int type;
	protected int fileResource;
	protected String file;
	protected PinchImageView imageView;

	/**
	 * Static helper method to start the activity, passing the path of the picture.
	 * 
	 * @param context
	 * @param filename
	 */
	public static void startActivity(Context context, String filename) {
		Intent intent = new Intent(context, DisplayOneActivity.class);
		intent.putExtra(STRING_EXTRA_FILE, filename);
		intent.putExtra(STRING_EXTRA_TYPE, TYPE_FILENAME);
		context.startActivity(intent);
	}

	/**
	 * Static helper method to start the activity, passing the path of the picture.
	 * 
	 * @param context
	 * @param filename
	 */
	public static void startActivity(Context context, int fileResource) {
		Intent intent = new Intent(context, DisplayOneActivity.class);
		intent.putExtra(STRING_EXTRA_FILERESOURCE, fileResource);
		intent.putExtra(STRING_EXTRA_TYPE, TYPE_FILERESOURCE);
		context.startActivity(intent);
	}

	/**
	 * Build the screen on creation
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getContentView());

		type = getIntent().getIntExtra(STRING_EXTRA_TYPE, -1);
		file = getIntent().getStringExtra(STRING_EXTRA_FILE);
		fileResource = getIntent().getIntExtra(STRING_EXTRA_FILERESOURCE, -1);

		imageView = (PinchImageView) findViewById(R.id.mainImage);
	}

	/**
	 * Get the content view resource
	 * 
	 * @return
	 */
	protected int getContentView() {
		return R.layout.activity_display_one;
	}

	/**
	 * Creating the bitmaps only after creation, so that the views have already determined size.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			// populate bitmaps in separate thread, so that screen keeps fluid.
			imageView.post(new Runnable() {
				@Override
				public void run() {
					if (type == TYPE_FILERESOURCE) {
						imageView.setImage(fileResource);
					}
					else {
						imageView.setImage(file);
					}
				}
			});
		}
	}

}
