# Lightstreamer - Basic Stock-List Demo - Java SE (Swing) Client #

This project contains an example of a Java Swing application that employs the Java SE client library.

<table>
  <tr>
    <td style="text-align: left">
      &nbsp;<a href="http://demos.lightstreamer.com/JavaSE_Swing_StockListDemo/demo.jnlp" target="_blank"><img src="screen_javaseswing.png"></a>&nbsp;
      
    </td>
    <td>
      &nbsp;Click here to download and install the application:<br>
      &nbsp;<a href="http://demos.lightstreamer.com/JavaSE_Swing_StockListDemo/demo.jnlp" target="_blank">http://demos.lightstreamer.com/JavaSE_Swing_StockListDemo/demo.jnlp</a>
    </td>
  </tr>
</table>

This is a Java Swing version of the [Stock-List Demos](https://github.com/Weswit/Lightstreamer-example-StockList-client-javascript), where thirty items are subscribed to.<br>

This app uses the <b>Java SE Client API for Lightstreamer</b> to handle the communications with Lightstreamer Server. A simple user interface is implemented to display the real-time data received from Lightstreamer Server.<br>

You can sort on any columns and drag the columns around.<br>
This application uses the "com.lightstreamer.ls_client" layer of the Lightstreamer Java SE client library to connect to Lightstreamer Server and subscribe to the 30 items. The application code implements auto-reconnection and auto-resubscription logic, together with a connection status indicator.
Java Swing classes are used to display the real-time updates received from Lightstreamer Server. The application code implements a cell highlighting mechanism too.

# Build #

If you want to skip the build and deploy processes of this demo please note that you can click the image or link above, a Java application will be downloaded and launched via Java Web Start technology. If Java Web Start is not already installed, you will be automatically redirected to the download site.
When prompted, please accept the digital signature by "www.lightstreamer.com".<br>

Otherwise, if you want to prodedere with the compilation of own version of this demo please consider that this example is comprised of the following folders:
* /src<br>
  Contains the sources to build the java application from the java compiler  and its embedded images.

* /lib<br>
  Drop here the ls-client.jar from the Lighstreamer SDK for Java SE Clients, to be used for the build process and execution.

* /bin<br>
  Drop here the application jar, as compiled from the provided source files. 

Example of build commands:
```sh
 >javac -source 1.7 -target 1.7 -nowarn -g -classpath lib/ls-client.jar -sourcepath src/javasedemo -d tmp_classes src/javasedemo/swing/StockListDemo.java
 
 >jar cvf java_sld.jar -C tmp_classes javasedemo
```

# Deploy #
  
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

The example requires that the [QUOTE_ADAPTER](https://github.com/Weswit/Lightstreamer-example-Stocklist-adapter-java) and [LiteralBasedProvider](https://github.com/Weswit/Lightstreamer-example-ReusableMetadata-adapter-java) have to be deployed in your local Lightstreamer server instance. The factory configuration of Lightstreamer server already provides this adapter deployed.<br>

# See Also #

## Lightstreamer Adapters needed by this demo client ##

* [Lightstreamer - Stock- List Demo - Java SE Adapter](https://github.com/Weswit/Lightstreamer-example-Stocklist-adapter-java)
* [Lightstreamer - Reusable Metadata Adapters- Java SE Adapter](https://github.com/Weswit/Lightstreamer-example-ReusableMetadata-adapter-java)

## Similar demo clients that may interest you ##

* [Lightstreamer - Stock-List Demos - HTML Clients](https://github.com/Weswit/Lightstreamer-example-Stocklist-client-javascript)
* [Lightstreamer - Basic Stock-List Demo - jQuery (jqGrid) Client](https://github.com/Weswit/Lightstreamer-example-StockList-client-jquery)
* [Lightstreamer - Stock-List Demo - Dojo Toolkit Client](https://github.com/Weswit/Lightstreamer-example-StockList-client-dojo)
* [Lightstreamer - Basic Stock-List Demo - .NET Client](https://github.com/Weswit/Lightstreamer-example-StockList-client-dotnet)
* [Lightstreamer - Stock-List Demos - Flex Clients](https://github.com/Weswit/Lightstreamer-example-StockList-client-flex)

# Lightstreamer Compatibility Notes #

- Compatible with Lightstreamer Java Client API v. 2.5.2 or newer.
- For Lightstreamer Allegro (+ Java Client API support), Presto, Vivace.
