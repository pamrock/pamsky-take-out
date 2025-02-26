package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName AddressBookController
 * @Description 地址簿管理相关接口
 * @Author 12459
 * @Date 2025/2/23 15:25
 **/
@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "地址簿管理相关接口")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /*
    * 新增地址
    * */
    @PostMapping
    @ApiOperation("新增地址")
    public Result save(@RequestBody AddressBook addressBook) {
        log.info("新增地址:{}", addressBook);
        addressBookService.addAddressBook(addressBook);
        return Result.success();
    }

    /*
    * 查询当前登录用户的所有地址信息
    * */
    @GetMapping("/list")
    @ApiOperation("查询当前登录用户的所有地址信息")
    public Result<List<AddressBook>> list() {
        log.info("查询当前登录用户的所有地址信息");
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        List<AddressBook> list = addressBookService.list(addressBook);
        return Result.success(list);
    }

    /*
    * 根据id查询地址
    * */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getById(@PathVariable Integer id) {
        log.info("根据id查询地址:{}", id);
        AddressBook addressBook = addressBookService.getById(id);
        return Result.success(addressBook);
    }

    /*
    * 根据id修改地址
    * */
    @PutMapping
    @ApiOperation("根据id删除地址")
    public Result update(@RequestBody AddressBook addressBook) {
        log.info("根据id修改地址：{}", addressBook);
        addressBookService.update(addressBook);
        return Result.success();
    }

    /*
    * 设置默认地址
    * */
    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefault(@RequestBody AddressBook addressBook) {
        log.info("设置默认地址");
        addressBookService.setDefault(addressBook);
        return Result.success();
    }

    /*
    * 根据id删除地址
    * */
    @DeleteMapping
    @ApiOperation("根据id删除地址")
    public Result delete(Long id){
        log.info("根据id删除地址");
        addressBookService.delete(id);
        return Result.success();
    }

    /*
    * 查询默认地址
    * */
    @GetMapping("/default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> getDefault(){
        log.info("查询默认地址");
        List<AddressBook> list = addressBookService.getDefault();
        if (list != null && list.size() == 1) {
            return Result.success(list.get(0));
        }
        return Result.error("没有默认地址");
    }


}
