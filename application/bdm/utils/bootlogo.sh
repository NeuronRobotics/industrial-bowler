#!/bin/bash

# This script generates a bootlogo for android given a source image matching
# the dimensions of your screen.  The source image can be any format that
# imagemagick suppports (jpg, png, gif....).
# You need imagemagick (convert) and rgb2565 (android utilities) on your path
# To build rgb2565, go to the top of your android source and issues these commands
#   $ source build/envsetup.sh
#   $ mmm build/tools/rgb2565
#   $ sudo cp out/host/linux-x86/bin/rgb2565 /usr/local/bin
OUTFILE=initlogo.rle.keep

if [[ $# < 1 ]]; then
  echo "USAGE:"
  echo "    $0 <source file of desired dimensions>"
  exit
fi
convert $1 -depth 8 -interlace none rgb:initlogo.raw
rgb2565 -rle < initlogo.raw > $OUTFILE
rm initlogo.raw
echo "Made $OUTFILE.  Copy this to your Android root file system."
