# Akka IO - UDP

Just messing around with Akka 2.2's IO module.

### Server 
If verbose is true it logs all incoming messages to console else it echos them back to the sender.

### Client
Sends messages to specified port(on localhost) to see magic happen. (and logs responses)

### Benchmark
Sends a lot of messages and counts responses. Occasionally prints out some statistics like throuput. 
Could use some improvements.
Currently it does about 60k roundtrips per second(client-server-client) on my machine(laptop), peaking at 100k sometimes for short bursts.

### Counter
Set a limit. It will print out some stats every limit received messages. About 100k received per second on localhost.

### BatchSend
port n msg
Fire and forget. Use with Counter to measure one-way throughput.
