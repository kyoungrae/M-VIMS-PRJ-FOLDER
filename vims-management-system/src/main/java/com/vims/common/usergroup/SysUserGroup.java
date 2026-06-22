package com.vims.common.usergroup;

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
public class SysUserGroup extends Common {
    @Transient
	private String keys = Arrays.toString(new String[]{"grp_id","id"});

    /***<pre> id : id </pre> */
    @Id
    private Long id;

    /***<pre> grp_id : 그룹아이디 </pre> */
	private String grp_id;


    /***<pre> eml : 사용자 email </pre> */
	private String eml;

    /***<pre> user_id : 사용자 ID </pre> */
	private String user_id;

    /***<pre> inst_cd : 소속코드 </pre> */
	private String inst_cd;

    /***<pre> sys_crt_dt : 작성일자 </pre> */
	private Date sys_crt_dt;

    /***<pre> sys_crt_usr_id : 작성자ID </pre> */
	private String sys_crt_usr_id;

    /***<pre> sys_upd_dt : 수정일자 </pre> */
	private Date sys_upd_dt;

    /***<pre> sys_upd_usr_id : 수정자ID </pre> */
	private String sys_upd_usr_id;



    /***<pre> grp_nm : 그룹이름 </pre> */
    @Transient
	private String grp_nm;
    
    /***<pre> fnm : 사용자이름 </pre> */
    @Transient
	private String fnm;

    /***<pre> inst_nm : 소속 </pre> */
    @Transient
    private String inst_nm;

    /***<pre> _id : 아이디 </pre> */
    @Transient
    private String _id;

    /***<pre> grp_id : 그룹아이디 </pre> */
    @Transient
	private String _grp_id;

    /***<pre> eml : 사용자 email </pre> */
    @Transient
	private String _eml;

    /***<pre> user_id : 사용자 ID </pre> */
    @Transient
	private String _user_id;

    /***<pre> inst_cd : 소속코드 </pre> */
    @Transient
	private String _inst_cd;

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
