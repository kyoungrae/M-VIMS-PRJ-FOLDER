package com.vims.common.siteconfig;

import com.system.common.base.Common;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.Arrays;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SysSiteConfig extends Common {
    @Transient
    private String keys = Arrays.toString(new String[]{"cfg_key"});


    /***<pre> cfg_grp_id : 설정그룹ID </pre> */
    @Id
    private String cfg_grp_id;

    /***<pre> cfg_key : 설정 키 </pre> */
    private String cfg_key;

    /***<pre> cfg_val : 설정 값 </pre> */
    private String cfg_val;

    /***<pre> description : 설명 </pre> */
    private String description;

    /***<pre> use_yn : 사용여부 </pre> */
    private String use_yn;

    /***<pre> sys_crt_dt : 작성일자 </pre> */
    private Date sys_crt_dt;

    /***<pre> sys_crt_usr_id : 작성자ID </pre> */
    private String sys_crt_usr_id;

    /***<pre> sys_upd_dt : 수정일자 </pre> */
    private Date sys_upd_dt;

    /***<pre> sys_upd_usr_id : 수정자ID </pre> */
    private String sys_upd_usr_id;

    /***<pre> cfg_grp_id : 설정그룹ID </pre> */
    @Transient
    private String _cfg_grp_id;

    /***<pre> cfg_key : 설정 키 </pre> */
    @Transient
    private String _cfg_key;

    /***<pre> cfg_val : 설정 값 </pre> */
    @Transient
    private String _cfg_val;

    /***<pre> description : 설명 </pre> */
    @Transient
    private String _description;

    /***<pre> use_yn : 사용여부 </pre> */
    @Transient
    private String _use_yn;

    /***<pre> sys_crt_dt : 작성일자 </pre> */
    @Transient
    private Date _sys_crt_dt;

    /***<pre> sys_crt_usr_id : 작성자ID </pre> */
    @Transient
    private String _sys_crt_usr_id;

    /***<pre> sys_upd_dt : 수정일자 </pre> */
    @Transient
    private Date _sys_upd_dt;

    /***<pre> sys_upd_usr_id : 수정자ID </pre> */
    @Transient
    private String _sys_upd_usr_id;

    @Transient
    private String use_yn_name;

}
