/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services;

import fr.jmmc.oitools.image.ImageOiInputParam;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.fits.FitsTable;

/**
 * Details service as a combinaison of program handled by a dedicated execution
 * mode, with other metadata attributes.
 * @author mellag
 */
// Could be replaced by a jaxb generated class that could load input from xml
// ...and completed at runtime by a remote capability discovery ...
public final class Service {

    private final String name;
    private final String program;
    private final OImagingExecutionMode execMode;
    private final String description;
    private final String contact;

    public Service(final String name, final String program, final OImagingExecutionMode execMode, final String description, final String contact) {
        this.name = name;
        this.program = program;
        this.execMode = execMode;
        this.description = description;
        this.contact = contact;
    }

    public String getName() {
        return name;
    }

    public String getProgram() {
        return program;
    }

    public OImagingExecutionMode getExecMode() {
        return execMode;
    }

    public String getDescription() {
        return description;
    }

    public String getContact() {
        return contact;
    }

    public String toString() {
        return name;
    }

    // Potential Conflict with ImageOiInputParam.KEYWORD_RGL_NAME ?
    private static final String[] RGL_NAME_DEFAULT = new String[]{"mem_prior"};
    //final String[] wisard_RGL_NAME = new String[]{"TOTVAR", "PSD", "L1L2", "L1L2WHITE", "SOFT_SUPPORT"};
    private static final String[] RGL_NAME_WISARD = new String[]{"TOTVAR", "L1L2", "L1L2WHITE"};

    public String[] getSupported_RGL_NAME() {
        // TODO: generalise ?
        if (name.startsWith(ServiceList.SERVICE_WISARD)) {
            return RGL_NAME_WISARD;
        } else {
            return RGL_NAME_DEFAULT;
        }
    }

    // TODO: cleanup => use template instances
    final WisardInputParam wisard_params = new WisardInputParam();

    public FitsTable initSpecificParams(final ImageOiInputParam params) {
        // check that RGL_NAME is compliant.
        final String rglName = params.getRglName();

        if (rglName == null || !isSupported(getSupported_RGL_NAME(), rglName)) {
            // use first as default one if null or not included in the supported values
            params.setRglName(getSupported_RGL_NAME()[0]);
        }

        // TODO: generalise ?
        if (name.startsWith(ServiceList.SERVICE_WISARD)) {
            wisard_params.update(params);
            return wisard_params;
        } else {
            params.addSubTable(null); // clean old specific params
            return null;
        }
    }

    private static boolean isSupported(final String[] list, final String value) {
        for (String str : list) {
            if (str.trim().contains(value)) {
                return true;
            }
        }
        return false;
    }

    private static final class WisardInputParam extends FitsTable {

        public final static String KEYWORD_NP_MIN = "NP_MIN";
        public final static String KEYWORD_FOV = "FOV";
        public final static String KEYWORD_SCALE = "SCALE";
        public final static String KEYWORD_DELTA = "DELTA";

        private final static KeywordMeta NP_MIN = new KeywordMeta(KEYWORD_NP_MIN, "minimum number of reconstructed voxels", Types.TYPE_INT);

        private final static KeywordMeta FOV = new KeywordMeta(KEYWORD_FOV, "field of view", Types.TYPE_DBL);

        private final static KeywordMeta SCALE = new KeywordMeta(KEYWORD_SCALE, "TBD", Types.TYPE_DBL);

        private final static KeywordMeta DELTA = new KeywordMeta(KEYWORD_DELTA, "TBD", Types.TYPE_DBL);

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
            // TODO: cleanup all tha logic !!
            params.addSubTable(null);

            // for our first implementation, just add to params if not TOTVAR
            getKeywordsDesc().remove(KEYWORD_SCALE);
            getKeywordsDesc().remove(KEYWORD_DELTA);

            if (params.getRglName().startsWith("L1")) {
                addKeywordMeta(SCALE);
                addKeywordMeta(DELTA);
            }

            params.addSubTable(this);
        }
    }
}
