
package fr.jmmc.oiexplorer.core.model.plot;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AxisRangeMode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="AxisRangeMode"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="Auto"/&gt;
 *     &lt;enumeration value="Default"/&gt;
 *     &lt;enumeration value="Range"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "AxisRangeMode")
@XmlEnum
public enum AxisRangeMode {


    /**
     * 'Auto' indicates to determine automatically the axis bounds according to the data range
     * 
     */
    @XmlEnumValue("Auto")
    AUTO("Auto"),

    /**
     * 'Default' indicates to use the default column range if defined else the 'Auto' mode
     * 
     */
    @XmlEnumValue("Default")
    DEFAULT("Default"),

    /**
     * 'Range' indicates to use the custom range if valid else the 'Default' mode
     * 
     */
    @XmlEnumValue("Range")
    RANGE("Range");
    private final String value;

    AxisRangeMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AxisRangeMode fromValue(String v) {
        for (AxisRangeMode c: AxisRangeMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
