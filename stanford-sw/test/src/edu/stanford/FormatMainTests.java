package edu.stanford;

import java.io.*;

import org.junit.*;
import org.marc4j.marc.*;

import edu.stanford.enumValues.Format;
import edu.stanford.enumValues.FormatOld;


/**
 * junit4 tests for Stanford University format_main_ssim field
 * Database formats are tested separately in FormatDatabaseTests
 * Physical formats are tested separated in FormatPhysicalTests
 * @author Naomi Dushay
 * @author Laney McGlohon
 */
public class FormatMainTests extends AbstractStanfordTest
{
	private final static String fldName = "format_main_ssim";
	private final static String journalVal = Format.JOURNAL_PERIODICAL.toString();
	private final static String dbazVal = Format.DATABASE_A_Z.toString();
	MarcFactory factory = MarcFactory.newInstance();
	private ControlField cf008 = factory.newControlField("008");
	private ControlField cf006 = factory.newControlField("006");
	private DataField df956sfx = factory.newDataField("956", '4', '0');
	DataField df999dbaz = factory.newDataField("999", ' ', ' ');
	{
		df956sfx.addSubfield(factory.newSubfield('u', " http://library.stanford.edu/sfx?stuff"));
		df999dbaz.addSubfield(factory.newSubfield('a', "INTERNET RESOURCE"));
		df999dbaz.addSubfield(factory.newSubfield('w', "ASIS"));
		df999dbaz.addSubfield(factory.newSubfield('i', "2475606-5001"));
		df999dbaz.addSubfield(factory.newSubfield('l', "INTERNET"));
		df999dbaz.addSubfield(factory.newSubfield('m', "SUL"));
		df999dbaz.addSubfield(factory.newSubfield('t', "DATABASE"));
	}



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
		
		// leader/06 i - audio non-music
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cid  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 v eng d");
		record.addVariableField(cf008);
		DataField df245 = factory.newDataField("245", '1', '0');
		df245.addSubfield(factory.newSubfield('a', "audio non-music: leader/06 i"));
		record.addVariableField(df245);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
	}

	/**
	 * Book format tests
	 *   includes monographic series
	 */
@Test
	public final void testBookFormat()
	{
		String bookVal = Format.BOOK.toString();

		// leader/06 a /07 m - book
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cam  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 0 eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, bookVal);
		
		// leader/06 t /07 a - book
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cta  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 0 eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, bookVal);

		// conference proceedings - 600v Congresses.
		record = factory.newRecord();
		record.setLeader(factory.newLeader("04473cam a2200313Ia 4500"));
		cf008.setData("040202s2003    fi g     b    100 0deng d");
		record.addVariableField(cf008);
		DataField df600 = factory.newDataField("600", '1', '0');
		df600.addSubfield(factory.newSubfield('a', "Sibelius, Jean,"));
		df600.addSubfield(factory.newSubfield('d', "1865-1957"));
		df600.addSubfield(factory.newSubfield('v', "Congresses."));
		record.addVariableField(df600);
		solrFldMapTest.assertSolrFldValue(record, fldName, bookVal);

		// Conference Proceedings - 650v Congresses
		record = factory.newRecord();
		record.setLeader(factory.newLeader("04473cam a2200313Ia 4500"));
		cf008.setData("040202s2003    fi g     b    100 0deng d");
		record.addVariableField(cf008);
		DataField df650 = factory.newDataField("650", ' ', '0');
		df650.addSubfield(factory.newSubfield('a', "Music"));
		df650.addSubfield(factory.newSubfield('v', "Congresses."));
		record.addVariableField(df650);
		solrFldMapTest.assertSolrFldValue(record, fldName, bookVal);

		// formerly believed to be monographic series
		// leader/07 b, 006/00 s, 008/21 m - serial publication
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cab  2200457Ia 4500"));
		cf006.setData("s        h        ");
		record.addVariableField(cf006);
		cf008.setData("780930m19391944nyu   m       000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldHasNoValue(record, fldName, bookVal);
	}

	/**
     * if a continuing monographic resource (a book series) has an SFX link,
     * then it should be format Journal.
     */
@Test
	public final void testBookSeriesAsBook()
	{
		String bookSeriesVal = Format.BOOK.toString();

		// based on 9343812 - SFX link
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01937cas a2200433 a 4500"));
		cf008.setData("070207c20109999mauqr m o     0   a0eng c");
		record.addVariableField(cf008);
		record.addVariableField(df956sfx);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, bookSeriesVal);

		// based on 9138750 - no SFX link
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01750cas a2200409 a 4500"));
		cf008.setData("101213c20109999dcufr m bs   i0    0eng c");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, bookSeriesVal);

		// monographic series without SFX links
		// leader/07 s, no 006, 008/21 m - book (monographic series)
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cas  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu   m       000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, bookSeriesVal);
		
		// leader/07 s  and 008/21 m - Book: monographic series
		record = factory.newRecord();
		record.setLeader(factory.newLeader("00868cas a22002294a 4500"));
		cf008.setData("050823c20029999ohuuu m       0    0eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, bookSeriesVal);

		// leader/07 b, 006/00 s, 006/04 m, 008/21 d - book (monographic series)
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cab  2200457Ia 4500"));
		cf006.setData("s   m    h        ");
		record.addVariableField(cf006);
		cf008.setData("780930m19391944nyu   d       000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, bookSeriesVal);
    }

	/**
	 * Collection from "Other" format tests
	 */
	@Test
	public final void testCollectionFromOther()
	{
		String fldVal = Format.MANUSCRIPT_ARCHIVE.toString();
		String otherFldVal = Format.OTHER.toString();

		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01529cac a2200397Ia 4500"));
		cf008.setData("081215c200u9999xx         b        eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
		solrFldMapTest.assertSolrFldHasNoValue(record, fldName, otherFldVal);

	}
	
	/**
	 * Computer File format tests
	 */
@Test
	public final void testComputerFile()
	{
		String fldVal = Format.COMPUTER_FILE.toString();
		
		// leader/06 m 008/26 u - other (not data)
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cmd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu        u  000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);

		// leader/06 m 008/26 is not a - other (not dataset)
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01529cmi a2200397Ia 4500"));
		cf008.setData("081215c200u9999xx         b        eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
	}

	/**
	 * Computer File and Database -- if both formats, and it is only an
	 *  online resource, then it is NOT a computer file.
	 */
@Test
	public final void testDatabaseAndComputerFile()
	{
		Leader LEADER = factory.newLeader("02441cms a2200517 a 4500");
		cf008.setData("920901d19912002pauuu1n    m  0   a0eng  ");

		// online copy only
		Record record = factory.newRecord();
		record.setLeader(LEADER);
		record.addVariableField(cf008);
		record.addVariableField(df999dbaz);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, dbazVal);

		// both physical copy and online copy
		DataField df999physical = factory.newDataField("999", ' ', ' ');
		df999physical = factory.newDataField("999", ' ', ' ');
		df999physical.addSubfield(factory.newSubfield('a', "F152 .A28"));
		df999physical.addSubfield(factory.newSubfield('w', "LC"));
		df999physical.addSubfield(factory.newSubfield('i', "36105018746623"));
		df999physical.addSubfield(factory.newSubfield('l', "HAS-DIGIT"));
		df999physical.addSubfield(factory.newSubfield('m', "GREEN"));
		record.addVariableField(df999physical);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 2);
		solrFldMapTest.assertSolrFldValue(record, fldName, Format.COMPUTER_FILE.toString());
		solrFldMapTest.assertSolrFldValue(record, fldName, dbazVal);

		// can't have physical copy only or it wouldn't be a database
	}


	/**
	 * Conference Proceedings format tests - Conf Proc is now a Genre
	 */
@Test
	public final void testConferenceProceedingsIsGone()
	{
	    String fldVal = FormatOld.CONFERENCE_PROCEEDINGS.toString();

		// test 650|v Congresses
		Record rec = factory.newRecord();
	    rec.setLeader(factory.newLeader("04473caa a2200313Ia 4500"));
	    cf008.setData("040202s2003    fi g     b    000 0deng d");
	    rec.addVariableField(cf008);
	    DataField df650 = factory.newDataField("650", ' ', '0');
	    df650.addSubfield(factory.newSubfield('a', "Music"));
	    df650.addSubfield(factory.newSubfield('v', "Congresses."));
	    rec.addVariableField(df650);
	    solrFldMapTest.assertSolrFldHasNoValue(rec, fldName, fldVal);

		// test 600|v Congresses
		rec = factory.newRecord();
	    rec.setLeader(factory.newLeader("04473caa a2200313Ia 4500"));
	    cf008.setData("040202s2003    fi g     b    000 0deng d");
	    rec.addVariableField(cf008);
	    DataField df600 = factory.newDataField("600", '1', '0');
	    df600.addSubfield(factory.newSubfield('a', "Sibelius, Jean,"));
	    df600.addSubfield(factory.newSubfield('d', "1865-1957"));
	    df600.addSubfield(factory.newSubfield('v', "Congresses."));
	    rec.addVariableField(df600);
	    solrFldMapTest.assertSolrFldHasNoValue(rec, fldName, fldVal);

	    // test LeaderChar07 = m and 008/29 = 1
		rec = factory.newRecord();
	    rec.setLeader(factory.newLeader("04473cam a2200313Ia 4500"));
	    cf008.setData("040202s2003    fi g     b    100 0deng d");
	    rec.addVariableField(cf008);
	    solrFldMapTest.assertSolrFldHasNoValue(rec, fldName, fldVal);

	    // test LeaderChar07 = s and 008/29 = 1
	    rec = factory.newRecord();
	    rec.setLeader(factory.newLeader("04473cas a2200313Ia 4500"));
	    cf008.setData("040202s2003    fi g     b    100 0deng d");
	    rec.addVariableField(cf008);
	    solrFldMapTest.assertSolrFldHasNoValue(rec, fldName, fldVal);

	}

	/**
	 * test format population of Datasets
	 */
@Test
	public final void testDataset()
	{
		String datasetVal = Format.DATASET.toString();

		// leader/06 m 008/26 a - data
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cmd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu        a  000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, datasetVal);

		// leader/06 m 008/26 a - data
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01529cmi a2200397Ia 4500"));
		cf008.setData("081215c200u9999xx         a        eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, datasetVal);
	}


/**
 * test format population of Equipment - if 914$a = "EQUIP"
 */
@Test
public final void testEquipment()
{
	Leader LEADER = factory.newLeader("02441cms a2200517 a 4500");
	cf008.setData("920901d19912002pauuu1n    m  0   a0eng  ");

	// Equipment
	Record record = factory.newRecord();
	record.setLeader(LEADER);
	record.addVariableField(cf008);
	DataField df914 = factory.newDataField("914", ' ', ' ');
	df914.addSubfield(factory.newSubfield('a', "EQUIP"));
	record.addVariableField(df914);
	DataField df999 = factory.newDataField("999", ' ', ' ');
	df999.addSubfield(factory.newSubfield('a', "F152 .A28"));
	df999.addSubfield(factory.newSubfield('w', "LC"));
	df999.addSubfield(factory.newSubfield('i', "36105018746623"));
	df999.addSubfield(factory.newSubfield('l', "HAS-DIGIT"));
	df999.addSubfield(factory.newSubfield('m', "GREEN"));
	record.addVariableField(df999);
	solrFldMapTest.assertSolrFldValue(record, fldName, Format.EQUIPMENT.toString());
	//If it has a format of Equipment, it shouldn't have a format of 3D object
	solrFldMapTest.assertSolrFldHasNoValue(record, fldName, Format.OBJECT_3D.toString());

	// not Equipment
	Record recordnot = factory.newRecord();
	recordnot.setLeader(factory.newLeader("02808cas a22005778a 4500"));
	cf008.setData("050127c20149999enkfr p       |   a0eng c");
	recordnot.addVariableField(cf008);
	DataField df914not = factory.newDataField("914", ' ', ' ');
	df914not.addSubfield(factory.newSubfield('a', "JUNK"));
	recordnot.addVariableField(df914not);
	DataField df999not = factory.newDataField("999", ' ', ' ');
	df999not.addSubfield(factory.newSubfield('a', "F152 .A28"));
	df999not.addSubfield(factory.newSubfield('w', "LC"));
	df999not.addSubfield(factory.newSubfield('i', "36105018746623"));
	df999not.addSubfield(factory.newSubfield('l', "HAS-DIGIT"));
	df999not.addSubfield(factory.newSubfield('m', "GREEN"));
	recordnot.addVariableField(df999not);
	solrFldMapTest.assertSolrFldHasNoValue(recordnot, fldName, Format.EQUIPMENT.toString());

	// no 914$a
	Record recordnone = factory.newRecord();
	recordnone.setLeader(LEADER);
	recordnone.addVariableField(cf008);
	DataField df999none = factory.newDataField("999", ' ', ' ');
	df999none.addSubfield(factory.newSubfield('a', "F152 .A28"));
	df999none.addSubfield(factory.newSubfield('w', "LC"));
	df999none.addSubfield(factory.newSubfield('i', "36105018746623"));
	df999none.addSubfield(factory.newSubfield('l', "HAS-DIGIT"));
	df999none.addSubfield(factory.newSubfield('m', "GREEN"));
	recordnone.addVariableField(df999none);
	solrFldMapTest.assertSolrFldHasNoValue(recordnot, fldName, Format.EQUIPMENT.toString());

}

/**
	 * Image format tests
	 */
@Test
	public final void testImage()
	{
		String imageVal = Format.IMAGE.toString();
		
		// leader/06 k 008/33 i - image
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01952ckd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 i eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, imageVal);

		// leader/06 k 008/33 k - image
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952ckd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 k eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, imageVal);

		// leader/06 k 008/33 p - image
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952ckd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 p eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, imageVal);

		// leader/06 k 008/33 s - image
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952ckd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 s eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, imageVal);

		// leader/06 k 008/33 t - image
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952ckd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 t eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, imageVal);
	}

	/**
	 * Journal/Periodical format tests
	 */
@Test
	public final void testJournalPeriodicalFormat()
	{
     	// leader/06 a /07 s 008/21 blank - serial publication
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cas  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 0 eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		// leader/07 s 008/21 blank, 65x sub v "Periodicals" - serial publication
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01823cas a22004457a 4500"));
		cf008.setData("961105d19961996dcuuuu       f0    0eng d");
		record.addVariableField(cf008);
		DataField df650 = factory.newDataField("650", ' ', '0');
		df650.addSubfield(factory.newSubfield('a', "Industrial statistics"));
		df650.addSubfield(factory.newSubfield('v', "Periodicals."));
		record.addVariableField(df650);
		DataField df651 = factory.newDataField("650", ' ', '0');
		df651.addSubfield(factory.newSubfield('a', "United States"));
		df651.addSubfield(factory.newSubfield('v', "Periodicals."));
		record.addVariableField(df651);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		// leader/07 b, 006/00 s, 006/04 blank, 008/21 blank - serial publication
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01823cab a22004457a 4500"));
		cf006.setData("s        h        ");
		record.addVariableField(cf006);
		cf008.setData("961105d19961996dcuuuu       f0    0eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		// leader/07 b, 006/00 s, 008/21 m - serial publication
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cab  2200457Ia 4500"));
		cf006.setData("s        h        ");
		record.addVariableField(cf006);
		cf008.setData("780930m19391944nyu   m       000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		// leader/07 b, 006/00 s, 008/21 p - Serial Publication
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cab  2200457Ia 4500"));
		cf006.setData("s        h        ");
		record.addVariableField(cf006);
		cf008.setData("780930m19391944nyu   p       000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		// format serial publication:  leader/07 s and 008/21 blank (ignore LCPER in 999w)
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01247cas a2200337 a 4500"));
		cf008.setData("830415c19809999vauuu    a    0    0eng  ");
		record.addVariableField(cf008);
		DataField df999 = factory.newDataField("999", ' ', ' ');
		df999.addSubfield(factory.newSubfield('a', "E184.S75 R47A V.1 1980"));
		df999.addSubfield(factory.newSubfield('w', "LCPER"));
		df999.addSubfield(factory.newSubfield('i', "36105007402873"));
		df999.addSubfield(factory.newSubfield('l', "STACKS"));
		df999.addSubfield(factory.newSubfield('m', "GREEN"));
		record.addVariableField(df999);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);
		
		// format Serial Publication:  leader/07 s and 008/21 blank (ignore DEWEYPER in 999w)
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01247cas a2200337 a 4500"));
		cf008.setData("830415c19809999vauuu    a    0    0eng  ");
		record.addVariableField(cf008);
		df999 = factory.newDataField("999", ' ', ' ');
		df999.addSubfield(factory.newSubfield('a', "666.27 .F22"));
		df999.addSubfield(factory.newSubfield('w', "DEWEYPER"));
		df999.addSubfield(factory.newSubfield('i', "36105007402873"));
		df999.addSubfield(factory.newSubfield('l', "STACKS"));
		df999.addSubfield(factory.newSubfield('m', "GREEN"));
		record.addVariableField(df999);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		// leader/07 s, no 006, 008/21 p - journal
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cas  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu   p       000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);
		
		// Journal: leader/07 s 008/21 d, 006/00 s 006/04 p
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01068cas a2200277 a 4500"));
		cf006.setData("s   p    h        ");
		record.addVariableField(cf006);
		cf008.setData("030807c20029999nyufx         0    0eng c");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		// Other: leader/07 s 008/21 d, 006/00 s 006/04 d
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01068cas a2200277 a 4500"));
		cf006.setData("s   d    h        ");
		record.addVariableField(cf006);
		cf008.setData("030807c20029999nyufx d       0    0eng c");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldHasNoValue(record, fldName, journalVal);

		// format Other should be access online leader/07 s, 006/00 m, 008/21 |
		// we are favoring anything in 008/21  over  006/00
		record = factory.newRecord();
		record.setLeader(factory.newLeader("00988nas a2200193z  4500"));
		cf006.setData("m        d        ");
		record.addVariableField(cf006);
		cf008.setData("071214uuuuuuuuuxx uu |ss    u|    |||| d");
		record.addVariableField(cf008);
		record.addVariableField(df956sfx);
		df999 = factory.newDataField("999", ' ', ' ');
		df999.addSubfield(factory.newSubfield('a', "INTERNET RESOURCE"));
		df999.addSubfield(factory.newSubfield('w', "ASIS"));
		df999.addSubfield(factory.newSubfield('i', "7117119-1001"));
		df999.addSubfield(factory.newSubfield('l', "INTERNET"));
		df999.addSubfield(factory.newSubfield('t', "SUL"));
		record.addVariableField(df999);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		// No 006
		// 008 byte 21 is p  (Journal / periodical)
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02808cas a22005778a 4500"));
		cf008.setData("050127c20149999enkfr p       |   a0eng c");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		// 008 byte 21 is blank
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02393cas a2200421Ki 4500"));
		cf008.setData("130923c20139999un uu         1    0ukr d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		// 008 byte 21 is | (pipe)  Journal
		record = factory.newRecord();
		record.setLeader(factory.newLeader("00756nas a22002175a 4500"));
		cf008.setData("110417s2011    le |||||||||||||| ||ara d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		// have 006

		// 006 byte 4 is p
		record = factory.newRecord();
		record.setLeader(factory.newLeader("03163cas a2200553 a 4500"));
		cf006.setData("ser p       0    0");
		record.addVariableField(cf006);
		cf008.setData("000000d197819uuilunnn         l    eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		// 006 byte 4 is blank
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02393cas a2200421Ki 4500"));
		cf008.setData("130923c20139999un uu         1    0ukr d");
		record.addVariableField(cf008);
		cf006.setData("ser         0    0");
		record.addVariableField(cf006);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		// 006 byte 4 is pipe
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02393cas a2200421Ki 4500"));
		cf008.setData("130923c20139999un uu         1    0ukr d");
		record.addVariableField(cf008);
		cf006.setData("suu wss|||||0   |2");
		record.addVariableField(cf006);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

// FIXME:  Not sure what to do with these double formats
//		// recording and journal
//		record = factory.newRecord();
//		record.setLeader(factory.newLeader("03163cis a2200553 a 4500"));
//		cf006.setData("ser p       0    0");
//		record.addVariableField(cf006);
//		cf008.setData("000000d197819uuilunnn         l    eng d");
//		record.addVariableField(cf008);
////		solrFldMapTest.assertSolrFldValue(record, fldName, journalFldVal);
//
//		// recording and conf proceedings
//		record = factory.newRecord();
//		record.setLeader(factory.newLeader("03701cim a2200421 a 4500"));
//		cf006.setData("sar         1    0");
//		record.addVariableField(cf006);
//		cf008.setData("040802c200u9999cau            l    eng d");
//		record.addVariableField(cf008);
////		solrFldMapTest.assertSolrFldValue(record, fldName, journalFldVal);
//
//		// score and database and journal
//		record = factory.newRecord();
//		record.setLeader(factory.newLeader("02081cci a2200385 a 4500"));
//		cf006.setData("m        d        ");
//		record.addVariableField(cf006);
//		cf006.setData("suu wss|||||0   |2");
//		record.addVariableField(cf006);
//		cf008.setData("050921c20039999iluuus ss0     n   2eng  ");
//		record.addVariableField(cf008);
////		solrFldMapTest.assertSolrFldValue(record, fldName, journalFldVal);
	}



	/**
	 * Manuscript/Archive format tests
	 */
@Test
	public final void testManuscriptArchive()
	{
		String fldVal = Format.MANUSCRIPT_ARCHIVE.toString();
		
		// leader/06 b (obsolete) - manuscript/archive
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cbd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);

		// leader/06 p mixed materials) - manuscript/archive
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cpd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
	}

	/**
	 * Map/Globe format tests
	 */
@Test
	public final void testMapGlobe()
	{
		String fldVal = Format.MAP.toString();
		
		// leader/06 e - globe
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01952ced  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 0 eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
		
		// leader/06 f - map
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cfd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 0 eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
	}

	/**
	 * test format of Journal for Marcit source records (per 590)
	 */
@Test
	public final void testMarcit()
	{
		Leader LEADER = factory.newLeader("00838cas a2200193z  4500");

		Record record = factory.newRecord();
		record.setLeader(LEADER);
		DataField df = factory.newDataField("590", ' ', ' ');
        df.addSubfield(factory.newSubfield('a', "MARCit brief record."));
        record.addVariableField(df);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);
		// without period
		record = factory.newRecord();
		record.setLeader(LEADER);
		df = factory.newDataField("590", ' ', ' ');
        df.addSubfield(factory.newSubfield('a', "MARCit brief record"));
        record.addVariableField(df);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		String otherVal = Format.OTHER.toString();
		// wrong string in 590
		record = factory.newRecord();
		record.setLeader(LEADER);
		df = factory.newDataField("590", ' ', ' ');
		df.addSubfield(factory.newSubfield('a', "incorrect string"));
        record.addVariableField(df);
		solrFldMapTest.assertSolrFldValue(record, fldName, otherVal);
		record = factory.newRecord();
		record.setLeader(LEADER);
		df = factory.newDataField("590", ' ', ' ');
		df.addSubfield(factory.newSubfield('a', "something MARCit something"));
        record.addVariableField(df);
		solrFldMapTest.assertSolrFldValue(record, fldName, otherVal);

		// marcit in wrong field
		record = factory.newRecord();
		record.setLeader(LEADER);
		df = factory.newDataField("580", ' ', ' ');
        df.addSubfield(factory.newSubfield('a', "MARCit brief record."));
        record.addVariableField(df);
		solrFldMapTest.assertSolrFldValue(record, fldName, otherVal);
	}


	/**
	 * Microformat format tests - Microformats are now Physical Format types
	 */
@Test
	public final void testMicroformatIsGone()
	{
		String microformatVal = FormatOld.MICROFORMAT.toString();
		
		// 245 h has "microform" - microfilm AND music-score
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01952adm  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 v eng d");
		record.addVariableField(cf008);
		DataField df245 = factory.newDataField("245", '1', '0');
		df245.addSubfield(factory.newSubfield('a', "microform: 245h"));
		df245.addSubfield(factory.newSubfield('c', "stuff."));
		df245.addSubfield(factory.newSubfield('h', "[microform]"));
		record.addVariableField(df245);
		solrFldMapTest.assertSolrFldHasNoValue(record, fldName, microformatVal);

		// 999 ALPHANUM starting with MFLIM
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01669nam a2200289ua 4500"));
		cf008.setData("870715r19741700ctu     a     000 0 eng d");
		record.addVariableField(cf008);
		DataField df999 = factory.newDataField("999", ' ', ' ');
		df999.addSubfield(factory.newSubfield('a', "MFILM N.S. 1350 REEL 230 NO. 3741"));
		df999.addSubfield(factory.newSubfield('w', "ALPHANUM"));
		df999.addSubfield(factory.newSubfield('i', "001AFX2969"));
		df999.addSubfield(factory.newSubfield('l', "MEDIA-MTXT"));
		df999.addSubfield(factory.newSubfield('m', "GREEN"));
		record.addVariableField(df999);
		solrFldMapTest.assertSolrFldHasNoValue(record, fldName, microformatVal);
		
		// 999 ALPHANUM starting with MFICHE
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01879cam a2200409 i 4500"));
		cf008.setData("101015q20092010fr a    bbm   000 0 fre c");
		record.addVariableField(cf008);
		df999 = factory.newDataField("999", ' ', ' ');
		df999.addSubfield(factory.newSubfield('a', "MFICHE 3239"));
		df999.addSubfield(factory.newSubfield('w', "ALPHANUM"));
		df999.addSubfield(factory.newSubfield('i', "8729402-1001"));
		df999.addSubfield(factory.newSubfield('l', "MEDIA-MTXT"));
		df999.addSubfield(factory.newSubfield('m', "GREEN"));
		record.addVariableField(df999);
		solrFldMapTest.assertSolrFldHasNoValue(record, fldName, microformatVal);
	}

	/**
	 * Music Recording format tests
	 */
@Test
	public final void testMusicRecording()
	{
		String fldVal = Format.MUSIC_RECORDING.toString();

		// music-audio: leader/06 j
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cjd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
	}

	/**
	 * Music Score format tests
	 */
@Test
	public final void testMusicScore()
	{
		String fldVal = Format.MUSIC_SCORE.toString();
		
		// leader/06 c - music-score
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01952ccd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
		
		// leader/06 d - music-score
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cdd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
		
		// 245 h has "microform" - microfilm AND music-score
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952adm  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 v eng d");
		record.addVariableField(cf008);
		DataField df245 = factory.newDataField("245", '1', '0');
		df245.addSubfield(factory.newSubfield('a', "microform: 245h"));
		df245.addSubfield(factory.newSubfield('c', "stuff."));
		df245.addSubfield(factory.newSubfield('h', "[microform]"));
		record.addVariableField(df245);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
	}

	/**
	 * Newspaper format tests
	 */
@Test
	public final void testNewspaper()
	{
        String fldVal = Format.NEWSPAPER.toString();

        // leader/07 s 008/21 n - newspaper
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cas  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu   n       000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);

		// leader/07 s, no 006, 008/21 n - newspaper
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cas  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu   n       000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);

		// FIXME:  presumably has 008 blank and 006 with byte 004 of n ...
// we are favoring anything in 008/21  over  006/00
/*		  <!-- Newspaper per 006/00 s 006/04 n -->
		  <record>
		    <leader>01068cas a2200277 a 4500</leader>
		    <controlfield tag="001">a334455</controlfield>
		    <controlfield tag="006">s   n    h        </controlfield>
		    <controlfield tag="008">030807c20029999nyufx         0    0eng c</controlfield>
		  </record>*/
//		solrFldMapTest.assertSolrFldValue(testFilePath, "334455", fldName, fldVal);

		// leader/07 b, 006/00 s, 006/04 w, 008/21 n - Other
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cab  2200457Ia 4500"));
		cf006.setData("s   w    h        ");
		record.addVariableField(cf006);
		cf008.setData("780930m19391944nyu   n       000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldHasNoValue(record, fldName, fldVal);

// FIXME:  we are checking to see if there are many differences between the old Journal/Newspaper algorithm and the new ...
// also,  we are favoring anything in 008/21  over  006/00
//		// 006 byte 4 is p
//		Record record = factory.newRecord();
//		record.setLeader(factory.newLeader("03163cas a2200553 a 4500"));
//		cf006.setData("ser n       0    0");
//		record.addVariableField(cf006);
//		cf008.setData("000000d197819uuilunnn         l    eng d");
//		record.addVariableField(cf008);
//		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
	}

	/**
	 * Thesis format tests - Thesis is now a Genre
	 */
@Test
	public final void testThesisIsGone()
	{
		String fldVal = FormatOld.THESIS.toString();
		
		// 502 exists - thesis
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cad  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 v eng d");
		record.addVariableField(cf008);
		DataField df502 = factory.newDataField("502", ' ', ' ');
		df502.addSubfield(factory.newSubfield('a', "dissertation note field; we don't care about the contents"));
		record.addVariableField(df502);
		solrFldMapTest.assertSolrFldHasNoValue(record, fldName, fldVal);
	}

	/**
	 * INDEX-14 updating database being folded into Database_A_Z
	 * regardless of the existence of sfx urls
	 */
@Test
	public final void testUpdatingDatabase()
	{

		// based on 9366507 - integrating, SFX
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("02018cai a2200397Ia 4500"));
		cf008.setData("120203c20089999enkwr d ob    0    2eng d");
		record.addVariableField(cf008);
		record.addVariableField(df956sfx);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, dbazVal);

		// based on 6735313 - integrating, no SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01622cai a2200397 a 4500"));
		cf008.setData("061227c20069999vau x dss    f0    2eng c");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, dbazVal);

		// integrating, db a-z
		record.addVariableField(df999dbaz);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, dbazVal);

		// based on 8774277 - serial, SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02056cas a2200445Ii 4500"));
		cf008.setData("101110c20099999nz ar d o    f0    0eng d");
		record.addVariableField(cf008);
		record.addVariableField(df956sfx);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, dbazVal);

		// serial, no SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01548cas a2200361Ia 4500"));
		cf008.setData("061227c20069999vau x dss    f0    2eng c");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, dbazVal);

		// serial, db a-z
		record.addVariableField(df999dbaz);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, dbazVal);
	}

	/**
	 * Updating Looseleaf can be a serial or integrating resource.
	 * Both should be assigned to format Book
	 */
@Test
	public final void testUpdatingLooseleaf()
	{
		String bookVal = Format.BOOK.toString();
		// based on 7911837 - integrating
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("02444cai a2200433 a 4500"));
		cf008.setData("090205c20089999nyuuu l   b   0   a2eng c");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, bookVal);

		// serial
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02444cas a2200433 a 4500"));
		cf008.setData("090205c20089999nyuuu l   b   0   a2eng c");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, bookVal);
	}

	/**
	 * Updating Website can be a serial or integrating resource.
	 * If they have an SFX url, then we will call them a journal.
	 * INDEX-16 Regardless of sfx url, Updating Website will be Journal/Periodical
	 */
@Test
	public final void testUpdatingWebsite()
	{
		// based on 10094805 - integrating, SFX
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("02015cai a2200385 a 4500"));
		cf008.setData("130110c20139999enk|| woo     0    2eng  ");
		record.addVariableField(cf008);
		record.addVariableField(df956sfx);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		// based on 8541457 - integrating, no SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01548cai a2200361Ia 4500"));
		cf008.setData("040730d19uu2012dcuar w os   f0    2eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		// integrating, db a-z
		record.addVariableField(df999dbaz);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 2);
		solrFldMapTest.assertSolrFldValue(record, fldName, dbazVal);

		// serial, SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02015cas a2200385 a 4500"));
		cf008.setData("130110c20139999enk|| woo     0    2eng  ");
		record.addVariableField(cf008);
		record.addVariableField(df956sfx);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		// serial, no SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01548cas a2200361Ia 4500"));
		cf008.setData("040730d19uu2012dcuar w os   f0    2eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);

		// serial, db a-z
		record.addVariableField(df999dbaz);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 2);
		solrFldMapTest.assertSolrFldValue(record, fldName, dbazVal);

		// leader/07 b, 006/00 s, 006/04 w, 008/21 n - Other -->
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cab  2200457Ia 4500"));
		cf006.setData("s   w    h        ");
		record.addVariableField(cf006);
		cf008.setData("780930m19391944nyu   n       000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);
		
		// leader/07 s, no 006, 008/21 w - other (web site)
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cas  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu   w       000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);
		
		// leader/07 b, 006/00 s, 008/21 w - other (web site)
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cab  2200457Ia 4500"));
		cf006.setData("s   w    h        ");
		record.addVariableField(cf006);
		cf008.setData("780930m19391944nyu   w       000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalVal);
	}

	/**
	 * Updating Other can be a serial or integrating resource.
	 * If they have an SFX url, then we will call them a journal.
	 *  INDEX-15 updating other (default) being folded into Book
	 */
@Test
	public final void testUpdatingOther()
	{
		String bookVal = Format.BOOK.toString();

		// based on 9539608 - integrating, SFX
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("02085cai a2200325 a 4500"));
		cf008.setData("111014c20119999enk|| p o     |    2eng c");
		record.addVariableField(cf008);
		record.addVariableField(df956sfx);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, bookVal);

		// based on 10182766k - integrating, no SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01579cai a2200337Ia 4500"));
		cf008.setData("081215c200u9999xx         a        eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, bookVal);

		// integrating, db a-z
		record.addVariableField(df999dbaz);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 2);
		solrFldMapTest.assertSolrFldValue(record, fldName, dbazVal);

		// serial, SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02085cas a2200325 a 4500"));
		cf008.setData("111014c20119999enk|| q o     |    2eng c");
		record.addVariableField(cf008);
		record.addVariableField(df956sfx);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, bookVal);

		// serial, no SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02085cas a2200325 a 4500"));
		cf008.setData("111014c20119999enk|| q o     |    2eng c");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, bookVal);

		// serial, db a-z
		record.addVariableField(df999dbaz);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 2);
		solrFldMapTest.assertSolrFldValue(record, fldName, dbazVal);
	}

	/**
	 * Video format tests
	 */
@Test
	public final void testVideo()
	{
		String fldVal = Format.VIDEO.toString();
		
		// leader/06 g 008/33 m - video
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cgd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 m eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
		
		// leader/06 g 008/33 v - video
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cgd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
	}

	/**
	 * Test assignment of Other format
	 */
@Test
	public final void testOtherFormat()
	{
        String fldVal = Format.OTHER.toString();

        // leader/06 t /07 b - other (not book)
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01952ctb  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 0 eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
		
		// leader/06 k 008/33 w - other  (not image)
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952ckd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 w eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
		
		// leader/06 g 008/33 w - other (not video)
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cgd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 w eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
				
		// 006/00 s /04 w
// FIXME:  temporary for format redo
		/*
		 *  <!-- leader/07 b, 006/00 s, 006/04 w, 008/21 n - Other -->
  			<record>
    				<leader>01952cab  2200457Ia 4500</leader>
    				<controlfield tag="001">aleader07b00600s00821n</controlfield>
    				<controlfield tag="006">s   w    h        </controlfield>
    				<controlfield tag="008">780930m19391944nyu   n       000 v eng d</controlfield>
  			</record>
		 */
//		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821n", fldName, fldVal);
		
		// instructional kit leader/06 o - other (instructional kit)
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01952cod  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
	}

	/**
	 * INDEX-18 implement 3D object resource type
	 * Test assignment of 3D object format
	 */
	@Test
	public final void test3dObjectFormat()
	{
		String fldVal = Format.OBJECT_3D.toString();

		// leader/06 r - 3D Object
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01952crd  2200457Ia 4500"));
		cf008.setData("780930m19391944nyu           000 v eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
	}
	
	/**
	 * test format population based on ALPHANUM field values from 999
	 */
@Test
	public final void testFormatsFrom999()
	{
		// 999 ALPHANUM starting with MCD
// currently not using callnums for MCD, or DVD
		/*
		 *     <!-- ALPHANUM MCD -->
    <record>
        <leader>01669nam a2200289ua 4500</leader>
        <controlfield tag="001">a1234673</controlfield>
        <controlfield tag="008">870715r19741700ctu     a     000 0 eng d</controlfield>
        <datafield tag="999" ind1=" " ind2=" ">
            <subfield code="a">MCD Brendel Plays Beethoven's Eroica variations</subfield>
            <subfield code="w">ALPHANUM</subfield>
            <subfield code="i">001AFX2969</subfield>
            <subfield code="l">MEDIA-MTXT</subfield>
            <subfield code="m">GREEN</subfield>
        </datafield>
    </record>
		 */
//		solrFldMapTest.assertSolrFldValue(testFilePath, "1234673", fldName, Format.MUSIC_RECORDING.toString());
	}

}
