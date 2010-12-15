package fr.nom.tam.fits;

/* Copyright: Thomas McGlynn 1999.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 */
import fr.nom.tam.util.RandomAccess;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;

import java.util.Map;
import java.util.List;

/** 
 * This class comprises static utility functions used throughout the FITS classes.
 */
public final class FitsUtil {

  /** Reposition a random access stream to a requested offset */
  public static void reposition(final Object o, final long offset)
          throws FitsException {

    if (o == null) {
      throw new FitsException("Attempt to reposition null stream");
    }
    if (!(o instanceof RandomAccess)
            || offset < 0) {
      throw new FitsException("Invalid attempt to reposition stream " + o
              + " of type " + o.getClass().getName()
              + " to " + offset);
    }

    try {
      ((RandomAccess) o).seek(offset);
    } catch (IOException e) {
      throw new FitsException("Unable to repostion stream " + o
              + " of type " + o.getClass().getName()
              + " to " + offset + "   Exception:" + e);
    }
  }

  /** Find out where we are in a random access file */
  public static long findOffset(final Object o) {

    if (o instanceof RandomAccess) {
      return ((RandomAccess) o).getFilePointer();
    } else {
      return -1;
    }
  }

  /** How many bytes are needed to fill the last 2880 block? */
  public static int padding(final int size) {
    return padding((long) size);
  }

  public static int padding(final long size) {

    int mod = (int) (size % 2880);
    if (mod > 0) {
      mod = 2880 - mod;
    }
    return mod;
  }

  /** Total size of blocked FITS element */
  public static int addPadding(final int size) {
    return size + padding(size);
  }

  public static long addPadding(final long size) {
    return size + padding(size);
  }

  /** Is a file compressed? */
  public static boolean isCompressed(final File test) {
    InputStream fis = null;
    try {
      if (test.exists()) {
        fis = new FileInputStream(test);
        int mag1 = fis.read();
        int mag2 = fis.read();

        if (mag1 == 0x1f && (mag2 == 0x8b || mag2 == 0x9d)) {
          return true;
        } else {
          return false;
        }
      }

    } catch (IOException e) {
      // This is probably a prelude to failure...
      return false;

    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
        }
      }
    }
    return false;
  }

  /** Check if a file seems to be compressed.
   */
  public static boolean isCompressed(final String filename) {
    if (filename == null) {
      return false;
    }
    FileInputStream fis = null;
    File test = new File(filename);
    if (test.exists()) {
      return isCompressed(test);
    }

    int len = filename.length();
    return len > 2 && (filename.substring(len - 3).equalsIgnoreCase(".gz") || filename.substring(len - 2).equals(".Z"));
  }

  /** Get the maximum length of a String in a String array.
   */
  public static int maxLength(final String[] o) throws FitsException {
    int max = 0;
    for (int i = 0, len = o.length; i < len; i++) {
      if (o[i] != null && o[i].length() > max) {
        max = o[i].length();
      }
    }
    return max;
  }

  /** 
   * Copy an array of Strings to bytes. For each string value :
   * if the string length is higher than maxLen, a substring is done with maxLen chars;
   * else the byte array is filled with 0x20 (space char) up to maxLen.
   */
  public static byte[] stringsToByteArray(final String[] o, final int maxLen) {
    final int len = o.length;
    final byte[] res = new byte[len * maxLen];
    byte[] bstr;
    for (int i = 0, cnt, j; i < len; i++) {
      if (o[i] == null) {
        bstr = new byte[0];
      } else {
        bstr = o[i].getBytes();
      }
      cnt = bstr.length;
      if (cnt > maxLen) {
        cnt = maxLen;
      }

      // TODO : check that bytes are valid ASCII text chars : see byteArrayToStrings

      System.arraycopy(bstr, 0, res, i * maxLen, cnt);
      for (j = cnt; j < maxLen; j++) {
        res[i * maxLen + j] = (byte) ' ';
      }
    }
    return res;
  }

  /** 
   * Convert bytes to Strings.
   *
   * Fits spec extract :
   * - ASCII text ASCII characters hexadecimal (20–7E)
   *
   * Character If the value of the TFORMn keyword specifies data type A, field n shall
   * contain a character string of zero or more members, composed of ASCII text.
   * This character string may be terminated before the length specified by the repeat count
   * by an ASCII NULL (hexadecimal code 00).
   * Characters after the first ASCII NULL are not defined.
   * A string with the number of characters specified by the repeat count is not NULL terminated.
   * Null strings are defined by the presence of an ASCII NULL as the first character.
   * 
   * @param o byte array to convert to strings
   * @param maxLen maximum length for strings
   * @return String array
   */
  public static String[] byteArrayToStrings(final byte[] o, final int maxLen) {
    final String[] res = new String[o.length / maxLen];

    byte b;
    for (int i = 0, j = 0, len = res.length, start, end, slen; i < len; i++) {

      start = i * maxLen;
      end = start + maxLen;

      // First pass to fix invalid characters (including ASCII NULL) :
      for (j = start; j < end; j++) {
        b = o[j];
        if (b == 0) {
          // ASCII NULL : string termination char

          // adjust end to the current char (stop string)
          end = j;
          break;

        } else if (b < 32 || b >= 127) {
          // Not ascii text : convert to space char (32) :
          // note : as it is not valid against the specification, throw an exception ?
          o[j] = 32;
        }
      }

      // Pre-trim the string to avoid keeping memory
      // hanging around. (Suggested by J.C. Segovia, ESA).

      // trim spaces from left to right :
      for (; start < end; start++) {
        if (o[start] != 32) {
          break; // Skip only spaces.
        }
      }

      // trim spaces from right to left :
      for (; end > start; end--) {
        if (o[end - 1] != 32) {
          break;
        }
      }

      slen = end - start;
      if (slen == 0) {
        res[i] = "";
      } else {
        res[i] = new String(o, start, end - start);
      }
    }
    return res;
  }

  /** Convert an array of booleans to bytes */
  static byte[] booleanToByte(final boolean[] bool) {
    final int len = bool.length;
    final byte[] byt = new byte[len];
    for (int i = 0; i < len; i++) {
      byt[i] = bool[i] ? (byte) 'T' : (byte) 'F';
    }
    return byt;
  }

  /** Convert an array of bytes to booleans */
  static boolean[] byteToBoolean(final byte[] byt) {
    final int len = byt.length;
    final boolean[] bool = new boolean[len];

    for (int i = 0; i < len; i++) {
      bool[i] = (byt[i] == 'T');
    }
    return bool;
  }

  /** Get a stream to a URL accommodating possible redirections.
   *  Note that if a redirection request points to a different
   *  protocol than the original request, then the redirection
   *  is not handled automatically.
   */
  public static InputStream getURLStream(final URL url, final int level) throws IOException {

    // Hard coded....sigh
    if (level > 5) {
      throw new IOException("Two many levels of redirection in URL");
    }
    URLConnection conn = url.openConnection();
//	Map<String,List<String>> hdrs = conn.getHeaderFields();
    Map hdrs = conn.getHeaderFields();

    // Read through the headers and see if there is a redirection header.
    // We loop (rather than just do a get on hdrs)
    // since we want to match without regard to case.
    String[] keys = (String[]) hdrs.keySet().toArray();
//	for (String key: hdrs.keySet()) {
    for (int i = 0; i < keys.length; i++) {
      String key = keys[i];

      if (key != null && key.toLowerCase().equals("location")) {
//	        String val = hdrs.get(key).get(0);
        String val = (String) ((List) hdrs.get(key)).get(0);
        if (val != null) {
          val = val.trim();
          if (val.length() > 0) {
            // Redirect
            return getURLStream(new URL(val), level + 1);
          }
        }
      }
    }
    // No redirection
    return conn.getInputStream();
  }

  private FitsUtil() {
  }
}
