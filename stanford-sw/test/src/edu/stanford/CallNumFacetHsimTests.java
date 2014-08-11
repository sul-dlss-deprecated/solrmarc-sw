package edu.stanford;

import java.io.File;
import java.util.*;

import org.junit.*;
import org.marc4j.marc.*;
import org.solrmarc.tools.SolrMarcIndexerException;

import edu.stanford.enumValues.CallNumberType;

/**
 * tests for callnum_facet_hsim field, which is for the call number facet (hierarchical)
 * @author Naomi Dushay
 */
public class CallNumFacetHsimTests extends AbstractStanfordTest
{
	private final String fldName = "callnum_facet_hsim";
	private final MarcFactory factory = MarcFactory.newInstance();

@Before
	public void setup()
	{
		mappingTestInit();
	}

//---- call numbers excluded for various reasons ------

	/** no value assigned if unexpected callnum type from 999w */
@Test
	public void excludedCallnumTypes()
	{
		String lcCallnum = "M123 .M234";
		Record record = getRecordWith999(lcCallnum, CallNumberType.ALPHANUM);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999(lcCallnum, CallNumberType.HARVYENCH);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999(lcCallnum, CallNumberType.OTHER);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999(lcCallnum, CallNumberType.THESIS);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999(lcCallnum, CallNumberType.XX);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999(lcCallnum, "ASIS");
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("(XX.4300523)", "AUTO"); // 4300523
		solrFldMapTest.assertNoSolrFld(record, fldName);
	}

	/** no value assigned if it's a weird LC callnum from Lane-Med */
@Test
	public void badLcLaneCallnum()
	{
		String badLC = "notLC";

		// invalid LC from Lane -- this should set item.hasBadLcLaneCallnum
		DataField df999 = get999(badLC, "LC");
		df999.addSubfield(factory.newSubfield('m', "LANE-MED"));
		Record record = factory.newRecord();
		record.addVariableField(df999);
		solrFldMapTest.assertNoSolrFld(record, fldName);

		// valid LC from Lane
		df999 = get999("M123 .M456", "LC");
		df999.addSubfield(factory.newSubfield('m', "LANE-MED"));
		record = factory.newRecord();
		record.addVariableField(df999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|M - Music|M - Music");

		// invalid LC not from Lane
		df999 = get999(badLC, "LC");
		df999.addSubfield(factory.newSubfield('m', "GREEN"));
		record = factory.newRecord();
		record.addVariableField(df999);
		solrFldMapTest.assertNoSolrFld(record, fldName);
	}

	/** assign value for valid LC even if it's a shelve-by location */
//@Test  FIXME
	public void shelbyLoc()
	{
		Set<String> shelbyLocs = new HashSet<String>();
		shelbyLocs.addAll(StanfordIndexer.SHELBY_LOCS);
		shelbyLocs.addAll(StanfordIndexer.BIZ_SHELBY_LOCS);
		for (String loc : shelbyLocs)
		{
			// valid LC
			DataField df999 = get999("M123 .M456", "LC");
			df999.addSubfield(factory.newSubfield('l', loc));
			Record record = factory.newRecord();
			record.addVariableField(df999);
			Assert.fail("FIXME: we DO want a value if there is valid LC for shelby location");
			solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|M - Music|M - Music");

			// invalid LC
			df999 = get999("not valid!", "LC");
			df999.addSubfield(factory.newSubfield('l', loc));
			record = factory.newRecord();
			record.addVariableField(df999);
			solrFldMapTest.assertNoSolrFld(record, fldName);

			// invalid Dewey
			df999 = get999("not valid!", "DEWEY");
			df999.addSubfield(factory.newSubfield('l', loc));
			record = factory.newRecord();
			record.addVariableField(df999);
			solrFldMapTest.assertNoSolrFld(record, fldName);
		}

		// Hopkins weird Shelby
		Record record = getRecordWith999(" 1976", "LCPER"); // 404891
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("1976", "LCPER");
		solrFldMapTest.assertNoSolrFld(record, fldName);
	}

	/** no value if item is missing or lost */
@Test
	public void missingOrLost()
	{
		for (String loc : StanfordIndexer.MISSING_LOCS)
		{
			// valid LC
			DataField df999 = get999("M123 .M456", "LC");
			df999.addSubfield(factory.newSubfield('l', loc));
			Record record = factory.newRecord();
			record.addVariableField(df999);
			try
			{
				solrFldMapTest.assertNoSolrFld(record, fldName);
			}
			catch (SolrMarcIndexerException e)
			{
				Assert.assertEquals("Record  purposely not indexed because item_display field is empty", e.getMessage());
			}

			// invalid LC
			df999 = get999("not valid!", "LC");
			df999.addSubfield(factory.newSubfield('l', loc));
			record = factory.newRecord();
			record.addVariableField(df999);
			try
			{
				solrFldMapTest.assertNoSolrFld(record, fldName);
			}
			catch (SolrMarcIndexerException e)
			{
				Assert.assertEquals("Record  purposely not indexed because item_display field is empty", e.getMessage());
			}

			// valid Dewey
			df999 = get999("123.4 .B45", "DEWEY");
			df999.addSubfield(factory.newSubfield('l', loc));
			record = factory.newRecord();
			record.addVariableField(df999);
			try
			{
				solrFldMapTest.assertNoSolrFld(record, fldName);
			}
			catch (SolrMarcIndexerException e)
			{
				Assert.assertEquals("Record  purposely not indexed because item_display field is empty", e.getMessage());
			}

			// invalid Dewey
			df999 = get999("not valid!", "DEWEY");
			df999.addSubfield(factory.newSubfield('l', loc));
			record = factory.newRecord();
			record.addVariableField(df999);
			try
			{
				solrFldMapTest.assertNoSolrFld(record, fldName);
			}
			catch (SolrMarcIndexerException e)
			{
				Assert.assertEquals("Record  purposely not indexed because item_display field is empty", e.getMessage());
			}
		}
	}

	/** no value assigned if we're supposed to ignore callnum */
//@Test  FIXME
	public void ignoredCallnum()
	{
		Assert.fail("FIXME: we DO want a value for INTERNET or NO CALLNUM, either from the bib, or if there is a valid callnum after INTERNET RESOURCE");

		/* Item constructor:
		if (StanfordIndexer.SKIPPED_CALLNUMS.contains(rawCallnum)
				|| rawCallnum.startsWith(ECALLNUM)
				|| rawCallnum.startsWith(TMP_CALLNUM_PREFIX))
			hasIgnoredCallnum = true;
		else
			hasIgnoredCallnum = false;
		*/
		Record record = getRecordWith999(Item.ECALLNUM + "stuff", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999(Item.TMP_CALLNUM_PREFIX + "stuff", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// skipped callnums
		for (String skippedCallnum : StanfordIndexer.SKIPPED_CALLNUMS)
		{
			record = getRecordWith999(skippedCallnum, CallNumberType.LC);
			solrFldMapTest.assertNoSolrFld(record, fldName);
		}

		// dewey
		record = getRecordWith999(Item.ECALLNUM + "stuff", CallNumberType.DEWEY);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999(Item.TMP_CALLNUM_PREFIX + "stuff", CallNumberType.DEWEY);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// skipped callnums
		for (String skippedCallnum : StanfordIndexer.SKIPPED_CALLNUMS)
		{
			record = getRecordWith999(skippedCallnum, CallNumberType.DEWEY);
			solrFldMapTest.assertNoSolrFld(record, fldName);
		}
	}

	/** no value if call number is null, empty string, or normalizes to empty string */
@Test
	public void emptyCallnums()
	{
		Record record = getRecordWith999(null, CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999(" ", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999(". . ", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// dewey
		record = getRecordWith999(null, CallNumberType.DEWEY);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("", CallNumberType.DEWEY);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999(" ", CallNumberType.DEWEY);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999(". . ", CallNumberType.DEWEY);
		solrFldMapTest.assertNoSolrFld(record, fldName);
	}

//---- END call numbers excluded for various reasons ----------------------

//---- LC call numbers ----------------------------------------------------

	/**
	 * when all items are online and/or all items have ignored callnums,
	 *  then we look for an LC call number first in 050, then in 090 and
	 *  if we find a good one, we use it for facet and browsing
	 */
@Test
	public void hasSeparateBrowseCallnum()
	{
		// 1 item online, type LC:  no 050 or 090
		Record record = getRecordWith999("INTERNET RESOURCE", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// add 090 - get value from 090
		DataField df090 = factory.newDataField("090", ' ', ' ');
		df090.addSubfield(factory.newSubfield('a', "QM142"));
		df090.addSubfield(factory.newSubfield('b', ".A84 2010"));
		record.addVariableField(df090);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|Q - Science|QM - Human Anatomy");
		// add 050 - get value from 050 instead of 090
		DataField df050 = factory.newDataField("050", '1', '4');
		df050.addSubfield(factory.newSubfield('a', "QA76.76.C672"));
		record.addVariableField(df050);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|Q - Science|QA - Mathematics");
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);

		// 1 item ignored callnum, type ASIS:  no 050 or 090
		record = getRecordWith999(StanfordIndexer.SKIPPED_CALLNUMS.toArray()[0].toString(), "ASIS");
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// add 090 - get value from 090
		df090 = factory.newDataField("090", ' ', ' ');
		df090.addSubfield(factory.newSubfield('a', "QM142"));
		df090.addSubfield(factory.newSubfield('b', ".A84 2010"));
		record.addVariableField(df090);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|Q - Science|QM - Human Anatomy");
		// add 050 - get value from 050 instead of 090
		df050 = factory.newDataField("050", '1', '4');
		df050.addSubfield(factory.newSubfield('a', "QA76.76.C672"));
		record.addVariableField(df050);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|Q - Science|QA - Mathematics");
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
	}


	/** LC call numbers that have 1 letter in the class */
@Test
	public void singleLetLC()
	{
		// a| D764.7 .K72 1990 w| LC c| 1 i| 36105043140537 l| STACKS m| SAL3  t| STKS-MONO
		Record record = getRecordWith999("D764.7 .K72 1990", CallNumberType.LC);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|D - World History|D - World History");
		// a| F1356 .M464 2005 w| LC c| 1 i| 36105122224160 l| STACKS m| GREEN t| STKS-MONO
		record = getRecordWith999("F1356 .M464 2005", CallNumberType.LC);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|F - History of the Americas (Local)|F - History of the Americas (Local)");
		// a| M2 .C17 L3 2005 w| LC c| 1 i| 36105114805240 l| SCORES m| MUSIC  t| SCORE
		record = getRecordWith999("M2 .C17 L3 2005", CallNumberType.LC);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|M - Music|M - Music");
		// a| U897 .C87 Z55 2001 w| LC c| 1 i| 36105025784799 l| SOUTH-MEZZ m| SAL t| STKS-MONO x| LEVEL3OCL
		record = getRecordWith999("U897 .C87 Z55 2001", CallNumberType.LC);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|U - Military Science|U - Military Science");

		record = getRecordWith999("Z3871.Z8", CallNumberType.LC);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|Z - Bibliography, Library Science, Information Resources|Z - Bibliography, Library Science, Information Resources");
	}

	/** LC call numbers that have 2 letters in the class */
@Test
	public void twoLetLC()
	{
		Record record = getRecordWith999("QE538.8 .N36 1975-1977", CallNumberType.LC);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|Q - Science|QE - Geology");
		// a| BX4659 .E85 W44 w| LC c| 1 i| 36105037439663 l| STACKS m| GREEN t| STKS-MONO
		record = getRecordWith999("BX4659 .E85 W44", CallNumberType.LC);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|B - Philosophy, Psychology, Religion|BX - Christian Denominations");
		// a| HG6046 .V28 1986 w| LC c| 1 i| 36105034181003 l| STACKS m| SAL3 t| STKS-MONO
		record = getRecordWith999("HG6046 .V28 1986", CallNumberType.LC);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|H - Social Sciences|HG - Finance");
	}

	/** LC call numbers that have 3 letters in the class */
@Test
	public void threeLetLC()
	{
		Record record = getRecordWith999("KKX500 .S98 2005", CallNumberType.LC); // 6830340
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|K - Law|KKX - Law of Turkey");
		// a| KJV4189 .A67 A15 2014 w| LC c| 1 i| 36105220732411 l| INPROCESS m| SAL3 t| STKS-MONO
		record = getRecordWith999("KJV4189 .A67 A15 2014", CallNumberType.LC);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|K - Law|KJV - Law of France");
	}

	/** LC class code not in map */
@Test
	public void classNotInMap()
	{
		// a| KFC1050 .C35 2014 w| LC c| 1 i| 36105064459949 k| CHECKEDOUT l| STACKS-2 m| LAW  t| LAW-STKS
		Record record = getRecordWith999("KFC1050 .C35 2014", CallNumberType.LC);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|K - Law|KFC");
	}

	/** multiple 999s with the same LC class */
@Test
	public void multLCSame()
	{
		Record record = getRecordWith999("ML171 .L38 2005", CallNumberType.LC);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|M - Music|ML - Literature on Music");
		record.addVariableField(get999("M2 .C17 L3 2005", "LC"));
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|M - Music|M - Music");
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|M - Music|ML - Literature on Music");
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 2);
	}

	/** multiple 999s with diff LC classes */
@Test
	public void multLCDiff()
	{
		// 5875790
		// a| ML171 .L38 2005 w| LC c| 1 i| 36105119997455 l| STACKS m| GREEN  t| STKS-MONO
		Record record = getRecordWith999("ML171 .L38 2005", CallNumberType.LC);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|M - Music|ML - Literature on Music");
		// a| M2 .C17 L3 2005 w| LC c| 1 i| 36105114805240 l| SCORES m| MUSIC  t| SCORE
		record.addVariableField(get999("M2 .C17 L3 2005", "LC"));
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|M - Music|M - Music");
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|M - Music|ML - Literature on Music");
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 2);
	}

	/** type is LCPER */
@Test
	public void LCPer()
	{
		// a| K6 .A2173 V.25:NO.1-6 2007 w| LCPER c| 1 i| 36105124763553 l| STACKS m| SAL3 t| STKS-PERI
		Record record = getRecordWith999("K6 .A2173 V.25:NO.1-6 2007", "LCPER"); //5319829
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|K - Law|K - Law");
		// a| H8 .G55 V.40:NO.1-4 1999:JAN.-AUG. w| LCPER c| 1 i| 36105112743351  l| STACKS m| GREEN t| STKS-PERI
		record = getRecordWith999("H8 .G55 V.40:NO.1-4 1999:JAN.-AUG.", "LCPER");  // 4208298
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|H - Social Sciences|H - Social Sciences");
		// a| E184 .S75 R47A V.1 1980 w| LCPER c| 1 i| 36105007402873 l| STACKS m| GREEN t| STKS-MONO
		record = getRecordWith999("E184 .S75 R47A V.1 1980", "LCPER");  // 4208298
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|E - History of the Americas (General)|E - History of the Americas (General)");
		// a| QE538.8 .N36 1975-1977 w| LCPER c| 2 i| 36105004617465 l| STACKS m| EARTH-SCI t| STKS
		record = getRecordWith999("QE538.8 .N36 1975-1977", "LCPER");
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|Q - Science|QE - Geology");
		// harvard yenching
		record = getRecordWith999("4488.301 0300 2005 CD-ROM", "LCPER");  // 4208298
		solrFldMapTest.assertNoSolrFld(record, fldName);
	}

	/** call numbers that are LC, but have scheme listed as Dewey or Alphanum or Other */
//@Test   FIXME
	public void LCwrongScheme()
	{
		Assert.fail("FIXME: we DO want a value when it's an LC call number typed as Dewey, for sure");

		Record record = getRecordWith999("QE538.8 .N36 1975-1977", CallNumberType.DEWEY);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|Q - Science|QE - Geology");
		record = getRecordWith999("QE538.8 .N36 1975-1977", CallNumberType.ALPHANUM);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|Q - Science|QE - Geology");
		record = getRecordWith999("QE538.8 .N36 1975-1977", CallNumberType.OTHER);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|Q - Science|QE - Geology");
	}

//---- END LC call numbers --------------------------------------------------

//---- invalid LC call numbers --------------------------------

	/** no value if call number doesn't validate as LC */
@Test
	public void invalidLC()
	{
		// bad Cutter
		Record record = getRecordWith999("QE538.8 .NB36 1975-1977", CallNumberType.DEWEY);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// paren start char
		record = getRecordWith999("(V) JN6695 .I28 1999 COPY", CallNumberType.LC);  // 4532699
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// a| ??? w| LC c| 1 i| LL204129 l| ASK@LANE m| LANE-MED r| N s| Y t| MEDICAL
		record = getRecordWith999("???", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// weird callnums
		record = getRecordWith999("158613F868 .C45 N37 2000", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("5115126059 A17 2004", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("70 03126", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
	}

	/** LC call numbers starting with illegal letter */
@Test
	public void illegalStartLC()
	{
		// a| INTERNET RESOURCE KF3400 .S36 2009 w| LC c| 1 i| 7864015-2001 l| INTERNET m| LAW r| Y s| Y t| ONLINE
		Record record = getRecordWith999("INTERNET RESOURCE KF3400 .S36 2009", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
// FIXME:  TODO:
//		Assert.fail("FIXME: we DO want a value for INTERNET or NO CALLNUM, either from the bib, or if there is a valid callnum after INTERNET RESOURCE");
//		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|K - Law|KF - Law of the U.S.");
		// a| INTERNET RESOURCE GALE EZPROXY w| LC c| 1 i| 7157752-2001 l| INTERNET m| LAW r| Y s| Y t| ONLINE
		record = getRecordWith999("INTERNET RESOURCE GALE EZPROXY", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// should be govdoc
		// 	a| ICAO DOC 4444/15TH ED w| LC c| 1 i| 36105133579198 l| INTL-DOCS m| GREEN  t| GOVSTKS
		record = getRecordWith999("ICAO DOC 4444/15TH ED", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// a| ORNL-6371 w| LC c| 1 i| 36105114013282 l| TECH-RPTS m| EARTH-SCI r| Y s| Y t| EASTK-DOC
		record = getRecordWith999("ORNL-6371", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// a| X X w| LC c| 1 i| 36105095640137 l| JAPANESE m| EAST-ASIA r| Y s| Y t| STKS-MONO
		record = getRecordWith999("X X", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// a| XM98-1 NO.1 w| LC c| 1 i| 36105023263168 l| MAP-CASES m| EARTH-SCI r| Y s| Y t| EASTK-DOC
		record = getRecordWith999("XM98-1 NO.1", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// a| XX(6661112.1) w| LC c| 1 i| 6661112-1001 k| INPROCESS l| MEDIA-MTXT m| GREEN r| Y s| Y t| NH-DVDCD
		record = getRecordWith999("XX(6661112.1)", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// a| YBP1834690 w| LC c| 1 i| 4761801-4001 k| INPROCESS l| RARE-BOOKS m| SPEC-COLL
		record = getRecordWith999("YBP1834690", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
	}

	/** call numbers that are alphanum, but have scheme listed as LC */
@Test
	public void testAlphanumTypedAsLC()
	{
		Record record = getRecordWith999("1ST AMERICAN BANCORP, INC.", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("2 B SYSTEM INC.", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("202 DATA SYSTEMS, INC.", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
	}

	/** unusual Lane (med school) call numbers */
@Test
	public void testWeirdLaneCallnums()
	{
		Record record = getRecordWith999("1.1", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("20.44", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("4.15[C]", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("6.4C-CZ[BC]", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
	}

	/** Harvard Yenching call numbers */
@Test
	public void harvardYenching()
	{
		Record record = getRecordWith999("3781 2009 T", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("2345 5861 V.3", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("2061 4246 NO.5-6 1936-1937", CallNumberType.ALPHANUM);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("4362 .S12P2 1965 .C3", CallNumberType.LC);  // 8215917
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("4861.1 3700 1989:NO.4-6", CallNumberType.ALPHANUM); // 5319829
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("4488.301 0300 2005 CD-ROM", "LCPER");  // 4208298
		solrFldMapTest.assertNoSolrFld(record, fldName);
	}

	/** weird in process call numbers that should NOT create a value */
@Test
	public void inProcess()
	{
		Record record = getRecordWith999("001AQJ5818", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("(XX.4300523)", "AUTO"); // 4300523
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// EDI in process
		record = getRecordWith999("427331959", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// Japanese
		record = getRecordWith999("7926635", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("7890569-1001", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("7885324-1001-2", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// Rare
		record = getRecordWith999("741.5 F", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("(ADL4044.1)XX", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("(XX.4300523)", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
	   // math-cs tech-reports  (home Loc TECH-RPTS)
		record = getRecordWith999("134776", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("262198", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
	}


//---- END invalid LC call numbers --------------------------------


//---- DEWEY call numbers --------------------------------

	/** dewey examples from our data */
@Test
	public void dewey()
	{
		// a| 159.32 .W211 w| DEWEY c| 1 i| 36105046693508 l| STACKS m| SAL3 t| STKS-MONO
		Record record = getRecordWith999("159.32 .W211", CallNumberType.DEWEY);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|100s - Philosophy & Psychology|150s - Psychology");
		// a| 550.6 .U58P NO.1707 w| DEWEY c| 1 i| 7732531-1001 l| STACKS m| EARTH-SCI t| EASTK-DOC  x| MARCIVE
		record = getRecordWith999("550.6 .U58P NO.1707", CallNumberType.DEWEY);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|500s - Science|550s - Earth Sciences");
	}

	/** dewey call numbers with and without 1 or 2 leading zeroes */
@Test
	public void deweyLeadingZeros()
	{
		// a| 062 .B862 V.193 w| DEWEY c| 1 i| 36105221123552 l| SOUTH-MEZZ m| SAL t| STKS-MONO
		Record record = getRecordWith999("062 .B862 V.193", CallNumberType.DEWEY);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|000s - Computer Science, Information & General Works|060s - General Organization & Museology");
		record = getRecordWith999("62 .B862 V.193", CallNumberType.DEWEY);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|000s - Computer Science, Information & General Works|060s - General Organization & Museology");

		// a| 002 U73 w| DEWEY c| 1 i| 36105094464133 l| ND-PAGE-EA m| SAL t| STKS-MONO
		record = getRecordWith999("002 U73", CallNumberType.DEWEY);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|000s - Computer Science, Information & General Works|000s - Computer Science, Information & General Works");
		record = getRecordWith999("2 U73", CallNumberType.DEWEY);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|000s - Computer Science, Information & General Works|000s - Computer Science, Information & General Works");
	}

	/* single record has multiple 999s with same dewey facet value */
@Test
	public void multDeweySame()
	{
		// a| 370.6 .N28 V.113:PT.1 w| DEWEY c| 1 i| 36105212633767 k| INPROCESS l| STACKS m| EDUCATION t| STKS-MONO
		Record record = getRecordWith999("370.6 .N28 V.113:PT.1", CallNumberType.DEWEY);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|300s - Social Sciences|370s - Education");
		// a| 370.6 .N28 V.106:PT.1 w| DEWEY c| 1 i| 36105124266961 l| STACKS m| EDUCATION t| STKS-MONO z| DIGI-SENT
		record.addVariableField(get999("370.6 .N28 V.106:PT.1", "DEWEY"));
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|300s - Social Sciences|370s - Education");
		// a| 370.6 .N28 V.106:PT.1 w| DEWEY c| 2 i| 36105124266979 l| STACKS m| EDUCATION t| STKS-MONO
		record.addVariableField(get999("370.6 .N28 V.106:PT.1", "DEWEY"));
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|300s - Social Sciences|370s - Education");
	}

	/** single record has multiple 999s with different dewey call numbers */
@Test
	public void multDeweyDiff()
	{
		// a| 518 .M161 w| DEWEY c| 1 i| 36105046454513 l| STACKS m| SAL3 t| STKS-MONO
		Record record = getRecordWith999("518 .M161", CallNumberType.DEWEY);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|500s - Science|510s - Mathematics");
		// a| 061 .R496 V.39:NO.4 w| DEWEY c| 1 i| 526284-4001 l| SOUTH-MEZZ m| SAL t| STKS-MONO
		record.addVariableField(get999("061 .R496 V.39:NO.4", "DEWEY"));
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 2);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|000s - Computer Science, Information & General Works|060s - General Organization & Museology");
	}

	/** single record with both LC and Dewey callnums */
@Test
	public void lcAndDewey()
	{
		// a| PR5190 .P3 Z48 2011 w| LC c| 1 i| 36105218632789 l| STACKS m| GREEN t| STKS-MONO
		// a| 968.006 .V274 SER.2:NO.42 w| DEWEY c| 1 i| 36105218758519 l| STACKS m| SAL3  t| STKS-MONO
		Record record = getRecordWith999("PR5190 .P3 Z48 2011", CallNumberType.LC);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|P - Language & Literature|PR - English Literature");
		record.addVariableField(get999("968.006 .V274 SER.2:NO.42", "DEWEY"));
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 2);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|900s - History & Geography|960s - General History of Africa");

		// a| QE539.2 .P34 O77 2005 w| LC c| 1 i| 36105114582328 l| SOUTH-MEZZ m| SAL t| STKS-MONO
		// a| 550.6 .U58P NO.1707 w| DEWEY c| 1 i| 7732531-1001 l| STACKS m| EARTH-SCI t| EASTK-DOC x| MARCIVE
		record = getRecordWith999("QE539.2 .P34 O77 2005", CallNumberType.LC);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|Q - Science|QE - Geology");
		record.addVariableField(get999("550.6 .U58P NO.1707", "DEWEY"));
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 2);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|500s - Science|550s - Earth Sciences");
	}

	/** DEWEYPER classification */
@Test
	public void testDeweyPer()
	{
		//a| 550.6 .U58O 92-600 A w| DEWEYPER c| 1 i| 36105028076078 j| 2 l| MICROTEXT m| EARTH-SCI t| EASTK-DOC
		Record record = getRecordWith999("550.6 .U58O 92-600", "DEWEYPER");
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|500s - Science|550s - Earth Sciences");
	}

	/** call numbers that are Dewey, but have scheme listed as LC */
@Test
	public void deweyTypedAsLC()
	{
		Record record = getRecordWith999("180.8 D25 V.1", CallNumberType.LC);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|100s - Philosophy & Psychology|180s - Ancient, Medieval, Oriental Philosophy");
		record = getRecordWith999("219.7 K193L V.5", CallNumberType.LC);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|200s - Religion|210s - Natural Theology");
		//	a| 3.37 D621 w| LC c| 1 i| 36105116366597 l| UARCH-30 m| SPEC-COLL r| Y s| Y t| NONCIRC u| 5/6/2004
		record = getRecordWith999("3.37 D621", CallNumberType.LC);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|000s - Computer Science, Information & General Works|000s - Computer Science, Information & General Works");
	}

	/** TX call numbers that are actually Dewey, not LC */
// TODO:  @Test
	public void cubberleyTXAsDewey()
	{

	}

//---- END DEWEY call numbers --------------------------------

//---- invalid DEWEY call numbers --------------------------------

	/** no value for invalid Dewey call numbers */
@Test
	public void invalidDewey()
	{
		// 2 letter cutter
		Record record = getRecordWith999("180.8 DX25 V.1", CallNumberType.LC);
		solrFldMapTest.assertNoSolrFld(record, fldName);
	}

//---- END invalid DEWEY call numbers --------------------------------


//---- Gov Doc (call numbers) --------------------------------

	/**
	 * some real life gov doc examples
	 */
@Test
	public void testGovDocs()
	{
		String firstPart = CallNumUtils.GOV_DOC_TOP_FACET_VAL + "|";

		// due to gov doc location
		for (String govDocLoc : StanfordIndexer.GOV_DOC_LOCS)
		{
			DataField df999 = get999("ICAO DOC 4444/15TH ED", "ALPHANUM");
			df999.addSubfield(factory.newSubfield('l', govDocLoc));
			Record record = factory.newRecord();
			record.addVariableField(df999);
			solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		}

		// a| CALIF L425 .L52 w| ALPHANUM c| 1 i| 36105132406864 k| BINDERY l| CALIF-DOCS m| GREEN t| GOVSTKS u| 5/30/2014
		DataField df999 = get999("CALIF L425 .L52", "ALPHANUM");
		df999.addSubfield(factory.newSubfield('l', "CALIF-DOCS"));
		Record record = factory.newRecord();
		record.addVariableField(df999);
		solrFldMapTest.assertSolrFldValue(record, fldName, firstPart + CallNumUtils.GOV_DOC_CALIF_FACET_VAL);

		// a| ICAO DOC 4444/15TH ED w| ALPHANUM c| 1 i| 36105133579198 l| INTL-DOCS m| GREEN t| GOVSTKS
		df999 = get999("ICAO DOC 4444/15TH ED", "ALPHANUM");
		df999.addSubfield(factory.newSubfield('l', "INTL-DOCS"));
		record = factory.newRecord();
		record.addVariableField(df999);
		solrFldMapTest.assertSolrFldValue(record, fldName, firstPart + CallNumUtils.GOV_DOC_INTL_FACET_VAL);

		// a| I 19.76:97-600-C w| SUDOC c| 1 i| 36105050083034 l| SSRC-FICHE m| GREEN t| NONCIRC u| 11/12/1999 x| MARCIV
		df999 = get999("I 19.76:97-600-C", "SUDOC");
		df999.addSubfield(factory.newSubfield('l', "SSRC-FICHE"));
		record = factory.newRecord();
		record.addVariableField(df999);
		solrFldMapTest.assertSolrFldValue(record, fldName, firstPart + CallNumUtils.GOV_DOC_FED_FACET_VAL);

		// a| I 19.66:979-981 w| SUDOC c| 1 i| 36105122902526 l| FED-DOCS m| GREEN t| GOVSTKS
		df999 = get999("I 19.66:979-981", "SUDOC");
		df999.addSubfield(factory.newSubfield('l', "FED-DOCS"));
		record = factory.newRecord();
		record.addVariableField(df999);
		solrFldMapTest.assertSolrFldValue(record, fldName, firstPart + CallNumUtils.GOV_DOC_FED_FACET_VAL);

		// a| Y 3.2:C 44/C 76/2013+ERRATA w| SUDOC c| 1 i| 36105050649727 l| FED-DOCS m| GREEN t| GOVSTKS x| MARCIVE
		df999 = get999("Y 3.2:C 44/C 76/2013+ERRATA", "SUDOC");
		df999.addSubfield(factory.newSubfield('l', "FED-DOCS"));
		record = factory.newRecord();
		record.addVariableField(df999);
		solrFldMapTest.assertSolrFldValue(record, fldName, firstPart + CallNumUtils.GOV_DOC_FED_FACET_VAL);

		// callnum type is SUDOC
		df999 = get999("something", "SUDOC");
		df999.addSubfield(factory.newSubfield('l', "somewhere"));
		record = factory.newRecord();
		record.addVariableField(df999);
		solrFldMapTest.assertSolrFldValue(record, fldName, firstPart + CallNumUtils.GOV_DOC_UNKNOWN_FACET_VAL);


		// due to presence of 086
		DataField df086 = factory.newDataField("086", ' ', ' ');

		// a| ICAO DOC 4444/15TH ED w| ALPHANUM c| 1 i| 36105133579198 l| INTL-DOCS m| GREEN t| GOVSTKS
		df999 = get999("something", "ALPHANUM");
		df999.addSubfield(factory.newSubfield('l', "somewhere"));
		record = factory.newRecord();
		record.addVariableField(df999);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record.addVariableField(df086);  // not SUDOC, so recognized as gov doc by 086
		solrFldMapTest.assertSolrFldValue(record, fldName, firstPart + CallNumUtils.GOV_DOC_UNKNOWN_FACET_VAL);
	}

	private String fileName = "callNumberTests.mrc";
    private String testFilePath = testDataParentPath + File.separator + fileName;

	/**
	 * Call number facet should be gov doc if the "type"
	 *  of call number indicated in the 999 is "SUDOC" or if there is an 086
	 *  present
	 */
@Test
	public final void govDocCallnumFromSUDOC()
	{
	    String firstPart = CallNumUtils.GOV_DOC_TOP_FACET_VAL + "|";
		solrFldMapTest.assertSolrFldValue(testFilePath, "2557826", fldName, firstPart + CallNumUtils.GOV_DOC_FED_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5511738", fldName, firstPart + CallNumUtils.GOV_DOC_UNKNOWN_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "2678655", fldName, firstPart + CallNumUtils.GOV_DOC_FED_FACET_VAL);
	}

	/**
	 * Call number facet should be both the LC call number stuff AND
	 *  "Gov't Doc" if the "type" of call number is LC and the location is
	 *  a gov doc location.
	 * If the call number is labeled LC, but does not parse, and the location is
	 *  a gov doc location, then the facet should be gov doc only.
	 */
@Test
	public final void govDocCallnumFromLocation()
	{
	    testFilePath = testDataParentPath + File.separator + "callNumberGovDocTests.mrc";
	    String firstPart = CallNumUtils.GOV_DOC_TOP_FACET_VAL + "|";

		solrFldMapTest.assertSolrFldValue(testFilePath, "brit", fldName, firstPart + CallNumUtils.GOV_DOC_BRIT_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "calif", fldName, firstPart + CallNumUtils.GOV_DOC_CALIF_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "intl", fldName, firstPart + CallNumUtils.GOV_DOC_INTL_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "fed", fldName, firstPart + CallNumUtils.GOV_DOC_FED_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "ssrcdocs", fldName, firstPart + CallNumUtils.GOV_DOC_FED_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "ssrcfiche", fldName, firstPart + CallNumUtils.GOV_DOC_FED_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "ssrcnwdoc", fldName, firstPart + CallNumUtils.GOV_DOC_FED_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "sudoc", fldName, firstPart + CallNumUtils.GOV_DOC_UNKNOWN_FACET_VAL);

		// ensure item has LC call number AND item has gov doc location
		solrFldMapTest.assertSolrFldValue(testFilePath, "brit", fldName, CallNumUtils.LC_TOP_FACET_VAL + "|Z - Bibliography, Library Science, Information Resources|Z - Bibliography, Library Science, Information Resources");

		// but not dewey
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "ssrcfiche", fldName, CallNumUtils.DEWEY_TOP_FACET_VAL + "|300s - Social Sciences|300s - Social Sciences|370s - Education");
	}

	/** from single record with all three types of call numbers */
@Test
	public void govDocLCandDewey()
	{
		// a| I 19.76:98-600-B w| SUDOC c| 1 i| 36105050102552 l| SSRC-FICHE m| GREEN t| NONCIRC x| MARCIVE
		DataField df999 = get999("I 19.76:98-600-B", "SUDOC");
		df999.addSubfield(factory.newSubfield('l', "SSRC-FICHE"));
		Record record = factory.newRecord();
		record.addVariableField(df999);
		solrFldMapTest.assertSolrFldValue(record, fldName, CallNumUtils.GOV_DOC_TOP_FACET_VAL + "|" + CallNumUtils.GOV_DOC_FED_FACET_VAL);
		// a| 550.6 .U58O 00-600 A w| DEWEYPER c| 1 i| 36105118388904 l| STACKS m| EARTH-SCI t| EASTK-DOC
		record.addVariableField(get999("550.6 .U58O 00-600", "DEWEYPER"));
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 2);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey Classification|500s - Science|550s - Earth Sciences");
		// a| QE538.8 .N36 1985:APR. w| LCPER c| 2 i| 36105017155594 j| 4 l| STACKS m| EARTH-SCI t| EASTK-DOC
		record.addVariableField(get999("QE538.8 .N36 1985:APR.", "LCPER"));
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 3);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LC Classification|Q - Science|QE - Geology");
	}


//---- END Gov Doc (call numbers) --------------------------------


	/** typed as Alphanum, and clearly not LC or Dewey */
@Test
	public void alphanum()
	{
		Record record = getRecordWith999("71 15446 V.1", CallNumberType.ALPHANUM);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("4488.301 0300 2001 CD-ROM", CallNumberType.ALPHANUM); // 4208298  Harvard-Yenching?
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("8291.209 .A963 V.5 1971/1972", CallNumberType.ALPHANUM); // 485907  Harvard-Yenching?
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("\"NEW BEGINNING\" INVESTMENT RESERVE FUND", CallNumberType.ALPHANUM); // 9993464
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("\"21\" BRANDS, INCORPORATED", CallNumberType.ALPHANUM); // 9998560
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999(" \"LA CONSOLIDADA\", S.A", CallNumberType.ALPHANUM); // 9974001
		solrFldMapTest.assertNoSolrFld(record, fldName);
		record = getRecordWith999("(THE) NWNL COMPANIES, INC.", CallNumberType.ALPHANUM); // 10053060
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// a| ISHII SPRING 2009 w| ALPHANUM c| 1 i| 20503961216 l| PERM-RES m| BUSINESS t| NH-PERMRES
		record = getRecordWith999("ISHII SPRING 2009", CallNumberType.ALPHANUM);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// a| OYER WINTER 2012 w| ALPHANUM c| 1 i| 20504019931 l| PERM-RES m| BUSINESS t| NH-PERMRES
		record = getRecordWith999("OYER WINTER 2012", CallNumberType.ALPHANUM);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// a| O'REILLY FALL 2006 w| ALPHANUM c| 1 i| 20503291271 l| PERM-RES m| BUSINESS t| NH-PERMRES
		record = getRecordWith999("O'REILLY FALL 2006", CallNumberType.ALPHANUM);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// a| XV 852 w| ALPHANUM c| 1 i| 36105062129460 k| CHECKEDOUT l| OPEN-RES m| LAW t| NH-PERMRES
		record = getRecordWith999("XV 852", CallNumberType.ALPHANUM);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// a| YUGOSLAV SERIAL 1963 NO.5-6 w| ALPHANUM c| 1 i| 36105072215937 l| STACKS m| HOOVER t| PERIBND
		record = getRecordWith999("YUGOSLAV SERIAL 1963 NO.5-6", CallNumberType.ALPHANUM);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// a| SUSEL-69048 w| ALPHANUM c| 1 i| 36105046377987 l| STACKS m| SAL3 t| STKS-MONO
		record = getRecordWith999("SUSEL-69048", CallNumberType.ALPHANUM);
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// a| MFICHE 3239 w| ALPHANUM c| 1 i| 8729402-1001 l| MEDIA-MTXT m| GREEN r| Y s| Y t| NH-MICR
		record = getRecordWith999("MFICHE 3239", CallNumberType.ALPHANUM);
		solrFldMapTest.assertNoSolrFld(record, fldName);
	}

	/** type is ASIS */
@Test
	public void asis()
	{
		Record record = getRecordWith999("(ADL4044.1)XX", "ASIS"); // 689085
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// a| 134776 w| ASIS c| 1 i| 36105111124884 l| STACKS m| SAL3 t| STKS-MONO
		record = getRecordWith999("134776", "ASIS");
		solrFldMapTest.assertNoSolrFld(record, fldName);
		// a| INTERNET RESOURCE w| ASIS c| 1 i| 6280316-1001 l| INTERNET m| SUL t| SUL
		record = getRecordWith999("INTERNET RESOURCE", "ASIS");
		solrFldMapTest.assertNoSolrFld(record, fldName);
	}

	/** integration test to assert the index has a value for each level in the path */
	public void indexedAsHierarchy()
	{
		Assert.fail("need to write test asserting the indexed data gives a value for each level in the path");
	}



// ---- private methods below ---------------------------

	private Record getRecordWith999(String callnum, CallNumberType callNumberType)
	{
		return getRecordWith999(callnum, callNumberType.toString().toUpperCase());
	}

	private Record getRecordWith999(String callnum, String type)
	{
		Record record = factory.newRecord();
		record.addVariableField(get999(callnum, type));
		return record;
	}

	private DataField get999(String suba, String subw)
	{
		DataField df999 = factory.newDataField("999", ' ', ' ');
		if (suba != null)
			df999.addSubfield(factory.newSubfield('a', suba));
		df999.addSubfield(factory.newSubfield('w', subw));
		return df999;
	}
}
