package de.eisfeldj.augendiagnose.fragments;

import android.app.Fragment;
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
import de.eisfeldj.augendiagnose.components.ContextMenuReferenceHolder;
import de.eisfeldj.augendiagnose.components.OverlayPinchImageView;
import de.eisfeldj.augendiagnose.components.OverlayPinchImageView.GuiElementUpdater;
import de.eisfeldj.augendiagnose.fragments.EditCommentFragment.EditCommentStarterActivity;
import de.eisfeldj.augendiagnose.util.JpegMetadataUtil;

/**
 * Variant of DisplayOneFragment that includes overlay handling
 * 
 * @author Joerg
 */
public class DisplayImageFragment extends Fragment implements GuiElementUpdater {
	protected static final String STRING_TYPE = "de.eisfeldj.augendiagnose.TYPE";
	protected static final String STRING_FILE = "de.eisfeldj.augendiagnose.FILE";
	protected static final String STRING_FILERESOURCE = "de.eisfeldj.augendiagnose.FILERESOURCE";
	protected static final String STRING_IMAGEINDEX = "de.eisfeldj.augendiagnose.IMAGEINDEX";
	protected static final int TYPE_FILENAME = 1;
	protected static final int TYPE_FILERESOURCE = 2;

	protected int type;
	protected int fileResource;
	protected String file;
	protected int imageIndex;

	private OverlayPinchImageView imageView;
	private static final int CONTRAST_MAX = 5;
	private static final int CONTRAST_DENSITY = 20;

	private static final int OVERLAY_COUNT = OverlayPinchImageView.OVERLAY_COUNT;
	private ToggleButton[] toggleOverlayButtons;

	private ToggleButton lockButton;
	private SeekBar seekbarBrightness;
	private SeekBar seekbarContrast;

	private boolean showUtilities = true;

	/**
	 * Initialize the fragment with the file name
	 * 
	 * @param text
	 * @param imageIndex
	 *            The index of the view (required if there are multiple such fragments)
	 * @return
	 */
	public void setParameters(String file, int imageIndex) {
		Bundle args = new Bundle();
		args.putString(STRING_FILE, file);
		args.putInt(STRING_TYPE, TYPE_FILENAME);
		args.putInt(STRING_IMAGEINDEX, imageIndex);

		setArguments(args);
	}

	/**
	 * Initialize the fragment with the file resource
	 * 
	 * @param text
	 * @param imageIndex
	 *            The index of the view (required if there are multiple such fragments)
	 * @return
	 */
	public void setParameters(int fileResource, int imageIndex) {
		Bundle args = new Bundle();
		args.putInt(STRING_FILERESOURCE, fileResource);
		args.putInt(STRING_TYPE, TYPE_FILERESOURCE);
		args.putInt(STRING_IMAGEINDEX, imageIndex);

		setArguments(args);
	}

	/**
	 * Retrieve parameters
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		type = getArguments().getInt(STRING_TYPE, -1);
		file = getArguments().getString(STRING_FILE);
		fileResource = getArguments().getInt(STRING_FILERESOURCE, -1);
		imageIndex = getArguments().getInt(STRING_IMAGEINDEX, 0);
	}

	/**
	 * Inflate View
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (Application.isLandscape()) {
			return inflater.inflate(R.layout.fragment_display_image_landscape, container, false);
		}
		else {
			return inflater.inflate(R.layout.fragment_display_image_portrait, container, false);
		}
	}

	/**
	 * Update data from view
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) {
			showUtilities = savedInstanceState.getBoolean("showUtilities");
		}
		else {
			showUtilities = getDefaultShowUtilities();
		}
		showUtilities(showUtilities);

		imageView = (OverlayPinchImageView) getView().findViewById(R.id.mainImage);
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

		if (!showUtilities) {
			// update text to show/hide utilities
			MenuItem item = menu.findItem(R.id.action_show_hide_utilities);

			item.setTitle(R.string.menu_show_utilities);
			// Hide store/reset actions when utilities are not shown
			menu.removeGroup(R.id.group_store_reset);
		}

		// need to store reference, because onContextItemSelected will be called on all fragments
		((ContextMenuReferenceHolder) getActivity()).setContextMenuReference(this);
	}

	/**
	 * Handle items in the context menu
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Object contextMenuReference = ((ContextMenuReferenceHolder) getActivity()).getContextMenuReference();

		if (contextMenuReference == this) {
			switch (item.getItemId()) {
			case R.id.action_show_hide_utilities:
				boolean newShowUtilities = !showUtilities;
				showUtilities(newShowUtilities);
				updateDefaultShowUtilities(newShowUtilities);
				return true;
			case R.id.action_edit_comment:
				((EditCommentStarterActivity) getActivity()).startEditComment(this, imageView.getMetadata().comment);
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
		else {
			return false;
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

	/**
	 * Show or hide the utilities (overlay bar, scrollbars)
	 * 
	 * @param show
	 *            If true, the utilities will be shown, otherwise hidden.
	 */
	protected void showUtilities(boolean show) {
		if (show) {
			getView().findViewById(R.id.seekBarBrightnessLayout).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.seekBarContrastLayout).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.buttonOverlayLayout).setVisibility(View.VISIBLE);
		}
		else {
			getView().findViewById(R.id.seekBarBrightnessLayout).setVisibility(View.GONE);
			getView().findViewById(R.id.seekBarContrastLayout).setVisibility(View.GONE);
			getView().findViewById(R.id.buttonOverlayLayout).setVisibility(View.GONE);
		}
		getView().findViewById(R.id.fragment_display_one_overlay).invalidate();
		showUtilities = show;
	}

	/**
	 * Get information if utilies should be shown according to default
	 */
	protected boolean getDefaultShowUtilities() {
		int level = getDefaultShowUtilitiesValue();

		return level >= getShowUtilitiesLimitLevel();
	}

	/**
	 * Return the level from which on the utilities are shown. 1 means: don't show. 2 means: show only on full screen. 3
	 * means: show always.
	 * 
	 * @return
	 */
	protected int getShowUtilitiesLimitLevel() {
		return 2;
	}

	/**
	 * Get the value of indicating if utilities should be shown.
	 * 
	 * 1 means: don't show. 2 means: show only on full screen. 3 means: show always.
	 * 
	 * @return
	 */
	protected int getDefaultShowUtilitiesValue() {
		int level = Application.getSharedPreferenceInt(R.string.key_internal_show_utilities, -1);

		if (level == -1) {
			// call this method only if no value is set
			level = Application.isTablet() ? 3 : 2;
		}

		return level;
	}

	/**
	 * Update default for showing utilities
	 */
	protected void updateDefaultShowUtilities(boolean show) {
		int level = getDefaultShowUtilitiesValue();

		if (show && level < getShowUtilitiesLimitLevel()) {
			level = getShowUtilitiesLimitLevel();
		}
		if (!show && level >= getShowUtilitiesLimitLevel()) {
			level = getShowUtilitiesLimitLevel() - 1;
		}

		Application.setSharedPreferenceInt(R.string.key_internal_show_utilities, level);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("showUtilities", showUtilities);
	}

	/**
	 * Initialize images - to be called after the views have restored instance state
	 */
	public void initializeImages() {
		if (type == TYPE_FILERESOURCE) {
			imageView.setImage(fileResource, getActivity(), imageIndex);
		}
		else {
			imageView.setImage(file, getActivity(), imageIndex);
		}

		if (!imageView.canHandleOverlays()) {
			getView().findViewById(R.id.buttonOverlayLayout).setVisibility(View.GONE);
		}
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
