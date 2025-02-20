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
package io.aklivity.zilla.specs.binding.mqtt.kafka.internal;

import static org.junit.Assert.assertEquals;

import java.util.function.IntConsumer;

import org.agrona.BitUtil;
import org.agrona.DirectBuffer;
import org.agrona.collections.IntArrayList;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;

import io.aklivity.zilla.specs.binding.mqtt.kafka.internal.types.MqttPublishOffsetMetadataFW;
import io.aklivity.zilla.specs.binding.mqtt.kafka.internal.types.MqttSubscribeOffsetMetadataFW;

public class MqttKafkaFunctionsTest
{
    @Test
    public void shouldGetMapper()
    {
        MqttKafkaFunctions.Mapper mapper = new MqttKafkaFunctions.Mapper();
        assertEquals("mqtt_kafka", mapper.getPrefixName());
    }
    @Test
    public void shouldEncodeMqttOffsetMetadataV1()
    {
        final String state = MqttKafkaFunctions.subscribeMetadata()
            .v1()
                .metadata(10)
                .metadata(14)
                .metadata(15)
                .build()
            .build();

        final IntArrayList metadataList = new IntArrayList();
        UnsafeBuffer buffer = new UnsafeBuffer(BitUtil.fromHex(state));
        MqttSubscribeOffsetMetadataFW offsetMetadata = new MqttSubscribeOffsetMetadataFW().wrap(buffer, 0, buffer.capacity());
        offsetMetadata.subscribeMetadataV1().packetIds().forEachRemaining((IntConsumer) metadataList::add);

        assertEquals(10, (int) metadataList.get(0));
        assertEquals(14, (int) metadataList.get(1));
        assertEquals(15, (int) metadataList.get(2));
    }

    @Test
    public void shouldEncodeMqttOffsetMetadataV2()
    {
        final String state = MqttKafkaFunctions.subscribeMetadata()
            .v2()
                .metadata(10)
                .metadata(14)
                .metadata(15)
                .build()
            .build();

        final IntArrayList metadataList = new IntArrayList();
        UnsafeBuffer buffer = new UnsafeBuffer(BitUtil.fromHex(state));
        MqttSubscribeOffsetMetadataFW offsetMetadata = new MqttSubscribeOffsetMetadataFW().wrap(buffer, 0, buffer.capacity());
        offsetMetadata.subscribeMetadataV2().packetIds().forEachRemaining((IntConsumer) metadataList::add);

        assertEquals(10, (int) metadataList.get(0));
        assertEquals(14, (int) metadataList.get(1));
        assertEquals(15, (int) metadataList.get(2));
    }

    @Test
    public void shouldEncodeMqttPublishOffsetMetadata()
    {
        final String state = MqttKafkaFunctions.publishMetadata()
            .producer(1L, (short) 1)
            .packetId(1)
            .build();

        DirectBuffer buffer = new UnsafeBuffer(BitUtil.fromHex(state));
        MqttPublishOffsetMetadataFW offsetMetadata = new MqttPublishOffsetMetadataFW().wrap(buffer, 0, buffer.capacity());

        assertEquals(1, offsetMetadata.version());
        assertEquals(1, offsetMetadata.packetIds().nextInt());
        assertEquals(1, offsetMetadata.producerId());
        assertEquals(1, offsetMetadata.producerEpoch());
    }
}
