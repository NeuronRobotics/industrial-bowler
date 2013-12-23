#!/bin/sh
if ps -ef | grep -v grep | grep java ; then
        echo "[tanuryMonitor]\tEverything is fine!"
	exit 0
else
	logger "Bath process not detected. Restarting."
	echo "[tanuryMonitor]\tStarting Bath Monitor" | wall
	echo "[tanuryMonitor] Uh Oh.. I don't see the logger... Starting logger."
	now=$(date +"%Y-%m-%d-%H:%M:%S")
	java -jar /TanuryDeviceServer.jar >/var/log/Tanury/tanury$now 2>&1 &
	ln -sf tanury$now /var/log/Tanury/tanury.current
	sleep 10
	if ps -ef | grep -v grep | grep java ; then
		logger "  Logger is back!"
	else
		logger "  Logger Still down!"
	fi
	echo "[tanuryMonitor] Done!"
fi
