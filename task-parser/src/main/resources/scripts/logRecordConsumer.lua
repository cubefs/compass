redis.replicate_commands()
local logRecordList = KEYS[1]
local processingHash = KEYS[2]
local logRecord = redis.call("RPOP", logRecordList)
if logRecord ~= false then
    local msg = cjson.decode(logRecord)
    msg.consumeCount = msg.consumeCount + 1
    redis.call("HSET", processingHash, msg.id, cjson.encode(msg))
    return cjson.encode(msg)
end
return nil


