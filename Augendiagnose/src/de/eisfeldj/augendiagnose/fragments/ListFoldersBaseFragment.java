package de.eisfeldj.augendiagnose.fragments;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.activities.ListFoldersBaseActivity;
import de.eisfeldj.augendiagnose.activities.ListFoldersForDisplayActivity;
import de.eisfeldj.augendiagnose.util.DialogUtil;
import de.eisfeldj.augendiagnose.util.EyePhoto;
import de.eisfeldj.augendiagnose.util.FileUtil;
import de.eisfeldj.augendiagnose.util.PreferenceUtil;

/**
 * Base listFoldersFragment to display the list of subfolders of a folder Abstract class - child classes determine the
 * detailed actions. The folders should contain eye photos (following the name policy).
 */
public abstract class ListFoldersBaseFragment extends Fragment {
	/**
	 * The resource key of the parent folder.
	 */
	protected static final String STRING_FOLDER = "de.eisfeldj.augendiagnose.FOLDER";

	/**
	 * The list of folder names which should be shown on top of the list.
	 */
	protected static final String[] FOLDERS_TOP = { "TOPOGRAPH", "TOPOGRAF", "IRIDOLOG" };

	// PUBLIC_FIELDS:START

	/**
	 * The parent folder.
	 */
	protected File parentFolder;

	/**
	 * The list view containing the folders.
	 */
	protected ListView listView;

	// PUBLIC_FIELDS:END

	/**
	 * The editText in which the name can be searched.
	 */
	private EditText editTextSearch;

	/**
	 * The array adapter displaying the list of names.
	 */
	private ArrayAdapter<String> directoryListAdapter;

	/**
	 * Initialize the listFoldersFragment with parentFolder.
	 *
	 * @param initialParentFolder
	 *            The parent folder for which the fragment should be created.
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
		parentFolder = new File(args.getString(STRING_FOLDER));
	}

	/*
	 * Inflate View
	 */
	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_list_names, container, false);
	}

	// OVERRIDABLE
	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		listView = (ListView) getView().findViewById(R.id.listViewNames);
		createList();

		editTextSearch = (EditText) getView().findViewById(R.id.searchName);
		editTextSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
				directoryListAdapter.getFilter().filter(s.toString());
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
	protected final void createList() {
		List<String> folderNames = getFolderNames(parentFolder);
		if (folderNames == null) {
			DialogUtil.displayError(getActivity(), R.string.message_dialog_folder_does_not_exist, true,
					parentFolder.getAbsolutePath());
			return;
		}
		if (folderNames.size() == 0) {
			DialogUtil.displayError(getActivity(), R.string.message_dialog_no_organized_photos, true);
			return;
		}

		directoryListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.adapter_list_names, folderNames);
		listView.setAdapter(directoryListAdapter);
		listView.setTextFilterEnabled(true);
	}

	/**
	 * Get the list of subfolders, using getFileNameForSorting() for ordering.
	 *
	 * @param parentFolder
	 *            The parent folder.
	 * @return The list of subfolders.
	 */
	public static final List<String> getFolderNames(final File parentFolder) {
		File[] folders = parentFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File pathname) {
				return pathname.isDirectory();
			}
		});

		if (folders == null) {
			return null;
		}

		Arrays.sort(folders, new Comparator<File>() {
			@Override
			public int compare(final File f1, final File f2) {
				return getFilenameForSorting(f1).compareTo(getFilenameForSorting(f2));
			}
		});
		List<String> folderNames = new ArrayList<String>();
		for (File f : folders) {
			folderNames.add(f.getName());
		}
		return folderNames;
	}

	/**
	 * Helper method to return the name of the file for sorting. Allows sorting by last name, and giving precedence to
	 * iris topography folders.
	 *
	 * @param f
	 *            The file
	 * @return The name for Sorting
	 */
	private static String getFilenameForSorting(final File f) {
		String name = f.getName().toUpperCase(Locale.getDefault());

		boolean sortByLastName = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_sort_by_last_name);
		if (sortByLastName) {
			int index = name.lastIndexOf(' ');
			if (index >= 0) {
				String firstName = name.substring(0, index);
				String lastName = name.substring(index + 1);
				name = lastName + " " + firstName;
			}
		}

		if (name.indexOf(' ') < 0) {
			for (int i = 0; i < FOLDERS_TOP.length; i++) {
				if (name.contains(FOLDERS_TOP[i])) {
					return "1" + name;
				}
			}
		}
		return "2" + name;
	}

	/**
	 * Rename a folder in the list, and rename all files in it (according to EyePhoto name policy).
	 *
	 * @param oldName
	 *            the old name.
	 * @param newName
	 *            the new name.
	 */
	protected final void renameFolderAndFiles(final String oldName, final String newName) {
		final File oldFolder = new File(parentFolder, oldName.trim());
		final File newFolder = new File(parentFolder, newName.trim());

		// rename folder and ensure that list is refreshed
		boolean success = FileUtil.renameFolder(oldFolder, newFolder);

		directoryListAdapter.clear();
		createList();
		directoryListAdapter.notifyDataSetChanged();

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
					directoryListAdapter.clear();
					createList();
					directoryListAdapter.notifyDataSetChanged();
				}
			});
		}

		// rename files in the new folder
		File[] files = newFolder.listFiles();
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
		if (getActivity() instanceof ListFoldersForDisplayActivity && Application.isTablet()) {
			ListFoldersForDisplayActivity activity = (ListFoldersForDisplayActivity) getActivity();
			activity.popBackStack();
			activity.listPicturesForName(newName);
		}

	}

	/**
	 * Delete a folder in the list, including all photos.
	 *
	 * @param name
	 *            the name for which the folder should be deleted.
	 */
	protected final void deleteFolder(final String name) {
		File folder = new File(parentFolder, name.trim());

		// delete files in folder.
		FileUtil.deleteFilesInFolder(folder);

		// delete folder and ensure that list is refreshed
		if (folder.delete()) {
			directoryListAdapter.clear();
			createList();
			directoryListAdapter.notifyDataSetChanged();
		}
		else {
			FileUtil.rmdirAsynchronously(getActivity(), folder, new Runnable() {
				@Override
				public void run() {
					directoryListAdapter.clear();
					createList();
					directoryListAdapter.notifyDataSetChanged();
				}
			});
		}
	}

	/**
	 * Show the dialog to change the selected name.
	 *
	 * @param oldName
	 *            The old name to be renamed
	 * @param inputText
	 *            The name to be initially displayed
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

			// This is a workaround - better solution might be a layout.
			// If the EditText is always recreated, then the content will be lost on orientation change.
			EditText input0;
			input0 = (EditText) getActivity().findViewById(R.id.editName);
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
						public void onClick(final DialogInterface dialog, final int id) {
							dialog.dismiss();
						}
					}).setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int id) {
							ListFoldersBaseActivity activity = (ListFoldersBaseActivity) getActivity();
							activity.getListFoldersFragment().renameFolderAndFiles(oldName.toString(), input.getText()
									.toString());
						}
					});

			return builder.create();
		}
	}

}
