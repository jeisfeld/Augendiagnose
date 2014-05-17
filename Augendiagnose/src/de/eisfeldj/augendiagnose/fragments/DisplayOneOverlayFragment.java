package de.eisfeldj.augendiagnose.fragments;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.ToggleButton;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.activities.EditCommentActivity;
import de.eisfeldj.augendiagnose.components.OverlayPinchImageView;
import de.eisfeldj.augendiagnose.components.OverlayPinchImageView.GuiElementUpdater;
import de.eisfeldj.augendiagnose.util.JpegMetadataUtil;

/**
 * Variant of DisplayOneFragment that includes overlay handling
 * 
 * @author Joerg
 */
public class DisplayOneOverlayFragment extends DisplayOneFragment implements GuiElementUpdater {

	private OverlayPinchImageView imageView;
	private static final int CONTRAST_MAX = 5;
	private static final int CONTRAST_DENSITY = 20;

	private static final int OVERLAY_COUNT = OverlayPinchImageView.OVERLAY_COUNT;
	private ToggleButton[] toggleOverlayButtons;

	private ToggleButton lockButton;
	private SeekBar seekbarBrightness;
	private SeekBar seekbarContrast;

	/**
	 * Inflate View
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_display_one_overlay, container, false);
	}

	/**
	 * Update data from view
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		imageView = (OverlayPinchImageView) super.imageView;
		imageView.setGuiElementUpdater(this);

		toggleOverlayButtons = new ToggleButton[OVERLAY_COUNT];
		toggleOverlayButtons[0] = (ToggleButton) getView().findViewById(R.id.toggleButtonOverlayCircle);
		toggleOverlayButtons[1] = (ToggleButton) getView().findViewById(R.id.toggleButtonOverlay1);
		toggleOverlayButtons[2] = (ToggleButton) getView().findViewById(R.id.toggleButtonOverlay2);
		toggleOverlayButtons[3] = (ToggleButton) getView().findViewById(R.id.toggleButtonOverlay3);
		toggleOverlayButtons[4] = (ToggleButton) getView().findViewById(R.id.toggleButtonOverlay4);
		toggleOverlayButtons[5] = (ToggleButton) getView().findViewById(R.id.toggleButtonOverlay5);

		lockButton = (ToggleButton) getView().findViewById(R.id.toggleButtonLink);

		if (!Application.isAuthorized()) {
			toggleOverlayButtons[4].setEnabled(false);
			toggleOverlayButtons[4].setVisibility(View.GONE);
			toggleOverlayButtons[5].setEnabled(false);
			toggleOverlayButtons[5].setVisibility(View.GONE);
		}

		// Initialize the onClick listeners for the buttons
		for (int i = 0; i < OVERLAY_COUNT; i++) {
			final int index = i;
			toggleOverlayButtons[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onToggleOverlayClicked(v, index);
				}
			});
		}

		lockButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onToggleLinkClicked(v);
			}
		});

		// Initialize the listeners for the seekbars (brightness and contrast)
		seekbarBrightness = (SeekBar) getView().findViewById(R.id.seekBarBrightness);
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

		seekbarContrast = (SeekBar) getView().findViewById(R.id.seekBarContrast);
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

		if (JpegMetadataUtil.changeJpegAllowed()) {
			registerForContextMenu(imageView);
		}
	}

	/**
	 * Helper method for onClick actions for Button to toggle display of Overlays
	 * 
	 * @param view
	 */
	public void onToggleOverlayClicked(View view, int position) {
		for (int i = 0; i < OVERLAY_COUNT; i++) {
			if (position != i) {
				toggleOverlayButtons[i].setChecked(false);
			}
		}

		imageView.triggerOverlay(position);
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
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.context_display_one, menu);
	}

	/**
	 * Handle items in the context menu
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_edit_comment:
			EditCommentActivity.startActivity(getActivity(), imageView.getMetadata().comment);
			return true;
		case R.id.action_store_brightness:
			imageView.storeBrightnessContrast(false);
			return true;
		case R.id.action_reset_brightness:
			imageView.storeBrightnessContrast(true);
			return true;
		case R.id.action_store_position:
			imageView.storePositionZoom(false);
			return true;
		case R.id.action_reset_position:
			imageView.storePositionZoom(true);
			return true;
		case R.id.action_delete_overlay_position:
			imageView.resetOverlayPosition(true);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	/**
	 * Store the comment in the image
	 * 
	 * @param comment
	 */
	public void storeComment(String comment) {
		imageView.storeComment(comment);
	}

	// Implementation of GuiElementUpdater

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
