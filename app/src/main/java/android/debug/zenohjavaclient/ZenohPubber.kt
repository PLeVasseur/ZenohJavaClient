package android.debug.zenohjavaclient

import android.util.Log
import io.zenoh.Session
import io.zenoh.keyexpr.KeyExpr;

class ZenohPubber {

    companion object {
        fun periodicallyPutOnZenoh() {
            try {
                Session.open().use { session ->
                    KeyExpr.tryFrom("demo/example/zenoh-java-pub").use { keyExpr ->
                        Log.d("ZenohDemo", "Declaring publisher on '$keyExpr'...")
                        session.declarePublisher(keyExpr).res().use { publisher ->
                            val payload = "Pub from Kotlin!"
                            var idx = 0
                            while (true) {
                                Thread.sleep(1000)
                                Log.d("ZenohDemo", "Putting Data ('$keyExpr': '[${String.format("%4s", idx)}] $payload')...")
                                publisher.put(payload).res()
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