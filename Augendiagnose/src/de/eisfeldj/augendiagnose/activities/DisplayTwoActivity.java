package de.eisfeldj.augendiagnose.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.PinchImageView;

/**
 * Activity to display two pictures on full screen (screen split in two halves)
 */
public class DisplayTwoActivity extends Activity {
	private static final String STRING_EXTRA_FILE1 = "de.eisfeldj.augendiagnose.FILE1";
	private static final String STRING_EXTRA_FILE2 = "de.eisfeldj.augendiagnose.FILE2";

	private String file1, file2;
	private PinchImageView imageView1, imageView2;

	/**
	 * Static helper method to start the activity, passing the paths of the two pictures.
	 * 
	 * @param context
	 * @param filename1
	 * @param filename2
	 */
	public static void startActivity(Context context, String filename1, String filename2) {
		Intent intent = new Intent(context, DisplayTwoActivity.class);
		intent.putExtra(STRING_EXTRA_FILE1, filename1);
		intent.putExtra(STRING_EXTRA_FILE2, filename2);
		context.startActivity(intent);
	}

	/**
	 * Build the screen on creation
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_two);

		file1 = getIntent().getStringExtra(STRING_EXTRA_FILE1);
		file2 = getIntent().getStringExtra(STRING_EXTRA_FILE2);

		imageView1 = (PinchImageView) findViewById(R.id.mainImage1);
		imageView2 = (PinchImageView) findViewById(R.id.mainImage2);
	}

	/**
	 * Creating the bitmaps only after creation, so that the views have already determined size.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			// populate bitmaps in separate thread, so that screen keeps fluid.
			imageView1.post(new Runnable() {
				@Override
				public void run() {
					imageView1.setImage(file1);
				}
			});
			imageView2.post(new Runnable() {
				@Override
				public void run() {
					imageView2.setImage(file2);
				}
			});
		}
	}

}
