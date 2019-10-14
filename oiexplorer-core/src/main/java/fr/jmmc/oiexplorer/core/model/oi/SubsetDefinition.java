
package fr.jmmc.oiexplorer.core.model.oi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 This type describes a subset definition.
 *             
 * 
 * <p>Java class for SubsetDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SubsetDefinition"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.jmmc.fr/oiexplorer-base/0.1}Identifiable"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="filter" type="{http://www.jmmc.fr/oiexplorer-data-collection/0.1}SubsetFilter" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubsetDefinition", propOrder = {
    "filters"
})
public class SubsetDefinition
    extends Identifiable
{

    @XmlElement(name = "filter", required = true)
    protected List<SubsetFilter> filters;

    /**
     * Gets the value of the filters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the filters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFilters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SubsetFilter }
     * 
     * 
     */
    public List<SubsetFilter> getFilters() {
        if (filters == null) {
            filters = new ArrayList<SubsetFilter>();
        }
        return this.filters;
    }
    
//--simple--preserve
    /**
    * Return the first SubsetFilter (or create a new instance)
    * @return SubsetFilter instance
    */
    public SubsetFilter getFilter() {
        final SubsetFilter filter;
        if (filters == null || filters.isEmpty()) {
            filter = new SubsetFilter();
            getFilters().add(filter);
        } else {
            filter = getFilters().get(0);
        }
        return filter;
    }
    
    /**
     * Perform a deep-copy EXCEPT Identifiable attributes of the given other instance into this instance
     * 
     * Note: to be overriden in child class to perform deep-copy of class fields
     * 
     * @param other other instance
     */
    @Override
    public void copyValues(final fr.jmmc.oiexplorer.core.model.OIBase other) {
        final SubsetDefinition subset = (SubsetDefinition) other;

        // deep copy filters:
        this.filters = fr.jmmc.jmcs.util.ObjectUtils.deepCopyList(subset.getFilters());
    }

    @Override
    public boolean equals(final Object obj) {
        if (!super.equals(obj)) { // Identifiable
            return false;
        }
        final SubsetDefinition other = (SubsetDefinition) obj;
        if (this.filters != other.filters && (this.filters == null || !this.filters.equals(other.filters))) {
            return false;
        }
        return true;
    }
    /** subset oiFitsFile structure (read only) */
    @javax.xml.bind.annotation.XmlTransient
    private fr.jmmc.oitools.model.OIFitsFile oiFitsSubset = null;

    /**
     * Return the subset oiFitsFile structure
     * @return subset oiFitsFile structure
     */
    public final fr.jmmc.oitools.model.OIFitsFile getOIFitsSubset() {
        return this.oiFitsSubset;
    }

    /**
     * Return the subset oiFitsFile structure
     * @param oiFitsSubset subset oiFitsFile structure
     */
    public final void setOIFitsSubset(final fr.jmmc.oitools.model.OIFitsFile oiFitsSubset) {
        this.oiFitsSubset = oiFitsSubset;
    }

    /**
     * toString() implementation using string builder
     * @param sb string builder to append to
     */
    @Override
    public void toString(final StringBuilder sb, final boolean full) {
        super.toString(sb, full); // Identifiable

        if (full) {
            sb.append(", filters=");
            fr.jmmc.jmcs.util.ObjectUtils.toString(sb, full, this.filters);
        }
        sb.append('}');
    }
//--simple--preserve

}
