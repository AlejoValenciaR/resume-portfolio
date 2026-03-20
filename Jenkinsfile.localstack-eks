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
    AWS_DEFAULT_REGION    = 'us-east-1'
    AWS_ACCESS_KEY_ID     = credentials('LS_AWS_ACCESS_KEY_ID')
    AWS_SECRET_ACCESS_KEY = credentials('LS_AWS_SECRET_ACCESS_KEY')
    LS_ENDPOINT_URL       = credentials('LS_ENDPOINT_URL')

    APP_NAME         = 'hello-spring'
    APP_BASE_PATH    = '/apps'
    ECR_REPOSITORY   = 'hello-spring'
    EKS_CLUSTER_NAME = 'localstack-eks-cluster'
    EKS_NODEGROUP_NAME = 'localstack-eks-node-group'
    K8S_NAMESPACE    = 'hello-spring'
    PUBLIC_APP_URL   = 'https://nauthappstest.tech/apps/api/hello'
  }

  stages {
    stage('checkout') {
      steps {
        checkout scm
      }
    }

    stage('test') {
      steps {
        sh '''
          set -euo pipefail
          chmod +x mvnw scripts/deploy-to-localstack-eks.sh scripts/verify-localstack-eks.sh
          ./mvnw -B test
        '''
      }
    }

    stage('build, push, deploy') {
      steps {
        sh '''
          set -euo pipefail
          chmod +x scripts/deploy-to-localstack-eks.sh
          ./scripts/deploy-to-localstack-eks.sh
        '''
      }
    }

    stage('verify kubernetes objects') {
      steps {
        sh '''
          set -euo pipefail
          export KUBECONFIG="${WORKSPACE}/.kube/config"
          chmod +x scripts/verify-localstack-eks.sh
          ./scripts/verify-localstack-eks.sh
        '''
      }
    }
  }

  post {
    always {
      sh '''
        rm -rf "${WORKSPACE}/.kube"
      '''
    }
  }
}
