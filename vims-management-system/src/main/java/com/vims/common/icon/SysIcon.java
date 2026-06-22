package com.vims.common.icon;

import com.system.common.base.Common;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SysIcon extends Common {
    @Transient
	private String keys = Arrays.toString(new String[]{});

    @Id
    /***<pre> icon_cd : 아이콘 코드 </pre> */
	private String icon_cd;

    /***<pre> icon_nm : 아이콘 이름 </pre> */
	private String icon_nm;

    /***<pre> icon_cls : 클래스명 </pre> */
	private String icon_cls;



    /***<pre> icon_cd : 아이콘 코드 </pre> */
    @Transient
	private String _icon_cd;

    /***<pre> icon_nm : 아이콘 이름 </pre> */
    @Transient
	private String _icon_nm;

    /***<pre> icon_cls : 클래스명 </pre> */
    @Transient
	private String _icon_cls;


}
