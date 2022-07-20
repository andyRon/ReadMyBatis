package com.andyron.template;

/**
 * 一套打扫卫生的模板
 * 把所有的打扫卫生工作定义了四个大的步骤：准备（prepare）、实施（implement）、善后（windup）和汇报（report）。
 */
public abstract class Cleaning {

    public void clean() {
        prepare();
        implement();
        windup();
        report();
    }

    /**
     * 准备
     */
    abstract void prepare();

    /**
     * 实施
     */
    abstract void implement();

    /**
     * 善后
     */
    abstract void windup();

    /**
     * 汇报
     */
    void report() {
        System.out.println("告诉别人已经打扫完成。");
    }


}
