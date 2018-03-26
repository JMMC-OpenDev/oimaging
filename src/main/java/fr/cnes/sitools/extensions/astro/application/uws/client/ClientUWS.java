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
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
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
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * Generic UWS client
 * @author Jean-Christophe Malapert
 * @version 0.1
 *
 * // Comes from https://github.com/SITools2/Astronomy-Extension-Server
 * // with new public String createJob(FormDataSet formDataSet) method to
 * // support upload.
 */
public class ClientUWS {

    /** Package name for JAXB generated code */
    private static final String UWS_JAXB_PATH = "net.ivoa.xml.uws.v1";

    /* reused JAXB Context to unmarshall UWS v1 elements */
    private static JAXBContext jaxbContext = null;

    /* reused restlet Http Client (thread-safe) */
    private static Client httpClient = null;

    private JAXBContext getJAXBContext() throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance(UWS_JAXB_PATH);
        }
        return jaxbContext;
    }

    private static Client getClient() {
        if (httpClient == null) {
            final Context ctx = new Context();
            // TODO: use NetworkingService settings:
            ctx.getParameters().set("maxTotalConnections", "10"); 
            ctx.getParameters().set("maxConnectionsPerHost", "4");
            Context.setCurrent(ctx);
            httpClient = new Client(ctx, Protocol.HTTP); 
        }
        return httpClient;
    }
    
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

    private static void initClient(final ClientResource client, final boolean followRedirect) {
        client.setRetryOnError(false);
        client.setFollowingRedirects(followRedirect);
        // reuse shared Http client:
        client.setNext(getClient());
    }

    /**
     * Get the home page
     * @return Returns the home page
     * @exception ClientUWSException
     */
    public String getHomePage() throws ClientUWSException {
        String homePage = null;
        ClientResource client = null;
        try {
            client = new ClientResource(Method.GET, this.serverUWS);
            initClient(client, true);
            if (client.getStatus().isSuccess()) {
                homePage = client.get().getText();
            } else {
                throw new ClientUWSException(client.getStatus(), "getHomePage: Cannot retrieve the home page");
            }
        } catch (IOException ioe) {
            throw new ClientUWSException(ioe);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
        return homePage;
    }

    /**
     * Create a list of new jobs from forms
     * @param forms list of parameters to send.
     * @return Returns the form object from the input and its job ID.
     * @exception ClientUWSException
     */
    public HashMap<Object, String> createJob(List<Form> forms) throws ClientUWSException {
        if (forms.isEmpty()) {
            throw new IllegalArgumentException("Process: Forms cannot be empty");
        }
        final HashMap<Object, String> jobs = new HashMap<Object, String>();

        Iterator<Form> iterObject = forms.iterator();
        while (iterObject.hasNext()) {
            Form object = iterObject.next();
            ClientResource client = null;
            try {
                client = new ClientResource(Method.POST, this.jobsUWS);
                initClient(client, false);
                client.post(object);
                if (client.getStatus().isRedirection()) {
                    Reference locationJob = client.getResponse().getLocationRef();
                    jobs.put(object, locationJob.getLastSegment());
                } else {
                    throw new ClientUWSException(client.getStatus(), "Process: Cannot create a new Job");
                }
            } catch (ResourceException re) {
                throw new ClientUWSException(re);
            } finally {
                if (client != null) {
                    client.release();
                }
            }
        }
        return jobs;
    }

    /**
     * Create a new job from a form
     * @param form parameter to send.
     * @return Returns its job ID.
     * @exception ClientUWSException
     */
    public String createJob(Form form) throws ClientUWSException {
        if (!Util.isSet(form)) {
            throw new IllegalArgumentException("Process: form cannot be null");
        }
        String jobId = null;
        ClientResource client = null;
        try {
            client = new ClientResource(Method.POST, this.jobsUWS);
            initClient(client, false);
            client.post(form);
            if (client.getStatus().isRedirection()) {
                Reference locationJob = client.getResponse().getLocationRef();
                jobId = locationJob.getLastSegment();
            } else {
                throw new ClientUWSException(client.getStatus(), "Process: Cannot create a new Job");
            }
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
        return jobId;
    }

    /**
     * Create a new job from a formDataSet
     * @param formDataSet parameter to send.
     * @return Returns its job ID.
     * @exception ClientUWSException
     */
    public String createJob(FormDataSet formDataSet) throws ClientUWSException {
        if (!Util.isSet(formDataSet)) {
            throw new IllegalArgumentException("Process: form cannot be null");
        }
        String jobId = null;
        ClientResource client = null;
        try {
            client = new ClientResource(Method.POST, this.jobsUWS);
            initClient(client, false);
            client.post(formDataSet);
            if (client.getStatus().isRedirection()) {
                Reference locationJob = client.getResponse().getLocationRef();
                jobId = locationJob.getLastSegment();
            } else {
                throw new ClientUWSException(client.getStatus(), "Process: Cannot create a new Job");
            }
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
        return jobId;
    }

    /**
     * Get the remaining tasks. A remain task has the following status :
     * EXECUTING, QUEUED, PENDING, HELD
     * @param jobsId List of job ID to test
     * @return Returns the remaining tasks with the following structure {taskID, phase name}
     * @exception ClientUWSException
     */
    public HashMap<String, String> getRemainingTasks(List<String> jobsId) throws ClientUWSException {
        if (jobsId.isEmpty()) {
            throw new IllegalArgumentException("GetRemainingTasks: jobsId cannot be empty");
        }
        HashMap<String, String> remainingTasks = new HashMap<String, String>();
        Jobs jobsResponse = this.getJobs();
        List<ShortJobDescription> shortJobsDescription = jobsResponse.getJobref();
        for (int i = 0; i < shortJobsDescription.size(); i++) {
            ShortJobDescription job = shortJobsDescription.get(i);
            String ID = job.getId();
            ExecutionPhase phase = job.getPhase();
            if (phase.equals(ExecutionPhase.EXECUTING) || phase.equals(ExecutionPhase.QUEUED)
                    || phase.equals(ExecutionPhase.PENDING) || phase.equals(ExecutionPhase.HELD)) {
                remainingTasks.put(ID, phase.value());
            }
        }
        return remainingTasks;
    }

    /**
     * Get information about a specific job ID
     * @param jobId Job ID
     * @return Returns information about the job ID
     * @exception ClientUWSException
     */
    public JobSummary getJobInfo(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetJobInfo: jobId is required");
        }
        JobSummary jobSummaryResponse = null;
        ClientResource client = null;
        try {
            client = new ClientResource(Method.GET, this.jobsUWS.toString() + '/' + jobId);
            initClient(client, true);
            if (client.getStatus().isSuccess()) {
                Unmarshaller um = getJAXBContext().createUnmarshaller();
                JAXBElement<JobSummary> response = (JAXBElement<JobSummary>) um.unmarshal(new StringReader(client.get().getText()));
                jobSummaryResponse = response.getValue();
            } else {
                throw new ClientUWSException(client.getStatus(), "GetJobInfo: Cannot get information about " + jobId);
            }
        } catch (IOException ioe) {
            throw new ClientUWSException(ioe);
        } catch (JAXBException je) {
            throw new ClientUWSException(je);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
        return jobSummaryResponse;
    }

    /**
     * Delete a job
     * @param jobId JobId
     * @exception ClientUWSException
     */
    public void deleteJobInfo(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("deleteJobInfo: jobId is required");
        }
        ClientResource client = null;
        try {
            client = new ClientResource(Method.DELETE, this.jobsUWS.toString() + '/' + jobId);
            initClient(client, false);
            client.delete();
            if (!client.getStatus().isRedirection()) {
                throw new ClientUWSException(client.getStatus(), "deleteJobInfo: No redirect is done after deleting " + jobId);
            }
        } catch (ResourceException re) {
            throw new ClientUWSException(re.getStatus(), "deleteJobInfo: Cannot delete " + jobId);
        } finally {
            if (client != null) {
                client.release();
            }
        }
    }

    /**
     * Get information about a specific job ID and delete this job after getting information
     * @param jobId Job ID
     * @param mustBeDeleted Set to true to delete the job after getting job information
     * @return Returns information about the job ID
     * @exception ClientUWSException
     */
    public JobSummary getJobInfo(String jobId, boolean mustBeDeleted) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetJobInfo: jobId is required");
        }
        JobSummary jobSummary = this.getJobInfo(jobId);
        if (mustBeDeleted) {
            deleteJobInfo(jobId);
        }
        return jobSummary;
    }

    /**
     * Get list of jobs
     * @return Returns list of jobs
     * @exception ClientUWSException
     */
    public Jobs getJobs() throws ClientUWSException {
        Jobs jobsResponse = null;
        ClientResource client = null;
        try {
            client = new ClientResource(Method.GET, this.jobsUWS);
            initClient(client, true);
            if (client.getStatus().isSuccess()) {
                Unmarshaller um = getJAXBContext().createUnmarshaller();
                jobsResponse = (Jobs) um.unmarshal(new StringReader(client.get().getText()));
            } else {
                throw new ClientUWSException(client.getStatus(), "GetJobs: Cannot retrieve the list of jobs");
            }
        } catch (IOException ioe) {
            throw new ClientUWSException(ioe);
        } catch (JAXBException je) {
            throw new ClientUWSException(je);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
        return jobsResponse;
    }

    /**
     * Set a desctruction time for a given job
     * @param jobId JobID
     * @param inputDate Date
     * @exception ClientUWSException
     */
    public void setDestructionTimeJob(String jobId, Date inputDate) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("setDestructionTimeJob: jobId is required");
        }
        ClientResource client = null;
        try {
            XMLGregorianCalendar calendar = Util.convertIntoXMLGregorian(inputDate);
            client = new ClientResource(Method.POST, this.jobsUWS.toString() + '/' + jobId + "/destruction");
            initClient(client, false);
            Form form = new Form();
            form.add("DESTRUCTION", calendar.toString());
            client.post(form);
            if (!client.getStatus().isRedirection()) {
                throw new ClientUWSException(client.getStatus(), "SetDestructionTimeJob: Unable to set DESTRUCTION=" + calendar.toString());
            }
        } catch (DatatypeConfigurationException ex) {
            throw new ClientUWSException(ex);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
    }

    /**
     * Set the exucation duration for a given job
     * @param jobId JobID
     * @param timeInSeconds Execution time in seconds
     * @exception ClientUWSException
     */
    public void setExecutionDurationJob(String jobId, int timeInSeconds) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("setExecutionDurationJob: jobId is required");
        }
        ClientResource client = null;
        try {
            client = new ClientResource(Method.POST, this.jobsUWS.toString() + '/' + jobId + "/executionduration");
            initClient(client, false);
            Form form = new Form();
            form.add("EXECUTIONDURATION", String.valueOf(timeInSeconds));
            client.post(form);
            if (!client.getStatus().isRedirection()) {
                throw new ClientUWSException(client.getStatus(), "SetExecutionDurationJob: Unable to set EXECUTIONDURATION=" + timeInSeconds);
            }
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
    }

    /**
     * Start a job
     * @param jobId JobID
     * @exception ClientUWSException
     */
    public void setStartJob(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("setStartJob: jobId is required");
        }
        ClientResource client = null;
        try {
            client = new ClientResource(Method.POST, this.jobsUWS.toString() + '/' + jobId + "/phase");
            initClient(client, false);
            Form form = new Form();
            form.add("PHASE", "RUN");
            client.post(form);
            if (!client.getStatus().isRedirection()) {
                throw new ClientUWSException(client.getStatus(), "SetStartJob: Unable to start the " + jobId);
            }
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
    }

    /**
     * Stop a job
     * @param jobId
     * @exception ClientUWSException
     */
    public void setAbortJob(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("setAbortJob: jobId is required");
        }
        ClientResource client = null;
        try {
            client = new ClientResource(Method.POST, this.jobsUWS.toString() + '/' + jobId + "/phase");
            initClient(client, false);
            Form form = new Form();
            form.add("PHASE", "ABORT");
            client.post(form);
            if (!client.getStatus().isRedirection()) {
                throw new ClientUWSException(client.getStatus(), "SetAbortJob: Unable to abort the " + jobId);
            }
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
    }

    /**
     * Get Error of a job ID
     * @param jobId Job ID
     * @return Returns ErrorSummary
     * @exception ClientUWSException
     */
    public ErrorSummary getJobError(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetJobError: jobId is required");
        }
        ErrorSummary errorSummaryResponse = null;
        ClientResource client = null;
        try {
            client = new ClientResource(Method.GET, this.jobsUWS.toString() + '/' + jobId + "/error");
            initClient(client, true);
            if (client.getStatus().isSuccess()) {
                Unmarshaller um = getJAXBContext().createUnmarshaller();
                JAXBElement<ErrorSummary> response = (JAXBElement<ErrorSummary>) um.unmarshal(new StringReader(client.get().getText()));
                errorSummaryResponse = response.getValue();
            } else {
                throw new ClientUWSException(client.getStatus(), "GetJobError: Cannot get error about " + jobId);
            }
        } catch (IOException ex) {
            throw new ClientUWSException(ex);
        } catch (JAXBException ex) {
            throw new ClientUWSException(ex);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
        return errorSummaryResponse;
    }

    /**
     * Get Quote of a job ID
     * @param jobId Job ID
     * @return Returns quote as a string
     * @exception ClientUWSException
     */
    public String getJobQuote(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetJobQuote: jobId is required");
        }
        JobSummary jobSummaryResponse = null;
        ClientResource client = null;
        try {
            client = new ClientResource(Method.GET, this.jobsUWS.toString() + '/' + jobId);
            initClient(client, true);
            if (client.getStatus().isSuccess()) {
                Unmarshaller um = getJAXBContext().createUnmarshaller();
                JAXBElement<JobSummary> response = (JAXBElement<JobSummary>) um.unmarshal(new StringReader(client.get().getText()));
                jobSummaryResponse = response.getValue();
            } else {
                throw new ClientUWSException(client.getStatus(), "GetJobInfo: Cannot get information about " + jobId);
            }
        } catch (IOException ex) {
            throw new ClientUWSException(ex);
        } catch (JAXBException ex) {
            throw new ClientUWSException(ex);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
        return jobSummaryResponse.getQuote().toString();
    }

    /**
     * Get results of a job ID
     * @param jobId Job ID
     * @return Returns Results
     * @exception ClientUWSException
     */
    public Results getJobResults(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetJobResults: jobId is required");
        }
        Results resultsResponse = null;
        ClientResource client = null;
        try {
            client = new ClientResource(Method.GET, this.jobsUWS.toString() + '/' + jobId + "/results");
            initClient(client, true);
            if (client.getStatus().isSuccess()) {
                Unmarshaller um = getJAXBContext().createUnmarshaller();
                Results response = (Results) um.unmarshal(new StringReader(client.get().getText()));
                resultsResponse = response;
            } else {
                throw new ClientUWSException(client.getStatus(), "GetJobResults: Cannot get results about " + jobId);
            }
        } catch (IOException ex) {
            throw new ClientUWSException(ex);
        } catch (JAXBException ex) {
            throw new ClientUWSException(ex);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
        return resultsResponse;
    }

    /**
     * Get prameters of a job ID
     * @param jobId Job ID
     * @return Returns Parameters
     * @exception ClientUWSException
     */
    public Parameters getJobParameters(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetJobParameters: jobId is required");
        }
        Parameters parametersResponse = null;
        ClientResource client = null;
        try {
            client = new ClientResource(Method.GET, this.jobsUWS.toString() + '/' + jobId + "/parameters");
            initClient(client, true);
            if (client.getStatus().isSuccess()) {
                Unmarshaller um = getJAXBContext().createUnmarshaller();
                Parameters response = (Parameters) um.unmarshal(new StringReader(client.get().getText()));
                parametersResponse = response;
            } else {
                throw new ClientUWSException(client.getStatus(), "GetJobParameters: Cannot get parameters about " + jobId);
            }
        } catch (IOException ex) {
            throw new ClientUWSException(ex);
        } catch (JAXBException ex) {
            throw new ClientUWSException(ex);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
        return parametersResponse;
    }

    /**
     * Get Owner of a job ID
     * @param jobId Job ID
     * @return Returns owner as string
     * @exception ClientUWSException
     */
    public String getJobOwner(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetJobOwner: jobId is required");
        }
        String ownerResponse = null;
        ClientResource client = null;
        try {
            client = new ClientResource(Method.GET, this.jobsUWS.toString() + '/' + jobId + "/owner");
            initClient(client, true);
            if (client.getStatus().isSuccess()) {
                ownerResponse = client.get().getText();
            } else {
                throw new ClientUWSException(client.getStatus(), "GetOwner: Cannot get owner about " + jobId);
            }
        } catch (IOException ex) {
            throw new ClientUWSException(ex);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
        return ownerResponse;
    }

    /**
     * Get Phase of a job ID
     * @param jobId Job ID
     * @return Returns ExecutionPhase
     * @exception ClientUWSException
     */
    public ExecutionPhase getJobPhase(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetJobPhase: jobId is required");
        }
        ExecutionPhase phaseResponse = null;
        ClientResource client = null;
        try {
            client = new ClientResource(Method.GET, this.jobsUWS.toString() + '/' + jobId + "/phase");
            initClient(client, true);
            if (client.getStatus().isSuccess()) {
                phaseResponse = ExecutionPhase.valueOf(client.get().getText());
            } else {
                throw new ClientUWSException(client.getStatus(), "GetPhase: Cannot get phase about " + jobId);
            }
        } catch (IOException ioe) {
            throw new ClientUWSException(ioe);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
        return phaseResponse;
    }

    public void setParameter(String jobId, String key) throws ClientUWSException {
        if (!Util.isSet(jobId) || !Util.isSet(key)) {
            throw new IllegalArgumentException("setParameter: jobId and key are required");
        }
        ClientResource client = null;
        try {
            client = new ClientResource(Method.PUT, this.jobsUWS.toString() + '/' + jobId + "/parameters/" + key);
            initClient(client, true);
            if (!client.getStatus().isSuccess()) {
                throw new ClientUWSException(client.getStatus(), "setOwner: Cannot get owner about " + jobId);
            }
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
    }

    /**
     * Get ExecutionDuration of a job ID
     * @param jobId Job ID
     * @return Returns executionduration as a string
     * @exception ClientUWSException
     */
    public String getExecutionDuration(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetExecutionDuration: jobId is required");
        }
        String edResponse = null;
        ClientResource client = null;
        try {
            client = new ClientResource(Method.GET, this.jobsUWS.toString() + '/' + jobId + "/executionduration");
            initClient(client, true);
            if (client.getStatus().isSuccess()) {
                edResponse = client.get().getText();
            } else {
                throw new ClientUWSException(client.getStatus(), "GetExecutionDuration: Cannot get information about " + jobId);
            }
        } catch (IOException ex) {
            throw new ClientUWSException(ex);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
        return edResponse;
    }

    /**
     * Get DestructionTime of a job ID
     * @param jobId Job ID
     * @return Returns destructionTime as ISO8601 format
     * @exception ClientUWSException
     */
    public String getDestructionTime(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetDestructionTime: jobId is required");
        }
        String edResponse = null;
        ClientResource client = null;
        try {
            client = new ClientResource(Method.GET, this.jobsUWS.toString() + '/' + jobId + "/destruction");
            initClient(client, true);
            if (client.getStatus().isSuccess()) {
                edResponse = client.get().getText();
            } else {
                throw new ClientUWSException(client.getStatus(), "GetDestructionTime: Cannot get information about " + jobId);
            }
        } catch (IOException ex) {
            throw new ClientUWSException(ex);
        } catch (ResourceException re) {
            throw new ClientUWSException(re);
        } finally {
            if (client != null) {
                client.release();
            }
        }
        return edResponse;
    }
}
