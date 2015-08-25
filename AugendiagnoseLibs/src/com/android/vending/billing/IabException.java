package com.android.vending.billing;

/**
 * Exception thrown when something went wrong with in-app billing. An IabException has an associated IabResult (an
 * error). To get the IAB result that caused this exception to be thrown, call {@link #getResult()}.
 */
public class IabException extends Exception {

	// JAVADOC:OFF

	private static final long serialVersionUID = 1L;

	private final IabResult mResult;

	public IabException(final IabResult r) {
		this(r, null);
	}

	public IabException(final int response, final String message) {
		this(new IabResult(response, message));
	}

	public IabException(final IabResult r, final Exception cause) {
		super(r.getMessage(), cause);
		mResult = r;
	}

	public IabException(final int response, final String message, final Exception cause) {
		this(new IabResult(response, message), cause);
	}

	/** Returns the IAB result (error) that this exception signals. */
	public final IabResult getResult() {
		return mResult;
	}
}
