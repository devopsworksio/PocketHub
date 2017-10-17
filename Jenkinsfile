def configuration

node ('android-test') {
    step([$class: 'WsCleanup', notFailBuild: true])
    stage('Checkout') {
        // Check out code
        //properties([disableConcurrentBuilds()])
        properties([disableConcurrentBuilds(),[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '7', artifactNumToKeepStr: '20', daysToKeepStr: '20', numToKeepStr: '20']]]);
        def scmVars = checkout scm
        env.GIT_COMMIT=scmVars.GIT_COMMIT
        println '>>> Git Commit HASH' + env.GIT_COMMIT  + '<<<'

        configuration = load 'scripts/jenkins/main/branch.groovy'
        checkout = load 'scripts/jenkins/steps/checkout.groovy'
        common = load 'scripts/jenkins/lib/common.groovy'
        gitStatus = load 'scripts/jenkins/lib/git-status.groovy'
        bupa = load 'scripts/jenkins/steps/bupa.groovy'

        def counter = common.buildCounter()
        println counter.number


        // Post-checkout prep
        checkout.exportGitEnvVars()

        sh 'env | sort'

        // stash the entire checkout including .git dir
        stash(name: 'sources', useDefaultExcludes: false)
        stash(name: 'pipeline', includes: 'scripts/jenkins/**')
        gitStatus.reportGitStatus('Jenkins Job', 'Running job...', 'PENDING')



        step([$class: 'WsCleanup', notFailBuild: true])
    }
}

configuration.execute()
