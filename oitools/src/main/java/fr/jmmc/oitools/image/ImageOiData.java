/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

import fr.jmmc.oitools.model.OIFitsFile;

/**
 * Container of usefull attribute to manage IMAGE-OI process (image data, params)
 * @author mellag
 */
public final class ImageOiData {

    /* members */
    /** Main OIFitsFile */
    private final OIFitsFile oifitsFile;

    private final ImageOiInputParam inputParam = new ImageOiInputParam();
    private final ImageOiOutputParam outputParam = new ImageOiOutputParam();

    public ImageOiData(final OIFitsFile oifitsFile) {
        super();
        this.oifitsFile = oifitsFile;
    }

    /**
     * Return the list of Fits image HDUs
     * @return list of Fits image HDUs
     */
    public ImageOiInputParam getInputParam() {
        return inputParam;
    }

    public ImageOiOutputParam getOutputParam() {
        return outputParam;
    }

}
