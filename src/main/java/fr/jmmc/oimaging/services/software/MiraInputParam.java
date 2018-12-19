/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services.software;

import java.util.List;

import fr.jmmc.oitools.image.ImageOiConstants;
import fr.jmmc.oitools.image.ImageOiInputParam;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Specific parameters for MiRA
 */
public final class MiraInputParam extends SoftwareInputParam {

    public static final Set<String> SUPPORTED_STD_KEYWORDS = new HashSet<String>(Arrays.asList(new String[]{
        ImageOiConstants.KEYWORD_MAXITER, // -maxiter
        ImageOiConstants.KEYWORD_RGL_NAME, // -regul
        ImageOiConstants.KEYWORD_RGL_WGT, // -mu
        ImageOiConstants.KEYWORD_FLUX,
        ImageOiConstants.KEYWORD_FLUXERR
    }));

    // public static final  String[] KEYWORD_XFORM = new String[]{"nfft",  "separable", "nonseparable"};
    public static final String[] KEYWORD_SMEAR_LIST = new String[]{"none", "sinc", "gauss"};
    public static final String KEYWORD_SMEAR_FN = "SMEAR_FN";
    public static final String KEYWORD_SMEAR_FC = "SMEAR_FC";
    // optional
    public static final String KEYWORD_RGL_TAU = "RGL_TAU"; // - tau
    public static final String KEYWORD_RGL_GAMM = "RGL_GAMM"; // -gamma

    // optional
    private static final KeywordMeta RGL_TAU = new KeywordMeta(KEYWORD_RGL_TAU,
            "Scalar factor for hyperbolic L1-L2 regularization, used to set the threshold between quadratic (l2) and linear (L1) regularizations", Types.TYPE_DBL);
    private static final KeywordMeta RGL_GAMM = new KeywordMeta(KEYWORD_RGL_GAMM,
            "A priori full half width at half maximum for compactness", Types.TYPE_DBL);

    private static final KeywordMeta SMEAR_FN = new KeywordMeta(KEYWORD_SMEAR_FN,
            "Smearing function", Types.TYPE_CHAR, KEYWORD_SMEAR_LIST);
    private static final KeywordMeta SMEAR_FC = new KeywordMeta(KEYWORD_SMEAR_FC,
            "Smearing factor", Types.TYPE_DBL);

    // Potential Conflict with ImageOiInputParam.KEYWORD_RGL_NAME ?
    public static final String[] RGL_NAME_MIRA = new String[]{"hyperbolic", "compactness"};

    public MiraInputParam() {
        super();
    }

    @Override
    public void update(final ImageOiInputParam params) {
        super.update(params);

        // define keywords:
        params.addKeyword(SMEAR_FN);
        params.addKeyword(SMEAR_FC);

        // default values:
        params.setKeywordDefaultDouble(KEYWORD_SMEAR_FC, 1.0);

        // for our first implementation, just add to params if not TOTVAR
        if (params.getRglName().startsWith("hyperbolic")) {
            params.addKeyword(RGL_TAU);
            params.setKeywordDefaultDouble(KEYWORD_RGL_TAU, 1.0);
        } else if (params.getRglName().startsWith("compactness")) {
            params.addKeyword(RGL_GAMM);
            params.setKeywordDefaultDouble(KEYWORD_RGL_GAMM, 5.0);
        }

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
    public boolean supportsStandardKeyword(final String keywordName) {
        return SUPPORTED_STD_KEYWORDS.contains(keywordName);
    }
}
