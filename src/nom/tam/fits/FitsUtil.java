package nom.tam.fits;

/* Copyright: Thomas McGlynn 1999.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 */
import nom.tam.util.RandomAccess;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;

import java.util.Map;
import java.util.List;

/** This class comprises static
 *  utility functions used throughout
 *  the FITS classes.
 */
public class FitsUtil {

  /** Reposition a random access stream to a requested offset */
  public static void reposition(Object o, long offset)
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
  public static long findOffset(Object o) {

    if (o instanceof RandomAccess) {
      return ((RandomAccess) o).getFilePointer();
    } else {
      return -1;
    }
  }

  /** How many bytes are needed to fill the last 2880 block? */
  public static int padding(int size) {
    return padding((long) size);
  }

  public static int padding(long size) {

    int mod = (int) (size % 2880);
    if (mod > 0) {
      mod = 2880 - mod;
    }
    return mod;
  }

  /** Total size of blocked FITS element */
  public static int addPadding(int size) {
    return size + padding(size);
  }

  public static long addPadding(long size) {
    return size + padding(size);
  }

  /** Is a file compressed? */
  public static boolean isCompressed(File test) {
    InputStream fis = null;
    try {
      if (test.exists()) {
        fis = new FileInputStream(test);
        int mag1 = fis.read();
        int mag2 = fis.read();
        fis.close();
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
  public static boolean isCompressed(String filename) {
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
  public static int maxLength(String[] o) throws FitsException {

    int max = 0;
    for (int i = 0; i < o.length; i++) {
      if (o[i] != null && o[i].length() > max) {
        max = o[i].length();
      }
    }
    return max;
  }

  /** Copy an array of Strings to bytes.*/
  public static byte[] stringsToByteArray(String[] o, int maxLen) {
    byte[] res = new byte[o.length * maxLen];
    for (int i = 0; i < o.length; i++) {
      byte[] bstr;
      if (o[i] == null) {
        bstr = new byte[0];
      } else {
        bstr = o[i].getBytes();
      }
      int cnt = bstr.length;
      if (cnt > maxLen) {
        cnt = maxLen;
      }
      System.arraycopy(bstr, 0, res, i * maxLen, cnt);
      for (int j = cnt; j < maxLen; j++) {
        res[i * maxLen + j] = (byte) ' ';
      }
    }
    return res;
  }

  /** Convert bytes to Strings */
  public static String[] byteArrayToStrings(byte[] o, int maxLen) {

    String[] res = new String[o.length / maxLen];
    for (int i = 0; i < res.length; i++) {

      int start = i * maxLen;
      int end = start + maxLen;
      // Pre-trim the string to avoid keeping memory
      // hanging around. (Suggested by J.C. Segovia, ESA).
      for (; start < end; start++) {
        if (o[start] != 32) {
          break; // Skip only spaces.
        }
      }

      for (; end > start; end--) {
        if (o[end - 1] != 32) {
          break;
        }
      }

      res[i] = new String(o, start, end - start);

    }
    return res;

  }

  /** Convert an array of booleans to bytes */
  static byte[] booleanToByte(boolean[] bool) {

    byte[] byt = new byte[bool.length];
    for (int i = 0; i < bool.length; i++) {
      byt[i] = bool[i] ? (byte) 'T' : (byte) 'F';
    }
    return byt;
  }

  /** Convert an array of bytes to booleans */
  static boolean[] byteToBoolean(byte[] byt) {
    boolean[] bool = new boolean[byt.length];

    for (int i = 0; i < byt.length; i++) {
      bool[i] = (byt[i] == 'T');
    }
    return bool;
  }

  /** Get a stream to a URL accommodating possible redirections.
   *  Note that if a redirection request points to a different
   *  protocol than the original request, then the redirection
   *  is not handled automatically.
   */
  public static InputStream getURLStream(URL url, int level) throws IOException {

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
