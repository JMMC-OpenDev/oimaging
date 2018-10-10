/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services.software;

import fr.jmmc.oitools.image.ImageOiConstants;
import fr.jmmc.oitools.image.ImageOiInputParam;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import java.util.List;

/**
 * Specific parameters for BSMEM
 */
public final class BsmemInputParam extends SoftwareInputParam {

    public final static String KEYWORD_AUTO_WGT = "AUTO_WGT";
    public final static String KEYWORD_FLUXERR = "FLUXERR";

    // Bsmem specific
    private final static KeywordMeta AUTO_WGT = new KeywordMeta(KEYWORD_AUTO_WGT,
            "Automatic regularization weight", Types.TYPE_LOGICAL);
    private final static KeywordMeta FLUXERR = new KeywordMeta(KEYWORD_FLUXERR,
            "Error on zero-baseline V^2 point", Types.TYPE_DBL); // like wisard

    // Potential Conflict with ImageOiInputParam.KEYWORD_RGL_NAME ?
    public static final String[] RGL_NAME_BSMEM = new String[]{"mem_prior"};

    public BsmemInputParam() {
        super();
    }

    @Override
    public void update(final ImageOiInputParam params) {
        super.update(params);

        // define keywords:
        params.addKeyword(AUTO_WGT);
        params.addKeyword(FLUXERR);

        // default values:
        params.setKeywordDefaultLogical(KEYWORD_AUTO_WGT, true);
        params.setKeywordDefaultDouble(KEYWORD_FLUXERR, 0.1);

        if (params.getKeywordLogical(KEYWORD_AUTO_WGT)) {
            params.removeKeyword(ImageOiConstants.KEYWORD_RGL_WGT);
        }
    }

    @Override
    public void validate(final ImageOiInputParam params, final List<String> failures) {
        // custom validation rules:
        final double fluxerr = params.getKeywordDouble(KEYWORD_FLUXERR);

        if (fluxerr < 1e-5) {
            failures.add("FluxErr must be greater than 1e-5");
        } else if (fluxerr > 1) {
            failures.add("FluxErr must be smaller than 1.0");
        }
    }

    @Override
    public String[] getSupported_RGL_NAME() {
        return RGL_NAME_BSMEM;
    }

    @Override
    public boolean supportsStandardKeyword(final String keywordName) {
        return true;
    }

}
