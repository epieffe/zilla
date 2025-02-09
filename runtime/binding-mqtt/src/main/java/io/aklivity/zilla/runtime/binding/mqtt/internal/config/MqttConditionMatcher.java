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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.agrona.collections.LongObjPredicate;

import io.aklivity.zilla.runtime.binding.mqtt.config.MqttConditionConfig;
import io.aklivity.zilla.runtime.binding.mqtt.config.MqttTopicParamConfig;

public final class MqttConditionMatcher
{
    private static final Pattern IDENTITY_PATTERN =
        Pattern.compile("\\$\\{guarded(?:\\['([a-zA-Z]+[a-zA-Z0-9\\._\\:\\-]*)'\\]).identity\\}");

    private final List<Matcher> sessionMatchers;
    private final List<LongObjPredicate<String>> subscribeMatchPredicates;
    private final List<LongObjPredicate<String>> publishMatchPredicates;

    public MqttConditionMatcher(
        MqttRouteConfig route,
        MqttConditionConfig condition)
    {
        this.sessionMatchers =
            condition.sessions != null && !condition.sessions.isEmpty() ?
                asWildcardMatcher(condition.sessions.stream().map(s -> s.clientId).collect(Collectors.toList())) : null;

        Matcher identityMatcher = IDENTITY_PATTERN.matcher("");
        this.subscribeMatchPredicates =
            condition.subscribes != null && !condition.subscribes.isEmpty() ?
                condition.subscribes.stream().map(s -> asTopicMatchPredicate(s.topic, s.params, route, identityMatcher))
                    .collect(Collectors.toList()) : null;
        this.publishMatchPredicates =
            condition.publishes != null && !condition.publishes.isEmpty() ?
                condition.publishes.stream().map(s -> asTopicMatchPredicate(s.topic, s.params, route, identityMatcher))
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
        long authorization,
        String topic)
    {
        boolean match = false;
        if (subscribeMatchPredicates != null)
        {
            for (LongObjPredicate<String> predicate : subscribeMatchPredicates)
            {
                match = predicate.test(authorization, topic);
                if (match)
                {
                    break;
                }
            }
        }
        return match;
    }

    public boolean matchesPublish(
        long authorization,
        String topic)
    {
        boolean match = false;
        if (publishMatchPredicates != null)
        {
            for (LongObjPredicate<String> predicate : publishMatchPredicates)
            {
                match = predicate.test(authorization, topic);
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

    private static LongObjPredicate<String> asTopicMatchPredicate(
        String wildcard,
        List<MqttTopicParamConfig> params,
        MqttRouteConfig route,
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
        topicParams.stream()
            .filter(tp -> params == null || params.stream().noneMatch(p -> p.name.equals(tp)))
            .forEach(tp -> System.out.format("Unconstrained param for MQTT topic %s: %s\n", wildcard, tp));

        LongObjPredicate<String> topicMatchPredicate;
        if (params == null || params.isEmpty())
        {
            topicMatchPredicate = (auth, topic) -> topicMatcher.reset(topic).matches();
        }
        else
        {
            List<GuardedIdentityParam> guardedIdentityParams = new ArrayList<>();
            List<MqttTopicParamConfig> constantParams = new ArrayList<>();
            for (MqttTopicParamConfig param : params)
            {
                if (!topicParams.contains(param.name))
                {
                    System.out.format("Undefined param constraint for MQTT topic %s: %s\n", wildcard, param.name);
                }
                if (identityMatcher.reset(param.value).matches())
                {
                    String guard = identityMatcher.group(1);
                    guardedIdentityParams.add(new GuardedIdentityParam(param.name, guard));
                }
                else
                {
                    constantParams.add(param);
                }
            }
            topicMatchPredicate = (auth, topic) ->
                matchesWithParams(auth, topic, topicMatcher, route, guardedIdentityParams, constantParams);
        }

        return topicMatchPredicate;
    }

    private static boolean matchesWithParams(
        long authorization,
        String topic,
        Matcher topicMatcher,
        MqttRouteConfig route,
        List<GuardedIdentityParam> guardedIdentityParams,
        List<MqttTopicParamConfig> constantParams)
    {
        boolean match = topicMatcher.reset(topic).matches();
        if (match)
        {
            for (GuardedIdentityParam param : guardedIdentityParams)
            {
                String identity = route.identity(param.guard, authorization);
                try
                {
                    match = topicMatcher.group(param.name).equals(identity);
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
        if (match)
        {
            for (MqttTopicParamConfig param : constantParams)
            {
                try
                {
                    match = topicMatcher.group(param.name).equals(param.value);
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

    private record GuardedIdentityParam(
        String name,
        String guard)
    {
    }
}
