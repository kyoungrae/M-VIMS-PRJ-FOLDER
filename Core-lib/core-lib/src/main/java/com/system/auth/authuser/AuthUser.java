package com.system.auth.authuser;

import com.system.common.base.Common;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AuthUser extends Common implements UserDetails{
    private Long id;
    private String user_id;
    private String email;
    private String pwd;

    private String offc_cd;
    private String user_nm;
    private String tel;
    private String addr;
    private String addr_dtl;
    private String post_cd;
    private String uuid;

    private Date sys_crt_dt;
    private Date sys_upd_dt;
    private String sys_crt_usr_id;
    private String sys_upd_usr_id;
    /*
    * @Enumerated(EnumType.STRING)
    * spring에게 이것이 enum 이고 enum유형인지 여부에 관계없이 사용하고 싶다고 알리는 것.
    * */
    @Enumerated(EnumType.STRING)
    private Role role;
    /*
    * 권한을 가져오기 목록이 반환되거나 반환되어야 하는 부분
    * */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }
    @Override
    public String getPassword() {
        return pwd;
    }

    @Override
    public String getUsername() {
        return email;
    }

    /*
    * 만료가 되지 않음 확인 메소드; 만료가 되지 않았기 때문에 true 반환으로 변경
    * */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    /*
    * 만료가 되지 않았음으로 잠기지 않음 설정도 true로 변경
    * */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    /*
     * 자격증명이 만료 되지 않았는지 확인하는 메소드 이기 때문에 true로 변경
     * */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
