
package fr.jmmc.oiexplorer.core.model.oi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import fr.jmmc.oiexplorer.core.model.OIBase;


/**
 * 
 *                 This type describes a target unique identifier among the OIDataCollection
 *             
 * 
 * <p>Java class for TargetUID complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TargetUID">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="target" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TargetUID", propOrder = {
    "target"
})
public class TargetUID
    extends OIBase
{

    @XmlElement(required = true)
    protected String target;

    /**
     * Gets the value of the target property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTarget() {
        return target;
    }

    /**
     * Sets the value of the target property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTarget(String value) {
        this.target = value;
    }
    
//--simple--preserve
    /**
     * Constructor for JAXB
     */
    public TargetUID() {
    }

    /**
     * Constructor
     * @param target target name
     */
    public TargetUID(final String target) {
        this.target = target;
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
        final TargetUID targetUID = (TargetUID) other;

        // copy target:
        this.target = targetUID.getTarget();
    }

    /**
     * Return the hashCode() of the target name 
     * @return hashCode() of the target name 
     */
    @Override
    public int hashCode() {
        return (this.target != null ? this.target.hashCode() : 0);
    }

    /**
     * Returns true only if:
     * - obj is a TargetUID instance and target name are equals
     * - obj is a String instance and target name are equals
     * @param obj other object to compare with
     * @return true if target name are equals
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        // identity check:
        if (this == obj) {
            return true;
        }
        if (TargetUID.class == obj.getClass()) {
            final TargetUID other = (TargetUID) obj;
            if ((this.target == null) ? (other.target != null) : !this.target.equals(other.getTarget())) {
                return false;
            }
        } else if (String.class == obj.getClass()) {
            final String other = (String) obj;
            if ((this.target == null) ? (other != null) : !this.target.equals(other)) {
                return false;
            }
        } else {
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
        sb.append("{target=").append(this.target).append('}');
    }
//--simple--preserve

}
