/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: oidataTest2.cpp,v 1.3 2006-05-11 13:04:56 mella Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2005/09/22 07:20:04  mella
 * Program reindented
 *
 * Revision 1.1  2005/06/24 14:07:12  mella
 * First revision
 *
 ******************************************************************************/

/**
 * \file
 *  used to make some cfitsio tests * 
 */

    "@(#) $Id: oidataTest2.cpp,v 1.3 2006-05-11 13:04:56 mella Exp $";
static char *rcsId __attribute__ ((unused)) ="@(#) $Id: oidataTest2.cpp,v 1.3 2006-05-11 13:04:56 mella Exp $";

/* 
 * System Headers 
 */
#include <stdlib.h>
#include <iostream>

#include "fitsio.h"

/**
 * \namespace std
 * Export standard iostream objects (cin, cout,...).
 */
using namespace std;



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
 * Signal catching functions  
 */



/* 
 * Main
 */

int main(int argc, char *argv[])
{
    // Initialize MCS services
    if(mcsInit(argv[0]) == mcsFAILURE)
    {
        // Error handling if necessary

        // Exit from the application with FAILURE
        exit(EXIT_FAILURE);
    }

    //
    //
    // 

    {
        fitsfile *oiFile;
        int status = 0;
        char filename[] = "data5.oifits";

        if(fits_open_file(&oiFile, filename, READONLY, &status))
        {
            printf("An error occured opening file\n");
            if(status)
                fits_report_error(stderr, status);      /* print any error message */
            return (status);
        }
        printf("+ %s file loaded.\n", filename);

    }

    // Close MCS services
    mcsExit();

    // Exit from the application with SUCCESS
    exit(EXIT_SUCCESS);
}


/*___oOo___*/
