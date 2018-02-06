package me.invoker.client

import io.reactivex.Observable
import me.invoker.client.message.*
import okio.Okio
import java.net.Socket


/**
 * Created by Kang on 2018/2/4.
 */

typealias ParamsMap = Map<String, String>

class DouyuDanmakuClient(private val server: String, private val port: Int) {
    private val socket: Socket by lazy { Socket(server, port) }

    fun messageSource(): Observable<ParamsMap> = Observable.create {
        try {
            val buffer = Okio.buffer(Okio.source(socket))

            do {
                val verifyA = buffer.readIntLe()
                val verifyB = buffer.readIntLe()
                val code = buffer.readIntLe()

                val length = if (verifyA == verifyB) {
                    verifyA - 8
                } else {
                    it.onError(IllegalStateException("verify length not match"))
                    break
                }

                val msg = buffer.readString(length.toLong(), Charsets.UTF_8)
                it.onNext(msg.toDouyuMap())
            } while (!buffer.exhausted())
            it.onComplete()
        } catch (e: Exception) {
            it.onError(e)
        }
    }

    fun heartbeat() = sendMessage(HeartbeatDouyuMessage())

    fun logout() = sendMessage(LogoutDouyuMessage())

    fun joinRoom(roomId: Int) {
        sendMessage(LoginDouyuMessage(roomId))
        sendMessage(JoinGroupDouyuMessage(roomId))
    }

    private fun sendMessage(douyuMessage: AbsDouyuMessage) {
        socket.getOutputStream().write(douyuMessage.getByteArray())
        socket.getOutputStream().flush()
    }

    companion object {
        const val CODE_CLIENT_SEND_TO_SERVER = 689 // 客户端发送给弹幕服务器的文本格式数据
        const val CODE_SERVER_PUSH_TO_CLIENT = 690 // 弹幕服务器发送给客户端的文本格式数据
    }
}

private const val HARDCODE_SPLIT_PATTERN = "/"
private const val HARDCODE_PARAM_MAP_PATTERN = "@="

private fun String.toDouyuMap() = split(HARDCODE_SPLIT_PATTERN)
        .asSequence()
        .map { it.split(HARDCODE_PARAM_MAP_PATTERN) }
        .filter { it.size == 2 }
        .map { Pair(it[0], it[1]) }
        .toMap()


const val DOUYU_HEARTBEAT_INTERVAL = 45L
const val DOUYU_SERVER = "openbarrage.douyutv.com"
const val DOUYU_PORT = 8601
