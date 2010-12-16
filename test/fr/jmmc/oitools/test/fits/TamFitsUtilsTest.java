/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: TamFitsUtilsTest.java,v 1.2 2010-12-16 15:13:59 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2010/12/15 17:17:04  bourgesl
 * updated test cases for invalid characters
 *
 */
package fr.jmmc.oitools.test.fits;

import fr.nom.tam.fits.FitsUtil;

/**
 * This class makes several tests concerning byte[] <=> String conversions ...
 * @author bourgesl
 */
public class TamFitsUtilsTest {

  private TamFitsUtilsTest() {
  }

  // --- TEST CODE -------------------------------------------------------------
  public static void main(String[] args) {

    String in = "\t\n\rABCDEFGH\u0394\u03BB\u003f";
    byte[] out = FitsUtil.getAsciiBytes(in);
    System.out.println("getAsciiBytes('"+in+"') = " + dumpBytes(out));

    byte[] input;
    String[] output;

    input = new byte[]{
              ' ', ' ',
              ' ', ' ',
              ' ', ' '};
    output = new String[]{
              "",
              "",
              ""};

    testBytesToStrings(input, 2, output);

    input = new byte[]{
              ' ', 'a', ' ',
              ' ', 'a', 'b',
              ' ', ' ', 'a',
              'a', ' ', ' ',
              'a', 'b', ' ',
              'a', 'b', 'c'
            };
    output = new String[]{"a", "ab", "a", "a", "ab", "abc"};

    testBytesToStrings(input, 3, output);

    // test invalid chars :
    input = new byte[]{
              0, 0,
              ' ', 0,
              0, ' ',
              'a', 0,
              13, 10, /* CR LF */
              126, 127
            };
    output = new String[]{
              "",
              "",
              "",
              "a",
              "",
              "~"};

    testBytesToStrings(input, 2, output);
  }

  private static void testBytesToStrings(final byte[] input, final int maxlen, final String[] output) {
    final String[] ostrings = FitsUtil.byteArrayToStrings(input, maxlen);
    equals(ostrings, output);
  }

  private static boolean equals(final String[] first, final String[] second) {
    final int len = first.length;
    if (len != second.length) {
      System.out.println("bad length : " + len + " <> " + second.length);
      return false;
    }
    for (int i = 0; i < len; i++) {
      if (!first[i].equals(second[i])) {
        System.out.println("bad result [" + i + "] : '" + first[i] + "' <> '" + second[i] + "'");
      }
    }
    return true;
  }

  private static String dumpBytes(final byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      return "";
    }
    final StringBuilder sb = new StringBuilder(4 * bytes.length);
    for (byte b : bytes) {
      sb.append("0x").append(Integer.toHexString(b)).append(' ');
    }
    return sb.toString();
  }
}
