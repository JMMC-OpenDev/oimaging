/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services.software;

import fr.jmmc.oitools.image.ImageOiConstants;
import fr.jmmc.oitools.image.ImageOiInputParam;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Specific parameters for BSMEM
 */
public final class BsmemInputParam extends SoftwareInputParam {

    public static final Set<String> SUPPORTED_STD_KEYWORDS = new HashSet<String>(Arrays.asList(new String[]{
        ImageOiConstants.KEYWORD_MAXITER,
        ImageOiConstants.KEYWORD_RGL_NAME,
        ImageOiConstants.KEYWORD_AUTO_WGT,
        ImageOiConstants.KEYWORD_RGL_WGT,
        ImageOiConstants.KEYWORD_FLUX,
        ImageOiConstants.KEYWORD_FLUXERR,
        ImageOiConstants.KEYWORD_RGL_PRIO
    }));

    /** Parameters that can be missing. */
    public static final Set<String> SUPPORTED_MISSING_KEYWORDS = new HashSet<>(Arrays.asList(
            ImageOiConstants.KEYWORD_RGL_PRIO
    ));

    public static final String[] RGL_NAME_BSMEM = new String[]{"mem_prior"};

    BsmemInputParam() {
        super();
    }

    @Override
    public void update(final ImageOiInputParam params, final boolean applyDefaults) {
        super.update(params, applyDefaults);

        if (params.isAutoWgt()) {
            params.removeKeyword(ImageOiConstants.KEYWORD_RGL_WGT);
        }

        // default values:
        if (applyDefaults) {
            // specific default values for BSMEM:
            params.setAutoWgt(true);
            params.setRglWgt(0.0);
        }
    }

    @Override
    public void validate(final ImageOiInputParam params, final List<String> failures) {
        super.validate(params, failures);

        // custom validation rules:
    }

    @Override
    public String[] getSupported_RGL_NAME() {
        return RGL_NAME_BSMEM;
    }

    @Override
    public boolean supportsStandardKeyword(final String keywordName) {
        return SUPPORTED_STD_KEYWORDS.contains(keywordName);
    }

    @Override
    public boolean supportsMissingKeyword(final String keywordName) {
        return SUPPORTED_MISSING_KEYWORDS.contains(keywordName);
    }
}
