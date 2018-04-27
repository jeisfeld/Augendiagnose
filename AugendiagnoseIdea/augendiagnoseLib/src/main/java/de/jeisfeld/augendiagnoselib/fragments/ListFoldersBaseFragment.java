package de.jeisfeld.augendiagnoselib.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.io.FileFilter;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.Application.AuthorizationLevel;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.activities.ListFoldersBaseActivity;
import de.jeisfeld.augendiagnoselib.activities.ListFoldersForDisplayActivity;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto;
import de.jeisfeld.augendiagnoselib.util.imagefile.FileUtil;

/**
 * Base listFoldersFragment to display the list of subfolders of a folder Abstract class - child classes determine the
 * detailed actions. The folders should contain eye photos (following the name policy).
 */
public abstract class ListFoldersBaseFragment extends Fragment {
	/**
	 * The resource key of the parent folder.
	 */
	protected static final String STRING_FOLDER = "de.jeisfeld.augendiagnoselib.FOLDER";

	/**
	 * The maximum allowed number of names in the trial version.
	 */
	public static final int TRIAL_MAX_NAMES = 3;

	// PUBLIC_FIELDS:START

	/**
	 * The parent folder.
	 */
	@Nullable
	protected File mParentFolder;

	/**
	 * The list view containing the folders.
	 */
	protected ListView mListView;

	// PUBLIC_FIELDS:END

	/**
	 * The array adapter displaying the list of names.
	 */
	@Nullable
	private ArrayAdapter<String> mDirectoryListAdapter = null;

	/**
	 * Initialize the listFoldersFragment with parentFolder.
	 *
	 * @param initialParentFolder The parent folder for which the fragment should be created.
	 */
	public final void setParameters(final String initialParentFolder) {
		Bundle args = new Bundle();
		args.putString(STRING_FOLDER, initialParentFolder);

		setArguments(args);
	}

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		//noinspection ConstantConditions
		mParentFolder = new File(args.getString(STRING_FOLDER));
	}

	/*
	 * Inflate View
	 */
	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
								   final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_list_names, container, false);
	}

	// OVERRIDABLE
	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getView() == null) {
			return;
		}

		mListView = getView().findViewById(R.id.listViewNames);
		createList();

		EditText editTextSearch = getView().findViewById(R.id.searchName);
		editTextSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(@NonNull final CharSequence s, final int start, final int before, final int count) {
				mDirectoryListAdapter.getFilter().filter(s.toString());
			}

			@Override
			public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
				// do nothing
			}

			@Override
			public void afterTextChanged(final Editable s) {
				// do nothing
			}
		});

		setOnItemClickListener();
	}

	/**
	 * Listener for a short click on a list item.
	 */
	protected abstract void setOnItemClickListener();

	/**
	 * Fill the list of subfolders and create the list adapter.
	 */
	private void createList() {
		List<String> folderNames = getFolderNames(mParentFolder);

		if (mDirectoryListAdapter == null) {
			// fill initial adapter
			mDirectoryListAdapter = new ArrayAdapter<>(getActivity(), R.layout.adapter_list_names, folderNames);
			mListView.setAdapter(mDirectoryListAdapter);
			mListView.setTextFilterEnabled(true);
		}
		else {
			// update existing adapter
			mDirectoryListAdapter.clear();
			mDirectoryListAdapter.addAll(folderNames);
			if (getView() != null) {
				mDirectoryListAdapter.getFilter().filter(((EditText) getView().findViewById(R.id.searchName)).getText());
			}
			mDirectoryListAdapter.notifyDataSetChanged();
		}

		getActivity().findViewById(R.id.textViewNoImages).setVisibility(folderNames.size() == 0 ? View.VISIBLE : View.GONE);
	}

	/**
	 * Get the list of subfolders, using getFileNameForSorting() for ordering.
	 *
	 * @param parentFolder The parent folder.
	 * @return The list of subfolders.
	 */
	@NonNull
	public static List<String> getFolderNames(@NonNull final File parentFolder) {
		File[] folders = parentFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(@NonNull final File pathname) {
				return pathname.isDirectory();
			}
		});

		List<String> folderNames = new ArrayList<>();
		if (folders == null) {
			return folderNames;
		}

		Collator collator = Collator.getInstance();
		final Map<File, CollationKey> collationMap = new HashMap<>();
		for (File folder : folders) {
			collationMap.put(folder, collator.getCollationKey(getFilenameForSorting(folder)));
		}

		Arrays.sort(folders, new Comparator<File>() {
			@Override
			public int compare(final File f1, final File f2) {
				return collationMap.get(f1).compareTo(collationMap.get(f2));
			}
		});

		for (File f : folders) {
			folderNames.add(f.getName());
		}

		if (Application.getAuthorizationLevel() == AuthorizationLevel.TRIAL_ACCESS
				&& folderNames.size() > TRIAL_MAX_NAMES) {
			folderNames = folderNames.subList(0, TRIAL_MAX_NAMES);
		}

		return folderNames;
	}

	/**
	 * Helper method to return the name of the file for sorting. Allows sorting by last name, and giving precedence to
	 * iris topography folders.
	 *
	 * @param f The file
	 * @return The name for Sorting
	 */
	@NonNull
	private static String getFilenameForSorting(@NonNull final File f) {
		String name = f.getName();

		boolean sortByLastName = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_sort_by_last_name);
		if (sortByLastName) {
			int index = name.lastIndexOf(' ');
			if (index >= 0) {
				String firstName = name.substring(0, index);
				String lastName = name.substring(index + 1);
				name = lastName + " " + firstName;
			}
		}

		return name;
	}

	/**
	 * Rename a folder in the list, and rename all files in it (according to EyePhoto name policy).
	 *
	 * @param oldName the old name.
	 * @param newName the new name.
	 */
	private void renameFolderAndFiles(@NonNull final String oldName, @NonNull final String newName) {
		final File oldFolder = new File(mParentFolder, oldName.trim());
		final File newFolder = new File(mParentFolder, newName.trim());

		// rename folder and ensure that list is refreshed
		boolean success = FileUtil.renameFolder(oldFolder, newFolder); // STORE_PROPERTY

		createList();

		if (!success) {
			// In Kitkat workaround, try to delete old folder only in the end - if done immediately, it fails.
			DialogUtil.displayError(getActivity(), R.string.message_dialog_failed_to_move_folder_partially, false,
					oldFolder.getAbsolutePath(), newFolder.getAbsolutePath());
			return;
		}

		if (oldFolder.exists()) {
			// try to delete old folder in separate thread. This is not successful directly after moving files.
			FileUtil.rmdirAsynchronously(getActivity(), oldFolder, new Runnable() {
				@Override
				public void run() {
					createList();
				}
			});
		}

		// rename files in the new folder
		File[] files = newFolder.listFiles();
		if (files == null) {
			files = new File[0];
		}
		for (File f : files) {
			EyePhoto source = new EyePhoto(f.getAbsolutePath());
			if (!source.isFormatted()) {
				DialogUtil.displayError(getActivity(), R.string.message_dialog_unformatted_file, false,
						oldFolder.getAbsolutePath());
				continue;
			}
			if (!source.changePersonName(newName)) {
				DialogUtil.displayError(getActivity(), R.string.message_dialog_failed_to_rename_file, false,
						oldFolder.getAbsolutePath(), newFolder.getAbsolutePath());
			}
		}

		// In two-pane mode, refresh right pane
		if (getActivity() instanceof ListFoldersForDisplayActivity && SystemUtil.isTablet()) {
			ListFoldersForDisplayActivity activity = (ListFoldersForDisplayActivity) getActivity();
			activity.popBackStack();
			activity.listPicturesForName(newName);
		}

	}

	/**
	 * Delete a folder in the list, including all photos.
	 *
	 * @param name the name for which the folder should be deleted.
	 */
	protected final void deleteFolder(@NonNull final String name) {
		File folder = new File(mParentFolder, name.trim());

		// delete files in folder.
		FileUtil.deleteFilesInFolder(folder);

		// delete folder and ensure that list is refreshed
		if (folder.delete()) {
			createList();
		}
		else {
			FileUtil.rmdirAsynchronously(getActivity(), folder, new Runnable() {
				@Override
				public void run() {
					createList();
				}
			});
		}
	}

	/**
	 * Show the dialog to change the selected name.
	 *
	 * @param oldName   The old name to be renamed
	 * @param inputText The name to be initially displayed
	 */
	protected final void showChangeNameDialog(final CharSequence oldName, final CharSequence inputText) {
		DisplayChangeNameFragment fragment = new DisplayChangeNameFragment();
		Bundle bundle = new Bundle();
		bundle.putCharSequence("inputText", inputText);
		bundle.putCharSequence("oldName", oldName);
		fragment.setArguments(bundle);
		fragment.show(getActivity().getFragmentManager(), DisplayChangeNameFragment.class.toString());
	}

	/**
	 * Fragment to change the name.
	 */
	public static class DisplayChangeNameFragment extends DialogFragment {
		@Override
		public final Dialog onCreateDialog(final Bundle savedInstanceState) {
			final CharSequence inputText = getArguments().getCharSequence("inputText");
			final CharSequence oldName = getArguments().getCharSequence("oldName");
			if (oldName == null) {
				return null;
			}

			// This is a workaround - better solution might be a layout.
			// If the EditText is always recreated, then the content will be lost on orientation change.
			EditText input0;
			input0 = getActivity().findViewById(R.id.editName);
			if (input0 == null) {
				input0 = new EditText(getActivity());
				input0.setText(inputText);
				input0.setId(R.id.editName);
			}
			final EditText input = input0;

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()) //
					.setTitle(R.string.title_dialog_change_name) //
					.setView(input) //
					.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(@NonNull final DialogInterface dialog, final int id) {
							dialog.dismiss();
						}
					}).setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int id) {
							ListFoldersBaseActivity activity = (ListFoldersBaseActivity) getActivity();
							activity.getListFoldersFragment().renameFolderAndFiles(oldName.toString(), input.getText().toString());
						}
					});

			return builder.create();
		}
	}

}
