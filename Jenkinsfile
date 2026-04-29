pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew clean build -x test'
            }
        }
        stage('Test') {
            steps {
                sh './gradlew test'
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }
    }

    post {
        success {
            echo '✅ 빌드 & 테스트 성공'
        }
        failure {
            echo '❌ 빌드 실패 - 로그 확인 필요'
        }
    }
}