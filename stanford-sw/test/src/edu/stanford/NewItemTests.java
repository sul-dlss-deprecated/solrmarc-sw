package edu.stanford;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.marc4j.marc.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University
 * Retrieve date cataloged from 916$b to use for new item feed
 * Format of 916 is 
 * 916   |aDATE CATALOGED|b20150504 (YYYYMMDD)
 *     OR
 * 916   |aDATE CATALOGED|bNEVER
 * @author Laney McGlohon
 */
public class NewItemTests extends AbstractStanfordTest
{

	static String testDataFname = "newItemsDateCataloged.xml";
	String testFilePath = testDataParentPath + File.separator + testDataFname;

	private final String fldName = "date_cataloged";
	MarcFactory factory = MarcFactory.newInstance();

@Before
	public final void setup()
	{
		mappingTestInit();
	}

/**
* If 916$b is never or there isn't a 916, there shouldn't be a date_cataloged field in the record
*/
@Test
	public final void testDateCatalogedNever()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		// index test records
		createFreshIx(testDataFname);

		// Value of NEVER
		solrFldMapTest.assertNoSolrFld(testFilePath, "7000010", fldName);

		// No 916 at all
		solrFldMapTest.assertNoSolrFld(testFilePath, "7000023", fldName);
	}

/**
* If 916$b is a date, there should be a date_cataloged field in the record in the format of
* YYYY-MM-DDT00:00:00Z
*/
@Test
	public final void testDateCatalogedDate()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		// index test records
		createFreshIx(testDataFname);

		solrFldMapTest.assertSolrFldValue(testFilePath, "7000011", fldName, "2007-11-08T00:00:00Z");
	}

}
