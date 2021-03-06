/*
 * Copyright (c) 2012.  The Board of Trustees of the Leland Stanford Junior University. All rights reserved.
 *
 * Redistribution and use of this distribution in source and binary forms, with or without modification, are permitted provided that: The above copyright notice and this permission notice appear in all copies and supporting documentation; The name, identifiers, and trademarks of The Board of Trustees of the Leland Stanford Junior University are not used in advertising or publicity without the express prior written permission of The Board of Trustees of the Leland Stanford Junior University; Recipients acknowledge that this distribution is made available as a research courtesy, "as is", potentially with defects, without any obligation on the part of The Board of Trustees of the Leland Stanford Junior University to provide support, services, or repair;
 *
 * THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT LIMITATION ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/

package edu.stanford;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.*;

/**
 * Unit tests for methods in edu.stanford.MhldDisplayUtil
 * @author ndushay
 *
 */
public class MhldDisplayUnitTests extends AbstractStanfordTest
{
	private MarcFactory factory = MarcFactory.newInstance();
	private String fldName = "mhld_display";

@Before
	public void setup()
	{
		mappingTestInit();
	}

	/**
	 * 863 has "unit" after "year"
	 */
@Test
	public void testMhldDisplayUnitAfterYear()
	{
		Record rec = factory.newRecord();
		ControlField cf = factory.newControlField("001", "aunitAfterYear");
		rec.addVariableField(cf);
		// from http://searchworks.stanford.edu/view/474135
		DataField df = factory.newDataField("852", ' ', ' ');
		df.addSubfield(factory.newSubfield('a', "CSt"));
		df.addSubfield(factory.newSubfield('b', "MATH-CS"));
		df.addSubfield(factory.newSubfield('c', "SHELBYTITL"));
		df.addSubfield(factory.newSubfield('=', "8287"));
		rec.addVariableField(df);
    	df = factory.newDataField("853", '2', ' ');
	    df.addSubfield(factory.newSubfield('8', "2"));
	    df.addSubfield(factory.newSubfield('a', "v."));
	    df.addSubfield(factory.newSubfield('b', "no."));
	    df.addSubfield(factory.newSubfield('u', "4"));
	    df.addSubfield(factory.newSubfield('v', "r"));
	    df.addSubfield(factory.newSubfield('i', "(year)"));
	    df.addSubfield(factory.newSubfield('j', "(unit)"));
	    rec.addVariableField(df);
    	df = factory.newDataField("863", ' ', '1');
	    df.addSubfield(factory.newSubfield('8', "2.57"));
	    df.addSubfield(factory.newSubfield('a', "54"));
	    df.addSubfield(factory.newSubfield('b', "1"));
	    df.addSubfield(factory.newSubfield('i', "2013"));
	    df.addSubfield(factory.newSubfield('j', "1_TRIMESTRE"));
	    rec.addVariableField(df);
		df = factory.newDataField("866", '3', '1');
		df.addSubfield(factory.newSubfield('8', "1"));
		df.addSubfield(factory.newSubfield('a', "v.25(1984)-"));
		rec.addVariableField(df);

		solrFldMapTest.assertSolrFldHasNumValues(rec, fldName, 1);
		solrFldMapTest.assertSolrFldValue(rec, fldName, "MATH-CS -|- SHELBYTITL -|-  -|- v.25(1984)- -|- v.54:no.1 (2013:1_TRIMESTRE)");
	}

	/**
	 * test latest received patterns
	 */
@Test
	public void testLatestReceivedPatterns()
	{
		Record rec = factory.newRecord();
		ControlField cf = factory.newControlField("001", "alatestRecdPatterns");
		rec.addVariableField(cf);
		// from http://searchworks.stanford.edu/view/474135
		DataField df852 = factory.newDataField("852", ' ', ' ');
		df852.addSubfield(factory.newSubfield('a', "CSt"));
		df852.addSubfield(factory.newSubfield('b', "lib"));
		df852.addSubfield(factory.newSubfield('c', "loc"));
		df852.addSubfield(factory.newSubfield('=', "output latest received"));
		rec.addVariableField(df852);
		DataField df = factory.newDataField("853", '2', ' ');
	    df.addSubfield(factory.newSubfield('8', "3"));
	    df.addSubfield(factory.newSubfield('a', "v."));
	    df.addSubfield(factory.newSubfield('b', "pt."));
	    df.addSubfield(factory.newSubfield('u', "3"));
	    df.addSubfield(factory.newSubfield('v', "r"));
	    df.addSubfield(factory.newSubfield('c', "no."));
	    df.addSubfield(factory.newSubfield('v', "c"));
	    df.addSubfield(factory.newSubfield('i', "(year)"));
	    df.addSubfield(factory.newSubfield('j', "(season)"));
	    rec.addVariableField(df);
		df = factory.newDataField("863", ' ', '1');
	    df.addSubfield(factory.newSubfield('8', "3.36"));
	    df.addSubfield(factory.newSubfield('a', "106"));
	    df.addSubfield(factory.newSubfield('b', "3"));
	    df.addSubfield(factory.newSubfield('c', "482"));
	    df.addSubfield(factory.newSubfield('i', "2010"));
	    df.addSubfield(factory.newSubfield('j', "WIN"));
	    rec.addVariableField(df);
		solrFldMapTest.assertSolrFldHasNumValues(rec, fldName, 1);
		solrFldMapTest.assertSolrFldValue(rec, fldName, "lib -|- loc -|-  -|-  -|- v.106:pt.3:no.482 (2010:WIN)");

		rec = factory.newRecord();
		rec.addVariableField(cf);
		rec.addVariableField(df852);
		df = factory.newDataField("853", '2', ' ');
	    df.addSubfield(factory.newSubfield('8', "1"));
	    df.addSubfield(factory.newSubfield('a', "v."));
	    df.addSubfield(factory.newSubfield('i', "(year)"));
	    rec.addVariableField(df);
		df = factory.newDataField("863", ' ', '1');
	    df.addSubfield(factory.newSubfield('8', "1.11"));
	    df.addSubfield(factory.newSubfield('a', "105"));
	    df.addSubfield(factory.newSubfield('i', "2009"));
	    rec.addVariableField(df);
		solrFldMapTest.assertSolrFldHasNumValues(rec, fldName, 1);
		solrFldMapTest.assertSolrFldValue(rec, fldName, "lib -|- loc -|-  -|-  -|- v.105 (2009)");

		rec = factory.newRecord();
		rec.addVariableField(cf);
		rec.addVariableField(df852);
		df = factory.newDataField("853", '2', ' ');
	    df.addSubfield(factory.newSubfield('8', "1"));
	    df.addSubfield(factory.newSubfield('a', "v."));
	    df.addSubfield(factory.newSubfield('b', "no."));
	    df.addSubfield(factory.newSubfield('u', "52"));
	    df.addSubfield(factory.newSubfield('v', "r"));
	    df.addSubfield(factory.newSubfield('i', "(year)"));
	    df.addSubfield(factory.newSubfield('j', "(month)"));
	    df.addSubfield(factory.newSubfield('k', "(day)"));
	    rec.addVariableField(df);
		df = factory.newDataField("863", ' ', '1');
	    df.addSubfield(factory.newSubfield('8', "1.569"));
	    df.addSubfield(factory.newSubfield('a', "205"));
	    df.addSubfield(factory.newSubfield('b', "10"));
	    df.addSubfield(factory.newSubfield('i', "2011"));
	    df.addSubfield(factory.newSubfield('j', "03"));
	    df.addSubfield(factory.newSubfield('k', "9"));
	    rec.addVariableField(df);
		solrFldMapTest.assertSolrFldHasNumValues(rec, fldName, 1);
		solrFldMapTest.assertSolrFldValue(rec, fldName, "lib -|- loc -|-  -|-  -|- v.205:no.10 (2011:March 9)");
	}

	/**
	 * per https://jirasul.stanford.edu/jira/browse/SW-885, "library has"
	 *  should include sub z in addition to sub a  from  866/867/868
	 */
@Test
	public void testSubZ866()
	{
		Record rec = factory.newRecord();
		ControlField cf = factory.newControlField("001", "asubz866");
		rec.addVariableField(cf);
		// from http://searchworks.stanford.edu/view/48690
		DataField df852 = factory.newDataField("852", ' ', ' ');
		df852.addSubfield(factory.newSubfield('a', "CSt"));
		df852.addSubfield(factory.newSubfield('b', "lib"));
		df852.addSubfield(factory.newSubfield('c', "loc"));
		rec.addVariableField(df852);
		DataField df = factory.newDataField("866", ' ', '0');
	    df.addSubfield(factory.newSubfield('8', "1"));
	    df.addSubfield(factory.newSubfield('a', "pt.1-4"));
	    df.addSubfield(factory.newSubfield('z', "<v.3,16,27-28 in series>"));
	    rec.addVariableField(df);

	    rec.addVariableField(df852);
		df = factory.newDataField("866", '8', '1');
	    df.addSubfield(factory.newSubfield('8', "1"));
	    df.addSubfield(factory.newSubfield('a', "pt.2"));
	    df.addSubfield(factory.newSubfield('z', "<v.16 in series>"));
	    rec.addVariableField(df);

	    rec.addVariableField(df852);
		df = factory.newDataField("866", '8', '1');
	    df.addSubfield(factory.newSubfield('8', "2"));
	    df.addSubfield(factory.newSubfield('a', "pt.5"));
	    rec.addVariableField(df);
		solrFldMapTest.assertSolrFldHasNumValues(rec, fldName, 3);
		solrFldMapTest.assertSolrFldValue(rec, fldName, "lib -|- loc -|-  -|- pt.1-4 <v.3,16,27-28 in series> -|- ");
		solrFldMapTest.assertSolrFldValue(rec, fldName, "lib -|- loc -|-  -|- pt.2 <v.16 in series> -|- ");
		solrFldMapTest.assertSolrFldValue(rec, fldName, "lib -|- loc -|-  -|- pt.5 -|- ");
	}


@Test
	public void testSubZ867()
	{
		Record rec = factory.newRecord();
		ControlField cf = factory.newControlField("001", "asubz867");
		rec.addVariableField(cf);
		DataField df852 = factory.newDataField("852", ' ', ' ');
		df852.addSubfield(factory.newSubfield('a', "CSt"));
		df852.addSubfield(factory.newSubfield('b', "lib"));
		df852.addSubfield(factory.newSubfield('c', "loc"));
		rec.addVariableField(df852);
		DataField df = factory.newDataField("867", ' ', '0');
	    df.addSubfield(factory.newSubfield('8', "1"));
	    df.addSubfield(factory.newSubfield('a', "first"));
	    df.addSubfield(factory.newSubfield('z', "subz"));
	    rec.addVariableField(df);
		df = factory.newDataField("867", ' ', '0');
	    df.addSubfield(factory.newSubfield('8', "1"));
	    df.addSubfield(factory.newSubfield('a', "second"));
	    rec.addVariableField(df);
		solrFldMapTest.assertSolrFldHasNumValues(rec, fldName, 2);
		solrFldMapTest.assertSolrFldValue(rec, fldName, "lib -|- loc -|-  -|- Supplement: first subz -|- ");
		solrFldMapTest.assertSolrFldValue(rec, fldName, "lib -|- loc -|-  -|- Supplement: second -|- ");

		df = factory.newDataField("866", '3', '1');
	    df.addSubfield(factory.newSubfield('8', "0"));
	    df.addSubfield(factory.newSubfield('a', "v.188"));
	    rec.addVariableField(df);
		solrFldMapTest.assertSolrFldHasNumValues(rec, fldName, 3);
		solrFldMapTest.assertSolrFldValue(rec, fldName, "lib -|- loc -|-  -|- v.188 -|- ");
	}

@Test
	public void testSubZ868()
	{
		Record rec = factory.newRecord();
		ControlField cf = factory.newControlField("001", "asubz868");
		rec.addVariableField(cf);
		DataField df852 = factory.newDataField("852", ' ', ' ');
		df852.addSubfield(factory.newSubfield('a', "CSt"));
		df852.addSubfield(factory.newSubfield('b', "lib"));
		df852.addSubfield(factory.newSubfield('c', "loc"));
		rec.addVariableField(df852);
		DataField df = factory.newDataField("868", ' ', '0');
	    df.addSubfield(factory.newSubfield('8', "1"));
	    df.addSubfield(factory.newSubfield('a', "first"));
	    df.addSubfield(factory.newSubfield('z', "subz"));
	    rec.addVariableField(df);
		df = factory.newDataField("868", ' ', '0');
	    df.addSubfield(factory.newSubfield('8', "1"));
	    df.addSubfield(factory.newSubfield('a', "second"));
	    rec.addVariableField(df);
		solrFldMapTest.assertSolrFldHasNumValues(rec, fldName, 2);
		solrFldMapTest.assertSolrFldValue(rec, fldName, "lib -|- loc -|-  -|- Index: first subz -|- ");
		solrFldMapTest.assertSolrFldValue(rec, fldName, "lib -|- loc -|-  -|- Index: second -|- ");

		df = factory.newDataField("866", '3', '1');
	    df.addSubfield(factory.newSubfield('8', "0"));
	    df.addSubfield(factory.newSubfield('a', "v.188"));
	    rec.addVariableField(df);
		solrFldMapTest.assertSolrFldHasNumValues(rec, fldName, 3);
		solrFldMapTest.assertSolrFldValue(rec, fldName, "lib -|- loc -|-  -|- v.188 -|- ");
	}


	/**
	 * per https://jirasul.stanford.edu/jira/browse/VUF-2617,
	 *  "latest" part of mhldDisplay should be populated ... but only when
	 *   853 pattern matches latest rec'd 863.
	 */
@Test
	public void testLatestShouldBePopulated()
	{
		Record rec = factory.newRecord();
		ControlField cf = factory.newControlField("001", "alatest");
		rec.addVariableField(cf);
		// from http://searchworks.stanford.edu/view/3454845
		DataField df852 = factory.newDataField("852", ' ', ' ');
		df852.addSubfield(factory.newSubfield('a', "CSt"));
		df852.addSubfield(factory.newSubfield('b', "lib"));
		df852.addSubfield(factory.newSubfield('c', "loc"));
		df852.addSubfield(factory.newSubfield('=', "41906"));
		rec.addVariableField(df852);

		DataField df = factory.newDataField("853", '2', ' ');
	    df.addSubfield(factory.newSubfield('8', "2"));
	    df.addSubfield(factory.newSubfield('a', "(year)"));
	    rec.addVariableField(df);
		df = factory.newDataField("853", '2', ' ');
	    df.addSubfield(factory.newSubfield('8', "3"));
	    df.addSubfield(factory.newSubfield('a', "(year)"));
	    df.addSubfield(factory.newSubfield('b', "pt."));
	    df.addSubfield(factory.newSubfield('u', "2"));
	    df.addSubfield(factory.newSubfield('v', "r"));
	    rec.addVariableField(df);
		df = factory.newDataField("866", '3', '1');
	    df.addSubfield(factory.newSubfield('8', "1"));
	    df.addSubfield(factory.newSubfield('a', "2003,2006-"));
	    rec.addVariableField(df);
		df = factory.newDataField("863", ' ', '1');
	    df.addSubfield(factory.newSubfield('8', "2.1"));
	    df.addSubfield(factory.newSubfield('a', "2003"));
	    rec.addVariableField(df);
		df = factory.newDataField("863", ' ', '1');
	    df.addSubfield(factory.newSubfield('8', "3.1"));
	    df.addSubfield(factory.newSubfield('a', "2006"));
	    df.addSubfield(factory.newSubfield('b', "1"));
	    rec.addVariableField(df);
		df = factory.newDataField("863", ' ', '1');
	    df.addSubfield(factory.newSubfield('8', "3.2"));
	    df.addSubfield(factory.newSubfield('a', "2006"));
	    df.addSubfield(factory.newSubfield('b', "2"));
	    rec.addVariableField(df);
		df = factory.newDataField("863", ' ', '1');
	    df.addSubfield(factory.newSubfield('8', "3.3"));
	    df.addSubfield(factory.newSubfield('a', "2011"));
	    df.addSubfield(factory.newSubfield('b', "1"));
	    rec.addVariableField(df);
		df = factory.newDataField("863", ' ', '1');
	    df.addSubfield(factory.newSubfield('8', "3.4"));
	    df.addSubfield(factory.newSubfield('a', "2011"));
	    df.addSubfield(factory.newSubfield('b', "2"));
	    rec.addVariableField(df);

		solrFldMapTest.assertSolrFldHasNumValues(rec, fldName, 1);
		solrFldMapTest.assertSolrFldValue(rec, fldName, "lib -|- loc -|-  -|- 2003,2006- -|- 2011:pt.2");
	}
}