/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services.software;

import fr.jmmc.oitools.image.ImageOiConstants;
import fr.jmmc.oitools.image.ImageOiInputParam;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Specific parameters for WISARD
 */
public final class WisardInputParam extends SoftwareInputParam {

    public static final Set<String> SUPPORTED_STD_KEYWORDS = new HashSet<String>(Arrays.asList(
            ImageOiConstants.KEYWORD_MAXITER,
            ImageOiConstants.KEYWORD_RGL_NAME,
            ImageOiConstants.KEYWORD_RGL_WGT,
            ImageOiConstants.KEYWORD_FLUX,
            ImageOiConstants.KEYWORD_FLUXERR,
            ImageOiConstants.KEYWORD_RGL_PRIO
    ));

    /** Parameters that can be missing. */
    public static final Set<String> SUPPORTED_MISSING_KEYWORDS = new HashSet<>(Arrays.asList(
            ImageOiConstants.KEYWORD_INIT_IMG,
            ImageOiConstants.KEYWORD_RGL_PRIO
    ));

    // optional
    public static final String KEYWORD_SCALE = "SCALE";

    // Wisard specific
    // optional
    private static final KeywordMeta SCALE = new KeywordMeta(KEYWORD_SCALE,
            "Scalar factor for L1-L2 regularization", Types.TYPE_DBL);

    // TODO:
    // - L1L2WHITE: add MEAN_OBJECT KW: An image, rescaled NP_MINxNP_MIN as INIT_IMG. (NULL)
    // - SOFT_SUPPORT: add MU_SUPPORT (0.5)
    // either:
    //     - MEAN_OBJECT : An image, rescaled NP_MINxNP_MIN as INIT_IMG. (NULL)
    //     - FWHM : Full Width Half maximum of a lorentzian used as prior, in pixels (3.) (less prefered as costly)

    /** parameter value "L1L2WHITE" for RGL_NAME for WISARD */
    private static final String RLG_NAME_WISARD_L1L2WHITE = "L1L2WHITE";
    /** parameter value "SOFT_SUPPORT" for RGL_NAME for WISARD */
    private static final String RLG_NAME_WISARD_SOFT_SUPPORT = "SOFT_SUPPORT";

    public static final String[] RGL_NAME_WISARD = new String[]{
        RLG_NAME_WISARD_L1L2WHITE, RLG_NAME_WISARD_SOFT_SUPPORT};

    WisardInputParam() {
        super();
    }

    @Override
    public void update(final ImageOiInputParam params, final boolean applyDefaults) {
        super.update(params, applyDefaults);

        if (params.getRglName().equals(RLG_NAME_WISARD_L1L2WHITE)) {
            params.addKeyword(SCALE);

            params.setKeywordDefaultDouble(KEYWORD_SCALE, 0.0001);
        }

        // default values:
        if (applyDefaults) {
            // specific default values for WISARD:
            params.setRglWgt(1E-4);
        }
        
        // change table default:
        if (params.getRglWgt() == ImageOiInputParam.DEF_KEYWORD_RGL_WGT) {
            params.setRglWgt(1E-4);
        }
    }

    @Override
    public void validate(final ImageOiInputParam params, final List<String> failures) {
        super.validate(params, failures);

        // custom validation rules:
    }

    @Override
    public String[] getSupported_RGL_NAME() {
        return RGL_NAME_WISARD;
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
