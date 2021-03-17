/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.function;

import fr.jmmc.jmcs.util.SpecialChars;
import fr.jmmc.oiexplorer.core.model.PlotDefinitionFactory;
import fr.jmmc.oitools.OIFitsConstants;
import java.util.ArrayList;
import java.util.HashMap;
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
    /* converter instances */
    /** Mega Lambda converter to convert spatial frequencies */
    public final static Converter CONVERTER_MEGA_LAMBDA = new ScalingConverter(1e-6d, SpecialChars.UNIT_MEGA_LAMBDA);
    /** meter to micro meter converter to convert wave lengths */
    public final static Converter CONVERTER_MICRO_METER = new ScalingConverter(1e6d, SpecialChars.UNIT_MICRO_METER);
    /** Reflection converter (opposite sign) */
    public final static Converter CONVERTER_REFLECT = new ReflectConverter();
    /* converter keys */
    /** Mega Lambda converter to convert spatial frequencies */
    public final static String KEY_MEGA_LAMBDA = "MEGA_LAMBDA";
    /** meter to micro meter converter to convert wave lengths */
    public final static String KEY_MICRO_METER = "MICRO_METER";
    /** Reflection converter (opposite sign) */
    public final static String KEY_REFLECT = "REFLECT";
    /** Factory instance */
    private static volatile ConverterFactory instance = null;

    /* members */
    /* TODO: load configuration from XML file ?? */
    /** predefined converters */
    private final Map<String, Converter> converters = new LinkedHashMap<String, Converter>(4);
    /** predefined converters by column names */
    private final Map<String, String> converterByColumns = new HashMap<String, String>(16);

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
        // create converters:
        converters.put(KEY_REFLECT, CONVERTER_REFLECT);
        converters.put(KEY_MEGA_LAMBDA, CONVERTER_MEGA_LAMBDA);
        converters.put(KEY_MICRO_METER, CONVERTER_MICRO_METER);

        // associate converters to columns by default:
        converterByColumns.put(OIFitsConstants.COLUMN_EFF_WAVE, KEY_MICRO_METER);
        converterByColumns.put(OIFitsConstants.COLUMN_SPATIAL_FREQ, KEY_MEGA_LAMBDA);
        converterByColumns.put(OIFitsConstants.COLUMN_UCOORD_SPATIAL, KEY_MEGA_LAMBDA);
        converterByColumns.put(OIFitsConstants.COLUMN_VCOORD_SPATIAL, KEY_MEGA_LAMBDA);
        converterByColumns.put(OIFitsConstants.COLUMN_U1COORD_SPATIAL, KEY_MEGA_LAMBDA);
        converterByColumns.put(OIFitsConstants.COLUMN_V1COORD_SPATIAL, KEY_MEGA_LAMBDA);
        converterByColumns.put(OIFitsConstants.COLUMN_U2COORD_SPATIAL, KEY_MEGA_LAMBDA);
        converterByColumns.put(OIFitsConstants.COLUMN_V2COORD_SPATIAL, KEY_MEGA_LAMBDA);
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

    /** 
     * Get the default converter name associated to given to the column name. 
     * @param columnName column name to look for 
     * @return converter name associated to given column name.
     */
    public String getDefaultByColumn(final String columnName) {
        return converterByColumns.get(columnName);
    }
}
