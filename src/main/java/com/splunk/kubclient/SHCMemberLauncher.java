/*
 * Proprietary and confidential. Copyright Splunk 2015
 */
package com.splunk.kubclient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ghendrey
 */
public class SHCMemberLauncher {

	private static final Logger logger = LoggerFactory.getLogger(
			SHCMemberLauncher.class);

	public static void main(String[] args) throws UnknownHostException, IOException {

		System.out.println("Starting splunk member");
		String hostname = InetAddress.getLocalHost().getCanonicalHostName();
		ProcessBuilder pb = new ProcessBuilder("/sbin/entrypoint.sh",
				"start-service");
		String cmd = "init shcluster-config -auth admin:changeme -mgmt_uri http://" + hostname + ":8092 -replication_port 34570 -replication_factor 3 -shcluster_label shcluster-01";
		//String cmd = "help -auth admin:changeme ";

		pb.environment().put("SPLUNK_CMD_THEN_RESTART_0", cmd);
		pb.environment().put("SPLUNK_START_ARGS", "--accept-license");
		pb.environment().put("SPLUNK_RUN_PSTACK_ON_SHUTDOWN_HANG", "true");	
		pb.environment().put("SPLUNK_USER", "root");
		System.out.println("Environment:");
		System.out.println(pb.environment());
		pb.inheritIO();
		
			
			Process p = pb.start();
			pb.redirectErrorStream(true);
			
			try {
			p.waitFor();
			
			} catch (InterruptedException ex) {
			java.util.logging.Logger.
			getLogger(SHCMemberLauncher.class.getName()).
			log(Level.SEVERE, null, ex);
			}
			if (0 != p.exitValue()) {
			throw new RuntimeException(
			"failed to init shc member. Process:  " + p);
			} else {
			System.out.println("success");
			}

			
			try {
			Thread.sleep(1000000);
		} catch (InterruptedException ex) {
			java.util.logging.Logger.getLogger(SHCMemberLauncher.class.getName()).
					log(Level.SEVERE, null, ex);
		}


	}
}
