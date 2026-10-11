package com.projectnox.app

import android.content.Context
import android.os.Build
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.LinkOption

/**
 * Offline-first Ubuntu Base installer and launcher for ARM64 Android.
 * Runtime binaries and the verified Ubuntu archive are embedded by the Gradle build task.
 */
object UbuntuRuntime {
    data class InstallResult(val success: Boolean, val message: String)

    fun isSupportedDevice(): Boolean =
        Build.SUPPORTED_ABIS.any { it == "arm64-v8a" }

    fun rootfs(context: Context): File =
        File(File(context.filesDir, "ubuntu"), "rootfs")

    fun isInstalled(context: Context): Boolean {
        val root = rootfs(context)
        return File(root, "bin/bash").exists() || File(root, "usr/bin/bash").exists()
    }

    fun install(context: Context, progress: (String) -> Unit): InstallResult {
        if (!isSupportedDevice()) {
            return InstallResult(false, "Este APK inclui o runtime ARM64. Use um celular Android ARM64.")
        }
        if (isInstalled(context)) return InstallResult(true, "Ubuntu já está instalado.")
        val assetManager = context.assets
        val baseDir = File(context.filesDir, "ubuntu")
        val root = rootfs(context)
        try {
            if (root.exists()) root.deleteRecursively()
            if (!baseDir.exists() && !baseDir.mkdirs()) {
                throw IOException("Não foi possível criar a pasta de Ubuntu.")
            }
            if (baseDir.usableSpace < 650L * 1024L * 1024L) {
                throw IOException("Libere pelo menos 650 MB de armazenamento para extrair o Ubuntu Base.")
            }
            progress("Preparando Ubuntu Base ARM64…")
            val links = mutableListOf<Pair<Path, Path>>()
            var extracted = 0
            val rootPath = root.toPath().toAbsolutePath().normalize()
            assetManager.open("ubuntu-base-arm64.tar.gz").use { asset ->
                GzipCompressorInputStream(BufferedInputStream(asset)).use { gzip ->
                    TarArchiveInputStream(gzip).use { tar ->
                        while (true) {
                            val entry = tar.nextTarEntry ?: break
                            val target = rootPath.resolve(entry.name).normalize()
                            if (!target.startsWith(rootPath)) {
                                throw IOException("Entrada inválida no arquivo Ubuntu: ${entry.name}")
                            }
                            when {
                                entry.isDirectory -> Files.createDirectories(target)
                                entry.isSymbolicLink -> {
                                    Files.createDirectories(target.parent)
                                    if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) Files.delete(target)
                                    Files.createSymbolicLink(target, Paths.get(entry.linkName))
                                }
                                entry.isLink -> {
                                    val linkTarget = rootPath.resolve(entry.linkName).normalize()
                                    links.add(target to linkTarget)
                                }
                                entry.isFile -> {
                                    Files.createDirectories(target.parent)
                                    FileOutputStream(target.toFile()).use { output ->
                                        val buffer = ByteArray(32 * 1024)
                                        while (true) {
                                            val count = tar.read(buffer)
                                            if (count < 0) break
                                            output.write(buffer, 0, count)
                                        }
                                    }
                                    val mode = entry.mode
                                    target.toFile().setReadable(mode and 0b100100100 != 0, false)
                                    target.toFile().setWritable(mode and 0b010010010 != 0, false)
                                    target.toFile().setExecutable(mode and 0b001001001 != 0, false)
                                }
                            }
                            extracted++
                            if (extracted % 1200 == 0) progress("Extraindo Ubuntu… $extracted itens")
                        }
                    }
                }
            }
            links.forEach { (link, target) ->
                if (target.startsWith(rootPath) && Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                    Files.createDirectories(link.parent)
                    if (!Files.exists(link, LinkOption.NOFOLLOW_LINKS)) Files.createLink(link, target)
                }
            }
            val shell = File(root, "bin/bash")
            if (!shell.exists()) throw IOException("O arquivo Ubuntu foi extraído, mas /bin/bash não foi encontrado.")
            File(root, "tmp").apply { mkdirs(); setWritable(true, false); setExecutable(true, false) }
            File(root, "root").mkdirs()
            val resolv = File(root, "etc/resolv.conf")
            resolv.parentFile?.mkdirs()
            if (Files.isSymbolicLink(resolv.toPath())) Files.delete(resolv.toPath())
            resolv.writeText("nameserver 1.1.1.1\nnameserver 8.8.8.8\n")
            return InstallResult(true, "Ubuntu Base ARM64 instalado offline. Espaço livre: ${baseDir.usableSpace / (1024 * 1024)} MB.")
        } catch (e: Exception) {
            root.deleteRecursively()
            return InstallResult(false, "Falha ao instalar Ubuntu: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    fun runtimeExecutable(context: Context): File =
        File(context.applicationInfo.nativeLibraryDir, "libproroot.so")

    fun startShell(context: Context): Process {
        check(isSupportedDevice()) { "Este runtime só está incluído para Android ARM64 (arm64-v8a)." }
        check(isInstalled(context)) { "Instale o Ubuntu antes de iniciar o terminal." }
        val runtime = runtimeExecutable(context)
        check(runtime.exists() && runtime.canExecute()) {
            "O runtime PRoot não está disponível no APK. Recompile usando o Gradle para incluir os componentes ARM64."
        }
        val root = rootfs(context).absolutePath
        val command = listOf(
            runtime.absolutePath,
            "-r", root,
            "-0",
            "--link2symlink",
            "-w", "/root",
            "-b", "/dev",
            "-b", "/proc",
            "-b", "/sys",
            "/bin/bash", "--noprofile", "--norc", "-i"
        )
        return ProcessBuilder(command).apply {
            directory(context.filesDir)
            environment()["PROROOT_TMP_DIR"] = context.filesDir.absolutePath
            environment()["HOME"] = "/root"
            environment()["TMPDIR"] = "/tmp"
            environment()["TERM"] = "xterm-256color"
            redirectErrorStream(true)
        }.start()
    }
}
