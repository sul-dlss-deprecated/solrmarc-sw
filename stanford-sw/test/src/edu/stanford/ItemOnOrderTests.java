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
  public void testOnOrderDisplay999l()
  {
    // 999 with ON-ORDER in 999$l, library in 999$m
    Record record = factory.newRecord();
    record.setLeader(ldr);
    DataField df999chloc = factory.newDataField("999", ' ', ' ');
    df999chloc.addSubfield(factory.newSubfield('a', "XX"));
    df999chloc.addSubfield(factory.newSubfield('k', "SOMETHING"));
    df999chloc.addSubfield(factory.newSubfield('l', "ON-ORDER"));
    df999chloc.addSubfield(factory.newSubfield('m', "GREEN"));
    record.addVariableField(df999chloc);

    DataField df999atLibrary = factory.newDataField("999", ' ', ' ');
    df999atLibrary.addSubfield(factory.newSubfield('a', "F152 .A28"));
    df999atLibrary.addSubfield(factory.newSubfield('w', "LC"));
    df999atLibrary.addSubfield(factory.newSubfield('i', "36105018746623"));
    df999atLibrary.addSubfield(factory.newSubfield('l', "HAS-DIGIT"));
    df999atLibrary.addSubfield(factory.newSubfield('m', "ART"));
		record.addVariableField(df999atLibrary);

    solrFldMapTest.assertSolrFldValue(record, AccfldName, onOrderFldVal);
    solrFldMapTest.assertSolrFldValue(record, LibfldName, "Green");
    solrFldMapTest.assertSolrFldValue(record, AccfldName, "On order");

    solrFldMapTest.assertSolrFldValue(record, AccfldName, "At the Library");
    solrFldMapTest.assertSolrFldValue(record, LibfldName, "Art & Architecture (Bowes)");

  }

@Test
  public void testOnOrderDisplay999k()
  {
    // 999 with ON-ORDER in 999$k, library in 999$m
    Record record = factory.newRecord();
    record.setLeader(ldr);
    DataField df999hloc = factory.newDataField("999", ' ', ' ');
    df999hloc.addSubfield(factory.newSubfield('a', "XX"));
    df999hloc.addSubfield(factory.newSubfield('k', "ON-ORDER"));
    df999hloc.addSubfield(factory.newSubfield('m', "HOOVER"));
    record.addVariableField(df999hloc);

    solrFldMapTest.assertSolrFldValue(record, AccfldName, onOrderFldVal);
    solrFldMapTest.assertSolrFldValue(record, LibfldName, "Hoover Library");
    solrFldMapTest.assertSolrFldValue(record, AccfldName, "On order");

  }

@Test
  public void testOnOrderDisplay999NoLib999m()
  {
    // 999 with ON-ORDER in 999$l or $k and no library in 999$m
    Record record = factory.newRecord();
    record.setLeader(ldr);
    DataField df999hcloc = factory.newDataField("999", ' ', ' ');
    df999hcloc.addSubfield(factory.newSubfield('a', "XX"));
    df999hcloc.addSubfield(factory.newSubfield('k', "ON-ORDER"));
    df999hcloc.addSubfield(factory.newSubfield('l', "SOMETHING"));
    record.addVariableField(df999hcloc);

    solrFldMapTest.assertSolrFldValue(record, AccfldName, onOrderFldVal);
    solrFldMapTest.assertNoSolrFld(record, LibfldName);
    solrFldMapTest.assertSolrFldValue(record, AccfldName, "On order");
  }

@Test
  public void testOnOrderDisplay596OnlyInvalidValue()
  {
    // no 999 but a 596$a with no corresponding value in the lookup table
    Record record = factory.newRecord();
    record.setLeader(ldr);
    DataField df596noValue = factory.newDataField("596", ' ', ' ');
    df596noValue.addSubfield(factory.newSubfield('a', "1000"));
    record.addVariableField(df596noValue);

    solrFldMapTest.assertSolrFldValue(record, AccfldName, onOrderFldVal);
    solrFldMapTest.assertNoSolrFld(record, LibfldName);
    solrFldMapTest.assertSolrFldValue(record, AccfldName, "On order");
  }

@Test
  public void testOnOrderDisplay596OnlyValidValue()
  {
    // no 999 but a 596$a with a value in the lookup table
    Record record = factory.newRecord();
    record.setLeader(ldr);
    DataField df596validValue = factory.newDataField("596", ' ', ' ');
    df596validValue.addSubfield(factory.newSubfield('a', "28"));
    record.addVariableField(df596validValue);

    solrFldMapTest.assertSolrFldValue(record, AccfldName, onOrderFldVal);
    solrFldMapTest.assertSolrFldValue(record, LibfldName, "Business");
    solrFldMapTest.assertSolrFldValue(record, AccfldName, "On order");
  }
}
