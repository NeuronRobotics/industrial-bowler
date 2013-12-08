#!/bin/sh
if ps -ef | grep -v grep | grep java ; then
        echo "[tanuryMonitor]\tEverything is fine!"
	exit 0
else
	echo "[tanuryMonitor]\tStarting Bath Monitor" | wall
	echo "[tanuryMonitor] Uh Oh.. I don't see the logger... Starting logger."
	java -jar /TanuryDeviceServer.jar /dev/DyIO0  >/dev/null 2 >&1  &
	echo "[tanuryMonitor] Done!"
fi
