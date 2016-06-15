/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.test;

import fr.jmmc.oitools.fits.FitsConstants;
import fr.jmmc.oitools.image.FitsImageConstants;
import fr.jmmc.oitools.image.FitsUnit;

/**
 * Test FitsUnit conversions
 * @author bourgesl
 */
public class FitsUnitTest {

    public static void main(String[] args) {
        test(90.0, "deg", FitsUnit.ANGLE_RAD, Math.PI / 2);
        // Hertz:
        test(1, "hz", FitsUnit.WAVELENGTH_METER, FitsConstants.C_LIGHT);
        test(1, "ghz", FitsUnit.WAVELENGTH_METER, 1e-9 * FitsConstants.C_LIGHT);
        test(1e14, "hz", FitsUnit.WAVELENGTH_METER, 1e-14 * FitsConstants.C_LIGHT);
    }

    private static void test(double value, final String unitString, final FitsUnit unitRef, final double expected) {
        final FitsUnit unit = FitsUnit.parseUnit(unitString);

        final double result = unit.convert(value, unitRef);

        if (Math.abs(result - expected) > 1e-6) {
            throw new IllegalStateException("Invalid result: " + result + " expected: " + expected);
        }
        System.out.println("Value[" + value + "] Unit[" + unit.getStandardRepresentation() + "] converted to Unit[" + unitRef.getStandardRepresentation() + "] = " + result);
    }
}
