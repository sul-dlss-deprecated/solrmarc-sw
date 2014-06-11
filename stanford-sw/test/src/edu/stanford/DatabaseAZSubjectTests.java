package edu.stanford;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.marc4j.marc.*;


/**
 * junit4 tests for Stanford University format fields for blacklight index
 * 
 * @author Naomi Dushay
 */
public class DatabaseAZSubjectTests extends AbstractStanfordTest 
{
	String facetFldName = "db_az_subject";
	MarcFactory factory = MarcFactory.newInstance();

@Before
	public final void setup() 
	{
		mappingTestInit();
	}

	/**
	 * test that an A-Z database with multiple 099 codes has values for all.
	 */
@Test
	public final void testMultGoodSubjects()
	{

		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01541cai a2200349Ia 4500"));
		ControlField cf008 = factory.newControlField("008");
		cf008.setData("040727c20049999nyuuu dss     0    2eng d");
		record.addVariableField(cf008);
		DataField df245 = factory.newDataField("245", '0', '0');
		df245.addSubfield(factory.newSubfield('a', "database 999t with 099s mapping to different subjects"));
		record.addVariableField(df245);
		DataField df099 = factory.newDataField("099", ' ', ' ');
		df099.addSubfield(factory.newSubfield('a', "AP"));
		record.addVariableField(df099);
		df099 = factory.newDataField("099", ' ', ' ');
		df099.addSubfield(factory.newSubfield('a', "Q"));
		record.addVariableField(df099);
		DataField df999 = factory.newDataField("999", ' ', ' ');
		df999.addSubfield(factory.newSubfield('a', "INTERNET RESOURCE"));
		df999.addSubfield(factory.newSubfield('w', "ALPHANUM"));
		df999.addSubfield(factory.newSubfield('i', "1"));
		df999.addSubfield(factory.newSubfield('l', "INTERNET"));
		df999.addSubfield(factory.newSubfield('m', "SUL"));
		df999.addSubfield(factory.newSubfield('t', "DATABASE"));
		record.addVariableField(df999);
		
		solrFldMapTest.assertSolrFldValue(record, facetFldName, "News");
		solrFldMapTest.assertSolrFldValue(record, facetFldName, "Science (General)");
	}

	/**
	 * test that an A-Z database with data in wrong subfield is ignored
	 */
@Test
	public final void testBadSubjects()
	{
	
		// Data in wrong subfield
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01541cai a2200349Ia 4500"));
		ControlField cf008 = factory.newControlField("008");
		cf008.setData("040727c20049999nyuuu dss     0    2eng d");
		record.addVariableField(cf008);
		DataField df245 = factory.newDataField("245", '0', '0');
		df245.addSubfield(factory.newSubfield('a', "database 999t with 099 but wrong subfield"));
		record.addVariableField(df245);
		DataField df099 = factory.newDataField("099", ' ', ' ');
		df099.addSubfield(factory.newSubfield('b', "Q"));
		record.addVariableField(df099);
		DataField df999 = factory.newDataField("999", ' ', ' ');
		df999.addSubfield(factory.newSubfield('a', "INTERNET RESOURCE"));
		df999.addSubfield(factory.newSubfield('w', "ALPHANUM"));
		df999.addSubfield(factory.newSubfield('i', "1"));
		df999.addSubfield(factory.newSubfield('l', "INTERNET"));
		df999.addSubfield(factory.newSubfield('m', "SUL"));
		df999.addSubfield(factory.newSubfield('t', "DATABASE"));
		record.addVariableField(df999);

		solrFldMapTest.assertNoSolrFld(record, facetFldName);
	}

	/**
	 * test that an A-Z database with no subjects doesn't get any
	 */
@Test
	public final void testSubjectsWithOtherDatabase()
	{
	
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01541cai a2200349Ia 4500"));
		ControlField cf008 = factory.newControlField("008");
		cf008.setData("040727c20049999nyuuu dss     0    2eng d");
		record.addVariableField(cf008);
		DataField df245 = factory.newDataField("245", '0', '0');
		df245.addSubfield(factory.newSubfield('a', "database NOT from 999t with 099a"));
		record.addVariableField(df245);
		DataField df099 = factory.newDataField("099", ' ', ' ');
		df099.addSubfield(factory.newSubfield('a', "Q"));
		record.addVariableField(df099);
		DataField df999 = factory.newDataField("999", ' ', ' ');
		df999.addSubfield(factory.newSubfield('a', "INTERNET RESOURCE"));
		df999.addSubfield(factory.newSubfield('w', "ALPHANUM"));
		df999.addSubfield(factory.newSubfield('i', "1"));
		df999.addSubfield(factory.newSubfield('l', "INTERNET"));
		df999.addSubfield(factory.newSubfield('m', "SUL"));
		record.addVariableField(df999);
	
		solrFldMapTest.assertNoSolrFld(record, facetFldName);
	}

	/**
	 * test that double assigned subject codes get both their values
	 */
@Test
	public final void testDoubleAssigned()
	{
	
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01744cai a2200373La 4500"));
		ControlField cf008 = factory.newControlField("008");
		cf008.setData("030701c200u9999dcuuu dss 000 02eng d");
		record.addVariableField(cf008);
		DataField df245 = factory.newDataField("245", '0', '0');
		df245.addSubfield(factory.newSubfield('a', "CQ Congress collection"));
		df245.addSubfield(factory.newSubfield('h', "[electronic resource]."));
		record.addVariableField(df245);
		DataField df099 = factory.newDataField("099", ' ', ' ');
		df099.addSubfield(factory.newSubfield('a', "JK"));
		record.addVariableField(df099);
		df099 = factory.newDataField("099", ' ', ' ');
		df099.addSubfield(factory.newSubfield('a', "XM"));
		record.addVariableField(df099);
		DataField df999 = factory.newDataField("999", ' ', ' ');
		df999.addSubfield(factory.newSubfield('a', "INTERNET RESOURCE"));
		df999.addSubfield(factory.newSubfield('w', "ASIS"));
		df999.addSubfield(factory.newSubfield('i', "6859025-2001"));
		df999.addSubfield(factory.newSubfield('l', "INTERNET"));
		df999.addSubfield(factory.newSubfield('m', "SUL"));
		df999.addSubfield(factory.newSubfield('r', "Y"));
		df999.addSubfield(factory.newSubfield('s', "Y"));
		df999.addSubfield(factory.newSubfield('t', "DATABASE"));
		df999.addSubfield(factory.newSubfield('u', "8/3/2007"));
		df999.addSubfield(factory.newSubfield('o', ".TECHSTAFF. online subscr. wanted/ mmd20070803"));
		record.addVariableField(df999);

		// XM
		solrFldMapTest.assertSolrFldValue(record, facetFldName, "Government Information: United States");

		// JK is assigned to both American History and Political Science
		solrFldMapTest.assertSolrFldValue(record, facetFldName, "American History");
		solrFldMapTest.assertSolrFldValue(record, facetFldName, "Political Science");

		solrFldMapTest.assertSolrFldHasNumValues(record, facetFldName, 3);
	}

    /**
     *A-Z database subjects should be searchable with terms (not whole String)
     *Need a fresh index so the test records are included in an external XML file 
     */
@Test
    public final void testSearched() 
    		throws ParserConfigurationException, IOException, SAXException, SolrServerException
    {
		createFreshIx("databasesAZsubjectTests.xml");
		String fldName = "db_az_subject_search";
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("2diffsubs");
		docIds.add("6859025");
		assertSearchResults(fldName, "Science", docIds);

		docIds.remove("6859025");
		docIds.add("singleTerm");
		assertSearchResults(fldName, "News", docIds);

		assertSingleResult("2diffsubs", fldName, "General");
		assertSingleResult("6859025", fldName, "Government");
		// double assigning subject code JK
		assertSingleResult("6859025", fldName, "History");
		assertSingleResult("6859025", fldName, "Political");
    }

/**
 * INDEX-14 test that an A-Z database with no 099 gets uncategorized topic facet value
 */
@Test
	public final void testNo099()
	{
	
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01541cai a2200349Ia 4500"));
		ControlField cf008 = factory.newControlField("008");
		cf008.setData("040727c20049999nyuuu dss     0    2eng d");
		record.addVariableField(cf008);
		DataField df245 = factory.newDataField("245", '0', '0');
		df245.addSubfield(factory.newSubfield('a', "database 999t with no 099"));
		record.addVariableField(df245);
		DataField df999 = factory.newDataField("999", ' ', ' ');
		df999.addSubfield(factory.newSubfield('a', "INTERNET RESOURCE"));
		df999.addSubfield(factory.newSubfield('w', "ALPHANUM"));
		df999.addSubfield(factory.newSubfield('i', "1"));
		df999.addSubfield(factory.newSubfield('l', "INTERNET"));
		df999.addSubfield(factory.newSubfield('m', "SUL"));
		df999.addSubfield(factory.newSubfield('t', "DATABASE"));
		record.addVariableField(df999);
	
		solrFldMapTest.assertSolrFldValue(record, facetFldName, "Uncategorized");
	
	}
}
