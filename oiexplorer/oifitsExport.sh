#!/bin/bash
#
# CLI OIFitsExplorer to export OIFITS as png using oixp templates (overview)
#
# | [-open <file>]               Open the given file (if supported)              |
# |------------------------------------------------------------------------------|
# | [-pdf]                    export plots to the given file (PDF format) [SHELL]    |
# | [-png]                    export plots to the given file (PNG format) [SHELL]    |
# | [-jpg]                    export plots to the given file (JPG format) [SHELL]    |
# | [-mode]                   export mode [multi|single] page    |
# | [-dims]                   export image dimensions [width,height] [SHELL]    |
#
# java -cp ./target/oiexplorer-TRUNK-jar-with-dependencies.jar fr.jmmc.oiexplorer.OIFitsExplorer -png test.png -mode=single -dims 1200,800 -open test.oixp 
#
# Extra flags: -Dtarget.matcher.name=true -Dinsmode.matcher.name=true -Dfix.bad.uid=true
#
#

function genOIXP(){
  OIFITS="${1}"
  OIXP_TMPL="${2}"
  OIXP="${3}"
  TargetUID="${4}"
  InsModeUID="${5}"
  NightId="${6}"

  cp export_templates/$OIXP_TMPL $OIXP

  # update fields
  xmlstarlet ed -L -u "//file/name" -v "$(basename $OIFITS)" -u "//file/file" -v "$OIFITS" -u "//filter/targetUID" -v "$TargetUID" -u "//filter/insModeUID" -v "$InsModeUID" -u "//filter/nightID" -v "$NightId" $OIXP
}

#
#
#
function genPNG(){
  OIXP="${1}"
  PNG="${OIXP/.oixp/.png}"
  java -cp ./target/oiexplorer-TRUNK-jar-with-dependencies.jar fr.jmmc.oiexplorer.OIFitsExplorer -png $PNG -mode=single -dims 1200,800 -open $OIXP
}

OIFITS="${1}"

if [ ! -e "$OIFITS" ]
then
    echo "ERROR: bad file : $OIFITS"
    exit 1;
fi

# Filters (granule fields)
# TODO: fix values only applicable to /iota/MWC275.2004Mar-Jun.A35B15C10.oifits
TargetUID="MWC275"
InsModeUID="IONIC3"
NightId="53090"

OIXP="low.oixp"
genOIXP $OIFITS 'oidb-template-low_res.oixp' $OIXP $TargetUID $InsModeUID $NightId
genPNG $OIXP

OIXP="med_high.oixp"
genOIXP $OIFITS 'oidb-template-med_high_res.oixp' $OIXP $TargetUID $InsModeUID $NightId
genPNG $OIXP

