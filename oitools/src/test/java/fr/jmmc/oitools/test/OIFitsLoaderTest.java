/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.test;

import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OITableUtils;
import fr.jmmc.oitools.model.XmlOutputVisitor;
import java.io.File;
import java.util.logging.Level;

/**
 * This class provides test cases for the OIFitsLoader class
 * @author bourgesl
 */
public class OIFitsLoaderTest implements TestEnv {

    /**
     * Forbidden constructor
     */
    private OIFitsLoaderTest() {
        super();
    }

    public static void main(String[] args) {
        int n = 0;
        int errors = 0;

        /*
[bourgesl@jmmc-laurent roxanne]$ pwd

[bourgesl@jmmc-laurent roxanne]$ ll
total 15380
-rw-r--r--. 1 bourgesl laogsite   190080  5 avril 09:19 Test1_aspro095beta1.fits
-rw-r--r--. 1 bourgesl laogsite 15554880  5 avril 09:24 Test1.fits
[bourgesl@jmmc-laurent roxanne]$ 

         */
        if (true) {

            // Compare two files:
            final String path = "/home/bourgesl/oidata/roxanne/";

            OIFitsFile refOIFitsFile, testOIFitsFile;

            refOIFitsFile = loadFile(path + "Test1_aspro095beta1.fits");
            if (refOIFitsFile == null) {
                errors++;
            } else {
                // verify and check :
                testOIFitsFile = loadFile(path + "Test1.fits");
                if (testOIFitsFile == null) {
                    errors++;
                } else if (!OITableUtils.compareOIFitsFile(refOIFitsFile, testOIFitsFile)) {
                    errors++;
                }
            }

            if (true) {
                return;
            }
        }
        
        if (true) {

            // Compare two files:
            final String path = "/home/bourgesl/ASPRO2/oifits/";

            OIFitsFile refOIFitsFile, testOIFitsFile;

            // Aspro2 AMBER 0.9.4b4 vs 0.9.4b11: NOISE
            refOIFitsFile = loadFile(path + "test_AMBER_OLD_NOISE.fits");
            if (refOIFitsFile == null) {
                errors++;
            } else {
                // verify and check :
                testOIFitsFile = loadFile(path + "test_AMBER_LAST_NOISE.fits");
                if (testOIFitsFile == null) {
                    errors++;
                } else if (!OITableUtils.compareOIFitsFile(refOIFitsFile, testOIFitsFile)) {
                    errors++;
                }
                /*
                 WARNING: WARN:  Column[VISAMP]     Max Absolute Error=1.2434497875801753E-14	Max Relative Error=1.9002835247700255E-13
                 WARNING: WARN:  Column[VISAMPERR]	Max Absolute Error=2.517985819849855E-13	Max Relative Error=1.9590283162074006E-13
                 WARNING: WARN:  Column[VISPHIERR]	Max Absolute Error=4.411049303598702E-11	Max Relative Error=5.398521776826671E-13
                 WARNING: WARN:  Column[UCOORD]     Max Absolute Error=2.8421709430404007E-14	Max Relative Error=6.932823324250251E-16
                 WARNING: WARN:  Column[VCOORD]     Max Absolute Error=4.973799150320701E-14	Max Relative Error=8.842632774677416E-15
                 * 
                 WARNING: WARN:  Column[VIS2DATA]	Max Absolute Error=8.881784197001252E-16	Max Relative Error=3.797375869044365E-13
                 WARNING: WARN:  Column[VIS2ERR]	Max Absolute Error=2.3592239273284576E-16	Max Relative Error=4.672404518559193E-15
                 WARNING: WARN:  Column[UCOORD]     Max Absolute Error=2.8421709430404007E-14	Max Relative Error=6.932823324250251E-16
                 WARNING: WARN:  Column[VCOORD]     Max Absolute Error=4.973799150320701E-14	Max Relative Error=8.842632774677416E-15
                 * 
                 WARNING: WARN:  Column[T3AMP]      Max Absolute Error=1.0148132334464322E-16	Max Relative Error=1.9285117301102395E-13
                 WARNING: WARN:  Column[T3AMPERR]	Max Absolute Error=3.989863994746656E-15	Max Relative Error=1.881555067821488E-13
                 WARNING: WARN:  Column[T3PHIERR]	Max Absolute Error=5.056790541857481E-10	Max Relative Error=9.12477073025435E-14
                 WARNING: WARN:  Column[U1COORD]	Max Absolute Error=2.8421709430404007E-14	Max Relative Error=4.048136363842218E-16
                 WARNING: WARN:  Column[V1COORD]	Max Absolute Error=4.263256414560601E-14	Max Relative Error=8.842632774677416E-15
                 WARNING: WARN:  Column[U2COORD]	Max Absolute Error=1.7763568394002505E-14	Max Relative Error=6.932823324250251E-16
                 WARNING: WARN:  Column[V2COORD]	Max Absolute Error=7.105427357601002E-15	Max Relative Error=2.7668589851061884E-16
                 */
            }

            if (true) {
                return;
            }
        }

        if (true) {

            // Compare two files:
            final String path = "/home/bourgesl/ASPRO2/oifits/";

            OIFitsFile refOIFitsFile, testOIFitsFile;

            // Aspro2 AMBER 0.9.4b10 vs 0.9.4b11: FASTMATH
            refOIFitsFile = loadFile(path + "test_AMBER_NEW.fits");
            if (refOIFitsFile == null) {
                errors++;
            } else {
                // verify and check :
                testOIFitsFile = loadFile(path + "test_AMBER_LAST.fits");
                if (testOIFitsFile == null) {
                    errors++;
                } else if (!OITableUtils.compareOIFitsFile(refOIFitsFile, testOIFitsFile)) {
                    errors++;
                }
                /*
                 WARNING: WARN:  Column[UCOORD]     Max Absolute Error=2.8421709430404007E-14	Max Relative Error=6.932823324250251E-16
                 WARNING: WARN:  Column[VCOORD]     Max Absolute Error=4.973799150320701E-14	Max Relative Error=8.458170480126223E-15
                 WARNING: WARN:  Column[VIS2DATA]	Max Absolute Error=8.881784197001252E-16	Max Relative Error=1.8593237759819683E-13
                 WARNING: WARN:  Column[UCOORD]     Max Absolute Error=2.8421709430404007E-14	Max Relative Error=6.932823324250251E-16
                 WARNING: WARN:  Column[VCOORD]     Max Absolute Error=4.973799150320701E-14	Max Relative Error=8.458170480126223E-15
                 WARNING: WARN:  Column[T3AMP]      Max Absolute Error=1.0148132334464322E-16	Max Relative Error=9.278394345247643E-14
                 WARNING: WARN:  Column[U1COORD]	Max Absolute Error=2.8421709430404007E-14	Max Relative Error=4.048136363842218E-16
                 WARNING: WARN:  Column[V1COORD]	Max Absolute Error=3.907985046680551E-14	Max Relative Error=8.458170480126223E-15
                 WARNING: WARN:  Column[U2COORD]	Max Absolute Error=1.7763568394002505E-14	Max Relative Error=6.932823324250251E-16
                 WARNING: WARN:  Column[V2COORD]	Max Absolute Error=7.105427357601002E-15	Max Relative Error=2.7668589851061884E-16
                 */
            }

            if (true) {
                return;
            }
        }

        if (true) {

            // Compare two files:
            final String path = "/home/bourgesl/ASPRO2/oifits/";

            OIFitsFile refOIFitsFile, testOIFitsFile;

            // Aspro2 AMBER 0.9.3 vs 0.9.4:
            refOIFitsFile = loadFile(path + "test_AMBER_OLD.fits");
            if (refOIFitsFile == null) {
                errors++;
            } else {
                // verify and check :
                testOIFitsFile = loadFile(path + "test_AMBER_NEW.fits");
                if (testOIFitsFile == null) {
                    errors++;
                } else if (!OITableUtils.compareOIFitsFile(refOIFitsFile, testOIFitsFile)) {
                    errors++;
                }
                /*
                 * OK: staIndex arrays OK (baselines and triplets)
                 * errors OK: less than 10^-13
                 WARNING: WARN:  Column[DIAMETER]	Max Absolute Error=6.399999618530273        Max Relative Error=0.7804877765117686   NORMAL (station changed)
                 WARNING: WARN:  Column[VCOORD]     Max Absolute Error=3.552713678800501E-15	Max Relative Error=4.530799407221996E-16
                 WARNING: WARN:  Column[VIS2DATA]	Max Absolute Error=4.440892098500626E-16	Max Relative Error=3.797375869044365E-13
                 WARNING: WARN:  Column[VCOORD]     Max Absolute Error=3.552713678800501E-15	Max Relative Error=4.530799407221996E-16
                 WARNING: WARN:  Column[T3AMP]      Max Absolute Error=8.847089727481716E-17	Max Relative Error=1.9209637389943497E-13
                 WARNING: WARN:  Column[V1COORD]	Max Absolute Error=3.552713678800501E-15	Max Relative Error=3.8446229455119526E-16
                 WARNING: WARN:  Column[V2COORD]	Max Absolute Error=3.552713678800501E-15	Max Relative Error=1.6383446543084652E-16
                 */
            }

            if (true) {
                return;
            }
        }

        if (true) {
            // Compare two files:
            final String path = "/home/bourgesl/ASPRO2/oifits/";

            OIFitsFile refOIFitsFile, testOIFitsFile;

            // DEFAULT vs FAST:
            refOIFitsFile = loadFile(path + "Aspro2_ETA_TAU_GRAVITY_UT1-UT2-UT3-UT4_DEFAULT.fits");
            if (refOIFitsFile == null) {
                errors++;
            } else {
                // verify and check :
                testOIFitsFile = loadFile(path + "Aspro2_ETA_TAU_GRAVITY_UT1-UT2-UT3-UT4_FAST.fits");
                if (testOIFitsFile == null) {
                    errors++;
                } else if (!OITableUtils.compareOIFitsFile(refOIFitsFile, testOIFitsFile)) {
                    errors++;
                }
                /*                
                 WARNING: WARN:  Column[VISAMP]	Max Absolute Error=3.122502256758253E-17	Max Relative Error=2.943534976436331E-14
                 WARNING: WARN:  Column[VISAMPERR]	Max Absolute Error=0.0013869817652312072	Max Relative Error=0.0997510362307679
                 WARNING: WARN:  Column[VISPHI]	Max Absolute Error=1.8474111129762605E-12	Max Relative Error=3.3158630356109147E-12
                 WARNING: WARN:  Column[VISPHIERR]	Max Absolute Error=19.19686940833244	Max Relative Error=0.9113901536192872
                 WARNING: WARN:  Column[VIS2DATA]	Max Absolute Error=1.5178830414797062E-18	Max Relative Error=5.915784768102743E-14
                 WARNING: WARN:  Column[VIS2ERR]	Max Absolute Error=5.421010862427522E-20	Max Relative Error=4.471809116292319E-16
                 WARNING: WARN:  Column[T3AMP]	Max Absolute Error=9.740878893424454E-21	Max Relative Error=3.613928543746403E-14
                 WARNING: WARN:  Column[T3AMPERR]	Max Absolute Error=3.441071348220595E-22	Max Relative Error=3.6192130029721625E-14
                 WARNING: WARN:  Column[T3PHI]	Max Absolute Error=1.8332002582610585E-12	Max Relative Error=1.6154567952731343E-13
                 */
            }

            refOIFitsFile = loadFile(path + "Aspro2_ETA_TAU_GRAVITY_UT1-UT2-UT3-UT4_FAST.fits");
            if (refOIFitsFile == null) {
                errors++;
            } else {
                // verify and check :
                testOIFitsFile = loadFile(path + "Aspro2_ETA_TAU_GRAVITY_UT1-UT2-UT3-UT4_QUICK.fits");
                if (testOIFitsFile == null) {
                    errors++;
                } else if (!OITableUtils.compareOIFitsFile(refOIFitsFile, testOIFitsFile)) {
                    errors++;
                }
                /*
                 WARNING: WARN:  Column[VISDATA]	Max Absolute Error=0.21170902252197266	Max Relative Error=155.32336222596265
                 WARNING: WARN:  Column[VISERR]	Max Absolute Error=1.7113983631134033E-5	Max Relative Error=5.123198196063256E-4
                 WARNING: WARN:  Column[VISAMP]	Max Absolute Error=0.012389325449663476	Max Relative Error=0.9827257597222762
                 WARNING: WARN:  Column[VISAMPERR]	Max Absolute Error=4.3704479622192665E-4	Max Relative Error=0.24928702639103537
                 WARNING: WARN:  Column[VISPHI]	Max Absolute Error=357.76365704000574	Max Relative Error=132.85957062222084
                 WARNING: WARN:  Column[VISPHIERR]	Max Absolute Error=54.185925961293876	Max Relative Error=0.9643291278418655
                 WARNING: WARN:  Column[VIS2DATA]	Max Absolute Error=4.3211132008532375E-4	Max Relative Error=3199.047758503112
                 WARNING: WARN:  Column[VIS2ERR]	Max Absolute Error=7.815369519747367E-9	Max Relative Error=6.780968471059581E-5
                 WARNING: WARN:  Column[T3AMP]	Max Absolute Error=1.8693429580914967E-7	Max Relative Error=0.11262751096020436
                 WARNING: WARN:  Column[T3AMPERR]	Max Absolute Error=4.423268697588574E-11	Max Relative Error=0.0016543021640061009
                 WARNING: WARN:  Column[T3PHI]	Max Absolute Error=6.734992735788779	Max Relative Error=6.813763216810152
                 */
            }
            return;
        }

        if (false) {
            // Bad File path :
//      final String file = TEST_DIR + "toto";

            // Invalid OI Fits (Fits image) :
//      final String file = TEST_DIR + "other/YSO_disk.fits.gz";

            // Complex visibilities in VISDATA / VISERR (OI_VIS table) :
//      final String file = TEST_DIR + "ASPRO-STAR_1-AMBER-08-OCT-2009T08:17:39.fits";

            // 1 extra byte at the End of file + NaN in vis* data :
//      final String file = TEST_DIR + "Mystery-Med_H-AmberVISPHI.oifits.gz";


            final String file = "/home/bourgesl/Documents/aspro-docs/aspro/ASPRO-ETA_TAU-AMBER-23-JUN-2010T15:26:46.fits";

            // Single Wave Length => NWAVE = 1 => 1D arrays instead of 2D arrays :
//      final String file = TEST_DIR + "2004-data2.fits";

            n++;
            errors += load(file);
        }

        if (true) {
            final File directory = new File(TEST_DIR);
            if (directory.exists() && directory.isDirectory()) {

                final long start = System.nanoTime();

                final File[] files = directory.listFiles();

                for (File f : files) {
                    if (f.isFile() && (f.getName().endsWith("fits") || f.getName().endsWith("fits.gz"))) {
                        n++;
                        errors += load(f.getAbsolutePath());
                    }
                }

                logger.info("dumpDirectory : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");
            }
        }

        logger.info("Errors = " + errors + " on " + n + " files.");
    }

    private static int load(final String absFilePath) {
        int error = 0;
        try {
            logger.info("Loading file : " + absFilePath);

            final long start = System.nanoTime();

            final OIFitsFile oiFitsFile = OIFitsLoader.loadOIFits(absFilePath);

            logger.info("load : toString : \n" + oiFitsFile.toString());

            if (false) {
                final boolean detailed = true;
                logger.info("load : XML DESC : \n" + XmlOutputVisitor.getXmlDesc(oiFitsFile, detailed));
            }

            logger.info("load : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

        } catch (Throwable th) {
            logger.log(Level.SEVERE, "load : IO failure occured while reading file : " + absFilePath, th);
            error = 1;
        }
        return error;
    }

    private static OIFitsFile loadFile(final String absFilePath) {
        OIFitsFile oiFitsFile = null;
        try {
            logger.info("Loading file : " + absFilePath);

            final long start = System.nanoTime();

            oiFitsFile = OIFitsLoader.loadOIFits(absFilePath);

            logger.info("load : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

        } catch (Throwable th) {
            logger.log(Level.SEVERE, "load : IO failure occured while reading file : " + absFilePath, th);
        }
        return oiFitsFile;
    }
}
