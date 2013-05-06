
package fr.jmmc.oiexplorer.core.model.plot;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ColorMapping.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ColorMapping">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="WavelengthRange"/>
 *     &lt;enumeration value="StationIndex"/>
 *     &lt;enumeration value="Configuration"/>
 *     &lt;enumeration value="ObservationDate"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ColorMapping")
@XmlEnum
public enum ColorMapping {


    /**
     * 'WavelengthRange' indicates to use the effective wave length (EFF_WAVE)
     * 
     */
    @XmlEnumValue("WavelengthRange")
    WAVELENGTH_RANGE("WavelengthRange"),

    /**
     * 'StationIndex' indicates to use the station index array (STA_INDEX) (baseline or triplet)
     * 
     */
    @XmlEnumValue("StationIndex")
    STATION_INDEX("StationIndex"),

    /**
     * 'Configuration' indicates to use the station configuration (STA_CONF)
     * 
     */
    @XmlEnumValue("Configuration")
    CONFIGURATION("Configuration"),

    /**
     * 'ObservationDate' indicates to use the observation date (DATE_OBS)
     * 
     */
    @XmlEnumValue("ObservationDate")
    OBSERVATION_DATE("ObservationDate");
    private final String value;

    ColorMapping(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ColorMapping fromValue(String v) {
        for (ColorMapping c: ColorMapping.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
