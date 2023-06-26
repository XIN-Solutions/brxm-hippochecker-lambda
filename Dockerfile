FROM amazoncorretto:11

COPY target/*jar-with-dependencies.jar /var/app/brxm-janitor.jar
COPY cronlauncher /opt/cronlauncher

   # Install application
RUN yum install -y crontabs
RUN chmod +x /opt/cronlauncher

ENTRYPOINT ["/opt/cronlauncher"]
