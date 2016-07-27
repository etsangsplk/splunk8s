/*
 * Proprietary and confidential. Copyright Splunk 2015
 */
package com.splunk.kubclient;

import java.net.InetAddress;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ghendrey
 */
public class SHCMemberLauncher {
	private boolean tailLogsAndWait;
	private String[] args;

	public SHCMemberLauncher(boolean tailLogsAndWait, String[] args) {
		this.tailLogsAndWait = tailLogsAndWait;
		this.args = args;
	}

	
	private static final Logger logger = LoggerFactory.getLogger(
			SHCMemberLauncher.class);
	
	private void run() throws Exception{
		System.out.println("Starting splunk member");
		//String hostname = InetAddress.getLocalHost().getCanonicalHostName();
		String hostIp = InetAddress.getLocalHost().getHostAddress();
		ProcessBuilder pb = new ProcessBuilder("/sbin/entrypoint.sh",
				"start-service");
		String cmd = "init shcluster-config -auth admin:changeme -mgmt_uri https://" 
				+ hostIp + 
				":8089 -replication_port 34570 -replication_factor 2 -shcluster_label shcluster-01";
		//String cmd = "help -auth admin:changeme ";
		System.out.println("will execute: " + cmd);

		pb.environment().put("SPLUNK_CMD_THEN_RESTART_0", cmd);
		pb.environment().put("SPLUNK_START_ARGS", "--accept-license");
		pb.environment().put("SPLUNK_RUN_PSTACK_ON_SHUTDOWN_HANG", "true");
		//pb.environment().put("SPLUNK_USER", "root");
		if(tailLogsAndWait){
			pb.environment().put("TAIL", "true");
		}
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
			System.out.println("done");
		}
	}

	//allows the option to decide whether to tail the splunk logs or
	//return after running the splunk command
	public static void run(boolean tail, String[] args) throws Exception{
		SHCMemberLauncher m = new SHCMemberLauncher(tail, args);
		m.run();
	}
	public static void main(String[] args) throws Exception {
		run(true, args); //by default we will sit and tail the logs (true)
	}
}
