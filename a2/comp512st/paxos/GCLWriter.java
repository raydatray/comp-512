package paxos;

import java.util.logging.Logger;
import java.util.List;

import comp512.gcl.GCL;

public class GCLWriter {
    private GCL gcl;
    private Logger logger;

    GCLWriter(GCL gcl, Logger logger) {
        this.gcl = gcl;
        this.logger = logger;
    }

    void send(String destProcess, PaxosMessage msg) {
        logger.fine("sending message " + msg + " to " + destProcess);
        gcl.sendMsg(msg, destProcess);
    }

    void multicast(List<String> processes, PaxosMessage msg) {
        logger.fine("multicasting message " + msg + " to " + processes);
        gcl.multicastMsg(msg, processes.toArray(new String[0]));
    }

    void broadcast(PaxosMessage msg) {
        logger.fine("broadcasting message " + msg + " to everyone else");
        gcl.broadcastMsg(msg);
    }
}
