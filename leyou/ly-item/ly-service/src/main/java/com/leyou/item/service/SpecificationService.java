package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.mapper.SpecificationMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author:lu
 * create time: 2020/1/7.
 */
@Service
public class SpecificationService {
    @Autowired
    private SpecificationMapper specificationMapper;
    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParamMapper specParamMapper;

    /**
     * 测试参数规格数据
     * @param id
     * @return
     */
    public Specification queryById(Long id) {
        return this.specificationMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据分类id查询规格组
     * @param id
     * @return
     */
    public List<SpecGroup> queryGroupByCid(Long id) {
        SpecGroup sg = new SpecGroup();
        sg.setCid(id);
        List<SpecGroup> list = this.specGroupMapper.select(sg);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.SPECGROUP_NO_FOND);
        }
        return list;
    }
    /**
     * 查询规格参数集合
     * @param gid
     * @return
     */
    public List<SpecParam> queryParamList(Long gid, Long cid, Boolean searching) {
        SpecParam sp=new SpecParam();
        sp.setGroupId(gid);
        sp.setCid(cid);
        sp.setSearching(searching);
        List<SpecParam> specParamList = this.specParamMapper.select(sp);
        if(CollectionUtils.isEmpty(specParamList)){
            throw new LyException(ExceptionEnum.SPECPRARAM_NOT_FOND);
        }
        return specParamList;
    }
    /**
     * 添加规格组
     * @param specGroup
     * @return
     */
    public void saveSpecGroup(SpecGroup specGroup) {

        int count =this.specGroupMapper.insert(specGroup);
        if(count!=1){
            throw new LyException(ExceptionEnum.SPECGROUP_SAVE_ERROE);
        }

    }

    public void updateSpecGroup(SpecGroup specGroup) {
        int count=this.specGroupMapper.updateByPrimaryKey(specGroup);
        if(count!=1){
            throw new LyException(ExceptionEnum.SPECGROUP_UPDATE_ERROE);
        }
    }

    @Transactional
    public void deleteSpecGroup(Long id) {
        SpecGroup sg = this.specGroupMapper.selectByPrimaryKey(id);
        SpecParam sp=new SpecParam();
        sp.setGroupId(sg.getId());
        this.specParamMapper.delete(sp);
        int count=this.specGroupMapper.deleteByPrimaryKey(sg.getId());
        if(count!=1){
            throw new LyException(ExceptionEnum.SPECGROUP_DELETE_ERROE);
        }
    }

    public void saveSpecParam(SpecParam specParam) {
        int count=this.specParamMapper.insert(specParam);
        if(count!=1){
            throw new LyException(ExceptionEnum.SPECPARAM_SAVE_ERROE);
        }

    }

    public void updateSpecParam(SpecParam specParam) {
        int count =this.specParamMapper.updateByPrimaryKey(specParam);
        if(count!=1){
            throw new LyException(ExceptionEnum.SPECPARAM_UPDATE_ERROE);
        }
    }

    public void deleteSpecParam(Long id) {
        int count= this.specParamMapper.deleteByPrimaryKey(id);
        if(count!=1){
            throw new LyException(ExceptionEnum.SPECPARAM_DELETE_ERROE);
        }
    }


    public List<SpecGroup> queryListByCid(Long cid) {
        //查询规格组
        List<SpecGroup> specGroups = queryGroupByCid(cid);
        //查询当前分类下的参数
        List<SpecParam> specParams = queryParamList(null, cid, null);
        //把规格参数变成map,map的key为规格组的id,map的值是组下的所有参数
        Map<Long,List<SpecParam>> map=new HashMap<>();
        for (SpecParam param : specParams) {

            if(!map.containsKey(param.getGroupId())){
               //这个组id在map中不存在，新增一个List
                map.put(param.getGroupId(),new ArrayList<>());
            }
            map.get(param.getGroupId()).add(param);
        }
        //填充param到group
        for (SpecGroup group : specGroups) {
            group.setParams(map.get(group.getId()));
        }
        return specGroups;
    }
}
