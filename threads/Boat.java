package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;

   static Communicator reportor;

   static Lock lock;

   // It is obvious that adults can never return.
   static Condition2 childLeft;
   static Condition2 childRight;
   static Condition2 adultLeft;
   static Condition2 inBoat;

   static Alarm alarm;

   static int restNumber;
   // Number on the left.
   static int childNumber;
   static int adultNumber;
   static int waitChildren;
   static int childCanBack;

   static boolean isAdult;
   static boolean pilotChosen;
   static boolean finish;

   static String boatSide;
    
    public static void selfTest()
    {
 BoatGrader b = new BoatGrader();
 
// System.out.println("\n ***Testing Boats with only 2 children***");
// begin(0, 2, b);

// System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//   begin(1, 2, b);

   System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
   begin(12, 22, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
  // Store the externally generated autograder in a class
  // variable to be accessible by children.
  bg = b;

  restNumber = adults + children;
  childNumber = 0;
  adultNumber = 0;
  waitChildren = 0;
  childCanBack = 0;

  pilotChosen = false;
  isAdult = false;
  finish = false;

  boatSide = "Left";

  lock = new Lock();

  childLeft = new Condition2(lock);
  childRight = new Condition2(lock);
  adultLeft = new Condition2(lock);
  inBoat = new Condition2(lock);

  reportor = new Communicator();

  // Instantiate global variables here
  
  // Create threads here. See section 3.4 of the Nachos for Java
  // Walkthrough linked from the projects page.

  Runnable adult_thread = new Runnable() {
            public void run() {
                AdultItinerary();
            }
        };

        Runnable child_thread = new Runnable() {
            public void run() {
                ChildItinerary();
            }
        };

  for (int i = 0; i < adults; i++) {

            KThread adult = new KThread(adult_thread);
            adult.setName("Adult: " + i);
            System.out.println("***Adult " + i + " created***");
            adult.fork();

        }

        for (int i = 0; i < children; i++) {
   
            KThread child = new KThread(child_thread);
            child.setName("Child: " + i);
            System.out.println("***Child " + i + " created***");
            child.fork();

        }

        while (restNumber != 0) {
   restNumber = reportor.listen();
        }

        finish = true;

    }

    /* the strategy of an adult is: First I sleep until a child wake me up. 
       The child makes sure I can go across. Then my task is finished. */
    static void AdultItinerary()
    {
  bg.initializeAdult(); 
  //Required for autograder interface. Must be the first thing called.
  //DO NOT PUT ANYTHING ABOVE THIS LINE. 

  lock.acquire();
  adultNumber += 1;

  // sleep first, and will be woke up when qualified.
  adultLeft.sleep();
  bg.AdultRowToMolokai();

  adultNumber -= 1;
  boatSide = "Right";
  // wake up a right child.
  childRight.wake();
  isAdult = false;

  lock.release();
  /* This is where you should put your solutions. Make calls
     to the BoatGrader to show that it is synchronized. For
     example:
         bg.AdultRowToMolokai();
     indicates that an adult has rowed the boat across to Molokai
  */
    }

    /* the strategy of a child is very complex: First I want to know if I can sleep: 
        1. boat is at the other side. 2. an adult will take it. Once I am woken up, one 
        of the following happens: the boat is on the left, or no adult will take it. Here
        I am not sure which side am I in, but it doesn't matter. I just need to consider
        whether an adult will take the boat. I can give adult a strict limitation, which 
        is children has priority, so that an adult can take the boat only when there is 
        exactly 1 child on the left. (However we need to make sure there are children on
        the right side) If it is true, I wake up an adult. Or we know that children will
        take this boat. When waiting children is less than 2, I need to wait for another
        child, so I sleep in boat and wait for waking. Once I am woken up, I will go across
        with another child.
        If the waiting children are more than 2, someone must be waiting in boat and I 
        need to wake him up. And we two go across. Then I check if it is finished. If not,
        I need to bow back.
    */
    static void ChildItinerary()
    {
  bg.initializeChild(); //Required for autograder interface. Must be the first thing called.
  //DO NOT PUT ANYTHING ABOVE THIS LINE. 

  lock.acquire();
  while (!finish) {

   // we can make sure the child is on the left here.
   childNumber += 1;

   // first consider if I can sleep
   while (boatSide.equals("Right") || isAdult) {
    childLeft.sleep();
   }

   // we need at least 2 child, so wake up another.
   childLeft.wake();


   waitChildren += 1;

   // first check the situation
   if (waitChildren < 2 || boatSide.equals("Right")) {
    if (childNumber == 1 && childCanBack > 0) {
    // strict limitation for adult taking the boat
     adultLeft.wake();
     isAdult = true;
    }
    // waitChildren < 2, so I sleep in boat and wait.
    inBoat.sleep();
    // woken up, I go across.
    bg.ChildRideToMolokai();
    // uodate counter.
    childNumber -= 2;
    childCanBack += 2;
    waitChildren -= 1;
   }

   // waitChildren > 2 && boatSide.equals("Left"), wake up the child in boat
   inBoat.wake();

   if (!pilotChosen) {
    pilotChosen = true;
    waitChildren -= 1;
    bg.ChildRowToMolokai();
    boatSide = "Right";
    // sleep on rightside, finish or bow back, just wait.
    childRight.sleep();
   }
   else
    if (childNumber + adultNumber == 0) {
     //finish flag
     reportor.speak(0);
    }
   if (!finish) {
    // not finish, I must be called to bow back
    pilotChosen = false;
    bg.ChildRowToOahu();
    boatSide = "Left";
    childCanBack -= 1;

   }
  }
  //finish, wake up all
  childRight.wakeAll();
  lock.release();
    }
    
}