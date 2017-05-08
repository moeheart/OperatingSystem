#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

#define BUFSIZE 1024

char buf[BUFSIZE];

int main(int argc, char** argv)
{
	
  open("aaa.txt");
  open("aab.txt");
  halt();
  return 0;
}
