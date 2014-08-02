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
import de.eisfeldj.augendiagnose.activities.DisplayImageActivity;
import de.eisfeldj.augendiagnose.components.ContextMenuReferenceHolder;
import de.eisfeldj.augendiagnose.components.OverlayPinchImageView;
import de.eisfeldj.augendiagnose.components.OverlayPinchImageView.GuiElementUpdater;
import de.eisfeldj.augendiagnose.util.JpegMetadataUtil;

/**
 * Variant of DisplayOneFragment that includes overlay handling.
 *
 * @author Joerg
 */
public class DisplayImageFragment extends Fragment implements GuiElementUpdater {

	/**
	 * "Show utilities value" indicating that utilities should never be shown.
	 */
	protected static final int UTILITIES_DO_NOT_SHOW = 1;
	/**
	 * "Show utilities value" indicating that utilities should be shown only on fullscreen.
	 */
	protected static final int UTILITIES_SHOW_FULLSCREEN = 2;
	/**
	 * "Show utilities value" indicating that utilities should always be shown.
	 */
	protected static final int UTILITIES_SHOW_ALWAYS = 3;

	/**
	 * The resource key of the image type (TYPE_FILENAME or TYPE_FILERESOURCE).
	 */
	protected static final String STRING_TYPE = "de.eisfeldj.augendiagnose.TYPE";
	/**
	 * The resource key of the file path.
	 */
	protected static final String STRING_FILE = "de.eisfeldj.augendiagnose.FILE";
	/**
	 * The resource key of the file resource.
	 */
	protected static final String STRING_FILERESOURCE = "de.eisfeldj.augendiagnose.FILERESOURCE";
	/**
	 * The resource kay of the image index (in case of multiple images).
	 */
	protected static final String STRING_IMAGEINDEX = "de.eisfeldj.augendiagnose.IMAGEINDEX";

	/**
	 * Type value set if the fragment shows an image by filename.
	 */
	protected static final int TYPE_FILENAME = 1;
	/**
	 * Type value set if the fragment shows an image by resource id.
	 */
	protected static final int TYPE_FILERESOURCE = 2;

	/**
	 * Type (TYPE_FILENAME or TYPE_FILERESOURCE).
	 */
	private int type;

	/**
	 * The file resource id.
	 */
	private int fileResource;

	/**
	 * The file path.
	 */
	private String file;

	/**
	 * The image index.
	 */
	private int imageIndex;

	/**
	 * The view displaying the image.
	 */
	private OverlayPinchImageView imageView;

	/**
	 * The maximum contrast allowed when changing the image contrast.
	 */
	private static final int CONTRAST_MAX = 5;
	/**
	 * The density of contrast levels.
	 */
	private static final int CONTRAST_DENSITY = 20;

	/**
	 * The number of overlay images.
	 */
	private static final int OVERLAY_COUNT = OverlayPinchImageView.OVERLAY_COUNT;

	/**
	 * The array of overlay buttons.
	 */
	private ToggleButton[] toggleOverlayButtons;

	/**
	 * The lock button.
	 */
	private ToggleButton lockButton;

	/**
	 * The brightness SeekBar.
	 */
	private SeekBar seekbarBrightness;

	/**
	 * The contrast SeekBar.
	 */
	private SeekBar seekbarContrast;

	/**
	 * A flag indicating if utilities (seekbars, buttons) should be displayed.
	 */
	private boolean showUtilities = true;

	/**
	 * Initialize the listFoldersFragment with the file name.
	 *
	 * @param initialFile
	 *            the file path.
	 * @param initialImageIndex
	 *            The index of the view (required if there are multiple such fragments)
	 * @return
	 */
	public final void setParameters(final String initialFile, final int initialImageIndex) {
		Bundle args = new Bundle();
		args.putString(STRING_FILE, initialFile);
		args.putInt(STRING_TYPE, TYPE_FILENAME);
		args.putInt(STRING_IMAGEINDEX, initialImageIndex);

		setArguments(args);
	}

	/**
	 * Initialize the listFoldersFragment with the file resource.
	 *
	 * @param initialFileResource
	 *            The file resource.
	 * @param initialImageIndex
	 *            The index of the view (required if there are multiple such fragments)
	 */
	public final void setParameters(final int initialFileResource, final int initialImageIndex) {
		Bundle args = new Bundle();
		args.putInt(STRING_FILERESOURCE, initialFileResource);
		args.putInt(STRING_TYPE, TYPE_FILERESOURCE);
		args.putInt(STRING_IMAGEINDEX, initialImageIndex);

		setArguments(args);
	}

	/*
	 * Retrieve parameters.
	 */
	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		type = getArguments().getInt(STRING_TYPE, -1);
		file = getArguments().getString(STRING_FILE);
		fileResource = getArguments().getInt(STRING_FILERESOURCE, -1);
		imageIndex = getArguments().getInt(STRING_IMAGEINDEX, 0);
	}

	/*
	 * Inflate View.
	 */
	// OVERRIDABLE
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (Application.isLandscape()) {
			return inflater.inflate(R.layout.fragment_display_image_landscape, container, false);
		}
		else {
			return inflater.inflate(R.layout.fragment_display_image_portrait, container, false);
		}
	}

	/*
	 * Update data from view.
	 */
	@Override
	public final void onActivityCreated(final Bundle savedInstanceState) {
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
		toggleOverlayButtons[3] = (ToggleButton) getView().findViewById(R.id.toggleButtonOverlay3); // MAGIC_NUMBER
		toggleOverlayButtons[4] = (ToggleButton) getView().findViewById(R.id.toggleButtonOverlay4); // MAGIC_NUMBER
		toggleOverlayButtons[5] = (ToggleButton) getView().findViewById(R.id.toggleButtonOverlay5); // MAGIC_NUMBER

		lockButton = (ToggleButton) getView().findViewById(R.id.toggleButtonLink);

		if (!Application.isAuthorized()) {
			toggleOverlayButtons[4].setEnabled(false); // MAGIC_NUMBER
			toggleOverlayButtons[4].setVisibility(View.GONE); // MAGIC_NUMBER
			toggleOverlayButtons[5].setEnabled(false); // MAGIC_NUMBER
			toggleOverlayButtons[5].setVisibility(View.GONE); // MAGIC_NUMBER
		}

		// Initialize the onClick listeners for the buttons
		for (int i = 0; i < OVERLAY_COUNT; i++) {
			final int index = i;
			toggleOverlayButtons[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					onToggleOverlayClicked(v, index);
				}
			});
		}

		lockButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				onToggleLinkClicked(v);
			}
		});

		// Initialize the listeners for the seekbars (brightness and contrast)
		seekbarBrightness = (SeekBar) getView().findViewById(R.id.seekBarBrightness);
		seekbarBrightness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
				imageView.setBrightness(((float) seekBar.getProgress()) / seekBar.getMax() * 2 - 1);
			}
		});

		seekbarContrast = (SeekBar) getView().findViewById(R.id.seekBarContrast);
		seekbarContrast.setMax(CONTRAST_MAX * CONTRAST_DENSITY);
		seekbarContrast.setProgress(CONTRAST_DENSITY);
		seekbarContrast.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
				imageView.setContrast(((float) seekBar.getProgress()) / CONTRAST_DENSITY);
			}
		});

		if (JpegMetadataUtil.changeJpegAllowed()) {
			registerForContextMenu(imageView);
		}
	}

	/**
	 * Helper method for onClick actions for Button to toggle display of Overlays.
	 *
	 * @param view
	 *            The overlay button.
	 * @param position
	 *            The number of the overlay button.
	 */
	public final void onToggleOverlayClicked(final View view, final int position) {
		for (int i = 0; i < OVERLAY_COUNT; i++) {
			if (position != i) {
				toggleOverlayButtons[i].setChecked(false);
			}
		}

		imageView.triggerOverlay(position);
	}

	/**
	 * onClick action for Button to switch link between overlay and image.
	 *
	 * @param view
	 *            The view of the link button.
	 */
	public final void onToggleLinkClicked(final View view) {
		ToggleButton button = (ToggleButton) view;
		imageView.lockOverlay(button.isChecked(), true);
	}

	/*
	 * Create the context menu.
	 */
	@Override
	public final void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
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

		if (((DisplayImageActivity) getActivity()).isEditingComment()) {
			// Do not allow duplicate selection of "edit comment"
			menu.removeItem(R.id.action_edit_comment);
		}

		// need to store reference, because onContextItemSelected will be called on all fragments
		((ContextMenuReferenceHolder) getActivity()).setContextMenuReference(this);
	}

	/*
	 * Handle items in the context menu.
	 */
	@Override
	public final boolean onContextItemSelected(final MenuItem item) {
		Object contextMenuReference = ((ContextMenuReferenceHolder) getActivity()).getContextMenuReference();

		if (contextMenuReference == this) {
			switch (item.getItemId()) {
			case R.id.action_show_hide_utilities:
				boolean newShowUtilities = !showUtilities;
				showUtilities(newShowUtilities);
				updateDefaultShowUtilities(newShowUtilities);
				return true;
			case R.id.action_edit_comment:
				((DisplayImageActivity) getActivity()).startEditComment(this, imageView.getMetadata().comment);
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
	 * Store the comment in the image.
	 *
	 * @param comment
	 *            The comment text to be stored.
	 */
	public final void storeComment(final String comment) {
		imageView.storeComment(comment);
	}

	/**
	 * Show or hide the utilities (overlay bar, scrollbars).
	 *
	 * @param show
	 *            If true, the utilities will be shown, otherwise hidden.
	 */
	protected final void showUtilities(final boolean show) {
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
	 * Get information if utilies should be shown according to default.
	 *
	 * @return true if utilities should be shown by default.
	 */
	protected final boolean getDefaultShowUtilities() {
		int level = getDefaultShowUtilitiesValue();

		return level >= getShowUtilitiesLimitLevel();
	}

	/**
	 * Return the level from which on the utilities are shown. 1 means: don't show. 2 means: show only on full screen. 3
	 * means: show always.
	 *
	 * @return The default limit level.
	 */
	// OVERRIDABLE
	protected int getShowUtilitiesLimitLevel() {
		return UTILITIES_SHOW_FULLSCREEN;
	}

	/**
	 * Get the value indicating if utilities should be shown.
	 *
	 * 1 means: don't show. 2 means: show only on full screen. 3 means: show always.
	 *
	 * @return The value indicating if utilities should be shown.
	 */
	private int getDefaultShowUtilitiesValue() {
		int level = Application.getSharedPreferenceInt(R.string.key_internal_show_utilities, -1);

		if (level == -1) {
			// call this method only if no value is set
			level = Application.isTablet() ? UTILITIES_SHOW_ALWAYS : UTILITIES_SHOW_FULLSCREEN;
		}

		return level;
	}

	/**
	 * Update default for showing utilities.
	 *
	 * @param show
	 *            true means that utilities should be shown.
	 */
	private void updateDefaultShowUtilities(final boolean show) {
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
	public final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("showUtilities", showUtilities);
	}

	/**
	 * Initialize images - to be called after the views have restored instance state.
	 */
	public final void initializeImages() {
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

	/**
	 * Trigger redrawing of the imageView from outside.
	 */
	public final void requestLayout() {
		imageView.post(new Runnable() {
			@Override
			public void run() {
				imageView.requestLayout();
				imageView.invalidate();
			}
		});
	}

	/**
	 * Base implementation of OnSeekBarChangeListener, used for brightness and contrast seekbars.
	 */
	private abstract class OnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
		@Override
		public void onStopTrackingTouch(final SeekBar seekBar) {
			imageView.refresh(true);
		}

		@Override
		public void onStartTrackingTouch(final SeekBar seekBar) {
			// do nothing
		}
	}

	// Implementation of GuiElementUpdater

	@Override
	public final void setLockChecked(final boolean checked) {
		lockButton.setChecked(checked);
	}

	@Override
	public final void updateSeekbarBrightness(final float brightness) {
		float progress = (brightness + 1) * seekbarBrightness.getMax() / 2;
		seekbarBrightness.setProgress(Float.valueOf(progress).intValue());
	}

	@Override
	public final void updateSeekbarContrast(final float contrast) {
		float progress = contrast * CONTRAST_DENSITY;
		seekbarContrast.setProgress(Float.valueOf(progress).intValue());
	}

	@Override
	public final void resetOverlays() {
		for (int i = 0; i < OVERLAY_COUNT; i++) {
			toggleOverlayButtons[i].setChecked(false);
		}
	}

}
