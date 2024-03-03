package com.nageoffer.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import com.nageoffer.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.nageoffer.shortlink.admin.dao.entity.UserDO;
import com.nageoffer.shortlink.admin.dao.mapper.UserMapper;
import com.nageoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.service.UserService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.nageoffer.shortlink.admin.common.constant.RedisCacheConstant.*;
import static com.nageoffer.shortlink.admin.common.enums.UserErrorCodeEnum.*;

/**
 * 用户接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private final GroupService groupService;

    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username); // UserDO::getUsername 代表数据库中的 username字段
        UserDO userDO = baseMapper.selectOne(queryWrapper); // baseMapper 代表 UserMapper
        if (userDO == null) { // 判空
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO, result);
        return result;
    }

    @Override
    public boolean hasUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO requestParam) {
        // 1. 判断用户名是否存在
        if (hasUsername(requestParam.getUsername())) {
            throw new ClientException(USER_NAME_EXIST);
        }
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());
        try {
            // 尝试获取锁
            // 为什么拿到锁之后还要判断用户记录创建是否成功？
            // 因为可能有些人刚释放锁，另一个人就获取到了
            if (lock.tryLock()) {
                try{
                    int inserted = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
                    // 2. 判断用户记录是否创建成功e1ebb82dab0e4f5fac8a8ae7891de14f
                    if (inserted < 1) {     // 数据库连接问题、事务问题、数据库异常
                        throw new ClientException(USER_SAVE_ERROR);     //用户保存失败
                    }
                } catch (DuplicateKeyException ex){     // 唯一索引异常
                    throw new ClientException(USER_EXIST);
                }
                // 3. 将用户名信息保存到布隆过滤器
                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
                //注意：这里直接 groupService.saveGroup("默认分组"); 会出错
                groupService.saveGroup(requestParam.getUsername(), "默认分组");
                return; // return 之后还会执行finally
            }
            throw new ClientException(USER_NAME_EXIST);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        // TODO 验证要修改的用户是否是当前登陆用户
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());
        baseMapper.update(BeanUtil.toBean(requestParam, UserDO.class), updateWrapper);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getPassword, requestParam.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        // 1. 判断用户是否存在
        if(userDO == null){
            throw new ClientException(USER_NULL);
        }
        // 2. 判断用户是否登录
        Boolean hasLogin = stringRedisTemplate.hasKey(USER_LOGIN_KEY + requestParam.getUsername());
        if(hasLogin){
            throw new ClientException(USER_HAS_LOGIN);
        }
        String token = UUID.randomUUID().toString(true);
        stringRedisTemplate.opsForHash().put(USER_LOGIN_KEY + requestParam.getUsername(), token, JSON.toJSONString(userDO));
        stringRedisTemplate.expire(USER_LOGIN_KEY + requestParam.getUsername(), USER_LOGIN_TTL, TimeUnit.DAYS);  // 改为30天
        return new UserLoginRespDTO(token);
    }

    @Override
    public Boolean checkLogin(String token, String username) {  //前端回传回来token和用户名
        return stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token) != null;
    }

    @Override
    public void logout(String token, String username) {
        // 先判断用户是否登录
        if(!checkLogin(token, username)){
            throw new ClientException("用户Token不存在或用户未登录");
        }
        stringRedisTemplate.opsForHash().delete(USER_LOGIN_KEY + username, token);
    }
}
