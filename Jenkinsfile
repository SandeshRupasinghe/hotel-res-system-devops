pipeline {
    agent any

    options {
        timestamps()
    }

    stages {
        stage('Build') {
            steps {
                echo 'Building Hotel Reservation System...'
                bat 'mvnw.cmd clean package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Running automated tests...'
                bat 'mvnw.cmd test'
            }
        }

        stage('Code Quality') {
            steps {
                echo 'Running SonarQube code quality analysis...'
                withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
                    bat 'mvnw.cmd sonar:sonar -Dsonar.projectKey=hotel-res-system-devops -Dsonar.projectName="Hotel Reservation System" -Dsonar.host.url=http://localhost:9000 -Dsonar.token=%SONAR_TOKEN% -Dsonar.qualitygate.wait=true -Dsonar.qualitygate.timeout=300'
                }
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
        }

        success {
            echo 'Build, Test and Code Quality stages completed successfully.'
        }

        failure {
            echo 'Pipeline failed. Check the Jenkins console output.'
        }
    }
}