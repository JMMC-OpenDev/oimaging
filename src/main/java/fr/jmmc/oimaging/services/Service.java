/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services;

import fr.jmmc.oitools.image.ImageOiInputParam;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.model.Table;

/**
 * Details service as a combinaison of program handled by a dedicated execution
 * mode, with other metadata attributes.
 * @author mellag
 */
// Could be replaced by a jaxb generated class that could load input from xml
// ...and completed at runtime by a remote capability discovery ...
public class Service {

    final String name;
    final String program;
    final OImagingExecutionMode execMode;
    final String description;
    final String contact;

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

    final String[] default_RGL_NAME = new String[]{"mem_prior"};
    //final String[] wisard_RGL_NAME = new String[]{"TOTVAR", "PSD", "L1L2", "L1L2WHITE", "SOFT_SUPPORT"};
    final String[] wisard_RGL_NAME = new String[]{"TOTVAR", "L1L2", "L1L2WHITE"};

    public String[] getSupported_RGL_NAME() {
        if (name.startsWith("WISARD")) {
            return wisard_RGL_NAME;
        } else {
            return default_RGL_NAME;
        }
    }

    final WisardInputParam wisard_params = new WisardInputParam();

    public Table initSpecificParams(ImageOiInputParam params) {
        final String rglName = params.getRglName();
        // check that RGL_NAME is compliant.

        if (rglName == null || !isSupported(getSupported_RGL_NAME(), rglName)) {
            // use first as default one if null or not included in the supported values
            params.setRglName(getSupported_RGL_NAME()[0]);
        }

        if (name.startsWith("WISARD")) {
            wisard_params.update(params);
            return wisard_params;
        } else {
            params.addSubTable(null); // clean old specific params
            return null;
        }
    }

    private boolean isSupported(String[] list, String value) {
        for (String str : list) {
            if (str.trim().contains(value)) {
                return true;
            }
        }
        return false;
    }

    private static class WisardInputParam extends Table {

        public final static String KEYWORD_NP_MIN = "NP_MIN";
        private KeywordMeta NP_MIN = new KeywordMeta(KEYWORD_NP_MIN, "minimum number of reconstructed voxels", Types.TYPE_INT);

        public final static String KEYWORD_FOV = "FOV";
        private KeywordMeta FOV = new KeywordMeta(KEYWORD_FOV, "field of view", Types.TYPE_DBL);

        public final static String KEYWORD_SCALE = "SCALE";
        private KeywordMeta SCALE = new KeywordMeta(KEYWORD_SCALE, "TBD", Types.TYPE_DBL);

        public final static String KEYWORD_DELTA = "DELTA";
        private KeywordMeta DELTA = new KeywordMeta(KEYWORD_DELTA, "TBD", Types.TYPE_DBL);
        private Object oldScale;
        private Object oldDelta;

        public WisardInputParam() {
            addKeywordMeta(NP_MIN);
            setKeywordInt(KEYWORD_NP_MIN, 32);
            addKeywordMeta(FOV);
            setKeywordInt(KEYWORD_FOV, 20);

            addKeywordMeta(SCALE);
            setKeywordDouble(KEYWORD_SCALE, .0001);
            addKeywordMeta(DELTA);
            setKeywordDouble(KEYWORD_DELTA, 1);
        }

        public void update(ImageOiInputParam params) {
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
