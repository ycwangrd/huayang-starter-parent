package com.huayang.product.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayang.product.system.domain.SysRoleMenu;
import com.huayang.product.system.mapper.SysRoleMenuMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wangrd
 * @version 1.0
 * @date 2024年07月30日 星期二 18:09
 * @desc
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SysRoleMenuService extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> {
}
