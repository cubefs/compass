import os
import time
import threading
import subprocess
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

'''
   Flume plugin to collect log
'''
class FlumeHandler:
    def __init__(self, file_name, relative_path, log_file):
        self.file_name = file_name
        self.relative_path = relative_path
        self.log_file = log_file
        self.flume_process = None
        self.is_updated = True
        self.is_processing = False
        self.semaphore = threading.Semaphore(0)
        self.timer = None
        self.flume_flag = '_'.join(self.relative_path.replace('/', '_').split('/') + self.file_name.split('.')[:-1])

    def init_flume_config(self):
        # Building flume configuration contents
        config_content = f"""
compass_{self.flume_flag}.sinks=ds2hdfsSink_{self.flume_flag}
compass_{self.flume_flag}.channels=ds2hdfsCh_{self.flume_flag}
compass_{self.flume_flag}.sources=ds2hdfsSrc_{self.flume_flag}

compass_{self.flume_flag}.sources.ds2hdfsSrc_{self.flume_flag}.type = TAILDIR
compass_{self.flume_flag}.sources.ds2hdfsSrc_{self.flume_flag}.positionFile=./taildir_position/taildir_position_{self.flume_flag}.json
compass_{self.flume_flag}.sources.ds2hdfsSrc_{self.flume_flag}.filegroups = fg1
compass_{self.flume_flag}.sources.ds2hdfsSrc_{self.flume_flag}.filegroups.fg1 = {self.log_file}
compass_{self.flume_flag}.sources.ds2hdfsSrc_{self.flume_flag}.fileHeader = true
compass_{self.flume_flag}.sources.ds2hdfsSrc_{self.flume_flag}.deserializer.maxLineLength = 1024
compass_{self.flume_flag}.sources.ds2hdfsSrc_{self.flume_flag}.channels=ds2hdfsCh_{self.flume_flag}

compass_{self.flume_flag}.channels.ds2hdfsCh_{self.flume_flag}.type=file
compass_{self.flume_flag}.channels.ds2hdfsCh_{self.flume_flag}.capacity=10000
compass_{self.flume_flag}.channels.ds2hdfsCh_{self.flume_flag}.transactionCapacity=100
compass_{self.flume_flag}.channels.ds2hdfsCh_{self.flume_flag}.checkpointDir=./.flume/file-channel/ds2hdfsCh/{self.flume_flag}/checkpoint
compass_{self.flume_flag}.channels.ds2hdfsCh_{self.flume_flag}.dataDirs=./.flume/file-channel/ds2hdfsCh/{self.flume_flag}/data

compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.type=hdfs
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.channel=ds2hdfsCh_{self.flume_flag}
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.path=/flume/dolphinscheduler/%Y-%m-%d/{self.relative_path}
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.rollCount=0
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.filePrefix={self.file_name}
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.fileSuffix=
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.fileType=DataStream
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.rollInterval=3600
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.rollSize=10737418240
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.useLocalTimeStamp=true
"""

        # Save configuration contents to a temporary configuration file
        config_file = f'./flume_{self.flume_flag}.conf'
        with open(config_file, 'w') as f:
            f.write(config_content)

        return config_file

    def start_flume(self):
        # Generate flume configuration file
        config_file = self.init_flume_config()

        # Start flume process, passing file name, relative path, and absolute path as arguments
        cmd = f'./flume/bin/flume-ng agent -n compass_{self.flume_flag} -c ./flume/conf/ -f {config_file}'
        self.flume_process = subprocess.Popen(cmd, shell=True)

    def stop_flume(self):
        if self.flume_process:
            print(f"File done: {self.log_file}")
            # Terminate flume process
            self.flume_process.terminate()
            self.flume_process.wait()
            self.flume_process = None

            # Delete configuration file
            config_file = self.init_flume_config()
            os.remove(config_file)

    def update_status(self, is_updated):
        self.is_updated = is_updated
        if not self.is_updated:
            # Release semaphore as file is no longer updating
            self.semaphore.release()

    def set_processing_status(self, is_processing):
        print(f"File processing: {self.log_file}")
        self.is_processing = is_processing

    def start_timer(self, interval):
        # Start timer to stop the flume process if the file is not updated within the specified time interval.
        self.timer = threading.Timer(interval, self.stop_flume)
        self.timer.start()

    def stop_timer(self):
        # Stop timer
        if self.timer and self.timer.is_alive():
            self.timer.cancel()


class LogFileEventHandler(FileSystemEventHandler):
    def __init__(self, root_directory):
        self.flume_handlers = {}
        self.root_directory = root_directory

    def on_modified(self, event):
        if event.is_directory:
            return  # Ignore changes in directory
        elif not os.path.basename(event.src_path).startswith(tuple(map(str, range(10)))):
            return  # Ignore logs from the ds application itself
        elif event.event_type == 'modified':
            # Process file content modifications
            print(f"File modified: {event.src_path}")
            log_file = event.src_path
            file_name = os.path.basename(event.src_path)
            relative_path = os.path.dirname(os.path.relpath(log_file, self.root_directory))
            file_path = log_file
            if file_path not in self.flume_handlers:
                # Create FlumeHandler and start flume process
                flume_handler = FlumeHandler(file_name, relative_path, log_file)
                flume_handler.start_flume()
                self.flume_handlers[file_path] = flume_handler

            flume_handler = self.flume_handlers[file_path]
            if not flume_handler.is_processing:
                # If file is not being processed, update file status to updated
                flume_handler.update_status(True)
                flume_handler.stop_timer()
            else:
                # If file is being processed, ignore this modification event
                return

            # Start timer and set time interval for file to stop updating as 180 seconds
            flume_handler.start_timer(180)

    def on_created(self, event):
        if event.is_directory:
            return  # Ignore changes in directory
        elif not os.path.basename(event.src_path).startswith(tuple(map(str, range(10)))):
            return  # Ignore logs from the ds application itself
        elif event.event_type == 'created':
            # Process newly created file
            print(f"File created: {event.src_path}")
            log_file = event.src_path
            file_name = os.path.basename(event.src_path)
            relative_path = os.path.dirname(os.path.relpath(log_file, self.root_directory))

            file_path = log_file
            if file_path not in self.flume_handlers:
                # Create FlumeHandler and start flume process
                flume_handler = FlumeHandler(file_name, relative_path, log_file)
                flume_handler.start_flume()
                self.flume_handlers[file_path] = flume_handler
            flume_handler = self.flume_handlers[file_path]
            if not flume_handler.is_processing:
                # If file is not being processed, update file status to updated
                flume_handler.update_status(True)
                flume_handler.stop_timer()
            else:
                # If the file is being processed, ignore this creation event.
                return

            # Start timer and set time interval for file to stop updating as 180 seconds
            flume_handler.start_timer(180)


if __name__ == "__main__":
    # Set working directory
    working_directory = "/opt/soft/"
    os.chdir(working_directory)
    # Watch the root path of log
    directory_to_watch = "/opt/ds/worker-server/logs"
    event_handler = LogFileEventHandler(directory_to_watch)
    observer = Observer()
    observer.schedule(event_handler, directory_to_watch, recursive=True)
    observer.start()

    try:
        while True:
            time.sleep(5)
    except KeyboardInterrupt:
        observer.stop()

    observer.join()
