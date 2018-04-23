import sys
import requests
import unittest
import argparse
import json

class TestServer(unittest.TestCase):

    def __init__(self, testname, address):
        super(TestServer, self).__init__(testname)
        self.address = address

    def test_different_version_of_secondary(self):
        url = "http://" + self.address + "/events/create"
        create_data = {"userid":2294, "eventname":"Big Data", "numtickets":20, "demo":True}
        r = requests.post(url, json=create_data)
        self.assertEqual(r.status_code, 200)
        print("sent to " + url)
        print("request body:")
        print(json.dumps(create_data, indent=4, sort_keys=True))
        print("response body:")
        print(json.dumps(r.json(), indent=4, sort_keys=True))
        print("------------------------------------------------------")

    def test_event_list(self):
        url = "http://" + self.address + "/events"
        r = requests.get(url)
        self.assertEqual(r.status_code, 200)
        print("sent to " + url)
        print("response body:")
        print(json.dumps(r.json(), indent=4, sort_keys=True))
        print("------------------------------------------------------")

args = None

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print ("usage: python3 test_diff_ver.py <address>")
        sys.exit()

    address = sys.argv[1]
    suite = unittest.TestSuite()

    # test different versions of secondary nodes to check election and consistency
    suite.addTest(TestServer("test_different_version_of_secondary", address))
    suite.addTest(TestServer("test_event_list", address))

    print("------------------------------------------------------")
    unittest.TextTestRunner().run(suite)

