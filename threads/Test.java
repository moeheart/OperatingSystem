package nachos.threads;

import nachos.machine.*;
import nachos.ag.BoatGrader;

public class Test {
	Test(){};
	public static void testAll(Alarm alarm){
		testP1();
		testP2();
		testP3(alarm);
		testP4();
		//testP4Cplx();
		testP6();
	}
	
	//Test P1
	private static KThread th1 = null;
	public static void testP1(){
		Lib.debug('1', "Enter KThread.selfTest");
		System.out.println("Test P1 start...");
		th1 = new KThread(new ThreadP1(null, 1));
		th1.setName("forked thread").fork();
		new KThread(new ThreadP1(th1, 2)).setName("forked thread2").fork();
		new ThreadP1(th1, 0).run();
		System.out.println("Test P1 complete!");
	}
	private static class ThreadP1 implements Runnable {
		ThreadP1(KThread th1, int which) {
			this.which = which;
			this.th1 = th1;
		}
		public void run() {
			for (int i=0; i<5; i++) {
				if (i==2 && which == 0) {
					th1.join();
				}
				System.out.println("P1*** thread " + which + " looped "
						   + i + " times");
				KThread.yieldCurrent();
			}
		}
		private int which;
		public KThread th1 = null;
    }
	
	//Test P2
	private static Lock l = new Lock();
	private static Condition2 cond = new Condition2(l);
	public static void testP2() {
		Lib.debug('2', "Enter Condition2.selfTest");
		System.out.println("Test P2 start...");
		new KThread(new ThreadP2(cond, 1)).setName("forked thread").fork();
		new KThread(new ThreadP2(cond, 2)).setName("forked thread2").fork();
		new ThreadP2(cond, 0).run();
		System.out.println("Test P2 complete!");
    }
	
	public static class ThreadP2 implements Runnable {
		ThreadP2(Condition2 cond, int which) {
			this.which = which;
			this.cond = cond;
		}
		public void run() {
			int[][] a = new int[][]{{0, 0, 1, 0, 0, 0, 0, 0, 3, 0} ,{0, 0, 0, 0, 2, 0, 0, 1, 0, 0},{0, 0, 0, 0, 0, 0, 1, 0, 0, 0}};
			for (int i=0; i<10; i++) {
				if (a[which][i] == 1) {
					l.acquire();
					cond.sleep();
					l.release();
				}
				else if (a[which][i] == 2) {
					l.acquire();
					cond.wake();
					l.release();
				}
				else if (a[which][i] == 3){
					l.acquire();
					cond.wakeAll();
					l.release();
				}
				System.out.println("P2*** thread " + which + " looped "
						   + i + " times");
				KThread.yieldCurrent();
			}
		}
		private int which;
		private Condition2 cond = null;
	}
	
	//Test P3
	public static void testP3(Alarm a) {
		Lib.debug('3', "Enter Alarm.selfTest");
		System.out.println("Test P3 start...");
		new KThread(new ThreadP3(a,1)).setName("forked thread").fork();
		new KThread(new ThreadP3(a,2)).setName("forked thread").fork();
		new ThreadP3(a,0).run();
		System.out.println("Test P3 complete!");
    }
	private static class ThreadP3 implements Runnable {
        ThreadP3(Alarm a, int which) {
            this.which = which;
			this.a = a;
        }
        public void run() {
            for (int i = 0; i < 5; i++) {
				if (i == 1 && which == 2)
					a.waitUntil(500);
				if (i == 3 && which == 1)
					a.waitUntil(12000000);
				if (i == 4 && which == 0)
					a.waitUntil(20000000);
                System.out.println("P3*** thread " + which + " looped "
                    + i + " times");
                KThread.yieldCurrent();
            }
        }
        private int which;
		private Alarm a;
    }
	
	//Test P4
	private static Communicator c = new Communicator();
	public static void testP4() {
		Lib.debug('4', "Enter Alarm.selfTest");
		System.out.println("Test P4 start...");
		new KThread(new ThreadP4(c,1)).setName("forked thread").fork();
		new KThread(new ThreadP4(c,2)).setName("forked thread2").fork();
		new ThreadP4(c,0).run();
		System.out.println("Test P4 complete!");
    }
	public static class ThreadP4 implements Runnable {
		ThreadP4(Communicator c, int which) {
			this.which = which;
			this.c = c;
		}
		public void run() {
			int[][] a = new int[][]{{0, 0, 1, 0, 0, 2, 0, 0, 0, 0} ,{0, 0, 0, 0, 2, 0, 2, 0, 0, 0},{0, 0, 0, 0, 0, 0, 0, 0, 1, 1}};
			int[]b = new int[]{0, 0, 0, 0, 233, 666, 1001, 0, 0, 0};
			for (int i=0; i<10; i++) {
				if (a[which][i] == 1) {
					System.out.println("Thread " + which + " ready to listen...");
					int res = c.listen();
					System.out.println("Thread " + which + " listened result: " + res);
				}
				if (a[which][i] == 2) {
					int text = b[i];
					System.out.println("Thread " + which + " ready to speak" + text);
					c.speak(text);
					System.out.println("Thread " + which + " speak complete!");
				}
				System.out.println("P4*** thread " + which + " looped "
						   + i + " times");
				KThread.yieldCurrent();
			}
		}
		private int which;
		private Communicator c = null;
    }
	
	//Test P4 - Complex Mode
	public static void testP4Cplx() {
		Lib.debug('4', "Enter Alarm.selfTest2");
		System.out.println("Test P4 (complex mode) start...");
		for (int i = 1; i < 10; i++) {
			new KThread(new ThreadP4Cplx(c,i)).setName("forked thread"+i).fork();
		}
		new ThreadP4Cplx(c,0).run();
		System.out.println("Test P4 (complex mode) complete!");
    }
	public static class ThreadP4Cplx implements Runnable {
		ThreadP4Cplx(Communicator c, int which) {
			this.which = which;
			this.c = c;
		}
		public void run() {
			java.util.Random r = new java.util.Random(); 
			for (int i=0; i<100; i++) {
				int random = r.nextInt(100);
				if ((i < 90 && random < 5 - listeners) || (i >= 90 && listeners < 0)) {
					System.out.println("Thread " + which + " ready to listen..." + " at loop "+i);
					listeners++;
					int res = c.listen();
					System.out.println("Thread " + which + " listened result: " + res + " at loop "+i);
				}
				else if ((i < 90 && random >= 95 - listeners) || (i >= 90 && listeners > 0)) {
					int text = random = r.nextInt(1000);;
					System.out.println("Thread " + which + " ready to speak " + text + " at loop "+i);
					listeners--;
					c.speak(text);
					System.out.println("Thread " + which + " speak complete!" + " at loop "+i);
				}
				KThread.yieldCurrent();
			}
		}
		private int which;
		private Communicator c = null;
		static private int listeners = 0;
    }
	
	//Test P6
	public static void testP6() {
		BoatGrader b = new BoatGrader();
		System.out.println("Test P6 start...");
		System.out.println("P6***Testing Boats with 11 children, 5 adults***");
		Boat.begin(5, 11, b);
		System.out.println("Test P6 complete!");
    }
}
