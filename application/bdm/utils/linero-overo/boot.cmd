setenv initrd_high "0xffffffff"
setenv fdt_high "0xffffffff"
setenv bootcmd "fatload mmc 0:1 0x80000000 uImage; fatload mmc 0:1 0x81600000 uInitrd; bootm 0x80000000 0x81600000"
setenv bootargs "console=tty0 console=ttyO2,115200n8 rootwait ro earlyprintk fixrtc nocompcache vram=12M omapfb.mode=dvi:1280x720MR-16@60 omapdss.def_disp=lcd35 mpurate=${mpurate} init=/init androidboot.console=ttyO2 
"
boot
