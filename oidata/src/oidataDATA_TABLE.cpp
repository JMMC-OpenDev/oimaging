/*******************************************************************************
 * JMMC project 
 * 
 * "@(#) $Id: oidataDATA_TABLE.cpp,v 1.10 2006-06-16 12:48:17 scetre Exp $"
 * 
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.9  2006/05/11 13:04:56  mella
 * Changed rcsId declaration to perform good gcc4 and gcc3 compilation
 *
 * Revision 1.8  2005/09/27 14:16:47  mella
 * Use oidataDataType instead of cfitsio typecode
 *
 * Revision 1.7  2005/09/27 12:00:10  mella
 * Do not reject during loadind checks 0 dimensions
 *
 * Revision 1.6  2005/09/27 11:52:18  mella
 * Improve structure for description of keywords and columns
 *
 * Revision 1.5  2005/09/26 12:59:52  mella
 * Add CheckColumns specialisation according nwave check
 *
 * Revision 1.4  2005/09/19 13:58:08  mella
 * Every colums, and keyword presence are checked
 *
 * Revision 1.3  2005/09/15 09:26:11  mella
 * Add extname to protected member and to constructor parameter list
 *
 * Revision 1.2  2005/06/28 13:27:03  mella
 * imporve doc
 *
 * Revision 1.1  2005/06/24 14:07:10  mella
 * First revision
 *
 *******************************************************************************/
/** 
 * \file
 * Definition of oidataDATA_TABLE class. 
 */

static char *rcsId __attribute__ ((unused)) ="@(#) $Id: oidataDATA_TABLE.cpp,v 1.10 2006-06-16 12:48:17 scetre Exp $";
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

#include "oidataErrors.h"
#include "oidataDATA_TABLE.h"
#include "oidataOI_WAVELENGTH.h"
#include "oidataOI_ARRAY.h"

#include "oidataPrivate.h"


/** 
 * Class constructor 
 *
 * @param fitsParent reference onto the parent FITS object.
 * @param hduIndex table index into the fits file.
 */
oidataDATA_TABLE::oidataDATA_TABLE(oidataFITS * fitsParent, mcsSTRING32 extName, mcsINT16 hduIndex):oidataOI_TABLE(fitsParent, extName,
               hduIndex)
{
    logTrace("oidataDATA_TABLE::oidataDATA_TABLE()");

    /* Init default value of members */
    _oiArray = NULL;
    _oiWavelength = NULL;
    _nwave = 0;
    _nbOfStations = 0;
}

/** 
 * Class destructor 
 */
oidataDATA_TABLE::~oidataDATA_TABLE()
{
    logTrace("oidataDATA_TABLE::~oidataDATA_TABLE()");
}

/*
 * public method
 */

/**
 * 
 * Get the corresponding OI_WAVELENGTH table.
 * 
 * @param table corresponding OI_WAVELENGTH.
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT
    oidataDATA_TABLE::GetOiWavelengthTable(oidataOI_WAVELENGTH ** table)
{
    logTrace("oidataDATA_TABLE::GetOiWavelengthTable()");
    if(_oiWavelength == NULL)
    {
        errAdd(oidataERR_NO_REFERENCE, "oiwavelength table ");
        return mcsFAILURE;
    }
    *table = _oiWavelength;
    return mcsSUCCESS;
}

/**
 * 
 * Get the corresponding OI_ARRAY table.
 * 
 * @param table corresponding OI_ARRAY.
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataDATA_TABLE::GetOiArrayTable(oidataOI_ARRAY ** table)
{
    logTrace("oidataDATA_TABLE::GetOiArrayTable()");
    if(_oiArray == NULL)
    {
        errAdd(oidataERR_NO_REFERENCE, "oiarray table ");
        return mcsFAILURE;
    }
    *table = _oiArray;

    return mcsSUCCESS;
}

/**
 * 
 * Get Number of distinct spectral channels of the relevant OI_WAVELENGTH
 * 
 * @return the number of distinct spectral channels 
 */
mcsINT16 oidataDATA_TABLE::GetNWave()
{
    logTrace("oidataDATA_TABLE::GetNWave()");
    return _nwave;
}

/**
 * 
 * One table reference is proposed to make crossReference possible.
 * 
 * @param table one table of the Fits ones.
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataDATA_TABLE::ProposeReference(oidataOI_TABLE * table)
{
    logTrace("oidataDATA_TABLE::ProposeReference()");
    int tableType;
    tableType = table->GetType();
    // Search for reference only if they still are NULL
    if((tableType == OI_ARRAY_TYPE) && (_oiArray == NULL))
    {
        mcsSTRING32 arrName;
        mcsSTRING32 myArrName;
        oidataOI_ARRAY *oiTable;
        oiTable = (oidataOI_ARRAY *) table;
        //  Get ArrName of oiArray
        if(oiTable->GetArrName(arrName) == mcsFAILURE)
        {
            return mcsFAILURE;
        }
        //  Get myArrName for reference
        if(GetArrName(myArrName) == mcsFAILURE)
        {
            return mcsFAILURE;
        }

        // Check if the reference is good
        if(strcmp(arrName, myArrName) == 0)
        {
            // Store reference;
            _oiArray = oiTable;
        }
    }
    else if((tableType == OI_WAVELENGTH_TYPE) && (_oiWavelength == NULL))
    {
        mcsSTRING32 insName;
        mcsSTRING32 myInsName;
        oidataOI_WAVELENGTH *oiTable;
        oiTable = (oidataOI_WAVELENGTH *) table;
        //  Get insName of oiArray
        if(oiTable->GetInsName(insName) == mcsFAILURE)
        {
            return mcsFAILURE;
        }
        //  Get myInsName for reference
        if(GetInsName(myInsName) == mcsFAILURE)
        {
            return mcsFAILURE;
        }

        // Check if the reference is good
        if(strcmp(insName, myInsName) == 0)
        {
            // Store reference;
            _oiWavelength = oiTable;
            // And save _nwave at the same time
            _nwave = _oiWavelength->GetNumberOfRows();
        }
    }

    return mcsSUCCESS;
}

/**
 * 
 * Get UTC start date of observations
 * 
 * @param dateObs UTC start date of observations
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataDATA_TABLE::GetDateObs(mcsSTRING32 dateObs)
{
    logTrace("oidataDATA_TABLE::GetDateObs()");
    return GetKeyword("DATE-OBS", dateObs);
}


/**
 * 
 * Get (optional) Identifies corresponding OI_ARRAY
 * 
 * @param arrName (optional) Identifies corresponding OI_ARRAY
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataDATA_TABLE::GetArrName(mcsSTRING32 arrName)
{
    logTrace("oidataDATA_TABLE::GetArrName()");
    return GetKeyword("ARRNAME", arrName);
}


/**
 * 
 * Get Identifies corresponding OI_WAVELENGTH table
 * 
 * @param insName Identifies corresponding OI_WAVELENGTH table
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataDATA_TABLE::GetInsName(mcsSTRING32 insName)
{
    logTrace("oidataDATA_TABLE::GetInsName()");
    return GetKeyword("INSNAME", insName);
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
mcsCOMPL_STAT oidataDATA_TABLE::GetTargetId(mcsINT16 * targetId)
{
    logTrace("oidataDATA_TABLE::GetTargetId()");
    return ReadColumn("TARGET_ID", _naxis2, 1, targetId);
}


/**
 * 
 * Get UTC time of observation (seconds)
 * 
 * @param time UTC time of observation (seconds)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataDATA_TABLE::GetTime(mcsDOUBLE * time)
{
    logTrace("oidataDATA_TABLE::GetTime()");
    return ReadColumn("TIME", _naxis2, 1, time);
}


/**
 * 
 * Get Modified Julian Day
 * 
 * @param mjd Modified Julian Day
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataDATA_TABLE::GetMJD(mcsDOUBLE * mjd)
{
    logTrace("oidataDATA_TABLE::GetMJD()");
    return ReadColumn("MJD", _naxis2, 1, mjd);
}


/**
 * 
 * Get Integration time (seconds)
 * 
 * @param intTime Integration time (seconds)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataDATA_TABLE::GetIntTime(mcsDOUBLE * intTime)
{
    logTrace("oidataDATA_TABLE::GetIntTime()");
    return ReadColumn("INT_TIME", _naxis2, 1, intTime);
}


/**
 * 
 * Get Station numbers contributing to the data.
 * Your array size must equals to nbOfRows* 2 for oidataOI_VIS 
 * and oidataOI_VIS2 or nbRows*3 for oidataOI_T3.
 * 
 * @param staIndex Station numbers contributing to the data
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataDATA_TABLE::GetStaIndex(mcsINT16 * staIndex)
{
    logTrace("oidataDATA_TABLE::GetStaIndex()");
    return ReadColumn("STA_INDEX", _naxis2 * _nbOfStations, 1, staIndex);
}


/**
 * 
 * Get Flag
 * 
 * @param flag Flag
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataDATA_TABLE::GetFlag(char *flag)
{
    logTrace("oidataDATA_TABLE::GetFlag()");
    oidataDataType type;
    long repeat = 0;
    long width = 0;
    oidataDataUnit unit = noUnit;
    if ( ReadColumnInfo("FLAG",
                                       &type,
                                       &repeat,
                                       &width,
                                       &unit) == mcsFAILURE )
    {
        return mcsFAILURE;
    }

    


    
    return ReadColumn("FLAG", _naxis2 * _nwave, 1, flag);
}

/*
 * protected method
 */

//
// This method has been overloaded to complet check for comarison with nwaves
//
mcsCOMPL_STAT oidataDATA_TABLE::CheckColumns(oidataColumnDefinition * columns)
{
    logTrace("oidataDATA_TABLE::CheckColumns()");
    mcsCOMPL_STAT complStat;

    complStat = oidataOI_TABLE::CheckColumns(columns);
    if(complStat == mcsFAILURE)
    {
        return mcsFAILURE;
    }

    int i = 0;
    oidataColumnDefinition columnEl;

    // Read first row of every columns 
    columnEl = columns[i];
    while(columnEl.name != NULL)
    {
        logDebug("Checking column dimension (NWAVE) %s", columnEl.name);
        if(columnEl.multiplier < 0)
        {
            // Search dimension and type of column into the oifits file
            oidataDataType type;
            long repeat = 0;
            long width = 0;
            oidataDataUnit unit = noUnit;
            char name[80];
            strncpy(name, columnEl.name, 80);
            mcsCOMPL_STAT ret = ReadColumnInfo((const char * )name,
                                               &type,
                                               &repeat,
                                               &width,
                                               &unit);
            if(ret == mcsFAILURE)
            {
                return mcsFAILURE;
            }
            // It is wrong if repeat differs from 0 or _nwave
            if( (repeat != 0 ) && (_nwave != repeat))
            {
                char msg[80];
                snprintf(msg, 80,
                         "column '%s' does not seem to have correct dimensions (according NWAVE)",
                         columnEl.name);
                errAdd(oidataERR_OITABLE_NOT_CONFORM, _extName, _hduIndex,
                       msg);
                return mcsFAILURE;
            }

        }

        // Go onto next line
        i++;
        columnEl = columns[i];
    }                           // End of loop for every column

    return mcsSUCCESS;
}



/*___oOo___*/
