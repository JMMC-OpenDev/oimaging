#!/bin/bash
set -eux

MVN_OPTS="-Djarsigner.skip=true -Dmaven.test.skip=true -Dmaven.javadoc.skip=true"

rm -rf tmp && mkdir tmp && cd tmp

# 1. jmcs:
svn export https://svn.jmmc.fr/jmmc-sw/MCS/trunk/jmcs
cd jmcs

    # 1.1. jmcs/parent-pom:
    # first time only: install parent-pom and missing libraries in maven repositories:
    cd parent-pom
    mvn -Dassembly.skipAssembly -Djarsigner.skip=true clean install
    cd ..
    mvn process-resources

mvn $MVN_OPTS clean install 
cd ..

    # 1.2. testgui:
    # first time only ?
    svn export https://svn.jmmc.fr/jmmc-sw/MCS/trunk/testgui
    cd testgui
    mvn $MVN_OPTS clean install 
    cd ..


# 2. jmal:
svn export https://svn.jmmc.fr/jmmc-sw/MCS/trunk/jmal
cd jmal
mvn $MVN_OPTS clean install 
cd ..


# 3. oitools:
git clone https://github.com/JMMC-OpenDev/OITools.git
cd OITools
mvn $MVN_OPTS clean install 
cd ..


# 4. oiexplorer-core:
svn export https://svn.jmmc.fr/jmmc-sw/oiTools/trunk/oiexplorer-core
cd oiexplorer-core
mvn $MVN_OPTS clean install 
cd ..


# 5. oimaging:
# svn export https://svn.jmmc.fr/jmmc-sw/oiTools/trunk/oimaging
# cd oimaging
# mvn $MVN_OPTS clean install 
# cd ..

echo "build done"

