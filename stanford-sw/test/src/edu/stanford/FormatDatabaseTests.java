package edu.stanford;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.*;

import edu.stanford.enumValues.Format;

/**
 * junit4 tests for Stanford University format field for blacklight index
 * @author Naomi Dushay
 * @author Laney McGlohon
 */
public class FormatDatabaseTests extends AbstractStanfordTest {

	String testFilePath = testDataParentPath + File.separator + "formatDatabaseTests.xml";
	String fldName = "format_main_ssim";
	String dbAZval = Format.DATABASE_A_Z.toString();
	String otherVal = Format.OTHER.toString();
	MarcFactory factory = MarcFactory.newInstance();

@Before
	public final void setup()
	{
		mappingTestInit();
	}

	/**
	 * test format value Database A-Z population based on item type from 999
	 */
@Test
	public final void testDatabaseAZ()
	{
		// when it has no other format (would have been "Other"), then Database is the only value
		solrFldMapTest.assertSolrFldValue(testFilePath, "one999db", fldName, dbAZval);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "one999db", fldName, otherVal);

		 // INDEX-15 updating other (default) being folded into Book
		 Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01541cai a2200349Ia 4500"));
		ControlField cf008 = factory.newControlField("008");
		cf008.setData("100315c20109999vau---        o   vleng d");
		record.addVariableField(cf008);
		DataField df245 = factory.newDataField("245", '0', '0');
		df245.addSubfield(factory.newSubfield('a', "one 999 not database (was format Other)"));
		df245.addSubfield(factory.newSubfield('b', "Scopus"));
		record.addVariableField(df245);
		DataField df999 = factory.newDataField("999", ' ', ' ');
		df999.addSubfield(factory.newSubfield('a', "INTERNET RESOURCE"));
		df999.addSubfield(factory.newSubfield('w', "ALPHANUM"));
		df999.addSubfield(factory.newSubfield('c', "1"));
		df999.addSubfield(factory.newSubfield('i', "8545966-1001"));
		df999.addSubfield(factory.newSubfield('l', "INTERNET"));
		df999.addSubfield(factory.newSubfield('m', "SUL"));
		record.addVariableField(df999);
		solrFldMapTest.assertSolrFldValue(record, fldName, Format.BOOK.toString());
		solrFldMapTest.assertSolrFldHasNoValue(record, fldName, dbAZval);

		solrFldMapTest.assertSolrFldValue(testFilePath, "dbVideo", fldName, dbAZval);
		solrFldMapTest.assertSolrFldValue(testFilePath, "dbVideo", fldName, Format.VIDEO.toString());

		solrFldMapTest.assertSolrFldValue(testFilePath, "dbMusicRecording", fldName, dbAZval);
		solrFldMapTest.assertSolrFldValue(testFilePath, "dbMusicRecording", fldName, Format.MUSIC_RECORDING.toString());

		solrFldMapTest.assertSolrFldValue(testFilePath, "mult999oneDb", fldName, dbAZval);
		solrFldMapTest.assertSolrFldValue(testFilePath, "mult999oneDb", fldName, Format.JOURNAL_PERIODICAL.toString());

		solrFldMapTest.assertSolrFldValue(testFilePath, "two99oneShadowWasOther", fldName, dbAZval);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "two99oneShadowWasOther", fldName, otherVal);

		solrFldMapTest.assertSolrFldValue(testFilePath, "DBandMusicRecOne999", fldName, dbAZval);
		solrFldMapTest.assertSolrFldValue(testFilePath, "DBandMusicRecOne999", fldName, Format.MUSIC_RECORDING.toString());

		solrFldMapTest.assertSolrFldValue(testFilePath, "otherBecomesDB", fldName, dbAZval);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "otherBecomesDB", fldName, otherVal);

		solrFldMapTest.assertSolrFldValue(testFilePath, "dbOther", fldName, dbAZval);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "dbOther", fldName, otherVal);

		solrFldMapTest.assertSolrFldValue(testFilePath, "nother", fldName, dbAZval);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "nother", fldName, otherVal);

	}

	/**
	 * test the additional Database format values aren't assigned
	 */
@Test
	public final void testDatabaseAZOnly()
			throws IOException, SAXException, ParserConfigurationException, SolrServerException
	{
		createFreshIx("formatDatabaseTests.xml");
		assertZeroResults(fldName, "\"Database (Other)\"");
		assertZeroResults(fldName, "\"Database (All)\"");
	}

}
