# Tech Conference 1

A walkthrough of containers, images, docker, kubernetes and related commands with a focus
on enough understanding, troubleshooting, and application to pipelines

# 0) Prep  
https://start.spring.io/  
https://github.com/GoogleContainerTools/distroless
 
``` git clone https://gitlab.gbcloud.com.au/dt-apps-poc-build/alister.shipman/tech-conference-1.git
mvn install
docker build -t craft-beer .
docker run -t craft-beer
```
OR
```
docker pull maven:3.8.3-openjdk-17
docker run --rm -v ${PWD}:/opt/maven -w /opt/maven maven:3.8.3-openjdk-17 mvn clean install
docker build -t craft-beer .
docker run -t craft-beer
```
# 1) Images
Somethings going on here we ran java ?  
``` 
docker inspect craft-beer
```
Note the entry point
How can we figure out what else is in the container ?  
https://github.com/wagoodman/dive
```
docker pull wagoodman/dive
dive craft-beer
docker run --rm -it -v /var/run/docker.sock:/var/run/docker.sock -e DOCKER_API_VERSION=1.37 wagoodman/dive:latest craft-beer
```
ok sho we've found a shell
```
docker run --name craft-beer-1 -it --entrypoint /busybox/sh craft-beer
```
ok so we can override the entry point, lets see what we can see
```
ps  
```
We are running as root, does that matter ?  

We only see a couple of processes ? What does that mean ?
  
  

# 2) Namespaces and Shell Commands  
Lets have a look at some switches e.g --pid="" 
Maybe we aren't that limited  
```
docker run --name craft-beer-2 --pid container:craft-beer-1 -it --entrypoint /busybox/sh craft-beer
```
Hmm, we don't look like we are isolated at all !


```
docker run --privileged --rm --pid=host -ti ubuntu 
nsenter --target 1 --mount sh 

# Whoa !! What the 
apt-get update
apt-get install libcap2-bin
getpcaps 24050

```
So we aren't isolated at all  
And this is definitely looks like a linux box
I'm getting out, and just using this a bit more 

```
git checkout 4
docker run -it -p 8080:8080/tcp craft-beer  
```
Hey - so we are up and running !  
And maybe i know a bit more now about how that port inside the container made it
to my local host

# 3)  Docker Container runtimes
ok - so i know what a "container" is now. So what how is that related to docker ?
https://www.ianlewis.org/en/container-runtimes-part-1-introduction-container-r

```
pushd
cd ~/.docker  
cat .\config.json
docker logout "australia-southeast1-docker.pkg.dev"
Get-Command docker-credential-gcloud.cmd
gcloud auth configure-docker
# 
docker pull australia-southeast1-docker.pkg.dev/gb-plat-dev-gar-v1/apps-docker-repo/build/helm-kubectl-gb:28352

popd
```
https://docs.docker.com/registry/spec/api/#pulling-an-image
# 4)  Container Monitoring and pods

docker run --name craft-beer -it -p 8080:8080/tcp -v data:/data craft-beer

```
docker ps
k8s.gcr.io/pause:3.7
https://learnk8s.io/kubernetes-network-packets#the-pause-container-creates-the-network-namespace-in-the-pod
```
# 5) Kubernetes architecture and Container orchestration 
```
cd ~/.kube
gcloud config config-helper --format=json
kubectx
kubectl options
kubectl get pods -n dev -v=5
kubectl get pods -n dev -v=7
kubectl get pods -n dev -v=9
```
Hey now we see that we are just hitting an http api  
Lets load that in the browser  
start 
Ahh bugger, looks like kubectl is providing some   kind of auth  
Lets have a look at the kube api  
create a namespace  
bootstrap and image pull secrets

metadata, spec, labels, annotations
```
kubectl create namespace techcon1
k9s
kubectl create deployment -n techcon1 busybox --image=australia-southeast1-docker.pkg.dev/gb-plat-dev-gar-v1/apps-docker-repo/build/busybox:28279
# Copy the secret to the new namespace and set it up on the service account
kubectl delete deployment -n techcon1 busybox
gc ..\getting-started\bootstrap.ps1
kubectl patch serviceaccount default -n techcon1 -p '{\"imagePullSecrets\": [{\"name\": \"gar-docker-registry\"}]}'
```

docker run --name craft-beer -it -p 8080:8080/tcp -v ${PWD}\data:/data craft-beer
# Confirm the mounted data is there 
docker exec -it craft-beer sh
Declaritive and imperative way of handling resources

service accounts, namespaces, secrets, .docker secrets to pull images
kubectl get resources
k9s ctrl + a
Secrets used by
Secrets decoding
Docker secret type
TLS secret type

# 6) Helm 
```
git checkout 10
helm ls
helm ls -A
helm ls -A -a
helm history gbs-backend
helm history gbs-backend -n dev
cd working
helm create craft-beer-chart
helm template foo .
# Edit the chart down - remove the tolerations, node selectors etc
helm install 
helm install craft-beer .\craft-beer-chart\ -n techcon1
https://kubernetes.io/docs/tasks/configure-pod-container/configure-persistent-volume-storage/

# Keep redeploying and editing until you've got 
helm install craft-beer .\craft-beer-chart\ -n techcon1
kubectl get pods -n techcon1
kubectl cp .\data\beers.json craft-beer-65487bf656-tpjd6:/data -n techcon1
kubectl port-forward pod/craft-beer-65487bf656-tpjd6 8081:8080 -n techcon1
git checkout 15
# Port forward
start http://localhost:8081

# Extra marks, turn on the ingress, and call it
# Hint - use an nip.io host name in the ingress to disambiguate the virtual server you're addressing
# The ingress will route according to the host header
```
Helm is handy but overengineered for our needs


Questions that i often see
- DNS
- Can't pull images
- Helm deploy didn't work
- Runner job stuck
- Pipeline status not updating
- I can't onboard locally


Immutable Pipelines
The binary auth plan - pipeline attestations

Gitlab runner executors

Why use containers for builds
Why use kubernetes for builds - binary auth
