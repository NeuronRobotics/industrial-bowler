cd ../NRSDK; 
ant; 
cd ../TanuryGoldLogger/;
cp ../NRSDK/target/nrsdk-3.9.1-jar-with-dependencies.jar lib/; 
ant;
#java -jar target/TanuryDeviceServer.jar /dev/DyIO0