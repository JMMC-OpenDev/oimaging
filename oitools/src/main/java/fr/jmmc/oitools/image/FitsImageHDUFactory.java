/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

/**
 * Create an object FitsImageHDU
 * @author kempsc
 */
public abstract class FitsImageHDUFactory {

    public final static FitsImageHDUFactory DEFAULT_FACTORY = new FitsImageHDUFactory() {
        @Override
        public FitsImageHDU create() {
            return new FitsImageHDU();
        }
    };

    public abstract FitsImageHDU create();

    protected FitsImageHDUFactory() {
    }

}
