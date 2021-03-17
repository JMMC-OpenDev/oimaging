/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.util;

import java.util.Arrays;

/**
 * Color utility methods to transform colors between color spaces (sRGB, linear RGB, XYZ, Lab, Lch)
 * @author bourgesl
 */
public final class ColorUtils {

    private static boolean TRACE = false;

    private ColorUtils() {
        // no-op
    }

    public static float luminance(final int rgba) {
        final int r = (rgba >> 16) & 0xFF;
        final int g = (rgba >> 8) & 0xFF;
        final int b = (rgba) & 0xFF;
        // TODO: take alpha into account
        return luminance(r, g, b);
    }
    
    public static int luminance(final int r, final int g, final int b) {
        // https://ninedegreesbelow.com/photography/srgb-luminance.html
        /*
        Adapted:
Red:   (255*0.2225 +   0*0.7169 +   0*0.0606) / 255 = 0.2225
Green: ( 0*0.2225  + 255*0.7169 +   0*0.0606) / 255 = 0.7169
Blue:  ( 0*0.2225  +   0*0.7169 + 255*0.0606) / 255 = 0.0606
         */
        // return r * 0.2225 + g * 0.7169 + b * 0.0606;
        return (r * 57 + g * 184 + b * 15) >> 8; // round to go up to 256
    }

    public static float lumaY(final int rgb) {
        return lumaY(rgb, new float[4]);
    }

    public static float lumaY(final int rgb, final float[] c4) {
        // convert sRGB to Lab (float)
        sRGB_to_Lab(rgb, c4);

        // Convert L to Y (luminance in [0..1])
        return L_to_Y(c4[0]);
    }

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

    public static float[] sRGB_to_OkLab(final int rgba, final float[] Lab) {
        return f_sRGB_to_OkLab(sRGB_to_f(rgba, Lab));
    }

    public static int OkLab_to_sRGB(final float[] Lab) {
        return sRGB_to_i(OkLab_to_f_sRGB(Lab));
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
    private final static float epsilon = 0.206896551f;
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
        if (c >= 0.0031308f) {
            return 1.055f * ((float) Math.pow(c, 1.0 / 2.4)) - 0.055f;
        } else {
            return c * 12.92f;
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
        if (c >= 0.04045f) {
            return (float) (Math.pow((c + 0.055f) / 1.055f, 2.4));
        } else {
            return c / 12.92f;
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

// OkLab -> sRGB
    static float[] OkLab_to_f_sRGB(final float[] lab) {
        if (TRACE) {
            System.out.println("OkLab: " + Arrays.toString(lab));
        }
        float l = lab[0] + 0.3963377774f * lab[1] + 0.2158037573f * lab[2];
        float m = lab[0] - 0.1055613458f * lab[1] - 0.0638541728f * lab[2];
        float s = lab[0] - 0.0894841775f * lab[1] - 1.2914855480f * lab[2];

        l = l * l * l;
        m = m * m * m;
        s = s * s * s;

        lab[0] = +4.0767245293f * l - 3.3072168827f * m + 0.2307590544f * s;
        lab[1] = -1.2681437731f * l + 2.6093323231f * m - 0.3411344290f * s;
        lab[2] = -0.0041119885f * l - 0.7034763098f * m + 1.7068625689f * s;

        if (TRACE) {
            System.out.println("sRGB: " + Arrays.toString(lab));
        }
        return lab;
    }

// sRGB -> OkLab
    static float[] f_sRGB_to_OkLab(final float[] sRGB) {
        if (TRACE) {
            System.out.println("sRGB: " + Arrays.toString(sRGB));
        }
        float l = 0.4121656120f * sRGB[0] + 0.5362752080f * sRGB[1] + 0.0514575653f * sRGB[2];
        float m = 0.2118591070f * sRGB[0] + 0.6807189584f * sRGB[1] + 0.1074065790f * sRGB[2];
        float s = 0.0883097947f * sRGB[0] + 0.2818474174f * sRGB[1] + 0.6302613616f * sRGB[2];

        l = (float) Math.cbrt(l);
        m = (float) Math.cbrt(m);
        s = (float) Math.cbrt(s);

        sRGB[0] = 0.2104542553f * l + 0.7936177850f * m - 0.0040720468f * s;
        sRGB[1] = 1.9779984951f * l - 2.4285922050f * m + 0.4505937099f * s;
        sRGB[2] = 0.0259040371f * l + 0.7827717662f * m - 0.8086757660f * s;

        if (TRACE) {
            System.out.println("OkLab: " + Arrays.toString(sRGB));
        }
        return sRGB;
    }

}
