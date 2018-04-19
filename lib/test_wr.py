import sys
import requests
import unittest
import argparse
import json

class TestServer(unittest.TestCase):

    def __init__(self, testname, test_no, address):
        super(TestServer, self).__init__(testname)
        self.test_no = test_no
        self.address = address
        self.events = ["Distributed Software Development", "System Foundations", "Principles of Software Developments"]
        self.user_id = 2294
        self.create_data = {"userid":self.user_id, "eventname":self.events[test_no], "numtickets":20}
        self.purchase_data = {"tickets":2}

    def test_create(self):
        url = "http://" + self.address + "/events/create"
        r = requests.post(url, json=self.create_data)
        self.assertEqual(r.status_code, 200)
        print("sent to " + url)
        print("request body:")
        print(json.dumps(self.create_data, indent=4, sort_keys=True))
        print("response body:")
        print(json.dumps(r.json(), indent=4, sort_keys=True))
        print("------------------------------------------------------")

    def test_purchase(self):
        url = "http://" + self.address + "/events/0/purchase/" + str(self.user_id)
        r = requests.post(url, json=self.purchase_data)
        self.assertEqual(r.status_code, 200)
        print("sent to " + url)
        print("request body:")
        print(json.dumps(self.purchase_data, indent=4, sort_keys=True))
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
    if len(sys.argv) < 3:
        print ("usage: python3 test_wr.py <choose 0~2 for eventname> <address>")
        sys.exit()

    test_no = sys.argv[1]
    address = sys.argv[2]
    suite = unittest.TestSuite()

    # write test including one create, one purchase, and two reads
    suite.addTest(TestServer("test_create", test_no, address))
    suite.addTest(TestServer("test_event_list", test_no, address))
    suite.addTest(TestServer("test_purchase", test_no, address))
    suite.addTest(TestServer("test_event_list", test_no, address))
    
    print("------------------------------------------------------")
    unittest.TextTestRunner().run(suite)

