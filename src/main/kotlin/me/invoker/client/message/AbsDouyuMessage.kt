package me.invoker.client.message

import me.invoker.client.DouyuDanmakuClient
import java.nio.ByteBuffer
import java.nio.ByteOrder

sealed class AbsDouyuMessage(private val message: String) {
    fun getByteArray(): ByteArray = ByteBuffer.allocate(message.length + FILL_LENGTH).apply {
        order(ByteOrder.LITTLE_ENDIAN)
        putInt(message.length + LENGTH)
        putInt(message.length + LENGTH)
        putInt(DouyuDanmakuClient.CODE_CLIENT_SEND_TO_SERVER)
        put(("$message\u0000").toByteArray())
    }.array()

    companion object {
        private const val LENGTH = 9
        private const val FILL_LENGTH = 13
    }
}

class LoginDouyuMessage(roomId: Int) : AbsDouyuMessage("type@=loginreq/roomid@=$roomId/")

class JoinGroupDouyuMessage(roomId: Int) : AbsDouyuMessage("type@=joingroup/rid@=$roomId/gid@=-9999/")

class HeartbeatDouyuMessage : AbsDouyuMessage("type@=keeplive/tick@=${System.currentTimeMillis() / 1000}/")

class LogoutDouyuMessage : AbsDouyuMessage("type@=logout/")