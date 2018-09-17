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

    public WisardInputParam() {
        super();
        // define keywords:
        addKeywordMeta(NP_MIN);
        addKeywordMeta(FOV);
        addKeywordMeta(SCALE);
        addKeywordMeta(DELTA);
        // default values:
        setKeywordInt(KEYWORD_NP_MIN, 32);
        setKeywordDouble(KEYWORD_FOV, 20.0);
        setKeywordDouble(KEYWORD_SCALE, 0.0001);
        setKeywordDouble(KEYWORD_DELTA, 1);
    }

    public void update(final ImageOiInputParam params) {
        super.update(params);

        // for our first implementation, just add to params if not TOTVAR
        getKeywordsDesc().remove(KEYWORD_SCALE);
        getKeywordsDesc().remove(KEYWORD_DELTA);

        if (params.getRglName().startsWith("L1")) {
            addKeywordMeta(SCALE);
            addKeywordMeta(DELTA);
        }
        params.addSubTable(this);
    }

    // Potential Conflict with ImageOiInputParam.KEYWORD_RGL_NAME ?
    //final String[] wisard_RGL_NAME = new String[]{"TOTVAR", "PSD", "L1L2", "L1L2WHITE", "SOFT_SUPPORT"};
    public static final String[] RGL_NAME_WISARD = new String[]{"TOTVAR", "L1L2", "L1L2WHITE"};

    @Override
    public String[] getSupported_RGL_NAME() {
        return RGL_NAME_WISARD;
    }

}
