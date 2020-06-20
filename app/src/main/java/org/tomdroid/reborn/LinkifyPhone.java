package org.tomdroid.reborn;

import java.util.regex.Pattern;

import android.text.util.Linkify.MatchFilter;

/**
 * Statics useful for Linkify to create a better phone handler than android's default one
 * Fixes bugs like lp:512204
 */
public class LinkifyPhone {
	/**
	  * Don't treat anything with fewer than this many digits as a
	  * phone number.
	  */
	private static final int PHONE_NUMBER_MINIMUM_DIGITS = 5;
	  
	public static final Pattern PHONE_PATTERN = Pattern.compile( // sdd = space, dot, or dash
			"(\\+[0-9]+[\\- \\.]*)?"                    // +<digits><sdd>*
			+ "(\\([0-9]+\\)[\\- \\.]*)?"               // (<digits>)<sdd>*
			+ "([0-9]+[\\- \\.][0-9\\- \\.]+[0-9])"); // <digits><sdd><digits|sdds><digit> (at least one sdd!) 

	/**
	 *  Filters out URL matches that:
	 *  - the character before the match is not a whitespace
	 *  - don't have enough digits to be a phone number
	 */
	public static final MatchFilter sPhoneNumberMatchFilter = new MatchFilter() {

		public final boolean acceptMatch(CharSequence s, int start, int end) {

			// make sure there was a whitespace before pattern
			try {
				if (!Character.isWhitespace(s.charAt(start - 1))) {
					return false;
				}
			} catch (IndexOutOfBoundsException e) {
				//Do nothing
			}

			// minimum length
			int digitCount = 0;
			for (int i = start; i < end; i++) {
				if (Character.isDigit(s.charAt(i))) {
					digitCount++;
					if (digitCount >= PHONE_NUMBER_MINIMUM_DIGITS) {
						return true;
					}
				}
			}
			return false;
		}
	};

}
