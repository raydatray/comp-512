package paxos;

import java.io.Serializable;

record GameMove(Integer pNum, Character m) implements Serializable {}
