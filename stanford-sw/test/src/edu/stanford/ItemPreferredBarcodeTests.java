package edu.stanford;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.*;

import edu.stanford.enumValues.CallNumberType;

/**
 * test choice of preferred item is correct.  It should be, per INDEX-153
 * 2014-09-29:
 *
 * 1. If Green item(s) have shelfkey, do this:
 * - pick the LC truncated callnum with the most items
 * - pick the shortest LC untruncated callnum if no truncation
 * - if no LC, got through callnum scheme order of preference:  LC, Dewey, Sudoc, Alphanum (without box and folder)
 * 2. If no Green shelfkey, use the above algorithm libraries (raw codes in 999) in alpha order.
 * @author ndushay
 */
public class ItemPreferredBarcodeTests extends AbstractStanfordTest
{
	String fldName = "preferred_barcode";
	MarcFactory factory = MarcFactory.newInstance();
	private final String GREEN_LIB_CODE = "GREEN";

@Before
	public void setup()
	{
		mappingTestInit();
	}

	/**
	 * no truncated callnums, but yes, shelfkeys:
	 *  choose Green if avail, callnum scheme priority order
	 * - pick the shortest LC untruncated callnum if no truncation
	 * - callnum scheme order of preference: LC, Dewey, Sudoc, Alphanum (without box and folder)
	 */
@Test
	public void preferredCallnumSchemeOrder()
	{
		Record record = factory.newRecord();

		// lc only
		DataField lc999 = get999("QE538.8 .N36 1975-1977", CallNumberType.LC, "LCbarcode", GREEN_LIB_CODE);
		record.addVariableField(lc999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LCbarcode");
		// lc + dewey
		DataField dewey999 = get999("159.32 .W211", CallNumberType.DEWEY, "DeweyBarcode", GREEN_LIB_CODE);
		record.addVariableField(dewey999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LCbarcode");
		// lc + dewey + sudoc
		DataField sudoc999 = get999("I 19.76:98-600-B", CallNumberType.SUDOC, "SudocBarcode", GREEN_LIB_CODE);
		record.addVariableField(sudoc999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LCbarcode");
		// lc + dewey + sudoc + alphanum
		DataField alpha999 = get999("ISHII SPRING 2009", CallNumberType.ALPHANUM, "AlphanumBarcode", GREEN_LIB_CODE);
		record.addVariableField(alpha999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LCbarcode");

		// dewey + sudoc + alphanum
		record.removeVariableField(lc999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "DeweyBarcode");
		// sudoc + alphanum
		record.removeVariableField(dewey999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "SudocBarcode");
		// alphanum only
		record.removeVariableField(sudoc999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "AlphanumBarcode");

		// dewey and alphanum
		record.addVariableField(dewey999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "DeweyBarcode");
		// lc and alphanum
		record.removeVariableField(dewey999);
		record.addVariableField(lc999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LCbarcode");
	}

	/**
	 * the callnum scheme is more important than the truncation (num items) for
	 * preferred barcode.
	 * e.g. an LC untruncated is prefered over a Dewey truncated in the same library
	 */
@Test
	public void preferCallNumSchemeBeforeTruncation()
	{
		Record record = factory.newRecord();

		// lc untruncated
		DataField lc999 = get999("QE538.8 .N36 1975-1977", CallNumberType.LC, "LCbarcode", GREEN_LIB_CODE);
		record.addVariableField(lc999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LCbarcode");
		// lc untrunc + dewey truncated
		DataField dewey1 = get999("888.4 .J788 V.5", CallNumberType.DEWEY, "Dewey1", GREEN_LIB_CODE);
		record.addVariableField(dewey1);
		DataField dewey2 = get999("888.4 .J788 V.6", CallNumberType.DEWEY, "Dewey2", GREEN_LIB_CODE);
		record.addVariableField(dewey2);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LCbarcode");
		// lc untrunc + dewey truncated + sudoc trunc
		DataField sudoc1 = get999("Y 4.G 74/7-11:110", CallNumberType.SUDOC, "Sudoc1", GREEN_LIB_CODE);
		record.addVariableField(sudoc1);
		DataField sudoc2 = get999("Y 4.G 74/7-11:111", CallNumberType.SUDOC, "Sudoc2", GREEN_LIB_CODE);
		record.addVariableField(sudoc2);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LCbarcode");
		// lc untrunc + dewey truncated + sudoc trunc + alphanum trunc
		DataField alpha1 = get999("ZDVD 19791 DISC 1", CallNumberType.ALPHANUM, "Alpha1", GREEN_LIB_CODE);
		record.addVariableField(alpha1);
		DataField alpha2 = get999("ZDVD 19791 DISC 2", CallNumberType.ALPHANUM, "Alpha2", GREEN_LIB_CODE);
		record.addVariableField(alpha2);
		solrFldMapTest.assertSolrFldValue(record, fldName, "LCbarcode");

		// dewey untruncated + sudoc trunc + alphanum trunc
		record.removeVariableField(lc999);
		record.removeVariableField(dewey2);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey1");
		// sudoc untrunc + alphanum trunc
		record.removeVariableField(dewey1);
		record.removeVariableField(sudoc2);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Sudoc1");
		// dewey untrunc + alphanum trunc
		record.addVariableField(dewey1);
		record.removeVariableField(sudoc1);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey1");
	}

	/**
	 * no truncated callnums, but yes, shelfkeys:
	 *  pick the shortest callnum within a given callnum scheme
	 */
@Test
	public void preferShortestNonTrunc()
	{
		// lc
		Record record = getRecordWith999("QE538.8 .N36 1975-1977", CallNumberType.LC, GREEN_LIB_CODE, "666");
		record = add999(record, "D764.7 .K72 1990", CallNumberType.LC, GREEN_LIB_CODE, "777");
		solrFldMapTest.assertSolrFldValue(record, fldName, "777");

		// dewey
		record = getRecordWith999("888.4 .J788 V.5", CallNumberType.DEWEY, GREEN_LIB_CODE, "Dewey1");
		record = add999(record, "505 .N285B V.241-245 1973", CallNumberType.DEWEY.toString(), GREEN_LIB_CODE, "Dewey2", "LOCATION");
		solrFldMapTest.assertSolrFldValue(record, fldName, "Dewey1");

		// sudoc
		record = getRecordWith999("Y 4.G 74/7-11:110", CallNumberType.SUDOC, GREEN_LIB_CODE, "Sudoc1");
		record = add999(record, "A 13.78:NC-315", CallNumberType.SUDOC.toString(), GREEN_LIB_CODE, "Sudoc2", "LOCATION");
		solrFldMapTest.assertSolrFldValue(record, fldName, "Sudoc2");

		// alphanum
		record = getRecordWith999("ZDVD 19791", CallNumberType.ALPHANUM, GREEN_LIB_CODE, "Alpha1");
		record = add999(record, "ARTDVD 1234", CallNumberType.ALPHANUM.toString(), GREEN_LIB_CODE, "Alpha2", "LOCATION");
		solrFldMapTest.assertSolrFldValue(record, fldName, "Alpha1");
	}

	/**
	 * truncation and shelfkeys
	 *  pick the shortest truncated callnum within a given callnum scheme
	 *  when the number of items is the same
	 */
@Test
	public void preferShortestTrunc()
	{
		Record record = factory.newRecord();

		// mult lc truncated
		record = getRecordWith999("QE538.8 .N36 1975-1977", CallNumberType.LC, GREEN_LIB_CODE, "lc1");
		record = add999(record, "QE538.8 .N36 1978-1980", CallNumberType.LC, GREEN_LIB_CODE, "lc2");
		record = add999(record, "E184.S75 R47A V.1 1980", CallNumberType.LC, GREEN_LIB_CODE, "lc3");
		record = add999(record, "E184.S75 R47A V.2 1980", CallNumberType.LC, GREEN_LIB_CODE, "lc4");
		solrFldMapTest.assertSolrFldValue(record, fldName, "lc1");

		// mult dewey truncated
		record = getRecordWith999("888.4 .J788 V.5", CallNumberType.DEWEY, GREEN_LIB_CODE, "dewey1");
		record = add999(record, "888.4 .J788 V.6", CallNumberType.DEWEY, GREEN_LIB_CODE, "dewey2");
		record = add999(record, "505 .N285B V.241-245 1973", CallNumberType.DEWEY, GREEN_LIB_CODE, "dewey3");
		record = add999(record, "505 .N285B V.241-245 1975", CallNumberType.DEWEY, GREEN_LIB_CODE, "dewey4");
		solrFldMapTest.assertSolrFldValue(record, fldName, "dewey4");

		// non-LC, non-Dewey are lopped by longest common prefix for lib/loc combo

		// mult sudoc truncated
		record = getRecordWith999("Y 4.G 74/7-11:110", CallNumberType.SUDOC, GREEN_LIB_CODE, "sudoc1");
		record = add999(record, "Y 4.G 74/7-11:222", CallNumberType.SUDOC, GREEN_LIB_CODE, "sudoc2");
		DataField d999 = get999("A 13.78:NC-315", CallNumberType.SUDOC.toString(), "sudoc3", GREEN_LIB_CODE, "SOMEWHERE");
		record.addVariableField(d999);
		d999 = get999("A 13.78:NC-315 1947", CallNumberType.SUDOC.toString(), "sudoc4", GREEN_LIB_CODE, "SOMEWHERE");
		record.addVariableField(d999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "sudoc2");

		// mult alphanum truncated
		record = getRecordWith999("ZDVD 19791 DISC 1", CallNumberType.ALPHANUM, GREEN_LIB_CODE, "alpha1");
		record = add999(record, "ZDVD 19791 DISC 2", CallNumberType.ALPHANUM, GREEN_LIB_CODE, "alpha2");
		d999 = get999("ARTDVD 666666 DISC 1", CallNumberType.ALPHANUM.toString(), "alpha3", GREEN_LIB_CODE, "SOMEWHERE");
		record.addVariableField(d999);
		d999 = get999("ARTDVD 666666 DISC 2", CallNumberType.ALPHANUM.toString(), "alpha4", GREEN_LIB_CODE, "SOMEWHERE");
		record.addVariableField(d999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "alpha1");
	}

	/**
	 * prefer more items over shorter truncated shelfkey
	 */
@Test
	public void preferMostItems()
	{
		Record record = factory.newRecord();

		// mult lc truncated
		record = getRecordWith999("QE538.8 .N36 1975-1977", CallNumberType.LC, GREEN_LIB_CODE, "lc1");
		record = add999(record, "QE538.8 .N36 1978-1980", CallNumberType.LC, GREEN_LIB_CODE, "lc2");
		record = add999(record, "E184.S75 R47A V.1 1980", CallNumberType.LC, GREEN_LIB_CODE, "lc3");
		record = add999(record, "E184.S75 R47A V.2 1980", CallNumberType.LC, GREEN_LIB_CODE, "lc4");
		solrFldMapTest.assertSolrFldValue(record, fldName, "lc1");
		record = add999(record, "E184.S75 R47A V.3", CallNumberType.LC, GREEN_LIB_CODE, "lc5");
		solrFldMapTest.assertSolrFldValue(record, fldName, "lc5");

		// 2 dewey truncated
		record = getRecordWith999("888 .J788 V.5", CallNumberType.DEWEY, GREEN_LIB_CODE, "dewey1");
		record = add999(record, "888 .J788 V.6", CallNumberType.DEWEY, GREEN_LIB_CODE, "dewey2");
		record = add999(record, "505 .N285B V.241-245 1973", CallNumberType.DEWEY, GREEN_LIB_CODE, "dewey3");
		record = add999(record, "505 .N285B V.241-245 1975", CallNumberType.DEWEY, GREEN_LIB_CODE, "dewey4");
		solrFldMapTest.assertSolrFldValue(record, fldName, "dewey1");
		record = add999(record, "505 .N285B V.283-285", CallNumberType.DEWEY, GREEN_LIB_CODE, "dewey5");
		solrFldMapTest.assertSolrFldValue(record, fldName, "dewey5");

		// non-LC, non-Dewey are lopped by longest common prefix for lib/loc combo

		// mult sudoc truncated
		record = getRecordWith999("Y 4.G 74/7-11:110", CallNumberType.SUDOC, GREEN_LIB_CODE, "sudoc1");
		record = add999(record, "Y 4.G 74/7-11:222", CallNumberType.SUDOC, GREEN_LIB_CODE, "sudoc2");
		DataField d999 = get999("A 13.78:NC-315", CallNumberType.SUDOC.toString(), "sudoc3", GREEN_LIB_CODE, "SOMEWHERE");
		record.addVariableField(d999);
		d999 = get999("A 13.78:NC-315 1947", CallNumberType.SUDOC.toString(), "sudoc4", GREEN_LIB_CODE, "SOMEWHERE");
		record.addVariableField(d999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "sudoc2");
		d999 = get999("A 13.78:NC-315 1956", CallNumberType.SUDOC.toString(), "sudoc5", GREEN_LIB_CODE, "SOMEWHERE");
		record.addVariableField(d999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "sudoc5");

		// mult alphanum truncated
		record = getRecordWith999("ZDVD 19791 DISC 1", CallNumberType.ALPHANUM, GREEN_LIB_CODE, "alpha1");
		record = add999(record, "ZDVD 19791 DISC 2", CallNumberType.ALPHANUM, GREEN_LIB_CODE, "alpha2");
		d999 = get999("ARTDVD 666666 DISC 1", CallNumberType.ALPHANUM.toString(), "alpha3", GREEN_LIB_CODE, "SOMEWHERE");
		record.addVariableField(d999);
		d999 = get999("ARTDVD 666666 DISC 2", CallNumberType.ALPHANUM.toString(), "alpha4", GREEN_LIB_CODE, "SOMEWHERE");
		record.addVariableField(d999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "alpha1");
		d999 = get999("ARTDVD 666666 DISC 3", CallNumberType.ALPHANUM.toString(), "alpha5", GREEN_LIB_CODE, "SOMEWHERE");
		record.addVariableField(d999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "alpha5");
	}


	/**
	 * If no Green shelfkey, apply same algorithm to other libraries in alpha
	 *  order by raw library code in 999 until you find one
	 *
	 * - pick the LC truncated callnum with the most items
	 * - pick the shortest LC untruncated callnum if no truncation
    * - if no LC, got through callnum scheme order of preference:  LC, Dewey, Sudoc, Alphanum (without box and folder)
	 */
@Test
	public void preferLibraryOrder()
	{
		Record record = factory.newRecord();

		DataField green999 = get999("ZDVD 19791 DISC 1", CallNumberType.ALPHANUM, "Green", GREEN_LIB_CODE);

		// check alg applied to non-green
		DataField ars999Lc1 = get999("QE538.8 .N36 V.7", CallNumberType.LC, "ArsLC1", "ARS");
		DataField ars999Lc2 = get999("QE538.8 .N36 V.8", CallNumberType.LC, "ArsLC2", "ARS");
		DataField ars999Lc3 = get999("E184.S75 R47A V.1", CallNumberType.LC, "ArsLC3", "ARS");
		DataField ars999Lc4 = get999("E184.S75 R47A V.2", CallNumberType.LC, "ArsLC4", "ARS");
		DataField ars999Lc5 = get999("E184.S75 R47A V.3", CallNumberType.LC, "ArsLC5", "ARS");
		record.addVariableField(ars999Lc1);
		solrFldMapTest.assertSolrFldValue(record, fldName, "ArsLC1");
		// two diff lc callnums - use shortest
		record.addVariableField(ars999Lc3);
		solrFldMapTest.assertSolrFldValue(record, fldName, "ArsLC1");
		// one lc truncated, one not - use truncated
		record.addVariableField(ars999Lc4);
		solrFldMapTest.assertSolrFldValue(record, fldName, "ArsLC3");
		// both truncated - use shortest
		record.addVariableField(ars999Lc2);
		solrFldMapTest.assertSolrFldValue(record, fldName, "ArsLC2");
		// both truncated - use most items
		record.addVariableField(ars999Lc5);
		solrFldMapTest.assertSolrFldValue(record, fldName, "ArsLC3");
		// one lc truncated, one not:  use truncated
		record.removeVariableField(ars999Lc2);
		solrFldMapTest.assertSolrFldValue(record, fldName, "ArsLC3");

		// use green if avail
		record.addVariableField(green999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Green");

		DataField ars999Dewey1 = get999("888.4 .J788 V.51", CallNumberType.DEWEY, "ArsDewey1", "ARS");
		DataField ars999Dewey2 = get999("888.4 .J788 V.61", CallNumberType.DEWEY, "ArsDewey2", "ARS");
		DataField ars999Dewey3 = get999("505 .N285B V.241", CallNumberType.DEWEY, "ArsDewey3", "ARS");
		DataField ars999Dewey4 = get999("505 .N285B V.242", CallNumberType.DEWEY, "ArsDewey4", "ARS");
		DataField ars999Dewey5 = get999("888.4 .J788 V.65", CallNumberType.DEWEY, "ArsDewey5", "ARS");
		record = factory.newRecord();
		record.addVariableField(ars999Dewey1);
		solrFldMapTest.assertSolrFldValue(record, fldName, "ArsDewey1");
		// two diff dewey callnums - use shortest
		record.addVariableField(ars999Dewey3);
		solrFldMapTest.assertSolrFldValue(record, fldName, "ArsDewey3");
		// one dewey truncated, one not - use truncated
		record.addVariableField(ars999Dewey2);
		solrFldMapTest.assertSolrFldValue(record, fldName, "ArsDewey1");
		// both truncated - use shortest
		record.addVariableField(ars999Dewey4);
		solrFldMapTest.assertSolrFldValue(record, fldName, "ArsDewey4");
		// both truncated - use most items
		record.addVariableField(ars999Dewey5);
		solrFldMapTest.assertSolrFldValue(record, fldName, "ArsDewey5");

		// use LC if avail
		record.addVariableField(ars999Lc1);
		solrFldMapTest.assertSolrFldValue(record, fldName, "ArsLC1");

		// etc for SUDOC, Alphanum ...

		// libraries prioritized in alpha order by code
		record = factory.newRecord();
		DataField artLc999 = get999("M57 .N42", CallNumberType.LC, "ArtBarcode", "ART");
		DataField engLc999 = get999("M57 .N42", CallNumberType.LC, "EngBarcode", "ENG");
		record.addVariableField(engLc999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "EngBarcode");
		record.addVariableField(artLc999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "ArtBarcode");
		record.addVariableField(ars999Lc1);
		solrFldMapTest.assertSolrFldValue(record, fldName, "ArsLC1");
		// use green if avail
		record.addVariableField(green999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "Green");
		// ars and eng only
		record.removeVariableField(green999);
		record.removeVariableField(artLc999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "ArsLC1");
	}

	/**
	 * online items should be allowed in preferred barcode
	 */
@Test
	public void testOnline()
	{
		Record record = factory.newRecord();
		record = makeSerial(record);

		// online callnum and no separate bib callnum
		record = add999(record, "INTERNET RESOURCE", "ASIS", GREEN_LIB_CODE, "onlineByCallnum");
		solrFldMapTest.assertNoSolrFld(record, fldName);

		// online callnum with bib callnum
		DataField d050 = factory.newDataField("050", ' ', ' ');
		d050.addSubfield(factory.newSubfield('a', "AB123"));
		d050.addSubfield(factory.newSubfield('b', "C45"));
		record.addVariableField(d050);
		solrFldMapTest.assertSolrFldValue(record, fldName, "onlineByCallnum");
		// NOTE:  as of 10/2014, there is no shelfkey avail for this item ...

		// online item with callnum; location "INTERNET"
		record = factory.newRecord();
		record = makeSerial(record);
		DataField onlineByLoc999 = get999("AB123 .C45", CallNumberType.LC.toString(), "onlineByLoc", GREEN_LIB_CODE, "INTERNET");
		record.addVariableField(onlineByLoc999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "onlineByLoc");

		// online item with callnum matches another group
		record = add999(record, "AB123 .C45", CallNumberType.LC, GREEN_LIB_CODE, "notOnline");
		solrFldMapTest.assertSolrFldValue(record, fldName, "notOnline");
	}

	/**
	 * no preferred barcode for ignored callnums
	 */
@Test
	public void testIgnoredCallnums()
	{
		// "NO CALL NUMBER"
		Record record = getRecordWith999("NO CALL NUMBER", CallNumberType.OTHER, GREEN_LIB_CODE, "noCallNum");
		solrFldMapTest.assertNoSolrFld(record, fldName);

		// starts XX
	}

	/**
	 * shelby locations SHOULD allow a preferred barcode
	 */
@Test
	public void testShelbyLoc()
	{
		Record record = factory.newRecord();
		DataField shelby999 = get999("M1503 .A5 VOL.22", CallNumberType.LC, "shelby", GREEN_LIB_CODE);
		shelby999.addSubfield(factory.newSubfield('k', "SHELBYTITL"));
		record.addVariableField(shelby999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "shelby");
	}

	/**
	 * missing/lost locations SHOULD allow a preferred barcode
	 */
@Test
	public void testMissingOrLost()
	{
		Record record = factory.newRecord();

		// missing 999
		DataField missing999 = get999("AB123 C45", CallNumberType.LC.toString(), "missing", GREEN_LIB_CODE, "MISSING");
		record.addVariableField(missing999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "missing");

		// lost 999
		record = factory.newRecord();
		DataField lost999 = get999("AB123 C45", CallNumberType.LC.toString(), "lost", GREEN_LIB_CODE, "LOST-PAID");
		record.addVariableField(lost999);
		solrFldMapTest.assertSolrFldValue(record, fldName, "lost");
	}


	/**
	 * cases when there shouldn't be a preferredItemBarcode
	 */
@Test
	public void testNoPreferredItemBarcode()
	{
		Record record = factory.newRecord();

		// no items
		solrFldMapTest.assertNoSolrFld(record, fldName);

		// ignored callnums with no browsable callnum
		DataField ignored999 = get999("NO CALL NUMBER", "ASIS", "nocallnum", GREEN_LIB_CODE);
		record.addVariableField(ignored999);
		solrFldMapTest.assertNoSolrFld(record, fldName);

		// bad lane lc callnum
		DataField badLaneLc999 = get999("XX13413", CallNumberType.LC.toString(), "lane", "LANE-MED", "ASK@LANE");
		record.addVariableField(badLaneLc999);
		solrFldMapTest.assertNoSolrFld(record, fldName);
	}

@Test
	public void preferBadLCDeweyIfNoOther()
	{
		String fldName = "preferred_barcode";

		// bad LC
		Record record = getRecordWith999("BAD", CallNumberType.LC, GREEN_LIB_CODE, "badLc");
		solrFldMapTest.assertSolrFldValue(record, fldName, "badLc");

		// bad dewey
		record = getRecordWith999("1234.5 .D6", CallNumberType.DEWEY, GREEN_LIB_CODE, "badDewey");
		solrFldMapTest.assertSolrFldValue(record, fldName, "badDewey");
	}


//---- private methods below ---------------------------

	private Record getRecordWith999(String callnum, CallNumberType callNumberType, String library, String barcode)
	{
		return getRecordWith999(callnum, callNumberType.toString().toUpperCase(), library, barcode);
	}
	private Record getRecordWith999(String callnum, String type, String library, String barcode)
	{
		Record record = makeSerial(factory.newRecord());
		return add999(record, callnum, type, library, barcode);
	}
	/** make it a serial for callnum lopping */
	private Record makeSerial(Record record)
	{
		record.setLeader(factory.newLeader("01952cas  2200457Ia 4500"));
		ControlField cf008 = factory.newControlField("008");
		cf008.setData("780930m19391944nyu           000 0 eng d");
		record.addVariableField(cf008);
		return record;
	}

	private Record add999(Record rec, String callnum, CallNumberType callNumberType, String library, String barcode)
	{
		return add999(rec, callnum, callNumberType.toString().toUpperCase(), library, barcode);
	}
	private Record add999(Record rec, String callnum, String type, String library, String barcode)
	{
		rec.addVariableField(get999(callnum, type, barcode, library));
		return rec;
	}
	private Record add999(Record rec, String callnum, String type, String library, String barcode, String location)
	{
		rec.addVariableField(get999(callnum, type, barcode, library, location));
		return rec;
	}

	private DataField get999(String suba, CallNumberType callNumberType,  String subi, String subm)
	{
		return get999(suba, callNumberType.toString().toUpperCase(), subi, subm);
	}
	private DataField get999(String suba, String subw,  String subi, String subm)
	{
		return get999(suba, subw, subi, subm, "STACKS");
	}
	private DataField get999(String suba, String subw,  String subi, String subm, String subl)
	{
		// a| BX4659 .E85 W44 w| LC c| 1 i| 36105037439663 l| STACKS m| GREEN
		DataField df999 = factory.newDataField("999", ' ', ' ');
		if (suba != null)
			df999.addSubfield(factory.newSubfield('a', suba));
		df999.addSubfield(factory.newSubfield('w', subw));
		df999.addSubfield(factory.newSubfield('i', subi));
		df999.addSubfield(factory.newSubfield('m', subm));
		df999.addSubfield(factory.newSubfield('l', subl));
		return df999;
	}

}
