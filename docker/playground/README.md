Create and start all containers:

```
docker compose up -d

# init dolphinscheduler database
docker compose --profile schema up -d

# start dolphinscheduler services
docker compose --profile dolphinscheduler up -d
```
