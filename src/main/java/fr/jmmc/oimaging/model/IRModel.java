/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.model;

import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.util.DateUtils;
import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.oimaging.services.Service;
import fr.jmmc.oimaging.services.ServiceList;
import fr.jmmc.oimaging.services.ServiceResult;
import fr.jmmc.oitools.image.FitsImageFile;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.ImageOiData;
import fr.jmmc.oitools.image.ImageOiInputParam;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIFitsWriter;
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
 * Simple model for Image Reconstruction based on top of observer/observable pattern.
 *
 * @author mellag
 */
public class IRModel {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(IRModel.class);

    /* Members */
    /** User's input oifits File */
    private OIFitsFile userOifitsFile;
    /** Model oifits File */
    private OIFitsFile oifitsFile;
    /** List of loaded imageHUDs */
    private List<FitsImageHDU> fitsImageHDUs;
    /** List of results */
    private List<ServiceResult> serviceResults;
    /** Selected algorithm */
    private Service selectedService;

    /** Selected input image */
    private FitsImageHDU selectedInputImageHDU;

    private final Hashtable<FitsImageHDU, String> fitsImageHdu2Filename = new Hashtable<FitsImageHDU, String>();
    ;
    private final List<String> targetList = new ArrayList<String>(5);
    private final GenericListModel<String> targetListModel = new GenericListModel<String>(targetList, true);

    /** status flag : set by RunAction */
    private boolean running;

    /** export counter */
    private int exportCount;

    public IRModel() {
        reset();
    }

    /**
     * Reset the main attributes of the model.
     */
    private void reset() {

        resetOiData(null);

        this.fitsImageHDUs = new LinkedList<FitsImageHDU>();
        this.serviceResults = new LinkedList<ServiceResult>();
        this.fitsImageHdu2Filename.clear();

        this.running = false;
        this.exportCount = 0;
    }

    /**
     * Reset the OiData attributes of the model (oifits file, targets).
     */
    private void resetOiData(final OIFitsFile inputOifitsFile) {

        this.targetListModel.clear();

        if (inputOifitsFile == null) {
            this.oifitsFile = new OIFitsFile();
            this.oifitsFile.setImageOiData(new ImageOiData());
        } else {
            this.oifitsFile = inputOifitsFile;

            // load targets
            String[] targets = null;
            if (oifitsFile.hasOiTarget()) {
                targets = oifitsFile.getOiTarget().getTarget();
                for (String target : targets) {
                    targetListModel.add(target);
                }
            }

            ImageOiInputParam inputParam;
            // load inputImageOiData or create a new one
            ImageOiData inputImageOiData = oifitsFile.getImageOiData();
            if (inputImageOiData == null) {
                inputImageOiData = new ImageOiData();
                oifitsFile.setImageOiData(inputImageOiData);

                inputParam = oifitsFile.getImageOiData().getInputParam();
                // Reset observable use according available tables
                inputParam.useVis(oifitsFile.hasOiVis());
                inputParam.useVis2(oifitsFile.hasOiVis2());
                inputParam.useT3(oifitsFile.hasOiT3());

                // Reset WLen bounds
                inputParam.setWaveMin(oifitsFile.getMinWavelentghBound());
                inputParam.setWaveMax(oifitsFile.getMaxWavelentghBound());
            }

            // fix input param according given input:
            inputParam = oifitsFile.getImageOiData().getInputParam();

            // Select first target by default
            // assume we have one
            if (targets != null) {
                inputParam.setTarget(targets[0]);
            }

            // load fits Image HDU data of given oifits if any present
            // TODO handle primary HDU ?
            if (!inputImageOiData.getFitsImageHDUs().isEmpty()) {
                addFitsImageHDUs(inputImageOiData.getFitsImageHDUs(), oifitsFile.getName());
            }

            final FitsImageHDU pHDU = oifitsFile.getPrimaryImageHDU();
            if (pHDU != null && pHDU.getImageCount() > 0) {
                setSelectedInputImageHDU(pHDU);
            }

            // Select first image as selected one if not yet initialized
            if (selectedInputImageHDU == null && !fitsImageHDUs.isEmpty()) {
                setSelectedInputImageHDU(fitsImageHDUs.get(0));
            }
        }

        // avoid null service
        if (getSelectedService() == null) {
            setSelectedSoftware(ServiceList.getPreferedService());
        }
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

    /**
     * Get user input file with OIData.
     * @param oifitsFile
     */
    public void loadOifitsFile(final OIFitsFile userOifitsFile) {

        this.userOifitsFile = userOifitsFile;
        logger.info("loadOifitsFile: userOifitsFile: {}", userOifitsFile);

        // reset oidata and put some user data into the new containers
        resetOiData(userOifitsFile);
    }

    /**
     * Add HDU to present ones.
     * @param hdus new hdus
     * @param filename filename of given hdu
     * @return true if some hdu have been added
     */
    private boolean addFitsImageHDUs(final List<FitsImageHDU> hdus, final String filename) {
        // Start with all hdus
        final List<FitsImageHDU> hdusToAdd = new LinkedList<FitsImageHDU>();
        hdusToAdd.addAll(hdus);

        // Remove duplicates and skip hdu with hduname present in current input oifits
        for (FitsImageHDU hdu : hdus) {
            if (getSelectedInputImageHDU() != null && hdu.getHduName().equals(getSelectedInputImageHDU().getHduName())) {
                hdusToAdd.remove(hdu);
                break;
            }
            for (FitsImageHDU currentHDU : getFitsImageHDUs()) {
                if (currentHDU.getChecksum() == hdu.getChecksum()) {
                    logger.info("skipping image hdu '{}' : already present ", hdu.getHduName());
                    hdusToAdd.remove(hdu);
                    break;
                }
            }
        }

        String now = DateUtils.now().substring(0, 19);
        // And continue with remaining ones
        for (FitsImageHDU hdu : hdusToAdd) {
            // if returned hdu has no name : use extname or filename + date
            if (hdu.getHduName() == null) {
                if (hdu.getExtName() != null) {
                    hdu.setHduName(hdu.getExtName() + "-" + now);
                } else {
                    hdu.setHduName(filename.substring(0, Math.min(50, filename.length())) + "-" + now);
                }
            } else {
                for (FitsImageHDU currentHDU : getFitsImageHDUs()) {
                    if (currentHDU.getHduName() == null) {
                        currentHDU.setHduName("undefined" + currentHDU.getChecksum());
                        logger.warn("present hdu had no hduName {}", currentHDU);
                    }
                    if (currentHDU.getHduName().equals(hdu.getHduName())) {
                        final String defaultHduName = hdu.getHduName() + "-" + now;
                        if (hdu.getHduName().equals(defaultHduName)) {
                            // TODO check if this branch can be reached
                            MessagePane.showErrorMessage("HDU already loaded with hduname='" + hdu.getHduName() + "'");
                            //TODO propose here to replace the previous loaded HDU
                            hdusToAdd.remove(hdu);
                            break;
                        } else {
                            String confMsg = "'" + hdu.getHduName() + "' HDU already exists.\n Do you agree to rename it '" + defaultHduName + "' ? \nElse it will be ignored. ";
                            boolean okToRename = MessagePane.showConfirmMessage(confMsg);
                            if (okToRename) {
                                logger.info("hduname '{}' already used, user accepted to rename to '{}'  ", hdu.getHduName(), defaultHduName);
                                hdu.setHduName(defaultHduName);
                                break;
                            } else {
                                hdusToAdd.remove(hdu);
                                logger.info("hduname '{}' already used : user skip prefer to skip it ", hdu.getHduName());
                            }
                        }
                    }
                }

            }
        }

        this.fitsImageHDUs.addAll(hdusToAdd);
        for (FitsImageHDU hdu : hdusToAdd) {
            this.fitsImageHdu2Filename.put(hdu, filename);
        }

        return !hdusToAdd.isEmpty();
    }

    public GenericListModel<String> getTargetListModel() {
        return targetListModel;
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

    /**
     * Set given fitsImageHDU as the selected one for input.
     * @param fitsImageHDU image to select (must be present in the previous list
     */
    public void setSelectedInputImageHDU(final FitsImageHDU fitsImageHDU) {
        if (fitsImageHDU.getHduName() == null) {
            // this imageHDU is probably not an image oi extension
            throw new IllegalStateException("Can't select given image HDU with null HDUNANE");
            // return;
        }

        boolean added = false;
        for (FitsImageHDU currentHDU : getFitsImageHDUs()) {
            if (currentHDU.getChecksum() == fitsImageHDU.getChecksum()) {
                selectedInputImageHDU = fitsImageHDU;
                oifitsFile.getImageOiData().getInputParam().setInitImg(fitsImageHDU.getHduName());
                oifitsFile.getImageOiData().getFitsImageHDUs().clear();
                oifitsFile.getImageOiData().getFitsImageHDUs().add(fitsImageHDU);
                logger.info("Set new hdu '{}' as selectedInputImageHDU", fitsImageHDU.getHduName());
                added = true;
            }
        }

        if (!added && !getFitsImageHDUs().contains(selectedInputImageHDU)) {
            throw new IllegalStateException(fitsImageHDU.getHduName() + " HDU was not added before");
        }
    }

    public List<FitsImageHDU> getFitsImageHDUs() {
        return this.fitsImageHDUs;
    }

    public List<ServiceResult> getResultSets() {
        return this.serviceResults;
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
            if (addFitsImageHDUs(hdus, fitsImageFile.getFileName())) {
                setSelectedInputImageHDU(hdus.get(hdus.size() - 1));
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
        initSpecificParams();
    }

    public void initSpecificParams() {
        ImageOiInputParam params = getImageOiData().getInputParam();
        // Get the specific params of given software if any
        selectedService.initSpecificParams(params);
    }

    public String toString() {
        return "IRModel [" + oifitsFile + ", " + fitsImageHDUs + ", " + selectedService + "]";
    }

    public File prepareTempFile() throws FitsException, IOException {
        File tmpFile = FileUtils.getTempFile(oifitsFile.getName(), ".export-" + exportCount + ".fits");
        exportCount++;
        oifitsFile.setAbsoluteFilePath(tmpFile.getAbsolutePath());
        OIFitsWriter.writeOIFits(tmpFile.getAbsolutePath(), oifitsFile);
        return tmpFile;
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
        return oifitsFile.getImageOiData();
    }

    public boolean updateWithNewModel(final ServiceResult serviceResult) {

        boolean dataAdded = false;

        final File resultFile = serviceResult.getOifitsResultFile();

        // the file does exist (checked previously):
        Exception e = null;
        try {
            OIFitsFile result = OIFitsLoader.loadOIFits(resultFile.getAbsolutePath());

            // TODO 1 - show plot for oidata part
            // 2 - show result images
            dataAdded = addFitsImageHDUs(result.getImageOiData().getFitsImageHDUs(), result.getAbsoluteFilePath());

        } catch (IOException ioe) {
            e = ioe;
        } catch (FitsException fe) {
            e = fe;
        }
        // TODO enhance user messages with details... button e.g.
        if (e != null) {
            showLog("Can't recover result data", serviceResult, e);
        }

        // TODO put this off using high level object for results
        if (dataAdded) {
            StatusBar.show("GUI updated with results ");
        } else {
            StatusBar.show("Image result unchanged");
        }

        // notify model change
        IRModelManager.getInstance().fireIRModelChanged(this, null);

        return dataAdded;
    }

    public void showLog(final String prefixMessage, final ServiceResult serviceResult, final Exception e) {
        String executionLog = null;

        final File logFile = serviceResult.getExecutionLogResultFile();
        if (logFile != null && logFile.exists()) {
            try {
                executionLog = FileUtils.readFile(serviceResult.getExecutionLogResultFile());
            } catch (IOException ioe) {
                logger.error("Can't read content of executionLog file ", ioe);
            }
        }
        MessagePane.showErrorMessage((executionLog != null) ? (prefixMessage + "\n\n" + executionLog) : prefixMessage, e);
    }

    public void addServiceResult(ServiceResult serviceResult) {
        getResultSets().add(0, serviceResult);

        if (serviceResult.isValid()) {
            try {
                addFitsImageHDUs(serviceResult.getOifitsFile().getImageOiData().getFitsImageHDUs(), serviceResult.getInputFile().getName());
            } catch (IOException ex) {
                logger.error("Can't get imageHDU from result oifile", ex);
            } catch (FitsException ex) {
                logger.error("Can't get imageHDU from result oifile", ex);
            }
        }

        // notify model update
        IRModelManager.getInstance().fireIRModelUpdated(this, null);

    }
}
