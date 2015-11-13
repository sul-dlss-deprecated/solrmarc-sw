package edu.stanford;

import java.util.*;

import org.solrmarc.tools.Utils;

import edu.stanford.enumValues.CallNumberType;

/**
 * Utility methods for item information Stanford SolrMarc
 *
 * @author Naomi Dushay
 */
public class ItemUtils {

  /** separator used in item_display field */
  public static final String SEP = " -|- ";

  /**
   * Default Constructor: private, so it can't be instantiated by other objects
   */
  private ItemUtils(){ }


  /**
   * lop call numbers in Item objects if there is more than one Item with
   *  the same library-translated home loc-scheme combination; otherwise
   *  don't lop the call numbers.  (Don't lop skipped callnumbers)
   * SIDE EFFECT: changes state of passed Item objects to reflect lopping as indicated
   *
   * @param itemSet - set of Item objects that does NOT include any items to be skipped
   * @param locationMap - mapping from raw locations to translated location
   * @param isSerial - true if the record is a serial, false otherwise
   */
  static void lopItemCallnums(Set<Item> itemSet, Map<String,String> locationMap, boolean isSerial)
  {
    if (itemSet.size() == 0)
      return;
    if (itemSet.size() == 1)
    {
      Item[] array = new Item[1];
      Item item = itemSet.toArray(array)[0];
      item.setLoppedCallnum(item.getCallnum());
    }
    else
    {
      // set up data structure grouping items by lib/loc/callnum scheme
      Map<String, Set<Item>> libLocScheme2Items = new HashMap<String, Set<Item>>();
      for (Item item : itemSet)
      {
        if (item.hasIgnoredCallnum())
          continue;
        String library = item.getLibrary();
        String homeLoc = item.getHomeLoc();
        String translatedHomeLoc = Utils.remap(homeLoc, locationMap, true);
        String callnumTypePrefix = item.getCallnumType().getPrefix();

        String key = library + ":" + translatedHomeLoc + ":" + callnumTypePrefix;

        Set<Item> currVal = libLocScheme2Items.get(key);
        if (currVal == null)
          currVal = new HashSet<Item>();
        currVal.add(item);
        libLocScheme2Items.put(key, currVal);
      }

      // process Item objects as necessary
      for (String key : libLocScheme2Items.keySet())
      {
        Set<Item> items = libLocScheme2Items.get(key);
        Set<String> loppedCallnums = new HashSet<String>(items.size());

        if (items.size() == 1) {
          // single items are not lopped
          Item[] array = new Item[1];
          Item item = items.toArray(array)[0];
          item.setLoppedCallnum(item.getCallnum());
        }
        else if (!key.contains(":" + CallNumberType.LC.getPrefix()) &&
            !key.contains(":" + CallNumberType.DEWEY.getPrefix()) ) {
          // non-LC, non-Dewey call numbers are lopped longest common
          //  prefix
          String lopped = CallNumUtils.setLopped2LongestComnPfx(items, 4);
          loppedCallnums.add(lopped);
          ensureCorrectEllipsis(loppedCallnums, items);
        }
        else
        {
          for (Item item : items)
          {
            String fullCallnum = item.getCallnum();
            String lopped = edu.stanford.CallNumUtils.getLoppedCallnum(fullCallnum, item.getCallnumType(), isSerial);
            if (!lopped.equals(fullCallnum))
            {
              item.setLoppedCallnum(lopped);
              loppedCallnums.add(lopped);
            }
          }
          ensureCorrectEllipsis(loppedCallnums, items);
        }
      }
    }
  }


  /**
   * ensure we add ellipsis to item's lopped call number when
   * when there is a lopped call number in the set of items that is the
   * same as a full call number of one of the items
   * SIDE EFFECT:  may change lopped callnums of item objects
   */
  private static void ensureCorrectEllipsis(Set<String> loppedCallnums, Set<Item> items)
  {
    if (loppedCallnums.size() > 0)
    {
      for (Item item : items)
      {
        String fullCallnum = item.getCallnum();
        if (loppedCallnums.contains(fullCallnum))
          item.setLoppedCallnum(fullCallnum + " ...");
      }
    }
  }


  /**
   * preferred item algorithm, per INDEX-153:
   * 1. If Green item(s) have shelfkey, do this:
   * - pick the LC truncated callnum with the most items
   * - pick the shortest LC untruncated callnum if no truncation
   * - if no LC, got through callnum scheme order of preference:  LC, Dewey, Sudoc, Alphanum (without box and folder)
   * 2. If no Green shelfkey, use the above algorithm libraries (raw codes in 999) in alpha order.
   *
   * @param itemSet - the set of items from which selection will be made
   * @param isSerial - true if the record is a serial, false otherwise
   * @return the barcode of the item with the preferred callnumber
   */
  static String getPreferredItemBarcode(Set<Item> itemSet, boolean isSerial)
  {
    boolean haveGreen = false;  // set to true if we have items from Green

    // set up data structure counting num items by lib/callnum scheme/lopped Callnum
    Set<LibSchemeCallNum> libSchemeCallNumSet = new HashSet<LibSchemeCallNum>();
    for (Item item : itemSet)
    {
      if (item.getShelfkey(isSerial) != null && !item.hasBadLcLaneCallnum())
      {
        String itemLib = item.getLibrary();
        if (itemLib.equals("GREEN"))
          haveGreen = true;

        // if we have Green, we can ignore all items not in Green
        if (haveGreen && !itemLib.equals("GREEN"))
          continue;

        // add item to libSchemeCallNumSet
        boolean foundMatch = false;
        for (LibSchemeCallNum setObj : libSchemeCallNumSet)
        {
          if (setObj.itemMatches(item, isSerial))
          {
            foundMatch = true;
            setObj.addItem(item, isSerial);
            break;
          }
        }
        if (!foundMatch)
        {
          LibSchemeCallNum newObj = new LibSchemeCallNum(item, isSerial);
          libSchemeCallNumSet.add(newObj);
        }
      }
    }

    // choose library:  prefer Green, then first alphabetically
    String chosenLib = "ZZZZZ";
    if (haveGreen)
      chosenLib = "GREEN";
    else
    {
      for (Iterator iter = libSchemeCallNumSet.iterator(); iter.hasNext();)
      {
        LibSchemeCallNum setObj = (LibSchemeCallNum) iter.next();
        String setObjLib = setObj.lib;
        if (chosenLib.compareTo(setObjLib) > 0)
          chosenLib = setObjLib;
        else if (chosenLib.compareTo(setObjLib) < 0)
          // won't need this object if it is doomed to be from unchosen library
          iter.remove();
      }
    }

    // pick callnum scheme: LC, Dewey, Sudoc, Alphanum (without box and folder)
    CallNumberType chosenScheme = CallNumberType.OTHER;
    for (Iterator iter = libSchemeCallNumSet.iterator(); iter.hasNext();)
    {
      LibSchemeCallNum setObj = (LibSchemeCallNum) iter.next();
      // drop all set members not matching chosen library
      if (!chosenLib.equals(setObj.lib))
      {
        iter.remove();
        continue;
      }

      CallNumberType scheme = setObj.scheme;
      if (scheme.equals(CallNumberType.LC))
      {
        chosenScheme = scheme;
        break;
      }
      else if (scheme.equals(CallNumberType.DEWEY) &&
          !chosenScheme.equals(CallNumberType.LC))
      {
        // we have a Dewey callnum and no LC yet
        if (!chosenScheme.equals(CallNumberType.DEWEY))
          chosenScheme = scheme;
      }
      else if (scheme.equals(CallNumberType.SUDOC) &&
          !chosenScheme.equals(CallNumberType.LC) &&
          !chosenScheme.equals(CallNumberType.DEWEY))
      {
        // we have a Sudoc callnum and no LC or Dewey yet
        if (!chosenScheme.equals(CallNumberType.SUDOC))
          chosenScheme = scheme;
      }
      else if (scheme.equals(CallNumberType.ALPHANUM) &&
          !chosenScheme.equals(CallNumberType.LC) &&
          !chosenScheme.equals(CallNumberType.DEWEY) &&
          !chosenScheme.equals(CallNumberType.SUDOC))
      {
        // we have an alphanum callnum and no LC or Dewey or Sudoc yet
        if (!chosenScheme.equals(CallNumberType.ALPHANUM))
          chosenScheme = scheme;
      }
    }

    // pick most items
    int mostItems = 1;
    for (Iterator iter = libSchemeCallNumSet.iterator(); iter.hasNext();)
    {
      LibSchemeCallNum setObj = (LibSchemeCallNum) iter.next();
      // drop all set members not matching chosen scheme
      if (!chosenScheme.equals(setObj.scheme))
      {
        iter.remove();
        continue;
      }

      int numItems = setObj.numItems;
      if (numItems > mostItems)
        mostItems = numItems;
      else if (numItems < mostItems)
        // won't need this object
        iter.remove();
    }

    // pick shortest (lopped) callnum if it's a tie
    int shortestLoppedCallnumLen = 999999;
    for (Iterator iter = libSchemeCallNumSet.iterator(); iter.hasNext();)
    {
      LibSchemeCallNum setObj = (LibSchemeCallNum) iter.next();
      // drop all set members with too few items
      if (mostItems > (setObj.numItems))
      {
        iter.remove();
        continue;
      }

      int loppedCallnumLen = setObj.loppedCallnum.length();
      if (loppedCallnumLen < shortestLoppedCallnumLen)
        shortestLoppedCallnumLen = loppedCallnumLen;
      else if (loppedCallnumLen > shortestLoppedCallnumLen)
        // won't need this object
        iter.remove();
    }

    String preferredBarcode = null;
    for (Iterator iter = libSchemeCallNumSet.iterator(); iter.hasNext();)
    {
      LibSchemeCallNum setObj = (LibSchemeCallNum) iter.next();
      // drop all set members with callnum too long
      if (shortestLoppedCallnumLen < (setObj.loppedCallnum.length()))
      {
        iter.remove();
        continue;
      }

      Map<String,String> objCallnum2barcode = setObj.callnum2barcode;
      Set<String> rawCallnums = objCallnum2barcode.keySet();
      preferredBarcode = objCallnum2barcode.get(rawCallnums.toArray()[0]);
    }

    return preferredBarcode;
  }

  /**
   * helper class to compute the preferred item and its barcode
   * @author ndushay
   */
  static class LibSchemeCallNum
  {
    String lib;
    CallNumberType scheme;
    String loppedCallnum;
    int numItems = 0;
    /** key: full raw callnum;  value: barcode */
    Map callnum2barcode = new HashMap();

    LibSchemeCallNum(Item item, boolean isSerial)
    {
      lib = item.getLibrary();
      scheme = item.getCallnumType();
      loppedCallnum = item.getBrowseCallnum(isSerial);
      numItems = 1;
      callnum2barcode.put(item.getCallnumVolSort(isSerial), item.getBarcode());
    }

    void addItem(Item item, boolean isSerial)
    {
      if (itemMatches(item, isSerial))
      {
        numItems++;
        callnum2barcode.put(item.getCallnumVolSort(isSerial), item.getBarcode());
      }
    }

    boolean itemMatches(Item item, boolean isSerial)
    {
      return lib.equals(item.getLibrary()) &&
          scheme.equals(item.getCallnumType()) &&
          loppedCallnum.equals(item.getBrowseCallnum(isSerial));
    }
  }


  /**
   * given a set of non-skipped Item objects, return a set of item_display field values
   * INDEX-132 For SW redesign Summer 2014, add 999|o (public note) and 999|w (call number type)
   * to item_display
   * @param itemSet - set of Item objects
   * @param isSerial - true if the record is a serial, false otherwise
   * @param id - record id, used for error messages
   * @return set of fields from non-skipped items:
   *   barcode + SEP +
   *   library + SEP +
   *   home location + SEP +
   *   current location + SEP +
   *   item type + SEP +
   *   loppedCallnum + SEP +
   *   shelfkey + SEP +
   *   reversekey + SEP +
   *   fullCallnum + SEP +
   *   volSort + SEP +
   *   publicNote + SEP +
   *   callnumType
   */
  static Set<String> getItemDisplay(Set<Item> itemSet, boolean isSerial, String id)
  {
    Set<String> result = new LinkedHashSet<String>();

    // itemSet contains all non-skipped items
    for (Item item : itemSet)
    {
      String homeLoc = item.getHomeLoc();

      // full call number & lopped call number
      String fullCallnum = item.getCallnum();
      String loppedCallnum = item.getLoppedCallnum(isSerial);

      // get shelflist pieces
      String shelfkey = "";
      String reversekey = "";
      if ( item.hasSeparateBrowseCallnum() || !(item.hasIgnoredCallnum() || item.hasBadLcLaneCallnum() ) )
      {
        shelfkey = item.getShelfkey(isSerial);
        reversekey = item.getReverseShelfkey(isSerial);
      }

      // get sortable call number for record view
      String volSort = "";
      if (!item.hasIgnoredCallnum())
        volSort = item.getCallnumVolSort(isSerial);

      String library = item.getLibrary();

      // deal with shelved by title locations
      if (item.hasShelbyLoc() &&
          !item.isInProcess() && !item.isOnOrder() && !item.isOnline())
      {
        // get volume info to show in record view
        String volSuffix = null;
        // ensure we're using a true lopped call number -- if only
        //   one item, this would have been set to full callnum
        CallNumberType callnumType = item.getCallnumType();
        loppedCallnum = CallNumUtils.getLoppedCallnum(fullCallnum, callnumType, isSerial);
        if (loppedCallnum != null && loppedCallnum.length() > 0)
          volSuffix = fullCallnum.substring(loppedCallnum.length()).trim();
        if ( (volSuffix == null || volSuffix.length() == 0) && CallNumUtils.callNumIsVolSuffix(fullCallnum))
          volSuffix = fullCallnum;

        if (homeLoc.equals("SHELBYSER"))
          loppedCallnum = "Shelved by Series title";
        else if (StanfordIndexer.SHELBY_LOCS.contains(homeLoc) ||
             (library.equals("BUSINESS") && item.hasBizShelbyLoc()))
          loppedCallnum = "Shelved by title";

        fullCallnum = loppedCallnum + " " + volSuffix;
        shelfkey = loppedCallnum.toLowerCase();
        reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
        isSerial = true;
        volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(fullCallnum, loppedCallnum, shelfkey, edu.stanford.enumValues.CallNumberType.OTHER, isSerial, id);
      }

      if (shelfkey == null)
        shelfkey = ""; // avoid NPE
      else
        shelfkey = shelfkey.toLowerCase();

      if (reversekey == null)
        reversekey = "";  // avoid NPE
      else
        reversekey = reversekey.toLowerCase();

      // lopped callnum in item_display field is left blank when
      //   the call number is not to be displayed in search results
      String itemDispCallnum = "";
      if (loppedCallnum == null)
        loppedCallnum = ""; // avoid NPE
      if ( ! (item.hasSeparateBrowseCallnum()
          || StanfordIndexer.SKIPPED_CALLNUMS.contains(loppedCallnum)
          || loppedCallnum.startsWith(Item.ECALLNUM)
          || loppedCallnum.startsWith(Item.TMP_CALLNUM_PREFIX)
           ) )
        itemDispCallnum = loppedCallnum;

      if ( item.hasSeparateBrowseCallnum()
          || fullCallnum.startsWith(Item.TMP_CALLNUM_PREFIX)
          || loppedCallnum.equals(Item.ECALLNUM)
          || StanfordIndexer.SKIPPED_CALLNUMS.contains(fullCallnum))
        fullCallnum = "";

      // create field
      // INDEX-132 For SW redesign Summer 2014, add 999|o (public note) and 999|w (call number type)
      // to item_display
      result.add( item.getBarcode() + SEP +
                  library + SEP +
                  homeLoc + SEP +
                  item.getCurrLoc() + SEP +
                  item.getType() + SEP +
                  itemDispCallnum + SEP +
                  (item.isMissingOrLost() ? "" : shelfkey) + SEP +
                  (item.isMissingOrLost() ? "" : reversekey) + SEP +
                  fullCallnum + SEP +
                  volSort + SEP + item.getPublicNote() + SEP + item.getCallnumType());
    } // end loop through items

    return result;
  }

}
