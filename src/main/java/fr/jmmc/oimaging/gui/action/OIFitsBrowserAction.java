/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui.action;

import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.oiexplorer.core.gui.OIFitsTableBrowser;
import fr.jmmc.oimaging.OImaging;
import fr.jmmc.oimaging.gui.ViewerPanel;
import fr.jmmc.oitools.model.OIFitsFile;
import java.awt.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Export one (and only one) file.
 * @author mella
 */
public final class OIFitsBrowserAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = OIFitsBrowserAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "browseOIFits";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public OIFitsBrowserAction() {
        super(className, actionName);
    }

    /**
     * Handle the action event
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        logger.debug("actionPerformed");
        final ViewerPanel viewerPanel = OImaging.getInstance().getMainPanel().getViewerPanelActive();
        if (viewerPanel.getShowMode() == ViewerPanel.SHOW_MODE.GRID) {
            MessagePane.showErrorMessage("Cannot open OIFits browser while in GRID mode. Please select one single result.");
        } else {
            final OIFitsFile oiFitsFile = viewerPanel.getCurrentOIFitsFile();

            if ((oiFitsFile != null)
                    && ((oiFitsFile.getImageHDUCount() != 0) || (oiFitsFile.getNbOiTables() != 0))) {
                OIFitsTableBrowser.showFitsBrowser(oiFitsFile);
            }
        }
    }

}
