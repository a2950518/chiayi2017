# -*- coding: utf8 -*-

import socket
import sys
from urllib import unquote
import json
import os


HOST = ''   # Symbolic name, meaning all available interfaces
PORT = 8000  # Arbitrary non-privileged port

socket_server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
socket_server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
print 'Socket created'


fo = open("road.json", "rw")
line = fo.read()
#print line
str_json = json.loads(line,encoding='big5')
now_num = 0
for a in str_json:
    #print a
    #print str_json[a]
    now_num = now_num + 1


#print(type(str_json))
#print(str_json)

# Bind socket to local host and port
try:
    socket_server.bind((HOST, PORT))
except socket.error as msg:
    print 'Bind failed. Error Code : ' + str(msg[0]) + ' Message ' + msg[1]
    sys.exit()

#print 'Socket bind complete'

# Start listening on socket
socket_server.listen(10)
#print 'Socket now listening'

# now keep talking with the client
while 1:

    conn, addr = socket_server.accept()
    conn.setblocking(False) 

    data = ''
    response = 'HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n'
    while True:
        if '\r\n\r\n' in data: 
            break
        try:
            data += conn.recv(1024)
        except:
            pass


    s = data.find('8000/')
    s_1 = data.find('&end')

    str_put = unquote(data[s+7:s_1])
    str_spt = str_put.split("&")

    list_req = dict()
    for a in str_spt:
        num = a.find('=')
	list_req[a[0:num]] = a[num+1:len(a)]
    now_num = now_num + 1
    str_json[now_num] = list_req

    #print "yaya"
    print_str = json.dumps(str_json,encoding='UTF-8',ensure_ascii = False,indent=1)
    #print type(print_str)
    print print_str
    print '=================================================================='

    conn.sendall(response+'''<h1>Hi</h1>''')
    conn.close()  


socket_server.close()


fo.close()
