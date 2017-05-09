#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

#define BUFSIZE 1024

char buf[BUFSIZE];

int main(int argc, char** argv)
{
  int x = 0;
  int y;
  int z = exec("testExit2.coff",1,argv);
  x = x+1;
  join(z,&y);
  x=x+100;
  halt();
  return 0;
}
