/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging;

import fr.jmmc.jmcs.util.DateUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author bourgesl
 */
public class TestDate {

    /** RegExp expression to match date (yyyy-MM-dd'T'HH:mm:ss) */
    private final static Pattern PATTERN_DATE = Pattern.compile("-\\d{4}-[01]\\d-[0123]\\dT\\d{2}:\\d{2}:\\d{2}");

    @Test
    public void testDates() throws Exception {
        System.out.println("testDates");

        final String base = "test";
        System.out.println("base: '" + base + "'");

        final String output = base + "-" + DateUtils.now().substring(0, 19);

        System.out.println("output: '" + output + "'");

        // remove date suffix
        final Matcher dateMatcher = PATTERN_DATE.matcher(output);

        // match first occurence:
        int dateStart = (dateMatcher.find()) ? dateMatcher.start() : -1;
        if (dateStart > 0) {
            final String result = output.substring(0, dateStart);
            System.out.println("result: '" + result + "'");

            Assert.assertEquals("Bad base name", base, result);
        } else {
            fail("no date match");
        }
    }

}
