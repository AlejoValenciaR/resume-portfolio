pipeline {
  agent any

  triggers {
    githubPush()
  }

  options {
    timestamps()
    ansiColor('xterm')
    disableConcurrentBuilds()
  }

  environment {
    APP_NAME           = 'resume-portfolio'
    APP_CONTAINER_NAME = 'resume-portfolio'
    DOMAIN_NAME        = 'alejandrovalencia.site'
    VPS_HOST           = '198.177.123.110'
    VPS_PORT           = '22'
    REMOTE_APP_DIR     = '/opt/resume-portfolio'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Choose VPS Workflow') {
      steps {
        timeout(time: 2, unit: 'HOURS') {
          script {
            env.SELECTED_WORKFLOW = input(
              message: "Choose what Jenkins should run on the VPS for build #${env.BUILD_NUMBER}.",
              ok: 'Run Selected Workflow',
              parameters: [
                choice(
                  name: 'VPS_WORKFLOW',
                  choices: ['DEPLOY_APP', 'RESET_VM'].join('\n'),
                  description: 'DEPLOY_APP is the normal redeploy. RESET_VM rebuilds the VPS app setup from zero.'
                )
              ]
            )
            currentBuild.description = "Workflow: ${env.SELECTED_WORKFLOW}"
            echo "Selected workflow: ${env.SELECTED_WORKFLOW}"
          }
        }
      }
    }

    stage('Test') {
      steps {
        sh '''
          set -euo pipefail
          chmod +x mvnw
          ./mvnw -B test
        '''
      }
    }

    stage('Build Docker Image') {
      steps {
        sh '''
          set -euo pipefail
          export IMAGE_REF="${APP_NAME}:${BUILD_NUMBER}-$(git rev-parse --short HEAD)"
          echo "${IMAGE_REF}" > .image-ref
          docker build -t "${IMAGE_REF}" .
        '''
      }
    }

    stage('Reset VPS From Zero') {
      when {
        expression { env.SELECTED_WORKFLOW == 'RESET_VM' }
      }
      steps {
        withCredentials([
          usernamePassword(credentialsId: 'NAMECHEAP_VPS_SSH', usernameVariable: 'SSH_USER', passwordVariable: 'SSH_PASSWORD'),
          file(credentialsId: 'PORTFOLIO_VPS_ZEROSSL_FULLCHAIN', variable: 'SSL_FULLCHAIN_FILE'),
          file(credentialsId: 'PORTFOLIO_VPS_ZEROSSL_PRIVKEY', variable: 'SSL_PRIVKEY_FILE')
        ]) {
          sh '''
            set -euo pipefail
            chmod +x scripts/vps-common.sh scripts/vps-bootstrap.sh
            export IMAGE_REF="$(cat .image-ref)"
            ./scripts/vps-bootstrap.sh
          '''
        }
      }
    }

    stage('Deploy/Redeploy Spring Boot') {
      when {
        expression { env.SELECTED_WORKFLOW == 'DEPLOY_APP' }
      }
      steps {
        withCredentials([
          usernamePassword(credentialsId: 'NAMECHEAP_VPS_SSH', usernameVariable: 'SSH_USER', passwordVariable: 'SSH_PASSWORD')
        ]) {
          sh '''
            set -euo pipefail
            chmod +x scripts/vps-common.sh scripts/vps-deploy.sh
            export IMAGE_REF="$(cat .image-ref)"
            ./scripts/vps-deploy.sh
          '''
        }
      }
    }
  }

  post {
    always {
      sh '''
        rm -f .image-ref
      '''
    }
  }
}
