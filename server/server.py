#!/usr/bin/env python
"""
Very simple HTTP server in python.
Usage::
    ./dummy-web-server.py [<port>]
Send a GET request::
    curl http://localhost
Send a HEAD request::
    curl -I http://localhost
Send a POST request::
    curl -d "foo=bar&bin=baz" http://localhost
"""
from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import SocketServer


venues_load = {}
template = open("index.html").read()


def render_html(content):
    cl = ""
    capacity = 2
    table = ""
    for key, value in content.items():
        if int(value) < capacity / 2:
            cl = "success"
        elif int(value) < capacity:
            cl = "warning"
        else:
            cl = "danger"

        table += """<tr class="{}">
        <td>{}</td>
        <td>{}</td>
        <td>{}</td>
    </tr>
    """.format(cl, key, value, capacity)
    return template.format(table)


class S(BaseHTTPRequestHandler):

    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def do_GET(self):
        self._set_headers()
        self.wfile.write(render_html(venues_load))

    def do_HEAD(self):
        self._set_headers()

    def do_POST(self):
        # Doesn't do anything with posted data
        self._set_headers()
        post_data = self.rfile.read(int(self.headers.getheader('content-length')))
        if post_data in venues_load:
            venues_load[post_data] += 1
        else:
            venues_load[post_data] = 1

    def do_PUT(self):
        self._set_headers()
        post_data = self.rfile.read(int(self.headers.getheader('content-length')))
        if post_data in venues_load:
            venues_load[post_data] -= 1


    def handle_method(self, method):
        if method == 'DELETE':
            self.do_DELETE()
        else:
            BaseHTTPRequestHandler.handle_method(method)
        


def run(server_class=HTTPServer, handler_class=S, port=80):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    print 'Starting httpd...'
    httpd.serve_forever()


if __name__ == "__main__":
    from sys import argv

    if len(argv) == 2:
        run(port=int(argv[1]))
    else:
        run()
