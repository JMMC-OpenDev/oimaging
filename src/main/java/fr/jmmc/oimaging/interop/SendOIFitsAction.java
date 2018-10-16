/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.interop;

import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.network.interop.SampCapability;
import fr.jmmc.jmcs.network.interop.SampCapabilityAction;
import fr.jmmc.oimaging.model.IRModel;
import fr.jmmc.oimaging.model.IRModelManager;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.nom.tam.fits.FitsException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This registered action represents an Interop Menu entry to
 * send generated oifits data to any FITS application (OIFitsExplorer, topcat)...
 *
 * @author LAURENT BOURGES
 */
public final class SendOIFitsAction extends SampCapabilityAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    private final static String className = SendOIFitsAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "sendOIFitsAction";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public SendOIFitsAction() {
        super(className, actionName, SampCapability.LOAD_FITS_TABLE);
    }

    /**
     * This method automatically sends the message returned by composeMessage()
     * to user selected client(s). Children classes should not overwrite this
     * method or must call super implementation to keep SAMP message management.
     *
     * @param e actionEvent coming from SWING objects. It contains in its
     * command the name of the destination.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        logger.debug("actionPerformed");

        final IRModel irModel = IRModelManager.getInstance().getIRModel();
        final OIFitsFile oiFitsFile = irModel.getOifitsFile();

        if (oiFitsFile == null || !oiFitsFile.hasOiData()) {
            MessagePane.showMessage("There is currently no data to export !");
            return;
        }
        composeAndSendMessage(e.getActionCommand());
    }

    /**
     * Should return the message you want to send
     * @throws IllegalStateException if the oifits file can not be written to a temporary file
     * @return Samp message parameters as a map
     */
    @Override
    public Map<?, ?> composeMessage() throws IllegalStateException {
        logger.debug("composeMessage");

        final IRModel irModel = IRModelManager.getInstance().getIRModel();
        final File file;

        try {
            file = irModel.prepareTempFile();
        } catch (FitsException fe) {
            throw new IllegalStateException("Could not export to temporary file", fe);
        } catch (IOException ioe) {
            throw new IllegalStateException("Could not export to temporary file", ioe);
        }

        // Store parameters into SAMP message:
        final Map<String, String> parameters = new HashMap<String, String>(4);
        addUrlParameter(parameters, file);
        return parameters;
    }
}
