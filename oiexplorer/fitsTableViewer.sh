#!/bin/bash
#
# CLI FitsTableViewer
#
# list:
#   ./oifitsTableViewer.sh /home/bourgesl/oidata/2007-06-29.fits
#

java -cp ./target/oiexplorer-TRUNK-jar-with-dependencies.jar fr.jmmc.oiexplorer.core.gui.OIFitsTableBrowser $1
