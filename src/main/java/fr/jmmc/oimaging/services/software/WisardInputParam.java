/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services.software;

import fr.jmmc.oitools.image.ImageOiInputParam;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;

/**
 * Specific parameters for WISARD
 */
public final class WisardInputParam extends SoftwareInputParam {

    public static final String KEYWORD_NP_MIN = "NP_MIN";
    public static final String KEYWORD_FOV = "FOV";
    public static final String KEYWORD_SCALE = "SCALE";
    public static final String KEYWORD_DELTA = "DELTA";
    private static final KeywordMeta NP_MIN = new KeywordMeta(KEYWORD_NP_MIN, "minimum number of reconstructed voxels", Types.TYPE_INT);
    private static final KeywordMeta FOV = new KeywordMeta(KEYWORD_FOV, "field of view", Types.TYPE_DBL);
    private static final KeywordMeta SCALE = new KeywordMeta(KEYWORD_SCALE, "TBD", Types.TYPE_DBL);
    private static final KeywordMeta DELTA = new KeywordMeta(KEYWORD_DELTA, "TBD", Types.TYPE_DBL);

    // Potential Conflict with ImageOiInputParam.KEYWORD_RGL_NAME ?
    public static final String[] RGL_NAME_WISARD = new String[]{"TOTVAR", "PSD", "L1L2", "L1L2WHITE", "SOFT_SUPPORT"};
    public static final String RGL_NAME_WISARD_L1 = "L1";

    public WisardInputParam() {
        super();
    }

    @Override
    public void update(final ImageOiInputParam params) {
        super.update(params);

        // define keywords:
        params.addKeyword(NP_MIN);
        params.addKeyword(FOV);

        // default values:
        params.setKeywordDefaultInt(KEYWORD_NP_MIN, 32);
        params.setKeywordDefaultDouble(KEYWORD_FOV, 20.0);

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

}
