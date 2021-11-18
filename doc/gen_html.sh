#!/bin/bash

if type -p pandoc
then
   for f in *.md; do 
           
       echo '######################'
       echo "Processing file : $f "

       NAME=`basename $f .md`
       pandoc --standalone --metadata pagetitle="${NAME}" ${NAME}.md > ${NAME}.html
   done
else
	echo "pandoc command was not found. Did not parse any markdown documentation file."
fi
