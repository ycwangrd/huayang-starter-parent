package com.huayang.product.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayang.product.common.constant.UserConstants;
import com.huayang.product.common.core.domain.Ztree;
import com.huayang.product.common.core.domain.entity.SysDictData;
import com.huayang.product.common.core.domain.entity.SysDictType;
import com.huayang.product.common.core.service.BaseService;
import com.huayang.product.common.exception.ServiceException;
import com.huayang.product.common.utils.DateUtils;
import com.huayang.product.common.utils.DictUtils;
import com.huayang.product.system.mapper.SysDictTypeMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.convert.ConvertUtil;
import org.dromara.hutool.core.util.ObjUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典 业务层处理
 *
 * @author huayang
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SysDictTypeService extends ServiceImpl<SysDictTypeMapper, SysDictType> implements BaseService<SysDictType> {
    @Autowired
    private SysDictDataService dictDataService;

    /**
     * 项目启动时，初始化字典到缓存
     */
    @PostConstruct
    public void init() {
        loadingDictCache();
    }

    /**
     * 根据条件分页查询字典类型
     *
     * @param entity 字典类型信息
     * @return 字典类型集合信息
     */
    public List<SysDictType> selectList(SysDictType entity) {
        LambdaQueryWrapper<SysDictType> queryWrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(entity.getDictName())) {
            queryWrapper.like(SysDictType::getDictName, entity.getDictName());
        }
        if (StringUtils.isNotBlank(entity.getDictType())) {
            queryWrapper.like(SysDictType::getDictType, entity.getDictType());
        }
        if (StringUtils.isNotBlank(entity.getStatus())) {
            queryWrapper.eq(SysDictType::getStatus, entity.getStatus());
        }
        // 时间查询条件
        paramsTimeCondition(queryWrapper, SysDictType::getCreateTime, entity.getParams());
        // 排序
        queryWrapper.orderByDesc(SysDictType::getCreateTime);
        return list(queryWrapper);
    }

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    public List<SysDictData> selectByType(String dictType) {
        List<SysDictData> dictDatas = DictUtils.getDictCache(dictType);
        if (CollUtil.isNotEmpty(dictDatas)) {
            return dictDatas;
        }
        LambdaQueryWrapper<SysDictData> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysDictData::getDictType, dictType);
        dictDatas = dictDataService.list(queryWrapper);
        if (CollUtil.isNotEmpty(dictDatas)) {
            DictUtils.setDictCache(dictType, dictDatas);
            return dictDatas;
        }
        return null;
    }

    /**
     * 根据字典类型查询信息
     *
     * @param dictType 字典类型
     * @return 字典类型
     */
    public SysDictType selectByDictType(String dictType) {
        LambdaQueryWrapper<SysDictType> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysDictType::getDictType, dictType);
        return getOne(queryWrapper);
    }

    /**
     * 批量删除字典类型
     *
     * @param ids 需要删除的数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(String ids) {
        Long[] dictIds = ConvertUtil.toLongArray(ids);
        for (Long dictId : dictIds) {
            SysDictType dictType = getById(dictId);
            LambdaQueryWrapper<SysDictData> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(SysDictData::getDictType, dictType.getDictType());
            if (dictDataService.count(queryWrapper) > 0) {
                throw new ServiceException(String.format("%1$s已分配,不能删除", dictType.getDictName()));
            }
            removeById(dictId);
            DictUtils.removeDictCache(dictType.getDictType());
        }
    }

    /**
     * 加载字典缓存数据
     */
    public void loadingDictCache() {
        SysDictData dictData = new SysDictData();
        dictData.setStatus("0");
        Map<String, List<SysDictData>> dictDataMap = dictDataService.selectList(dictData).stream().collect(Collectors.groupingBy(SysDictData::getDictType));
        for (Map.Entry<String, List<SysDictData>> entry : dictDataMap.entrySet()) {
            DictUtils.setDictCache(entry.getKey(), entry.getValue().stream().sorted(Comparator.comparing(SysDictData::getDictSort)).collect(Collectors.toList()));
        }
    }

    /**
     * 清空字典缓存数据
     */
    public void clearDictCache() {
        DictUtils.clearDictCache();
    }

    /**
     * 重置字典缓存数据
     */
    public void resetDictCache() {
        clearDictCache();
        loadingDictCache();
    }

    /**
     * 新增保存字典类型信息
     *
     * @param dict 字典类型信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean insertDictType(SysDictType dict) {
        dict.setCreateTime(DateUtils.getNowDate());
        boolean result = save(dict);
        if (result) {
            DictUtils.setDictCache(dict.getDictType(), null);
        }
        return result;
    }

    /**
     * 修改保存字典类型信息
     *
     * @param dict 字典类型信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictType(SysDictType dict) {
        SysDictType oldDict = getById(dict.getDictId());
        LambdaUpdateWrapper<SysDictData> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(SysDictData::getDictType, dict.getDictType());
        updateWrapper.eq(SysDictData::getDictType, oldDict.getDictType());
        dictDataService.update(updateWrapper);
        boolean result = updateById(dict);
        if (result) {
            SysDictData dictData = new SysDictData();
            dictData.setDictType(dict.getDictType());
            List<SysDictData> dictDatas = dictDataService.selectList(dictData);
            DictUtils.setDictCache(dict.getDictType(), dictDatas);
        }
        return result;
    }

    /**
     * 校验字典类型称是否唯一
     *
     * @param dict 字典类型
     * @return 结果
     */
    public boolean checkDictTypeUnique(SysDictType dict) {
        Long dictId = ObjUtil.isNull(dict.getDictId()) ? -1L : dict.getDictId();
        LambdaQueryWrapper<SysDictType> queryWrapper = new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dict.getDictType())
                .ne(SysDictType::getDictId, dictId);
        List<SysDictType> typeList = list(queryWrapper);
        return CollUtil.isNotEmpty(typeList) ? UserConstants.NOT_UNIQUE : UserConstants.UNIQUE;
    }

    /**
     * 查询字典类型树
     *
     * @param dictType 字典类型
     * @return 所有字典类型
     */
    public List<Ztree> selectDictTree(SysDictType dictType) {
        List<Ztree> ztrees = new ArrayList<>();
        List<SysDictType> dictList = selectList(dictType);
        for (SysDictType dict : dictList) {
            if (UserConstants.DICT_NORMAL.equals(dict.getStatus())) {
                Ztree ztree = new Ztree();
                ztree.setId(dict.getDictId());
                ztree.setName(transDictName(dict));
                ztree.setTitle(dict.getDictType());
                ztrees.add(ztree);
            }
        }
        return ztrees;
    }

    public String transDictName(SysDictType dictType) {
        StringBuffer sb = new StringBuffer();
        sb.append("(" + dictType.getDictName() + ")");
        sb.append("&nbsp;&nbsp;&nbsp;" + dictType.getDictType());
        return sb.toString();
    }

    /**
     * 查询字典类型明细
     *
     * @param dictId
     * @return
     */
    public SysDictType selectById(Long dictId) {
        return getById(dictId);
    }
}
