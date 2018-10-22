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
import java.util.Set;

/**
 * Specific parameters for WISARD
 */
public final class WisardInputParam extends SoftwareInputParam {

    public static final Set<String> SUPPORTED_STD_KEYWORDS = new HashSet<String>(Arrays.asList(new String[]{
        ImageOiConstants.KEYWORD_MAXITER,
        ImageOiConstants.KEYWORD_RGL_NAME
    }));

    public static final String KEYWORD_FOV = "FOV";
    public static final String KEYWORD_NP_MIN = "NP_MIN";
    public static final String KEYWORD_THRESHOLD = "THRESHOL";
    // optional
    public static final String KEYWORD_SCALE = "SCALE";
    public static final String KEYWORD_DELTA = "DELTA";

    // Wisard specific
    private static final KeywordMeta FOV = new KeywordMeta(KEYWORD_FOV,
            "Field of view (mas)", Types.TYPE_DBL);
    private static final KeywordMeta NP_MIN = new KeywordMeta(KEYWORD_NP_MIN,
            "MINimum width (Number of Points) of the reconstructed image", Types.TYPE_INT);
    private final static KeywordMeta THRESHOLD = new KeywordMeta(KEYWORD_THRESHOLD,
            "Convergence threshold to be used as a stopping criterion for the iterations", Types.TYPE_DBL);
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
    // Potential Conflict with ImageOiInputParam.KEYWORD_RGL_NAME ?
    public static final String[] RGL_NAME_WISARD = new String[]{"L1L2", "L1L2WHITE", "PSD", "SOFT_SUPPORT", "TOTVAR"};
    public static final String RGL_NAME_WISARD_L1 = "L1";

    public WisardInputParam() {
        super();
    }

    @Override
    public void update(final ImageOiInputParam params) {
        super.update(params);

        // define keywords:
        params.addKeyword(FOV);
        params.addKeyword(NP_MIN);
        params.addKeyword(THRESHOLD);

        // default values:
        params.setKeywordDefaultDouble(KEYWORD_FOV, 20.0);
        params.setKeywordDefaultInt(KEYWORD_NP_MIN, 32);
        params.setKeywordDefaultDouble(KEYWORD_THRESHOLD, 1E-6);

        // for our first implementation, just add to params if not TOTVAR
        if (params.getRglName().startsWith(RGL_NAME_WISARD_L1)) {
            params.addKeyword(SCALE);
            params.addKeyword(DELTA);

            params.setKeywordDefaultDouble(KEYWORD_SCALE, 0.0001);
            params.setKeywordDefaultDouble(KEYWORD_DELTA, 1);
        }
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
        if (ImageOiConstants.KEYWORD_INIT_IMG.equals(keywordName)) {
            return true;
        }
        return false;
    }

}
