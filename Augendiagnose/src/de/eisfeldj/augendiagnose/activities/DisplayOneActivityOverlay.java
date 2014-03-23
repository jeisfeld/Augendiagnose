package de.eisfeldj.augendiagnose.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.ToggleButton;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.components.OverlayPinchImageView;
import de.eisfeldj.augendiagnose.components.OverlayPinchImageView.GuiElementUpdater;

/**
 * Variant of DisplayOneActivity that includes overlay handling
 * 
 * @author Joerg
 */
public class DisplayOneActivityOverlay extends DisplayOneActivity implements GuiElementUpdater {

	private OverlayPinchImageView imageView;
	private static final int CONTRAST_MAX = 5;
	private static final int CONTRAST_DENSITY = 20;

	private static final int OVERLAY_COUNT = OverlayPinchImageView.OVERLAY_COUNT;
	private ToggleButton[] toggleOverlayButtons;

	private ToggleButton lockButton;
	private SeekBar seekbarBrightness;
	private SeekBar seekbarContrast;

	/**
	 * Static helper method to start the activity, passing the path of the picture.
	 * 
	 * @param context
	 * @param filename
	 */
	public static void startActivity(Context context, String filename) {
		Intent intent = new Intent(context, DisplayOneActivityOverlay.class);
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
		Intent intent = new Intent(context, DisplayOneActivityOverlay.class);
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
		imageView = (OverlayPinchImageView) super.imageView;
		imageView.setGuiElementUpdater(this);

		toggleOverlayButtons = new ToggleButton[OVERLAY_COUNT];
		toggleOverlayButtons[0] = (ToggleButton) findViewById(R.id.toggleButtonOverlayCircle);
		toggleOverlayButtons[1] = (ToggleButton) findViewById(R.id.toggleButtonOverlay1);
		toggleOverlayButtons[2] = (ToggleButton) findViewById(R.id.toggleButtonOverlay2);
		toggleOverlayButtons[3] = (ToggleButton) findViewById(R.id.toggleButtonOverlay3);
		toggleOverlayButtons[4] = (ToggleButton) findViewById(R.id.toggleButtonOverlay4);
		toggleOverlayButtons[5] = (ToggleButton) findViewById(R.id.toggleButtonOverlay5);

		lockButton = (ToggleButton) findViewById(R.id.toggleButtonLink);

		if (!Application.isAuthorized()) {
			toggleOverlayButtons[4].setEnabled(false);
			toggleOverlayButtons[4].setVisibility(View.GONE);
			toggleOverlayButtons[5].setEnabled(false);
			toggleOverlayButtons[5].setVisibility(View.GONE);
		}

		// Initialize the listeners for the seekbars (brightness and contrast)
		seekbarBrightness = (SeekBar) findViewById(R.id.seekBarBrightness);
		seekbarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				imageView.refresh(true);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// do nothing
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				imageView.setBrightness(((float) seekBar.getProgress()) / seekBar.getMax() * 2 - 1);
			}
		});

		seekbarContrast = (SeekBar) findViewById(R.id.seekBarContrast);
		seekbarContrast.setMax(CONTRAST_MAX * CONTRAST_DENSITY);
		seekbarContrast.setProgress(CONTRAST_DENSITY);
		seekbarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				imageView.refresh(true);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// do nothing
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				imageView.setContrast(((float) seekBar.getProgress()) / CONTRAST_DENSITY);
			}
		});

		registerForContextMenu(imageView);

	}

	@Override
	protected int getContentView() {
		return R.layout.activity_display_one_overlay;
	}

	/**
	 * Helper method for onClick actions for Button to toggle display of Overlays
	 * 
	 * @param view
	 */
	private void onToggleOverlayClicked(View view, int position) {
		for (int i = 0; i < OVERLAY_COUNT; i++) {
			if (position != i) {
				toggleOverlayButtons[i].setChecked(false);
			}
		}

		imageView.triggerOverlay(position);
	}

	/**
	 * onClick action for Button to toggle display of Overlay 1
	 * 
	 * @param view
	 */
	public void onToggleOverlay0Clicked(View view) {
		onToggleOverlayClicked(view, 0);
	}

	/**
	 * onClick action for Button to toggle display of Overlay 1
	 * 
	 * @param view
	 */
	public void onToggleOverlay1Clicked(View view) {
		onToggleOverlayClicked(view, 1);
	}

	/**
	 * onClick action for Button to toggle display of Overlay 2
	 * 
	 * @param view
	 */
	public void onToggleOverlay2Clicked(View view) {
		onToggleOverlayClicked(view, 2);
	}

	/**
	 * onClick action for Button to toggle display of Overlay 3
	 * 
	 * @param view
	 */
	public void onToggleOverlay3Clicked(View view) {
		onToggleOverlayClicked(view, 3);
	}

	/**
	 * onClick action for Button to toggle display of Overlay 4
	 * 
	 * @param view
	 */
	public void onToggleOverlay4Clicked(View view) {
		onToggleOverlayClicked(view, 4);
	}

	/**
	 * onClick action for Button to toggle display of Overlay 5
	 * 
	 * @param view
	 */
	public void onToggleOverlay5Clicked(View view) {
		onToggleOverlayClicked(view, 5);
	}

	/**
	 * onClick action for Button to switch link between overlay and image
	 * 
	 * @param view
	 */
	public void onToggleLinkClicked(View view) {
		ToggleButton button = (ToggleButton) view;
		imageView.lockOverlay(button.isChecked(), true);
	}

	/**
	 * Create the context menu
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_display_one, menu);
	}

	/**
	 * Handle items in the context menu
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_edit_comment:
			// TODO
			return true;
		case R.id.action_store_brightness:
			imageView.storeBrightnessContrast(false);
			return true;
		case R.id.action_reset_brightness:
			imageView.storeBrightnessContrast(true);
			return true;
		case R.id.action_delete_overlay_position:
			imageView.resetOverlayPosition(true);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public void setLockChecked(boolean checked) {
		lockButton.setChecked(checked);
	}

	@Override
	public void updateSeekbarBrightness(float brightness) {
		float progress = (brightness + 1) * seekbarBrightness.getMax() / 2;
		seekbarBrightness.setProgress(Float.valueOf(progress).intValue());
	}

	@Override
	public void updateSeekbarContrast(float contrast) {
		float progress = contrast * CONTRAST_DENSITY;
		seekbarContrast.setProgress(Float.valueOf(progress).intValue());
	}

	@Override
	public void resetOverlays() {
		for (int i = 0; i < OVERLAY_COUNT; i++) {
			toggleOverlayButtons[i].setChecked(false);
		}
	}

}
