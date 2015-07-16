# Lightstreamer - Basic Stock-List Demo - Java SE (Swing) Client

<!-- START DESCRIPTION lightstreamer-example-stocklist-client-java -->

This project contains an example of a <b>Java Swing application</b> that employs the [Lightstreamer Java SE client library](http://www.lightstreamer.com/docs/client_javase_api/index.html).

## Live Demo

[![Demo ScreenShot](screen_javaseswing_large.png)](http://demos.lightstreamer.com/JavaSE_Swing_StockListDemo/javase-stocklist-demo.zip)<br>
###[![](http://demos.lightstreamer.com/site/img/play.png) View live demo](http://demos.lightstreamer.com/JavaSE_Swing_StockListDemo/javase-stocklist-demo.zip)<br>
(download javase-stocklist-demo.zip; unzip it; launch `start_demo.bat` on Windows or `start_demo.sh` on Linux)
*To run this demo, you must have Java installed*. If you don't have Java already installed, please download it from [here] (https://www.java.com/en/download/).<BR/>

## Details

This is a Java Swing version of the [Stock-List Demos](https://github.com/Weswit/Lightstreamer-example-StockList-client-javascript), where thirty items are subscribed to.<br>

This app uses the <b>Java SE Client API for Lightstreamer</b> to handle the communications with Lightstreamer Server. A simple user interface is implemented to display the real-time data received from Lightstreamer Server.<br>
You can sort on any columns and drag the columns around.<br>

This application uses the "com.lightstreamer.ls_client" layer of the Lightstreamer Java SE client library to connect to Lightstreamer Server and subscribe to the 30 items. The application code implements auto-reconnection and auto-resubscription logic, together with a connection status indicator.
Java Swing classes are used to display the real-time updates received from Lightstreamer Server. The application code implements a cell highlighting mechanism, too.

<!-- END DESCRIPTION lightstreamer-example-stocklist-client-java -->

## Install

If you want to install a version of this demo pointing to your local Lightstreamer Server, follow these steps:

* Note that, as prerequisite, the [Lightstreamer - Stock- List Demo - Java Adapter](https://github.com/Weswit/Lightstreamer-example-Stocklist-adapter-java) has to be deployed on your local Lightstreamer Server instance. Please check out that project and follow the installation instructions provided with it.
* Launch Lightstreamer Server.
* Download the `deploy.zip` file, which you can find in the [deploy release](https://github.com/Weswit/Lightstreamer-example-StockList-client-java/releases) of this project and extract the `javase-stocklist-demo` folder.
* Launch `start_demo.bat` on Windows or `start_demo.sh` on Linux (please note that the demo tries to connect to http://localhost:8080).

## Build

To build your own version of `java_sld.jar`, instead of using the one provided in the deploy.zip file from the Install section above, follow these steps:

Please consider that this example is comprised of the following folders:
* /src<br>
  Contains the sources to build the java application from the java compiler and its embedded images.

* /lib<br>
  Drop here the `ls-client.jar` from the Lighstreamer SDK for Java SE Clients, to be used for the build process and execution.

* /bin<br>
  Drop here the application jar, as compiled from the provided source files. 

Example of build commands:
```sh
 >javac -source 1.7 -target 1.7 -nowarn -g -classpath lib/ls-client.jar -sourcepath src/javasedemo -d tmp_classes src/javasedemo/swing/StockListDemo.java
 
 >jar cvf java_sld.jar -C tmp_classes javasedemo
```

A couple of shell/batch files that can be used to run the demo:
* batch command:

```cmd
@echo off

set JAVA_HOME=C:\Program Files\Java\jdk1.7.0
set CONF=localhost 8080

call "%JAVA_HOME%\bin\java.exe" -cp "java_sld.jar";"../lib/ls-client.jar" javasedemo.swing.StockListDemo %CONF%
pause
```

* shell command:

```sh
#! /bin/sh

JAVA_HOME=/usr/jdk1.7.0
CONF="localhost 8080"

exec $JAVA_HOME/bin/java -cp "java_sld.jar:../lib/ls-client.jar" javasedemo.swing.StockListDemo $CONF
```

Those scripts are ready to run the client against the default Lightstreamer configuration but it may be necessary to change the reference to the java process inside them.

The example requires that the [QUOTE_ADAPTER](https://github.com/Weswit/Lightstreamer-example-Stocklist-adapter-java) has to be deployed in your local Lightstreamer server instance;
the [LiteralBasedProvider](https://github.com/Weswit/Lightstreamer-example-ReusableMetadata-adapter-java) is also needed, but it is already provided by Lightstreamer server.<br>

## See Also

### Lightstreamer Adapters Needed by This Demo Client
<!-- START RELATED_ENTRIES -->

* [Lightstreamer - Stock- List Demo - Java Adapter](https://github.com/Weswit/Lightstreamer-example-Stocklist-adapter-java)
* [Lightstreamer - Reusable Metadata Adapters- Java Adapter](https://github.com/Weswit/Lightstreamer-example-ReusableMetadata-adapter-java)

<!-- END RELATED_ENTRIES -->

### Related Projects

* [Lightstreamer - Stock-List Demos - HTML Clients](https://github.com/Weswit/Lightstreamer-example-Stocklist-client-javascript)
* [Lightstreamer - Basic Stock-List Demo - jQuery (jqGrid) Client](https://github.com/Weswit/Lightstreamer-example-StockList-client-jquery)
* [Lightstreamer - Stock-List Demo - Dojo Toolkit Client](https://github.com/Weswit/Lightstreamer-example-StockList-client-dojo)
* [Lightstreamer - Basic Stock-List Demo - .NET Client](https://github.com/Weswit/Lightstreamer-example-StockList-client-dotnet)
* [Lightstreamer - Stock-List Demos - Flex Clients](https://github.com/Weswit/Lightstreamer-example-StockList-client-flex)

## Lightstreamer Compatibility Notes

* Compatible with Lightstreamer Java Client API v. 2.5.2 or newer 2.x versions.
* For Lightstreamer Allegro (+ Java Client API support), Presto, Vivace.
