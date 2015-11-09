package com.android.vending.billing;

/**
 * Represents the result of an in-app billing operation. A result is composed of a response code (an integer) and
 * possibly a message (String). You can get those by calling {@link #getResponse} and {@link #getMessage()},
 * respectively. You can also inquire whether a result is a success or a failure by calling {@link #isSuccess()} and
 * {@link #isFailure()}.
 */
public class IabResult {

	// JAVADOC:OFF

	private int mResponse;
	private String mMessage;

	public IabResult(final int response, final String message) {
		mResponse = response;
		if (message == null || message.trim().length() == 0) {
			mMessage = IabHelper.getResponseDesc(response);
		}
		else {
			mMessage = message + " (response: " + IabHelper.getResponseDesc(response) + ")";
		}
	}

	public final int getResponse() {
		return mResponse;
	}

	public final String getMessage() {
		return mMessage;
	}

	public final boolean isSuccess() {
		return mResponse == IabHelper.BILLING_RESPONSE_RESULT_OK;
	}

	public final boolean isFailure() {
		return !isSuccess();
	}

	@Override
	public final String toString() {
		return "IabResult: " + getMessage();
	}
}
