/*
 * Copyright 2021-2024 Aklivity Inc
 *
 * Licensed under the Aklivity Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 *   https://www.aklivity.io/aklivity-community-license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.aklivity.zilla.runtime.binding.sse.kafka.config;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class SseKafkaWithFilterConfig
{
    public final Optional<String> key;
    public final Optional<List<SseKafkaWithFilterHeaderConfig>> headers;

    SseKafkaWithFilterConfig(
        String key,
        List<SseKafkaWithFilterHeaderConfig> headers)
    {
        this.key = Optional.ofNullable(key);
        this.headers = Optional.ofNullable(headers);
    }

    public static SseKafkaWithFilterConfigBuilder<SseKafkaWithFilterConfig> builder()
    {
        return new SseKafkaWithFilterConfigBuilder<>(SseKafkaWithFilterConfig.class::cast);
    }

    public static <T> SseKafkaWithFilterConfigBuilder<T> builder(
        Function<SseKafkaWithFilterConfig, T> mapper)
    {
        return new SseKafkaWithFilterConfigBuilder<>(mapper);
    }
}
