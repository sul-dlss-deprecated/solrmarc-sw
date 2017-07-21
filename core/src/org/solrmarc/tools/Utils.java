package org.solrmarc.tools;
/**
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.apache.log4j.*;

/**
* General utility functions for solrmarc
*
* @author Wayne Graham
* @version $Id: Utils.java 868 2009-09-18 19:05:15Z rh9ec@virginia.edu $
*/

public final class Utils {

  protected static Logger logger = Logger.getLogger(Utils.class.getName());

  /**
   * Default Constructor,  private, so it can't be instantiated by other objects
   */
  private Utils(){ }

  /**
   * return an int for the passed string, catching NumberFormatException
   *  and ignoring it
   * @param str
   * @param defValue - default value, if string doesn't parse into int
   */
  public static int parseIntNoNFE(String str, int defValue)
  {
    int value = defValue;
    try
    {
        value = Integer.parseInt(str);
    }
    catch (NumberFormatException nfe)
    {
        // provided value is not valid numeric string
        // Ignoring it and moving happily on.
    }
    return (value);
  }


  /**
   * Takes an InputStream, reads the entire contents into a String
   * @param stream - the stream to read in.
   * @return String containing entire contents of stream.
   */
  public static String readStreamIntoString(InputStream stream) throws IOException
  {
    Reader in = new BufferedReader(new InputStreamReader(stream));

    StringBuilder sb = new StringBuilder();
    char[] chars = new char[4096];
    int length;

    while ((length = in.read(chars)) > 0) {
      sb.append(chars, 0, length);
    }

    return sb.toString();
   }

  /**
   * Removes trailing characters (space, comma, slash, semicolon, colon),
   *  trailing period if it is preceded by at least three letters,
   *  and single square bracket characters if they are the start and/or end
   *  chars of the cleaned string
   *
   * @param origStr String to clean
   * @return cleaned string
   */
  public static String cleanData(String origStr)
  {
    String currResult = origStr;
    String prevResult;
    do {
        prevResult = currResult;
        currResult = currResult.trim();

        currResult = currResult.replaceAll(" *([,/;:])$", "");

        // trailing period removed in certain circumstances
        if (currResult.endsWith("."))
        {
            if (currResult.matches(".*\\w\\w\\.$"))
                currResult = currResult.substring(0, currResult.length()-1);
            else if (currResult.matches(".*\\p{L}\\p{L}\\.$"))
                currResult = currResult.substring(0, currResult.length()-1);
            else if (currResult.matches(".*\\w\\p{InCombiningDiacriticalMarks}?\\w\\p{InCombiningDiacriticalMarks}?\\.$"))
                currResult = currResult.substring(0, currResult.length()-1);
        }

        currResult = removeOuterBrackets(currResult);

        if (currResult.length() == 0)
            return currResult;

    } while (! currResult.equals(prevResult));

    return currResult;
  }

  /**
   * Call cleanData on an entire set of Strings has a side effect
   * of deleting entries that are identical when they are cleaned.
   * @param values - the set to clean
   * @return Set<String> - the "same" set with all of its entries cleaned.
   */
  private static Set<String> cleanData(Set<String> values)
  {
    Set<String> result = new LinkedHashSet<String>();
    for (String entry : values)
    {
        String cleaned = cleanData(entry);
        result.add(cleaned);
    }
    return(result);
  }


  /**
   * Repeatedly removes trailing characters indicated in regular expression,
   * PLUS trailing period if it is preceded by its regular expression
   *
   * @param origStr String to clean
   * @param trailingCharsRegEx a regular expression of trailing chars to be
   *   removed (see java Pattern class).  Note that the regular expression
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
   * @return cleaned string
   */
  public static String removeAllTrailingCharAndPeriod(String origStr, String trailingCharsRegEx, String charsB4periodRegEx)
  {
    if (origStr == null)
        return null;

    String currResult = origStr;
    String prevResult;
    do {
        prevResult = currResult;
        currResult = removeTrailingCharAndPeriod(currResult.trim(), trailingCharsRegEx, charsB4periodRegEx);

        if (currResult.length() == 0)
            return currResult;

    } while (! currResult.equals(prevResult));

    return currResult;
  }

  /**
   * Removes trailing characters indicated in regular expression, PLUS
   *  trailing period if it is preceded by its regular expression.
   *
   * @param origStr String to clean
   * @param trailingCharsRegEx a regular expression of trailing chars to be
   *   removed (see java Pattern class).  Note that the regular expression
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
   * @return cleaned string
   */
  public static String removeTrailingCharAndPeriod(String origStr, String trailingCharsRegEx, String charsB4periodRegEx)
  {
    if (origStr == null)
        return null;

    String result = removeTrailingChar(origStr, trailingCharsRegEx);
    return removeTrailingPeriod(result, charsB4periodRegEx);
  }

  /**
   * Remove the characters per the regular expression if they are at the end
   *  of the string.
   * @param origStr string to be cleaned
   * @param charsToReplaceRegEx - a regular expression of the trailing string/chars
   *   to be removed e.g. " *([,/;:])" meaning last character is a comma,
   *   slash, semicolon, colon, possibly preceded by one or more spaces.
   * @see Pattern class in java api
   * @return the string with the specified trailing characters removed
   */
  public static String removeTrailingChar(String origStr, String charsToReplaceRegEx)
  {
    if (origStr == null)
        return origStr;
       // get rid of reg ex specified chars at the end of the string
    return origStr.trim().replaceAll(charsToReplaceRegEx + "$", "");
  }

  /**
   * If there is a period at the end of the string, remove the period if it is
   *  immediately preceded by the regular expression
   * @param origStr the string to be cleaned
   * @param precedingCharsRegEx a regular expression that must immediately
   *  precede a trailing period IN ORDER FOR THE PERIOD TO BE REMOVED.
   *  Note that the regular expression will NOT have the period or '$' at
   *  the end.
   *   (e.g. "[a-zA-Z]{3,}" means at least three letters must immediately
   *   precede the period for it to be removed.)
   * @return the string without a trailing period iff the regular expression
   *   param was found immediately before the trailing period
   */
  public static String removeTrailingPeriod(String origStr, String precedingCharsRegEx)
  {
    if (origStr == null)
        return origStr;
    String result = origStr.trim();
    if (result.endsWith(".") && result.matches(".*" + precedingCharsRegEx + "\\.$"))
        result = result.substring(0, result.length() - 1).trim();

    return result;
   }


  /**
   * Remove single square bracket characters if they are the start and/or end
   *  chars (matched or unmatched) and are the only square bracket chars in
   *  the string.
   */
  public static String removeOuterBrackets(String origStr)
  {
    if (origStr == null || origStr.length() == 0)
        return origStr;

    String result = origStr.trim();

    if (result.length() > 0)
    {
        boolean openBracketFirst = result.charAt(0) == '[';
        boolean closeBracketLast = result.endsWith("]");
        if (openBracketFirst && closeBracketLast &&
                result.indexOf('[', 1) == -1 &&
                result.lastIndexOf(']', result.length()-2) == -1)
            // only square brackets are at beginning and end
            result = result.substring(1, result.length()-1);
        else if (openBracketFirst && result.indexOf(']') == -1)
            // starts with '[' but no ']'; remove open bracket
            result = result.substring(1);
        else if (closeBracketLast && result.indexOf('[') == -1)
            // ends with ']' but no '['; remove close bracket
            result = result.substring(0, result.length()-1);
    }

    return result.trim();
  }


  /**
   * Test if a String has a numeric equivalent
   * @param number String representation of a number
   * @return True if String is a number; False if it is not
   */
  public static boolean isNumber(final String number)
  {
    boolean isNumber; // fix for dd-anomaly

    try
    {
        Integer.parseInt(number);
        isNumber = true;
    }
    catch(NumberFormatException nfe)
    {
        // eat the exception
        isNumber = false;
    }

    return isNumber;
  }

  /**
   * Remap a field value.  If the field value is not present in the map, then:
   *   if "displayRawIfMissing" is a key in the map, then the raw field value
   *   is used.
   *   if "displayRawIfMissing" is not a key in the map, and the allowDefault
   *   param is set to true, then if the map contains "__DEFAULT" as a key,
   *   the value of "__DEFAULT" in the map is used;  if allowDefault is true
   *   and there is neither "displayRawIfMissing" nor "__DEFAULT", as a key
   *   in the map, then if the map contains an empty key, the map value of the
   *   empty key is used.
   *     NOTE:  If the spec for a field is supposed to contain all matching
   *      values, then the default lookup needs to be done here.  If the spec
   *      for a field is only supposed to return the first matching mappable
   *      value, then the default mapping should be done in the calling method
   * @param fieldVal - the raw value to be mapped
   * @param map - the map to be used
   * @param allowDefault - if "displayRawIfMissing" is not a key in the map,
   *   and this is to true, then if the map contains "__DEFAULT" as a key,
   *   the value of "__DEFAULT" in the map is used.
   * @return the new value, as determined by the mapping.
   */
  public static String remap(String fieldVal, Map<String, String> map, boolean allowDefault)
  {
    String result = null;

    if (map.keySet().contains("pattern_0"))
    {
        for (int i = 0; i < map.keySet().size(); i++)
        {
            String patternStr = map.get("pattern_"+i);
            String parts[] = patternStr.split("=>");
            if (containsMatch(fieldVal, parts[0]))
            {
                String newVal = parts[1];
                if (parts[1].contains("$"))
                {
                    newVal = fieldVal.replaceAll(parts[0], parts[1]);
                    fieldVal = newVal;
                }
                result = newVal;
            }
        }
    }
    if (map.containsKey(fieldVal))
        result = map.get(fieldVal);
    else if (map.containsKey("displayRawIfMissing"))
        result = fieldVal;
    else if (allowDefault && map.containsKey("__DEFAULT"))
        result = map.get("__DEFAULT");
    else if (allowDefault && map.containsKey(""))
        result = map.get("");

    if (result == null || result.length() == 0)
      return null;
    return result;
  }

  /**
   * Remap a set of field values.  If a field value is not present in the map,
   * then:
   *   if "displayRawIfMissing" is a key in the map, then the raw field value
   *   is used.
   *   if "displayRawIfMissing" is not a key in the map, and the allowDefault
   *   param is set to true, then if the map contains "__DEFAULT" as a key,
   *   the value of "__DEFAULT" in the map is used;  if allowDefault is true
   *   and there is neither "displayRawIfMissing" nor "__DEFAULT", as a key
   *   in the map, then if the map contains an empty key, the map value of the
   *   empty key is used.
   *     NOTE:  If the spec for a field is supposed to contain all matching
   *      values, then the default lookup needs to be done here.  If the spec
   *      for a field is only supposed to return the first matching mappable
   *      value, then the default mapping should be done in the calling method
   * @param set - set of raw vaues to be mapped
   * @param map - the map to be used
   * @param allowDefault - if "displayRawIfMissing" is not a key in the map,
   *   and this is to true, then if the map contains "__DEFAULT" as a key,
   *   the value of "__DEFAULT" in the map is used.
   * @return the new value, as determined by the mapping.
   */
  public static Set<String> remap(Set<String> set, Map<String, String> map, boolean allowDefault)
  {
    if (map == null)  return(set);
    Iterator<String> iter = set.iterator();
    Set<String> result = new LinkedHashSet<String>();

    while(iter.hasNext())
    {
        String val = iter.next();
        if (map.keySet().contains("pattern_0"))
        {
            String tmpResult = null;
            for (int i = 0; i < map.keySet().size(); i++)
            {
                String patternStr = map.get("pattern_"+i);
                String parts[] = patternStr.split("=>");
                if (containsMatch(val, parts[0]))
                {
                    String newVal = parts[1];
                    if (parts[1].contains("$"))
                    {
                        newVal = val.replaceAll(parts[0], parts[1]);
                        val = newVal;
                    }
                    else
                        result.add(newVal);
                    tmpResult = newVal;
                }
            }
            if (tmpResult != null) result.add(tmpResult);
        }
        else
        {
            String mappedVal = remap(val, map, allowDefault);
            if (mappedVal != null)
                result.add(mappedVal);
        }
    }
    return result;
  }

  private static boolean containsMatch(String val, String pattern)
  {
    String rep = val.replaceFirst(pattern, "###match###");
    if (!rep.equals(val))
        return true;

    return false;
  }

  /**
   * Test if a set contains a specified pattern
   * @param set Set of marc fields to test
   * @param pattern Regex String pattern to match
   * @return If the set contains the pattern, return true, else false
   */
  public static boolean setItemContains(Set<String> set, String pattern)
  {
    if (set.isEmpty())
        return(false);

    Iterator<String> iter = set.iterator();
    while (iter.hasNext())
    {
        String value = (String)iter.next();
        if (containsMatch(value, pattern))
            return true;
    }
    return false;
  }

  /**
   * Join two fields together with seperator
   * @param set Set of marc fields to join
   * @param separator Separation character to put between
   * @return Joined fields
   */
  public static String join(Set<String> set, String separator)
  {
    Iterator<String> iter = set.iterator();
    StringBuilder result = new StringBuilder("");

    while(iter.hasNext())
    {
        result.append(iter.next());
        if (iter.hasNext())  {
            result.append(separator);
        }
    }

    return result.toString();
  }

  public static Set<String> trimNearDuplicates(Set<String> locations)
  {
    locations = cleanData(locations);
    if (locations.size() <= 1) return(locations);
    Object locArr[] = locations.toArray();
    int size = locArr.length;
    for (int i = 0; i < size; i++)
    {
        boolean copyStrI = true;
        for (int j = 0; j < size; j++)
        {
            if (i == j) continue;
            if (locArr[j].toString().contains(locArr[i].toString())) { copyStrI = false; break; }
        }
        if (copyStrI == false) locations.remove(locArr[i]);
    }
    return locations;
  }

  public static String cleanFacetPunct(String origStr)
  {
    String currResult = origStr;
    currResult = currResult.replaceAll("^[%\\*]", ""); // begins with percent sign or asterisk
    currResult = currResult.replaceAll("\\({2,}+", "("); // two or more open parentheses
    currResult = currResult.replaceAll("\\){2,}+", ")"); // two or more close parentheses
    currResult = currResult.replaceAll("!{2,}+", "!"); // two or more exlamation points
    currResult = currResult.replaceAll("\\s+", " "); // one or more spaces

    // remove unmatched parentheses
    int len = currResult.length();
    StringBuilder result = new StringBuilder(currResult);
    Stack<Integer> stk = new Stack<Integer>();
    for (int i = 0; i < len; i++)
    {
      char ch = currResult.charAt(i);
      if (ch == '(')
        stk.push(i);
      else if (ch == ')')
        try
        {
          int p = stk.pop(); // matched parentheses
        }
        catch(Exception e)
        {
          result.deleteCharAt(i);
        }
    }
    while (!stk.isEmpty() )
      result.deleteCharAt(stk.pop());
    String newResult = result.toString();
    return newResult;
  }

  /**
   * returns true if the 3 letter language code is for a right to left
   *  language (one written in arabic or hebrew characters)
   * @param langcode
   */
  public final static boolean isRightToLeftLanguage(String langcode)
  {
    if (
      // arabic characters
      langcode.equals("ara") || langcode.equals("per") || langcode.equals("urd")
      ||
      // hebrew characters
      langcode.equals("heb") || langcode.equals("yid") || langcode.equals("lad")
      || langcode.equals("jpr") || langcode.equals("jrb")
      )
      return true;
    else
      return false;
  }


  /**
   * return the index within this string of the first occurrence of an open
   *  parenthesis that isn't escaped with a backslash.
   * @param str
   * @return if an unescaped open parenthesis occurs within this object,
   * return the index of the first open paren; -1 if no unescaped open paren.
   */
  public final static int getIxUnescapedOpenParen(String str)
  {
    if (str.startsWith("("))
      return 0;
    Pattern p = Pattern.compile(".*[^\\\\](\\().*");
    Matcher m = p.matcher(str);
    if (m.matches())
      return m.start(1);
    else
      return -1;
  }


  /**
   * return the index within this string of the first occurrence of a comma
   *  that isn't escaped with a backslash.
   * @param str
   * @return if an unescaped comma occurs within this object, the index of the
   *  first comma; -1 if no unescaped comma.
   */
  public final static int getIxUnescapedComma(String str)
  {
    if (str.startsWith(","))
      return 0;
    Pattern p = Pattern.compile(".*[^\\\\](,).*");
    Matcher m = p.matcher(str);
    if (m.matches())
      return m.start(1);
    else
      return -1;
  }

  /**
   * Look for Strings in the set, that start with the given prefix.  If found,
   *  remove the prefix, trim the result and add it to the returned set of
   *  Strings to be returned.
   * @param valueSet
   * @param prefix
   * @return set members that had the prefix, but now prefix is removed and
   *   remaining value is trimmed.
   */
  public final static Set<String> getPrefixedVals(Set<String> valueSet, String prefix) {
    Set<String> resultSet = new LinkedHashSet<String>();
    if (!valueSet.isEmpty()) {
      Iterator<String> iter = valueSet.iterator();
      while (iter.hasNext()) {
        String s = removePrefix((String) iter.next(), prefix);
        if (s != null) {
          String value = s.trim();
          if (value != null && value.length() != 0)
            resultSet.add(value);
        }
      }
    }
    return resultSet;
  }

  /**
   * remove prefix from the beginning of the value string.
   */
  public final static String removePrefix(String value, String prefix) {
    if (value.startsWith(prefix)) {
      value = value.substring(prefix.length());
      if (value != null && value.length() != 0)
        return value;
    }
    return null;
  }

  /**
   * returns the valid ISBN(s) from the set of candidate Strings
   * @return Set of strings containing valid ISBN numbers
   */
  public static Set<String> returnValidISBNs(Set<String> candidates)
  {
    // NOTE 1: last digit of ISBN is a check digit and may be "X" (0,1,2,3,4,5,6.7.8.9.X)
    // NOTE 2: ISBN can be 10 or 13 digits (and may end with X).
    // NOTE 3: 13 digit ISBN must start with 978 or 979.
    // NOTE 4: there may be text after the ISBN, which should be removed

    Set<String> isbnSet = new LinkedHashSet<String>();

    Pattern p10 = Pattern.compile("^\\d{9}[\\dX].*");
    Pattern p13 = Pattern.compile("^(978|979)\\d{9}[X\\d].*");
    // p13any matches a 13 digit isbn pattern without the correct prefix
    Pattern p13any = Pattern.compile("^\\d{12}[X\\d].*");

    for (String cand : candidates) {
      String value = cand.trim();
      // check we have the right pattern, and remove trailing text
      if (p13.matcher(value).matches())
        isbnSet.add(value.substring(0, 13));
      else if (p10.matcher(value).matches() && !p13any.matcher(value).matches())
        isbnSet.add(value.substring(0, 10));
    }
    return isbnSet;
  }

  /**
   * given a latin letter with a diacritic, return the latin letter without
   *  the diacritic.
   * Shamelessly stolen from UnicodeCharUtil class of UnicodeNormalizeFilter
   *  by Bob Haschart
   */
  public static char foldDiacriticLatinChar ( char c )
  {
    switch (c) {
      case 0x0181:  return(0x0042);    //  LATIN CAPITAL LETTER B WITH HOOK -> LATIN CAPITAL LETTER B
      case 0x0182:  return(0x0042);    //  LATIN CAPITAL LETTER B WITH TOPBAR -> LATIN CAPITAL LETTER B
      case 0x0187:  return(0x0043);    //  LATIN CAPITAL LETTER C WITH HOOK -> LATIN CAPITAL LETTER C
      case 0x0110:  return(0x0044);    //  LATIN CAPITAL LETTER D WITH STROKE -> LATIN CAPITAL LETTER D
      case 0x018A:  return(0x0044);    //  LATIN CAPITAL LETTER D WITH HOOK -> LATIN CAPITAL LETTER D
      case 0x018B:  return(0x0044);    //  LATIN CAPITAL LETTER D WITH TOPBAR -> LATIN CAPITAL LETTER D
      case 0x0191:  return(0x0046);    //  LATIN CAPITAL LETTER F WITH HOOK -> LATIN CAPITAL LETTER F
      case 0x0193:  return(0x0047);    //  LATIN CAPITAL LETTER G WITH HOOK -> LATIN CAPITAL LETTER G
      case 0x01E4:  return(0x0047);    //  LATIN CAPITAL LETTER G WITH STROKE -> LATIN CAPITAL LETTER G
      case 0x0126:  return(0x0048);    //  LATIN CAPITAL LETTER H WITH STROKE -> LATIN CAPITAL LETTER H
      case 0x0197:  return(0x0049);    //  LATIN CAPITAL LETTER I WITH STROKE -> LATIN CAPITAL LETTER I
      case 0x0198:  return(0x004B);    //  LATIN CAPITAL LETTER K WITH HOOK -> LATIN CAPITAL LETTER K
      case 0x0141:  return(0x004C);    //  LATIN CAPITAL LETTER L WITH STROKE -> LATIN CAPITAL LETTER L
      case 0x019D:  return(0x004E);    //  LATIN CAPITAL LETTER N WITH LEFT HOOK -> LATIN CAPITAL LETTER N
      case 0x0220:  return(0x004E);    //  LATIN CAPITAL LETTER N WITH LONG RIGHT LEG -> LATIN CAPITAL LETTER N
      case 0x00D8:  return(0x004F);    //  LATIN CAPITAL LETTER O WITH STROKE -> LATIN CAPITAL LETTER O
      case 0x019F:  return(0x004F);    //  LATIN CAPITAL LETTER O WITH MIDDLE TILDE -> LATIN CAPITAL LETTER O
      case 0x01FE:  return(0x004F);    //  LATIN CAPITAL LETTER O WITH STROKE AND ACUTE -> LATIN CAPITAL LETTER O
      case 0x01A4:  return(0x0050);    //  LATIN CAPITAL LETTER P WITH HOOK -> LATIN CAPITAL LETTER P
      case 0x0166:  return(0x0054);    //  LATIN CAPITAL LETTER T WITH STROKE -> LATIN CAPITAL LETTER T
      case 0x01AC:  return(0x0054);    //  LATIN CAPITAL LETTER T WITH HOOK -> LATIN CAPITAL LETTER T
      case 0x01AE:  return(0x0054);    //  LATIN CAPITAL LETTER T WITH RETROFLEX HOOK -> LATIN CAPITAL LETTER T
      case 0x01B2:  return(0x0056);    //  LATIN CAPITAL LETTER V WITH HOOK -> LATIN CAPITAL LETTER V
      case 0x01B3:  return(0x0059);    //  LATIN CAPITAL LETTER Y WITH HOOK -> LATIN CAPITAL LETTER Y
      case 0x01B5:  return(0x005A);    //  LATIN CAPITAL LETTER Z WITH STROKE -> LATIN CAPITAL LETTER Z
      case 0x0224:  return(0x005A);    //  LATIN CAPITAL LETTER Z WITH HOOK -> LATIN CAPITAL LETTER Z
      case 0x0180:  return(0x0062);    //  LATIN SMALL LETTER B WITH STROKE -> LATIN SMALL LETTER B
      case 0x0183:  return(0x0062);    //  LATIN SMALL LETTER B WITH TOPBAR -> LATIN SMALL LETTER B
      case 0x0253:  return(0x0062);    //  LATIN SMALL LETTER B WITH HOOK -> LATIN SMALL LETTER B
      case 0x0188:  return(0x0063);    //  LATIN SMALL LETTER C WITH HOOK -> LATIN SMALL LETTER C
      case 0x0255:  return(0x0063);    //  LATIN SMALL LETTER C WITH CURL -> LATIN SMALL LETTER C
      case 0x0111:  return(0x0064);    //  LATIN SMALL LETTER D WITH STROKE -> LATIN SMALL LETTER D
      case 0x018C:  return(0x0064);    //  LATIN SMALL LETTER D WITH TOPBAR -> LATIN SMALL LETTER D
      case 0x0221:  return(0x0064);    //  LATIN SMALL LETTER D WITH CURL -> LATIN SMALL LETTER D
      case 0x0256:  return(0x0064);    //  LATIN SMALL LETTER D WITH TAIL -> LATIN SMALL LETTER D
      case 0x0257:  return(0x0064);    //  LATIN SMALL LETTER D WITH HOOK -> LATIN SMALL LETTER D
      case 0x0192:  return(0x0066);    //  LATIN SMALL LETTER F WITH HOOK -> LATIN SMALL LETTER F
      case 0x01E5:  return(0x0067);    //  LATIN SMALL LETTER G WITH STROKE -> LATIN SMALL LETTER G
      case 0x0260:  return(0x0067);    //  LATIN SMALL LETTER G WITH HOOK -> LATIN SMALL LETTER G
      case 0x0127:  return(0x0068);    //  LATIN SMALL LETTER H WITH STROKE -> LATIN SMALL LETTER H
      case 0x0266:  return(0x0068);    //  LATIN SMALL LETTER H WITH HOOK -> LATIN SMALL LETTER H
      case 0x0268:  return(0x0069);    //  LATIN SMALL LETTER I WITH STROKE -> LATIN SMALL LETTER I
      case 0x029D:  return(0x006A);    //  LATIN SMALL LETTER J WITH CROSSED-TAIL -> LATIN SMALL LETTER J
      case 0x0199:  return(0x006B);    //  LATIN SMALL LETTER K WITH HOOK -> LATIN SMALL LETTER K
      case 0x0142:  return(0x006C);    //  LATIN SMALL LETTER L WITH STROKE -> LATIN SMALL LETTER L
      case 0x019A:  return(0x006C);    //  LATIN SMALL LETTER L WITH BAR -> LATIN SMALL LETTER L
      case 0x0234:  return(0x006C);    //  LATIN SMALL LETTER L WITH CURL -> LATIN SMALL LETTER L
      case 0x026B:  return(0x006C);    //  LATIN SMALL LETTER L WITH MIDDLE TILDE -> LATIN SMALL LETTER L
      case 0x026C:  return(0x006C);    //  LATIN SMALL LETTER L WITH BELT -> LATIN SMALL LETTER L
      case 0x026D:  return(0x006C);    //  LATIN SMALL LETTER L WITH RETROFLEX HOOK -> LATIN SMALL LETTER L
      case 0x0271:  return(0x006D);    //  LATIN SMALL LETTER M WITH HOOK -> LATIN SMALL LETTER M
      case 0x019E:  return(0x006E);    //  LATIN SMALL LETTER N WITH LONG RIGHT LEG -> LATIN SMALL LETTER N
      case 0x0235:  return(0x006E);    //  LATIN SMALL LETTER N WITH CURL -> LATIN SMALL LETTER N
      case 0x0272:  return(0x006E);    //  LATIN SMALL LETTER N WITH LEFT HOOK -> LATIN SMALL LETTER N
      case 0x0273:  return(0x006E);    //  LATIN SMALL LETTER N WITH RETROFLEX HOOK -> LATIN SMALL LETTER N
      case 0x00F8:  return(0x006F);    //  LATIN SMALL LETTER O WITH STROKE -> LATIN SMALL LETTER O
      case 0x01FF:  return(0x006F);    //  LATIN SMALL LETTER O WITH STROKE AND ACUTE -> LATIN SMALL LETTER O
      case 0x01A5:  return(0x0070);    //  LATIN SMALL LETTER P WITH HOOK -> LATIN SMALL LETTER P
      case 0x02A0:  return(0x0071);    //  LATIN SMALL LETTER Q WITH HOOK -> LATIN SMALL LETTER Q
      case 0x027C:  return(0x0072);    //  LATIN SMALL LETTER R WITH LONG LEG -> LATIN SMALL LETTER R
      case 0x027D:  return(0x0072);    //  LATIN SMALL LETTER R WITH TAIL -> LATIN SMALL LETTER R
      case 0x0282:  return(0x0073);    //  LATIN SMALL LETTER S WITH HOOK -> LATIN SMALL LETTER S
      case 0x0167:  return(0x0074);    //  LATIN SMALL LETTER T WITH STROKE -> LATIN SMALL LETTER T
      case 0x01AB:  return(0x0074);    //  LATIN SMALL LETTER T WITH PALATAL HOOK -> LATIN SMALL LETTER T
      case 0x01AD:  return(0x0074);    //  LATIN SMALL LETTER T WITH HOOK -> LATIN SMALL LETTER T
      case 0x0236:  return(0x0074);    //  LATIN SMALL LETTER T WITH CURL -> LATIN SMALL LETTER T
      case 0x0288:  return(0x0074);    //  LATIN SMALL LETTER T WITH RETROFLEX HOOK -> LATIN SMALL LETTER T
      case 0x028B:  return(0x0076);    //  LATIN SMALL LETTER V WITH HOOK -> LATIN SMALL LETTER V
      case 0x01B4:  return(0x0079);    //  LATIN SMALL LETTER Y WITH HOOK -> LATIN SMALL LETTER Y
      case 0x01B6:  return(0x007A);    //  LATIN SMALL LETTER Z WITH STROKE -> LATIN SMALL LETTER Z
      case 0x0225:  return(0x007A);    //  LATIN SMALL LETTER Z WITH HOOK -> LATIN SMALL LETTER Z
      case 0x0290:  return(0x007A);    //  LATIN SMALL LETTER Z WITH RETROFLEX HOOK -> LATIN SMALL LETTER Z
      case 0x0291:  return(0x007A);    //  LATIN SMALL LETTER Z WITH CURL -> LATIN SMALL LETTER Z
      default:      return(0x00);
    }
  }


  /**
   * delete the directory indicated by the argument.
   * @param dirPath - path of directory to be deleted.
   */
  public static final void deleteDirContents(String dirPath) {
    File d = new File(dirPath);
    File[] files = d.listFiles();
    if (files != null)
      for (File file: files)
      {  // recursively remove files and directories
        deleteDir(file.getAbsolutePath());
      }
  }

  /**
   * delete the directory indicated by the argument.
   * @param dirPath - path of directory to be deleted.
   */
  public static final void deleteDir(String dirPath) {
    File d = new File(dirPath);
    File[] files = d.listFiles();
    if (files != null)
      for (File file: files)
      {  // recursively remove files and directories
        deleteDir(file.getAbsolutePath());
      }
    logger.debug("Deleting: "+ d.getAbsolutePath());
    d.delete();
  }


   /** compares two strings after removing periods and spaces */
  public static Comparator<String> compareNoPeriodsOrSpaces = new Comparator<String>() {
    public int compare(String s1, String s2)
    {
      String s1Norm = s1.replaceAll("[. ]", "");
      String s2Norm = s2.replaceAll("[. ]", "");
      return s1Norm.compareToIgnoreCase(s2Norm);
    }
  };

  /**
   * return the longest prefix common to both strings by checking each
   *  character from the beginning for equality.
   * @param str1 - first string to be compared
   * @param str2 - second string to be compared
   * @param comp - comparator used to determine equality of strings
   * @return longest prefix common to both strings
   */
  public static String getCommonPrefix(String str1, String str2, Comparator<String> comp) {
    int shortestLen = Math.min(str1.length(), str2.length());
    int prefixLen = shortestLen;
    for (int i = 0; i < shortestLen; i++)
    {
      if (comp.compare(str1.substring(i, i+1), str2.substring(i, i+1) ) != 0)
      {
        prefixLen = i;
        break;
      }
    }
    return (str1.substring(0, prefixLen));
  }

  /**
   * default settings: solr: WARNING; solrmarc: WARN Solr uses
   * java.util.logging; level settings for solr logging: OFF, SEVERE, WARNING, INFO, FINE, FINER, FINEST, ALL
   * SolrMarc uses log4j logging; level settings for solrmarc logging: OFF, FATAL, WARN, INFO, DEBUG, ALL
   */
  public static void setLoggingLevels(String solrLogLevel, String solrmarcLogLevel)
  {
    org.apache.log4j.Level solrLevel = org.apache.log4j.Level.WARN;
    if (solrLevel != null)
    {
      if (solrLevel.equals("OFF")) solrLevel = Level.OFF;
      if (solrLevel.equals("FATAL")) solrLevel = Level.FATAL;
      if (solrLevel.equals("WARN")) solrLevel = Level.WARN;
      if (solrLevel.equals("INFO")) solrLevel = Level.INFO;
      if (solrLevel.equals("DEBUG")) solrLevel = Level.DEBUG;
      if (solrLevel.equals("ALL"))  solrLevel = Level.ALL;
    }
    setLog4jLogLevel(solrLevel);

    org.apache.log4j.Level solrmarcLevel = org.apache.log4j.Level.WARN;
    if (solrmarcLogLevel != null)
    {
      if (solrmarcLogLevel.equals("OFF")) solrmarcLevel = Level.OFF;
      if (solrmarcLogLevel.equals("FATAL")) solrmarcLevel = Level.FATAL;
      if (solrmarcLogLevel.equals("WARN")) solrmarcLevel = Level.WARN;
      if (solrmarcLogLevel.equals("INFO")) solrmarcLevel = Level.INFO;
      if (solrmarcLogLevel.equals("DEBUG")) solrmarcLevel = Level.DEBUG;
      if (solrmarcLogLevel.equals("ALL"))  solrmarcLevel = Level.ALL;
    }
    setLog4jLogLevel(solrmarcLevel);
  }

  /**
   * loop through all the log4j loggers under the root and set them to the
   *  new log4j level.
   * @param newLevel
   */
 @SuppressWarnings("unchecked")
  public static void setLog4jLogLevel(org.apache.log4j.Level newLevel)
  {
      Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
      Enumeration<Logger> enLogger = rootLogger.getLoggerRepository().getCurrentLoggers();
      // loop through all loggers under root
      while(enLogger.hasMoreElements())
      {
          Logger tmpLogger = (Logger)(enLogger.nextElement());
          tmpLogger.setLevel(newLevel);
      }
      Enumeration<Appender> enAppenders = rootLogger.getAllAppenders();
      Appender appender;
      while(enAppenders.hasMoreElements())
      {
        appender = (Appender)enAppenders.nextElement();

        if(appender instanceof AsyncAppender)
        {
          AsyncAppender asyncAppender = (AsyncAppender)appender;
          asyncAppender.activateOptions();
        }
      }
  }

}
