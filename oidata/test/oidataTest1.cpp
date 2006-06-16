/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: oidataTest1.cpp,v 1.3 2006-05-11 13:04:56 mella Exp $"
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
 * brief description of the program, which ends at this dot.
 *
 * \synopsis
 * \<Command Name\> [\e \<param1\> ... \e \<paramN\>] 
 *                     [\e \<option1\> ... \e \<optionN\>] 
 *
 * \param param1 : description of parameter 1, if it exists
 * \param paramN : description of parameter N, if it exists
 *
 * \n
 * \opt
 * \optname option1 : description of option 1, if it exists
 * \optname optionN : description of option N, if it exists
 * 
 * \n
 * \details
 * OPTIONAL detailed description of the c main file follows here.
 * 
 * \usedfiles
 * OPTIONAL. If files are used, for each one, name, and usage description.
 * \filename fileName1 :  usage description of fileName1
 * \filename fileName2 :  usage description of fileName2
 *
 * \n
 * \env
 * OPTIONAL. If needed, environmental variables accessed by the program. For
 * each variable, name, and usage description, as below.
 * \envvar envVar1 :  usage description of envVar1
 * \envvar envVar2 :  usage description of envVar2
 * 
 * \n
 * \warning OPTIONAL. Warning if any (software requirements, ...)
 *
 * \n
 * \ex
 * OPTIONAL. Command example if needed
 * \n Brief example description.
 * \code
 * Insert your command example here
 * \endcode
 *
 * \sa OPTIONAL. See also section, in which you can refer other documented
 * entities. Doxygen will create the link automatically.
 * \sa <entity to refer>
 * 
 * \bug OPTIONAL. Known bugs list if it exists.
 * \bug Bug 1 : bug 1 description
 *
 * \todo OPTIONAL. Things to forsee list, if needed. 
 * \todo Action 1 : action 1 description
 * 
 */

    "@(#) $Id: oidataTest1.cpp,v 1.3 2006-05-11 13:04:56 mella Exp $";
static char *rcsId __attribute__ ((unused)) ="@(#) $Id: oidataTest1.cpp,v 1.3 2006-05-11 13:04:56 mella Exp $";

/* 
 * System Headers 
 */
#include <stdlib.h>
#include <iostream>

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


#include <CCfits>
// The library is enclosed in a namespace.
using namespace CCfits;

/*
 * Local Variables
 */



/* 
 * Signal catching functions  
 */



/* 
 * Main
 */
int readTable()
{

    // read a table and explicitly read selected columns. To read instead all the
    // data on construction, set the last argument of the FITS constructor
    // call to 'true'. This functionality was tested in the last release.
    std::vector < string > hdus;

    FITS f("data5.oifits", Read, true);
    f.extensions();

    std::auto_ptr < FITS > pInfile(new FITS("data5.oifits", Read, true));

    // set verbose mode
    pInfile->setVerboseMode(true);


    // get primary hdu 
    std::cout << "pHDU:" << std::endl;
    HDU & phdu = pInfile->pHDU();
    std::cout << phdu << std::endl;


    //get all extensions
    const ExtMap & extensions = f.extensions();
    int nbOfHDU = extensions.size();
    std::cout << nbOfHDU << " HDU founded " << std::endl;
    {
        int i;
        for(i = 1; i <= nbOfHDU; i++)
        {
            ExtHDU & hdu = f.extension(i);
            std::cout << "HDU[" << i << "]: " << hdu.name() << std::endl;
        }
    }


    BinTable & table = (BinTable &) f.extension("OI_TARGET");
    std::cout << "OI_TARGET table=" << table << std::endl;

    std::cout << "-- OI_TARGET COLUMNS" << std::endl;
    std::map < String, CCfits::Column * >::const_iterator colIterator;

    for(colIterator = table.column().begin();
        colIterator != table.column().end(); colIterator++)
    {
        std::cout << "OI_TARGET..." << (*colIterator).first << std::endl;
    }

    {
        string extName = "OI_WAVELENGTH";
        string colName = "EFF_BAND";
        BinTable & one_table = (BinTable &) f.extension(extName);
        std::cout << "-- EXTENSION -- " << extName << std::endl;
        Column & one_column = one_table.column(colName);
        int nbOfRows = one_column.rows();
        std::vector < int >data;
        one_column.read(data, 0, nbOfRows);
        int i;
        for(i = 0; i < nbOfRows; i++)
        {
            std::
                cout << extName << "." << colName << "[" << i << "]: " <<
                data[i] << std::endl;
        }
        std::cout << one_column << "-- EXTENSION -- " << std::endl;
    }
    /* commented because valarray can't be converted {
       string extName = "OI_VIS2";
       string colName = "VIS2DATA";
       BinTable &one_table = (BinTable &)f.extension(extName);
       std::cout << "-- EXTENSION -- " << extName << std::endl;
       Column &one_column  =  one_table.column(colName);
       unsigned int nbOfRows = one_column.rows();
       std::vector< valarray<double> > datasrc;
       one_column.readArrays(datasrc, 0, nbOfRows);
       double ** data = (double **) datasrc;
       unsigned int i,j;

       //for (i=0; i < nbOfRows; i++){
       for (i=0; i < one_column.width(); i++){
       for (j=0; j < one_column.rows(); j++){
       std::cout<<extName<<"."<<colName<<"["<<i<<"]["<<j<<"]: "<< data[i][j]<< std::endl;                
       }
       }
       std::cout << "-- EXTENSION -- " << std::endl;
       }
     */


/*
        std::vector < valarray <std::complex<double> > > cc;
        table.column("dcomplex-roots").readArrays( cc, 1,3 );

        std::valarray < std::complex<float> > ff;
        table.column("fcomplex-roots").read( ff, 4 );
*/
    // !! lot of job onto hdu[0] make segfault... !!
    // std::cout << pInfile->extension(hdus[0]) << std::endl;

    std::cout << pInfile->extension(1) << std::endl;

    return 0;
}

int main(int argc, char *argv[])
{
    // Initialize MCS services
    if(mcsInit(argv[0]) == mcsFAILURE)
    {
        // Error handling if necessary

        // Exit from the application with FAILURE
        exit(EXIT_FAILURE);
    }

    readTable();



    //
    // Insert your code here
    // 



    // Close MCS services
    mcsExit();

    // Exit from the application with SUCCESS
    exit(EXIT_SUCCESS);
}


/*___oOo___*/
