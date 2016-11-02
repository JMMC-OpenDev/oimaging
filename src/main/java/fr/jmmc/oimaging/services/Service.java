/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services;

/**
 * Details service as a combinaison of program handled by a dedicated execution
 * mode, with other metadata attributes.
 * @author mellag
 */
// Could be replaced by a jaxb generated class that could load input from xml
// ...and completed at runtime by a remote capability discovery ...
public class Service {

    final String name;
    final String program;
    final OImagingExecutionMode execMode;
    final String description;
    final String contact;

    public Service(final String name, final String program, final OImagingExecutionMode execMode, final String description, final String contact) {
        this.name = name;
        this.program = program;
        this.execMode = execMode;
        this.description = description;
        this.contact = contact;
    }

    public String getName() {
        return name;
    }

    public String getProgram() {
        return program;
    }

    public OImagingExecutionMode getExecMode() {
        return execMode;
    }

    public String getDescription() {
        return description;
    }

    public String getContact() {
        return contact;
    }

    public String toString() {
        return name;
    }
}
