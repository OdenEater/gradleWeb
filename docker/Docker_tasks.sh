##未完成なので続きやります。

##Docker有効化(AlmaLinux)



dnf update -y
dnf install -y dnf-plugins-core
dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
dnf install -y docker-ce docker-ce-cli containerd.io

systemctl start docker

docker --version
#ここでDockerのバージョンが表示されればOK

##Docker起動まで
docker pull tomcat:10.1

docker run -d --name my-tomcat -p 8080:8080 tomcat:10.1

WARDIR=/mnt/c/Users/KattaMiyamoto/IdeaProjects/gradleWEB/build/libs/
WARFILE=myapp.war

docker cp $WARDIR/myapp.war my-tomcat:/usr/local/tomcat/webapps/

##アクセスURL http://localhost:8080/myapp/hello.html

#コンテナログイン



##clean

docker stop my-tomcat
docker rm my-tomcat
