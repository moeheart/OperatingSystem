#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

#define BUFSIZE 1024

char buf[BUFSIZE];

int main(int argc, char** argv)
{
  int f1 = creat("aae.txt");
  int f2 = creat("aaf.txt");
  close(f1);
  unlink("aae.txt");
  int f3 = creat("aag.txt");
  unlink("aaf.txt");
  close(f2);
  close(f3);
  halt();
  return 0;
}
