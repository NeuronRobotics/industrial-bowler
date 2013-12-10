#!/bin/sh
if ps -ef | grep -v grep | grep java ; then
        logger "Hourly Status. Logger is fine"
	exit 0
else
	logger "Hourly Status. Logger is down"

fi
