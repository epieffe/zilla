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

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.bind.adapter.JsonbAdapter;

import io.aklivity.zilla.runtime.binding.http.filesystem.internal.HttpFileSystemBinding;
import io.aklivity.zilla.runtime.engine.config.WithConfig;
import io.aklivity.zilla.runtime.engine.config.WithConfigAdapterSpi;

public final class HttpFileSystemWithConfigAdapter implements WithConfigAdapterSpi, JsonbAdapter<WithConfig, JsonObject>
{
    private static final String PATH_NAME = "path";
    private static final String DIRECTORY_NAME = "directory";

    @Override
    public String type()
    {
        return HttpFileSystemBinding.NAME;
    }

    @Override
    public JsonObject adaptToJson(
        WithConfig with)
    {
        HttpFileSystemWithConfig httpFsWith = (HttpFileSystemWithConfig) with;

        JsonObjectBuilder object = Json.createObjectBuilder();

        object.add(PATH_NAME, httpFsWith.path);

        return object.build();
    }

    @Override
    public WithConfig adaptFromJson(
        JsonObject object)
    {
        String directory = object.containsKey(DIRECTORY_NAME) ? object.getString(DIRECTORY_NAME) : null;
        String path = object.containsKey(PATH_NAME) ? object.getString(PATH_NAME) : null;

        return HttpFileSystemWithConfig.builder()
            .directory(directory)
            .path(path)
            .build();
    }
}
