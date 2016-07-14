# splunk8s
Splunk on kubernetes. Currently working on search head clustering.

 * Docker image is ghendrey/splunk
 * To run docker image without kubernetes: docker run ghendrey/splunk [shc-cap|shc-mbr]
 * To run splunk deploymen in kubernetes: kubectl -f splunk.yml
