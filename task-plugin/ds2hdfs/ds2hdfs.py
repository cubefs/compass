import os
import time
import threading
import subprocess
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler


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
        # 构建Flume配置内容
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
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.path=/data/ds2hdfs/dolphinscheduler/%Y-%m-%d/{self.relative_path}
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.rollCount=0
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.filePrefix={self.file_name}
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.fileSuffix=
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.fileType=DataStream
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.rollInterval=3600
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.rollSize=10737418240
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.kerberosPrincipal=ds2hdfs_user
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.kerberosKeytab=./ds2hdfs_user.keytab
compass_{self.flume_flag}.sinks.ds2hdfsSink_{self.flume_flag}.hdfs.useLocalTimeStamp=true
"""

        # 将配置内容保存到临时配置文件
        config_file = f'./flume_{self.flume_flag}.conf'
        with open(config_file, 'w') as f:
            f.write(config_content)

        return config_file

    def start_flume(self):
        # 生成Flume配置文件
        config_file = self.init_flume_config()

        # 启动Flume进程，传递文件名、相对路径和绝对路径作为参数
        cmd = f'./flume-1.11.0/bin/flume-ng agent -n compass_{self.flume_flag} -c ./flume-1.11.0/conf/ -f {config_file}'
        self.flume_process = subprocess.Popen(cmd, shell=True)

    def stop_flume(self):
        if self.flume_process:
            print(f"File done: {self.log_file}")
            # 终止Flume进程
            self.flume_process.terminate()
            self.flume_process.wait()
            self.flume_process = None

            # 删除配置文件
            config_file = self.init_flume_config()
            os.remove(config_file)

    def update_status(self, is_updated):
        self.is_updated = is_updated
        if not self.is_updated:
            # 文件不再更新，释放信号量
            self.semaphore.release()

    def set_processing_status(self, is_processing):
        print(f"File processing: {self.log_file}")
        self.is_processing = is_processing

    def start_timer(self, interval):
        # 启动定时器，如果在指定的时间间隔内文件没有更新，则停止Flume进程
        self.timer = threading.Timer(interval, self.stop_flume)
        self.timer.start()

    def stop_timer(self):
        # 停止定时器
        if self.timer and self.timer.is_alive():
            self.timer.cancel()


class LogFileEventHandler(FileSystemEventHandler):
    def __init__(self, root_directory):
        self.flume_handlers = {}
        self.root_directory = root_directory

    def on_modified(self, event):
        if event.is_directory:
            return  # 忽略目录的变化
        elif not os.path.basename(event.src_path).startswith(tuple(map(str, range(10)))):
            return  # 忽略ds本身日志
        elif event.event_type == 'modified':
            # 处理文件内容修改
            print(f"File modified: {event.src_path}")
            log_file = event.src_path
            file_name = os.path.basename(event.src_path)
            relative_path = os.path.dirname(os.path.relpath(log_file, self.root_directory))
            file_path = log_file
            if file_path not in self.flume_handlers:
                # 创建FlumeHandler并启动Flume进程
                flume_handler = FlumeHandler(file_name, relative_path, log_file)
                flume_handler.start_flume()
                self.flume_handlers[file_path] = flume_handler

            flume_handler = self.flume_handlers[file_path]
            if not flume_handler.is_processing:
                # 如果文件不在处理中，更新文件状态为已更新
                flume_handler.update_status(True)
                flume_handler.stop_timer()
            else:
                # 如果文件在处理中，忽略此次修改事件
                return

            # 启动定时器，设置文件不再更新的时间间隔为180秒
            flume_handler.start_timer(180)

    def on_created(self, event):
        if event.is_directory:
            return  # 忽略目录的变化
        elif not os.path.basename(event.src_path).startswith(tuple(map(str, range(10)))):
            return  # 忽略ds本身日志
        elif event.event_type == 'created':
            # 处理新创建的文件
            print(f"File created: {event.src_path}")
            log_file = event.src_path
            file_name = os.path.basename(event.src_path)
            relative_path = os.path.dirname(os.path.relpath(log_file, self.root_directory))

            file_path = log_file
            if file_path not in self.flume_handlers:
                # 创建FlumeHandler并启动Flume进程
                flume_handler = FlumeHandler(file_name, relative_path, log_file)
                flume_handler.start_flume()
                self.flume_handlers[file_path] = flume_handler
            flume_handler = self.flume_handlers[file_path]
            if not flume_handler.is_processing:
                # 如果文件不在处理中，更新文件状态为已更新
                flume_handler.update_status(True)
                flume_handler.stop_timer()
            else:
                # 如果文件在处理中，忽略此次创建事件
                return

            # 启动定时器，设置文件不再更新的时间间隔为180秒
            flume_handler.start_timer(180)


if __name__ == "__main__":
    # 设置工作目录
    working_directory = "/opt/ds2hdfs/"
    os.chdir(working_directory)
    # 监听的根目录
    directory_to_watch = "/opt/soft/dolphinscheduler/logs/"
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
