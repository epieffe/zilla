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
package io.aklivity.zilla.specs.binding.kafka.streams.network;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.rules.RuleChain.outerRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import io.aklivity.k3po.runtime.junit.annotation.Specification;
import io.aklivity.k3po.runtime.junit.rules.K3poRule;


public class GroupSaslIT
{
    private final K3poRule k3po = new K3poRule()
        .addScriptRoot("net",
            "io/aklivity/zilla/specs/binding/kafka/streams/network/group.sasl.f1.j5.s3.l3.h3.handshake.v1");

    private final TestRule timeout = new DisableOnDebug(new Timeout(5, SECONDS));

    @Rule
    public final TestRule chain = outerRule(k3po).around(timeout);

    @Test
    @Specification({
        "${net}/leader.assignment.with.sasl.plain/client",
        "${net}/leader.assignment.with.sasl.plain/server"})
    public void shouldAssignLeaderWithSaslPlain() throws Exception
    {
        k3po.finish();
    }

    @Test
    @Specification({
        "${net}/leader.assignment.with.sasl.scram/client",
        "${net}/leader.assignment.with.sasl.scram/server"})
    public void shouldAssignLeaderWithSaslScram() throws Exception
    {
        k3po.finish();
    }

    @Test
    @Specification({
        "${net}/initial.delay.config.with.sasl.plain/client",
        "${net}/initial.delay.config.with.sasl.plain/server"})
    public void shouldCreateConnectionForJoinGroupWithSaslPlain() throws Exception
    {
        k3po.finish();
    }

    @Test
    @Specification({
        "${net}/initial.delay.config.with.sasl.scram/client",
        "${net}/initial.delay.config.with.sasl.scram/server"})
    public void shouldCreateConnectionForJoinGroupWithSaslScram() throws Exception
    {
        k3po.finish();
    }
}
