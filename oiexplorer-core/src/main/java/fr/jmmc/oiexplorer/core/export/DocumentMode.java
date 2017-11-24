/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.export;

/**
 *This class is dedicated to inform the mode of exportation :
 * single export the document in a unique page/image using the global view
 * multi export the document in multi page/image with global view and each plot
 * default export the document with each plot (but no global view)
 @author grosje
 */
public enum DocumentMode {

    SINGLE_PAGE, MULTI_PAGE, DEFAULT;

    public static DocumentMode parse(final String value) {
        if ("multi".equals(value)) {
            return MULTI_PAGE;
        }
        if ("single".equals(value)) {
            return SINGLE_PAGE;
        }
        if ("default".equals(value)) {
            return DEFAULT;
        }
        throw new IllegalArgumentException("Invalid mode: " + value);
    }

}
