#!/bin/bash

if type -p pandoc
then
   for f in *.md; do 
       NAME=`basename $f .md`

       if [ "${NAME}.md" -nt "${NAME}.html" ]; then
           echo '######################'
           echo "Processing file : $f "

           pandoc --standalone --metadata pagetitle="${NAME}" ${NAME}.md > ${NAME}.html
       fi
   done
else
    echo "pandoc command was not found. Did not parse any markdown documentation file."
fi

