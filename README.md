
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
> git clone https://github.com/alisters/codersGroupDocker.git
```

Now pull the image we are going to use to build this - apologies for the download sizes
```
> docker pull maven:3.8.3-openjdk-17
```
Pick and create a temp directory so you can cache the libraries locally
```
> docker run -v ${PWD}:/opt/maven  -v c:\users\ashipman\temp\.m2:/root/.m2 -w /opt/maven maven:3.8.3-openjdk-17 -- mvn install
```

# 2) Test Drive
```
> docker build -t craft-beer .
> docker run -t craft-beer --name craft-beer1
Error: Unable to access jarfile craft-beer1
> docker run --tty --rm --name craft-beer1  craft-beer
> docker ps
> docker stop craft-beer1
```

Sure, you can run these commands - but in my experience you'll quickly run into a problem with docker and want to know what's going on. 

Lets browse to this server !
``` 
> start http:\\localhost:8080 
# Nothing
> netstat -a | sls LISTENING | sls 8080
```

Hmm, nothing.... so lets start with understanding what we just did

# 2) Images

Somethings going on here we ran java ?

```
> docker --help
> docker inspect craft-beer
> docker events
```

Note the entry point

How can we figure out what else is in the container ?

https://github.com/wagoodman/dive

```
> docker pull wagoodman/dive
> dive craft-beer
> docker run --rm -it -v /var/run/docker.sock:/var/run/docker.sock -e DOCKER_API_VERSION=1.37 wagoodman/dive:latest craft-beer
```

ok so we've found a shell
```
> docker run --name craft-beer-1 -it --entrypoint /busybox/sh craft-beer
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
> docker run --name craft-beer-2 --pid container:craft-beer-1 -it --entrypoint /busybox/sh craft-beer
```
Hmm, we don't look like we are isolated at all !
So, where are these running on my machine ?  

```
> docker run --privileged --rm --pid=host -ti ubuntu
> nsenter --target 1 --mount sh
```
So we aren't isolated at all
And this is definitely looks like a linux box
I'm getting out, and just using this a bit more

Get a shell using k9s
cd /proc
cd {somePid}
cd ns
ls -la

Show that the two processes do in fact have the same namespaces in /proc

lsns
  

```
> docker run -it -p 8080:8080/tcp craft-beer
> docker run --name craft-beer2 -it --rm -p 8080:8080/tcp -v ${PWD}\data:/data craft-beer
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
> pushd
cd ~/.docker
cat .\config.json
Get-Command docker-credential-gcloud.cmd
gcloud auth configure-docker
```

https://docs.docker.com/registry/spec/api/#pulling-an-image

# 5) Container Monitoring, pods and kubernetes


```
> docker ps
```
k8s.gcr.io/pause:3.7

https://learnk8s.io/kubernetes-network-packets#the-pause-container-creates-the-network-namespace-in-the-pod

```
cd ~/.kube
> gcloud config config-helper --format=json
> kubectx
> kubectl options
> kubectl get pods -n dev -v=7
> kubectl get pods -n dev -v=9
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