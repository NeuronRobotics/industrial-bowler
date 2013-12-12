for VAR in $(java -jar FindDevices.jar)
do
	echo Found $VAR
	echo Uploading keys to $VAR;
	scp ~/.ssh/id_dsa.pub tanury@$VAR:.ssh/authorized_keys2
	scp ~/.ssh/id_dsa.pub root@$VAR:.ssh/authorized_keys2
	
done