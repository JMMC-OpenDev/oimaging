#ifndef oidataOI_T3_H
#define oidataOI_T3_H

/*******************************************************************************
* JMMC project 
*
* "@(#) $Id: oidataOI_T3.h,v 1.3 2005-09-21 09:54:56 mella Exp $"
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
 * Declaration of oidataOI_T3 class. 
 */

#ifndef __cplusplus
#error This is a C++ include file and cannot be used from plain C
#endif

#include "oidataDATA_TABLE.h"

/**
 * 
 */
class oidataOI_T3:public oidataDATA_TABLE
{
  public:
    // Class constructor 
    oidataOI_T3(oidataFITS * fitsParent,
                mcsSTRING32 extName, mcsINT16 hduIndex);
    // Class destructor 
    virtual ~ oidataOI_T3();

    virtual mcsCOMPL_STAT CheckStructure();
    virtual mcsCOMPL_STAT GetT3Amp(mcsDOUBLE * t3Amp);
    virtual mcsCOMPL_STAT GetT3AmpErr(mcsDOUBLE * t3AmpErr);
    virtual mcsCOMPL_STAT GetT3Phi(mcsDOUBLE * t3Phi);
    virtual mcsCOMPL_STAT GetT3PhiErr(mcsDOUBLE * t3PhiErr);
    virtual mcsCOMPL_STAT GetU1Coord(mcsDOUBLE * u1Coord);
    virtual mcsCOMPL_STAT GetV1Coord(mcsDOUBLE * v1Coord);
    virtual mcsCOMPL_STAT GetU2Coord(mcsDOUBLE * u2Coord);
    virtual mcsCOMPL_STAT GetV2Coord(mcsDOUBLE * v2Coord);


  protected:

  private:
    // Declaration of copy constructor and assignment operator as private
    // methods, in order to hide them from the users.

      oidataOI_T3(const oidataOI_T3 &);
      oidataOI_T3 & operator=(const oidataOI_T3 &);

};

#endif /* _oidataOI_T3_H */
