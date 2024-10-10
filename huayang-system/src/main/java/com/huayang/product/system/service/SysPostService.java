package com.huayang.product.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayang.product.common.constant.UserConstants;
import com.huayang.product.common.core.service.BaseService;
import com.huayang.product.common.exception.ServiceException;
import com.huayang.product.common.utils.DateUtils;
import com.huayang.product.system.domain.SysPost;
import com.huayang.product.system.domain.SysUserPost;
import com.huayang.product.system.mapper.SysPostMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.convert.ConvertUtil;
import org.dromara.hutool.core.util.ObjUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 岗位信息 服务层处理
 *
 * @author huayang
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SysPostService extends ServiceImpl<SysPostMapper, SysPost> implements BaseService<SysPost> {
    private static final String CACHE_NAME = "sysPost";
    @Autowired
    private SysUserPostService userPostService;

    /**
     * 查询岗位信息集合
     *
     * @param post 岗位信息
     * @return 岗位信息集合
     */
    public List<SysPost> selectList(SysPost post) {
        LambdaQueryWrapper<SysPost> queryWrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(post.getPostCode())) {
            queryWrapper.like(SysPost::getPostCode, post.getPostCode());
        }
        if (StringUtils.isNotBlank(post.getPostName())) {
            queryWrapper.like(SysPost::getPostName, post.getPostName());
        }
        if (StringUtils.isNotBlank(post.getStatus())) {
            queryWrapper.eq(SysPost::getStatus, post.getStatus());
        }
        return list(queryWrapper);
    }

    /**
     * 根据用户ID查询岗位
     *
     * @param userId 用户ID
     * @return 岗位列表
     */
    public List<SysPost> selectPostsByUserId(Long userId) {
        List<SysPost> userPosts = baseMapper.selectPostsByUserId(userId);
        List<Long> postIdList = userPosts.stream().map(SysPost::getPostId).collect(Collectors.toList());
        List<SysPost> posts = list();
        for (SysPost post : posts) {
            if (postIdList.contains(post.getPostId())) {
                post.setFlag(true);
            }
        }
        return posts;
    }

    /**
     * 批量删除岗位信息
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(String ids) {
        Long[] postIds = ConvertUtil.toLongArray(ids);
        for (Long postId : postIds) {
            SysPost post = getById(postId);
            LambdaQueryWrapper<SysUserPost> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(SysUserPost::getPostId, postId);
            if (userPostService.count(queryWrapper) > 0) {
                throw new ServiceException(String.format("%1$s已分配,不能删除", post.getPostName()));
            }
        }
        return removeByIds(Arrays.asList(postIds));
    }

    /**
     * 新增保存岗位信息
     *
     * @param post 岗位信息
     * @return 结果
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean insertPost(SysPost post) {
        post.setCreateTime(DateUtils.getNowDate());
        return save(post);
    }

    /**
     * 修改保存岗位信息
     *
     * @param post 岗位信息
     * @return 结果
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePost(SysPost post) {
        post.setUpdateTime(DateUtils.getNowDate());
        return updateById(post);
    }

    /**
     * 校验岗位名称是否唯一
     *
     * @param post 岗位信息
     * @return 结果
     */
    @Cacheable(value = CACHE_NAME, unless = "#result == false")
    public boolean checkPostNameUnique(SysPost post) {
        Long postId = ObjUtil.isNull(post.getPostId()) ? -1L : post.getPostId();
        LambdaQueryWrapper<SysPost> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.ne(SysPost::getPostId, postId);
        queryWrapper.eq(SysPost::getPostName, post.getPostName());
        return count(queryWrapper) > 0 ? UserConstants.NOT_UNIQUE : UserConstants.UNIQUE;
    }

    /**
     * 校验岗位编码是否唯一
     *
     * @param post 岗位信息
     * @return 结果
     */
    @Cacheable(value = CACHE_NAME, unless = "#result == false")
    public boolean checkPostCodeUnique(SysPost post) {
        Long postId = ObjUtil.isNull(post.getPostId()) ? -1L : post.getPostId();
        LambdaQueryWrapper<SysPost> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.ne(SysPost::getPostId, postId);
        queryWrapper.eq(SysPost::getPostCode, post.getPostCode());
        return count(queryWrapper) > 0 ? UserConstants.NOT_UNIQUE : UserConstants.UNIQUE;
    }

    /**
     * 通过第查询岗位明细
     *
     * @param postId
     * @return
     */
    @Cacheable(value = CACHE_NAME, unless = "#result == null")
    public SysPost selectById(Long postId) {
        return getById(postId);
    }
}
