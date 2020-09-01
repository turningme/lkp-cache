package org.lkpnotice.infra.requestMemFromOs;

import java.util.Vector;

/**
 * Created by jpliu on 2020/9/1.
 * malloc备用使用的是mmap，如果你用strace跟踪jvm的话，你会发现每次jvm启动都会申请一定的内存
 * mmap(NULL, 134217728, PROT_NONE, MAP_PRIVATE|MAP_ANONYMOUS|MAP_NORESERVE, -1, 0) = 0x7f0eb26ca000
 * sudo strace -f java -Xms8192m -XX:NativeMemoryTracking=summary foo.java
 * jvm是在什么时候申请os的内存的? - 知乎
 * 深入理解JVM内存区域与内存分配 - 不仅仅是一位码农 - 博客园
 *
 *
 * 我把最小堆内存设置成1G，运行一个较小的程序，在系统的任务管理器中看到程序只占用了300多m。JVM不是在Java程序运行时一次性申请-Xms设置的大小的吗？如果是程序需要用到那么大的内存是才申请，那-Xms这个设置还有什么意义，就直接只设置最大堆内存不就好了吗？
 *
 *
 * 注意区别申请的大小和实际占用的大小。
 -Xms 表示启动时要向OS申请的大小，相当于是提前锁定，OS会为JVM分配虚拟地址空间，但该空间并未mapping到真是物理内存地址，只有在使用时产生一个缺页中断，才会生成真正的物理地址mapping。
 你看到的300m是JVM实际使用的，只有这部分内存才会真正的分配物理内存地址空间。

 pmap -x 14734 |sort -k 3 -n -r |more
 strace -f -e"brk,mmap,munmap" -p 14734

 */
public class Main {
    public static void main(String[] args) {
        // Prints "Hello, World" to the terminal window.
        System.out.println("Hello, World");
        Vector v = new Vector();
        while (true)
        {
            byte b[] = new byte[1048576];
            v.add(b);
            Runtime rt = Runtime.getRuntime();
            System.out.println( "free memory: " + rt.freeMemory() );
        }
    }
}
