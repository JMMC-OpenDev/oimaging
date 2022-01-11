/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.interop;

import fr.jmmc.jmcs.network.interop.SampCapability;
import fr.jmmc.jmcs.network.interop.SampCapabilityAction;
import fr.jmmc.oimaging.OImaging;
import fr.jmmc.oimaging.gui.ViewerPanel;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This registered action represents an Interop Menu entry to
 * send fits image to any FITS application (ds9)...
 *
 * @author LAURENT BOURGES
 */
public final class SendFitsAction extends SampCapabilityAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = SendFitsAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "sendFitsAction";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public SendFitsAction() {
        super(className, actionName, SampCapability.LOAD_FITS_IMAGE);
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

        ViewerPanel viewerPanel = OImaging.getInstance().getMainPanel().getViewerPanelActive();

        if (viewerPanel != null) {
            final File file = viewerPanel.exportFitsImage(false);

            if (file != null) {
                // Store parameters into SAMP message:
                final Map<String, String> parameters = new HashMap<String, String>(4);
                addUrlParameter(parameters, file);
                return parameters;
            }
        }

        return null;
    }
}
