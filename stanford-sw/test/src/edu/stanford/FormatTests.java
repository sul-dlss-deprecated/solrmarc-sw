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
	String displayFldName = "format";
	String facetFldName = "format";
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
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06i", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06i", displayFldName, fldVal);
	}
	
	/**
	 * Book format tests
	 *   includes monographic series
	 */
@Test
	public final void testBookFormat() 
	{
		String fldVal = Format.BOOK.toString();
		
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06a07m", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06a07m", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06t07a", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06t07a", displayFldName, fldVal);
		// monographic series
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07s00821m", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07s00821m", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5987319", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5987319", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5598989", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5598989", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "223344", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "223344", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5666387", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5666387", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "666", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "666", displayFldName, fldVal);

		// formerly believed to be monographic series 
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "leader07b00600s00821m", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "leader07b00600s00821m", displayFldName, fldVal);		
	}

	/**
	 * Computer File format tests
	 */
@Test
	public final void testComputerFile() 
	{
		String fldVal = Format.COMPUTER_FILE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06m00826a", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06m00826a", displayFldName, fldVal);
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
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833i", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833i", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833k", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833k", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833p", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833p", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833s", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833s", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833t", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833t", displayFldName, fldVal);
	}

	/**
	 * Journal/Periodical format tests
	 */
@Test
	public final void testJournalPeriodicalFormat() 
	{
        String fldVal = "Journal/Periodical";
		
     	// leader/07 s 008/21 blank
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06a07s", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06a07s", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "4114632", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "4114632", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "123", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "123", displayFldName, fldVal);
		// 006/00 s /04 blank
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821m", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821m", displayFldName, fldVal);
		// 006/00 s /04 blank
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821p", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821p", displayFldName, fldVal);
		// even though LCPER in 999 w
		solrFldMapTest.assertSolrFldValue(testFilePath, "460947", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "460947", displayFldName, fldVal);
		// even though DEWEYPER in 999 w
		solrFldMapTest.assertSolrFldValue(testFilePath, "446688", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "446688", displayFldName, fldVal);

		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821p", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821p", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "335577", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "335577", displayFldName, fldVal);
        
		// leader/07s 008/21 d   006/00 s  006/04 d -- other 
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "112233", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "112233", displayFldName, fldVal);
	}

	/**
	 * Manuscript/Archive format tests
	 */
@Test
	public final void testManuscriptArchive() 
	{
		String fldVal = Format.MANUSCRIPT_ARCHIVE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06b", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06b", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06p", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06p", displayFldName, fldVal);
	}
	
	/**
	 * Map/Globe format tests
	 */
@Test
	public final void testMapGlobe() 
	{
		String fldVal = Format.MAP_GLOBE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06e", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06e", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06f", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06f", displayFldName, fldVal);
	}
	
	/**
	 * Microformat format tests
	 */
@Test
	public final void testMicroformat() 
	{
		String fldVal = Format.MICROFORMAT.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "245hmicroform", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "245hmicroform", displayFldName, fldVal);
	}
	
	/**
	 * Music Recording format tests
	 */
@Test
	public final void testMusicRecording() 
	{
		String fldVal = Format.MUSIC_RECORDING.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06j", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06j", displayFldName, fldVal);
	}
	
	/**
	 * Music Score format tests
	 */
@Test
	public final void testMusicScore() 
	{
		String fldVal = Format.MUSIC_SCORE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06c", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06c", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06d", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06d", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "245hmicroform", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "245hmicroform", displayFldName, fldVal);		
	}
	
	/**
	 * Newspaper format tests
	 */
@Test
	public final void testNewspaper() 
	{
        String fldVal = Format.NEWSPAPER.toString();
		
		solrFldMapTest.assertSolrFldValue(testFilePath, "newspaper", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "newspaper", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821n", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821n", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "334455", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "334455", displayFldName, fldVal);
		
		// leader/07b 006/00s 008/21n - serial publication
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "leader07b00600s00821n", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "leader07b00600s00821n", displayFldName, fldVal);
	}

	/**
	 * Thesis format tests
	 */
@Test
	public final void testThesis() 
	{
		String fldVal = Format.THESIS.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "502", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "502", displayFldName, fldVal);
	}	
	
	/**
	 * Video format tests
	 */
@Test
	public final void testVideo() 
	{
		String fldVal = Format.VIDEO.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06g00833m", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06g00833m", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06g00833v", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06g00833v", displayFldName, fldVal);
	}

	/**
	 * Test assignment of Other format
	 */
@Test
	public final void testOtherFormat() 
	{
        String fldVal = Format.OTHER.toString();
        
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06t07b", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06t07b", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833w", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833w", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06g00833w", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06g00833w", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06m00826u", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06m00826u", displayFldName, fldVal);
		// 006/00 s /04 w
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821n", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821n", displayFldName, fldVal);
		// instructional kit 
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06o", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06o", displayFldName, fldVal);
		// object 
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06r", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06r", displayFldName, fldVal);
		// web site
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821w", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821w", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821w", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821w", displayFldName, fldVal);
		// leader/07 s, 006/00 m, 008/21 |
		solrFldMapTest.assertSolrFldValue(testFilePath, "7117119", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "7117119", displayFldName, fldVal);
		
		// as of 2010-10-03 008/21 d   means database if nothing else is assigned.
		//   See FormatDatabaseTests
		// leader/07 s 008/21 d, 006/00 s 006/04 d
//		solrFldMapTest.assertSolrFldValue(testFilePath, "112233", facetFldName, fldVal);
//		solrFldMapTest.assertSolrFldValue(testFilePath, "112233", displayFldName, fldVal);
		// leader/07 s 008/21 d, 006/00 j 006/04 p
//		solrFldMapTest.assertSolrFldValue(testFilePath, "778899", facetFldName, fldVal);
//		solrFldMapTest.assertSolrFldValue(testFilePath, "778899", displayFldName, fldVal);
//		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07s00600j00821d", facetFldName, fldVal);
//		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07s00600j00821d", displayFldName, fldVal);
		// 006/00 s  006/04 d
//		solrFldMapTest.assertSolrFldValue(testFilePath, "321", facetFldName, fldVal);
//		solrFldMapTest.assertSolrFldValue(testFilePath, "321", displayFldName, fldVal);
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
		solrFldMapTest.assertSolrFldValue(testFilePath, "1261173", displayFldName, microVal);
		// 999 ALPHANUM starting with MFICHE
		solrFldMapTest.assertSolrFldValue(testFilePath, "mfiche", displayFldName, microVal);

		// 999 ALPHANUM starting with MCD
		solrFldMapTest.assertSolrFldValue(testFilePath, "1234673", displayFldName, Format.MUSIC_RECORDING.toString());
	}

}
