# (WIP) Compass Playground based on docker compose

Included components:

- [X] Postgres
- [X] Zookeeper
- [X] Kafka
- [X] Redis
- [X] Elasticsearch
- [X] Minio
- [X] Hive Metastore
- [X] Dolphinscheduler Services (Optional) [dolphinscheduler-tools、dolphinscheduler-api、dolphinscheduler-alert-server、dolphinscheduler-master、dolphinscheduler-worker(with spark 3.3)]
- [X] Compass Services

# Install Compass Playground

Build compass distribution and copy it to docker/playground:

```
mvn package -DskipTests -Pdist

cp dist/compass-v1.1.2.tar.gz docker/playground
```

Create and start all containers:

```
cd docker/playground

## Start dependent components
docker compose up -d

## With Dolphinscheduler (Optional)
# init dolphinscheduler database
docker compose --profile schema up -d

# start dolphinscheduler services
docker compose --profile dolphinscheduler up -d

## Start compass
docker compose --profile compass up -d
```
