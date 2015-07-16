package de.eisfeldj.augendiagnose.components;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * A Preference which just acts as a button.
 */
public class ButtonPreference extends Preference {
	/**
	 * The view in which the preference is displayed.
	 */
	private View view;

	/**
	 * Standard constructor.
	 *
	 * @param context
	 *            The Context this is associated with.
	 */
	public ButtonPreference(final Context context) {
		super(context);
	}

	/**
	 * Constructor that is called when inflating the Preference from XML.
	 *
	 * @param context
	 *            The Context this is associated with.
	 * @param attrs
	 *            The attributes of the XML tag that is inflating the preference.
	 */
	public ButtonPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected final View onCreateView(final ViewGroup parent) {
		view = super.onCreateView(parent);
		return view;
	}

	/**
	 * Get the view in which the preference is displayed.
	 *
	 * @return The view.
	 */
	public final View getView() {
		return view;
	}

}
