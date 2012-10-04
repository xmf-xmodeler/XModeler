#define byte1(word) word & BYTE1
#define byte2(word) (word & BYTE2) >>> 8
#define byte3(word) (word & BYTE3) >>> 16
#define byte4(word)	(word & BYTE4) >>> 24

#define set(ptr,value) words[ptr] = value
#define ref(ptr) words[ptr]
#define tag(word) (word & BYTE4) >>> 24
#define ptr(word) word & PTR
#define value(word) word & DATA
#define mkImmediate(tag,value) (tag << 24) | (value & DATA)
#define mkPtr(tag,value) (tag << 24) | (value & DATA)
