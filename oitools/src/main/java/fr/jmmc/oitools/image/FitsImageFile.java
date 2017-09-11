/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

import fr.jmmc.oitools.model.ModelBase;
import fr.jmmc.oitools.model.ModelVisitor;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents the data model of a Fits standard file containing one or multiple images.
 * @author bourgesl
 */
public class FitsImageFile extends ModelBase {

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
    public final int getImageHDUCount() {
        return this.fitsImageHDUs.size();
    }

    /**
     * Return the list of Fits image HDUs
     * @return list of Fits image HDUs
     */
    public final List<FitsImageHDU> getFitsImageHDUs() {
        return this.fitsImageHDUs;
    }

    /**
     * Get the primary ImageHDU if defined.
     * @return the primary image HDU or null
     */
    public final FitsImageHDU getPrimaryImageHDU() {
        return this.getFitsImageHDUs().isEmpty() ? null : getFitsImageHDUs().get(0);
    }

    /**
     * Define the primary image HDU.
     * @param imageHdu image HDU 
     */
    public final void setPrimaryImageHdu(final FitsImageHDU imageHdu) {
        if (this.getFitsImageHDUs().isEmpty()) {
            getFitsImageHDUs().add(imageHdu);
        } else {
            getFitsImageHDUs().remove(imageHdu);
            getFitsImageHDUs().add(0, imageHdu);
        }
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
    public final String getFileName() {
        return fileName;
    }

    /**
     * Return the absolute file path
     * @return absolute file path or null if the file does not exist
     */
    public final String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    /**
     * Define the absolute file path
     * @param absoluteFilePath absolute file path
     */
    public final void setAbsoluteFilePath(final String absoluteFilePath) {
        this.absoluteFilePath = absoluteFilePath;
        this.fileName = absoluteFilePath;
        if (absoluteFilePath != null && !absoluteFilePath.isEmpty()) {
            final int pos = absoluteFilePath.lastIndexOf(File.separatorChar);
            if (pos != -1) {
                this.fileName = absoluteFilePath.substring(pos + 1);
            }
        }
    }

    /**
     * Implements the Visitor pattern
     * @param visitor visitor implementation
     */
    @Override
    public void accept(final ModelVisitor visitor) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
