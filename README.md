# splunk8s
Splunk on kubernetes. Currently working on search head clustering.

 * Docker image is ghendrey/splunk
 * To run docker image without kubernetes: docker run ghendrey/splunk
 * To run specific command on docker image without kubernetes: docker run ghendrey/splunk:latest /sbin/my_init "java -Xmx1024m -jar /kubclient-1.0-SNAPSHOT.jar shc-mbr"
    * as you can see I use /sbin/myinit which solves "docker PID 1 Zombie problem" to launch a java wrapper program which in terms calls entrypoint.sh (from outcoldman/splunk)
 * To run splunk deploymen in kubernetes: kubectl -f splunk.yml --validate=false
