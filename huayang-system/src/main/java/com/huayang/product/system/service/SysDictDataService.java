package com.huayang.product.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayang.product.common.core.domain.entity.SysDictData;
import com.huayang.product.common.core.service.BaseService;
import com.huayang.product.common.utils.DateUtils;
import com.huayang.product.system.mapper.SysDictDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.convert.ConvertUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 字典 业务层处理
 *
 * @author huayang
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SysDictDataService extends ServiceImpl<SysDictDataMapper, SysDictData> implements BaseService<SysDictData> {
    private static final String CACHE_NAME = "sysDictData";

    /**
     * 根据条件分页查询字典数据
     *
     * @param dictData 字典数据信息
     * @return 字典数据集合信息
     */
    public List<SysDictData> selectList(SysDictData dictData) {
        LambdaQueryWrapper<SysDictData> queryWrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotEmpty(dictData.getDictLabel())) {
            queryWrapper.like(SysDictData::getDictLabel, dictData.getDictLabel());
        }
        if (StringUtils.isNotEmpty(dictData.getDictType())) {
            queryWrapper.eq(SysDictData::getDictType, dictData.getDictType());
        }
        if (StringUtils.isNotEmpty(dictData.getStatus())) {
            queryWrapper.eq(SysDictData::getStatus, dictData.getStatus());
        }
        return list(queryWrapper);
    }

    /**
     * 根据字典类型和字典键值查询字典数据信息
     *
     * @param dictType  字典类型
     * @param dictValue 字典键值
     * @return 字典标签
     */
    @Cacheable(value = CACHE_NAME, unless = "#result == null")
    public String selectDictLabel(String dictType, String dictValue) {
        LambdaQueryWrapper<SysDictData> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(SysDictData::getDictLabel);
        queryWrapper.eq(SysDictData::getDictType, dictType);
        queryWrapper.eq(SysDictData::getDictValue, dictValue);
        List<SysDictData> list = list(queryWrapper);
        return CollUtil.isNotEmpty(list) ? list.get(0).getDictLabel() : StringUtils.EMPTY;
    }

    /**
     * 批量删除字典数据
     *
     * @param ids 需要删除的数据
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(String ids) {
        removeByIds(Arrays.asList(ConvertUtil.toStrArray(ids)));
    }

    /**
     * 新增保存字典数据信息
     *
     * @param data 字典数据信息
     * @return 结果
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean insertDictData(SysDictData data) {
        data.setCreateTime(DateUtils.getNowDate());
        return save(data);
    }

    /**
     * 修改保存字典数据信息
     *
     * @param data 字典数据信息
     * @return 结果
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictData(SysDictData data) {
        data.setUpdateTime(DateUtils.getNowDate());
        return updateById(data);
    }

    /**
     * 查找字典数据
     *
     * @param dictCode
     * @return
     */
    @Cacheable(value = CACHE_NAME, unless = "#result == null")
    public SysDictData selectById(Long dictCode) {
        return getById(dictCode);
    }
}
