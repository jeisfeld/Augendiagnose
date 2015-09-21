package de.jeisfeld.augendiagnoselib.fragments;

import android.app.Fragment;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.SeekBar;
import android.widget.ToggleButton;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.Application.AuthorizationLevel;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.activities.DisplayHtmlActivity;
import de.jeisfeld.augendiagnoselib.activities.DisplayImageActivity;
import de.jeisfeld.augendiagnoselib.activities.DisplayOneActivity;
import de.jeisfeld.augendiagnoselib.components.OverlayPinchImageView;
import de.jeisfeld.augendiagnoselib.components.OverlayPinchImageView.GuiElementUpdater;
import de.jeisfeld.augendiagnoselib.components.colorpicker.ColorPickerConstants;
import de.jeisfeld.augendiagnoselib.components.colorpicker.ColorPickerDialog;
import de.jeisfeld.augendiagnoselib.components.colorpicker.ColorPickerSwatch.OnColorSelectedListener;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto.RightLeft;

/**
 * Variant of DisplayOneFragment that includes overlay handling.
 */
public class DisplayImageFragment extends Fragment implements GuiElementUpdater, OnColorSelectedListener {

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
	 * The resource key for the image type (TYPE_FILENAME or TYPE_FILERESOURCE).
	 */
	protected static final String STRING_TYPE = "de.jeisfeld.augendiagnoselib.TYPE";
	/**
	 * The resource key for the file path.
	 */
	protected static final String STRING_FILE = "de.jeisfeld.augendiagnoselib.FILE";
	/**
	 * The resource key for the file resource.
	 */
	protected static final String STRING_FILERESOURCE = "de.jeisfeld.augendiagnoselib.FILERESOURCE";
	/**
	 * The resource kay for the image index (in case of multiple images).
	 */
	protected static final String STRING_IMAGEINDEX = "de.jeisfeld.augendiagnoselib.IMAGEINDEX";
	/**
	 * The resource kay for the rightleft information (if not contained in the image).
	 */
	protected static final String STRING_RIGHTLEFT = "de.jeisfeld.augendiagnoselib.RIGHTLEFT";

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
	private int mType;

	/**
	 * The file resource id.
	 */
	private int mFileResource;

	/**
	 * The file path.
	 */
	private String mFile;

	/**
	 * The image index.
	 */
	private int mImageIndex;

	/**
	 * Information if right or left image.
	 */
	private RightLeft mRightLeft;

	/**
	 * Flag indicating if overlays are allowed.
	 */
	private boolean mAllowOverlays = true;

	/**
	 * Flag holding information if fragment is shown in landscape mode.
	 */
	private boolean mIsLandscape;

	protected final boolean isLandscape() {
		return mIsLandscape;
	}

	protected final void setLandscape(final boolean newIsLandscape) {
		this.mIsLandscape = newIsLandscape;
	}

	/**
	 * The view displaying the image.
	 */
	private OverlayPinchImageView mImageView;

	/**
	 * The number of overlay images.
	 */
	private static final int OVERLAY_COUNT = OverlayPinchImageView.OVERLAY_COUNT;

	/**
	 * The button for showing the image in full resolution.
	 */
	private Button mClarityButton;

	/**
	 * The button for showing the image comment.
	 */
	private Button mCommentButton;

	/**
	 * The button for showing the image info.
	 */
	private Button mInfoButton;

	/**
	 * The button for saving image metadata.
	 */
	private Button mSaveButton;

	/**
	 * The button for showing or hiding the tools.
	 */
	private Button mToolsButton;

	/**
	 * The button for showing or hiding the tools.
	 */
	private Button mHelpButton;

	/**
	 * The array of overlay buttons.
	 */
	private ToggleButton[] mToggleOverlayButtons;

	/**
	 * The lock button.
	 */
	private ToggleButton mLockButton;

	/**
	 * The color selector button.
	 */
	private Button mSelectColorButton;

	/**
	 * The brightness SeekBar.
	 */
	private SeekBar mSeekbarBrightness;

	/**
	 * The contrast SeekBar.
	 */
	private SeekBar mSeekbarContrast;

	/**
	 * A flag indicating if utilities (seekbars, buttons) should be displayed.
	 */
	private boolean mShowUtilities = true;

	/**
	 * The overlay color.
	 */
	private int mOverlayColor = Color.RED;

	/**
	 * Initialize the fragment with the file name.
	 *
	 * @param initialFile
	 *            the file path.
	 * @param initialImageIndex
	 *            The index of the view (required if there are multiple such fragments)
	 * @param initialRightLeft
	 *            Information if it is the right or left eye (if not in image metadata)
	 * @return
	 */
	public final void setParameters(final String initialFile, final int initialImageIndex,
			final RightLeft initialRightLeft) {
		Bundle args = new Bundle();
		args.putString(STRING_FILE, initialFile);
		args.putInt(STRING_TYPE, TYPE_FILENAME);
		args.putInt(STRING_IMAGEINDEX, initialImageIndex);
		if (initialRightLeft != null) {
			args.putSerializable(STRING_RIGHTLEFT, initialRightLeft);
		}

		setArguments(args);
	}

	/**
	 * Initialize the fragment with the file resource.
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

		mType = getArguments().getInt(STRING_TYPE, -1);
		mFile = getArguments().getString(STRING_FILE);
		mFileResource = getArguments().getInt(STRING_FILERESOURCE, -1);
		mImageIndex = getArguments().getInt(STRING_IMAGEINDEX, 0);
		mRightLeft = (RightLeft) getArguments().getSerializable(STRING_RIGHTLEFT);
	}

	/*
	 * Inflate View.
	 */
	// OVERRIDABLE
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {
		if (SystemUtil.isLandscape()) {
			setLandscape(true);
			return inflater.inflate(R.layout.fragment_display_image_landscape, container, false);
		}
		else {
			setLandscape(false);
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
			mShowUtilities = savedInstanceState.getBoolean("showUtilities");
			mOverlayColor = savedInstanceState.getInt("overlayColor", Color.RED);
		}
		else {
			mShowUtilities = getDefaultShowUtilities();
			mOverlayColor = PreferenceUtil.getSharedPreferenceInt(R.string.key_overlay_color, Color.RED);
		}

		mImageView = (OverlayPinchImageView) getView().findViewById(R.id.mainImage);
		mImageView.setGuiElementUpdater(this);
		mImageView.allowFullResolution(allowFullResolution());

		TypedArray overlayButtonResources = getResources().obtainTypedArray(R.array.overlay_buttons);
		mToggleOverlayButtons = new ToggleButton[OVERLAY_COUNT];
		for (int i = 0; i < OVERLAY_COUNT; i++) {
			mToggleOverlayButtons[i] = (ToggleButton) getView().findViewById(overlayButtonResources.getResourceId(i, -1));
			mToggleOverlayButtons[i].setVisibility(View.VISIBLE);
		}
		overlayButtonResources.recycle();

		mLockButton = (ToggleButton) getView().findViewById(R.id.toggleButtonLink);

		mSelectColorButton = (Button) getView().findViewById(R.id.buttonSelectColor);

		mClarityButton = (Button) getView().findViewById(R.id.buttonClarity);
		mInfoButton = (Button) getView().findViewById(R.id.buttonInfo);
		mCommentButton = (Button) getView().findViewById(R.id.buttonComment);
		mSaveButton = (Button) getView().findViewById(R.id.buttonSave);
		mToolsButton = (Button) getView().findViewById(R.id.buttonTools);
		mHelpButton = (Button) getView().findViewById(R.id.buttonHelp);

		showUtilities(mShowUtilities);

		// Initialize the onClick listeners for the buttons
		for (int i = 0; i < OVERLAY_COUNT; i++) {
			final int index = i;
			mToggleOverlayButtons[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					onToggleOverlayClicked(v, index);
				}
			});
		}

		mLockButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				onToggleLockClicked(v);
			}
		});

		mSelectColorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				onButtonSelectColorClicked(v);
			}
		});

		if (!allowFullResolution()) {
			mClarityButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					mImageView.showFullResolutionSnapshot(false);
					DialogUtil.displayTip(getActivity(), R.string.message_tip_clarity, R.string.key_tip_clarity);
				}
			});
		}
		else {
			mClarityButton.setVisibility(View.GONE);
		}

		mInfoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				DialogUtil.displayImageInfo(getActivity(), mImageView.getEyePhoto());
			}
		});

		mCommentButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				((DisplayImageActivity) getActivity()).startEditComment(DisplayImageFragment.this,
						mImageView.getMetadata().mComment);
				DialogUtil.displayTip(getActivity(), R.string.message_tip_editcomment, R.string.key_tip_editcomment);
			}
		});

		mSaveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				showSaveMenu(v);
				DialogUtil.displayTip(getActivity(), R.string.message_tip_saveview, R.string.key_tip_saveview);
			}
		});

		mToolsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				boolean newShowUtilities = !mShowUtilities;
				showUtilities(newShowUtilities);
				updateDefaultShowUtilities(newShowUtilities);
			}
		});

		mHelpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				DisplayHtmlActivity.startActivity(getActivity(), R.string.html_display_photos);
			}
		});

		// Initialize the listeners for the seekbars (brightness and contrast)
		mSeekbarBrightness = (SeekBar) getView().findViewById(R.id.seekBarBrightness);
		mSeekbarBrightness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
				mImageView.setBrightness(((float) seekBar.getProgress()) / seekBar.getMax() * 2 - 1);
			}
		});

		mSeekbarContrast = (SeekBar) getView().findViewById(R.id.seekBarContrast);
		mSeekbarContrast.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
				mImageView.setContrast(((float) seekBar.getProgress()) / seekBar.getMax() * 2 - 1);
			}
		});

		// The following also updates the selectColorButton
		mImageView.setOverlayColor(mOverlayColor);

	}

	/**
	 * Helper method for onClick actions for Button to toggle display of Overlays.
	 *
	 * @param view
	 *            The overlay button.
	 * @param position
	 *            The number of the overlay button.
	 */
	private void onToggleOverlayClicked(final View view, final int position) {
		boolean buttonGetsUnchecked = false;

		if (Application.getAuthorizationLevel() == AuthorizationLevel.TRIAL_ACCESS
				&& position >= Integer.parseInt(Application.getResourceString(R.string.overlay_trial_count))) {
			DialogUtil.displayAuthorizationError(getActivity(), R.string.message_dialog_trial_overlays);
			mToggleOverlayButtons[position].setChecked(false);
			return;
		}

		for (int i = 0; i < OVERLAY_COUNT; i++) {
			if (position != i) {
				if (mToggleOverlayButtons[i].isChecked()) {
					mToggleOverlayButtons[i].setChecked(false);
					buttonGetsUnchecked = true;
				}
			}
		}

		mImageView.triggerOverlay(position);

		if (mToggleOverlayButtons[position].isChecked() && !buttonGetsUnchecked) {
			DialogUtil.displayTip(getActivity(), R.string.message_tip_overlay, R.string.key_tip_overlay);
		}
	}

	/**
	 * onClick action for Button to switch link between overlay and image.
	 *
	 * @param view
	 *            The view of the link button.
	 */
	private void onToggleLockClicked(final View view) {
		ToggleButton button = (ToggleButton) view;
		mImageView.lockOverlay(button.isChecked(), true);
	}

	/**
	 * onClick action for Button to select color of overlays.
	 *
	 * @param view
	 *            The view of the select color button.
	 */
	private void onButtonSelectColorClicked(final View view) {
		ColorPickerDialog dialog =
				ColorPickerDialog
						.newInstance(R.string.color_picker_title, ColorPickerConstants.COLOR_PICKER_COLORS,
								mImageView.getOverlayColor(),
								ColorPickerConstants.COLOR_PICKER_COLUMNS,
								ColorPickerConstants.COLOR_PICKER_SIZE);

		dialog.setTargetFragment(this, 0);
		dialog.show(getFragmentManager(), ColorPickerDialog.class.toString());
	}

	/**
	 * Create the popup menu for saving metadata.
	 *
	 * @param view
	 *            The view opening the menu.
	 */
	public final void showSaveMenu(final View view) {
		PopupMenu popup = new PopupMenu(getActivity(), view);
		popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(final MenuItem item) {
				int itemId = item.getItemId();
				if (itemId == R.id.action_store_brightness) {
					mImageView.storeBrightnessContrast(false);
					return true;
				}
				else if (itemId == R.id.action_reset_brightness) {
					mImageView.storeBrightnessContrast(true);
					return true;
				}
				else if (itemId == R.id.action_store_position) {
					mImageView.storePositionZoom(false);
					return true;
				}
				else if (itemId == R.id.action_reset_position) {
					mImageView.storePositionZoom(true);
					return true;
				}
				else if (itemId == R.id.action_store_overlay_color) {
					mImageView.storeOverlayColor(false);
					return true;
				}
				else if (itemId == R.id.action_reset_overlay_color) {
					mImageView.storeOverlayColor(true);
					return true;
				}
				else if (itemId == R.id.action_delete_overlay_position) {
					mImageView.resetOverlayPosition(true);
					return true;
				}
				else {
					return true;
				}
			}
		});
		popup.inflate(R.menu.context_display_one);

		if (!mShowUtilities) {
			// Hide store/reset actions when utilities are not shown
			popup.getMenu().removeGroup(R.id.group_store_reset);
		}
		popup.show();
	}

	/**
	 * Store the comment in the image.
	 *
	 * @param comment
	 *            The comment text to be stored.
	 */
	public final void storeComment(final String comment) {
		mImageView.storeComment(comment);
	}

	/**
	 * Show or hide the utilities (overlay bar, scrollbars).
	 *
	 * @param show
	 *            If true, the utilities will be shown, otherwise hidden.
	 */
	protected final void showUtilities(final boolean show) {
		if (show) {
			if (mIsLandscape) {
				mToolsButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_tools_right, 0);
			}
			else {
				mToolsButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_tools_down);
			}
			getView().findViewById(R.id.separatorTools).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.seekBarBrightnessLayout).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.seekBarContrastLayout).setVisibility(View.VISIBLE);
			if (mAllowOverlays) {
				getView().findViewById(R.id.buttonOverlayLayout).setVisibility(View.VISIBLE);
			}
		}
		else {
			if (mIsLandscape) {
				mToolsButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_tools_left, 0);
			}
			else {
				mToolsButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_tools_up);
			}
			getView().findViewById(R.id.separatorTools).setVisibility(View.GONE);
			getView().findViewById(R.id.seekBarBrightnessLayout).setVisibility(View.GONE);
			getView().findViewById(R.id.seekBarContrastLayout).setVisibility(View.GONE);
			getView().findViewById(R.id.buttonOverlayLayout).setVisibility(View.GONE);
		}
		requestLayout();
		mShowUtilities = show;
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
	 * <p>1 means: don't show. 2 means: show only on full screen. 3 means: show always.
	 *
	 * @return The value indicating if utilities should be shown.
	 */
	private int getDefaultShowUtilitiesValue() {
		int level = PreferenceUtil.getSharedPreferenceInt(R.string.key_internal_show_utilities, -1);

		if (level == -1) {
			// call this method only if no value is set
			level = SystemUtil.isTablet() ? UTILITIES_SHOW_ALWAYS : UTILITIES_SHOW_FULLSCREEN;
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

		PreferenceUtil.setSharedPreferenceInt(R.string.key_internal_show_utilities, level);
	}

	@Override
	public final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("showUtilities", mShowUtilities);
		outState.putInt("overlayColor", mOverlayColor);
	}

	/**
	 * Initialize images - to be called after the views have restored instance state.
	 */
	public final void initializeImages() {
		if (mType == TYPE_FILERESOURCE) {
			mImageView.setImage(mFileResource, getActivity(), mImageIndex);
		}
		else {
			mImageView.setImage(mFile, getActivity(), mImageIndex);
		}

		if (mImageView.getEyePhoto().getRightLeft() == null && mRightLeft != null) {
			mImageView.getEyePhoto().setRightLeft(mRightLeft);
		}

		if (!mImageView.canHandleOverlays()) {
			getView().findViewById(R.id.buttonOverlayLayout).setVisibility(View.GONE);
			mAllowOverlays = false;
		}
	}

	/**
	 * Trigger redrawing of the imageView from outside.
	 */
	public final void requestLayout() {
		mImageView.post(new Runnable() {
			@Override
			public void run() {
				mImageView.requestLayout();
				mImageView.invalidate();
			}
		});
	}

	/**
	 * Method indicating if images should be displayed in full resolution automatically.
	 *
	 * @return true if should be shown in full resoltion.
	 */
	public final boolean allowFullResolution() {
		String fullResolutionFlag =
				PreferenceUtil.getSharedPreferenceString(R.string.key_full_resolution);

		if (getActivity().getClass().equals(DisplayOneActivity.class)) {
			return !"0".equals(fullResolutionFlag);
		}
		else {
			return "2".equals(fullResolutionFlag);
		}
	}

	/**
	 * Implementation of OnColorSelectedListener, to react on selected overlay color.
	 *
	 * @param color
	 *            the selected color.
	 */
	@Override
	public final void onColorSelected(final int color) {
		mImageView.setOverlayColor(color);
		mOverlayColor = color;
	}

	// Implementation of GuiElementUpdater

	@Override
	public final void setLockChecked(final boolean checked) {
		mLockButton.setChecked(checked);
	}

	@Override
	public final void updateSeekbarBrightness(final float brightness) {
		float progress = (brightness + 1) * mSeekbarBrightness.getMax() / 2;
		mSeekbarBrightness.setProgress(Float.valueOf(progress).intValue());
	}

	@Override
	public final void updateSeekbarContrast(final float contrast) {
		float progress = (contrast + 1) * mSeekbarContrast.getMax() / 2;
		mSeekbarContrast.setProgress(Float.valueOf(progress).intValue());
	}

	@Override
	public final void updateOverlayColorButton(final int color) {
		mSelectColorButton.setTextColor(color);
	}

	@Override
	public final int getOverlayDefaultColor() {
		return mOverlayColor;
	}

	@Override
	public final void resetOverlays() {
		for (int i = 0; i < OVERLAY_COUNT; i++) {
			mToggleOverlayButtons[i].setChecked(false);
		}
	}

	/*
	 * Ensure that the full size bitmap is cleaned from memory if memory is low.
	 */
	@Override
	public final void onTrimMemory(final int level) {
		mImageView.cleanFullBitmap();
		super.onTrimMemory(level);
	}

	/**
	 * Base implementation of OnSeekBarChangeListener, used for brightness and contrast seekbars.
	 */
	private abstract class OnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
		@Override
		public void onStopTrackingTouch(final SeekBar seekBar) {
			mImageView.refresh();
		}

		@Override
		public void onStartTrackingTouch(final SeekBar seekBar) {
			// do nothing
		}
	}
}
