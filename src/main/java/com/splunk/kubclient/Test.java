/*
 * Proprietary and confidential. Copyright Splunk 2015
 */
package com.splunk.kubclient;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 *
 * @author ghendrey
 */
public class Test {

	public static void main(String[] args) throws InterruptedException {
		try (KubernetesClient client = new DefaultKubernetesClient();) {
			PodList pl = client.pods().list();
			pl.getItems().forEach(pod -> {
				System.out.println(pod);
			}
			);
		}

	}
}
