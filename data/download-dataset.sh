#!/bin/bash

DATASET="simplewiki"
DUMPVERSION=20170501
MIRROR="https://dumps.wikimedia.org"

WANTED=(
pages-articles.xml.bz2
sha1sums.txt
)


for W in ${WANTED[@]} ; do
  if [[ -f "$DATASET-$DUMPVERSION-$W" ]]; then
    echo "$DATASET-$DUMPVERSION-$W exists, skipping"
  else
    curl -O "$MIRROR/$DATASET/$DUMPVERSION/$DATASET-$DUMPVERSION-$W"
  fi
done

echo ""
echo "== Validating files"
sha1sum --ignore-missing -c "$DATASET-$DUMPVERSION-sha1sums.txt"
