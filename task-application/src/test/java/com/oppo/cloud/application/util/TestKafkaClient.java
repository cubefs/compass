/*
 * Copyright 2023 OPPO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oppo.cloud.application.util;

import lombok.val;
import org.apache.kafka.clients.admin.*;
import org.junit.jupiter.api.Test;

import java.util.*;

public class TestKafkaClient {

    @Test
    public void testGetConsumers() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        try (AdminClient client = AdminClient.create(props)) {
            // List<String> groups = client.listConsumerGroups().all().get()
            // .stream().map(s -> s.groupId()).collect(Collectors.toList());
            List<String> groups = new ArrayList<>();
            groups.add("task-application");
            Map<String, ConsumerGroupDescription> descriptionMap = client.describeConsumerGroups(groups).all().get();
            System.out.println(descriptionMap);
            for (String groupId : groups) {
                ConsumerGroupDescription description = descriptionMap.get(groupId);
                Collection<MemberDescription> members = description.members();
                System.out.print(groupId + " - ");
                System.out.println(members);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
