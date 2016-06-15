/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

import java.util.LinkedList;
import java.util.List;

/**
 * Container of usefull attribute to manage IMAGE-OI process (image data, params)
 * @author mellag
 */
public class ImageOiData {

    //OIFitsFile parentOIFitsFile = null;
    ImageOiInputParam inputParam = null;
    FitsImageHDU inputFitsImageHDU = null;

    /** (optional) image HDU to store additional imageHDU TODO to export in OIFitsWriter */
    private final List<FitsImageHDU> optionalFitsImageHDUs = new LinkedList<FitsImageHDU>();

    //public ImageOiData(OIFitsFile parentOIFitsFile) {
    public ImageOiData() {
        //  this.parentOIFitsFile = parentOIFitsFile;
        inputParam = new ImageOiInputParam();
    }

    /**
     * Return the list of Fits image HDUs
     * @return list of Fits image HDUs
     */
    public List<FitsImageHDU> getFitsImageHDUs() {
        return this.optionalFitsImageHDUs;
    }

    public ImageOiInputParam getInputParam() {
        return inputParam;
    }

}
