#ifndef oidataOI_WAVELENGTH_H
#define oidataOI_WAVELENGTH_H

/*******************************************************************************
* JMMC project 
*
* "@(#) $Id: oidataOI_WAVELENGTH.h,v 1.3 2005-09-21 09:54:56 mella Exp $"
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
 * Declaration of oidataOI_WAVELENGTH class. 
 */

#ifndef __cplusplus
#error This is a C++ include file and cannot be used from plain C
#endif

#include "oidataOI_TABLE.h"

/**
 * 
 */
class oidataOI_WAVELENGTH:public oidataOI_TABLE
{
  public:
    // Class constructor 
    oidataOI_WAVELENGTH(oidataFITS * fitsParent,
                        mcsSTRING32 extName, mcsINT16 hduIndex);
    // Class destructor 
    virtual ~ oidataOI_WAVELENGTH();

    virtual mcsCOMPL_STAT CheckStructure();
    virtual mcsCOMPL_STAT GetInsName(mcsSTRING32 insname);
    virtual mcsCOMPL_STAT GetEffWave(mcsFLOAT * effWave);
    virtual mcsCOMPL_STAT GetEffBand(mcsFLOAT * effBand);


  protected:

  private:
    // Declaration of copy constructor and assignment operator as private
    // methods, in order to hide them from the users.

      oidataOI_WAVELENGTH(const oidataOI_WAVELENGTH &);
      oidataOI_WAVELENGTH & operator=(const oidataOI_WAVELENGTH &);

};

#endif /* _oidataOI_WAVELENGTH_H */
