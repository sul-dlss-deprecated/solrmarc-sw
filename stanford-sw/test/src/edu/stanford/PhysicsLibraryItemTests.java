package edu.stanford;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.marc4j.marc.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University
 *   Remove Physics library as a building facet value  SW-849
 * 1. if a record only has library = Physics and *no* item has location PHYSTEMP, then do NOT index the record.
 * 2. if a record only has library = Physics and at least one item has location PHYSTEMP, then index the record but:
 *    only include items with location PHYSTEMP
 *    do not assign a library facet value (treat like an online record)
 * 3. if a record has multiple libraries, and one of them is Physics, then
 *    do not assign a library facet value for Physics items (treat like online records)
 *    for Physics items, only include the items if they have location PHYSTEMP (and treat as online item)
 * @author Laney McGlohon
 */
public class PhysicsLibraryItemTests extends AbstractStanfordTest
{

	static String testDataFname = "physicsRemoval.xml";
	String testFilePath = testDataParentPath + File.separator + testDataFname;

	private final String fldName = "building_facet";
	MarcFactory factory = MarcFactory.newInstance();

@Before
	public final void setup()
	{
		mappingTestInit();
	}

     /**
      *  Required tests based upon values in 999:
      *  1.  PHYSICS and PHYSTEMP => no building facet but index, so item_display field exists
      *  2.  PHYSICS and not PHYSTEMP => don't index and no items
      *  3.  GREEN and STACKS => building facet, index, so item_display field exists
      *  4.  PHYSICS and PHYSTEMP 			=> no building facet but index, so item_display field exists
      *       PHYSICS and not PHYSTEMP    => don't index and no item
      *  5.  PHYSICS and PHYSTEMP
      *       PHYSICS and PHYSTEMP => no building facet but index both, so two item_display fields
      *  6.  PHYSICS and not PHYSTEMP
      *       PHYSICS and not PHYSTEMP => don't index and no items
      *  7.  PHYSICS and PHYSTEMP
      *       GREEN and STACKS       => Green building facet, index both, so two item_display fields
      *  8.  PHYSICS and not PHYSTEMP
      *       GREEN and STACKS       => Green building facet, index this one only, so one item_display field exists
      */
@Test
	public void testNoBuildingFacet()
	{
    	// Test case #3
		Record rec = factory.newRecord();
		rec.setLeader(factory.newLeader("04473caa a2200313Ia 4500"));
		ControlField cf = factory.newControlField("001", "anoPhysics");
		rec.addVariableField(cf);

		DataField df999 = factory.newDataField("999", '0', '0');
		df999.addSubfield(factory.newSubfield('a', "DO1 .A8133 V.402"));
		df999.addSubfield(factory.newSubfield('w', "LC"));
		df999.addSubfield(factory.newSubfield('c', "1"));
		df999.addSubfield(factory.newSubfield('i', "36105210713652"));
		df999.addSubfield(factory.newSubfield('l', "STACKS"));
		df999.addSubfield(factory.newSubfield('m', "GREEN"));
		df999.addSubfield(factory.newSubfield('t', "STKS"));
		rec.addVariableField(df999);

		// Building_facet value is Green
		solrFldMapTest.assertSolrFldValue(rec, fldName, "Green");

		// Actual item (8230276) 999 a| QB1 .A8133 V.402 w| LC c| 1 i| 36105210713652 d| 9/14/2009 e| 9/4/2009 l| PHYSTEMP m| PHYSICS n| 1 r| Y s| Y t| STKS u| 8/20/2009
	    // Test case #4
		rec = factory.newRecord();
	    rec.setLeader(factory.newLeader("04473caa a2200313Ia 4500"));
		cf = factory.newControlField("001", "aactualRecord");
		rec.addVariableField(cf);
		df999 = factory.newDataField("999", '0', '0');
		df999.addSubfield(factory.newSubfield('a', "QB1 .A8133 V.402"));
		df999.addSubfield(factory.newSubfield('w', "LC"));
		df999.addSubfield(factory.newSubfield('c', "1"));
		df999.addSubfield(factory.newSubfield('i', "36105210713652"));
		df999.addSubfield(factory.newSubfield('l', "PHYSTEMP"));
		df999.addSubfield(factory.newSubfield('m', "PHYSICS"));
		df999.addSubfield(factory.newSubfield('t', "STKS"));
		rec.addVariableField(df999);

		df999 = factory.newDataField("999", '0', '0');
		df999.addSubfield(factory.newSubfield('a', "DO1 .A8133 V.402"));
		df999.addSubfield(factory.newSubfield('w', "LC"));
		df999.addSubfield(factory.newSubfield('c', "1"));
		df999.addSubfield(factory.newSubfield('i', "36105210713654"));
		df999.addSubfield(factory.newSubfield('k', "STACKS"));
		df999.addSubfield(factory.newSubfield('m', "PHYSICS"));
    	df999.addSubfield(factory.newSubfield('t', "STKS"));
    	rec.addVariableField(df999);

    	// Index but no building_facet value
		solrFldMapTest.assertNoSolrFld(rec,fldName);

		// Ignore item without location of PHYSTEMP
		solrFldMapTest.assertSolrFldHasNoValue(rec, "barcode", "36105210713654");
	}

	/** In order to check the exclusion of PHYSICS items without PHYSTEMP locs, a fresh index needs to be created so
	 *   these tests require a test file
	 */
@Test
	public void testSomeNotIndexed() throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		// index test records
		createFreshIx(testDataFname);

		// Test Case #1 Make sure record with PHYSICS and PHYSTEMP in 999$l gets indexed
		assertResultSize("id", "indexL", 1);

		// Test Case #1 Make sure record with PHYSICS and PHYSTEMP in 999$k gets indexed
		assertResultSize("id", "indexK", 1);

		// Test Case #2 Make sure record with PHYSICS and no PHYSTEMP doesn't get indexed
		assertZeroResults("id", "doNotIndex");

		// Test Case #3 Make sure record with GREEN gets indexed
		assertResultSize("id", "noPhysics", 1);

		// Test Case #5 Make sure record with multiple libraries of only PHYSICS and PHYSTEMP in 999$k gets indexed
		assertResultSize("id", "indexMultK", 1);

		// Test Case #6 Make sure record with multiple libraries of only PHYSICS and no PHYSTEMP doesn't get indexed
		assertResultSize("id", "doNotIndexMultAll", 0);

		// Test Case #7 Make sure record with multiple libraries including PHYSICS and PHYSTEMP in 999$l gets indexed
		assertResultSize("id", "indexMultL", 1);

		// Test Case #8 Make sure item with PHYSICS and no PHYSTEMP doesn't get indexed
		assertResultSize("id", "doNotIndexMult", 1);

		// Make sure no records have a building_facet of Physics
		assertZeroResults("building_facet", "Physics");
	}
}
