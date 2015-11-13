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

  MarcFactory factory = MarcFactory.newInstance();
  Leader ldr = factory.newLeader("01426cas a2200385Ia 4500");

  DataField df999hloc = factory.newDataField("999", ' ', ' ');
  {
    df999hloc.addSubfield(factory.newSubfield('a', "XX"));
    df999hloc.addSubfield(factory.newSubfield('k', "ON-ORDER"));
    df999hloc.addSubfield(factory.newSubfield('m', "GREEN"));
  }
  DataField df999hcloc = factory.newDataField("999", ' ', ' ');
  {
    df999hcloc.addSubfield(factory.newSubfield('a', "XX"));
    df999hcloc.addSubfield(factory.newSubfield('k', "ON-ORDER"));
    df999hcloc.addSubfield(factory.newSubfield('l', "SOMETHING"));
  }
  DataField df999chloc = factory.newDataField("999", ' ', ' ');
  {
    df999chloc.addSubfield(factory.newSubfield('a', "XX"));
    df999chloc.addSubfield(factory.newSubfield('k', "SOMETHING"));
    df999chloc.addSubfield(factory.newSubfield('l', "ON-ORDER"));
    df999chloc.addSubfield(factory.newSubfield('m', "GREEN"));
  }
  DataField df596noValue = factory.newDataField("596", ' ', ' ');
  {
    df596noValue.addSubfield(factory.newSubfield('a', "1000"));
  }
  DataField df596validValue = factory.newDataField("596", ' ', ' ');
  {
    df596validValue.addSubfield(factory.newSubfield('a', "13"));
  }

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
  public void testOnOrderDisplay999Only()
  {
    Record record = factory.newRecord();
    record.setLeader(ldr);
    // tests to write:
    // 1. 999 with ON-ORDER in 999$l, library in 999$m
    record.addVariableField(df999chloc);
    solrFldMapTest.assertSolrFldValue(record, AccfldName, onOrderFldVal);
    solrFldMapTest.assertSolrFldValue(record, LibfldName, "Green");

    // 2. 999 with ON-ORDER in 999$k, library in 999$m
    record = factory.newRecord();
    record.setLeader(ldr);
    record.addVariableField(df999hloc);
    solrFldMapTest.assertSolrFldValue(record, AccfldName, onOrderFldVal);
    solrFldMapTest.assertSolrFldValue(record, LibfldName, "Green");

    // 3. 999 with ON-ORDER in 999$l or $k and no library in 999$m
    record = factory.newRecord();
    record.setLeader(ldr);
    record.addVariableField(df999hcloc);
    solrFldMapTest.assertSolrFldValue(record, AccfldName, onOrderFldVal);
    solrFldMapTest.assertSolrFldHasNoValue(record, LibfldName, "Green");
  }

@Test
  public void testOnOrderDisplay596Only()
  {
    // 4. no 999 but a 596$a with no corresponding value in the lookup table
    Record record = factory.newRecord();
    record.setLeader(ldr);
    record.addVariableField(df596noValue);
    solrFldMapTest.assertSolrFldValue(record, AccfldName, onOrderFldVal);

    // 5. no 999 but a 596$a with a value in the lookup table
    record = factory.newRecord();
    record.setLeader(ldr);
    record.addVariableField(df596validValue);
    System.out.println(record);
    solrFldMapTest.assertSolrFldValue(record, AccfldName, onOrderFldVal);
    solrFldMapTest.assertSolrFldValue(record, LibfldName, "Applied Physics Department");
  }

/**
 */
@Test
  public void testOnOrderDisplay999and596()
  {
    // 6. 999 with ON-ORDER in 999$l or $k, library in 999$m, and 596$a with value in lookup table
    // 7. 999 with ON-ORDER in 999$l or $k, library in 999$m, and 596$a without value in lookup table
    // 8. 999 with ON-ORDER in 999$l or $k, no library in 999$m, and 596$a with value in lookup table
    // 9. 999 with ON-ORDER in 999$l or $k, no library in 999$m, and 596$a without value in lookup table
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
