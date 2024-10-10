package com.huayang.product.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayang.product.system.domain.SysUserPost;
import com.huayang.product.system.mapper.SysUserPostMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wangrd
 * @version 1.0
 * @date 2024年07月30日 星期二 17:05
 * @desc
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserPostService extends ServiceImpl<SysUserPostMapper, SysUserPost> {
}
