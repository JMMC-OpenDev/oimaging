/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui.action;

import fr.jmmc.jmcs.gui.component.FileChooser;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.nom.tam.fits.FitsException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This registered action represents a File Menu entry to export an OIFits file
 * containing the current subset (oifits merged).
 * @author bourgesl
 */
public final class ExportOIFitsAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    private final static String className = ExportOIFitsAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "exportOIFits";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);
    /** OIFits MimeType */
    private final static MimeType mimeType = MimeType.OIFITS;
    /** fits extension including '.' (dot) character ie '.fits' */
    public final static String OIFITS_EXTENSION = "." + mimeType.getExtension();

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public ExportOIFitsAction() {
        super(className, actionName);
    }

    /**
     * Handle the action event
     * @param ae action event
     */
    @Override
    public final void actionPerformed(final ActionEvent ae) {
        logger.debug("actionPerformed");

        final OIFitsFile oiFitsFile = OIFitsCollectionManager.getInstance().createOIFitsFromCurrentSubsetDefinition();

        if (oiFitsFile == null || !oiFitsFile.hasOiData()) {
            MessagePane.showMessage("There is currently no data to export !");
            return;
        }

        File file = FileChooser.showSaveFileChooser("Export this observation as an OIFits file", null, mimeType, getDefaultFileName(oiFitsFile));

        logger.debug("Selected file: {}", file);

        // If a file was defined (No cancel in the dialog)
        if (file != null) {
            try {
                OIFitsWriter.writeOIFits(file.getAbsolutePath(), oiFitsFile);

                StatusBar.show(file.getName() + " created.");

            } catch (FitsException fe) {
                MessagePane.showErrorMessage("Could not export to file : " + file.getAbsolutePath(), fe);
            } catch (IOException ioe) {
                MessagePane.showErrorMessage("Could not export to file : " + file.getAbsolutePath(), ioe);
            }
        }
    }

    /**
     * Generate a default name for the given OIFits structure
     * @param oiFitsFile OIFits structure
     * @return default name [Aspro2_<TARGET>_<INSTRUMENT>_<CONFIGURATION>_<DATE>]
     */
    public static String getDefaultFileName(final OIFitsFile oiFitsFile) {
        return getDefaultFileName(oiFitsFile, true);
    }

    /**
     * Generate a default name for the given OIFits structure
     * @param oiFitsFile OIFits structure
     * @param addExtension true to add oifits extension into the returned file name; false otherwise
     * @return default name [Aspro2_<TARGET>_<INSTRUMENT>_<CONFIGURATION>_<DATE>]
     */
    public static String getDefaultFileName(final OIFitsFile oiFitsFile, final boolean addExtension) {

        final StringBuilder sb = new StringBuilder(128).append("OiXP_");

        final String altName = StringUtils.replaceNonAlphaNumericCharsByUnderscore(oiFitsFile.getOiTarget().getTarget()[0]);

        sb.append(altName).append('_');

        final OIData oiData = oiFitsFile.getOiDatas()[0];

        final String insName = StringUtils.replaceNonAlphaNumericCharsByUnderscore(oiData.getInsName());

        sb.append(insName).append('_');

        // requires analysis:
        final short[] staConf = oiData.getDistinctStaConf().iterator().next();
        final String staConfName = oiData.getStaNames(staConf);

        sb.append(StringUtils.replaceWhiteSpaces(staConfName, "-"));

        sb.append('_');

        final String dateObs = oiData.getDateObs();

        sb.append(dateObs);

        if (addExtension) {
            sb.append(OIFITS_EXTENSION);
        }

        return sb.toString();
    }
}
