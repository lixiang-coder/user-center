package com.xzy.usercenter.once;

import com.alibaba.excel.EasyExcel;
import java.util.List;

public class ImportXingQiuUser {
    public static void main(String[] args) {
        String fileName = "D:\\code\\user_center\\user-center-backend\\src\\main\\resources\\test.xlsx";
        List<XingQiuTableUserInfo> totalDataList =
                EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        for (XingQiuTableUserInfo xingQiuTableUserInfo : totalDataList) {
            System.out.println(xingQiuTableUserInfo);
        }
    }
}
