#ifndef oidata_H
#define oidata_H
/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: oidata.h,v 1.6 2005-09-23 09:32:28 mella Exp $"
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
 * Revision 1.3  2005/06/28 13:28:27  mella
 * Reorder some includes
 *
 * Revision 1.2  2005/06/24 14:01:45  mella
 * Add minimum material
 *
 ******************************************************************************/

/**
 * \file
 * oidata general header file.
 */


/*
 * Local headers
 */
#include "mcs.h"



#include "oidataFITS.h"
#include "oidataOI_TABLE.h"
#include "oidataOI_ARRAY.h"
#include "oidataOI_TARGET.h"
#include "oidataDATA_TABLE.h"
#include "oidataOI_VIS.h"
#include "oidataOI_VIS2.h"
#include "oidataOI_T3.h"
#include "oidataOI_WAVELENGTH.h"

/**
 * Define actual supported version of oidata revision 
 */
#define oidataACTUAL_REVISION 1


#endif /*!oidata_H */

/*___oOo___*/
