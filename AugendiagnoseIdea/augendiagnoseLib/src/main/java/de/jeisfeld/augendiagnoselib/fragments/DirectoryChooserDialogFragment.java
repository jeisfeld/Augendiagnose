package de.jeisfeld.augendiagnoselib.fragments;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;

/**
 * Class to present a dialog for selection of a directory.
 *
 * <p>Inspired by http://www.codeproject.com/Articles/547636/Android-Ready-to-use-simple-directory-chooser-dial
 */
public class DirectoryChooserDialogFragment extends DialogFragment {
	/**
	 * The text view showing the current folder.
	 */
	private TextView mCurrentFolderView;

	/**
	 * The list view showing the sub-elements.
	 */
	private ListView mListView;

	/**
	 * The current folder.
	 */
	private String mDir = "";

	/**
	 * The sub-elements of the current folder.
	 */
	private List<String> mSubdirs = null;

	/**
	 * The list adapter handling the sub-elements of the current folder.
	 */
	private ArrayAdapter<String> mListAdapter = null;

	/**
	 * The backstack of last browsed folders.
	 */
	private Stack<String> mBackStack = new Stack<String>();

	/**
	 * Create a DirectoryChooserDialogFragment.
	 *
	 * @param activity
	 *            The activity calling the dialog.
	 * @param listener
	 *            The callback listener reacting on the dialog response.
	 * @param dir
	 *            The start folder.
	 */
	public static void displayDirectoryChooserDialog(final Activity activity, final ChosenDirectoryListener listener,
			final String dir) {
		DirectoryChooserDialogFragment fragment = new DirectoryChooserDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString("dir", dir);
		bundle.putSerializable("listener", listener);
		fragment.setArguments(bundle);
		fragment.show(activity.getFragmentManager(), DirectoryChooserDialogFragment.class.toString());
	}

	/*
	 * Instantiate the view of the dialog.
	 */
	@Override
	public final Dialog onCreateDialog(final Bundle savedInstanceState) {
		// retrieve arguments

		String dir = getArguments().getString("dir");
		final ChosenDirectoryListener listener = (ChosenDirectoryListener) getArguments().getSerializable("listener");

		File dirFile = new File(dir);
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			dir = getSdCardDirectory();
		}

		try {
			dir = new File(dir).getCanonicalPath();
		}
		catch (IOException ioe) {
			return null;
		}

		mDir = dir;
		mSubdirs = getDirectories(dir);

		Context context = getActivity();

		// Create dialog

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

		dialogBuilder.setTitle(R.string.title_dialog_select_folder);

		View layout =
				LayoutInflater.from(context).inflate(R.layout.dialog_directory_chooser, new LinearLayout(context));
		dialogBuilder.setView(layout);

		mCurrentFolderView = (TextView) layout.findViewById(R.id.textCurrentFolder);
		mCurrentFolderView.setText(dir);

		mListView = (ListView) layout.findViewById(R.id.listViewSubfolders);
		mListAdapter = createListAdapter(mSubdirs);
		mListView.setAdapter(mListAdapter);

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				mBackStack.push(mDir);
				mDir += File.separator + mListAdapter.getItem(position);
				updateDirectory();
			}
		});

		dialogBuilder.setCancelable(false);

		dialogBuilder.setPositiveButton(R.string.button_select, new OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				// Current directory chosen
				if (listener != null) {
					// Call registered listener supplied with the chosen directory
					listener.onChosenDir(mDir);
				}
			}
		}).setNegativeButton(R.string.button_cancel, new OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				if (listener != null) {
					listener.onCancelled();
				}
			}
		});

		final AlertDialog dirsDialog = dialogBuilder.create();

		dirsDialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(final DialogInterface dialog, final int keyCode, final KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
					// Back button pressed - go to the last directory if existing - otherwise cancel the dialog.
					if (mBackStack.size() == 0) {
						dirsDialog.dismiss();
						if (listener != null) {
							listener.onCancelled();
						}
					}
					else {
						mDir = mBackStack.pop();
						updateDirectory();
					}

					return true;
				}
				else {
					return false;
				}
			}
		});

		return dirsDialog;
	}

	/**
	 * Get the list of subdirectories of the current directory. Returns ".." as first value if appropriate.
	 *
	 * @param dir
	 *            The current directory.
	 * @return The list of subdirectories.
	 */
	private List<String> getDirectories(final String dir) {
		List<String> dirs = new ArrayList<String>();

		if (dir != null && dir.startsWith(File.separator) && !dir.equals(File.separator)) {
			dirs.add("..");
		}

		try {
			File dirFile = new File(dir);
			if (!dirFile.exists() || !dirFile.isDirectory()) {
				return dirs;
			}
			File[] files = dirFile.listFiles();
			if (files == null) {
				return dirs;
			}

			for (File file : files) {
				if (file.isDirectory()) {
					dirs.add(file.getName());
				}
			}
		}
		catch (Exception e) {
			Log.e(Application.TAG, "Could not get directories", e);
		}

		Collections.sort(dirs, new Comparator<String>() {
			@Override
			public int compare(final String o1, final String o2) {
				return o1.compareTo(o2);
			}
		});

		return dirs;
	}

	/**
	 * Update the current directory.
	 */
	private void updateDirectory() {
		try {
			mDir = new File(mDir).getCanonicalPath();
		}
		catch (IOException e) {
			// i
		}
		if (mDir == null || "".equals(mDir)) {
			mDir = File.separator;
		}

		mSubdirs.clear();
		mSubdirs.addAll(getDirectories(mDir));
		mCurrentFolderView.setText(mDir);

		mListAdapter.notifyDataSetChanged();
	}

	/**
	 * Create the list adapter for the list of folders.
	 *
	 * @param items
	 *            The list of folders.
	 * @return The list adapter.
	 */
	private ArrayAdapter<String> createListAdapter(final List<String> items) {
		return new ArrayAdapter<String>(getActivity(), R.layout.adapter_list_names, android.R.id.text1, items) {
			@Override
			public View getView(final int position, final View convertView, final ViewGroup parent) {
				View v = super.getView(position, convertView, parent);

				if (v instanceof TextView) {
					// Enable list item (directory) text wrapping
					TextView tv = (TextView) v;
					tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
					tv.setEllipsize(null);
				}
				return v;
			}
		};
	}

	/**
	 * Get the SD card directory.
	 *
	 * @return The SD card directory.
	 */
	private String getSdCardDirectory() {
		String sdCardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

		try {
			sdCardDirectory = new File(sdCardDirectory).getCanonicalPath();
		}
		catch (IOException ioe) {
			Log.e(Application.TAG, "Could not get SD directory", ioe);
		}
		return sdCardDirectory;
	}

	/**
	 * Callback interface for selected directory.
	 */
	public interface ChosenDirectoryListener extends Serializable {
		/**
		 * Called when a folder is selected.
		 *
		 * @param chosenDir
		 *            The selected folder.
		 */
		void onChosenDir(final String chosenDir);

		/**
		 * Called when the dialog is cancelled.
		 */
		void onCancelled();
	}
}
