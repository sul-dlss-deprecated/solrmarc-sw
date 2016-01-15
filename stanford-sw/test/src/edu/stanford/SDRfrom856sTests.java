package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University
 *   Add SDR for MARC records with purl.stanford.edu in an 856
 * @author Laney McGlohon
 */
public class SDRfrom856sTests extends AbstractStanfordTest
{
  String fldName = "building_facet";
  private static MarcFactory factory = MarcFactory.newInstance();

@Before
	public final void setup()
	{
		mappingTestInit();
	}

	/**
	 * 856 has purl.stanford.edu as part of the URL so add SDR into building_facet
	 */
@Test
	public void testSDRbuildingAddition()
	{
    Record record = factory.newRecord();
    DataField df = factory.newDataField("856", ' ', ' ');
    df.addSubfield(factory.newSubfield('u', "https://purl.stanford.edu"));
    record.addVariableField(df);
    df = factory.newDataField("856", ' ', ' ');
    df.addSubfield(factory.newSubfield('u', "http://example.org"));
    record.addVariableField(df);
    solrFldMapTest.assertSolrFldValue(record, fldName, "Stanford Digital Repository");
	}

  /**
   * 856 does not have purl.stanford.edu as part of the URL so no SDR in building_facet
   */
@Test
  public void testNoSDRbuildingAddition()
  {
    Record record = factory.newRecord();
    DataField df = factory.newDataField("856", ' ', ' ');
    df.addSubfield(factory.newSubfield('u', "http://example.org"));
    record.addVariableField(df);
    df = factory.newDataField("856", ' ', ' ');
    df.addSubfield(factory.newSubfield('u', "http://cnn.com"));
    record.addVariableField(df);
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "SDR");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Stanford Digital Repository");
  }

}
