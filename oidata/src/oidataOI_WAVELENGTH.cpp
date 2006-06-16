/*******************************************************************************
 * JMMC project 
 * 
 * "@(#) $Id: oidataOI_WAVELENGTH.cpp,v 1.7 2006-06-16 12:48:17 scetre Exp $"
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
 * Definition of oidataOI_WAVELENGTH class. 
 */

static char *rcsId __attribute__ ((unused)) ="@(#) $Id: oidataOI_WAVELENGTH.cpp,v 1.7 2006-06-16 12:48:17 scetre Exp $";
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

#include "oidataOI_WAVELENGTH.h"

#include "oidataPrivate.h"


/** 
 * Class constructor 
 *
 * @param fitsParent reference onto the parent FITS object.
 * @param hduIndex table index into the fits file.
  */
oidataOI_WAVELENGTH::oidataOI_WAVELENGTH(oidataFITS * fitsParent, mcsSTRING32 extName, mcsINT16 hduIndex):oidataOI_TABLE(fitsParent, extName,
               hduIndex)
{
    logTrace("oidataOI_WAVELENGTH::oidataOI_WAVELENGTH()");
    _tableType = OI_WAVELENGTH_TYPE;
}

/** 
 * Class destructor 
 */
oidataOI_WAVELENGTH::~oidataOI_WAVELENGTH()
{
    logTrace("oidataOI_WAVELENGTH::~oidataOI_WAVELENGTH()");
}

/*
 * public method
 */
mcsCOMPL_STAT oidataOI_WAVELENGTH::CheckStructure()
{
    logTrace("oidataOI_WAVELENGTH::CheckStructure()");

    oidataKeywordDefinition oidataOiWavelengthKeywords[] = {
        {"OI_REVN", typeInteger, 1, noUnit,
         "revision number of the table definition"},
        {"INSNAME", typeCharacter, 1, noUnit,
         "name of detector for cross-referencing"},
        {"NAXIS2", typeInteger, 1, noUnit, "number of spectral channels"},
        // Do not remove the next line 
        {NULL, typeInteger, 0, noUnit, NULL}
    };

    oidataColumnDefinition oidataOiWavelengthColumns[] = {
        {"EFF_WAVE", typeFloat, 1, unitInMeters,
         "effective wavelength of channel"},
        {"EFF_BAND", typeFloat, 1, unitInMeters,
         "effective bandpass of channel"},
        // Do not remove the next line 
        {NULL, typeInteger, 0, noUnit, NULL}
    };

    // Check keywords
    if(CheckKeywords(oidataOiWavelengthKeywords) == mcsFAILURE)
    {
        return mcsFAILURE;
    }

    // Check columns
    if(CheckColumns(oidataOiWavelengthColumns) == mcsFAILURE)
    {
        return mcsFAILURE;
    }

    return mcsSUCCESS;
}

/**
 * Get insname Identifies corresponding OI_WAVELENGTH table
 * 
 * @param insname Identifies corresponding OI_WAVELENGTH table
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_WAVELENGTH::GetInsName(mcsSTRING32 insname)
{
    logTrace("oidataOI_WAVELENGTH::GetInsName()");
    return GetKeyword("INSNAME", insname);
}


/**
 * 
 * Get Effective wavelength of channel (meters)
 * 
 * @param effWave Effective wavelength of channel (meters)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_WAVELENGTH::GetEffWave(mcsFLOAT * effWave)
{
    logTrace("oidataOI_WAVELENGTH::GetEffWave()");
    return ReadColumn("EFF_WAVE", _naxis2, 1, effWave);
}


/**
 * 
 * Get Effective bandpass of channel (meters)
 * 
 * @param effBand Effective bandpass of channel (meters)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_WAVELENGTH::GetEffBand(mcsFLOAT * effBand)
{
    logTrace("oidataOI_WAVELENGTH::GetEffBand()");
    return ReadColumn("EFF_BAND", _naxis2, 1, effBand);
}




/*___oOo___*/
