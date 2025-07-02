
FROM tomcat:10.1-jdk21
COPY /build/libs/*.war /usr/local/tomcat/webapps/
EXPOSE 8080
CMD ["catalina.sh", "run"]