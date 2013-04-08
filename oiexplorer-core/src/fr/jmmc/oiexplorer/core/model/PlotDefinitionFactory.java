/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model;

import fr.jmmc.jmcs.util.jaxb.JAXBFactory;
import fr.jmmc.jmcs.util.jaxb.JAXBUtils;
import fr.jmmc.jmcs.util.jaxb.XmlBindException;
import fr.jmmc.jmcs.util.ResourceUtils;
import fr.jmmc.oiexplorer.core.model.plot.PlotDefinition;
import fr.jmmc.oiexplorer.core.model.plot.PlotDefinitions;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bourgesl, mella
 */
public final class PlotDefinitionFactory {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(PlotDefinitionFactory.class.getName());
    /** default plot */
    public final static String PLOT_DEFAULT = "VIS2DATA_T3PHI/SPATIAL_FREQ"; // TODO: externalize in presets.xml
    /** Presets Filename */
    private final static String PRESETS_FILENAME = "fr/jmmc/oiexplorer/core/resource/plotDefinitionPresets.xml";
    /** Factory instance */
    private static volatile PlotDefinitionFactory instance = null;

    /* members */
    /** default plot definitions */
    private final Map<String, PlotDefinition> defaults = new LinkedHashMap<String, PlotDefinition>(16);

    /** 
     * Return the factory singleton instance 
     * @return factory singleton instance 
     */
    public static PlotDefinitionFactory getInstance() {
        if (instance == null) {
            instance = new PlotDefinitionFactory();
        }
        return instance;
    }

    /**
     * Private constructor for singleton pattern.
     */
    private PlotDefinitionFactory() {
        initializeDefaults();
    }

    /**
     * Initialize default presets extracted from preset file.
     * @throws IllegalStateException if the preset file is not found, an I/O exception occurred, unmarshalling failed
     */
    private void initializeDefaults() throws IllegalStateException {

        PlotDefinitions presets;
        try {
            JAXBFactory jbf = JAXBFactory.getInstance(PlotDefinition.class.getPackage().getName());

            URL presetUrl = ResourceUtils.getResource(PRESETS_FILENAME);

            logger.info("Loading presets from : {}", presetUrl);

            presets = (PlotDefinitions) JAXBUtils.loadObject(presetUrl, jbf);

        } catch (IOException ioe) {
            throw new IllegalStateException("Can't load default preset file from " + PRESETS_FILENAME, ioe);
        } catch (IllegalStateException ise) {
            throw new IllegalStateException("Can't load default preset file from " + PRESETS_FILENAME, ise);
        } catch (XmlBindException xbe) {
            throw new IllegalStateException("Can't load default preset file from " + PRESETS_FILENAME, xbe);
        }

        /* Store defaults computing names (actually, as described in constants ) */
        for (PlotDefinition plotDefinition : presets.getPlotDefinitions()) {
            defaults.put(plotDefinition.getName(), plotDefinition);
        }
    }

    /** 
     * Get default presets.
     * @return List of default plotDefinitions
     */
    public List<String> getDefaultList() {
        return new ArrayList<String>(defaults.keySet());
    }

    /** 
     * Get default presets.
     * @return List of default plotDefinitions
     */
    public Collection<PlotDefinition> getDefaults() {
        return defaults.values();
    }

    /** 
     * Get the default preset given to its name. 
     * @param name name to look for 
     * @return PlotDefinition associated to given name.
     */
    public PlotDefinition getDefault(final String name) {
        if (name == null || name.length() == 0) {
            return null;
        }
        final PlotDefinition plotDefinition = defaults.get(name);
        if (plotDefinition == null) {
            throw new IllegalArgumentException("Plot definition [" + name + "] not found !");
        }
        return plotDefinition;
    }
}
