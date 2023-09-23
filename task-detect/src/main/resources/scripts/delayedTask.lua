redis.replicate_commands()
local delayQueue= KEYS[1]
local processingKey= KEYS[2]
local tasks = redis.call('ZRANGEBYSCORE', delayQueue, '0', ARGV[1])
if next(tasks) ~= nil then
    for _, task in ipairs(tasks) do
        local msg = cjson.decode(task)
        if msg.key ~= nil then
            redis.call("HSET", processingKey, msg.key, task)
        end
        redis.call('ZREM', delayQueue, task)
    end
    return cjson.encode(tasks)
end
return nil

