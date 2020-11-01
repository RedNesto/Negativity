# Negativity

A Minecraft AntiCheat for **Spigot** (1.7.10 to 1.15.2) and **Sponge** (API 7.1.0). (*work also with PaperSpigot*)
It is also compatible with **Bungeecord and Velocity**.
You need to put the plugin on Spigot/Sponge even if you put it on your proxy.

## Informations

Need help ? Have a question or something to suggest ?

Contact me via Discord private messages (`Elikill58#0743`) or in my server ([join it here](https://discord.gg/KHRVTX2)).

Suggestions and bug reports can also be filed in [this repository issue tracker](https://github.com/Elikill58/Negativity/issues).

## How to install ?

1) Download

You can download the plugin here (it's the same plugin, just not on the same website):

[Spigot/Bungeecord](https://www.spigotmc.org/resources/48399),
[Sponge](https://ore.spongepowered.org/Elikill58/Negativity)

2) Add to your serveur

On Sponge, you have to put the downloaded file on **/mods folder**.

On Spigot/Bungeecord/Velocity, you have to put it on **/plugins folder**.

3) **Restart** your serveur

A config file will be automatically generated (at /config/negativity for sponge, /plugins/Negativity for other).

A new folder "lang" will appear on Negativty's config folder.

4) **Enjoy !**

The default config enable you to directly test the plugin !

## I want to test it before download !

You can check it on **server.negativity.fr** (server in 1.8.8, you can connect with 1.8.8 to 1.15.2).

## What are the detected cheat ?

We made a wiki for all [detected cheat](https://github.com/Elikill58/Negativity/wiki/Cheat) which explain what every hack does.

## How detection works ? What do you check ?

If they are enabled, I'm checking a lot of things : movement, action, packet ...
Else, I'm doing anything. So, if you don't need detection, disable it and you will keep resources.

## And for bad connection ? TPS drop ?

By default, if the player have a **ping higher than 150 ms**, it will not create alert.

Also by default, if the server is **less than 19 TPS**, alert will be disabled

## Translation

We support a lot of languages :
- **English** (*en_US*, default lang) By Elikill58 & RedNesto
- **French** (*fr_FR*) By Elikill58
- **Portuguese** (*pr_BR*) By jheyson
- **Norwegian** (*no_NO*) By SuchHero
- **Russian** (*ru_RU*) By VidTu
- **Chinese** (*zh_CN*) By f0rb1d, SGYRE & Dust
- **German** (*de_DE*) By CodingAir
- **Dutch** (*nl_NL*) By DeveloperBoy
- **Swedish** (*sv_SV*) By YungSloop
- **Spanish** (*es_ES*) By SolitaSolaa
- **Vietnamese** (*vi_VN*) By HuyMCYTTM#7592
- **Italian** (*it_IT*) By Herobrine99dan#1564
- **Czech Republic** (*cs_CZ*) By Disordeon#1824
- **Albanian** (*sq_SQ*) By ErzenX#2439
- **Polish** (*pl_PL*) By Tytano#5336

To change the lang, just use "/nlang" or change "Translation.default" to the one that you want.

You find a grammar error on your language ? You want to add your own ? Contact me on discord (link below)

(For developer, you can set your own translation system)

## I don't understand the config, HELP !

For general config, [click here](https://github.com/Elikill58/Negativity/wiki/Configurations).

For ban config, [click here](https://github.com/Elikill58/Negativity/wiki/Bans).

For permissions config, [click here](https://github.com/Elikill58/Negativity/wiki/Permissions).

## Build the plugin

Prerequisites:
- [Git](https://git-scm.com)
- JDK 8 (any distribution should work, [AdoptOpenJDK](https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot) for example)

In the following instructions you will have to execute Gradle tasks. You **must** use the Gradle wrapper distributed in this repository.

Examples will be using `./gradlew`, if you are on Windows you need to use `gradlew.bat` instead.

1. Clone this repository: `git clone https://github.com/Elikill58/Negativity.git`
2. Manually download the following dependencies into `/common/externalPlugins/` (you need to create the directory):
 - [AdvancedBan](https://github.com/DevLeoko/AdvancedBan/releases/tag/v2.1.8)
 - [GadgetsMenu](https://www.spigotmc.org/resources/gadgetsmenu-1-8-1-15-free.10885/download?version=328675)
 - [MaxBans](https://dev.bukkit.org/projects/maxbans/files/2421382)
 - [PacketGate](https://github.com/CrushedPixel/PacketGate/releases/tag/0.1.1)
 - [Floodgate](https://ci.nukkitx.com/job/GeyserMC/job/Floodgate/job/development/) (any platform-specific jar should work)
3. Get Spigot 1.7.10 and 1.13.2 jars and put them in `/spigot/spigotJars/` (you need to create this directory too)
4. Setup a Forge workspace for the Sponge plugin: `./gradlew :sponge:setupDecompWorkspace`. This step will take some time to complete
5. Build the plugin: `./gradlew shadowJar`
 - You can find the all-in-one jar in `/build/libs/`
 - Platform-specific jars are available in their own project subdirectories (`/spigot/build/libs/` for example)
