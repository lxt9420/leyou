package com.leyou.item.pojo;

import lombok.Data;

import javax.persistence.*;

/**
 * author:lu
 * create time: 2020/1/8.
 */
@Table(name="tb_spec_param")
@Data
public class SpecParam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long cid;
    private Long groupId;
    private String name;
    @Column(name = "`numeric`")
    private Boolean numeric;
    private String unit;
    private Boolean generic;
    private Boolean searching;
    private String segments;
}
