package edu.stanford;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.marc4j.marc.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University
 *   Remove closed libraries from the index
 * 1. if a record only has library from the closed libraries list, then do NOT index the record unless it is on reserve
 * 2. if a record has multiple libraries, and one of them is from the closed libraries list, then
 *    do not assign a library facet value for the closed libraries items
 * @author Shelley Doljack
 */
public class ClosedLibrariesTests extends AbstractStanfordTest
{

  static String testDataFname = "closedLibraries.xml";
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
      *  1. Record with only a closed library does not get indexed => BIOLOGY, CHEMCHMENG, MATH-CS
      *  2. GREEN and STACKS => building facet, index, so item_display field exists
      *  3. Record with multiple libraries including a closed library in 999$m gets indexed
      *       but no building facet for the closed libraries
      *  4. Record with only a closed library but item is on reserve should get indexed
      */
@Test
  public void testClosedLibrariesNoBuildingFacet()
  {
    // Test case #3
    Record rec = factory.newRecord();
    rec.setLeader(factory.newLeader("04473caa a2200313Ia 4500"));
    ControlField cf = factory.newControlField("001", "aindexGreen");
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

    // Test case #3
    rec = factory.newRecord();
    rec.setLeader(factory.newLeader("04473caa a2200313Ia 4500"));
    cf = factory.newControlField("001", "aactualRecord");
    rec.addVariableField(cf);
    df999 = factory.newDataField("999", '0', '0');
    df999.addSubfield(factory.newSubfield('a', "QB1 .A8133 V.402"));
    df999.addSubfield(factory.newSubfield('w', "LC"));
    df999.addSubfield(factory.newSubfield('c', "1"));
    df999.addSubfield(factory.newSubfield('i', "36105210713652"));
    df999.addSubfield(factory.newSubfield('l', "POP-SCI"));
    df999.addSubfield(factory.newSubfield('m', "SCIENCE"));
    df999.addSubfield(factory.newSubfield('t', "STKS"));
    rec.addVariableField(df999);

    df999 = factory.newDataField("999", '0', '0');
    df999.addSubfield(factory.newSubfield('a', "DO1 .A8133 V.402"));
    df999.addSubfield(factory.newSubfield('w', "LC"));
    df999.addSubfield(factory.newSubfield('c', "1"));
    df999.addSubfield(factory.newSubfield('i', "36105210713654"));
    df999.addSubfield(factory.newSubfield('k', "STACKS"));
    df999.addSubfield(factory.newSubfield('m', "CHEMCHMENG"));
    df999.addSubfield(factory.newSubfield('t', "STKS"));
    rec.addVariableField(df999);

    // Index but no building_facet value
    // solrFldMapTest.assertNoSolrFld(rec,fldName);

    // Building_facet value is Science (Li and Ma)
    solrFldMapTest.assertSolrFldValue(rec, fldName, "Science (Li and Ma)");

    // Ignore item with library CHEMCHMENG
    solrFldMapTest.assertSolrFldHasNoValue(rec, "barcode", "36105210713654");

    // Test case #4
    rec = factory.newRecord();
    rec.setLeader(factory.newLeader("04473caa a2200313Ia 4500"));
    cf = factory.newControlField("001", "aindexEng");
    rec.addVariableField(cf);

    df999 = factory.newDataField("999", '0', '0');
    df999.addSubfield(factory.newSubfield('a', "QC793.3 .F5 Q53 1983"));
    df999.addSubfield(factory.newSubfield('w', "LC"));
    df999.addSubfield(factory.newSubfield('c', "1"));
    df999.addSubfield(factory.newSubfield('i', "36105017547022"));
    df999.addSubfield(factory.newSubfield('k', "ENG-RESV"));
    df999.addSubfield(factory.newSubfield('l', "STACKS"));
    df999.addSubfield(factory.newSubfield('m', "CHEMCHMENG"));
    df999.addSubfield(factory.newSubfield('t', "STKS"));
    rec.addVariableField(df999);

    // Building_facet value is Engineering
    solrFldMapTest.assertSolrFldValue(rec, fldName, "Engineering (Terman)");
  }

  /** In order to check the exclusion of closed library items without open library items, a fresh index needs to be created so
   *   these tests require a test file
   */
@Test
  public void testClosedLibrariesNotIndexed() throws ParserConfigurationException, IOException, SAXException, SolrServerException
  {
    // index test records
    createFreshIx(testDataFname);

    // Test Case #1 Make sure record with BIOLOGY only does not get indexed
    assertZeroResults("id", "doNotIndexBio");

    // Test Case #1 Make sure record with CHEMCHMENG only does not get indexed
    assertZeroResults("id", "doNotIndexChem");

    // Test Case #1 Make sure record with MATH-CS only does not get indexed
    assertZeroResults("id", "doNotIndexMath");

    // Test Case #2 Make sure record with GREEN gets indexed
    assertResultSize("id", "noClosedLib", 1);

    // Test Case #3 Make sure record with multiple libraries including BIOLOGY and CHEMCHMENG in 999$m gets indexed
    // but no building_facet for Biology or Chemistry/Chemical Eng
    assertResultSize("id", "indexMultL", 1);

    // Test Cse #4 Make sure an item on reserve at an open reserve desk but library is a closed library gets indexed
    assertResultSize("id", "IndexEngResv", 1);

    // Make sure no records have a building_facet belonging to a closed library
    assertZeroResults("building_facet", "Biology (Falconer)");
    assertZeroResults("building_facet", "Chemistry & ChemEng (Swain)");
    assertZeroResults("building_facet", "Math & Statistics");
  }
}
