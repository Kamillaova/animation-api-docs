package me.func.mod.conversation

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import me.func.mod.Kit
import net.minecraft.server.v1_12_R1.PacketDataSerializer
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload
import org.apache.commons.io.IOUtils
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import ru.cristalix.core.display.DisplayChannels
import ru.cristalix.core.util.UtilNetty
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*


object ModLoader {

    private val mods: MutableMap<String, ByteBuf?> = HashMap()

    @JvmStatic
    fun download(fileUrl: String, saveDir: String): String {
        return try {
            val dir = Paths.get(saveDir)
            if (Files.notExists(dir))
                Files.createDirectory(dir)

            val website = URL(fileUrl)
            val file = File(saveDir + "/" + fileUrl.split('/').last())
            file.createNewFile()
            website.openStream().use { `in` ->
                Files.copy(
                    `in`,
                    file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
            file.path
        } catch (exception: Exception) {
            throw RuntimeException(exception)
        }
    }

    @JvmStatic
    fun loadFromWeb(fileUrl: String, saveDir: String) {
        load(download(fileUrl, saveDir))
    }

    @JvmStatic
    fun loadManyFromWeb(saveDir: String, vararg fileUrls: String) {
        for (url in fileUrls) loadFromWeb(url, saveDir)
    }

    @JvmStatic
    private fun readMod(buffer: ByteBuf, file: File) {
        FileInputStream(file).use { stream ->
            val data = ByteArray(stream.available())
            IOUtils.readFully(stream, data)
            val tmp = Unpooled.buffer(data.size + 4)
            UtilNetty.writeByteArray(tmp, data)
            buffer.writeBytes(ByteBufUtil.getBytes(tmp, 0, tmp.writerIndex(), false))
        }
    }

    @JvmStatic
    fun load(filePath: String) {
        try {
            mods[filePath.split('/').last()] = Unpooled.buffer().also { readMod(it, File(filePath)) }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @JvmStatic
    fun loadAll(dirPath: String) {
        val files = Objects.requireNonNull(
            File(
                "./$dirPath"
            ).listFiles()
        )
        if (files.size > 100) throw UnsupportedOperationException("To many files in dir: $dirPath")
        for (file in files) {
            // ?????????? ?????? ?????????????? ???????????? ????????, ???????? ??????????????????????
            try {
                mods[file.name] = Unpooled.buffer().also { readMod(it, file) }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun send(modName: String, player: Player) {
        val mod = mods.getOrDefault(modName, null)
            ?: throw RuntimeException("Mod sending failure, mod is null: $modName")
        (player as CraftPlayer).handle.playerConnection.sendPacket(
            PacketPlayOutCustomPayload(
                DisplayChannels.MOD_CHANNEL,
                PacketDataSerializer(mod.retainedSlice())
            )
        )
    }

    @JvmStatic
    fun manyToOne(player: Player) {
        for ((key, _) in mods) {
            if (Kit.values().any { it.fromUrl.split('/').last() == key })
                continue

            send(key, player)
        }
    }

    @JvmStatic
    fun oneToMany(modName: String) {
        for (player in Bukkit.getOnlinePlayers()) {
            send(modName, player)
        }
    }

    @JvmStatic
    fun sendAll() {
        for (player in Bukkit.getOnlinePlayers()) {
            manyToOne(player)
        }
    }
}