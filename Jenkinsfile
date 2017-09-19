@Library('jenkins-pipeline')

properties([disableConcurrentBuilds(), [$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '7', artifactNumToKeepStr: '20', daysToKeepStr: '20', numToKeepStr: '20']]]);

branch.execute()






