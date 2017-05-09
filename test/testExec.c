#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

#define BUFSIZE 1024

char buf[BUFSIZE];

int main(int argc, char** argv)
{
  int x = 0;
  exec("testCreate.coff",1,argv);
  x = x+1;
  return 0;
}
