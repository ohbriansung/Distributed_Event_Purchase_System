import sys
import requests
import unittest
import argparse
import json

class TestServer(unittest.TestCase):

    def __init__(self, testname, address):
        super(TestServer, self).__init__(testname)
        self.address = address

    def test_event_list(self):
        url = "http://" + self.address + "/list"
        r = requests.get(url)
        self.assertEqual(r.status_code, 200)
        print("sent to " + url)
        print("response body:")
        print(json.dumps(r.json(), indent=4, sort_keys=True))
        print("------------------------------------------------------")

args = None

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print ("usage: python3 test_read_backend.py <address>")
        sys.exit()

    address = sys.argv[1]
    suite = unittest.TestSuite()

    # read directly from backend nodes
    suite.addTest(TestServer("test_event_list", address))
    
    print("------------------------------------------------------")
    unittest.TextTestRunner().run(suite)