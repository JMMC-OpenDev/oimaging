/*******************************************************************************
 * JMMC project 
 * 
 * "@(#) $Id: oidataOI_TABLE.cpp,v 1.11 2006-06-16 12:48:17 scetre Exp $"
 * 
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.10  2006/05/11 13:04:56  mella
 * Changed rcsId declaration to perform good gcc4 and gcc3 compilation
 *
 * Revision 1.9  2005/09/27 14:16:47  mella
 * Use oidataDataType instead of cfitsio typecode
 *
 * Revision 1.8  2005/09/27 12:00:10  mella
 * Do not reject during loadind checks 0 dimensions
 *
 * Revision 1.7  2005/09/27 11:52:18  mella
 * Improve structure for description of keywords and columns
 *
 * Revision 1.6  2005/09/23 09:32:35  mella
 * Add unit check for columns
 *
 * Revision 1.5  2005/09/22 07:27:15  mella
 * Add method to get more column informations
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
 * Definition of oidataOI_TABLE class. 
 */

static char *rcsId __attribute__ ((unused)) ="@(#) $Id: oidataOI_TABLE.cpp,v 1.11 2006-06-16 12:48:17 scetre Exp $";
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

#include "oidata.h"
#include "oidataOI_TABLE.h"
#include "oidataFITS.h"

#include "oidataPrivate.h"

/** 
 * Class constructor 
 *
 * @param fitsParent reference onto the parent FITS object.
 * @param hduIndex table index into the fits file.
 */
oidataOI_TABLE::oidataOI_TABLE(oidataFITS * fitsParent,
                               mcsSTRING32 extName, mcsINT16 hduIndex)
{
    logTrace("oidataOI_TABLE::oidataOI_TABLE()");
    _fitsParent = fitsParent;
    _hduIndex = hduIndex;
    strncpy(_extName, extName, 32);
    _tableType = UNKNOWN_OI_TYPE;
    GetKeyword("NAXIS2", &_naxis2);
}

/** 
 * Class destructor 
 */
oidataOI_TABLE::~oidataOI_TABLE()
{
    logTrace("oidataOI_TABLE::~oidataOI_TABLE()");
}

/*
 * public method
 */
/**
 * 
 * Get type of OI_TABLE.
 * 
 * @return one of the oidataTABLE_TYPE enum.
 */
oidataTABLE_TYPE oidataOI_TABLE::GetType()
{
    logTrace("oidataOI_TABLE::GetType()");
    return _tableType;
}

/** 
 *  Check content structure of the oidataOI_TABLE.
 *  This method check keywords or columns of given fits files.
 *
 *  @returns an MCS completion status code (SUCCESS or FAILURE) 
 */
mcsCOMPL_STAT oidataOI_TABLE::CheckStructure()
{
    logTrace("oidataOI_TABLE::CheckStructure()");
    logWarning(" Developper should have overloaded this method for %s table",
               _extName);
    errAdd(oidataERR_OITABLE_NOT_CONFORM, _extName, _hduIndex,
           " no check have been done ");
    return mcsSUCCESS;
    return mcsFAILURE;
}


/**
 * 
 * Get number of rows.
 * 
 * @return the number of rows of the column.
 */
mcsINT16 oidataOI_TABLE::GetNumberOfRows()
{
    logTrace("oidataOI_TABLE::GetNumberOfRows()");
    return _naxis2;
}

/**
 * 
 * Get 
 * 
 * @param extname 
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TABLE::GetExtname(mcsSTRING32 extname)
{
    logTrace("oidataOI_TABLE::GetExtname()");
    strncpy(extname, _extName, 32);
    return mcsSUCCESS;
}


/**
 * 
 * Get revision number of the table definition.
 * 
 * @param oiRevn Revision number of the table definition.
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataOI_TABLE::GetOiRevn(mcsINT16 * oiRevn)
{
    logTrace("oidataOI_TABLE::GetOiRevn()");
    return GetKeyword("OI_REVN", oiRevn);
}

mcsCOMPL_STAT oidataOI_TABLE::GetHduIndex(mcsINT16 * hduIndex)
{
    logTrace("oidataOI_TABLE::GetHduIndex()");
    *hduIndex = _hduIndex;
    return mcsSUCCESS;
}


/**
 * 
 * Get 16bit integer keyword.
 * 
 * @param keyName name of the keyword 
 * @param keyValue returned value of the keyword
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT
    oidataOI_TABLE::GetKeyword(const mcsSTRING32 keyName, mcsINT16 * keyValue)
{
    logTrace("oidataOI_TABLE::GetKeyword()");
    return _fitsParent->GetKeyword(keyName, keyValue, _hduIndex);
}


/**
 * 
 * Get 32bit real keyword.
 * 
 * @param keyName name of the keyword 
 * @param keyValue returned value of the keyword
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT
    oidataOI_TABLE::GetKeyword(const mcsSTRING32 keyName, mcsFLOAT * keyValue)
{
    logTrace("oidataOI_TABLE::GetKeyword()");
    return _fitsParent->GetKeyword(keyName, keyValue, _hduIndex);
}


/**
 * 
 * Get 64bit double keyword.
 * 
 * @param keyName name of the keyword 
 * @param keyValue returned value of the keyword
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT
    oidataOI_TABLE::GetKeyword(const mcsSTRING32 keyName,
                               mcsDOUBLE * keyValue)
{
    logTrace("oidataOI_TABLE::GetKeyword()");
    return _fitsParent->GetKeyword(keyName, keyValue, _hduIndex);
}


/**
 * 
 * Get logical keyword.
 * 
 * @param keyName name of the keyword 
 * @param keyValue returned value of the keyword
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT
    oidataOI_TABLE::GetKeyword(const mcsSTRING32 keyName,
                               mcsLOGICAL * keyValue)
{
    logTrace("oidataOI_TABLE::GetKeyword()");
    return _fitsParent->GetKeyword(keyName, keyValue, _hduIndex);
}


/**
 * 
 * Get string keyword.
 * 
 * @param keyName name of the keyword 
 * @param keyValue returned value of the keyword
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT
    oidataOI_TABLE::GetKeyword(const mcsSTRING32 keyName,
                               mcsSTRING32 keyValue)
{
    logTrace("oidataOI_TABLE::GetKeyword()");
    return _fitsParent->GetKeyword(keyName, keyValue, _hduIndex);
}


mcsCOMPL_STAT
    oidataOI_TABLE::ReadColumn(const mcsSTRING32 colName,
                               mcsINT16 nbElements,
                               mcsINT16 rowIdx, mcsINT16 * array)
{
    logTrace("oidataOI_TABLE::ReadColumn()");
    return _fitsParent->ReadColumn(colName,
                                   _hduIndex, nbElements, rowIdx, array);
}

mcsCOMPL_STAT
    oidataOI_TABLE::ReadColumn(const mcsSTRING32 colName,
                               mcsINT16 nbElements,
                               mcsINT16 rowIdx, mcsFLOAT * array)
{
    logTrace("oidataOI_TABLE::ReadColumn()");
    return _fitsParent->ReadColumn(colName,
                                   _hduIndex, nbElements, rowIdx, array);
}

mcsCOMPL_STAT
    oidataOI_TABLE::ReadColumn(const mcsSTRING32 colName,
                               mcsINT16 nbElements,
                               mcsINT16 rowIdx, mcsDOUBLE * array)
{
    logTrace("oidataOI_TABLE::ReadColumn()");
    return _fitsParent->ReadColumn(colName,
                                   _hduIndex, nbElements, rowIdx, array);
}

// Used for Logical due to cfitsio mapping
mcsCOMPL_STAT
    oidataOI_TABLE::ReadColumn(const mcsSTRING32 colName,
                               mcsINT16 nbElements, mcsINT16 rowIdx,
                               char *array)
{
    logTrace("oidataOI_TABLE::ReadColumn()");
    return _fitsParent->ReadColumn(colName,
                                   _hduIndex, nbElements, rowIdx, array);
}

mcsCOMPL_STAT
    oidataOI_TABLE::ReadColumn(const mcsSTRING32 colName,
                               mcsINT16 nbElements,
                               mcsINT16 rowIdx, char **array)
{
    logTrace("oidataOI_TABLE::ReadColumn()");
    return _fitsParent->ReadColumn(colName,
                                   _hduIndex, nbElements, rowIdx, array);
}

mcsCOMPL_STAT oidataOI_TABLE::ReadColumnInfo(const mcsSTRING32 colName,
                                             oidataDataType *type,
                                             long *repeat, long *width,
                                             oidataDataUnit * unit)
{
    logTrace("oidataOI_TABLE::ReadColumnInfo()");
    return _fitsParent->ReadColumnInfo(colName,
                                       _hduIndex,
                                       type, repeat, width, unit);
}


/*
 * protected method
 */

/** 
 *  This method is used during loading sequence to check structure of columns
 *  respect to the oidata format.   
 *  User does not have to call this method.
 *
 * @param columns column's definition array 
 *
 *  @returns an MCS completion status code (SUCCESS or FAILURE) 
 */
mcsCOMPL_STAT oidataOI_TABLE::CheckColumns(oidataColumnDefinition * columns)
{
    logTrace("oidataOI_TABLE::CheckColumns()");

    int i = 0;
    oidataColumnDefinition columnEl;

    // Read first row of every columns 
    columnEl = columns[i];
    while(columnEl.name != NULL)
    {
        logDebug("Checking column '%s'", columnEl.name);
        // Search dimension and type of column into the oifits file
        oidataDataType type;
        long repeat = 0;
        long width = 0;
        oidataDataUnit unit = noUnit;
        mcsCOMPL_STAT ret = ReadColumnInfo(columnEl.name,
                                           &type,
                                           &repeat,
                                           &width,
                                           &unit);
        if(ret == mcsFAILURE)
        {
            char msg[80];
            snprintf(msg, 80, "error reading column '%s'", columnEl.name);
            errAdd(oidataERR_OITABLE_NOT_CONFORM, _extName, _hduIndex, msg);

            return mcsFAILURE;
        }

        // If dimension is positive in definition, do test if it equals
        if(columnEl.multiplier > 0)
        {
            if(repeat != columnEl.multiplier)
            {
                char msg[80];
                snprintf(msg, 80,
                         "column '%s' does not seem to have correct dimensions",
                         columnEl.name);
                errAdd(oidataERR_OITABLE_NOT_CONFORM, _extName, _hduIndex,
                       msg);
                return mcsFAILURE;
            }
        }

        // Compare type definition and type indicated by file
        if(type !=  columnEl.type)
        {
            char msg[80];
            snprintf(msg, 80,
                     "column '%s' does not seem to have right type",
                     columnEl.name);
            errAdd(oidataERR_OITABLE_NOT_CONFORM, _extName, _hduIndex, msg);
            return mcsFAILURE;

        }

        // Do not read column if it is supposed null
        if( repeat > 0)
        {
            // Read typed column depending one the founded type in definition
            mcsCOMPL_STAT complStat;
            mcsINT16 oneInteger;
            char *oneString[1];
            mcsFLOAT oneFloat;
            mcsDOUBLE oneDouble;
            char oneLogical;
            oneString[0] = (char *) malloc(256);
            switch (columnEl.type)
            {
                case typeInteger:
                    complStat = ReadColumn(columnEl.name, 1, 1, &oneInteger);
                    break;
                case typeCharacter:
                    complStat = ReadColumn(columnEl.name, 1, 1, (char **) oneString);
                    break;
                case typeFloat:
                    complStat = ReadColumn(columnEl.name, 1, 1, &oneFloat);
                    break;
                case typeDouble:
                    complStat = ReadColumn(columnEl.name, 1, 1, &oneDouble);
                    break;
                case typeLogical:
                    complStat = ReadColumn(columnEl.name, 1, 1, &oneLogical);
                    break;
                default:
                    errAdd(oidataERR_GENERIC,
                           "error getting definition of column format");
                    return mcsFAILURE;
            }
            if(complStat == mcsFAILURE)
            {
                char msg[80];
                snprintf(msg, 80,
                         "error reading column '%s' during structural check",
                         columnEl.name);
                errAdd(oidataERR_OITABLE_NOT_CONFORM, _extName, _hduIndex, msg);
                return mcsFAILURE;
            }
        }
        // Check units
        if(columnEl.unit != unit)
        {
            char msg[80];
            snprintf(msg, 80, "unit error for column '%s'", columnEl.name);
            errAdd(oidataERR_OITABLE_NOT_CONFORM, _extName, _hduIndex, msg);
            return mcsFAILURE;

        }


        // Go onto next line
        i++;
        columnEl = columns[i];
    }                           // End of loop for every column

    return mcsSUCCESS;
}

/** 
 *  This method is used during loading sequence to check structure of keywords
 *  respect to the oidata format.   
 *  User does not have to call this method.
 *
 * @param keywords keyword's definition array 
 *
 *  @returns an MCS completion status code (SUCCESS or FAILURE) 
 */
mcsCOMPL_STAT oidataOI_TABLE::CheckKeywords(oidataKeywordDefinition *
                                            keywords)
{
    logTrace("oidataOI_TABLE::CheckKeywords()");

    int i = 0;
    oidataKeywordDefinition keywordEl;

    // Read first row of every keywords 
    keywordEl = keywords[i];
    while(keywordEl.name != NULL)
    {
        logDebug("Checking keyword '%s'", keywordEl.name);

        mcsCOMPL_STAT complStat;
        mcsINT16 oneInteger;
        mcsSTRING128 oneString;
        mcsFLOAT oneFloat;
        mcsDOUBLE oneDouble;
        mcsLOGICAL oneLogical;

        // Read typed keyword depending one the founded letter
        switch (keywordEl.type)
        {
        case typeInteger:
            complStat = GetKeyword(keywordEl.name, &oneInteger);
            break;
        case typeCharacter:
            complStat = GetKeyword(keywordEl.name, oneString);
            break;
        case typeFloat:
            complStat = GetKeyword(keywordEl.name, &oneFloat);
            break;
        case typeDouble:
            complStat = GetKeyword(keywordEl.name, &oneDouble);
            break;
        case typeLogical:
            complStat = GetKeyword(keywordEl.name, &oneLogical);
            break;
        default:
            errAdd(oidataERR_GENERIC,
                   "error getting definition of keyword format");
            return mcsFAILURE;
        }
        if(complStat == mcsFAILURE)
        {
            // Do return error only if keyword should be present
            if(keywordEl.multiplier > 0)
            {
                char msg[80];
                snprintf(msg, 80,
                         "error reading keyword '%s' during structural check",
                         keywordEl.name);
                errAdd(oidataERR_OITABLE_NOT_CONFORM, _extName, _hduIndex,
                       msg);
                return mcsFAILURE;
            }
            else
            {
                logDebug("Keyword considered optional");
            }

        }

        // Go onto next line
        i++;
        keywordEl = keywords[i];
    }

    return mcsSUCCESS;

}

/*___oOo___*/
