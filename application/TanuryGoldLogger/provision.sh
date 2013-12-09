# needs .ssh directory in both root and tanury and ssh installed.
#	Run: sudo apt-get install openssh-server && sudo mkdir /root/.ssh && sudo mkdir /home/tanury/.ssh
#


echo "Enter root password to copy user ssh keys"
cat ~/.ssh/id_dsa.pub | ssh root@$1 'cat >> ~/.ssh/authorized_keys'
cat ~/.ssh/id_dsa.pub | ssh root@$1 'cat >> /home/tanury/.ssh/authorized_keys'

echo "installing java"
ssh root@$1 'sudo apt-get -y install openjdk-7-jre'
echo "installing TanuryServer launcher script"
cat crontabline | ssh root@$1 'cat >> /etc/crontab'
echo "deploying jar"
bash pushJar.sh $1
