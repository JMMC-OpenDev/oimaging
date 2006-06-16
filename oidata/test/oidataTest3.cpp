/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: oidataTest3.cpp,v 1.5 2006-05-11 13:04:56 mella Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2005/09/22 07:20:04  mella
 * Program reindented
 *
 * Revision 1.3  2005/09/14 15:20:10  mella
 * Load without stoping for any wrong format
 *
 * Revision 1.2  2005/08/31 20:21:11  mella
 * Remove bad printf parameters
 *
 * Revision 1.1  2005/06/24 14:07:12  mella
 * First revision
 *
 ******************************************************************************/

/**
 * \file Perform first tests with oidata Objects  
 */

    "@(#) $Id: oidataTest3.cpp,v 1.5 2006-05-11 13:04:56 mella Exp $";
static char *rcsId __attribute__ ((unused)) ="@(#) $Id: oidataTest3.cpp,v 1.5 2006-05-11 13:04:56 mella Exp $";
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

/*
 * Local Variables
 */



/* 
 * Signal catching functions  
 */

/* prototypes */
void f_oiArray(oidataOI_ARRAY * oiArray);
void f_oiVis(oidataOI_VIS * oiVis);
void f_oiVis2(oidataOI_VIS2 * oiVis2);

void f_oiTarget(oidataOI_TARGET * oiTarget)
{
}

void f_oiWavelength(oidataOI_WAVELENGTH * oiWavelength)
{
}
void f_oiT3(oidataOI_T3 * oiT3)
{
}



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

    logSetStdoutLogLevel(logTRACE);
    char filename[128];

    if(argc != 2)
    {
        printf("no filename given\n");
        sprintf(filename, "data5.oifits");
    }
    else
    {
        sprintf(filename, argv[1]);
    }

    // Work onto a FITS FILE
    oidataFITS oidata1;

    printf("@oidata1 = %p\n", &oidata1);

    // This block doesn't work because Load has not been loaded yet.
    {

        // PRINT NUMBER OF TABLE
        mcsINT16 nbOfOiTable = 0;
        if(oidata1.GetNumberOfOiTable(&nbOfOiTable) == mcsFAILURE)
        {
            printf("Error for test getting nb of tables\n");
            errCloseStack();
        }
        //printf("%s contains %d OI tables\n", filename, nbOfOiTable);
    }

    //if(oidata1.Load(filename, mcsFALSE) == mcsFAILURE)
    if(oidata1.Load(filename) == mcsFAILURE)
    {
        printf("Error during file loading\n");
        errCloseStack();
        exit(1);
    }
    printf("Successfull file loading\n");

    // PRINT NUMBER OF TABLE
    {
        mcsINT16 nbOfOiTable = 0;
        if(oidata1.GetNumberOfOiTable(&nbOfOiTable) == mcsFAILURE)
        {
            printf("Error getting nb of tables\n");
            errCloseStack();
            exit(1);
        }
        printf("%s contains %d OI tables\n", filename, nbOfOiTable);
    }



    // play with the oi_target using given reference by oidata.GetOiTarget()
    {
        oidataOI_TARGET *oiTarget;
        mcsINT16 hduIndex;
        if(oidata1.GetOiTarget(&oiTarget) == mcsFAILURE)
        {
            printf("Error getting oiTarget reference\n");
            errCloseStack();
            exit(1);
        }
        oiTarget->GetHduIndex(&hduIndex);
        printf("OI_TARGET 's hdu is '%d'\n", hduIndex);
    }

    // play with the 2nd oi_table (C index is 1 for second position)
    {
        oidataOI_TABLE *oiTable;
        mcsINT16 hduIndex;
        if(oidata1.GetOiTable(&oiTable, 1) == mcsFAILURE)
        {
            printf("Error getting oiTable reference\n");
            errCloseStack();
            exit(1);
        }

        // get hdu index 
        oiTable->GetHduIndex(&hduIndex);
        printf("OI_TABLE[1]'s hdu is '%d'\n", hduIndex);

        // get keyword using low level access 
        // (it could be placed into protected area...
        mcsSTRING32 keyValue;
        mcsSTRING32 keyName = "OI_REvN";
        if(oiTable->GetKeyword(keyName, keyValue) == mcsFAILURE)
        {
            printf("Error getting oiTable keyword '%s'\n", keyName);
            errCloseStack();
            exit(1);
        }
        printf("OI_TABLE[1].%s = '%s'\n", keyName, keyValue);

        // get numerical keyword
        mcsINT16 iValue;
        if(oiTable->GetKeyword(keyName, &iValue) == mcsFAILURE)
        {
            printf("Error getting oiTable keyword '%s'\n", keyName);
            errCloseStack();
            exit(1);
        }
        printf("OI_TABLE[1].%s = '%d'\n", keyName, iValue);

    }

    // test each oiTable of the fits
    {
        mcsINT16 nbOfOiTable = 0;
        mcsINT16 tableIdx = 0;
        oidataOI_TABLE *table;
        if(oidata1.GetNumberOfOiTable(&nbOfOiTable) == mcsFAILURE)
        {
            printf("Error getting nb of tables\n");
            errCloseStack();
            exit(1);
        }
        for(tableIdx = 0; tableIdx < nbOfOiTable; tableIdx++)
        {
            // get reference of table
            if(oidata1.GetOiTable(&table, tableIdx) == mcsFAILURE)
            {
                printf("Error getting reference of oiTable number %d\n",
                       tableIdx);
                errCloseStack();
                exit(1);
            }
            // get type and print it
            oidataTABLE_TYPE type = table->GetType();
            printf("###############\n # Table[%d] 's type is %d\n",
                   tableIdx, type);
            if(type == OI_ARRAY_TYPE)
            {
                oidataOI_ARRAY *oiArray = (oidataOI_ARRAY *) table;
                f_oiArray(oiArray);
            }
            else if(type == OI_TARGET_TYPE)
            {
                f_oiTarget((oidataOI_TARGET *) table);
            }
            else if(type == OI_WAVELENGTH_TYPE)
            {
                f_oiWavelength((oidataOI_WAVELENGTH *) table);
            }
            else if(type == OI_VIS_TYPE)
            {
                f_oiVis((oidataOI_VIS *) table);
            }
            else if(type == OI_VIS2_TYPE)
            {
                f_oiVis2((oidataOI_VIS2 *) table);
            }
            else if(type == OI_T3_TYPE)
            {
                f_oiT3((oidataOI_T3 *) table);
            }
            else
            {
                printf("TYPE OF TABLE UNKNOWN :(\n");
            }
        }
    }

    // Close MCS services
    mcsExit();

    // Exit from the application with SUCCESS
    exit(EXIT_SUCCESS);
}


void f_oiArray(oidataOI_ARRAY * oiArray)
{
    mcsINT16 hduIndex;
    int stationIdx;
    int idx;

    // get hdu index 
    oiArray->GetHduIndex(&hduIndex);
    printf("OI_TABLE's hdu is '%d'\n", hduIndex);

    // get keyword
    mcsSTRING32 keyValue;
    mcsSTRING32 keyName = "EXTNAME";
    if(oiArray->GetKeyword(keyName, keyValue) == mcsFAILURE)
    {
        printf("Error getting oiArray keyword '%s'\n", keyName);
        errCloseStack();
        exit(1);
    }
    printf("OI_TABLE.%s = '%s'\n", keyName, keyValue);

    // Get nb of telescope
    mcsINT16 nbStations;
    nbStations = oiArray->GetNumberOfElements();
    printf("Nb of telescopes :%d\n", nbStations);


    // Get one column
    // Low level access
    mcsDOUBLE staXYZ[nbStations * 3];
    oiArray->ReadColumn("STAXYZ", nbStations * 3, 1, staXYZ);
    for(idx = 0; idx < nbStations; idx++)
    {
        int i;
        for(i = 0; i < 3; i++)
        {
            printf("StaXYZ[%d][%d]=%f\n", idx, i, staXYZ[idx * 3 + i]);
        }
    }

    // Get one ascii column
    // Low level access
    char *staNames[nbStations];
    /* allocate space for string column value */
    for(idx = 0; idx < nbStations; idx++)
    {
        staNames[idx] = (char *) malloc(64);
    }
    oiArray->ReadColumn("STA_NAME", nbStations, 1, staNames);
    for(idx = 0; idx < nbStations; idx++)
    {
        printf("STA_NAME[][%d]=%s\n", idx, staNames[idx]);
    }

    // Get STAXYZ
    // High level access.
    mcsDOUBLE stationsXYZ[nbStations][3];
    oiArray->GetStaXYZ((mcsDOUBLE *) stationsXYZ);

    for(stationIdx = 0; stationIdx < nbStations; stationIdx++)
    {
        for(idx = 0; idx < 3; idx++)
        {
            printf("StaXYZ[%d][%d]=%f\n", stationIdx, idx,
                   stationsXYZ[stationIdx][idx]);
        }
    }

    // Get one ascii column
    // High level access
    oiArray->GetStaName((mcsSTRING16 *) staNames);
    for(idx = 0; idx < nbStations; idx++)
    {
        printf("STA_NAME[%d]=%s\n", idx, staNames[idx]);
    }
    oiArray->GetTelName((mcsSTRING16 *) staNames);
    for(idx = 0; idx < nbStations; idx++)
    {
        printf("TEL_NAME[%d]=%s\n", idx, staNames[idx]);
    }

    mcsSTRING32 arrName;
    oiArray->GetArrName(arrName);
    printf("ARRNAME is '%s'\n", arrName);
}

void f_oiVis(oidataOI_VIS * oiVis)
{
    // Print name of both used array
    mcsINT16 nbOfData;

    oidataOI_ARRAY *myArray;
    int i, j, k;
    mcsINT16 nbOfElements;

    nbOfData = oiVis->GetNumberOfRows();


    mcsINT16 staIndex[nbOfData][2];
    // Get my station indexes
    if(oiVis->GetStaIndex((mcsINT16 *) staIndex) == mcsFAILURE)
    {
        printf("Error getting array index \n");
        errCloseStack();
        exit(1);
    }

    // Get reference of my array
    if(oiVis->GetOiArrayTable(&myArray) == mcsFAILURE)
    {
        printf("Error getting array reference \n");
        errCloseStack();
        exit(1);
    }

    // Get number of stations of the array
    nbOfElements = myArray->GetNumberOfElements();

    mcsINT16 arrayStaIndex[nbOfElements];
    char *arrayStaName[nbOfElements];
    /* allocate space for string column value */
    for(i = 0; i < nbOfElements; i++)
    {
        arrayStaName[i] = (char *) malloc(64);
    }


    // Get stations index of the array
    if(myArray->GetStaIndex(arrayStaIndex) == mcsFAILURE)
    {
        printf("Error getting array sta index \n");
        errCloseStack();
        exit(1);
    }
    // Get stations names of the array
    if(myArray->GetStaName((mcsSTRING16 *) arrayStaName) == mcsFAILURE)
    {
        printf("Error getting array sta name \n");
        errCloseStack();
        exit(1);
    }

    // Do print
    for(k = 0; k < nbOfData; k++)
    {
        printf("Vis Station[%d]", k);
        for(j = 0; j < 2; j++)
        {
            for(i = 0; i < nbOfElements; i++)
            {
                if(staIndex[k][j] == arrayStaIndex[i])
                {
                    printf(" '%s'  ", arrayStaName[i]);
                }
            }
        }
        for(j = 0; j < 2; j++)
        {
            for(i = 0; i < nbOfElements; i++)
            {
                if(staIndex[k][j] == arrayStaIndex[i])
                {
                    printf(" '%d'  ", arrayStaIndex[i]);
                }
            }
        }

        printf("\n");
    }
}

void f_oiVis2(oidataOI_VIS2 * oiVis2)
{
    // Print name of both used array for each data
    int i, j;

    // Get oiWavelength reference
    oidataOI_WAVELENGTH *myOiWavelength;

    // Get reference of my wavelength 
    if(oiVis2->GetOiWavelengthTable(&myOiWavelength) == mcsFAILURE)
    {
        printf("Error getting wavelength reference \n");
        errCloseStack();
        exit(1);
    }
    // Number of data
    mcsINT16 nbOfData;
    nbOfData = oiVis2->GetNumberOfRows();
    // Number of waves
    mcsINT16 nbOfWaves;
    nbOfWaves = myOiWavelength->GetNumberOfRows();

    printf("this OI_VIS2 contains %d rows for each columns\n", nbOfData);
    printf("its OI_WAVELENGTH table contains %d rows for each columns\n",
           nbOfWaves);

    // This block displays Flags
    {
        char flags[nbOfData][nbOfWaves];
        if(oiVis2->GetFlag((char *) flags) == mcsFAILURE)
        {
            printf("Error getting flags");
            errCloseStack();
            exit(1);
        }

        for(i = 0; i < nbOfData; i++)
        {
            printf("Flag[%d] \t ", i);
            for(j = 0; j < nbOfWaves; j++)
            {
                printf("%d", flags[i][j]);
            }
            printf("\n");
        }
    }


}

/*___oOo___*/
