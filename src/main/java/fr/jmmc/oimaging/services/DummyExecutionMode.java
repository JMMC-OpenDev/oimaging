/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services;

import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support remote service runner.
 * @author Guillaume MELLA.
 */
public final class DummyExecutionMode implements OImagingExecutionMode {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(DummyExecutionMode.class.getName());

    private int idx = 0;

    public DummyExecutionMode() {
    }

    @Override
    public ServiceResult reconstructsImage(final String software, final File inputFile) {
        logger.info("skip remote call for testing purpose. Just loop for pause");
        ServiceResult result = new ServiceResult(inputFile);

        try {
            int max = 10;
            for (int i = 1; i <= max; i++) {
                Thread.sleep(500);
                StatusBar.show("Fake process [" + software + "] - " + i + "/" + max);
            }
        } catch (InterruptedException ex) {
            logger.info("interruped during loop", ex);
            return null;
        }
        // TODO perform something more elaborated here
        try {
            OIFitsFile outputOIFitsFile = OIFitsLoader.loadOIFits(inputFile.getAbsolutePath());

            // TODO change hdu names for images
            for (FitsImageHDU imageHdu : outputOIFitsFile.getFitsImageHDUs()) {
                imageHdu.setHduName(imageHdu.getHduName() + idx);
                idx++;
            }

            OIFitsWriter.writeOIFits(result.getOifitsResultFile().getAbsolutePath(), outputOIFitsFile);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (FitsException ex) {
            throw new RuntimeException(ex);
        }

        return result;
    }

}
