/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.meta;

/**
 * In OIFITS V2, the physical units for few columns are 'user-defined' given in the column description
 * WARNING: CustomUnits INSTANCES MUST NEVER BE SHARED among keyword / columns !
 *
 * @author kempsc
 */
public final class CustomUnits extends Units {

    /**
     * Public constructor
     * WARNING: CustomUnits INSTANCES MUST NEVER BE SHARED among keyword / columns !
     */
    public CustomUnits() {
        super("UNIT_CUSTOM", "");
    }

    /**
     * Define the value of unit
     * @param unit unit in the file
     */
    public void setRepresentation(final String unit) {
        set(unit);
    }

}
