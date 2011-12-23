#!/bin/bash
# taken from rowboat packaging script

#=== MODIFY ME ===
OUTDIR="out/target/product/overo"
MLO="MLO"
UBOOT="u-boot.bin"
KERNEL="uImage"
ROOTFS="rootfs.tar.bz2"
SCRIPT="boot.scr"
MEDIA="Media"

#==== JUST CODE HERE :) ===
EXPECTED_ARGS=1
if [ $# == $EXPECTED_ARGS ]; then
	echo "Assuming images found in ${OUTDIR}."
	$0 $1 ${OUTDIR}
	exit
fi

if [[ -z $1 || -z $2 ]]; then
        echo "This utility creates a bootable microSD card given a set of files."
        echo "These file names are expected:"
        echo "  ${MLO}, ${UBOOT}, ${KERNEL}, ${ROOTFS} and, optionally,"
        echo -e "  ${SCRIPT} and a ${MEDIA} directory.\n"
	echo "Usage:"
	echo "	$0 <device> [path to files]"
	echo "Example:"
	echo "	sudo mkandroidsd.sh /dev/mmcblk0 out/target/product/overo"
	exit
fi
OUTDIR=$2

if ! [[ -e ${OUTDIR}/${MLO} ]]; then
	echo "No ${MLO} found! Quitting..."
	exit
fi
if ! [[ -e ${OUTDIR}/${UBOOT} ]]; then
	echo "No ${UBOOT} found! Quitting..."
	exit
fi
if ! [[ -e ${OUTDIR}/${KERNEL} ]]; then
	echo "No ${KERNEL} found! Quitting..."
	exit
fi
if ! [[ -e ${OUTDIR}/${ROOTFS} ]]; then
	echo "No ${ROOTFS} found! Quitting..."
	exit
fi

echo -n "All data on "$1" now will be destroyed! Continue? [y/n]: "
read ans
if ! [ $ans == 'y' ]; then
	exit
fi

echo "[Unmounting all existing partitions on the device ]"
umount $11 &> /dev/null
umount $12 &> /dev/null
umount $13 &> /dev/null
umount $1p1 &> /dev/null
umount $1p2 &> /dev/null
umount $1p3 &> /dev/null

# Cylinders are approx. 8MB each (8225280 bytes to be precise)
echo "[Partitioning $1...]"
DRIVE=$1
dd if=/dev/zero of=$DRIVE bs=1024 count=1024 &>/dev/null
SIZE=`fdisk -l $DRIVE | grep Disk | awk '{print $5}'`
echo DISK SIZE - $SIZE bytes
CYLINDERS=`echo $SIZE/255/63/512 | bc`
echo CYLINDERS - $CYLINDERS
{
echo ,9,0x0C,*
echo ,120,,-
echo ,,0x0C,-
} | sfdisk -D -H 255 -S 63 -C $CYLINDERS $DRIVE &> /dev/null

echo "[Making boot partition...]"
if [ -b ${1}1 ]; then
    mkfs.vfat -F 32 -n boot "$1"1 &> /dev/null
    mount "$1"1 /mnt
else
    mkfs.vfat -F 32 -n boot "$1"p1 &> /dev/null
    mount "$1"p1 /mnt
fi
cp ${OUTDIR}/${MLO} /mnt/MLO
cp ${OUTDIR}/${UBOOT} /mnt/u-boot.bin
cp ${OUTDIR}/${KERNEL} /mnt/uImage
if [[ -e ${OUTDIR}/${SCRIPT} ]]; then
    cp ${OUTDIR}/${SCRIPT} /mnt/boot.scr
fi
umount /mnt

echo "[Making rootfs partition...]"
if [ -b ${1}2 ]; then
    mkfs.ext3 -L rootfs "$1"2 &> /dev/null
    mount "$1"2 /mnt
else
    mkfs.ext3 -L rootfs "$1"p2 &> /dev/null
    mount "$1"p2 /mnt
fi
tar jxvf ${OUTDIR}/${ROOTFS} -C /mnt &> /dev/null
umount /mnt

echo "[Making data partition]"
if [ -b ${1}3 ]; then
    mkfs.vfat -F 32 -n data "$1"3 &> /dev/null
    mount "$1"3 /mnt
else
    mkfs.vfat -F 32 -n data "$1"p3 &> /dev/null
    mount "$1"p3 /mnt
fi
if [[ -e ${OUTDIR}/${MEDIA}/* ]]; then
    cp -r ${OUTDIR}/${MEDIA}/* /mnt
fi
umount /mnt
echo "[Done]"
