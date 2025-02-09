/*
 * Copyright 2021-2024 Aklivity Inc.
 *
 * Aklivity licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.aklivity.zilla.runtime.binding.mqtt.internal.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import io.aklivity.zilla.runtime.binding.mqtt.config.MqttConditionConfig;
import io.aklivity.zilla.runtime.binding.mqtt.config.MqttPublishConfig;
import io.aklivity.zilla.runtime.binding.mqtt.config.MqttSubscribeConfig;
import io.aklivity.zilla.runtime.binding.mqtt.config.MqttTopicParamConfig;
import io.aklivity.zilla.runtime.engine.config.GuardedConfig;
import io.aklivity.zilla.runtime.engine.config.RouteConfig;

public class MqttConditionMatcherTest
{

    @Test
    public void shouldMatchIsolatedMultiLevelWildcard()
    {
        MqttConditionMatcher matcher = buildMatcher(
            "#",
            "#");
        assertTrue(matcher.matchesPublish(1L, "#"));
        assertTrue(matcher.matchesSubscribe(1L, "#"));
        assertTrue(matcher.matchesPublish(1L, "topic"));
        assertTrue(matcher.matchesSubscribe(1L, "topic"));
        assertTrue(matcher.matchesPublish(1L, "topic/pub"));
        assertTrue(matcher.matchesSubscribe(1L, "topic/sub"));
        assertTrue(matcher.matchesPublish(1L, "topic/+/pub"));
        assertTrue(matcher.matchesSubscribe(1L, "topic/+/sub"));
        assertTrue(matcher.matchesPublish(1L, "topic/pub/#"));
        assertTrue(matcher.matchesSubscribe(1L, "topic/sub/#"));
    }

    @Test
    public void shouldMatchMultipleTopicNames()
    {
        MqttConditionMatcher matcher = buildMatcher(
            "topic/pub",
            "topic/sub");
        assertTrue(matcher.matchesPublish(1L, "topic/pub"));
        assertTrue(matcher.matchesSubscribe(1L, "topic/sub"));
        assertFalse(matcher.matchesPublish(1L, "topic/#"));
        assertFalse(matcher.matchesSubscribe(1L, "topic/#"));
        assertFalse(matcher.matchesPublish(1L, "topic/+"));
        assertFalse(matcher.matchesSubscribe(1L, "topic/+"));
        assertFalse(matcher.matchesPublish(1L, "topic/sub"));
        assertFalse(matcher.matchesSubscribe(1L, "topic/pub"));
        assertFalse(matcher.matchesPublish(1L, "topic/pu"));
        assertFalse(matcher.matchesSubscribe(1L, "topic/su"));
        assertFalse(matcher.matchesPublish(1L, "topic/put"));
        assertFalse(matcher.matchesSubscribe(1L, "topic/sup"));
        assertFalse(matcher.matchesPublish(1L, "topic/publ"));
        assertFalse(matcher.matchesSubscribe(1L, "topic/subs"));
        assertFalse(matcher.matchesPublish(1L, "topicpub"));
        assertFalse(matcher.matchesSubscribe(1L, "topicsub"));
        assertFalse(matcher.matchesPublish(1L, "opic/pub"));
        assertFalse(matcher.matchesSubscribe(1L, "opic/sub"));
        assertFalse(matcher.matchesPublish(1L, "popic/pub"));
        assertFalse(matcher.matchesSubscribe(1L, "zopic/sub"));
    }

    @Test
    public void shouldMatchMultipleTopicNamesWithSingleLevelWildcard()
    {
        MqttConditionMatcher matcher = buildMatcher(
            "topic/pub/+",
            "topic/sub/+");
        assertTrue(matcher.matchesPublish(1L, "topic/pub/aa"));
        assertTrue(matcher.matchesSubscribe(1L, "topic/sub/bbb"));
        assertTrue(matcher.matchesPublish(1L, "topic/pub/+"));
        assertTrue(matcher.matchesSubscribe(1L, "topic/sub/+"));
        assertFalse(matcher.matchesPublish(1L, "topic/sub/aa"));
        assertFalse(matcher.matchesSubscribe(1L, "topic/pub/bbb"));
        assertFalse(matcher.matchesPublish(1L, "topic/pub/aa/one"));
        assertFalse(matcher.matchesSubscribe(1L, "topic/sub/bbb/two"));
    }

    @Test
    public void shouldMatchMultipleTopicNamesWithSingleAndMultiLevelWildcard()
    {
        MqttConditionMatcher matcher = buildMatcher(
            "topic/+/pub/#",
            "topic/+/sub/#");
        assertTrue(matcher.matchesPublish(1L, "topic/x/pub/aa"));
        assertTrue(matcher.matchesSubscribe(1L, "topic/y/sub/b"));
        assertTrue(matcher.matchesPublish(1L, "topic/x/pub/test/cc"));
        assertTrue(matcher.matchesSubscribe(1L, "topic/y/sub/test/bb"));
        assertFalse(matcher.matchesPublish(1L, "topic/pub/aa"));
        assertFalse(matcher.matchesSubscribe(1L, "topic/sub/b"));
    }

    @Test
    public void shouldMatchTopicNameWithIdentityPlaceholder()
    {
        MqttConditionMatcher matcher = buildMatcher(
            "pub/{id}",
            Map.of("id", "${guarded['gname'].identity}"),
            "sub/{id}",
            Map.of("id", "${guarded['gname'].identity}"),
            "gname",
            Map.of(
                1L, "myuser",
                2L, "otheruser"));
        assertTrue(matcher.matchesPublish(1L, "pub/myuser"));
        assertTrue(matcher.matchesSubscribe(1L, "sub/myuser"));
        assertTrue(matcher.matchesPublish(2L, "pub/otheruser"));
        assertTrue(matcher.matchesSubscribe(2L, "sub/otheruser"));
        assertFalse(matcher.matchesPublish(2L, "pub/myuser"));
        assertFalse(matcher.matchesSubscribe(2L, "sub/myuser"));
        assertFalse(matcher.matchesPublish(1L, "pub/otheruser"));
        assertFalse(matcher.matchesSubscribe(1L, "sub/otheruser"));
        assertFalse(matcher.matchesPublish(1L, "pub/myuset"));
        assertFalse(matcher.matchesSubscribe(1L, "sub/myuset"));
        assertFalse(matcher.matchesPublish(1L, "pub/myusert"));
        assertFalse(matcher.matchesSubscribe(1L, "sub/myusert"));
        assertFalse(matcher.matchesPublish(1L, "pub/myuser/a"));
        assertFalse(matcher.matchesSubscribe(1L, "sub/myuser/a"));
        assertFalse(matcher.matchesPublish(3L, "pub/myuser"));
        assertFalse(matcher.matchesSubscribe(3L, "sub/myuser"));
        assertFalse(matcher.matchesPublish(3L, "pub/null"));
        assertFalse(matcher.matchesSubscribe(3L, "sub/null"));
    }

    @Test
    public void shouldMatchTopicNameWithIdentityPlaceholderAndMultiLevelWildcard()
    {
        MqttConditionMatcher matcher = buildMatcher(
            "pub/{id}/#",
            Map.of("id", "${guarded['gname'].identity}"),
            "sub/{id}/#",
            Map.of("id", "${guarded['gname'].identity}"),
            "gname",
            Map.of(
                1L, "myuser",
                2L, "otheruser"));
        assertTrue(matcher.matchesPublish(1L, "pub/myuser/pubtest"));
        assertTrue(matcher.matchesSubscribe(1L, "sub/myuser/subtest"));
        assertTrue(matcher.matchesPublish(1L, "pub/myuser/pubtest/aaa"));
        assertTrue(matcher.matchesSubscribe(1L, "sub/myuser/subtest/aa"));
        assertTrue(matcher.matchesPublish(2L, "pub/otheruser/pubtest"));
        assertTrue(matcher.matchesSubscribe(2L, "sub/otheruser/subtest"));
        assertTrue(matcher.matchesPublish(2L, "pub/otheruser/pubtest/aa"));
        assertTrue(matcher.matchesSubscribe(2L, "sub/otheruser/subtest/aa"));
        assertFalse(matcher.matchesPublish(2L, "pub/myuser/pubtest"));
        assertFalse(matcher.matchesSubscribe(2L, "sub/myuser/subtest"));
        assertFalse(matcher.matchesPublish(2L, "pub/myuser/pubtest/aaa"));
        assertFalse(matcher.matchesSubscribe(2L, "sub/myuser/subtest/aa"));
        assertFalse(matcher.matchesPublish(1L, "pub/otheruser/pubtest"));
        assertFalse(matcher.matchesSubscribe(1L, "sub/otheruser/subtest"));
        assertFalse(matcher.matchesPublish(1L, "pub/otheruser/pubtest/aa"));
        assertFalse(matcher.matchesSubscribe(1L, "sub/otheruser/subtest/aa"));
    }

    @Test
    public void shouldNotMatchTopicNameWithInvalidIdentityPlaceholder()
    {
        MqttConditionMatcher matcher = buildMatcher(
            "pub/{id}",
            Map.of("id", "${guarded['invalid'].identity}"),
            "sub/{id}",
            Map.of("id", "${guarded['invalid'].identity}"),
            "gname",
            Map.of(
                1L, "myuser",
                2L, "otheruser"));
        assertFalse(matcher.matchesPublish(1L, "pub/{id}"));
        assertFalse(matcher.matchesSubscribe(1L, "sub/{id}"));
        assertFalse(matcher.matchesPublish(1L, "pub/myuser"));
        assertFalse(matcher.matchesSubscribe(1L, "sub/myuser"));
        assertFalse(matcher.matchesPublish(2L, "pub/otheruser"));
        assertFalse(matcher.matchesSubscribe(2L, "sub/otheruser"));
    }

    @Test
    public void shouldMatchTopicNameWithUnconstrainedParam()
    {
        MqttConditionMatcher matcher = buildMatcher(
            "pub/{id}",
            "sub/{id}");
        assertTrue(matcher.matchesPublish(1L, "pub/aaa"));
        assertTrue(matcher.matchesSubscribe(1L, "sub/aaa"));
        assertTrue(matcher.matchesPublish(2L, "pub/bbb"));
        assertTrue(matcher.matchesSubscribe(2L, "sub/bbb"));
    }

    @Test
    public void shouldNotMatchTopicNameWithNonExistentParamConstraint()
    {
        MqttConditionMatcher matcher = buildMatcher(
            "pub/{id}",
            Map.of("other", "${guarded['gname'].identity}"),
            "sub/{id}",
            Map.of("other", "${guarded['gname'].identity}"),
            "gname",
            Map.of(
                1L, "myuser",
                2L, "otheruser"));
        assertFalse(matcher.matchesPublish(1L, "pub/myuser"));
        assertFalse(matcher.matchesSubscribe(1L, "sub/myuser"));
        assertFalse(matcher.matchesPublish(2L, "pub/otheruser"));
        assertFalse(matcher.matchesSubscribe(2L, "sub/otheruser"));
    }

    private static MqttConditionMatcher buildMatcher(
        String publishTopic,
        String subscribeTopic)
    {
        return buildMatcher(publishTopic, Map.of(), subscribeTopic, Map.of(), "", Map.of());
    }

    private static MqttConditionMatcher buildMatcher(
        String publishTopic,
        Map<String, String> publishParams,
        String subscribeTopic,
        Map<String, String> subscribeParams,
        String guardName,
        Map<Long, String> identities)
    {
        var publishConfigBuilder = MqttPublishConfig.builder()
            .topic(publishTopic);
        publishParams.forEach((k, v) -> publishConfigBuilder
            .param(MqttTopicParamConfig.builder()
                .name(k)
                .value(v)
                .build()));

        var subscribeConfigBuilder = MqttSubscribeConfig.builder()
            .topic(subscribeTopic);
        subscribeParams.forEach((k, v) -> subscribeConfigBuilder
            .param(MqttTopicParamConfig.builder()
                .name(k)
                .value(v)
                .build()));

        MqttConditionConfig condition = MqttConditionConfig.builder()
            .publish(publishConfigBuilder.build())
            .subscribe(subscribeConfigBuilder.build())
            .build();

        GuardedConfig guarded = GuardedConfig.builder()
            .name(guardName)
            .build();
        guarded.identity = identities::get;
        var route = new MqttRouteConfig(RouteConfig.builder()
            .guarded(guarded)
            .build());
        return new MqttConditionMatcher(route, condition);
    }
}
