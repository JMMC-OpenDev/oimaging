/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services.software;

import fr.jmmc.oitools.image.ImageOiConstants;
import fr.jmmc.oitools.image.ImageOiInputParam;
import static fr.jmmc.oitools.meta.CellMeta.NO_STR_VALUES;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import java.util.List;

/**
 * Specific parameters for MiRA
 */
public final class MiraInputParam extends SoftwareInputParam {

    // Potential Conflict with ImageOiInputParam.KEYWORD_RGL_NAME ?
    public static final String[] RGL_NAME_MIRA = new String[]{""};

    public MiraInputParam() {
        super();
    }

    @Override
    public void update(final ImageOiInputParam params) {
        super.update(params);

        // TODO
    }

    @Override
    public void validate(final ImageOiInputParam params, final List<String> failures) {
        // custom validation rules:
        // TODO
    }

    @Override
    public String[] getSupported_RGL_NAME() {
        return RGL_NAME_MIRA;
    }

    @Override
    public boolean supportsStandardKeyword(final String name) {
        return false;
    }

}
