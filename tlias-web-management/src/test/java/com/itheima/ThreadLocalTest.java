package com.itheima;

import net.bytebuddy.asm.Advice;

public class ThreadLocalTest {
    //创建ThreadLocal对象
    private static ThreadLocal<String> local = new ThreadLocal<>();
    //泛型中因为是测试就随便整个字符串

    //模拟用户服务类
    public static void main(String[] args) throws InterruptedException {//主线程
        //设置当前线程的局部变量的值
        local.set("主线程");//主线程

        //创建线程
        Thread thread1=new Thread(()-> {
                local.set("子线程");
                //尝试获取主线程的数据并输出
                System.out.println(Thread.currentThread().getName()+":"+local.get());
            //此线程中没有获取到主线程存入的数据，
        });
        thread1.start();//start方法不会立即执行线程中的代码，具体什么时候执行由CPU调度决定
        //主线程和子线程是并发执行的，但是可能主线程执行完了，但是子线程还在等待CPU调度
        //因为主线程不需要等待CPU调度

        //如果需要主线程等待子线程执行完毕，加上
        thread1.join();
        //获取返回当前线程的局部变量值---主线程
        System.out.println(Thread.currentThread().getName()+":"+local.get());
        //清除当前线程的局部变量值
        local.remove();

    }


}
