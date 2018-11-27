package com.trj.sqlite.sqlitesample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.stetho.Stetho;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Stetho.initializeWithDefaults(this);
        init();
    }



    private View nni , nsi , simr , mr ,sinmr;

    void init(){
        //多线程写， 每个线程使用各自的SQLiteOpenHelper。可能会抛出异常，导致插入错误
        nni = findViewById(R.id.nni);
        nni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<InsertThread> list = new ArrayList<InsertThread>();
                for(int i = 0 ; i < 100 ; ++i){
                    list.add(new InsertThread(MainActivity.this  , new DbHelper(MainActivity.this) , 50));
                }
                startThreads(list);
                waitAndClose(list);
            }
        });

        //多线程写， 使用同一个SQLiteOpenHelper。不会出问题
        nsi = findViewById(R.id.nsi);
        nsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DbHelper db = new DbHelper(MainActivity.this);
                List<InsertThread> list = new ArrayList<InsertThread>();
                for(int i = 0 ; i < 1 ; ++i){
                    list.add(new InsertThread(MainActivity.this  , db , 5000));
                }

                List<ReadThread> rlist = new ArrayList<ReadThread>();
                for(int i = 0 ; i < 100 ; ++i){
                    rlist.add(new ReadThread(MainActivity.this  , db , 50));
                }
//                startThreads(list);
//                waitAndClose(list);
//                startThreads(list);
                startThreads(rlist);
                waitAndClose(rlist);
//                waitAndClose(list);
            }
        });

        //1个线程写，多个线程读，每个读线程使用各自的SQLiteOpenHelper
        simr = findViewById(R.id.simr);
        simr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<InsertThread> list = new ArrayList<InsertThread>();
                list.add(new InsertThread(MainActivity.this  , new DbHelper(MainActivity.this) , 5000));

                List<ReadThread> rlist = new ArrayList<ReadThread>();
                for(int i = 0 ; i < 100 ; ++i){
                    rlist.add(new ReadThread(MainActivity.this  , new DbHelper(MainActivity.this) , 50));
                }

//                startThreads(list);
                startThreads(rlist);
                waitAndClose(rlist);
//                waitAndClose(list);
            }
        });


        //多个线程读，每个读线程使用各自的SQLiteOpenHelper
        mr = findViewById(R.id.mr);
        mr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ReadThread> rlist = new ArrayList<ReadThread>();
                for(int i = 0 ; i < 10 ; ++i){
                    rlist.add(new ReadThread(MainActivity.this  , new DbHelper(MainActivity.this) , 200));
                }
                startThreads(rlist);
                waitAndClose(rlist);
            }
        });

        //单线程写，多线程读，使用一个SQLiteOpenHelper，读线程使用只读sqlitedatebase
        sinmr = findViewById(R.id.sinmr);
        sinmr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DbHelper db = new DbHelper(MainActivity.this);
                List<InsertThread> list = new ArrayList<InsertThread>();
                list.add(new InsertThread(MainActivity.this  , db , 1000));

                List<OnlyReadThread> rlist = new ArrayList<OnlyReadThread>();
                for(int i = 0 ; i < 100 ; ++i){
                    rlist.add(new OnlyReadThread(MainActivity.this  ,db, 50));
                }

                startThreads(list);
                startThreads(rlist);
                for(Thread t : rlist){
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                waitAndClose(list);
            }
        });

    }



    static class InsertThread extends Thread{
        static AtomicInteger count = new AtomicInteger(0);
        public DbHelper db;
        private int insertCount;
        private static String toinsert=new String("考了多少分就哭了是否四季咖啡的发生的发生的飞水jkjhkjljkljkl;kl;kl;cxkl;v电费水电费水电费收到发生的飞水电费收到发生的飞水电费水电费的飞水电费收到发生的飞收到发生的飞kl;kl;sdffklsdfklsdkl;fsdkl;fkl;sdfkl;sdkl;fkl;sdfkl;sdfsdakl;fkl;sdkl;fasdkl;fkl;sdfkl;sdkl;第三方圣达菲圣达菲收到飞收到飞收到发生的飞阿斯蒂芬收到发生的发生的发送飞");
        InsertThread(Context context , DbHelper db , int insertCount){
            setName("InsertThread#"+count.getAndIncrement());
            this.insertCount = insertCount;
            this.db = db;
        }


        @Override
        public void run() {
            super.run();
            try{
                for(int i = 0 ; i < insertCount ; ++i){
                    db.insert(toinsert);
                }
            }finally{
            }
        }
    }


    static class ReadThread extends Thread{
        static AtomicInteger count = new AtomicInteger(0);
        public DbHelper db;
        private int readCount;

        ReadThread(Context context , DbHelper db , int readCount){
            setName("ReadThread#"+count.getAndIncrement());
            this.readCount = readCount;
            this.db = db;
        }

        @Override
        public void run() {
            super.run();
            for(int i = 0 ; i < readCount ; ++i){
                db.getCount();
            }

        }
    }

    static class OnlyReadThread extends Thread{
        static AtomicInteger count = new AtomicInteger(0);
        public DbHelper db;
        private int readCount;
        private SQLiteDatabase sqldb;

        OnlyReadThread(Context context , DbHelper db , int readCount){
            setName("onlyReadThread#"+count.getAndIncrement());
            this.readCount = readCount;
            this.db = db;
            sqldb = db.getOnlyReadDatabase();
        }

        @Override
        public void run() {
            super.run();
            for(int i = 0 ; i < readCount ; ++i){
                db.getCount(sqldb);
            }

        }
    }

    void startThreads(Collection<? extends Thread> ts ){
        for(Thread t : ts){
            t.start();
        }
    }

    public static final String TAG = "DbHelper";
    void log(String dec){
        Log.i(TAG, dec + ". current thread="+Thread.currentThread().getName());
    }

    void waitAndClose(Collection<? extends Thread> ts ){
        for(Thread t : ts){
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for(Thread t : ts){
            if(t instanceof InsertThread){
                ((InsertThread)t).db.close();
            }else if(t instanceof ReadThread){
                ((ReadThread)t).db.close();
            }
        }
        log("finish!");
    }

}
