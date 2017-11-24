/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.meta.OIFitsStandard;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author kempsc
 */
public enum Rule {

    ARRNAME_UNIQ("check if a single OI_ARRAY table corresponds to the ARRNAME keyword",
            "V2.5.2§1"
    ),
    COL_UNKNOWN("check if the column belongs to the OIFITS standard and version",
            Const.JMMC
    ),
    CORRNAME_REF("check if an OI_CORR table matches the CORRNAME keyword",
            "V2.6.1§3"
    ),
    CORRNAME_UNIQ("check if a single OI_CORR table corresponds to the CORRNAME keyword",
            "V2.7.2§4"
    ),
    FILE_EXIST("check if the file exist",
            Const.JMMC
    ),
    FILE_LOAD("check if the OIFITS file is loaded properly (IO error)",
            Const.JMMC
    ),
    INSNAME_REF("check if an OI_WAVELENGTH table matches the INSNAME keyword",
            "V2.6.1§3"
    ),
    INSNAME_UNIQ("check if a single OI_WAVELENGTH table corresponds to the INSNAME keyword",
            "V1.6.3.1"
    ),
    GENERIC_ARRNAME_REF("check if an OI_ARRAY table matches the ARRNAME keyword",
            "V2.6.1§3"
    ),
    GENERIC_COL_DIM("check if the dimension of column values >= 1",
            Const.JMMC
    ),
    GENERIC_COL_ERR("check if the UNFLAGGED *ERR column values are valid (positive or NULL)",
            Const.JMMC
    ),
    GENERIC_COL_FORMAT("check if the column format matches the expected format (data type and dimensions)",
            "V2.4§1"
    ),
    GENERIC_COL_MANDATORY("check if the required column is present",
            "V1-V2.Tables"
    ),
    GENERIC_COL_NBROWS("check if the column length matches the expected number of rows",
            Const.JMMC
    ),
    GENERIC_COL_UNIT("check if the column unit matches the expected unit",
            "V2.4§2"
    ),
    GENERIC_COL_UNIT_EXIST("check if the column unit exists",
            "V2.4§2"
    ),
    GENERIC_COL_VAL_ACCEPTED_INT("check if column values match the 'accepted' values (integer)",
            "V1-V2.Tables"
    ),
    GENERIC_COL_VAL_ACCEPTED_STR("check if column values match the 'accepted' values (string)",
            "V1-V2.Tables"
    ),
    GENERIC_COL_VAL_POSITIVE("check if column values are finite and positive",
            Const.JMMC
    ),
    GENERIC_CORRINDX_MIN("check if the CORRINDX values >= 1",
            "V2.7.2§4"
    ),
    GENERIC_CORRINDX_MAX("check if the CORRINDX values <= NDATA",
            "V2.7.2§4"
    ),
    GENERIC_CORRINDX_UNIQ("check duplicates or overlaps within correlation indexes (CORRINDX)",
            "V2.7.2§4"
    ),
    GENERIC_DATE_OBS_RANGE("check if the DATE_OBS value is within 'normal' range (1933 - 2150)",
            "V2.6.1§1"
    ),
    GENERIC_DATE_OBS_STANDARD("check if the DATE_OBS keyword is in the format 'YYYY-MM-DD'",
            "V2.6.1§1"
    ),
    GENERIC_KEYWORD_FORMAT("check if the keyword format matches the expected format (data type)",
            "V2.4§2"
    ),
    GENERIC_KEYWORD_MANDATORY("check if the required keyword is present",
            "V1-V2.Tables"
    ),
    GENERIC_KEYWORD_VAL_ACCEPTED_INT("check if the keyword value matches the 'accepted' values (integer)",
            "V1-V2.Tables"
    ),
    GENERIC_KEYWORD_VAL_ACCEPTED_STR("check if the keyword value matches the 'accepted' values (string)",
            "V1-V2.Tables"
    ),
    GENERIC_MJD_RANGE("check if the MJD value is within 'normal' range (1933 - 2150)",
            "V2.6.1§1"
    ),
    GENERIC_OIREV_FIX("Fix the OI_REV keyword when the table is not in the proper OIFITS version",
            Const.JMMC
    ),
    GENERIC_STA_INDEX_UNIQ("check duplicated indexes inside each STA_INDEX column values (data table)",
            "V1.6.1.4"
    ),
    OI_ARRAY_ARRNAME("check the ARRNAME keyword has a not null or empty value",
            "V2.5.2§1"
    ),
    OI_ARRAY_EXIST_V2("check if at least one OI_ARRAY table exists in the OIFITS 2 file",
            "V2.4.2§1"
    ),
    OI_ARRAY_STA_NAME("check if the STA_NAME column values have a not null or empty value",
            Const.JMMC
    ),
    OI_ARRAY_STA_NAME_UNIQ("check duplicated values in the STA_NAME column of the OI_ARRAY table",
            Const.JMMC
    ),
    OI_ARRAY_STA_INDEX_MIN("check if the STA_INDEX values >= 1",
            "V1.6.1.4"
    ),
    OI_ARRAY_STA_INDEX_UNIQ("check duplicated indexes in the STA_INDEX column of the OI_ARRAY table",
            "V1.6.1.4"
    ),
    OI_ARRAY_XYZ("check if the ARRAY_XYZ keyword values corresponds to a proper coordinate on earth",
            "V1.6.1.3"
    ),
    OI_ARRAY_XYZ_FIX("fix the ARRAY_XYZ keyword values (to VLTI or CHARA according to the ARRNAME keyword) when the ARRAY_XYZ keyword values are incorrect",
            "V1.6.1.3"
    ),
    OI_CORR_CORRNAME("check the CORRNAME keyword has a not null or empty value",
            Const.JMMC
    ),
    OI_CORR_IINDEX_MIN("check if the IINDEX values >= 1 (JINDEX >= 2)",
            "V2.OI_CORR_Table"
    ),
    OI_CORR_JINDEX_SUP("check if the JINDEX values > IINDEX values",
            "V2.OI_CORR_Table"
    ),
    OI_CORR_IJINDEX_MAX("check if the IINDEX values <= NDATA and JINDEX values <= NDATA",
            "V2.OI_CORR_Table"
    ),
    OIFITS_OIDATA("check if at least one data table exists in the OIFITS file",
            "V2.4.2§1"
    ),
    OI_FLUX_CORRINDX("check if the referenced OI_CORR table exists when the column CORRINDX_FLUXDATA is present",
            "V2.7.2§4"
    ),
    OI_INSPOL_INSNAME_UNIQ("TODO: check if the INSNAME column values are only present in a single OI_INSPOL table (compare multi OI_INSPOL table)",
            "V2.7.3§2"
    ),
    OI_INSPOL_MJD_RANGE("check if MJD values in data tables are within MJD intervals (MJD_OBS and MJD_END columns) of the referenced OI_INSPOL table",
            "V2.6.1§3"
    ),
    OI_T3_CORRINDX("check if the referenced OI_CORR exists when the column CORRINDX_T3AMP or CORRINDX_T3PHI is present",
            "V2.7.2§4"
    ),
    OI_TARGET_COORD("check if the TARGET RA and DEC values are not 0.0",
            Const.JMMC
    ),
    OI_TARGET_COORD_EXIST("check if the TARGET RA or DEC value is not undefined",
            Const.JMMC
    ),
    OI_TARGET_EXIST("check if only one OI_TARGET table exists in the OIFITS file",
            "V2.4.2§1"
    ),
    OI_TARGET_TARGET("check if the TARGET column values have a not null or empty value",
            Const.JMMC
    ),
    OI_TARGET_TARGET_UNIQ("check duplicated values in the TARGET column of the OI_TARGET table",
            Const.JMMC
    ),
    OI_TARGET_TARGETID_MIN("check if the TARGET_ID values >= 1",
            "V2.OI_TARGET_Table"
    ),
    OI_TARGET_TARGETID_UNIQ("check duplicated indexes in the TARGET_ID column of the OI_TARGET table",
            Const.JMMC
    ),
    OI_VIS_CORRINDX("check if the referenced OI_CORR table exists when the column CORRINDX_VISAMP, CORRINDX_VISPHI, CORRINDX_RVIS or CORRINDX_IVIS is present",
            "V2.7.2§4"
    ),
    OI_VIS2_CORRINDX("check if the referenced OI_CORR table exists when the column CORRINDX_VIS2DATA is present",
            "V2.7.2§4"
    ),
    OI_WAVELENGTH_EFF_WAVE("check the EFF_WAVE column values are within range [0.1E-6 ... 20.0E-6]",
            Const.JMMC
    ),
    OI_WAVELENGTH_EXIST("check if at least one OI_WAVELENGTH table exists in the OIFITS file",
            "V2.4.2§1"
    ),
    OI_WAVELENGTH_INSNAME("check the INSNAME keyword has a not null or empty value",
            "V2.5.3§3"
    ),
    PRIMARYHDU_EXIST_V2("check if the main header (PRIMARY HDU) exists in the OIFITS 2 file",
            "V2.4.1§3"
    ),
    PRIMARYHDU_MULTI_TARGET("check if main header keywords are set to 'MULTI' for heterogenous content",
            "V2.MAIN_HEADER_Table(3)"
    ),
    PRIMARYHDU_TYPE_ATOMIC("check if supplementary keywords are present, when OIFITS contains only one target observed on a single interferometer",
            "V2.MAIN_HEADER_Table(3)"
    ),
    TABLE_NOT_OIFITS2(
            "check if any OIFITS 2 specific table (OI_CORR, OI_INSPOL or OI_FLUX) is present in the OIFITS 1 file",
            Const.JMMC
    ),
    TABLE_UNKNOWN("check if the table belongs to the OIFITS standard and version",
            "V2.4.2§3-4"
    ),
    TARGET_EXIST("check if the OI_TARGET table have at least one target",
            Const.JMMC
    );

    // members:
    private final String description;
    private final String paragraph;
    // TODO: replace String by ModelSource[String struct, String member]
    private final Set<String> applyToSet = (OIFitsChecker.isInspectRules()) ? new HashSet<String>() : null;
    private final Set<OIFitsStandard> standardSet = (OIFitsChecker.isInspectRules()) ? new TreeSet<OIFitsStandard>() : null;

    public String getDescription() {
        return description;
    }

    public String getParagraph() {
        return paragraph;
    }

    public Set<String> getApplyTo() {
        return applyToSet;
    }

    void addApplyTo(String v) {
        applyToSet.add(v);
    }

    public Set<OIFitsStandard> getStandard() {
        return standardSet;
    }

    void addStandard(OIFitsStandard v) {
        standardSet.add(v);
    }

    /**
     * Constructor, use it when we have expression or for Apply To
    @param description
    @param expression
     */
    private Rule(final String description, final String paragraph) {
        this.description = description;
        this.paragraph = paragraph;
    }

    @Override
    public String toString() {
        return "Rule{name=\"" + name() + "\", description=\"" + description + "\", paragraph=\"" + paragraph + "\", applyTo=\"" + applyToSet + "\", standardSet=\"" + standardSet + "\"}";
    }

    public static Comparator<Rule> getComparatorByName() {
        return Const.CMP_NAME;
    }

    private static final class Const {

        public final static String JMMC = "JMMC";
        public final static String UNDEFINED = "UNDEFINED";

        static Comparator<Rule> CMP_NAME = new Comparator<Rule>() {
            @Override
            public int compare(final Rule r1, final Rule r2) {
                return r1.name().compareTo(r2.name());
            }

        };

        private Const() {
            // no-op
        }
    }

    static {
        /*
         * Add missing applyTo() in rules
         */
        if (OIFitsChecker.isInspectRules()) {
            Rule rule = Rule.FILE_EXIST;
            rule.addApplyTo("FILE");
            rule.addStandard(OIFitsStandard.VERSION_1);
            rule.addStandard(OIFitsStandard.VERSION_2);

            rule = Rule.FILE_LOAD;
            rule.addApplyTo("FILE");
            rule.addStandard(OIFitsStandard.VERSION_1);
            rule.addStandard(OIFitsStandard.VERSION_2);

            rule = Rule.COL_UNKNOWN;
            rule.addApplyTo("FILE");
            rule.addStandard(OIFitsStandard.VERSION_1);
            rule.addStandard(OIFitsStandard.VERSION_2);

            rule = Rule.OIFITS_OIDATA;
            rule.addApplyTo("FILE");
            rule.addStandard(OIFitsStandard.VERSION_1);
            rule.addStandard(OIFitsStandard.VERSION_2);
        }
    }

    public static void main(String[] unused) {
        System.out.println("Rules:");
        for (Rule r : Rule.values()) {
            System.out.println(r.toString());
        }
    }
}
