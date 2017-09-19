
@Library('jenkins-pipeline')
node('android-test') {
    step([$class: 'WsCleanup', notFailBuild: true])
    stage('Checkout') {
        // Check out code
        sh 'env | sort'

        properties([disableConcurrentBuilds(), [$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '7', artifactNumToKeepStr: '20', daysToKeepStr: '20', numToKeepStr: '20']]]);

        branch.execute()


    }
}


