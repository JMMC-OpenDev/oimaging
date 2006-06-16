#ifndef oidataOI_TABLE_H
#define oidataOI_TABLE_H

/*******************************************************************************
 * JMMC project 
 *
 * "@(#) $Id: oidataOI_TABLE.h,v 1.8 2006-02-02 08:42:08 mella Exp $"
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.7  2005/10/06 09:33:18  mella
 * Add Type enum and some documentation
 *
 * Revision 1.6  2005/09/26 12:59:44  mella
 * Add CheckColumns specialisation according nwave check
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
 * Revision 1.2  2005/09/15 09:26:57  mella
 * Add Extname parameter to constructors
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
 * Declaration of oidataOI_TABLE class. 
 */

#ifndef __cplusplus
#error This is a C++ include file and cannot be used from plain C
#endif

//class oidataFITS;
#include "oidataFITS.h"

/**
 * Use to get type of OI_TABLE.
 */
typedef enum
{
    UNKNOWN_OI_TYPE = 0,
    OI_TARGET_TYPE,
    OI_ARRAY_TYPE,
    OI_WAVELENGTH_TYPE,
    OI_VIS_TYPE,
    OI_VIS2_TYPE,
    OI_T3_TYPE
} oidataTABLE_TYPE;


/**
 * 
 */
class oidataOI_TABLE
{
  public:
    // Class constructor 
    oidataOI_TABLE(oidataFITS * fitsParent,
                   mcsSTRING32 extName, mcsINT16 hduIndex);
    // Class destructor 
    virtual ~ oidataOI_TABLE();

    // most of following methods aren't virtual because today they must
    // not be overloaded

    // Utility functions
    oidataTABLE_TYPE GetType();
    virtual mcsCOMPL_STAT CheckStructure();

    // Information functions
    mcsINT16 GetNumberOfRows();
    mcsCOMPL_STAT GetExtname(mcsSTRING32 extname);
    mcsCOMPL_STAT GetOiRevn(mcsINT16 * oiRevn);
    mcsCOMPL_STAT GetHduIndex(mcsINT16 * hduIndex);
    mcsCOMPL_STAT GetKeyword(const mcsSTRING32 keyName, mcsINT16 * keyValue);
    mcsCOMPL_STAT GetKeyword(const mcsSTRING32 keyName, mcsFLOAT * keyValue);
    mcsCOMPL_STAT GetKeyword(const mcsSTRING32 keyName, mcsDOUBLE * keyValue);
    mcsCOMPL_STAT GetKeyword(const mcsSTRING32 keyName,
                             mcsLOGICAL * keyValue);
    mcsCOMPL_STAT GetKeyword(const mcsSTRING32 keyName, mcsSTRING32 keyValue);
    mcsCOMPL_STAT ReadColumn(const mcsSTRING32 colName, mcsINT16 nbElements,
                             mcsINT16 rowIdx, mcsINT16 * array);
    mcsCOMPL_STAT ReadColumn(const mcsSTRING32 colName, mcsINT16 nbElements,
                             mcsINT16 rowIdx, mcsFLOAT * array);
    mcsCOMPL_STAT ReadColumn(const mcsSTRING32 colName, mcsINT16 nbElements,
                             mcsINT16 rowIdx, mcsDOUBLE * array);
    mcsCOMPL_STAT ReadColumn(const mcsSTRING32 colName, mcsINT16 nbElements,
                             mcsINT16 rowIdx, char *array);
    mcsCOMPL_STAT ReadColumn(const mcsSTRING32 colName, mcsINT16 nbElements,
                             mcsINT16 rowIdx, char **array);
    mcsCOMPL_STAT ReadColumnInfo(const mcsSTRING32 colName, 
                                 oidataDataType *type,
                                 long *repeat, long * width,
                                 oidataDataUnit *unit);

  protected:
    virtual mcsCOMPL_STAT CheckColumns( oidataColumnDefinition *columns);
    mcsCOMPL_STAT CheckKeywords( oidataKeywordDefinition *keywords);

    /* This attribute must be changed into inherented classes.
     *  It's actually done into the constructors.
     */
    oidataTABLE_TYPE _tableType;
                                 /**< Enumerate giving type of Table */

    // @todo verify if mcsINT16 is big enough to support row Length
    mcsINT16 _naxis2;      /**< number of rows per column */

    oidataFITS *_fitsParent;      /**< reference onto the fits file class */
    mcsINT16 _hduIndex; /**< hdu index into the associated fits file */
    mcsSTRING32 _extName; /**< hdu extension name */
  private:
    // Declaration of copy constructor and assignment operator as private
    // methods, in order to hide them from the users.

      oidataOI_TABLE(const oidataOI_TABLE &);
      oidataOI_TABLE & operator=(const oidataOI_TABLE &);

};

#endif /* _oidataOI_TABLE_H */
