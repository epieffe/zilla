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
package io.aklivity.zilla.runtime.binding.http.internal.streams.rfc7540.client;

import static io.aklivity.zilla.runtime.binding.http.internal.HttpConfiguration.HTTP_CONCURRENT_STREAMS;
import static io.aklivity.zilla.runtime.binding.http.internal.HttpConfigurationTest.HTTP_STREAM_INITIAL_WINDOW_NAME;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.rules.RuleChain.outerRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import io.aklivity.k3po.runtime.junit.annotation.Specification;
import io.aklivity.k3po.runtime.junit.rules.K3poRule;
import io.aklivity.zilla.runtime.engine.test.EngineRule;
import io.aklivity.zilla.runtime.engine.test.annotation.Configuration;
import io.aklivity.zilla.runtime.engine.test.annotation.Configure;

public class ValidationIT
{
    private final K3poRule k3po = new K3poRule()
        .addScriptRoot("net", "io/aklivity/zilla/specs/binding/http/streams/network/rfc7540/validation")
        .addScriptRoot("app", "io/aklivity/zilla/specs/binding/http/streams/application/rfc7540/validation");

    private final TestRule timeout = new DisableOnDebug(new Timeout(10, SECONDS));

    private final EngineRule engine = new EngineRule()
        .directory("target/zilla-itests")
        .countersBufferCapacity(8192)
        .configurationRoot("io/aklivity/zilla/specs/binding/http/config/v2")
        .configure(HTTP_CONCURRENT_STREAMS, 100)
        .external("net0")
        .clean();

    @Rule
    public final TestRule chain = outerRule(engine).around(k3po).around(timeout);

    @Test
    @Configuration("client.validation.yaml")
    @Specification({
        "${app}/invalid.response.header/client",
        "${net}/invalid.response.header/server" })
    @Configure(name = HTTP_STREAM_INITIAL_WINDOW_NAME, value = "65535")
    public void shouldSendErrorForInvalidHeaderResponse() throws Exception
    {
        k3po.finish();
    }

    @Test
    @Configuration("client.validation.yaml")
    @Specification({
        "${app}/invalid.response.content/client",
        "${net}/invalid.response.content/server" })
    @Configure(name = HTTP_STREAM_INITIAL_WINDOW_NAME, value = "65535")
    public void shouldAbortForInvalidResponse() throws Exception
    {
        k3po.finish();
    }

    @Test
    @Configuration("client.validation.yaml")
    @Specification({
        "${app}/valid.response/client",
        "${net}/valid.response/server" })
    @Configure(name = HTTP_STREAM_INITIAL_WINDOW_NAME, value = "65535")
    public void shouldProcessValidResponse() throws Exception
    {
        k3po.finish();
    }
}
