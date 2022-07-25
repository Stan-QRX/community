package com.newcoder.community;

import java.io.IOException;

public class WkTests {

    public static void main(String[] args) {
        String cmd = " F:/StudySoftWare/wkhtmltopdf/bin/wkhtmltoimage --quality 75  https://www.nowcoder.com F:/StudySoftWare/data/wk-images/2.png";
        try {
           //   调用cmd命令
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
