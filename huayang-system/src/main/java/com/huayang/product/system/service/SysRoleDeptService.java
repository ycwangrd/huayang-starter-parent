package com.huayang.product.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayang.product.system.domain.SysRoleDept;
import com.huayang.product.system.mapper.SysRoleDeptMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wangrd
 * @version 1.0
 * @date 2024年07月30日 星期二 18:07
 * @desc
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SysRoleDeptService extends ServiceImpl<SysRoleDeptMapper, SysRoleDept> {
}
