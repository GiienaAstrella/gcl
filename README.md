# Giiena's Config Library

The config library used by all of [Giiena]'s mods.

You are welcome to use this library, so long as you follow [LICENSE] and [THIRDPARTY], but no
support will be provided for your use-case.

Players of Giiena's mods should report all issues *including ones related to this library* to the
mod's issue tracker.
Maintainers of Giiena's mods will handle the reporting of GCL related issues to this repository.

The instructions below are mostly for the maintainers Giiena's mods.

## Configure Maven

GCL is available through GitHub Packages Maven registry, which requires authentication.

First, create a [Personal Access Token (classic)][pat classic] with the `read:packages` scope.

Then, create the following file in `~/.gradle/gradle.properties` on Linux and MacOS or
`%USERPROFILE%\.gradle\gradle.properties` on Windows.

``` properties
gpr.user=GITHUB_USERNAME_HERE
gpr.key=TOKEN_HERE
```

You can also configure authentication by setting the `GITHUB_ACTOR` and `GITHUB_TOKEN` environment
variables.

Next, add the Maven repository to `build.gradle`.
For multi-loader mods, you may want to add the repository to
`build-logic/src/main/groovy/multiloader-common.gradle`.

``` groovy
repositories {
    maven {
        name = "GCL"
        url = uri("https://maven.pkg.github.com/GiienaAstrella/gcl")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

## Configure Dependency

In `gradle.properties`, add the following

``` properties
gcl_version=gcl_version=0.1.0
```

### Multi-loader

Add `gcl-common` as a compile only dependency to the common project's `build.gradle`, then follow
the instructions for each loader-specific projects.

``` groovy
dependencies {
    compileOnly("me.giiena.config:gcl-common:${gcl_version}")
}
```

### Fabric

Add `gcl-fabric` as a dependency.
Giiena's mods also includes GCL inside the final mod jar.

``` groovy
dependencies {
    // Compile against GCL Fabric
    implementation("me.giiena.config:gcl-fabric:${gcl_version}")
    // Include GCL Fabric inside the final mod jar
    include("me.giiena.config:gcl-fabric:${gcl_version}")
}
```

### NeoForge

Add `gcl-neoforge` as a dependency.
Giiena's mods also includes GCL inside the final mod jar.

``` groovy
dependencies {
    // Compile against GCL NeoForge WITHOUT including it inside the final mod jar
    implementation("me.giiena.config:gcl-neoforge:$gcl_version")
    // Compile against GCL NeoForge and include it inside the final mod jar
    jarJar(implementation("me.giiena.config:gcl-neoforge:$gcl_version"))
}
```

[Giiena]: https://giiena.me
[LICENSE]: LICENSE
[THIRDPARTY]: THIRDPARTY
[pat classic]: https://github.com/settings/tokens/new