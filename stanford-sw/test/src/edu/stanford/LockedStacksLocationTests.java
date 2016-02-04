package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for location facet for locked stacks
 * @author Laney McGlohon
 */
public class LockedStacksLocationTests extends AbstractStanfordTest
{
  private static String fldName = "location_facet";
  private static String fldBldgName = "building_facet";
  private static MarcFactory factory = MarcFactory.newInstance();

@Before
  public void setup()
  {
    mappingTestInit();
  }

  /**
   * Test population of location_facet from the 852 subfield c
   */
@Test
  public void testLocationFacetFrom852()
  {
    Record record = factory.newRecord();
    DataField df = factory.newDataField("852", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "CSt"));
    df.addSubfield(factory.newSubfield('b', "MATH-CS"));
    df.addSubfield(factory.newSubfield('c', "ARTLCKL"));
    record.addVariableField(df);
    solrFldMapTest.assertSolrFldValue(record, fldName, "Art Locked Stacks");
  }

  /**
   * Test population of location_facet from 999 subfield t
   */
@Test
  public void testLocationFacetFrom999()
  {
    Record record = factory.newRecord();
    DataField df = factory.newDataField("999", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "PR3724.T3 V2"));
    df.addSubfield(factory.newSubfield('w', "LC"));
    df.addSubfield(factory.newSubfield('c', "1"));
    df.addSubfield(factory.newSubfield('i', "36105003934432"));
    df.addSubfield(factory.newSubfield('m', "ART"));
    df.addSubfield(factory.newSubfield('r', "Y"));
    df.addSubfield(factory.newSubfield('s', "Y"));
    df.addSubfield(factory.newSubfield('l', "ARTLCKL-R"));
    record.addVariableField(df);
    solrFldMapTest.assertSolrFldValue(record, fldName, "Art Locked Stacks");
  }

/**
 * Test no location_facet if not appropriate value in either 852$c or 999$t
 */
@Test
  public void testNoLocationFacet()
  {
    Record record = factory.newRecord();
    DataField df = factory.newDataField("852", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "CSt"));
    df.addSubfield(factory.newSubfield('b', "MATH-CS"));
    df.addSubfield(factory.newSubfield('c', "NOTLOCKED"));
    record.addVariableField(df);
    df = factory.newDataField("999", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "PR3724.T3 V2"));
    df.addSubfield(factory.newSubfield('w', "LC"));
    df.addSubfield(factory.newSubfield('c', "1"));
    df.addSubfield(factory.newSubfield('i', "36105003934432"));
    df.addSubfield(factory.newSubfield('m', "ART"));
    df.addSubfield(factory.newSubfield('r', "Y"));
    df.addSubfield(factory.newSubfield('s', "Y"));
    df.addSubfield(factory.newSubfield('l', "NOTLOCKED"));
    record.addVariableField(df);
    solrFldMapTest.assertNoSolrFld(record, fldName);
  }

  /**
	 * Test Art library added to library facet when 999$l = PAGE-AR
	 * and library = SAL3
	 */
	@Test
	  public void testAdditionalLibrary()
	  {
	    Record record = factory.newRecord();
      DataField df = factory.newDataField("999", ' ', ' ');
	    df.addSubfield(factory.newSubfield('a', "PR3724.T3 V2"));
	    df.addSubfield(factory.newSubfield('w', "LC"));
	    df.addSubfield(factory.newSubfield('c', "1"));
	    df.addSubfield(factory.newSubfield('i', "36105003934432"));
	    df.addSubfield(factory.newSubfield('m', "SAL3"));
	    df.addSubfield(factory.newSubfield('r', "Y"));
	    df.addSubfield(factory.newSubfield('s', "Y"));
	    df.addSubfield(factory.newSubfield('l', "PAGE-AR"));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, fldBldgName, "Art & Architecture (Bowes)");
			solrFldMapTest.assertSolrFldValue(record, fldBldgName, "SAL3 (off-campus storage)");
	  }
}
