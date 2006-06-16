#ifndef oidataOI_VIS2_H
#define oidataOI_VIS2_H

/*******************************************************************************
* JMMC project 
*
* "@(#) $Id: oidataOI_VIS2.h,v 1.3 2005-09-21 09:54:56 mella Exp $"
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
 * Declaration of oidataOI_VIS2 class. 
 */

#ifndef __cplusplus
#error This is a C++ include file and cannot be used from plain C
#endif

#include "oidataDATA_TABLE.h"

/**
 * 
 */
class oidataOI_VIS2:public oidataDATA_TABLE
{
  public:
    // Class constructor 
    oidataOI_VIS2(oidataFITS * fitsParent,
                  mcsSTRING32 extName, mcsINT16 hduIndex);
    // Class destructor 
    virtual ~ oidataOI_VIS2();

    virtual mcsCOMPL_STAT CheckStructure();
    virtual mcsCOMPL_STAT GetVis2Data(mcsDOUBLE * vis2Data);
    virtual mcsCOMPL_STAT GetVis2Err(mcsDOUBLE * vis2Err);
    virtual mcsCOMPL_STAT GetUCoord(mcsDOUBLE * uCoord);
    virtual mcsCOMPL_STAT GetVCoord(mcsDOUBLE * vCoord);

  protected:

  private:
    // Declaration of copy constructor and assignment operator as private
    // methods, in order to hide them from the users.

      oidataOI_VIS2(const oidataOI_VIS2 &);
      oidataOI_VIS2 & operator=(const oidataOI_VIS2 &);

};

#endif /* _oidataOI_VIS2_H */
