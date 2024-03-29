/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package com.atguigu.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据
 *
 * @author Mark sunlightcs@gmail.com
 */
public class R extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;

	/**
	 * 如果是复杂类型，比如List<HashMap<xxx,xxx>>。可以通过类型先转JSON，然后JSON转所需类型。
	 * 原因：接收到的复杂类型里面的对象被自动反序列化成Map了,所以只能转JSON接受
	 * 场景：待补充...
	 * @param typeReference
	 * @param <T>
	 * @return
	 */
	public <T> T getData(TypeReference<T> typeReference){
		Object data = get("data");//默认会是map类型
		String str = JSON.toJSONString(data);
		T t = JSON.parseObject(str, typeReference);
		return t;
	}

	/**
	 * 将其转为指定类型并返回
	 * @param name
	 * @param typeReference
	 * @param <T>
	 * @return
	 */
	public <T> T getDataByName(String name,TypeReference<T> typeReference){
		Object data = get(name);//默认会是map类型
		String str = JSON.toJSONString(data);
		T t = JSON.parseObject(str, typeReference);
		return t;
	}

	/**
	 *
	 * @param data
	 * @return
	 */
	public R setData(Object data){
		put("data",data);
		return this;
	}
	
	public R() {
		put("code", 0);
		put("msg", "success");
	}
	
	public static R error() {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
	}
	
	public static R error(String msg) {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
	}
	
	public static R error(int code, String msg) {
		R r = new R();
		r.put("code", code);
		r.put("msg", msg);
		return r;
	}

	public static R ok(String msg) {
		R r = new R();
		r.put("msg", msg);
		return r;
	}
	
	public static R ok(Map<String, Object> map) {
		R r = new R();
		r.putAll(map);
		return r;
	}
	
	public static R ok() {
		return new R();
	}

	public R put(String key, Object value) {
		super.put(key, value);
		return this;
	}

	public Integer getCode(){
		return (Integer) this.get("code");
	}
}
