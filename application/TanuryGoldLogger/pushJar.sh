
for VAR in "$@"
do
	echo Found $VAR
    #if  [ $(ssh pi@$VAR 'mkdir .ssh') ];  then
	#	echo Uploading keys $VAR;
	#	$(scp ~/.ssh/id_dsa.pub pi@$VAR:.ssh/authorized_keys2);	
	#fi;
	
	#scp setupRoot.sh pi@$VAR: 
	
	ssh pi@$VAR 'mkdir -p Tanury/device'
	
	ssh root@$VAR 'mount -o remount,rw /'
	
	scp target/TanuryDeviceServer.jar root@$VAR:/
	
	ssh root@$VAR 'ntptime;rpi-update'
	
	ssh root@$VAR 'mount -o remount,ro /'
	
	ssh root@$VAR 'pkill java'
done

