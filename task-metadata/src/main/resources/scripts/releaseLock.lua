redis.replicate_commands()
if redis.call("GET", KEYS[1]) == ARGV[1] then
    return tostring(redis.call("DEL", KEYS[1]))
else
    return nil
end