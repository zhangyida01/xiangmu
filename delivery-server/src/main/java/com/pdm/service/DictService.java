package com.pdm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pdm.common.BizException;
import com.pdm.entity.SysDictItem;
import com.pdm.mapper.SysDictItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DictService {

    private final SysDictItemMapper dictMapper;

    public Map<String, List<SysDictItem>> byTypes(List<String> types) {
        Map<String, List<SysDictItem>> result = new LinkedHashMap<>();
        for (String t : types) {
            result.put(t, list(t));
        }
        return result;
    }

    public List<SysDictItem> list(String dictType) {
        return dictMapper.selectList(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictType, dictType)
                .eq(SysDictItem::getEnabled, true)
                .orderByAsc(SysDictItem::getSortNo));
    }

    public List<SysDictItem> all() {
        return dictMapper.selectList(new LambdaQueryWrapper<SysDictItem>()
                .orderByAsc(SysDictItem::getDictType).orderByAsc(SysDictItem::getSortNo));
    }

    public Long create(SysDictItem item) {
        ensureUnique(item.getDictType(), item.getItemCode(), null);
        if (item.getSortNo() == null) {
            item.setSortNo(0);
        }
        if (item.getEnabled() == null) {
            item.setEnabled(true);
        }
        dictMapper.insert(item);
        return item.getId();
    }

    public void update(Long id, SysDictItem item) {
        SysDictItem exist = require(id);
        item.setId(exist.getId());
        ensureUnique(item.getDictType(), item.getItemCode(), id);
        dictMapper.updateById(item);
    }

    public void delete(Long id) {
        require(id);
        dictMapper.deleteById(id);
    }

    private SysDictItem require(Long id) {
        SysDictItem item = dictMapper.selectById(id);
        if (item == null) {
            throw BizException.notFound("字典项");
        }
        return item;
    }

    private void ensureUnique(String type, String code, Long excludeId) {
        Long cnt = dictMapper.selectCount(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictType, type)
                .eq(SysDictItem::getItemCode, code)
                .ne(excludeId != null, SysDictItem::getId, excludeId));
        if (cnt != null && cnt > 0) {
            throw BizException.conflict("字典项已存在：" + type + "/" + code);
        }
    }
}