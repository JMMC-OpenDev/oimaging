#ifndef oidataFITS_H
#define oidataFITS_H

/*******************************************************************************
 * JMMC project 
 *
 * "@(#) $Id: oidataFITS.h,v 1.7 2005-10-06 09:33:18 mella Exp $"
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.6  2005/09/27 11:53:03  mella
 * Add units for DegreesPer year
 *
 * Revision 1.5  2005/09/23 09:32:28  mella
 * Add unit check for columns
 *
 * Revision 1.4  2005/09/22 07:26:54  mella
 * Add method to get more column informations
 *
 * Revision 1.3  2005/09/21 09:54:56  mella
 * Reindented code
 *
 * Revision 1.2  2005/08/31 20:11:32  mella
 * Implement Load with configurable checks
 *
 * Revision 1.1  2005/08/19 12:56:11  mella
 * First revision
 * 
 *
 *
 *
 *******************************************************************************/
/** 
 * \file
 * Declaration of oidataFITS class. 
 */

#ifndef __cplusplus
#error This is a C++ include file and cannot be used from plain C
#endif

/* used for CFitsio */
#include "fitsio.h"

#include <vector>



/**
 * Used to indicate column or keyword unit
 */
typedef enum {
    noUnit=0,               /**< no Unit acssociated */
    unitInMeters,           /**< must be 'm' or 'meters'*/
    unitInDegrees,          /**< must be 'deg' or 'degrees'*/
    unitInSeconds,          /**< must be 's', 'sec' or 'seconds'*/
    unitInMJD,              /**< must be 'day'*/
    unitInYears,            /**< must be 'yr', 'year' or 'years'*/
    unitInMetersPerSecond,  /**< must be 'm/s', 'm / s', 'meters per second',
                             'meters/second', 'meters / second' */
    unitInDegreesPerYear    /**< must be 'deg/yr', 'deg/year', 'deg / year'
                              or 'deg / yr'*/
} oidataDataUnit;

/**
 * Used to indicate column or keyword type
 */
typedef enum {
   typeInteger,         /**< equ. to fits I */ 
   typeCharacter,       /**< equ. to fits A */
   typeFloat,           /**< equ. to fits E */
   typeDouble,          /**< equ. to fits D */
   typeLogical          /**< equ. to fits L */
} oidataDataType;



/**
 * Used to perform columns tests during load.
 * Would be better to be placed in private but cause some problems...
 */
typedef struct {
    char * name;
    oidataDataType type;
    long multiplier;
    oidataDataUnit unit;
    char * comment;
}oidataColumnDefinition;

/**
 * Used to perform keyword tests during load
 * Would be better to be placed in private but cause some problems...
 */
typedef struct {
    char * name;
    oidataDataType type;
    long multiplier;
    oidataDataUnit unit;
    char * comment;
}oidataKeywordDefinition;


class oidataOI_TABLE;
class oidataOI_TARGET;
/*
#include "oidataOI_TARGET.h"
#include "oidataOI_TABLE.h"
*/
/**
 *  Main class to handle oidata.
 *
 *  Note:
 *  Load try to make some assert which where placed into the oi_data.i file of
 *  the mfityorick module.
 *  # oi_data.i:v1_1:lineNumber give the link
 *
 *  \todo implement the not understanded check done in oi_data.i:v1_1:455
 */
class oidataFITS
{
  public:
    // Class constructor 
    oidataFITS();
    // Class destructor 
    virtual ~ oidataFITS();

    virtual mcsCOMPL_STAT Load(const mcsSTRING32 filename,
                               const mcsLOGICAL stopForBadFormat = mcsTRUE);
    virtual mcsCOMPL_STAT Save(const mcsSTRING32 filename,
                               const mcsLOGICAL overwrite = mcsFALSE);
    virtual mcsCOMPL_STAT GetOiTarget(oidataOI_TARGET ** oiTarget);
    virtual mcsCOMPL_STAT GetNumberOfOiTable(mcsINT16 * nbOfTable);
    virtual mcsCOMPL_STAT GetOiTable(oidataOI_TABLE ** table, mcsINT16 index);

    virtual mcsCOMPL_STAT GetKeyword(const mcsSTRING32 keyName,
                                     mcsINT16 * keyValue, mcsINT16 hduIndex);
    virtual mcsCOMPL_STAT GetKeyword(const mcsSTRING32 keyName,
                                     mcsFLOAT * keyValue, mcsINT16 hduIndex);
    virtual mcsCOMPL_STAT GetKeyword(const mcsSTRING32 keyName,
                                     mcsDOUBLE * keyValue, mcsINT16 hduIndex);
    virtual mcsCOMPL_STAT GetKeyword(const mcsSTRING32 keyName,
                                     mcsLOGICAL * keyValue,
                                     mcsINT16 hduIndex);
    virtual mcsCOMPL_STAT GetKeyword(const mcsSTRING32 keyName,
                                     mcsSTRING32 keyValue, mcsINT16 hduIndex);

    virtual mcsCOMPL_STAT oidataFITS::ReadColumn(const mcsSTRING32 colName,
                                                 mcsINT16 hduIndex,
                                                 mcsINT16 nbElements,
                                                 mcsINT16 rowIdx,
                                                 mcsINT16 * array);

    virtual mcsCOMPL_STAT oidataFITS::ReadColumn(const mcsSTRING32 colName,
                                                 mcsINT16 hduIndex,
                                                 mcsINT16 nbElements,
                                                 mcsINT16 rowIdx,
                                                 mcsFLOAT * array);

    virtual mcsCOMPL_STAT oidataFITS::ReadColumn(const mcsSTRING32 colName,
                                                 mcsINT16 hduIndex,
                                                 mcsINT16 nbElements,
                                                 mcsINT16 rowIdx,
                                                 mcsDOUBLE * array);
    virtual mcsCOMPL_STAT oidataFITS::ReadColumn(const mcsSTRING32 colName,
                                                 mcsINT16 hduIndex,
                                                 mcsINT16 nbElements,
                                                 mcsINT16 rowIdx,
                                                 char *array);

    virtual mcsCOMPL_STAT oidataFITS::ReadColumn(const mcsSTRING32 colName,
                                                 mcsINT16 hduIndex,
                                                 mcsINT16 nbElements,
                                                 mcsINT16 rowIdx,
                                                 char **array);
    
    virtual mcsCOMPL_STAT oidataFITS::ReadColumnInfo(const mcsSTRING32 colName,
                                                 mcsINT16 hduIndex,
                                                 oidataDataType *type,
                                                 long *repeat,
                                                 long *width,
                                                 oidataDataUnit *unit);
  
  protected:

  private:
    // Declaration of copy constructor and assignment operator as private
    // methods, in order to hide them from the users.

      oidataFITS(const oidataFITS &);
      oidataFITS & operator=(const oidataFITS &);

    mcsCOMPL_STAT MakeCrossReferences();
    mcsCOMPL_STAT CheckIntegrity();
    mcsCOMPL_STAT FactorOiTable(mcsSTRING32 extname,
                                mcsINT16 hduIndex,
                                oidataOI_TABLE ** newTable);

    fitsfile *_oiFile;

        /** flag that indicates if one fits file has been loaded */
    mcsLOGICAL _fitsFileLoaded;

        /** vector of oi tables */
      std::vector < oidataOI_TABLE * >_oiTables;

        /** reference onto the first associated OI_TARGET */
    oidataOI_TARGET *_oiTarget;

};

#endif /* _oidataFITS_H */
