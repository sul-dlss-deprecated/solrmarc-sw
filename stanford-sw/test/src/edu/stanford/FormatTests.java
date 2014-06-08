package edu.stanford;

import java.io.*;

import org.junit.*;

import edu.stanford.enumValues.Format;

import org.marc4j.marc.*;


/**
 * junit4 tests for Stanford University format fields
 * Database formats are tested separately in FormatDatabaseTests
 * @author Naomi Dushay
 */
public class FormatTests extends AbstractStanfordTest
{
	private final String testDataFname = "formatTests.mrc";
	String testFilePath = testDataParentPath + File.separator + testDataFname;
	String fldName = "format";
	MarcFactory factory = MarcFactory.newInstance();

@Before
	public final void setup()
	{
		mappingTestInit();
	}


	/**
	 * Audio Non-Music format tests
	 */
@Test
	public final void testAudioNonMusic()
	{
		String fldVal = Format.SOUND_RECORDING.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06i", fldName, fldVal);
	}

	/**
	 * Book format tests
	 *   includes monographic series
	 */
@Test
	public final void testBookFormat()
	{
		String fldVal = Format.BOOK.toString();

		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06a07m", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06t07a", fldName, fldVal);
		// monographic series
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07s00821m", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5987319", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5598989", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "223344", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5666387", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "666", fldName, fldVal);

		// formerly believed to be monographic series
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "leader07b00600s00821m", fldName, fldVal);
	}

	/**
	 * Computer File format tests
	 */
@Test
	public final void testComputerFile()
	{
		String fldVal = Format.COMPUTER_FILE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06m00826a", fldName, fldVal);
	}

	/**
	 * Conference Proceedings format tests
	 */
@Test
	public final void testConferenceProceedings()
	{
		String fldVal = Format.CONFERENCE_PROCEEDINGS.toString();

		// test 650|v Congresses
		Record rec = factory.newRecord();
	    rec.setLeader(factory.newLeader("04473caa a2200313Ia 4500"));
	    ControlField cf008 = factory.newControlField("008");
	    cf008.setData("040202s2003    fi g     b    000 0deng d");
	    rec.addVariableField(cf008);
	    DataField df650 = factory.newDataField("650", ' ', '0');
	    df650.addSubfield(factory.newSubfield('a', "Music"));
	    df650.addSubfield(factory.newSubfield('v', "Congresses."));
	    rec.addVariableField(df650);
	    solrFldMapTest.assertSolrFldValue(rec, fldName, fldVal);

		// test 600|v Congresses
		rec = factory.newRecord();
	    rec.setLeader(factory.newLeader("04473caa a2200313Ia 4500"));
	    cf008 = factory.newControlField("008");
	    cf008.setData("040202s2003    fi g     b    000 0deng d");
	    rec.addVariableField(cf008);
	    DataField df600 = factory.newDataField("600", '1', '0');
	    df600.addSubfield(factory.newSubfield('a', "Sibelius, Jean,"));
	    df600.addSubfield(factory.newSubfield('d', "1865-1957"));
	    df600.addSubfield(factory.newSubfield('v', "Congresses."));
	    rec.addVariableField(df600);
	    solrFldMapTest.assertSolrFldValue(rec, fldName, fldVal);

	    // test LeaderChar07 = m and 008/29 = 1
		rec = factory.newRecord();
	    rec.setLeader(factory.newLeader("04473cam a2200313Ia 4500"));
	    cf008 = factory.newControlField("008");
	    cf008.setData("040202s2003    fi g     b    100 0deng d");
	    rec.addVariableField(cf008);
	    solrFldMapTest.assertSolrFldValue(rec, fldName, fldVal);

	    // test LeaderChar07 = s and 008/29 = 1
	    rec = factory.newRecord();
	    rec.setLeader(factory.newLeader("04473cas a2200313Ia 4500"));
	    cf008 = factory.newControlField("008");
	    cf008.setData("040202s2003    fi g     b    100 0deng d");
	    rec.addVariableField(cf008);
	    solrFldMapTest.assertSolrFldValue(rec, fldName, fldVal);

	    // test LeaderChar07 = m and 008/29 not 1
	    rec = factory.newRecord();
	    rec.setLeader(factory.newLeader("04473cam a2200313Ia 4500"));
	    cf008 = factory.newControlField("008");
	    cf008.setData("040202s2003    fi g     b    000 0deng d");
	    rec.addVariableField(cf008);
	    solrFldMapTest.assertSolrFldHasNoValue(rec, fldName, fldVal);

	    // test LeaderChar07 = s and 008/29 not 1
	    rec = factory.newRecord();
	    rec.setLeader(factory.newLeader("04473cas a2200313Ia 4500"));
	    cf008 = factory.newControlField("008");
	    cf008.setData("040202s2003    fi g     b    000 0deng d");
	    rec.addVariableField(cf008);
	    solrFldMapTest.assertSolrFldHasNoValue(rec, fldName, fldVal);

	    // test LeaderChar07 not s or m and 008/29 = 1
	    rec = factory.newRecord();
	    rec.setLeader(factory.newLeader("04473caa a2200313Ia 4500"));
	    cf008 = factory.newControlField("008");
	    cf008.setData("040202s2003    fi g     b    100 0deng d");
	    rec.addVariableField(cf008);
	    solrFldMapTest.assertSolrFldHasNoValue(rec, fldName, fldVal);

	    // test LeaderChar07 not s or m and 008/29 not 1
	    rec = factory.newRecord();
	    rec.setLeader(factory.newLeader("04473caa a2200313Ia 4500"));
	    cf008 = factory.newControlField("008");
	    cf008.setData("040202s2003    fi g     b    000 0deng d");
	    rec.addVariableField(cf008);
	    solrFldMapTest.assertSolrFldHasNoValue(rec, fldName, fldVal);
	}

	/**
	 * Image format tests
	 */
@Test
	public final void testImage()
	{
		String fldVal = Format.IMAGE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833i", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833k", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833p", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833s", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833t", fldName, fldVal);
	}

	/**
	 * Journal/Periodical format tests
	 */
@Test
	public final void testJournalPeriodicalFormat()
	{
        String fldVal = "Journal/Periodical";

     	// leader/07 s 008/21 blank
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06a07s", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "4114632", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "123", fldName, fldVal);
		// 006/00 s /04 blank
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821m", fldName, fldVal);
		// 006/00 s /04 blank
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821p", fldName, fldVal);
		// even though LCPER in 999 w
		solrFldMapTest.assertSolrFldValue(testFilePath, "460947", fldName, fldVal);
		// even though DEWEYPER in 999 w
		solrFldMapTest.assertSolrFldValue(testFilePath, "446688", fldName, fldVal);

		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821p", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "335577", fldName, fldVal);

		// leader/07s 008/21 d   006/00 s  006/04 d -- other
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "112233", fldName, fldVal);
	}

	/**
	 * Manuscript/Archive format tests
	 */
@Test
	public final void testManuscriptArchive()
	{
		String fldVal = Format.MANUSCRIPT_ARCHIVE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06b", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06p", fldName, fldVal);
	}

	/**
	 * Map/Globe format tests
	 */
@Test
	public final void testMapGlobe()
	{
		String fldVal = Format.MAP_GLOBE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06e", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06f", fldName, fldVal);
	}

	/**
	 * Microformat format tests
	 */
@Test
	public final void testMicroformat()
	{
		String fldVal = Format.MICROFORMAT.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "245hmicroform", fldName, fldVal);
	}

	/**
	 * Music Recording format tests
	 */
@Test
	public final void testMusicRecording()
	{
		String fldVal = Format.MUSIC_RECORDING.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06j", fldName, fldVal);
	}

	/**
	 * Music Score format tests
	 */
@Test
	public final void testMusicScore()
	{
		String fldVal = Format.MUSIC_SCORE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06c", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06d", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "245hmicroform", fldName, fldVal);
	}

	/**
	 * Newspaper format tests
	 */
@Test
	public final void testNewspaper()
	{
        String fldVal = Format.NEWSPAPER.toString();

		solrFldMapTest.assertSolrFldValue(testFilePath, "newspaper", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821n", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "334455", fldName, fldVal);

		// leader/07b 006/00s 008/21n - serial publication
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "leader07b00600s00821n", fldName, fldVal);
	}

	/**
	 * Thesis format tests
	 */
@Test
	public final void testThesis()
	{
		String fldVal = Format.THESIS.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "502", fldName, fldVal);
	}

	/**
	 * Video format tests
	 */
@Test
	public final void testVideo()
	{
		String fldVal = Format.VIDEO.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06g00833m", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06g00833v", fldName, fldVal);
	}

	/**
	 * Test assignment of Other format
	 */
@Test
	public final void testOtherFormat()
	{
        String fldVal = Format.OTHER.toString();

		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06t07b", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833w", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06g00833w", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06m00826u", fldName, fldVal);
		// 006/00 s /04 w
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821n", fldName, fldVal);
		// instructional kit
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06o", fldName, fldVal);
		// object
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06r", fldName, fldVal);
		// web site
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821w", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821w", fldName, fldVal);
		// leader/07 s, 006/00 m, 008/21 |
		solrFldMapTest.assertSolrFldValue(testFilePath, "7117119", fldName, fldVal);

		// as of 2010-10-03 008/21 d   means database if nothing else is assigned.
		//   See FormatDatabaseTests
		// leader/07 s 008/21 d, 006/00 s 006/04 d
//		solrFldMapTest.assertSolrFldValue(testFilePath, "112233", fldName, fldVal);
		// leader/07 s 008/21 d, 006/00 j 006/04 p
//		solrFldMapTest.assertSolrFldValue(testFilePath, "778899", fldName, fldVal);
//		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07s00600j00821d", fldName, fldVal);
		// 006/00 s  006/04 d
//		solrFldMapTest.assertSolrFldValue(testFilePath, "321", fldName, fldVal);
	}


	/**
	 * test format population based on ALPHANUM field values from 999
	 */
@Test
	public final void testFormatsFrom999()
	{
		String testFilePath = testDataParentPath + File.separator + "callNumberTests.mrc";

		String microVal = Format.MICROFORMAT.toString();
		// 999 ALPHANUM starting with MFLIM
		solrFldMapTest.assertSolrFldValue(testFilePath, "1261173", fldName, microVal);
		// 999 ALPHANUM starting with MFICHE
		solrFldMapTest.assertSolrFldValue(testFilePath, "mfiche", fldName, microVal);

		// 999 ALPHANUM starting with MCD
		solrFldMapTest.assertSolrFldValue(testFilePath, "1234673", fldName, Format.MUSIC_RECORDING.toString());
	}

}
