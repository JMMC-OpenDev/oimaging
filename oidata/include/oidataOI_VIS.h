#ifndef oidataOI_VIS_H
#define oidataOI_VIS_H

/*******************************************************************************
* JMMC project 
*
* "@(#) $Id: oidataOI_VIS.h,v 1.3 2005-09-21 09:54:56 mella Exp $"
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
 * Declaration of oidataOI_VIS class. 
 */

#ifndef __cplusplus
#error This is a C++ include file and cannot be used from plain C
#endif

#include "oidataDATA_TABLE.h"

/**
 * 
 */
class oidataOI_VIS:public oidataDATA_TABLE
{
  public:
    // Class constructor 
    oidataOI_VIS(oidataFITS * fitsParent,
                 mcsSTRING32 extName, mcsINT16 hduIndex);
    // Class destructor 
    virtual ~ oidataOI_VIS();

    virtual mcsCOMPL_STAT CheckStructure();
    virtual mcsCOMPL_STAT GetVisAmp(mcsDOUBLE * visAmp);
    virtual mcsCOMPL_STAT GetVisAmpErr(mcsDOUBLE * visAmpErr);
    virtual mcsCOMPL_STAT GetVisPhi(mcsDOUBLE * visPhi);
    virtual mcsCOMPL_STAT GetVisPhiErr(mcsDOUBLE * visPhiErr);
    virtual mcsCOMPL_STAT GetUCoord(mcsDOUBLE * uCoord);
    virtual mcsCOMPL_STAT GetVCoord(mcsDOUBLE * vCoord);


  protected:

  private:
    // Declaration of copy constructor and assignment operator as private
    // methods, in order to hide them from the users.

      oidataOI_VIS(const oidataOI_VIS &);
      oidataOI_VIS & operator=(const oidataOI_VIS &);

};

#endif /* _oidataOI_VIS_H */
