# 2.2-pi-weather-station

## Requirements

- Raspberry Pi 3.
- Git

## Getting Started

There are multiple methods of retrieving the source files:

#### Method 1: Cloning

```
git clone https://github.com/Bloomdex/2.2-pi-weather-station.git
cd 2.2-pi-weather-station/
```

#### Method 2: Saving

```
wget https://github.com/Bloomdex/2.2-pi-weather-station/archive/master.zip
unzip 2.2-pi-weather-station-master.zip
cd 2.2-pi-weather-station/
```

## Building

Building the final product can be done in many ways.  
The source files and resources folders must be compiled into a .jar.  
If no resource folder is present the compiled program will ask for extra information upon launch.

Building a .jar can be varied so here are some methods you could follow:

- [Standard Java method](https://docs.oracle.com/javase/tutorial/deployment/jar/build.html)
- [Build using IntelliJ](https://www.jetbrains.com/help/idea/packaging-a-module-into-a-jar-file.html)

## Setting up the Raspberry Pi

For the Raspberry Pi a light Operating System is preffered. In addition to being light a 64-bit version is also preffered due to performance reasons. In this case wou found that, [OpenSuse JeOS](https://en.opensuse.org/Portal:JeOS) works best in case of performance, but any other light 32-bit Operating System could suffice.

There are many JDKs out there but preffered would be a JDK that runs on Java 8, as this yields the best performance based on testing. This is why we chose to run OpenJDK 8, the default JDK for Linux.

## Connecting to the Pi

- Firstly, the Pi should have the SSH package installed. If it hasn't been installed it, do so using the package manager.
- Get the public IP of the Pi using the **ifconfig** command.
- On the desktop you're accessing the Raspberry Pi from, download and install an SSH application, we used PuTTY, which also comes in handy in a later step.
- Using PuTTY or any other software, fill in the public IP of the Raspberry Pi together with port 22, as this is is standard SSH port.
- Great, we're now connected to the Raspberry Pi, let's send the .jar so we can run it.

### Sending the .jar

If you have PuTTY installed:

- Open the terminal and use the following command: ```pscp ".jar source" user@ip-addr:/file/```
- Fill in the password of the chosen user in the command, the file will now be sent over to the Raspberry Pi.

If you don't have PuTTY installed, the steps will be the exact same but with a different command. Following is the standard scp commando on Linux and Unix machines alike: ```scp ".jar source" user@ip-addr:/file```

## Running

To run the .jar using the installed JDK you can use the following command: ```"jre java source" -server -Xms"max memory"m -Xmx"min memory"m -jar ".jar source"```

The application can be ran with the following arguments: NC.  
NC stands for No Client, when used, the application will not try to set up a connection to a database server. This command can be used when there is no database server present/active, but the application should still run.  
The command could look something like this: ```jre/bin/java -server -Xms128m -Xmx868m -jar pi-weather-station.jar NC```

## Final words

If you followed this readme successfully you should now have the application up and running.

Notice: The data printed on the screen during runtime shows the amount of data collected, parsed and handled/(sent).

All source code is fully documented using Javadoc. Source code can be found [here](https://github.com/Bloomdex/2.2-pi-weather-station)