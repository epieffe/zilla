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
package io.aklivity.zilla.runtime.binding.http.filesystem.internal.config;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Predicate;

import io.aklivity.zilla.runtime.binding.http.filesystem.internal.types.HttpHeaderFW;
import io.aklivity.zilla.runtime.binding.http.filesystem.internal.types.String8FW;
import io.aklivity.zilla.runtime.binding.http.filesystem.internal.types.stream.HttpBeginExFW;
import io.aklivity.zilla.runtime.engine.config.BindingConfig;
import io.aklivity.zilla.runtime.engine.config.KindConfig;

public final class HttpFileSystemBindingConfig
{
    private static final Predicate<HttpHeaderFW> HEADER_METHOD_NAME;
    private static final Predicate<HttpHeaderFW> HEADER_PATH_NAME;

    static
    {
        String8FW headerMethod = new String8FW(":method");
        String8FW headerPath = new String8FW(":path");
        HEADER_METHOD_NAME = h -> headerMethod.equals(h.name());
        HEADER_PATH_NAME = h -> headerPath.equals(h.name());
    }

    public final long id;
    public final String name;
    public final KindConfig kind;
    public final List<HttpFileSystemRouteConfig> routes;

    public HttpFileSystemBindingConfig(
        BindingConfig binding)
    {
        this.id = binding.id;
        this.name = binding.name;
        this.kind = binding.kind;
        this.routes = binding.routes.stream().map(HttpFileSystemRouteConfig::new).collect(toList());
    }

    public HttpFileSystemRouteConfig resolve(
        long authorization,
        HttpBeginExFW beginEx)
    {
        HttpHeaderFW methodHeader = beginEx != null ? beginEx.headers().matchFirst(HEADER_METHOD_NAME) : null;
        String method = methodHeader != null ? methodHeader.value().asString() : null;
        HttpHeaderFW pathHeader = beginEx != null ? beginEx.headers().matchFirst(HEADER_PATH_NAME) : null;
        String path = asPathWithoutQuery(pathHeader);
        return routes.stream()
                .filter(r -> r.authorized(authorization) && r.matches(path, method))
                .findFirst()
                .orElse(null);
    }

    private String asPathWithoutQuery(
        HttpHeaderFW pathHeader)
    {
        String path = null;

        if (pathHeader != null)
        {
            path = pathHeader.value().asString();

            int queryAt = path.indexOf('?');
            if (queryAt != -1)
            {
                path = path.substring(0, queryAt);
            }
        }

        return path;
    }
}
