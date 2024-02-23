package android.debug.zenohjavaclient

import android.util.Log
import com.google.protobuf.Empty
import io.zenoh.Session
import io.zenoh.keyexpr.KeyExpr
import io.zenoh.prelude.Encoding
import io.zenoh.prelude.KnownEncoding
import io.zenoh.value.Value
import org.eclipse.uprotocol.transport.builder.UAttributesBuilder
import org.eclipse.uprotocol.transport.builder.UPayloadBuilder.packToAny
import org.eclipse.uprotocol.uri.factory.UResourceBuilder
import org.eclipse.uprotocol.v1.UEntity
import org.eclipse.uprotocol.v1.UMessage
import org.eclipse.uprotocol.v1.UPriority
import org.eclipse.uprotocol.v1.UResource
import org.eclipse.uprotocol.v1.UUri

class ZenohPubber {

    companion object {
        fun periodicallyPutOnZenoh() {
            // use `up-java` to build a UMessage to send
            val service = UEntity.newBuilder()
                .setName("example.client")
                .setVersionMajor(1)
                .build()

            val resource = UResource.newBuilder()
                .setName("doors")
                .setInstance("front_left")
                .setMessage("Doors")
                .build()

            val METHOD_EXECUTE_DOOR_COMMAND = "ExecuteDoorCommand"

            val methodUri = UUri.newBuilder()
                .setEntity(service)
                .setResource(UResourceBuilder.forRpcRequest(METHOD_EXECUTE_DOOR_COMMAND))
                .build()

            val mUri = UUri.newBuilder()
                .setEntity(service)
                .build()

            val mResponseUri = UUri.newBuilder(mUri)
                .setResource(UResourceBuilder.forRpcResponse())
                .build()

            val timeout = 10000

            val builder = UAttributesBuilder.request(
                mResponseUri,
                methodUri,
                UPriority.UPRIORITY_CS4,
                timeout
            )

            val requestPayload = packToAny(Empty.getDefaultInstance())

            val requestMessage: UMessage = UMessage.newBuilder()
                .setPayload(requestPayload)
                .setAttributes(builder.build())
                .build()

            try {
                Session.open().use { session ->
                    KeyExpr.tryFrom("demo/example/zenoh-java-pub").use { keyExpr ->
                        Log.d("ZenohDemo", "Declaring publisher on '$keyExpr'...")
                        session.declarePublisher(keyExpr).res().use { publisher ->
                            var idx = 0
                            while (true) {
                                Thread.sleep(100)
                                val publishTime = System.nanoTime()
                                val umessageByteArray = requestMessage.toByteArray()

                                val protobufEncoding = Encoding(KnownEncoding.APP_OCTET_STREAM) // Or replace APP_OCTET_STREAM with the actual encoding if there's a specific one for Protobuf

                                val protobufValue = Value(umessageByteArray, protobufEncoding)
                                publisher.put(protobufValue).res()
                                Log.d("ZenohJavaClient", "[${String.format("%4s", idx)}] publishTime: $publishTime")
                                idx++
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ZenohDemo", "An error occurred: ${e.message}", e)
            }
        }
    }

}