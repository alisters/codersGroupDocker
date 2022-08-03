
# Newcastle Coders Group - Containers

A walkthrough of containers, images, docker, kubernetes and related commands with a focus on enough understanding what the commands we run do under the hood

We are going to build a server inside a docker container. 
It's going to be a bit inception like.... we are going to use docker to build our server, run some tools AND run our server, then kind of from the "inside out" we will put together clues that help us understand and cement what containers are and how dockers tools create them. 

# 0) Prep

We will use a kotlin spring mvc rest server, run by spring boot and generated using 
https://start.spring.io/

This server will be installed and run in a distroless base image  
https://github.com/GoogleContainerTools/distroless

Start by cloning this repo
``` 
git clone https://github.com/alisters/codersGroupDocker.git
```

Now pull the image we are going to use to build this - apologies for the download sizes
```
docker pull maven:3.8.3-openjdk-17
```
Pick and create a temp directory so you can cache the libraries locally
```
docker run -v ${PWD}:/opt/maven  -v c:\users\ashipman\temp\.m2:/root/.m2 -w /opt/maven maven:3.8.3-openjdk-17 -- mvn install
```
```
docker pull wagoodman/dive
```


# 2) Test Drive
```
docker --help
```
```
docker build -f .\Dockerfile.1 -t craft-beer1 .
```
```
docker run --tty --rm --name craft-beer1  craft-beer1
```
*Error: -jar requires jar file specification*  
Somethings going on here ? we ran java 

```
docker inspect craft-beer1
```
Note the entrypoint
```
docker build -f .\Dockerfile.2 -t craft-beer2 .
```
```
docker run --tty --rm --name craft-beer2  craft-beer2
```

And we are stuck, so in a new terminal
```
docker ps
```
```
docker stop craft-beer2
```
```
docker inspect craft-beer2
```
```
docker run --tty --rm --interactive --name craft-beer2  craft-beer2
```


Sure, you can run these commands - but in my experience you'll quickly run into a problem with docker and want to know what's going on. 

Lets browse to this server !
``` 
start http:\\localhost:8080 
```
*Nothing whats going on ? We've got an expose statement so why can't we browse it*
```
netstat -a | sls LISTENING | sls 8080
```

Hmm, nothing.... so lets start with understanding what we just did

# 2) Images


```
docker --help
```
```
docker events
```
```
docker run --help
```
```
docker run --help | sls "namespace|cgroup" -Context 0,1
```
Note the entry point

How can we figure out what else is in the container ?

https://github.com/wagoodman/dive

```
docker run --rm -it -v /var/run/docker.sock:/var/run/docker.sock -e DOCKER_API_VERSION=1.37 wagoodman/dive:latest craft-beer2
```

ok so we've found a shell
```
docker run --name craft-beer-1-sh -it --entrypoint /busybox/sh craft-beer1
```

ok so we can override the entry point, so the cmd in the manifest is not what makes the container.....
 

# 3) Namespaces and Shell Commands
```
$ ps
```

We are running as root, does that matter ?
We only see a couple of processes ? What does that mean ?


Lets have a look at some switches e.g --pid=""
Maybe we aren't that limited
```
start https://docs.docker.com/engine/reference/run/
```

```
docker run --name craft-beer-1b --pid container:craft-beer-1-sh -it --entrypoint /busybox/sh craft-beer1
```
Get a shell using k9s
```
cd /proc
```
```
cd 1
```
```
cd ns
```
```
ls -la
```
Show that the two processes do in fact have the same namespaces in /proc

Hmm, we don't look like we are isolated at all !
So, where are these running on my machine ?  

```
docker run --privileged --rm --pid=host -ti ubuntu
```
```
ps
```
```
nsenter --target 1 --mount sh
```
```
ps
```
So we aren't isolated at all
And this is definitely looks like a linux box
I'm getting out, and just using this a bit more

Lets play with the dockerfile and see if we can get a different user namespace

ok - lets take a look at dockerfiles again
```
start https://docs.docker.com/engine/reference/builder/
```
Investigate why user namespace is still shared
```
https://docs.docker.com/engine/reference/commandline/dockerd/
```
And now we will try and our docker file
```
docker build -f .\Dockerfile.2 -t craft-beer2 .
```
```
docker run --tty --rm --interactive --name craft-beer2  craft-beer2
```
```
docker build -f .\Dockerfile.3 -t craft-beer3 .
```
```
docker run --name craft-beer-1c -it --entrypoint /busybox/sh craft-beer3
```
*docker: Error response from daemon: unable to find user beer: no matching entries in passwd file.*
```
docker build -f .\Dockerfile.4 -t craft-beer4 .
```
```
docker build -f .\Dockerfile.5 -t craft-beer5 .
```
```
docker run --tty --rm --interactive --name craft-beer1e  craft-beer5
```
```
docker run --name craft-beer-1e -it --entrypoint /busybox/sh craft-beer5
```

ok - so we got it running as a different user, but we still don't have a seperate user namespace !

And we still can't access the rest endpoint

---
## Interlude - Video on name spaces and cgroups

```
start https://youtu.be/sK5i-N34im8?t=116
start https://youtu.be/sK5i-N34im8?t=1483
```
---

  

```
docker run -it -p 8080:8080/tcp craft-beer
```
```
docker run --name craft-beer2 -it --rm -p 8080:8080/tcp -v ${PWD}\data:/data craft-beer
```
Confirm the mounted data is there, exec commands, to show that a container is something we still have access to, and is not just it's running process - we can run other processes inside it
```
docker exec -it craft-beer sh
```

Hey - so we are up and running !

And maybe i know a bit more now about how that port inside the container made it
to my local host
 

# 4) Container runtimes and Docker

ok - so i know what a "container" is now. So what how is that related to docker ?

https://www.ianlewis.org/en/container-runtimes-part-1-introduction-container-r


```
cd ~/.docker
```
```
cat .\config.json
```
```
Get-Command docker-credential-gcloud.cmd
```
```
gcloud auth configure-docker
```

https://docs.docker.com/registry/spec/api/#pulling-an-image

# 5) Container Monitoring, pods and kubernetes


```
docker ps
```
k8s.gcr.io/pause:3.7

https://learnk8s.io/kubernetes-network-packets#the-pause-container-creates-the-network-namespace-in-the-pod

```
cd ~/.kube
```
```
gcloud config config-helper --format=json
```
```
kubectx
```
```
kubectl options
```
```
kubectl get pods -n dev -v=7
```
```
kubectl get pods -n dev -v=9
```

Hey now we see that we are just hitting an http api

create a namespace
bootstrap and image pull secrets
metadata, spec, labels, annotations
Declaritive and imperative way of handling resources

Create a docker secret
service accounts, namespaces, secrets, .docker secrets to pull images


# 6 ) Back to the rock....
Lets have a look at dockerfiles with fresh eyes
Directives - describing the container to create, which docker translates to an OCI spec so its runtime can run it
Multi-stage build


# 7 ) Extras


kubectl get resources

k9s ctrl + a
Secrets used by
Secrets decoding
Docker secret type
TLS secret type