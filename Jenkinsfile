pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master',
                    url: 'https://github.com/hack4569/bkrc-v3.git',
                    credentialsId: 'github-credentials'  // ID와 일치
            }
        }
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
        stage('Deploy') {
            steps {
                withCredentials([file(credentialsId: 'bkrc-env-file', variable: 'ENV_FILE')]) {
                    sh '''
                            cp $ENV_FILE .env
                            docker-compose stop bkrc
                            docker-compose rm -f bkrc
                            docker-compose up -d --build --no-deps bkrc
                        '''
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