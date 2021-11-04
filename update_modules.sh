#!/bin/bash
cd build

#set -ux;
set -e

for mod in jmcs oitools jmal oiexplorer-core
do
  cd $mod
  git pull
  cd -
done

echo "Done."

