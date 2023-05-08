package com.atguigu.gulimall.member.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * 
 * @author xuyang.li
 * @email xuyang.li@gmail.com
 * @date 2023-05-08 18:31:12
 */
@Data
@TableName("ums_social_user")
public class SocialUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键id
	 */
	@TableId
	private Long id;
	/**
	 * 会员id
	 */
	private Long memberId;
	/**
	 * 社交类型【0-微博、1-Gitee、2-WeChat】
	 */
	private Integer socialType;
	/**
	 * 访问令牌
	 */
	private String accessToken;
	/**
	 * 访问令牌过期时间
	 */
	private Long expiresIn;
	/**
	 * 社交账号id
	 */
	private String socialId;
	/**
	 * 创建时间
	 */
	@TableField(fill = FieldFill.INSERT)
	private Date createTime;
	/**
	 * 修改时间
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Date updateTime;

}
