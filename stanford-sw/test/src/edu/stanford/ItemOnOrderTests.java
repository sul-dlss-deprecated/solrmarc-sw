package edu.stanford;

import java.io.*;

import org.junit.*;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

import edu.stanford.enumValues.Access;

/**
 * junit4 tests for Stanford University
 *   cope properly with on order items - display, discoverable  INDEX-92
 * @author Laney McGlohon
 */
public class ItemOnOrderTests extends AbstractStanfordTest
{
	private String AccfldName = "access_facet";
	private final String onOrderFldVal = "On order";
	private String LibfldName = "building_facet";


@Before
	public final void setup()
	{
		mappingTestInit();
	}

	
/**
 * test on order materials
 * the on order library will come from either
 * 1. 999$m or
 * 2. look up value from the 596$a
 * Regardless of source of library, the item will have an entry in the access facet
 * and the library facet for the ordering library
 */
@Test
	public void testOnOrderDisplay()
	{
	

		// tests to write:
		// 1. 999 with ON-ORDER in 999$l, library in 999$m
	    // 2. 999 with ON-ORDER in 999$k, library in 999$m
	    // 3. 999 with ON-ORDER in 999$l or $k and no library in 999$m
	    // 4. no 999 but a 596$a with a value in the lookup table
	    // 5. no 999 but a 596$a with no corresponding value in the lookup table
	    // 6. 999 with ON-ORDER in 999$l or $k, library in 999$m, and 596$a with value in lookup table
        // 7. 999 with ON-ORDER in 999$l or $k, library in 999$m, and 596$a without value in lookup table
        // 8. 999 with ON-ORDER in 999$l or $k, no library in 999$m, and 596$a with value in lookup table
        // 9. 999 with ON-ORDER in 999$l or $k, no library in 999$m, and 596$a without value in lookup table
		MarcFactory factory = MarcFactory.newInstance();
		Leader ldr = factory.newLeader("01426cas a2200385Ia 4500");
		Record record = factory.newRecord();
		record.setLeader(ldr);
		ControlField cf008 = factory.newControlField("008");
		cf008.setData("110912c20119999mnuar l       0    0eng  ");
		record.addVariableField(cf008);
		DataField df999onOrder = factory.newDataField("999", ' ', ' ');
		df999onOrder.addSubfield(factory.newSubfield('a', "F152 .A28"));
		df999onOrder.addSubfield(factory.newSubfield('w', "LC"));
		df999onOrder.addSubfield(factory.newSubfield('i', "36105018746623"));
		df999onOrder.addSubfield(factory.newSubfield('l', "ON-ORDER"));
		df999onOrder.addSubfield(factory.newSubfield('m', "GREEN"));
		record.addVariableField(df999onOrder);
		
		solrFldMapTest.assertSolrFldValue(record, AccfldName, onOrderFldVal);
		solrFldMapTest.assertSolrFldValue(record, LibfldName, "Green");

	
	}

	/**
	 */
@Test
	public void testAccessFacetValue()
	{
	}


	/**
	 */
@Test
	public void testLibraryFacetValue()
	{
	}

}