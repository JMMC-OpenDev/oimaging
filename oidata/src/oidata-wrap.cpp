
#if SWIG
#define mcsCOMPL_STAT      int

#define mcsINT8            char
#define mcsUINT8           unsigned char
#define mcsINT16           short
#define mcsUINT16          unsigned short
#define mcsINT32           int
#define mcsUINT32          unsigned int
#define mcsDOUBLE          double
#define mcsFLOAT           float

#define mcsBYTES4       unsigned char
#define mcsBYTES8       unsigned char
#define mcsBYTES12     unsigned char
#define mcsBYTES16     unsigned char
#define mcsBYTES20     unsigned char
#define mcsBYTES32     unsigned char
#define mcsBYTES48     unsigned char
#define mcsBYTES64     unsigned char
#define mcsBYTES80     unsigned char
#define mcsBYTES128   unsigned char
#define mcsBYTES256   unsigned char
#define mcsBYTES512   unsigned char
#define mcsBYTES1024 unsigned char

#define mcsSTRING4      char *
#define mcsSTRING8      char *
#define mcsSTRING12    char *
#define mcsSTRING16    char *
#define mcsSTRING20    char *
#define mcsSTRING32    char *
#define mcsSTRING48    char *
#define mcsSTRING64    char *
#define mcsSTRING80    char *
#define mcsSTRING128  char *
#define mcsSTRING256  char *
#define mcsSTRING512  char *
#define mcsSTRING1024 char *

/*
 *Enum redifinition
 */

#define oidataTABLE_TYPE int


#else

#include "mcs.h"
#include "oidata.h"
#include "oidataOI_TABLE.h"
#include "oidataOI_TARGET.h"
#include "oidataFITS.h"
#include "oidataOI_ARRAY.h"
#include "oidataDATA_TABLE.h"
#include "oidataOI_VIS.h"
#include "oidataOI_VIS2.h"
#include "oidataOI_T3.h"
#include "oidataOI_WAVELENGTH.h"

#endif
extern "C"
{

/* 
 * Wrapping 'oidataOI_TABLE' class 
 */

/* constructor 'oidataOI_TABLE' method */

    void oidataOI_TABLE_new(void **obj, oidataFITS * fitsParent,
                            mcsSTRING32 extName, mcsINT16 hduIndex)
    {
        *obj = new oidataOI_TABLE(fitsParent, extName, hduIndex);
    }


/* destructor 'oidataOI_TABLE' method */

    void oidataOI_TABLE_delete(void *obj)
    {
        oidataOI_TABLE *o = (oidataOI_TABLE *) obj;
        delete o;
    }

/* Wrapping 'GetType' method */
    oidataTABLE_TYPE oidataOI_TABLE_GetType(void *obj)
    {
        oidataOI_TABLE *o = (oidataOI_TABLE *) obj;
        return o->GetType();
    }

/* Wrapping 'CheckStructure' method */
    mcsCOMPL_STAT oidataOI_TABLE_CheckStructure(void *obj)
    {
        oidataOI_TABLE *o = (oidataOI_TABLE *) obj;
        return o->CheckStructure();
    }

/* Wrapping 'GetNumberOfRows' method */
    mcsINT16 oidataOI_TABLE_GetNumberOfRows(void *obj)
    {
        oidataOI_TABLE *o = (oidataOI_TABLE *) obj;
        return o->GetNumberOfRows();
    }

/* Wrapping 'GetExtname' method */
    mcsCOMPL_STAT oidataOI_TABLE_GetExtname(void *obj, mcsSTRING32 extname)
    {
        oidataOI_TABLE *o = (oidataOI_TABLE *) obj;
        return o->GetExtname(extname);
    }

/* Wrapping 'GetOiRevn' method */
    mcsCOMPL_STAT oidataOI_TABLE_GetOiRevn(void *obj, mcsINT16 * oiRevn)
    {
        oidataOI_TABLE *o = (oidataOI_TABLE *) obj;
        return o->GetOiRevn(oiRevn);
    }

/* Wrapping 'GetHduIndex' method */
    mcsCOMPL_STAT oidataOI_TABLE_GetHduIndex(void *obj, mcsINT16 * hduIndex)
    {
        oidataOI_TABLE *o = (oidataOI_TABLE *) obj;
        return o->GetHduIndex(hduIndex);
    }

/* Wrapping 'GetKeyword' method */
    mcsCOMPL_STAT oidataOI_TABLE_GetKeyword_1(void *obj,
                                              const mcsSTRING32 keyName,
                                              mcsINT16 * keyValue)
    {
        oidataOI_TABLE *o = (oidataOI_TABLE *) obj;
        return o->GetKeyword(keyName, keyValue);
    }

/* Wrapping 'GetKeyword' method */
    mcsCOMPL_STAT oidataOI_TABLE_GetKeyword_2(void *obj,
                                              const mcsSTRING32 keyName,
                                              mcsFLOAT * keyValue)
    {
        oidataOI_TABLE *o = (oidataOI_TABLE *) obj;
        return o->GetKeyword(keyName, keyValue);
    }

/* Wrapping 'GetKeyword' method */
    mcsCOMPL_STAT oidataOI_TABLE_GetKeyword_3(void *obj,
                                              const mcsSTRING32 keyName,
                                              mcsDOUBLE * keyValue)
    {
        oidataOI_TABLE *o = (oidataOI_TABLE *) obj;
        return o->GetKeyword(keyName, keyValue);
    }

/* Wrapping 'GetKeyword' method */
    mcsCOMPL_STAT oidataOI_TABLE_GetKeyword_4(void *obj,
                                              const mcsSTRING32 keyName,
                                              mcsLOGICAL * keyValue)
    {
        oidataOI_TABLE *o = (oidataOI_TABLE *) obj;
        return o->GetKeyword(keyName, keyValue);
    }

/* Wrapping 'GetKeyword' method */
    mcsCOMPL_STAT oidataOI_TABLE_GetKeyword_5(void *obj,
                                              const mcsSTRING32 keyName,
                                              mcsSTRING32 keyValue)
    {
        oidataOI_TABLE *o = (oidataOI_TABLE *) obj;
        return o->GetKeyword(keyName, keyValue);
    }

/* Wrapping 'ReadColumn' method */
    mcsCOMPL_STAT oidataOI_TABLE_ReadColumn_1(void *obj,
                                              const mcsSTRING32 colName,
                                              mcsINT16 nbElements,
                                              mcsINT16 rowIdx,
                                              mcsINT16 * array)
    {
        oidataOI_TABLE *o = (oidataOI_TABLE *) obj;
        return o->ReadColumn(colName, nbElements, rowIdx, array);
    }

/* Wrapping 'ReadColumn' method */
    mcsCOMPL_STAT oidataOI_TABLE_ReadColumn_2(void *obj,
                                              const mcsSTRING32 colName,
                                              mcsINT16 nbElements,
                                              mcsINT16 rowIdx,
                                              mcsFLOAT * array)
    {
        oidataOI_TABLE *o = (oidataOI_TABLE *) obj;
        return o->ReadColumn(colName, nbElements, rowIdx, array);
    }

/* Wrapping 'ReadColumn' method */
    mcsCOMPL_STAT oidataOI_TABLE_ReadColumn_3(void *obj,
                                              const mcsSTRING32 colName,
                                              mcsINT16 nbElements,
                                              mcsINT16 rowIdx,
                                              mcsDOUBLE * array)
    {
        oidataOI_TABLE *o = (oidataOI_TABLE *) obj;
        return o->ReadColumn(colName, nbElements, rowIdx, array);
    }

/* Wrapping 'ReadColumn' method */
    mcsCOMPL_STAT oidataOI_TABLE_ReadColumn_4(void *obj,
                                              const mcsSTRING32 colName,
                                              mcsINT16 nbElements,
                                              mcsINT16 rowIdx, char *array)
    {
        oidataOI_TABLE *o = (oidataOI_TABLE *) obj;
        return o->ReadColumn(colName, nbElements, rowIdx, array);
    }

/* Wrapping 'ReadColumn' method */
    mcsCOMPL_STAT oidataOI_TABLE_ReadColumn_5(void *obj,
                                              const mcsSTRING32 colName,
                                              mcsINT16 nbElements,
                                              mcsINT16 rowIdx, char **array)
    {
        oidataOI_TABLE *o = (oidataOI_TABLE *) obj;
        return o->ReadColumn(colName, nbElements, rowIdx, array);
    }

/* 
 * Wrapping 'oidataOI_TARGET' class 
 */

/* constructor 'oidataOI_TARGET' method */

    void oidataOI_TARGET_new(void **obj, oidataFITS * fitsParent,
                             mcsSTRING32 extName, mcsINT16 hduIndex)
    {
        *obj = new oidataOI_TARGET(fitsParent, extName, hduIndex);
    }


/* destructor 'oidataOI_TARGET' method */

    void oidataOI_TARGET_delete(void *obj)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        delete o;
    }

/* Wrapping 'CheckStructure' method */
    mcsCOMPL_STAT oidataOI_TARGET_CheckStructure(void *obj)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->CheckStructure();
    }

/* Wrapping 'GetTargetId' method */
    mcsCOMPL_STAT oidataOI_TARGET_GetTargetId(void *obj, mcsINT16 * targetId)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->GetTargetId(targetId);
    }

/* Wrapping 'GetTarget' method */
    mcsCOMPL_STAT oidataOI_TARGET_GetTarget(void *obj, mcsSTRING16 * target)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->GetTarget(target);
    }

/* Wrapping 'GetRaEp0' method */
    mcsCOMPL_STAT oidataOI_TARGET_GetRaEp0(void *obj, mcsDOUBLE * raEp0)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->GetRaEp0(raEp0);
    }

/* Wrapping 'GetDecEp0' method */
    mcsCOMPL_STAT oidataOI_TARGET_GetDecEp0(void *obj, mcsDOUBLE * decEp0)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->GetDecEp0(decEp0);
    }

/* Wrapping 'GetEquinox' method */
    mcsCOMPL_STAT oidataOI_TARGET_GetEquinox(void *obj, mcsFLOAT * equinox)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->GetEquinox(equinox);
    }

/* Wrapping 'GetRaErr' method */
    mcsCOMPL_STAT oidataOI_TARGET_GetRaErr(void *obj, mcsDOUBLE * raErr)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->GetRaErr(raErr);
    }

/* Wrapping 'GetDecErr' method */
    mcsCOMPL_STAT oidataOI_TARGET_GetDecErr(void *obj, mcsDOUBLE * decErr)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->GetDecErr(decErr);
    }

/* Wrapping 'GetSysVel' method */
    mcsCOMPL_STAT oidataOI_TARGET_GetSysVel(void *obj, mcsDOUBLE * sysVel)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->GetSysVel(sysVel);
    }

/* Wrapping 'GetVelTyp' method */
    mcsCOMPL_STAT oidataOI_TARGET_GetVelTyp(void *obj, mcsSTRING8 * velTyp)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->GetVelTyp(velTyp);
    }

/* Wrapping 'GetVelDef' method */
    mcsCOMPL_STAT oidataOI_TARGET_GetVelDef(void *obj, mcsSTRING8 * velDef)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->GetVelDef(velDef);
    }

/* Wrapping 'GetPMRa' method */
    mcsCOMPL_STAT oidataOI_TARGET_GetPMRa(void *obj, mcsDOUBLE * pmRa)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->GetPMRa(pmRa);
    }

/* Wrapping 'GetPMDec' method */
    mcsCOMPL_STAT oidataOI_TARGET_GetPMDec(void *obj, mcsDOUBLE * pmDec)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->GetPMDec(pmDec);
    }

/* Wrapping 'GetPMraErr' method */
    mcsCOMPL_STAT oidataOI_TARGET_GetPMraErr(void *obj, mcsDOUBLE * pmRaErr)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->GetPMraErr(pmRaErr);
    }

/* Wrapping 'GetPMDecErr' method */
    mcsCOMPL_STAT oidataOI_TARGET_GetPMDecErr(void *obj, mcsDOUBLE * pmdecErr)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->GetPMDecErr(pmdecErr);
    }

/* Wrapping 'GetParallax' method */
    mcsCOMPL_STAT oidataOI_TARGET_GetParallax(void *obj, mcsFLOAT * parallax)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->GetParallax(parallax);
    }

/* Wrapping 'GetParaErr' method */
    mcsCOMPL_STAT oidataOI_TARGET_GetParaErr(void *obj, mcsFLOAT * paraErr)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->GetParaErr(paraErr);
    }

/* Wrapping 'GetSpecTyp' method */
    mcsCOMPL_STAT oidataOI_TARGET_GetSpecTyp(void *obj, mcsSTRING16 * specTyp)
    {
        oidataOI_TARGET *o = (oidataOI_TARGET *) obj;
        return o->GetSpecTyp(specTyp);
    }

/* 
 * Wrapping 'oidataFITS' class 
 */

/* constructor 'oidataFITS' method */

    void oidataFITS_new(void **obj)
    {
        *obj = new oidataFITS();
    }


/* destructor 'oidataFITS' method */

    void oidataFITS_delete(void *obj)
    {
        oidataFITS *o = (oidataFITS *) obj;
        delete o;
    }

/* Wrapping 'Load' method */
    mcsCOMPL_STAT oidataFITS_Load(void *obj, const mcsSTRING32 filename,
                                  const mcsLOGICAL stopForBadFormat)
    {
        oidataFITS *o = (oidataFITS *) obj;
        return o->Load(filename, stopForBadFormat);
    }

/* Wrapping 'Save' method */
    mcsCOMPL_STAT oidataFITS_Save(void *obj, const mcsSTRING32 filename,
                                  const mcsLOGICAL overwrite)
    {
        oidataFITS *o = (oidataFITS *) obj;
        return o->Save(filename, overwrite);
    }

/* Wrapping 'GetOiTarget' method */
    mcsCOMPL_STAT oidataFITS_GetOiTarget(void *obj,
                                         oidataOI_TARGET ** oiTarget)
    {
        oidataFITS *o = (oidataFITS *) obj;
        return o->GetOiTarget(oiTarget);
    }

/* Wrapping 'GetNumberOfOiTable' method */
    mcsCOMPL_STAT oidataFITS_GetNumberOfOiTable(void *obj,
                                                mcsINT16 * nbOfTable)
    {
        oidataFITS *o = (oidataFITS *) obj;
        return o->GetNumberOfOiTable(nbOfTable);
    }

/* Wrapping 'GetOiTable' method */
    mcsCOMPL_STAT oidataFITS_GetOiTable(void *obj, oidataOI_TABLE ** table,
                                        mcsINT16 index)
    {
        oidataFITS *o = (oidataFITS *) obj;
        return o->GetOiTable(table, index);
    }

/* Wrapping 'GetKeyword' method */
    mcsCOMPL_STAT oidataFITS_GetKeyword_1(void *obj,
                                          const mcsSTRING32 keyName,
                                          mcsINT16 * keyValue,
                                          mcsINT16 hduIndex)
    {
        oidataFITS *o = (oidataFITS *) obj;
        return o->GetKeyword(keyName, keyValue, hduIndex);
    }

/* Wrapping 'GetKeyword' method */
    mcsCOMPL_STAT oidataFITS_GetKeyword_2(void *obj,
                                          const mcsSTRING32 keyName,
                                          mcsFLOAT * keyValue,
                                          mcsINT16 hduIndex)
    {
        oidataFITS *o = (oidataFITS *) obj;
        return o->GetKeyword(keyName, keyValue, hduIndex);
    }

/* Wrapping 'GetKeyword' method */
    mcsCOMPL_STAT oidataFITS_GetKeyword_3(void *obj,
                                          const mcsSTRING32 keyName,
                                          mcsDOUBLE * keyValue,
                                          mcsINT16 hduIndex)
    {
        oidataFITS *o = (oidataFITS *) obj;
        return o->GetKeyword(keyName, keyValue, hduIndex);
    }

/* Wrapping 'GetKeyword' method */
    mcsCOMPL_STAT oidataFITS_GetKeyword_4(void *obj,
                                          const mcsSTRING32 keyName,
                                          mcsLOGICAL * keyValue,
                                          mcsINT16 hduIndex)
    {
        oidataFITS *o = (oidataFITS *) obj;
        return o->GetKeyword(keyName, keyValue, hduIndex);
    }

/* Wrapping 'GetKeyword' method */
    mcsCOMPL_STAT oidataFITS_GetKeyword_5(void *obj,
                                          const mcsSTRING32 keyName,
                                          mcsSTRING32 keyValue,
                                          mcsINT16 hduIndex)
    {
        oidataFITS *o = (oidataFITS *) obj;
        return o->GetKeyword(keyName, keyValue, hduIndex);
    }

/* Wrapping 'ReadColumn' method */
    mcsCOMPL_STAT oidataFITS_ReadColumn_1(void *obj,
                                          const mcsSTRING32 colName,
                                          mcsINT16 hduIndex,
                                          mcsINT16 nbElements,
                                          mcsINT16 rowIdx, mcsINT16 * array)
    {
        oidataFITS *o = (oidataFITS *) obj;
        return o->ReadColumn(colName, hduIndex, nbElements, rowIdx, array);
    }

/* Wrapping 'ReadColumn' method */
    mcsCOMPL_STAT oidataFITS_ReadColumn_2(void *obj,
                                          const mcsSTRING32 colName,
                                          mcsINT16 hduIndex,
                                          mcsINT16 nbElements,
                                          mcsINT16 rowIdx, mcsFLOAT * array)
    {
        oidataFITS *o = (oidataFITS *) obj;
        return o->ReadColumn(colName, hduIndex, nbElements, rowIdx, array);
    }

/* Wrapping 'ReadColumn' method */
    mcsCOMPL_STAT oidataFITS_ReadColumn_3(void *obj,
                                          const mcsSTRING32 colName,
                                          mcsINT16 hduIndex,
                                          mcsINT16 nbElements,
                                          mcsINT16 rowIdx, mcsDOUBLE * array)
    {
        oidataFITS *o = (oidataFITS *) obj;
        return o->ReadColumn(colName, hduIndex, nbElements, rowIdx, array);
    }

/* Wrapping 'ReadColumn' method */
    mcsCOMPL_STAT oidataFITS_ReadColumn_4(void *obj,
                                          const mcsSTRING32 colName,
                                          mcsINT16 hduIndex,
                                          mcsINT16 nbElements,
                                          mcsINT16 rowIdx, char *array)
    {
        oidataFITS *o = (oidataFITS *) obj;
        return o->ReadColumn(colName, hduIndex, nbElements, rowIdx, array);
    }

/* Wrapping 'ReadColumn' method */
    mcsCOMPL_STAT oidataFITS_ReadColumn_5(void *obj,
                                          const mcsSTRING32 colName,
                                          mcsINT16 hduIndex,
                                          mcsINT16 nbElements,
                                          mcsINT16 rowIdx, char **array)
    {
        oidataFITS *o = (oidataFITS *) obj;
        return o->ReadColumn(colName, hduIndex, nbElements, rowIdx, array);
    }

/* 
 * Wrapping 'oidataOI_ARRAY' class 
 */

/* constructor 'oidataOI_ARRAY' method */

    void oidataOI_ARRAY_new(void **obj, oidataFITS * fitsParent,
                            mcsSTRING32 extName, mcsINT16 hduIndex)
    {
        *obj = new oidataOI_ARRAY(fitsParent, extName, hduIndex);
    }


/* destructor 'oidataOI_ARRAY' method */

    void oidataOI_ARRAY_delete(void *obj)
    {
        oidataOI_ARRAY *o = (oidataOI_ARRAY *) obj;
        delete o;
    }

/* Wrapping 'GetArrName' method */
    mcsCOMPL_STAT oidataOI_ARRAY_GetArrName(void *obj, mcsSTRING32 arrName)
    {
        oidataOI_ARRAY *o = (oidataOI_ARRAY *) obj;
        return o->GetArrName(arrName);
    }

/* Wrapping 'GetFrame' method */
    mcsCOMPL_STAT oidataOI_ARRAY_GetFrame(void *obj, mcsSTRING32 frame)
    {
        oidataOI_ARRAY *o = (oidataOI_ARRAY *) obj;
        return o->GetFrame(frame);
    }

/* Wrapping 'GetArrayX' method */
    mcsCOMPL_STAT oidataOI_ARRAY_GetArrayX(void *obj, mcsDOUBLE * arrayX)
    {
        oidataOI_ARRAY *o = (oidataOI_ARRAY *) obj;
        return o->GetArrayX(arrayX);
    }

/* Wrapping 'GetArrayY' method */
    mcsCOMPL_STAT oidataOI_ARRAY_GetArrayY(void *obj, mcsDOUBLE * arrayY)
    {
        oidataOI_ARRAY *o = (oidataOI_ARRAY *) obj;
        return o->GetArrayY(arrayY);
    }

/* Wrapping 'GetArrayZ' method */
    mcsCOMPL_STAT oidataOI_ARRAY_GetArrayZ(void *obj, mcsDOUBLE * arrayZ)
    {
        oidataOI_ARRAY *o = (oidataOI_ARRAY *) obj;
        return o->GetArrayZ(arrayZ);
    }

/* Wrapping 'GetTelName' method */
    mcsCOMPL_STAT oidataOI_ARRAY_GetTelName(void *obj, mcsSTRING16 * telName)
    {
        oidataOI_ARRAY *o = (oidataOI_ARRAY *) obj;
        return o->GetTelName(telName);
    }

/* Wrapping 'GetStaName' method */
    mcsCOMPL_STAT oidataOI_ARRAY_GetStaName(void *obj, mcsSTRING16 * staName)
    {
        oidataOI_ARRAY *o = (oidataOI_ARRAY *) obj;
        return o->GetStaName(staName);
    }

/* Wrapping 'GetStaIndex' method */
    mcsCOMPL_STAT oidataOI_ARRAY_GetStaIndex(void *obj, mcsINT16 * staIndex)
    {
        oidataOI_ARRAY *o = (oidataOI_ARRAY *) obj;
        return o->GetStaIndex(staIndex);
    }

/* Wrapping 'GetDiameter' method */
    mcsCOMPL_STAT oidataOI_ARRAY_GetDiameter(void *obj, mcsFLOAT * diameter)
    {
        oidataOI_ARRAY *o = (oidataOI_ARRAY *) obj;
        return o->GetDiameter(diameter);
    }

/* Wrapping 'GetStaXYZ' method */
    mcsCOMPL_STAT oidataOI_ARRAY_GetStaXYZ(void *obj, mcsDOUBLE * staXYZ)
    {
        oidataOI_ARRAY *o = (oidataOI_ARRAY *) obj;
        return o->GetStaXYZ(staXYZ);
    }

/* Wrapping 'GetNumberOfElements' method */
    mcsINT16 oidataOI_ARRAY_GetNumberOfElements(void *obj)
    {
        oidataOI_ARRAY *o = (oidataOI_ARRAY *) obj;
        return o->GetNumberOfElements();
    }

/* 
 * Wrapping 'oidataDATA_TABLE' class 
 */

/* constructor 'oidataDATA_TABLE' method */

    void oidataDATA_TABLE_new(void **obj, oidataFITS * fitsParent,
                              mcsSTRING32 extName, mcsINT16 hduIndex)
    {
        *obj = new oidataDATA_TABLE(fitsParent, extName, hduIndex);
    }


/* destructor 'oidataDATA_TABLE' method */

    void oidataDATA_TABLE_delete(void *obj)
    {
        oidataDATA_TABLE *o = (oidataDATA_TABLE *) obj;
        delete o;
    }

/* Wrapping 'GetOiWavelengthTable' method */
    mcsCOMPL_STAT oidataDATA_TABLE_GetOiWavelengthTable(void *obj,
                                                        oidataOI_WAVELENGTH
                                                        ** table)
    {
        oidataDATA_TABLE *o = (oidataDATA_TABLE *) obj;
        return o->GetOiWavelengthTable(table);
    }

/* Wrapping 'GetOiArrayTable' method */
    mcsCOMPL_STAT oidataDATA_TABLE_GetOiArrayTable(void *obj,
                                                   oidataOI_ARRAY ** table)
    {
        oidataDATA_TABLE *o = (oidataDATA_TABLE *) obj;
        return o->GetOiArrayTable(table);
    }

/* Wrapping 'GetNWave' method */
    mcsINT16 oidataDATA_TABLE_GetNWave(void *obj)
    {
        oidataDATA_TABLE *o = (oidataDATA_TABLE *) obj;
        return o->GetNWave();
    }

/* Wrapping 'ProposeReference' method */
    mcsCOMPL_STAT oidataDATA_TABLE_ProposeReference(void *obj,
                                                    oidataOI_TABLE * table)
    {
        oidataDATA_TABLE *o = (oidataDATA_TABLE *) obj;
        return o->ProposeReference(table);
    }

/* Wrapping 'GetDateObs' method */
    mcsCOMPL_STAT oidataDATA_TABLE_GetDateObs(void *obj, mcsSTRING32 dateObs)
    {
        oidataDATA_TABLE *o = (oidataDATA_TABLE *) obj;
        return o->GetDateObs(dateObs);
    }

/* Wrapping 'GetArrName' method */
    mcsCOMPL_STAT oidataDATA_TABLE_GetArrName(void *obj, mcsSTRING32 arrName)
    {
        oidataDATA_TABLE *o = (oidataDATA_TABLE *) obj;
        return o->GetArrName(arrName);
    }

/* Wrapping 'GetInsName' method */
    mcsCOMPL_STAT oidataDATA_TABLE_GetInsName(void *obj, mcsSTRING32 insName)
    {
        oidataDATA_TABLE *o = (oidataDATA_TABLE *) obj;
        return o->GetInsName(insName);
    }

/* Wrapping 'GetTargetId' method */
    mcsCOMPL_STAT oidataDATA_TABLE_GetTargetId(void *obj, mcsINT16 * targetId)
    {
        oidataDATA_TABLE *o = (oidataDATA_TABLE *) obj;
        return o->GetTargetId(targetId);
    }

/* Wrapping 'GetTime' method */
    mcsCOMPL_STAT oidataDATA_TABLE_GetTime(void *obj, mcsDOUBLE * time)
    {
        oidataDATA_TABLE *o = (oidataDATA_TABLE *) obj;
        return o->GetTime(time);
    }

/* Wrapping 'GetMJD' method */
    mcsCOMPL_STAT oidataDATA_TABLE_GetMJD(void *obj, mcsDOUBLE * mjd)
    {
        oidataDATA_TABLE *o = (oidataDATA_TABLE *) obj;
        return o->GetMJD(mjd);
    }

/* Wrapping 'GetIntTime' method */
    mcsCOMPL_STAT oidataDATA_TABLE_GetIntTime(void *obj, mcsDOUBLE * intTime)
    {
        oidataDATA_TABLE *o = (oidataDATA_TABLE *) obj;
        return o->GetIntTime(intTime);
    }

/* Wrapping 'GetStaIndex' method */
    mcsCOMPL_STAT oidataDATA_TABLE_GetStaIndex(void *obj, mcsINT16 * staIndex)
    {
        oidataDATA_TABLE *o = (oidataDATA_TABLE *) obj;
        return o->GetStaIndex(staIndex);
    }

/* Wrapping 'GetFlag' method */
    mcsCOMPL_STAT oidataDATA_TABLE_GetFlag(void *obj, char *flag)
    {
        oidataDATA_TABLE *o = (oidataDATA_TABLE *) obj;
        return o->GetFlag(flag);
    }

/* 
 * Wrapping 'oidataOI_VIS' class 
 */

/* constructor 'oidataOI_VIS' method */

    void oidataOI_VIS_new(void **obj, oidataFITS * fitsParent,
                          mcsSTRING32 extName, mcsINT16 hduIndex)
    {
        *obj = new oidataOI_VIS(fitsParent, extName, hduIndex);
    }


/* destructor 'oidataOI_VIS' method */

    void oidataOI_VIS_delete(void *obj)
    {
        oidataOI_VIS *o = (oidataOI_VIS *) obj;
        delete o;
    }

/* Wrapping 'GetVisAmp' method */
    mcsCOMPL_STAT oidataOI_VIS_GetVisAmp(void *obj, mcsDOUBLE * visAmp)
    {
        oidataOI_VIS *o = (oidataOI_VIS *) obj;
        return o->GetVisAmp(visAmp);
    }

/* Wrapping 'GetVisAmpErr' method */
    mcsCOMPL_STAT oidataOI_VIS_GetVisAmpErr(void *obj, mcsDOUBLE * visAmpErr)
    {
        oidataOI_VIS *o = (oidataOI_VIS *) obj;
        return o->GetVisAmpErr(visAmpErr);
    }

/* Wrapping 'GetVisPhi' method */
    mcsCOMPL_STAT oidataOI_VIS_GetVisPhi(void *obj, mcsDOUBLE * visPhi)
    {
        oidataOI_VIS *o = (oidataOI_VIS *) obj;
        return o->GetVisPhi(visPhi);
    }

/* Wrapping 'GetVisPhiErr' method */
    mcsCOMPL_STAT oidataOI_VIS_GetVisPhiErr(void *obj, mcsDOUBLE * visPhiErr)
    {
        oidataOI_VIS *o = (oidataOI_VIS *) obj;
        return o->GetVisPhiErr(visPhiErr);
    }

/* Wrapping 'GetUCoord' method */
    mcsCOMPL_STAT oidataOI_VIS_GetUCoord(void *obj, mcsDOUBLE * uCoord)
    {
        oidataOI_VIS *o = (oidataOI_VIS *) obj;
        return o->GetUCoord(uCoord);
    }

/* Wrapping 'GetVCoord' method */
    mcsCOMPL_STAT oidataOI_VIS_GetVCoord(void *obj, mcsDOUBLE * vCoord)
    {
        oidataOI_VIS *o = (oidataOI_VIS *) obj;
        return o->GetVCoord(vCoord);
    }

/* 
 * Wrapping 'oidataOI_VIS2' class 
 */

/* constructor 'oidataOI_VIS2' method */

    void oidataOI_VIS2_new(void **obj, oidataFITS * fitsParent,
                           mcsSTRING32 extName, mcsINT16 hduIndex)
    {
        *obj = new oidataOI_VIS2(fitsParent, extName, hduIndex);
    }


/* destructor 'oidataOI_VIS2' method */

    void oidataOI_VIS2_delete(void *obj)
    {
        oidataOI_VIS2 *o = (oidataOI_VIS2 *) obj;
        delete o;
    }

/* Wrapping 'GetVis2Data' method */
    mcsCOMPL_STAT oidataOI_VIS2_GetVis2Data(void *obj, mcsDOUBLE * vis2Data)
    {
        oidataOI_VIS2 *o = (oidataOI_VIS2 *) obj;
        return o->GetVis2Data(vis2Data);
    }

/* Wrapping 'GetVis2Err' method */
    mcsCOMPL_STAT oidataOI_VIS2_GetVis2Err(void *obj, mcsDOUBLE * vis2Err)
    {
        oidataOI_VIS2 *o = (oidataOI_VIS2 *) obj;
        return o->GetVis2Err(vis2Err);
    }

/* Wrapping 'GetUCoord' method */
    mcsCOMPL_STAT oidataOI_VIS2_GetUCoord(void *obj, mcsDOUBLE * uCoord)
    {
        oidataOI_VIS2 *o = (oidataOI_VIS2 *) obj;
        return o->GetUCoord(uCoord);
    }

/* Wrapping 'GetVCoord' method */
    mcsCOMPL_STAT oidataOI_VIS2_GetVCoord(void *obj, mcsDOUBLE * vCoord)
    {
        oidataOI_VIS2 *o = (oidataOI_VIS2 *) obj;
        return o->GetVCoord(vCoord);
    }

/* 
 * Wrapping 'oidataOI_T3' class 
 */

/* constructor 'oidataOI_T3' method */

    void oidataOI_T3_new(void **obj, oidataFITS * fitsParent,
                         mcsSTRING32 extName, mcsINT16 hduIndex)
    {
        *obj = new oidataOI_T3(fitsParent, extName, hduIndex);
    }


/* destructor 'oidataOI_T3' method */

    void oidataOI_T3_delete(void *obj)
    {
        oidataOI_T3 *o = (oidataOI_T3 *) obj;
        delete o;
    }

/* Wrapping 'GetT3Amp' method */
    mcsCOMPL_STAT oidataOI_T3_GetT3Amp(void *obj, mcsDOUBLE * t3Amp)
    {
        oidataOI_T3 *o = (oidataOI_T3 *) obj;
        return o->GetT3Amp(t3Amp);
    }

/* Wrapping 'GetT3AmpErr' method */
    mcsCOMPL_STAT oidataOI_T3_GetT3AmpErr(void *obj, mcsDOUBLE * t3AmpErr)
    {
        oidataOI_T3 *o = (oidataOI_T3 *) obj;
        return o->GetT3AmpErr(t3AmpErr);
    }

/* Wrapping 'GetT3Phi' method */
    mcsCOMPL_STAT oidataOI_T3_GetT3Phi(void *obj, mcsDOUBLE * t3Phi)
    {
        oidataOI_T3 *o = (oidataOI_T3 *) obj;
        return o->GetT3Phi(t3Phi);
    }

/* Wrapping 'GetT3PhiErr' method */
    mcsCOMPL_STAT oidataOI_T3_GetT3PhiErr(void *obj, mcsDOUBLE * t3PhiErr)
    {
        oidataOI_T3 *o = (oidataOI_T3 *) obj;
        return o->GetT3PhiErr(t3PhiErr);
    }

/* Wrapping 'GetU1Coord' method */
    mcsCOMPL_STAT oidataOI_T3_GetU1Coord(void *obj, mcsDOUBLE * u1Coord)
    {
        oidataOI_T3 *o = (oidataOI_T3 *) obj;
        return o->GetU1Coord(u1Coord);
    }

/* Wrapping 'GetV1Coord' method */
    mcsCOMPL_STAT oidataOI_T3_GetV1Coord(void *obj, mcsDOUBLE * v1Coord)
    {
        oidataOI_T3 *o = (oidataOI_T3 *) obj;
        return o->GetV1Coord(v1Coord);
    }

/* Wrapping 'GetU2Coord' method */
    mcsCOMPL_STAT oidataOI_T3_GetU2Coord(void *obj, mcsDOUBLE * u2Coord)
    {
        oidataOI_T3 *o = (oidataOI_T3 *) obj;
        return o->GetU2Coord(u2Coord);
    }

/* Wrapping 'GetV2Coord' method */
    mcsCOMPL_STAT oidataOI_T3_GetV2Coord(void *obj, mcsDOUBLE * v2Coord)
    {
        oidataOI_T3 *o = (oidataOI_T3 *) obj;
        return o->GetV2Coord(v2Coord);
    }

/* 
 * Wrapping 'oidataOI_WAVELENGTH' class 
 */

/* constructor 'oidataOI_WAVELENGTH' method */

    void oidataOI_WAVELENGTH_new(void **obj, oidataFITS * fitsParent,
                                 mcsSTRING32 extName, mcsINT16 hduIndex)
    {
        *obj = new oidataOI_WAVELENGTH(fitsParent, extName, hduIndex);
    }


/* destructor 'oidataOI_WAVELENGTH' method */

    void oidataOI_WAVELENGTH_delete(void *obj)
    {
        oidataOI_WAVELENGTH *o = (oidataOI_WAVELENGTH *) obj;
        delete o;
    }

/* Wrapping 'GetInsName' method */
    mcsCOMPL_STAT oidataOI_WAVELENGTH_GetInsName(void *obj,
                                                 mcsSTRING32 insname)
    {
        oidataOI_WAVELENGTH *o = (oidataOI_WAVELENGTH *) obj;
        return o->GetInsName(insname);
    }

/* Wrapping 'GetEffWave' method */
    mcsCOMPL_STAT oidataOI_WAVELENGTH_GetEffWave(void *obj,
                                                 mcsFLOAT * effWave)
    {
        oidataOI_WAVELENGTH *o = (oidataOI_WAVELENGTH *) obj;
        return o->GetEffWave(effWave);
    }

/* Wrapping 'GetEffBand' method */
    mcsCOMPL_STAT oidataOI_WAVELENGTH_GetEffBand(void *obj,
                                                 mcsFLOAT * effBand)
    {
        oidataOI_WAVELENGTH *o = (oidataOI_WAVELENGTH *) obj;
        return o->GetEffBand(effBand);
    }

}
