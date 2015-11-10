package de.jeisfeld.augendiagnoselib.components;

/**
 * This interface serves as a resolution to the problem that a click on a context menu item calls
 * onContextItemSelected() on all active activities and fragments, not only on the one that triggered the context menu.
 *
 * <p>The idea to solve this is to store a reference to the originator of the context menu in the activity when creating
 * the context menu. Then in onContextItemSelected, the originator can validate if the context menu belonged to him.
 */
public interface ContextMenuReferenceHolder {

	/**
	 * Store a reference to the context menu holder.
	 *
	 * @param o the reference to be stored (typically a fragment)
	 */
	void setContextMenuReference(Object o);

	/**
	 * Retrieve a reference to the context menu holder.
	 *
	 * @return the stored reference.
	 */
	Object getContextMenuReference();

}
