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

    private ImageOiInputParam inputParam = null;
    private ImageOiOutputParam outputParam = null;

    /** (optional) image HDU to store additional imageHDU TODO to export in OIFitsWriter */
    private final List<FitsImageHDU> optionalFitsImageHDUs = new LinkedList<FitsImageHDU>();

    //public ImageOiData(OIFitsFile parentOIFitsFile) {
    public ImageOiData() {
        //  this.parentOIFitsFile = parentOIFitsFile;
        inputParam = new ImageOiInputParam();
        outputParam = new ImageOiOutputParam();
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

    public ImageOiOutputParam getOutputParam() {
        return outputParam;
    }

    public void registerTables(List<FitsImageHDU> fitsImageHDUs) {
        getFitsImageHDUs().addAll(fitsImageHDUs);
        /*for (FitsImageHDU fitsImageHDU : fitsImageHDUs) {
            final String hduName = fitsImageHDU.getHduName();
            if (hduName.equals(ImageOiConstants.EXTNAME_IMAGE_OI_INPUT_PARAM)) {
                // we have to synchronize data
            }
            System.out.println("TODO: import table" + hduName);
        }*/
    }

}
