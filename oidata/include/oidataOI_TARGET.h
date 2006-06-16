#ifndef oidataOI_TARGET_H
#define oidataOI_TARGET_H

/*******************************************************************************
* JMMC project 
*
* "@(#) $Id: oidataOI_TARGET.h,v 1.3 2005-09-21 09:54:56 mella Exp $"
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
 * Declaration of oidataOI_TARGET class. 
 */

#ifndef __cplusplus
#error This is a C++ include file and cannot be used from plain C
#endif

#include "oidataOI_TABLE.h"

/**
 * 
 */
class oidataOI_TARGET:public oidataOI_TABLE
{
  public:
    // Class constructor 
    oidataOI_TARGET(oidataFITS * fitsParent,
                    mcsSTRING32 extName, mcsINT16 hduIndex);
    // Class destructor 
    virtual ~ oidataOI_TARGET();

    mcsCOMPL_STAT CheckStructure();

    virtual mcsCOMPL_STAT GetTargetId(mcsINT16 * targetId);
    virtual mcsCOMPL_STAT GetTarget(mcsSTRING16 * target);
    virtual mcsCOMPL_STAT GetRaEp0(mcsDOUBLE * raEp0);
    virtual mcsCOMPL_STAT GetDecEp0(mcsDOUBLE * decEp0);
    virtual mcsCOMPL_STAT GetEquinox(mcsFLOAT * equinox);
    virtual mcsCOMPL_STAT GetRaErr(mcsDOUBLE * raErr);
    virtual mcsCOMPL_STAT GetDecErr(mcsDOUBLE * decErr);
    virtual mcsCOMPL_STAT GetSysVel(mcsDOUBLE * sysVel);
    virtual mcsCOMPL_STAT GetVelTyp(mcsSTRING8 * velTyp);
    virtual mcsCOMPL_STAT GetVelDef(mcsSTRING8 * velDef);
    virtual mcsCOMPL_STAT GetPMRa(mcsDOUBLE * pmRa);
    virtual mcsCOMPL_STAT GetPMDec(mcsDOUBLE * pmDec);
    virtual mcsCOMPL_STAT GetPMraErr(mcsDOUBLE * pmRaErr);
    virtual mcsCOMPL_STAT GetPMDecErr(mcsDOUBLE * pmdecErr);
    virtual mcsCOMPL_STAT GetParallax(mcsFLOAT * parallax);
    virtual mcsCOMPL_STAT GetParaErr(mcsFLOAT * paraErr);
    virtual mcsCOMPL_STAT GetSpecTyp(mcsSTRING16 * specTyp);


  protected:

  private:
    // Declaration of copy constructor and assignment operator as private
    // methods, in order to hide them from the users.

      oidataOI_TARGET(const oidataOI_TARGET &);
      oidataOI_TARGET & operator=(const oidataOI_TARGET &);

};

#endif /* _oidataOI_TARGET_H */
