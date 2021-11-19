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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple model for Image Reconstruction based on top of observer/observable pattern.
 *
 * @author mellag
 */
public final class IRModel {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(IRModel.class);

    public final static KeywordMeta KEYWORD_RATING = new KeywordMeta("RATING", "User rating of the result", Types.TYPE_INT);
    public final static KeywordMeta KEYWORD_SOFTWARE = new KeywordMeta("SOFTWARE", "Software used to produce the result", Types.TYPE_CHAR);
    public final static KeywordMeta KEYWORD_START_DATE = new KeywordMeta("JOBSTART", "Starting timestamp of the run", Types.TYPE_CHAR);
    public final static KeywordMeta KEYWORD_END_DATE = new KeywordMeta("JOBEND", "Ending timestamp of the run", Types.TYPE_CHAR);
    public final static KeywordMeta KEYWORD_OIMAGING_COMMENT = new KeywordMeta("USERNOTE", "User comment written by OImaging GUI", Types.TYPE_CHAR);

    /**
     * Null Fits Image HDU. Used in HDU selection lists, as a null item.
     */
    public static final FitsImageHDU NULL_IMAGE_HDU = new FitsImageHDU();

    static {
        NULL_IMAGE_HDU.setHduName("[No Image]");
    }

    /** Role of a HDU */
    private enum Role {
        RESULT, INIT, RGL, NO_ROLE
    };

    /* Members */
    /** Selected algorithm */
    private Service selectedService;
    /** Model oifits File */
    private OIFitsFile oifitsFile;
    /** Optional cliOptions (null value is not fowarded to execution)*/
    private String cliOptions;

    /**
     * Which of the input images has been changed last : INIT_IMG or RGL_PRIO.
     * Knowing this, viewerPanel can display the good one.
     * Values: null, "INIT_IMG", "RGL_PRIO"
     */
    private String inputImageView = null;

    /** Selected input image */
    private FitsImageHDU selectedInputImageHDU;
    /** Selected RGL PRIO image. Can be null. */
    private FitsImageHDU selectedRglPrioImageHdu;

    /** 
     * Global list of FitsImageHDUs.
     * is used in the GUI for selecting an initial image or a regulation image.
     * unicity of HDU_NAME among the library.
     */
    private final List<FitsImageHDU> imageLibrary = new LinkedList<FitsImageHDU>();
    /** List model of target names */
    private final GenericListModel<String> targetListModel = new GenericListModel<String>(new ArrayList<String>(10), true);
    /** List of results */
    private final List<ServiceResult> serviceResults = new LinkedList<ServiceResult>();

    /** 
     * Counter of results since startup.
     *  is used as INDEX each time a result is added.
     *  then is incremented.
     * is reset in IRModel.reset().
     */
    private final AtomicInteger resultCounter = new AtomicInteger(0);

    /** status flag : set by RunAction */
    private boolean running;

    /** export counter */
    private int exportCount;

    protected IRModel() {
        reset();
    }

    /**
     * Reset the main attributes of the model.
     */
    private void reset() {
        this.cliOptions = null;
        this.inputImageView = KEYWORD_INIT_IMG;
        this.selectedInputImageHDU = null;
        this.selectedRglPrioImageHdu = null;
        this.imageLibrary.clear();
        this.serviceResults.clear();
        this.resultCounter.set(0);

        this.running = false;
        this.exportCount = 0;

        // reset oifitsFile and targetListModel
        resetOIFits();
    }

    public File prepareTempFile() throws FitsException, IOException {
        final OIFitsFile oiFitsFile = this.oifitsFile;
        // validate OIFITS:
        final OIFitsChecker checker = new OIFitsChecker();
        oiFitsFile.check(checker);
        // validation results
        logger.info("validation results:\n{}", checker.getCheckReport());

        // store original filename
        final String originalAbsoluteFilePath = oiFitsFile.getAbsoluteFilePath();

        final File tmpFile = FileUtils.getTempFile(oiFitsFile.getFileName(), ".export-" + exportCount + ".fits");

        // Pre-processing:
        // Ensure OIFITS File is correct.
        OIFitsWriter.writeOIFits(tmpFile.getAbsolutePath(), oiFitsFile);

        //restore filename
        oifitsFile.setAbsoluteFilePath(originalAbsoluteFilePath);

        exportCount++;
        return tmpFile;
    }

    private void resetOIFits() {
        loadOIFits(new OIFitsFile(OIFitsStandard.VERSION_1));
    }

    /**
     * Load the given OIFits file as input file.
     * Called by IRModelManager
     *
     * @param oifitsFile oifits file to load
     */
    protected void loadOifitsFile(final OIFitsFile oifitsFile) {
        logger.info("loadOifitsFile: oifitsFile: {}", oifitsFile);

        // reset oidata and put some user data into the new containers
        loadOIFits(oifitsFile);
    }

    /**
     * Load the OiData tables of the model (oifits file, targets).
     * @param oifitsFile OIFitsFile to use
     */
    private void loadOIFits(final OIFitsFile oifitsFile) {
        // change current model immediately:
        this.oifitsFile = oifitsFile;

        // load targets
        this.targetListModel.clear();

        if (oifitsFile.hasOiTarget()) {
            for (String target : oifitsFile.getOiTarget().getTarget()) {
                targetListModel.add(target);
            }
        }

        // get ImageOiData or create a new one
        final ImageOiInputParam inputParam = oifitsFile.getImageOiData().getInputParam();

// TODO: fix loading a result file (restore ImageOiData)
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

        // get roles in the given list order:
        final List<Role> hdusRoles = getHdusRoles(oifitsFile);

        // load all FitsImageHDUs:
        final List<FitsImageHDU> libraryHdus = addFitsImageHDUs(oifitsFile.getFitsImageHDUs(), oifitsFile.getFileName(), null);

        // this needs to be done after the call addFitsImageHDUs()
        // so it uses the (possible) new name
        // it also needs to be called before selecting HDUs
        updateImageIdentifiers(oifitsFile);

        // selecting equivalents for INIT_IMG and RGL_PRIO
        FitsImageHDU initHduEquiv = null;
        FitsImageHDU rglHduEquiv = null;

        for (int i = 0, end = libraryHdus.size(); i < end; i++) {
            final Role role = hdusRoles.get(i);

            switch (role) {
                case INIT:
                    initHduEquiv = libraryHdus.get(i);
                    break;
                case RGL:
                    rglHduEquiv = libraryHdus.get(i);
                    break;
                default:
            }
        }

        // Always update selected images:
        setSelectedInputImageHDU(initHduEquiv);
        setSelectedRglPrioImageHdu(rglHduEquiv);

        // try to guess and set service
        final Service service = ServiceList.getServiceFromOIFitsFile(oifitsFile);
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
    }

    /**
     * Add all FitsImageHDUs of the given FITS file and select first imageHDU if no input image was selected before.
     * Each added HDU get HDUNAME set with original fits filename with suffixe # and extension index.
     * @param fitsImageFile fitsImageFile to load for fitsImageHDU discover
     */
    protected void addFitsImageFile(final FitsImageFile fitsImageFile) {
        final List<FitsImageHDU> hdus = fitsImageFile.getFitsImageHDUs();
        if (!hdus.isEmpty()) {
            // load all FitsImageHDUs:
            final List<FitsImageHDU> libraryHdus = addFitsImageHDUs(hdus, fitsImageFile.getFileName(), null);

            // this needs to be done after the call addFitsImageHDUs()
            // so it uses the (possible) new name
            // it also needs to be called before selecting HDUs
            updateImageIdentifiers(fitsImageFile);

            // select the equivalent of the first image
            final FitsImageHDU firstImageEquiv = libraryHdus.get(0);
            if (firstImageEquiv != null) {
                setSelectedInputImageHDU(firstImageEquiv);
            }
        } else {
            MessagePane.showErrorMessage("no ImageHDUs found in " + fitsImageFile.getAbsoluteFilePath(), "Image loading");
        }
    }

    /**
     * Add FitsImageHDUs to the image library
     * @param hdus new hdus from files (not in the image library)
     * @param filename filename of given hdu
     * @param roles optional list of FitsImageHDU roles to early skip INIT or RGL images
     * @return list of FitsImageHDU present in the image library or NULL
     */
    protected List<FitsImageHDU> addFitsImageHDUs(final List<FitsImageHDU> hdus, final String filename,
                                                  final List<Role> roles) {

        final int nHdus = hdus.size();
        logger.debug("addFitsImageHDUs: {} ImageHDUs from {}", nHdus, filename);

        hdus.forEach(hdu -> {
            // prepare images (negative values, padding, orientation):
            try {
                FitsImageUtils.prepareImages(hdu);
            } catch (IllegalArgumentException iae) {
                MessagePane.showErrorMessage("Unable to prepare images from HDU \"{}\", error: {}", hdu.getHduName(), iae);
            }
        });

        final List<FitsImageHDU> libraryHdus = new ArrayList<>(nHdus);

        for (int i = 0; i < nHdus; i++) {
            final FitsImageHDU hdu = hdus.get(i);
            FitsImageHDU hduRef = null;

            // if earlySkipInitRgl enabled, we early skip when role is INIT or RGL
            if ((roles != null) && (roles.get(i) == Role.INIT || roles.get(i) == Role.RGL)) {
                // skip hdu with hduname present in current input oifits
                if (hdu.getHduName() != null) {
                    if ((getSelectedInputImageHDU() != null)
                            && hdu.getHduName().equals(getSelectedInputImageHDU().getHduName())) {

                        logger.info("skipping image hdu '{}' : already present (InputImage)", hdu.getHduName());
                        hduRef = getSelectedInputImageHDU();

                    } else if ((getSelectedRglPrioImageHdu() != null)
                            && hdu.getHduName().equals(getSelectedRglPrioImageHdu().getHduName())) {
                        logger.info("skipping image hdu '{}' : already present (RglPrioImage)", hdu.getHduName());
                        hduRef = getSelectedRglPrioImageHdu();
                    }
                }
            }
            libraryHdus.add((hduRef != null) ? hduRef : addToImageLibrary(hdu, filename));
        }
        return libraryHdus;
    }

    /** 
     * Add one FitsImageHDU to the image library and replace the previous input image HDU or rgl prior HDU. (modify image)
     * @param hduToReplace a library HDU that is to be replaced by the copy of hdu. (required)
     * @param hdu the HDU to add to the library. (required)
     */
    public void addFitsImageHDUAndSelect(final FitsImageHDU hduToReplace, final FitsImageHDU hdu) {
        // prepare images (negative values, padding, orientation):
        try {
            FitsImageUtils.prepareImages(hdu);
        } catch (IllegalArgumentException iae) {
            MessagePane.showErrorMessage("Unable to prepare images from HDU \"{}\", error: {}", hdu.getHduName(), iae);
        }

        final FitsImageHDU libraryHdu = addToImageLibrary(hdu, null);

        // this needs to be done after the call addFitsImageHDU()
        // so it uses the (possible) new name
        updateImageIdentifiers(hdu);

        if (libraryHdu != null) {
            // change selected image according to the given source reference:
            if (selectedRglPrioImageHdu == hduToReplace) {
                setSelectedRglPrioImageHdu(libraryHdu);
            } else {
                // select first added hdu as selected input
                setSelectedInputImageHDU(libraryHdu);
            }
        }
        // notify model change (to display model):
        IRModelManager.getInstance().fireIRModelChanged(this, null);
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
                setSelectedInputImageHDU(null);
            }
            if (selectedRglPrioImageHdu == hdu) {
                setSelectedRglPrioImageHdu(null);
            }
            // notify model change (to display model):
            IRModelManager.getInstance().fireIRModelUpdated(this, null);
        }
        return removed;
    }

    /** 
     * Return the roles associated to each hdu in the oifitsfile (in the same order)
     * @param oifitsFile the oifitsfile where we take the list of hdus
     * @return the list of roles, each hdu gets a role (if no special role, get NO_ROLE)
     */
    private static List<Role> getHdusRoles(final OIFitsFile oifitsFile) {
        final List<FitsImageHDU> hdus = new ArrayList<>(oifitsFile.getFitsImageHDUs());
        final List<Role> roles = new ArrayList<>(hdus.size());

        // early return when there is no hdus
        if (hdus.isEmpty()) {
            return roles;
        }

        final ImageOiInputParam inputParam = oifitsFile.getImageOiData().getInputParam();
        final ImageOiOutputParam outputParam = oifitsFile.getImageOiData().getOutputParam();

        // result image
        // the output param LAST_IMG contains the HDU_NAME.
        // the result image is always in the first HDU.
        final String lastImgParam = outputParam.getLastImg();

        if (lastImgParam != null && !lastImgParam.isEmpty()) {
            // if the first HDU_NAME is equal to LAST_IMG
            if (lastImgParam.equals(hdus.get(0).getHduName())) {
                roles.add(Role.RESULT);
                hdus.remove(0);
            }
        }
        // TODO: if last_img is missing ?

        // the init image and the rgl image
        // we use the first hdu that correspond to INIT_IMG
        // then the first hdu that correspond to RGL_PRIO
        // the rest of the hdus are marked with NO_ROLE
        final String initImgParam = inputParam.getInitImg();
        final String rglImgParam = inputParam.getRglPrio();

        boolean foundInitImg = false, foundRglImg = false;

        for (FitsImageHDU hdu : hdus) {
            if (!foundInitImg && !StringUtils.isEmpty(initImgParam) && initImgParam.equals(hdu.getHduName())) {
                foundInitImg = true;
                // init image
                roles.add(Role.INIT);
            } else if (!foundRglImg && !StringUtils.isEmpty(rglImgParam) && rglImgParam.equals(hdu.getHduName())) {
                foundRglImg = true;
                // rgl image
                roles.add(Role.RGL);
            } else {
                roles.add(Role.NO_ROLE);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("getHdusRoles[{}] : {}", oifitsFile.getAbsoluteFilePath(), roles);
        }
        return roles;
    }

    // --- selected image Hdu (init and rgl prior) ---
    /**
     * Return the selected imageHDU for input or first one.
     * @return selected fitsImageHDU
     */
    public FitsImageHDU getSelectedInputImageHDU() {
        return selectedInputImageHDU;
    }

    /**
     * Set given fitsImageHDU as the selected one for input.
     * Modify this.oifitsFile params accordingly.
     * Also remove unused HDUs in this.oifitsFile.
     * @param selectedInitImage image to select (must belong to imageLibrary to be selected)
     * @throws IllegalStateException when HDU is not in the library.
     */
    public void setSelectedInputImageHDU(final FitsImageHDU selectedInitImage) {
        final String hduName;
        if (selectedInitImage == null || selectedInitImage == NULL_IMAGE_HDU) {
            hduName = "";
        } else {
            hduName = selectedInitImage.getHduName();

            if (!existInImageLibrary(selectedInitImage)) {
                throw new IllegalStateException(
                        "Cannot select hdu " + hduName + " because it is not in imageLibrary.");
            }
        }

        logger.info("Select hdu '{}' for selectedInputImageHDU.", hduName);

        this.selectedInputImageHDU = selectedInitImage;

        oifitsFile.getImageOiData().getInputParam().setInitImg(hduName);

        updateOifitsFileHDUs(); // alter input OIFits in memory
    }

    /**
     * Return the selected imageHDU for RGL Prio or first one.
     * @return selected fitsImageHDU for RGL Prio
     */
    public FitsImageHDU getSelectedRglPrioImageHdu() {
        return selectedRglPrioImageHdu;
    }

    /**
     * Set given fitsImageHDU as the selected one for Rgl prio.
     * Modify this.oifitsFile params accordingly.
     * Also remove unused HDUs in this.oifitsFile.
     * @param selectedRglPrioImageHdu image to select. (must belong to imageLibrary to be selected)
     * @throws IllegalStateException when HDU is not in the library.
     */
    public void setSelectedRglPrioImageHdu(final FitsImageHDU selectedRglPrioImageHdu) {
        final String hduName;
        if (selectedRglPrioImageHdu == null || selectedRglPrioImageHdu == NULL_IMAGE_HDU) {
            hduName = "";
        } else {
            hduName = selectedRglPrioImageHdu.getHduName();

            if (!existInImageLibrary(selectedRglPrioImageHdu)) {
                throw new IllegalStateException(
                        "Cannot select hdu " + hduName + " because it is not in imageLibrary.");
            }
        }

        logger.info("Select hdu '{}' for selectedRglPrioImageHdu.", hduName);

        this.selectedRglPrioImageHdu = selectedRglPrioImageHdu;

        oifitsFile.getImageOiData().getInputParam().setRglPrio(hduName);

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
        if ((selectedInputImageHDU != null) && (selectedInputImageHDU != NULL_IMAGE_HDU)) {
            oifitsFileHDUs.add(selectedInputImageHDU);
        }

        // rgl image
        if ((selectedRglPrioImageHdu != null) && (selectedRglPrioImageHdu != NULL_IMAGE_HDU)) {
            oifitsFileHDUs.add(selectedRglPrioImageHdu);
        }
    }

    // TODO KILL
    public String getSelectedInputFitsImageError() {
        if (getSelectedInputImageHDU() == null) {
            return "No image data loaded";
        }

        // TODO implement more test in the future to verify HDU confirmity
        return null;
    }

    // --- Image Library handling --- 
    /** 
     * Get a read-only access to the image library.
     * @return imageLibrary read-only (all write will throw an exception).
     */
    public List<FitsImageHDU> getImageLibrary() {
        return Collections.unmodifiableList(imageLibrary);
    }

    /** 
     * Add a FitsImageHDU to the library.
     * the hdu is not added if there is already an checksum-equivalent one in the library.
     * @param hdu hdu to add to imageLibrary. its HDU_NAME can be modified.
     * @param altName alternative name to be used if HDU_NAME of the hdu is empty. (optional)
     * @return the equivalent hdu in the library.
     * if the hdu had no equivalent, return the hdu.
     * if there was en equivalent hdu in imageLibrary, return this equivalent hdu.
     * if there was a problem, return null.
     */
    private FitsImageHDU addToImageLibrary(final FitsImageHDU hdu, final String altName) {

        if (hdu == null || hdu == NULL_IMAGE_HDU) {
            logger.info("HDU not added to image library because it is null.");
            return null;

        } else if (!hdu.hasImages()) {
            logger.info("HDU {} not added because it has no images.", hdu.getHduName());
            return null;
        }

        // return an equivalent if there exists one
        final FitsImageHDU equivalentHDU = findInImageLibrary(hdu);
        if (equivalentHDU != null) {
            logger.info("HDU {} no added to image library because the equivalent HDU {} is already in the library.",
                    hdu.getHduName(), equivalentHDU.getHduName());
            return equivalentHDU;
        }

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
        if (existHduNameInImageLibrary(tryHduName)) {
            // TODO: parse tryHduName to remove date suffix

            // duplicated HDU name found:
            String newName = tryHduName + "-" + DateUtils.now().substring(0, 19);

            // Always ensure name fits in header card:
            if (newName.length() > 68) {
                newName = newName.substring(0, 68);
            }

            if (existHduNameInImageLibrary(newName)) {
                int idx = 1;
                String newNameAlt = newName + "_" + idx;

                for (;;) {
                    if (!existHduNameInImageLibrary(newNameAlt)) {
                        break;
                    }
                    // use another suffix (_nn):
                    idx++;
                    newNameAlt = newName + "_" + idx;
                }
                newName = newNameAlt;
            }
            // name is available:
            logger.info("HDU_NAME '{}' is already used in imageLibrary, renamed to '{}'.", hdu.getHduName(), newName);
            hdu.setHduName(newName);
        }

        // finally, add the hdu to the library
        this.imageLibrary.add(hdu);

        logger.info("Added HDU \"{}\" to imageLibrary.", hdu.getHduName());
        return hdu;
    }

    private boolean existHduNameInImageLibrary(final String hduName) {
        // traverses the library until an equivalent HDU is found
        for (FitsImageHDU libraryHDU : imageLibrary) {
            if (libraryHDU.getHduName().equals(hduName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if the given HDU is in imageLibrary.
     * @param hdu hdu to look up
     */
    private boolean existInImageLibrary(final FitsImageHDU hdu) {
        return (findInImageLibrary(hdu) != null);
    }

    /** 
     * find an equivalent HDU in imageLibrary.
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
     * Identity function to be used for HDUs in imageLibrary.
     * @param first HDU to compare to second. (optional)
     * @param second HDU to compare to first. (optional)
     * @return true if both contains same reference,
     * or if both are null, or if both have same checksum. false otherwise.
     */
    private static boolean hduEquals(final FitsImageHDU first, final FitsImageHDU second) {
        if (first == second) {
            return true; // when they are the same reference or both null
        } else if (first == null || second == null) {
            return false; // when only one of them is null
        } else {
            return first.getChecksum() == second.getChecksum(); // compare the checksums
        }
    }

    // --- ServiceResult handling ---
    public ServiceResult getLastResultSet() {
        // last results is added at the beginning:
        return (getResultSets().isEmpty()) ? null : getResultSets().get(0);
    }

    public List<ServiceResult> getResultSets() {
        return this.serviceResults;
    }

    public void addServiceResult(final ServiceResult serviceResult) {
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

            // get roles in the given list order:
            final List<Role> hdusRoles = getHdusRoles(serviceResult.getOifitsFile());

            // import partially FitsImageHDUs:
            final List<FitsImageHDU> libraryHdus = addFitsImageHDUs(serviceResult.getOifitsFile().getFitsImageHDUs(),
                    serviceResult.getInputFile().getName(), hdusRoles);

            // this needs to be done after the call addFitsImageHDUs()
            // so it uses the (possible) new name
            // it also needs to be called before selecting HDUs
            updateImageIdentifiers(serviceResult);

            // selecting equivalents for INIT_IMG and RGL_PRIO
            FitsImageHDU initHduEquiv = null;
            FitsImageHDU rglHduEquiv = null;

            for (int i = 0, end = libraryHdus.size(); i < end; i++) {
                final Role role = hdusRoles.get(i);

                switch (role) {
                    case RESULT:
                        // select RESULT as initial image
                        initHduEquiv = libraryHdus.get(i);
                        break;
                    case RGL:
                        rglHduEquiv = libraryHdus.get(i);
                        break;
                    default:
                }
            }

            // Always update selected images:
            setSelectedInputImageHDU(initHduEquiv);
            setSelectedRglPrioImageHdu(rglHduEquiv);
        }
        // notify model update
        IRModelManager.getInstance().fireIRModelUpdated(this, null);
    }

    /** 
     * Add some OImaging specific keywords in the OIFitsFile.
     * @param serviceResult required. serviceResult.getOiFitsFile() must not return null.
     */
    private static void postProcessOIFitsFile(final ServiceResult serviceResult) {
        final OIFitsFile oiFitsFile = serviceResult.getOifitsFile();

        final ImageOiOutputParam outputParams = oiFitsFile.getImageOiData().getOutputParam();

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

    private void loadLog(final ServiceResult serviceResult) {
        try {
            serviceResult.loadExecutionLogFile();
        } catch (IOException ioe) {
            logger.error("Can't read content of executionLog file ", ioe);
        }
    }

    // --- updateImageIdentifiers ---
    private void updateImageIdentifiers(FitsImageFile fitsImageFile) {
        updateImageIdentifiers(fitsImageFile.getFitsImageHDUs(), fitsImageFile.getFileName());
    }

    private void updateImageIdentifiers(FitsImageHDU fitsImageHDU) {
        updateImageIdentifiers(fitsImageHDU, "(mem)", 0);
    }

    private void updateImageIdentifiers(ServiceResult serviceResult) {
        updateImageIdentifiers(serviceResult.getOifitsFile().getFitsImageHDUs(), "result#" + serviceResult.getIndex());
    }

    /** 
     * Update fitsImageIdentifier to be more user friendly in the GUI
     * @param fitsImageHDUs list of HDU to rename. the order will be used as the number so be exhaustive.
     * @param source where do the image come from. if from a run, it will be an index number. if not, the file name.
     */
    private void updateImageIdentifiers(final List<FitsImageHDU> fitsImageHDUs, final String source) {
        int hduIndex = 0;
        for (FitsImageHDU fitsImageHDU : fitsImageHDUs) {
            updateImageIdentifiers(fitsImageHDU, source, hduIndex);
            hduIndex++;
        }
    }

    private void updateImageIdentifiers(final FitsImageHDU fitsImageHDU, final String source, final int hduIndex) {
        for (FitsImage fitsImage : fitsImageHDU.getFitsImages()) {
            String name = fitsImageHDU.getHduName() + " " + source + " hdu#" + hduIndex;
            if (fitsImage.getImageCount() > 1) {
                name += " img#" + fitsImage.getImageIndex() + "/" + fitsImage.getImageCount();
            }
            fitsImage.setFitsImageIdentifier(name);
        }
    }

    // --- Service handling ---
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

    // --- Getter / Setter ---
    public OIFitsFile getOifitsFile() {
        return oifitsFile;
    }

    public ImageOiData getImageOiData() {
        return oifitsFile.getImageOiData();
    }

    public GenericListModel<String> getTargetListModel() {
        return targetListModel;
    }

    public String getCliOptions() {
        return cliOptions;
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

    public String getInputImageView() {
        return inputImageView;
    }

    public void setInputImageView(String inputImageView) {
        this.inputImageView = inputImageView;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public String toString() {
        return "IRModel [" + oifitsFile + ", " + imageLibrary + ", " + selectedService + "]";
    }
}
