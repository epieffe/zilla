/*
 * Copyright 2021-2023 Aklivity Inc
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
package io.aklivity.zilla.runtime.binding.openapi.asyncapi.internal;

import static io.aklivity.zilla.runtime.engine.config.KindConfig.PROXY;

import java.util.EnumMap;
import java.util.Map;

import io.aklivity.zilla.runtime.binding.openapi.asyncapi.internal.streams.OpenapiAsyncapiProxyFactory;
import io.aklivity.zilla.runtime.binding.openapi.asyncapi.internal.streams.OpenapiAsyncapiStreamFactory;
import io.aklivity.zilla.runtime.engine.EngineContext;
import io.aklivity.zilla.runtime.engine.binding.BindingContext;
import io.aklivity.zilla.runtime.engine.binding.BindingHandler;
import io.aklivity.zilla.runtime.engine.config.BindingConfig;
import io.aklivity.zilla.runtime.engine.config.KindConfig;

final class OpenapiAsyncapiBindingContext implements BindingContext
{
    private final Map<KindConfig, OpenapiAsyncapiStreamFactory> factories;

    OpenapiAsyncapiBindingContext(
        OpenapiAsyncapiConfiguration config,
        EngineContext context)
    {
        Map<KindConfig, OpenapiAsyncapiStreamFactory> factories = new EnumMap<>(KindConfig.class);
        factories.put(PROXY, new OpenapiAsyncapiProxyFactory(config, context));
        this.factories = factories;
    }

    @Override
    public BindingHandler attach(
        BindingConfig binding)
    {
        OpenapiAsyncapiStreamFactory factory = factories.get(binding.kind);

        if (factory != null)
        {
            factory.attach(binding);
        }

        return factory;
    }

    @Override
    public void detach(
        BindingConfig binding)
    {
        OpenapiAsyncapiStreamFactory factory = factories.get(binding.kind);

        if (factory != null)
        {
            factory.detach(binding.id);
        }
    }

    @Override
    public String toString()
    {
        return String.format("%s %s", getClass().getSimpleName(), factories);
    }
}
