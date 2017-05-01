import sys

f1 = open(sys.argv[1]).read().splitlines()
f2 = open(sys.argv[2]).read().splitlines()
print len([1 for a, b in zip(f1, f2) if a == b]) * 1.0 / len(f1)

