#!/bin/bash

for f in *.md; do 
        
    echo '######################'
    echo "Processing file : $f "

    NAME=`basename $f .md`
    pandoc --standalone --metadata pagetitle="${NAME}" ${NAME}.md > ${NAME}.html
done

