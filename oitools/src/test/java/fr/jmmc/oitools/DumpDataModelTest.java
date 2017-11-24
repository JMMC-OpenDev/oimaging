/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools;

import static fr.jmmc.oitools.JUnitBaseTest.logger;
import fr.jmmc.oitools.model.DataModel;
import java.util.logging.Level;
import org.junit.Test;

/**
 * Warning: to avoid any side effect, calling DataModel.main() must be alone in the test file
 * @author kempsc
 */
public class DumpDataModelTest {

    @Test
    public void dumpDataModels() {
        try {
            DataModel.main(null);
        } catch (Throwable th) {
            logger.log(Level.SEVERE, "failure:", th);
        }
    }

}
