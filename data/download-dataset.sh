#!/bin/bash

DATASET="simplewiki"
DUMPVERSION=20170501
MIRROR="https://dumps.wikimedia.org"

WANTED=(
page.sql.gz
pagelinks.sql.gz
pages-meta-current.xml.bz2
all-titles.gz
)


for W in ${WANTED[@]} ; do
  if [[ -f "$DATASET-$DUMPVERSION-$W" ]]; then
    echo "$DATASET-$DUMPVERSION-$W exists, skipping"
  else
    curl -O "$MIRROR/$DATASET/$DUMPVERSION/$DATASET-$DUMPVERSION-$W"
  fi
done


# Fancy but unnecessary:
#WANTED=(
#pagetable
#)
# for W in ${WANTED[@]} ; do
#   URL=$(cat dumpstatus.json | jq '.jobs|to_entries[] | select(.key=="'$W'").value.files[] | .url' | tr -d '"')
#   SHA1=$(cat dumpstatus.json | jq '.jobs|to_entries[] | select(.key=="'$W'").value.files[] | .sha1' | tr -d '"')
#   curl "$MIRROR/$URL" -z $W -o $W
# done

