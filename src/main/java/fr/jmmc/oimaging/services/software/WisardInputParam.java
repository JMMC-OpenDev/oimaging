/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services.software;

import fr.jmmc.oitools.image.ImageOiConstants;
import static fr.jmmc.oitools.image.ImageOiConstants.KEYWORD_RGL_WGT;
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
    public static final String KEYWORD_DELTA = "DELTA";

    // Wisard specific
    // optional
    private static final KeywordMeta SCALE = new KeywordMeta(KEYWORD_SCALE,
            "Scalar factor for L1-L2 regularization", Types.TYPE_DBL);
    private static final KeywordMeta DELTA = new KeywordMeta(KEYWORD_DELTA,
            "Scalar factor for L1-L2 regularization, used to set the threshold between quadratic (l2) and linear (L1) regularizations", Types.TYPE_DBL);

    // TODO:
    // - L1L2WHITE/PSD: add MEAN_OBJECT KW: An image, rescaled NP_MINxNP_MIN as INIT_IMG. (NULL)
    // - PSD: add PSD KW: An image, rescaled NP_MINxNP_MIN as INIT_IMG. (NULL)
    // - SOFT_SUPPORT: add MU_SUPPORT (0.5)
    // either:
    //     - MEAN_OBJECT : An image, rescaled NP_MINxNP_MIN as INIT_IMG. (NULL)
    //     - FWHM : Full Width Half maximum of a lorentzian used as prior, in pixels (3.) (less prefered as costly)
    public static final String[] RGL_NAME_WISARD = new String[]{"L1L2", "L1L2WHITE", "PSD", "SOFT_SUPPORT", "TOTVAR"};
    private static final String PREFIX_RGL_NAME_WISARD_L1 = "L1";

    WisardInputParam() {
        super();
    }

    @Override
    public void update(final ImageOiInputParam params, final boolean applyDefaults) {
        super.update(params, applyDefaults);

        // for our first implementation, just add to params if not TOTVAR
        if (params.getRglName().startsWith(PREFIX_RGL_NAME_WISARD_L1)) {
            params.addKeyword(SCALE);
            params.addKeyword(DELTA);

            params.setKeywordDefaultDouble(KEYWORD_SCALE, 0.0001);
            params.setKeywordDefaultDouble(KEYWORD_DELTA, 1);
        }

        // default values:
        if (applyDefaults) {
            // specific default values for WISARD:
            params.setRglWgt(1E-4);
        }
        
        // change table default:
        if (params.getRglWgt() == 0.0) {
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
