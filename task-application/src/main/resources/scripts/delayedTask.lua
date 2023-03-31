redis.replicate_commands()
local delayedQueue= KEYS[1]
local processingKey= KEYS[2]
local tasks = redis.call('ZRANGEBYSCORE', delayedQueue, '0', ARGV[1])
if next(tasks) ~= nil then
    for _, task in ipairs(tasks) do
        local msg = cjson.decode(task)
        if msg.key ~= nil then
            redis.call("HSET", processingKey, msg.key, task)
        end
        redis.call('ZREM', delayedQueue, task)
    end
    return cjson.encode(tasks)
end
return nil

