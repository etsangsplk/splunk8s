/*
 * Proprietary and confidential. Copyright Splunk 2015
 */
package com.splunk.kubclient;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ghendrey
 */
public class SHCCaptainLauncher {

	private static final Logger logger = LoggerFactory.getLogger(
			SHCCaptainLauncher.class);

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.out.println(
					"Usage: shcmember_podname numPodsExpected [master] [namespace] [timeout-secs]");
			System.exit(1);
		}
		String podName = args[0];
		int expectedNumPods = Integer.parseInt(args[1]);
		String namespace = "default";
		String master = "http://localhost:8080/";
		long timeout = Long.MAX_VALUE;
		if (args.length > 2) {
			master = args[2];
		}

		if (args.length > 3) {
			namespace = args[3];
		}
		if (args.length > 4) {
			timeout = Long.parseLong(args[4]);
		}

		//initialize search head clustering on this member
		SHCMemberLauncher.main(new String[]{});

		//bootstrap the captain
		long start = System.currentTimeMillis();
		Config config = new ConfigBuilder().withMasterUrl(master).build();
		StringBuilder serverList = new StringBuilder();
		//get member list
		try (KubernetesClient client = new DefaultKubernetesClient(config)) {
			while (true) {
				if ((System.currentTimeMillis() - start) / 1000 > timeout) {
					logger.warn("exiting due to timeout:" + timeout + " secs");
					System.exit(1);
				}
				PodList pl = client.pods().list();
				if (pl.getItems().size() >= expectedNumPods) {

					pl.getItems().forEach(pod -> {

						serverList.append("http://").append(
								pod.getStatus().getPodIP()
						).append(":8092").append(",");
					}
					);
					//blow away trailing commna
					serverList.deleteCharAt(serverList.length() - 1);
					System.out.print(serverList);
					//String[] cmdArrray = {"/sbin/entrpoint.sh"};
					//String[] envp = {"SPLUNK_CMD_0=bootstrap shcluster-captain -servers_list \""+serverList+"\" -auth admin:changeme"};
					//Process p = Runtime.getRuntime().exec(cmdArrray, envp);

					ProcessBuilder pb = new ProcessBuilder("/sbin/entrypoint.sh");
					pb.environment().put("SPLUNK_CMD_0",
							"bootstrap shcluster-captain -servers_list \""+serverList+"\" -auth admin:changeme");
					//Process p = Runtime.getRuntime().exec(cmdArrray, envp);
					pb.inheritIO();
					Process p = pb.start();
					if (0 != p.exitValue()) {
						throw new RuntimeException(
								"failed to bootstrap shc captain:" + p);
					}

					//					
					//Runtime.getRuntime().exec(podName)
					System.exit(0); //OK
				}
				Thread.sleep(5 * 1000);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			System.exit(1);
		}
	}
}
