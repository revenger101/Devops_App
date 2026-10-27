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

    stage('SonarQube analysis') {
      steps {
        dir('microservices') {
          // The scanner needs Java 11+; the project itself still builds on jdk8 above.
          withEnv(['JAVA_HOME=/opt/java/openjdk']) {
            withSonarQubeEnv('sonarqube') {
              sh 'mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184:sonar -Dsonar.projectKey=achat-microservices -Dsonar.projectName=achat-microservices'
            }
          }
        }
      }
    }

    stage('Quality Gate') {
      steps {
        timeout(time: 5, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }
  }
}
