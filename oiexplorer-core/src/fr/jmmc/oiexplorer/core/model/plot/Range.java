
package fr.jmmc.oiexplorer.core.model.plot;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import fr.jmmc.oiexplorer.core.model.OIBase;


/**
 * 
 *                 This type describes an axis range.
 *             
 * 
 * <p>Java class for Range complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Range">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="min" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="max" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Range", propOrder = {
    "min",
    "max"
})
public class Range
    extends OIBase
{

    protected double min;
    protected double max;

    /**
     * Gets the value of the min property.
     * 
     */
    public double getMin() {
        return min;
    }

    /**
     * Sets the value of the min property.
     * 
     */
    public void setMin(double value) {
        this.min = value;
    }

    /**
     * Gets the value of the max property.
     * 
     */
    public double getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     * 
     */
    public void setMax(double value) {
        this.max = value;
    }
    
//--simple--preserve
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
        final Range range = (Range) other;

        // copy min, max:
        this.min = range.getMin();
        this.max = range.getMax();
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
        final Range other = (Range) obj;
        if (Double.doubleToLongBits(this.min) != Double.doubleToLongBits(other.min)) {
            return false;
        }
        if (Double.doubleToLongBits(this.max) != Double.doubleToLongBits(other.max)) {
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
        sb.append("{min=").append(this.min);
        sb.append(", max=").append(this.max);
        sb.append('}');
    }
//--simple--preserve

}
