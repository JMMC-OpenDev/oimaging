#!/bin/bash
mkdir -p build
cd build

set -ux;

# jmcs
git clone --depth 1 -b master https://github.com/JMMC-OpenDev/jmcs.git

# oitools
git clone --depth 1 -b master https://github.com/JMMC-OpenDev/oitools.git

# jmal
git clone --depth 1 -b master https://github.com/JMMC-OpenDev/jmal.git

# oiexplorer core
git clone --depth 1 -b master https://github.com/JMMC-OpenDev/oiexplorer-core.git

echo "Done."

