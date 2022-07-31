
# Newcastle Coders Group - Containers

A walkthrough of containers, images, docker, kubernetes and related commands with a focus on enough understanding what the commands we run do under the hood

We are going to build a server inside a docker container. 
It's going to be a bit inception like.... we are going to use docker to build our server AND run our server, then figure out what might 

  

# 0) Prep

https://start.spring.io/

https://github.com/GoogleContainerTools/distroless

``` 
> git clone https://gitlab.gbcloud.com.au/dt-apps-poc-build/alister.shipman/tech-conference-1.git
> docker pull maven:3.8.3-openjdk-17
> docker run -v ${PWD}:/opt/maven  -v c:\users\ashipman\temp\.m2:/root/.m2 -w /opt/maven maven:3.8.3-openjdk-17 -- mvn install
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

# 1) Images

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

ok so we can override the entry point, lets see what we can see
```
$ ps
```

We are running as root, does that matter ?
We only see a couple of processes ? What does that mean ?
  

# 2) Namespaces and Shell Commands

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
 

# 3) Docker Container runtimes

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

# 4) Container Monitoring and pods


```
> docker ps
```
k8s.gcr.io/pause:3.7

https://learnk8s.io/kubernetes-network-packets#the-pause-container-creates-the-network-namespace-in-the-pod

# 5) Kubernetes architecture and Container orchestration

```
cd ~/.kube
> gcloud config config-helper --format=json
> kubectx
> kubectl options
> kubectl get pods -n dev -v=7
> kubectl get pods -n dev -v=9
```

Hey now we see that we are just hitting an http api
Lets load that in the browser

create a namespace
bootstrap and image pull secrets
metadata, spec, labels, annotations

# 6 ) Extras

Declaritive and imperative way of handling resources
service accounts, namespaces, secrets, .docker secrets to pull images

kubectl get resources

k9s ctrl + a
Secrets used by
Secrets decoding
Docker secret type
TLS secret type