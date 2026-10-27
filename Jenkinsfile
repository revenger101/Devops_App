pipeline {
  agent any
  tools { jdk 'jdk8'; maven 'maven3' }
  options { timestamps(); disableConcurrentBuilds() }

  stages {
    stage('Build & Test') {
      steps {
        dir('microservices') {
          sh 'mvn -B clean verify'
        }
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: 'microservices/**/target/surefire-reports/*.xml'
        }
      }
    }
  }
}
