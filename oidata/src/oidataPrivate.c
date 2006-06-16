/*******************************************************************************
 * JMMC project
 * 
 * "@(#) $Id: oidataPrivate.c,v 1.2 2006-05-11 13:04:56 mella Exp $"
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
 *  define some utility functions 
 */

static char *rcsId __attribute__ ((unused)) ="@(#) $Id: oidataPrivate.c,v 1.2 2006-05-11 13:04:56 mella Exp $";

/* 
 * System Headers
 */
#include <stdio.h>


/*
 * MCS Headers 
 */
#include "mcs.h"
#include "log.h"
#include "err.h"


/* 
 * Local Headers
 */
#include "oidata.h"
#include "oidataPrivate.h"


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

void oidataAddFitsErr(int status , char* fileline,char * info)
{
   mcsSTRING256 errtext="";
   if ((info!=NULL) && (strlen (info)!=0))
   {
       sprintf((char*)errtext,"Error with:%.80s",info);
       errAdd( oidataERR_FITS, (char*)errtext);
   }
   strcpy((char*)errtext,"");
   //report all cfitsio error into the stack
   while (fits_read_errmsg((char*)errtext)!=0)
   {
       errAdd(fileline,(char*)errtext);
   }

   //clear cfitsio error stacks
   fits_clear_errmsg();
} 


/*___oOo___*/
