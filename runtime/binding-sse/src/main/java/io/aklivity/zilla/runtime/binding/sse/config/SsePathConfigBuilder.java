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
package io.aklivity.zilla.runtime.binding.sse.config;

import java.util.function.Function;

import io.aklivity.zilla.runtime.engine.config.ConfigBuilder;
import io.aklivity.zilla.runtime.engine.config.ModelConfig;

public class SsePathConfigBuilder<T> extends ConfigBuilder<T, SsePathConfigBuilder<T>>
{
    private final Function<SseRequestConfig, T> mapper;

    private String path;
    private ModelConfig content;

    SsePathConfigBuilder(
        Function<SseRequestConfig, T> mapper)
    {
        this.mapper = mapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<SsePathConfigBuilder<T>> thisType()
    {
        return (Class<SsePathConfigBuilder<T>>) getClass();
    }

    public SsePathConfigBuilder<T> path(
        String path)
    {
        this.path = path;
        return this;
    }

    public SsePathConfigBuilder<T> content(
        ModelConfig content)
    {
        this.content = content;
        return this;
    }

    public <C extends ConfigBuilder<SsePathConfigBuilder<T>, C>> C content(
        Function<Function<ModelConfig, SsePathConfigBuilder<T>>, C> content)
    {
        return content.apply(this::content);
    }

    @Override
    public T build()
    {
        return mapper.apply(new SseRequestConfig(path, content));
    }
}
