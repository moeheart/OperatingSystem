#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

#define BUFSIZE 1024

char buf[BUFSIZE];

int main(int argc, char** argv)
{

  int inf = open("aac.txt");
  int ouf = creat("aad.txt");
  unsigned char c;
  read(inf, &c, 1);
  c = c + 1;
  write(ouf, &c, 1);
  halt();
  return 0;
}
