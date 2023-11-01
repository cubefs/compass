# flume 实现 DolphinScheduler 日志上传 hdfs

## 插件功能说明
原生flume-taildir-source插件是不支持递归遍历子目录文件的，本插件通过python代码实现目录递归解析。

## 版本依赖
  - python3.7+ (依赖 watchdog threading time 模块)
  - flume-1.11.0

## 环境依赖
  - DolphinScheduler worker 能正常访问hdfs 

## 启动脚本
```
cd <working_directory>; nohup python3 ./ds2hdfs.py > ./ds2hdfs.log 2>&1 &
```

## 配置说明
   - ds2hdfs.py 中 flume sink 参数根据实际情况配置
```
sink 下的hdfs.path , kerberosKeytab , kerberosPrincipal 需要自行配置
```
  - ds2hdfs.py 中的工作目录和directory_to_watch 目录需要配置
``` 
    working_directory 目录下放置flume代码等
    directory_to_watch DolphinScheduler 日志目录
```
