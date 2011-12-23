if test ${bootdelay} -ne 1; then
  echo "Setting boot delay to 0"
  setenv bootdelay 0
  saveenv
fi

setenv anddisplay 'vram=12M omapfb.mode=dvi:1024x768MR-16@60 omapdss.def_disp=lcd35'
setenv andconsole 'console=ttyS2,115200n8 console=tty0 androidboot.console=ttyS2'
setenv androot 'root=/dev/mmcblk0p2 rw rootfstype=ext3 rootwait init=/init'
setenv andbootargs 'setenv bootargs mpurate=600 ${andconsole} ${androot} ${anddisplay}'

setenv bootcmd 'mmc init; fatload mmc 0 ${loadaddr} uImage; run andbootargs; bootm ${loadaddr}'
boot
