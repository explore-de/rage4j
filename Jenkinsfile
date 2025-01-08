def TAG
pipeline {
    agent { label 'docker' }
    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '14', numToKeepStr: '10')
        gitLabConnection('gitlab')
    }
    environment {
        TAG = "${getTag(this)}"
    }

    stages {
        stage('checkout') {
            steps {
                cleanWs()
                checkout scm
                println "TAG is set to " + TAG
                updateGitlabCommitStatus name: 'build', state: 'running'
            }
        }

        stage('Build Docusaurus Docker Image') {
            steps {
                script {
                    dir('docusaurus') {
                        sh 'docker build -t harbor.explore.de/explore/rage4j/docusaurus:${TAG} .'
                    }
                }
            }
        }

        stage('Push Docusaurus Docker Image') {
            when {
                anyOf {
                    branch 'main'
                    tag '*'
                }
            }
            steps {
                script {
                    dir('docusaurus') {
                        sh 'docker push harbor.explore.de/explore/rage4j/docusaurus:${TAG}'
                    }
                }
            }
        }

        stage('mvn package') {
            agent {
                docker {
                    image 'harbor.explore.de/base/maven:3.9.7-eclipse-temurin-21'
                    args '-u jenkins -v /var/run/docker.sock:/var/run/docker.sock -v maven-repository:/usr/share/repository'
                    reuseNode true
                }
            }
            steps {
                withCredentials([
                    string(credentialsId: 'open-ai-token', variable: 'OPEN_API_KEY'),
                ]) {
                    sh 'mvn -B formatter:validate'
                    sh 'mvn -B -U -fae clean package'
                    sh 'mvn cyclonedx:makeAggregateBom'
                }
            }
        }

        stage('mvn publish sbom') {
            agent {
                docker {
                    image 'harbor.explore.de/explore/maven:3.9.7-eclipse-temurin-21'
                    args '-v maven-repository:/usr/share/repository'
                    reuseNode true
                }
            }
            when {
                anyOf {
                    branch 'main'
                    tag '*'
                }
            }

            steps {
                script {
                    publishMavenSBOM('target/bom.xml')
                }
            }
        }

        stage('SonarQube Analysis') {
            agent {
                docker {
                    image 'harbor.explore.de/explore/maven:3.9.7-eclipse-temurin-21'
                    args '-u jenkins -v /var/run/docker.sock:/var/run/docker.sock'
                    reuseNode true
                }
            }
            when {
                anyOf {
                    branch 'main'
                    tag '*'
                }
           }
           steps {
                withSonarQubeEnv('sonarqube') {
                    withCredentials([
                        string(credentialsId: 'open-ai-token', variable: 'OPEN_API_KEY'),
                    ]) {
                        sh "mvn -B -fae clean verify sonar:sonar -Dsonar.projectKey=exp_explore_g-lora_RAGE4j_AZJrgXv66ukUdhQUUnJS -Dsonar.projectName='RAGE4j' -Pcoverage"
                    }
                }
           }
        }
    }

    post {
        always {
            cleanWs()
            script {
                syncGitlabPipeline(currentBuild)
            }
        }
    }
}
