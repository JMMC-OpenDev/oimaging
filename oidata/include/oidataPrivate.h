#ifndef oidataPrivate_H
#define oidataPrivate_H
/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: oidataPrivate.h,v 1.6 2005-09-23 09:32:28 mella Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.5  2005/09/21 09:54:56  mella
 * Reindented code
 *
 * Revision 1.4  2005/09/15 09:26:57  mella
 * Add Extname parameter to constructors
 *
 * Revision 1.3  2005/08/31 20:11:09  mella
 * Add a temporary define for normal number of table
 *
 * Revision 1.2  2005/06/24 14:01:45  mella
 * Add minimum material
 *
 ******************************************************************************/

/**
 * \file
 * oidata private header file.
 */
#ifdef __cplusplus
extern "C"
{
#endif

    
    
#include "oidataErrors.h"
/*
 * Constants definition
 */

/* Module name */
#define MODULE_ID "oidata"

/* Indicates how many tables should be present into the normal oidata fits file 
 * (primary hdu is taken into account) 
 */
#define oidataREQUESTED_NUMBER_OF_OI_TABLE 7

    void oidataAddFitsError(int status, const char *fileLine,
                            const char *info);

    
      
#ifdef __cplusplus
}
#endif


#endif                          /*!oidataPrivate_H */

/*___oOo___*/
