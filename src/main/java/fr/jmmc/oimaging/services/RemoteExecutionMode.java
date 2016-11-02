/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services;

import fr.cnes.sitools.extensions.astro.application.uws.client.ClientUWS;
import fr.cnes.sitools.extensions.astro.application.uws.client.ClientUWSException;
import fr.jmmc.jmcs.gui.component.MessagePane;
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

    /** UWS client to execute IR on a remote server */
    static ClientUWS uwsClient = null;

    public RemoteExecutionMode() {

    }

    /**
     * try to connect to the first server of the hardcoded list.
     * // TODO move this method in a factory
     */
    private static void searchService() {

        if (uwsClient != null) {
            return;
        }

        // Move it in a property file ( or constant at least)
        String urls[] = new String[]{
            "http://jmmc-fe-1.jmmc.fr/uws/oimaging/oimaging",
            "http://127.0.0.1:8080/uws/oimaging/oimaging",
            "http://localhost:8800/uws/oimaging/oimaging",
            "http://localhost:8888/uws/oimaging/oimaging",};
        ClientUWS c;
        for (String url : urls) {
            try {
                c = new ClientUWS(url);

                if (c.getJobs() != null) {
                    uwsClient = c;
                    _logger.info("UWS service endpoint : '{}'", url);
                    break;
                }
            } catch (ClientUWSException ex) {
                _logger.info("UWS service endpoint unreachable: '{}'", url);
            } catch (org.restlet.resource.ResourceException ex) {
                _logger.info("UWS service endpoint unreachable: '{}'", url);
            } catch (Exception e) {
                // we should avoid to catch Exception, please catch e just before
                _logger.info("UWS service endpoint unreachable: '{}'", url);
                _logger.info("Exception:", e);
            }
        }
        if (uwsClient == null) {
            MessagePane.showErrorMessage("Can't reach a working uws server");
        }
    }

    /**
     * Start the remote application and wait end of execution.
     *
     * @param software software to run
     * @param inputFilename input filename
     * @param result the service result pointing result file to write data into.
     * @return the error command code
     * @throws IllegalStateException if the job can not be submitted to the job queue
     */
    public static int exec(final String software, final String inputFilename, ServiceResult result) throws IllegalStateException, ClientUWSException, InterruptedException, URISyntaxException, IOException {

        if (StringUtils.isEmpty(software)) {
            throw new IllegalArgumentException("empty application name !");
        }
        if (StringUtils.isEmpty(inputFilename)) {
            throw new IllegalArgumentException("empty input filename !");
        }
        if (StringUtils.isEmpty(result.oifits.getAbsolutePath())) {
            throw new IllegalArgumentException("empty output filename !");
        }
        if (StringUtils.isEmpty(result.executionLog.getAbsolutePath())) {
            throw new IllegalArgumentException("empty log filename !");
        }

        searchService();
        if (uwsClient == null) {
            // TODO put information into logFile so that GUI can report a generic message
            return 254;
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

        // create job
        final String jobId = uwsClient.createJob(fds);

        // Assume that first state is executing
        ExecutionPhase phase = ExecutionPhase.EXECUTING;

        // loop and query return status
        while (phase == ExecutionPhase.EXECUTING) {
            Thread.sleep(2000);
            phase = uwsClient.getJobPhase(jobId);
            // TODO timeout ? or just wait 'Cancel' button
        }
        _logger.info("End of execution for job '{}' in phase '{}'", jobId, phase);

        if (phase == ExecutionPhase.COMPLETED) {
            prepareResult(jobId, result);
        } else {
            JobSummary jobInfo = uwsClient.getJobInfo(jobId, true);
            _logger.error("Error in execution for job '{}': {} ", jobId, jobInfo.getErrorSummary());
        }

        // return failure or result file if everything succeeded
        return 254;
    }

    private static void prepareResult(String jobId, ServiceResult result) throws ClientUWSException, URISyntaxException, IOException {
        Results results = uwsClient.getJobResults(jobId);

        for (ResultReference resultRef : results.getResult()) {
            String id = resultRef.getId();
            URI uri = new URI(resultRef.getHref());
            if (id.equals("logfile")) {
                // get logfile
                Http.download(uri, result.executionLog, false);
            } else if (id.equals("outputfile")) {
                // get result file
                Http.download(uri, result.oifits, false);
            } else {
                // store additional information
                throw new IllegalStateException("uws service return more info than required : " + id);
            }
        }

    }

    @Override
    public ServiceResult reconstructsImage(final String software, final File inputFile) {
        ServiceResult result = new ServiceResult(inputFile);

        Exception e = null;
        try {
            // TODO add log output retrieval
            RemoteExecutionMode.exec(software, inputFile.getAbsolutePath(), result);
        } catch (IllegalStateException ex) {
            e = ex;
        } catch (ClientUWSException ex) {
            e = ex;
        } catch (InterruptedException ex) {
            e = ex;
        } catch (URISyntaxException ex) {
            e = ex;
        } catch (IOException ex) {
            e = ex;
        }
        if (e != null) {
            throw new IllegalStateException(e);
        }
        return result;
    }
}
