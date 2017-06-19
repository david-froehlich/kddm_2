#!/bin/bash

DATASET="enwiki"
DUMPVERSION=20170601
MIRROR="rsync://ftp.acc.umu.se/mirror/wikimedia.org/dumps"

WANTED=(
pages-articles.xml.bz2
sha1sums.txt
)


for W in ${WANTED[@]} ; do
  rsync -P "$MIRROR/$DATASET/$DUMPVERSION/$DATASET-$DUMPVERSION-$W" .
done

echo ""
echo "== Validating files"
sha1sum --ignore-missing -c "$DATASET-$DUMPVERSION-sha1sums.txt"
