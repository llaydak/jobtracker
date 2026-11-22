pipeline {
    agent any

    environment {
        APP_VERSION = "0.0.1-SNAPSHOT"
    }

    stages {
        stage('1. Checkout') {
            steps {
                checkout scm
            }
        }

        stage('2. Build & Unit Tests') {
            steps {
                sh './mvnw clean test jacoco:report'
            }
            post {
                always {
                    // graphic report
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('3. SonarQube Static Analysis') {
            steps {
                withSonarQubeEnv('SonarQube-Server') {
                    sh './mvnw sonar:sonar -Dsonar.projectKey=job-tracker'
                }
            }
        }

        stage('4. Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('5. Publish Artifact to Nexus') {
            steps {
                sh './mvnw deploy -DskipTests'
            }
        }

        stage('6. Build Docker Images') {
            steps {
                sh 'docker compose build'
            }
        }
    }

    post {
        success {
            echo "Pipeline completed. Artifact uploaded to Nexus and Docker images are ready."
        }
        failure {
            echo "Pipeline failed! Check test, Quality Gate, or build logs."
        }
    }
}