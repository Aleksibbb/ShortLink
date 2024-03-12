-- 1. 参数列表
-- 1.1 用户名
local username = KEYS[1]
-- 1.2 时间窗口，单位：秒
local timeWindow = tonumber(ARGV[1])

-- 2. 用户访问次数Key
local accessKey = "short-link:user-flow-risk-control:" .. username

-- 3. 判断Key是否存在
if(redis.call('EXISTS', accessKey) == 0) then
    -- 不存在，递增 并 设置键的过期时间
    local currentAccessCount = redis.call("INCR", accessKey)
    redis.call("EXPIRE", accessKey, timeWindow)
    return currentAccessCount
end

-- 4. 返回当前访问次数
return redis.call("INCR", accessKey)