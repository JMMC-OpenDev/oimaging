/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services.software;

import fr.jmmc.oimaging.services.ServiceList;
import fr.jmmc.oitools.image.ImageOiConstants;
import fr.jmmc.oitools.image.ImageOiInputParam;
import java.util.List;

/**
 *
 */
public class SoftwareInputParam {

    private static final String[] RGL_NAME_DEFAULT = new String[]{""};

    /**
     * Factory pattern
     * @param name Service name
     * @return new SoftwareInputParam instance
     */
    public static SoftwareInputParam newInstance(final String name) {
        if (name.startsWith(ServiceList.SERVICE_BSMEM)) {
            return new BsmemInputParam();
        }
        if (name.startsWith(ServiceList.SERVICE_MIRA)) {
            return new MiraInputParam();
        }
        if (name.startsWith(ServiceList.SERVICE_SPARCO)) {
            return new SparcoInputParam();
        }
        if (name.startsWith(ServiceList.SERVICE_WISARD)) {
            return new WisardInputParam();
        }
        return new SoftwareInputParam();
    }

    SoftwareInputParam() {
        super();
    }

    public void update(final ImageOiInputParam params, final boolean applyDefaults) {
        // reset initial keywords:
        params.resetDefaultKeywords();

        // check that RGL_NAME is compliant.
        final String rglName = params.getRglName();

        if (rglName == null || !isSupported(getSupported_RGL_NAME(), rglName)) {
            // use first as default one if null or not included in the supported values
            params.setRglName(getSupported_RGL_NAME()[0]);
        }

        if (applyDefaults) {
            params.setFluxErr(0.01);
        }
    }

    public void validate(final ImageOiInputParam params, final List<String> failures) {
        // no-op
    }

    public String getDefaultCliOptions() {
        return null;
    }

    public boolean supportsStandardKeyword(final String keywordName) {
        return false;
    }

    public boolean supportsMissingKeyword(final String keywordName) {
        return false;
    }

    public String[] getSupported_RGL_NAME() {
        return RGL_NAME_DEFAULT;
    }

    private static boolean isSupported(final String[] list, final String value) {
        for (String str : list) {
            if (str.trim().contains(value)) {
                return true;
            }
        }
        return false;
    }
}
