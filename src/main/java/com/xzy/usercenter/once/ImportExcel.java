package com.xzy.usercenter.once;

import com.alibaba.excel.EasyExcel;

import java.util.List;


public class ImportExcel {

    /**
     * 读取数据
     */
    public static void main(String[] args) {
        String fileName = "D:\\code\\user_center\\user-center-backend\\src\\main\\resources\\test.xlsx";
//        readByListener(fileName);
        synchronouRead(fileName);
    }

    /**
     * 监听器
     *
     * @param fileName
     */
    public static void readByListener(String fileName) {
        EasyExcel.read(fileName, XingQiuTableUserInfo.class, new TableListener()).sheet().doRead();
    }

    /**
     * 同步读
     *
     * @param fileName
     */
    public static void synchronouRead(String fileName) {
        List<XingQiuTableUserInfo> totalDataList =
                EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        for (XingQiuTableUserInfo xingQiuTableUserInfo : totalDataList) {
            System.out.println(xingQiuTableUserInfo);
        }
    }
}
