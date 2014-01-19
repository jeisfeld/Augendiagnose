package de.eisfeldj.augendiagnose.activities;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.DialogUtil;
import de.eisfeldj.augendiagnose.util.EyePhoto;

/**
 * Base activity to display the list of subfolders of a folder Abstract class - child classes determine the detailed
 * actions. The folders should contain eye photos (following the name policy).
 */
public abstract class ListFoldersBaseActivity extends ListActivity {
	protected static final String STRING_EXTRA_FOLDER = "de.eisfeldj.augendiagnose.FOLDER";
	protected static final List<String> FOLDERS_TOP = Arrays.asList(new String[] { "IRISTOPOGRAPHIE" });

	protected File parentFolder;
	protected List<String> folderNames = new ArrayList<String>();
	private File[] folders;
	protected ArrayAdapter<String> directoryListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		parentFolder = new File(getIntent().getStringExtra(STRING_EXTRA_FOLDER));
		createList();

		setOnItemLongClickListener();
		setOnItemClickListener();
	}

	/**
	 * Listener for a long click on a list item
	 */
	protected abstract void setOnItemLongClickListener();

	/**
	 * Listener for a shore click on a list item
	 */
	protected abstract void setOnItemClickListener();

	/**
	 * Fill the list of subfolders and create the list adapter
	 */
	protected void createList() {
		folders = parentFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		if (folders == null) {
			DialogUtil.displayErrorAndReturn(this, R.string.message_dialog_folder_does_not_exist,
					parentFolder.getAbsolutePath());
			return;
		}

		Arrays.sort(folders, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return getFilenameForSorting(f1).compareTo(getFilenameForSorting(f2));
			}
		});
		folderNames.clear();
		for (File f : folders) {
			folderNames.add(f.getName());
		}
		directoryListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, folderNames);
		setListAdapter(directoryListAdapter);
	}

	/**
	 * Helper method to return the name of the file for sorting
	 * 
	 * @param f
	 *            The file
	 * @return The name for Sorting
	 */
	private String getFilenameForSorting(File f) {
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
	 * @param index
	 * @param newFileName
	 */
	protected void renameFolderAndFiles(int index, String newFileName) {
		File oldFolder = folders[index];
		File newFolder = new File(oldFolder.getParent(), newFileName.trim());

		// rename folder and ensure that list is refreshed
		boolean success = oldFolder.renameTo(newFolder);
		directoryListAdapter.clear();
		createList();
		directoryListAdapter.notifyDataSetChanged();
		if (!success) {
			DialogUtil.displayError(this, R.string.message_dialog_failed_to_rename_folder, oldFolder.getAbsolutePath(),
					newFolder.getAbsolutePath());
			return;
		}

		// rename files in this folder
		File[] files = newFolder.listFiles();
		for (File f : files) {
			EyePhoto source = new EyePhoto(f.getAbsolutePath());
			if (!source.isFormatted()) {
				DialogUtil.displayError(this, R.string.message_dialog_unformatted_file, oldFolder.getAbsolutePath());
				continue;
			}
			if (!source.changeName(newFileName)) {
				DialogUtil.displayError(this, R.string.message_dialog_failed_to_rename_file,
						oldFolder.getAbsolutePath(), newFolder.getAbsolutePath());
			}
		}
	}

	/**
	 * Listener for a long click on a list item, which allows to change the name. Shows a dialog to enter the new name.
	 */
	protected class RenameOnLongClickListener implements OnItemLongClickListener {
		private AlertDialog dialog;

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, final long rowId) {
			final ListFoldersBaseActivity activity = ListFoldersBaseActivity.this;
			final EditText input = new EditText(activity);
			input.setText(((TextView) view).getText());

			new AlertDialog.Builder(activity) //
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
							renameFolderAndFiles((int) rowId, input.getText().toString());
						}
					}).show();

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

}
