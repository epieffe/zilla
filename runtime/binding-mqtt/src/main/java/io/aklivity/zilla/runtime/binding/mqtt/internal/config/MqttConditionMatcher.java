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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.agrona.collections.ObjLongPredicate;

import io.aklivity.zilla.runtime.binding.mqtt.config.MqttConditionConfig;
import io.aklivity.zilla.runtime.binding.mqtt.config.MqttTopicParamConfig;
import io.aklivity.zilla.runtime.engine.config.GuardedConfig;

public final class MqttConditionMatcher
{
    private static final Pattern IDENTITY_PATTERN =
        Pattern.compile("\\$\\{guarded(?:\\['([a-zA-Z]+[a-zA-Z0-9\\._\\:\\-]*)'\\]).identity\\}");

    private final List<Matcher> sessionMatchers;
    private final List<ObjLongPredicate<String>> subscribeMatchPredicates;
    private final List<ObjLongPredicate<String>> publishMatchPredicates;

    public MqttConditionMatcher(
        MqttConditionConfig condition,
        List<GuardedConfig> guarded)
    {
        this.sessionMatchers =
            condition.sessions != null && !condition.sessions.isEmpty() ?
                asWildcardMatcher(condition.sessions.stream().map(s -> s.clientId).collect(Collectors.toList())) : null;

        Matcher identityMatcher = IDENTITY_PATTERN.matcher("");
        this.subscribeMatchPredicates =
            condition.subscribes != null && !condition.subscribes.isEmpty() ?
                condition.subscribes.stream().map(s -> asTopicMatchPredicate(s.topic, s.params, guarded, identityMatcher))
                    .collect(Collectors.toList()) : null;
        this.publishMatchPredicates =
            condition.publishes != null && !condition.publishes.isEmpty() ?
                condition.publishes.stream().map(s -> asTopicMatchPredicate(s.topic, s.params, guarded, identityMatcher))
                    .collect(Collectors.toList()) : null;
    }

    public boolean matchesSession(
        String clientId)
    {
        boolean match = sessionMatchers == null;
        if (!match)
        {
            for (Matcher matcher : sessionMatchers)
            {
                match = matcher.reset(clientId).matches();
                if (match)
                {
                    break;
                }
            }
        }
        return match;
    }

    public boolean matchesSubscribe(
        String topic,
        long authorization)
    {
        boolean match = false;
        if (subscribeMatchPredicates != null)
        {
            for (ObjLongPredicate<String> predicate : subscribeMatchPredicates)
            {
                match = predicate.test(topic, authorization);
                if (match)
                {
                    break;
                }
            }
        }
        return match;
    }

    public boolean matchesPublish(
        String topic,
        long authorization)
    {
        boolean match = false;
        if (publishMatchPredicates != null)
        {
            for (ObjLongPredicate<String> predicate : publishMatchPredicates)
            {
                match = predicate.test(topic, authorization);
                if (match)
                {
                    break;
                }
            }
        }
        return match;
    }

    private static List<Matcher> asWildcardMatcher(
        List<String> wildcards)
    {
        List<Matcher> matchers = new ArrayList<>();
        for (String wildcard : wildcards)
        {
            String pattern = wildcard.replace(".", "\\.").replace("*", ".*");

            if (!pattern.endsWith(".*"))
            {
                pattern = pattern + "(\\?.*)?";
            }
            matchers.add(Pattern.compile(pattern).matcher(""));

        }

        return matchers;
    }

    private static ObjLongPredicate<String> asTopicMatchPredicate(
        String wildcard,
        List<MqttTopicParamConfig> params,
        List<GuardedConfig> guarded,
        Matcher identityMatcher
    )
    {
        final Matcher topicMatcher = Pattern.compile(wildcard
            .replace(".", "\\.")
            .replace("$", "\\$")
            .replace("+", "[^/]*")
            .replace("#", ".*")
            .replaceAll("\\{([a-zA-Z_]+)\\}", "(?<$1>[^/]+)")).matcher("");

        Collection<String> topicParams = topicMatcher.namedGroups().keySet();
        params.stream()
            .filter(p -> !topicParams.contains(p.name))
            .forEach(p -> System.out.format("Undefined param constraint for MQTT topic %s: %s\n", wildcard, p.name));
        topicParams.stream()
            .filter(tp -> params.stream().noneMatch(p -> p.name.equals(tp)))
            .forEach(tp -> System.out.format("Unconstrained param for MQTT topic %s: %s\n", wildcard, tp));

        return params.isEmpty()
            ? (topic, auth) -> topicMatcher.reset(topic).matches()
            : (topic, auth) -> matchesWithParams(topic, params, topicMatcher, identityMatcher, auth, guarded);
    }

    private static boolean matchesWithParams(
        String topic,
        List<MqttTopicParamConfig> params,
        Matcher topicMatcher,
        Matcher identityMatcher,
        long authorization,
        List<GuardedConfig> guarded)
    {
        boolean match = topicMatcher.reset(topic).matches();
        if (match)
        {
            for (MqttTopicParamConfig param : params)
            {
                String value = param.value;
                identityMatcher.reset(value);
                if (identityMatcher.find())
                {
                    String guardName = identityMatcher.group(1);
                    Optional<String> identity = guarded.stream()
                        .filter(g -> guardName.equals(g.name))
                        .findFirst()
                        .map(g -> g.identity.apply(authorization));
                    if (identity.isEmpty())
                    {
                        match = false;
                        break;
                    }
                    value = identityMatcher.replaceAll(identity.get());
                }
                try
                {
                    match = topicMatcher.group(param.name).equals(value);
                }
                catch (IllegalArgumentException e)
                {
                    match = false;
                }
                if (!match)
                {
                    break;
                }
            }
        }
        return match;
    }
}
