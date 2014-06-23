package de.eisfeldj.augendiagnose.components;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.eisfeldj.augendiagnose.R;

/**
 * Class to present a dialog for selection of a directory.
 * 
 * Inspired by http://www.codeproject.com/Articles/547636/Android-Ready-to-use-simple-directory-chooser-dial
 */
public class DirectoryChooserDialog extends DialogFragment {
	private TextView mTitleView;

	private String mDir = "";
	private List<String> mSubdirs = null;
	private ArrayAdapter<String> mListAdapter = null;

	/**
	 * Callback interface for selected directory
	 */
	public interface ChosenDirectoryListener extends Serializable {
		public void onChosenDir(String chosenDir);

		public void onCancelled();
	}

	public static void displayDirectoryChooserDialog(Activity activity, ChosenDirectoryListener listener, String dir) {
		DirectoryChooserDialog fragment = new DirectoryChooserDialog();
		Bundle bundle = new Bundle();
		bundle.putString("dir", dir);
		bundle.putSerializable("listener", listener);
		fragment.setArguments(bundle);
		fragment.show(activity.getFragmentManager(), DirectoryChooserDialog.class.toString());
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
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

		LinearLayout titleLayout = new LinearLayout(context);
		titleLayout.setOrientation(LinearLayout.VERTICAL);

		mTitleView = new TextView(context);
		mTitleView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		mTitleView.setTextAppearance(context, android.R.style.TextAppearance_Large);
		mTitleView.setTextColor(context.getResources().getColor(android.R.color.white));
		mTitleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		mTitleView.setText(dir);

		titleLayout.addView(mTitleView);

		dialogBuilder.setCustomTitle(titleLayout);

		mListAdapter = createListAdapter(mSubdirs);

		dialogBuilder.setSingleChoiceItems(mListAdapter, -1, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Navigate into the sub-directory
				mDir += "/" + ((AlertDialog) dialog).getListView().getAdapter().getItem(which);
				updateDirectory();
			}
		});

		dialogBuilder.setCancelable(false);

		dialogBuilder.setPositiveButton(R.string.button_select, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Current directory chosen
				if (listener != null) {
					// Call registered listener supplied with the chosen directory
					listener.onChosenDir(mDir);
				}
			}
		}).setNegativeButton(R.string.button_cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (listener != null) {
					listener.onCancelled();
				}
			}
		});

		final AlertDialog dirsDialog = dialogBuilder.create();

		dirsDialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
					// Back button pressed
					dirsDialog.dismiss();
					if (listener != null) {
						listener.onCancelled();
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

	private List<String> getDirectories(String dir) {
		List<String> dirs = new ArrayList<String>();

		if (!(dir == null || !dir.startsWith("/") || dir.equals("/"))) {
			dirs.add("..");
		}

		try {
			File dirFile = new File(dir);
			if (!dirFile.exists() || !dirFile.isDirectory()) {
				return dirs;
			}

			for (File file : dirFile.listFiles()) {
				if (file.isDirectory()) {
					dirs.add(file.getName());
				}
			}
		}
		catch (Exception e) {
		}

		Collections.sort(dirs, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});

		return dirs;
	}

	private void updateDirectory() {
		try {
			mDir = new File(mDir).getCanonicalPath();
		}
		catch (IOException e) {
		}
		if (mDir == null || mDir.equals("")) {
			mDir = "/";
		}

		mSubdirs.clear();
		mSubdirs.addAll(getDirectories(mDir));
		mTitleView.setText(mDir);

		mListAdapter.notifyDataSetChanged();
	}

	private ArrayAdapter<String> createListAdapter(List<String> items) {
		return new ArrayAdapter<String>(getActivity(), R.layout.adapter_list_names, android.R.id.text1, items) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
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
	 * Get the SD card directory
	 * 
	 * @return
	 */
	private String getSdCardDirectory() {
		String sdCardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

		try {
			sdCardDirectory = new File(sdCardDirectory).getCanonicalPath();
		}
		catch (IOException ioe) {
		}
		return sdCardDirectory;
	}
}
