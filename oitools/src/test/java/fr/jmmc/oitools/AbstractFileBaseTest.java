/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.junit.Assert;

/**
 * Allows to manage all the .properties (the map) 
 * 
 * @author kempsc
 */
public abstract class AbstractFileBaseTest extends JUnitBaseTest {

    /** stores key / value pairs */
    private static Properties props = null;
    /** counter of all valid assert */
    private static int assertCount;

    protected static void initializeTest() {
        props = new Properties();
    }

    protected static void shutdownTest() {
        props = null;
    }

    protected static void reset() {
        props.clear();
        assertCount = 0;
    }

    protected static void checkAssertCount() {
        // TODO: sort properties into TreeMap
        // check that all keys in TreeMap are tested (store flag into usedSet)
        // log all untested keys !!

        Assert.assertEquals("checkAssertCount", props.size(), assertCount);
    }

    protected static void load(File propFile) throws IOException {
        logger.info("Loading properties: " + propFile.getAbsolutePath());

        // Load the properties:
        props.load(new FileInputStream(propFile));
    }

    protected static void save(File propFile) throws IOException {
        if (!props.isEmpty()) {
            logger.info("Saving properties: " + propFile.getAbsolutePath());

            // Save the properties:
            props.store(new FileOutputStream(propFile), "");
        }
    }

    protected static void assertEquals(Object expected, Object actual) {
        Assert.assertEquals((expected == null) ? "null" : expected, actual);
        assertCount++;
    }

    protected static void assertEqualsInt(Object expected, int actual) {
        Assert.assertEquals(expected, Integer.toString(actual));
        assertCount++;
    }

    protected static Object get(String key) {
        return props.get(key);
    }

    protected static void putInt(String key, int value) {
        put(key, Integer.toString(value));
    }

    protected static void put(String key, String value) {
        props.put(key, value);
    }

    protected static boolean contains(String key) {
        return props.containsKey(key);
    }
}
