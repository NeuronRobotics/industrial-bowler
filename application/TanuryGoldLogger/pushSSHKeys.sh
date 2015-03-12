for VAR in $(java -jar FindDevices.jar)
do
	echo Found $VAR
	echo Uploading keys to $VAR;
	cat ~/.ssh/id_rsa.pub | ssh root@$VAR 'cat >> /home/tanury/.ssh/authorized_keys'
	cat ~/.ssh/id_rsa.pub | ssh root@$VAR 'cat >> .ssh/authorized_keys'
	#scp ~/.ssh/id_dsa.pub tanury@$VAR:.ssh/authorized_keys2
	#scp ~/.ssh/id_dsa.pub root@$VAR:.ssh/authorized_keys2
	
done