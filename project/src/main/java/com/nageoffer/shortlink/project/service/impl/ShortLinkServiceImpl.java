package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import com.nageoffer.shortlink.admin.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.common.enums.ValidDateTypeEnum;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.toolkit.HashUtil;
import com.nageoffer.shortlink.project.toolkit.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.*;


/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    /**
     * 生成短链接
     */
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        // fullShortUrl ： 域名 + "/" + 短链接六位后缀
        // 1. 获取短链接后缀
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = requestParam.getDomain() + "/" + shortLinkSuffix;
        // 2. 获取短链接实体对象（需要单独设置的属性：短链接、完整短链接、启用标识）
        ShortLinkDO shortLinkDO = BeanUtil.toBean(requestParam, ShortLinkDO.class);
        shortLinkDO.setShortUri(shortLinkSuffix);
        shortLinkDO.setFullShortUrl(fullShortUrl);
        shortLinkDO.setFavicon(getFavicon(requestParam.getOriginUrl()));
        shortLinkDO.setEnableStatus(0);     // 设置启用
        // 3. 获取短链接跳转实体对象
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO
                .builder()
                .gid(requestParam.getGid())
                .fullShortUrl(fullShortUrl)
                .build();
        try {
            // 3. 保存到 t_link
            baseMapper.insert(shortLinkDO);
            // 4. 保存到 t_link_goto
            shortLinkGotoMapper.insert(shortLinkGotoDO);
        } catch (DuplicateKeyException ex) {    // 捕获到唯一索引冲突
            // TODO 已经误判的短链接如何处理
            // 短链接确实真实存在缓存
            // 短链接不一定存在缓存中
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl);
            ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
            if (hasShortLinkDO != null) {
                // 数据库中确实存在
                log.warn("短链接: {} 重复入库", fullShortUrl);
                throw new ServiceException("短链接重复生成");
            }
        }
        stringRedisTemplate.opsForValue().set(
                String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestParam.getOriginUrl(),
                LinkUtil.getLinkCacheValidDate(requestParam.getValidDate()), TimeUnit.MILLISECONDS);
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        // 4. 返回响应对象
        return ShortLinkCreateRespDTO.builder()
                .gid(requestParam.getGid())
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .build();
    }

    /**
     * 短链接修改存在的问题：
     * t_link 是以 gid 为分片键分表的，因此如果修改时，需要切换分组，那么就需要对数据库的数据进行迁移。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        // 1. 查询数据库
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0);
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDO == null) {
            throw new ClientException("短链接不存在");
        }
        // 2. 构造新的短链接对象
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(hasShortLinkDO.getDomain())
                .shortUri(hasShortLinkDO.getShortUri())
                .fullShortUrl(hasShortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .clickNum(hasShortLinkDO.getClickNum())
                .enableStatus(0)
                .gid(requestParam.getGid())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .favicon(hasShortLinkDO.getFavicon())
                .build();

        // 3. 判断是否更改gid
        // 3.1 gid 不变，更新数据库
        if (Objects.equals(requestParam.getGid(), requestParam.getOriginGid())) {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMENT.getType()), ShortLinkDO::getValidDateType, null);
            baseMapper.update(shortLinkDO, updateWrapper);
        } else {  // 3.2 gid 改变，先删除，再插入。记得修改路由表中的 gid
            LambdaUpdateWrapper<ShortLinkDO> deleteWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .eq(ShortLinkDO::getDelFlag, 0);
            baseMapper.delete(deleteWrapper);
            baseMapper.insert(shortLinkDO);
            // 路由表删除、插入
            LambdaQueryWrapper<ShortLinkGotoDO> gotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkGotoDO::getGid, requestParam.getOriginGid());
            shortLinkGotoMapper.delete(gotoQueryWrapper);
            ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                    .gid(requestParam.getGid())
                    .fullShortUrl(requestParam.getFullShortUrl())
                    .build();
            shortLinkGotoMapper.insert(shortLinkGotoDO);
        }
    }

    /**
     * 分页查询短链接
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    /**
     * 短链接分组内数量查询
     */
    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .eq("del_flag", 0)
                .groupBy("gid");
        List<Map<String, Object>> maps = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(maps, ShortLinkGroupCountQueryRespDTO.class);
    }

    /**
     * 短链接跳转
     */
    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();    // 域名
        String fullShortUrl = serverName + "/" + shortUri;  // 完整短链接
        // 1. 先查Redis
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        // 2. 判断Redis中是否存在
        if(StrUtil.isNotBlank(originalLink)){
            // 2.1 存在，直接跳转
            ((HttpServletResponse) response).sendRedirect(originalLink);
            return;
        }
        // 3. 判断布隆过滤器中是否存在
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if(!contains){
            // 3.1 不存在，直接返回
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        // 4. 存在，查询是否缓存空对象
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if(StrUtil.isNotBlank(gotoIsNullShortLink)){
            // 4.1 查询缓存不为空，即如果查到 缓存的value 为 "-" ，说明是空对象，直接返回
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        // 5. 没有缓存空对象，需要查数据库（避免同一时间，大量请求查询数据库，要加分布式锁)
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try{
            // 5.1 Double Check，防止有的用户，在尝试获取锁的时候，锁被前人释放，但此时缓存中已有数据
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if(StrUtil.isNotBlank(originalLink)){
                // 5.2 存在，直接跳转
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;
            }
            // 5.3 不存在，再去查数据库
            LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
            // 5.4 查询路由表
            if(shortLinkGotoDO == null){
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            // 5.5 查询短链接表
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .eq(ShortLinkDO::getDelFlag, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if(shortLinkDO != null){
                //5.6 查询短链接是否超过有效期（一般来说，对于非永久有效的短链接，Redis中缓存过期，就应该是超过有效期了。这里做个兜底判断）
                if(shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(new Date())){
                    stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                    ((HttpServletResponse) response).sendRedirect("/page/notfound");
                    return;
                }
                stringRedisTemplate.opsForValue().set(
                        String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                        shortLinkDO.getOriginUrl(),
                        LinkUtil.getLinkCacheValidDate(shortLinkDO.getValidDate()), TimeUnit.MILLISECONDS
                );
                ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 生成短链接六位后缀
     */
    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        // 避免生成的短链接重复
        int customGenerateCount = 0;
        String shortUri;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接频繁生成，请稍后再试");
            }
            String originUrl = requestParam.getOriginUrl();
            // originUrl += System.currentTimeMillis();
            shortUri = HashUtil.hashToBase62(originUrl);
            // 布隆过滤器中不存在
            if (!shortUriCreateCachePenetrationBloomFilter.contains(requestParam.getDomain() + "/" + shortUri)) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }

    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }
}
