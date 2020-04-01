/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.exist;

import fr.jmmc.oitools.OIFitsViewer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.exist.Namespaces;
import org.exist.dom.QName;
import org.exist.dom.memtree.SAXAdapter;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.ErrorCodes;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.BinaryValue;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * OIExplorer existdb extension code implementation for viewer.
 *
 * @author Patrick Bernaud, Guillaume Mella
 */
public class Viewer extends BasicFunction {

    private static final SequenceType[] VIEWER_PARAMS = new SequenceType[]{
        new FunctionParameterSequenceType("data", Type.ANY_TYPE, Cardinality.EXACTLY_ONE,
        "An URL to an OIFits file or a binary chunk (xs:base64Binary) to process")
    };

    /**
     * declare some xquery functions
     */
    public final static FunctionSignature[] VIEWER_SIGNATURES = {
        /* oi:to-xml($data as item()) as node()? */
        new FunctionSignature(
        new QName("to-xml", OIExplorerModule.NAMESPACE_URI, OIExplorerModule.PREFIX),
        "Parse OIFits data from URL or binary chunk. It makes use of the "
        + "JMMC's oitools library to output a XML description of the data. "
        + "An error is raised if the data does not follow the OIFits format.",
        VIEWER_PARAMS,
        new FunctionReturnSequenceType(Type.DOCUMENT, Cardinality.ZERO_OR_ONE,
        "an XML description of the OIFits content")
        ),
        /* oi:check($data as item()) as empty() */
        new FunctionSignature(
        new QName("check", OIExplorerModule.NAMESPACE_URI, OIExplorerModule.PREFIX),
        "Try parsing OIFits data from URL or binary chunk. It raises an "
        + "error if the contents is not recognized as OIFits data.",
        VIEWER_PARAMS,
        new FunctionReturnSequenceType(Type.EMPTY, Cardinality.ZERO,
        "empty")
        )
    };

    /** Logger (existdb extensions uses log4j) */
    private final static Logger logger = LogManager.getLogger(Viewer.class);

    /* members */
    private final SAXParserFactory factory;

    /**
     * Constructor of Viewer to provide the extension code.
     *
     * @param context
     * @param signature
     */
    public Viewer(XQueryContext context, FunctionSignature signature) {
        super(context, signature);

        factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
    }

    /**
     * eval implementation.
     *
     * @param args input sequence, mainly one location
     * @param contextSequence context
     * @return a sequence with xml representation of given location oifits
     * @throws XPathException
     */
    @Override
    public Sequence eval(final Sequence[] args, final Sequence contextSequence) throws XPathException {
        Sequence result = Sequence.EMPTY_SEQUENCE;

        File tmpFile = null;
        try {
            final Sequence arg0 = args[0];
            final Item param = arg0.itemAt(0);
            final String absFilePath;

            if (param.getType() == Type.BASE64_BINARY) {
                // Prepare a temporary file where to save binary data to process
                try {
                    tmpFile = File.createTempFile("jmmc-oiexplorer", "viewer");
                } catch (IOException ioe) {
                    throw new XPathException(this, ioe.getMessage(), ioe);
                }
                absFilePath = tmpFile.getAbsolutePath();
                logger.info("Create temporary file " + absFilePath);

                // Fill in the temporary file with binary data
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(tmpFile);
                    ((BinaryValue) param).streamBinaryTo(fos);
                } catch (IOException ioe) {
                    throw new XPathException(this, ioe.getMessage(), ioe);
                } finally {
                    // anyway close the tmp file descriptor:
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException ioe) {
                            logger.warn("IO failure while closing file : " + absFilePath, ioe);
                        }
                    }
                }
            } else {
                // Get location from input args
                // TODO can we extend to multiple locations at once
                absFilePath = param.getStringValue();
                if (absFilePath.isEmpty()) {
                    return result;
                }
            }

            final boolean outputXml = isCalledAs("to-xml");

            // Get our viewer reference
            final OIFitsViewer oiFitsViewer = new OIFitsViewer(outputXml, true, false);

            logger.info("Process data from " + absFilePath + " (xml mode : " + outputXml + ")");

            String output = null;
            try {
                output = oiFitsViewer.process(absFilePath);
            } catch (Exception e) {
                throw new XPathException(this, "Can't read oifits properly: " + e.getMessage(), e);
            }

            if (outputXml) {
                // Parse given xml string to provide a document object
                try {
                    final XMLReader xr = factory.newSAXParser().getXMLReader();

                    final SAXAdapter adapter = new SAXAdapter(context);
                    xr.setContentHandler(adapter);
                    xr.setProperty(Namespaces.SAX_LEXICAL_HANDLER, adapter);
                    xr.parse(new InputSource(new StringReader(output)));

                    result = adapter.getDocument();

                } catch (final ParserConfigurationException e) {
                    throw new XPathException(this, ErrorCodes.EXXQDY0002, "Error while constructing XML parser: " + e.getMessage(), arg0, e);
                } catch (final SAXException e) {
                    throw new XPathException(this, ErrorCodes.EXXQDY0002, "Error while parsing XML: " + e.getMessage(), arg0, e);
                } catch (final IOException e) {
                    throw new XPathException(this, ErrorCodes.EXXQDY0002, "Error while parsing XML: " + e.getMessage(), arg0, e);
                }
            }
        } finally {
            // resource cleanup
            if (tmpFile != null) {
                logger.info("Delete temporary file " + tmpFile.getAbsolutePath());
                tmpFile.delete();
            }
        }
        return result;
    }
}
