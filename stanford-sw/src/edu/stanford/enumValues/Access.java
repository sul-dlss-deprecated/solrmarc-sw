package edu.stanford.enumValues;

/**
 * access facet values for Stanford University
 * INDEX-92 - Add "On order" as an access facet
 * @author - Naomi Dushay, Laney McGlohon
 */
public enum Access {
	ONLINE,
	AT_LIBRARY,
	ON_ORDER;

	/**
	 * need to override for text of multiple words
	 */
	@Override
	public String toString() {
		switch (this) {
		case AT_LIBRARY:
			return "At the Library";
		case ONLINE:
			return "Online";
		case ON_ORDER:
			return "On order";
		}
		String lc = super.toString().toLowerCase();
		String firstchar = lc.substring(0, 1).toUpperCase();
		return lc.replaceFirst(".{1}", firstchar);
	}
}
