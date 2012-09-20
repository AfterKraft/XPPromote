XPPromote
=========

This is a Maven build project. It can be packaged without Maven.
I'm using Maven for dependency and API sorting (Eclipse has a nice way of being able to look at the Hierarchy
etc. So since Vault isn't a real Maven project, and if you wish to set up this project with Maven in Eclipse,
please follow these steps:

1)Gitclone this project
2)Import project as a general project
3)Change the .project to use Java nature
4)Convert to Maven
5)If you already have Bukkit and Craftbukkit as projects in eclipse, then continue, if not go here: http://wiki.bukkit.org/Setting_Up_Your_Workspace
6)Right click on XPPromote project > Maven > Add Dependency
7)In the "Enter groupId, artifactId, sha1 prefix or pattern:" field, type bukkit
8)Select org.bukkit and select 1.3.2-R0.2-SNAPSHOT (It's no different from 1.3.2-R0.1 minus some chunk changes)
9)Go ahead and follow this instruction as well: http://dev.bukkit.org/server-mods/vault/forum/29086-maven-pom-xml-entry/?post=4 Replacing 1.16 with 1.2.18
10)If you have never used Maven before, I hope this helps somewhat. When hovering over methods from bukkit, now Eclipse will actually take you to the api.

If there's any problems, let me know