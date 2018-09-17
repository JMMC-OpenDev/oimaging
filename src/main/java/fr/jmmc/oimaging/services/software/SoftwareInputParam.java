/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services.software;

import fr.jmmc.oimaging.services.ServiceList;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.image.ImageOiInputParam;

/**
 *
 */
// TODO: remove dependency on FitsTable
public class SoftwareInputParam extends FitsTable {

    /**
     * Factory pattern
     * @param name Service name
     * @return new SoftwareInputParam instance
     */
    public static SoftwareInputParam newInstance(final String name) {
        if (name.startsWith(ServiceList.SERVICE_WISARD)) {
            return new WisardInputParam();
        }
        return new SoftwareInputParam();
    }

    public SoftwareInputParam() {
        super();
    }

    public void update(final ImageOiInputParam params) {
        // check that RGL_NAME is compliant.
        final String rglName = params.getRglName();

        if (rglName == null || !isSupported(getSupported_RGL_NAME(), rglName)) {
            System.out.println("RESET");
            // use first as default one if null or not included in the supported values
            params.setRglName(getSupported_RGL_NAME()[0]);
        }
    }

    // Potential Conflict with ImageOiInputParam.KEYWORD_RGL_NAME ?
    public static final String[] RGL_NAME_DEFAULT = new String[]{"mem_prior"};

    public String[] getSupported_RGL_NAME() {
        return RGL_NAME_DEFAULT;
    }

    protected static boolean isSupported(final String[] list, final String value) {
        for (String str : list) {
            if (str.trim().contains(value)) {
                return true;
            }
        }
        return false;
    }
}
