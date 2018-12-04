/*******************************************************************************
 * Copyright 2010-2014 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of SITools2.
 *
 * SITools2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SITools2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SITools2.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.cnes.sitools.extensions.astro.application.uws.client;

import fr.cnes.sitools.extensions.astro.application.uws.common.Util;
import fr.jmmc.jmcs.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import net.ivoa.xml.uws.v1.ErrorSummary;
import net.ivoa.xml.uws.v1.ExecutionPhase;
import net.ivoa.xml.uws.v1.JobSummary;
import net.ivoa.xml.uws.v1.Jobs;
import net.ivoa.xml.uws.v1.Parameters;
import net.ivoa.xml.uws.v1.Results;
import net.ivoa.xml.uws.v1.ShortJobDescription;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic UWS client
 * @author Jean-Christophe Malapert
 * @version 0.1
 *
 * // Comes from https://github.com/SITools2/Astronomy-Extension-Server
 * // with new public String createJob(FormDataSet formDataSet) method to
 * // support upload.
 */
public final class ClientUWS {

    /** logger */
    private final static Logger _logger = LoggerFactory.getLogger(ClientUWS.class.getName());

    /** Package name for JAXB generated code */
    private static final String UWS_JAXB_PATH = "net.ivoa.xml.uws.v1";

    /* reused JAXB Context to unmarshall UWS v1 elements */
    private static JAXBContext jaxbContext = null;

    /* reused restlet Http Client (thread-safe) */
    private static Client httpClient = null;

    private static JAXBContext getJAXBContext() throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance(UWS_JAXB_PATH);
        }
        return jaxbContext;
    }

    private static Object unmarshal(final String xml) throws ClientUWSException {
        try {
            final Unmarshaller um = getJAXBContext().createUnmarshaller();
            return um.unmarshal(new StringReader(xml));
        } catch (JAXBException je) {
            throw new ClientUWSException(je);
        }
    }

    private static Client getClient() {
        if (httpClient == null) {
            final Context ctx = new Context();
            // TODO: use NetworkingService settings:
            ctx.getParameters().set("maxTotalConnections", "10");
            ctx.getParameters().set("maxConnectionsPerHost", "5");
            ctx.getParameters().set("idleCheckInterval", "10000"); // 10s

            Context.setCurrent(ctx);
            httpClient = new Client(ctx, Protocol.HTTP);
        }
        return httpClient;
    }

    // members:
    private final Reference serverUWS;
    private final Reference jobsUWS;

    /**
     * Constructor having the UWS service name as parameter.
     * @param uwsServerURL URL of the UWS server
     * @param jobsPath path to the {jobs} service
     */
    public ClientUWS(final String uwsServerURL, final String jobsPath) {
        this.serverUWS = new Reference(uwsServerURL);
        this.jobsUWS = new Reference(uwsServerURL + jobsPath);

        // initialize the http client early:
        getClient();
    }

    private static void initClient(final ClientResource resource, final boolean followRedirect) {
        resource.setRetryOnError(false);
        resource.setFollowingRedirects(followRedirect);
        // reuse shared Http client:
        resource.setNext(getClient());
    }

    private static void release(final ClientResource resource, final Representation representation) throws ClientUWSException {
        try {
            if (representation != null) {
                representation.exhaust();
            }
        } catch (IOException ioe) {
            throw new ClientUWSException(ioe);
        } finally {
            if (representation != null) {
                representation.release();
            }
            resource.release();
        }
    }

    private static String getText(final ClientResource resource, final Representation representation,
                                  final String errorMsg) throws ClientUWSException {
        return getText(resource, representation, errorMsg, null);
    }

    private static String getText(final ClientResource resource, final Representation representation,
                                  final String errorMsg, final String errorArg) throws ClientUWSException {
        if (resource.getStatus().isSuccess()) {
            try {
                return representation.getText();
            } catch (IOException ioe) {
                throw new ClientUWSException(ioe);
            }
        } else {
            throw new ClientUWSException(resource.getStatus(),
                    (errorArg != null) ? (errorMsg + errorArg) : errorMsg);
        }
    }

    private static void checkStatus(final ClientResource resource,
                                    final String errorMsg, final String errorArg) throws ClientUWSException {
        if (!resource.getStatus().isSuccess()) {
            throw new ClientUWSException(resource.getStatus(),
                    (errorArg != null) ? (errorMsg + errorArg) : errorMsg);
        }
    }

    private static void checkRedirect(final ClientResource resource, final String errorMsg) throws ClientUWSException {
        checkRedirect(resource, errorMsg, null);
    }

    private static void checkRedirect(final ClientResource resource,
                                      final String errorMsg, final String errorArg) throws ClientUWSException {
        if (!resource.getStatus().isRedirection()) {
            throw new ClientUWSException(resource.getStatus(),
                    (errorArg != null) ? (errorMsg + errorArg) : errorMsg);
        }
    }

    private ClientResource createResource(final Method method, final Reference reference, final boolean followRedirect) {
        final ClientResource resource = new ClientResource(method, reference);
        initClient(resource, followRedirect);
        return resource;
    }

    private ClientResource createResource(final Method method, final String url, final boolean followRedirect) {
        final ClientResource resource = new ClientResource(method, url);
        initClient(resource, followRedirect);
        return resource;
    }

    private ClientResource createJobResource(final Method method, final boolean followRedirect) {
        final ClientResource resource = new ClientResource(method, jobsUWS);
        initClient(resource, followRedirect);
        return resource;
    }

    private ClientResource createJobResource(final Method method, final String jobId, final String query, final boolean followRedirect) {
        final ClientResource resource = new ClientResource(method, jobsUWS.toString() + '/' + jobId + query);
        initClient(resource, followRedirect);
        return resource;
    }

    /**
     * Get the home page
     * @return Returns the home page
     * @exception ClientUWSException
     */
    public String getHomePage() throws ClientUWSException {
        final ClientResource resource = createResource(Method.GET, this.serverUWS, true);
        Representation representation = null;
        try {
            return getText(resource, representation = resource.get(),
                    "getHomePage: Cannot retrieve the home page");
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    /**
     * Get list of jobs
     * @return Returns list of jobs
     * @exception ClientUWSException
     */
    public Jobs getJobs() throws ClientUWSException {
        final ClientResource resource = createJobResource(Method.GET, true);
        Representation representation = null;
        try {
            return (Jobs) unmarshal(getText(resource, representation = resource.get(),
                    "getJobs: Cannot retrieve the list of jobs"));
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    /**
     * Get the remaining tasks. A remain task has the following status :
     * EXECUTING, QUEUED, PENDING, HELD
     * @param jobsId List of job ID to test
     * @return Returns the remaining tasks with the following structure {taskID, phase name}
     * @exception ClientUWSException
     */
    public Map<String, String> getRemainingTasks(final List<String> jobsId) throws ClientUWSException {
        if (jobsId.isEmpty()) {
            throw new IllegalArgumentException("getRemainingTasks: jobsId cannot be empty");
        }
        final HashMap<String, String> remainingTasks = new HashMap<String, String>();

        for (ShortJobDescription job : getJobs().getJobref()) {
            final String id = job.getId();
            if (!jobsId.contains(id)) {
                continue;
            }
            final ExecutionPhase phase = job.getPhase();

            if (phase == ExecutionPhase.EXECUTING
                    || phase == ExecutionPhase.QUEUED
                    || phase == ExecutionPhase.PENDING
                    || phase == ExecutionPhase.HELD) {
                remainingTasks.put(id, phase.value());
            }
        }
        return remainingTasks;
    }

    /**
     * Create a new job from a form
     * @param form parameter to send.
     * @return Returns its job ID.
     * @exception ClientUWSException
     */
    public String createJob(final Form form) throws ClientUWSException {
        if (!Util.isSet(form)) {
            throw new IllegalArgumentException("createJob: form cannot be null");
        }
        final ClientResource resource = createJobResource(Method.POST, false);
        Representation representation = null;
        try {
            representation = resource.post(form);
            checkRedirect(resource, "createJob: Cannot create a new Job");
            return resource.getResponse().getLocationRef().getLastSegment();
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    /**
     * Create a new job from a formDataSet
     * @param formDataSet parameter to send.
     * @return Returns its job ID.
     * @exception ClientUWSException
     */
    public String createJob(final FormDataSet formDataSet) throws ClientUWSException {
        if (!Util.isSet(formDataSet)) {
            throw new IllegalArgumentException("createJob: form cannot be null");
        }
        final ClientResource resource = createJobResource(Method.POST, false);
        Representation representation = null;
        try {
            representation = resource.post(formDataSet);
            checkRedirect(resource, "createJob: Cannot create a new Job");
            return resource.getResponse().getLocationRef().getLastSegment();
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    /**
     * Get information about a specific job
     * @param jobId Job ID
     * @return Returns information about the job ID
     * @exception ClientUWSException
     */
    @SuppressWarnings("unchecked")
    public JobSummary getJobInfo(final String jobId) throws ClientUWSException {
        checkJobId(jobId);
        final ClientResource resource = createJobResource(Method.GET, jobId, "", true);
        Representation representation = null;
        try {
            return ((JAXBElement<JobSummary>) unmarshal(getText(resource, representation = resource.get(),
                    "getJobInfo: Cannot get information about job ", jobId))).getValue();
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    /**
     * Get information about a specific job and delete this job after getting information
     * @param jobId Job ID
     * @param mustBeDeleted Set to true to delete the job after getting job information
     * @return Returns information about the job ID
     * @exception ClientUWSException
     */
    public JobSummary getJobInfo(final String jobId, final boolean mustBeDeleted) throws ClientUWSException {
        checkJobId(jobId);
        final JobSummary jobSummary = getJobInfo(jobId);
        if (mustBeDeleted) {
            deleteJobInfo(jobId);
        }
        return jobSummary;
    }

    /**
     * Get Quote of a job
     * @param jobId Job ID
     * @return Returns quote as a string
     * @exception ClientUWSException
     */
    public String getJobQuote(final String jobId) throws ClientUWSException {
        return getJobInfo(jobId).getQuote().toString();
    }

    /**
     * Delete a job
     * @param jobId Job ID
     * @exception ClientUWSException
     */
    public void deleteJobInfo(final String jobId) throws ClientUWSException {
        checkJobId(jobId);
        final ClientResource resource = createJobResource(Method.DELETE, jobId, "", false);
        Representation representation = null;
        try {
            representation = resource.delete();
            checkRedirect(resource, "deleteJobInfo: No redirect is done after deleting job ", jobId);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    /**
     * Get the destruction time of a job
     * @param jobId Job ID
     * @return Returns destructionTime as ISO8601 format
     * @exception ClientUWSException
     */
    public String getJobDestructionTime(final String jobId) throws ClientUWSException {
        checkJobId(jobId);
        final ClientResource resource = createJobResource(Method.GET, jobId, "/destruction", true);
        Representation representation = null;
        try {
            return getText(resource, representation = resource.get(),
                    "getJobDestructionTime: Cannot get information about job ", jobId);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    /**
     * Set a destruction time for a given job
     * @param jobId Job ID
     * @param inputDate Date
     * @exception ClientUWSException
     */
    public void setJobDestructionTime(final String jobId, final Date inputDate) throws ClientUWSException {
        checkJobId(jobId);
        final ClientResource resource = createJobResource(Method.POST, jobId, "/destruction", false);
        Representation representation = null;
        try {
            final String destruction = Util.convertIntoXMLGregorian(inputDate).toString();
            final Form form = new Form();
            form.add("DESTRUCTION", destruction);

            representation = resource.post(form);
            checkRedirect(resource, "setJobDestructionTime: Unable to set DESTRUCTION=", destruction);
        } catch (DatatypeConfigurationException ex) {
            throw new ClientUWSException(ex);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    /**
     * Get the execution duration of a job
     * @param jobId Job ID
     * @return Returns executionduration as a string
     * @exception ClientUWSException
     */
    public String getJobExecutionDuration(final String jobId) throws ClientUWSException {
        checkJobId(jobId);
        final ClientResource resource = createJobResource(Method.GET, jobId, "/executionduration", true);
        Representation representation = null;
        try {
            return getText(resource, representation = resource.get(),
                    "getJobExecutionDuration: Cannot get information about job ", jobId);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    /**
     * Set the execution duration for a given job
     * @param jobId Job ID
     * @param timeInSeconds Execution time in seconds
     * @exception ClientUWSException
     */
    public void setJobExecutionDuration(final String jobId, final int timeInSeconds) throws ClientUWSException {
        checkJobId(jobId);
        final ClientResource resource = createJobResource(Method.POST, jobId, "/executionduration", false);
        Representation representation = null;
        try {
            final String duration = String.valueOf(timeInSeconds);
            final Form form = new Form();
            form.add("EXECUTIONDURATION", duration);

            representation = resource.post(form);
            checkRedirect(resource, "setJobExecutionDuration: Unable to set EXECUTIONDURATION=", duration);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    /**
     * Get Phase of a job 
     * @param jobId Job ID
     * @return Returns ExecutionPhase
     * @exception ClientUWSException
     */
    public ExecutionPhase getJobPhase(final String jobId) throws ClientUWSException {
        checkJobId(jobId);
        final ClientResource resource = createJobResource(Method.GET, jobId, "/phase", true);
        Representation representation = null;
        try {
            return ExecutionPhase.valueOf(getText(resource, representation = resource.get(),
                    "getJobPhase: Cannot get phase about job ", jobId));
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    /**
     * Set the phase of a job
     * @param jobId Job ID
     * @param phase phase to set
     * @exception ClientUWSException
     */
    private void setJobPhase(final String jobId, final String phase) throws ClientUWSException {
        checkJobId(jobId);
        final ClientResource resource = createJobResource(Method.POST, jobId, "/phase", false);
        Representation representation = null;
        try {
            final Form form = new Form();
            form.add("PHASE", phase);

            representation = resource.post(form);
            checkRedirect(resource, "setJobPhase: Unable to set phase on job ", jobId);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    /**
     * Start a job
     * @param jobId Job ID
     * @exception ClientUWSException
     */
    public void setStartJob(final String jobId) throws ClientUWSException {
        setJobPhase(jobId, "RUN");
    }

    /**
     * Stop a job
     * @param jobId Job ID
     * @exception ClientUWSException
     */
    public void setAbortJob(final String jobId) throws ClientUWSException {
        setJobPhase(jobId, "ABORT");
    }

    /**
     * Get Owner of a job
     * @param jobId Job ID
     * @return Returns owner as string
     * @exception ClientUWSException
     */
    public String getJobOwner(final String jobId) throws ClientUWSException {
        checkJobId(jobId);
        final ClientResource resource = createJobResource(Method.GET, jobId, "/owner", true);
        Representation representation = null;
        try {
            return getText(resource, representation = resource.get(),
                    "getJobOwner: Cannot get owner about job ", jobId);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    /**
     * Get Error of a job
     * @param jobId Job ID
     * @return Returns ErrorSummary
     * @exception ClientUWSException
     */
    @SuppressWarnings("unchecked")
    public ErrorSummary getJobError(final String jobId) throws ClientUWSException {
        checkJobId(jobId);
        final ClientResource resource = createJobResource(Method.POST, jobId, "/error", true);
        Representation representation = null;
        try {
            return ((JAXBElement<ErrorSummary>) unmarshal(getText(resource, representation = resource.get(),
                    "getJobError: Cannot get error about job ", jobId))).getValue();
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    /**
     * Get results of a job
     * @param jobId Job ID
     * @return Returns Results
     * @exception ClientUWSException
     */
    public Results getJobResults(final String jobId) throws ClientUWSException {
        checkJobId(jobId);
        final ClientResource resource = createJobResource(Method.GET, jobId, "/results", true);
        Representation representation = null;
        try {
            return (Results) unmarshal(getText(resource, representation = resource.get(),
                    "getJobResults: Cannot get results about job ", jobId));
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    /**
     * Get parameters of a job
     * @param jobId Job ID
     * @return Returns Parameters
     * @exception ClientUWSException
     */
    public Parameters getJobParameters(final String jobId) throws ClientUWSException {
        checkJobId(jobId);
        final ClientResource resource = createJobResource(Method.GET, jobId, "/parameters", true);
        Representation representation = null;
        try {
            return (Parameters) unmarshal(getText(resource, representation = resource.get(),
                    "getJobParameters: Cannot get parameters about job ", jobId));
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    /**
     * Set parameter of a job
     * @param jobId Job ID
     * @param key parameter to set
     * @exception ClientUWSException
     */
    public void setJobParameter(final String jobId, final String key) throws ClientUWSException {
        if (!Util.isSet(jobId) || !Util.isSet(key)) {
            throw new IllegalArgumentException("setJobParameter: jobId and key are required");
        }
        final ClientResource resource = createJobResource(Method.PUT, jobId, "/parameters/" + key, true);
        Representation representation = null;
        try {
            representation = resource.handle();
            checkStatus(resource, "setJobParameter: Cannot set parameter about job ", jobId);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    public boolean downloadFile(final String url, final File outputFile) throws ClientUWSException {
        if (!Util.isSet(url) || !Util.isSet(outputFile)) {
            throw new IllegalArgumentException("downloadFile: url and outputFile are required");
        }
        final ClientResource resource = createResource(Method.GET, url, true);
        Representation representation = null;
        try {
            representation = resource.get();

            FileUtils.saveStream(representation.getStream(), outputFile);

            if (_logger.isDebugEnabled()) {
                _logger.debug("File '{}' saved ({} bytes).", outputFile, outputFile.length());
            }
            return true;

        } catch (IOException ioe) {
            if (outputFile.exists()) {
                outputFile.delete();
            }
            throw new ClientUWSException(ioe);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            release(resource, representation);
        }
    }

    private static void checkJobId(final String jobId) {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("jobId is required");
        }
    }
}
