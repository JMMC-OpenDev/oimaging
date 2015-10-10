/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.test.fits;

import fr.nom.tam.fits.FitsFactory;
import fr.nom.tam.fits.FitsUtil;
import fr.nom.tam.util.AsciiFuncs;

/**
 * This class makes several tests concerning byte[] <=> String conversions ...
 * @author bourgesl
 */
public class TamFitsUtilsTest {
    
    private TamFitsUtilsTest() {
    }

    // --- TEST CODE -------------------------------------------------------------
    public static void main(String[] args) {
        
        FitsFactory.setCheckAsciiStrings(true);
        
        String in = "\t\n\rABCDEFGH\u0394\u03BB\u003f";
        byte[] out = AsciiFuncs.getBytes(in);
        System.out.println("getBytes('" + in + "') = " + dumpBytes(out));
        
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
