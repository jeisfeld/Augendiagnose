package de.eisfeldj.augendiagnose.components;

public interface ContextMenuReferenceHolder {

	/**
	 * Store a reference to the context menu holder
	 * 
	 * @param o
	 */
	public void setContextMenuReference(Object o);
	
	/**
	 * Retrieve a reference to the context menu holder
	 * 
	 * @return
	 */
	public Object getContextMenuReference();
	
}
