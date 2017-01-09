/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.model.Table;

/**
 * This class is a container for IMAGE-OI INPUT PARAM.
 * https://github.com/emmt/OI-Imaging-JRA
 * It can be associated to an OIFitsFile to produce IMAGE-OI compliant files.
 *
 * @author mellag
 */
public class ImageOiInputParam extends Table {

    /* constants */
    /** Logger associated to meta model classes */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ImageOiInputParam.class.getName());

    // Define Data selection keywords
    private KeywordMeta target = new KeywordMeta(ImageOiConstants.KEYWORD_TARGET, "Identifier of the target object to reconstruct", Types.TYPE_CHAR);
    private KeywordMeta wave_min = new KeywordMeta(ImageOiConstants.KEYWORD_WAVE_MIN, "Minimum wavelentgh to select (in meters)", Types.TYPE_DBL);
    private KeywordMeta wave_max = new KeywordMeta(ImageOiConstants.KEYWORD_WAVE_MAX, "Maximum wavelentgh to select (in meters)", Types.TYPE_DBL);
    private KeywordMeta use_vis = new KeywordMeta(ImageOiConstants.KEYWORD_USE_VIS, "Use complex visibility data if any", Types.TYPE_LOGICAL);
    private KeywordMeta use_vis2 = new KeywordMeta(ImageOiConstants.KEYWORD_USE_VIS2, "Use squared visibility data if any", Types.TYPE_LOGICAL);
    private KeywordMeta use_t3 = new KeywordMeta(ImageOiConstants.KEYWORD_USE_T3, "Use triple product data if any", Types.TYPE_LOGICAL);

    // Define Algorithm settings keywords
    private KeywordMeta init_img = new KeywordMeta(ImageOiConstants.KEYWORD_INIT_IMG, "Identifier of the initial image", Types.TYPE_CHAR);
    private KeywordMeta maxiter = new KeywordMeta(ImageOiConstants.KEYWORD_MAXITER, "Maximum number of iterations to run", Types.TYPE_INT);
    private KeywordMeta rgl_name = new KeywordMeta(ImageOiConstants.KEYWORD_RGL_NAME, "Name of the regularization method", Types.TYPE_CHAR);
    private KeywordMeta rgl_wgt = new KeywordMeta(ImageOiConstants.KEYWORD_RGL_WGT, "Weight of the regularization", Types.TYPE_DBL);
    private KeywordMeta rgl_alph = new KeywordMeta(ImageOiConstants.KEYWORD_RGL_ALPH, "Parameter alpha of the regularization", Types.TYPE_DBL);
    private KeywordMeta rgl_beta = new KeywordMeta(ImageOiConstants.KEYWORD_RGL_BETA, "Parameter beta of the regularization", Types.TYPE_DBL);
    private KeywordMeta rgl_prio = new KeywordMeta(ImageOiConstants.KEYWORD_RGL_PRIO, "Identifier of the HDU with the prior image", Types.TYPE_CHAR);

    // Bsmem specific
    private KeywordMeta auto_wgt = new KeywordMeta(ImageOiConstants.KEYWORD_AUTO_WGT, "Automatic regularization weight", Types.TYPE_LOGICAL);
    private KeywordMeta fluxerr = new KeywordMeta(ImageOiConstants.KEYWORD_FLUXERR, "Error on zero-baseline V^2 point", Types.TYPE_DBL);

    // Image parameters
    public ImageOiInputParam() {

        // Register Data selection keywords
        addKeywordMeta(target);
        addKeywordMeta(wave_min);
        addKeywordMeta(wave_max);
        addKeywordMeta(use_vis);
        addKeywordMeta(use_vis2);
        addKeywordMeta(use_t3);

        // Register Algorithm settings keywords
        addKeywordMeta(init_img);
        addKeywordMeta(maxiter);
        addKeywordMeta(rgl_name);
        addKeywordMeta(rgl_wgt);
        addKeywordMeta(rgl_alph);
        addKeywordMeta(rgl_beta);
        addKeywordMeta(rgl_prio);

        addKeywordMeta(auto_wgt);
        addKeywordMeta(fluxerr);

        setExtName(ImageOiConstants.EXTNAME_IMAGE_OI_INPUT_PARAM);

        // Set default values
        // TODO make it dynamic and software dependant
        setWaveMin(-1);
        setWaveMax(-1);
        setMaxiter(200);
        setRglWgt(0);
        setRglAlph(0);
        setRglBeta(0);
        setRglName("mem_prior");
        ImageOiInputParam.this.useAutoWgt(true);
        setFluxErr(0.1);

    }

    public String getTarget() {
        return getKeyword(this.target.getName());
    }

    public void setTarget(String target) {
        setKeyword(this.target.getName(), target);
    }

    public double getWaveMin() {
        return getKeywordDouble(wave_min.getName());
    }

    public void setWaveMin(double wave_min) {
        setKeywordDouble(this.wave_min.getName(), wave_min);
    }

    public double getWaveMax() {
        return getKeywordDouble(wave_max.getName());
    }

    public void setWaveMax(double wave_max) {
        setKeywordDouble(this.wave_max.getName(), wave_max);
    }

    public boolean useVis() {
        return getKeywordLogical(use_vis.getName());
    }

    public void useVis(boolean use_vis) {
        setKeywordLogical(this.use_vis.getName(), use_vis);
    }

    public boolean useVis2() {
        return getKeywordLogical(use_vis2.getName());
    }

    public void useVis2(boolean use_vis2) {
        setKeywordLogical(this.use_vis2.getName(), use_vis2);
    }

    public boolean useT3() {
        return getKeywordLogical(use_t3.getName());
    }

    public void useT3(boolean use_t3) {
        setKeywordLogical(this.use_t3.getName(), use_t3);
    }

    public String getInitImg() {
        return getKeyword(this.init_img.getName());
    }

    public void setInitImg(String init_img) {
        setKeyword(this.init_img.getName(), init_img);
    }

    public int getMaxiter() {
        return getKeywordInt(this.maxiter.getName());
    }

    public void setMaxiter(int maxiter) {
        setKeywordInt(this.maxiter.getName(), maxiter);
    }

    public String getRglName() {
        return getKeyword(this.rgl_name.getName());
    }

    public void setRglName(String rgl_name) {
        setKeyword(this.rgl_name.getName(), rgl_name);
    }

    public double getRglWgt() {
        return getKeywordDouble(rgl_wgt.getName());
    }

    public void setRglWgt(double rgl_wgt) {
        setKeywordDouble(this.rgl_wgt.getName(), rgl_wgt);
    }

    public double getRglAlph() {
        return getKeywordDouble(rgl_alph.getName());
    }

    public void setRglAlph(double rgl_alph) {
        setKeywordDouble(this.rgl_alph.getName(), rgl_alph);
    }

    public double getRglBeta() {
        return getKeywordDouble(rgl_beta.getName());
    }

    public void setRglBeta(double rgl_beta) {
        setKeywordDouble(this.rgl_beta.getName(), rgl_beta);
    }

    public String getRglPrio() {
        return getKeyword(this.rgl_prio.getName());
    }

    public void setRglPrio(String rgl_prio) {
        setKeyword(this.rgl_prio.getName(), rgl_prio);
    }

    // bsmem specific
    public double getFluxErr() {
        return getKeywordDouble(fluxerr.getName());

    }

    public void setFluxErr(double fluxErr) {
        setKeywordDouble(this.fluxerr.getName(), fluxErr);

    }

    public boolean useAutoWgt() {
        return getKeywordLogical(this.auto_wgt.getName());
    }

    public void useAutoWgt(boolean auto_rgl) {
        setKeywordLogical(this.auto_wgt.getName(), auto_rgl);
    }

}
