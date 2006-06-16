/*******************************************************************************
 * JMMC project 
 * 
 * "@(#) $Id: oidataOI_TARGET.cpp,v 1.8 2006-06-16 12:48:17 scetre Exp $"
 * 
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.7  2006/05/11 13:04:56  mella
 * Changed rcsId declaration to perform good gcc4 and gcc3 compilation
 *
 * Revision 1.6  2005/09/27 11:52:18  mella
 * Improve structure for description of keywords and columns
 *
 * Revision 1.5  2005/09/23 09:32:35  mella
 * Add unit check for columns
 *
 * Revision 1.4  2005/09/19 13:58:08  mella
 * Every colums, and keyword presence are checked
 *
 * Revision 1.3  2005/09/15 09:26:12  mella
 * Add extname to protected member and to constructor parameter list
 *
 * Revision 1.2  2005/09/14 15:29:56  mella
 * Add some tests and do not work directly with cfitsio access during Load
 *
 * Revision 1.1  2005/06/24 14:07:10  mella
 * First revision
 *
 *******************************************************************************/
/** 
 * \file
 * Definition of oidataOI_TARGET class. 
 */

static char *rcsId __attribute__ ((unused)) ="@(#) $Id: oidataOI_TARGET.cpp,v 1.8 2006-06-16 12:48:17 scetre Exp $";
/* 
 * System Headers
 */
#include <iostream>
using namespace std;

/* 
 * MCS Headers 
 */

#include "mcs.h"
#include "log.h"
#include "err.h"
/* 
 * Local Header 
 */

#include "oidataOI_TARGET.h"

#include "oidataPrivate.h"

/** 
 * Class constructor 
 */
oidataOI_TARGET::oidataOI_TARGET(oidataFITS * fitsParent, mcsSTRING32 extName, mcsINT16 hduIndex):oidataOI_TABLE(fitsParent, extName,
               hduIndex)
{
    logTrace("oidataOI_TARGET::oidataOI_TARGET()");
    _tableType = OI_TARGET_TYPE;
}

/** 
 * Class destructor 
 */
oidataOI_TARGET::~oidataOI_TARGET()
{
    logTrace("oidataOI_TARGET::~oidataOI_TARGET()");
}

/*
 * public method
 */


mcsCOMPL_STAT oidataOI_TARGET::CheckStructure()
{
    logTrace("oidataOI_TARGET::CheckStructure()");

    // OI_REVN keyword was previously checked

    oidataColumnDefinition oidataOiTargetColumns[] = {
        {"TARGET_ID", typeInteger, 1, noUnit, "index number"},
        {"TARGET", typeCharacter, 16, noUnit, "target name"},
        {"RAEP0", typeDouble, 1, unitInDegrees, "RA at mean equinox"},
        {"DECEP0", typeDouble, 1, unitInDegrees, "DEC at mean equinox"},
        {"EQUINOX", typeFloat, 1, unitInYears, "equinox"},
        {"RA_ERR", typeDouble, 1, unitInDegrees,
         "error in RA at mean equinox"},
        {"DEC_ERR", typeDouble, 1, unitInDegrees,
         "error in DEC at mean equinox"},
        {"SYSVEL", typeDouble, 1, unitInMetersPerSecond,
         "systemic radial velocity"},
        {"VELTYP", typeCharacter, 8, noUnit, "reference for radial velocity"},
        {"VELDEF", typeCharacter, 8, noUnit, "definition of radial velocity"},
        {"PMRA", typeDouble, 1, unitInDegreesPerYear, "proper motion in RA"},
        {"PMDEC", typeDouble, 1, unitInDegreesPerYear,
         "proper motion in DEC"},
        {"PMRA_ERR", typeDouble, 1, unitInDegreesPerYear,
         "error of proper motion in RA"},
        {"PMDEC_ERR", typeDouble, 1, unitInDegreesPerYear,
         "error of proper motion in DEC"},
        {"PARALLAX", typeFloat, 1, unitInDegrees, "parallax"},
        {"PARA_ERR", typeFloat, 1, unitInDegrees, "error in parallax"},
        {"SPECTYP", typeCharacter, 16, noUnit, "spectral type"},
        // Do not remove the next line 
        {NULL, typeInteger, 0, noUnit, NULL}
    };

    // Check columns
    if(CheckColumns(oidataOiTargetColumns) == mcsFAILURE)
    {
        return mcsFAILURE;
    }

    return mcsSUCCESS;
}

/**
 * 
 * Get Index number
 * 
 * @param targetId Index number
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TARGET::GetTargetId(mcsINT16 * targetId)
{
    logTrace("oidataOI_TARGET::GetTargetId()");
    return ReadColumn("TARGET_ID", _naxis2, 1, targetId);
}


/**
 * 
 * Get Target name
 * 
 * @param target Target name
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TARGET::GetTarget(mcsSTRING16 * target)
{
    logTrace("oidataOI_TARGET::GetTarget()");
    return ReadColumn("TARGET", _naxis2, 1, (char **) target);
}


/**
 * 
 * Get RA at mean equinox (degrees)
 * 
 * @param raEp0 RA at mean equinox (degrees)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TARGET::GetRaEp0(mcsDOUBLE * raEp0)
{
    logTrace("oidataOI_TARGET::GetRaEp0()");
    return ReadColumn("RAEP0", _naxis2, 1, raEp0);
}


/**
 * 
 * Get DEC at mean equinox (degrees)
 * 
 * @param decEp0 DEC at mean equinox (degrees)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TARGET::GetDecEp0(mcsDOUBLE * decEp0)
{
    logTrace("oidataOI_TARGET::GetDecEp0()");
    return ReadColumn("DECEP0", _naxis2, 1, decEp0);
}


/**
 * 
 * Get Equinox
 * 
 * @param equinox Equinox
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TARGET::GetEquinox(mcsFLOAT * equinox)
{
    logTrace("oidataOI_TARGET::GetEquinox()");
    return ReadColumn("EQUINOX", _naxis2, 1, equinox);
}


/**
 * 
 * Get Error in RA at mean equinox (degrees)
 * 
 * @param raErr Error in RA at mean equinox (degrees)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TARGET::GetRaErr(mcsDOUBLE * raErr)
{
    logTrace("oidataOI_TARGET::GetRaErr()");
    return ReadColumn("RA_ERR", _naxis2, 1, raErr);
}


/**
 * 
 * Get Error in DEC at mean equinox (degrees)
 * 
 * @param decErr Error in DEC at mean equinox (degrees)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TARGET::GetDecErr(mcsDOUBLE * decErr)
{
    logTrace("oidataOI_TARGET::GetDecErr()");
    return ReadColumn("DEC_ERR", _naxis2, 1, decErr);
}


/**
 * 
 * Get Systemic radial velocity (meters per second)
 * 
 * @param sysVel Systemic radial velocity (meters per second)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TARGET::GetSysVel(mcsDOUBLE * sysVel)
{
    logTrace("oidataOI_TARGET::GetSysVel()");
    return ReadColumn("SYSVEL", _naxis2, 1, sysVel);
}


/**
 * 
 * Get Reference for radial velocity (LSR, GEOCENTR, etc)
 * 
 * @param velTyp Reference for radial velocity (LSR, GEOCENTR, etc)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TARGET::GetVelTyp(mcsSTRING8 * velTyp)
{
    logTrace("oidataOI_TARGET::GetVelTyp()");
    return ReadColumn("VELTYP", _naxis2, 1, (char **) velTyp);
}


/**
 * 
 * Get Definition of radial velocity(OPTICAL, RADIO)
 * 
 * @param velDef Definition of radial velocity(OPTICAL, RADIO)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TARGET::GetVelDef(mcsSTRING8 * velDef)
{
    logTrace("oidataOI_TARGET::GetVelDef()");
    return ReadColumn("VELDEF", _naxis2, 1, (char **) velDef);
}


/**
 * 
 * Get Proper motion in RA (degrees per year)
 * 
 * @param pmRa Proper motion in RA (degrees per year)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TARGET::GetPMRa(mcsDOUBLE * pmRa)
{
    logTrace("oidataOI_TARGET::GetPMRa()");
    return ReadColumn("PMRA", _naxis2, 1, pmRa);
}


/**
 * 
 * Get Proper motion in DEC (degrees per year)
 * 
 * @param pmDec Proper motion in DEC (degrees per year)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TARGET::GetPMDec(mcsDOUBLE * pmDec)
{
    logTrace("oidataOI_TARGET::GetPMDec()");
    return ReadColumn("PMDEC", _naxis2, 1, pmDec);
}


/**
 * 
 * Get Error of proper motion in RA (degrees per year)
 * 
 * @param pmRaErr Error of proper motion in RA (degrees per year)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TARGET::GetPMraErr(mcsDOUBLE * pmRaErr)
{
    logTrace("oidataOI_TARGET::GetPMraErr()");
    return ReadColumn("PMRA_ERR", _naxis2, 1, pmRaErr);
}


/**
 * 
 * Get Error of proper motion in DEC (degrees per year)
 * 
 * @param pmdecErr Error of proper motion in DEC (degrees per year)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TARGET::GetPMDecErr(mcsDOUBLE * pmdecErr)
{
    logTrace("oidataOI_TARGET::GetPMDecErr()");
    return ReadColumn("PMDEC_ERR", _naxis2, 1, pmdecErr);
}


/**
 * 
 * Get Parallax (degrees)
 * 
 * @param parallax Parallax (degrees)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TARGET::GetParallax(mcsFLOAT * parallax)
{
    logTrace("oidataOI_TARGET::GetParallax()");
    return ReadColumn("PARALLAX", _naxis2, 1, parallax);
}


/**
 * 
 * Get Error in parallax (degrees)
 * 
 * @param paraErr Error in parallax (degrees)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TARGET::GetParaErr(mcsFLOAT * paraErr)
{
    logTrace("oidataOI_TARGET::GetParaErr()");
    return ReadColumn("PARA_ERR", _naxis2, 1, paraErr);
}


/**
 * 
 * Get Spectral type
 * 
 * @param specTyp Spectral type
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TARGET::GetSpecTyp(mcsSTRING16 * specTyp)
{
    logTrace("oidataOI_TARGET::GetSpecTyp()");
    return ReadColumn("SPECTYP", _naxis2, 1, (char **) specTyp);
}




/*___oOo___*/
