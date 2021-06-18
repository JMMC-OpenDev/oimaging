/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.jmmc.oiexplorer.core.gui;

import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.oiexplorer.core.gui.selection.DataPointer;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEvent;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType;
import fr.jmmc.oitools.fits.FitsHDU;
import fr.jmmc.oitools.model.DataModel;
import fr.jmmc.oitools.model.OIArray;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OITable;
import fr.nom.tam.fits.FitsException;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public final class OIFitsTableBrowser extends javax.swing.JPanel implements OIFitsCollectionManagerEventListener {

    private static final long serialVersionUID = 1L;
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(OIFitsTableBrowser.class.getName());

    /** OIFitsCollectionManager singleton */
    private final static OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();

    /* members */
    /** table viewer panel */
    private final FitsTableViewerPanel tableViewer;
    /** oifits (weak) file reference */
    private WeakReference<OIFitsFile> oiFitsFileRef = null;

    /** Creates new form FitsBrowserPanel */
    public OIFitsTableBrowser() {
        initComponents();

        this.tableViewer = new FitsTableViewerPanel();
        postInit();

        ocm.getSelectionChangedEventNotifier().register(this);
    }

    /**
     * Free any resource or reference to this instance :
     * remove this instance from OIFitsCollectionManager event notifiers
     * dispose also child components
     */
    @Override
    public void dispose() {
        if (logger.isDebugEnabled()) {
            logger.debug("dispose: {}", ObjectUtils.getObjectInfo(this));
        }
        ocm.unbind(this);

        reset();
    }

    private void postInit() {
        this.jSplitPane.setRightComponent(tableViewer);
    }

    private void reset() {
        this.jListTables.clearSelection();
        this.tableViewer.setHdu(null);
        this.oiFitsFileRef = null;
    }

    /*
     * OIFitsCollectionManagerEventListener implementation
     */
    /**
     * Return the optional subject id i.e. related object id that this listener accepts
     * @param type event type
     * @return subject id (null means accept any event) or DISCARDED_SUBJECT_ID to discard event
     */
    @Override
    public String getSubjectId(final OIFitsCollectionManagerEventType type) {
        return DISCARDED_SUBJECT_ID;
    }

    /**
     * Handle the given OIFits collection event
     * @param event OIFits collection event
     */
    @Override
    public void onProcess(final OIFitsCollectionManagerEvent event) {
        logger.debug("onProcess {}", event);

        switch (event.getType()) {
            case SELECTION_CHANGED:
                updateSelection(event.getSelection());
                break;
            default:
        }
        logger.debug("onProcess {} - done", event);
    }

    private void updateSelection(final DataPointer ptr) {
        logger.debug("updateSelection: {}", ptr);

        final OIData oiData = ptr.getOiData();

        // lazy resolve HDU:
        final OIFitsFile oiFitsFile = getOIFitsFile();

        if (oiFitsFile != null) {
            logger.debug("oiFitsFile: {}", oiFitsFile);

            final GenericListModel<HduRef> listModel = (GenericListModel<HduRef>) this.jListTables.getModel();

            for (int i = 0, len = listModel.getSize(); i < len; i++) {
                final HduRef hduRef = listModel.get(i);

                final FitsHDU hdu;

                if (hduRef.isTable()) {
                    hdu = oiFitsFile.getOITableList().get(hduRef.getIndex());
                } else {
                    hdu = oiFitsFile.getFitsImageHDUs().get(hduRef.getIndex());
                }
                if (oiData == hdu) {
                    // table found:
                    logger.debug("hdu found: {}", hdu);

                    if (this.jListTables.getSelectedIndex() != i) {
                        if (logger.isDebugEnabled())
                        logger.debug("change selected index: {}", i);
                        this.jListTables.setSelectedIndex(i);
                    }

                    // use invokeLater (if jListTables changed)
                    SwingUtils.invokeLaterEDT(new Runnable() {
                        @Override
                        public void run() {
                            tableViewer.setSelection(ptr.getRow(), ptr.getCol());
                        }
                    });
                    break;
                }
            }
        }
    }

    public void setOiFitsFileRef(final WeakReference<OIFitsFile> oiFitsFileRef) {
        final OIFitsFile oiFitsFile = (oiFitsFileRef != null) ? oiFitsFileRef.get() : null;

        if (this.getOIFitsFile() != oiFitsFile) {
            reset();
            this.oiFitsFileRef = (oiFitsFile != null) ? new WeakReference<OIFitsFile>(oiFitsFile) : null;

            final List<HduRef> hduRefs;

            if (oiFitsFile != null) {
                hduRefs = new ArrayList<HduRef>();

                if (oiFitsFile.getPrimaryImageHDU() != null) {
                    hduRefs.add(getHduRef(0, oiFitsFile.getPrimaryImageHDU()));
                }

                final List<OITable> oiTables = oiFitsFile.getOITableList();

                for (int i = 0, len = oiTables.size(); i < len; i++) {
                    hduRefs.add(getHduRef(i, oiTables.get(i)));
                }
            } else {
                hduRefs = new ArrayList<HduRef>(0);
            }

            // no hard reference to FITS HDU:
            this.jListTables.setModel(new GenericListModel<HduRef>(hduRefs));

            if (!hduRefs.isEmpty()) {
                // ensure selection:
                this.jListTables.setSelectedIndex(0);
            }
        }
    }

    private OIFitsFile getOIFitsFile() {
        if (this.oiFitsFileRef != null) {
            return this.oiFitsFileRef.get();
        }
        return null;
    }

    /**
     * Called whenever the target selection changes.
     * @param e the event that characterizes the change.
     */
    private void processListTableValueChanged(final ListSelectionEvent e) {
        // skip events when the user selection is adjusting :
        if (e.getValueIsAdjusting()) {
            return;
        }

        final HduRef hduRef = (HduRef) this.jListTables.getSelectedValue();

        if (hduRef != null) {
            logger.debug("Selected HDU: {}", hduRef);

            // lazy resolve HDU:
            final OIFitsFile oiFitsFile = getOIFitsFile();
            final FitsHDU hdu;

            logger.debug("oiFitsFile: {}", oiFitsFile);

            if (oiFitsFile == null) {
                hdu = null;
            } else {
                if (hduRef.isTable()) {
                    hdu = oiFitsFile.getOITableList().get(hduRef.getIndex());
                } else {
                    hdu = oiFitsFile.getFitsImageHDUs().get(hduRef.getIndex());
                }
            }

            logger.debug("hdu: {}", hdu);

            final boolean includeDerivedColumns = true;
            final boolean expandRows = isTableRowsExpanded(hdu);

            this.tableViewer.setViewerOptions(includeDerivedColumns, expandRows);
            this.tableViewer.setHdu(hdu);
        }
    }

    private boolean isTableRowsExpanded(final FitsHDU hdu) {
        if (hdu instanceof OIArray) {
            return false;
        }
        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane = new javax.swing.JSplitPane();
        jScrollPaneTables = new javax.swing.JScrollPane();
        jListTables = new javax.swing.JList();

        setLayout(new java.awt.BorderLayout());

        jListTables.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListTables.setPrototypeCellValue("OI_WAVELENGTH#99");
        jListTables.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListTablesValueChanged(evt);
            }
        });
        jScrollPaneTables.setViewportView(jListTables);

        jSplitPane.setLeftComponent(jScrollPaneTables);

        add(jSplitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jListTablesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListTablesValueChanged
        processListTableValueChanged(evt);
    }//GEN-LAST:event_jListTablesValueChanged

    private final static HduRef getHduRef(final int index, final FitsHDU hdu) {
        return new HduRef(index, (hdu instanceof OITable), (hdu != null) ? hdu.idToString() : null);
    }

    private final static class HduRef {

        private final int index;
        private final boolean isTable;
        private final String label;

        HduRef(final int index, final boolean isTable, final String label) {
            this.index = index;
            this.isTable = isTable;
            this.label = label;
        }

        public int getIndex() {
            return index;
        }

        public boolean isTable() {
            return isTable;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return getLabel();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList jListTables;
    private javax.swing.JScrollPane jScrollPaneTables;
    private javax.swing.JSplitPane jSplitPane;
    // End of variables declaration//GEN-END:variables

    // --- CLI interface ---
    /** hard reference to loaded OIFitsFile (CLI) */
    private static OIFitsFile LOADED_OIFITS_FILE = null;

    private final static boolean DEBUG_MEMORY = false;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("No file location given in arguments.");
            System.exit(1);
        }
        // invoke Bootstrapper method to initialize logback now:
        Bootstrapper.getState();

        DataModel.setOiVisComplexSupport(true);
        try {
            final OIFitsFile oiFitsFile = OIFitsLoader.loadOIFits(args[0]);
            oiFitsFile.analyze();

            // pin OIFitsFile reference in memory:
            // disable to test weak references (memory leaks)
            if (!DEBUG_MEMORY) {
                LOADED_OIFITS_FILE = oiFitsFile;
            }
            showFitsBrowser(oiFitsFile);

            if (DEBUG_MEMORY) {
                SwingUtils.invokeEDT(new Runnable() {
                    @Override
                    public void run() {
                        final Timer timer = new Timer(500, new ActionListener() {
                            /**
                             * Handle the timer call
                             * @param ae action event
                             */
                            @Override
                            public void actionPerformed(final ActionEvent ae) {
                                logger.debug("timer called");
                                System.gc();
                            }
                        });
                        timer.start();
                    }
                });
            }

        } catch (IOException ioe) {
            logger.error("IO exception occured:", ioe);
        } catch (FitsException fe) {
            logger.error("Fits exception occured:", fe);
        }
    }

    private static void showFitsBrowser(final OIFitsFile oiFitsFile) {
        final WeakReference<OIFitsFile> oiFitsFileRef = new WeakReference<OIFitsFile>(oiFitsFile);
        final String oiFitsFileName = oiFitsFile.getFileName();

        SwingUtils.invokeEDT(new Runnable() {
            @Override
            public void run() {
                final JFrame frame = new JFrame("File: " + oiFitsFileName);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setMinimumSize(new Dimension(800, 800));

                final OIFitsTableBrowser fb = new OIFitsTableBrowser();
                fb.setOiFitsFileRef(oiFitsFileRef);

                frame.add(fb);

                frame.pack();
                frame.setVisible(true);
            }
        });
    }
}
