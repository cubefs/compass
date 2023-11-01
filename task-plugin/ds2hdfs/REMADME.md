# Flume Implementation for DolphinScheduler Log Upload to HDFS

## Plugin Functionality
The native flume-taildir-source plugin does not support recursive traversal of subdirectories. This plugin accomplishes directory recursion using Python code.

## Version Dependencies
- python3.7+ (requires watchdog, threading, and time modules)
- flume-1.11.0

## Environment Dependencies
- DolphinScheduler worker can access HDFS correctly

## Startup Script
```shell
cd <working_directory>
nohup python3 ./ds2hdfs.py > ./ds2hdfs.log 2>&1 &
```

## Configuration Instructions
  - Configure the Flume sink parameters in the ds2hdfs.py according to your specific requirements.
```
The parameters under the 'sink' section, including hdfs.path, kerberosKeytab, and kerberosPrincipal, need to be configured as per your needs.
```
  - Configure the working directory and the directory_to_watch in the `ds2hdfs.py`:
```
The 'working_directory' is where Flume code is placed.
The 'directory_to_watch' is the DolphinScheduler log directory.
```

These configuration parameters should be adjusted to suit your specific environment and requirements. Ensure that you specify the correct HDFS path, Kerberos settings, and the log directory to monitor in the configuration file.

Once configured, you can use the startup script to launch the Flume plugin, which will monitor the DolphinScheduler log directory and upload the logs to HDFS. Make sure the monitored directory contains the logs you want to upload and has the correct permissions for Flume to read and write to these files.
