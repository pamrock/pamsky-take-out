package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("select * from user where openid = #{openid}")
    User getByOpenId(String openId);

    /*
    * 插入数据
    * */
    void insert(User user);

    /*
    * 根据id查询用户
    * */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);
}
