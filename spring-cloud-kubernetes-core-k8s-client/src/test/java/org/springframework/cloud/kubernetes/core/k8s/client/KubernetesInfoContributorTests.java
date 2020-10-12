/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.kubernetes.core.k8s.client;

import java.util.HashMap;
import java.util.Map;

import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.cloud.kubernetes.commons.PodUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Ryan Baxter
 */
@ExtendWith(MockitoExtension.class)
class KubernetesInfoContributorTests {

	@Mock
	private PodUtils<V1Pod> utils;

	@Test
	void getDetailsIsNotInside() {
		when(utils.currentPod()).thenReturn(() -> null);
		KubernetesInfoContributor infoContributor = new KubernetesInfoContributor(utils);
		assertThat(infoContributor.getDetails().containsKey(KubernetesHealthIndicator.INSIDE)).isTrue();
		assertThat(infoContributor.getDetails().get(KubernetesHealthIndicator.INSIDE)).isEqualTo(false);
	}

	@Test
	void getDetailsInside() throws Exception {
		Map<String, String> labels = new HashMap<>();
		labels.put("spring", "cloud");
		V1ObjectMeta metaData = new V1ObjectMeta();
		metaData.setLabels(labels);
		metaData.setName("mypod");
		metaData.setNamespace("default");

		V1PodStatus status = new V1PodStatus();
		status.setPodIP("127.0.0.1");
		status.setHostIP("123.456.789.1");

		V1PodSpec spec = new V1PodSpec();
		spec.setNodeName("nodeName");
		spec.setServiceAccountName("serviceAccount");

		V1Pod pod = new V1Pod();
		pod.setMetadata(metaData);
		pod.setStatus(status);
		pod.setSpec(spec);

		when(utils.currentPod()).thenReturn(() -> pod);
		KubernetesInfoContributor infoContributor = new KubernetesInfoContributor(utils);
		assertThat(infoContributor.getDetails().containsKey(KubernetesHealthIndicator.INSIDE)).isTrue();
		assertThat(infoContributor.getDetails().get(KubernetesHealthIndicator.INSIDE)).isEqualTo(true);
		assertThat(infoContributor.getDetails().get(KubernetesHealthIndicator.HOST_IP)).isEqualTo("123.456.789.1");
		assertThat(infoContributor.getDetails().get(KubernetesHealthIndicator.POD_IP)).isEqualTo("127.0.0.1");
		assertThat(infoContributor.getDetails().get(KubernetesHealthIndicator.NODE_NAME)).isEqualTo("nodeName");
		assertThat(infoContributor.getDetails().get(KubernetesHealthIndicator.SERVICE_ACCOUNT))
				.isEqualTo("serviceAccount");
		assertThat(infoContributor.getDetails().get(KubernetesHealthIndicator.POD_NAME)).isEqualTo("mypod");
		assertThat(infoContributor.getDetails().get(KubernetesHealthIndicator.NAMESPACE)).isEqualTo("default");
	}

}
