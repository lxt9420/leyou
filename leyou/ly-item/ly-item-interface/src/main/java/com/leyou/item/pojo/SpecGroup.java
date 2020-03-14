package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

/**
 * author:lu
 * create time: 2020/1/8.
 */
@Data
@Table(name = "tb_spec_group")
public class SpecGroup {

    @Id
    @KeySql(useGeneratedKeys =true)
    private Long id;

    private Long cid;

    private String name;
    @Transient
    private List<SpecParam> params;
}
