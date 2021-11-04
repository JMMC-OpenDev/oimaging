/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.model;

import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
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
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.jmmc.oitools.model.range.Range;
import fr.nom.tam.fits.FitsDate;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
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
    private FitsImageHDU selectedRglPrioImage;

    /**
     * Which of the input images has been changed last : INIT_IMG or RGL_PRIO.
     * Knowing this, viewerPanel can display the good one.
     * Values: null, "INIT_IMG", "RGL_PRIO"
     */
    private String inputImageView = null;

    /** List of loaded imageHUDs */
    private final List<FitsImageHDU> fitsImageHDUs = new LinkedList<FitsImageHDU>();
    /** Mapping between FitsImageHDU and file names */
    private final Hashtable<FitsImageHDU, String> fitsImageHduToFilenames = new Hashtable<FitsImageHDU, String>(32);
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
        this.selectedRglPrioImage = null;
        this.inputImageView = KEYWORD_INIT_IMG;
        this.fitsImageHDUs.clear();
        this.fitsImageHduToFilenames.clear();
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
            addFitsImageHDUs(oifitsFile.getFitsImageHDUs(), oifitsFile.getFileName());
        }

        // this needs tobe done after the call addFitsImageHDUs()
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
        this.selectedRglPrioImage = null;
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

    /** iterate over the library to find one with same HDUname.
    @param targetHDUName required.
    @return FitsImageHDU if found one with the name, null otherwise.
     */
    private FitsImageHDU findFitsImageHduInLibrary(final String targetHDUName) {

        ListIterator<FitsImageHDU> iterFitsImageHDUs = this.fitsImageHDUs.listIterator();

        while (iterFitsImageHDUs.hasNext()) {
            FitsImageHDU fitsImageHDU = iterFitsImageHDUs.next();

            // if there is another with same name
            if (targetHDUName.equals(fitsImageHDU.getHduName())) {

                return fitsImageHDU;
            }
        }

        // if nothing found
        return null;
    }

    /** Add FitsImageHDU to library (provided it passes some checks).
    @param fitsImageHDU (required)
    @param filename (can be null)
    @return true if added successfully. false otherwise.
     */
    public boolean addFitsImageHDU(final FitsImageHDU hdu, final String filename) {
        try {
            // prepare images (negative values, padding, orientation):
            FitsImageUtils.prepareImages(hdu);
        } catch (IllegalArgumentException iae) {
            MessagePane.showErrorMessage("Unable to load image from file '{}'",
                    (filename != null) ? filename : "null", iae);
            return false;
        }

        if (!hdu.hasImages()) {
            return false;
        }

        // Remove duplicates and skip hdu with hduname present in current input oifits
        if ((getSelectedInputImageHDU() != null) && (hdu.getHduName() != null) && hdu.getHduName().equals(getSelectedInputImageHDU().getHduName())) {
            return false;
        }
        if (existsInImageLib(hdu)) {
            logger.info("skipping image hdu '{}' : already present ", hdu.getHduName());
            return false;
        }

        final String now = DateUtils.now().substring(0, 19);


        final String tryName
                = (hdu.getHduName() != null) ? hdu.getHduName()
                : (hdu.getExtName() != null) ? hdu.getExtName()
                : (filename != null) ? filename.substring(0, Math.min(50, filename.length()))
                        : "UNNAMED";

        hdu.setHduName(tryName);

        for (FitsImageHDU currentHDU : getFitsImageHDUs()) {
            if (currentHDU.getHduName() == null) {
                currentHDU.setHduName("UNDEFINED_" + currentHDU.getChecksum());
                logger.warn("Hdu has no hduName {}", currentHDU);
            } else if (currentHDU.getHduName().equals(tryName)) {
                final String newName = tryName + "-" + now;

                if (hdu.getHduName().equals(newName)) {
                    // TODO check if this branch can be reached
                    MessagePane.showErrorMessage("HDU already loaded with hduname='" + newName + "', skipping");
                    // TODO propose here to replace the previous loaded HDU
                    return false;
                } else {
                    logger.info("hduname '{}' already used, automatically renamed to '{}'.", hdu.getHduName(), newName);
                    hdu.setHduName(newName);
                }
                break;
            }
        }

        // Move FitsImageHDU into model:
        this.fitsImageHDUs.add(hdu);
        if (filename != null) {
            this.fitsImageHduToFilenames.put(hdu, filename);
        }

        // avoid modifying input file while loading OIFits (reentrance):
        if (hdu != oifitsFile.getFitsImageHDUs()) {
            // select first added as selected input
            setSelectedInputImageHDU(hdu);
        }
        return true;
    }

    /** version without filename */
    public boolean addFitsImageHDU(final FitsImageHDU hdu) {
        return addFitsImageHDU(hdu, null);
    }

    /**
     * Add HDU to present ones and select the first new one as selected image input.
     * @param hdus new hdus
     * @param filename filename of given hdu
     * @return true if some hdu have been added
     */
    public boolean addFitsImageHDUs(final List<FitsImageHDU> hdus, final String filename) {
        logger.debug("addFitsImageHDUs: {} ImageHDUs from {}", hdus.size(), filename);

        List<FitsImageHDU> addedHdus = new LinkedList<>();

        hdus.forEach(hdu -> {
            final boolean hduAdded = addFitsImageHDU(hdu, filename);
            if (hduAdded) {
                addedHdus.add(hdu);
            }
        });

        final boolean added = (addedHdus.size() > 0);

        // avoid modifying input file while loading OIFits (reentrance):
        if (hdus != oifitsFile.getFitsImageHDUs()) {
            // select first added as selected input
            if (added) {
                setSelectedInputImageHDU(addedHdus.get(0));
            } else if (selectedInputImageHDU != null) {
                // restore selected image to fix current OifitsFile:
                setSelectedInputImageHDU(selectedInputImageHDU);
            }
        }
        return added;
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
    public FitsImageHDU getSelectedRglPrioImage() {
        return selectedRglPrioImage;
    }

    /**
     * Set given fitsImageHDU as the selected one for input.
     * @param fitsImageHDU image to select (must be present in the previous list
     */
    public void setSelectedInputImageHDU(final FitsImageHDU selectedInitImage) {

        if (selectedInitImage == null || selectedInitImage == NULL_IMAGE_HDU) {
            oifitsFile.getImageOiData().getInputParam().setInitImg("");
            logger.info("Set selectedInputImageHDU to empty.");
        } else {
            String hduName = selectedInitImage.getHduName();

            if (hduName == null) {
                // this imageHDU is probably not an image oi extension
                throw new IllegalStateException("Can't select given image HDU with null HDUNAME");
            }

            if (!existsInImageLib(selectedInitImage)) {
                throw new IllegalStateException(hduName + " HDU was not added !");
            }

            oifitsFile.getImageOiData().getInputParam().setInitImg(hduName);
            logger.info("Set new hdu '{}' as selectedInputImageHDU.", hduName);
        }
        
        this.selectedInputImageHDU = selectedInitImage;
        selectHDUs(); // alter input OIFits in memory
    }

    /**
     * Set given fitsImageHDU as the selected one for Rgl prio.
     * @param selectedRglPrioImage image to select. optional : null means we
     * want no image.
     */
    public void setSelectedRglPrioImage(final FitsImageHDU selectedRglPrioImage) {

        if (selectedRglPrioImage == null || selectedRglPrioImage == NULL_IMAGE_HDU) {
            oifitsFile.getImageOiData().getInputParam().setRglPrio("");
            logger.info("Set selectedRglPrioImage to empty.");
        } else {
            String hduName = selectedRglPrioImage.getHduName();

            if (hduName == null) {
                // this imageHDU is probably not an image oi extension
                throw new IllegalStateException("Can't select given image HDU with null HDUNAME");
            }

            if (!existsInImageLib(selectedRglPrioImage)) {
                throw new IllegalStateException(hduName + " HDU was not added !");
            }

            oifitsFile.getImageOiData().getInputParam().setRglPrio(hduName);
            logger.info("Set new hdu '{}' as selectedRglPrioImage.", hduName);
        }
        
        this.selectedRglPrioImage = selectedRglPrioImage;
        selectHDUs(); // alter input OIFits in memory
    }

    /**
     * select HDUs that are targeted by input image or rgl prio.
     */
    private void selectHDUs() {
        oifitsFile.getFitsImageHDUs().clear();
        if (selectedInputImageHDU != null) {
            oifitsFile.getFitsImageHDUs().add(selectedInputImageHDU);
        }
        if (selectedRglPrioImage != null && selectedRglPrioImage != NULL_IMAGE_HDU) {
            oifitsFile.getFitsImageHDUs().add(selectedRglPrioImage);
        }
    }

    /**
     * tell if the target HDU is in the library of loaded images.
     * this.getFitsImageHDUs() must not be null. this.getFitsImageHDUs() must
     * not contain null elements.
     *
     * @param targetHDU required.
     * @return true if targetHDU has same memory address than one loaded image,
     * or true if targetHDU has same checkSum than one loaded image, or false
     * otherwise.
     */
    private boolean existsInImageLib(final FitsImageHDU targetHDU) {
        for (FitsImageHDU fitsImageHDU : getFitsImageHDUs()) {
            if (fitsImageHDU == targetHDU
                    || fitsImageHDU.getChecksum() == targetHDU.getChecksum()) {
                return true;
            }
        }
        return false;
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
        final List<FitsImageHDU> hdus = fitsImageFile.getFitsImageHDUs();
        if (!hdus.isEmpty()) {
            addFitsImageHDUs(hdus, fitsImageFile.getFileName());
        } else {
            logger.debug("no ImageHDUs found in " + fitsImageFile.getAbsoluteFilePath());
            MessagePane.showErrorMessage("no ImageHDUs found in " + fitsImageFile.getAbsoluteFilePath(), "Image loading");
        }
        // this needs tobe done after the call addFitsImageHDUs()
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

    public String getImageHDUFilename(FitsImageHDU hdu) {
        return fitsImageHduToFilenames.get(hdu);
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
            addFitsImageHDUs(serviceResult.getOifitsFile().getFitsImageHDUs(), serviceResult.getInputFile().getName());
            // this needs tobe done after the call addFitsImageHDUs()
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
            for (FitsImage fitsImage : fitsImageHDU.getFitsImages()) {
                String name = fitsImageHDU.getHduName() + " " + source + " hdu#" + hduIndex;
                if (fitsImage.getImageCount() > 1) {
                    name += " img#" + fitsImage.getImageIndex() + "/" + fitsImage.getImageCount();
                }
                fitsImage.setFitsImageIdentifier(name);
            }
            hduIndex++;
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
