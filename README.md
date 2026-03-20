# Resume Portfolio

This project is now prepared for a simple VPS deployment on `alejandrovalencia.site`.

## What Changed

- the app now serves from the root context by default
- `GET /` now redirects to `/portfolio/alejandro`
- `Jenkinsfile` is now the single VPS pipeline
- that pipeline pauses and asks whether to run `RESET_VM` or `DEPLOY_APP`
- the previous LocalStack EKS pipeline was preserved as `Jenkinsfile.localstack-eks`

## Public URLs

- `https://alejandrovalencia.site/` -> redirects to the portfolio CV
- `https://alejandrovalencia.site/portfolio/alejandro`
- `https://alejandrovalencia.site/api/hello`
- `https://alejandrovalencia.site/api/users`

## Jenkins Credential IDs Used Here

- `NAMECHEAP_VPS_SSH`
- `PORTFOLIO_VPS_ZEROSSL_FULLCHAIN`
- `PORTFOLIO_VPS_ZEROSSL_PRIVKEY`

`NAMECHEAP_VPS_SSH` should now be a Jenkins `Username with password` credential.

## Important Files

- `Jenkinsfile`
- `scripts/vps-bootstrap.sh`
- `scripts/vps-deploy.sh`
- `deploy/vps/nginx-site.conf.template`
- `docs/NAMECHEAP_VPS_DEPLOYMENT.md`

## Local Run

```bash
./mvnw spring-boot:run
```

Then open:

- `http://localhost:8080/`
- `http://localhost:8080/portfolio/alejandro`

## Full Walkthrough

Read the beginner guide in:

- `docs/NAMECHEAP_VPS_DEPLOYMENT.md`
