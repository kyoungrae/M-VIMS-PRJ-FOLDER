package com.system.common.base;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@MappedSuperclass
public class Common{
    @Transient
    private Integer no;

    @Transient
    private String from_date;

    @Transient
    private String to_date;

    @Transient
    private Integer page_no;

    @Transient
    private Integer total;

    @Transient
    private Integer row_range;

    @Transient
    private Integer offset;

    @Transient
    private String is_not_null;

    @Transient
    private String is_null;

    /***<pre> sort_id : 정렬 기준 ID </pre> */
    @Transient
    private String sort_id;
    
    /***<pre> sort_order : 정렬방법(asc, desc) </pre> */
    @Transient
    private String sort_order;

    /***<pre> sort_id : 정렬 기준 ID </pre> */
    @Transient
    private String _sort_id;

    /***<pre> sort_order : 정렬방법(asc, desc) </pre> */
    @Transient
    private String _sort_order;
}
