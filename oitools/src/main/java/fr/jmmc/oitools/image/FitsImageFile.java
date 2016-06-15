/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents the data model of a Fits standard file containing one or multiple images.
 * @author bourgesl
 */
public final class FitsImageFile {
    /* constants */

    /** Logger associated to image classes */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FitsImageFile.class.getName());

    /* members */
    /** file name */
    private String fileName = null;
    /** absolute file path */
    private String absoluteFilePath = null;
    /** Storage of fits image HDU references */
    private final List<FitsImageHDU> fitsImageHDUs = new LinkedList<FitsImageHDU>();

    /**
     * Public constructor
     */
    public FitsImageFile() {
        super();
    }

    /**
     * Public constructor
     * @param absoluteFilePath absolute file path
     */
    public FitsImageFile(final String absoluteFilePath) {
        super();
        setAbsoluteFilePath(absoluteFilePath);
    }

    /**
     * Return the number of Fits image HDUs present in this FitsImageFile structure
     * @return number of Fits image HDUs
     */
    public int getImageHDUCount() {
        return this.fitsImageHDUs.size();
    }

    /**
     * Return the list of Fits image HDUs
     * @return list of Fits image HDUs
     */
    public List<FitsImageHDU> getFitsImageHDUs() {
        return this.fitsImageHDUs;
    }

    /** 
     * Return a short description of FitsImageFile content.
     * @return short description of FitsImageFile content
     */
    @Override
    public String toString() {
        return "FitsImageFile[" + getAbsoluteFilePath() + "](" + getImageHDUCount() + ")\n" + getFitsImageHDUs();
    }

    /*
     * Getter - Setter -----------------------------------------------------------
     */
    /**
     * Get the name of this FitsImageFile file.
     *  @return a string containing the name of the FitsImageFile file.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Return the absolute file path
     * @return absolute file path or null if the file does not exist
     */
    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    /**
     * Define the absolute file path
     * @param absoluteFilePath absolute file path
     */
    public void setAbsoluteFilePath(final String absoluteFilePath) {
        this.absoluteFilePath = absoluteFilePath;
        if (absoluteFilePath != null && absoluteFilePath.length() > 0) {
            final int pos = absoluteFilePath.lastIndexOf(File.separatorChar);
            if (pos != -1) {
                this.fileName = absoluteFilePath.substring(pos + 1);
            }
        }
    }    
}
