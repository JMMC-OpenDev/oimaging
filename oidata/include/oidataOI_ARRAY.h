#ifndef oidataOI_ARRAY_H
#define oidataOI_ARRAY_H

/*******************************************************************************
* JMMC project 
*
* "@(#) $Id: oidataOI_ARRAY.h,v 1.3 2005-09-21 09:54:56 mella Exp $"
* History
* -------
* $Log: not supported by cvs2svn $
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
 * Declaration of oidataOI_ARRAY class. 
 */

#ifndef __cplusplus
#error This is a C++ include file and cannot be used from plain C
#endif

#include "oidataOI_TABLE.h"

/**
 * 
 */
class oidataOI_ARRAY:public oidataOI_TABLE
{
  public:
    // Class constructor 
    oidataOI_ARRAY(oidataFITS * fitsParent,
                   mcsSTRING32 extName, mcsINT16 hduIndex);
    // Class destructor 
    virtual ~ oidataOI_ARRAY();
    
    virtual mcsCOMPL_STAT CheckStructure();
    virtual mcsCOMPL_STAT GetArrName(mcsSTRING32 arrName);
    virtual mcsCOMPL_STAT GetFrame(mcsSTRING32 frame);
    virtual mcsCOMPL_STAT GetArrayX(mcsDOUBLE * arrayX);
    virtual mcsCOMPL_STAT GetArrayY(mcsDOUBLE * arrayY);
    virtual mcsCOMPL_STAT GetArrayZ(mcsDOUBLE * arrayZ);
    virtual mcsCOMPL_STAT GetTelName(mcsSTRING16 * telName);
    virtual mcsCOMPL_STAT GetStaName(mcsSTRING16 * staName);
    virtual mcsCOMPL_STAT GetStaIndex(mcsINT16 * staIndex);
    virtual mcsCOMPL_STAT GetDiameter(mcsFLOAT * diameter);
    virtual mcsCOMPL_STAT GetStaXYZ(mcsDOUBLE * staXYZ);
    virtual mcsINT16 GetNumberOfElements();

  protected:

  private:
    // Declaration of copy constructor and assignment operator as private
    // methods, in order to hide them from the users.

      oidataOI_ARRAY(const oidataOI_ARRAY &);
      oidataOI_ARRAY & operator=(const oidataOI_ARRAY &);

    mcsINT16 _nbOfElements;
};

#endif /* _oidataOI_ARRAY_H */
