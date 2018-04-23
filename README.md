# Strong Consistency Event Purchase RESTful API Service

## Introduction

This is an event create/purchase RESTful API Service. Client can create user, create event, purchase tickets, and transfer tickets. Focus on the replication features of the event service which is a replicated, fault tolerant service.

## Overview

The architecture of the service looks like this:

![Project4 Architecture](https://i.imgur.com/g14jmLB.jpg)

## Design and Demo

The design of my service and the demonstration of the testing framework are recorded here: [Youtube](https://youtu.be/fq_06zrdYSk?t=17m53s)

## Features

#### Fault Tolerance

In a system with N data storage servers, you will tolerate the failure of up to N-1 nodes. As long as one data storage server is available, a client request will succeed.

#### Read-My-Writes Consistency

A front end will not receive data older than it has seen before if fresher data is available. If, for example, a client posts a message and then performs a read, the response the client receives must include the most recent messages unless all data storage servers storing the newest data have failed.

#### Membership

The event services will maintain the membership of all the services, including: primary/secondary event service, primary user service, and front end service. Event services will gossip with each other to get the service list, and to add itself to each other's list. Front end services will greet with the primary service to add itself to primary's list. The primary event/user service will be configured when the service starts. Once event service detcets a service is unreachable, it will remove it from its list.

#### Bully Election

During the gossip between secondaries, start an election when detecting primary is down. Send GET request to other event services with higher rank address. If no reply, send POST request to announce there is a new primary. If received a response, it means that there is an outstanding service, wait for announcement. Each node will only send one election request in one round of the election, unless it timeout to wait for the announcement.

#### Service States

The event service has three states: primary, secondary, and candidate. The service will start with primary or secondary state by configuration. A secondary will trun into candidate state when it detects the primary is down or when it receives a election request. A candidate will turn into primary if there is no outstanding node, and it will turn into secondary when receives an announcement.

#### Committed Log

A synchronized data structure to maintain the logs of committed request. It contains with Universally Unique IDentifier, Lamport Timestamps, and the committed data. The purpose is to avoid duplicate data, and to maintain the order of replications.

#### Replication

When a front end service receives a write request, it will assign the request with an uuid and pass it to the primary event service. The primary event service will start the write operation, and right after it finished, it will assign the request with a Lamport Timestamp, commit to log, and pass it to the secondary event service. If the primary fails during replication, the front end will hold the request and retry it when a new primary comes up. If a new primary has already committed the write with the same uuid, it will ignore it and pass it with the timestamp it committed to the secondary event service. If a secondary receives a write request with the uuid it already committed, it will match with its timestamp. If the uuid and the timestamp don't match, it will request a full copy from the primary to overwrite the data. Full backup from primary will only happen when new secondary comes up or the above situation.

## API

### Front End Service

<details>
<summary>GET /events</summary>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>Event Details<br/>
<pre>
[
	{
		"eventid": 0, 
		"eventname": "string", 
		"userid": 0,		
		"avail": 0, 
		"purchased": 0
	} 
]
	</pre></td></tr>
	<tr><td>400</td><td>No events found</td></tr>
</table>
</details>


<details>
<summary>POST /events/create </summary>
	
Body:

<pre>
{
	"userid": 0,
	"eventname": "string",
	"numtickets": 0
}
</pre>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>Event created
<pre>
{
	"eventid": 0
}	
</pre></td></tr>
	<tr><td>400</td><td>Event unsuccessfully created</td></tr>
</table>
</details>

<details>
<summary>GET /events/{eventid}</summary>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>Event Details<br/>
<pre>
{
	"eventid": 0, 
	"eventname": "string", 
	"userid": 0,		
	"avail": 0, 
	"purchased": 0
}
</pre></td></tr>
	<tr><td>400</td><td>Event not found</td></tr>
</table>
</details>

<details>
<summary>POST /events/{eventid}/purchase/{userid}</summary>
Body:

<pre>
{
	"tickets": 0
}
</pre>


Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>Tickets purchased</td></tr>
	<tr><td>400</td><td>Tickets could not be purchased</td></tr>
</table>
</details>

<details>
<summary>POST /users/create</summary>

Body:

<pre>
{
	"username": "string"
}
</pre>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>User created<br/>
<pre>
{
	"userid": 0
}	
</pre></td></tr>
	<tr><td>400</td><td>User could not be created</td></tr>
</table>
</details>

<details>
<summary>GET /users/{userid}</summary>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>User Details<br/>
<pre>
{
	"userid": 0,
	"username": "string",
	"tickets": [
		{
			"eventid": 0, 
			"eventname": "string", 
			"userid": 0,		
			"avail": 0, 
			"purchased": 0
		}
	]	
}
</pre></td></tr>
	<tr><td>400</td><td>User not found</td></tr>
</table>
</details>

<details>
<summary>POST /users/{userid}/tickets/transfer</summary>

Body:
<pre>
{
	"eventid": 0,
	"tickets": 0,
	"targetuser": 0
}
</pre>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>Event tickets transferred</td></tr>
	<tr><td>400</td><td>Tickets could not be transferred</td></tr>
</table>

</details>

<details>
<summary>GET /greet</summary>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>Front End Service is running</td></tr>
</table>

</details>

<details>
<summary>POST /election</summary>

Body:
<pre>
{
	"port": 0
}
</pre>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>New primary event service has been configured</td></tr>
</table>

</details>


### Event Service

<details>
<summary>POST /create</summary>

Body:

<pre>
{
	"userid": 0,
	"eventname": "string",
	"numtickets": 0
}
</pre>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>Event created
<pre>
{
	"eventid": 0
}	
</pre></td></tr>
	<tr><td>400</td><td>Event unsuccessfully created</td></tr>

</table>
</details>

<details>
<summary>GET /list</summary>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>List of events <br/>
<pre>
[
	{
		"eventid": 0, 
		"eventname": "string", 
		"userid": 0,		
		"avail": 0, 
		"purchased": 0
	}
]	
</pre>
	</td></tr>
</table>
</details>

<details>
<summary>GET /{eventid}</summary>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>Event details<br/>
<pre>
{
	"eventid": 0, 
	"eventname": "string", 
	"userid": 0,		
	"avail": 0, 
	"purchased": 0
}
</pre>
	</tr>
	<tr><td>400</td><td>Event not found</tr>
</table>
</details>

<details>
<summary>POST /purchase/{eventid}</summary>

Body:

<pre>
{
	"userid": 0,
	"eventid": 0,
	"tickets": 0
}
</pre>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>Event tickets purchased</tr>
	<tr><td>400</td><td>Tickets could not be purchased</tr>
</table>

</details>

<details>
<summary>POST /greet/event</summary>

Body:

<pre>
{
	"port": 0,
}
</pre>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>Service list<br/>
<pre>
[
	{
		"service": "event",
		"address": "10.0.1.9:4599",
		"primary": true
	},
	{
		"service": "frontend",
		"address": "10.0.1.5:4560",
		"primary": false
	}
]
</pre>
	</tr>
	<tr><td>400</td><td>Service unreachable</tr>
</table>

</details>

<details>
<summary>POST /greet/frontend</summary>

Body:

<pre>
{
	"port": 0,
}
</pre>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>Service list<br/>
<pre>
[
	{
		"service": "event",
		"address": "10.0.1.9:4599",
		"primary": true
	},
	{
		"service": "frontend",
		"address": "10.0.1.5:4560",
		"primary": false
	}
]
</pre>
	</tr>
	<tr><td>400</td><td>Service unreachable</tr>
</table>

</details>

<details>
<summary>GET /election</summary>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>A service with higher rank is running</tr>
</table>

</details>

<details>
<summary>POST /election</summary>

Body:

<pre>
{
	"port": 0,
}
</pre>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>New primary event service has been configured</tr>
	<tr><td>400</td><td>Service unreachable</tr>
</table>

</details>


### User Service

<details>
<summary>POST /create</summary>

Body:

<pre>
{
	"username": "string"
}
</pre>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>User created<br/>
<pre>
{
	"userid": 0
}	
</pre>
</tr>
<tr><td>400</td><td>User unsuccessfully created</tr>
</table>
</details>

<details>
<summary>GET /{userid}</summary>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>User details<br/>
<pre>
{
	"userid": 0,
	"username": "string",
	"tickets": [
		{
			"eventid": 0
		}
	]
}
</pre>
</tr>
	<tr><td>400</td><td>User not found</tr>
</table>
</details>

<details>
<summary>POST /{userid}/tickets/add</summary>

Body:

<pre>
{
	"eventid": 0,
	"tickets": 0
}
</pre>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>Event tickets added</tr>
	<tr><td>400</td><td>Tickets could not be added</tr>

</table>
</details>

<details>
<summary>POST /{userid}/tickets/transfer</summary>

Body:

<pre>
{
	"eventid": 0,
	"tickets": 0,
	"targetuser": 0
}
</pre>

Responses:

<table>
	<tr><td>Code</td><td>Description</td></tr>
	<tr><td>200</td><td>Event tickets transfered</tr>
	<tr><td>400</td><td>Tickets could not be transfered</tr>
</table>

</details>


## Program and testing framework configuration

<details>
<summary>Start Event Service (Primary)</summary>

```
$ java -cp project4.jar EventService.EventServiceDriver -port <port> -primaryEvent this - primaryUser <address_of_primary_user>
```

</details>

<details>
<summary>Start Event Service (Secondary)</summary>

```
$ java -cp project4.jar EventService.EventServiceDriver -port <port> -primaryEvent <address_of_primary_event> - primaryUser <address_of_primary_user>
```

</details>

<details>
<summary>Start Front End Service</summary>

```
$ java -cp project4.jar FrontEndService.FrontEndDriver -port <port> -primaryEvent <address_of_primary_event> - primaryUser <address_of_primary_user>
```

</details>

<details>
<summary>Test for write and read</summary>

```
$ python test_wr.py <choose_0_to_2_for_different_eventname> <address_of_front_end>
```

</details>

<details>
<summary>Read directly from backend</summary>

```
$ python3 test_read_backend.py <address_of_event_service>
```

</details>

<details>
<summary>Test for election and replication of secondaries with different version data</summary>

```
$ python3 test_diff_ver.py <address_of_front_end>
```

</details>

</details>

<details>
<summary>Test for concurrent writes</summary>

```
$ java -cp project4.jar Usage.ConcurrentTest <0_for_create_1_for_purchase> <address_of_front_end> <times_of_test>
```

</details>


## References
* [University of San Francisco](https://www.usfca.edu/)
* [Gson](https://mvnrepository.com/artifact/com.google.code.gson/gson/2.8.2)
* [Jetty-all](https://mvnrepository.com/artifact/org.eclipse.jetty.aggregate/jetty-all/9.4.9.v20180320)
* [Maven](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)
* [Imgur](https://imgur.com/)

## Acknowledgment

This is a course project, not using for any commercial purpose.

## Author and contributors

* **Brian Sung** - *Graduate student in department of Computer Science at University of San Francisco* - [LinkedIn](https://www.linkedin.com/in/brianisadog/)
* **Dr. Rollins** - *Professor in department of Computer Science at University of San Francisco* - [page](http://srollins.cs.usfca.edu/)