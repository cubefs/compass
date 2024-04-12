# Compass Playground based on docker compose

Included components:

- [X] Postgres
- [X] Zookeeper
- [X] Kafka
- [X] Redis
- [X] OpenSearch
- [X] Minio
- [X] Hive Metastore
- [X] Dolphinscheduler Services (Optional) [dolphinscheduler-tools、dolphinscheduler-api、dolphinscheduler-alert-server、dolphinscheduler-master、dolphinscheduler-worker(with spark 3.3)]
- [X] Compass Services

Note: You can install virtulbox to start docker compose, which requires about 10G of memory. If you simply start compass-demo and deploy other dependent components (redis, kafka, opensearch, postpresql, etc.) on other machines, the memory required will be very small.

# Install Compass Playground

1. Build compass distribution and copy it to docker/playground:

    ```
    mvn clean package -DskipTests -Pdist
    
    cp dist/compass-*.tar.gz docker/playground
    ```

2. Change vm.max_map_count(for postgresql)

    ```
    # Linux
    sudo sysctl -w vm.max_map_count=262144
    
    # Windows: 
    wsl -d docker-desktop
    sysctl -w vm.max_map_count=262144
    ```

3. Create dependent components (postgres,zookeeper,kafka,redis,opensearch)

    ```
    docker compose --profile dependencies up -d
    ```

    Note: redis-cluster container is to join the redis node into cluster, then it will stop and exit.

    If the dependent components do not use docker, you need to modify the conf/compass_env.sh configuration


4. Start compass components

    You can just start a compass demo (Optional)

    ```
    docker compose --profile compass-demo up -d
    ```

    To Start compass all components, You should download [canal.deployer](https://github.com/alibaba/canal/releases/download/canal-1.1.6/canal.deployer-1.1.6.tar.gz) and [canal.adapter](https://github.com/alibaba/canal/releases/download/canal-1.1.6/canal.adapter-1.1.6.tar.gz) first,
    then modify the conf/compass_env.sh and application-hadoop.yml configuration.

    ```
    cp canal.deployer-*.tar.gz docker/playground
    cp canal.adapter-*.tar.gz docker/playground
    docker rm -f compass-demo (if you start the compass demo)
    docker compose --profile compass up -d --build
    ```

    Web UI : http://127.0.0.1:7075/compass

    Login Account & Password: compass / compass


5. With hadoop (Optional)

    ```
    wget https://archive.apache.org/dist/hadoop/common/hadoop-3.1.1/hadoop-3.1.1.tar.gz
    wget https://dlcdn.apache.org/spark/spark-3.3.4/spark-3.3.4-bin-hadoop3.tgz
    
    docker compose --profile hadoop up -d
    ```

    Run spark example
    ```
    docker exec hadoop /bin/bash -c "/opt/spark/bin/spark-submit --class org.apache.spark.examples.SparkPi --master yarn --deploy-mode cluster --executor-memory 1G  --num-executors 1 /opt/spark/examples/jars/spark-examples_2.12-3.3.4.jar"
    ```

6. With Dolphinscheduler (Optional) (dependent components: dolphinscheduler, hadoop, spark, flume)
    ```
    wget https://archive.apache.org/dist/dolphinscheduler/3.1.5/apache-dolphinscheduler-3.1.5-bin.tar.gz
    wget https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.19/mysql-connector-java-8.0.19.jar
    wget https://archive.apache.org/dist/flume/1.11.0/apache-flume-1.11.0-bin.tar.gz
    
        
    # start dolphinscheduler services
    docker-compose --profile dolphinscheduler up -d --build
    ```

Run dolphinscheduler example, please refer to [dolphinscheduler example document](../../document/manual/ds_example.md)

For more details, please refer to [deployment document](../../document/manual/deployment.md)
