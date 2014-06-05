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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.Application;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.activities.ListFoldersBaseActivity;
import de.eisfeldj.augendiagnose.activities.ListFoldersForDisplayActivity;
import de.eisfeldj.augendiagnose.util.DialogUtil;
import de.eisfeldj.augendiagnose.util.EyePhoto;

/**
 * Base fragment to display the list of subfolders of a folder Abstract class - child classes determine the detailed
 * actions. The folders should contain eye photos (following the name policy).
 */
public abstract class ListFoldersBaseFragment extends Fragment {
	protected static final String STRING_FOLDER = "de.eisfeldj.augendiagnose.FOLDER";
	protected static final List<String> FOLDERS_TOP = Arrays.asList(new String[] { "IRISTOPOGRAPHIE" });

	protected File parentFolder;

	protected ListView listView;
	protected EditText editText;
	protected ArrayAdapter<String> directoryListAdapter;

	/**
	 * Initialize the fragment with parentFolder
	 * 
	 * @param parentFolder
	 * @return
	 */
	public void setParameters(String parentFolder) {
		Bundle args = new Bundle();
		args.putString(STRING_FOLDER, parentFolder);

		setArguments(args);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		parentFolder = new File(args.getString(STRING_FOLDER));
	}

	/**
	 * Inflate View
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_list_names, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		listView = (ListView) getView().findViewById(R.id.listViewNames);
		createList();

		editText = (EditText) getView().findViewById(R.id.searchName);
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				directoryListAdapter.getFilter().filter(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// do nothing
			}

			@Override
			public void afterTextChanged(Editable s) {
				// do nothing
			}
		});

		setOnItemClickListener();
	}

	/**
	 * Listener for a short click on a list item
	 */
	protected abstract void setOnItemClickListener();

	/**
	 * Fill the list of subfolders and create the list adapter
	 */
	protected void createList() {
		List<String> folderNames = getFolderNames(parentFolder);
		if (folderNames == null) {
			DialogUtil.displayErrorAndReturn(getActivity(), R.string.message_dialog_folder_does_not_exist,
					parentFolder.getAbsolutePath());
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
	 * @return
	 */
	public static List<String> getFolderNames(File parentFolder) {
		File[] folders = parentFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		if (folders == null) {
			return null;
		}

		Arrays.sort(folders, new Comparator<File>() {
			public int compare(File f1, File f2) {
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
	 * Helper method to return the name of the file for sorting
	 * 
	 * @param f
	 *            The file
	 * @return The name for Sorting
	 */
	private static String getFilenameForSorting(File f) {
		String name = f.getName().toUpperCase(Locale.getDefault());
		if (FOLDERS_TOP.contains(name)) {
			return "1" + name;
		}
		else {
			return "2" + name;
		}
	}

	/**
	 * Rename a folder in the list, and rename all files in it (according to EyePhoto name policy)
	 * 
	 * @param oldFileName
	 * @param newFileName
	 */
	protected void renameFolderAndFiles(String oldFileName, String newFileName) {
		File oldFolder = new File(parentFolder, oldFileName.trim());
		File newFolder = new File(parentFolder, newFileName.trim());

		// rename folder and ensure that list is refreshed
		boolean success = oldFolder.renameTo(newFolder);
		directoryListAdapter.clear();
		createList();
		directoryListAdapter.notifyDataSetChanged();
		if (!success) {
			DialogUtil.displayError(getActivity(), R.string.message_dialog_failed_to_rename_folder,
					oldFolder.getAbsolutePath(), newFolder.getAbsolutePath());
			return;
		}

		// rename files in this folder
		File[] files = newFolder.listFiles();
		for (File f : files) {
			EyePhoto source = new EyePhoto(f.getAbsolutePath());
			if (!source.isFormatted()) {
				DialogUtil.displayError(getActivity(), R.string.message_dialog_unformatted_file,
						oldFolder.getAbsolutePath());
				continue;
			}
			if (!source.changePersonName(newFileName)) {
				DialogUtil.displayError(getActivity(), R.string.message_dialog_failed_to_rename_file,
						oldFolder.getAbsolutePath(), newFolder.getAbsolutePath());
			}
		}

		// In two-pane mode, refresh right pane
		if (getActivity() instanceof ListFoldersForDisplayActivity && Application.isTablet()) {
			ListFoldersForDisplayActivity activity = (ListFoldersForDisplayActivity) getActivity();
			activity.popBackStack();
			activity.listPicturesForName(newFileName);
		}

	}

	/**
	 * Delete a folder in the list, including all photos
	 * 
	 * @param name
	 */
	protected void deleteFolder(String name) {
		File folder = new File(parentFolder, name.trim());

		// delete folder and ensure that list is refreshed
		String[] children = folder.list();
		for (int i = 0; i < children.length; i++) {
			new File(folder, children[i]).delete();
		}
		boolean success = folder.delete();
		directoryListAdapter.clear();
		createList();
		directoryListAdapter.notifyDataSetChanged();
		if (!success) {
			DialogUtil.displayError(getActivity(), R.string.message_dialog_failed_to_delete_folder,
					folder.getAbsolutePath());
			return;
		}
	}

	/**
	 * Listener for a long click on a list item, which allows to change the name. Shows a dialog to enter the new name.
	 */
	protected class RenameOnLongClickListener implements OnItemLongClickListener {
		private AlertDialog dialog;

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, final long rowId) {
			CharSequence name = ((TextView) view).getText();
			showChangeNameDialog(name, name);
			return true;
		}

		public void closeDialog() {
			try {
				dialog.dismiss();
			}
			catch (Exception e) {

			}
		}

	}

	/**
	 * Show the dialog to change the selected name
	 * 
	 * @param oldName
	 *            The old name to be renamed
	 * @param inputText
	 *            The name to be initially displayed
	 */
	protected void showChangeNameDialog(final CharSequence oldName, final CharSequence inputText) {
		DisplayChangeNameFragment fragment = new DisplayChangeNameFragment();
		Bundle bundle = new Bundle();
		bundle.putCharSequence("inputText", inputText);
		bundle.putCharSequence("oldName", oldName);
		fragment.setArguments(bundle);
		fragment.show(getActivity().getFragmentManager(), DisplayChangeNameFragment.class.toString());
	}

	/**
	 * Fragment to change the name
	 */
	public static class DisplayChangeNameFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
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
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					}).setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							ListFoldersBaseActivity activity = (ListFoldersBaseActivity) getActivity();
							activity.fragment.renameFolderAndFiles(oldName.toString(), input.getText().toString());
						}
					});

			return builder.create();
		}
	}

}
