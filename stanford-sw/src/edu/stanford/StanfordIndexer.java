package edu.stanford;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.regex.*;

import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.*;
//could import static, but this seems clearer
import org.solrmarc.tools.*;

import edu.stanford.enumValues.*;

/**
 * Stanford custom methods for SolrMarc
 * @author Naomi Dushay
 */
public class StanfordIndexer extends org.solrmarc.index.SolrIndexer
{
  /** name of map used to translate raw location code to display value
   *   map used to determine if call numbers should be lopped */
  private static String LOCATION_MAP_NAME = null;

  /** locations indicating item should not be displayed */
  static Set<String> SKIPPED_LOCS = null;
  /** locations indicating item is missing or lost */
  static Set<String> MISSING_LOCS = null;
  /** locations indicating item is online */
  static Set<String> ONLINE_LOCS = null;
  /** locations indicating item is a government document */
  static Set<String> GOV_DOC_LOCS = null;
  /** locations indicating item is not shelved by callnum */
  static Set<String> SHELBY_LOCS = null;
  /** locations indicating business library item is not shelved by callnum */
  static Set<String> BIZ_SHELBY_LOCS = null;
  /** call numbers that should not be displayed */
  static Set<String> SKIPPED_CALLNUMS = null;
  /** locations indicating art library item is in the locked stacks */
  static Set<String> ART_LOCKED_LOCS = null;

  /**
   * Default constructor
     * @param indexingPropsFile the name of xxx_index.properties file mapping
     *  solr field names to values in the marc records
     * @param propertyDirs - array of directories holding properties files
   */
  public StanfordIndexer(String indexingPropsFile, String[] propertyDirs)
        throws FileNotFoundException, IOException, ParseException
    {
    super(indexingPropsFile, propertyDirs);
        try
        {
          LOCATION_MAP_NAME = loadTranslationMap(null, "location_map.properties");
        }
        catch (IllegalArgumentException e)
        {
      e.printStackTrace();
    }

        SKIPPED_LOCS = PropertiesUtils.loadPropertiesSet(propertyDirs, "locations_skipped_list.properties");
        MISSING_LOCS = PropertiesUtils.loadPropertiesSet(propertyDirs, "locations_missing_list.properties");
        ONLINE_LOCS = PropertiesUtils.loadPropertiesSet(propertyDirs, "locations_online_list.properties");
        GOV_DOC_LOCS = PropertiesUtils.loadPropertiesSet(propertyDirs, "gov_doc_location_list.properties");
        SHELBY_LOCS = PropertiesUtils.loadPropertiesSet(propertyDirs, "locations_shelby_list.properties");
        BIZ_SHELBY_LOCS = PropertiesUtils.loadPropertiesSet(propertyDirs, "locations_biz_shelby_list.properties");
        SKIPPED_CALLNUMS = PropertiesUtils.loadPropertiesSet(propertyDirs, "callnums_skipped_list.properties");
        ART_LOCKED_LOCS = PropertiesUtils.loadPropertiesSet(propertyDirs, "art_locked_location_list.properties");

        // try to reuse HashSet, etc. objects instead of creating fresh each time
        old_formats = new LinkedHashSet<String>();
        main_formats = new LinkedHashSet<String>();
        accessMethods = new HashSet<String>();
        sfxUrls = new LinkedHashSet<String>();
        fullTextUrls = new LinkedHashSet<String>();
        managedPurls = new LinkedHashSet<String>();
        buildings = new HashSet<String>();
        shelfkeys = new HashSet<String>();
        govDocCats = new HashSet<String>();
        itemSet = new LinkedHashSet<Item>();
        collectionDruids = new HashSet<String>();
        collectionsWithTitles = new HashSet<String>();
        displayType = new HashSet<String>();
        fileId = new HashSet<String>();
        bookplatesDisplay = new LinkedHashSet<String>();
        fundFacet = new LinkedHashSet<String>();
        locationFacet = new LinkedHashSet<String>();
  }

  // variables used in more than one method
  /** the id of the record - used for error messages in addition to id field */
  String id = null;
  /** @deprecated the old formats of the record, kept for UI URL continuity */
  Set<String> old_formats;
  /** the formats of the record - used for display rules in addition to format field */
  Set<String> main_formats;
  /** sfxUrls are used for access_method in addition to sfxUrl field */
  Set<String> sfxUrls;
  /** fullTextUrls are used for access_method in addition to fullTextUrl field */
  Set<String> fullTextUrls;
  /** accessMethods are used for format in addition to access_method field */
  Set<String> accessMethods;
  /** buildings are used for topics due to weird law 655s */
  Set<String> buildings;
  /** shelfkeys are used for reverse_shelfkeys */
  Set<String> shelfkeys;
  /** govDocCats are used for top level call number facet */
  Set<String> govDocCats;
  /** isSerial is used for shelfkeys and item_display */
  boolean isSerial;
  /** managedPurls are used for access_method */
  Set<String> managedPurls;
  /** collectionDruids are used in UI to display corresponding collections for digitized items */
  Set<String> collectionDruids;
  /** collectionsWithTitles are used in UI to display corresponding collections for digitized items */
  Set<String> collectionsWithTitles;
  Set<String> displayType;
  String collectionType = null;
  Set<String> fileId;
  Set<String> bookplatesDisplay;
  Set<String> fundFacet;
  Set<String> locationFacet;

  /** 008 field */
  ControlField cf008 = null;
  /** cf008date1 is bytes 7-10 (0 based index) in 008 field */
  String cf008date1 = null;
  /** 007 field */
  ControlField cf007 = null;
  /** 006 field */
  ControlField cf006 = null;

  /** date260c is a four character String containing year from 260c
   * "cleaned" per DateUtils.cleanDate() */
  String date260c = null;
  /** Set of 020 subfield a */
  Set<String> f020suba;
  /** Set of 020 subfield z */
  Set<String> f020subz;
  /** Set of 655 subfield a */
  Set<String> f655suba;
  /** Set of 956 subfield u */
  Set<String> f956subu;

  /** all items without skipped locations (shadowed, withdrawn) as a Set of
   *  Item objects */
  Set<Item> itemSet;

  /** true if the record has items, false otherwise.  Used to detect on-order records */
  boolean has999s = false;

  /** all LC call numbers from the items without skipped locations */
  Set<String> lcCallnums;
  /** all Dewey call numbers from the items without skipped locations */
  Set<String> deweyCallnums;

  /**
   * Method from superclass allowing processing that can be done once per
   * record, rather than repeatedly for several indexing specifications,
   * especially custom methods. The default version does nothing.
   * @param record - The MARC record that is being indexed.
   */
  @SuppressWarnings("unchecked")
  protected void perRecordInit(Record record) {
    cf006 = (ControlField) record.getVariableField("006");
    cf007 = (ControlField) record.getVariableField("007");
    cf008 = (ControlField) record.getVariableField("008");
    if (cf008 != null)
      cf008date1 = cf008.getData().substring(7, 11);
    else
      cf008date1 = null;
    date260c = MarcUtils.getDate(record);
    f020suba = MarcUtils.getFieldList(record, "020a");
    f020subz = MarcUtils.getFieldList(record, "020z");
    f655suba = MarcUtils.getFieldList(record, "655a");
    f956subu = MarcUtils.getFieldList(record, "956u");

    List<VariableField> list999df = record.getVariableFields("999");
    has999s = !list999df.isEmpty();

    setId(record);
    boolean getBrowseCallnumFromBib = true;

    itemSet.clear();
    for (VariableField vf999 : list999df) {
      DataField df999 = (DataField) vf999;
      Item item = new Item(df999, id);
      if (!item.shouldBeSkipped())
        itemSet.add(item);
      // we need to get a browseable call number from bib only if
      //   all items are online, or all items have callnum of "NO CALL NUMBER"
      if (getBrowseCallnumFromBib) {
        if (!item.isOnline() && !item.hasIgnoredCallnum())
          getBrowseCallnumFromBib = false;
      }
    }

    setSFXUrls(); // doesn't need record b/c they come from 999s, which are already in itemSet
    setFullTextUrls(record);
    setAccessMethods(record);
    setOldFormats(record);  // vestigial for continuity in UI URLs for old formats
    setMainFormats(record);
    isSerial = main_formats.contains(Format.JOURNAL_PERIODICAL.toString());
    ItemUtils.lopItemCallnums(itemSet, findTranslationMap(LOCATION_MAP_NAME), isSerial);
    setBuildings(record);
    setGovDocCats(record);

    if (getBrowseCallnumFromBib) {
      // get a call number from the bib fields, if there is one
      boolean isGovDoc = !govDocCats.isEmpty();
      CallNumUtils.setCallnumsFromBib(record, itemSet, isGovDoc);
    }

    setShelfkeys(record);

    lcCallnums = CallNumUtils.getLCcallnums(itemSet);
    for (String callnum : lcCallnums) {
      if (!org.solrmarc.tools.CallNumUtils.isValidLC(callnum))
        lcCallnums.remove(callnum);
    }

    deweyCallnums = CallNumUtils.getDeweyNormCallnums(itemSet);

    managedPurls.clear();
    collectionDruids.clear();
    collectionsWithTitles.clear();
    collectionType = null;
    displayType.clear();
    fileId.clear();
    bookplatesDisplay.clear();
    fundFacet.clear();
    locationFacet.clear();

    collectionDruids.add("sirsi");
    displayType.add("sirsi");
    processManaged856s(record);
    setBookplatesDisplay(record);
    setFundFacet(record);
    setLocationFacet(record);
    addSDRfrom856s(record);

  }

// Id Methods  -------------------- Begin --------------------------- Id Methods

  /**
   * Get local id for the Marc record.
   * @param record a marc4j Record object
   */
  public String getId(final Record record) {
    return id;
  }

  /**
   * Assign id of record to be the ckey. Our ckeys are in 001 subfield a.
   * Marc4j is unhappy with subfields in a control field so this is a kludge
   * work around.
   */
  private void setId(final Record record)
  {
    id = null;
    ControlField fld = (ControlField) record.getVariableField("001");
    if (fld != null && fld.getData() != null)
    {
      String rawVal = fld.getData();
      if (rawVal.startsWith("a"))
        id = rawVal.substring(1);
    }
  }

// Id Methods  --------------------- End ---------------------------- Id Methods

// Format Methods  --------------- Begin ------------------------ Format Methods

  /**
   * keeping these formats around for continuity in UI URLs for old formats
   * @return Set of strings containing format values for the resource
   * @param record a marc4j Record object
   * @deprecated (used for old format only)
   */
  public Set<String> getOldFormats(final Record record)
  {
    return old_formats;
  }

  /**
   * Assign formats per algorithm and marc bib record
   * keeping these formats around for continuity in UI URLs for old formats
   *  As of July 28, 2008, algorithms for formats are currently in email
   *  message from Vitus Tang to Naomi Dushay, cc Phil Schreur, Margaret
   *  Hughes, and Jennifer Vine dated July 23, 2008.
   * @deprecated (used for old format only)
   */
  @SuppressWarnings("unchecked")
  private void setOldFormats(final Record record)
  {
    old_formats.clear();

    // assign formats based on leader chars 06, 07 and chars in 008
    String leaderStr = record.getLeader().marshal();
    old_formats.addAll(FormatUtils.getFormatsPerLdrAnd008Old(leaderStr, cf008));

    if (old_formats.isEmpty()) {
      // see if it's a serial for format assignment
      char leaderChar07 = leaderStr.charAt(7);
      VariableField f006 = record.getVariableField("006");
      String serialFormat = FormatUtils.getSerialFormat(leaderChar07, cf008, f006);
      if (serialFormat != null)
        old_formats.add(serialFormat);
    }

    // look for conference proceedings in 6xx
    List<DataField> dfList = (List<DataField>) record.getDataFields();
    for (DataField df : dfList) {
      if (df.getTag().startsWith("6")) {
        List<String> subList = MarcUtils.getSubfieldStrings(df, 'x');
        subList.addAll(MarcUtils.getSubfieldStrings(df, 'v'));
        for (String s : subList) {
          if (s.toLowerCase().contains("congresses")) {
            old_formats.remove(FormatOld.JOURNAL_PERIODICAL.toString());
            old_formats.add(FormatOld.CONFERENCE_PROCEEDINGS.toString());
          }
        }
      }
    }

    // check for format information from 999 ALPHANUM call numbers
    // and from itemType (999 subfield t)
    for (Item item : itemSet) {
      if (item.getCallnumType() == CallNumberType.ALPHANUM && !item.getLibrary().equals("SPEC-COLL")) {
        String callnum = item.getCallnum();
        if (callnum.startsWith("MFILM") || callnum.startsWith("MFICHE"))
          old_formats.add(FormatOld.MICROFORMAT.toString());
        else if (callnum.startsWith("MCD"))
          old_formats.add(FormatOld.MUSIC_RECORDING.toString());
        else if (callnum.startsWith("ZDVD") || callnum.startsWith("ADVD"))
          old_formats.add(FormatOld.VIDEO.toString());
      }
      if (item.getType().equalsIgnoreCase("DATABASE"))
        old_formats.add(FormatOld.DATABASE_A_Z.toString());
    }

    if (FormatUtils.isMicroformatOld(record))
      old_formats.add(FormatOld.MICROFORMAT.toString());

    if (!record.getVariableFields("502").isEmpty())
      old_formats.add(FormatOld.THESIS.toString());

    // if we still don't have a format, it's an "other"
    if (old_formats.isEmpty() || old_formats.size() == 0)
      old_formats.add(FormatOld.OTHER.toString());
  }


  /**
   * @return Set of strings containing format values for the resource
   * @param record a marc4j Record object
   */
  public Set<String> getMainFormats(final Record record)
  {
    return main_formats;
  }

  /**
   * Assign formats per decisions made late fall 2013
   * INDEX-14 updating database being folded into Database_A_Z
   *  INDEX-16 updating website being folded into Journal_Periodical
   *  INDEX-15 updating other (default) being folded into Book
   */
  @SuppressWarnings("unchecked")
  private void setMainFormats(final Record record)
  {
    main_formats.clear();

    // assign formats based on leader chars 06, 07 and chars in 008
    String leaderStr = record.getLeader().marshal();
    char leaderChar06 = leaderStr.charAt(6);
    char leaderChar07 = leaderStr.charAt(7);
    main_formats.addAll(FormatUtils.getFormatsPerLdrAnd008(leaderStr, cf008));

    String journalVal = Format.JOURNAL_PERIODICAL.toString();
    if (main_formats.isEmpty())
    {
      // see if it's a serial for format assignment
      char cf008c21 = '\u0000';
      if (cf008 != null)
        cf008c21 = ((ControlField) cf008).getData().charAt(21);

      VariableField f006 = record.getVariableField("006");
      String serialFormat = FormatUtils.getMainFormatSerial(leaderChar07, cf008c21, (ControlField) f006);
      if (serialFormat != null)
        main_formats.add(serialFormat);

      // see if it's an integrating resource
      if (main_formats.isEmpty() && leaderChar07 == 'i' &&  cf008c21 != '\u0000')
      {
        String integrFormat = FormatUtils.getIntegratingMainFormatFromChar(cf008c21);
        if (integrFormat != null)
          main_formats.add(integrFormat);
      }

    }

    // check for format information from 999 ALPHANUM call numbers
    // and from itemType (999 subfield t)
    String dbazVal = Format.DATABASE_A_Z.toString();
    for (Item item : itemSet) {
      if (item.getType().equalsIgnoreCase("DATABASE"))
      {
        main_formats.add(dbazVal);

        // if it is a Database and a Computer File, and it is not
        //  "at the library", then it should only be a Database
        String compFileVal = Format.COMPUTER_FILE.toString();
        if (main_formats.contains(compFileVal) &&
          !accessMethods.contains(Access.AT_LIBRARY.toString()))
          main_formats.remove(compFileVal);
      }

      /* If the call number prefixes in the MARC 999a are for Manuscript/Archive items, add Manuscript/Archive format
       * A (e.g. A0015), F (e.g. F0110), M (e.g. M1810), MISC (e.g. MISC 1773), MSS CODEX (e.g. MSS CODEX 0335),
        MSS MEDIA (e.g. MSS MEDIA 0025), MSS PHOTO (e.g. MSS PHOTO 0463), MSS PRINTS (e.g. MSS PRINTS 0417),
        PC (e.g. PC0012), SC (e.g. SC1076), SCD (e.g. SCD0012), SCM (e.g. SCM0348), and V (e.g. V0321).  However,
        A, F, M, PC, and V are also in the Library of Congress classification which could be in the 999a, so need to make sure that
        the call number type in the 999w == ALPHANUM and the library in the 999m == SPEC-COLL.
       */
      if (item.getLibrary().equals("SPEC-COLL") && item.getCallnumType().equals(CallNumberType.ALPHANUM))
      {
        Pattern callNumPattern = Pattern.compile("^(A\\d|F\\d|M\\d|MISC \\d|(MSS (CODEX|MEDIA|PHOTO|PRINTS))|PC\\d|SC[\\d|D|M]|V\\d).*", Pattern.CASE_INSENSITIVE);
        Matcher callNumMatcher = callNumPattern.matcher(item.getCallnum());

        if (callNumMatcher.matches())
          main_formats.add(Format.MANUSCRIPT_ARCHIVE.toString());
      }

       // INDEX-124 If 245h = [manuscript] and 999m = LANE-MED --> Book resource type
      DataField title = (DataField) record.getVariableField("245");
      if (title != null && title.getSubfield('h') != null)
        if (item.hasLaneLoc() && title.getSubfield('h').toString().contains("manuscript"))
          main_formats.add(Format.BOOK.toString());

      // INDEX-124 If Leader/06 = a or t and Leader/07 = c or d and 999m = LANE-MED, assign Book as Resource Type
      // remove Archive/Manuscript resource type and add Book resource type
      if (item.hasLaneLoc()) {
        if ((leaderChar06 == 'a' || leaderChar06 == 't') && (leaderChar07 == 'c' || leaderChar07 == 'd')) {
          main_formats.add(Format.BOOK.toString());
          main_formats.remove(Format.MANUSCRIPT_ARCHIVE.toString());
        }
      }

    }

    if (FormatUtils.isMarcit(record))
      main_formats.add(journalVal);

    // If it is Equipment, add Equipment resource type and remove 3D object resource type
    // INDEX-123 If it is Equipment, that should be the only item in main_formats
    if (FormatUtils.isEquipment(record)) {
      main_formats.clear();
      main_formats.add(Format.EQUIPMENT.toString());
    }

    if (main_formats.isEmpty() || main_formats.contains(Format.OTHER.toString()))
    {
      // Use value of 245h to determine resource type and remove Other resource type
      DataField title = (DataField) record.getVariableField("245");

      if (title != null && title.getSubfield('h') != null)
      {
        String formatFrom245h = FormatUtils.getFormatsPer245h(title.getSubfield('h').toString(), cf007);
        if (formatFrom245h != null)
        {
          main_formats.add(formatFrom245h);
          main_formats.remove(Format.OTHER.toString());
        }
      }
    }

    // if we still don't have a format, it's an "other"
    if (main_formats.isEmpty() || main_formats.size() == 0)
      main_formats.add(Format.OTHER.toString());
  }

  /**
   * INDEX-89 Video Physical Formats - The order of checking for data has discussed and this is the order suggested: call number, then 538$a,
   * then 300$b and 347$b, and finally 007
   * @return Set of strings containing physical format values for the resource
   * @param record a marc4j Record object
   */
  public Set<String> getPhysicalFormats(final Record record)
  {
    Set<String> format538 = new HashSet<String>();
    Set<String> format3xx = new HashSet<String>();
    Set<String> format999a = new HashSet<String>();

    Set<String> physicalFormats = new HashSet<String>();

    // INDEX-89 - Add video physical formats from call numbers
    format999a = FormatUtils.getPhysicalFormat999(record);
    if (format999a != null)
      physicalFormats.addAll(format999a);

    physicalFormats.addAll(FormatUtils.getPhysicalFormatsPer007(record.getVariableFields("007"), accessMethods));

    // INDEX-89 - Add video physical formats from 538$a
    format538 = FormatUtils.getPhysicalFormat538(record);
    if (format538 != null)
      physicalFormats.addAll(format538);

    // INDEX-89 - Add video physical formats from 300$b, 347$b
    format3xx = FormatUtils.getPhysicalFormat3xxb(record);
    if (format3xx != null)
      physicalFormats.addAll(format3xx);

    String mfilmVal = FormatPhysical.MICROFILM.toString();
    String mficheVal = FormatPhysical.MICROFICHE.toString();

    // check call numbers for physical format information
    for (Item item : itemSet) {
      String callnum = item.getCallnum();
      if (!physicalFormats.contains(mficheVal) && callnum.startsWith("MFICHE"))
        physicalFormats.add(mficheVal);
      if (!physicalFormats.contains(mfilmVal) && callnum.startsWith("MFILM"))
        physicalFormats.add(mfilmVal);
    }

    // check for format information in 300

    // check in all alpha subfields
    Set<String> df300abcSet = MarcUtils.getAllAlphaSubfields(record, "300");
    for (String df300abc : df300abcSet)
    {
      String CDphysform = FormatPhysical.CD.toString();
      if (!physicalFormats.contains(CDphysform) && FormatUtils.describesCD(df300abc))
        physicalFormats.add(CDphysform);
      String vinylPhysform = FormatPhysical.VINYL.toString();
      if (!physicalFormats.contains(vinylPhysform) && FormatUtils.describesVinyl(df300abc))
        physicalFormats.add(vinylPhysform);
    }

    // check subfield a only
    for  (Object obj300 : record.getVariableFields("300"))
    {
      DataField df300 = (DataField) obj300;
      for (Object subaObj : df300.getSubfields('a'))
      {
        String subaStr = ((Subfield) subaObj).getData().toLowerCase();
        if (subaStr.contains("microfiche") && !physicalFormats.contains(mficheVal))
          physicalFormats.add(mficheVal);
        if (subaStr.contains("microfilm") && !physicalFormats.contains(mfilmVal))
          physicalFormats.add(mfilmVal);
        String photoValPlain = FormatPhysical.PHOTO.toString();
        if (subaStr.contains("photograph") && !physicalFormats.contains(photoValPlain))
          physicalFormats.add(photoValPlain);
        String rsiValPlain = FormatPhysical.REMOTE_SENSING_IMAGE.toString();
        if ((subaStr.contains("remote-sensing image") ||  subaStr.contains("remote sensing image"))
          && !physicalFormats.contains(rsiValPlain))
          physicalFormats.add(rsiValPlain);
        String slideValPlain = FormatPhysical.SLIDE.toString();
        if (subaStr.contains("slide") && !physicalFormats.contains(slideValPlain))
          physicalFormats.add(slideValPlain);
      }
    }

    return physicalFormats;
  }


  public Set<String> getGenres(final Record record)
  {
    String leaderStr = record.getLeader().marshal();
    char leaderChar07 = leaderStr.charAt(7);
    char leaderChar06 = leaderStr.charAt(6);

    Set<String> resultSet = new HashSet<String>();

    // look for thesis by existence of 502 field
    if (!record.getVariableFields("502").isEmpty())
      resultSet.add(Genre.THESIS.toString());

    // look for conference proceedings in 6xx sub x or v
    List<DataField> dfList = (List<DataField>) record.getDataFields();
    for (DataField df : dfList) {
      if (df.getTag().startsWith("6")) {
        List<String> subList = MarcUtils.getSubfieldStrings(df, 'x');
        subList.addAll(MarcUtils.getSubfieldStrings(df, 'v'));
        for (String s : subList) {
          if (s.toLowerCase().contains("congresses")) {
            resultSet.add(Genre.CONFERENCE_PROCEEDINGS.toString());
          }
        }
      }
    }

    /** Based upon SW-1056, added the following to the algorithm to determine if something is a conference proceeding:
     *  Leader/07 = 'm' or 's' and 008/29 = '1'
     **/
    if (leaderChar07 == 'm' || leaderChar07 == 's') {
      // check if it's a conference proceeding based on 008 char 29
      char c29 = '\u0000';
      if (cf008 != null && cf008.getData().length() >= 30) {
        c29 = ((ControlField) cf008).getData().charAt(29);
        if (c29 == '1')
          resultSet.add(Genre.CONFERENCE_PROCEEDINGS.toString());
      }
    }

    /** Based upon SW-1489, if the record is for a certain format (MARC, MRDF,
     *  MAP, SERIAL, or VM and not SCORE, RECORDING, and MANUSCRIPT) and it has
     *  something in the 008/28 byte, I’m supposed to give it a genre type of
     *  government document
    **/
    if (cf008 != null && cf008.getData().length() >= 29) {
      if (!main_formats.contains(Format.MUSIC_SCORE.toString()) &&
          !main_formats.contains(Format.MUSIC_RECORDING.toString()) &&
          !main_formats.contains(Format.MANUSCRIPT_ARCHIVE.toString()) &&
          cf008.find("^.{28}[a-z]"))
      {
        resultSet.add(Genre.GOVERNMENT_DOCUMENT.toString());
      }
    }
    /** Based upon SW-1506 - add technical report as a genre if
     *  leader/06: a or t AND 008/24-27 (any position, i.e. 24, 25, 26, or 27): t
     *    OR
     *  Presence of 027 OR 088
     *    OR
     *  006/00: a or t AND 006/7-10 (any position, i.e. 7, 8, 9, or 10): t
    **/
    if (cf008 != null && cf008.getData().length() >= 28) {
      String tech_rpt008 = cf008.getData().substring(24, 28);
      if ((leaderChar06 == 'a' || leaderChar06 == 't') &&
          tech_rpt008.contains("t")) {
        resultSet.add(Genre.TECHRPTS.toString());
      }
    } else if (!record.getVariableFields("027").isEmpty() ||
        !record.getVariableFields("088").isEmpty()) {
      resultSet.add(Genre.TECHRPTS.toString());
    } else if (cf006 != null && cf006.getData().length() >= 11) {
      String cf00601 = cf006.getData().substring(0, 1);
      String tech_rpt006 = cf006.getData().substring(7, 11);
      if ((cf00601.contains("a") || cf00601.contains("t")) &&
          tech_rpt006.contains("t")) {
        resultSet.add(Genre.TECHRPTS.toString());
      }
    }
    return resultSet;
  }


// Format Methods  ---------------- End ------------------------- Format Methods

// Language Methods ---------------- Begin -------------------- Language Methods

  /**
   * returns the language codes from the 008, 041a and 041d fields, splitting
   *  out separate lang codes from 041a if they are smushed together.
   * @param record a marc4j Record object
   * @return Set of strings containing three letter language codes
   */
  public Set<String> getLanguages(final Record record)
  {
    Set<String> langResultSet = MarcUtils.getFieldList(record, "008[35-37]:041d:041e:041j");

    Set<String> lang041a = MarcUtils.getFieldList(record, "041a");
    for (String langCodeStr : lang041a) {
      int len = langCodeStr.length();
      if (len == 3)
        langResultSet.add(langCodeStr);
      else if (len % 3 == 0) {
        for (int startIx = 0; startIx < len; startIx += 3) {
          langResultSet.add(langCodeStr.substring(startIx, startIx+3));
        }
      }
    }

    return langResultSet;
  }


// Language Methods ----------------- End --------------------- Language Methods

// Standard Number Methods --------- Begin ------------- Standard Number Methods

  /**
   * returns the ISBN(s) from a record for external lookups (such as Google
   * Book Search) (rather than the potentially larger set of ISBNs for the end
   * user to search our index)
   * @param record a marc4j Record object
   * @return Set of strings containing ISBN numbers
   */
  public Set<String> getISBNs(final Record record)
  {
    // ISBN algorithm
    // 1. all 020 subfield a starting with 10 or 13 digits (last "digit" may be X). Ignore following text.
    // 2. if no ISBN from any 020 subfield a "yields a search result", use all 020 subfield z starting with 10 or 13 digits (last "digit" may be X). Ignore following text.
    Set<String> isbnSet = new LinkedHashSet<String>();
    if (!f020suba.isEmpty())
      isbnSet.addAll(Utils.returnValidISBNs(f020suba));

    if (isbnSet.isEmpty()) {
      isbnSet.addAll(Utils.returnValidISBNs(f020subz));
    }
    return isbnSet;
  }

  /**
   * returns the ISBN(s) from a record for the end user to search our index
   * (not the potentially smaller set of ISBNs for us to use for external
   * lookups such as Google Book Search)
   * @param record
   * @return Set of strings containing ISBN numbers
   */
  public Set<String> getUserISBNs(final Record record)
  {
    // ISBN algorithm - more inclusive
      // 1. all 020 subfield a starting with 10 or 13 digits (last "digit" may be X). Ignore following text.
    // AND
    // 2. all 020 subfield z starting with 10 or 13 digits (last "digit" may be X). Ignore following text.

    // per SW-522
    //  77x-78x subfield z

    Set<String> isbnSet = new HashSet<String>();

    Set<String> allCandidates = new HashSet<String>(f020suba);
    allCandidates.addAll(f020subz);
    allCandidates.addAll(MarcUtils.getFieldList(record, "770z:771z:772z:773z:774z:775z:776z:777z:778z:779z"));
    allCandidates.addAll(MarcUtils.getFieldList(record, "780z:781z:782z:783z:784z:785z:786z:787z:788z:789z"));
    isbnSet.addAll(Utils.returnValidISBNs(allCandidates));
    return isbnSet;
  }

  /**
     * returns the ISSN(s) from a record.  As ISSN is rarely multivalued, but
     *  MAY be multivalued, Naomi has decreed
     * This is a custom routine because we want multiple ISSNs only if they are
     * subfield a.
   * @param record a marc4j Record object
   * @return Set of strings containing ISSN numbers
   */
    public Set<String> getISSNs(final Record record)
    {
    // ISSN algorithm - rare but possible to have multiple ISSNs for an item
    // 1. 022 subfield a with ISSN
    // 2. if no ISSN from any 022 subfields a, use 022 subfield z

    // NOTE 1: the ISSN is always an eight digit number divided into two halves by a hyphen.
       // NOTE 2: the last digit of an ISSN is a check digit and could be an uppercase X.
        // INDEX-142 NOTE 3: Lane Medical adds (Print) or (Digital) descriptors to their ISSNs
        // so need to account for it in the pattern match below

    Set<String> issnSet = new HashSet<String>();

    Set<String> set = MarcUtils.getFieldList(record, "022a");
    if (set.isEmpty())
      set.addAll(MarcUtils.getFieldList(record, "022z"));

    Pattern p = Pattern.compile("^\\d{4}-\\d{3}[X\\d]\\D*$");
    Iterator<String> iter = set.iterator();
    while (iter.hasNext()) {
      String value = (String) iter.next().trim();
      // check we have the right pattern
      if (p.matcher(value).matches())
        issnSet.add(value);
    }
    return issnSet;
  }

  /**
   * returns the OCLC numbers from a record, if they exist. Note that this
   * method does NOT pad with leading zeros. (Who needs 'em?)
   * @param record a marc4j Record object
   * @return Set of Strings containing OCLC numbers. There could be none.
   */
    public Set<String> getOCLCNums(final Record record)
    {
    // OCLC number algorithm
    // 1. 035 subfield a, value prefixed "(OCoLC-M)" - remove prefix
    // 2. if no 035 subfield a prefixed "(OCoLC-M)",
      //      use 079 field subfield a, value prefixed "ocm" or "ocn" - remove prefix
      //      (If the id is eight digits in length, the prefix is "ocm", if 9 digits, "ocn")
      //      Id's that are smaller than eight digits are padded with leading zeros.
      // 3. if no "(OCoLC-M)" 035 subfield a and no "ocm" or "ocn" 079 field subfield a,
    // use 035 subfield a, value prefixed "(OCoLC)" - remove prefix

    Set<String> oclcSet = new LinkedHashSet<String>();

    Set<String> set035a = MarcUtils.getFieldList(record, "035a");
    oclcSet = Utils.getPrefixedVals(set035a, "(OCoLC-M)");
    if (oclcSet.isEmpty()) {
      // check for 079 prefixed "ocm" or "ocn"
      // 079 is not repeatable
      String val = MarcUtils.getFirstFieldVal(record, "079a");
      if (val != null && val.length() != 0)
      {
        String good = null;
        if (val.startsWith("ocm"))
          good = Utils.removePrefix(val, "ocm");
        else if (val.startsWith("ocn"))
          good = Utils.removePrefix(val, "ocn");
        else if (val.startsWith("on"))
          good = Utils.removePrefix(val, "on");
        if (good != null && good.length() != 0)
        {
          oclcSet.add(good.trim());
          return oclcSet;
        }
      }
      // check for 035a prefixed "(OCoLC)"
      oclcSet = Utils.getPrefixedVals(set035a, "(OCoLC)");
    }
    return oclcSet;
  }

// Standard Number Methods --------- End --------------- Standard Number Methods


// Title Methods ------------------- Begin ----------------------- Title Methods

  /**
     * returns string for title sort:  a string containing
     *  1. the uniform title (130), if there is one - not including non-filing chars
     *      as noted in 2nd indicator
     * followed by
     *  2.  the 245 title, not including non-filing chars as noted in ind 2
     *
   * @param record a marc4j Record object
   */
  public String getSortTitle(final Record record)
    {
    StringBuilder resultBuf = new StringBuilder();

    // uniform title
    DataField df = (DataField) record.getVariableField("130");
    if (df != null)
      resultBuf.append(MarcUtils.getAlphaSubfldsAsSortStr(df, false));

    // 245 (required) title statement
    df = (DataField) record.getVariableField("245");
    if (df != null)
      resultBuf.append(MarcUtils.getAlphaSubfldsAsSortStr(df, true));

    return resultBuf.toString().trim();
  }

// Title Methods -------------------- End ------------------------ Title Methods

  /**
   * return a set of "author-title" strings derived from:
   *   100,110,111 - all alpha except e + 240[a-z] ; 1xx + 245a if no 240
   *   700,710,711 - if no subfield t, ignore
   *     if there is a subfield t, all alpha except e, x
   *   800,810,811 - if no subfield t, ignore
   *     if there is a subfield t, all alpha except e, v, w, x
   *
   *   vern1xx all alpha except e + vern240; if no vern240, vern245a; if no vern240 and no vern 245a, then skip.
   *   vern7xx - as above
   *   vern8xx - as above
   *
   * @param record a marc4j Record object
   */
  public Set<String> getAuthorTitleSearch(final Record record)
  {
    Set<String> resultSet = new HashSet<String>(10);

    String one_xx_spec = "100[a-df-z]:110[a-df-z]:111[a-hj-z]";
    String two40_spec = "240[a-z]";
    String two45_spec = "245a";

    // 1xx + 24x
    String one_xx = MarcUtils.getFirstFieldVal(record, one_xx_spec);
    if (one_xx != null) {
      String two4x = MarcUtils.getFirstFieldVal(record, two40_spec);
      if (two4x == null) {
        two4x = MarcUtils.getFirstFieldVal(record, two45_spec);
      }
      resultSet.add(one_xx + " " + two4x);
    }

    // 880 version of 1xx + 24x
    //   vern1xx all alpha except e + vern240; if no vern240, vern245a; if no vern240 and no vern 245a, then skip.
    Set<String> vern_one_xx_set = MarcUtils.getLinkedField(record, one_xx_spec);
    if (vern_one_xx_set.size() > 0) {
          String vern_one_xx = vern_one_xx_set.iterator().next();
          // linked 240?
          Set<String> two40_set = MarcUtils.getLinkedField(record, two40_spec);
      String verntwo4x = null;
      if (two40_set.size() > 0)
        verntwo4x = two40_set.iterator().next();
          else {
            // linked 245?
        Set<String> two45_set = MarcUtils.getLinkedField(record, two45_spec);
        if (two45_set.size() > 0)
          verntwo4x = two45_set.iterator().next();
          }
      if (verntwo4x != null)
        resultSet.add(vern_one_xx + " " + verntwo4x);
    }


    String desiredTagFldSpec = "700:710:711:800:810:811";

    List<VariableField> fieldList = MarcUtils.getVariableFields(record, desiredTagFldSpec);
    resultSet.addAll(getAuthTitleStringsFrom7xx8xx(fieldList, false));

    // linked versions
    fieldList = MarcUtils.getLinkedVariableFields(record, desiredTagFldSpec);
    resultSet.addAll(getAuthTitleStringsFrom7xx8xx(fieldList, true));

    return resultSet;
  }


  /**
   * given a List of VariableField objects return a set of "author-title"
   *  strings derived from:
   *   7xx - if no subfield t, ignore
   *     if there is a subfield t, all alpha except e, x
   *   8xx - if no subfield t, ignore
   *     if there is a subfield t, all alpha except e, v, w, x
   *
   * @param fieldList - a List of VariableField objects containing 7xx and
   *    8xx fields (or their linked versions) desired for author-title searching
   * @param linked - true if the field list is for linked fields (880 fields
   *    corresponding to 7xx and 8xx fields).
   */
  @SuppressWarnings("unchecked")
  private List<String> getAuthTitleStringsFrom7xx8xx(List<VariableField> fieldList, boolean linked)
  {
    List<String> result = new ArrayList<String>();

    Pattern sub711pattern = Pattern.compile("[a-hj-wyz]");
    Pattern sub7xxpattern = Pattern.compile("[a-df-hj-wyz]");
    Pattern sub8xxpattern = Pattern.compile("[a-df-uyz]");
    for (VariableField vf : fieldList) {
      DataField df = (DataField) vf;
      if (df.getSubfield('t') != null) {
        String tag = null;
        if (linked)
          tag = df.getSubfield('6').getData();
        else
          tag = df.getTag();
        // for THIS field, we need to get the appropriate subfields
          List<Subfield> subfields = df.getSubfields();
          StringBuilder buffer = new StringBuilder();
          for (Subfield sf : subfields) {
            Matcher matcher = null;
            if (tag.startsWith("711"))
              matcher = sub711pattern.matcher(String.valueOf(sf.getCode()));
            else if (tag.startsWith("7"))
              matcher = sub7xxpattern.matcher(String.valueOf(sf.getCode()));
            else if (tag.startsWith("8"))
              matcher = sub8xxpattern.matcher(String.valueOf(sf.getCode()));
            if (matcher.matches()) {
                if (buffer.length() > 0)
                    buffer.append(" " + sf.getData());
                else
                    buffer.append(sf.getData());
              }
          }

          if (buffer.length() > 0)
            result.add(buffer.toString());
      } // end if |t
    }
    return result;
  }

// Subject Methods ----------------- Begin --------------------- Subject Methods

  /**
   * Gets the value strings, but skips over 655a values when Lane is one of
   * the locations. Also ignores 650a with value "nomesh".
   * @param record a marc4j Record object
     * @param fieldSpec - which marc fields / subfields to use as values
   * @return Set of strings containing values without Lane 655a or 650a nomesh
   */
    public Set<String> getTopicAllAlphaExcept(final Record record, final String fieldSpec)
    {
    Set<String> resultSet = MarcUtils.getAllAlphaExcept(record, fieldSpec);
    if (buildings.contains("LANE-MED"))
      resultSet.removeAll(f655suba);
    resultSet.remove("nomesh");
    return resultSet;
  }

  /**
   * Gets the value strings, but skips over 655a values when Lane is one of
   * the locations. Also ignores 650a with value "nomesh". Removes trailing
   * characters indicated in regular expression, PLUS trailing period if it is
   * preceded by its regular expression.
   *
   * @param record a marc4j Record object
     * @param fieldSpec - which marc fields / subfields to use as values
     * @param charsToReplaceRegEx a regular expression of trailing chars to be
     *   replaced (see java Pattern class).  Note that the regular expression
     *   should NOT have '$' at the end.
     *   (e.g. " *[,/;:]" replaces any commas, slashes, semicolons or colons
     *     at the end of the string, and these chars may optionally be preceded
     *     by a space)
     * @param charsB4periodRegEx a regular expression that must immediately
     *  precede a trailing period IN ORDER FOR THE PERIOD TO BE REMOVED.
     *  Note that the regular expression will NOT have the period or '$' at
     *  the end.
     *   (e.g. "[a-zA-Z]{3,}" means at least three letters must immediately
     *   precede the period for it to be removed.)
   * @return Set of strings containing values without trailing characters and
   *         without Lane 655a or 650a nomesh
   */
    public Set<String> getTopicWithoutTrailingPunct(final Record record, final String fieldSpec, String charsToReplaceRegEx, String charsB4periodRegEx)
    {
      Set<String> resultSet = removeTrailingPunct(record, fieldSpec, charsToReplaceRegEx, charsB4periodRegEx);
    if (buildings.contains("LANE-MED"))
      resultSet.removeAll(f655suba);
    resultSet.remove("nomesh");
    return resultSet;
  }

  /**
   * Returns all 651a and the first subfield z in any 6xx field
   * @param record a marc4j Record object
     * @param charsToReplaceRegEx a regular expression of trailing chars to be
     *   replaced (see java Pattern class).  Note that the regular expression
     *   should NOT have '$' at the end.
     *   (e.g. " *[,/;:]" replaces any commas, slashes, semicolons or colons
     *     at the end of the string, and these chars may optionally be preceded
     *     by a space)
     * @param charsB4periodRegEx a regular expression that must immediately
     *  precede a trailing period IN ORDER FOR THE PERIOD TO BE REMOVED.
     *  Note that the regular expression will NOT have the period or '$' at
     *  the end.
     *   (e.g. "[a-zA-Z]{3,}" means at least three letters must immediately
     *   precede the period for it to be removed.)
   *
   * @return Set of strings containing geographic_facet values without trailing chars
   */
    @SuppressWarnings("unchecked")
  public Set<String> getGeographicFacet(final Record record, String charsToReplaceRegEx, String charsB4periodRegEx)
    {
    Set<String> values = MarcUtils.getFieldList(record, "651a");

    // look for first subfield z in 6xx
    List<DataField> dfList = (List<DataField>) record.getDataFields();
    for (DataField df : dfList) {
      if (df.getTag().startsWith("6")) {
        List<String> subList = MarcUtils.getSubfieldStrings(df, 'z');
        if (subList.size() > 0)
          values.add(subList.get(0));
      }
    }

    // remove trailing punctuataion
    Set<String> resultSet = new LinkedHashSet<String>();
    for (String val : values) {
        String result = Utils.removeAllTrailingCharAndPeriod(val, "(" + charsToReplaceRegEx + ")+", charsB4periodRegEx);
      resultSet.add(result);
    }

    return resultSet;
  }


  /**
   * given that there is a Format.DATABASE_A_Z assigned to the record,
   *  look in the 099a for subject codes.  Some subject codes are mapped
   *  to multiple values -- for these we must assign a second code value
   * @param record - marc4j Record object
   * @return Set of strings database A-Z subject codes from 099a
   */
  public Set<String> getDbAZSubjects(final Record record)
  {
    Set<String> subjectsSet = new LinkedHashSet<String>();
    if (main_formats.contains(Format.DATABASE_A_Z.toString())) {
      subjectsSet = MarcUtils.getFieldList(record, "099a");

      // INDEX-14 If there is no 099, set subject to "Uncategorized"
      if (record.getVariableFields("099").isEmpty())
        subjectsSet.add("Uncategorized");
    }

    // add second value for those codes mapping to two values
    if (subjectsSet.contains("BP"))
      subjectsSet.add("BP2");
    if (subjectsSet.contains("BQ"))
      subjectsSet.add("BQ2");

    if (subjectsSet.contains("GF"))
      subjectsSet.add("GF2");

    if (subjectsSet.contains("JK"))
      subjectsSet.add("JK2");
    if (subjectsSet.contains("JX"))
      subjectsSet.add("JX2");

    if (subjectsSet.contains("KJV"))
      subjectsSet.add("KJV2");
    if (subjectsSet.contains("KJW"))
      subjectsSet.add("KJW2");
    if (subjectsSet.contains("KK"))
      subjectsSet.add("KK2");
    if (subjectsSet.contains("KKA"))
      subjectsSet.add("KKA2");
    if (subjectsSet.contains("KKB"))
      subjectsSet.add("KKB2");
    if (subjectsSet.contains("KKC"))
      subjectsSet.add("KKC2");

    if (subjectsSet.contains("PA"))
      subjectsSet.add("PA2");

    return subjectsSet;
  }


// Subject Methods ----------------- End ----------------------- Subject Methods

// Access Methods ----------------- Begin ----------------------- Access Methods

  /**
   * returns the access facet values for a record. A record can have multiple
   * values: online, on campus and upon request are not mutually exclusive.
   * @param record a marc4j Record object
   * @return Set of Strings containing access facet values.
   */
  public Set<String> getAccessMethods(final Record record)
  {
    return accessMethods;
  }

  /**
   * sets the accessMethods for a record.
   * @param record a marc4j Record object
   * @return Set of Strings containing access facet values.
   */
  private void setAccessMethods(final Record record)
  {
    accessMethods.clear();
    for (Item item : itemSet) {
      if (item.isOnline())
        accessMethods.add(Access.ONLINE.toString());
      else
        accessMethods.add(Access.AT_LIBRARY.toString());
    }

    if (fullTextUrls.size() > 0)
      accessMethods.add(Access.ONLINE.toString());
    if (sfxUrls.size() > 0)
      accessMethods.add(Access.ONLINE.toString());
  }

// Access Methods -----------------  End  ----------------------- Access Methods

// URL Methods -------------------- Begin -------------------------- URL Methods

    /**
     * returns a set of strings containing the sfx urls in a record.  Returns
     *   empty set if none.
   * @param record a marc4j Record object
     */
    public Set<String> getSFXUrls(final Record record)
    {
      return sfxUrls;
  }

  /**
   * assign sfxUrls to be strings containing the sfx urls in a record.
   */
  private void setSFXUrls()
  {
    sfxUrls.clear();
    // all 956 subfield u contain fulltext urls that aren't SFX
    for (String url : f956subu) {
      if (isSFXUrl(url))
        sfxUrls.add(url);
    }
  }

  /**
   * returns the URLs for the full text of a resource described by the 856u
   * @param record a marc4j Record object
   */
  public Set<String> getFullTextUrls(final Record record)
  {
    return fullTextUrls;
  }

  /**
   * assign fullTextUrls to be the URLs for the full text of a resource as
   *  described by the 856u
   * @param record a marc4j Record object
   */
  private void setFullTextUrls(final Record record) {
    fullTextUrls.clear();

    // get full text urls from 856, then check for gsb forms
    fullTextUrls = super.getFullTextUrls(record);

    // avoid ConcurrentModificationException  SW-322
    String[] urlArray = new String[fullTextUrls.size()];
    urlArray = fullTextUrls.toArray(urlArray);
    for (int i = 0; i < urlArray.length; i++) {
      String possUrl = urlArray[i];
           if (possUrl.startsWith("http://www.gsb.stanford.edu/jacksonlibrary/services/") ||
                  possUrl.startsWith("https://www.gsb.stanford.edu/jacksonlibrary/services/"))
        fullTextUrls.remove(possUrl);
    }

    // get all 956 subfield u containing fulltext urls that aren't SFX
    for (String url : f956subu) {
      if (!isSFXUrl(url))
        fullTextUrls.add(url);
    }
  }

  /**
   * returns the URLs for restricted full text of a resource described
   *  by the 856u.  Restricted is determined by matching a string against
   *  the 856z.  ("available to stanford-affiliated users at:")
   * @param record a marc4j Record object
   */
  @SuppressWarnings("unchecked")
  public Set<String> getRestrictedUrls(final Record record)
  {
    // get full text urls from 856, then check for restricted access clause
    Set<String> resultSet = new LinkedHashSet<String>();

    Pattern RESTRICTED_PATTERN = Pattern.compile("available to stanford-affiliated users at:", Pattern.CASE_INSENSITIVE);

    List<VariableField> list856 = record.getVariableFields("856");
    for (VariableField vf : list856)
    {
        DataField df = (DataField) vf;
        List<String> subzs = MarcUtils.getSubfieldStrings(df, 'z');
        if (subzs.size() > 0)
        {
          boolean restricted = false;
          for (String subz : subzs)
          {
            Matcher matcher = RESTRICTED_PATTERN.matcher(subz);
            if (matcher.find())
            {
              restricted = true;
              break;
            }
          }

          if (restricted)
          {
            List<String> possUrls = MarcUtils.getSubfieldStrings(df, 'u');
            if (possUrls.size() > 0)
            {
              char ind2 = df.getIndicator2();
              switch (ind2)
              {
                case '0':
                  resultSet.addAll(possUrls);
                  break;
                case '2':
                  break;
                default:
                  if (!MarcUtils.isSupplementalUrl(df))
                    resultSet.addAll(possUrls);
                  break;
              }
            }
          }
        }
      }

    return resultSet;
  }


  private boolean isSFXUrl(String urlStr) {
      if (urlStr.startsWith("http://caslon.stanford.edu:3210/sfxlcl3?") ||
           urlStr.startsWith("http://library.stanford.edu/sfx?") )
      return true;
    else
      return false;
  }

// URL Methods --------------------  End  -------------------------- URL Methods


// Publication Methods  -------------- Begin --------------- Publication Methods

  /**
   * Gets 260ab and 264ab  but ignore s.l in 260a and s.n. in 260b
   * @param record a marc4j Record object
   * @return Set of strings containing values in 260ab and 264ab, without s.l in 260a
   *  and without s.n. in 260b
   */
    @SuppressWarnings("unchecked")
  public Set<String> getPublication(final Record record)
    {
      return PublicationUtils.getPublication(record.getVariableFields(new String[]{"260", "264"}));
  }


    /**
     * gets the value from 008 bytes 7-10 if 008 byte 6 is in byte6vals; null otherwise
   * @param record a marc4j Record object
   * @param byte6vals a String containing the desired values of 008 byte 6
     * @return a four digit year if 008 byte 6 matched and there was a four
     *  digit year in 008 bytes 7-10, null otherwise
     */
    public String get008Date1(final Record record, String byte6vals)
    {
      return PublicationUtils.get008Date1(cf008, byte6vals);
    }

    /**
     * gets the value from 008 bytes 11-14 if 008 byte 6 is in byte6vals; null otherwise,
     *  and null if date is 9999
   * @param record a marc4j Record object
   * @param byte6vals a String containing the desired values of 008 byte 6
     * @return a four digit year if 008 byte 6 matched and there was a four
     *  digit year in 008 bytes 7-10, null otherwise
     */
    public String get008Date2(final Record record, String byte6vals)
    {
      return PublicationUtils.get008Date2(cf008, byte6vals);
    }

    /**
     * gets a value from 008 bytes 11-14 if 008 date1 wasn't already
     * assigned to one of other fields:
     *   publication_year_isi = custom, get008Date1(est)
   *   beginning_year_isi = custom, get008Date1(cdmu)
   *   earliest_year_isi = custom, get008Date1(ik)
   *   earliest_poss_year_isi = custom, get008Date1(q)
   *   release_year_isi = custom, get008Date1(p)
   *   reprint_year_isi = custom, get008Date1(r)
   *   production_year_isi = custom, get008Date2(p)
   *   original_year_isi = custom, get008Date2(r)
   *   copyright_year_isi = custom, get008Date2(t)
   * if no usable value (dddd or dddu) from 008 date 1, look in 260c
   * for usable value
  **/
    public String getOtherYear(final Record record)
    {
      return PublicationUtils.getOtherYear(cf008, id, logger);
    }

    /**
     * returns the a value comprised of 250ab and 260a-g, suitable for display
     *
   * @param record a marc4j Record object
     * @return String
     */
    public String getImprint(final Record record)
    {
    String edition = MarcUtils.getFieldVals(record, "250ab", " ").trim();
    String vernEdition = MarcUtils.getLinkedFieldVals(record, "250ab", " ").trim();
    if (edition != null && edition.length() > 0)
    {
      if (vernEdition != null && vernEdition.length() > 0)
        edition = edition + " " + vernEdition;
    }
    else
      if (vernEdition != null && vernEdition.length() > 0)
        edition = vernEdition;

    String imprint = MarcUtils.getFieldVals(record, "260abcefg", " ").trim();
    String vernImprint = MarcUtils.getLinkedFieldVals(record, "260abcefg", " ").trim();
    if (imprint != null && imprint.length() > 0)
    {
      if (vernImprint != null && vernImprint.length() > 0)
        imprint = imprint + " " + vernImprint;
    }
    else
      if (vernImprint != null && vernImprint.length() > 0)
        imprint = vernImprint;

    if (edition.length() > 0)
      if (imprint.length() > 0)
        return edition + " - " + imprint;
      else
        return edition;
    else
      return imprint;
    }

  /**
   * returns the publication date from a record, if it is present and not
     *  beyond the current year + 1 (and not earlier than 0500 if it is a
     *  4 digit year
     *   four digit years < 0500 trigger an attempt to get a 4 digit date from 260c
     * Side Effects:  errors in pub date are logged
   * @param record a marc4j Record object
   * @return String containing publication date, or null if none
   * @deprecated
   */
  public String getPubDate(final Record record)
  {
    return PublicationUtils.getPubDate(cf008date1, date260c, record.getVariableFields("264"), id, logger);
  }

  /**
   * returns the publication dates from a record, if it is present and not
     *  beyond the current year + 1 (and not earlier than 0500 if it is a
     *  4 digit year
     *   four digit years < 0500 trigger an attempt to get a 4 digit date from 260c
   * @param record a marc4j Record object
   * @return Set of Strings (to be converted to int by Solr) containing publication dates for pub date slider (facet)
   */
  public Set<String> getPubDateSliderVals(final Record record)
  {
    return PublicationUtils.getPubDateSliderVals(cf008, MarcUtils.getFieldList(record, "260c"), id, logger);
  }

  /**
     * returns the sortable publication date from a record, if it is present
     *  and not beyond the current year + 1, and not earlier than 0500 if
     *   a four digit year
     *   four digit years < 0500 trigger an attempt to get a 4 digit date from 260c
     *  NOTE: errors in pub date are not logged;  that is done in getPubDate()
   * @param record a marc4j Record object
   * @return String containing publication date, or null if none
   */
  public String getPubDateSort(final Record record) {
    return PublicationUtils.getPubDateSort(cf008, date260c, record.getVariableFields("264"));
  }

  /**
   * returns the publication date groupings from a record, if pub date is
     *  given and is no later than the current year + 1, and is not earlier
     *  than 0500 if it is a 4 digit year.
     *   four digit years < 0500 trigger an attempt to get a 4 digit date from 260c
     *  NOTE: errors in pub date are not logged;  that is done in getPubDate()
   * @param record a marc4j Record object
   * @return Set of Strings containing the publication date groupings
   *         associated with the publish date
   * @deprecated
   */
  public Set<String> getPubDateGroups(final Record record)
  {
    return PublicationUtils.getPubDateGroups(cf008date1, date260c, record.getVariableFields("264"));
  }

// Pub Date Methods  --------------  End  --------------------- Pub Date Methods

// Date Cataloged Methods -------------- Begin ---------------- Date Cataloged Methods

  /**
   * returns the date cataloged in the format of YYYY-MM-DDT00:00:00Z
   * @param record a marc4j Record object
   * @return String with correct format
   */
  public String getDateCataloged(final Record record)
  {
    String date_cat = null;
    DataField date = (DataField) record.getVariableField("916");
    if (date != null && date.getSubfield('b') != null) {
      String date_str = date.getSubfield('b').getData();
      if (!date_str.equalsIgnoreCase("NEVER")) {
        date_cat = date_str.substring(0, 4) + "-" + date_str.substring(4, 6) + "-" + date_str.substring(6, 8) + "T00:00:00Z";
      }
    }
    return date_cat;
  }

// Date Cataloged Methods --------------- End ----------------- Date Cataloged Methods


// AllFields Methods  --------------- Begin ------------------ AllFields Methods

  /**
   * fields in the 0xx range (not including control fields) that should be
   * indexed in allfields
   */
  Set<String> keepers0xx = new HashSet<String>();
  {
    keepers0xx.add("024");
    keepers0xx.add("027");
    keepers0xx.add("028");
    keepers0xx.add("033");
  }

  /**
   * fields in the 9xx range (not including control fields) that should be
   * indexed in allfields
   */
  Set<String> keepers9xx = new HashSet<String>();
  {
    keepers9xx.add("905");
    keepers9xx.add("908");  // added 2013-02-26 for Business entering Symphony
    keepers9xx.add("920");
    keepers9xx.add("986");
    keepers9xx.add("979");
  }

  /**
   * Returns all subfield contents of all the data fields (non control fields)
   *  between 100 and 899 inclusive, as a single string
   *  plus the "keeper" fields
   * @param record Marc record to extract data from
   */
  @SuppressWarnings("unchecked")
  public String getAllFields(final Record record)
  {
    StringBuilder result = new StringBuilder(5000);
    List<DataField> dataFieldList = record.getDataFields();
    for (DataField df : dataFieldList) {
      String tag = df.getTag();
      if ( (!tag.startsWith("0") || (tag.startsWith("0") && keepers0xx.contains(tag)))
        &&
        (!tag.startsWith("9") || (tag.startsWith("9") && keepers9xx.contains(tag))))
      {
        List<Subfield> subfieldList = df.getSubfields();
        for (Subfield sf : subfieldList) {
          result.append(sf.getData() + " ");
        }
      }
    }
    return result.toString().trim();
  }

  /**
   * Returns all subfield contents of all the data fields (non control fields)
   *  between 100 and 899 inclusive, as a single string
   *  plus the "keeper" fields
   * @param record Marc record to extract data from
   */
  @SuppressWarnings("unchecked")
  public String getAllLinkedSearchableFields(final Record record)
  {
    StringBuilder result = new StringBuilder(5000);
      List<VariableField> linkFldList = MarcUtils.getVariableFields(record, "880");
    for (VariableField vf : linkFldList) {
      if (vf instanceof DataField) {
        List<Subfield> subfieldList = ((DataField) vf).getSubfields();
        for (Subfield sf : subfieldList) {
          if (sf.getCode() !=  '\u0000' && Character.isLetter(sf.getCode())) {
            String d = sf.getData();
            if (d != null && d.length() > 0)
              result.append(sf.getData() + " ");
          }
        }
      }
    }
    return result.toString().trim();
  }

  /**
   * Returns the marc record as an XML String, with the following holdings
   *  related fields removed:
   *   852, 853-5, 863-5, 866-8, 999
   */
  public String bibOnlyXml(final Record record)
  {
    // FIXME:  do I need to do a deep copy first?  Or copy over all fields except these?
    String[] fldsToRemove = {"852", "853", "854", "855", "863", "864", "865", "866", "867", "868", "999"};

    MarcFactory factory = MarcFactory.newInstance();
    Record bibOnly = factory.newRecord(record.getLeader());
    for (VariableField vf : record.getVariableFields())
    {
      if (!Arrays.asList(fldsToRemove).contains(vf.getTag()))
        bibOnly.addVariableField(vf);
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    MarcWriter writer = new MarcXmlWriter(baos, "UTF-8", false);
    writer.write(bibOnly);
    writer.close();
    try
    {
      return baos.toString("UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      logger.error(e.getCause());
    }
    // very sad default, but perhaps more informative than "" or empty xml
    return record.toString();
  }


// AllFields Methods  ---------------  End  ------------------ AllFields Methods


// Item Related Methods ------------- Begin --------------- Item Related Methods

  /**
   * get buildings holding a copy of this resource
   * @param record a marc4j Record object
   */
  public Set<String> getBuildings(final Record record) {
    return buildings;
  }

  /**
   * set buildings from the 999 subfield m
   * @param record a marc4j Record object
   */
  private void setBuildings(final Record record)
  {
    buildings.clear();
    for (Item item : itemSet) {
      String buildingStr = item.getLibrary();
      if (buildingStr.length() > 0) {
        buildings.add(buildingStr);
        // https://github.com/sul-dlss/solrmarc-sw/issues/101
        // Per Peter Blank - items with library = SAL3 and location = PAGE-AR
        // should be given two library facet values:
        // SAL3 (off-campus storage) <- they are currently getting this
        // and Art & Architecture (Bowes) <- new requirement
        String type = item.getType();
        if (buildingStr.equals("SAL3") && type.equals("PAGE_AR")) {
          buildings.add("ART");
        }
      }
    }
  }

  /**
   * @return the barcode for the item to be used as the default choice for
   *  nearby-on-shelf display (i.e. when no particular item is selected by
   *  the user).  The current algorithm is:
   *  1. If Green item(s) have shelfkey, do this:
   *  - pick the LC truncated callnum with the most items
   *  - pick the shortest LC untruncated callnum if no truncation
   *  - if no LC, got through callnum scheme order of preference: LC, Dewey, Sudoc, Alphanum (without box and folder)
   *  2. If no Green shelfkey, use the above algorithm for libraries (can use raw codes) in alpha order until you get a shelfkey
   * @param record a marc4j Record object
   */
  public String getPreferredItemBarcode(final Record record)
  {
    String barcode = ItemUtils.getPreferredItemBarcode(itemSet, isSerial);
    if (barcode == null || barcode.length() == 0) {
      for (Item item : itemSet) {
        if ( ( item.isOnline() || item.hasIgnoredCallnum() )
            && item.hasSeparateBrowseCallnum()) {
          String skey = item.getShelfkey(isSerial);
          if (skey != null && skey.length() > 0)
            return item.getBarcode();
          }
        }
    }
    return barcode;
  }

  /**
   * for search results and record view displays:
   * @return set of fields containing individual item information
   *  (callnums, lib, location, status ...)
   * @param record a marc4j Record object
   */
  public Set<String> getItemDisplay(final Record record)
  {
    Set<String> result = new LinkedHashSet<String>();

    // if there are no 999s, then it's on order
    if (!has999s) {
      String sep = ItemUtils.SEP;
      result.add( "" + sep +  // barcode
                  "" + sep +   // library
                  "ON-ORDER" + sep +  // home loc
                  "ON-ORDER" + sep +  // current loc
                  "" + sep +  // item type
                  "" + sep +   // lopped Callnum
                  "" + sep +   // shelfkey
                  "" + sep +   // reverse shelfkey
                  "" + sep +   // fullCallnum
                  "");   // volSort
    }
    else result.addAll(ItemUtils.getItemDisplay(itemSet, isSerial, id));

    return result;
  }

// Item Related Methods -------------  End  --------------- Item Related Methods

// Mhld Methods ---------------------- Begin ---------------------- Mhld Methods

  /**
   * for search results and record view displays:
   * @return set of fields containing summary holdings information
   *  (lib, location, holdings, latest received ...)
   * @param record a marc4j Record object
   */
  public Set<String> getMhldDisplay(final Record record)
  {
    MhldDisplayUtil mhldDisplayUtil = new MhldDisplayUtil(record, id);
    return mhldDisplayUtil.getMhldDisplayValues();
  }

// Mhld Methods ---------------------- End ------------------------ Mhld Methods

// Call Number Methods -------------- Begin ---------------- Call Number Methods

  /**
   * Get our local call numbers from subfield a in 999. Does not get call
   * number when item or callnum should be ignored, or for online items.
   * @param record a marc4j Record object
   */
  public Set<String> getLocalCallNums(final Record record)
  {
    Set<String> result = new HashSet<String>();
    for (Item item : itemSet) {
      if (!item.hasShelbyLoc()
          && !item.hasIgnoredCallnum()
          && !item.hasBadLcLaneCallnum()) {
        String callnum = item.getCallnum();
        if (callnum.length() > 0)
          result.add(callnum);
      }
    }
    return result;
  }

  /**
   * Get hierarchical values for call number facet
   *  for LC:
   *    "LC Classification|(first class letter trans)|(class letter trans)"
   *      e.g. "LC Classification|M - Music|ML - Music Literature"
   *      e.g. "LC Classification|M - Music|M - Music"
   *  for Dewey:
   *    "Dewey Classification|(first digit trans)|(first 2 digits trans)"
   *      e.g. "Dewey Classification|200s - Religion|210s - Natural Theology"
   *      e.g. "Dewey Classification|200s - Religion|200s - Religion"
   *  for gov docs:
   *    "Government Document|(type)"
   *      e.g. "Government Document|Federal"
   *  other types of call numbers currently ignored
   *
   * note that the separator is passed in
   *
   *  it is expected that these values will go to a field analyzed with
   *   solr.PathHierarchyTokenizerFactory  so a value like
   *    "LC Classification|M - Music|ML - Music Literature"
   *  will be indexed as 3 values:
   *    "LC Classification|M - Music|ML - Music Literature"
   *    "LC Classification|M - Music"
   *    "LC Classification"
   * @param record a marc4j Record object
   * @param separator the char(s) to use as a separator between levels of the hierarchy within a value
   * @param mapName the name of the translation map for the call number values
   */
  public Set<String> getCallNumHierarchVals(final Record record, String separator, String mapName)
  {
    try
    {
      loadTranslationMap(null, mapName + ".properties");
    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }
    Set<String> result = new HashSet<String>();
    for (String callnum : lcCallnums)
    {
      String firstLet = callnum.substring(0, 1).toUpperCase();
      String letters = org.solrmarc.tools.CallNumUtils.getLCstartLetters(callnum);
      if (firstLet != null && mapName != null && findTranslationMap(mapName) != null)
      {
        firstLet = Utils.remap(firstLet, findTranslationMap(mapName), true);
        letters = Utils.remap(letters, findTranslationMap(mapName), true);
      }
      result.add(CallNumUtils.LC_TOP_FACET_VAL + separator + firstLet + separator + letters);
    }

    for (String govDocCat : govDocCats)
    {
      result.add(CallNumUtils.GOV_DOC_TOP_FACET_VAL + separator + govDocCat);
    }

    for (String callnum : deweyCallnums)
    {
      String firstDigit = callnum.substring(0, 1) + "00s";
      String twoDigits = callnum.substring(0, 2) + "0s";
      if (mapName != null && findTranslationMap(mapName) != null)
       {
        firstDigit = Utils.remap(firstDigit, findTranslationMap(mapName), true);
        twoDigits = Utils.remap(twoDigits, findTranslationMap(mapName), true);
       }
      result.add(CallNumUtils.DEWEY_TOP_FACET_VAL + separator + firstDigit + separator + twoDigits);
    }

    return result;
  }

  /**
   * Assign type of government doc based on:
   *   callnumber scheme of SUDOC
   *   location in 999
   *   presence of 086 field (use all 999s that aren't to be skipped)
   *   additional criteria for type https://jirasul.stanford.edu/jira/browse/INDEX-140
   * @param record a marc4j Record object
   */
  private void setGovDocCats(final Record record)
  {
    govDocCats.clear();

    boolean has086 = !record.getVariableFields("086").isEmpty();

    for (Item item : itemSet) {
      if (item.hasGovDocLoc() || has086
        || item.getCallnumType() == CallNumberType.SUDOC) {
        String rawLoc = CallNumUtils.getGovDocTypeFromLocCode(item.getHomeLoc());
        if (rawLoc == "Other") {
          if (has086) {
            List<VariableField> list086 = record.getVariableFields("086");
            for (VariableField vf : list086)
            {
              DataField df = (DataField) vf;
              String sub2 = MarcUtils.getSubfieldData(df, '2');
              if (sub2 == "cadocs") {
                govDocCats.add("California");
              } else if (sub2 == "sudocs") {
                govDocCats.add("Federal");
              } else if (sub2 == "undocs") {
                govDocCats.add("International");
              } else if (df.getIndicator1() == '0') {
                  govDocCats.add("Federal");
              } else {
                govDocCats.add(rawLoc);
              }
            }
          } else {
            govDocCats.add(rawLoc);
          }
        } else {
          govDocCats.add(rawLoc);
        }
      }
    }
  }

  /**
   * Get shelfkey versions of "lopped" call numbers (call numbers without
   * volume info).  Can access shelfkeys in lexigraphical order for browsing
   * @param record a marc4j Record object
   */
  public Set<String> getShelfkeys(final Record record) {
    if (shelfkeys == null || shelfkeys.size() == 0)
      setShelfkeys(record);
    return shelfkeys;
  }

  /**
   * Assign shelfkeys to sortable versions of "lopped" call numbers (call
   * numbers without volume info)
   * @param record a marc4j Record object
   */
  private void setShelfkeys(final Record record)
  {
    shelfkeys.clear();
    shelfkeys.addAll(CallNumUtils.getShelfkeys(itemSet, id, isSerial));
  }

  /**
   * Get reverse shelfkey versions of "lopped" call numbers (call numbers
   * without volume info). Can access in lexigraphical order for browsing
   * (used to get previous callnums ...)
   * @param record a marc4j Record object
   */
  public Set<String> getReverseShelfkeys(final Record record)
  {
    return CallNumUtils.getReverseShelfkeys(itemSet, isSerial);
  }

// Call Number Methods -------------- End ---------------- Call Number Methods

/**
 * returns the digital bookplates data from the 979
 * @param record a marc4j Record object
 */
public Set<String> getBookplatesDisplay(final Record record)
{
  return bookplatesDisplay;
}

/**
* set the digital bookplates data from the 979
* @param record a marc4j Record object
*/
private void setBookplatesDisplay(final Record record) {
  String sep = ItemUtils.SEP;

  List<VariableField> list979 = record.getVariableFields("979");
  for (VariableField vf : list979)
  {
    DataField df = (DataField) vf;
    String subC = MarcUtils.getSubfieldData(df, 'c');
    if (!subC.equalsIgnoreCase("no content metadata")) {
      String[] druid = MarcUtils.getSubfieldData(df, 'b').split(":");
      bookplatesDisplay.add( MarcUtils.getSubfieldData(df, 'f') + sep +
                             druid[1] + sep +
                             subC + sep +
                             MarcUtils.getSubfieldData(df, 'd'));
    }
  }
}

/**
 * returns the digital bookplates fund facet value from the 979
 * @param record a marc4j Record object
 */
 public Set<String> getFundFacet(final Record record)
 {
  return fundFacet;
 }

/**
* set the digital bookplates fund facet value from the 979
* @param record a marc4j Record object
*/
private void setFundFacet(final Record record) {

  List<VariableField> list979 = record.getVariableFields("979");
  for (VariableField vf : list979)
  {
    DataField df = (DataField) vf;
    String subC = MarcUtils.getSubfieldData(df, 'c');
    if (!subC.equalsIgnoreCase("no content metadata")) {
      String[] druid = MarcUtils.getSubfieldData(df, 'b').split(":");
      fundFacet.add(druid[1]);
    }
  }
}

/**
 * returns locations based upon the data in the 852 subfield c or 999 subfield t
 * @param record a marc4j Record object
 */
 public Set<String> getLocationFacet(final Record record)
 {
  return locationFacet;
 }

/**
* set locations based upon the value in the 852 subfield c or 999 subfield t
* "Art Locked Stacks"
* @param record a marc4j Record object
*/
private void setLocationFacet(final Record record) {

  for (Item item : itemSet) {
    if (item.hasArtLockedLoc()) {
      // TODO: if we get more values for locationFacet, make it an enum like Genre
      locationFacet.add("Art Locked Stacks");
    }
  }

  List<VariableField> list852 = record.getVariableFields("852");
  for (VariableField vf : list852)
  {
    DataField df = (DataField) vf;
    String subC = MarcUtils.getSubfieldData(df, 'c');
    if (ART_LOCKED_LOCS.contains(subC)){
      // TODO: if we get more values for locationFacet, make it an enum like Genre
      locationFacet.add("Art Locked Stacks");
    }
  }
}


// Vernacular Methods --------------- Begin ----------------- Vernacular Methods

  /**
   * Get the vernacular (880) field based which corresponds to the fieldSpec
   * in the subfield 6 linkage, handling multiple occurrences as indicated
   * @param record a marc4j Record object
     * @param fieldSpec - which marc fields / subfields need to be sought in
     *  880 fields (via linkages)
     * @param multOccurs - "first", "join" or "all" indicating how to handle
     *  multiple occurrences of field values
   */
  public final Set<String> getVernacular(final Record record, String fieldSpec, String multOccurs)
  {
    Set<String> result = MarcUtils.getLinkedField(record, fieldSpec);

    if (multOccurs.equals("first")) {
      Set<String> first = new HashSet<String>();
      for (String r : result) {
        first.add(r);
        return first;
      }
    } else if (multOccurs.equals("join")) {
      StringBuilder resultBuf = new StringBuilder();
      for (String r : result) {
        if (resultBuf.length() > 0)
          resultBuf.append(' ');
        resultBuf.append(r);
      }
      Set<String> resultAsSet = new LinkedHashSet<String>();
      resultAsSet.add(resultBuf.toString());
      return resultAsSet;
    }
    // "all" is default

    return result;
  }

  /**
   *
   * For each occurrence of a marc field in the fieldSpec list, get the
     * matching vernacular (880) field (per subfield 6) and extract the
     * contents of all subfields except the ones specified, concatenate the
     * subfield contents with a space separator and add the string to the result
     * set.
     * @param record - the marc record
     * @param fieldSpec - the marc fields (e.g. 600:655) for which we will grab
     *  the corresponding 880 field containing subfields other than the ones
     *  indicated.
     * @return a set of strings, where each string is the concatenated values
     *  of all the alphabetic subfields in the 880s except those specified.
   */
  @SuppressWarnings("unchecked")
  public final Set<String> getVernAllAlphaExcept(final Record record, String fieldSpec)
  {
    Set<String> resultSet = new LinkedHashSet<String>();

    String[] fldTags = fieldSpec.split(":");
    for (int i = 0; i < fldTags.length; i++)
    {
      String fldTag = fldTags[i].substring(0, 3);
      if (fldTag.length() < 3 || Integer.parseInt(fldTag) < 10)
      {
        System.err.println("Invalid marc field specified for getAllAlphaExcept: " + fldTag);
        continue;
      }

      String tabooSubfldTags = fldTags[i].substring(3);

      Set<VariableField> vernFlds = MarcUtils.getVernacularFields(record, fldTag);

      for (VariableField vf : vernFlds)
      {
        StringBuilder buffer = new StringBuilder(500);
        DataField df = (DataField) vf;
        if (df != null)
        {
          List<Subfield> subfields = df.getSubfields();
          for (Subfield sf : subfields)
          {
            if (Character.isLetter(sf.getCode())
                && tabooSubfldTags.indexOf(sf.getCode()) == -1)
            {
              if (buffer.length() > 0)
                buffer.append(' ' + sf.getData());
              else
                buffer.append(sf.getData());
            }
          }
          if (buffer.length() > 0)
            resultSet.add(buffer.toString());
        }
      }
    }

    return resultSet;
  }

  /**
   * Get the vernacular (880) field based which corresponds to the fieldSpec
   * in the subfield 6 linkage, handling trailing punctuation as incidated
   * @param record a marc4j Record object
     * @param fieldSpec - which marc fields / subfields need to be sought in
     *  880 fields (via linkages)
     * @param charsToReplaceRegEx a regular expression of trailing chars to be
     *   replaced (see java Pattern class).  Note that the regular expression
     *   should NOT have '$' at the end.
     *   (e.g. " *[,/;:]" replaces any commas, slashes, semicolons or colons
     *     at the end of the string, and these chars may optionally be preceded
     *     by a space)
     * @param charsB4periodRegEx a regular expression that must immediately
     *  precede a trailing period IN ORDER FOR THE PERIOD TO BE REMOVED.
     *  Note that the regular expression will NOT have the period or '$' at
     *  the end.
     *   (e.g. "[a-zA-Z]{3,}" means at least three letters must immediately
     *   precede the period for it to be removed.)
   */
  public final Set<String> vernRemoveTrailingPunc(final Record record, String fieldSpec, String charsToReplaceRegEx, String charsB4periodRegEx)
  {
    Set<String> origVals = MarcUtils.getLinkedField(record, fieldSpec);
    Set<String> result = new LinkedHashSet<String>();

    for (String val : origVals) {
      result.add(Utils.removeAllTrailingCharAndPeriod(val,
          "(" + charsToReplaceRegEx + ")+", charsB4periodRegEx));
    }
    return result;
  }

// Vernacular Methods ---------------  End  ----------------- Vernacular Methods

// Digital Objects Methods ---------------  Begin  ----------------- Digital Objects Methods

  /**
   * returns the PURLs that are managed through StanfordSync
   * @param record a marc4j Record object
   */
  public Set<String> getManagedPurls(final Record record)
  {
    return managedPurls;
  }

  /**
   * assign managedPurls to be the PURLs for objects that have been digitized and are
   *   managed through StanfordSync
   * @param all 856 subfield u
   */
  private void setManagedPurls(List<String> subus) {
    managedPurls.addAll(subus);
  }

  /**
   * returns the display_type that is passed through StanfordSync 856 subfield x #3
   * @param record a marc4j Record object
   */
  public Set<String> getDisplayType(final Record record)
  {
    return displayType;
  }

  /**
   * assign display_type from the third 856 subfield x in 856s
   *   managed through StanfordSync
   * @param record a marc4j Record object
   */
  private void setDisplayType(String display) {
    if (display.toLowerCase().contains("image") || display.toLowerCase().contains("map") || display.toLowerCase().contains("manuscript")) {
      displayType.add("image");
    } else {
      displayType.add("file");
    }
  }

  /**
   * returns the file Ids that is passed through StanfordSync 856 subfield x #5
   * @param record a marc4j Record object
   */
  public Set<String> getFileId(final Record record)
  {
    return fileId;
  }

  /**
   * assign file_id from the fifth 856 subfield x in 856s
   *   managed through StanfordSync
   * @param record a marc4j Record object
   */
  private void setFileId(String file) {
    fileId.add(file);
  }

  /**
   * returns the collection druids for items that are managed through StanfordSync
   *   856 subfield x #6..n
   * @param record a marc4j Record object
   */
  public Set<String> getCollectionDruids(final Record record)
  {
    return collectionDruids;
  }

  /**
   * assign collection druids from the items managed through StanfordSync
   *   856 subfield #6..n
   * @param collection druid
   */
  private void setCollectionDruids(String coll_druid) {
    collectionDruids.add(coll_druid);
  }

  /**
   * returns collection druids and titles from the items managed through StanfordSync
   *   856 subfield #4..n
   * @param record a marc4j Record object
   */
  public Set<String> getCollectionsWithTitles(final Record record)
  {
    return collectionsWithTitles;
  }

  /**
   * assigns collection druids and titles from the items managed through StanfordSync
   *   856 subfield #4..n
   * @param record a marc4j Record object
   */
  private void setCollectionsWithTitles(String coll_with_title) {
    collectionsWithTitles.add(coll_with_title);
  }

  /**
   * returns the collection_type that is set to "Digital Collection" if managed 856 from
   *   StanfordSync is a collection object
   * @param record a marc4j Record object
   */
  public String getCollectionType(final Record record)
  {
    return collectionType;
  }

  /**
   * assign collectionType if object that has an 856 managed through StanfordSync
   *  if a collection object
   */
  private void setCollectionType() {
    collectionType = "Digital Collection";
  }

  /**
   * process 856s managed through StanfordSync
   * @param record a marc4j Record object
   */
  private void processManaged856s(final Record record) {

    Pattern MANAGED_PATTERN = Pattern.compile("SDR-PURL");

    List<VariableField> list856 = record.getVariableFields("856");
    for (VariableField vf : list856)
    {
      DataField df = (DataField) vf;
      List<String> subxs = MarcUtils.getSubfieldStrings(df, 'x');
      if (subxs.size() > 0)
      {
        Matcher matcher = MANAGED_PATTERN.matcher(subxs.get(0));
        if (matcher.find())
        {
          setManagedPurls(MarcUtils.getSubfieldStrings(df, 'u'));
          buildings.add("SDR");
          accessMethods.add(Access.ONLINE.toString());
          if (subxs.get(1).equalsIgnoreCase("item")) {
            for(int i=3; i<subxs.size(); i++){
              String[] coll_split = subxs.get(i).split(":");
              if (coll_split[0].equalsIgnoreCase("file")) {
                setFileId(coll_split[1]);
              } else if (coll_split[0].equalsIgnoreCase("collection")) {
                if (coll_split[2].length() > 2) {
                  setCollectionsWithTitles(coll_split[2] + "-|-" + coll_split[3]);
                  setCollectionDruids(coll_split[2]);
                } else {
                  setCollectionsWithTitles(coll_split[1] + "-|-" + coll_split[3]);
                  setCollectionDruids(coll_split[1]);
                }
              }
            }
          } else if (subxs.get(1).equalsIgnoreCase("collection")) {
            setCollectionType();
          }
          setDisplayType(subxs.get(2));
        }
      }
    }
  }

  /**
   * If an 856 has purl.stanford.edu, add SDR to building facet
   * @param record a marc4j Record object
   */
  private void addSDRfrom856s(final Record record) {

    Pattern MANAGED_PATTERN = Pattern.compile("purl.stanford.edu");

    List<VariableField> list856 = record.getVariableFields("856");
    for (VariableField vf : list856)
    {
      DataField df = (DataField) vf;
      List<String> subus = MarcUtils.getSubfieldStrings(df, 'u');
      if (subus.size() > 0)
      {
        for (String subu : subus) {
          Matcher matcher = MANAGED_PATTERN.matcher(subu);
          if (matcher.find())
          {
            buildings.add("SDR");
          }
        }
      }
    }
  }

// Digital Objects Methods ---------------  End  ----------------- Digital Objects Methods

// Generic Methods ---------------- Begin ---------------------- Generic Methods

  /**
   * Removes trailing characters indicated in regular expression, PLUS
   * trailing period if it is preceded by its regular expression.
   *
   * @param record a marc4j Record object
     * @param fieldSpec - which marc fields / subfields to use as values
     * @param charsToReplaceRegEx a regular expression of trailing chars to be
     *   replaced (see java Pattern class).  Note that the regular expression
     *   should NOT have '$' at the end.
     *   (e.g. " *[,/;:]" replaces any commas, slashes, semicolons or colons
     *     at the end of the string, and these chars may optionally be preceded
     *     by a space)
     * @param charsB4periodRegEx a regular expression that must immediately
     *  precede a trailing period IN ORDER FOR THE PERIOD TO BE REMOVED.
     *  Note that the regular expression will NOT have the period or '$' at
     *  the end.
     *   (e.g. "[a-zA-Z]{3,}" means at least three letters must immediately
     *   precede the period for it to be removed.)
   *
   * @return Set of strings containing values without trailing characters
   */
    public Set<String> removeTrailingPunct(final Record record, final String fieldSpec, String charsToReplaceRegEx, String charsB4periodRegEx)
    {
    Set<String> resultSet = new LinkedHashSet<String>();
    for (String val : MarcUtils.getFieldList(record, fieldSpec)) {
        String result = Utils.removeAllTrailingCharAndPeriod(val, "(" + charsToReplaceRegEx + ")+", charsB4periodRegEx);
      resultSet.add(result);
    }

    return resultSet;
  }

// Generic Methods ------------------ End ---------------------- Generic Methods

}
