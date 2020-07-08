package org.lkpnotice.infra.ttest;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Created by jpliu on 2020/7/8.
 */
public class Test {
    public static void main(String[] args) throws UnsupportedEncodingException {
        String t1 = "segments:rnopmr7ok.qoy2tt1z4.qs1z30ya3.q0i7144zx.qs15tr588.qryvv06az.qs1qhvlu3.s46zz25dp.qrd6wq2ga.q9g0tyjkf.qkb9th6k8.q8g5tmarj.qs57hbugl.sqi3eld2j.qrd60fqh1.r8qqby868.qrd7glbf2.q8hrhsyte.quonkizmy.su1gidpk9.qryvneueb.q3u6bd89c.q0jjyjhox.q59io3s65.qs1z6x0ai.q6t168up0.szkjuzu7f.qrd4az0wj.qrd6sllmg.qmyxz81rk.q5uqe1dnm.q7a0t26t5.stpzz6215.q59imzl1s.s46y6mod1.q8x5hurod.qk3gncicb.s4602r2ck.qbx3rc5w7.qly1j6jun.qrd44q8jo.q1it5b3w8.q1joheyrh.r183wt669.quonowxsm";
        System.out.println(t1.getBytes().length);
        System.out.println(t1.toCharArray().length);

        String t2 = "大王";

        System.out.println(Charset.defaultCharset().displayName());
        System.out.println(t2.getBytes().length);
        System.out.println(t2.getBytes("GBK").length);
        System.out.println(t2.toCharArray().length);


        System.out.println("---------------------------------------------------------------");
        System.out.println(t2);

        System.out.println(t2.getBytes());
        System.out.println(t2.getBytes("GBK").toString());
        System.out.println(new String(t2.getBytes()));
        System.out.println(new String(t2.getBytes(),"GBK"));

        t2.toCharArray();
        System.out.println(t2.getBytes().length);

        System.out.println("---------------------------------------------------------------");
        char t11='1';
        char t12 ='王';
        System.out.printf("ANSCII DIFF %d  \n", t12-t11);

        System.out.println("segments:rnopmr7ok.qoy2tt1z4.qs1z30ya3.rwy5p2ung.qrhhnjhwj.qs1qhvlu3.qq9dc8fhe.r0tj6on0n.sslf5wy4q.r0tsnx9dh.rnpgoxcbu.sslhft3vu.su1gidpk9.qgcy7lko6.qrhhozws3.q3u6bd89c.rnlpcyg2z.qrd5fxu9z.qs15r90sl.qgczrxlzb.qs1z6x0ai.rzef6s36f.ssl961hat.sg1flyeie.qrd596et4.q59imzl1s.qupnxsvc6.qly1j6jun.qybvdhawa.qs1o968au.quonowxsm.qvv9lw8l9.r0tjyiqbs.tbsmnuosx.q7c0zeh2q.qrhgd06ye.qrhivrwod.qrd6wq2ga.rb8jxsenn.qrhgo1xpg.q8hrhsyte.qrd4dfhcz.rzhple1m7.qrhfvjqit.quonihatf.qmyxz81rk.qrhjlid3w.qh4dzmxnc.qk3gncicb.qbx3rc5w7.rwy5wz3t4.qrd58l8if.rntvzq5xr.sslhkjh5e.s460wcfrs.qkb9th6k8.qrd660fuo.q8g5tmarj.s3awhc3bz.r8qqby868.rlg7boghs.qryvz6jnk.qryvneueb.qrd6dih2a.qi0jx5kau.qrhijv8wb.qrhhy6t9x.qrd6sllmg.qrhgix9op.rnlo9yrkh.q4ansdfaq.qqlmdcwhh.qrd64nqay.qlv0nqfat.qnfht9iq8.qs15tr588.r0st813ut.rp8uu9rbt.qryvv06az.rf7tm4pll.qrhhxknmw.rvrdkic2w.qrhhrvzzm.ssmagj058.qs57hbugl.q7c0xaxjs.qrd60fqh1.qrhgz0l2f.s3av99wc2.q59io3s65.qrhg20kg4.qthxj5lev.qrd6815l7.rlg7mxwnl.q7a0t26t5.qrelbk5ro.s46y6mod1.rnptautxy.rnlo0wuk7.q498phwyw.s3axp1ppy".getBytes().length);
        System.out.println("segments:rnopmr7ok.qoy2tt1z4.qs1z30ya3.rwy5p2ung.qrhhnjhwj.qs1qhvlu3.qq9dc8fhe.r0tj6on0n.sslf5wy4q.r0tsnx9dh.rnpgoxcbu.sslhft3vu.su1gidpk9.qgcy7lko6.qrhhozws3.q3u6bd89c.rnlpcyg2z.qrd5fxu9z.qs15r90sl.qgczrxlzb.qs1z6x0ai.rzef6s36f.ssl961hat.sg1flyeie.qrd596et4.q59imzl1s.qupnxsvc6.qly1j6jun.qybvdhawa.qs1o968au.quonowxsm.qvv9lw8l9.r0tjyiqbs.tbsmnuosx.q7c0zeh2q.qrhgd06ye.qrhivrwod.qrd6wq2ga.rb8jxsenn.qrhgo1xpg.q8hrhsyte.qrd4dfhcz.rzhple1m7.qrhfvjqit.quonihatf.qmyxz81rk.qrhjlid3w.qh4dzmxnc.qk3gncicb.qbx3rc5w7.rwy5wz3t4.qrd58l8if.rntvzq5xr.sslhkjh5e.s460wcfrs.qkb9th6k8.qrd660fuo.q8g5tmarj.s3awhc3bz.r8qqby868.rlg7boghs.qryvz6jnk.qryvneueb.qrd6dih2a.qi0jx5kau.qrhijv8wb.qrhhy6t9x.qrd6sllmg.qrhgix9op.rnlo9yrkh.q4ansdfaq.qqlmdcwhh.qrd64nqay.qlv0nqfat.qnfht9iq8.qs15tr588.r0st813ut.rp8uu9rbt.qryvv06az.rf7tm4pll.qrhhxknmw.rvrdkic2w.qrhhrvzzm.ssmagj058.qs57hbugl.q7c0xaxjs.qrd60fqh1.qrhgz0l2f.s3av99wc2.q59io3s65.qrhg20kg4.qthxj5lev.qrd6815l7.rlg7mxwnl.q7a0t26t5.qrelbk5ro.s46y6mod1.rnptautxy.rnlo0wuk7.q498phwyw.s3axp1ppy".toCharArray().length);

    }
}
