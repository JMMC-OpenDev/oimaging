/*******************************************************************************
 * JMMC project 
 * 
 * "@(#) $Id: oidataFITS.cpp,v 1.11 2006-06-16 12:48:17 scetre Exp $"
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
 * Revision 1.8  2005/09/27 11:52:18  mella
 * Improve structure for description of keywords and columns
 *
 * Revision 1.7  2005/09/23 09:32:35  mella
 * Add unit check for columns
 *
 * Revision 1.6  2005/09/22 07:27:15  mella
 * Add method to get more column informations
 *
 * Revision 1.5  2005/09/19 13:58:08  mella
 * Every colums, and keyword presence are checked
 *
 * Revision 1.4  2005/09/15 09:26:11  mella
 * Add extname to protected member and to constructor parameter list
 *
 * Revision 1.3  2005/09/14 15:29:56  mella
 * Add some tests and do not work directly with cfitsio access during Load
 *
 * Revision 1.2  2005/08/31 20:07:59  mella
 * First revision
 *
 * Revision 1.1  2005/06/24 14:07:10  mella
 * First revision
 *
 *******************************************************************************/
/** 
 * \file
 * Definition of oidataFITS class. 
 */

static char *rcsId __attribute__ ((unused)) ="@(#) $Id: oidataFITS.cpp,v 1.11 2006-06-16 12:48:17 scetre Exp $";
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
#include "oidataFITS.h"
#include "oidataPrivate.h"
#include "oidataErrors.h"





/** 
 * Class constructor 
 */
oidataFITS::oidataFITS()
{
    logTrace("oidataFITS::oidataFITS()");

    /* Init class members */
    _oiFile = NULL;
    _fitsFileLoaded = mcsFALSE;
    _oiTarget = NULL;
}

/** 
 * Class destructor 
 */
oidataFITS::~oidataFITS()
{
    logTrace("oidataFITS::~oidataFITS()");
}

/*
 * public method
 */

/**
 * Load oifits file. Some checks are done to verify oidata format integrity.
 * 
 * @param filename name of the oifits file.
 * @param stopForBadFormat indicates if process should stop against FITS format
 * error.
 * 
 * @return mcsSUCCESS on successful completion (file has been loaded and
 * did not stop for a bad format). Otherwise mcsFAILURE is returned.
 *
 * 
 */
mcsCOMPL_STAT oidataFITS::Load(const mcsSTRING32 filename,
                               const mcsLOGICAL stopForBadFormat)
{
    int status = 0;
    int nbHDU = 0;
    int i;
    char extname[FLEN_VALUE];
    mcsINT16 oi_revn;

    logTrace("oidataFITS::Load()");
    if(stopForBadFormat == mcsTRUE)
    {
        logDebug("Stop if format error encountered");
    }
    else
    {
        logDebug("Do not stop if format error encountered");
    }

    /* check if no fits file has been previously loaded before operation */
    if(_fitsFileLoaded == mcsTRUE)
    {
        errAdd(oidataERR_NOT_LOADED, "Load");
        return mcsFAILURE;
    }

    if(fits_open_file(&_oiFile, filename, READONLY, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    /* Build vector of table and assign some references */
    {
        if(fits_get_num_hdus(_oiFile, &nbHDU, &status))
        {
            if(status)
            {
                oidataAddFitsError(status, __FILE_LINE__, NULL);
                return mcsFAILURE;
            }
        }

        logDebug("Total number of HDU: %d", nbHDU);

        // Loop for all hdu
        for(i = 2; i <= nbHDU; i++)
        {
            status = 0;

            // Move to right hdu into fits file
            fits_movabs_hdu(_oiFile, i, NULL, &status);
            if(status)
            {
                oidataAddFitsError(status, __FILE_LINE__, NULL);
                return mcsFAILURE;
            }

            // Read hdu EXTNAME
            if(GetKeyword("EXTNAME", extname, i) == mcsFAILURE)
            {
                return mcsFAILURE;
            }



            // only store OI tables (extname starts with "OI_")
            if(strncmp(extname, "OI_", 3) == 0)
            {
                // CHECK OIDATA FORMAT for OI_REVN numerical revision number  
                if(GetKeyword("OI_REVN", &oi_revn, i) == mcsFAILURE)
                {
                    if(stopForBadFormat == mcsTRUE)
                    {
                        errAdd(oidataERR_OITABLE_NOT_CONFORM,
                               extname,
                               i,
                               "Unknown revision of the OI Exchange Format");
                        return mcsFAILURE;
                    }
                    else
                    {
                        // \todo would be fine to indicate which table it is
                        logWarning("Can't read revision for table '%s:%d'",
                                   extname, i);
                        logWarning
                            ("Unknown revision of the OI Exchange Format");
                    }
                }
                // if OI_REVN present, 
                // CHECK OIDATA FORMAT for higher version than 1 of OI_REVN
                else if(oi_revn > oidataACTUAL_REVISION)
                {
                    if(stopForBadFormat == mcsTRUE)
                    {
                        errAdd(oidataERR_OITABLE_NOT_CONFORM,
                               extname,
                               i,
                               "Unknown revision of the OI Exchange Format");
                        return mcsFAILURE;
                    }
                    else
                    {
                        logWarning
                            ("Revision '%d' of the OI Exchange Format unsupported ",
                             oi_revn);
                    }
                }
                logDebug("OI_HDU[%d]: %s", i, extname);
                oidataOI_TABLE *newOiTable;
                if(FactorOiTable(extname, i, &newOiTable) == mcsFAILURE)
                {
                    return mcsFAILURE;
                }
                _oiTables.push_back(newOiTable);

                // try to store ref for the OI_TARGET
                if(strncmp(extname, "OI_TARGET", 9) == 0)
                {
                    if(_oiTarget == NULL)
                    {
                        _oiTarget = (oidataOI_TARGET *) newOiTable;
                    }
                    // CHECK OIDATA FORMAT for presence of one OI_TARGET
                    else if(stopForBadFormat == mcsTRUE)
                    {
                        // oidata.i:1.1:425 
                        errAdd(oidataERR_OITABLE_NOT_CONFORM,
                               extname,
                               i, "Multiple OI_TARGET not yet supported");
                        return mcsFAILURE;
                    }
                    else
                    {
                        logWarning("Multiple OI_TARGET not yet supported");
                    }
                }

            }
        }
    }

    // CHECK OIDATA FORMAT for presence of one OI_TARGET
    if(_oiTarget == NULL)
    {
        if(stopForBadFormat == mcsTRUE)
        {
            // oidata.i:1.1:425 
            errAdd(oidataERR_GENERIC, "No OI_TARGET founded");
            return mcsFAILURE;
        }
        else
        {
            logWarning("No OI_TARGET founded");
        }

    }

    // Perform cross reference
    MakeCrossReferences();

    // check oidata structure 
    if(CheckIntegrity() == mcsFAILURE)
    {
        if(stopForBadFormat == mcsTRUE)
        {
            return mcsFAILURE;
        }
        logWarning(" Some structural error were encountered");
        errCloseStack();
    }

    _fitsFileLoaded = mcsTRUE;
    return mcsSUCCESS;
}

/**
 * Save oifits file. 
 * 
 * @param filename new filename.
 * @param overwrite logical to permit file overwriting.
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT
    oidataFITS::Save(const mcsSTRING32 filename, const mcsLOGICAL overwrite)
{
    logTrace("oidataFITS::Save()");

    /* check if one fits file has been loaded before operation */
    if(_fitsFileLoaded == mcsFALSE)
    {
        errAdd(oidataERR_NOT_LOADED, "Save");
        return mcsFAILURE;
    }

    return mcsSUCCESS;
}


/**
 * 
 * Get associated OI_TARGET.
 * 
 * @param oiTarget 
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataFITS::GetOiTarget(oidataOI_TARGET ** oiTarget)
{
    logTrace("oidataFITS::GetOiTarget()");

    /* check if one fits file has been loaded before operation */
    if(_fitsFileLoaded == mcsFALSE)
    {
        errAdd(oidataERR_NOT_LOADED, "GetOiTarget");
        return mcsFAILURE;
    }

    /* reference should be valid because validity has already been checked */
    *oiTarget = _oiTarget;

    return mcsSUCCESS;
}


/**
 * 
 * Get number of OI_TABLE present into this FITS. Only OI_TABLE are considered
 * by this method and other tables are ignored.
 * 
 * @param nbOfTable number of OI_TABLE present into this FITS
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataFITS::GetNumberOfOiTable(mcsINT16 * nbOfTable)
{
    logTrace("oidataFITS::GetNumberOfOiTable()");

    /* check if one fits file has been loaded before operation */
    if(_fitsFileLoaded == mcsFALSE)
    {
        errAdd(oidataERR_NOT_LOADED, "GetNumberOfOiTable");
        return mcsFAILURE;
    }

    *nbOfTable = _oiTables.size();

    return mcsSUCCESS;
}


/**
 * 
 * Get table containing oidata informations
 * 
 * @param table table containing oidata informations
 * @param index index of requested OI_TABLE
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 * @warning this index is not the same as the fits hdu index.
 */
mcsCOMPL_STAT oidataFITS::GetOiTable(oidataOI_TABLE ** table, mcsINT16 index)
{
    logTrace("oidataFITS::GetOiTable()");

    /* check if one fits file has been loaded before operation */
    if(_fitsFileLoaded == mcsFALSE)
    {
        errAdd(oidataERR_NOT_LOADED, "GetOiTable");
        return mcsFAILURE;
    }

    // Get reference from vector
    *table = _oiTables[index];

    return mcsSUCCESS;
}

/*
 * private method
 */

/**
 * 
 * This method should only be called by the Load method after getting 
 * full vector of tables. It can scan every table and propose the refernce of 
 * every other ones.
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataFITS::MakeCrossReferences()
{
    int nbOfTables;
    int i, j;
    int tableType;
    oidataOI_TABLE *tableI, *tableJ;
    logTrace("oidataFITS::MakeCrossReferences()");

    nbOfTables = _oiTables.size();
    for(i = 0; i < nbOfTables; i++)
    {
        tableI = _oiTables[i];
        for(j = 0; j < nbOfTables; j++)
        {
            if(i != j)
            {
                tableJ = _oiTables[j];
                tableType = tableJ->GetType();
                // This switch is needed because actually only
                // oidataDATA_TABLE get ProposeRefernce method
                // It may be extended to oidataOI_TABLE to make it simpler
                switch (tableType)
                {
                case OI_VIS_TYPE:
                case OI_VIS2_TYPE:
                case OI_T3_TYPE:
                    oidataDATA_TABLE * dataTable;
                    dataTable = (oidataDATA_TABLE *) tableJ;
                    if(dataTable->ProposeReference(tableI) == mcsFAILURE)
                    {
                        return mcsFAILURE;
                    }
                    //   default:
                    // actually do nothing for other table
                    // because they do not require any crossRef
                }
            }
        }
    }
    return mcsSUCCESS;
}

/**
 * 
 * This method should only be called by the Load method. 
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT oidataFITS::CheckIntegrity()
{
    int nbOfTables;
    logTrace("oidataFITS::CheckIntegrity()");

    nbOfTables = _oiTables.size();
    for(int i = 0; i < nbOfTables; i++)
    {
        if(_oiTables[i]->CheckStructure() == mcsFAILURE)
        {
            return mcsFAILURE;
        }
    }
    return mcsSUCCESS;
}

/** 
 *  
 *
 * \param extname  
 * \param hduIndex  
 * \param newTable  
 *
 *  \returns an MCS completion status code (SUCCESS or FAILURE)
 */
mcsCOMPL_STAT
    oidataFITS::FactorOiTable(mcsSTRING32 extname,
                              mcsINT16 hduIndex, oidataOI_TABLE ** newTable)
{
    logTrace("oidataFITS::FactorOiTable()");

    if(strncmp(extname, "OI_TARGET", 9) == 0)
    {

        *newTable = new oidataOI_TARGET(this, extname, hduIndex);
    }
    else if(strncmp(extname, "OI_ARRAY", 8) == 0)
    {
        *newTable = new oidataOI_ARRAY(this, extname, hduIndex);
    }
    else if(strncmp(extname, "OI_WAVELENGTH", 13) == 0)
    {
        *newTable = new oidataOI_WAVELENGTH(this, extname, hduIndex);
    }
    else if(strncmp(extname, "OI_VIS2", 7) == 0)
    {
        // OI_VIS2 must be checked before OI_VIS to match correctly
        *newTable = new oidataOI_VIS2(this, extname, hduIndex);
    }
    else if(strncmp(extname, "OI_VIS", 6) == 0)
    {
        *newTable = new oidataOI_VIS(this, extname, hduIndex);
    }
    else if(strncmp(extname, "OI_T3", 5) == 0)
    {
        *newTable = new oidataOI_T3(this, extname, hduIndex);
    }
    else
    {
        errAdd(oidataERR_GENERIC,
               "Can't allocate new object for wrong oi type");
        return mcsFAILURE;
    }

    return mcsSUCCESS;
}


/**
 * 
 * Get 16bit integer keyword.
 * 
 * @param keyName name of the keyword 
 * @param keyValue returned value of the keyword
 * @param hduIndex hdu index of the requested table 
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT
    oidataFITS::GetKeyword(const mcsSTRING32 keyName, mcsINT16 * keyValue,
                           mcsINT16 hduIndex)
{
    logTrace("oidataFITS::GetKeyword()");

    int status = 0;
    char comment[FLEN_COMMENT];
    short value;

    // Go onto the right HDU
    fits_movabs_hdu(_oiFile, hduIndex, NULL, &status);
    if(status)
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    fits_read_key(_oiFile, TUSHORT, (char *) keyName, &value, comment,
                  &status);
    if(status)
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }
    *keyValue = value;

    return mcsSUCCESS;
}


/**
 * 
 * Get 32bit real keyword.
 * 
 * @param keyName name of the keyword 
 * @param keyValue returned value of the keyword
 * @param hduIndex hdu index of the requested table 
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT
    oidataFITS::GetKeyword(const mcsSTRING32 keyName, mcsFLOAT * keyValue,
                           mcsINT16 hduIndex)
{
    logTrace("oidataFITS::GetKeyword()");

    int status = 0;
    char comment[FLEN_COMMENT];
    float value;

    // Go onto the right HDU
    fits_movabs_hdu(_oiFile, hduIndex, NULL, &status);
    if(status)
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    fits_read_key(_oiFile, TFLOAT, (char *) keyName, &value, comment,
                  &status);
    if(status)
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }
    *keyValue = value;

    return mcsSUCCESS;
}


/**
 * 
 * Get 64bit double keyword.
 * 
 * @param keyName name of the keyword 
 * @param keyValue returned value of the keyword
 * @param hduIndex hdu index of the requested table 
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT
    oidataFITS::GetKeyword(const mcsSTRING32 keyName, mcsDOUBLE * keyValue,
                           mcsINT16 hduIndex)
{
    logTrace("oidataFITS::GetKeyword()");

    int status = 0;
    char comment[FLEN_COMMENT];
    double value;

    // Go onto the right HDU
    fits_movabs_hdu(_oiFile, hduIndex, NULL, &status);
    if(status)
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    fits_read_key(_oiFile, TDOUBLE, (char *) keyName, &value, comment,
                  &status);
    if(status)
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }
    *keyValue = value;
    return mcsSUCCESS;
}


/**
 * 
 * Get logical keyword.
 * 
 * @param keyName name of the keyword 
 * @param keyValue returned value of the keyword
 * @param hduIndex hdu index of the requested table 
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT
    oidataFITS::GetKeyword(const mcsSTRING32 keyName, mcsLOGICAL * keyValue,
                           mcsINT16 hduIndex)
{
    logTrace("oidataFITS::GetKeyword()");

    int status = 0;
    char comment[FLEN_COMMENT];
    int value;

    // Go onto the right HDU
    fits_movabs_hdu(_oiFile, hduIndex, NULL, &status);
    if(status)
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    fits_read_key(_oiFile, TLOGICAL, (char *) keyName, &value, comment,
                  &status);
    if(status)
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }
    if(value)
    {
        *keyValue = mcsTRUE;
    }
    else
    {
        *keyValue = mcsFALSE;
    }
    return mcsSUCCESS;
}


/**
 * 
 * Get string keyword.
 * 
 * @param keyName name of the keyword 
 * @param keyValue returned value of the keyword
 * @param hduIndex hdu index of the requested table 
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT
    oidataFITS::GetKeyword(const mcsSTRING32 keyName, mcsSTRING32 keyValue,
                           mcsINT16 hduIndex)
{
    logTrace("oidataFITS::GetKeyword()");

    int status = 0;
    char comment[FLEN_COMMENT];
    char value[FLEN_VALUE];

    // Go onto the right HDU
    fits_movabs_hdu(_oiFile, hduIndex, NULL, &status);
    if(status)
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    fits_read_key(_oiFile, TSTRING, (char *) keyName, value, comment,
                  &status);
    if(status)
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }
    strncpy(keyValue, value, 32);
    return mcsSUCCESS;
}

/**
 * Read an integer (I) data column.
 * 
 * @param colName name of the column 
 * @param hduIndex hdu index of the requested table 
 * @param nbElements number of elements to read
 * @param rowIdx start copy from given rowIndex (firstrow index is 1)
 * @param array user allocaded array where data will be written
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT
    oidataFITS::ReadColumn(const mcsSTRING32 colName,
                           mcsINT16 hduIndex,
                           mcsINT16 nbElements,
                           mcsINT16 rowIdx, mcsINT16 * array)
{
    int status = 0;
    int colNum;
    long nbRows;
    short nullvalue = 0;
    int anynull;

    logTrace("oidataFITS::ReadColumn()");

    // Go onto the right HDU
    fits_movabs_hdu(_oiFile, hduIndex, NULL, &status);
    if(status)
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // getcolNum from given colName
    if(fits_get_colnum
       (_oiFile, CASEINSEN, (char *) colName, &colNum, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // Get number of rows
    if(fits_get_num_rows(_oiFile, &nbRows, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // @todo assert nbElements == nbRows*n  from nTYPE of TFORMcolIdx
    // using 
    // int fits_get_bcolparms / ffgbcl
    //  (fitsfile *fptr, int colnum, > char *ttype, char *tunit,
    //   char *typechar, long *repeat, double *scale, double *zero,
    //   long *nulval, char *tdisp, int  *status)


    if(nbElements != nbRows)
    {
        logWarning("nbElements could be wrong (%d, must be %ld) \n",
                   nbElements, nbRows);
    }


    // fill array 
    if(fits_read_col(_oiFile, TSHORT, colNum, rowIdx, 1, nbElements,
                     &nullvalue, array, &anynull, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    return mcsSUCCESS;
}

/**
 * Read a real (E) data column.
 * 
 * @param colName name of the column 
 * @param hduIndex hdu index of the requested table 
 * @param nbElements number of elements to read
 * @param rowIdx start copy from given rowIndex (firstrow index is 1)
 * @param array user allocaded array where data will be written
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT
    oidataFITS::ReadColumn(const mcsSTRING32 colName,
                           mcsINT16 hduIndex,
                           mcsINT16 nbElements,
                           mcsINT16 rowIdx, mcsFLOAT * array)
{
    int status = 0;
    int colNum;
    long nbRows;
    double nullvalue = 0.0;
    int anynull;

    logTrace("oidataFITS::ReadColumn()");

    // Go onto the right HDU
    fits_movabs_hdu(_oiFile, hduIndex, NULL, &status);
    if(status)
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // getcolNum from given colName
    if(fits_get_colnum
       (_oiFile, CASEINSEN, (char *) colName, &colNum, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // Get number of rows
    if(fits_get_num_rows(_oiFile, &nbRows, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // @todo assert nbElements == nbRows*n  from nTYPE of TFORMcolIdx
    // using 
    // int fits_get_bcolparms / ffgbcl
    //  (fitsfile *fptr, int colnum, > char *ttype, char *tunit,
    //   char *typechar, long *repeat, double *scale, double *zero,
    //   long *nulval, char *tdisp, int  *status)


    if(nbElements != nbRows)
    {
        logWarning("##### nbElements could be wrong (%d, must be %ld) \n",
                   nbElements, nbRows);
    }


    // fill array 
    if(fits_read_col(_oiFile, TFLOAT, colNum, rowIdx, 1, nbElements,
                     &nullvalue, array, &anynull, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    return mcsSUCCESS;
}

/**
 * Read a double (D) data column.
 * 
 * @param colName name of the column 
 * @param hduIndex hdu index of the requested table 
 * @param nbElements number of elements to read
 * @param rowIdx start copy from given rowIndex (firstrow index is 1)
 * @param array user allocaded array where data will be written
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT
    oidataFITS::ReadColumn(const mcsSTRING32 colName,
                           mcsINT16 hduIndex,
                           mcsINT16 nbElements,
                           mcsINT16 rowIdx, mcsDOUBLE * array)
{
    int status = 0;
    int colNum;
    long nbRows;
    double nullvalue = 0.0;
    int anynull;

    logTrace("oidataFITS::ReadColumn()");

    // Go onto the right HDU
    fits_movabs_hdu(_oiFile, hduIndex, NULL, &status);
    if(status)
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // getcolNum from given colName
    if(fits_get_colnum
       (_oiFile, CASEINSEN, (char *) colName, &colNum, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // Get number of rows
    if(fits_get_num_rows(_oiFile, &nbRows, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // @todo assert nbElements == nbRows*n  from nTYPE of TFORMcolIdx
    // using 
    // int fits_get_bcolparms / ffgbcl
    //  (fitsfile *fptr, int colnum, > char *ttype, char *tunit,
    //   char *typechar, long *repeat, double *scale, double *zero,
    //   long *nulval, char *tdisp, int  *status)


    if(nbElements != nbRows)
    {
        logWarning("##### nbElements could be wrong (%d, must be %ld) \n",
                   nbElements, nbRows);
    }


    // fill array 
    if(fits_read_col(_oiFile, TDOUBLE, colNum, rowIdx, 1, nbElements,
                     &nullvalue, array, &anynull, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    return mcsSUCCESS;
}

/**
 * Read a logical (L) data column.
 * 
 * @param colName name of the column 
 * @param hduIndex hdu index of the requested table 
 * @param nbElements number of elements to read
 * @param rowIdx start copy from given rowIndex (firstrow index is 1)
 * @param array user allocaded array where data will be written
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 */
mcsCOMPL_STAT
    oidataFITS::ReadColumn(const mcsSTRING32 colName,
                           mcsINT16 hduIndex,
                           mcsINT16 nbElements, mcsINT16 rowIdx, char *array)
{
    int status = 0;
    int colNum;
    long nbRows;
    int nullvalue = 0;
    int anynull;

    logTrace("oidataFITS::ReadColumn()");

    // Go onto the right HDU
    fits_movabs_hdu(_oiFile, hduIndex, NULL, &status);
    if(status)
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // getcolNum from given colName
    if(fits_get_colnum
       (_oiFile, CASEINSEN, (char *) colName, &colNum, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // Get number of rows
    if(fits_get_num_rows(_oiFile, &nbRows, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // @todo assert nbElements == nbRows*n  from nTYPE of TFORMcolIdx
    // using 
    // int fits_get_bcolparms / ffgbcl
    //  (fitsfile *fptr, int colnum, > char *ttype, char *tunit,
    //   char *typechar, long *repeat, double *scale, double *zero,
    //   long *nulval, char *tdisp, int  *status)


    if(nbElements != nbRows)
    {
        logWarning("##### nbElements could be wrong (%d, must be %ld) \n",
                   nbElements, nbRows);
    }


    // fill array 
    if(fits_read_col(_oiFile, TLOGICAL, colNum, rowIdx, 1, nbElements,
                     &nullvalue, array, &anynull, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    return mcsSUCCESS;
}

/**
 * Read a character (A) data column.
 * 
 * @param colName name of the column 
 * @param hduIndex hdu index of the requested table 
 * @param nbElements number of elements to read
 * @param rowIdx start copy from given rowIndex (firstrow index is 1)
 * @param array user allocaded array where data will be written
 * 
 * @return mcsSUCCESS on successful completion. Otherwise mcsFAILURE is
 * returned 
 *
 * Null Value fields get 'NoValue'
 */
mcsCOMPL_STAT
    oidataFITS::ReadColumn(const mcsSTRING32 colName,
                           mcsINT16 hduIndex,
                           mcsINT16 nbElements, mcsINT16 rowIdx, char **array)
{
    int status = 0;
    int colNum;
    long nbRows;
    char *nullvalue = "NoValue";
    int anynull;

    logTrace("oidataFITS::ReadColumn()");

    // Go onto the right HDU
    fits_movabs_hdu(_oiFile, hduIndex, NULL, &status);
    if(status)
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // getcolNum from given colName
    if(fits_get_colnum
       (_oiFile, CASEINSEN, (char *) colName, &colNum, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // Get number of rows
    if(fits_get_num_rows(_oiFile, &nbRows, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // @todo assert nbElements == nbRows and add error in stack
    if(nbElements != nbRows)
    {
        logWarning("##### nbElements could be wrong (%d, must be %ld) \n",
                   nbElements, nbRows);
    }

    // fill array 
    if(fits_read_col(_oiFile, TSTRING, colNum, 1, 1, nbElements,
                     &nullvalue, array, &anynull, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    return mcsSUCCESS;
}

/** 
 *  Read some column information from the fits file.
 *
 * @param colName  name of the column  
 * @param hduIndex hdu index of the requested table 
 * @param type data type for column elements 
 * @param repeat vector repeat value
 * @param width width in bytes of a column in an ASCII or binary table
 *
 *  @returns an MCS completion status code (SUCCESS or FAILURE) 
 */
mcsCOMPL_STAT oidataFITS::ReadColumnInfo(const mcsSTRING32 colName,
                                         mcsINT16 hduIndex, 
                                         oidataDataType *type,
                                         long *repeat, long *width,
                                         oidataDataUnit * unit)
{
    int status = 0;
    int colNum = 0;

    logTrace("oidataFITS::ReadColumnInfo()");

    // Go onto the right HDU
    fits_movabs_hdu(_oiFile, hduIndex, NULL, &status);
    if(status)
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // Search dimension and type of column into the oifits TFORM field

    // getcolNum from given colName
    if(fits_get_colnum
       (_oiFile, CASEINSEN, (char *) colName, &colNum, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // identify type of data
    int typecode;
    if(fits_get_coltype(_oiFile, colNum, &typecode, repeat, width, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }
    switch(typecode)
    {
        case TSHORT:
            *type=typeInteger;
            break;
        case TSTRING:
            *type=typeCharacter;
            break;
        case  TFLOAT:
            *type = typeFloat;
            break;
        case TDOUBLE:
            *type = typeDouble;
            break;
        case TLOGICAL:
            *type = typeLogical;
            break;
        default:
            logDebug("type=%d", typecode);
            errAdd(oidataERR_GENERIC,
                   "unsupported column type");
            return mcsFAILURE;
    }

    // some function params could be replaced by NULL values
    char ttype[80];
    char tunit[80];
    char typechar[80];
    double scale;
    double zero;
    long nulval;
    char tdisp[80];

    if(fits_get_bcolparms(_oiFile, colNum, ttype, tunit, typechar,
                          repeat, &scale, &zero, &nulval, tdisp, &status))
    {
        oidataAddFitsError(status, __FILE_LINE__, NULL);
        return mcsFAILURE;
    }

    // Check for     noUnit,
    if(strlen(tunit) == 0)
    {
        *unit = noUnit;
    }
    // Check for     unitInMeters,
    else if((strcasecmp(tunit, "m") == 0) ||
            (strcasecmp(tunit, "meters") == 0))
    {
        *unit = unitInMeters;
    }
    // Check for     unitInDegrees,
    else if((strcasecmp(tunit, "deg") == 0) ||
            (strcasecmp(tunit, "degrees") == 0))
    {
        *unit = unitInDegrees;
    }
    // Check for     unitInSeconds,
    else if((strcasecmp(tunit, "s") == 0) ||
            (strcasecmp(tunit, "sec") == 0) ||
            (strcasecmp(tunit, "seconds") == 0))
    {
        *unit = unitInSeconds;
    }
    // Check for     unitInMJD,
    else if((strcasecmp(tunit, "day") == 0))
    {
        *unit = unitInMJD;
    }
    // Check for     unitInYears,
    else if((strcasecmp(tunit, "yr") == 0) ||
            (strcasecmp(tunit, "year") == 0) ||
            (strcasecmp(tunit, "years") == 0))
    {
        *unit = unitInYears;
    }
    // Check for     unitInMetersPerSecond,
    else if((strcasecmp(tunit, "m/s") == 0) ||
            (strcasecmp(tunit, "m / s") == 0) ||
            (strcasecmp(tunit, "meters per second") == 0) ||
            (strcasecmp(tunit, "meters/second") == 0) ||
            (strcasecmp(tunit, "meters / second") == 0))
    {
        *unit = unitInMetersPerSecond;
    }
    // Check for     unitInDegreesPerYear
    else if((strcasecmp(tunit, "deg/yr") == 0) ||
            (strcasecmp(tunit, "deg/year") == 0) ||
            (strcasecmp(tunit, "deg / year") == 0) ||
            (strcasecmp(tunit, "deg / yr") == 0))
    {
        *unit = unitInDegreesPerYear;
    }

    return mcsSUCCESS;
}

/*___oOo___*/
