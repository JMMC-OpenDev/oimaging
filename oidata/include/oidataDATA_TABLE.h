#ifndef oidataDATA_TABLE_H
#define oidataDATA_TABLE_H

/*******************************************************************************
* JMMC project 
*
* "@(#) $Id: oidataDATA_TABLE.h,v 1.4 2005-09-26 12:59:44 mella Exp $"
* History
* -------
* $Log: not supported by cvs2svn $
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
 * Declaration of oidataDATA_TABLE class. 
 */

#ifndef __cplusplus
#error This is a C++ include file and cannot be used from plain C
#endif

#include "oidataOI_TABLE.h"

class oidataOI_WAVELENGTH;
class oidataOI_ARRAY;

/**
 * This class must not be instanciated by final user. 
 * Prefer one of oidataOI_VIS, oidataOI_VIS2 or oidataOI_T3
 */
class oidataDATA_TABLE:public oidataOI_TABLE
{
  public:
    // Class constructor 
    oidataDATA_TABLE(oidataFITS * fitsParent,
                     mcsSTRING32 extName, mcsINT16 hduIndex);
    // Class destructor 
    virtual ~ oidataDATA_TABLE();

    // Utility functions
    virtual mcsCOMPL_STAT GetOiWavelengthTable(oidataOI_WAVELENGTH ** table);
    virtual mcsCOMPL_STAT GetOiArrayTable(oidataOI_ARRAY ** table);
    virtual mcsINT16 GetNWave();
    virtual mcsCOMPL_STAT ProposeReference(oidataOI_TABLE * table);

    // Fits related functions 
    virtual mcsCOMPL_STAT GetDateObs(mcsSTRING32 dateObs);
    virtual mcsCOMPL_STAT GetArrName(mcsSTRING32 arrName);
    virtual mcsCOMPL_STAT GetInsName(mcsSTRING32 insName);
    virtual mcsCOMPL_STAT GetTargetId(mcsINT16 * targetId);
    virtual mcsCOMPL_STAT GetTime(mcsDOUBLE * time);
    virtual mcsCOMPL_STAT GetMJD(mcsDOUBLE * mjd);
    virtual mcsCOMPL_STAT GetIntTime(mcsDOUBLE * intTime);
    virtual mcsCOMPL_STAT GetStaIndex(mcsINT16 * staIndex);
    virtual mcsCOMPL_STAT GetFlag(char *flag);


  protected:
    virtual mcsCOMPL_STAT CheckColumns( oidataColumnDefinition *columns);
    
    /** Wavelength channels of the oidataOI_WAVELENGTH table */
    mcsINT16 _nwave;

        /** Station number contributing to the data
     * It must be modified in the constructor of inherited classes 
     */
    mcsINT16 _nbOfStations;

  private:
    // Declaration of copy constructor and assignment operator as private
    // methods, in order to hide them from the users.

      oidataDATA_TABLE(const oidataDATA_TABLE &);
      oidataDATA_TABLE & operator=(const oidataDATA_TABLE &);

    // Reference onto the oi_array and oi_wavelength
    oidataOI_ARRAY *_oiArray;
    oidataOI_WAVELENGTH *_oiWavelength;

};

#endif /* _oidataDATA_TABLE_H */
