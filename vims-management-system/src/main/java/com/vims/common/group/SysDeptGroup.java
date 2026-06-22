package com.vims.common.group;

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
public class SysDeptGroup extends Common {
    @Transient
	private String keys = Arrays.toString(new String[]{"grp_id"});

    @Id

    /***<pre> grp_id :  </pre> */
	private String grp_id;

    /***<pre> grp_nm : 그룹이름 </pre> */
	private String grp_nm;

    /***<pre> grp_lvl : 그룹레벨 </pre> */
	private String grp_lvl;

    /***<pre> top_grp_id : 상위 그룹아이디 </pre> */
	private String top_grp_id;

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


    /***<pre> menu_cd : menu_cd in common_menu </pre> */
    @Transient
    private String menu_cd;

    /***<pre> grp_id :  </pre> */
    @Transient
	private String _grp_id;

    /***<pre> grp_nm : 그룹이름 </pre> */
    @Transient
	private String _grp_nm;

    /***<pre> grp_lvl : 그룹레벨 </pre> */
    @Transient
	private String _grp_lvl;

    /***<pre> top_grp_id : 상위 그룹아이디 </pre> */
    @Transient
	private String _top_grp_id;

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


}
