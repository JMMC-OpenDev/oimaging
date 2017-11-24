/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

import fr.jmmc.oitools.fits.FitsTable;

/**
 * This class is a container for IMAGE-OI OUTPUT PARAM.
 * https://github.com/emmt/OI-Imaging-JRA
 * It is returned be processing software and included in IMAGE-OI compliant files.
 *
 * @author mellag
 */
public final class ImageOiOutputParam extends FitsTable {

    // Image parameters
    public ImageOiOutputParam() {
        super();

        // TODO add standard keywords
        // Set default values
        setExtName(ImageOiConstants.EXTNAME_IMAGE_OI_OUTPUT_PARAM);
    }

}
