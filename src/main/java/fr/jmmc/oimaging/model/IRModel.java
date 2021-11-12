/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.model;

import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.util.DateUtils;
import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.oiexplorer.core.util.FitsImageUtils;
import fr.jmmc.oimaging.services.Service;
import fr.jmmc.oimaging.services.ServiceList;
import fr.jmmc.oimaging.services.ServiceResult;
import fr.jmmc.oitools.image.FitsImage;
import fr.jmmc.oitools.image.FitsImageFile;
import fr.jmmc.oitools.image.FitsImageHDU;
import static fr.jmmc.oitools.image.ImageOiConstants.KEYWORD_INIT_IMG;
import fr.jmmc.oitools.image.ImageOiData;
import fr.jmmc.oitools.image.ImageOiInputParam;
import fr.jmmc.oitools.image.ImageOiOutputParam;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.OIFitsStandard;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.jmmc.oitools.model.range.Range;
import fr.nom.tam.fits.FitsDate;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple model for Image Reconstruction based on top of observer/observable pattern.
 *
 * @author mellag
 */
public class IRModel {

    /**
     * Null Fits Image HDU. Used in HDU selection lists, as a null item.
     */
    public static final FitsImageHDU NULL_IMAGE_HDU = new FitsImageHDU();

    static {
        NULL_IMAGE_HDU.setHduName("[No Image]");
    }

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(IRModel.class);

    public final static KeywordMeta KEYWORD_RATING = new KeywordMeta("RATING", "User rating of the result", Types.TYPE_INT);
    public final static KeywordMeta KEYWORD_SOFTWARE = new KeywordMeta("SOFTWARE", "Software used to produce the result", Types.TYPE_CHAR);
    public final static KeywordMeta KEYWORD_START_DATE = new KeywordMeta("JOBSTART", "Starting timestamp of the run", Types.TYPE_CHAR);
    public final static KeywordMeta KEYWORD_END_DATE = new KeywordMeta("JOBEND", "Ending timestamp of the run", Types.TYPE_CHAR);
    public final static KeywordMeta KEYWORD_OIMAGING_COMMENT = new KeywordMeta("USERNOTE", "User comment written by OImaging GUI", Types.TYPE_CHAR);

    /* Members */
    /** Selected algorithm */
    private Service selectedService;
    /** Model oifits File */
    private OIFitsFile oifitsFile;
    /** Optional cliOptions (null value is not fowarded to execution)*/
    private String cliOptions;

    /** Selected input image */
    private FitsImageHDU selectedInputImageHDU;
    /** Selected RGL PRIO image. Can be null. */
    private FitsImageHDU selectedRglPrioImageHdu;

    /**
     * Which of the input images has been changed last : INIT_IMG or RGL_PRIO.
     * Knowing this, viewerPanel can display the good one.
     * Values: null, "INIT_IMG", "RGL_PRIO"
     */
    private String inputImageView = null;

    /** Global list of FitsImageHDUs.
     * is used in the GUI for selecting an initial image or a regulation image.
     * unicity of HDU_NAME among the library.
     */
    private final List<FitsImageHDU> imageLibrary = new LinkedList<FitsImageHDU>();
    /** List model of target names */
    private final GenericListModel<String> targetListModel = new GenericListModel<String>(new ArrayList<String>(10), true);
    /** List of results */
    private final List<ServiceResult> serviceResults = new LinkedList<ServiceResult>();

    /** counter of results since startup.
     *  is used as INDEX each time a result is added.
     *  then is incremented.
     * is reset in IRModel.reset().
     */
    private AtomicInteger resultCounter = new AtomicInteger(0);

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
        this.cliOptions = null;
        this.selectedInputImageHDU = null;
        this.selectedRglPrioImageHdu = null;
        this.inputImageView = KEYWORD_INIT_IMG;
        this.imageLibrary.clear();
        this.serviceResults.clear();
        this.resultCounter.set(0);

        this.running = false;
        this.exportCount = 0;

        resetOIFits();
    }

    private void resetOIFits() {
        loadOIFits(new OIFitsFile(OIFitsStandard.VERSION_1));
    }

    /**
     * Load the OiData tables of the model (oifits file, targets).
     * @param oifitsFile OIFitsFile to use
     */
    private void loadOIFits(final OIFitsFile oifitsFile) {
        this.oifitsFile = oifitsFile;
        // load inputImageOiData or create a new one
        final ImageOiInputParam inputParam = oifitsFile.getImageOiData().getInputParam();

        // load targets
        this.targetListModel.clear();

        if (oifitsFile.hasOiTarget()) {
            for (String target : oifitsFile.getOiTarget().getTarget()) {
                targetListModel.add(target);
            }
        }
        // Select first target by default
        // assume we have one
        if (targetListModel.isEmpty()) {
            inputParam.setTarget("MISSING TARGET");
        } else {
            inputParam.setTarget(targetListModel.get(0));
        }

        // Reset WLen bounds
        final Range effWaveRange = oifitsFile.getWavelengthRange();
        inputParam.setWaveMin(effWaveRange.getMin());
        inputParam.setWaveMax(effWaveRange.getMax());

        // Reset observable use according available tables
        inputParam.useVis(oifitsFile.hasOiVis());
        inputParam.useVis2(oifitsFile.hasOiVis2());
        inputParam.useT3(oifitsFile.hasOiT3());

        // load fits Image HDU data if any present
        // Select first image as selected one if not yet initialized
        if (!oifitsFile.getFitsImageHDUs().isEmpty()) {
            addFitsImageHDUs(oifitsFile.getFitsImageHDUs(), oifitsFile.getFileName(),
                    null, false);
        }
        // this needs to be done after the call addFitsImageHDUs()
        // so it uses the (possible) new name
        updateImageIdentifiers(oifitsFile);

        // try to guess and set service
        Service service = ServiceList.getServiceFromOIFitsFile(oifitsFile);
        if (service == null) {
            // avoid null service
            if (getSelectedService() == null) {
                // Note: setSelectedService() calls initSpecificParams():
                setSelectedService(ServiceList.getPreferedService());
            } else {
                initSpecificParams(false);
            }
        } else {
            if ((getSelectedService() == null) || (!service.getProgram().equals(getSelectedService().getProgram()))) {
                // Note: setSelectedService() calls initSpecificParams():
                setSelectedService(service);
            } else {
                initSpecificParams(false);
            }
        }

        // TODO: should restore INIT_IMG & RGL_PRIOR ?
        this.selectedInputImageHDU = null;
        this.selectedRglPrioImageHdu = null;
    }

    private static Set<String> getInputImageRefs(final OIFitsFile oifitsFile) {
        final Set<String> inputImageRefs = new HashSet<>(4);

        final ImageOiInputParam inputParam = oifitsFile.getImageOiData().getInputParam();

        final String initImg = inputParam.getInitImg();
        if (!StringUtils.isEmpty(initImg)) {
            inputImageRefs.add(initImg);
        }

        final String rglPrio = inputParam.getRglPrio();
        if (!StringUtils.isEmpty(rglPrio)) {
            inputImageRefs.add(rglPrio);
        }
        logger.debug("getInputImageRefs[{}] : {}", oifitsFile.getAbsoluteFilePath(), inputImageRefs);
        return inputImageRefs;
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
     * @param userOifitsFile
     */
    public void loadOifitsFile(final OIFitsFile userOifitsFile) {
        logger.info("loadOifitsFile: userOifitsFile: {}", userOifitsFile);

        // reset oidata and put some user data into the new containers
        loadOIFits(userOifitsFile);
    }

    /**
     * Add many HDUs to present ones and select the first new one as input image.
     * @param hdus new hdus
     * @param filename filename of given hdu
     * @param inputImageRefs HDU names of input images (to check)
     * @param addAndSelectFirstHDU always add and select the first hdu in `hdus`
     * @return true if some hdu have been added
     */
    public boolean addFitsImageHDUs(final List<FitsImageHDU> hdus, final String filename,
            final Set<String> inputImageRefs, final boolean addAndSelectFirstHDU) {

        logger.debug("addFitsImageHDUs: {} ImageHDUs from {}", hdus.size(), filename);

        final List<FitsImageHDU> addedHdus = new ArrayList<>(hdus.size());

        boolean firstHduAdded = false;

        for (int i = 0; i < hdus.size(); i++) {
            final FitsImageHDU hdu = hdus.get(i);

            final boolean checkSelectedImageHDUs;
            if (addAndSelectFirstHDU && i == 0) {
                checkSelectedImageHDUs = false;
            } else {
                checkSelectedImageHDUs = (inputImageRefs != null) && inputImageRefs.contains(hdu.getHduName());
            }

            final boolean hduAdded = addFitsImageHDU(hdu, filename, checkSelectedImageHDUs);
            if (hduAdded) {
                addedHdus.add(hdu);
                if (i == 0) {
                    firstHduAdded = true;
                }
            }
        }

        final boolean added = !addedHdus.isEmpty();

        // avoid modifying input file while loading OIFits (reentrance):
        if (hdus != oifitsFile.getFitsImageHDUs()) {
            final FitsImageHDU selectedInitImage;
            if (added) {
                // select first added hdu as selected input
                selectedInitImage = addedHdus.get(0);

                // logging if first HDU should have been added
                if (addAndSelectFirstHDU && !firstHduAdded) {
                    logger.debug("First HDU {} should have been added.",
                            hdus.get(0).getHduName());
                }
            } else {
                // restore selected image (even null) to fix current OifitsFile:
                selectedInitImage = selectedInputImageHDU;
            }
            setSelectedInputImageHDU(selectedInitImage);
        }
        return added;
    }

    /** 
     * Add one HDU to the image library and replace the previous input image HDU or rgl prior HDU. (modify image)
     * @param hduToReplace a library HDU that is to be replaced by the copy of hdu. (required)
     * @param hdu the HDU to add to the library. (required)
     */
    public void addFitsImageHDUAndSelect(final FitsImageHDU hduToReplace, final FitsImageHDU hdu) {
        // do not checkSelectedImageHDUs (hduName):
        final boolean added = addFitsImageHDU(hdu, null, false);

        // this needs to be done after the call addFitsImageHDU()
        // so it uses the (possible) new name
        updateImageIdentifiers(hdu);

        if (added) {
            // change selected image according to the given source reference:
            if (selectedRglPrioImageHdu == hduToReplace) {
                setSelectedRglPrioImageHdu(hdu);
            } else {
                // select first added hdu as selected input
                setSelectedInputImageHDU(hdu);
            }
        }
        // notify model change (to display model):
        IRModelManager.getInstance().fireIRModelChanged(this, null);
    }

    /** 
     * Add one HDU to the image library.
     * @param hdu (required)
     * @param filename (can be null)
     * @param checkSelectedImageHDUs true to skip HDU whose hduname is present in current input oifits
     * @return true if added successfully. false otherwise.
     */
    private boolean addFitsImageHDU(final FitsImageHDU hdu, final String filename, final boolean checkSelectedImageHDUs) {
        if (!hdu.hasImages()) {
            logger.info("HDU {} not added because it has no images.", hdu.getHduName());
            return false;
        }
        try {
            // prepare images (negative values, padding, orientation):
            FitsImageUtils.prepareImages(hdu);
        } catch (IllegalArgumentException iae) {
            MessagePane.showErrorMessage("Unable to prepare image from file '{}'", filename, iae);
            return false;
        }

        // Remove duplicates and skip hdu with hduname present in current input oifits
        if (checkSelectedImageHDUs && (hdu.getHduName() != null)) {
            if ((getSelectedInputImageHDU() != null) && hdu.getHduName().equals(getSelectedInputImageHDU().getHduName())) {
                logger.info("skipping image hdu '{}' : already present (InputImage)", hdu.getHduName());
                return false;
            }
            if ((getSelectedRglPrioImageHdu() != null) && hdu.getHduName().equals(getSelectedRglPrioImageHdu().getHduName())) {
                logger.info("skipping image hdu '{}' : already present (RglPrioImage)", hdu.getHduName());
                return false;
            }
        }

        // Add FitsImageHDU into model:
        final boolean added = addToImageLibrary(hdu, filename, true);

        return added;
    }

    /**
     * Remove the given image from the image library and cleanup references if needed
     * @param hdu image to remove
     * @return true if removed; false otherwise
     */
    public boolean removeFitsImageHDU(final FitsImageHDU hdu) {
        // check if image is used ?
        final boolean removed = this.imageLibrary.remove(hdu);

        if (removed) {
            // cleanup references:
            if (selectedInputImageHDU == hdu) {
                selectedInputImageHDU = null;
            }
            if (selectedRglPrioImageHdu == hdu) {
                selectedRglPrioImageHdu = null;
            }
            // notify model change (to display model):
            IRModelManager.getInstance().fireIRModelChanged(this, null);
        }
        return removed;
    }

    /** Get a read-only access to the image library.
     * @return imageLibrary read-only (all write will throw an exception).
     */
    public List<FitsImageHDU> getImageLibrary() {
        return Collections.unmodifiableList(imageLibrary);
    }

    public GenericListModel<String> getTargetListModel() {
        return targetListModel;
    }

    /**
     * Return the selected imageHDU for input or first one.
     * @return selected fitsImageHDU
     */
    public FitsImageHDU getSelectedInputImageHDU() {
        return selectedInputImageHDU;
    }

    /**
     * Return the selected imageHDU for RGL Prio or first one.
     * @return selected fitsImageHDU for RGL Prio
     */
    public FitsImageHDU getSelectedRglPrioImageHdu() {
        return selectedRglPrioImageHdu;
    }

    /**
     * Set given fitsImageHDU as the selected one for input.
     * Modify this.oifitsFile params accordingly.
     * Also remove unused HDUs in this.oifitsFile.
     * @param selectedInitImage image to select (must belong to imageLibrary to be selected)
     * @throws IllegalStateException when HDU is not in the library.
     */
    public void setSelectedInputImageHDU(final FitsImageHDU selectedInitImage) {

        if (selectedInitImage == null || selectedInitImage == NULL_IMAGE_HDU) {
            oifitsFile.getImageOiData().getInputParam().setInitImg("");
            logger.info("Select no hdu for selectedInputImageHDU.");
        } else {

            final String hduName = selectedInitImage.getHduName();

            if (!existInImageLibrary(selectedInitImage)) {
                throw new IllegalStateException(
                        "Cannot select hdu " + hduName + " because it is not in imageLibrary.");
            }

            oifitsFile.getImageOiData().getInputParam().setInitImg(hduName);
            logger.info("Select hdu '{}' for selectedInputImageHDU.", hduName);
        }

        this.selectedInputImageHDU = selectedInitImage;
        updateOifitsFileHDUs(); // alter input OIFits in memory
    }

    /**
     * Set given fitsImageHDU as the selected one for Rgl prio.
     * Modify this.oifitsFile params accordingly.
     * Also remove unused HDUs in this.oifitsFile.
     * @param selectedRglPrioImageHdu image to select. (must belong to imageLibrary to be selected)
     * @throws IllegalStateException when HDU is not in the library.
     */
    public void setSelectedRglPrioImageHdu(final FitsImageHDU selectedRglPrioImageHdu) {

        if (selectedRglPrioImageHdu == null || selectedRglPrioImageHdu == NULL_IMAGE_HDU) {
            oifitsFile.getImageOiData().getInputParam().setRglPrio("");
            logger.info("Select no hdu for selectedRglPrioImage.");
        } else {
            final String hduName = selectedRglPrioImageHdu.getHduName();

            if (!existInImageLibrary(selectedRglPrioImageHdu)) {
                throw new IllegalStateException(
                        "Cannot select hdu " + hduName + " because it is not in imageLibrary.");
            }

            oifitsFile.getImageOiData().getInputParam().setRglPrio(hduName);
            logger.info("Select hdu '{}' for selectedRglPrioImage.", hduName);
        }

        this.selectedRglPrioImageHdu = selectedRglPrioImageHdu;
        updateOifitsFileHDUs(); // alter input OIFits in memory
    }

    /**
     * Look at the selected HDUs in the input form, and reference them in this.oifitsFile.
     * Do not reference them if they are null or NULL_IMAGE_HDU.
     */
    private void updateOifitsFileHDUs() {
        final List<FitsImageHDU> oifitsFileHDUs = oifitsFile.getFitsImageHDUs();

        oifitsFileHDUs.clear();

        // init image
        if (selectedInputImageHDU != null && selectedRglPrioImageHdu != NULL_IMAGE_HDU) {
            oifitsFileHDUs.add(selectedInputImageHDU);
        }

        // rgl image
        if (selectedRglPrioImageHdu != null && selectedRglPrioImageHdu != NULL_IMAGE_HDU) {
            oifitsFileHDUs.add(selectedRglPrioImageHdu);
        }
    }

    /** Identity function to be used for HDUs in imageLibrary.
     * @param first HDU to compare to second. (optional)
     * @param second HDU to compare to first. (optional)
     * @return true if both contains same reference,
     * or if both are null, or if both have same checksum. false otherwise.
     */
    private boolean hduEquals(final FitsImageHDU first, final FitsImageHDU second) {
        if (first == second) {
            return true; // when they are the same reference or both null
        } else if (first == null || second == null) {
            return false; // when only one of them is null
        } else {
            return first.getChecksum() == second.getChecksum(); // compare the checksums
        }
    }

    /** find an equivalent HDU in imageLibrary.
     * @param hdu we want to find an equivalent to this HDU. (optional)
     * @return an equivalent HDU from imageLibrary
     * return null when no equivalent HDU has been found, or when hdu is null.
     */
    private FitsImageHDU findInImageLibrary(final FitsImageHDU hdu) {
        // no need to search if hdu is null
        if (hdu == null) {
            return null;
        }
        // traverses the library until an equivalent HDU is found
        for (FitsImageHDU libraryHDU : imageLibrary) {
            if (hduEquals(libraryHDU, hdu)) {
                return libraryHDU; // return the HDU from the library, not the target one !
            }
        }
        // when no equivalent HDU is found, return null
        return null;
    }

    /**
     * Return true if the given HDU is in imageLibrary.
     * @param hdu hdu to look up
     */
    private boolean existInImageLibrary(final FitsImageHDU hdu) {
        return (findInImageLibrary(hdu) != null);
    }

    /** add a HDU to the library.
     * @param hdu hdu to add to imageLibrary. its HDU_NAME can be modified.
     * @param altName alternative name to be used if HDU_NAME of the hdu is empty. (optional)
     * @param checkHdu enable checking of the hdu.
     * if enabled, and hdu is null or already in image library, hdu is not added.
     * @return true if added. false otherwise.
     */
    private boolean addToImageLibrary(final FitsImageHDU hdu, final String altName, final boolean checkHdu) {
        boolean hduOK = true;

        if (checkHdu) {
            if (hdu == null || hdu == NULL_IMAGE_HDU) {
                hduOK = false;
                logger.info("HDU not added to image library because it is null.");

            } else if (existInImageLibrary(hdu)) {
                hduOK = false;
                logger.info("HDU {} no added to image library because an equivalent HDU is already in the library.", hdu.getHduName());

            }
        }

        if (hduOK) {

            // Ensure that HDU_NAME is unique among imageLibrary

            String tryHduName = (!StringUtils.isEmpty(hdu.getHduName())) ? hdu.getHduName()
                    : (altName != null) ? altName.substring(0, Math.min(50, altName.length())) : null;

            if (StringUtils.isEmpty(tryHduName)) {
                tryHduName = "UNDEFINED_" + hdu.getChecksum();
                logger.warn("Hdu has no hduName {}, using '{}'", hdu, tryHduName);
            }
            // hdu name is always set:

            hdu.setHduName(tryHduName);

            // Fix duplicated hduName in the image library:
            final String now = DateUtils.now().substring(0, 19);

            for (FitsImageHDU libraryHDU : imageLibrary) {
                if (libraryHDU.getHduName().equals(tryHduName)) {
                    final String newName = tryHduName + "-" + now;

                    if (hdu.getHduName().equals(newName)) {
                        // TODO check if this branch can be reached
                        MessagePane.showErrorMessage("HDU already loaded with hduname='" + newName + "', skipping");
                        // TODO propose here to replace the previous loaded HDU
                        return false;
                    } else {
                        logger.info("HDU_NAME '{}' is already used in imageLibrary, so it has been renamed to '{}'.",
                                hdu.getHduName(), newName);
                        hdu.setHduName(newName);
                    }
                    break;
                }
            }

            // finally, add the hdu to the library
            final boolean added = this.imageLibrary.add(hdu);
            if (added) {
                logger.info("Added HDU \"{}\" to imageLibrary.", hdu.getHduName());
            } else {
                logger.info("Could not add HDU \"{}\" to imageLibrary for a unknown reason.", hdu.getHduName());
            }
            return added;

        } else {
            // checks failed, so we don't add the hdu
            return false;
        }
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
        final List<FitsImageHDU> hdus = fitsImageFile.getFitsImageHDUs();
        if (!hdus.isEmpty()) {
            addFitsImageHDUs(hdus, fitsImageFile.getFileName(), null, false);
        } else {
            logger.debug("no ImageHDUs found in " + fitsImageFile.getAbsoluteFilePath());
            MessagePane.showErrorMessage("no ImageHDUs found in " + fitsImageFile.getAbsoluteFilePath(), "Image loading");
        }
        // this needs to be done after the call addFitsImageHDUs()
        // so it uses the (possible) new name
        updateImageIdentifiers(fitsImageFile);
    }

    /**
     * Set cliOptions. Blank or null values avoid cli option passing.
     * @param cliOptions software options on command line or null
     */
    public void setCliOptions(String cliOptions) {
        if (StringUtils.isEmpty(cliOptions)) {
            this.cliOptions = null;
        } else {
            this.cliOptions = cliOptions;
        }
    }

    public String getCliOptions() {
        return cliOptions;
    }

    public Service getSelectedService() {
        return selectedService;
    }

    public void setSelectedService(final Service selectedService) {
        this.selectedService = selectedService;
        initSpecificParams(false);
    }

    public void initSpecificParams(final boolean applyDefaults) {
        // Get the specific params of given software if any
        selectedService.initSpecificParams(getImageOiData().getInputParam(), applyDefaults);
    }

    public String toString() {
        return "IRModel [" + oifitsFile + ", " + imageLibrary + ", " + selectedService + "]";
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

        // Pre-processing:
        // Ensure OIFITS File is correct.
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

    private void loadLog(final ServiceResult serviceResult) {
        try {
            serviceResult.loadExecutionLogFile();
        } catch (IOException ioe) {
            logger.error("Can't read content of executionLog file ", ioe);
        }
    }

    public void showLog(final String prefixMessage, final ServiceResult serviceResult, final Exception e) {
        loadLog(serviceResult);
        final String executionLog = serviceResult.getExecutionLog();
        MessagePane.showErrorMessage((executionLog != null) ? (prefixMessage + "\n\n" + executionLog) : prefixMessage, e);
    }

    public ServiceResult getLastResultSet() {
        // last results is added at the beginning:
        return (getResultSets().isEmpty()) ? null : getResultSets().get(0);
    }

    public void addServiceResult(ServiceResult serviceResult) {
        loadLog(serviceResult);
        // Load result:
        try {
            serviceResult.loadOIFitsFile();
        } catch (FitsException fe) {
            logger.error("Can't get imageHDU from result oifile", fe);
        } catch (IOException ioe) {
            logger.error("Can't get imageHDU from result oifile", ioe);
        }
        // last results is added at the beginning:
        getResultSets().add(0, serviceResult);

        if (serviceResult.isValid()) {
            serviceResult.setIndex(resultCounter.incrementAndGet());
            postProcessOIFitsFile(serviceResult);

            addFitsImageHDUs(serviceResult.getOifitsFile().getFitsImageHDUs(), serviceResult.getInputFile().getName(),
                    getInputImageRefs(serviceResult.getOifitsFile()), true);

            // this needs to be done after the call addFitsImageHDUs()
            // so it uses the (possible) new name
            updateImageIdentifiers(serviceResult);
        }

        // notify model update
        IRModelManager.getInstance().fireIRModelUpdated(this, null);
    }

    /** updates fitsImageIdentifier to be more user friendly in the GUI
     * @param fitsImageHDUs list of HDU to rename. the order will be used as the number so be exhaustive.
     * @param source where do the image come from. if from a run, it will be an index number. if not, the file name.
     */
    private void updateImageIdentifiers(List<FitsImageHDU> fitsImageHDUs, String source) {
        int hduIndex = 0;
        for (FitsImageHDU fitsImageHDU : fitsImageHDUs) {
            updateImageIdentifiers(fitsImageHDU, source, hduIndex);
            hduIndex++;
        }
    }

    private void updateImageIdentifiers(FitsImageHDU fitsImageHDU, String source, int hduIndex) {
        for (FitsImage fitsImage : fitsImageHDU.getFitsImages()) {
            String name = fitsImageHDU.getHduName() + " " + source + " hdu#" + hduIndex;
            if (fitsImage.getImageCount() > 1) {
                name += " img#" + fitsImage.getImageIndex() + "/" + fitsImage.getImageCount();
            }
            fitsImage.setFitsImageIdentifier(name);
        }
    }

    /** updates fitsImageIdentifier with the index as source */
    private void updateImageIdentifiers(ServiceResult serviceResult) {
        updateImageIdentifiers(serviceResult.getOifitsFile().getFitsImageHDUs(), "result#" + serviceResult.getIndex());
    }

    /** updates fitsImageIdentifier with the index as source */
    private void updateImageIdentifiers(FitsImageFile fitsImageFile) {
        updateImageIdentifiers(fitsImageFile.getFitsImageHDUs(), fitsImageFile.getFileName());
    }

    /** updates fitsImageIdentifier with the index as source */
    private void updateImageIdentifiers(FitsImageHDU fitsImageHDU) {
        updateImageIdentifiers(fitsImageHDU, "(mem)", 0);
    }

    /** Add some OIMaging specific keywords in the OIFitsFile.
     * @param serviceResult required. serviceResult.getOiFitsFile() must not return null.
     */
    private static void postProcessOIFitsFile(final ServiceResult serviceResult) {
        final OIFitsFile oiFitsFile = serviceResult.getOifitsFile();

        ImageOiOutputParam outputParams = oiFitsFile.getImageOiData().getOutputParam();

        outputParams.addKeyword(KEYWORD_RATING);
        // set KEYWORD_RATING if missing
        outputParams.setKeywordDefaultInt(KEYWORD_RATING.getName(), 0);

        outputParams.addKeyword(KEYWORD_SOFTWARE);
        // set KEYWORD_SOFTWARE if missing
        outputParams.setKeywordDefault(KEYWORD_SOFTWARE.getName(), serviceResult.getService().getName());

        outputParams.addKeyword(KEYWORD_START_DATE);
        // set KEYWORD_START_DATE if missing
        outputParams.setKeywordDefault(
                KEYWORD_START_DATE.getName(), FitsDate.getFitsDateString(serviceResult.getStartTime()));

        outputParams.addKeyword(KEYWORD_END_DATE);
        // set KEYWORD_END_DATE if missing
        outputParams.setKeywordDefault(KEYWORD_END_DATE.getName(), FitsDate.getFitsDateString(serviceResult.getEndTime()));

        outputParams.addKeyword(KEYWORD_OIMAGING_COMMENT);
        // set KEYWORD_OIMAGING_COMMENT if missing
        outputParams.setKeywordDefault(KEYWORD_OIMAGING_COMMENT.getName(), "");
    }

    public void removeServiceResult(ServiceResult serviceResultToDelete) {
        getResultSets().remove(serviceResultToDelete);
        // notify model update
        IRModelManager.getInstance().fireIRModelUpdated(this, null);
    }

    public void removeServiceResults(List<ServiceResult> selectedServicesList) {
        getResultSets().removeAll(selectedServicesList);
        // notify model update
        IRModelManager.getInstance().fireIRModelUpdated(this, null);
    }

    /**
     * @return the inputImageView
     */
    public String getInputImageView() {
        return inputImageView;
    }

    /**
     * @param inputImageView the inputImageView to set
     */
    public void setInputImageView(String inputImageView) {
        this.inputImageView = inputImageView;
    }
}
