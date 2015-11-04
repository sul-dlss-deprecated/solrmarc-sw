package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for all_search field
 * @author Laney McGlohon
 */
public class BookplatesDisplayTests extends AbstractStanfordTest
{
  static String testDataFname = "bookplatesDisplayTests.xml";
  private static String fldNameBk = "bookplates_display";
  private static String fldNameFund = "fund_facet";
  private static MarcFactory factory = MarcFactory.newInstance();

@Before
  public void setup()
  {
    mappingTestInit();
  }

  /**
   * Test population of bookplates_display
   */
@Test
  public void testBookplatesDisplay()
      throws ParserConfigurationException, IOException, SAXException, SolrServerException
  {
    Record record = factory.newRecord();
    DataField df = factory.newDataField("979", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "BAILEYT"));
    df.addSubfield(factory.newSubfield('b', "druid:tf882hn2198"));
    df.addSubfield(factory.newSubfield('c', "tf882hn2198_00_0001.jp2"));
    df.addSubfield(factory.newSubfield('d', "Annie Nelson Bailey Memorial Book Fund"));
    record.addVariableField(df);
    solrFldMapTest.assertSolrFldValue(record, fldNameBk, "BAILEYT -|- tf882hn2198 -|- tf882hn2198_00_0001.jp2 -|- Annie Nelson Bailey Memorial Book Fund");
  }

  /**
   * Test population of bookplates_display with multiples 979
   */
@Test
  public void testBookplatesDisplayMultiples()
  {
    Record record = factory.newRecord();
    DataField df = factory.newDataField("979", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "BAILEYT"));
    df.addSubfield(factory.newSubfield('b', "druid:tf882hn2198"));
    df.addSubfield(factory.newSubfield('c', "tf882hn2198_00_0001.jp2"));
    df.addSubfield(factory.newSubfield('d', "Annie Nelson Bailey Memorial Book Fund"));
    record.addVariableField(df);
    df = factory.newDataField("979", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "HARRISL"));
    df.addSubfield(factory.newSubfield('b', "druid:bm267dr4255"));
    df.addSubfield(factory.newSubfield('c', "No content metadata"));
    df.addSubfield(factory.newSubfield('d', "Lucie King Harris Fund"));
    record.addVariableField(df);
    df = factory.newDataField("979", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "BENDERRM"));
    df.addSubfield(factory.newSubfield('b', "druid:hd360gv1231"));
    df.addSubfield(factory.newSubfield('c', "hd360gv1231_00_0001.jp2"));
    df.addSubfield(factory.newSubfield('d', "Stanford Bookstore : Centennial"));
    record.addVariableField(df);
    solrFldMapTest.assertSolrFldHasNumValues(record, fldNameBk, 2);
    solrFldMapTest.assertSolrFldValue(record, fldNameBk, "BAILEYT -|- tf882hn2198 -|- tf882hn2198_00_0001.jp2 -|- Annie Nelson Bailey Memorial Book Fund");
    solrFldMapTest.assertSolrFldValue(record, fldNameBk, "BENDERRM -|- hd360gv1231 -|- hd360gv1231_00_0001.jp2 -|- Stanford Bookstore : Centennial");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldNameBk, "HARRISL -|- bm267dr4255 -|- No content metadata -|- Lucie King Harris Fund");
  }

  /**
   * Test population of bookplates_display
   */
@Test
  public void testBookplatesDisplayNoContent()
  {
    Record record = factory.newRecord();
    DataField df = factory.newDataField("979", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "HARRISL"));
    df.addSubfield(factory.newSubfield('b', "druid:bm267dr4255"));
    df.addSubfield(factory.newSubfield('c', "No content metadata"));
    df.addSubfield(factory.newSubfield('d', "Lucie King Harris Fund"));
    record.addVariableField(df);
    solrFldMapTest.assertSolrFldHasNoValue(record, fldNameBk, "HARRISL -|- bm267dr4255 -|- No content metadata -|- Lucie King Harris Fund");

  }

  /**
   * Test population of fund_facet
   */
@Test
  public void testFundFacet()
    throws ParserConfigurationException, IOException, SAXException, SolrServerException
  {
    createFreshIx(testDataFname);
    Record record = factory.newRecord();
    DataField df = factory.newDataField("979", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "BENDERRM"));
    df.addSubfield(factory.newSubfield('b', "druid:hd360gv1231"));
    df.addSubfield(factory.newSubfield('c', "hd360gv1231_00_0001.jp2"));
    df.addSubfield(factory.newSubfield('d', "Stanford Bookstore : Centennial"));
    record.addVariableField(df);
    solrFldMapTest.assertSolrFldValue(record, fldNameFund, "hd360gv1231");
  }

  /**
   * Test population of fund_facet with multiples
   */
@Test
  public void testFundFacetMultiples()
  {
    Record record = factory.newRecord();
    DataField df = factory.newDataField("979", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "BENDERRM"));
    df.addSubfield(factory.newSubfield('b', "druid:hd360gv1231"));
    df.addSubfield(factory.newSubfield('c', "hd360gv1231_00_0001.jp2"));
    df.addSubfield(factory.newSubfield('d', "Stanford Bookstore : Centennial"));
    record.addVariableField(df);
    df = factory.newDataField("979", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "BAILEYT"));
    df.addSubfield(factory.newSubfield('b', "druid:tf882hn2198"));
    df.addSubfield(factory.newSubfield('c', "tf882hn2198_00_0001.jp2"));
    df.addSubfield(factory.newSubfield('d', "Annie Nelson Bailey Memorial Book Fund"));
    record.addVariableField(df);
    df = factory.newDataField("979", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "HARRISL"));
    df.addSubfield(factory.newSubfield('b', "druid:bm267dr4255"));
    df.addSubfield(factory.newSubfield('c', "No content metadata"));
    df.addSubfield(factory.newSubfield('d', "Lucie King Harris Fund"));
    record.addVariableField(df);
    solrFldMapTest.assertSolrFldHasNumValues(record, fldNameFund, 2);
    solrFldMapTest.assertSolrFldValue(record, fldNameFund, "tf882hn2198");
    solrFldMapTest.assertSolrFldValue(record, fldNameFund, "hd360gv1231");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldNameFund, "bm267dr4255");
  }

}
