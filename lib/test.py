import sys
import requests
import unittest
import argparse
import json

class TestServer(unittest.TestCase):

    def __init__(self, testname, no):
        super(TestServer, self).__init__(testname)
        self.no = no
        self.address = ["localhost:4560", "localhost:4561", "localhost:4597", "localhost:4598", "localhost:4599"]
        self.events = ["Distributed Software Development", "System Foundations", "Principles of Software Developments"]
        self.user_id = 2294
        self.create_data = {"userid":self.user_id, "eventname":self.events[no % 2], "numtickets":20}
        self.purchase_data = {"tickets":2}

    def test_create(self):
        url = "http://" + self.address[self.no] + "/events/create"
        r = requests.post(url, json=self.create_data)
        self.assertEqual(r.status_code, 200)
        print("sent to " + url)
        print("request body:")
        print(json.dumps(self.create_data, indent=4, sort_keys=True))
        print("response body:")
        print(json.dumps(r.json(), indent=4, sort_keys=True))
        print("------------------------------------------------------")

    def test_purchase(self):
        url = "http://" + self.address[self.no] + "/events/" + str(self.no) + "/purchase/" + str(self.user_id)
        r = requests.post(url, json=self.purchase_data)
        self.assertEqual(r.status_code, 200)
        print("sent to " + url)
        print("request body:")
        print(json.dumps(self.purchase_data, indent=4, sort_keys=True))
        print("------------------------------------------------------")

    def test_event_list(self):
        url = "http://" + self.address[self.no] + "/events"
        r = requests.get(url)
        self.assertEqual(r.status_code, 200)
        print("sent to " + url)
        print("response body:")
        print(json.dumps(r.json(), indent=4, sort_keys=True))
        print("------------------------------------------------------")

    def test_event_list_on_backend(self):
        url = "http://" + self.address[self.no] + "/list"
        r = requests.get(url)
        self.assertEqual(r.status_code, 200)
        print("sent to " + url)
        print("response body:")
        print(json.dumps(r.json(), indent=4, sort_keys=True))
        print("------------------------------------------------------")

    def test_different_version_of_secondary(self):
        url = "http://" + self.address[self.no] + "/events/create"
        dv_data = {"userid":self.user_id, "eventname":self.events[2], "numtickets":20, "demo":True}
        r = requests.post(url, json=dv_data)
        self.assertEqual(r.status_code, 200)
        print("sent to " + url)
        print("request body:")
        print(json.dumps(dv_data, indent=4, sort_keys=True))
        print("response body:")
        print(json.dumps(r.json(), indent=4, sort_keys=True))
        print("------------------------------------------------------")

args = None

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print ("usage: python3 test.py <no_of_test>")
        sys.exit()

    no = sys.argv[1]

    suite = unittest.TestSuite()
    if no == '1':
        suite.addTest(TestServer("test_create", 0))
        suite.addTest(TestServer("test_event_list", 0))
        suite.addTest(TestServer("test_purchase", 0))
        suite.addTest(TestServer("test_event_list", 0))

    if no == '2':
        suite.addTest(TestServer("test_create", 1))
        suite.addTest(TestServer("test_event_list", 1))
        suite.addTest(TestServer("test_purchase", 1))
        suite.addTest(TestServer("test_event_list", 1))

    if no == '3':
        suite.addTest(TestServer("test_create", 0))
        suite.addTest(TestServer("test_event_list_on_backend", 2))

    if no == '4':
        suite.addTest(TestServer("test_different_version_of_secondary", 1))
        suite.addTest(TestServer("test_event_list_on_backend", 2))
        suite.addTest(TestServer("test_event_list_on_backend", 3))

    
    print("------------------------------------------------------")
    unittest.TextTestRunner().run(suite)

