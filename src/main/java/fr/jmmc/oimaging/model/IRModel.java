/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.model;

import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.gui.component.FileChooser;
import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.oimaging.services.Service;
import fr.jmmc.oimaging.services.ServiceResult;
import fr.jmmc.oitools.image.FitsImageFile;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.ImageOiData;
import fr.jmmc.oitools.image.ImageOiInputParam;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.jmmc.oitools.model.OITable;
import fr.jmmc.oitools.model.OIWavelength;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple model based on top of observer/observable pattern.
 *
 * @author mellag
 */
public class IRModel {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(IRModel.class);

    /* Members */
    /** Main input oifits File */
    private OIFitsFile oifitsFile;
    /** List of loaded imageHUDs */
    private List<FitsImageHDU> fitsImageHDUs;
    /** Selected algorithm */
    private Service selectedService;

    /** Selected input image */
    private FitsImageHDU selectedInputImageHDU;

    private final Hashtable<FitsImageHDU, String> fitsImageHdu2Filename = new Hashtable<FitsImageHDU, String>();

    private final List<String> targetList = new ArrayList<String>(5);
    private final GenericListModel<String> targetListModel = new GenericListModel<String>(targetList, true);

    /** Store min wavelength of oifits file */
    private double minWavelentghBound;
    /** Store max wavelength of oifits file */
    private double maxWavelentghBound;

    private ImageOiData imageOiData = null;

    /** status flag : set by RunAction */
    private boolean running = false;

    /** export counter */
    private int exportCount = 0;

    public IRModel() {
        reset();
    }

    private void reset() {
        this.oifitsFile = null;
        this.fitsImageHDUs = new LinkedList<FitsImageHDU>();
        this.selectedService = null;
        this.selectedInputImageHDU = null;
        this.fitsImageHdu2Filename.clear();
        imageOiData = new ImageOiData();

        // Set default values
        ImageOiInputParam params = imageOiData.getInputParam();
        params.setWaveMin(-1);
        params.setWaveMax(-1);
        params.setMaxiter(200);
        params.setRglAlph(0);
        params.setRglBeta(0);

    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public OIFitsFile getOifitsFile() {
        return oifitsFile;
    }

    public void setOifitsFile(OIFitsFile oifitsFile) {
        this.oifitsFile = oifitsFile;

        // TODO refactor using OifitsCollectionManager
        // Reset + add all target of given OIFits
        targetListModel.clear();

        String[] targets = null;
        if (oifitsFile.hasOiTarget()) {
            targets = oifitsFile.getOiTarget().getTarget();
            for (String target : targets) {
                targetListModel.add(target);
            }
        }

        // Fix input param according content:
        ImageOiInputParam inputParam = imageOiData.getInputParam();
        // Select first target by default
        // assume we have one
        if (targets != null) {
            inputParam.setTarget(targets[0]);
        }

        // Define observable use according available tables
        inputParam.useVis(oifitsFile.hasOiVis());
        inputParam.useVis2(oifitsFile.hasOiVis2());
        inputParam.useT3(oifitsFile.hasOiT3());

        // Set wavelength bounds
        minWavelentghBound = Double.MAX_VALUE;
        maxWavelentghBound = Double.MIN_VALUE;
        for (OIWavelength oiWavelength : oifitsFile.getOiWavelengths()) {
            float omin = oiWavelength.getEffWaveMin();
            float omax = oiWavelength.getEffWaveMax();
            minWavelentghBound = omin < minWavelentghBound ? omin : minWavelentghBound;
            maxWavelentghBound = omax > maxWavelentghBound ? omax : maxWavelentghBound;
        }
        // TODO init input param
        inputParam.setWaveMin(minWavelentghBound);
        inputParam.setWaveMax(maxWavelentghBound);

        ImageOiData iod = oifitsFile.getImageOiData();

        // use imageOi data of given oifits if any present
        if (iod != null) {
            imageOiData = iod;
            addFitsImageHDUs(iod.getFitsImageHDUs(), oifitsFile.getName());
        }

    }

    private void addFitsImageHDUs(List<FitsImageHDU> hdus, String filename) {
        for (FitsImageHDU hdu : hdus) {
            final String defaultHduName = hdu.getHduName() + "-" + exportCount;
            // TODO check length because of fits keyword limitation
            if (hdu.getHduName() == null) {
                hdu.setHduName(filename + "#" + hdu.getHduIndex());
            }
            for (FitsImageHDU loadedHDU : getFitsImageHDUs()) {
                if (loadedHDU.getHduName().equals(hdu.getHduName())) {

                    if (hdu.getHduName().equals(defaultHduName)) {
                        MessagePane.showErrorMessage("HDU already loaded with hduname='" + hdu.getHduName() + "'");
                        //TODO propose here to replace the previous loaded HDU
                        return;
                    } else {
                        String confMsg = "'" + hdu.getHduName() + "' HDU already exists.\n Do you agree to rename it '" + defaultHduName + "' ?";
                        boolean okToRename = MessagePane.showConfirmMessage(confMsg);
                        if (okToRename) {
                            hdu.setHduName(defaultHduName);
                        } else {
                            return;
                        }
                    }
                }
            }

        }

        this.fitsImageHDUs.addAll(hdus);
        for (FitsImageHDU hdu : hdus) {
            this.fitsImageHdu2Filename.put(hdu, filename);
        }
    }

    public GenericListModel<String> getTargetListModel() {
        return targetListModel;
    }

    /**
     * Returns the smallest wavelength of given oifits.
     *
     * @return smallest wlen
     */
    public double getMinWavelentghBound() {
        return minWavelentghBound;
    }

    /**
     * Returns the biggest wavelength of given oifits.
     *
     * @return biggest wlen
     */
    public double getMaxWavelentghBound() {
        return maxWavelentghBound;
    }

    /**
     * Return the selected imageHDU for input or first one.
     * @return selected fitsImageHDU
     */
    public FitsImageHDU getSelectedInputImageHDU() {
        if (selectedInputImageHDU != null) {
            return selectedInputImageHDU;
        } else if (getFitsImageHDUs().size() > 0) {
            setSelectedInputImageHDU(getFitsImageHDUs().get(0));
            return getSelectedInputImageHDU();
        }
        return null;

    }

    public void setSelectedInputImageHDU(FitsImageHDU fitsImageHDU) {
        this.selectedInputImageHDU = fitsImageHDU;
        imageOiData.getInputParam().setInitImg(selectedInputImageHDU.getHduName());
    }

    public List<FitsImageHDU> getFitsImageHDUs() {
        return this.fitsImageHDUs;
    }

    /**
     * Add image HDU of given file and select first imageHDU if no input image was selected before.
     * Each added HDU get HDUNAME set with original fits filename with suffixe # and extension index.
     * @param fitsImageFile fitsImageFile to load for fitsImageHDU discover
     */
    public void addFitsImageFile(FitsImageFile fitsImageFile) {
        List<FitsImageHDU> hdus = fitsImageFile.getFitsImageHDUs();
        if (hdus.size() > 0) {
            logger.debug("add " + hdus.size() + "ImageHDUs from " + fitsImageFile.getAbsoluteFilePath());
            addFitsImageHDUs(hdus, fitsImageFile.getFileName());

            // select first if no selection was set before
            if (selectedInputImageHDU == null) {
                setSelectedInputImageHDU(hdus.get(0));
            }
        } else {
            logger.debug("no ImageHDUs found in " + fitsImageFile.getAbsoluteFilePath());
            MessagePane.showErrorMessage("Image loading", "no ImageHDUs found in " + fitsImageFile.getAbsoluteFilePath());
        }
    }

    public String getImageHDUFilename(FitsImageHDU hdu) {
        return fitsImageHdu2Filename.get(hdu);
    }

    public Service getSelectedService() {
        return selectedService;
    }

    public void setSelectedSoftware(Service selectedService) {
        this.selectedService = selectedService;
    }

    public String toString() {
        return "IRModel [" + oifitsFile + ", " + fitsImageHDUs + ", " + selectedService + "]";
    }

    public void exportOIFits() {

        File dir = FileUtils.getDirectory(oifitsFile.getAbsoluteFilePath());
        String name = FileUtils.getFileNameWithoutExtension(oifitsFile.getName()) + ".image-oi." + FileUtils.getExtension(oifitsFile.getName());
        File file = FileChooser.showSaveFileChooser("Choose location of prepared OIFits file", dir, MimeType.OIFITS, name);

        // Cancel
        if (file == null) {
            return;
        }

        try {
            prepareOIFits(file);
            StatusBar.show("OIFits exported into " + file.getName());
        } catch (IOException ioe) {
            MessagePane.showErrorMessage("Can't write oifits", "Can't store OIFits into " + file, ioe);
        } catch (FitsException fe) {
            MessagePane.showErrorMessage("Can't write oifits", "Can't store OIFits into " + file, fe);
        }

    }

    private void prepareOIFits(File file) throws FitsException, IOException {
        // Create a virtual container, associate related material
        OIFitsFile virtu = new OIFitsFile();
        virtu.setAbsoluteFilePath(file.getAbsolutePath());
        virtu.setImageOiData(imageOiData);
        imageOiData.getFitsImageHDUs().clear();
        imageOiData.getFitsImageHDUs().add(selectedInputImageHDU);

        // Feed every oitables reference (no cloning -> reference to internal oifitsfile will not be consistent)
        for (OITable table : oifitsFile.getOiTables()) {
            virtu.addOiTable(table);
        }

        OIFitsWriter.writeOIFits(file.getAbsolutePath(), virtu);
    }

    public String getelectedInputFitsImageError() {
        if (getSelectedInputImageHDU() == null) {
            return "No image data loaded";
        }

        // TODO implement more test in the future to verify HDU confirmity
        return null;
    }

    public String getSelectedInputFitsImageDescriptor() {
        if (getSelectedInputImageHDU() == null) {
            return null;
        }
        return getSelectedInputImageHDU().toString();
    }

    public ImageOiData getImageOiData() {
        return imageOiData;
    }

    public File prepareTempFile() throws FitsException, IOException {
        File tmpFile = FileUtils.getTempFile("export-" + exportCount + "-" + oifitsFile.getName(), ".fits");
        prepareOIFits(tmpFile);
        return tmpFile;
    }

    public void updateWithNewModel(final ServiceResult serviceResult) {
        final File resultFile = serviceResult.getOifits();

        // file exists:
        Exception e = null;
        try {
            OIFitsFile result = OIFitsLoader.loadOIFits(resultFile.getAbsolutePath());

            // TODO 1 - show plot for oidata part
            // 2 - show result images
            addFitsImageHDUs(result.getImageOiData().getFitsImageHDUs(), result.getAbsoluteFilePath());
            IRModelManager.getInstance().fireIRModelChanged(this, null);

            StatusBar.show("GUI updated with results ");

        } catch (IOException ioe) {
            e = ioe;
        } catch (FitsException fe) {
            e = fe;
        }
        // TODO enhance user messages with details... button e.g.
        if (e != null) {
            showLog("Can't recover result data", serviceResult, e);
        }
    }

    public void showLog(final String prefixMessage, final ServiceResult serviceResult, final Exception e) {
        String executionLog = "";
        try {
            executionLog = FileUtils.readFile(serviceResult.getExecutionLog());
        } catch (IOException ex) {
            logger.error("Can't read content of executionLog file ", ex);
        }
        MessagePane.showErrorMessage(prefixMessage + "\n\n" + executionLog, e);
    }
}
