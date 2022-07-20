package com.andyron;

import org.apache.ibatis.io.DefaultVFS;

import java.io.IOException;
import java.net.URL;

public class VFSTest {
    public static void main(String[] args) throws IOException {
        DefaultVFS vfs = new DefaultVFS();
        System.out.println(vfs.list("."));
        System.out.println(vfs.list("com/andyron"));



    }
}
