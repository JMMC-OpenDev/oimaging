/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui.action;

import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.FileChooser;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.oiexplorer.core.model.LoadOIFitsListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oitools.model.OIFitsChecker;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load one (or more) files from given OIFitsCollection file.
 * @author mella
 */
public final class LoadOIFitsFromCollectionAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = LoadOIFitsFromCollectionAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "loadOIFitsFromCollection";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);
    /** OIFits MimeType */
    private final static MimeType mimeType = MimeType.OIFITS_EXPLORER_COLLECTION;

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public LoadOIFitsFromCollectionAction() {
        super(className, actionName);
    }

    /**
     * Handle the action event
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        logger.debug("actionPerformed");

        final OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();

        final File file = FileChooser.showOpenFileChooser("Select OIFits Explorer Collection", null, mimeType, null);

        // If a file was defined (No cancel in the dialog)
        if (file != null) {
            final String fileLocation = file.getAbsolutePath();

            StatusBar.show("loading OIFits from OIFits Explorer Collection: " + fileLocation);

            // Create progress panel:
            final JProgressBar progressBar = new JProgressBar();
            final JPanel progressPanel = LoadOIFitsAction.createLoadOIFitsProgressPanel(progressBar);

            StatusBar.addCustomPanel(progressPanel);

            Exception e = null;
            try {
                final OIFitsChecker checker = new OIFitsChecker();

                ocm.loadOIFitsCollection(file, checker,
                        new LoadOIFitsListener() {

                            @Override
                            public void propertyChange(final PropertyChangeEvent pce) {
                                if ("progress".equals(pce.getPropertyName())) {
                                    progressBar.setValue((Integer) pce.getNewValue());
                                }
                            }

                            @Override
                            public void done(final boolean cancelled) {
                                StatusBar.removeCustomPanel(progressPanel);

                                // log validation messages anyway:
                                final String checkReport = checker.getCheckReport();
                                logger.info("validation results:\n{}", checkReport);

                                // TODO: use a preference to show or hide the validation report:
                                if (false && !cancelled) {
                                    MessagePane.showMessage(checkReport);
                                }

                                // Fire the Ready event to any listener:
                                ocm.fireReady(this, null);
                            }
                        }, true);

            } catch (IllegalStateException ise) {
                e = ise;
            } catch (IOException ioe) {
                e = ioe;
            } finally {
                if (e != null) {
                    StatusBar.removeCustomPanel(progressPanel);

                    StatusBar.show("Could not load OIFits Explorer Collection: " + fileLocation);
                    MessagePane.showErrorMessage("Could not load OIFits Explorer Collection: " + fileLocation, e);
                }
            }
        }
    }
}
