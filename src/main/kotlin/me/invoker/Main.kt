package me.invoker

import me.invoker.client.DOUYU_PORT
import me.invoker.client.DOUYU_SERVER
import me.invoker.client.DouyuDanmakuClient
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import me.invoker.client.DOUYU_HEARTBEAT_INTERVAL
import java.util.concurrent.TimeUnit

private const val RUA_ROOM_ID = 58428

/**
 * Created by Kang on 2018/2/6.
 */
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val client = DouyuDanmakuClient(DOUYU_SERVER, DOUYU_PORT)

        client.joinRoom(RUA_ROOM_ID)

        Observable.interval(DOUYU_HEARTBEAT_INTERVAL, DOUYU_HEARTBEAT_INTERVAL, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe {
                    client.heartbeat()
                }

        client.messageSource()
                .filter { it["type"] == "chatmsg" }
                .subscribe {
                    println(String.format("[%s]\t%s", it["nn"], it["txt"]))
                }
    }

}