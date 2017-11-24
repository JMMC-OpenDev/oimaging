/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.meta;

import fr.jmmc.oitools.fits.FitsHDU;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.Rule;
import java.util.logging.Level;

/**
 * This class describes a FITS keyword.
 * 
 * Notes :
 * - OIFits uses only 'A', 'I', 'D' types for keywords => Other types are not supported for keywords.
 * - Keyword units are only useful for XML representation of an OIFits file (not defined in FITS)
 *
 * @author bourgesl
 */
public class KeywordMeta extends CellMeta {

    /**
     * KeywordMeta class constructor
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     */
    public KeywordMeta(final String name, final String desc, final Types dataType) {
        super(MetaType.KEYWORD, name, desc, dataType, false, NO_INT_VALUES, NO_STR_VALUES, Units.NO_UNIT);
    }

    /**
     * KeywordMeta class constructor
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     * @param unit keyword unit
     */
    public KeywordMeta(final String name, final String desc, final Types dataType, final Units unit) {
        super(MetaType.KEYWORD, name, desc, dataType, false, NO_INT_VALUES, NO_STR_VALUES, unit);
    }

    /**
     * KeywordMeta class constructor
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     * @param optional
     * @param acceptedValues
     */
    public KeywordMeta(final String name, final String desc, final Types dataType, final boolean optional, final String[] acceptedValues) {
        super(MetaType.KEYWORD, name, desc, dataType, optional, NO_INT_VALUES, acceptedValues, Units.NO_UNIT);
    }

    /**
     * KeywordMeta class constructor
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     * @param unit keyword unit
     * @param optional
     */
    public KeywordMeta(final String name, final String desc, final Types dataType, final Units unit, final boolean optional) {
        super(MetaType.KEYWORD, name, desc, dataType, optional, NO_INT_VALUES, NO_STR_VALUES, unit);
    }

    /**
     * KeywordMeta class constructor with integer possible values
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     * @param acceptedValues integer possible values
     */
    public KeywordMeta(final String name, final String desc, final Types dataType, final short[] acceptedValues) {
        super(MetaType.KEYWORD, name, desc, dataType, false, acceptedValues, NO_STR_VALUES, Units.NO_UNIT);
    }

    /**
     * KeywordMeta class constructor with string possible values
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     * @param acceptedValues string possible values
     */
    public KeywordMeta(final String name, final String desc, final Types dataType, final String[] acceptedValues) {
        super(MetaType.KEYWORD, name, desc, dataType, false, NO_INT_VALUES, acceptedValues, Units.NO_UNIT);
    }

    /**
     * Check if the given keyword value is valid.
     *
     * @param hdu
     * @param value keyword value to check
     * @param checker checker component
     */
    public final void check(final FitsHDU hdu, final Object value, final OIFitsChecker checker) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "check : {0} = {1}", new Object[]{getName(), value});
        }

        // Check type
        final Types kDataType = Types.getDataType(value.getClass());

        if (kDataType != this.getDataType() || OIFitsChecker.isInspectRules()) {
            // rule [GENERIC_KEYWORD_FORMAT] check if the keyword format matches the expected format (data type)
            checker.ruleFailed(Rule.GENERIC_KEYWORD_FORMAT, hdu, this.getName());
            checker.severe("Invalid format for keyword '" + this.getName() + "', found '" + kDataType.getRepresentation() + "' should be '" + this.getType() + "'");
        }

        // Check accepted value
        if (kDataType == this.getDataType() || OIFitsChecker.isInspectRules()) {
            checkAcceptedValues(hdu, value, checker);
        }
    }

    /**
     * If any are mentioned, check keyword values are fair.
     *
     * @param value keyword value to check
     * @param checker checker component
     */
    private void checkAcceptedValues(final FitsHDU hdu, final Object value, final OIFitsChecker checker) {
        final short[] intAcceptedValues = getIntAcceptedValues();
        final String[] stringAcceptedValues = getStringAcceptedValues();

        if (intAcceptedValues.length != 0) {
            final short val = ((Number) value).shortValue();

            if (!OIFitsChecker.isInspectRules()) {
                for (int i = 0, len = intAcceptedValues.length; i < len; i++) {
                    if (val == intAcceptedValues[i]) {
                        return;
                    }
                }
            }
            // rule [GENERIC_KEYWORD_VAL_ACCEPTED_INT] check if the keyword value matches the 'accepted' values (integer)
            checker.ruleFailed(Rule.GENERIC_KEYWORD_VAL_ACCEPTED_INT, hdu, this.getName());
            checker.severe("Invalid value for keyword '" + this.getName() + "', found '" + val + "' should be '" + getIntAcceptedValuesAsString() + "'");

        } else if (stringAcceptedValues.length != 0) {
            final String val = (String) value;

            if (!OIFitsChecker.isInspectRules()) {
                for (int i = 0, len = stringAcceptedValues.length; i < len; i++) {
                    if (val.equals(stringAcceptedValues[i])) {
                        return;
                    }
                }
            }
            // rule [GENERIC_KEYWORD_VAL_ACCEPTED_STR] check if the keyword value matches the 'accepted' values (string)
            checker.ruleFailed(Rule.GENERIC_KEYWORD_VAL_ACCEPTED_STR, hdu, this.getName());
            checker.severe("Invalid value for keyword '" + this.getName() + "', found '" + val + "' should be '" + getStringAcceptedValuesAsString() + "'");
        }
    }
}
