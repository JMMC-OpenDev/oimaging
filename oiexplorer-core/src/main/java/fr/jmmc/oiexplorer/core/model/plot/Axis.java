
package fr.jmmc.oiexplorer.core.model.plot;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import fr.jmmc.oiexplorer.core.model.OIBase;


/**
 * 
 *                 This type describes a plot axis.
 *             
 * 
 * <p>Java class for Axis complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Axis"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="logScale" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="includeZero" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="rangeMode" type="{http://www.jmmc.fr/oiexplorer-core-plot-definition/0.1}AxisRangeMode"/&gt;
 *         &lt;element name="range" type="{http://www.jmmc.fr/oiexplorer-core-plot-definition/0.1}Range" minOccurs="0"/&gt;
 *         &lt;element name="converter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Axis", propOrder = {
    "name",
    "logScale",
    "includeZero",
    "rangeMode",
    "range",
    "converter"
})
public class Axis
    extends OIBase
{

    @XmlElement(required = true)
    protected String name;
    protected boolean logScale;
    protected boolean includeZero;
    @XmlElement(required = true)
    
    protected AxisRangeMode rangeMode;
    protected Range range;
    protected String converter;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the logScale property.
     * 
     */
    public boolean isLogScale() {
        return logScale;
    }

    /**
     * Sets the value of the logScale property.
     * 
     */
    public void setLogScale(boolean value) {
        this.logScale = value;
    }

    /**
     * Gets the value of the includeZero property.
     * 
     */
    public boolean isIncludeZero() {
        return includeZero;
    }

    /**
     * Sets the value of the includeZero property.
     * 
     */
    public void setIncludeZero(boolean value) {
        this.includeZero = value;
    }

    /**
     * Gets the value of the rangeMode property.
     * 
     * @return
     *     possible object is
     *     {@link AxisRangeMode }
     *     
     */
    public AxisRangeMode getRangeMode() {
        return rangeMode;
    }

    /**
     * Sets the value of the rangeMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link AxisRangeMode }
     *     
     */
    public void setRangeMode(AxisRangeMode value) {
        this.rangeMode = value;
    }

    /**
     * Gets the value of the range property.
     * 
     * @return
     *     possible object is
     *     {@link Range }
     *     
     */
    public Range getRange() {
        return range;
    }

    /**
     * Sets the value of the range property.
     * 
     * @param value
     *     allowed object is
     *     {@link Range }
     *     
     */
    public void setRange(Range value) {
        this.range = value;
    }

    /**
     * Gets the value of the converter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConverter() {
        return converter;
    }

    /**
     * Sets the value of the converter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConverter(String value) {
        this.converter = value;
    }
    
//--simple--preserve
    
    /**
    * Return the axis range mode or AxisRangeMode.DEFAULT
    * @return axis range mode or AxisRangeMode.DEFAULT
    */
    public AxisRangeMode getRangeModeOrDefault() {
        if (this.rangeMode == null) {
            this.rangeMode = AxisRangeMode.DEFAULT;
        }
        return rangeMode;
    }
    
    /**
     * Perform a deep-copy of the given other instance into this instance
     * 
     * Note: to be overriden in child class to perform deep-copy of class fields
     * @see OIBase#clone() 
     * 
     * @param other other instance
     */
    @Override
    public void copy(final fr.jmmc.oiexplorer.core.model.OIBase other) {
        final Axis axis = (Axis) other;

        // copy name, logScale, includeZero, plotError, converter:
        this.name = axis.getName();
        this.logScale = axis.isLogScale();
        this.includeZero = axis.isIncludeZero();
        this.rangeMode = axis.getRangeMode();
        this.converter = axis.getConverter();

        // deep copy range:
        this.range = (axis.getRange() != null) ? (Range) axis.getRange().clone() : null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        // identity check:
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Axis other = (Axis) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.logScale != other.logScale) {
            return false;
        }
        if (this.includeZero != other.includeZero) {
            return false;
        }
        if ((this.rangeMode == null) ? (other.rangeMode != null) : !this.rangeMode.equals(other.rangeMode)) {
            return false;
        }
        if ((this.range == null) ? (other.range != null) : !this.range.equals(other.range)) {
            return false;
        }
        if ((this.converter == null) ? (other.converter != null) : !this.converter.equals(other.converter)) {
            return false;
        }
        return true;
    }

    /**
     * toString() implementation using string builder
     * @param sb string builder to append to
     * @param full true to get complete information; false to get main information (shorter)
     */
    @Override
    public void toString(final StringBuilder sb, final boolean full) {
        super.toString(sb, full); // OIBase
        sb.append("{name=").append(this.name);
        if (full) {
            sb.append(", logScale=").append(this.logScale);
            sb.append(", includeZero=").append(this.includeZero);
            sb.append(", rangeMode=").append(this.rangeMode);
            if (this.range != null) {
                sb.append(", range=");
                this.range.toString(sb, full);
            }
            sb.append(", converter=").append(this.converter);
        }
        sb.append('}');
    }
//--simple--preserve

}
