package com.vims.common.menu;

import com.system.common.base.Common;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SysMenu extends Common {
    @Transient
    private String keys = Arrays.toString(new String[] { "menu_cd", "menu_seq" });

    @Id
    /***
     * <pre>
     *  menu_cd : 메뉴코드
     * </pre>
     */
    private String menu_cd;

    /***
     * <pre>
     *  menu_nm_kr : 메뉴명_한국어
     * </pre>
     */
    private String menu_nm_kr;

    /***
     * <pre>
     *  menu_nm_en : 메뉴명_영어
     * </pre>
     */
    private String menu_nm_en;

    /***
     * <pre>
     *  menu_nm_mn : 메뉴명_몽골어
     * </pre>
     */
    private String menu_nm_mn;

    /***
     * <pre>
     *  menu_no : 메뉴순서
     * </pre>
     */
    private String menu_no;

    /***
     * <pre>
     *  menu_lvl : 메뉴레벨
     * </pre>
     */
    private String menu_lvl;

    /***
     * <pre>
     *  top_menu_cd : 상위 메뉴코드
     * </pre>
     */
    private String top_menu_cd;

    /***
     * <pre>
     *  url : URL
     * </pre>
     */
    private String url;

    /***
     * <pre>
     *  use_yn : 사용여부
     * </pre>
     */
    private String use_yn;

    /***
     * <pre>
     *  sys_crt_dt : 작성일자
     * </pre>
     */
    private Date sys_crt_dt;

    /***
     * <pre>
     *  sys_crt_usr_id : 작성자ID
     * </pre>
     */
    private String sys_crt_usr_id;

    /***
     * <pre>
     *  sys_upd_dt : 수정일자
     * </pre>
     */
    private Date sys_upd_dt;

    /***
     * <pre>
     *  sys_upd_usr_id : 수정자ID
     * </pre>
     */
    private String sys_upd_usr_id;

    /***
     * <pre>
     *  menu_seq : 메뉴노출순서
     * </pre>
     */
    private String menu_seq;

    /***
     * <pre>
     *  menu_icon :
     * </pre>
     */
    private String menu_icon;

    /***
     * <pre>
     *  prgm_url :
     * </pre>
     */
    private String prgm_url;

    /***
     * <pre>
     *  user_email : 사용자 이메일
     * </pre>
     */
    @Transient
    private String user_email;
    /***
     * <pre>
     *  menu_cd : 메뉴코드
     * </pre>
     */
    @Transient
    private String _menu_cd;

    /***
     * <pre>
     *  menu_nm_kr : 메뉴명_한국어
     * </pre>
     */
    @Transient
    private String _menu_nm_kr;

    /***
     * <pre>
     *  menu_nm_en : 메뉴명_영어
     * </pre>
     */
    @Transient
    private String _menu_nm_en;

    /***
     * <pre>
     *  menu_nm_mn : 메뉴명_몽골어
     * </pre>
     */
    @Transient
    private String _menu_nm_mn;

    /***
     * <pre>
     *  menu_no : 메뉴순서
     * </pre>
     */
    @Transient
    private String _menu_no;

    /***
     * <pre>
     *  menu_lvl : 메뉴레벨
     * </pre>
     */
    @Transient
    private String _menu_lvl;

    /***
     * <pre>
     *  top_menu_cd : 상위 메뉴코드
     * </pre>
     */
    @Transient
    private String _top_menu_cd;

    /***
     * <pre>
     *  url : URL
     * </pre>
     */
    @Transient
    private String _url;

    /***
     * <pre>
     *  use_yn : 사용여부
     * </pre>
     */
    @Transient
    private String _use_yn;

    /***
     * <pre>
     *  sys_crt_dt : 작성일자
     * </pre>
     */
    @Transient
    private String _sys_crt_dt;

    /***
     * <pre>
     *  sys_crt_usr_id : 작성자ID
     * </pre>
     */
    @Transient
    private String _sys_crt_usr_id;

    /***
     * <pre>
     *  sys_upd_dt : 수정일자
     * </pre>
     */
    @Transient
    private String _sys_upd_dt;

    /***
     * <pre>
     *  sys_upd_usr_id : 수정자ID
     * </pre>
     */
    @Transient
    private String _sys_upd_usr_id;

    /***
     * <pre>
     *  menu_seq : 메뉴노출순서
     * </pre>
     */
    @Transient
    private String _menu_seq;

    /***
     * <pre>
     *  menu_icon :
     * </pre>
     */
    @Transient
    private String _menu_icon;

    /***
     * <pre>
     *  prgm_url :
     * </pre>
     */
    @Transient
    private String _prgm_url;

}
