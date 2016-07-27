/*
 * Proprietary and confidential. Copyright Splunk 2015
 */
package com.splunk.kubclient;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ghendrey
 */
public class SHCCaptainLauncher {

	private static final String DEFAULT_K8S_MASTER = "http://localhost:8001";
	private static final int DEFAULT_SHC_MGMT_PORT = 8089;

	private static final Logger logger = LoggerFactory.getLogger(
			SHCCaptainLauncher.class);

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println(
					"Usage: shcmember_podname numPodsExpected [master] [namespace] [timeout-secs]");
			System.exit(1);
		}
		String shcMemberPodname = args[0];
		System.out.println(
				"kubernetes splunk shc member pods expected name is: " + shcMemberPodname);
		int expectedNumPods = Integer.parseInt(args[1]);
		String namespace = "default";
		String master = DEFAULT_K8S_MASTER;
		long timeout = Long.MAX_VALUE;
		if (args.length > 2) {
			master = args[2];
		}
		System.out.println("kubernetes master should be at: " + master);

		if (args.length > 3) {
			namespace = args[3];
		}
		System.out.
				println("expecting to use kubernetes namespace: " + namespace);
		if (args.length > 4) {
			timeout = Long.parseLong(args[4]);
		}
		System.out.println(
				"using timeout for members to come up (sec): " + timeout);

		//initialize search head clustering on this member
		System.out.println("Setting up as member...");
		//the 'false' in run means do not tail the logs, rather return 
		//so we can go on to our other business
		SHCMemberLauncher.run(false, new String[]{});
		System.out.println("Bootstrapping as captain...");

		//bootstrap the captain
		long start = System.currentTimeMillis();
		Config config = new ConfigBuilder().withMasterUrl(master).build();

		//get member list
		try (KubernetesClient client = new DefaultKubernetesClient();) {
			while (true) {
				if ((System.currentTimeMillis() - start) / 1000 > timeout) {
					logger.warn("exiting due to timeout:" + timeout + " secs");
					System.exit(1);
				}

				System.out.println("Asking kubernetes for pod list...");
				PodList pl = client.pods().list();
				StringBuilder serverList = new StringBuilder();
				final AtomicInteger count = new AtomicInteger(0);
				pl.getItems().forEach(pod -> {
					//only pay attention to pods that are shc members
					//Names look like "splunk-shc-mbr-pod-x9394"
					if (pod.getMetadata().getName().startsWith(shcMemberPodname)) {
						System.out.println(pod.getMetadata().getName());
						serverList.append("https://").append(
								pod.getStatus().getPodIP()
						).append(":").append(DEFAULT_SHC_MGMT_PORT).append(",");
						count.incrementAndGet();
					}
				});//end forEach
					
				//make sure at least as many member pods as expected	
				if (count.get() < expectedNumPods) {
					System.out.println("Waiting...");
					Thread.sleep(1000);
					continue;
				}					
				System.out.println(
						"these pods are non-captain shc members: " + serverList);
				String hostIp = InetAddress.getLocalHost().getHostAddress();
				serverList.append("https://").append(hostIp).append(":").append(
						DEFAULT_SHC_MGMT_PORT);
				System.out.println(
						"these are all members (including the captain): " + serverList);

				ProcessBuilder pb = new ProcessBuilder("/sbin/entrypoint.sh");	//, "splunk bootstrap shcluster-captain -auth admin:changeme"
//				String cmd = "bootstrap shcluster-captain";
//						+ "-servers_list \"" + serverList.toString() 
//						+ "\" -auth admin:changeme";
				//System.out.println(pb.command());
				//pb.command().add(cmd);
				String[] bootstrapCMD = {
					"splunk",
					"bootstrap",
					"shcluster-captain",
					"-auth",
					"admin:changeme",
					"-servers_list",
					serverList.toString()
				};
				System.out.println(Arrays.toString(bootstrapCMD));
				pb.command().addAll(Arrays.asList(bootstrapCMD));

				pb.inheritIO();
				Process p = pb.start();
				p.waitFor();
				if (0 != p.exitValue()) {
					throw new RuntimeException(
							"failed to bootstrap shc captain:" + p);
				}
				System.out.
						println("Succeeded in bootstrapping cluster captain.");
				break;

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(
					"An Exception was caught while trying to bootstrap the captain. " + e.
					getMessage());
			logger.error(e.getMessage(), e);
		}

		//ok. so now the captain is bootstrapped. And splunk is already
		//started so let's just hang out and tail the logs. (note we 
		//give no args to entrypoint. Just the env variable TAIL. So
		//no command is run, we just tail splunk logs
		ProcessBuilder pb = new ProcessBuilder("/sbin/entrypoint.sh");
		pb.environment().put("TAIL", "true");
		pb.inheritIO();
		Process p = pb.start();
		p.waitFor();

	}
}
