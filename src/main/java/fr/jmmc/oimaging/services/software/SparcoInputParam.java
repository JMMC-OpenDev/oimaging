/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services.software;

import java.util.List;

import fr.jmmc.oitools.image.ImageOiInputParam;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;

/**
 * Specific parameters for Sparco (MiRA)

see mira-sparco-multi/mira2_plugin_sparcomulti.i

func read_keywords (tab, fh)
{
  nmods                  = mira_get_fits_integer(fh, "SNMODS");
  h_set, tab,  sparco_w0 = mira_get_fits_real(   fh, "SWAVE0");

  if (is_void(nmods)) {
    nmods = 0;
  }
  for (i=0; i<=nmods; ++i) {
    sparco_spectrum_i =  mira_get_fits_string(fh, swrite(format="SPEC%d", i));
    sparco_spectrum =  _(sparco_spectrum,sparco_spectrum_i);

    if (sparco_spectrum_i =="POW") {
      sparco_index = _(sparco_index, mira_get_fits_real(fh, swrite(format="SIDX%d", i)));
    } else if (sparco_spectrum_i  =="BB") {
      sparco_temp = _(sparco_temp, mira_get_fits_real(fh, swrite(format="STEM%d", i)));
    }

    if(i>0){
      sparco_flux = _(sparco_flux, mira_get_fits_real(fh, swrite(format="SFLU%d", i)));
      sparco_model_i = mira_get_fits_string(fh, swrite(format="SMOD%d", i));
      if (sparco_model_i=="UD") {
        sparco_params = _(sparco_params, mira_get_fits_real(fh, swrite(format="SPAR%d", i)));
      }
      sparco_model=  _(sparco_model,sparco_model_i );
      sparco_xy= _(sparco_xy, mira_get_fits_real(fh, swrite(format="SDEX%d", i)), mira_get_fits_real(fh, swrite(format="SDEY%d", i)));
    }
  }
...
}
func add_keywords (master, fh)
 */
public final class SparcoInputParam extends MiraInputParam {

    /** Sparco default command line options (do not use -recenter like MiRA as shifts must be absolute coordinates) */
    private static final String DEFAULT_CLI_OPTIONS = "-verb=1";

    public static final String KEYWORD_SWAVE0 = "SWAVE0";
    public static final String KEYWORD_SNMODS = "SNMODS";

    // dynamic keywords:
    public static final String KEYWORD_SPEC = "SPEC";
    public static final String KEYWORD_IDX = "SIDX";
    public static final String KEYWORD_TEM = "STEM";
    public static final String KEYWORD_MOD = "SMOD";
    public static final String KEYWORD_PAR = "SPAR";
    public static final String KEYWORD_FLU = "SFLU";
    public static final String KEYWORD_DEX = "SDEX";
    public static final String KEYWORD_DEY = "SDEY";

    public static final String KEYWORD_SPEC_POW = "POW";
    public static final String KEYWORD_SPEC_BB = "BB";
    // TODO: support for 'spectrum' for a spectrum specified in an ascii file (TO BE IMPLEMENTED)
    public static final String[] KEYWORD_SPEC_LIST = new String[]{KEYWORD_SPEC_POW, KEYWORD_SPEC_BB};

    public static final String KEYWORD_MODEL_STAR = "star";
    public static final String KEYWORD_MODEL_UD = "UD";
    public static final String KEYWORD_MODEL_BG = "bg";
    // TODO: support for 'image' computed from specified fits-file (TO BE IMPLEMENTED)
    public static final String[] KEYWORD_MODEL_LIST = new String[]{KEYWORD_MODEL_STAR, KEYWORD_MODEL_UD, KEYWORD_MODEL_BG};

    private static final KeywordMeta SWAVE0 = new KeywordMeta(KEYWORD_SWAVE0,
            "SPARCO: Central wavelength (m) for chromatism", Types.TYPE_DBL, Units.UNIT_METER);
    private static final KeywordMeta SNMODS = new KeywordMeta(KEYWORD_SNMODS,
            "SPARCO: Number of models used in SPARCO", Types.TYPE_INT);

    SparcoInputParam() {
        super();
    }

    @Override
    public void update(final ImageOiInputParam params, final boolean applyDefaults) {
        super.update(params, applyDefaults);

        // define keywords:
        params.addKeyword(SWAVE0);
        params.addKeyword(SNMODS);

        // default values:
        params.setKeywordDefaultDouble(KEYWORD_SWAVE0, 1E-6); // 1 microns
        params.setKeywordDefaultInt(KEYWORD_SNMODS, 1); // 1 star model (by default)

        final int nmods = params.getKeywordInt(KEYWORD_SNMODS);

        for (int i = 0; i <= nmods; i++) {

            if (i >= 1) {
                // Models:
                final String modelid = KEYWORD_MOD + i;

                params.addKeyword(new KeywordMeta(modelid,
                        "SPARCO: Model used", Types.TYPE_CHAR, KEYWORD_MODEL_LIST));
                params.setKeywordDefault(modelid, KEYWORD_MODEL_LIST[0]);

                final String fluxid = KEYWORD_FLU + i;

                params.addKeyword(new KeywordMeta(fluxid,
                        "SPARCO: Flux ratio of the model", Types.TYPE_DBL));
                params.setKeywordDefaultDouble(fluxid, 0.1);
            }

            final String specid = KEYWORD_SPEC + i;

            if (!params.hasKeywordMeta(specid)) {
                params.addKeyword(new KeywordMeta(specid,
                        (i == 0) ? "SPARCO: spectrum associated with the reconstructed image"
                                : "SPARCO: spectrum associated with the model", Types.TYPE_CHAR, KEYWORD_SPEC_LIST));
                params.setKeywordDefault(specid, KEYWORD_SPEC_LIST[0]);
            }

            final String spectrum_i = params.getKeyword(specid);

            if (KEYWORD_SPEC_POW.equals(spectrum_i)) {
                final String indexid = KEYWORD_IDX + i;

                params.addKeyword(new KeywordMeta(indexid,
                        (i == 0) ? "SPARCO: spectral index of the reconstructed image"
                                : "SPARCO: spectral index of the model", Types.TYPE_DBL));
                params.setKeywordDefaultDouble(indexid, -2.0);
            } else if (KEYWORD_SPEC_BB.equals(spectrum_i)) {
                final String tempid = KEYWORD_TEM + i;

                params.addKeyword(new KeywordMeta(tempid,
                        (i == 0) ? "SPARCO: black body temperature of the reconstructed image (K)"
                                : "SPARCO: black body temperature of the model (K)", Types.TYPE_DBL));
                params.setKeywordDefaultDouble(tempid, 1000.0);
            }

            if (i >= 1) {
                // Models:
                final String modelid = KEYWORD_MOD + i;

                final String model_i = params.getKeyword(modelid);

                if (KEYWORD_MODEL_UD.equals(model_i)) {
                    final String paramid = KEYWORD_PAR + i;

                    params.addKeyword(new KeywordMeta(paramid,
                            "SPARCO: UD diameter (mas)", Types.TYPE_DBL, Units.UNIT_MILLI_ARCSEC));
                    params.setKeywordDefaultDouble(paramid, 1.0);
                }

                if (!KEYWORD_MODEL_BG.equals(model_i)) {
                    // XY shift:
                    final String xid = KEYWORD_DEX + i;

                    params.addKeyword(new KeywordMeta(xid,
                            "SPARCO: RA shift of the model (mas)", Types.TYPE_DBL, Units.UNIT_MILLI_ARCSEC));
                    params.setKeywordDefaultDouble(xid, 0.0);

                    final String yid = KEYWORD_DEY + i;

                    params.addKeyword(new KeywordMeta(yid,
                            "SPARCO: DEC shift of the model (mas)", Types.TYPE_DBL, Units.UNIT_MILLI_ARCSEC));
                    params.setKeywordDefaultDouble(yid, 0.0);
                }
            }
        }
    }

    @Override
    public void validate(final ImageOiInputParam params, final List<String> failures) {
        super.validate(params, failures);

        // custom validation rules:
        final int nmods = params.getKeywordInt(KEYWORD_SNMODS);

        if (nmods <= 0) {
            failures.add("SNMODS must be greater than 0");
        }
    }

    public String getDefaultCliOptions() {
        return DEFAULT_CLI_OPTIONS;
    }
}
