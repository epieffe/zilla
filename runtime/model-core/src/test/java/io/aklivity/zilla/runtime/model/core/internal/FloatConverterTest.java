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
package io.aklivity.zilla.runtime.model.core.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;

import io.aklivity.zilla.runtime.engine.EngineContext;
import io.aklivity.zilla.runtime.engine.binding.function.MessageConsumer;
import io.aklivity.zilla.runtime.engine.model.function.ValueConsumer;
import io.aklivity.zilla.runtime.model.core.config.FloatModelConfig;

public class FloatConverterTest
{
    private final EngineContext context = mock(EngineContext.class);
    private final FloatModelConfig config = FloatModelConfig.builder()
        .format("binary")
        .build();
    private final FloatConverterHandler converter = new FloatConverterHandler(config, context);

    @Test
    public void shouldVerifyValidFloat()
    {
        DirectBuffer data = new UnsafeBuffer();

        byte[] bytes = {-13, -96, 4, 0};
        data.wrap(bytes, 0, bytes.length);
        assertEquals(data.capacity(), converter.convert(0L, 0L, data, 0, data.capacity(), ValueConsumer.NOP));
    }

    @Test
    public void shouldVerifyInvalidFloat()
    {
        when(context.clock()).thenReturn(Clock.systemUTC());
        when(context.supplyEventWriter()).thenReturn(mock(MessageConsumer.class));
        FloatConverterHandler converter = new FloatConverterHandler(config, context);
        DirectBuffer data = new UnsafeBuffer();

        byte[] bytes = "Invalid Float".getBytes();
        data.wrap(bytes, 0, bytes.length);
        assertEquals(-1, converter.convert(0L, 0L, data, 0, data.capacity(), ValueConsumer.NOP));
    }
}
