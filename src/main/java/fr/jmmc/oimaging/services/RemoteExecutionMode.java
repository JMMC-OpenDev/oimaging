/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services;

import fr.cnes.sitools.extensions.astro.application.uws.client.ClientUWS;
import fr.cnes.sitools.extensions.astro.application.uws.client.ClientUWSException;
import fr.jmmc.jmcs.network.http.Http;
import fr.jmmc.jmcs.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import net.ivoa.xml.uws.v1.ExecutionPhase;
import net.ivoa.xml.uws.v1.JobSummary;
import net.ivoa.xml.uws.v1.ResultReference;
import net.ivoa.xml.uws.v1.Results;
import org.restlet.data.MediaType;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.FileRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support remote service runner.
 * @author Guillaume MELLA.
 */
public final class RemoteExecutionMode implements OImagingExecutionMode {

    /** Class logger */
    private static final Logger _logger = LoggerFactory.getLogger(RemoteExecutionMode.class.getName());

    public static final String[] SERVICE_URLS = new String[]{
        "http://jmmc-fe-1.jmmc.fr/OImaging-uws/oimaging/oimaging",
        "http://127.0.0.1:8080/OImaging-uws/oimaging/oimaging",
        "http://localhost:8800/OImaging-uws/oimaging/oimaging",
        "http://localhost:8888/OImaging-uws/oimaging/oimaging"
    };

    private static final ClientFactory FACTORY = new ClientFactory();

    private final static class ClientFactory {

        /** UWS client to execute IR on a remote server */
        private ClientUWS uwsClient = null;

        ClientFactory() {
        }

        /**
         * try to connect to the first server of the hardcoded list.
         * // TODO move this method in a factory
         */
        public ClientUWS getClient() {
            if (uwsClient == null) {
                // Move it in a property file ( or constant at least)
                for (String url : SERVICE_URLS) {
                    try {
                        final ClientUWS c = new ClientUWS(url);

                        if (c.getJobs() != null) {
                            uwsClient = c;
                            _logger.info("UWS service endpoint : '{}'", url);
                            break;
                        }
                    } catch (ClientUWSException ex) {
                        _logger.info("UWS service endpoint unreachable: '{}'", url);
                    } catch (Exception e) {
                        // we should avoid to catch Exception, please catch e just before
                        _logger.info("UWS service endpoint unreachable: '{}'", url);
                        _logger.info("Exception:", e);
                    }
                }
                if (uwsClient == null) {
                    throw new IllegalStateException("Can't reach a working uws server !");
                }
            }
            return uwsClient;
        }
    }

    public RemoteExecutionMode() {
    }

    /**
     * Start the remote application and wait end of execution.
     *
     * @param software software to run
     * @param inputFilename input filename
     * @param result the service result pointing result file to write data into.
     * @throws IllegalStateException if the job can not be submitted to the job queue
     */
    public void callUwsOimagingService(final String software, final String inputFilename, ServiceResult result) throws IllegalStateException, ClientUWSException, URISyntaxException, IOException {

        if (StringUtils.isEmpty(software)) {
            throw new IllegalArgumentException("empty application name !");
        }
        if (StringUtils.isEmpty(inputFilename)) {
            throw new IllegalArgumentException("empty input filename !");
        }
        if (StringUtils.isEmpty(result.getOifits().getAbsolutePath())) {
            throw new IllegalArgumentException("empty output filename !");
        }
        if (StringUtils.isEmpty(result.getExecutionLog().getAbsolutePath())) {
            throw new IllegalArgumentException("empty log filename !");
        }

        _logger.info("exec: {} {}", software, inputFilename);

        // prepare input of next uws call
        FormDataSet fds = new FormDataSet();
        fds.setMultipart(true);

        //Disposition disposition = new Disposition(Disposition.TYPE_INLINE, fileForm);
        File f = new File(inputFilename);
        FileRepresentation entity = new FileRepresentation(f, MediaType.IMAGE_PNG);
        //entity.setDisposition(disposition);

        FormData fdFile = new FormData("inputfile", entity);
        fds.getEntries().add(fdFile);

        // start task in autostart mode
        fds.add("PHASE", "RUN");

        // may throw IllegalStateException if no running service available:
        final ClientUWS client = FACTORY.getClient();
        // create job
        // TODO retry if connect exception
        final String jobId = client.createJob(fds);

        // Assume that first state is executing
        ExecutionPhase phase = ExecutionPhase.EXECUTING;

        // loop and query return status
        while (phase == ExecutionPhase.EXECUTING) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                _logger.info("Interrupted.");

                result.setErrorMessage("Cancelled job.");
                client.setAbortJob(jobId);
                return;
            }
            phase = client.getJobPhase(jobId);
            // TODO timeout ? or just wait 'Cancel' button
        }
        _logger.info("End of execution for job '{}' in phase '{}'", jobId, phase);

        if (phase == ExecutionPhase.COMPLETED) {
            prepareResult(jobId, result);
        } else {
            JobSummary jobInfo = client.getJobInfo(jobId, true);
            _logger.error("Error in execution for job '{}': {} ", jobId, jobInfo.getErrorSummary());

            result.setErrorMessage("Execution error: " + jobInfo.getErrorSummary());
        }
    }

    private static void prepareResult(String jobId, ServiceResult result) throws ClientUWSException, URISyntaxException, IOException {
        final ClientUWS client = FACTORY.getClient();

        Results results = client.getJobResults(jobId);

        for (ResultReference resultRef : results.getResult()) {
            final String id = resultRef.getId();
            URI uri = new URI(resultRef.getHref());
            if ("logfile".equals(id)) {
                // get logfile
                if (Http.download(uri, result.getExecutionLog(), false)) {
                    _logger.info("logfile downloaded at : {}", result.getExecutionLog());
                }
            } else if ("outputfile".equals(id)) {
                // get result file
                if (Http.download(uri, result.getOifits(), false)) {
                    _logger.info("outputfile downloaded at : {}", result.getOifits());
                }
            } else {
                // TODO: FIX such error
                // store additional information
                throw new IllegalStateException("UWS service return more info than required : " + id);
            }
        }
        // Result is valid only if the OIFITS file was downloaded successfully:
        final boolean exist = result.getOifits().exists();
        result.setValid(exist);
        if (!exist) {
            result.setErrorMessage("No OIFits ouput (probably a server error occured) !");
        }
    }

    @Override
    public ServiceResult reconstructsImage(final String software, final File inputFile) {
        final ServiceResult result = new ServiceResult(inputFile);

        Exception e = null;
        try {
            // TODO add log output retrieval
            callUwsOimagingService(software, inputFile.getAbsolutePath(), result);
        } catch (IllegalStateException ise) {
            throw ise;
        } catch (ClientUWSException ce) {
            e = ce;
        } catch (URISyntaxException use) {
            e = use;
        } catch (IOException ioe) {
            e = ioe;
        }
        if (e != null) {
            throw new IllegalStateException(e);
        }
        return result;
    }
}
