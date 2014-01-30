HOMEDIR=$PWD
NRSDK=../../../java-bowler/javasdk/NRSDK/
cd $NRSDK; 
ant; 
cd $HOMEDIR;
cp $NRSDK/target/nrsdk-3.9.1-jar-with-dependencies.jar lib/; 
ant;
java -jar target/TanuryDeviceServer.jar /dev/DyIO0
