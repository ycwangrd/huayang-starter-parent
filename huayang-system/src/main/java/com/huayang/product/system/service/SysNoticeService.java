package com.huayang.product.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayang.product.common.core.service.BaseService;
import com.huayang.product.common.utils.DateUtils;
import com.huayang.product.system.domain.SysNotice;
import com.huayang.product.system.mapper.SysNoticeMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.convert.ConvertUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 公告 服务层实现
 *
 * @author huayang
 * @date 2018-06-25
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SysNoticeService extends ServiceImpl<SysNoticeMapper, SysNotice> implements BaseService<SysNotice> {
    private static final String CACHE_NAME = "sysNotice";

    /**
     * 查询公告列表
     *
     * @param entity 公告信息
     * @return 公告集合
     */
    public List<SysNotice> selectList(SysNotice entity) {
        LambdaQueryWrapper<SysNotice> queryWrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotEmpty(entity.getNoticeTitle())) {
            queryWrapper.like(SysNotice::getNoticeTitle, entity.getNoticeTitle());
        }
        if (StringUtils.isNotEmpty(entity.getNoticeType())) {
            queryWrapper.eq(SysNotice::getNoticeType, entity.getNoticeType());
        }
        if (StringUtils.isNotEmpty(entity.getCreateBy())) {
            queryWrapper.like(SysNotice::getCreateBy, entity.getCreateBy());
        }
        // 排序
        queryWrapper.orderByDesc(SysNotice::getCreateTime);
        return list(queryWrapper);
    }

    /**
     * 保存公告
     *
     * @param notice
     * @return
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean insertNotice(SysNotice notice) {
        notice.setCreateTime(DateUtils.getNowDate());
        return save(notice);
    }

    /**
     * 通过id查询公告
     *
     * @param noticeId
     * @return
     */
    @Cacheable(value = CACHE_NAME, unless = "#result == null")
    public SysNotice selectById(Long noticeId) {
        return getById(noticeId);
    }

    /**
     * 更新公告
     *
     * @param notice
     * @return
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNotice(SysNotice notice) {
        notice.setUpdateTime(DateUtils.getNowDate());
        return updateById(notice);
    }

    /**
     * 删除公告
     *
     * @param ids
     * @return
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(String ids) {
        return removeByIds(Arrays.asList(ConvertUtil.toLongArray(ids)));
    }
}
