# Negativity

A Minecraft AntiCheat for Spigot and Sponge.

## Downloads

[Spigot/Bungeecord](https://www.spigotmc.org/resources/48399)
[Sponge](https://ore.spongepowered.org/Elikill58/Negativity)

## Informations

Need help ? Have a question or something to suggest ?

Contact me via Discord private messages (`Elikill58#0743`) or in my server ([join it here](https://discord.gg/KHRVTX2)).

Suggestions and bug reports can also be filed in [this repository issue tracker](https://github.com/Elikill58/Negativity/issues).

## Using the API

**Note**: For now there is no proper and fixed API, everything should be considered experimental. Anything and can break at any moment.

We provide a Maven repository to easily consume artifacts with various build tools. Here are examples for Gradle and Maven:

With Gradle:

```groovy
repositories {
    maven {
        name = 'eliapp'
        url = 'http://eliapp.fr:8081/repository/maven-public/'
    }
}

dependencies {
    compileOnly 'com.elikill58:negativity:1.4.1-SNAPSHOT'
}
```

With Maven:

```xml
<repositories>
    <repository>
        <id>eliapp</id>
        <url>http://eliapp.fr:8081/repository/maven-public/</url>
    </repository>
</repositories>
<!-- ... -->
<dependencies>
    <dependency>
        <groupId>com.elikill58</groupId>
        <artifactId>negativity</artifactId>
        <version>1.4.1-SNAPSHOT</version>
    </dependency>
</dependencies>
```

Different artifacts are available:
- `negativity-universal`: code shared by all platforms implementations.
- `negativity-<platform>` where `platform` can be `spigot`, `sponge`, `bungeecord` or `velocity`: platform-specific implementation. Includes `negativity-universal` transiently.
- `negativity`: meta-artifact grouping all platform-specific artifacts.

**Note**: If you depends on `negativity` or `negativity-velocity` you will need to add the Sonatype repository `https://oss.sonatype.org/content/groups/public/`.

## Compiling the plugin

Each supported platform has its own subproject.

You can compile individually them by executing `gradlew :<platform>:build` where `<platform>` is either `bungeecord`, `sponge`, `spigot` or `velocity`, the output jar will be in `<platform>/build/libs/`.

It is possible to make a multiplatform distribution by running `gradlew :build`, the output jar will be in `build/libs/`.
