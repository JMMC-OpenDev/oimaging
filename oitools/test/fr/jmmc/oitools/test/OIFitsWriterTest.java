/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OIFitsWriterTest.java,v 1.6 2010-09-06 13:49:50 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.5  2010/06/21 10:03:14  bourgesl
 * updated creation code to produce a valid OIFits file
 *
 * Revision 1.4  2010/06/18 15:43:07  bourgesl
 * new test case : create an OIFits file from scratch
 *
 * Revision 1.3  2010/06/02 15:23:53  bourgesl
 * added a test case which copy all OIFits files from oidata/ to oidata/copy and compare files
 *
 * Revision 1.2  2010/06/02 11:52:27  bourgesl
 * use logger instead of System.out
 *
 * Revision 1.1  2010/05/28 14:57:45  bourgesl
 * first attempt to write OIFits from a loaded OIFitsFile structure
 *
 */
package fr.jmmc.oitools.test;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.model.OIArray;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.jmmc.oitools.model.OIT3;
import fr.jmmc.oitools.model.OITableUtils;
import fr.jmmc.oitools.model.OITarget;
import fr.jmmc.oitools.model.OIVis;
import fr.jmmc.oitools.model.OIVis2;
import fr.jmmc.oitools.model.OIWavelength;
import fr.jmmc.oitools.model.XmlOutputVisitor;
import fr.jmmc.oitools.test.fits.TamFitsTest;
import java.io.File;
import java.util.logging.Level;

/**
 * This class provides test cases for the OIFitsWriter class
 * @author bourgesl
 */
public class OIFitsWriterTest implements TestEnv {

  /** flag to compare raw fits files */
  private final static boolean COMPARE_FITS = false;

  /**
   * Forbidden constructor
   */
  private OIFitsWriterTest() {
    super();
  }

  public static void main(String[] args) {
    int n = 0;
    int errors = 0;

    if (false) {
      // Bad File path :
//      final String file = TEST_DIR + "toto";

      // Invalid OI Fits (Fits image) :
//      final String file = TEST_DIR + "other/YSO_disk.fits.gz";

      // Complex visibilities in VISDATA / VISERR (OI_VIS table) :
//      final String fileSrc = TEST_DIR + "ASPRO-STAR_1-AMBER-08-OCT-2009T08:17:39";
//      final String ext = ".fits";

      // 1 extra byte at the End of file + NaN in vis* data :
      // missing TEL_NAME (empty values) :
      final String fileSrc = TEST_DIR + "Mystery-Med_H-AmberVISPHI";
      final String ext = ".oifits.gz";

      // Single Wave Length => NWAVE = 1 => 1D arrays instead of 2D arrays :
//      final String file = TEST_DIR + "2004-data2.fits";


//      final String fileSrc = TEST_DIR + "2008-Contest2_H";
//      final String ext = ".oifits";

      n++;
      final OIFitsFile srcOIFitsFile = load(fileSrc + ext);
      if (srcOIFitsFile == null) {
        errors++;
      } else {
        final String fileTo = fileSrc + "-copy.oifits";

        errors += write(fileTo, srcOIFitsFile);

        // verify and check :
        final OIFitsFile destOIFitsFile = load(fileTo);
        if (destOIFitsFile == null) {
          errors++;
        } else if (!OITableUtils.compareOIFitsFile(srcOIFitsFile, destOIFitsFile)) {
          errors++;
        }

        // compare fits files at fits level (header / data) :
        if (COMPARE_FITS && !TamFitsTest.compareFile(fileSrc + ext, fileTo)) {
          errors++;
        }
      }
    }

    if (false) {
      final String file = COPY_DIR + "test-create.oifits";
      // create an oifits file :
      create(file);
    }

    if (true) {
      final File directory = new File(TEST_DIR);
      if (directory.exists() && directory.isDirectory()) {

        final long start = System.nanoTime();

        final File[] files = directory.listFiles();

        for (File f : files) {
          if (f.isFile() && (f.getName().endsWith("fits") || f.getName().endsWith("fits.gz"))) {
            n++;

            final String fileSrc = f.getAbsolutePath();
            final String fileTo = COPY_DIR + f.getName().replaceFirst("\\.", "-copy.").replaceFirst("\\.gz", "");

            final OIFitsFile srcOIFitsFile = load(fileSrc);
            if (srcOIFitsFile == null) {
              errors++;
            } else {

              errors += write(fileTo, srcOIFitsFile);

              // verify and check :
              if (true) {
                final OIFitsFile destOIFitsFile = load(fileTo);
                if (destOIFitsFile == null) {
                  errors++;
                } else if (!OITableUtils.compareOIFitsFile(srcOIFitsFile, destOIFitsFile)) {
                  errors++;
                }
              }
            }
          }
        }

        logger.info("copyDirectory : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");
      }
    }

    logger.info("Errors = " + errors + " on " + n + " files.");
  }

  private static OIFitsFile load(final String absFilePath) {
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

  private static int write(final String absFilePath, final OIFitsFile oiFitsFile) {
    int error = 0;
    try {
      logger.info("Writing file : " + absFilePath);

      final long start = System.nanoTime();

      OIFitsWriter.writeOIFits(absFilePath, oiFitsFile);

      logger.info("write : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

    } catch (Throwable th) {
      logger.log(Level.SEVERE, "write : IO failure occured while writing file : " + absFilePath, th);
      error = 1;
    }
    return error;
  }

  private static int create(final String absFilePath) {
    int error = 0;
    try {
      logger.info("Creating file : " + absFilePath);

      final long start = System.nanoTime();

      final OIFitsFile oiFitsFile = new OIFitsFile();

      fill(oiFitsFile);

      if (false) {
        logger.info("create : XML DESC : \n" + XmlOutputVisitor.getXmlDesc(oiFitsFile, false));
      }

      final OIFitsChecker checker = new OIFitsChecker();
      oiFitsFile.check(checker);

      // validation results
      logger.info("create : validation results\n" + checker.getCheckReport());

      OIFitsWriter.writeOIFits(absFilePath, oiFitsFile);

      logger.info("create : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

    } catch (Throwable th) {
      logger.log(Level.SEVERE, "create : IO failure occured while writing file : " + absFilePath, th);
      error = 1;
    }
    return error;
  }

  private static void fill(final OIFitsFile oiFitsFile) {

    final String arrName = "VLTI-like";
    final String insName = "AMBER-like";

    final short targetId = 1;
    final short sta1Id = 1;
    final short sta2Id = 2;
    final short sta3Id = 3;

    // OI_ARRAY :
    final OIArray array = new OIArray(oiFitsFile, 3);
    array.setArrName(arrName);
    array.setFrame(OIFitsConstants.KEYWORD_FRAME_GEOCENTRIC);
    array.setArrayXYZ(new double[]{1942042.8584924d, -5455305.996911d, -2654521.4011759d});

    array.getTelName()[0] = "UT1";
    array.getStaName()[0] = "U1";
    array.getStaIndex()[0] = sta1Id;
    array.getDiameter()[0] = 8f;
    array.getStaXYZ()[0] = new double[]{-0.73422599479242d, -9.92488562146125d, -22.03283353519204d};

    array.getTelName()[1] = "UT2";
    array.getStaName()[1] = "U2";
    array.getStaIndex()[1] = sta2Id;
    array.getDiameter()[1] = 8f;
    array.getStaXYZ()[1] = new double[]{20.45018397209875d, 14.88732843219187d, 24.17944630588896d};

    array.getTelName()[2] = "UT3";
    array.getStaName()[2] = "U3";
    array.getStaIndex()[2] = sta3Id;
    array.getDiameter()[2] = 8f;
    array.getStaXYZ()[2] = new double[]{35.32766648520568d, 44.91458329169021d, 56.61105628712381d};

    oiFitsFile.addOiTable(array);

    // OI_WAVELENGTH :
    final int nWave = 512;
    final OIWavelength waves = new OIWavelength(oiFitsFile, nWave);
    waves.setInsName(insName);

    final float wMin = 1.54E-6f;
    final float wMax = 1.82E-6f;
    final float step = (wMax - wMin) / (nWave - 1);

    final float[] effWave = waves.getEffWave();
    final float[] effBand = waves.getEffBand();

    float waveLength = wMin;
    for (int i = 0; i < nWave; i++) {
      effWave[i] = waveLength;
      effBand[i] = 5.48E-10f;

      waveLength += step;
    }

    oiFitsFile.addOiTable(waves);

    // OI_TARGET :
    final OITarget target = new OITarget(oiFitsFile, 1);
    target.getTargetId()[0] = targetId;
    target.getTarget()[0] = "V*zet And";

    target.getRaEp0()[0] = 11.77854d;
    target.getDecEp0()[0] = 24.268334d;
    target.getEquinox()[0] = 2000f;

    target.getRaErr()[0] = 1e-4d;
    target.getDecErr()[0] = 1e-4d;

    target.getSysVel()[0] = 120d;
    target.getVelTyp()[0] = OIFitsConstants.COLUMN_VELTYP_LSR;
    target.getVelDef()[0] = OIFitsConstants.COLUMN_VELDEF_OPTICAL;

    target.getPmRa()[0] = -2.8119e-5d;
    target.getPmDec()[0] = -2.2747e-5d;

    target.getPmRaErr()[0] = 2e-7d;
    target.getPmDecErr()[0] = 3e-7d;

    target.getParallax()[0] = 0.004999f;
    target.getParaErr()[0] = 1e-5f;

    target.getSpecTyp()[0] = "AOV";

    oiFitsFile.addOiTable(target);

    // OI_VIS :
    final int nRows = 7;
    short[] targetIds;
    short[][] staIndexes;

    final OIVis vis = new OIVis(oiFitsFile, insName, nRows);
    vis.setArrName(arrName);
    vis.setDateObs("2010-06-18");

    targetIds = vis.getTargetId();
    staIndexes = vis.getStaIndex();

    for (int i = 0; i < nRows; i++) {
      targetIds[i] = targetId;
      staIndexes[i][0] = sta1Id;
      staIndexes[i][1] = sta2Id;
    }

    oiFitsFile.addOiTable(vis);

    // OI_VIS2 :
    final OIVis2 vis2 = new OIVis2(oiFitsFile, insName, nRows);
    vis2.setArrName(arrName);
    vis2.setDateObs("2010-06-18");

    targetIds = vis2.getTargetId();
    staIndexes = vis2.getStaIndex();

    for (int i = 0; i < nRows; i++) {
      targetIds[i] = targetId;
      staIndexes[i][0] = sta1Id;
      staIndexes[i][1] = sta2Id;
    }

    oiFitsFile.addOiTable(vis2);

    // OI_T3 :
    final OIT3 t3 = new OIT3(oiFitsFile, insName, nRows);
    t3.setArrName(arrName);
    t3.setDateObs("2010-06-18");

    targetIds = t3.getTargetId();
    staIndexes = t3.getStaIndex();

    for (int i = 0; i < nRows; i++) {
      targetIds[i] = targetId;
      staIndexes[i][0] = sta1Id;
      staIndexes[i][1] = sta2Id;
      staIndexes[i][2] = sta3Id;
    }

    oiFitsFile.addOiTable(t3);
  }
}
