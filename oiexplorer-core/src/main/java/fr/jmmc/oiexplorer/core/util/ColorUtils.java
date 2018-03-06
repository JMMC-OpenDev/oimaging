/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.util;

import java.util.Arrays;

/**
 * Color utility methods to transform colors between color spaces (sRGB, linear RGB, XYZ, Lab, Lch)
 * @author bourgesl
 */
public class ColorUtils {
    private static boolean TRACE = false;
    
    
    static void blend_LCH(float[] c1, float[] c2, float ratio, float[] result) {

        if (TRACE) {
            System.out.println("c1: " + Arrays.toString(c1));
            System.out.println("c2: " + Arrays.toString(c2));
        }

        // c1 & c2 are Lab or LCH:
        result[0] = (c2[0] + ratio * (c1[0] - c2[0]));

        // a(Lab) or C(LCH):
        result[1] = (c2[1] + ratio * (c1[1] - c2[1]));

        // H(Lch) angle combination:
        float d = c1[2] - c2[2];
        if (d > 180f) {
            d -= 360f;
        } else if (d < -180f) {
            d += 360f;
        }
        result[2] = (c2[2] + ratio * d);

        // mix alpha ?
        result[3] = 1f;

        if (TRACE) {
            System.out.println("mixLCH: " + Arrays.toString(result));
        }
    }

    public static float[] sRGB_to_Lab(final int rgba, final float[] Lab) {
        return XYZ_to_Lab(sRGB_to_XYZ(sRGB_to_f(rgba, Lab)));
    }

    public static int Lab_to_sRGB(final float[] Lab) {
        return sRGB_to_i(XYZ_to_sRGB(Lab_to_XYZ(Lab)));
    }

    public static float[] sRGB_to_LCH(final int rgba, final float[] LCH) {
        return Lab_to_LCH(XYZ_to_Lab(sRGB_to_XYZ(sRGB_to_f(rgba, LCH))));
    }

    public static int LCH_to_sRGB(final float[] LCH) {
        return sRGB_to_i(XYZ_to_sRGB(Lab_to_XYZ(LCH_to_Lab(LCH))));
    }

    public static float[] Lab_to_LCH(float[] Lab) {
        if (TRACE) {
            System.out.println("Lab: " + Arrays.toString(Lab));
        }
        float H = (float) (Math.atan2(Lab[2], Lab[1]));

        if (H > 0f) {
            H = (float) ((H / Math.PI) * 180.0);
        } else {
            H = (float) (360.0 - (Math.abs(H) / Math.PI) * 180.0);
        }

        float L = Lab[0];
        // in [0..100]
        float C = (float) Math.sqrt(Lab[1] * Lab[1] + Lab[2] * Lab[2]) / 1.28f; 

        Lab[0] = L;
        Lab[1] = C;
        Lab[2] = H;
        if (TRACE) {
            System.out.println("Lch: " + Arrays.toString(Lab));
        }
        return Lab;
    }

    public static float[] LCH_to_Lab(float[] LCH) {
        if (TRACE) {
            System.out.println("LCH: " + Arrays.toString(LCH));
        }
        float L = LCH[0];
        final double angle = LCH[2] * (Math.PI / 180.0);
        float a = (float) (Math.cos(angle)) * LCH[1];
        float b = (float) (Math.sin(angle)) * LCH[1];

        LCH[0] = L;
        LCH[1] = a;
        LCH[2] = b;
        if (TRACE) {
            System.out.println("Lab: " + Arrays.toString(LCH));
        }
        return LCH;
    }

    public static float Y_to_L(float Y) {
        return 116.0f * lab_f_to(Y) - 16.0f;
    }

    public static float[] XYZ_to_Lab(float[] xyz) {
        if (TRACE) {
            System.out.println("XYZ: " + Arrays.toString(xyz));
        }

        // divide by white point:
        //CIE XYZ tristimulus values of the reference white point: Observer= 2 degrees, Illuminant= D65
//        private static final float REF_X_D65 = 95.047f;
//        private static final float REF_Y_D65 = 100.000f;
//        private static final float REF_Z_D65 = 108.883f;
        xyz[0] *= (1.0f / 0.95047f);
        xyz[2] *= (1.0f / 1.08883f);

        xyz[0] = lab_f_to(xyz[0]);
        xyz[1] = lab_f_to(xyz[1]);
        xyz[2] = lab_f_to(xyz[2]);

        float L = 116.0f * xyz[1] - 16.0f;
        float a = 500.0f * (xyz[0] - xyz[1]);
        float b = 200.0f * (xyz[1] - xyz[2]);

        xyz[0] = L;
        xyz[1] = a;
        xyz[2] = b;
        if (TRACE) {
            System.out.println("Lab: " + Arrays.toString(xyz));
        }
        return xyz;
    }

    private static float lab_f_to(final float v) {
        return (v > 0.008856f) ? (float) Math.cbrt(v) : 7.787037f * v + (16.0f / 116.0f);
    }
    private final static float epsilon = 0.20689656f;
    private final static float kappa = (24389.0f / 27.0f);

    private static float lab_f_inv(float x) {
        return (x > epsilon) ? x * x * x : (116.0f * x - 16.0f) / kappa;
    }

    public static float L_to_Y(float L) {
        return lab_f_inv((L + 16.0f) / 116.0f);
    }

    public static float[] Lab_to_XYZ(float[] Lab) {
        if (TRACE) {
            System.out.println("Lab: " + Arrays.toString(Lab));
        }
        float y = (Lab[0] + 16.0f) / 116.0f;
        float x = Lab[1] / 500.0f + y;
        float z = y - Lab[2] / 200.0f;

        x = 0.95047f * lab_f_inv(x);
        y = lab_f_inv(y);
        z = 1.08883f * lab_f_inv(z);

        Lab[0] = x;
        Lab[1] = y;
        Lab[2] = z;
        if (TRACE) {
            System.out.println("XYZ: " + Arrays.toString(Lab));
        }
        return Lab;
    }

// XYZ -> sRGB matrix, D65
    public static float[] XYZ_to_sRGB(float[] XYZ) {
        if (TRACE) {
            System.out.println("XYZ: " + Arrays.toString(XYZ));
        }
        /*        
         float r = 3.1338561f * XYZ[0] - 1.6168667f * XYZ[1] - 0.4906146f * XYZ[2];
         float g = -0.9787684f * XYZ[0] + 1.9161415f * XYZ[1] + 0.0334540f * XYZ[2];
         float b = 0.0719453f * XYZ[0] - 0.2289914f * XYZ[1] + 1.4052427f * XYZ[2];
         */
        float r = 3.2404542f * XYZ[0] - 1.5371385f * XYZ[1] - 0.4985314f * XYZ[2];
        float g = -0.9692660f * XYZ[0] + 1.8760108f * XYZ[1] + 0.0415560f * XYZ[2];
        float b = 0.0556434f * XYZ[0] - 0.2040259f * XYZ[1] + 1.0572252f * XYZ[2];

        XYZ[0] = r;
        XYZ[1] = g;
        XYZ[2] = b;
        if (TRACE) {
            System.out.println("sRGB: " + Arrays.toString(XYZ));
        }
        return XYZ;
    }

    public static float[] sRGB_to_XYZ(final int rgba, final float[] Lab) {
        return sRGB_to_XYZ(sRGB_to_f(rgba, Lab));
    }
    
// sRGB -> XYZ matrix, D65
    public static float[] sRGB_to_XYZ(float[] sRGB) {
        if (TRACE) {
            System.out.println("sRGB: " + Arrays.toString(sRGB));
        }
        /* sRGB 	D65 */
        float x = 0.4124564f * sRGB[0] + 0.3575761f * sRGB[1] + 0.1804375f * sRGB[2];
        float y = 0.2126729f * sRGB[0] + 0.7151522f * sRGB[1] + 0.0721750f * sRGB[2];
        float z = 0.0193339f * sRGB[0] + 0.1191920f * sRGB[1] + 0.9503041f * sRGB[2];

        sRGB[0] = x;
        sRGB[1] = y;
        sRGB[2] = z;
        if (TRACE) {
            System.out.println("XYZ: " + Arrays.toString(sRGB));
        }
        return sRGB;
    }

    public static float[] sRGB_to_f(final int rgba, final float[] sRGB) {
        if (TRACE) {
            System.out.println("rgba: " + rgba);
        }
        sRGB[0] = sRGBi_to_RGB((rgba >> 16) & 0xFF);
        sRGB[1] = sRGBi_to_RGB((rgba >> 8) & 0xFF);
        sRGB[2] = sRGBi_to_RGB((rgba) & 0xFF);
        sRGB[3] = ((rgba >> 24) & 0xFF) / 255f;
        if (TRACE) {
            System.out.println("sRGB: " + Arrays.toString(sRGB));
        }
        return sRGB;
    }

    public static int sRGB_to_i(final float[] sRGB) {
        if (TRACE) {
            System.out.println("sRGB: " + Arrays.toString(sRGB));
        }
        int rgba = clamp(Math.round(255f * sRGB[3])) << 24
                | RGB_to_sRGBi(sRGB[0]) << 16
                | RGB_to_sRGBi(sRGB[1]) << 8
                | RGB_to_sRGBi(sRGB[2]);
        if (TRACE) {
            final double brightness = Math.sqrt(0.299 * sRGB[0] * sRGB[0] + 0.587 * sRGB[1] * sRGB[1] + 0.114 * sRGB[2] * sRGB[2]);
            System.out.println("rgba: " + rgba + " P= " + brightness);
        }
        return rgba;
    }

    public static int RGB_to_sRGBi(float val) {
        int c = Math.round(255f * RGB_to_sRGB(val));
        if (TRACE) {
            System.out.println("val: " + val + " c: " + c);
        }
        return c;
    }

    public static float RGB_to_sRGB(float c) {
        if (c <= 0f) {
            return 0f;
        }
        if (c >= 1f) {
            return 1f;
        }
        if (c <= 0.0031308f) {
            return c * 12.92f;
        } else {
            return 1.055f * ((float) Math.pow(c, 1.0 / 2.4)) - 0.055f;
        }
    }

    public static float sRGBi_to_RGB(int val8b) {
        float c = sRGB_to_RGB(val8b / 255f);
        if (TRACE) {
            System.out.println("val: " + val8b + " c: " + c);
        }
        return c;
    }

    public static float sRGB_to_RGB(float c) {
        // Convert non-linear RGB coordinates to linear ones,
        //  numbers from the w3 spec.
        if (c <= 0f) {
            return 0f;
        }
        if (c >= 1f) {
            return 1f;
        }
        if (c <= 0.04045f) {
            return c / 12.92f;
        } else {
            return (float) (Math.pow((c + 0.055f) / 1.055f, 2.4));
        }
    }

    public static int clamp(final int val) {
        if (val < 0) {
            return 0;
        }
        if (val > 255) {
            return 255;
        }
        return val;
    }

}
