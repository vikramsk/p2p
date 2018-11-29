# __p2p__
A p2p file sharing tool

### __Authors__

* Vikram Sreekumar - 5108 8960
* Shreenivas Pai N - 7823 6469
* Akash R Vasishta - 5395 5080

### __Remote Peer Launch__

## SSH Setup

You need to setup SSH keys with the nodes in order to execute the process remotely without having to enter the password.
This can be done using the following steps:

```sh
ssh-keygen -t rsa
ssh <HostName> mkdir -p $HOME/.ssh
cat $HOME/.ssh/id_rsa.pub | ssh b@B 'cat >> $HOME/.ssh/authorized_keys'
```

Once this is done, you can execute the remote processes with the command

```sh
	javac p2p/*.java
	java p2p.RemotePeerStarter
```

### __Rnning PeerProcess Directly__

Start as many Peers with PeerIDs as given in the PeerInfo.cfg

Note that the peers need to be launched in the order mentioned in the PeerInfo.cfg file.

```sh
cd src
javac p2p/*.java
java p2p.PeerProcess <peerID>
```