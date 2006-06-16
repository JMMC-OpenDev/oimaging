#*******************************************************************************
# JMMC project
#
# "@(#) $Id: Makefile,v 1.6 2005-09-15 09:23:18 mella Exp $"
#
# Makefile of oidata module
#
# History
# -------
# $Log: not supported by cvs2svn $
# Revision 1.5  2005/08/31 20:07:59  mella
# First revision
#
# Revision 1.4  2005/08/19 13:00:10  mella
# Give general name for C wrapper file
#
# Revision 1.3  2005/06/28 13:28:10  mella
# Add more include to install
#
# Revision 1.2  2005/06/24 14:01:59  mella
# minimum base
#
#*******************************************************************************
# REMARKS
#    Makefile based on the VLT/ALMA Makefile
#
#-------------------------------------------------------------------------------

#
# user definable C-compilation flags
USER_CFLAGS =  -DPLUG_IN

#
# user definable javac compilation flags
#USER_JFLAGS = 

#
# additional include and library search paths
USER_INC = -I/usr/local/include
USER_LIB = -L/usr/local/lib

#
# MODULE CODE DESCRIPTION:
# ------------------------
# As a general rule:  public file are "cleaned" and "installed"  
#                     local (_L) are not "installed".

#
# C programs (public and local)
# -----------------------------
EXECUTABLES     =
EXECUTABLES_L   = 

#
# <brief description of xxxxx program>
xxxxx_OBJECTS   =
xxxxx_LDFLAGS   =
xxxxx_LIBS      =

#
# special compilation flags for single c sources
#yyyyy_CFLAGS   = 

#
# Includes (.h) files (public only)
# ---------------------------------
INCLUDES        = oidata.h				\
				  oidataFITS.h			\
				  oidataDATA_TABLE.h 	\
				  oidataOI_ARRAY.h		\
				  oidataOI_T3.h			\
				  oidataOI_TABLE.h		\
				  oidataOI_TARGET.h		\
				  oidataOI_VIS.h		\
				  oidataOI_VIS2.h		\
				  oidataOI_WAVELENGTH.h	\
				  

#
# Libraries (public and local)
# ----------------------------
LIBRARIES       = oidata
LIBRARIES_L     =

#
# <brief description of lllll library>
oidata_OBJECTS   = oidataFITS \
				   oidataDATA_TABLE \
				   oidataOI_ARRAY	\
				   oidataOI_T3		\
				   oidataOI_TABLE	\
				   oidataOI_TARGET	\
				   oidataOI_VIS2	\
				   oidataOI_VIS	\
				   oidataOI_WAVELENGTH \
				   oidataAddFitsError_L \
				   oidata-wrap \

#
# Scripts (public and local)
# --------------------------
SCRIPTS         =
SCRIPTS_L       =

#
# TCL scripts (public and local)
# ------------------------------
TCL_SCRIPTS     =
TCL_SCRIPTS_L   =

#
# <brief description of tttttt tcl-script>
tttttt_OBJECTS  =
tttttt_TCLSH    = 
tttttt_LIBS     = 

#
# TCL libraries (public and local)
# --------------------------------
TCL_LIBRARIES   =
TCL_LIBRARIES_L =

#
# <brief description of tttlll library>
tttlll_OBJECTS  = 

#
# Python scripts (public and local)
# ---------------------------------
PY_SCRIPTS     =
PY_SCRIPTS_L   =

#
# Jarfiles and their directories
# ---------------------------------
JARFILES        =

#
# <brief description of jjjfff jar files>
jjjfff_DIRS     =
jjjfff_EXTRAS   = 

#
# Java programs
# ---------------------------------
JAVA_EXECUTABLES=

#
# <brief description of jjjjjj java program>
jjjjjj_CLASS    =

#
# Command Definition Files (CDF)
# ---------------------------------
CDF_FILES       =

#
# man pages to be done
# --------------------
MANSECTIONS =
MAN1 =
MAN3 =
MAN5 =
MAN7 =
MAN8 =

#
# other files to be installed
#----------------------------
INSTALL_FILES =

#
# list of all possible C-sources (used to create automatic dependencies)
# ----------------------------------------------------------------------
CSOURCENAMES = \
	$(foreach exe, $(EXECUTABLES) $(EXECUTABLES_L), $($(exe)_OBJECTS)) \
	$(foreach lib, $(LIBRARIES) $(LIBRARIES_L), $($(lib)_OBJECTS))

#
#>>>>> END OF standard rules

#
# INCLUDE STANDARDS
# -----------------
MAKEDIR = $(MCSROOT)/include
include $(MAKEDIR)/mkfMakefile

#
# TARGETS
# -------
all:	do_all
	@echo " . . . 'all' done" 

clean : clean_all 
	@echo " . . . clean done"

man   : do_man 
	@echo " . . . man page(s) done"

install : install_all
	@echo " . . . installation done"

oidata_wrap.xml: ../include/oidata.h
	swig -c++ -xml -includeall -ignoremissing -I../include -module oidata -o oidata_wrap.xml $<
	
oidata-wrap.cpp:	oidata_wrap.xml
	xsltproc -path "$(INTROOT)/config/:$(MCSROOT)/config/" mkfSTKToCWrapperForCpp.xsl $< > $@

oidata-wrap_wrap.xml:	oidata-wrap.cpp 
	swig -c++ -xml -includeall -ignoremissing -module oidata $< >	$@

oidata.i:	oidata-wrap_wrap.xml
	xsltproc -path "$(INTROOT)/config/:$(MCSROOT)/config/" mkfSTKToYorickWrapperForC.xsl $< > $@
#___oOo___
