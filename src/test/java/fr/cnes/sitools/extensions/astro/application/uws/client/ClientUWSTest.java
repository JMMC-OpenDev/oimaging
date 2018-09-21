/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.cnes.sitools.extensions.astro.application.uws.client;

import fr.jmmc.oimaging.services.RemoteExecutionMode;
import java.io.File;
import net.ivoa.xml.uws.v1.Jobs;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.FileRepresentation;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mellag
 */
public class ClientUWSTest {

    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(ClientUWSTest.class.getName());

    static ClientUWS instance;

    public ClientUWSTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        instance = new ClientUWS("http://127.0.0.1:8080/OImaging-uws/", RemoteExecutionMode.SERVICE_PATH);
    }

    @AfterClass
    public static void tearDownClass() {

    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testMain() throws Exception {
        File inputFile = new File("src/test/resources/Bin_Ary--MIRC_H_with_img.fits");

        logger.info("submit input file : " + inputFile.getAbsolutePath());

        // Prepare form with autostart
        FormDataSet fds = new FormDataSet("time=20");
        fds.setMultipart(true);

        fds.add("PHASE", "RUN");

        Disposition disposition = new Disposition(Disposition.TYPE_INLINE);
        FileRepresentation entity = new FileRepresentation(inputFile, MediaType.ALL);
        entity.setDisposition(disposition);

        FormData fdFile = new FormData("inputfile", entity);
        fds.getEntries().add(fdFile);

        // Create job
        String jobId = instance.createJob(fds);
        logger.warn("new jobid: " + jobId);

        // Get job info
        System.out.println("JobInfo:" + instance.getJobInfo(jobId));

        // Start jobs (if not previously autostarted)
        //instance.setStartJob(jobId);
        // List jobs
        Jobs result = instance.getJobs();
        System.out.println("result = " + result.getJobref());
    }
}
