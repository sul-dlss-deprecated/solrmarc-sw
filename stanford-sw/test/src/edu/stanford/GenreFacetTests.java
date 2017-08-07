package edu.stanford;

import java.io.*;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.marc4j.marc.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University
 *   Add Genre facet values for MARC record 655a and 6XXv
 * @author Shelley Doljack
 */

public class GenreFacetTests extends AbstractStanfordTest
{
  String fldName = "genre_ssim";
  private static MarcFactory factory = MarcFactory.newInstance();

  static String testDataFname = "genreFacetTests.xml";
  String testFilePath = testDataParentPath + File.separator + testDataFname;

  @Before
  public final void setup()
  {
    mappingTestInit();
  }

  /**
   * genre_facet should contain all 651a and the first subfield v in
   * list of 6xx fields.
   */
  @Test
  public void test655aGenreFacet()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("04473cam a2200313Ia 4500"));
    ControlField cf = factory.newControlField("001", "a655aGenre");
    record.addVariableField(cf);
    DataField df = factory.newDataField("655", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Silent films."));
    record.addVariableField(df);
    df = factory.newDataField("655", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Clay animation films."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Silent films");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Clay animation films");
  }

  @Test
  public void test655vGenreFacet()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("04473cam a2200313Ia 4500"));
    ControlField cf = factory.newControlField("001", "a655vGenre");
    record.addVariableField(cf);
    DataField df = factory.newDataField("655", ' ', ' ');
    df.addSubfield(factory.newSubfield('v', "Software."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Software");
  }

  @Test
  public void test6xxvGenreFacet()
  {
    // 600:610:611:630:647:648:650:651:654:656:657
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("04473cam a2200313Ia 4500"));
    ControlField cf = factory.newControlField("001", "a6xxvGenre");
    record.addVariableField(cf);
    DataField df = factory.newDataField("600", ' ', '0');
    df.addSubfield(factory.newSubfield('a', "Gautama Buddha"));
    df.addSubfield(factory.newSubfield('v', "Early works to 1800."));
    record.addVariableField(df);

    df = factory.newDataField("600", ' ', '1');
    df.addSubfield(factory.newSubfield('v', "Bildband"));
    record.addVariableField(df);

    df = factory.newDataField("610", ' ', '0');
    df.addSubfield(factory.newSubfield('a', "Something"));
    df.addSubfield(factory.newSubfield('v', "Case studies"));
    record.addVariableField(df);

    df = factory.newDataField("610", ' ', ' ');
    df.addSubfield(factory.newSubfield('v', "Guidebooks"));
    record.addVariableField(df);

    df = factory.newDataField("611", ' ', '0');
    df.addSubfield(factory.newSubfield('a', "Something"));
    df.addSubfield(factory.newSubfield('v', "Speeches in Congress"));
    record.addVariableField(df);

    df = factory.newDataField("611", ' ', '4');
    df.addSubfield(factory.newSubfield('v', "Fiction)"));
    record.addVariableField(df);

    df = factory.newDataField("630", ' ', '0');
    df.addSubfield(factory.newSubfield('a', "Something"));
    df.addSubfield(factory.newSubfield('v', "Criticism, interpretation, etc."));
    record.addVariableField(df);

    df = factory.newDataField("630", ' ', '7');
    df.addSubfield(factory.newSubfield('v', "Teatro"));
    record.addVariableField(df);

    df = factory.newDataField("650", ' ', '0');
    df.addSubfield(factory.newSubfield('a', "World War, 1939-1945"));
    df.addSubfield(factory.newSubfield('v', "Personal narratives."));
    record.addVariableField(df);

    df = factory.newDataField("650", ' ', '1');
    df.addSubfield(factory.newSubfield('v', "Humor"));
    record.addVariableField(df);

    df = factory.newDataField("651", ' ', '0');
    df.addSubfield(factory.newSubfield('a', "Something"));
    df.addSubfield(factory.newSubfield('v', "Census, 1999."));
    record.addVariableField(df);

    df = factory.newDataField("651", ' ', '4');
    df.addSubfield(factory.newSubfield('v', "Ausstellung"));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Gautama Buddha");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Something");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "World War, 1939-1945");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Early works to 1800");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Bildband");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Case studies");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Guidebooks");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Speeches in Congress");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Fiction");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Criticism, interpretation, etc.");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Teatro");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Personal narratives");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Humor");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Census, 1999");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Ausstellung");
  }

  @Test
  public void testMultiple650vGenreFacet()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("04473cam a2200313Ia 4500"));
    ControlField cf = factory.newControlField("001", "aMultiple650vGenre");
    record.addVariableField(cf);
    DataField df = factory.newDataField("650", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Automobiles"));
    df.addSubfield(factory.newSubfield('x', "Collision damage"));
    df.addSubfield(factory.newSubfield('z', "California"));
    df.addSubfield(factory.newSubfield('v', "Statistics"));
    df.addSubfield(factory.newSubfield('v', "Periodicals."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Statistics");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Periodicals");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Automobiles");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Collision damage");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "California");
  }

  @Test
  public void testGenreFacetNormalization()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("04473cam a2200313Ia 4500"));
    ControlField cf = factory.newControlField("001", "aNormalization");
    record.addVariableField(cf);
    // Trailing periods
    DataField df = factory.newDataField("650", ' ', ' ');
    df.addSubfield(factory.newSubfield('v', "Anecdotes.."));
    record.addVariableField(df);

    // Trailing period
    df = factory.newDataField("650", ' ', '0');
    df.addSubfield(factory.newSubfield('v', "Art and the war."));
    record.addVariableField(df);

    // Trailing whitespace and multiple intra-field whitespace
    df = factory.newDataField("655", ' ', '7');
    df.addSubfield(factory.newSubfield('a', "Accordion fold format  (Binding) "));
    record.addVariableField(df);

    // Trailing space period
    df = factory.newDataField("650", ' ', '0');
    df.addSubfield(factory.newSubfield('v', "Underwater photography ."));
    record.addVariableField(df);

    // Trailing period space period
    df = factory.newDataField("655", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Sociology. ."));
    record.addVariableField(df);

    // Period succeeding word with three letters
    df = factory.newDataField("650", ' ', '0');
    df.addSubfield(factory.newSubfield('v', "Translations into Udi."));
    record.addVariableField(df);
    df = factory.newDataField("650", ' ', '0');
    df.addSubfield(factory.newSubfield('v', "Mic."));
    record.addVariableField(df);
    df = factory.newDataField("650", ' ', '0');
    df.addSubfield(factory.newSubfield('v', "Art."));
    record.addVariableField(df);
    df = factory.newDataField("655", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Dr."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Anecdotes");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Art and the war");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Accordion fold format (Binding)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Underwater photography");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Sociology");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Translations into Udi");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Mic");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Art");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Dr.");
  }

  // In order to check the genre facet counts a fresh index needs to be created so these tests require a test file

  @Test
  public void testGenreFacetCounts() throws ParserConfigurationException, IOException, SAXException, SolrServerException
  {
    // index test records
    createFreshIx(testDataFname);

    assertResultSize("genre_ssim", "Correspondence", 3);
    assertDocHasNoFieldValue("NoGenre", "genre_ssim", "");
  }
}
