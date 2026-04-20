# JFX-Runner

This project aims to solve the task of running and manually testing various builds and PRs for the [JavaFX repo](https://github.com/openjdk/jfx) with many different test programs and applications.

## The problem

Up until now, the process of manual testing was quite tedious, especially when doing this across multiple systems:

- Limited IDE and debugger integrations when running a custom JavaFX build and external applications/programs outside the jfx repo. To fix this, you would have to do a lot of IDE-specific setup
- Launcher issues when not using a module path and having the main class and Application class being the same
- You have to manually check out the respective branches each time and build them
- If custom build properties are used, you will have to remember to update them each time you switch test cases/branches again
- More complex projects that use maven dependencies for JavaFX have to be manually adjusted to use the local JavaFX build jars
- On newly created test systems like VMs, everything has to be set up again

## The solution

The jfx-runner project solves all these issues and allows you to build and run jfx with any kind of target program in one action. It has the following features:

- Full integration for code editing and debugging of the jfx codebase and any additional sources. All projects are added automatically. This is done on the gradle level, meaning that any IDE with gradle support will support it
- Automatically clones any needed repos and switches to the correct branches in a separate location to not interfere with your normal development setup
- Automatic generation of any needed launcher classes to run an Application without a module path
- Support to run single files, source directories, and entire gradle projects. Both locally and remote via the git + URL fetch support
- The dependencies of any used gradle projects are automatically substituted with the locally built jfx libraries. Allows seamless testing of larger real-world JavaFX projects
- Automatically configures and updates any defined build properties for the jfx gradle build
- Create your own reusable configurations for your own testing setups

## Usage

- Clone this repository. Fork this repository to set your own configurations that you want to use
- Make sure to have a compatible JDK installed, depending on which JavaFX version you want to use. [sdkman](https://sdkman.io/) is recommended for that
- Edit the [config.properties](/config.properties) file to select the jfx source and application target you want to use
- If you want to see available sources and targets or add custom ones, take a look at the [source.gradle](/source.gradle) and [target.gradle](/target.gradle) files

### IDEs

If you plan on using an IDE, e.g. for a debugger and source code view, then you can just open the project in your IDE. If the gradle integration of the IDE works properly, all JavaFX sources and target projects should automatically be added.

> [!NOTE]
> Since the source and target codebase retrieval of any potential external sources is run during the gradle settings initialization phase, the import might take a while. Refreshing the gradle tasks in an IDE will also take some time for the same reason.

After the gradle project is opened, execute the `run` task of the application plugin for the `jfx-runner` project.

### Terminal

Run the command `./gradlew run` in your terminal.

## Configuration

You are able to define your own jfx sources and application targets that you can combine any way you like. Jfx sources are defined the in the [source.gradle](/source.gradle) file with the following DSL:

```groovy
source("jfx") {
    fetch {
        /**
         * You can change the repo and git ref
         */
        checkoutGit("https://github.com/openjdk/jfx/", "master")
    }
}
```

```groovy
/**
 * If you have a local clone, you can use this one instead
 */
source("sampleJfxLocal") {
    local(file("C:\\Projects\\jfx"))

    /**
     * You can set build properties for the JavaFX build
     */
    buildProperties("COMPILE_WEBKIT=true")
}
```

```groovy
/**
 * This helper method allows you to directly clone a specific pull request branch
 */
source("sampleJfxPrUrl") {
    jfxPrUrl("https://github.com/openjdk/jfx/pull/2145")
}
```

```groovy
/**
 * Alternatively, you can just specify the PR ID
 */
source("sampleJfxPrId") {
    jfxPrId(2146)
}
```

```groovy
/**
 * Use the jfx21u master branch
 */
source("jfx21u") {
    fetch {
        checkoutGit("https://github.com/openjdk/jfx21u/", "master")
    }
}
```

You can then combine any of the sources with an application target to run. These are defined in the [target.gradle](/target.gradle) file with the following DSL:

```groovy
/**
 * Launches an Application (does not have a main method) in the local src
 */
target("sampleApp") {
    sourceFile("io.xpipe.jfx_runner.SampleApp")

    /**
     * Pass custom jvm options to the application
     */
    jvmArgs("-Dprism.order=sw")
}
```

```groovy
/**
 * Includes a directory in the source set and launches the selected main class.
 * This allows for imports to resolve in case the main class references other classes in the src dir
 */
target("sampleExternalSourceDirectory") {
    externalSourceDirectory(file("C:\\Projects\\javafx-test\\src\\main\\java"), "com.crschnick.javafxtest.ContextMenuBug")
}
```

```groovy
/**
 * Includes an external gradle project directory into this project and launches the specified task to run it
 *
 * The dependency substitution to use the custom JavaFX build is done automatically through gradle.
 */
target("sampleExternalGradleProject") {
    externalGradleProject(file("C:\\Projects\\xpipe\\kickstartfx"), ":app:run")
}
```

```groovy
/**
 * Pull a remote git ref, adds the src dir in it to the sources, and runs the main class.
 */
target("sampleFetchSourceDirectory") {
    fetchSourceDirectory("src", "goryachev.bugs.AbstractPrimaryTimer_PoorAnimation_8339606") {
        checkoutGit("https://github.com/andy-goryachev-oracle/Test", "main")
    }
}
```

```groovy
/**
 * Downloads a source file from a URL, adds it to the src, and runs the class.
 */
target("sampleUrl") {
    urlFile("https://github.com/andy-goryachev-oracle/Test/blob/main/src/goryachev/bugs/AbstractPrimaryTimer_PoorAnimation_8339606.java")
}
```

```groovy
/**
 * Runs the monkey tester application from https://github.com/andy-goryachev-oracle/MonkeyTest
 *
 * This requires ant to be installed.
 */
target("monkeytesterGit") {
    fetchSourceDirectory("src", "com.oracle.tools.fx.monkey.MonkeyTesterApp") {
        checkoutGit("https://github.com/andy-goryachev-oracle/MonkeyTest", "main")
    }
    
    customExec { File srcDir, File jfxDir, List<String> jvmArgs ->
        def os = DefaultNativePlatform.currentOperatingSystem
        // Use parent dir as srcDir is in /src/
        // Starting ant on Windows from gradle is weird with multiple suitable scripts
        exec(srcDir.parentFile, true, os.isWindows() ? "ant.bat" : "ant", "-Djavafx.home=\"$jfxDir/build/sdk\"")

        def commandLine = (["java"] + jvmArgs + ["-p", "$jfxDir/build/sdk/lib", "--add-modules", "ALL-MODULE-PATH", "-jar", "$srcDir.parentFile/build/jars/MonkeyTester.jar"]) as String[]
        exec(srcDir.parentFile, true, commandLine)
    }
}
```

## Code quality

If you look through the implementation, you might see that it is not built like a typical gradle plugin/extension and has a lot atypical things going on. The reason for this is that all the logic for extending the gradle build to include a custom JavaFX source and a target application requires to work in the settings initialization phase of gradle.

This means that for the implementation files there is no `buildSrc` available, the `project` variable is not defined yet, no `build.gradle` file is run yet, and plugins are also not available yet. The implementation had to go another way of relying on a pure groovy implementation that is quite detached from the typical gradle workflow.

## TODO

- Add functionality to install JavaFX build dependencies automatically according to https://wiki.openjdk.org/spaces/OpenJFX/pages/8257548/Building+OpenJFX#BuildingOpenJFX-PlatformPrerequisites. See [/install](/install) for current state.