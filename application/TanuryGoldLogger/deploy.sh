
#Do this if you have not set up the ssh keys
#ssh-keygen -t dsa

java -jar FindDevices.jar
HOME=$PWD

NRSDK=../../../java-bowler/javasdk/NRSDK/
NR_JAR=$NRSDK/target/nrsdk-*-jar-with-dependencies.jar

cd $NRSDK
#ant

cd $HOME
rm -rf lib/nrsdk-*-jar-with-dependencies.jar
if (test -e $NR_JAR ); then
	cp $NR_JAR lib/; 
else 
	echo no jar at $NR_JAR
	exit 1;
fi

#build the sources
if ant; then
	java -jar FindDevices.jar
	bash pushJar.sh $(java -jar FindDevices.jar);
	#bash pushJar.sh 192.168.3.102 192.168.3.110 192.168.3.111 192.168.3.112 192.168.3.115;
	echo Restarting application...;
	#this waits for the crontab to have run and the application starts back up;
	sleep 105 ;
	echo Looking for devices...;
	java -jar FindDevices.jar;
	#java -jar TanuryGUI.jar;
fi
