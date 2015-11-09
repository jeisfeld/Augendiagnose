package com.android.vending.billing;

/**
 * Exception thrown when encountering an invalid Base64 input character.
 *
 * @author nelson
 */
public class Base64DecoderException extends Exception {

	// JAVADOC:OFF

	public Base64DecoderException() {
	}

	public Base64DecoderException(final String s) {
		super(s);
	}

	private static final long serialVersionUID = 1L;
}
