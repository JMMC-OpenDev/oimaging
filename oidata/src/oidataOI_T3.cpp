/*******************************************************************************
 * JMMC project 
 * 
 * "@(#) $Id: oidataOI_T3.cpp,v 1.9 2006-06-16 12:48:17 scetre Exp $"
 * 
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.8  2006/05/11 13:04:56  mella
 * Changed rcsId declaration to perform good gcc4 and gcc3 compilation
 *
 * Revision 1.7  2006/02/01 15:12:15  mella
 * Completed documentation
 *
 * Revision 1.6  2005/09/27 11:52:18  mella
 * Improve structure for description of keywords and columns
 *
 * Revision 1.5  2005/09/23 09:32:35  mella
 * Add unit check for columns
 *
 * Revision 1.4  2005/09/20 12:27:45  mella
 * Replace bad columns description
 *
 * Revision 1.3  2005/09/19 13:58:08  mella
 * Every colums, and keyword presence are checked
 *
 * Revision 1.2  2005/09/15 09:26:12  mella
 * Add extname to protected member and to constructor parameter list
 *
 * Revision 1.1  2005/06/24 14:07:10  mella
 * First revision
 *
 *******************************************************************************/
/** 
 * \file
 * Definition of oidataOI_T3 class. 
 */

static char *rcsId __attribute__ ((unused)) ="@(#) $Id: oidataOI_T3.cpp,v 1.9 2006-06-16 12:48:17 scetre Exp $";
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

#include "oidataOI_T3.h"

#include "oidataPrivate.h"


/** 
 * Class constructor 
 *
 * @param fitsParent reference onto the parent FITS object.
 * @param hduIndex table index into the fits file.
 */
oidataOI_T3::oidataOI_T3(oidataFITS * fitsParent, mcsSTRING32 extName, mcsINT16 hduIndex):oidataDATA_TABLE(fitsParent, extName,
                 hduIndex)
{
    logTrace("oidataOI_T3::oidataOI_T3()");
    _tableType = OI_T3_TYPE;
    _nbOfStations = 3;
}

/** 
 * Class destructor 
 */
oidataOI_T3::~oidataOI_T3()
{
    logTrace("oidataOI_T3::~oidataOI_T3()");
}

/*
 * public method
 */

/**
 *  Get triple product amplitude.
 * 
 * @param t3Amp Triple product amplitude
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_T3::GetT3Amp(mcsDOUBLE * t3Amp)
{
    logTrace("oidataOI_T3::getT3Amp()");
    return ReadColumn("T3AMP", _naxis2 * _nwave, 1, t3Amp);
}


/**
 * Get Error in Triple product amplitude.
 * 
 * @param t3AmpErr Error in Triple product amplitude
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_T3::GetT3AmpErr(mcsDOUBLE * t3AmpErr)
{
    logTrace("oidataOI_T3::GetT3AmpErr()");
    return ReadColumn("T3AMPERR", _naxis2 * _nwave, 1, t3AmpErr);
}


/**
 * Get Triple product phase (degrees).
 * 
 * @param t3Phi Triple product phase (degrees).
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_T3::GetT3Phi(mcsDOUBLE * t3Phi)
{
    logTrace("oidataOI_T3::GetT3Phi()");
    return ReadColumn("T3PHI", _naxis2 * _nwave, 1, t3Phi);
}


/**
 * Get Error in Triple product phase (degrees).
 * 
 * @param t3PhiErr Error in Triple product phase (degrees).
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_T3::GetT3PhiErr(mcsDOUBLE * t3PhiErr)
{
    logTrace("oidataOI_T3::GetT3PhiErr()");
    return ReadColumn("T3PHIERR", _naxis2 * _nwave, 1, t3PhiErr);
}


/**
 * Get U coordinate of baseline AB of the triangle (meters).
 * 
 * @param u1Coord U coordinate of baseline AB of the triangle (meters).
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_T3::GetU1Coord(mcsDOUBLE * u1Coord)
{
    logTrace("oidataOI_T3::GetU1Coord()");
    return ReadColumn("U1COORD", _naxis2, 1, u1Coord);
}


/**
 * Get V coordinate of baseline AB of the triangle (meters).
 * 
 * @param v1Coord V coordinate of baseline AB of the triangle (meters).
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_T3::GetV1Coord(mcsDOUBLE * v1Coord)
{
    logTrace("oidataOI_T3::GetV1Coord()");
    return ReadColumn("V1COORD", _naxis2, 1, v1Coord);
}


/**
 * Get U coordinate of baseline BC of the triangle (meters).
 * 
 * @param u2Coord U coordinate of baseline BC of the triangle (meters).
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_T3::GetU2Coord(mcsDOUBLE * u2Coord)
{
    logTrace("oidataOI_T3::GetU2Coord()");
    return ReadColumn("U2COORD", _naxis2, 1, u2Coord);
}


/**
 * Get V coordinate of baseline BC of the triangle (meters).
 * 
 * @param v2Coord V coordinate of baseline BC of the triangle (meters).
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_T3::GetV2Coord(mcsDOUBLE * v2Coord)
{
    logTrace("oidataOI_T3::GetV2Coord()");
    return ReadColumn("V2COORD", _naxis2, 1, v2Coord);
}


mcsCOMPL_STAT oidataOI_T3::CheckStructure()
{
    logTrace("oidataOI_T3::CheckStructure()");

    oidataKeywordDefinition oidataOiT3Keywords[] = {
        {"OI_REVN", typeInteger, 1, noUnit,
         "revision number of the table definition"}
        ,
        {"DATE-OBS", typeCharacter, 1, noUnit,
         "UTC start date of observations"}
        ,
        {"ARRNAME", typeCharacter, 0, noUnit, "name of corresponding array"}
        ,
        {"INSNAME", typeCharacter, 1, noUnit, "name of correspondingdetector"}
        ,
        {"NAXIS2", typeInteger, 1, noUnit, "number of spectral channels"}
        ,
        // Do not remove the next line 
        {NULL, typeInteger, 0, noUnit, NULL}
    };

    oidataColumnDefinition oidataOiT3Columns[] = {
        {"TARGET_ID", typeInteger, 1, noUnit,
         "target number as index into OI_TARGET table"}
        ,
        {"TIME", typeDouble, 1, unitInSeconds, "UTC time of observation"}
        ,
        {"MJD", typeDouble, 1, unitInMJD, "modified Julian Day"}
        ,
        {"INT_TIME", typeDouble, 1, unitInSeconds, "integration time"}
        ,
        {"T3AMP", typeDouble, -1, noUnit, "triple product amplitude"}
        ,
        {"T3AMPERR", typeDouble, -1, noUnit,
         "error in triple product amplitude"}
        ,
        {"T3PHI", typeDouble, -1, unitInDegrees, "triple product phase"}
        ,
        {"T3PHIERR", typeDouble, -1, unitInDegrees,
         "error in triple product phase"}
        ,
        {"U1COORD", typeDouble, 1, unitInMeters,
         "U coordinate of baseline AB of the triangle"}
        ,
        {"V1COORD", typeDouble, 1, unitInMeters,
         "V coordinate of baseline AB of the triangle"}
        ,
        {"U2COORD", typeDouble, 1, unitInMeters,
         "U coordinate of baseline BC of the triangle"}
        ,
        {"V2COORD", typeDouble, 1, unitInMeters,
         "V coordinate of baseline BC of the triangle"}
        ,
        {"STA_INDEX", typeInteger, 3, noUnit,
         "station numbers contributing to the data"}
        ,
        {"FLAG", typeLogical, -1, noUnit, "flag"}
        ,
        // Do not remove the next line 
        {NULL, typeInteger, 0, noUnit, NULL}
    };

    // Check keywords
    if(CheckKeywords(oidataOiT3Keywords) == mcsFAILURE)
    {
        return mcsFAILURE;
    }

    // Check columns
    if(CheckColumns(oidataOiT3Columns) == mcsFAILURE)
    {
        return mcsFAILURE;
    }

    return mcsSUCCESS;
}


/*___oOo___*/
