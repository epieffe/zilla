/*
 * Copyright 2021-2022 Aklivity Inc.
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
package io.aklivity.zilla.runtime.cog.tls.internal;

import java.net.URL;

import io.aklivity.zilla.runtime.engine.EngineContext;
import io.aklivity.zilla.runtime.engine.cog.Cog;

public final class TlsCog implements Cog
{
    public static final String NAME = "tls";

    private final TlsConfiguration config;

    TlsCog(
        TlsConfiguration config)
    {
        this.config = config;
    }

    @Override
    public String name()
    {
        return TlsCog.NAME;
    }

    @Override
    public URL type()
    {
        return getClass().getResource("schema/tls.json");
    }

    @Override
    public TlsContext supply(
        EngineContext context)
    {
        return new TlsContext(config, context);
    }
}
