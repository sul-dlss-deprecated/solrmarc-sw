package edu.stanford;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.marc4j.marc.*;
import org.solrmarc.tools.SolrMarcIndexerException;
import org.xml.sax.SAXException;

/**
 * tests for stanford_theses_facet_hsim field, which is for the Stanford
 * dissertatations and theses facet (hierarchical)
 * @author Shelley Doljack
 */
public class StanfordThesesFacetHsimTests extends AbstractStanfordTest
{
  private final String fldName = "stanford_theses_facet_hsim";
  private final MarcFactory factory = MarcFactory.newInstance();

@Before
  public void setup()
  {
  mappingTestInit();
  }

  @Test
  public void test502BA()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a4820195");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (B.A.)--Stanford University, 2002."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', "Department of Geophysics"));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Bachelor's|Bachelor of Arts (BA)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Department of Geophysics");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Type of degree|Bachelor's|Undergraduate honors thesis");
  }

  @Test
  public void test502DMA()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a1343750");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "D.M.A. term project Department of Music, Stanford University, 1989."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', "Department of Music."));
    df.addSubfield(factory.newSubfield('t', "Projects."));
    df.addSubfield(factory.newSubfield('p', "D.M.A. Term."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Doctor of Musical Arts (DMA)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Department of Music");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Type of degree|Master's|Master of Arts (MA)");
  }

  @Test
  public void test502EdD()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a965475");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('g', "Thesis"));
    df.addSubfield(factory.newSubfield('b', "Ed.D."));
    df.addSubfield(factory.newSubfield('c', "Stanford University"));
    df.addSubfield(factory.newSubfield('d', "1977."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', "Department of School of Education."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Doctor of Education (EdD)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Department of School of Education");
    solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 2);
  }

  @Test
  public void test502EdM()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a2303030");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (Ed.M.)--Leland Stanford Junior University, 1934."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Education (EdM)");
  }

  @Test
  public void test502EdS()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a2285433");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (Ed.S.)--Stanford University."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Other|Educational Specialist (EdS)");
  }

  @Test
  public void test502NotEdS()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a1010947");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('g', "Thesis"));
    df.addSubfield(factory.newSubfield('b', "Ph.D."));
    df.addSubfield(factory.newSubfield('c', "Stanford University"));
    df.addSubfield(factory.newSubfield('g', "Graduate Division Special Programs, Altered States of Consciousness."));
    df.addSubfield(factory.newSubfield('d', "1981."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Doctor of Philosophy (PhD)");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Type of degree|Other|Educational Specialist (EdS)");
  }

  @Test
  public void test502Engineering()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a11688582");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (Engineering)--Stanford University, 2016."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', "Department of Aeronautics and Astronautics."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Engineer");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Department of Aeronautics and Astronautics");
  }

  @Test
  public void test502Engineer()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a5650590");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('b', "Engineer"));
    df.addSubfield(factory.newSubfield('c', "Stanford University"));
    df.addSubfield(factory.newSubfield('d', "1912"));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', "Department of Geology."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Engineer");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Department of Geology");
  }

  @Test
  public void test502DegreeEngineer()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a2950747");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('b', "Degree of Engineer"));
    df.addSubfield(factory.newSubfield('c', "Stanford University"));
    df.addSubfield(factory.newSubfield('d', "1994"));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', "Department of Civil Engineering."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Engineer");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Department of Civil Engineering");
  }

  @Test
  public void test502Engr()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a2161308");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (Engr.)--Dept. of Electrical Engineering, Stanford University."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Engineer");
  }

  @Test
  public void test502NotEngineer()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a2230657");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (M.A.)--Dept. of English, Stanford University."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Arts (MA)");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Type of degree|Master's|Engineer");
  }

  @Test
  public void test502JSD()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a7912414");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (J.S.D)--Stanford University, 2008."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', "School of Law."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Doctor of the Science of Law (JSD)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|School of Law");
  }

  @Test
  public void test502JSM()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a1811178");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (J.S.M.)--Stanford University, 1972."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of the Science of Law (JSM)");
  }

  @Test
  public void test502LLM()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a1803872");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (L.L.M.) - Stanford University."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Laws (LLM)");
  }

  @Test
  public void test502AM()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a2001059");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (A.M.)--Leland Stanford Junior University."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', "Department of Chemistry"));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Arts (MA)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Department of Chemistry");
  }

  @Test
  public void test502MA()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a5730269");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('g', "Thesis"));
    df.addSubfield(factory.newSubfield('b', "M.A."));
    df.addSubfield(factory.newSubfield('c', "Stanford University"));
    df.addSubfield(factory.newSubfield('d', "1939."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Arts (MA)");
  }

  @Test
  public void test502MA2()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a2188938");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "The editor's thesis (M.A.)--Dept. of Modern European Languages, Stanford University."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Arts (MA)");
  }

  @Test
  public void test502MA3()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a5628179");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (M.A)--Stanford University, 1931."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Arts (MA)");
  }

  @Test
  public void test502NotMA()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "anotMA");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University drama master's thesis"));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Type of degree|Master's|Master of Arts (MA)");
  }

  @Test
  public void test502MD()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a11652845");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (M.D.)--Stanford University, 1931."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University"));
    df.addSubfield(factory.newSubfield('b', "School of Medicine."));
    df.addSubfield(factory.newSubfield('b', "Department of Medicine."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Doctor of Medicine (MD)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|School of Medicine");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Department, school, or program|Department of Medicine");
  }

  @Test
  public void test502MFA()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a10197046");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (M.F.A.)--Stanford University, 2013."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', "Master of Fine Arts."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', "Department of Art and Art History."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Fine Arts (MFA)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Master of Fine Arts");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Department of Art and Art History");
  }

  @Test
  public void test502MLA()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a10370180");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (M.L.A.)--Stanford University, 2013."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', "Master of Liberal Arts Program."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Liberal Arts (MLA)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Master of Liberal Arts Program");
  }

  @Test
  public void test502MLS()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a5799855");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (M.L.S.)--Stanford University, 2003."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford Program in International Legal Studies."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Legal Studies (MLS)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Stanford Program in International Legal Studies");
  }

  @Test
  public void test502MS()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a2478369");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (M.S.)--Stanford University, 1993."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', "Department of Applied Earth Sciences."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Science (MS)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Department of Applied Earth Sciences");
  }

  @Test
  public void test502DegreeMS()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a4106221");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('b', "Degree of Master of Science"));
    df.addSubfield(factory.newSubfield('c', "Stanford University"));
    df.addSubfield(factory.newSubfield('d', "1998"));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', "Department of Biological Sciences."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Science (MS)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Department of Biological Sciences");
  }

  @Test
  public void test502PhD()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a12080422");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (Ph.D.)--Stanford University, 2017."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', "Department of Electrical Engineering."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Doctor of Philosophy (PhD)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Department of Electrical Engineering");
  }

  @Test
  public void test502StudentReport()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a8390172");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Student Report--Stanford University, 2009."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', "Department of Petroleum Engineering."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Other|Student report");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Department of Petroleum Engineering");
  }

  @Test
  public void test502ThesisGradSchoolBusiness()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a10037295");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis--Graduate School of Business, Stanford University."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', "Graduate School of Business"));
    df.addSubfield(factory.newSubfield('t', "Dissertation."));
    df.addSubfield(factory.newSubfield('d', "1979."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Unspecified");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Graduate School of Business");
  }

  @Test
  public void test502UgradHonorsThesis()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a2759546");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Honors thesis (B.A.)--Stanford University."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Bachelor's|Undergraduate honors thesis");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Type of degree|Bachelor's|Bachelor of Arts (BA)");
  }

  @Test
  public void test502UgradSeniorHonorsProject()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a750717");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Senior Honors project - Department of Music, Stanford University."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Bachelor's|Undergraduate honors thesis");
  }

  @Test
  public void test502UnspecifiedDoctoral()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a4086853");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Doctoral dissertation, Stanford University."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Unspecified");
    solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
  }

  @Test
  public void test502UnspecifiedMasters()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a8109556");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Master's project--Stanford University, 2005."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Unspecified");
    solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
  }

  @Test
  public void test502CatchAll()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a2163948");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('g', "Thesis"));
    df.addSubfield(factory.newSubfield('c', "Stanford University"));
    df.addSubfield(factory.newSubfield('d', "1930."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Other|Thesis Stanford University");
    solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
  }

  @Test
  public void test502WithSpaces()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "aWithSpaces");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (Ed. D. )--Stanford University, 2008."));
    record.addVariableField(df);

    df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (Ph. D.) - Dept, of Music, Stanford University."));
    record.addVariableField(df);

    df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (M. F. A.) -- Stanford University."));
    record.addVariableField(df);

    df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "M.A. Stanford University."));
    df.addSubfield(factory.newSubfield('g', "1923."));
    record.addVariableField(df);

    df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (B.A. Music) -- Stanford University."));
    record.addVariableField(df);

    df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (M. S.)--Stanford University, 1983."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Doctor of Education (EdD)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Doctor of Philosophy (PhD)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Fine Arts (MFA)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Arts (MA)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Bachelor's|Bachelor of Arts (BA)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Science (MS)");
  }

  @Test
  public void test502WithOutSpaces()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "aWithOutSpaces");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis(A.M.)--Leland Stanford Junior University."));
    record.addVariableField(df);

    df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis(B.A.)--Stanford University, 2002."));
    record.addVariableField(df);

    df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (E.Eng.)--Dept. of Electrical Engineering, Stanford University."));
    record.addVariableField(df);

    df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (Ph .D.)--Stanford University, 2010."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Arts (MA)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Bachelor's|Bachelor of Arts (BA)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Engineer");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Doctor of Philosophy (PhD)");
  }

  @Test
  public void test502PeriodsMissing()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "aPeriodsMissing");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (JSD)--Stanford University, 2008."));
    record.addVariableField(df);

    df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis Phd.D.)--School of Education, Stanford University."));
    record.addVariableField(df);

    df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (LL.M.)--Stanford University, 1943."));
    record.addVariableField(df);

    df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (MFA) --Stanford University."));
    record.addVariableField(df);

    df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('g', "Thesis"));
    df.addSubfield(factory.newSubfield('b', "M.A"));
    df.addSubfield(factory.newSubfield('c', "Stanford University"));
    df.addSubfield(factory.newSubfield('d', "1960"));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Doctor of the Science of Law (JSD)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Doctor of Philosophy (PhD)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Laws (LLM)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Fine Arts (MFA)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Master's|Master of Arts (MA)");
  }

  @Test
  public void test502NoDept()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a10152502");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Dissertation (Ph.D.)-- Stanford University."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Doctor of Philosophy (PhD)");
    solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
  }

  @Test
  public void test502NotStanford()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a3312224");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('b', "M.A."));
    df.addSubfield(factory.newSubfield('c', "California State University, Fresno."));
    df.addSubfield(factory.newSubfield('d', "1996"));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "California State University, Fresno."));
    df.addSubfield(factory.newSubfield('b', "Department of English."));
    record.addVariableField(df);

    solrFldMapTest.assertNoSolrFld(record, fldName);
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Type of degree|Master's|Master of Arts (MA)");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Department, school, or program|Department of English");
  }

  @Test
  public void test502Multiple710SubB()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a5638754");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('b', "Ph. D."));
    df.addSubfield(factory.newSubfield('c', "Stanford University"));
    df.addSubfield(factory.newSubfield('d', "1924"));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University"));
    df.addSubfield(factory.newSubfield('b', "Department of Pharmacology."));
    df.addSubfield(factory.newSubfield('b', "School of Medicine."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Doctor of Philosophy (PhD)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Department of Pharmacology");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Department, school, or program|School of Medicine");
  }

  @Test
  public void test502Multiple710NoSubB()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a11073269");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (Ph.D.); Supercedes report DE98059289; Thesis submitted to Stanford Univ., CA (US); TH: Thesis (Ph.D.); PBD: May 1997; PBD: 1 May 1997"));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford Linear Accelerator Center."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "United States."));
    df.addSubfield(factory.newSubfield('b', "Dept. of Energy. "));
    df.addSubfield(factory.newSubfield('b', "Office of Energy Research"));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Doctor of Philosophy (PhD)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Stanford Linear Accelerator Center");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Department, school, or program|Dept. of Energy");
  }

  @Test
  public void test502One710EmptySubB()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("01737cam a2200445Ka 4500"));
    ControlField cf = factory.newControlField("001", "a2101659");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('g', "Thesis"));
    df.addSubfield(factory.newSubfield('b', "Ph.D."));
    df.addSubfield(factory.newSubfield('c', "Stanford University"));
    df.addSubfield(factory.newSubfield('d', "1960."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', " "));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Doctor of Philosophy (PhD)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Stanford University");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Department, school, or program|");
    solrFldMapTest.assertSolrFldHasNoValue(record, fldName, "Department, school, or program");
  }

  @Test
  public void test710ReplaceDeptWithDepartment()
  {
    Record record = factory.newRecord();
    record.setLeader(factory.newLeader("03884nam a22002897i 4500"));
    ControlField cf = factory.newControlField("001", "a12116908");
    record.addVariableField(cf);
    DataField df = factory.newDataField("502", ' ', ' ');
    df.addSubfield(factory.newSubfield('a', "Thesis (Ph.D.)--Stanford University, 2017."));
    record.addVariableField(df);

    df = factory.newDataField("710", '2', ' ');
    df.addSubfield(factory.newSubfield('a', "Stanford University."));
    df.addSubfield(factory.newSubfield('b', "Dept. of Material Sci & Eng."));
    record.addVariableField(df);

    solrFldMapTest.assertSolrFldValue(record, fldName, "Type of degree|Doctoral|Doctor of Philosophy (PhD)");
    solrFldMapTest.assertSolrFldValue(record, fldName, "Department, school, or program|Department of Material Sci & Eng");
  }

}
