import sys
read =  sys.argv[1]
write =  sys.argv[2]

r =  open(read,'r')

w= open(write,"w+")
line = r.readline()

while line!="":
    w.write("<item> "+line.strip()+" </item>\n")
    line = r.readline()
