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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
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
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.engine.Engine;
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
 *
 */
public class ClientUWS {

    private HashMap<Object, String> jobs = new HashMap<Object, String>();
    private final Reference jobsUWS;

    /**
     * Constructor having the UWS service name as parameter.
     * @param serviceName Name of the UWS service
     */
    public ClientUWS(String serviceName) {
        this.jobsUWS = new Reference(serviceName);
    }

    /**
     * Create a list of new jobs from forms
     * @param form parameters to send.
     * @return Returns the form object from the input and its job ID.
     * @exception ClientUWSException
     * @exception IllegalArgumentException
     */
    public HashMap<Object, String> createJob(List<Form> forms) throws ClientUWSException {
        if (forms.isEmpty()) {
            throw new IllegalArgumentException("Process: Forms cannot be empty");
        }
        Iterator<Form> iterObject = forms.iterator();
        while (iterObject.hasNext()) {
            Form object = iterObject.next();
            ClientResource client = new ClientResource(Method.POST, this.jobsUWS);
            client.setFollowingRedirects(false);
            client.post(object);
            if (client.getStatus().isRedirection()) {
                Reference locationJob = client.getResponse().getLocationRef();
                this.jobs.put(object, locationJob.getLastSegment());
            } else {
                client.release();
                throw new ClientUWSException(client.getStatus(), "Process: Cannot create a new Job");
            }
            client.release();
        }
        return this.jobs;
    }

    /**
     * Create a new job from a form
     * @param form parameter to send.
     * @return Returns its job ID.
     * @exception ClientUWSException
     * @exception IllegalArgumentException
     */
    public String createJob(Form form) throws ClientUWSException {
        if (!Util.isSet(form)) {
            throw new IllegalArgumentException("Process: form cannot be null");
        }
        String jobId = null;
        ClientResource client = new ClientResource(Method.POST, this.jobsUWS);
        client.setFollowingRedirects(false);
        client.post(form);
        if (client.getStatus().isRedirection()) {
            Reference locationJob = client.getResponse().getLocationRef();
            jobId = locationJob.getLastSegment();
        } else {
            client.release();
            throw new ClientUWSException(client.getStatus(), "Process: Cannot create a new Job");
        }
        client.release();
        return jobId;
    }

    /**
     * Create a new job from a formDataSet
     * @param formDataSet parameter to send.
     * @return Returns its job ID.
     * @exception ClientUWSException
     * @exception IllegalArgumentException
     */
    public String createJob(FormDataSet formDataSet) throws ClientUWSException {
        if (!Util.isSet(formDataSet)) {
            throw new IllegalArgumentException("Process: form cannot be null");
        }
        String jobId = null;
        ClientResource client = new ClientResource(Method.POST, this.jobsUWS);
        client.setFollowingRedirects(false);
        client.post(formDataSet);
        if (client.getStatus().isRedirection()) {
            Reference locationJob = client.getResponse().getLocationRef();
            jobId = locationJob.getLastSegment();
        } else {
            client.release();
            throw new ClientUWSException(client.getStatus(), "Process: Cannot create a new Job");
        }
        client.release();
        return jobId;
    }

    /**
     * Get the remaining tasks. A remain task has the following status :
     * EXECUTING, QUEUED, PENDING, HELD
     * @param jobsId List of job ID to test
     * @return Returns the remaining tasks with the following structure {taskID, phase name}
     * @exception ClientUWSException
     * @exception IllegalArgumentException
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
            if (phase.equals(phase.EXECUTING) || phase.equals(phase.QUEUED) || phase.equals(phase.PENDING) || phase.equals(phase.HELD)) {
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
     * @exception IllegalArgumentException
     */
    public JobSummary getJobInfo(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetJobInfo: jobId is required");
        }
        JobSummary jobSummaryResponse = null;
        ClientResource client = new ClientResource(Method.GET, this.jobsUWS + "/" + jobId);
        if (client.getStatus().isSuccess()) {
            try {
                JAXBContext ctx = JAXBContext.newInstance(new Class[]{net.ivoa.xml.uws.v1.JobSummary.class});
                Unmarshaller um = ctx.createUnmarshaller();
                JAXBElement<JobSummary> response = (JAXBElement<JobSummary>) um.unmarshal(new ByteArrayInputStream(client.get().getText().getBytes()));
                jobSummaryResponse = response.getValue();
            } catch (IOException ex) {
                Engine.getLogger(ClientUWS.class.getName()).log(Level.SEVERE, null, ex);
                throw new ClientUWSException(ex);
            } catch (JAXBException ex) {
                Engine.getLogger(ClientUWS.class.getName()).log(Level.SEVERE, null, ex);
                throw new ClientUWSException(ex);
            } catch (ResourceException ex) {
                throw new ClientUWSException(ex);
            } finally {
                client.release();
            }
        } else {
            client.release();
            throw new ClientUWSException(client.getStatus(), "GetJobInfo: Cannot get information about " + jobId);
        }
        return jobSummaryResponse;
    }

    /**
     * Delete a job
     * @param jobId JobId
     * @exception IllegalArgumentException
     * @exception ClientUWSException
     */
    public void deleteJobInfo(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetJobInfo: jobId is required");
        }
        ClientResource client = new ClientResource(Method.DELETE, this.jobsUWS + "/" + jobId);
        client.setFollowingRedirects(false);
        try {
            client.delete();
        } catch (ResourceException ex) {
            client.release();
            throw new ClientUWSException(ex.getStatus(), "deleteJobInfo: Cannot delete " + jobId);
        }
        if (client.getStatus().isRedirection()) {
            // do nothing
        } else {
            client.release();
            throw new ClientUWSException(client.getStatus(), "deleteJobInfo: No redirect is done after deleting " + jobId);
        }
        client.release();

    }

    /**
     * Get information about a specific job ID and delete this job after getting information
     * @param jobId Job ID
     * @param mustBeDeleted Set to true to delete the job after getting job information
     * @return Returns information about the job ID
     * @exception ClientUWSException
     * @exception IllegalArgumentException
     */
    public JobSummary getJobInfo(String jobId, boolean mustBeDeleted) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetJobInfo: jobId is required");
        }

        JobSummary jobSummary = this.getJobInfo(jobId);
        if (mustBeDeleted) {
            ClientResource client = new ClientResource(Method.DELETE, this.jobsUWS);
            client.delete();
            if (client.getStatus().isRedirection()) {
                // do nothing
            } else {
                client.release();
                throw new ClientUWSException(client.getStatus(), "deleteJobInfo: Cannot delete " + jobId);
            }
            client.release();
        }
        return jobSummary;
    }

    /**
     * Get list of jobs
     * @return Returns list of jobs
     * @exception ClientUWSException
     * @exception IllegalArgumentException
     */
    public Jobs getJobs() throws ClientUWSException {
        Jobs jobsResponse = null;
        ClientResource client = new ClientResource(Method.GET, this.jobsUWS);
        if (client.getStatus().isSuccess()) {
            try {
                JAXBContext ctx = JAXBContext.newInstance(new Class[]{Jobs.class});
                Unmarshaller um = ctx.createUnmarshaller();
                jobsResponse = (Jobs) um.unmarshal(new ByteArrayInputStream(client.get().getText().getBytes()));
            } catch (IOException ex) {
                Engine.getLogger(ClientUWS.class.getName()).log(Level.SEVERE, null, ex);
                throw new ClientUWSException(ex);
            } catch (JAXBException ex) {
                Engine.getLogger(ClientUWS.class.getName()).log(Level.SEVERE, null, ex);
                throw new ClientUWSException(ex);
            } finally {
                client.release();
            }
        } else {
            client.release();
            throw new ClientUWSException(client.getStatus(), "GetJobs: Cannot retrieve the list of jobs");
        }
        return jobsResponse;
    }

    /**
     * Set a desctruction time for a given job
     * @param jobId JobID
     * @param inputDate Date
     * @exception ClientUWSException
     * @exception IllegalArgumentException
     */
    public void setDestructionTimeJob(String jobId, Date inputDate) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("setDestructionTimeJob: jobId is required");
        }
        try {
            XMLGregorianCalendar calendar = Util.convertIntoXMLGregorian(inputDate);
            ClientResource client = new ClientResource(Method.POST, this.jobsUWS + "/" + jobId + "/destruction");
            client.setFollowingRedirects(false);
            Form form = new Form();
            form.add("DESTRUCTION", calendar.toString());
            client.post(form);
            if (client.getStatus().isRedirection()) {
                // do nothing
            } else {
                client.release();
                throw new ClientUWSException(client.getStatus(), "SetDestructionTimeJob: Unable to set DESTRUCTION=" + calendar.toString());
            }
            client.release();
        } catch (DatatypeConfigurationException ex) {
            throw new ClientUWSException(ex);
        }
    }

    /**
     * Set the exucation duration for a given job
     * @param jobId JobID
     * @param timeInSeconds Execution time in seconds
     * @exception ClientUWSException
     * @exception IllegalArgumentException
     */
    public void setExecutionDurationJob(String jobId, int timeInSeconds) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("setExecutionDurationJob: jobId is required");
        }
        ClientResource client = new ClientResource(Method.POST, this.jobsUWS + "/" + jobId + "/executionduration");
        client.setFollowingRedirects(false);
        Form form = new Form();
        form.add("EXECUTIONDURATION", String.valueOf(timeInSeconds));
        client.post(form);
        if (client.getStatus().isRedirection()) {
            // do nothing
        } else {
            client.release();
            throw new ClientUWSException(client.getStatus(), "SetExecutionDurationJob: Unable to set EXECUTIONDURATION=" + timeInSeconds);
        }
        client.release();
    }

    /**
     * Start a job
     * @param jobId JobID
     * @exception ClientUWSException
     * @exception IllegalArgumentException
     */
    public void setStartJob(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("setStartJob: jobId is required");
        }
        ClientResource client = new ClientResource(Method.POST, this.jobsUWS + "/" + jobId + "/phase");
        client.setFollowingRedirects(false);
        Form form = new Form();
        form.add("PHASE", "RUN");
        client.post(form);
        if (client.getStatus().isRedirection()) {
            // do nothing
        } else {
            client.release();
            throw new ClientUWSException(client.getStatus(), "SetStartJob: Unable to start the " + jobId);
        }
        client.release();
    }

    /**
     * Stop a job
     * @param jobId
     * @exception ClientUWSException
     * @exception IllegalArgumentException
     */
    public void setAbortJob(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("setAbortJob: jobId is required");
        }
        ClientResource client = new ClientResource(Method.POST, this.jobsUWS + "/" + jobId + "/phase");
        client.setFollowingRedirects(false);
        Form form = new Form();
        form.add("PHASE", "ABORT");
        client.post(form);
        if (client.getStatus().isRedirection()) {
            // do nothing
        } else {
            client.release();
            throw new ClientUWSException(client.getStatus(), "SetAbortJob: Unable to abort the " + jobId);
        }
        client.release();
    }

    /**
     * Get Error of a job ID
     * @param jobId Job ID
     * @return Returns ErrorSummary
     * @exception ClientUWSException
     * @exception IllegalArgumentException
     */
    public ErrorSummary getJobError(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetJobError: jobId is required");
        }
        ErrorSummary errorSummaryResponse = null;
        ClientResource client = new ClientResource(Method.GET, this.jobsUWS + "/" + jobId + "/error");
        if (client.getStatus().isSuccess()) {
            try {
                JAXBContext ctx = JAXBContext.newInstance(new Class[]{net.ivoa.xml.uws.v1.ErrorSummary.class});
                Unmarshaller um = ctx.createUnmarshaller();
                JAXBElement<ErrorSummary> response = (JAXBElement<ErrorSummary>) um.unmarshal(new ByteArrayInputStream(client.get().getText().getBytes()));
                errorSummaryResponse = response.getValue();
            } catch (IOException ex) {
                Engine.getLogger(ClientUWS.class.getName()).log(Level.SEVERE, null, ex);
                throw new ClientUWSException(ex);
            } catch (JAXBException ex) {
                Engine.getLogger(ClientUWS.class.getName()).log(Level.SEVERE, null, ex);
                throw new ClientUWSException(ex);
            } finally {
                client.release();
            }
        } else {
            client.release();
            throw new ClientUWSException(client.getStatus(), "GetJobError: Cannot get error about " + jobId);
        }
        return errorSummaryResponse;
    }

    /**
     * Get Quote of a job ID
     * @param jobId Job ID
     * @return Returns quote as a string
     * @exception ClientUWSException
     * @exception IllegalArgumentException
     */
    public String getJobQuote(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetJobQuote: jobId is required");
        }
        JobSummary jobSummaryResponse = null;
        ClientResource client = new ClientResource(Method.GET, this.jobsUWS + "/" + jobId);
        if (client.getStatus().isSuccess()) {
            try {
                JAXBContext ctx = JAXBContext.newInstance(new Class[]{net.ivoa.xml.uws.v1.JobSummary.class});
                Unmarshaller um = ctx.createUnmarshaller();
                JAXBElement<JobSummary> response = (JAXBElement<JobSummary>) um.unmarshal(new ByteArrayInputStream(client.get().getText().getBytes()));
                jobSummaryResponse = response.getValue();
            } catch (IOException ex) {
                Engine.getLogger(ClientUWS.class.getName()).log(Level.SEVERE, null, ex);
                throw new ClientUWSException(ex);
            } catch (JAXBException ex) {
                Engine.getLogger(ClientUWS.class.getName()).log(Level.SEVERE, null, ex);
                throw new ClientUWSException(ex);
            } finally {
                client.release();
            }
        } else {
            client.release();
            throw new ClientUWSException(client.getStatus(), "GetJobInfo: Cannot get information about " + jobId);
        }
        return jobSummaryResponse.getQuote().toString();
    }

    /**
     * Get results of a job ID
     * @param jobId Job ID
     * @return Returns Results
     * @exception ClientUWSException
     * @exception IllegalArgumentException
     */
    public Results getJobResults(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetJobResults: jobId is required");
        }
        Results resultsResponse = null;
        ClientResource client = new ClientResource(Method.GET, this.jobsUWS + "/" + jobId + "/results");
        if (client.getStatus().isSuccess()) {
            try {
                JAXBContext ctx = JAXBContext.newInstance(new Class[]{net.ivoa.xml.uws.v1.Results.class});
                Unmarshaller um = ctx.createUnmarshaller();
                Results response = (Results) um.unmarshal(new ByteArrayInputStream(client.get().getText().getBytes()));
                resultsResponse = response;
            } catch (IOException ex) {
                Engine.getLogger(ClientUWS.class.getName()).log(Level.SEVERE, null, ex);
                throw new ClientUWSException(ex);
            } catch (JAXBException ex) {
                Engine.getLogger(ClientUWS.class.getName()).log(Level.SEVERE, null, ex);
                throw new ClientUWSException(ex);
            } finally {
                client.release();
            }
        } else {
            client.release();
            throw new ClientUWSException(client.getStatus(), "GetJobResults: Cannot get results about " + jobId);
        }
        return resultsResponse;
    }

    /**
     * Get prameters of a job ID
     * @param jobId Job ID
     * @return Returns Parameters
     * @exception ClientUWSException
     * @exception IllegalArgumentException
     */
    public Parameters getJobParameters(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetJobParameters: jobId is required");
        }
        Parameters parametersResponse = null;
        ClientResource client = new ClientResource(Method.GET, this.jobsUWS + "/" + jobId + "/parameters");
        if (client.getStatus().isSuccess()) {
            try {
                JAXBContext ctx = JAXBContext.newInstance(new Class[]{net.ivoa.xml.uws.v1.Parameters.class});
                Unmarshaller um = ctx.createUnmarshaller();
                Parameters response = (Parameters) um.unmarshal(new ByteArrayInputStream(client.get().getText().getBytes()));
                parametersResponse = response;
            } catch (IOException ex) {
                Engine.getLogger(ClientUWS.class.getName()).log(Level.SEVERE, null, ex);
                throw new ClientUWSException(ex);
            } catch (JAXBException ex) {
                Engine.getLogger(ClientUWS.class.getName()).log(Level.SEVERE, null, ex);
                throw new ClientUWSException(ex);
            } finally {
                client.release();
            }
        } else {
            client.release();
            throw new ClientUWSException(client.getStatus(), "GetJobParameters: Cannot get parameters about " + jobId);
        }
        return parametersResponse;
    }

    /**
     * Get Owner of a job ID
     * @param jobId Job ID
     * @return Returns owner as string
     * @exception ClientUWSException
     * @exception IllegalArgumentException
     */
    public String getJobOwner(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetJobOwner: jobId is required");
        }
        String ownerResponse = null;
        ClientResource client = new ClientResource(Method.GET, this.jobsUWS + "/" + jobId + "/owner");
        if (client.getStatus().isSuccess()) {
            try {
                ownerResponse = client.get().getText();
            } catch (IOException ex) {
                throw new ClientUWSException(ex);
            } finally {
                client.release();
            }
        } else {
            client.release();
            throw new ClientUWSException(client.getStatus(), "GetOwner: Cannot get owner about " + jobId);
        }
        return ownerResponse;
    }

    /**
     * Get Phase of a job ID
     * @param jobId Job ID
     * @return Returns ExecutionPhase
     * @exception ClientUWSException
     * @exception IllegalArgumentException
     */
    public ExecutionPhase getJobPhase(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetJobPhase: jobId is required");
        }
        ExecutionPhase phaseResponse = null;
        ClientResource client = new ClientResource(Method.GET, this.jobsUWS + "/" + jobId + "/phase");
        if (client.getStatus().isSuccess()) {
            try {
                phaseResponse = ExecutionPhase.valueOf(client.get().getText());
            } catch (IOException ex) {
                throw new ClientUWSException(ex);
            } finally {
                client.release();
            }
        } else {
            client.release();
            throw new ClientUWSException(client.getStatus(), "GetPhase: Cannot get phase about " + jobId);
        }
        return phaseResponse;
    }

    public void setParameter(String jobId, String key) throws ClientUWSException {
        if (!Util.isSet(jobId) || !Util.isSet(key)) {
            throw new IllegalArgumentException("setParameter: jobId and key are required");
        }
        ClientResource client = new ClientResource(Method.PUT, this.jobsUWS + "/" + jobId + "/parameters/" + key);
        if (client.getStatus().isSuccess()) {
        } else {
            client.release();
            throw new ClientUWSException(client.getStatus(), "setOwner: Cannot get owner about " + jobId);
        }
        client.release();
    }

    /**
     * Get ExecutionDuration of a job ID
     * @param jobId Job ID
     * @return Returns executionduration as a string
     * @exception ClientUWSException
     * @exception IllegalArgumentException
     */
    public String getExecutionDuration(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetExecutionDuration: jobId is required");
        }
        String edResponse = null;
        ClientResource client = new ClientResource(Method.GET, this.jobsUWS + "/" + jobId + "/executionduration");
        if (client.getStatus().isSuccess()) {
            try {
                edResponse = client.get().getText();
            } catch (IOException ex) {
                throw new ClientUWSException(ex);
            } finally {
                client.release();
            }
        } else {
            client.release();
            throw new ClientUWSException(client.getStatus(), "GetExecutionDuration: Cannot get information about " + jobId);
        }
        return edResponse;
    }

    /**
     * Get DestructionTime of a job ID
     * @param jobId Job ID
     * @return Returns destructionTime as ISO8601 format
     * @exception ClientUWSException
     * @exception IllegalArgumentException
     */
    public String getDestructionTime(String jobId) throws ClientUWSException {
        if (!Util.isSet(jobId)) {
            throw new IllegalArgumentException("GetDestructionTime: jobId is required");
        }
        String edResponse = null;
        ClientResource client = new ClientResource(Method.GET, this.jobsUWS + "/" + jobId + "/destruction");
        if (client.getStatus().isSuccess()) {
            try {
                edResponse = client.get().getText();
            } catch (IOException ex) {
                throw new ClientUWSException(ex);
            } finally {
                client.release();
            }
        } else {
            client.release();
            throw new ClientUWSException(client.getStatus(), "GetDestructionTime: Cannot get information about " + jobId);
        }
        return edResponse;
    }
}
