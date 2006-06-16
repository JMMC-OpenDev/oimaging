/*******************************************************************************
 * JMMC project
 * 
 * "@(#) $Id: oidataAddFitsError_L.c,v 1.2 2006-05-11 13:04:56 mella Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2005/06/24 14:07:10  mella
 * First revision
 *
 ******************************************************************************/

/**
 * \file
 * Transform cfitisio error stack into the err management system.
 */

static char *rcsId __attribute__ ((unused)) ="@(#) $Id: oidataAddFitsError_L.c,v 1.2 2006-05-11 13:04:56 mella Exp $";

/* 
 * System Headers
 */
#include <stdio.h>
#include <string.h>
#include <fitsio.h>


/*
 * MCS Headers 
 */
#include "mcs.h"
#include "log.h"
#include "err.h"


/* 
 * Local Headers
 */
#include "oidataPrivate.h"
#include "oidataErrors.h"


/*
 * Local Variables
 */



/*
 * Local Functions declaration
 */



/* 
 * Local functions definition
 */



/*
 * Public functions definition
 */

/**
 * Copy cfitsio erro stack into mcs one...
 *
 * @param fileLine fileLine.
 */
void oidataAddFitsError(int status, const char * fileLine, const char * info)
{
    mcsLOGICAL cfitsioMsg = mcsFALSE;
    
    logTrace("oidataAddFitsError()");

    mcsSTRING256 errtext="";
    if ((info!=NULL) && (strlen (info)!=0))
    {
        sprintf((char*)errtext,"Error with:%.80s",info);
        errAddInStack(MODULE_ID, __FILE_LINE__, oidataERR_GENERIC ,
                      mcsFALSE, (char*)errtext);
    }
    errtext[0]=0;
                  
    while (fits_read_errmsg((char*)errtext)!=0)
    {
        errAddInStack(MODULE_ID, fileLine, oidataERR_FITS, mcsFALSE,
                      (char*)errtext );
        cfitsioMsg = mcsTRUE;
    }

    if(cfitsioMsg == mcsFALSE){
        fits_get_errstatus(status, (char*)errtext);
        errAddInStack(MODULE_ID, fileLine, oidataERR_FITS, mcsFALSE,
                      (char*)errtext );
    }

    /* clear cfitsio error stack */
    fits_clear_errmsg();
}


/*___oOo___*/
