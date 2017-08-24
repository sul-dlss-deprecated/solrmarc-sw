package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for location facet for curriculum and locked stacks
 * @author Shelley Doljack
 */
public class LocationFacetTests extends AbstractStanfordTest
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
  public void testLocationFacetCurriculumFrom852()
  {
    Record record = factory.newRecord();
    DataField df = factory.newDataField("852", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "CSt"));
    df.addSubfield(factory.newSubfield('b', "EDUCATION"));
    df.addSubfield(factory.newSubfield('c', "CURRICULUM"));
    record.addVariableField(df);
    solrFldMapTest.assertSolrFldValue(record, fldName, "Curriculum Collection");
  }

  /**
   * Test population of location_facet from 999 subfield l (home location)
   */
@Test
  public void testLocationFacetCurriculumFrom999()
  {
    Record record = factory.newRecord();
    DataField df = factory.newDataField("999", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "QA107 .B47 2000 V.1A"));
    df.addSubfield(factory.newSubfield('w', "LC"));
    df.addSubfield(factory.newSubfield('c', "1"));
    df.addSubfield(factory.newSubfield('i', "36105219836918"));
    df.addSubfield(factory.newSubfield('m', "EDUCATION"));
    df.addSubfield(factory.newSubfield('r', "Y"));
    df.addSubfield(factory.newSubfield('s', "Y"));
    df.addSubfield(factory.newSubfield('l', "CURRICULUM"));
    record.addVariableField(df);
    solrFldMapTest.assertSolrFldValue(record, fldName, "Curriculum Collection");
  }

/**
 * Test no location_facet if not appropriate value in 999 l
 */
@Test
  public void testLocationFacetNoCurriculumFrom999()
  {
    Record record = factory.newRecord();
    DataField df = factory.newDataField("999", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Z253 .U69 2010"));
    df.addSubfield(factory.newSubfield('w', "LC"));
    df.addSubfield(factory.newSubfield('c', "1"));
    df.addSubfield(factory.newSubfield('i', "36105215224689"));
    df.addSubfield(factory.newSubfield('m', "EDUCATION"));
    df.addSubfield(factory.newSubfield('r', "Y"));
    df.addSubfield(factory.newSubfield('s', "Y"));
    df.addSubfield(factory.newSubfield('l', "REFERENCE"));
    record.addVariableField(df);
    solrFldMapTest.assertNoSolrFld(record, fldName);
  }

  /**
   * Test population of location_facet from the 852 subfield c
   */
@Test
  public void testLocationFacetArtLockedFrom852()
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
   * Test population of location_facet from 999 subfield l
   */
@Test
  public void testLocationFacetArtLockedFrom999()
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
 * Test no location_facet if not appropriate value in either 852$c or 999$l
 */
@Test
  public void testLocationFacetNoArtLocked()
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
