plugins {
  kotlin("jvm") version "2.4.20"
  id("com.gradleup.shadow") version "9.6.1"
  id("xyz.jpenilla.run-paper") version "3.1.0"
}

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
  constraints {
    implementation("org.codehaus.plexus:plexus-utils:3.6.1") {
      because("https://www.mend.io/vulnerability-database/CVE-2025-67030/")
    }

    implementation("org.apache.commons:commons-lang3:3.18.0") {
      because("https://www.mend.io/vulnerability-database/CVE-2025-48924/")
    }
  }

  compileOnly("io.papermc.paper:paper-api:26.3.build.+")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

kotlin {
  jvmToolchain(25)
}

tasks {
  build {
    dependsOn(shadowJar)
  }

  runServer {
    // Configure the Minecraft version for our task.
    // This is the only required configuration besides applying the plugin.
    // Your plugin's jar (or shadowJar if present) will be used automatically.
    minecraftVersion("26.3")
    jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")
  }

  processResources {
    val props = mapOf("version" to version, "description" to project.description)
    filesMatching("plugin.yml") {
      expand(props)
    }
  }
}
