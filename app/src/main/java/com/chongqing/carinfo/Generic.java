package com.chongqing.carinfo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.text.format.Time;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;

public class Generic {
    public static void showMsg(Context c, String msg, boolean flag){
    if(flag)
    /**
     * Toast是已经用于显示给用户的控件，显示一段时间后消失，可以多久消失
     * LENGTH_SHORT：断的显示时间
     * LENGTH_LONG :长的显示时间
     */
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
    else
        Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
}


    // get sysTime
    public static String getSysNowTime() {
        Time localTime = new Time();
        localTime.setToNow();
        String strTime = localTime.format("%Y-%m-%d-%H-%M-%S");

        return strTime;
    }

    /**
     * 得到sdcard的路径
     * @return 失败返回null
     */
    public static File getSdCardFile(){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return Environment.getExternalStorageDirectory();
        }
        return null;
    }



    /**
     * 获取所有连接到本wifi热点的手机IP地址
     */
    public static ArrayList<String> getConnectedIP() {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" ");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connectedIP;
    }

    /**
     * 得到照片的缩略图
     * @param f 照片文件
     //* @param w 图片缩小的目标宽度
     //* @param h 图片缩小的目标高度
     * @return
     * 1.根据android提供的BitmapFactory.Options类创建并设置好options
     * 2.根据File获得流对象
     * 3.根据BitmapFactory.decodeStream获得位图
     * 4.改变图片为居中缩放，返回位图
     */
    public static Bitmap getShrinkedPic(File f){
        Bitmap smallBitmap = null;

        // 直接通过图片路径将图片转化为bitmap,并将bitmap压缩，避免内存溢出
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 10;// 图片宽高都为原来的十分之一
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;// 每个像素占用2byte内存
        options.inPurgeable = true;// 如果 inPurgeable
        // 设为True的话表示使用BitmapFactory创建的Bitmap
        // 用于存储Pixel的内存空间在系统内存不足时可以被回收
        options.inInputShareable = true;
        FileInputStream fInputStream;
        try {
            fInputStream = new FileInputStream(f);
            // 建议使用BitmapFactory.decodeStream
            Bitmap bitmap = BitmapFactory.decodeStream(
                    fInputStream, null, options);// 直接根据图片路径转化为bitmap
            smallBitmap = ThumbnailUtils.extractThumbnail(
                    bitmap, 64, 48);// 创建所需尺寸居中缩放的位图
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        return smallBitmap;
    }

    /**
     * Integer值越大，则排在前面
     * @author Administrator
     *
     */
    public static class DescendSortByIndex implements Comparator<Integer> {
        /**
         * @return 负数：object2<object1，正数：object2>object1，0：相等
         */
        @Override
        public int compare(Integer object1, Integer object2) {

            return object2.compareTo(object1);
        }

    }

    /**
     * File的最后修改时间值越大，则排在前面
     * @author Administrator
     *
     */
    public static class DescendSortByTime implements Comparator<File>{
        /**
         * @return 负数：object2<object1，正数：object2>object1，0：相等
         */
        @Override
        public int compare(File object1, File object2) {

            return (int) (object2.lastModified() - object1.lastModified());
        }

    }
}