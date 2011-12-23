#!/bin/bash

ORIG_PWD=${PWD}
TARDIR=${PWD}/out/target/product/overo/

cd ${TARDIR}
if [ -e android_rootfs ]; then
    echo "[remove old root directory]"
    rm -rf android_rootfs
fi
echo "[make fresh root directory]"
mkdir android_rootfs
echo "[copy root file system]"
cp -r root/* android_rootfs
echo "[copy system folder]"
cp -r system android_rootfs
echo "[build tarball]"
../../../../build/tools/mktarball.sh ../../../host/linux-x86/bin/fs_get_stats android_rootfs . rootfs rootfs.tar.bz2
cd ${ORIG_PWD}
echo "[DONE!]"
