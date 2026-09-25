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
                    bat 'mvnw.cmd org.sonarsource.scanner.maven:sonar-maven-plugin:3.11.0.3922:sonar -Dsonar.projectKey=hotel-res-system-devops -Dsonar.projectName="Hotel Reservation System" -Dsonar.host.url=http://localhost:9000 -Dsonar.token=%SONAR_TOKEN% -Dsonar.qualitygate.wait=true -Dsonar.qualitygate.timeout=300'
                }
            }
        }

        stage('Security') {
            steps {
                echo 'Building Docker image for vulnerability scanning...'
                bat 'docker build -t hotel-res-system:%BUILD_NUMBER% .'

                echo 'Saving Docker image for Trivy analysis...'
                bat 'docker save hotel-res-system:%BUILD_NUMBER% -o hotel-res-system-ci.tar'

                echo 'Scanning Docker image for HIGH and CRITICAL vulnerabilities...'
                bat '''
                docker run --rm ^
                -v "%CD%:/work" ^
                -v trivy-cache:/root/.cache/trivy ^
                aquasec/trivy:latest image ^
                --input /work/hotel-res-system-ci.tar ^
                --scanners vuln ^
                --severity HIGH,CRITICAL ^
                --exit-code 1 ^
                --timeout 15m ^
                --format table ^
                --output /work/trivy-report.txt
                '''

                echo 'Trivy security report:'
                bat 'type trivy-report.txt'
            }

            post {
                always {
                    archiveArtifacts artifacts: 'trivy-report.txt', allowEmptyArchive: true
                    bat 'if exist hotel-res-system-ci.tar del /Q hotel-res-system-ci.tar'
                }
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
        }

        success {
            echo 'Build, Test, Code Quality and Security stages completed successfully.'
        }

        failure {
            echo 'Pipeline failed. Check the Jenkins console output.'
        }
    }
}