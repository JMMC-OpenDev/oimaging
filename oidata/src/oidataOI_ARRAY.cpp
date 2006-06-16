/*******************************************************************************
 * JMMC project 
 * 
 * "@(#) $Id: oidataOI_ARRAY.cpp,v 1.7 2006-06-16 12:48:17 scetre Exp $"
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
 * Definition of oidataOI_ARRAY class. 
 */

static char *rcsId __attribute__ ((unused)) ="@(#) $Id: oidataOI_ARRAY.cpp,v 1.7 2006-06-16 12:48:17 scetre Exp $";
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

#include "oidataOI_ARRAY.h"

#include "oidataPrivate.h"


/** 
 * Class constructor 
 */
oidataOI_ARRAY::oidataOI_ARRAY(oidataFITS * fitsParent, mcsSTRING32 extName, mcsINT16 hduIndex):oidataOI_TABLE(fitsParent, extName,
               hduIndex)
{
    logTrace("oidataOI_ARRAY::oidataOI_ARRAY()");
    _tableType = OI_ARRAY_TYPE;

    // There is no keyword giving the nb of el
    // However it is equal to the nb of rows in the table
    // GetKeyword("NAXIS2", nbElements);
    _nbOfElements = _naxis2;
}

/** 
 * Class destructor 
 */
oidataOI_ARRAY::~oidataOI_ARRAY()
{
    logTrace("oidataOI_ARRAY::~oidataOI_ARRAY()");
}

/*
 * public method
 */

mcsCOMPL_STAT oidataOI_ARRAY::CheckStructure()
{
    logTrace("oidataOI_ARRAY::CheckStructure()");

    oidataKeywordDefinition oidataOiArrayKeywords[] = {
        {"OI_REVN", typeInteger, 1, noUnit,
         "revision number of the table definition"},
        {"ARRNAME", typeCharacter, 1, noUnit,
         "array name for cross-referencing"},
        {"FRAME", typeCharacter, 1, noUnit, "coordinate frame"},
        {"ARRAYX", typeDouble, 1, unitInMeters, "array center X-coordinate"},
        {"ARRAYY", typeDouble, 1, unitInMeters, "array center Y-coordinate"},
        {"ARRAYZ", typeDouble, 1, unitInMeters, "array center Z-coordinate"},
        {"NAXIS2", typeInteger, 1, noUnit, "number of stations"},
        // Do not remove the next line 
        {NULL, typeInteger, 0, noUnit, NULL}
    };


    oidataColumnDefinition oidataOiArrayColumns[] = {
        {"TEL_NAME", typeCharacter, 16, noUnit, "telescope name"},
        {"STA_NAME", typeCharacter, 16, noUnit, "station name"},
        {"STA_INDEX", typeInteger, 1, noUnit, "station index"},
        {"DIAMETER", typeFloat, 1, unitInMeters, "element diameter"},
        {"STAXYZ", typeDouble, 3, unitInMeters,
         "station coordinates relative to array center"},
        // Do not remove the next line 
        {NULL, typeInteger, 0, noUnit, NULL}
    };

    // Check keywords
    if(CheckKeywords(oidataOiArrayKeywords) == mcsFAILURE)
    {
        return mcsFAILURE;
    }

    // Check columns
    if(CheckColumns(oidataOiArrayColumns) == mcsFAILURE)
    {
        return mcsFAILURE;
    }

    return mcsSUCCESS;
}

/**
 * Get name of array for cross referencing.
 * 
 * @param arrName Array name, for cross referencing
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_ARRAY::GetArrName(mcsSTRING32 arrName)
{
    logTrace("oidataOI_ARRAY::getArrName()");
    return GetKeyword("ARRNAME", arrName);
}


/**
 * 
 * Get Coordinate frame
 * 
 * @param frame Coordinate frame
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_ARRAY::GetFrame(mcsSTRING32 frame)
{
    logTrace("oidataOI_ARRAY::GetFrame()");
    return GetKeyword("FRAME", frame);
}


/**
 * 
 * Get Array center coordinates (meters)
 * 
 * @param arrayX Array center coordinates (meters)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_ARRAY::GetArrayX(mcsDOUBLE * arrayX)
{
    logTrace("oidataOI_ARRAY::GetArrayX()");
    return GetKeyword("ARRAYX", arrayX);
}


/**
 * 
 * Get Array center coordinates (meters)
 * 
 * @param arrayY Array center coordinates (meters)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_ARRAY::GetArrayY(mcsDOUBLE * arrayY)
{
    logTrace("oidataOI_ARRAY::GetArrayY()");
    return GetKeyword("ARRAYY", arrayY);
}


/**
 * 
 * Get Array center coordinates (meters)
 * 
 * @param arrayZ Array center coordinates (meters)
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_ARRAY::GetArrayZ(mcsDOUBLE * arrayZ)
{
    logTrace("oidataOI_ARRAY::GetArrayZ()");
    return GetKeyword("ARRAYZ", arrayZ);
}


/**
 * 
 * Get Telescope name column
 * 
 * @param telName Telescope name column
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_ARRAY::GetTelName(mcsSTRING16 * telName)
{
    logTrace("oidataOI_ARRAY::GetTelName()");
    return ReadColumn("TEL_NAME", _nbOfElements, 1, (char **) telName);
}


/**
 * Get Station name column
 * 
 * @param staName Station name column
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_ARRAY::GetStaName(mcsSTRING16 * staName)
{
    logTrace("oidataOI_ARRAY::GetStaName()");
    return ReadColumn("STA_NAME", _nbOfElements, 1, (char **) staName);
}


/**
 * 
 * Get Station number column
 * 
 * @param staIndex Station number column
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_ARRAY::GetStaIndex(mcsINT16 * staIndex)
{
    logTrace("oidataOI_ARRAY::GetStaIndex()");
    return ReadColumn("STA_INDEX", _nbOfElements, 1, staIndex);
}


/**
 * 
 * Get Element diameter (meters) column.
 * 
 * @param diameter Element diameter (meters) column
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_ARRAY::GetDiameter(mcsFLOAT * diameter)
{
    logTrace("oidataOI_ARRAY::GetDiameter()");
    return ReadColumn("DIAMETER", _nbOfElements, 1, diameter);
}

/**
 * 
 * Get Station coordinates relative to array center (meters) column.
 * You should give address of an allocated array for 3*nbOfElements mcsDOUBLE.
 * 
 * @param staXYZ Station coordinates relative to array center (meters) column
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_ARRAY::GetStaXYZ(mcsDOUBLE * staXYZ)
{
    logTrace("oidataOI_ARRAY::GetStaXYZ()");
    return ReadColumn("STAXYZ", _nbOfElements * 3, 1, staXYZ);
}

/** 
 *  Get number of elements for this array.
 *
 *  \returns the number of elements.
 */
mcsINT16 oidataOI_ARRAY::GetNumberOfElements()
{
    logTrace("oidataOI_ARRAY::GetNumberOfElements()");
    return _nbOfElements;
}


/*___oOo___*/
