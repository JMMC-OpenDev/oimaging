/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.function;

import fr.jmmc.oiexplorer.core.model.PlotDefinitionFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bourgesl
 */
public final class ConverterFactory {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(PlotDefinitionFactory.class.getName());
    /* converter keys */
    /** Mega Lambda converter to convert spatial frequencies */
    public final static String CONVERTER_MEGA_LAMBDA = "MEGA_LAMBDA";
    /** meter to micro meter converter to convert wave lengths */
    public final static String CONVERTER_MICRO_METER = "MICRO_METER";
    /** Factory instance */
    private static volatile ConverterFactory instance = null;

    /* members */
    /** predefined converters : TODO could be loaded from XML file ?? */
    private final Map<String, Converter> converters = new LinkedHashMap<String, Converter>(4);

    /** 
     * Return the factory singleton instance 
     * @return factory singleton instance 
     */
    public static ConverterFactory getInstance() {
        if (instance == null) {
            instance = new ConverterFactory();
        }
        return instance;
    }

    /**
     * Private constructor for singleton pattern.
     */
    private ConverterFactory() {
        initializeDefaults();
    }

    /**
     * Initialize default presets extracted from preset file.
     * @throws IllegalStateException if the preset file is not found, an I/O exception occured, unmarshalling failed
     */
    private void initializeDefaults() throws IllegalStateException {
        converters.put(CONVERTER_MEGA_LAMBDA, new ScalingConverter(1e-6d, "M\u03BB"));
        converters.put(CONVERTER_MICRO_METER, new ScalingConverter(1e6d, "micrometer"));
    }

    /** 
     * Get converter keys.
     * @return List of converter keys
     */
    public List<String> getDefaultList() {
        return new ArrayList<String>(converters.keySet());
    }

    /** 
     * Get the converter given to its name. 
     * @param name name to look for 
     * @return Converter associated to given name.
     */
    public Converter getDefault(final String name) {
        if (name == null || name.length() == 0) {
            return null;
        }
        final Converter converter = converters.get(name);
        if (converter == null) {
            throw new IllegalArgumentException("Converter [" + name + "] not found !");
        }
        return converter;
    }
}
