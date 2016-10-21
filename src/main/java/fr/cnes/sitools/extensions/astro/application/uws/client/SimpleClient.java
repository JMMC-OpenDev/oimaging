/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.cnes.sitools.extensions.astro.application.uws.client;

import java.io.File;
import java.io.IOException;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 *
 * @author mellag
 */
public class SimpleClient {

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub

        // Create the client resource
        ClientResource resource = new ClientResource("http://127.0.0.1:8080/uws/oimaging/oimaging");

        Form fileForm = new Form();
        fileForm.add("toto", "toto");

//        FormDataSet fds = new FormDataSet();
//        fds.setMultipart(true);
//        fds.add("toto", "value");
        //fileForm.add(Disposition.NAME_FILENAME, "myimage.png");
        //Disposition disposition = new Disposition(Disposition.TYPE_INLINE, fileForm);
        File f = new File("/etc/passwd");
        FileRepresentation entity = new FileRepresentation(f, MediaType.IMAGE_PNG);
        //entity.setDisposition(disposition);

        //FormData fdFile = new FormData("fileToUpload", entity);
        //fds.getEntries().add(fdFile);
        // FormData fdValue = new FormData("field", "value");
        //fds.getEntries().add(fdValue);
        // Write the response entity on the console
        try {
            resource.post(fileForm).write(System.out);
        } catch (ResourceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
