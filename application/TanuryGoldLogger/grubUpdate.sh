
for VAR in "$@"
do
	echo Found $VAR
	
	# update boot process and bootloader to prevent hangs on boot
	#scp rcS root@$VAR:/etc/default/
	
	#this won't work unless you run boot-repair
	#scp grub root@$VAR:/etc/default/
	#ssh root@$VAR 'add-apt-repository ppa:yannubuntu/boot-repair'
	#ssh root@$VAR 'apt-get update'
	#ssh root@$VAR 'apt-get install -y boot-repair'
	#ssh -XC root@$VAR 'boot-repair' 
	#ssh root@$VAR 'update-grub2'
	#ssh root@$VAR 'reboot'
	ssh -XC root@$VAR 'boot-repair -v' 
done

