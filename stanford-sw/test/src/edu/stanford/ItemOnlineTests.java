package edu.stanford;

import java.io.*;

import org.junit.*;

import edu.stanford.enumValues.CallNumberType;

/**
 * junit4 tests for Stanford University
 *   cope properly with online items - display, discoverable  SW-232
 * @author Naomi Dushay
 */
public class ItemOnlineTests extends AbstractStanfordTest
{
	private String fileName = "onlineItems.mrc";
    private String testFilePath = testDataParentPath + File.separator + fileName;

@Before
	public final void setup()
	{
		mappingTestInit();
	}

	//  If there is no call number in any 999 for the record
  	//   look for one in the 050, 090, 086 (see below)

	// search results - do not display:   item_display    lopped callnum
	// record view - do not display:   item_display   full callnum, sort callnum
	// availability - display (don't call jenson)
	// facets  - assign if there is a call number in the bib record
	// shelflist  - if call number, include
	//   (preferred_barcode, shelfkey, reversekey, item_display  shelfkey, reversekey)
    //   NOTE:  display call number as starting point, but display the link text in the list
    //

	/**
	 * online resources without call number in any item record should get a
	 *   call number (for a shelfkey) from the bib record, if there is one
	 */
@Test
	public void testShelfkeyFromBibCallnum()
	{
		String fldName = "shelfkey";

	    solrFldMapTest.assertNoSolrFld(testFilePath, "only999", fldName);

	    String shelfkey = CallNumberType.LC.getPrefix() + org.solrmarc.tools.CallNumUtils.getLCShelfkey("A1 .B2", null).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "addlItem", fldName, shelfkey);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "addlItem", fldName, "other INTERNET RESOURCE");

	    // 050
	    shelfkey = CallNumberType.LC.getPrefix() + org.solrmarc.tools.CallNumUtils.getLCShelfkey("QA76.76.C672 B367 2001eb", null).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "050", fldName, shelfkey);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "050090", fldName, shelfkey);

	    // 090
	    shelfkey = CallNumberType.LC.getPrefix() + org.solrmarc.tools.CallNumUtils.getLCShelfkey("QM142 .A84 2010", null).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "090-1", fldName, shelfkey);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "090082", fldName, shelfkey);
	    shelfkey = CallNumberType.LC.getPrefix() + org.solrmarc.tools.CallNumUtils.getLCShelfkey("JZ5584.U6 SR no.227", null).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "090-2", fldName, shelfkey);

	    // 086
	    //  only sudoc if first indicator is 0 or sudoc is specified in subfield 2
	    shelfkey = edu.stanford.CallNumUtils.getShelfKey("E/ESCWA/EDGD/2009/1", CallNumberType.OTHER, null).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086-1", fldName, shelfkey);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "082086", fldName, shelfkey);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086050", fldName, shelfkey);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086090", fldName, shelfkey);
	    shelfkey = edu.stanford.CallNumUtils.getShelfKey("Y 4.W 36:110-64", CallNumberType.SUDOC, null).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086-2", fldName, shelfkey);
	    shelfkey = edu.stanford.CallNumUtils.getShelfKey("E2015.A5 M64", CallNumberType.OTHER, null).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086-3", fldName, shelfkey);
	    shelfkey = edu.stanford.CallNumUtils.getShelfKey("Y 4.G 74/9:S.HRG.110-819", CallNumberType.SUDOC, null).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086-4", fldName, shelfkey);
	    shelfkey = edu.stanford.CallNumUtils.getShelfKey("L425.B9bas 2009/10", CallNumberType.OTHER, null).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086-5", fldName, shelfkey);

	    // 082 - we ignore these (Dewey);  note that slashes are a problem
	    solrFldMapTest.assertNoSolrFld(testFilePath, "082-1", fldName);
	    solrFldMapTest.assertNoSolrFld(testFilePath, "082-2", fldName);
 	    shelfkey = CallNumberType.DEWEY.getPrefix() + org.solrmarc.tools.CallNumUtils.getDeweyShelfKey("794.8/15265").toLowerCase();
 	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "082086", fldName, shelfkey);
 	    shelfkey = CallNumberType.DEWEY.getPrefix() + CallNumUtils.getShelfKey("794.8/15265", CallNumberType.OTHER, null).toLowerCase();
 	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "082086", fldName, shelfkey);
	}


	/**
	 * online resources without call number in any item record should get a
	 *   call number (for a shelfkey) from the bib record, if there is one,
	 *   and therefore there should be a reverse shelfkey as well
	 */
@Test
	public void testReverseShelfkeyFromBibCallnum()
	{
		String fldName = "reverse_shelfkey";

	    solrFldMapTest.assertNoSolrFld(testFilePath, "only999", fldName);

	    String shelfkey = CallNumberType.LC.getPrefix() + org.solrmarc.tools.CallNumUtils.getLCShelfkey("A1 .B2", null).toLowerCase();
	    String rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "addlItem", fldName, rkey);

	    // 050
	    shelfkey = CallNumberType.LC.getPrefix() + org.solrmarc.tools.CallNumUtils.getLCShelfkey("QA76.76.C672 B367 2001eb", null).toLowerCase();
	    rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "050", fldName, rkey);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "050090", fldName, rkey);

	    // 090
	    shelfkey = CallNumberType.LC.getPrefix() + org.solrmarc.tools.CallNumUtils.getLCShelfkey("QM142 .A84 2010", null).toLowerCase();
	    rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "090-1", fldName, rkey);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "090082", fldName, rkey);
	    shelfkey = CallNumberType.LC.getPrefix() + org.solrmarc.tools.CallNumUtils.getLCShelfkey("JZ5584.U6 SR no.227", null).toLowerCase();
	    rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "090-2", fldName, rkey);

	    // 086
	    //  only sudoc if first indicator is 0 or sudoc is specified in subfield 2
	    shelfkey = edu.stanford.CallNumUtils.getShelfKey("E/ESCWA/EDGD/2009/1", CallNumberType.OTHER, null).toLowerCase();
	    rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086-1", fldName, rkey);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "082086", fldName, rkey);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086050", fldName, rkey);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086090", fldName, rkey);
	    shelfkey = edu.stanford.CallNumUtils.getShelfKey("Y 4.W 36:110-64", CallNumberType.SUDOC, null).toLowerCase();
	    rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086-2", fldName, rkey);
	    shelfkey = edu.stanford.CallNumUtils.getShelfKey("E2015.A5 M64", CallNumberType.OTHER, null).toLowerCase();
	    rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086-3", fldName, rkey);
	    shelfkey = edu.stanford.CallNumUtils.getShelfKey("Y 4.G 74/9:S.HRG.110-819", CallNumberType.SUDOC, null).toLowerCase();
	    rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086-4", fldName, rkey);
	    shelfkey = edu.stanford.CallNumUtils.getShelfKey("L425.B9bas 2009/10", CallNumberType.OTHER, null).toLowerCase();
	    rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086-5", fldName, rkey);

	    // 082 - we ignore these (Dewey);  note that slashes are a problem
	    solrFldMapTest.assertNoSolrFld(testFilePath, "082-1", fldName);
	    solrFldMapTest.assertNoSolrFld(testFilePath, "082-2", fldName);
	    shelfkey = CallNumberType.DEWEY.getPrefix() + org.solrmarc.tools.CallNumUtils.getDeweyShelfKey("794.8/15265").toLowerCase();
	    rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "082086", fldName, rkey);
	    shelfkey = CallNumberType.DEWEY.getPrefix() + CallNumUtils.getShelfKey("794.8/15265", CallNumberType.OTHER, null).toLowerCase();
	    rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "082086", fldName, rkey);
	}

	/**
	 * online resources without call number in any item record should get a
	 *   call number (for a shelfkey) from the bib record, if there is one,
	 *   and then set the preferred_barcode if there are no other choices
	 */
@Test
	public void testPrefBarcodeFromBibCallnum()
	{
		String fldName = "preferred_barcode";

	    solrFldMapTest.assertNoSolrFld(testFilePath, "only999", fldName);

	    solrFldMapTest.assertSolrFldValue(testFilePath, "addlItem", fldName, "1");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "050", fldName, "1");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "050090", fldName, "1");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "090-1", fldName, "1");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "090082", fldName, "1");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "090-2", fldName, "1");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086-1", fldName, "1");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "082086", fldName, "1");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086-2", fldName, "1");
	    // not using dewey
	    solrFldMapTest.assertNoSolrFld(testFilePath, "082-1", fldName);
	    solrFldMapTest.assertNoSolrFld(testFilePath, "082-2", fldName);
	}


	/**
	 * online resources without call number in any item record should get a
	 *   call number (for shelfkey) from the bib record, if there is one
	 *   and include the shelfkey and reverse_shelfkey in the item_display field
	 *    it should NOT include a lopped call number, a full call number, or
	 *    a volume sort call number
	 */
@Test
	public void testItemDispFromBibCallnum()
	{
		String fldName = "item_display";

		String sep = ItemUtils.SEP;
		String firstPart = "1" + sep + "SUL" + sep + Item.ELOC + sep + sep + sep + sep;
		// no full or lopped call number
		String fldVal = firstPart + sep + sep + Item.ECALLNUM + sep + sep + sep + CallNumberType.OTHER;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "only999", fldName, fldVal);
		fldVal = firstPart + sep + sep + sep + sep + sep + CallNumberType.OTHER;
	    solrFldMapTest.assertSolrFldValue(testFilePath, "only999", fldName, fldVal);

	    fldVal = firstPart.replace('1', '2') + sep + sep + "INTERNET RESOURCE KF3400 .S36 2009" + sep + sep + sep + CallNumberType.LC;
	    solrFldMapTest.assertSolrFldValue(testFilePath, "addlItem", fldName, fldVal);

	    String callnum = "A1 .B2";
	    String skey = CallNumberType.LC.getPrefix() + org.solrmarc.tools.CallNumUtils.getLCShelfkey(callnum, null).toLowerCase();
	    String rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(skey).toLowerCase();
		fldVal = "1" + sep + "LAW" + sep + "STACKS-3" + sep + sep + sep + callnum + sep + skey + sep + rkey + sep + callnum + sep + skey + sep + sep + CallNumberType.LC;
	    solrFldMapTest.assertSolrFldValue(testFilePath, "addlItem", fldName, fldVal);

	    // 050
	    skey = CallNumberType.LC.getPrefix() + org.solrmarc.tools.CallNumUtils.getLCShelfkey("QA76.76.C672 B367 2001eb", null).toLowerCase();
	    rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(skey).toLowerCase();
		fldVal = firstPart + skey + sep + rkey + sep + sep + sep + sep + CallNumberType.LC;
	    solrFldMapTest.assertSolrFldValue(testFilePath, "050", fldName, fldVal);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "050090", fldName, fldVal);

	    // 090
	    skey = CallNumberType.LC.getPrefix() + org.solrmarc.tools.CallNumUtils.getLCShelfkey("QM142 .A84 2010", null).toLowerCase();
	    rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(skey).toLowerCase();
		fldVal = firstPart + skey + sep + rkey + sep + sep + sep + sep + CallNumberType.LC;
	    solrFldMapTest.assertSolrFldValue(testFilePath, "090-1", fldName, fldVal);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "090082", fldName, fldVal);

	    // 086
	    //  only sudoc if first indicator is 0 or sudoc is specified in subfield 2
	    skey = edu.stanford.CallNumUtils.getShelfKey("E/ESCWA/EDGD/2009/1", CallNumberType.OTHER, null).toLowerCase();
	    rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(skey).toLowerCase();
		fldVal = firstPart + skey + sep + rkey + sep + sep + sep + sep + CallNumberType.OTHER;
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086-1", fldName, fldVal);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "082086", fldName, fldVal);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086050", fldName, fldVal);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086090", fldName, fldVal);

	    // 082 - Dewey not used
	    skey = CallNumberType.DEWEY.getPrefix() + org.solrmarc.tools.CallNumUtils.getDeweyShelfKey("794.8/15265").toLowerCase();
	    rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(skey).toLowerCase();
		fldVal = firstPart + skey + sep + rkey + sep + sep + sep + sep + CallNumberType.LC;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "082-1", fldName, fldVal);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "082086", fldName, fldVal);
	}

	/**
	 * online resources without call number in any item record should get a
	 *   call number (for a shelfkey) from the bib record, if there is one
	 */
@Test
	public void testCallnumFacetFromBib()
	{
		String fldName = "callnum_facet_hsim";

	    solrFldMapTest.assertNoSolrFld(testFilePath, "only999", fldName);

		solrFldMapTest.assertSolrFldValue(testFilePath, "addlItem", fldName, CallNumUtils.LC_TOP_FACET_VAL + "|A - General Works|A - General Works");

	    // 050
	    String qval = CallNumUtils.LC_TOP_FACET_VAL + "|Q - Science|QA - Mathematics";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "050", fldName, qval);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "050090", fldName, qval);

	    // 090
	    qval = CallNumUtils.LC_TOP_FACET_VAL + "|Q - Science|QM - Human Anatomy";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "090-1", fldName, qval);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "090082", fldName, qval);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "090-2", fldName, CallNumUtils.LC_TOP_FACET_VAL + "|J - Political Science|JZ - International Relations");

	    // 086
	    String firstPart = CallNumUtils.GOV_DOC_TOP_FACET_VAL + "|";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086-1", fldName, firstPart + CallNumUtils.GOV_DOC_UNKNOWN_FACET_VAL);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "082086", fldName, firstPart + CallNumUtils.GOV_DOC_UNKNOWN_FACET_VAL);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086-2", fldName, firstPart + CallNumUtils.GOV_DOC_UNKNOWN_FACET_VAL);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086050", fldName, firstPart + CallNumUtils.GOV_DOC_UNKNOWN_FACET_VAL);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "086090", fldName, firstPart + CallNumUtils.GOV_DOC_UNKNOWN_FACET_VAL);

	    // 082 - not using dewey
	    solrFldMapTest.assertNoSolrFld(testFilePath, "082-1", fldName);
	    solrFldMapTest.assertNoSolrFld(testFilePath, "082-2", fldName);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "082086", fldName, CallNumUtils.DEWEY_TOP_FACET_VAL + "|700s - Arts & Recreation|790s - Recreational & Performing Arts");
	}

	/**
	 * online resources that are on order or in process:  on order/in process
	 *  current location should be preserved, but otherwise treat as above
	 */
@Test
	public void testOnOrderOnline()
	{
		String id = "onOrder";
		String id050 = "onOrder050";

		String fldName = "callnum_facet_hsim";
		solrFldMapTest.assertNoSolrFld(testFilePath, id, fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, id050, fldName);


		fldName = "preferred_barcode";
		solrFldMapTest.assertNoSolrFld(testFilePath, id, fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, id050, fldName);

		fldName = "shelfkey";
		solrFldMapTest.assertNoSolrFld(testFilePath, id, fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, id050, fldName);

// TODO: should  on order and in process items have XX callnum in lopped, full and volsort?  SW-229
		fldName = "item_display";
		String sep = ItemUtils.SEP;
		String firstPart = "1" + sep + "SUL" + sep + Item.ELOC + sep + "ON-ORDER" + sep + sep;
		String callnum = "XX(8438957.2)";
		// call number included (lopped, full) but no volsort
	    String fldVal = firstPart + callnum + sep + sep + sep + callnum + sep + sep + sep + CallNumberType.LC;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    // with vol sort
	    fldVal = firstPart + callnum + sep + sep + sep + callnum + sep + callnum + sep + sep + CallNumberType.LC;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    // no call number:  lopped, nor full nor volsort
	    fldVal = firstPart + sep + sep + sep + sep + sep + sep + CallNumberType.OTHER;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// on order online item with 050
		String bibCallnum = "A1 .B2";
	    String skey = CallNumberType.LC.getPrefix() + org.solrmarc.tools.CallNumUtils.getLCShelfkey(bibCallnum, null).toLowerCase();
	    String rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(skey).toLowerCase();
	    fldVal = firstPart + callnum + sep + skey + sep + rkey + sep + callnum + sep + sep + sep + CallNumberType.LC;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id050, fldName, fldVal);
	    fldVal = firstPart + sep + skey + sep + rkey + sep + callnum + sep + sep + sep + CallNumberType.LC;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id050, fldName, fldVal);
	    fldVal = firstPart + callnum + sep + skey + sep + rkey + sep + callnum + sep + sep + sep + CallNumberType.LC;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id050, fldName, fldVal);
	    fldVal = firstPart + callnum + sep + sep + sep + callnum + sep + sep + sep + CallNumberType.LC;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id050, fldName, fldVal);
	    // no call number, no shelflist
	    fldVal = firstPart + sep + sep + sep + sep + sep + sep + CallNumberType.OTHER;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id050, fldName, fldVal);
	}

	/**
	 * when the call number in the 050 or 090 is a partial LC call number
	 *   (just the class code), then it should be included in the facets,
	 *   but not in the shelflist (preferred_barcode, shelfkey, reverse_shelfkey,
	 *   item_display)
	 */
//@Test
	public void testPartialLC()
	{
		String id = "050partial";
		String fldName = "callnum_facet_hsim";
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, CallNumUtils.LC_TOP_FACET_VAL + "|A - General Works|A - General Works");

		fldName = "preferred barcode";
		solrFldMapTest.assertNoSolrFld(testFilePath, id, fldName);

		fldName = "shelfkey";
		solrFldMapTest.assertNoSolrFld(testFilePath, id, fldName);

		fldName = "item_display";
		String sep = ItemUtils.SEP;
		String firstPart = "1" + sep + "SUL" + sep + Item.ELOC + sep + sep + sep;
		String fldVal = firstPart + sep + sep + sep + sep + sep + sep + CallNumberType.LC;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}

	/**
	 * item has a real call number, but is online also b/c it has an 856
	 */
@Test
	public void testOnline856()
	{
		String id = "online856";
		String fldName = "callnum_facet_hsim";
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, CallNumUtils.LC_TOP_FACET_VAL + "|A - General Works|A - General Works");

		fldName = "preferred_barcode";
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, "1");

		fldName = "shelfkey";
		String callnum = "A1 .B2";
	    String skey = CallNumberType.LC.getPrefix() + org.solrmarc.tools.CallNumUtils.getLCShelfkey(callnum, null).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, skey);

		fldName = "item_display";
	    String rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(skey).toLowerCase();
		String sep = ItemUtils.SEP;
		String firstPart = "1" + sep + "GREEN" + sep + "STACKS" + sep + sep + sep;
	    String fldVal = firstPart + callnum + sep + skey + sep + rkey + sep + callnum + sep + skey + sep + sep + CallNumberType.LC;
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}
}