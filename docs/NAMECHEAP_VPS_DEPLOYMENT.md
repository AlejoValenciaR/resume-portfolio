# Namecheap VPS Deployment Guide

This guide is now based on the workflow you actually want:

- only 1 Jenkins pipeline job
- `git push` starts that job automatically
- Jenkins pauses and asks you which workflow to run
- you choose:
  - `RESET_VM`
  - `DEPLOY_APP`

## Goal

We want this final behavior:

1. You push code to GitHub.
2. GitHub notifies Jenkins.
3. Jenkins starts the pipeline automatically.
4. Jenkins pauses and asks what to do on the VPS.
5. You choose one option:
   - `RESET_VM`
   - `DEPLOY_APP`
6. Jenkins runs the selected workflow on the VPS.
7. The site stays available at `https://alejandrovalencia.site`.

## Very Important Truth About This Design

This is the closest Jenkins behavior to what you asked for.

What Jenkins can do:

- start the pipeline automatically after `git push`
- pause the running build
- ask you to choose between reset and deploy

What Jenkins cannot do:

- show you a choice before the build exists

So the real flow is:

1. `git push`
2. Jenkins job starts
3. Jenkins waits for your selection inside the running build
4. you open the build page and choose the workflow

## Final Architecture

```text
Git push
  -> GitHub webhook
  -> Jenkins single pipeline job
  -> choose RESET_VM or DEPLOY_APP
  -> SSH into VPS 198.177.123.110
  -> Docker + Nginx on Ubuntu 24.04
  -> Spring Boot container on 127.0.0.1:8080
  -> https://alejandrovalencia.site
```

## Files In This Repo That Matter

- `Jenkinsfile`
  This is the only Jenkins pipeline you need to configure.
- `scripts/vps-bootstrap.sh`
  This is what runs when you choose `RESET_VM`.
- `scripts/vps-deploy.sh`
  This is what runs when you choose `DEPLOY_APP`.
- `scripts/vps-common.sh`
  Shared SSH logic, including password-based SSH with `sshpass`.

## What The Two Workflow Options Mean

### `RESET_VM`

Use this when:

- the VPS is brand new
- Docker is not installed yet
- Nginx is not installed yet
- SSL files are not installed yet
- you want to rebuild the VPS application setup from zero

What it does:

- installs or verifies Docker
- installs or verifies Nginx
- configures firewall rules when available
- uploads SSL files
- writes Nginx configuration
- loads the Docker image
- starts the Spring Boot container
- waits for the Spring Boot app to become reachable
- verifies the site

### `DEPLOY_APP`

Use this when:

- the VPS was already prepared before
- Docker and Nginx already exist
- SSL is already installed
- you only want to deploy or redeploy the latest Spring Boot app

What it does:

- loads the new Docker image
- replaces the running container
- waits for the Spring Boot app to become reachable
- verifies the app over HTTPS

## Before Jenkins: Manual Things You Must Do One Time

### 1. Verify SSH Username And Password Access

The pipeline will use SSH with username and password.

Manual test from your computer:

```powershell
ssh root@198.177.123.110
```

If that login does not work manually, Jenkins will not work either.

For your current VPS, the username is:

```text
root
```

### 2. Configure DNS In Namecheap

In the Namecheap DNS panel for `alejandrovalencia.site`, create:

- `A` record for `@` -> `198.177.123.110`
- optional `A` record for `www` -> `198.177.123.110`
- optional `A` record for `server1` -> `198.177.123.110`

Why:

- `@` makes `alejandrovalencia.site` point to the VPS
- `www` is useful if you want it later
- `server1` is useful if you want DNS for the machine hostname too

### 3. Create Or Download The ZeroSSL Certificate Files

For this repo, Jenkins expects:

- `fullchain.pem`
- `privkey.pem`

ZeroSSL usually gives:

- `certificate.crt`
- `ca_bundle.crt`
- `private.key`

So create the files like this on Windows PowerShell:

```powershell
Get-Content .\certificate.crt, .\ca_bundle.crt | Set-Content .\fullchain.pem
Copy-Item .\private.key .\privkey.pem
```

Before uploading them to Jenkins, verify that they match:

```powershell
$certHash = openssl x509 -in .\fullchain.pem -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256
$keyHash = openssl pkey -in .\privkey.pem -pubout -outform der | openssl dgst -sha256
$certHash
$keyHash
```

The 2 SHA256 values must be the same.

If they are different:

- the certificate and private key do not belong together
- Jenkins will fail when Nginx tries to start HTTPS
- you must upload the correct matching pair or reissue the ZeroSSL certificate

The certificate should cover at least:

- `alejandrovalencia.site`

Optional:

- `www.alejandrovalencia.site`
- `server1.alejandrovalencia.site`

### 4. Create Jenkins Credentials

Create these Jenkins credentials with these exact IDs:

- `NAMECHEAP_VPS_SSH`
  Type: `Username with password`
  Username: `root`
  Password: the VPS password from the email

- `PORTFOLIO_VPS_ZEROSSL_FULLCHAIN`
  Type: `Secret file`
  File: `fullchain.pem`

- `PORTFOLIO_VPS_ZEROSSL_PRIVKEY`
  Type: `Secret file`
  File: `privkey.pem`

### 5. Confirm The Jenkins Linux Agent Has The Right Tools

The Jenkins agent that runs this pipeline should have:

- Git
- Docker CLI
- Java 21
- Bash
- OpenSSH client
- `sshpass`

If your Jenkins agent is Ubuntu or Debian:

```bash
sudo apt-get update
sudo apt-get install -y sshpass
```

Important:

- this pipeline uses `sh`, not Windows batch
- the Jenkins build agent should be Linux-based

### 6. Confirm Jenkins Is Reachable From GitHub

For automatic builds after `git push`, GitHub must be able to reach Jenkins.

That means:

- Jenkins must have a public URL or a public reverse proxy URL
- GitHub must be able to call that URL from the internet

Example:

```text
https://your-jenkins-domain/github-webhook/
```

If Jenkins is behind a path prefix:

```text
https://your-jenkins-domain/jenkins/github-webhook/
```

If Jenkins is reachable only on `localhost` or only inside your private LAN, GitHub webhooks will not work.

## How To Configure The Single Jenkins Job

Recommended job name:

```text
resume-portfolio-vps
```

Create it like this:

1. Open Jenkins Dashboard.
2. Click `New Item`.
3. Type `resume-portfolio-vps`.
4. Select `Pipeline`.
5. Click `OK`.
6. Scroll to the `Pipeline` section.
7. In `Definition`, choose `Pipeline script from SCM`.
8. In `SCM`, choose `Git`.
9. In `Repository URL`, use:

```text
https://github.com/AlejoValenciaR/resume-portfolio.git
```

10. In `Branches to build`, use:

```text
*/main
```

11. In `Script Path`, use:

```text
Jenkinsfile
```

12. Save the job.

## Very Important First Manual Run

After creating `resume-portfolio-vps`, run it one time manually.

Why:

- Jenkins needs to load the `Jenkinsfile`
- the `Jenkinsfile` contains the `githubPush()` trigger
- the first run also proves the Jenkins job can see your credentials and tools

## How The Jenkins UI Choice Works

When the pipeline runs, it pauses and waits for your decision.

The dropdown options are:

- `DEPLOY_APP`
- `RESET_VM`

The first option is `DEPLOY_APP` because that is the normal daily use case.

The pipeline waits up to 2 hours for your choice.

Important:

- if you do nothing, the build will time out
- while a build is waiting, later pushes may queue because concurrent builds are disabled

## What You Will See After `git push`

This is the real sequence:

1. You do `git push`.
2. GitHub sends the webhook to Jenkins.
3. Jenkins starts the `resume-portfolio-vps` job.
4. The build reaches the choice stage and pauses.
5. You open Jenkins.
6. You open the running build.
7. You click the input/continue button.
8. You choose:
   - `RESET_VM`, or
   - `DEPLOY_APP`
9. Jenkins continues with the selected path.

## How To Configure GitHub Webhook For Automatic Start

Open your repository:

```text
https://github.com/AlejoValenciaR/resume-portfolio
```

Then do this:

1. Click `Settings`.
2. Click `Webhooks`.
3. Click `Add webhook`.
4. In `Payload URL`, put:

```text
https://YOUR-JENKINS-URL/github-webhook/
```

5. In `Content type`, choose:

```text
application/json
```

6. In `Secret`, you may leave it empty for now.
7. In events, choose:

```text
Just the push event
```

8. Make sure `Active` is checked.
9. Click `Add webhook`.

After that:

- a push to GitHub should start the Jenkins job automatically
- the running build should then wait for your workflow choice

## First Real Run: Exact Beginner Order

Follow this order:

1. Verify `ssh root@198.177.123.110` works manually.
2. Add the Namecheap DNS `A` record for `@`.
3. Wait for the domain to resolve to `198.177.123.110`.
4. Generate or download the ZeroSSL certificate.
5. Create `fullchain.pem` and `privkey.pem`.
6. Create the 3 Jenkins credentials.
7. Make sure the Jenkins Linux agent has `sshpass`.
8. Create the Jenkins job `resume-portfolio-vps`.
9. Run `resume-portfolio-vps` manually one time.
10. When Jenkins asks for the workflow, choose:

```text
RESET_VM
```

11. Wait for the build to finish.
12. Open `https://alejandrovalencia.site`.
13. Confirm the portfolio page works.
14. Configure the GitHub webhook.
15. Push a small code change to GitHub.
16. Wait for Jenkins to start the pipeline automatically.
17. Open the running Jenkins build.
18. When Jenkins asks for the workflow, choose:

```text
DEPLOY_APP
```

19. Wait for the build to finish.
20. Confirm the new version is live.

## Daily Use After The First Setup

Normal daily workflow:

1. Change code.
2. `git push`
3. Jenkins starts automatically.
4. Open the running build.
5. Choose `DEPLOY_APP`.

Only choose `RESET_VM` when:

- the VPS was rebuilt
- Docker or Nginx is broken
- you want to recreate the VPS app setup from zero

## Public App Behavior

The site should behave like this:

- `https://alejandrovalencia.site/` redirects to `/portfolio/alejandro`
- Nginx terminates HTTPS
- Nginx proxies traffic to `127.0.0.1:8080`
- the Spring Boot app serves the portfolio

## Paths And Ports On The VPS

The scripts use:

```text
/opt/resume-portfolio
```

Container name:

```text
resume-portfolio
```

Ports:

- Nginx: `80` and `443`
- Spring Boot container: `127.0.0.1:8080`

## Troubleshooting

### Jenkins Cannot SSH

Check:

- the `NAMECHEAP_VPS_SSH` credential uses username `root`
- the password is correct
- port `22` is reachable

### Jenkins Fails With `scp: stat local "22": No such file or directory`

That means the Jenkins workspace is using an older version of the deployment script.

What to do:

1. make sure your Jenkins job is building the latest commit from `main`
2. pull the latest repository changes into Jenkins
3. run the pipeline again

This error is fixed in the current `scripts/vps-common.sh`.

### Git Push Does Not Start Jenkins

Check:

- the Jenkins job uses `Jenkinsfile`
- the job was run once manually after creation
- the GitHub webhook points to `https://YOUR-JENKINS-URL/github-webhook/`
- Jenkins is reachable from the public internet
- GitHub webhook deliveries show HTTP `200`
- you pushed to the branch Jenkins watches, normally `main`

### Git Push Starts Jenkins But Jenkins Does Nothing

That usually means the build is waiting for your choice.

Open the running build and look for the input step.

### Jenkins Build Waited Too Long And Failed

That usually means the pipeline timed out waiting for your selection.

Just start the build again or push again, then choose the workflow before the timeout expires.

### Browser Shows Bad Gateway

Check:

- Docker is running on the VPS
- the container started correctly
- the app is listening on `127.0.0.1:8080`
- Nginx is proxying correctly

### Jenkins Fails With `curl: (56) Recv failure: Connection reset by peer`

That usually means one of these:

- the Spring Boot container is still starting
- the container started and then crashed
- Nginx is ready, but the app behind it is not ready yet

The current scripts now wait longer and print diagnostics automatically.

If you still hit this error, inspect:

- Docker container logs
- Docker container status
- Nginx service status

Those details should now appear directly in the Jenkins console log.

### HTTPS Fails

Check:

- DNS points to `198.177.123.110`
- the certificate really covers `alejandrovalencia.site`
- Jenkins uploaded the correct PEM files
- Nginx restarted successfully

### Jenkins Fails With `SSL_CTX_use_PrivateKey ... key values mismatch`

That means:

- `fullchain.pem` and `privkey.pem` do not match

What to do:

1. go back to the files you downloaded from ZeroSSL
2. make sure `privkey.pem` comes from the same certificate request as `certificate.crt`
3. rebuild `fullchain.pem` from the correct `certificate.crt` and `ca_bundle.crt`
4. verify both files with the OpenSSL SHA256 check shown earlier in this guide
5. update the Jenkins credentials:
   - `PORTFOLIO_VPS_ZEROSSL_FULLCHAIN`
   - `PORTFOLIO_VPS_ZEROSSL_PRIVKEY`
6. run the pipeline again with `RESET_VM`

## Optional Improvement Later

Later, if you want less manual work, we can change this again so that:

- normal `git push` always does `DEPLOY_APP` automatically
- `RESET_VM` stays manual

That is more production-friendly.
