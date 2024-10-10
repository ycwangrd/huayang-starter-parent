package com.huayang.product.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayang.product.common.core.service.BaseService;
import com.huayang.product.system.domain.SysLogininfor;
import com.huayang.product.system.mapper.SysLogininforMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统访问日志情况信息 服务层处理
 *
 * @author huayang
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SysLogininforService extends ServiceImpl<SysLogininforMapper, SysLogininfor> implements BaseService<SysLogininfor> {
    /**
     * 查询系统登录日志集合
     *
     * @param entity 访问日志对象
     * @return 登录记录集合
     */
    public List<SysLogininfor> selectList(SysLogininfor entity) {
        LambdaQueryWrapper<SysLogininfor> queryWrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(entity.getIpaddr())) {
            queryWrapper.like(SysLogininfor::getIpaddr, entity.getIpaddr());
        }
        if (StringUtils.isNotBlank(entity.getStatus())) {
            queryWrapper.eq(SysLogininfor::getStatus, entity.getStatus());
        }
        if (StringUtils.isNotBlank(entity.getLoginName())) {
            queryWrapper.like(SysLogininfor::getLoginName, entity.getLoginName());
        }
        // 时间查询参数
        paramsTimeCondition(queryWrapper, SysLogininfor::getLoginTime, entity.getParams());
        // 排序
        queryWrapper.orderByDesc(SysLogininfor::getLoginTime);
        return list(queryWrapper);
    }

    /**
     * 清空系统登录日志
     */
    @Transactional(rollbackFor = Exception.class)
    public void cleanLogininfor() {
        baseMapper.cleanLogininfor();
    }

    /**
     * 保存系统登录系统
     *
     * @param logininfor
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertLogininfor(SysLogininfor logininfor) {
        save(logininfor);
    }
}
