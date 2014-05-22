package edu.stanford;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.marc4j.marc.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University 
 *   Deal with Physics library as a building facet value  SW-849
 * @author Laney McGlohon
 */
public class PhysicsLibraryItemTests extends AbstractStanfordTest 
{

	static String testDataFname = "8230276.xml";
	String testFilePath = testDataParentPath + File.separator + testDataFname;

	private final String fldName = "building_facet";
	MarcFactory factory = MarcFactory.newInstance();
	
@Before
	public final void setup() 
	{
		mappingTestInit();
	}	

	/**
	 * SW-849
     * 1. if a record only has library = Physics and *no* item has location PHYSTEMP, then do NOT index the record. 
     * 2. if a record only has library = Physics and at least one item has location PHYSTEMP, then index the record but: 
     *    only include items with location PHYSTEMP 
     *    do not assign a library facet value (treat like an online record) 
     * 3. if a record has multiple libraries, and one of them is Physics, then 
     *    do not assign a library facet value for Physics items (treat like online records) 
     *    for Physics items, only include the items if they have location PHYSTEMP (and treat as online item) 
	 */

@Test
	public void testNoBuildingFacet()
	{
		
		// Actual item (8230276) 999 a| QB1 .A8133 V.402 w| LC c| 1 i| 36105210713652 d| 9/14/2009 e| 9/4/2009 l| PHYSTEMP m| PHYSICS n| 1 r| Y s| Y t| STKS u| 8/20/2009
		Record rec = factory.newRecord();
	    rec.setLeader(factory.newLeader("04473caa a2200313Ia 4500"));
		ControlField cf = factory.newControlField("001", "aactualRecord");
		rec.addVariableField(cf);
		DataField df999 = factory.newDataField("999", '0', '0');
		df999.addSubfield(factory.newSubfield('a', "QB1 .A8133 V.402"));
		df999.addSubfield(factory.newSubfield('w', "LC"));
		df999.addSubfield(factory.newSubfield('c', "1"));
		df999.addSubfield(factory.newSubfield('i', "36105210713652"));
		df999.addSubfield(factory.newSubfield('d', "9/14/2009"));
		df999.addSubfield(factory.newSubfield('e', "9/14/2009"));
		df999.addSubfield(factory.newSubfield('l', "PHYSTEMP"));
		df999.addSubfield(factory.newSubfield('m', "PHYSICS"));
		df999.addSubfield(factory.newSubfield('n', "1"));
		df999.addSubfield(factory.newSubfield('r', "Y"));
		df999.addSubfield(factory.newSubfield('s', "Y"));
    		df999.addSubfield(factory.newSubfield('t', "STKS"));
    		df999.addSubfield(factory.newSubfield('u', "8/20/2009"));
    		rec.addVariableField(df999);
    	
    		df999 = factory.newDataField("999", '0', '0');
    		df999.addSubfield(factory.newSubfield('a', "DO1 .A8133 V.402"));
    		df999.addSubfield(factory.newSubfield('w', "LC"));
    		df999.addSubfield(factory.newSubfield('c', "1"));
    		df999.addSubfield(factory.newSubfield('i', "36105210713654"));
    		df999.addSubfield(factory.newSubfield('d', "9/14/2009"));
    		df999.addSubfield(factory.newSubfield('e', "9/14/2009"));
    		df999.addSubfield(factory.newSubfield('k', "STACKS"));
    		df999.addSubfield(factory.newSubfield('m', "PHYSICS"));
    		df999.addSubfield(factory.newSubfield('n', "1"));
    		df999.addSubfield(factory.newSubfield('r', "Y"));
    		df999.addSubfield(factory.newSubfield('s', "Y"));
        	df999.addSubfield(factory.newSubfield('t', "STKS"));
        	df999.addSubfield(factory.newSubfield('u', "8/20/2009"));
        	rec.addVariableField(df999);
    		
        	// Index but no building_facet value
    		solrFldMapTest.assertNoSolrFld(rec,fldName);

    		// Ignore item without location of PHYSTEMP
    		solrFldMapTest.assertSolrFldHasNoValue(rec, "barcode", "36105210713654");
    	
	}

@Test
	public void testSomeNotIndexed() throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		// index test records
		createFreshIx(testDataFname);
		
		// Make sure record with PHYSICS and PHYSTEMP in 999$l gets indexed
		assertResultSize("id", "indexL", 1);
		
		// Make sure record with PHYSICS and PHYSTEMP in 999$k gets indexed
		assertResultSize("id", "indexK", 1);

		// Make sure record with PHYSICS and no PHYSTEMP doesn't get indexed
		assertZeroResults("id", "doNotIndex");
		
		// Make sure record with multiple libraries including PHYSICS and PHYSTEMP in 999$l gets indexed
		assertResultSize("id", "indexMultL", 1);
		
		// Make sure record with multiple libraries including PHYSICS and PHYSTEMP in 999$k gets indexed
		assertResultSize("id", "indexMultK", 1);

		// Make sure record with PHYSICS and no PHYSTEMP doesn't get indexed
		assertResultSize("id", "doNotIndexMult", 1);

		// Make sure no records have a building_facet of Physics
		assertZeroResults("building_facet", "Physics");

		
	}
}