package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {

    /*
    * 新增地址
    * */
    void addAddressBook(AddressBook addressBook);

    /*
    * 查询当前登录用户的所有地址信息
    * */
    List<AddressBook> list(AddressBook addressBook);

    /*
    * 根据id修改地址
    * */
    void update(AddressBook addressBook);

    /*
    * 设置默认地址
    * */
    void setDefault(AddressBook addressBook);

    /*
    * 根据id查询地址
    * */
    AddressBook getById(Integer id);

    /*
    * 根据id删除地址
    * */
    void delete(Long id);

    /*
    * 查询默认地址
    * */
    List<AddressBook> getDefault();
}
