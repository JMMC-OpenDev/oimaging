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
import fr.jmmc.oitools.meta.OIFitsStandard;
import fr.jmmc.oitools.model.OIFitsChecker;
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
import org.apache.commons.lang.StringUtils;
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
    /** Optional cliOptions (null value is not fowarded to execution)*/
    private String cliOptions;

    /** Selected input image */
    private FitsImageHDU selectedInputImageHDU;

    private final Hashtable<FitsImageHDU, String> fitsImageHdu2Filename = new Hashtable<FitsImageHDU, String>();

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
        this.cliOptions = null;
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
            this.oifitsFile = new OIFitsFile(OIFitsStandard.VERSION_1);
        } else {
            this.oifitsFile = inputOifitsFile;
        }
        // create ImageOiData if needed:
        oifitsFile.getImageOiData();

        // load targets
        String[] targets = null;
        if (oifitsFile.hasOiTarget()) {
            targets = oifitsFile.getOiTarget().getTarget();
            for (String target : targets) {
                targetListModel.add(target);
            }
        }

        // load inputImageOiData or create a new one
        final ImageOiData inputImageOiData = oifitsFile.getImageOiData();
        final ImageOiInputParam inputParam = oifitsFile.getImageOiData().getInputParam();

        // Reset observable use according available tables
        inputParam.useVis(oifitsFile.hasOiVis());
        inputParam.useVis2(oifitsFile.hasOiVis2());
        inputParam.useT3(oifitsFile.hasOiT3());

        // Reset WLen bounds
        inputParam.setWaveMin(oifitsFile.getMinWavelengthBound());
        inputParam.setWaveMax(oifitsFile.getMaxWavelengthBound());

        // Select first target by default
        // assume we have one
        if (targets != null) {
            inputParam.setTarget(targets[0]);
        }

        // load fits Image HDU data of given oifits if any present
        // TODO handle primary HDU ?
        if (!oifitsFile.getFitsImageHDUs().isEmpty()) {
            addFitsImageHDUs(oifitsFile.getFitsImageHDUs(), oifitsFile.getFileName());
        }

        final FitsImageHDU pHDU = oifitsFile.getPrimaryImageHDU();
        if (pHDU != null && pHDU.hasImages()) {
            setSelectedInputImageHDU(pHDU);
        }

        // Select first image as selected one if not yet initialized
        if (selectedInputImageHDU == null && fitsImageHDUs != null && !fitsImageHDUs.isEmpty()) {
            setSelectedInputImageHDU(fitsImageHDUs.get(0));
        }

        // avoid null service
        if (getSelectedService() == null) {
            setSelectedService(ServiceList.getPreferedService());
        } else {
            initSpecificParams();
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

    public OIFitsFile getUserOifitsFile() {
        return userOifitsFile;
    }

    /**
     * Add HDU to present ones and select the first new one as selected image input.
     * @param hdus new hdus
     * @param filename filename of given hdu
     * @return true if some hdu have been added
     */
    private boolean addFitsImageHDUs(final List<FitsImageHDU> hdus, final String filename) {
        // Start with all hdus
        final List<FitsImageHDU> hdusToAdd = new LinkedList<FitsImageHDU>();

        for (FitsImageHDU fitsImageHDU : hdus) {
            if (fitsImageHDU.hasImages()) {
                hdusToAdd.add(fitsImageHDU);
            }
        }

        // Remove duplicates and skip hdu with hduname present in current input oifits
        for (FitsImageHDU hdu : hdus) {
            if (getSelectedInputImageHDU() != null && hdu.getHduName() != null && hdu.getHduName().equals(getSelectedInputImageHDU().getHduName())) {
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
        // And continue with remaining ones to find proper name
        for (FitsImageHDU hdu : hdusToAdd) {
            final String tryName = hdu.getHduName() != null ? hdu.getHduName() : (hdu.getExtName() != null ? hdu.getExtName() : filename.substring(0, Math.min(50, filename.length())));
            hdu.setHduName(tryName);

            for (FitsImageHDU currentHDU : getFitsImageHDUs()) {
                if (currentHDU.getHduName() == null) {
                    currentHDU.setHduName("undefined" + currentHDU.getChecksum());
                    logger.warn("present hdu had no hduName {}", currentHDU);
                } else if (currentHDU.getHduName().equals(tryName)) {
                    final String newName = tryName + "-" + now;
                    if (hdu.getHduName().equals(newName)) {
                        // TODO check if this branch can be reached
                        MessagePane.showErrorMessage("HDU already loaded with hduname='" + newName + "', skipping");
                        // TODO propose here to replace the previous loaded HDU
                        hdusToAdd.remove(hdu);
                    } else {
                        String confMsg = "'" + tryName + "' HDU already exists in the available init images.\n Do you agree to rename it '" + newName + "' ? \nElse it will be ignored. ";
                        boolean okToRename = MessagePane.showConfirmMessage(confMsg);
                        if (okToRename) {
                            logger.info("hduname '{}' already used, user accepted to rename to '{}'  ", hdu.getHduName(), newName);
                            hdu.setHduName(newName);
                        } else {
                            hdusToAdd.remove(hdu);
                            logger.info("hduname '{}' already used : user skip prefer to skip it ", hdu.getHduName());
                        }
                    }
                    break;
                }
            }
        }

        this.fitsImageHDUs.addAll(hdusToAdd);
        for (FitsImageHDU hdu : hdusToAdd) {
            this.fitsImageHdu2Filename.put(hdu, filename);
        }

        // select first new one as selected input if anyone has been added
        if (hdusToAdd.isEmpty()) {
            return false;
        }

        setSelectedInputImageHDU(hdusToAdd.get(0));

        return true;
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

                // OIFITS2 are not supported (2017/09/08)
/*
                // keep primary HDU if present (oifits 2 ?)
                final FitsImageHDU primaryHDU = oifitsFile.getPrimaryImageHDU();
                 */
                final List<FitsImageHDU> imageHdus = oifitsFile.getFitsImageHDUs();
                imageHdus.clear();
                // add images:
/*
                if (primaryHDU != null) {
                    imageHdus.add(primaryHDU);
                }
                 */
                imageHdus.add(fitsImageHDU);

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
            MessagePane.showErrorMessage("no ImageHDUs found in " + fitsImageFile.getAbsoluteFilePath(), "Image loading");
        }
    }

    /**
     * Set cliOptions. Blank or null values avoid cli option passing.
     * @param cliOptions software options on command line or null
     */
    public void setCliOptions(String cliOptions) {
        if (StringUtils.isBlank(cliOptions)) {
            this.cliOptions = null;
        } else {
            this.cliOptions = cliOptions;
        }
    }

    public String getCliOptions() {
        return cliOptions;
    }

    public String getImageHDUFilename(FitsImageHDU hdu) {
        return fitsImageHdu2Filename.get(hdu);
    }

    public Service getSelectedService() {
        return selectedService;
    }

    public void setSelectedService(final Service selectedService) {
        this.selectedService = selectedService;
        initSpecificParams();
    }

    public void initSpecificParams() {
        // Get the specific params of given software if any
        selectedService.initSpecificParams(getImageOiData().getInputParam());
    }

    public String toString() {
        return "IRModel [" + oifitsFile + ", " + fitsImageHDUs + ", " + selectedService + "]";
    }

    public File prepareTempFile() throws FitsException, IOException {

        // TODO don't copy file on every loop (because the oifitsFile always is synchronized as an input file model)
        // validate OIFITS:
        final OIFitsChecker checker = new OIFitsChecker();
        oifitsFile.check(checker);
        // validation results
        logger.info("validation results:\n{}", checker.getCheckReport());

        // store original filename
        final String originalAbsoluteFilePath = oifitsFile.getAbsoluteFilePath();

        File tmpFile = FileUtils.getTempFile(oifitsFile.getFileName(), ".export-" + exportCount + ".fits");
        OIFitsWriter.writeOIFits(tmpFile.getAbsolutePath(), oifitsFile);

        //restore filename
        oifitsFile.setAbsoluteFilePath(originalAbsoluteFilePath);

        exportCount++;
        return tmpFile;
    }

    public String getSelectedInputFitsImageError() {
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
            OIFitsFile result = OIFitsLoader.loadOIFits(OIFitsStandard.VERSION_1, resultFile.getAbsolutePath());

            // TODO 1 - show plot for oidata part
            // 2 - show result images
            dataAdded = addFitsImageHDUs(result.getFitsImageHDUs(), result.getAbsoluteFilePath());

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
                addFitsImageHDUs(serviceResult.getOifitsFile().getFitsImageHDUs(), serviceResult.getInputFile().getName());
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
