/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools;

import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.XmlOutputVisitor;
import fr.nom.tam.fits.FitsException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This command line program loads OIFits files given as arguments
 * and print their XML description in the system out stream
 * @author bourgesl, mella
 */
public final class OIFitsViewer {

    /* members */
    /** internal OIFits checker */
    private final OIFitsChecker checker;
    /** internal XML serializer (null if XML output disabled) */
    private final XmlOutputVisitor xmlSerializer;

    /**
     * Creates a new OifitsViewer object with default options.
     */
    public OIFitsViewer() {
        this(false, false);
    }

    /**
     * Creates a new OifitsViewer object.
     *
     * @param format flag to represent data with less accuracy but a better string representation
     * @param verbose if true the result will contain the table content
     */
    public OIFitsViewer(final boolean format, final boolean verbose) {
        this(true, format, verbose);
    }

    /**
     * Creates a new OifitsViewer object.
     *
     * @param format flag to represent data with less accuracy but a better string representation
     * @param verbose if true the result will contain the table content
     * @param doXmlOutput enable/disable XML output
     */
    public OIFitsViewer(final boolean doXmlOutput, final boolean format, final boolean verbose) {
        this.checker = new OIFitsChecker();
        this.xmlSerializer = (doXmlOutput) ? new XmlOutputVisitor(format, verbose, this.checker) : null;
    }

    /**
     * Process the given file
     *
     * @param filename name of the file to visualize its content.
     * @return serializer output or null if undefined
     * @throws FitsException excpetion thrown during fits reading
     * @throws IOException exception thrown on file reading error
     */
    public String process(final String filename) throws IOException, FitsException {
        // Load file
        final OIFitsFile oiFitsFile = OIFitsLoader.loadOIFits(this.checker, filename, true);

        String output = null;
        if (this.xmlSerializer != null) {
            oiFitsFile.accept(this.xmlSerializer);
            output = this.xmlSerializer.toString();
        }

        // clean the checker before processing any other files
        this.checker.clearCheckReport();

        return output;
    }

    /**
     * Main entry point.
     *
     * @param args command line arguments.
     */
    public static void main(final String[] args) {

        boolean format = false;
        boolean verbose = false;
        boolean xml = true;

        final List<String> fileNames = new ArrayList<String>(args.length);

        // parse command line arguments :
        for (final String arg : args) {
            if (arg.startsWith("-")) {
                if (arg.equals("-f") || arg.equals("-format")) {
                    format = true;
                } else if (arg.equals("-v") || arg.equals("-verbose")) {
                    verbose = true;
                } else if (arg.equals("-c") || arg.equals("-check")) {
                    xml = false;
                } else if (arg.equals("-h") || arg.equals("-help")) {
                    showArgumentsHelp();
                    System.exit(0);
                } else {
                    error("'" + arg + "' option not supported.");
                }
            } else {
                fileNames.add(arg);
            }
        }

        if (fileNames.isEmpty()) {
            error("Missing file name argument.");
        }

        final OIFitsViewer viewer = new OIFitsViewer(xml, format, verbose);

        System.out.println("<oifits_list>");
        for (String fileName : fileNames) {
            try {
                System.out.println(viewer.process(fileName));
            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.out.println("Error reading file '" + fileName + "'");
            }
        }
        System.out.println("</oifits_list>");

    }

    /**
     * Print an error message when parsing the command line arguments
     * @param message message to print
     */
    private static void error(final String message) {
        System.err.println(message);
        showArgumentsHelp();
        System.exit(1);
    }

    /** Show command arguments help */
    private static void showArgumentsHelp() {
        System.out.println(
                "-------------------------------------------------------------------------");
        System.out.println(
                "Usage: OIFitsViewer [-f|-format] [-v|-verbose] <file names>");
        System.out.println(
                "------------- Arguments help --------------------------------------------");
        System.out.println(
                "| Key          Value           Description                              |");
        System.out.println(
                "|-----------------------------------------------------------------------|");
        System.out.println(
                "| [-f] or [-format]            Use the number formatter                 |");
        System.out.println(
                "| [-v] or [-verbose]           Dump all column data                     |");
        System.out.println(
                "| [-c] or [-check]             Check only given file(s)                 |");
        System.out.println(
                "| [-h|-help]                   Show arguments help                      |");
        System.out.println(
                "-------------------------------------------------------------------------");
    }
}
/*___oOo___*/
