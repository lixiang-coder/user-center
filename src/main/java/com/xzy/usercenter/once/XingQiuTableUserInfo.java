package com.xzy.usercenter.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;


@Data
public class XingQiuTableUserInfo {
    /**
     * id
     */
    @ExcelProperty("成员编号")
    private String planetCode;

    /**
     * 用户昵称
     */
    @ExcelProperty("成员昵称")
    private String username;
}