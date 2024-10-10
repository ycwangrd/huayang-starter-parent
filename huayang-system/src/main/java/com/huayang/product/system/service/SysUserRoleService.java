package com.huayang.product.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayang.product.system.domain.SysUserRole;
import com.huayang.product.system.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wangrd
 * @version 1.0
 * @date 2024年07月30日 星期二 17:17
 * @desc
 */
@Service
@Transactional(readOnly = true)
public class SysUserRoleService extends ServiceImpl<SysUserRoleMapper, SysUserRole> {
}
