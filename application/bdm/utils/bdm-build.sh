#!/bin/bash
# run from /home/ash/Store/Android/android
# https://wiki.linaro.org/Platform/Android/GetSource
# https://wiki.linaro.org/Platform/Android/BuildSource
export MANIFEST_REPO=git://github.com/gumdroid/android-manifest.git
export MANIFEST_BRANCH=linaro
export MANIFEST_FILENAME=default.xml
export TARGET_PRODUCT=overo
export TARGET_SIMULATOR=false
export TARGET_TOOLS_PREFIX=$PWD/utils/android-toolchain-eabi/bin/arm-eabi-


repo init -u ${MANIFEST_REPO} -b ${MANIFEST_BRANCH} -m ${MANIFEST_FILENAME}
repo sync -j4

if (test -d out/target/common/docs/gen) then
	echo dir ok
else
	mkdir -p out/target/common/docs/gen
fi

if (test -d frameworks/base/frameworks/base/docs/html) then
	echo dir ok
else
	mkdir -p frameworks/base/frameworks/base/docs/html
fi
#this sets up the x/y swap
cp kernel/drivers/input/touchscreen/ads7846.c utils/backup/ads7846.c.$(date +'%F_%T')
cp utils/linero-overo/ads7846.c kernel/drivers/input/touchscreen/  

#this adds the kernel bindings for the 3.4inch screen
cp kernel/arch/arm/configs/android_omap3_defconfig utils/backup/android_omap3_defconfig.$(date +'%F_%T')
cp utils/linero-overo/android_omap3_defconfig kernel/arch/arm/configs 

#this adds the mapping for the buttons
cp device/linaro/overo/gpio-keys.kl utils/backup/gpio-keys.kl.$(date +'%F_%T')
cp utils/linero-overo/gpio-keys.kl device/linaro/overo/gpio-keys.kl 

#this adds overo edits
cp kernel/arch/arm/mach-omap2/board-overo.c utils/backup/board-overo.c.$(date +'%F_%T')
cp utils/linero-overo/board-overo.c kernel/arch/arm/mach-omap2/board-overo.c

sudo chmod -R 777 *
if (make boottarball systemtarball userdatatarball ) then
	sudo chmod -R 777 *
	sudo linaro-android-media-create --mmc /dev/sdc --dev beagle --system out/target/product/overo/system.tar.bz2 --userdata out/target/product/overo/userdata.tar.bz2 --boot out/target/product/overo/boot.tar.bz2
	if (test -d mnt) then
		echo dir ok
	else
		mkdir -p mnt
	fi
	sudo mount /dev/sdc1 ./mnt
	sudo cp utils/linero-overo/boot.scr ./mnt/
	sudo cp utils/linero-overo/MLO ./mnt/
	sudo cp utils/linero-overo/u-boot.bin ./mnt/
	sudo umount ./mnt/
else
	echo "[make failed]"
fi
