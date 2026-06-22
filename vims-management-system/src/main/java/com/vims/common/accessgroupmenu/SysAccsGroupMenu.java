package com.vims.common.accessgroupmenu;

import com.system.common.base.Common;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.Arrays;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SysAccsGroupMenu extends Common {
    @Transient
	private String keys = Arrays.toString(new String[]{"menu_cd","acs_rts_grp_id"});

    /***<pre> menu_cd : 메뉴코드 </pre> */
    @Id
	private String menu_cd;

    /***<pre> acs_rts_grp_id : 권한그룹 아이디 </pre> */
	private String acs_rts_grp_id;

    /***<pre> sys_crt_dt : 작성일자 </pre> */
	private Date sys_crt_dt;

    /***<pre> sys_crt_usr_id : 작성자ID </pre> */
	private String sys_crt_usr_id;

    /***<pre> sys_upd_dt : 수정일자 </pre> */
	private Date sys_upd_dt;

    /***<pre> sys_upd_usr_id : 수정자ID </pre> */
	private String sys_upd_usr_id;



    /***<pre> menu_cd : 메뉴코드 </pre> */
    @Transient
	private String _menu_cd;

    /***<pre> acs_rts_grp_id : 권한그룹 아이디 </pre> */
    @Transient
	private String _acs_rts_grp_id;

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
