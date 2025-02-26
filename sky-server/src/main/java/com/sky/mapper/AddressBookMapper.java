package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AddressBookMapper {

    /*
    * 新增地址
    * */
    @Insert("INSERT INTO address_book (user_id, consignee, sex, phone, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default) " +
            "VALUES (#{userId}, #{consignee}, #{sex}, #{phone}, #{provinceCode}, #{provinceName}, #{cityCode}, #{cityName}, #{districtCode}, #{districtName}, #{detail}, #{label}, #{isDefault})")
    void insert(AddressBook addressBook);

    /*
    * 查询当前登录用户的所有地址信息
    * */
    List<AddressBook> list(AddressBook addressBook);

    /*
    * 根据id修改地址
    * */
    void update(AddressBook addressBook);

    /*
    * 查询默认地址
    * */
    @Select("select * from address_book where user_id = #{useriD} and is_default = 1;")
    AddressBook getDefault(Long uesrId);

    /*
    * 根据id查询地址
    * */
    @Select("select * from address_book where id = #{id}")
    AddressBook getById(Integer id);

    /*
    * 根据id删除地址
    * */
    @Delete("delete from address_book where id = #{id}")
    void deleteById(Long id);
}
