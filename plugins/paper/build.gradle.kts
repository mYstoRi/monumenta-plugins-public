import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.session.SessionHandler
import org.checkerframework.gradle.plugin.CheckerFrameworkExtension
import org.checkerframework.gradle.plugin.CheckerFrameworkTaskExtension

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.playmonumenta.plugins.java-conventions")
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1" // Generates plugin.yml
    id("net.minecrell.plugin-yml.bungee") version "0.5.1" // Generates bungee.yml
    id("org.hidetake.ssh") version "2.10.1"
    id("org.checkerframework") version "0.6.7"
    id("java")
}

dependencies {
    implementation(project(":adapter_api"))
    implementation(project(":adapter_unsupported"))
    implementation(project(":adapter_v1_16_R3"))
    implementation(project(":adapter_v1_18_R1", "reobf"))
    implementation("org.openjdk.jmh:jmh-core:1.19")
    implementation("org.openjdk.jmh:jmh-generator-annprocess:1.19")
    implementation("com.github.LeonMangler:PremiumVanishAPI:2.6.3")
    implementation("org.checkerframework:checker-qual:3.21.1")
    implementation("net.kyori:adventure-text-minimessage:4.2-ab62718")
    implementation("com.opencsv:opencsv:5.5")
    compileOnly("com.destroystokyo.paper:paper:1.16.5-R0.1-SNAPSHOT")
    compileOnly("dev.jorel.CommandAPI:commandapi-core:6.0.0")
    compileOnly("me.clip:placeholderapi:2.10.4")
    compileOnly("de.jeff_media:ChestSortAPI:12.0.0")
    compileOnly("net.luckperms:api:5.3")
    compileOnly("net.coreprotect:coreprotect:2.15.0")
    compileOnly("com.playmonumenta:scripted-quests:4.5")
    compileOnly("com.playmonumenta:redissync:2.8")
    compileOnly("com.playmonumenta:monumenta-network-relay:1.0")
    compileOnly("com.playmonumenta:structures:7.2")
    compileOnly("com.playmonumenta:worlds:1.1")
    compileOnly("com.playmonumenta:libraryofsouls:4.2")
    compileOnly("com.bergerkiller.bukkit:BKCommonLib:1.15.2-v2")
    compileOnly("com.goncalomb.bukkit:nbteditor:3.2")
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.3.1")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")

    // Bungeecord deps
    compileOnly("net.md-5:bungeecord-api:1.12-SNAPSHOT")
    compileOnly("com.google.code.gson:gson:2.8.5")
    compileOnly("com.playmonumenta:monumenta-network-relay:1.0")
    compileOnly("com.vexsoftware:nuvotifier-universal:2.7.2")
}

group = "com.playmonumenta"
description = "Monumenta Main Plugin"
version = rootProject.version

// Configure plugin.yml generation
bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "com.playmonumenta.plugins.Plugin"
    apiVersion = "1.16"
    name = "Monumenta"
    authors = listOf("The Monumenta Team")
    depend = listOf("CommandAPI", "ScriptedQuests", "NBTAPI")
    softDepend = listOf("MonumentaRedisSync", "PlaceholderAPI", "ChestSort", "LuckPerms", "CoreProtect", "NBTEditor", "LibraryOfSouls", "BKCommonLib", "MonumentaNetworkRelay", "PremiumVanish", "ProtocolLib", "MonumentaStructureManagement", "MonumentaWorldManagement")
}

// Configure bungee.yml generation
bungee {
    name = "Monumenta-Bungee"
    main = "com.playmonumenta.bungeecord.Main"
    author = "The Monumenta Team"
    softDepends = setOf("MonumentaNetworkRelay", "Votifier", "SuperVanish", "PremiumVanish", "BungeeTabListPlus", "LuckPerms")
}

configure<CheckerFrameworkExtension> {
    skipCheckerFramework = false
    excludeTests = true
    checkers = listOf(
        "org.checkerframework.checker.nullness.NullnessChecker"
    )
    extraJavacArgs = listOf(
        "-Awarns",
        "-Xmaxwarns", "10000",
        // Better arrays
        "-AinvariantArrays",
        // Stub files for annotating used libraries
        "-Astubs=$rootDir/../stubs/",
        // assumePure gets rid of lots of false positives at the cost of allowing some false negatives.
        // Appears to not work reliably.
        "-AassumePure",
        // The Map Key Checker is pretty useless, ignore it.
        // Initialisation checks are nice, but cause tons of warnings due to the way bosses are set up.
        "-AsuppressWarnings=keyfor,initialization",
    )
}
tasks.withType(JavaCompile::class).configureEach {
    configure<CheckerFrameworkTaskExtension> {
      skipCheckerFramework = true
    }
}
tasks.register<JavaCompile>("checkerframework") {
    val compileJava by tasks.named<JavaCompile>("compileJava")
    dependsOn(compileJava)
    classpath = compileJava.classpath.plus(files(compileJava.destinationDir))
    sourceCompatibility = compileJava.sourceCompatibility
    targetCompatibility = compileJava.targetCompatibility
    destinationDir = compileJava.destinationDir

    options.isIncremental = false
    source("src/main/java/")
    include(project.findProperty("analyzed.classes") as String? ?: "**/*")
    configure<CheckerFrameworkTaskExtension> {
        skipCheckerFramework = false
    }
}

val basicssh = remotes.create("basicssh") {
    host = "admin-eu.playmonumenta.com"
    port = 8822
    user = "epic"
    agent = true
    knownHosts = allowAnyHosts
}

val adminssh = remotes.create("adminssh") {
    host = "admin-eu.playmonumenta.com"
    port = 9922
    user = "epic"
    agent = true
    knownHosts = allowAnyHosts
}

tasks.create("dev1-deploy") {
    val shadowJar by tasks.named<ShadowJar>("shadowJar")
    dependsOn(shadowJar)
    doLast {
        ssh.runSessions {
            session(basicssh) {
                execute("cd /home/epic/dev1_shard_plugins && rm -f Monumenta*.jar")
                put(shadowJar.archiveFile.get().getAsFile(), "/home/epic/dev1_shard_plugins")
            }
        }
    }
}

tasks.create("dev2-deploy") {
    val shadowJar by tasks.named<ShadowJar>("shadowJar")
    dependsOn(shadowJar)
    doLast {
        ssh.runSessions {
            session(basicssh) {
                execute("cd /home/epic/dev2_shard_plugins && rm -f Monumenta*.jar")
                put(shadowJar.archiveFile.get().getAsFile(), "/home/epic/dev2_shard_plugins")
            }
        }
    }
}

tasks.create("dev3-deploy") {
    val shadowJar by tasks.named<ShadowJar>("shadowJar")
    dependsOn(shadowJar)
    doLast {
        ssh.runSessions {
            session(basicssh) {
                execute("cd /home/epic/dev3_shard_plugins && rm -f Monumenta*.jar")
                put(shadowJar.archiveFile.get().getAsFile(), "/home/epic/dev3_shard_plugins")
            }
        }
    }
}

tasks.create("dev4-deploy") {
    val shadowJar by tasks.named<ShadowJar>("shadowJar")
    dependsOn(shadowJar)
    doLast {
        ssh.runSessions {
            session(basicssh) {
                execute("cd /home/epic/dev4_shard_plugins && rm -f Monumenta*.jar")
                put(shadowJar.archiveFile.get().getAsFile(), "/home/epic/dev4_shard_plugins")
            }
        }
    }
}

tasks.create("mobs-deploy") {
    val shadowJar by tasks.named<ShadowJar>("shadowJar")
    dependsOn(shadowJar)
    doLast {
        ssh.runSessions {
            session(basicssh) {
                execute("cd /home/epic/mob_shard_plugins && rm -f Monumenta*.jar")
                put(shadowJar.archiveFile.get().getAsFile(), "/home/epic/mob_shard_plugins")
            }
        }
    }
}

tasks.create("stage-deploy") {
    val shadowJar by tasks.named<ShadowJar>("shadowJar")
    dependsOn(shadowJar)
    doLast {
        ssh.runSessions {
            session(basicssh) {
                put(shadowJar.archiveFile.get().getAsFile(), "/home/epic/stage/m12/server_config/plugins")
                execute("cd /home/epic/stage/m12/server_config/plugins && rm -f Monumenta.jar && ln -s " + shadowJar.archiveFileName.get() + " Monumenta.jar")
            }
        }
    }
}

tasks.create("build-deploy") {
    val shadowJar by tasks.named<ShadowJar>("shadowJar")
    dependsOn(shadowJar)
    doLast {
        ssh.runSessions {
            session(adminssh) {
                put(shadowJar.archiveFile.get().getAsFile(), "/home/epic/project_epic/server_config/plugins")
                execute("cd /home/epic/project_epic/server_config/plugins && rm -f Monumenta.jar && ln -s " + shadowJar.archiveFileName.get() + " Monumenta.jar")
                execute("cd /home/epic/project_epic/mobs/plugins && rm -f Monumenta.jar && ln -s ../../server_config/plugins/Monumenta.jar")
            }
        }
    }
}

tasks.create("play-deploy") {
    val shadowJar by tasks.named<ShadowJar>("shadowJar")
    dependsOn(shadowJar)
    doLast {
        ssh.runSessions {
            session(adminssh) {
                put(shadowJar.archiveFile.get().getAsFile(), "/home/epic/play/m8/server_config/plugins")
                put(shadowJar.archiveFile.get().getAsFile(), "/home/epic/play/m11/server_config/plugins")
                execute("cd /home/epic/play/m8/server_config/plugins && rm -f Monumenta.jar && ln -s " + shadowJar.archiveFileName.get() + " Monumenta.jar")
                execute("cd /home/epic/play/m8/server_config/plugins && rm -f Monumenta.jar && ln -s " + shadowJar.archiveFileName.get() + " Monumenta.jar")
            }
        }
    }
}

fun Service.runSessions(action: RunHandler.() -> Unit) =
    run(delegateClosureOf(action))

fun RunHandler.session(vararg remotes: Remote, action: SessionHandler.() -> Unit) =
    session(*remotes, delegateClosureOf(action))

fun SessionHandler.put(from: Any, into: Any) =
    put(hashMapOf("from" to from, "into" to into))
