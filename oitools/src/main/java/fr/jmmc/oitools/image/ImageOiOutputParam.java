/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

import fr.jmmc.oitools.model.Table;

/**
 * This class is a container for IMAGE-OI OUTPUT PARAM.
 * https://github.com/emmt/OI-Imaging-JRA
 * It is returned be processing software and included in IMAGE-OI compliant files.
 *
 * @author mellag
 */
public class ImageOiOutputParam extends Table {

    /* constants */
    /** Logger associated to meta model classes */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ImageOiOutputParam.class.getName());

    // Image parameters
    public ImageOiOutputParam() {
        setExtName(ImageOiConstants.EXTNAME_IMAGE_OI_INPUT_PARAM);
    }

    // TODO add standard keywords
}
