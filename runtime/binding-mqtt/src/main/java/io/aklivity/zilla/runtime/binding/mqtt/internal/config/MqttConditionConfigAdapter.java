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

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.bind.adapter.JsonbAdapter;

import io.aklivity.zilla.runtime.binding.mqtt.config.MqttConditionConfig;
import io.aklivity.zilla.runtime.binding.mqtt.config.MqttConditionConfigBuilder;
import io.aklivity.zilla.runtime.binding.mqtt.config.MqttTopicParamConfig;
import io.aklivity.zilla.runtime.binding.mqtt.internal.MqttBinding;
import io.aklivity.zilla.runtime.engine.config.ConditionConfig;
import io.aklivity.zilla.runtime.engine.config.ConditionConfigAdapterSpi;

public final class MqttConditionConfigAdapter implements ConditionConfigAdapterSpi, JsonbAdapter<ConditionConfig, JsonObject>
{
    private static final String SESSION_NAME = "session";
    private static final String SUBSCRIBE_NAME = "subscribe";
    private static final String PUBLISH_NAME = "publish";
    private static final String CLIENT_ID_NAME = "client-id";
    private static final String TOPIC_NAME = "topic";
    private static final String PARAMS_NAME = "params";

    private static final String CLIENT_ID_DEFAULT = "*";

    @Override
    public String type()
    {
        return MqttBinding.NAME;
    }

    @Override
    public JsonObject adaptToJson(
        ConditionConfig condition)
    {
        MqttConditionConfig mqttCondition = (MqttConditionConfig) condition;

        JsonObjectBuilder object = Json.createObjectBuilder();

        if (!mqttCondition.sessions.isEmpty())
        {
            JsonArrayBuilder sessions = Json.createArrayBuilder();

            mqttCondition.sessions.forEach(p ->
            {
                JsonObjectBuilder sessionJson = Json.createObjectBuilder();
                if (!CLIENT_ID_DEFAULT.equals(p.clientId))
                {
                    sessionJson.add(CLIENT_ID_NAME, p.clientId);
                }
                sessions.add(sessionJson);
            });
            object.add(SESSION_NAME, sessions);
        }

        if (!mqttCondition.subscribes.isEmpty())
        {
            JsonArrayBuilder subscribes = Json.createArrayBuilder();

            mqttCondition.subscribes.forEach(sub ->
            {
                JsonObjectBuilder subscribeJson = Json.createObjectBuilder();
                subscribeJson.add(TOPIC_NAME, sub.topic);
                if (sub.params != null && !sub.params.isEmpty())
                {
                    JsonObjectBuilder params = Json.createObjectBuilder();
                    sub.params.forEach(p -> params.add(p.name, p.value));
                    subscribeJson.add(PARAMS_NAME, params);
                }
                subscribes.add(subscribeJson);
            });
            object.add(SUBSCRIBE_NAME, subscribes);
        }

        if (!mqttCondition.publishes.isEmpty())
        {
            JsonArrayBuilder publishes = Json.createArrayBuilder();

            mqttCondition.publishes.forEach(pub ->
            {
                JsonObjectBuilder publishJson = Json.createObjectBuilder();
                publishJson.add(TOPIC_NAME, pub.topic);
                if (pub.params != null && !pub.params.isEmpty())
                {
                    JsonObjectBuilder params = Json.createObjectBuilder();
                    pub.params.forEach(p -> params.add(p.name, p.value));
                    publishJson.add(PARAMS_NAME, params);
                }
                publishes.add(publishJson);
            });
            object.add(PUBLISH_NAME, publishes);
        }

        return object.build();
    }

    @Override
    public ConditionConfig adaptFromJson(
        JsonObject object)
    {
        MqttConditionConfigBuilder<MqttConditionConfig> mqttConfig = MqttConditionConfig.builder();

        if (object.containsKey(SESSION_NAME))
        {
            JsonArray sessionsJson = object.getJsonArray(SESSION_NAME);
            sessionsJson.forEach(s ->
            {
                String clientId = s.asJsonObject().getString(CLIENT_ID_NAME, CLIENT_ID_DEFAULT);

                mqttConfig.session()
                    .clientId(clientId)
                    .build();
            });
        }

        if (object.containsKey(SUBSCRIBE_NAME))
        {
            JsonArray subscribesJson = object.getJsonArray(SUBSCRIBE_NAME);
            subscribesJson.forEach(s ->
            {
                var subscribeConfig = mqttConfig.subscribe();
                JsonObject subscribeJson = s.asJsonObject();

                String topic = subscribeJson.getString(TOPIC_NAME);
                subscribeConfig.topic(topic);

                if (subscribeJson.containsKey(PARAMS_NAME))
                {
                    subscribeJson.getJsonObject(PARAMS_NAME).forEach((k, v) ->
                    {
                        MqttTopicParamConfig param = MqttTopicParamConfig.builder()
                            .name(k)
                            .value(((JsonString) v).getString())
                            .build();
                        subscribeConfig.param(param);
                    });
                }

                subscribeConfig.build();
            });
        }

        if (object.containsKey(PUBLISH_NAME))
        {
            JsonArray publishesJson = object.getJsonArray(PUBLISH_NAME);
            publishesJson.forEach(p ->
            {
                var publishConfig = mqttConfig.publish();
                JsonObject publishJson = p.asJsonObject();

                String topic = publishJson.getString(TOPIC_NAME);
                publishConfig.topic(topic);

                if (publishJson.containsKey(PARAMS_NAME))
                {
                    publishJson.getJsonObject(PARAMS_NAME).forEach((k, v) ->
                    {
                        MqttTopicParamConfig param = MqttTopicParamConfig.builder()
                            .name(k)
                            .value(((JsonString) v).getString())
                            .build();
                        publishConfig.param(param);
                    });
                }

                publishConfig.build();
            });
        }

        return mqttConfig.build();
    }
}
