package org.lkpnotice.infra.enumtest;

import static org.lkpnotice.infra.enumtest.Enumtest.SS.S2;

/**
 * Created by jpliu on 2020/12/17.
 * javap -v org.lkpnotice.infra.enumtest.Enumtest
 *
 * 和 IDEA 的的结果不一样， ASM 插件是能够指导人编写ASM 程序， LIKE ANTLR JAVAC YACC 等一样的动西， CODE GEN ，看是编译期间还是运行期间～
 */
public class Enumtest {
    enum SS{
        S1,
        S2,
    }


    public static void main(String[] args){
        SS t1 = null;
        boolean sss = t1 == S2;

        S2.equals(t1);
    }

}
