/*
 * Proprietary and confidential. Copyright Splunk 2015
 */
package com.splunk.kubclient;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;

/**
 *
 * @author ghendrey
 */
public class Test {
	public static void main(String[] args){
		KubernetesClient kube = new DefaultKubernetesClient();
		try (KubernetesClient client = new DefaultKubernetesClient()) {
			client.services().watch(new Watcher<Service>() {
				@Override
				public void eventReceived(Action action, Service service) {
					System.out.println(action + ": " + service);
				}
				
				
				@Override
				public void onClose(KubernetesClientException e) {
					System.out.println("Closed: " + e);
				}
			});
		}
	}
}
