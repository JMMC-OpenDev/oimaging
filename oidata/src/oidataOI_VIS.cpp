/*******************************************************************************
 * JMMC project 
 * 
 * "@(#) $Id: oidataOI_VIS.cpp,v 1.7 2006-06-16 12:48:17 scetre Exp $"
 * 
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.6  2006/05/11 13:04:56  mella
 * Changed rcsId declaration to perform good gcc4 and gcc3 compilation
 *
 * Revision 1.5  2005/09/27 11:52:18  mella
 * Improve structure for description of keywords and columns
 *
 * Revision 1.4  2005/09/23 09:32:35  mella
 * Add unit check for columns
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
 * Definition of oidataOI_VIS class. 
 */

static char *rcsId __attribute__ ((unused)) ="@(#) $Id: oidataOI_VIS.cpp,v 1.7 2006-06-16 12:48:17 scetre Exp $";
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

#include "oidataOI_VIS.h"

#include "oidataPrivate.h"


/** 
 * Class constructor 
 *
 * @param fitsParent reference onto the parent FITS object.
 * @param hduIndex table index into the fits file.
 */
oidataOI_VIS::oidataOI_VIS(oidataFITS * fitsParent, mcsSTRING32 extName, mcsINT16 hduIndex):oidataDATA_TABLE(fitsParent, extName,
                 hduIndex)
{
    logTrace("oidataOI_VIS::oidataOI_VIS()");
    _tableType = OI_VIS_TYPE;
    _nbOfStations = 2;
}

/** 
 * Class destructor 
 */
oidataOI_VIS::~oidataOI_VIS()
{
    logTrace("oidataOI_VIS::~oidataOI_VIS()");
}

/*
 * public method
 */

/**
 * Get visibility amplitude
 * 
 * @param visAmp Visibility amplitude
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_VIS::GetVisAmp(mcsDOUBLE * visAmp)
{
    logTrace("oidataOI_VIS::GetVisAmp()");
    return ReadColumn("VISAMP", _naxis2 * _nwave, 1, visAmp);
}


/**
 * 
 * Get Error in visibility amplitude
 * 
 * @param visAmpErr Error in visibility amplitude
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_VIS::GetVisAmpErr(mcsDOUBLE * visAmpErr)
{
    logTrace("oidataOI_VIS::GetVisAmpErr()");
    return ReadColumn("VISAMPERR", _naxis2 * _nwave, 1, visAmpErr);
}


/**
 * 
 * 
 * @param visPhi Visibility phase in degrees
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_VIS::GetVisPhi(mcsDOUBLE * visPhi)
{
    logTrace("oidataOI_VIS::GetVisPhi()");
    return ReadColumn("VISPHI", _naxis2 * _nwave, 1, visPhi);
}


/**
 * 
 * Get Error in visibility phase in degrees
 * 
 * @param visPhiErr Error in visibility phase in degrees
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_VIS::GetVisPhiErr(mcsDOUBLE * visPhiErr)
{
    logTrace("oidataOI_VIS::GetVisPhiErr()");
    return ReadColumn("VISPHIERR", _naxis2 * _nwave, 1, visPhiErr);
}


/**
 * 
 * Get U coordinate of the data (meters)
 * 
 * @param uCoord U coordinate of the data (meters)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_VIS::GetUCoord(mcsDOUBLE * uCoord)
{
    logTrace("oidataOI_VIS::GetUCoord()");
    return ReadColumn("UCOORD", _naxis2, 1, uCoord);
}


/**
 * 
 * Get V coordinate of the data (meters)
 * 
 * @param vCoord V coordinate of the data (meters)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_VIS::GetVCoord(mcsDOUBLE * vCoord)
{
    logTrace("oidataOI_VIS::GetVCoord()");
    return ReadColumn("VCOORD", _naxis2, 1, vCoord);
}

mcsCOMPL_STAT oidataOI_VIS::CheckStructure()
{
    logTrace("oidataOI_VIS::CheckStructure()");

    oidataKeywordDefinition oidataOiVisKeywords[] = {
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

    oidataColumnDefinition oidataOiVisColumns[] = {
        {"TARGET_ID", typeInteger, 1, noUnit,
         "target number as index into OI_TARGET table"}
        ,
        {"TIME", typeDouble, 1, unitInSeconds, "UTC time of observation"}
        ,
        {"MJD", typeDouble, 1, unitInMJD, "modified Julian Day"}
        ,
        {"INT_TIME", typeDouble, 1, unitInSeconds, "integration time"}
        ,
        {"VISAMP", typeDouble, -1, noUnit, "visibility amplitude"}
        ,
        {"VISAMPERR", typeDouble, -1, noUnit, "error in visibility amplitude"}
        ,
        {"VISPHI", typeDouble, -1, unitInDegrees, "visibility phase"}
        ,
        {"VISPHIERR", typeDouble, -1, unitInDegrees,
         "error in visibility phase"}
        ,
        {"UCOORD", typeDouble, 1, unitInMeters, "U coordinate of the data"}
        ,
        {"VCOORD", typeDouble, 1, unitInMeters, "V coordinate of the data"}
        ,
        {"STA_INDEX", typeInteger, 2, noUnit,
         "station numbers contributing to the data"}
        ,
        {"FLAG", typeLogical, -1, noUnit, "flag"}
        ,
        // Do not remove the next line 
        {NULL, typeInteger, 0, noUnit, NULL}
    };

    // Check keywords
    if(CheckKeywords(oidataOiVisKeywords) == mcsFAILURE)
    {
        return mcsFAILURE;
    }

    // Check columns
    if(CheckColumns(oidataOiVisColumns) == mcsFAILURE)
    {
        return mcsFAILURE;
    }

    return mcsSUCCESS;
}

/*___oOo___*/
