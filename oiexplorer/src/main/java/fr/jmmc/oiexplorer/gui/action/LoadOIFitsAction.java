/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui.action;

import fr.jmmc.jmcs.data.preference.SessionSettingsPreferences;
import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.FileChooser;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.oiexplorer.OIFitsExplorer;
import fr.jmmc.oiexplorer.core.model.LoadOIFitsListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oitools.model.OIFitsChecker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load one (or more) files.
 * @author mella
 */
public final class LoadOIFitsAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = LoadOIFitsAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "loadOIFits";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);
    /** OIFits MimeType */
    private final static MimeType mimeType = MimeType.OIFITS;

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public LoadOIFitsAction() {
        super(className, actionName);
    }

    /**
     * Handle the action event
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        logger.debug("actionPerformed");

        File[] files;

        // If the action was automatically triggered from App launch
        if (evt.getSource() == ActionRegistrar.getInstance()) {
            File file = new File(evt.getActionCommand());

            if (!file.exists() || !file.isFile()) {
                MessagePane.showErrorMessage("Could not load the file : " + file.getAbsolutePath());
                files = null;
            } else {
                // update current directory for oidata:
                SessionSettingsPreferences.setCurrentDirectoryForMimeType(mimeType, file.getParent());

                files = new File[]{file};
            }

        } else {
            files = FileChooser.showOpenFilesChooser("Load oifits file", null, mimeType);
        }

        // If a file was defined (No cancel in the dialog)
        if (files != null) {

            // Create progress panel:
            final JProgressBar progressBar = new JProgressBar();
            final JPanel progressPanel = createLoadOIFitsProgressPanel(progressBar);

            StatusBar.addCustomPanel(progressPanel);

            final OIFitsChecker checker = new OIFitsChecker();

            OIFitsCollectionManager.getInstance().loadOIFitsFiles(files, checker,
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

                    // display validation messages anyway:
                    final String checkReport = checker.getCheckReport();
                    logger.info("validation results:\n{}", checkReport);

                    if (!cancelled) {
                        MessagePane.showMessage(checkReport);
                    }
                }
            });
        }
    }

    static JPanel createLoadOIFitsProgressPanel(final JProgressBar progressBar) {
        return OIFitsExplorer.createProgressPanel("loading OIFits files ...", progressBar,
                new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                OIFitsCollectionManager.cancelTaskLoadOIFits();
            }
        });
    }
}
