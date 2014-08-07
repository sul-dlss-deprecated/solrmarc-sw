package edu.stanford;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.*;

/**
 * @author Naomi Dushay
 */
public class BibOnlyXmlTests extends AbstractStanfordTest
{
	MarcFactory factory = MarcFactory.newInstance();
	String fldName = "marcbib_xml";

	String leaderVal = "01952cas  2200457Ia 4500";
	String cf008Val = "780930m19391944nyu           000 0 eng d";
	String cf001Val = "aBibOnly";
	String titleVal = "title";
	String coreRecXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection xmlns=\"http://www.loc.gov/MARC21/slim\"><record><leader>" +
		leaderVal + "</leader><controlfield tag=\"001\">" +
		cf001Val + "</controlfield><controlfield tag=\"008\">" +
		cf008Val + "</controlfield><datafield tag=\"245\" ind1=\" \" ind2=\" \"><subfield code=\"a\">" +
		titleVal + "</subfield></datafield></record></collection>\n";

	private Record getCoreRecObj()
	{
		Record rec = factory.newRecord();
		rec.setLeader(factory.newLeader(leaderVal));
		ControlField cf = factory.newControlField("001", cf001Val);
		rec.addVariableField(cf);
		cf = factory.newControlField("008", cf008Val);
		rec.addVariableField(cf);
		DataField df = factory.newDataField("245", ' ', ' ');
		df.addSubfield(factory.newSubfield('a', titleVal));
		rec.addVariableField(df);
		return rec;
	}

	@Before
	public final void setup()
	{
		mappingTestInit();
	}

@Test
	public void test999Removed()
	{
		Record rec = getCoreRecObj();
		for (int i = 0; i < 3; i++)
		{
			DataField df = factory.newDataField("999", '0', '0');
			df.addSubfield(factory.newSubfield('a', "DO1 .A8133 V.402"));
			df.addSubfield(factory.newSubfield('w', "LC"));
			df.addSubfield(factory.newSubfield('c', "1"));
			df.addSubfield(factory.newSubfield('i', "3610521071365" + String.valueOf(i)));
			df.addSubfield(factory.newSubfield('l', "STACKS"));
			df.addSubfield(factory.newSubfield('m', "GREEN"));
			rec.addVariableField(df);
			solrFldMapTest.assertSolrFldValue(rec, fldName, coreRecXml);
		}
	}

	/** MHLD location */
@Test
	public void test852Removed()
	{
		Record rec = getCoreRecObj();
		for (int i = 0; i < 3; i++)
		{
			DataField df = factory.newDataField("852", ' ', ' ');
			df.addSubfield(factory.newSubfield('a', "CSt"));
			df.addSubfield(factory.newSubfield('b', "GREEN"));
			df.addSubfield(factory.newSubfield('c', "CURRENTPER"));
			df.addSubfield(factory.newSubfield('t', "1"));
			df.addSubfield(factory.newSubfield('z', "text"));
			rec.addVariableField(df);
			solrFldMapTest.assertSolrFldValue(rec, fldName, coreRecXml);
		}
	}

	/** MHLD captions and pattern fields */
@Test
	public void test853_5Removed()
	{
		Record rec = getCoreRecObj();
		for (int i = 0; i < 3; i++)
		{
			DataField df = factory.newDataField("85" + String.valueOf(i + 3), ' ', ' ');
			df.addSubfield(factory.newSubfield('a', "stuff"));
			rec.addVariableField(df);
			solrFldMapTest.assertSolrFldValue(rec, fldName, coreRecXml);
		}
	}

	/** MHLD enumeration and chronology fields */
@Test
	public void test863_5Removed()
	{
		Record rec = getCoreRecObj();
		for (int i = 0; i < 3; i++)
		{
			DataField df = factory.newDataField("86" + String.valueOf(i + 3), ' ', ' ');
			df.addSubfield(factory.newSubfield('a', "stuff"));
			rec.addVariableField(df);
			solrFldMapTest.assertSolrFldValue(rec, fldName, coreRecXml);
		}
	}

	/** MHLD holdings */
@Test
	public void test866Removed()
	{
		Record rec = getCoreRecObj();
		for (int i = 0; i < 3; i++)
		{
			DataField df = factory.newDataField("866", ' ', ' ');
			df.addSubfield(factory.newSubfield('8', "1"));
			df.addSubfield(factory.newSubfield('a', "v.151(1972)-v.152(1972)"));
			rec.addVariableField(df);
			solrFldMapTest.assertSolrFldValue(rec, fldName, coreRecXml);
		}
	}

	/** MHLD index holdings */
@Test
	public void test867Removed()
	{
		Record rec = getCoreRecObj();
		for (int i = 0; i < 3; i++)
		{
			DataField df = factory.newDataField("867", ' ', ' ');
			df.addSubfield(factory.newSubfield('8', "1"));
			df.addSubfield(factory.newSubfield('a', "v.151(1972)-v.152(1972)"));
			rec.addVariableField(df);
			solrFldMapTest.assertSolrFldValue(rec, fldName, coreRecXml);
		}
	}

	/** MHLD supplemental holdings */
@Test
	public void test868Removed()
	{
		Record rec = getCoreRecObj();
		for (int i = 0; i < 3; i++)
		{
			DataField df = factory.newDataField("868", ' ', ' ');
			df.addSubfield(factory.newSubfield('8', "1"));
			df.addSubfield(factory.newSubfield('a', "v.151(1972)-v.152(1972)"));
			rec.addVariableField(df);
			solrFldMapTest.assertSolrFldValue(rec, fldName, coreRecXml);
		}
	}

@Test
	public void test8xxAnd999Removed()
	{
		Record rec = getCoreRecObj();
		for (int i = 0; i < 3; i++)
		{
			DataField df = factory.newDataField("852", ' ', ' ');
			df.addSubfield(factory.newSubfield('a', "CSt"));
			df.addSubfield(factory.newSubfield('b', "GREEN"));
			df.addSubfield(factory.newSubfield('c', "CURRENTPER"));
			df.addSubfield(factory.newSubfield('t', "1"));
			df.addSubfield(factory.newSubfield('z', "text"));
			rec.addVariableField(df);

			df = factory.newDataField("85" + String.valueOf(i + 3), ' ', ' ');
			df.addSubfield(factory.newSubfield('a', "stuff"));
			rec.addVariableField(df);

			df = factory.newDataField("86" + String.valueOf(i + 3), ' ', ' ');
			df.addSubfield(factory.newSubfield('a', "stuff"));
			rec.addVariableField(df);

			df = factory.newDataField("86" + String.valueOf(i + 6), ' ', ' ');
			df.addSubfield(factory.newSubfield('8', "1"));
			df.addSubfield(factory.newSubfield('a', "v.151(1972)-v.152(1972)"));
			rec.addVariableField(df);
			solrFldMapTest.assertSolrFldValue(rec, fldName, coreRecXml);
		}

		for (int i = 0; i < 3; i++)
		{
			DataField df = factory.newDataField("999", '0', '0');
			df.addSubfield(factory.newSubfield('a', "DO1 .A8133 V.402"));
			df.addSubfield(factory.newSubfield('w', "LC"));
			df.addSubfield(factory.newSubfield('c', "1"));
			df.addSubfield(factory.newSubfield('i', "3610521071365" + String.valueOf(i)));
			df.addSubfield(factory.newSubfield('l', "STACKS"));
			df.addSubfield(factory.newSubfield('m', "GREEN"));
			rec.addVariableField(df);
			solrFldMapTest.assertSolrFldValue(rec, fldName, coreRecXml);
		}
	}

@Test
	public void test856NotRemoved()
	{
		Record rec = getCoreRecObj();
		DataField df = factory.newDataField("856", ' ', ' ');
		df.addSubfield(factory.newSubfield('a', "stuff"));
		rec.addVariableField(df);

		String recXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection xmlns=\"http://www.loc.gov/MARC21/slim\"><record><leader>" +
				leaderVal + "</leader><controlfield tag=\"001\">" +
				cf001Val + "</controlfield><controlfield tag=\"008\">" +
				cf008Val + "</controlfield><datafield tag=\"245\" ind1=\" \" ind2=\" \"><subfield code=\"a\">" +
				titleVal + "</subfield></datafield>" +
				"<datafield tag=\"856\" ind1=\" \" ind2=\" \"><subfield code=\"a\">stuff</subfield></datafield>" +
				"</record></collection>\n";
		solrFldMapTest.assertSolrFldValue(rec, fldName, recXml);
	}
}
