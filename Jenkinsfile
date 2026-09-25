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
                "%DOCKER_EXE%" rm -f hotel-app hotel-mysql 2>NUL || echo No previous application containers to remove

                set "HOTEL_IMAGE=hotel-res-system:%BUILD_NUMBER%"

                "%DOCKER_EXE%" compose up -d
                '''

                echo 'Checking deployed Docker containers...'

                bat '"%DOCKER_EXE%" compose ps'

                echo 'Waiting for deployed application...'

                bat '''
                powershell -NoProfile -Command "$ok=$false; for($i=1;$i -le 12;$i++){ try { $r=Invoke-WebRequest -UseBasicParsing 'http://localhost:8081/' -TimeoutSec 10; if($r.StatusCode -eq 200){ Write-Host 'Deployment health check passed - HTTP 200'; $ok=$true; break } } catch { Write-Host ('Waiting for application... attempt ' + $i) }; Start-Sleep -Seconds 5 }; if(-not $ok){ Write-Error 'Deployment health check failed'; exit 1 }"
                '''
            }
        }

        stage('Release') {
            steps {
                echo 'Creating automated versioned release...'

                bat '''
                "%DOCKER_EXE%" tag hotel-res-system:%BUILD_NUMBER% hotel-res-system:release-%BUILD_NUMBER%
                "%DOCKER_EXE%" tag hotel-res-system:%BUILD_NUMBER% hotel-res-system:latest

                for /f %%i in ('git rev-parse --short HEAD') do set GIT_COMMIT_SHORT=%%i

                (
                    echo Hotel Reservation System Release
                    echo =================================
                    echo Release Version: v1.0.%BUILD_NUMBER%
                    echo Jenkins Build: %BUILD_NUMBER%
                    echo Git Commit: %GIT_COMMIT_SHORT%
                    echo Docker Image: hotel-res-system:release-%BUILD_NUMBER%
                    echo Stable Image: hotel-res-system:latest
                    echo Release Date: %DATE%
                    echo Release Time: %TIME%
                ) > release-info.txt

                type release-info.txt
                '''

                echo 'Release created successfully.'
            }

            post {
                success {
                    archiveArtifacts artifacts: 'release-info.txt',
                                     fingerprint: true
                }
            }
        }

        stage('Monitoring') {
            steps {

                echo 'Verifying live application metrics...'

                bat '''
                powershell -NoProfile -Command "$ok=$false; for($i=1;$i -le 12;$i++){ try { $r=Invoke-WebRequest -UseBasicParsing 'http://localhost:8081/actuator/prometheus' -TimeoutSec 10; if($r.StatusCode -eq 200){ Write-Host 'LIVE METRICS AVAILABLE - HTTP 200'; $ok=$true; break } } catch { Write-Host ('Waiting for application metrics... attempt ' + $i) }; Start-Sleep -Seconds 5 }; if(-not $ok){ Write-Error 'Prometheus metrics endpoint unavailable'; exit 1 }"
                '''

                echo 'Checking Prometheus monitoring service...'

                bat '''
                powershell -NoProfile -Command "$ok=$false; for($i=1;$i -le 12;$i++){ try { $r=Invoke-WebRequest -UseBasicParsing 'http://localhost:9090/-/ready' -TimeoutSec 10; if($r.StatusCode -eq 200){ Write-Host 'PROMETHEUS READY'; $ok=$true; break } } catch { Write-Host ('Waiting for Prometheus... attempt ' + $i) }; Start-Sleep -Seconds 5 }; if(-not $ok){ Write-Error 'Prometheus unavailable'; exit 1 }"
                '''

                echo 'Checking Alertmanager service...'

                bat '''
                powershell -NoProfile -Command "$ok=$false; for($i=1;$i -le 12;$i++){ try { $r=Invoke-WebRequest -UseBasicParsing 'http://localhost:9093/-/ready' -TimeoutSec 10; if($r.StatusCode -eq 200){ Write-Host 'ALERTMANAGER READY'; $ok=$true; break } } catch { Write-Host ('Waiting for Alertmanager... attempt ' + $i) }; Start-Sleep -Seconds 5 }; if(-not $ok){ Write-Error 'Alertmanager unavailable'; exit 1 }"
                '''

                echo 'Verifying Prometheus is scraping the application...'

                bat '''
                powershell -NoProfile -Command "$ok=$false; for($i=1;$i -le 12;$i++){ try { $q=Invoke-RestMethod 'http://localhost:9090/api/v1/query?query=up%7Bjob%3D%22hotel-reservation%22%7D'; if($q.data.result.Count -gt 0 -and $q.data.result[0].value[1] -eq '1'){ Write-Host 'PROMETHEUS TARGET STATUS: UP'; $ok=$true; break } } catch {}; Write-Host ('Waiting for Prometheus scrape... attempt ' + $i); Start-Sleep -Seconds 5 }; if(-not $ok){ Write-Error 'Prometheus target is not UP'; exit 1 }"
                '''

                echo 'Simulating production incident...'

                bat '"%DOCKER_EXE%" stop hotel-app'

                echo 'Waiting for HotelApplicationDown alert to fire...'

                bat '''
                powershell -NoProfile -Command "$ok=$false; for($i=1;$i -le 18;$i++){ try { $r=Invoke-RestMethod 'http://localhost:9090/api/v1/alerts'; $found=@($r.data.alerts | Where-Object { $_.labels.alertname -eq 'HotelApplicationDown' -and $_.state -eq 'firing' }).Count -gt 0; if($found){ Write-Host 'INCIDENT ALERT FIRED: HotelApplicationDown'; $ok=$true; break } } catch {}; Write-Host ('Waiting for alert... attempt ' + $i); Start-Sleep -Seconds 5 }; if(-not $ok){ Write-Error 'HotelApplicationDown alert did not fire'; exit 1 }"
                '''

                echo 'Verifying Alertmanager received the incident alert...'

                bat '''
                powershell -NoProfile -Command "$ok=$false; for($i=1;$i -le 12;$i++){ try { $r=Invoke-RestMethod 'http://localhost:9093/api/v2/alerts'; $found=@($r | Where-Object { $_.labels.alertname -eq 'HotelApplicationDown' }).Count -gt 0; if($found){ Write-Host 'ALERTMANAGER RECEIVED HotelApplicationDown'; $ok=$true; break } } catch {}; Write-Host ('Waiting for Alertmanager... attempt ' + $i); Start-Sleep -Seconds 5 }; if(-not $ok){ Write-Error 'Alertmanager did not receive incident alert'; exit 1 }"
                '''

                echo 'Recovering production application...'

                bat '"%DOCKER_EXE%" start hotel-app'

                echo 'Verifying application recovery...'

                bat '''
                powershell -NoProfile -Command "$ok=$false; for($i=1;$i -le 12;$i++){ try { $r=Invoke-WebRequest -UseBasicParsing 'http://localhost:8081/' -TimeoutSec 10; if($r.StatusCode -eq 200){ Write-Host 'APPLICATION RECOVERED - HTTP 200'; $ok=$true; break } } catch { Write-Host ('Waiting for recovery... attempt ' + $i) }; Start-Sleep -Seconds 5 }; if(-not $ok){ Write-Error 'Application failed to recover'; exit 1 }"
                '''

                echo 'Confirming Prometheus detects recovery...'

                bat '''
                powershell -NoProfile -Command "$ok=$false; for($i=1;$i -le 12;$i++){ try { $q=Invoke-RestMethod 'http://localhost:9090/api/v1/query?query=up%7Bjob%3D%22hotel-reservation%22%7D'; if($q.data.result.Count -gt 0 -and $q.data.result[0].value[1] -eq '1'){ Write-Host 'PROMETHEUS TARGET RECOVERED: UP'; $ok=$true; break } } catch {}; Write-Host ('Waiting for monitoring recovery... attempt ' + $i); Start-Sleep -Seconds 5 }; if(-not $ok){ Write-Error 'Prometheus did not detect recovery'; exit 1 }"
                '''

                bat '''
                (
                    echo Hotel Reservation System Monitoring Evidence
                    echo ============================================
                    echo Live application metrics: VERIFIED
                    echo Prometheus target monitoring: VERIFIED
                    echo Incident simulation: COMPLETED
                    echo HotelApplicationDown alert: FIRED
                    echo Alertmanager integration: VERIFIED
                    echo Application recovery: VERIFIED
                    echo Monitoring recovery: VERIFIED
                    echo Jenkins Build: %BUILD_NUMBER%
                    echo Date: %DATE%
                    echo Time: %TIME%
                ) > monitoring-evidence.txt

                type monitoring-evidence.txt
                '''
            }

            post {
                always {
                    bat '''
                    "%DOCKER_EXE%" start hotel-app 2>NUL || echo Application already running
                    '''
                }

                success {
                    archiveArtifacts artifacts: 'monitoring-evidence.txt',
                                     fingerprint: true
                }
            }
        }
    }

    post {

        always {
            junit testResults: 'target/surefire-reports/*.xml',
                  allowEmptyResults: true
        }

        success {
            echo 'ALL 7 DEVOPS PIPELINE STAGES COMPLETED SUCCESSFULLY.'
        }

        failure {
            echo 'Pipeline failed. Check the failed stage in Jenkins Console Output.'
        }
    }
}