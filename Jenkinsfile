pipeline {
    agent any

    environment {
        DOCKER_EXE = 'C:\\Program Files\\Docker\\Docker\\resources\\bin\\docker.exe'
    }

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
                    archiveArtifacts artifacts: 'target/*.jar',
                                     fingerprint: true
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

                withCredentials([
                    string(
                        credentialsId: 'sonarqube-token',
                        variable: 'SONAR_TOKEN'
                    )
                ]) {
                    bat '''
                    mvnw.cmd org.sonarsource.scanner.maven:sonar-maven-plugin:3.11.0.3922:sonar ^
                    -Dsonar.projectKey=hotel-res-system-devops ^
                    -Dsonar.projectName="Hotel Reservation System" ^
                    -Dsonar.host.url=http://localhost:9000 ^
                    -Dsonar.token=%SONAR_TOKEN% ^
                    -Dsonar.qualitygate.wait=true ^
                    -Dsonar.qualitygate.timeout=300
                    '''
                }
            }
        }

        stage('Security') {
            steps {
                echo 'Building Docker image for vulnerability scanning...'

                bat '"%DOCKER_EXE%" build -t hotel-res-system:%BUILD_NUMBER% .'

                echo 'Saving Docker image for Trivy scanning...'

                bat '"%DOCKER_EXE%" save hotel-res-system:%BUILD_NUMBER% -o hotel-res-system-ci.tar'

                echo 'Scanning for HIGH and CRITICAL vulnerabilities...'

                bat '''
                "%DOCKER_EXE%" run --rm ^
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

                echo 'Trivy security scan completed.'
                bat 'type trivy-report.txt'
            }

            post {
                always {
                    archiveArtifacts artifacts: 'trivy-report.txt',
                                     allowEmptyArchive: true

                    bat '''
                    if exist hotel-res-system-ci.tar del /Q hotel-res-system-ci.tar
                    '''
                }
            }
        }

        stage('Deployment') {
            steps {
                echo 'Deploying secured Hotel Reservation System...'

                bat '''
                "%DOCKER_EXE%" rm -f hotel-app hotel-mysql 2>NUL || echo No previous containers to remove

                set "HOTEL_IMAGE=hotel-res-system:%BUILD_NUMBER%"

                "%DOCKER_EXE%" compose up -d
                '''

                echo 'Checking deployed Docker containers...'

                bat '"%DOCKER_EXE%" compose ps'

                echo 'Waiting for the application and performing health check...'

                bat '''
                powershell -NoProfile -Command "$ok=$false; for($i=1;$i -le 12;$i++){ try { $r=Invoke-WebRequest -UseBasicParsing 'http://localhost:8081/' -TimeoutSec 10; if($r.StatusCode -eq 200){ Write-Host 'Deployment health check passed - HTTP 200'; $ok=$true; break } } catch { Write-Host ('Waiting for application... attempt ' + $i) }; Start-Sleep -Seconds 5 }; if(-not $ok){ Write-Error 'Deployment health check failed'; exit 1 }"
                '''
            }
        }
    }

    post {

        always {
            junit testResults: 'target/surefire-reports/*.xml',
                  allowEmptyResults: true
        }

        success {
            echo 'Build, Test, Code Quality, Security and Deployment stages completed successfully.'
        }

        failure {
            echo 'Pipeline failed. Check the failed stage in Jenkins Console Output.'
        }
    }
}