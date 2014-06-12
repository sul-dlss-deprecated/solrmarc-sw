package edu.stanford.enumValues;

/**
 * format facet values for Stanford University SearchWorks
 * @author - Naomi Dushay
 */
public enum Format {
	BOOK,
	COMPUTER_FILE,
	DATABASE_A_Z,
	DATASET,
	EQUIPMENT,
	IMAGE,
	JOURNAL_PERIODICAL,
	MANUSCRIPT_ARCHIVE,
	MAP,
	MUSIC_RECORDING,
	MUSIC_SCORE,
	NEWSPAPER,
	SOUND_RECORDING,
	VIDEO,
	OTHER;

	/**
	 * need to override for text of multiple words
	 */
	@Override
	public String toString() {
		switch (this) {
			case COMPUTER_FILE:
				return "Software/Multimedia";
			case DATABASE_A_Z:
				return "Database";
			case JOURNAL_PERIODICAL:
				return "Journal/Periodical";
			case MANUSCRIPT_ARCHIVE:
				return "Archive/Manuscript";
			case MUSIC_RECORDING:
				return "Music recording";
			case MUSIC_SCORE:
				return "Music score";
			case SOUND_RECORDING:
				return "Sound recording";
			default:
				String lc = super.toString().toLowerCase();
				String firstchar = lc.substring(0, 1).toUpperCase();
				return lc.replaceFirst(".{1}", firstchar);
		}
	}
}
