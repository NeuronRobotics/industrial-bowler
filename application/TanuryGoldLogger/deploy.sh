
#Do this if you have not set up the ssh keys
#ssh-keygen -t dsa
HOME=$PWD

cd ../NRSDK/
rm target/*.jar
ant
cd $HOME


#build the sources
if ant; then
	bash pushJar.sh $(java -jar FindDevices.jar);
	#bash pushJar.sh 192.168.3.102 192.168.3.110 192.168.3.111 192.168.3.112 192.168.3.115;
	echo Restarting application...;
	#this waits for the crontab to have run and the application starts back up;
	sleep 105 ;
	echo Looking for devices...;
	java -jar FindDevices.jar;
	#java -jar TanuryGUI.jar;
fi